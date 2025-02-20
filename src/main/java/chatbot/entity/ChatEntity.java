package chatbot.entity;

import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Document(collection = "chatbot")
public class ChatEntity {
    private String id;
    private String name;
    private String email;
    private String password;
    private String image;
    private boolean verified = false;
    private String verificationToken;
    
}
