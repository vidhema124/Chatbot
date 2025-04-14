package chatbot.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

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
	String getChatResponse(String userMessage);
	
	ResponseEntity<Map<String, Object>> getChatResponse(ObjectId userId, String decodedMessage);
	String getImageResponse(String prompt);
	Map<String, Object> analyzePDF(MultipartFile file, String userId);
	
	ResponseEntity<Map<String, Object>> handleFileUpload1(ObjectId userId, MultipartFile file, String message);
	
}