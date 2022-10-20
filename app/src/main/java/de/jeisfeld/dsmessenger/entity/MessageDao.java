package de.jeisfeld.dsmessenger.entity;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface MessageDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insert(Message message);

	@Delete
	void delete(Message message);

	@Query("SELECT * FROM message WHERE conversationId = :conversationId ORDER BY timestamp")
	List<Message> getMessagesByConversationId(String conversationId);

	@Query("SELECT * FROM message WHERE messageId = :messageId")
	Message getMessageById(String messageId);

}