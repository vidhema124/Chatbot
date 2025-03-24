package chatbot.entity;

import lombok.Data;

@Data
public class PaymentRequestDto {
	private Long amount;
	private String email;
}
