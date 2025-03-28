package chatbot.respository;

import chatbot.entity.ChatEntity;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface ChatRepository extends MongoRepository<ChatEntity, String> {
    Optional<ChatEntity> findByEmail(String email);
    Optional<ChatEntity> findByVerificationToken(String token);
    
    Optional<ChatEntity> findById(String id);
    
    
    @Query(value = "{ '_id': ?0 }", fields = "{ 'credits': 1 }")
    Optional<Double> findCreditsByUserId(ObjectId userId);

    

    
    
}
