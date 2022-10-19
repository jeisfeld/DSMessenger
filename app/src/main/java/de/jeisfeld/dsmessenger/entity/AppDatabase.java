package de.jeisfeld.dsmessenger.entity;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Conversation.class, Message.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
	public abstract ConversationDao getConversationDao();

	public abstract MessageDao getMessageDao();
}
