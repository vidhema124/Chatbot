package chatbot.controller;


import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import chatbot.entity.ChatEntity;
import chatbot.entity.ChatMessage;
import chatbot.entity.PaymentEntity;
import chatbot.respository.ChatMessageRepository;
import chatbot.respository.ChatRepository;
import chatbot.respository.PaymentRepository;
import chatbot.service.ChatbotMessageService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/chatbot")
@CrossOrigin(origins = { "http://localhost:3000", "https://vchatai.netlify.app" })
public class ChatMessageController {

	private final ChatRepository chatRepository;
	private final ChatbotMessageService chatbotService;
	private final ChatMessageRepository chatMessageRepository;
	@Autowired
	private PaymentRepository paymentRepository;


	@Autowired
	public ChatMessageController(ChatbotMessageService chatbotService, ChatMessageRepository chatMessageRepository ,ChatRepository chatRepository) {
		this.chatbotService = chatbotService;
		this.chatMessageRepository = chatMessageRepository;
		this.chatRepository=chatRepository;
	}
	@GetMapping("/openai")
	public ResponseEntity<?> sendMessage(@RequestParam String userId, @RequestParam String message) {
	    try {
	       
	        ObjectId objectId = new ObjectId(userId);
	        Optional<ChatEntity> userOptional = chatRepository.findById(objectId.toString());
	        if (userOptional.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
	        }

	        ChatEntity user = userOptional.get();
	        if (user.getCredits() < 0.25) {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient credits");
	        }
	        user.setCredits(user.getCredits() - 0.25);
	        chatRepository.save(user);
	        String botResponse = chatbotService.getChatResponse(message);
	        Map<String, Object> response = new HashMap<>();
	        response.put("botResponse", botResponse);
	        response.put("remainingCredits", user.getCredits());
	        response.put("ModelType", "OpenAI");

	        return ResponseEntity.ok(response);

	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user ID format");
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("An error occurred while processing the request");
	    }
	}

	
	
	@GetMapping("/search-history")
	public ResponseEntity<List<ChatMessage>> historyChake() {
		List<ChatMessage> chatHistory = chatbotService.getHistory();
		return ResponseEntity.ok(chatHistory);
	}


	@GetMapping("/get-by-userid/{userId}")
	public ResponseEntity<Map<String, Object>> getChatMessagesByUserId(@PathVariable String userId) {

	    List<ChatMessage> chatMessages = chatbotService.getByUserId(userId);

	    ChatEntity chatEntity = chatRepository.findById(userId).orElse(null);
	    
	    double credits = (chatEntity != null) ? chatEntity.getCredits() : 0.0;
	    String planId = (chatEntity != null) ? chatEntity.getPlanId() : null;

	    Map<String, Object> response = new HashMap<>();
	    response.put("credits", credits);
	    response.put("planId", planId);

	    if (chatMessages.isEmpty()) {
	        response.put("message", "No chats found for this user");
	        return ResponseEntity.ok(response);
	    }

	    response.put("chatMessages", chatMessages);
	    return ResponseEntity.ok(response);
	}


	@PutMapping("/update-by/{id}")
	public ResponseEntity<Map<String, Object>> updateChatMessageById(@PathVariable String id,
			@RequestBody ChatMessage updatedMessage) {
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

    @GetMapping("kkkkkkk")
    public ResponseEntity<Map<String, Object>> chat(@RequestParam ObjectId userId, @RequestParam String message) {
        String decodedMessage = URLDecoder.decode(message, StandardCharsets.UTF_8);
        return chatbotService.getChatResponse(userId, decodedMessage);
    }

    @GetMapping("/generate-image")
    public ResponseEntity<?> generateImage(@RequestParam String userId, @RequestParam String prompt) {

        try {
            
            ObjectId objectId = new ObjectId(userId);
            Optional<ChatEntity> userOptional = chatRepository.findById(objectId.toString());
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            ChatEntity user = userOptional.get();
            if (user.getCredits() < 0.25) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient credits");
            }
            user.setCredits(user.getCredits() - 0.25);
            chatRepository.save(user);
            String imageUrl = chatbotService.getImageResponse(prompt);

            Map<String, Object> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("remainingCredits", user.getCredits());
            response.put("ModelType", "OpenAI");
            

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user ID format");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while generating the image");
        }
    }
    @PostMapping("/analyze-pdf")
    public ResponseEntity<Map<String, Object>> analyzePDF(@RequestParam("file") MultipartFile file,
                                                          @RequestParam("userId") String userId) {
        try {
            Map<String, Object> response = chatbotService.analyzePDF(file, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error analyzing PDF: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
  
	  
	    
	    @PostMapping("/search") 
	    public ResponseEntity<Map<String, Object>> uploadFiles(
	            @RequestParam("userId") ObjectId userId,
	            @RequestParam(value = "file", required = false) MultipartFile file,  // Make 'file' optional
	            @RequestParam(value = "message", required = false) String message) {
	        return chatbotService.handleFileUpload1(userId, file, message);
	    }

}
