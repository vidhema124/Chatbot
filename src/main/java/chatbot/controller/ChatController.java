package chatbot.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import chatbot.entity.ChatEntity;
import chatbot.service.ChatService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = { "http://localhost:3000", "https://vchatai.netlify.app" })
//@RequestMapping("/api")
public class ChatController {
	ChatService chatService;
	private final PasswordEncoder passwordEncoder;

	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> requestBody) {
		String email = requestBody.get("email");
		String password = requestBody.get("password");

		Map<String, Object> response = chatService.login(email, password);

		if ((int) response.get("status") == 400) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		return ResponseEntity.ok(response);
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<Map<String, String>> updateProductById(@PathVariable String id,
	                                                             @RequestBody ChatEntity chatEntity) {
	    Optional<ChatEntity> existingChatOpt = chatService.findById(id);

	    if (existingChatOpt.isPresent()) {
	        ChatEntity existingChat = existingChatOpt.get();

	       
	        if (chatEntity.getName() != null) {
	            existingChat.setName(chatEntity.getName());
	        }
	        if (chatEntity.getEmail() != null) {
	            existingChat.setEmail(chatEntity.getEmail());
	        }
	        if (chatEntity.getImage() != null) {
	            existingChat.setImage(chatEntity.getImage());
	        }
	        if (chatEntity.getPassword() != null && !chatEntity.getPassword().isEmpty()) {
	            
	            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	            existingChat.setPassword(passwordEncoder.encode(chatEntity.getPassword()));
	        }

	        chatService.chatUpdate(existingChat);

	        Map<String, String> response = new HashMap<>();
	        response.put("message", "User details updated successfully");
	        return ResponseEntity.ok(response);
	    } else {
	        Map<String, String> errorResponse = new HashMap<>();
	        errorResponse.put("error", "User not found");
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	    }
	}

	@GetMapping("/verify-link")
	public ResponseEntity<?> verifyUser(@RequestParam String token) {
		boolean verified = chatService.verifyUser(token);
		if (verified) {
			return ResponseEntity.ok("Email verified successfully!");
		}
		return ResponseEntity.badRequest().body("Invalid or expired token!");
	}

	@PostMapping("/signup-verification")
	public ResponseEntity<Map<String, Object>> signUp(@RequestBody ChatEntity chatEntity) {
		Map<String, Object> response = new HashMap<>();

		if (!isValidEmail(chatEntity.getEmail())) {
			response.put("message", "Invalid email format. Please check your email format.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		String responseMessage = chatService.signUp(chatEntity);
		if ("Email already exists!".equals(responseMessage)) {
			response.put("message", "Email already exists!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		ChatEntity registeredUser = chatService.registerUser(chatEntity);
		if (registeredUser != null) {
			response.put("message", "User registered successfully! Verification email sent.");
			response.put("userId", registeredUser.getId());
			return ResponseEntity.ok(response);
		} else {
			response.put("message", "Error sending verification email.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	private boolean isValidEmail(String email) {
		String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
		Pattern pattern = Pattern.compile(emailRegex);
		return pattern.matcher(email).matches();
	}

	@DeleteMapping("/user-delete/{id}")
	public ResponseEntity<Map<String, Object>> deleteChatEntity(@PathVariable String id) {
		return chatService.deleteChatEntity(id);
	}

	@GetMapping("/user-by-email")
	public ResponseEntity<Map<String, Object>> getUserByEmail(@RequestParam String email) {
		return chatService.getUserByEmail(email);
	}

	@PostMapping("/create-user")
	public ResponseEntity<Map<String, Object>> createUser(@RequestBody ChatEntity chatEntity) {
		return chatService.createUser(chatEntity);
	}
	
	@PostMapping("/user-login")
	public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> requestBody) {
	    String email = requestBody.get("email");
	    String password = requestBody.get("password");

	    Map<String, Object> response = new HashMap<>();

	    if (email == null || email.trim().isEmpty()) {
	        response.put("message", "Email is required");
	        response.put("status", 400);
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	    }
	    if (password == null || password.trim().isEmpty()) {
	        response.put("message", "Password is required");
	        response.put("status", 400);
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	    }
	    response = chatService.userLogin(email, password);

	    if ((int) response.get("status") == 400) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	    }

	    return ResponseEntity.ok(response);
	}

	
	
}