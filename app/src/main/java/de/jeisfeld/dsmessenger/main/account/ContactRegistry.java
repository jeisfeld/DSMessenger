package de.jeisfeld.dsmessenger.main.account;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.jeisfeld.dsmessenger.Application;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.entity.Contact;
import de.jeisfeld.dsmessenger.entity.Contact.ContactStatus;
import de.jeisfeld.dsmessenger.entity.Conversation;
import de.jeisfeld.dsmessenger.entity.ConversationDao;
import de.jeisfeld.dsmessenger.entity.Message;
import de.jeisfeld.dsmessenger.http.HttpSender;
import de.jeisfeld.dsmessenger.util.PreferenceUtil;

/**
 * Registry for contacts.
 */
public final class ContactRegistry {
	/**
	 * The singleton instance.
	 */
	private static ContactRegistry instance;
	/**
	 * The contacts.
	 */
	private final SparseArray<Contact> contacts = new SparseArray<>();

	/**
	 * Constructor.
	 */
	private ContactRegistry() {
		List<Integer> contactIds = PreferenceUtil.getSharedPreferenceIntList(R.string.key_contact_ids);
		for (int contactId : contactIds) {
			contacts.put(contactId, new Contact(contactId));
		}
	}

	/**
	 * Get an the singleton instance.
	 *
	 * @return The singleton instance.
	 */
	public static synchronized ContactRegistry getInstance() {
		if (instance == null) {
			instance = new ContactRegistry();
		}
		return instance;
	}

	/**
	 * Get all contacts.
	 *
	 * @return The list of contacts.
	 */
	public List<Contact> getContacts() {
		List<Contact> result = new ArrayList<>();
		for (int i = 0; i < contacts.size(); i++) {
			result.add(contacts.valueAt(i));
		}
		return result;
	}

	/**
	 * Get all contacts.
	 *
	 * @return The list of contacts.
	 */
	public List<Contact> getConnectedContacts() {
		List<Contact> result = new ArrayList<>();
		for (int i = 0; i < contacts.size(); i++) {
			Contact contact = contacts.valueAt(i);
			if (contact.getStatus() == ContactStatus.CONNECTED) {
				result.add(contact);
			}
		}
		return result;
	}

	/**
	 * Get the contact for a given relationId.
	 *
	 * @param relationId The relationId.
	 * @return The contact.
	 */
	public Contact getContact(final int relationId) {
		return contacts.get(relationId);
	}

	/**
	 * Get all contacts of one type.
	 *
	 * @param isSlave Flag indicating if doms or subs are requested.
	 * @return The list of contacts of given type.
	 */
	public List<Contact> getContacts(final boolean isSlave) {
		List<Contact> result = new ArrayList<>();
		for (int i = 0; i < contacts.size(); i++) {
			Contact contact = contacts.valueAt(i);
			if (contact.isSlave() == isSlave) {
				result.add(contact);
			}
		}
		return result;
	}

	/**
	 * Add or update a contact in local store.
	 *
	 * @param contact the stored color
	 */
	public void addOrUpdate(final Contact contact) {
		contact.store();
		contacts.put(contact.getRelationId(), contact);
	}

	/**
	 * Remove a contact from local store.
	 *
	 * @param contact The contact to be removed
	 */
	public void remove(final Contact contact) {
		int relationId = contact.getRelationId();
		contacts.remove(relationId);

		List<Integer> contactIds = PreferenceUtil.getSharedPreferenceIntList(R.string.key_contact_ids);
		contactIds.remove((Integer) relationId);
		PreferenceUtil.setSharedPreferenceIntList(R.string.key_contact_ids, contactIds);

		PreferenceUtil.removeIndexedSharedPreference(R.string.key_contact_name, relationId);
		PreferenceUtil.removeIndexedSharedPreference(R.string.key_contact_my_name, relationId);
		PreferenceUtil.removeIndexedSharedPreference(R.string.key_contact_contact_id, relationId);
		PreferenceUtil.removeIndexedSharedPreference(R.string.key_contact_is_slave, relationId);
		PreferenceUtil.removeIndexedSharedPreference(R.string.key_contact_connection_code, relationId);
		PreferenceUtil.removeIndexedSharedPreference(R.string.key_contact_slave_permissions, relationId);
		PreferenceUtil.removeIndexedSharedPreference(R.string.key_contact_status, relationId);

		Application.getAppDatabase().getConversationDao().deleteConversationsByRelationId(relationId);
	}

	/**
	 * Clean all contacts.
	 */
	public void cleanContacts() {
		while (contacts.size() > 0) {
			remove(contacts.valueAt(0));
		}
	}

	/**
	 * Async query for contact information. From results, update stored contacts.
	 *
	 * @param context  The context.
	 * @param runnable Code to be executed after finishing the refresh.
	 */
	public void refreshContacts(final Context context, final Runnable runnable) {
		if (!AccountFragment.isLoggedIn()) {
			return;
		}
		new Thread(() -> new HttpSender(context).sendMessage("db/usermanagement/querycontacts.php", (response, responseData) -> {
			if (responseData.isSuccess()) {
				SparseArray<Contact> newContacts = (SparseArray<Contact>) responseData.getData().get("contacts");
				if (newContacts == null) {
					return;
				}
				List<Integer> keys = new ArrayList<>();
				synchronized (contacts) {
					for (int i = 0; i < newContacts.size(); i++) {
						keys.add(newContacts.keyAt(i));
						addOrUpdate(newContacts.valueAt(i));
					}
					for (int i = 0; i < contacts.size(); i++) {
						if (!keys.contains(contacts.keyAt(i))) {
							remove(contacts.valueAt(i));
						}
					}
				}
				int usertype = (int) responseData.getData().get("usertype");
				PreferenceUtil.setSharedPreferenceInt(R.string.key_pref_usertype, usertype);
				if (runnable != null) {
					runnable.run();
				}
			}
			else {
				Log.e(Application.TAG, "Failed to retrieve contact data: " + responseData.getErrorMessage());
			}
		})).start();
	}

	/**
	 * Async query for conversation information. From results, update stored conversations.
	 *
	 * @param context  The context.
	 * @param runnable Code to be executed after finishing the refresh.
	 */
	public void refreshConversations(final Context context, final Runnable runnable) {
		if (!AccountFragment.isLoggedIn()) {
			return;
		}
		final long lastTimestamp = PreferenceUtil.getSharedPreferenceLong(R.string.key_last_conversation_timestamp, 0);
		final ConversationDao conversationDao = Application.getAppDatabase().getConversationDao();
		new Thread(() -> new HttpSender(context).sendMessage("db/conversation/queryconversationsandmessages.php", (response, responseData) -> {
			if (responseData.isSuccess()) {
				List<Conversation> conversations = (List<Conversation>) responseData.getData().get("conversations");
				assert conversations != null;
				long newTimestamp = lastTimestamp;
				List<UUID> keys = new ArrayList<>();
				for (Conversation conversation : conversations) {
					Conversation oldConversation = conversationDao.getConversationById(conversation.getConversationId());
					if (oldConversation == null) {
						conversationDao.insert(conversation);
					}
					else {
						conversationDao.update(conversation);
					}
					newTimestamp = Math.max(newTimestamp, conversation.getLastTimestamp());
					keys.add(conversation.getConversationId());
				}
				for (Conversation conversation : conversationDao.getAllConversations()) {
					if (!keys.contains(conversation.getConversationId())) {
						conversationDao.delete(conversation);
					}
				}

				List<Message> messages = (List<Message>) responseData.getData().get("messages");
				Application.getAppDatabase().getMessageDao().insert(messages);
				PreferenceUtil.setSharedPreferenceLong(R.string.key_last_conversation_timestamp, newTimestamp);
				if (runnable != null) {
					runnable.run();
				}
			}
			else {
				Log.e(Application.TAG, "Failed to retrieve conversation data: " + responseData.getErrorMessage());
			}
		})).start();
	}
}
