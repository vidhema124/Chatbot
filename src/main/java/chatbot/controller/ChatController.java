package chatbot.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import chatbot.entity.ChatEntity;
import chatbot.service.ChatService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
//@RequestMapping("/api")
public class ChatController {
	ChatService chatService;

//	@PostMapping("/signup")
//	public ResponseEntity<Map<String, Object>> signUp(@RequestBody ChatEntity chatEntity) {
//	    Map<String, Object> response = new HashMap<>();
//	    if (!isValidEmail(chatEntity.getEmail())) {
//	        response.put("message", "Invalid email format. Please check your email format.");
//	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//	    }
//	    String responseMessage = chatService.signUp(chatEntity);
//	    if ("Email already exists!".equals(responseMessage)) {
//	        response.put("message", "Email already exists!");
//	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//	    } else {
//	        response.put("message", "User registered successfully!");
//	        return ResponseEntity.ok(response); 
//	    }
//	}
//
//	private boolean isValidEmail(String email) {
//	    String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
//	    Pattern pattern = Pattern.compile(emailRegex);
//	    return pattern.matcher(email).matches();
//	}

	
	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> requestBody) {
	    String email = requestBody.get("email");
	    String password = requestBody.get("password");
	    Map<String, Object> response = chatService.login(email, password);
	    if (response.containsKey("status") && (int) response.get("status") == 400) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	    }
	    return ResponseEntity.ok(response);
	}


	@PutMapping("/update/{id}")
	public ResponseEntity<String> updateChatEntity(@PathVariable String id, @RequestBody ChatEntity updatedChatEntity) {
		String response = chatService.updateUser(id, updatedChatEntity);
		return ResponseEntity.ok(response);
	}


//	@PostMapping("/Verificationlink-send")
//	public ResponseEntity<Map<String, Object>> registerUser(@RequestBody ChatEntity user) {
//	    Map<String, Object> response = new HashMap<>();
//	    ChatEntity registeredUser = chatService.registerUser(user);
//
//	    if (registeredUser != null) {
//	        response.put("message", "Verification email sent to: " + registeredUser.getEmail());
//	        return ResponseEntity.ok(response);
//	    } else {
//	        response.put("message", "Email does not exist. Please check your email and password.");
//	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//	    }
//	}

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
}