package chatbot.serviceimpl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import chatbot.entity.ChatEntity;
import chatbot.respository.ChatRepository;
import chatbot.service.ChatService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ChatServiceIMPL implements ChatService {

	private final ChatRepository chatRepository;
	private final PasswordEncoder passwordEncoder;

//	  @Override
//	    public String signUp(ChatEntity chatEntity) {
//	        Optional<ChatEntity> existingUser = chatRepository.findByEmail(chatEntity.getEmail());
//	        if (existingUser.isPresent()) {
//	            return "Email already exists!";
//	        }
//	        String encryptedPassword = passwordEncoder.encode(chatEntity.getPassword());
//	        chatEntity.setPassword(encryptedPassword);
//	        chatRepository.save(chatEntity);
//	        return "User registered successfully!";
//	    }
//	

	@Override
	public Map<String, Object> login(String email, String password) {
	    Optional<ChatEntity> user = chatRepository.findByEmail(email);

	    if (user.isPresent()) {
	        ChatEntity userData = user.get();
	        if (passwordEncoder.matches(password, userData.getPassword())) {
	            if (userData.isVerified()) {
	                SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
	                String token = Jwts.builder()
	                        .setSubject(email)
	                        .setIssuedAt(new Date())
	                        .setExpiration(new Date(System.currentTimeMillis() + 3600000)) 
	                        .signWith(key, SignatureAlgorithm.HS256)
	                        .compact();
	                Map<String, Object> response = new HashMap<>();
	                response.put("message", "Login successfully");
	                response.put("status", 200);
	                response.put("token", token);
	                response.put("name", userData.getName());  
	                response.put("email", userData.getEmail()); 
	                response.put("image", userData.getImage());  

	                return response;
	            } else {
	                Map<String, Object> errorResponse = new HashMap<>();
	                errorResponse.put("message", "Please check your email and verify your email address.");
	                errorResponse.put("status", 400);
	                return errorResponse;
	            }
	        }
	    }
	    Map<String, Object> errorResponse = new HashMap<>();
	    errorResponse.put("message", "Email or Password is incorrect.");
	    errorResponse.put("status", 400);  
	    return errorResponse;
	}


	@Override
	public String updateUser(String id, ChatEntity updatedChatEntity) {
		Optional<ChatEntity> existingUserOptional = chatRepository.findById(id);

		if (!existingUserOptional.isPresent()) {
			return "User not found!";
		}

		ChatEntity existingUser = existingUserOptional.get();

		if (!existingUser.getEmail().equals(updatedChatEntity.getEmail())) {
			Optional<ChatEntity> emailCheck = chatRepository.findByEmail(updatedChatEntity.getEmail());
			if (emailCheck.isPresent()) {
				return "Email is already taken!";
			}
			existingUser.setEmail(updatedChatEntity.getEmail());
		}

		existingUser.setName(updatedChatEntity.getName());

		if (updatedChatEntity.getPassword() != null && !updatedChatEntity.getPassword().isEmpty()) {
			String encryptedPassword = passwordEncoder.encode(updatedChatEntity.getPassword());
			existingUser.setPassword(encryptedPassword);
		}

		if (updatedChatEntity.getImage() != null && !updatedChatEntity.getImage().isEmpty()) {
			existingUser.setImage(updatedChatEntity.getImage());
		}

		chatRepository.save(existingUser);
		return "User updated successfully!";
	}

//	@Autowired
//	private JavaMailSender mailSender;
//
//	private final String FRONTEND_URL = "https://chat.chatbotapp.ai/auth/action?mode=verifyEmail&oobCode=%s";
//	@Override
//	public ChatEntity registerUser(ChatEntity user) {
//	    Optional<ChatEntity> existingUserOpt = chatRepository.findByEmail(user.getEmail());
//	    if (existingUserOpt.isPresent()) {
//	        ChatEntity userToSave = existingUserOpt.get();
//	        userToSave.setVerificationToken(UUID.randomUUID().toString());  
//	        ChatEntity savedUser = chatRepository.save(userToSave);
//	        sendVerificationEmail(savedUser.getEmail(), savedUser.getVerificationToken());
//	        return savedUser; 
//	    } else {
//	        return null; // User not found
//	    }
//	}
//
//	private void sendVerificationEmail(String email, String token) {
//	    String subject = "Follow this link to verify your email address.";
//	   
//	    String url = String.format(FRONTEND_URL, token);  
//
//	    
//	    String emailBody = "Hello,\n\n"
//	                     + "Subject: " + subject + "\n\n"
//	                     + "Follow this link to verify your email address:\n\n"
//	                     + url + "\n\n"
//	                     + "If you didn’t ask to verify this address, you can ignore this email.\n\n"
//	                     + "Thanks,\n\n"
//	                     + "Your Chatbot App team";
//
//	    try {
//	        MimeMessage mimeMessage = mailSender.createMimeMessage();
//	        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
//	        helper.setTo(email);
//	        helper.setSubject(subject);
//	        helper.setText(emailBody, false);  
//	        mailSender.send(mimeMessage);
//	    } catch (MessagingException e) {
//	        e.printStackTrace();
//	    }
//	}
	@Override
	public boolean verifyUser(String token) {
		Optional<ChatEntity> optionalUser = chatRepository.findByVerificationToken(token);
		if (optionalUser.isPresent()) {
			ChatEntity user = optionalUser.get();
			user.setVerified(true);
			user.setVerificationToken(null);
			chatRepository.save(user);
			return true;
		}
		return false;
	}
	

	@Autowired
	private JavaMailSender mailSender;

	private final String FRONTEND_URL = "https://vchatai.netlify.app/verify-email?token=%s";

	@Override
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
	public ChatEntity registerUser(ChatEntity user) {
	    Optional<ChatEntity> existingUserOpt = chatRepository.findByEmail(user.getEmail());
	    if (existingUserOpt.isPresent()) {
	        ChatEntity userToSave = existingUserOpt.get();
	        userToSave.setVerificationToken(UUID.randomUUID().toString());
	        ChatEntity savedUser = chatRepository.save(userToSave);
	        sendVerificationEmail(savedUser.getEmail(), savedUser.getVerificationToken());

	        return savedUser;
	    } else {
	        return null; 
	    }
	}

	private void sendVerificationEmail(String email, String token) {
	    String subject = "Follow this link to verify your email address.";
	    String url = String.format(FRONTEND_URL, token);

	    String emailBody = "Hello,\n\n"
	            + "Subject: " + subject + "\n\n"
	            + "Follow this link to verify your email address:\n\n"
	            + url + "\n\n"
	            + "If you didn’t ask to verify this address, you can ignore this email.\n\n"
	            + "Thanks,\n\n"
	            + "Your Chatbot App team";

	    try {
	        MimeMessage mimeMessage = mailSender.createMimeMessage();
	        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
	        helper.setTo(email);
	        helper.setSubject(subject);
	        helper.setText(emailBody, false); 
	        mailSender.send(mimeMessage);
	    } catch (MessagingException e) {
	        e.printStackTrace();
	    }
	}

}
