package chatbot.serviceimpl;

import chatbot.entity.PaymentEntity;
import chatbot.entity.PaymentRequestDto;
import chatbot.respository.PaymentRepository;
import chatbot.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StripeServiceImpl implements StripeService {

	private final PaymentRepository paymentRepository;

	@Override
	public PaymentIntent createPaymentIntent(PaymentRequestDto paymentRequest) throws StripeException {
		PaymentIntentCreateParams params = PaymentIntentCreateParams.builder().setAmount(paymentRequest.getAmount())
				.setCurrency("usd").setReceiptEmail(paymentRequest.getEmail()).setDescription("Payment for order")
				.addPaymentMethodType("card").build();

		return PaymentIntent.create(params);
	}

	@Override
	public PaymentEntity savePayment(PaymentEntity payment) {
		return paymentRepository.save(payment);
	}

	@Override
	public PaymentIntent retrievePaymentIntent(String paymentId) throws StripeException {
		return PaymentIntent.retrieve(paymentId); // Stripe API key is set globally in StripeConfig
	}

	@Override
	public PaymentEntity updatePaymentStatus(String paymentId, String status) {
		PaymentEntity payment = paymentRepository.findByPaymentId(paymentId);

		if (payment != null) {
			payment.setStatus(status);
			if ("succeeded".equals(status)) {
				payment.setCreateDate(LocalDateTime.now());
			}
			return paymentRepository.save(payment);
		}
		return null;
	}
}
