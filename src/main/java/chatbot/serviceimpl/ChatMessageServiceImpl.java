package chatbot.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import chatbot.entity.ChatMessage;
import chatbot.respository.ChatMessageRepository;
import chatbot.service.ChatbotMessageService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ChatMessageServiceImpl implements ChatbotMessageService {

	 ChatMessageRepository chatMessageRepository;

	@Override
	public List<ChatMessage> getHistory() {
		List<ChatMessage> response = chatMessageRepository.findAll();
		return response;
	}

	@Override
	public List<ChatMessage> getByUserId(String userId) {
		ObjectId objectId = new ObjectId(userId); // Convert String to ObjectId
		return chatMessageRepository.findByUserId(objectId);
	}

	@Override
	public Optional<ChatMessage> updateById(String id, ChatMessage updatedMessage) {
		return chatMessageRepository.findById(id).map(existingMessage -> {
			if (updatedMessage.getUserSearch() != null && !updatedMessage.getUserSearch().isEmpty()) {
				List<ChatMessage.UserSearch> mergedUserSearch = new ArrayList<>(existingMessage.getUserSearch());
				mergedUserSearch.addAll(updatedMessage.getUserSearch());

				existingMessage.setUserSearch(mergedUserSearch);
			}
			return chatMessageRepository.save(existingMessage);
		});
	}

	@Override
	public boolean deleteById(String id) {
		if (chatMessageRepository.existsById(id)) {
			chatMessageRepository.deleteById(id);
			return true;
		}
		return false;
	}

	@Override
	public ChatMessage saveChatMessage(ChatMessage chatMessage) {
		return chatMessageRepository.save(chatMessage);
	}

	@Override
	public boolean deleteAll() {
		if (chatMessageRepository.count() > 0) {
			chatMessageRepository.deleteAll();
			return true;
		}
		return false;
	}

	@Override
	public Optional<ChatMessage> getById(String id) {
		return chatMessageRepository.findById(id);
	}

	@Override
	public boolean deleteByUserId(String userId) {
		try {
			ObjectId objectId = new ObjectId(userId);
			long count = chatMessageRepository.countByUserId(objectId);

			if (count > 0) {
				chatMessageRepository.deleteByUserId(objectId);
				return true;
			}
		} catch (IllegalArgumentException e) {
			System.out.println("Invalid ObjectId format: " + userId);
		}
		return false;
	}

	
	
	
	
}