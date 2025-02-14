package chatbot.service;

import java.util.List;

import chatbot.entity.ChatMessage;

public interface ChatbotMessageService {
    String getChatResponse(String userMessage);

	List<ChatMessage> getHistory();
}