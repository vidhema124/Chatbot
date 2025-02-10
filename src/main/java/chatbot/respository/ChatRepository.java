package chatbot.respository;

import chatbot.entity.ChatEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ChatRepository extends MongoRepository<ChatEntity, String> {
    Optional<ChatEntity> findByEmail(String email);
}
