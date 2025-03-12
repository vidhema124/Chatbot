//package chatbot.controller;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//import java.util.*;
//
//@SpringBootApplication
//@RestController
//@RequestMapping("/image")
//@CrossOrigin(origins = {"http://localhost:3000", "https://vchatai.netlify.app"})
//public class OpenAIImageGenerator {
//
//    @Value("${openai.api.key}")
//    private String apiKey;
//
//    private final String apiUrl = "https://api.openai.com/v1/images/generations";
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    public static void main(String[] args) {
//        SpringApplication.run(OpenAIImageGenerator.class, args);
//    }
//
//    @GetMapping("/generate")
//    public String generateImage(@RequestParam String prompt) {
//        // Set headers
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setBearerAuth(apiKey);
//
//        // Create request body
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("model", "dall-e-3");
//        requestBody.put("prompt", prompt);
//        requestBody.put("n", 1);
//        requestBody.put("size", "1024x1024");
//
//        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
//
//        // Call OpenAI API
//        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);
//
//        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//            List<Map<String, String>> data = (List<Map<String, String>>) response.getBody().get("data");
//            return data.get(0).get("url"); // Return the image URL
//        } else {
//            return "Error generating image.";
//        }
//    }
//}


package chatbot.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import java.util.*;
import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;

@SpringBootApplication
@RestController
@RequestMapping("/image")
@CrossOrigin(origins = {"http://localhost:3000", "https://vchatai.netlify.app"})
public class OpenAIImageGenerator {

    @Value("${openai.api.key}")
    private String apiKey;

    private final String apiUrl = "https://api.openai.com/v1/images/generations";
    private final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        SpringApplication.run(OpenAIImageGenerator.class, args);
    }

    @GetMapping("/generate")
    public ResponseEntity<byte[]> generateImage(@RequestParam String prompt) {
        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // Create request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "dall-e-3");
        requestBody.put("prompt", prompt);
        requestBody.put("n", 1);
        requestBody.put("size", "1024x1024");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
           
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, String>> data = (List<Map<String, String>>) response.getBody().get("data");
                String imageUrl = data.get(0).get("url"); // Get image URL

             
                byte[] imageBytes = downloadImage(imageUrl);

                if (imageBytes != null) {
                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_PNG)
                            .body(imageBytes);
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(null);
                }
            } else {
                return ResponseEntity.status(response.getStatusCode()).body(null);
            }
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

   
    private byte[] downloadImage(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (InputStream inputStream = connection.getInputStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                return outputStream.toByteArray();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

