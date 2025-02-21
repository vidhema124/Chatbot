package chatbot.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import chatbot.entity.ChatEntity;

public interface ChatService {
    String signUp(ChatEntity chatEntity);
    Map<String, Object> login(String email, String password);
    public String updateUser(String id, ChatEntity updatedChatEntity);
    ChatEntity registerUser(ChatEntity user);
    boolean verifyUser(String token);
//	ChatEntity registerUser(ChatEntity chatEntity);
	ResponseEntity<Map<String, Object>> deleteChatEntity(String id);

}
