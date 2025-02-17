package chatbot.controller;

import chatbot.entity.ChatMessage;
import chatbot.service.ChatbotMessageService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ChatMessageController {

	private final ChatbotMessageService chatbotService;

	@GetMapping("/search")
	public String chat(@RequestParam String message) {
		return chatbotService.getChatResponse(message);
	}
	
	@GetMapping("/search-history")
	public ResponseEntity<List<ChatMessage>> historyChake() {
	    List<ChatMessage> chatHistory = chatbotService.getHistory();  
	    return ResponseEntity.ok(chatHistory);
	}
}
