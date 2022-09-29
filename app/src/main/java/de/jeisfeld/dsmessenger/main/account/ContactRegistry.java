package de.jeisfeld.dsmessenger.main.account;

import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

import de.jeisfeld.dsmessenger.Application;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.http.HttpSender;
import de.jeisfeld.dsmessenger.util.PreferenceUtil;

/**
 * Registry for contacts.
 */
public class ContactRegistry {
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
		PreferenceUtil.removeIndexedSharedPreference(R.string.key_contact_is_slave, relationId);
		PreferenceUtil.removeIndexedSharedPreference(R.string.key_contact_connection_code, relationId);
		PreferenceUtil.removeIndexedSharedPreference(R.string.key_contact_status, relationId);
	}

	/**
	 * Clean all contacts.
	 */
	public void cleanContacts() {
		for (int i = 0; i < contacts.size(); i++) {
			remove(contacts.valueAt(i));
		}
	}

	/**
	 * Async query for contact information. From results, update stored contacts.
	 *
	 * @param runnable Code to be executed after finishing the refresh.
	 */
	public void refreshContacts(final Runnable runnable) {
		if (PreferenceUtil.getSharedPreferenceString(R.string.key_pref_username) == null) {
			return;
		}
		new Thread(() -> new HttpSender().sendMessage("db/usermanagement/querycontacts.php", (response, responseData) -> {
			if (responseData.isSuccess()) {
				SparseArray<Contact> newContacts = (SparseArray<Contact>) responseData.getData().get("contacts");
				if (newContacts == null) {
					return;
				}
				List<Integer> keys = new ArrayList<>();
				for (int i = 0; i < newContacts.size(); i++) {
					keys.add(newContacts.keyAt(i));
					addOrUpdate(newContacts.valueAt(i));
				}
				for (int i = 0; i < contacts.size(); i++) {
					if (!keys.contains(contacts.keyAt(i))) {
						remove(contacts.valueAt(i));
					}
				}
				if (runnable != null) {
					runnable.run();
				}
			}
			else {
				Log.e(Application.TAG, "Failed to retrieve contact data: " + responseData.getErrorMessage());
			}
		})).start();
	}
}
