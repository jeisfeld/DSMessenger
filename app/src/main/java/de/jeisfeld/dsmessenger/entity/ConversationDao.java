package de.jeisfeld.dsmessenger.entity;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface ConversationDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(Conversation conversation);

	@Delete
	void delete(Conversation conversation);

	@Delete
	void delete(List<Conversation> conversation);

	@Query("SELECT * FROM conversation WHERE relationId = :relationId ORDER BY lastTimestamp DESC")
	List<Conversation> getConversationsByRelationId(int relationId);

	@Query("SELECT * FROM conversation WHERE conversationId = :conversationId")
	Conversation getConversationById(String conversationId);
}
