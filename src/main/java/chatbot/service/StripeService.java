package chatbot.service;

import chatbot.entity.PaymentEntity;
import chatbot.entity.PaymentRequestDto;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import java.util.Map;

public interface StripeService {
//    PaymentIntent createPaymentIntent(Map<String, Object> paymentRequest) throws StripeException;
    PaymentEntity savePayment(PaymentEntity payment);
	PaymentIntent createPaymentIntent(PaymentRequestDto paymentRequest) throws StripeException;
	PaymentIntent retrievePaymentIntent(String paymentId) throws StripeException;
    PaymentEntity updatePaymentStatus(String paymentId, String status);
}
