package chatbot.serviceimpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import chatbot.config.OpenAIConfig;
import chatbot.entity.ChatMessage;
import chatbot.respository.ChatMessageRepository;
import chatbot.service.ChatbotMessageService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatbotMessageService {

	 ChatMessageRepository chatMessageRepository;

	 private final OpenAIConfig openAIConfig;
	    private final RestTemplate restTemplate = new RestTemplate();

	    @Override
	    public String getChatResponse(String userMessage) {
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        headers.setBearerAuth(openAIConfig.getKey());

	        Map<String, Object> requestBody = new HashMap<>();
	        requestBody.put("model", "gpt-3.5-turbo");
	        requestBody.put("messages", List.of(
//	                Map.of("role", "system", "content", "You are a helpful assistant."),
	                Map.of("role", "user", "content", userMessage)
	        ));
	        requestBody.put("temperature", 0.7);

	        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
	        ResponseEntity<Map> response = restTemplate.exchange(openAIConfig.getUrl(), HttpMethod.POST, entity, Map.class);

	        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
	        return choices.get(0).get("message").toString();
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

	
	
	
	
}