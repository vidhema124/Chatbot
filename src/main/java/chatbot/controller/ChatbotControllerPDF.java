package chatbot.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/chatbot")
public class ChatbotControllerPDF {
	private final RestTemplate restTemplate = new RestTemplate();
	@Value("${chatbot.api.key}")
	private String apiKey;

	private static final String AI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

	@GetMapping("/generate-pdf")
	public void generatePdf(@RequestParam String message, HttpServletResponse response) {
		String aiResponse = getChatResponse(message);

		if (aiResponse == null || aiResponse.isEmpty()) {
			aiResponse = "No response received from AI.";
		}

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=generated.pdf");

		try {
			Document document = new Document();
			OutputStream out = response.getOutputStream();
			PdfWriter.getInstance(document, out);
			document.open();
			document.add(new Paragraph("User Input: " + message));
			document.add(new Paragraph("\nAI Response:\n" + aiResponse));
			document.close();
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getChatResponse(String userMessage) {
		int maxRetries = 3;
		int retryDelay = 2000;

		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			try {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);

				String requestBody = "{ \"contents\": [{ \"parts\": [{ \"text\": \"" + userMessage + "\" }] }] }";
				HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

				ResponseEntity<String> response = restTemplate.exchange(AI_API_URL + "?key=" + apiKey, HttpMethod.POST,
						request, String.class);

				if (response.getStatusCode() == HttpStatus.OK) {
					ObjectMapper objectMapper = new ObjectMapper();
					JsonNode jsonNode = objectMapper.readTree(response.getBody());

					return jsonNode.path("candidates").get(0).path("content").path("parts").get(0).path("text")
							.asText();
				}
			} catch (Exception e) {
				if (attempt < maxRetries) {
					try {
						TimeUnit.MILLISECONDS.sleep(retryDelay);
						retryDelay *= 2;
					} catch (InterruptedException ignored) {
					}
				} else {

					return "Sorry, I am currently unavailable. Please try again later.";
				}
			}
		}
		return "Failed to fetch response from AI.";
	}
}
