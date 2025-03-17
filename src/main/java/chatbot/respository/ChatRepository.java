package chatbot.respository;

import chatbot.entity.ChatEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ChatRepository extends MongoRepository<ChatEntity, String> {
    Optional<ChatEntity> findByEmail(String email);
    Optional<ChatEntity> findByVerificationToken(String token);
    
    
}
