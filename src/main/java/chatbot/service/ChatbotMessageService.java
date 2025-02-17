package chatbot.service;

import java.util.List;
import java.util.Optional;

import chatbot.entity.ChatMessage;

public interface ChatbotMessageService {
    String getChatResponse(String userMessage);

	List<ChatMessage> getHistory();

	Optional<ChatMessage> getById(String id);

	Optional<ChatMessage> updateById(String id, ChatMessage updatedMessage);

	boolean deleteById(String id);
}