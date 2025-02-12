package chatbot.serviceimpl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



import chatbot.entity.ChatEntity;
import chatbot.respository.ChatRepository;
import chatbot.service.ChatService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ChatServiceIMPL implements ChatService {

	private final ChatRepository chatRepository;
	private final PasswordEncoder passwordEncoder;

	public String signUp(ChatEntity chatEntity) {

		Optional<ChatEntity> existingUser = chatRepository.findByEmail(chatEntity.getEmail());
		if (existingUser.isPresent()) {
			return "Email already exists!";
		}
		String encryptedPassword = passwordEncoder.encode(chatEntity.getPassword());
		chatEntity.setPassword(encryptedPassword);
		chatRepository.save(chatEntity);
		return "User registered successfully!";
	}

	@Override
	public Map<String, Object> login(String email, String password) {
		Optional<ChatEntity> user = chatRepository.findByEmail(email);

		if (user.isPresent()) {
			if (passwordEncoder.matches(password, user.get().getPassword())) {

				SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
				String token = Jwts.builder().setSubject(email).setIssuedAt(new Date())
						.setExpiration(new Date(System.currentTimeMillis() + 3600000))
						.signWith(key, SignatureAlgorithm.HS256).compact();

				Map<String, Object> response = new HashMap<>();
				response.put("message", "Login successfully");
				response.put("status", 200);
				response.put("token", token);

				return response;
			}
		}

		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("message", "Email Or Password Wrong");
		errorResponse.put("status", 401);

		return errorResponse;
	}

	@Override
	public List<ChatEntity> getAllChats() {
		return chatRepository.findAll();
	}

}
