package chatbot.config;

import lombok.Data;

@Data // Lombok will generate getters and setters
public class ChatRequest {
    private String query;
}
