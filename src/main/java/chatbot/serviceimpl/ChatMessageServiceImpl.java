package chatbot.serviceimpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import chatbot.config.GeminiConfig;
import chatbot.config.OpenAIConfig;
import chatbot.entity.ChatMessage;
import chatbot.respository.ChatMessageRepository;
import chatbot.service.ChatbotMessageService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatbotMessageService {

	private final ChatMessageRepository chatMessageRepository;
	private final OpenAIConfig openAIConfig;
	private final GeminiConfig geminiConfig;
	private final RestTemplate restTemplate;

	@Override
	public String getChatResponse(String userMessage) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBearerAuth(openAIConfig.getKey());

			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("model", "gpt-3.5-turbo");
			requestBody.put("messages", List.of(Map.of("role", "user", "content", userMessage)));
			requestBody.put("temperature", 0.7);

			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

			// ✅ Use getChatUrl() instead of getUrl()
			ResponseEntity<Map> response = restTemplate.exchange(openAIConfig.getChatUrl(), HttpMethod.POST, entity,
					Map.class);

			if (response.getBody() == null || !response.getBody().containsKey("choices")) {
				return "Error: Invalid response from OpenAI";
			}

			List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
			Map<String, Object> firstChoice = (Map<String, Object>) choices.get(0).get("message");

			return firstChoice.get("content").toString();

		} catch (Exception e) {
			e.printStackTrace();
			return "Error: Unable to process request";
		}
	}

	@Override
	public List<ChatMessage> getHistory() {
		List<ChatMessage> response = chatMessageRepository.findAll();
		return response;
	}

	@Override
	public List<ChatMessage> getByUserId(String userId) {
		ObjectId objectId = new ObjectId(userId); // Convert String to ObjectId
		return chatMessageRepository.findByUserId(objectId);
	}

	@Override
	public Optional<ChatMessage> updateById(String id, ChatMessage updatedMessage) {
		return chatMessageRepository.findById(id).map(existingMessage -> {
			if (updatedMessage.getUserSearch() != null && !updatedMessage.getUserSearch().isEmpty()) {
				List<ChatMessage.UserSearch> mergedUserSearch = new ArrayList<>(existingMessage.getUserSearch());
				mergedUserSearch.addAll(updatedMessage.getUserSearch());

				existingMessage.setUserSearch(mergedUserSearch);
			}
			return chatMessageRepository.save(existingMessage);
		});
	}

	@Override
	public boolean deleteById(String id) {
		if (chatMessageRepository.existsById(id)) {
			chatMessageRepository.deleteById(id);
			return true;
		}
		return false;
	}

	@Override
	public ChatMessage saveChatMessage(ChatMessage chatMessage) {
		return chatMessageRepository.save(chatMessage);
	}

	@Override
	public boolean deleteAll() {
		if (chatMessageRepository.count() > 0) {
			chatMessageRepository.deleteAll();
			return true;
		}
		return false;
	}

	@Override
	public Optional<ChatMessage> getById(String id) {
		return chatMessageRepository.findById(id);
	}

	@Override
	public boolean deleteByUserId(String userId) {
		try {
			ObjectId objectId = new ObjectId(userId);
			long count = chatMessageRepository.countByUserId(objectId);

			if (count > 0) {
				chatMessageRepository.deleteByUserId(objectId);
				return true;
			}
		} catch (IllegalArgumentException e) {
			System.out.println("Invalid ObjectId format: " + userId);
		}
		return false;
	}

	@Override
	public ResponseEntity<Map<String, Object>> getChatResponse(ObjectId userId, String userMessage) {
		Map<String, Object> responseMap = new HashMap<>();
		Optional<ChatMessage> optionalUser = chatMessageRepository.findById(userId);
		if (optionalUser.isEmpty()) {
			responseMap.put("message", "User not found.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMap);
		}

		ChatMessage user = optionalUser.get();
		if (user.getCredits() < 0.25) {
			responseMap.put("message", "Insufficient credits. Please top up your balance.");
			return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(responseMap);
		}

		user.setCredits(user.getCredits() - 0.25);
		chatMessageRepository.save(user);

		int maxRetries = 3;
		int retryDelay = 2000;
		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			try {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);

				String requestBody = "{ \"contents\": [{ \"parts\": [{ \"text\": \"" + userMessage + "\" }] }] }";
				HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

				String apiUrl = geminiConfig.getUrl() + "?key=" + geminiConfig.getKey();
				ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);

				if (response.getStatusCode() == HttpStatus.OK) {
					ObjectMapper objectMapper = new ObjectMapper();
					JsonNode jsonNode = objectMapper.readTree(response.getBody());
					String botResponse = jsonNode.path("candidates").get(0).path("content").path("parts").get(0)
							.path("text").asText();

					responseMap.put("botResponse", botResponse);
					return ResponseEntity.ok(responseMap);
				}
			} catch (Exception e) {
				if (attempt < maxRetries) {
					try {
						TimeUnit.MILLISECONDS.sleep(retryDelay);
						retryDelay *= 2;
					} catch (InterruptedException ignored) {
					}
				} else {
					responseMap.put("status", "error");
					responseMap.put("message", "Sorry, I am currently unavailable. Please try again later.");
					return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(responseMap);
				}
			}
		}

		responseMap.put("status", "error");
		responseMap.put("message", "Failed to fetch response from AI.");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
	}

	@Override
	public String getImageResponse(String prompt) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBearerAuth(openAIConfig.getKey());

			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("model", "dall-e-3"); // OpenAI's latest model
			requestBody.put("prompt", prompt);
			requestBody.put("n", 1); // Number of images
			requestBody.put("size", "1024x1024"); // Image size

			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

			// Correct URL for image generation
			ResponseEntity<Map> response = restTemplate.exchange(openAIConfig.getImageUrl(), HttpMethod.POST, entity,
					Map.class);

			if (response.getBody() == null || !response.getBody().containsKey("data")) {
				return "Error: Invalid response from OpenAI";
			}

			List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
			return data.get(0).get("url").toString(); // Return the generated image URL

		} catch (Exception e) {
			e.printStackTrace();
			return "Error: Unable to generate image";
		}
	}

}