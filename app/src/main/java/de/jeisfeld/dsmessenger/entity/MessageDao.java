package de.jeisfeld.dsmessenger.entity;

import java.util.List;
import java.util.UUID;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import de.jeisfeld.dsmessenger.main.message.MessageFragment.MessageStatus;

/**
 * The DAO for accessing message table.
 */
@Dao
public interface MessageDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(Message message);

	@Update
	void update(Message message);

	@Delete
	void delete(Message message);

	@Query("SELECT * FROM message WHERE conversationId = :conversationId ORDER BY timestamp")
	List<Message> getMessagesByConversationId(String conversationId);

	default List<Message> getMessagesByConversationId(UUID conversationId) {
		return getMessagesByConversationId(conversationId.toString());
	}

	@Query("SELECT * FROM message WHERE messageId = :messageId")
	Message getMessageById(String messageId);

	default Message getMessageById(UUID messageId) {
		return getMessageById(messageId.toString());
	}

	default void acknowledgeMessages(String[] messageIds) {
		for (String messageId : messageIds) {
			Message acknowledgedMessage = getMessageById(messageId);
			if (acknowledgedMessage != null && acknowledgedMessage.getStatus() != MessageStatus.MESSAGE_ACKNOWLEDGED) {
				acknowledgedMessage.setStatus(MessageStatus.MESSAGE_ACKNOWLEDGED);
				acknowledgedMessage.update();
			}
		}
	}
}
