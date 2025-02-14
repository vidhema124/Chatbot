package chatbot.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import chatbot.entity.ChatEntity;
import chatbot.service.ChatService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ChatController {
	ChatService chatService;

	@PostMapping("/signup")
	public ResponseEntity<Map<String, Object>> signUp(@RequestBody ChatEntity chatEntity) {
		String responseMessage = chatService.signUp(chatEntity);
		Map<String, Object> response = new HashMap<>();
		if ("Email already exists!".equals(responseMessage)) {
			response.put("message", "Email already exists!");
		} else {
			response.put("message", "User registered successfully!");
		}
		return ResponseEntity.ok(response);
	}

	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> requestBody) {
		String email = requestBody.get("email");
		String password = requestBody.get("password");
		Map<String, Object> response = chatService.login(email, password);
		return ResponseEntity.ok(response);
	}
}
