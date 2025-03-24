package chatbot.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "payments")
public class PaymentEntity {
    @Id
    private String id; // MongoDB document ID
    private String paymentId; // Stripe payment ID
    private String customerEmail;
    private String status;
    private Long amount;
}
