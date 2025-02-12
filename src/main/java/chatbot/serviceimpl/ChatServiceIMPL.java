package chatbot.serviceimpl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import chatbot.entity.ChatEntity;
import chatbot.respository.ChatRepository;
import chatbot.service.ChatService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ChatServiceIMPL implements ChatService {

    ChatRepository chatRepository;

   

    @Override
    public String signUp(ChatEntity chatEntity) {
        Optional<ChatEntity> existingUser = chatRepository.findByEmail(chatEntity.getEmail());
        if (existingUser.isPresent()) {
            return "Email already exists!";
        }
        chatRepository.save(chatEntity);
        return "User registered successfully!";
    }

    @Override
    public String login(String email, String password) {
        Optional<ChatEntity> user = chatRepository.findByEmail(email);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            return "Login successful!";
        }
        return "Invalid email or password!";
    }
    @Override
    public List<ChatEntity> getAllChats() {
        return chatRepository.findAll();
    }
}
