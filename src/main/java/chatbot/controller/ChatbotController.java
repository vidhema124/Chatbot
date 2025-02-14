package chatbot.controller;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import chatbot.config.ChatRequest;
import chatbot.entity.ChatMessage;
import chatbot.service.ChatbotService;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@AllArgsConstructor 
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/ask")
    public ResponseEntity<String> askQuestion(@RequestBody ChatRequest chatRequest) {
        String response = chatbotService.getChatbotResponse(chatRequest.getQuery());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public List<ChatMessage> getChatHistory() {
        return chatbotService.getChatHistory();
    }
    @PostMapping("/insert")
    public ResponseEntity<String> insertChatMessage(@RequestBody ChatMessage chatMessage) {
        // Save the chat message to MongoDB
        chatbotService.insertChatMessage(chatMessage);
        return ResponseEntity.ok("Chat message inserted successfully");
    }
}
