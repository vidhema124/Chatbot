package chatbot.controller;

import chatbot.entity.ChatEntity;
import chatbot.service.ChatService;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password) {
        String response = chatService.login(email, password);
        return ResponseEntity.ok(response);
    }
}
