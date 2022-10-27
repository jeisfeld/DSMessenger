package de.jeisfeld.dsmessenger.entity;

import java.util.List;
import java.util.UUID;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

/**
 * The DAO for accessing conversation table.
 */
@Dao
public interface ConversationDao {
	@Insert
	void insert(Conversation conversation);

	@Update
	void update(Conversation conversation);

	@Delete
	void delete(Conversation conversation);

	@Delete
	void delete(List<Conversation> conversation);

	@Query("SELECT * FROM conversation WHERE relationId = :relationId ORDER BY lastTimestamp DESC")
	List<Conversation> getConversationsByRelationId(int relationId);

	@Query("SELECT * FROM conversation WHERE conversationId = :conversationId")
	Conversation getConversationById(String conversationId);

	default Conversation getConversationById(UUID conversationId) {
		return getConversationById(conversationId.toString());
	}
}
