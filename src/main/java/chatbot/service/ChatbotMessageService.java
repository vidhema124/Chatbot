package chatbot.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;

import chatbot.entity.ChatMessage;

public interface ChatbotMessageService {
	List<ChatMessage> getHistory();

	boolean deleteById(String id);

	Optional<ChatMessage> updateById(String id, ChatMessage updatedMessage);

	ChatMessage saveChatMessage(ChatMessage chatMessage);

	boolean deleteAll();

	List<ChatMessage> getByUserId(String userId);

	Optional<ChatMessage> getById(String id);

	boolean deleteByUserId(String userId);

	ResponseEntity<Map<String, Object>> getChatResponse(ObjectId userId, String decodedMessage);

	ResponseEntity<Map<String, Object>> chatWithGPT(String message, String userId);

	ResponseEntity<Map<String, Object>> generateImage(String prompt, String userId);

	ResponseEntity<Map<String, Object>> generateImages(ObjectId userId, String prompt);

}