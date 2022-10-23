package de.jeisfeld.dsmessenger.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.NonNull;
import de.jeisfeld.dsmessenger.Application;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.entity.Conversation;
import de.jeisfeld.dsmessenger.http.HttpSender;
import de.jeisfeld.dsmessenger.main.account.AccountFragment;
import de.jeisfeld.dsmessenger.main.account.AccountFragment.ActionType;
import de.jeisfeld.dsmessenger.main.account.ContactRegistry;
import de.jeisfeld.dsmessenger.main.lut.LutFragment;
import de.jeisfeld.dsmessenger.main.message.ConversationsFragment;
import de.jeisfeld.dsmessenger.main.message.MessageFragment;
import de.jeisfeld.dsmessenger.message.AdminMessageDetails;
import de.jeisfeld.dsmessenger.message.AdminMessageDetails.AdminType;
import de.jeisfeld.dsmessenger.message.LutMessageDetails;
import de.jeisfeld.dsmessenger.message.MessageActivity;
import de.jeisfeld.dsmessenger.message.MessageDetails;
import de.jeisfeld.dsmessenger.message.MessageDetails.MessageType;
import de.jeisfeld.dsmessenger.message.RandomimageMessageDetails;
import de.jeisfeld.dsmessenger.message.TextMessageDetails;
import de.jeisfeld.dsmessenger.util.PreferenceUtil;

/**
 * The service receiving firebase messages.
 */
public class FirebaseDsMessagingService extends FirebaseMessagingService {
	/**
	 * Update the token locally and on the server.
	 *
	 * @param context The context.
	 * @param token   The new token.
	 */
	public static void updateToken(final Context context, final String token) {
		if (AccountFragment.isLoggedIn()) {
			new HttpSender(context).sendMessage("db/usermanagement/changetoken.php", (response, responseData) -> {
				if (responseData != null && responseData.isSuccess()) {
					PreferenceUtil.setSharedPreferenceString(R.string.key_pref_messaging_token, token);
				}
			}, "token", token, "deviceId", Integer.toString(PreferenceUtil.getSharedPreferenceInt(R.string.key_pref_device_id, -1)));
		}
		else {
			PreferenceUtil.setSharedPreferenceString(R.string.key_pref_messaging_token, token);
		}
	}

	@Override
	public final void onMessageReceived(@NonNull final RemoteMessage message) {
		super.onMessageReceived(message);

		Log.d(Application.TAG, "Received message from " + message.getFrom() + " with priority " + message.getPriority());

		if (message.getData().size() > 0) {
			Log.i(Application.TAG, "Message data: " + message.getData());
		}

		MessageDetails messageDetails = MessageDetails.fromRemoteMessage(message);
		if (messageDetails.getType() == null) {
			return;
		}
		switch (messageDetails.getType()) {
		case ADMIN:
			AdminMessageDetails adminDetails = (AdminMessageDetails) messageDetails;
			switch (adminDetails.getAdminType()) {
			case INVITATION_ACCEPTED:
			case CONTACT_DELETED:
			case CONTACT_UPDATED:
				ContactRegistry.getInstance().refreshContacts(this, () -> AccountFragment.sendBroadcast(this, ActionType.CONTACTS_CHANGED));
				break;
			case DEVICE_ADDED:
			case DEVICE_DELETED:
			case DEVICE_UPDATED:
				AccountFragment.sendBroadcast(this, ActionType.DEVICES_CHANGED);
				break;
			case DEVICE_LOGGED_OUT:
				PreferenceUtil.removeSharedPreference(R.string.key_pref_username);
				PreferenceUtil.removeSharedPreference(R.string.key_pref_password);
				AccountFragment.removeStoredDeviceInfo();
				ContactRegistry.getInstance().cleanContacts();
				AccountFragment.sendBroadcast(this, ActionType.DEVICE_LOGGED_OUT);
				MessageFragment.sendBroadcast(this, MessageFragment.ActionType.DEVICE_LOGGED_OUT, null,
						adminDetails.getContact(), null, null);
				break;
			case CONVERSATION_EDITED:
				Conversation editedConversation =
						Application.getAppDatabase().getConversationDao().getConversationById(adminDetails.getValue("conversationId"));
				if (editedConversation != null) {
					editedConversation.setSubject(adminDetails.getValue("subject"));
					editedConversation.update();
					ConversationsFragment.sendBroadcast(this, ConversationsFragment.ActionType.CONVERSATION_EDITED, editedConversation);
					MessageFragment.sendBroadcast(this, MessageFragment.ActionType.CONVERSATION_EDITED,
							null, null, editedConversation, null);
					MessageActivity.sendBroadcast(this, MessageActivity.ActionType.CONVERSATION_EDITED, null, editedConversation);
				}
				break;
			case CONVERSATION_DELETED:
				Conversation deletedConversation =
						Application.getAppDatabase().getConversationDao().getConversationById(adminDetails.getValue("conversationId"));
				if (deletedConversation != null) {
					Application.getAppDatabase().getConversationDao().delete(deletedConversation);
					ConversationsFragment.sendBroadcast(this, ConversationsFragment.ActionType.CONVERSATION_DELETED, deletedConversation);
					MessageFragment.sendBroadcast(this, MessageFragment.ActionType.CONVERSATION_DELETED,
							null, null, deletedConversation, null);
					MessageActivity.sendBroadcast(this, MessageActivity.ActionType.CONVERSATION_DELETED, null, deletedConversation);
				}
				break;
			case MESSAGE_RECEIVED:
				MessageFragment.sendBroadcast(this, MessageFragment.ActionType.MESSAGE_RECEIVED, adminDetails.getMessageId(),
						adminDetails.getContact(), null, null);
				break;
			case MESSAGE_ACKNOWLEDGED:
				MessageFragment.sendBroadcast(this, MessageFragment.ActionType.MESSAGE_ACKNOWLEDGED, adminDetails.getMessageId(),
						adminDetails.getContact(), null, null);
				break;
			case MESSAGE_SELF_ACKNOWLEDGED:
				MessageActivity.sendBroadcast(this, MessageActivity.ActionType.MESSAGE_ACKNOWLEDGED, adminDetails.getMessageId(), null);
				break;
			case PING:
				new HttpSender(this).sendMessage(adminDetails.getContact(), adminDetails.getMessageId(), null,
						"messageType", MessageType.ADMIN.name(), "adminType", AdminType.PONG.name());
				break;
			case PONG:
				MessageFragment.sendBroadcast(this, MessageFragment.ActionType.PONG, adminDetails.getMessageId(),
						adminDetails.getContact(), null, null);
				LutFragment.sendBroadcast(this, MessageFragment.ActionType.PONG, adminDetails.getMessageId(), adminDetails.getContact());
				break;
			case UNKNOWN:
			default:
				break;
			}
			break;
		case TEXT:
			startActivity(MessageActivity.createIntent(this, (TextMessageDetails) messageDetails));
			break;
		case TEXT_ACKNOWLEDGE:
			MessageFragment.sendBroadcast(this, MessageFragment.ActionType.TEXT_ACKNOWLEDGE, messageDetails.getMessageId(),
					messageDetails.getContact(), null, (TextMessageDetails) messageDetails);
			break;
		case RANDOMIMAGE:
			RandomimageMessageDetails randomimageMessageDetails = (RandomimageMessageDetails) messageDetails;
			Intent randomImageIntent = new Intent("de.jeisfeld.randomimage.DISPLAY_RANDOM_IMAGE_FROM_EXTERNAL");
			switch (randomimageMessageDetails.getRandomImageOrigin()) {
			case NOTIFICATION:
				randomImageIntent.putExtra("de.eisfeldj.randomimage.NOTIFICATION_NAME", ((RandomimageMessageDetails) messageDetails).getNotificationName());
				sendBroadcast(randomImageIntent);
				break;
			case WIDGET:
				randomImageIntent.putExtra("de.eisfeldj.randomimage.WIDGET_NAME", ((RandomimageMessageDetails) messageDetails).getWidgetName());
				sendBroadcast(randomImageIntent);
				break;
			case UNKNOWN:
			default:
				break;
			}
			break;
		case LUT:
			LutMessageDetails lutMessageDetails = (LutMessageDetails) messageDetails;
			Intent lutIntent = new Intent("de.jeisfeld.dsmessenger.TRIGGER_LUT");
			lutIntent.putExtra("de.jeisfeld.dsmessenger.lut.messageType", lutMessageDetails.getLutMessageType().name());
			if (lutMessageDetails.getDuration() != null) {
				lutIntent.putExtra("de.jeisfeld.dsmessenger.lut.duration", lutMessageDetails.getDuration());
			}
			if (lutMessageDetails.getPowerFactor() != null) {
				lutIntent.putExtra("de.jeisfeld.dsmessenger.lut.powerFactor", lutMessageDetails.getPowerFactor());
			}
			sendBroadcast(lutIntent);
		case UNKNOWN:
		default:
			break;
		}
	}

	@Override
	public final void onNewToken(@NonNull final String token) {
		Log.i(Application.TAG, "Received new messaging token: " + token);
		updateToken(this, token);
	}
}
