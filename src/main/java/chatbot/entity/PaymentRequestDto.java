package chatbot.entity;

import lombok.Data;

@Data
public class PaymentRequestDto {
	private String name;
	private Long amount;
	private String email;
}
