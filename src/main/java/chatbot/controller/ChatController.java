package chatbot.controller;

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
    public ResponseEntity<String> signUp(@RequestBody ChatEntity chatEntity) {
        String response = chatService.signUp(chatEntity);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestParam String email, @RequestParam String password) {
        Map<String, Object> response = chatService.login(email, password);
        return ResponseEntity.ok(response);
    }


   
}
