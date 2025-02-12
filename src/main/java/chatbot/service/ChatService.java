package chatbot.service;

import java.util.List;

import chatbot.entity.ChatEntity;

public interface ChatService {
    String signUp(ChatEntity chatEntity);
    String login(String email, String password);
    List<ChatEntity> getAllChats();
}
