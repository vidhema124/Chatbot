package chatbot.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

import chatbot.entity.ChatEntity;
import chatbot.entity.PaymentEntity;
import chatbot.entity.PaymentRequestDto;
import chatbot.respository.ChatRepository;
import chatbot.respository.PaymentRepository;
import chatbot.service.ChatService;
import chatbot.service.StripeService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stripe")
@CrossOrigin(origins = { "http://localhost:3000", "https://vchatai.netlify.app" })
@RequiredArgsConstructor
public class StripeController {

	@Autowired
	private ChatService chatService;

	@Autowired
	private StripeService stripeService;

	@Autowired
	private ChatRepository chatRepository; 

	@Autowired
	private PaymentRepository paymentRepository;

	@PostMapping("/create-payment-intent")
	public ResponseEntity<?> createPaymentIntent(@RequestBody PaymentRequestDto paymentRequest) {
		try {
			PaymentIntent paymentIntent = stripeService.createPaymentIntent(paymentRequest);

			PaymentEntity payment = new PaymentEntity();
			payment.setPaymentId(paymentIntent.getId());
			payment.setCustomerEmail(paymentRequest.getEmail());
			payment.setStatus(paymentIntent.getStatus());
			payment.setAmount(paymentRequest.getAmount());
			payment.setName(paymentRequest.getName());

			stripeService.savePayment(payment);
			Map<String, Object> response = new HashMap<>();
			response.put("paymentId", payment.getPaymentId());
			response.put("clientSecret", paymentIntent.getClientSecret());

			return ResponseEntity.ok(response);
		} catch (StripeException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
		}
	}

	
	@PostMapping("/process-payment")
	public ResponseEntity<?> processPayment(@RequestBody Map<String, String> request) {
		try {

			String userId = request.get("userId");
			String email = request.get("email");

			if (userId == null || email == null) {
				return ResponseEntity.badRequest().body("Missing required parameters: userId or email");
			}
			List<PaymentEntity> userPayments = paymentRepository.findByCustomerEmail(email);

			if (userPayments.isEmpty()) {
				return ResponseEntity.badRequest().body("No payments found for the given email.");
			}
			List<PaymentEntity> updatedPayments = new LinkedList<>();
			for (PaymentEntity payment : userPayments) {
				String paymentId = payment.getPaymentId();
				PaymentIntent paymentIntent = stripeService.retrievePaymentIntent(paymentId);

				if ("succeeded".equals(paymentIntent.getStatus())) {
					PaymentEntity updatedPayment = stripeService.updatePaymentStatus(paymentId, "succeeded");
					updatedPayments.add(updatedPayment);
				}
			}

			if (updatedPayments.isEmpty()) {
				return ResponseEntity.ok("No successful payments found to update.");
			}
			ChatEntity updatedUser = chatService.updateUserCreditsByPayments(userId);

			Map<String, Object> response = new HashMap<>();
			response.put("message", "Payments processed successfully");
			response.put("userDetails", updatedUser);
			response.put("payments", updatedPayments);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
		}
	}

}
