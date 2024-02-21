package de.jeisfeld.coachat.entity;

import java.util.List;
import java.util.UUID;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

/**
 * The DAO for accessing conversation table.
 */
@Dao
public interface ConversationDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(Conversation conversation);

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(List<Conversation> conversation);

	@Update
	void update(Conversation conversation);

	@Delete
	void delete(Conversation conversation);

	@Query("DELETE FROM conversation WHERE relationId = :relationId")
	void deleteConversationsByRelationId(int relationId);

	@Query("SELECT * FROM conversation WHERE relationId = :relationId ORDER BY lastTimestamp DESC")
	List<Conversation> getConversationsByRelationId(int relationId);

	@Query("SELECT * FROM conversation WHERE relationId = :relationId AND archived = 0 ORDER BY lastTimestamp DESC")
	List<Conversation> getUnarchivedConversationsByRelationId(int relationId);

	@Query("SELECT * FROM conversation WHERE conversationId = :conversationId")
	Conversation getConversationById(String conversationId);

	@Query("SELECT * FROM conversation")
	List<Conversation> getAllConversations();

	@Query("SELECT MAX(lastTimestamp) FROM conversation WHERE relationId = :relationId")
	long getLastTimestampForContact(int relationId);

	default Conversation getConversationById(UUID conversationId) {
		return getConversationById(conversationId.toString());
	}
}
