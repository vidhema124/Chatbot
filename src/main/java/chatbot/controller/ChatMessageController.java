package chatbot.controller;

import chatbot.entity.ChatMessage;
import chatbot.service.ChatbotMessageService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;


@RestController
@RequestMapping("/chatbot")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ChatMessageController {

	 ChatbotMessageService chatbotService;

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

	@GetMapping("/get-by-userid/{userId}")
	public ResponseEntity<Map<String, Object>> getChatMessagesByUserId(@PathVariable String userId) {
	    List<ChatMessage> chatMessages = chatbotService.getByUserId(userId);

	    if (chatMessages.isEmpty()) {
	        Map<String, Object> response = new HashMap<>();
	        response.put("message", "No chats found for this user");
	        return ResponseEntity.ok(response); // Status 200
	    }

	    return ResponseEntity.ok(Map.of("chatMessages", chatMessages));
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
	        if (chatMessage.getUserId() != null) {
	            try {
	             
	                chatMessage.setUserId(new ObjectId(chatMessage.getUserId().toString()));
	            } catch (IllegalArgumentException e) {
	                return ResponseEntity.badRequest().body(null); 
	            }
	        }
	        ChatMessage savedChat = chatbotService.saveChatMessage(chatMessage);
	        return ResponseEntity.ok(savedChat);
	    }
	    
	    @DeleteMapping("/delete-all")
	    public ResponseEntity<Map<String, Object>> deleteAllChatMessages() {
	        boolean deleted = chatbotService.deleteAll();

	        Map<String, Object> response = new HashMap<>();
	        if (deleted) {
	            response.put("message", "All chat messages deleted successfully.");
	            return ResponseEntity.ok(response);
	        } else {
	            response.put("message", "No chat messages found to delete.");
	            return ResponseEntity.status(404).body(response);
	        }
	    }
	    
	    @GetMapping("/get-By/{id}")
		public ResponseEntity<ChatMessage> getChatMessageById(@PathVariable String id) {
			Optional<ChatMessage> chatMessage = chatbotService.getById(id);
			return chatMessage.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(404).body(null));
		}
	    
	    @DeleteMapping("/delete-ByUserId/{userId}")
	    public ResponseEntity<Map<String, Object>> deleteByUserId(@PathVariable String userId) {
	        boolean deleted = chatbotService.deleteByUserId(userId);

	        Map<String, Object> response = new HashMap<>();
	        if (deleted) {
	            response.put("message", "Chat messages deleted successfully for userId " + userId);
	            return ResponseEntity.ok(response);
	        } else {
	            response.put("message", "No chat messages found for userId " + userId);
	            return ResponseEntity.status(404).body(response);
	        }
	    }
	   
	}