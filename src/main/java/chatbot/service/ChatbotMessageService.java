package chatbot.service;

import java.util.List;
import java.util.Optional;

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
	
}