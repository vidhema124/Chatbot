package chatbot.service;

import java.util.List;
import java.util.Map;

import chatbot.entity.ChatEntity;

public interface ChatService {
    String signUp(ChatEntity chatEntity);
    Map<String, Object> login(String email, String password);
    List<ChatEntity> getAllChats();
//	Users userLogin(Users userEntity);
}
