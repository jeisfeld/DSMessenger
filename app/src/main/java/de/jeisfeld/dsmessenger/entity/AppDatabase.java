package de.jeisfeld.dsmessenger.entity;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * The application database.
 */
@Database(entities = {Conversation.class, Message.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
	public abstract ConversationDao getConversationDao();

	public abstract MessageDao getMessageDao();
}
