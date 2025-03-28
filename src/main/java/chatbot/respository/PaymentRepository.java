package chatbot.respository;

import chatbot.entity.PaymentEntity;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends MongoRepository<PaymentEntity, String> {
	
	 PaymentEntity findByPaymentId(String paymentId);
	 List<PaymentEntity> findByCustomerEmail(String customerEmail);
}
