package de.jeisfeld.coachat.entity;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * The application database.
 */
@Database(entities = {Conversation.class, Message.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {
	/**
	 * Migration from version 2 to version 3
	 */
	public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
			database.execSQL("ALTER TABLE conversation ADD COLUMN preparedMessage TEXT");
		}
	};

	/**
	 * Migration from version 3 to version 4
	 */
	public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
		@Override
		public void migrate(SupportSQLiteDatabase database) {
			database.execSQL("ALTER TABLE conversation ADD COLUMN archived INTEGER not null DEFAULT 0");
		}
	};

	public abstract ConversationDao getConversationDao();

	public abstract MessageDao getMessageDao();
}
