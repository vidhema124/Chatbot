package chatbot.service;

import java.util.List;
import java.util.Optional;

import chatbot.entity.ChatMessage;


public interface ChatbotMessageService {
	List<ChatMessage> getHistory();
	Optional<ChatMessage> getById(String id);
	boolean deleteById(String id);
	String getChatResponse(String userMessage);
	Optional<ChatMessage> updateById(String id, ChatMessage updatedMessage);
	ChatMessage saveChatMessage(ChatMessage chatMessage);
	boolean deleteAll();
}