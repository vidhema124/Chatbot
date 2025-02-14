package chatbot.service;

import java.util.List;

import chatbot.entity.ChatMessage;

public interface ChatbotService {
    String getChatbotResponse(String query);
    List<ChatMessage> getChatHistory();
//	void insertChatMessage(ChatMessage chatMessage);
//	void saveChatMessage(ChatMessage chatMessage);
	void insertChatMessage(ChatMessage chatMessage);
}
