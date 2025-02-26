package chatbot.respository;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import chatbot.entity.ChatMessage;

@Repository

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

	 @Query("{'userSearch.userMessage': ?0}")
	    Optional<ChatMessage> findByUserSearch_UserMessage(String userMessage);

	    List<ChatMessage> findByUserId(ObjectId userId);

}