package chatbot.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;

import chatbot.entity.PaymentEntity;
import chatbot.entity.PaymentRequestDto;
import chatbot.service.StripeService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stripe")
@CrossOrigin(origins = { "http://localhost:3000", "https://vchatai.netlify.app" })
@RequiredArgsConstructor
public class StripeController {

	private final StripeService stripeService;

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

	@PostMapping("/update-status")
	public ResponseEntity<String> updatePaymentStatus(@RequestBody Map<String, String> request) {
		String paymentId = request.get("paymentId");

		try {
			PaymentIntent paymentIntent = stripeService.retrievePaymentIntent(paymentId);

			if ("succeeded".equals(paymentIntent.getStatus())) {
				PaymentEntity payment = stripeService.updatePaymentStatus(paymentId, "succeeded");
				if (payment != null) {
					return ResponseEntity.ok("Payment status updated successfully");
				}
			}
			return ResponseEntity.ok("Payment is not completed yet");

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error updating payment: " + e.getMessage());
		}
	}
	
	  @PostMapping("/webhook")
	    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
	                                                @RequestHeader("Stripe-Signature") String sigHeader) {
	        try {
	            // Convert JSON payload to Stripe Event object
	            Event event = Event.GSON.fromJson(payload, Event.class);

	            if ("payment_intent.succeeded".equals(event.getType())) {
	                // Extract Payment Intent object
	                PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);

	                if (paymentIntent != null) {
	                    String paymentId = paymentIntent.getId();
	                    stripeService.updatePaymentStatus(paymentId, "succeeded");
	                    System.out.println("✅ Payment status updated for: " + paymentId);
	                }
	            }

	            return ResponseEntity.ok("Webhook received");

	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error: " + e.getMessage());
	        }
	    }
	
	
	@GetMapping("/payments-getby-email")
	public ResponseEntity<?> getPaymentsByEmail(@RequestParam String email) {
        List<PaymentEntity> payments = stripeService.getPaymentsByEmail(email);
        if (payments.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No payments found for this email.");
        }
        return ResponseEntity.ok(payments);
    }
	
}
