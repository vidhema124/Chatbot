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

	ResponseEntity<Map<String, Object>> deleteChatEntity(String id);
	ResponseEntity<Map<String, Object>> getUserByEmail(String email);
	ResponseEntity<Map<String, Object>> createUser(ChatEntity chatEntity);

}
