package chatbot.controller;

import chatbot.entity.PaymentEntity;
import chatbot.entity.PaymentRequestDto;
import chatbot.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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

            stripeService.savePayment(payment);

            return ResponseEntity.ok(Map.of("clientSecret", paymentIntent.getClientSecret()));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

}
