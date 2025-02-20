package chatbot.serviceimpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import chatbot.entity.ChatMessage;
import chatbot.respository.ChatMessageRepository;
import chatbot.service.ChatbotMessageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatbotMessageService {

	private final RestTemplate restTemplate;
	private final ChatMessageRepository chatMessageRepository;

	@Value("${chatbot.api.key}")
	private String apiKey;

	private static final String AI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

	@Override
	public String getChatResponse(String userMessage) {
	    int maxRetries = 3;
	    int retryDelay = 2000;

	    for (int attempt = 1; attempt <= maxRetries; attempt++) {
	        try {
	            HttpHeaders headers = new HttpHeaders();
	            headers.setContentType(MediaType.APPLICATION_JSON);

	            String requestBody = "{ \"contents\": [{ \"parts\": [{ \"text\": \"" + userMessage + "\" }] }] }";
	            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

	            ResponseEntity<String> response = restTemplate.exchange(
	                AI_API_URL + "?key=" + apiKey, 
	                HttpMethod.POST, 
	                request, 
	                String.class
	            );

	            if (response.getStatusCode() == HttpStatus.OK) {
	                ObjectMapper objectMapper = new ObjectMapper();
	                JsonNode jsonNode = objectMapper.readTree(response.getBody());

	                String botResponse = jsonNode.path("candidates")
	                        .get(0)
	                        .path("content")
	                        .path("parts")
	                        .get(0)
	                        .path("text")
	                        .asText();

	                // Correctly building the ChatMessage object
	                ChatMessage chatMessage = ChatMessage.builder()
	                        .userSearch(List.of(
	                                ChatMessage.UserSearch.builder()
	                                        .userMessage(userMessage)
	                                        .botResponse(botResponse)
	                                        .build()))
	                        .build();

//	                chatMessageRepository.save(chatMessage);
	                return botResponse;
	            }
	        } catch (Exception e) {
	            if (attempt < maxRetries) {
	                try {
	                    TimeUnit.MILLISECONDS.sleep(retryDelay);
	                    retryDelay *= 2;
	                } catch (InterruptedException ignored) {}
	            } else {
	                return "Sorry, I am currently unavailable. Please try again later.";
	            }
	        }
	    }
	    return "Failed to fetch response from AI.";
	}

	@Override
	public List<ChatMessage> getHistory() {
		List<ChatMessage> response = chatMessageRepository.findAll();
		return response;
	}

	@Override
	public Optional<ChatMessage> getById(String id) {
		return chatMessageRepository.findById(id);
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
}