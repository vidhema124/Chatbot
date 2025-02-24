package chatbot.controller;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

public class ApiTemplateClient {

    private static final String API_KEY = "368aMjQ4MDE6MjE5NDQ6emNOSFFiRmpISUtsOUExdg=";
    private static final String BASE_URL = "https://api.apitemplate.io/v1";
    private static final RestTemplate restTemplate = new RestTemplate();

    // Generate PDF
    public static void generatePdf(String templateId, Map<String, Object> data) {
        String url = BASE_URL + "/create-pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("template_id", templateId);
        requestBody.put("data", data); // Dynamic data for PDF

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        System.out.println("PDF Response: " + response.getBody());
    }

    // Generate Image
    public static void generateImage(String templateId, Map<String, Object> data) {
        String url = BASE_URL + "/create-image";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("template_id", templateId);
        requestBody.put("data", data); // Dynamic data for image

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        System.out.println("Image Response: " + response.getBody());
    }

    // Get Template by ID
    public static void getTemplateById(String templateId) {
        String url = BASE_URL + "/templates/" + templateId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", API_KEY);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        System.out.println("Template Info: " + response.getBody());
    }

    // Get All Templates
    public static void getAllTemplates() {
        String url = BASE_URL + "/templates";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", API_KEY);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        System.out.println("All Templates: " + response.getBody());
    }

    public static void main(String[] args) {
        String templateId = "your-template-id";

      
        Map<String, Object> data = new HashMap<>();
        data.put("name", "John Doe");
        data.put("date", "2025-02-21");

        generatePdf(templateId, data);
        generateImage(templateId, data);
        getTemplateById(templateId);
        getAllTemplates();
    }
}
