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
import chatbot.entity.PaymentEntity;
import chatbot.respository.ChatRepository;
import chatbot.respository.PaymentRepository;
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
	private final PaymentRepository paymentRepository;

	@Override
	public Map<String, Object> login(String email, String password) {
		Optional<ChatEntity> user = chatRepository.findByEmail(email);

		if (user.isPresent()) {
			ChatEntity userData = user.get();

			if (userData.isGoogleLogin()) {

				if (password != null && !password.trim().isEmpty()) {
					return createErrorResponse("User not found");
				}
				return generateLoginResponse(userData);
			}
			if (password == null || password.trim().isEmpty()) {
				return createErrorResponse("Please Signup First,");
			}
			if (passwordEncoder.matches(password, userData.getPassword())) {
				if (userData.isVerified()) {
					return generateLoginResponse(userData);
				} else {
					return createErrorResponse("Please check your email and verify your email address.");
				}
			}
		}

		return createErrorResponse("Email or Password is incorrect.");
	}

	private Map<String, Object> generateLoginResponse(ChatEntity userData) {
		SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
		String token = Jwts.builder().setSubject(userData.getEmail()).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 3600000)).signWith(key, SignatureAlgorithm.HS256)
				.compact();

		Map<String, Object> response = new HashMap<>();
		response.put("message", "Login successfully");
		response.put("status", 200);
		response.put("token", token);
		response.put("id", userData.getId());
		response.put("name", userData.getName());
		response.put("email", userData.getEmail());
		response.put("image", userData.getImage());
		response.put("credits", userData.getCredits());

		return response;
	}

	private Map<String, Object> createErrorResponse(String message) {
		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("message", message);
		errorResponse.put("status", 400);
		return errorResponse;
	}

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

		String emailBody = "Hello,\n\n" + "Subject: " + subject + "\n\n"
				+ "Follow this link to verify your email address:\n\n" + url + "\n\n"
				+ "If you didn’t ask to verify this address, you can ignore this email.\n\n" + "Thanks,\n\n"
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

	@Override
	public ResponseEntity<Map<String, Object>> deleteChatEntity(String id) {
		Map<String, Object> response = new HashMap<>();

		if (chatRepository.existsById(id)) {
			chatRepository.deleteById(id);
			response.put("status", "success");
			response.put("message", "User details deleted successfully.");
			return ResponseEntity.ok(response);
		} else {
			response.put("status", "error");
			response.put("message", "User details with id " + id + " not found.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
	}

	@Override
	public ResponseEntity<Map<String, Object>> getUserByEmail(String email) {
		Map<String, Object> response = new HashMap<>();
		Optional<ChatEntity> user = chatRepository.findByEmail(email);

		if (user.isPresent()) {
			response.put("status", "success");
			response.put("user", user.get());
			return ResponseEntity.ok(response);
		} else {
			response.put("status", "error");
			response.put("message", "User not found with email: " + email);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
	}

	@Override
	public ResponseEntity<Map<String, Object>> createUser(ChatEntity chatEntity) {
		Map<String, Object> response = new HashMap<>();

		if (chatRepository.findByEmail(chatEntity.getEmail()).isPresent()) {
			response.put("status", "error");
			response.put("message", "Email already registered.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		ChatEntity savedUser = chatRepository.save(chatEntity);

		response.put("status", "success");
		response.put("message", "User created successfully.");
		response.put("id", savedUser.getId());
		response.put("user", savedUser);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Override
	public Optional<ChatEntity> findById(String id) {
		Optional<ChatEntity> response = chatRepository.findById(id);
		return response;
	}

	@Override
	public ChatEntity chatUpdate(ChatEntity chatEntity) {
		return chatRepository.save(chatEntity);
	}
	
	
	
	@Override
	public ChatEntity updateUserCreditsByPayments(String userId) {
	    Optional<ChatEntity> userOptional = chatRepository.findById(userId);

	    if (userOptional.isEmpty()) {
	        throw new RuntimeException("User not found");
	    }

	    ChatEntity user = userOptional.get();
	    List<PaymentEntity> payments = paymentRepository.findByCustomerEmail(user.getEmail());

	    if (payments.isEmpty()) {
	        throw new RuntimeException("No payments found for this user");
	    }

	    double totalCredits = user.getCredits();
	    String latestPlanId = user.getPlanId(); 

	    for (PaymentEntity payment : payments) {
	        if ("succeeded".equals(payment.getStatus()) && !payment.isAmountStatus()) {
	            double amountInDollars = payment.getAmount() / 100.0;
	            totalCredits += amountInDollars * 10;

	            if (payment.getPlanId() != null) {
	                latestPlanId = payment.getPlanId();
	            }

	            payment.setAmountStatus(true);
	            paymentRepository.save(payment);
	        }
	    }

	    user.setCredits(totalCredits);
	    user.setPlanId(latestPlanId); 

	    return chatRepository.save(user);
	}



//	@Override
//	public ChatEntity updateUserCreditsByPayments(String userId) {
//		Optional<ChatEntity> userOptional = chatRepository.findById(userId);
//
//		if (userOptional.isEmpty()) {
//			throw new RuntimeException("User not found");
//		}
//
//		ChatEntity user = userOptional.get();
//		List<PaymentEntity> payments = paymentRepository.findByCustomerEmail(user.getEmail());
//
//		if (payments.isEmpty()) {
//			throw new RuntimeException("No payments found for this user");
//		}
//
//		double totalCredits = user.getCredits();
//
//		for (PaymentEntity payment : payments) {
//			if ("succeeded".equals(payment.getStatus()) && payment.isAmountStatus()) {
//				double amountInDollars = payment.getAmount() / 100.0;
//				totalCredits += amountInDollars * 10;
//
//				payment.setAmountStatus(false);
//				paymentRepository.save(payment);
//			}
//		}
//
//		user.setCredits(totalCredits);
//
//		if (totalCredits > 20) {
//			user.setPlans("premium");
//		} else {
//			user.setPlans("free");
//		}
//
//		return chatRepository.save(user);
//	}

	@Override
	public ResponseEntity<?> getUserById(String userId) {
		Optional<ChatEntity> userOptional = chatRepository.findById(userId);

		if (userOptional.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
		}

		return ResponseEntity.ok(userOptional.get());
	}

}
