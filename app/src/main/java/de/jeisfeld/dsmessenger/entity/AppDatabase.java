package de.jeisfeld.dsmessenger.entity;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * The application database.
 */
@Database(entities = {Conversation.class, Message.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
	public abstract ConversationDao getConversationDao();

	public abstract MessageDao getMessageDao();

	public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
			database.execSQL("ALTER TABLE conversation ADD COLUMN conversationFlags TEXT");
			database.execSQL("UPDATE conversation SET conversationFlags ='0'");
		}
	};
}
