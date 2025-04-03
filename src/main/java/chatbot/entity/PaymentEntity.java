package chatbot.entity;

import lombok.Data;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "payments")
public class PaymentEntity {
	@Id
	private String id;
	private String paymentId;
	private String customerEmail;
	private String status;
	private Long amount;
	private String name;
	private LocalDateTime createDate;
	private boolean amountStatus = true;
	private String planId;
    
}
