package chatbot.service;

import chatbot.entity.ChatEntity;
import chatbot.entity.PaymentEntity;
import chatbot.entity.PaymentRequestDto;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

import java.util.List;
import java.util.Map;

public interface StripeService {

	PaymentEntity savePayment(PaymentEntity payment);

	PaymentIntent createPaymentIntent(PaymentRequestDto paymentRequest) throws StripeException;

	PaymentIntent retrievePaymentIntent(String paymentId) throws StripeException;

	PaymentEntity updatePaymentStatus(String paymentId, String status);

	List<PaymentEntity> getPaymentsByEmail(String email);

}
