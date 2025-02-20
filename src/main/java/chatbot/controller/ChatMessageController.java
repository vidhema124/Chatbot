package chatbot.controller;

import chatbot.entity.ChatMessage;
import chatbot.service.ChatbotMessageService;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;


@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ChatMessageController {

	private final ChatbotMessageService chatbotService;

//	@GetMapping("/search")
//	public String chat(@RequestParam String message) {
//		return chatbotService.getChatResponse(message);
//	}
	
	@GetMapping("/search")
	public String chat(@RequestParam String message) {
	    // URL-decode the message parameter
	    String decodedMessage = URLDecoder.decode(message, StandardCharsets.UTF_8);
	    
	    // Now, pass the decoded message to the chatbotService
	    return chatbotService.getChatResponse(decodedMessage);
	}


	@GetMapping("/search-history")
	public ResponseEntity<List<ChatMessage>> historyChake() {
		List<ChatMessage> chatHistory = chatbotService.getHistory();
		return ResponseEntity.ok(chatHistory);
	}

	@GetMapping("/get-By/{id}")
	public ResponseEntity<ChatMessage> getChatMessageById(@PathVariable String id) {
		Optional<ChatMessage> chatMessage = chatbotService.getById(id);
		return chatMessage.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(404).body(null));
	}

	@PutMapping("/update-by/{id}")
	public ResponseEntity<Map<String, Object>> updateChatMessageById(@PathVariable String id, @RequestBody ChatMessage updatedMessage) {
	    Optional<ChatMessage> updatedChatMessage = chatbotService.updateById(id, updatedMessage);

	    Map<String, Object> response = new HashMap<>();
	    if (updatedChatMessage.isPresent()) {
	        response.put("message", "Chat message updated successfully.");
	        return ResponseEntity.ok(response);
	    } else {
	        response.put("message", "Chat message with ID " + id + " does not exist.");
	        return ResponseEntity.status(404).body(response);
	    }
	}

	    @DeleteMapping("/delete-by/{id}")
	    public ResponseEntity<Map<String, Object>> deleteChatMessageById(@PathVariable String id) {
	        boolean deleted = chatbotService.deleteById(id);

	        Map<String, Object> response = new HashMap<>();
	        if (deleted) {
	            response.put("message", "Chat message deleted successfully.");
	            return ResponseEntity.ok(response);
	        } else {
	            response.put("message", "Chat message with ID " + id + " does not exist.");
	            return ResponseEntity.status(404).body(response);
	        }
	    }
	    
	    @PostMapping("/create")
	    public ResponseEntity<ChatMessage> createChatMessage(@RequestBody ChatMessage chatMessage) {
	        ChatMessage savedMessage = chatbotService.saveChatMessage(chatMessage);
	        return ResponseEntity.ok(savedMessage);
	    }
	}
