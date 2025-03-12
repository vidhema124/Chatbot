package chatbot.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.*;


@SpringBootApplication
@RestController
@RequestMapping("/huggingface")
@CrossOrigin(origins = {"http://localhost:3000", "https://vchatai.netlify.app"})
public class ImageGeneratorApplication {

    @Value("${huggingface.api.key}")
    private String apiKey;

    private final String apiUrl = "https://api-inference.huggingface.co/models/CompVis/stable-diffusion-v1-4";
    private final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        SpringApplication.run(ImageGeneratorApplication.class, args);
    }

    @GetMapping("/generate")
    public ResponseEntity<?> generateImage(@RequestParam String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("inputs", prompt);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, byte[].class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(response.getBody());
            } else {
                return ResponseEntity.status(response.getStatusCode()).body("Error generating image.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate image: " + e.getMessage());
        }
    }
}
