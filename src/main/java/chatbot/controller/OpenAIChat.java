package chatbot.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@RestController
@RequestMapping("chat")
@CrossOrigin(origins = {"http://localhost:3000", "https://vchatai.netlify.app"})
public class OpenAIChat {

    @Value("${openai.api.key}") 
    private String apiKey;

    private final String apiUrl = "https://api.openai.com/v1/chat/completions"; 
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/search")
    public ResponseEntity<?> chatWithGPT(@RequestParam String message) { 
      
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

       
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4"); 
        requestBody.put("messages", List.of(
            Map.of("role", "system", "content", "You are a helpful assistant."),
            Map.of("role", "user", "content", message)
        ));
        requestBody.put("max_tokens", 100);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");

                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, String> assistantMessage = (Map<String, String>) firstChoice.get("message");
                    String assistantResponse = assistantMessage.get("content");

                    return ResponseEntity.ok(assistantResponse);
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No response from OpenAI.");
                }
            } else {
                return ResponseEntity.status(response.getStatusCode()).body("Error getting response from OpenAI.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to connect to OpenAI: " + e.getMessage());
        }
    }
}
