package chatbot.serviceimpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
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

import chatbot.entity.ChatMessage;
import chatbot.respository.ChatMessageRepository;
import chatbot.service.ChatbotMessageService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ChatMessageServiceImpl implements ChatbotMessageService {

	 private final ChatMessageRepository chatMessageRepository;
	    private final RestTemplate restTemplate;

	    @Autowired
	    public ChatMessageServiceImpl(ChatMessageRepository chatMessageRepository, RestTemplate restTemplate) {
	        this.chatMessageRepository = chatMessageRepository;
	        this.restTemplate = restTemplate;
	    }
    
	@Value("${openai.api.key}")
	private String openaiapiKey;
	
	private final String apiUrl = "https://api.openai.com/v1/chat/completions";

	@Value("${huggingface.api.key}")
	private String huggingfaceapiKey;
	private final String apiUrll = "https://api-inference.huggingface.co/models/CompVis/stable-diffusion-v1-4";

	
	private final String apiUrls = "https://api.openai.com/v1/images/generations";

	@Value("${chatbot.api.key}")
	private String chatbotapiKey;
	private static final String AI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

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
		chatMessageRepository.save(user); // Save updated credits

		int maxRetries = 3;
		int retryDelay = 2000;

		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			try {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);

				String requestBody = "{ \"contents\": [{ \"parts\": [{ \"text\": \"" + userMessage + "\" }] }] }";
				HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

				ResponseEntity<String> response = restTemplate.exchange(AI_API_URL + "?key=" + chatbotapiKey,
						HttpMethod.POST, request, String.class);

				if (response.getStatusCode() == HttpStatus.OK) {
					ObjectMapper objectMapper = new ObjectMapper();
					JsonNode jsonNode = objectMapper.readTree(response.getBody());

					String botResponse = jsonNode.path("candidates").get(0).path("content").path("parts").get(0)
							.path("text").asText();

					// Store chat message in database
					ChatMessage chatMessage = ChatMessage.builder().userId(userId).userSearch(List.of(
							ChatMessage.UserSearch.builder().userMessage(userMessage).botResponse(botResponse).build()))
							.build();

//	                chatMessageRepository.save(chatMessage);

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
	public List<ChatMessage> getHistory() {
		List<ChatMessage> response = chatMessageRepository.findAll();
		return response;
	}

	@Override
	public List<ChatMessage> getByUserId(String userId) {
		ObjectId objectId = new ObjectId(userId);
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
	public ResponseEntity<Map<String, Object>> chatWithGPT(String message, String userId) {

		Optional<ChatMessage> userOptional = chatMessageRepository.findById(userId);
		if (userOptional.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Collections.singletonMap("message", "User not found"));
		}

		ChatMessage user = userOptional.get();

		double costPerMessage = 0.25;
		if (user.getCredits() < costPerMessage) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Collections.singletonMap("message", "Insufficient credits"));
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(openaiapiKey);

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("model", "gpt-4");
		requestBody.put("messages", List.of(Map.of("role", "system", "content", "You are a helpful assistant."),
				Map.of("role", "user", "content", message)));
		requestBody.put("max_tokens", 200);

		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

		try {
			ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

			if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
				List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");

				if (choices != null && !choices.isEmpty()) {
					Map<String, Object> firstChoice = choices.get(0);
					Map<String, String> assistantMessage = (Map<String, String>) firstChoice.get("message");
					String assistantResponse = assistantMessage.get("content");

					user.setCredits(user.getCredits() - costPerMessage);
					chatMessageRepository.save(user);

					return ResponseEntity.ok(Collections.singletonMap("response", assistantResponse));
				}
			}
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Collections.singletonMap("message", "No response from OpenAI."));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", "Failed to connect to OpenAI: " + e.getMessage()));
		}
	}

	@Override
	public ResponseEntity<Map<String, Object>> generateImage(String prompt, String userId) {

		Optional<ChatMessage> userOpt = chatMessageRepository.findById(userId);
		if (userOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Collections.singletonMap("message", "User not found"));
		}

		ChatMessage user = userOpt.get();

		double costPerImage = 0.25;
		if (user.getCredits() < costPerImage) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Collections.singletonMap("message", "Insufficient credits"));
		}

		user.setCredits(user.getCredits() - costPerImage);
		chatMessageRepository.save(user);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(openaiapiKey);

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("model", "dall-e-3");
		requestBody.put("prompt", prompt);
		requestBody.put("n", 1);
		requestBody.put("size", "1024x1024");

		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

		try {
			ResponseEntity<Map> response = restTemplate.exchange(apiUrls, HttpMethod.POST, entity, Map.class);

			if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
				List<Map<String, String>> data = (List<Map<String, String>>) response.getBody().get("data");

				if (data != null && !data.isEmpty()) {
					Map<String, Object> responseBody = new HashMap<>();

					responseBody.put("imageUrl", data.get(0).get("url"));
					return ResponseEntity.ok(responseBody);
				} else {
					return ResponseEntity.status(HttpStatus.BAD_REQUEST)
							.body(Collections.singletonMap("message", "No image generated"));
				}
			} else {
				return ResponseEntity.status(response.getStatusCode())
						.body(Collections.singletonMap("message", "Error generating image"));
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", "Failed to connect to OpenAI: " + e.getMessage()));
		}
	}

	@Override
	public ResponseEntity generateImages(ObjectId userId, String prompt) {
		Optional<ChatMessage> optionalUser = chatMessageRepository.findById(userId);

		if (optionalUser.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("status", "error", "message", "User not found."));
		}

		ChatMessage user = optionalUser.get();

		if (user.getCredits() < 0.25) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("status", "error", "message", "Insufficient credits. Please top up your balance."));
		}

		user.setCredits(user.getCredits() - 0.25);
		chatMessageRepository.save(user);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(huggingfaceapiKey);

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("inputs", prompt);

		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

		try {
			ResponseEntity<byte[]> response = restTemplate.exchange(apiUrll, HttpMethod.POST, entity, byte[].class);
			if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
				HttpHeaders imageHeaders = new HttpHeaders();
				imageHeaders.setContentType(MediaType.IMAGE_PNG);

				return ResponseEntity.ok().headers(imageHeaders).body(response.getBody());
			} else {
				return ResponseEntity.status(response.getStatusCode()).body(Map.of("status", "error", "message",
						"Error generating image.", "remainingCredits", user.getCredits()));
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", "error", "message",
					"Failed to generate image", "remainingCredits", user.getCredits()

			));
		}
	}

}
