package de.jeisfeld.dsmessenger.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.UUID;

import androidx.annotation.NonNull;
import de.jeisfeld.dsmessenger.Application;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.entity.Conversation;
import de.jeisfeld.dsmessenger.entity.ConversationFlags;
import de.jeisfeld.dsmessenger.entity.Message;
import de.jeisfeld.dsmessenger.http.HttpSender;
import de.jeisfeld.dsmessenger.main.account.AccountFragment;
import de.jeisfeld.dsmessenger.main.account.AccountFragment.ActionType;
import de.jeisfeld.dsmessenger.main.account.ContactRegistry;
import de.jeisfeld.dsmessenger.main.lut.LutFragment;
import de.jeisfeld.dsmessenger.main.message.ConversationsFragment;
import de.jeisfeld.dsmessenger.main.message.MessageFragment;
import de.jeisfeld.dsmessenger.main.message.MessageFragment.MessageStatus;
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
		TextMessageDetails textMessageDetails;
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
				ContactRegistry.getInstance().refreshContacts(this, () -> {
					AccountFragment.sendBroadcast(this, ActionType.CONTACTS_UPDATED);
					ConversationsFragment.sendBroadcast(this, ConversationsFragment.ActionType.CONTACTS_UPDATED, null);
				});
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
					editedConversation.setConversationFlags(ConversationFlags.fromString(adminDetails.getValue("conversationFlags")));
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
				Message receivedMessage = Application.getAppDatabase().getMessageDao().getMessageById(adminDetails.getMessageId());
				if (receivedMessage != null) {
					receivedMessage.setStatus(MessageStatus.MESSAGE_RECEIVED);
					receivedMessage.update();
					Conversation receivedConversation =
							Application.getAppDatabase().getConversationDao().getConversationById(adminDetails.getValue("conversationId"));
					MessageFragment.sendBroadcast(this, MessageFragment.ActionType.MESSAGE_RECEIVED, adminDetails.getMessageId(),
							adminDetails.getContact(), receivedConversation, null);
					MessageActivity.sendBroadcast(this, MessageActivity.ActionType.MESSAGE_RECEIVED, null, receivedConversation);
				}
				break;
			case MESSAGE_ACKNOWLEDGED:
				String messageIdsString = adminDetails.getValue("messageIds");
				if (messageIdsString != null && messageIdsString.length() > 0) {
					Application.getAppDatabase().getMessageDao().acknowledgeMessages(messageIdsString.split(","));
					MessageFragment.sendBroadcast(this, MessageFragment.ActionType.MESSAGE_ACKNOWLEDGED, adminDetails.getMessageId(),
							adminDetails.getContact(),
							Application.getAppDatabase().getConversationDao().getConversationById(adminDetails.getValue("conversationId")), null);
				}
				break;
			case MESSAGE_SELF_ACKNOWLEDGED:
			case MESSAGE_SELF_RESPONDED:
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
			textMessageDetails = (TextMessageDetails) messageDetails;
			UUID conversationId = textMessageDetails.getConversationId();
			Conversation messageConversation = Application.getAppDatabase().getConversationDao().getConversationById(conversationId);
			if (messageConversation == null && textMessageDetails.getMessageText() != null && textMessageDetails.getMessageText().length() > 0) {
				messageConversation = Conversation.createNewConversation(textMessageDetails);
				messageConversation.insertIfNew(textMessageDetails.getMessageText());
				ConversationsFragment.sendBroadcast(this, ConversationsFragment.ActionType.CONVERSATION_ADDED, messageConversation);
			}
			messageConversation.setPreparedMessage(textMessageDetails.getPreparedMessage());
			Message textMessage = new Message(textMessageDetails.getMessageText(), false, textMessageDetails.getMessageId(),
					conversationId, textMessageDetails.getTimestamp(), MessageStatus.MESSAGE_RECEIVED);
			if (textMessage.getMessageText() != null && textMessage.getMessageText().length() > 0) {
				textMessage.store(messageConversation);
				messageConversation.updateWithNewMessage();
				ConversationsFragment.sendBroadcast(this, ConversationsFragment.ActionType.CONVERSATION_EDITED, messageConversation);
			}
			MessageFragment.sendBroadcast(this, MessageFragment.ActionType.MESSAGE_RECEIVED, textMessageDetails.getMessageId(),
					textMessageDetails.getContact(), messageConversation, textMessage);
			Application.getAppDatabase().getMessageDao().acknowledgeMessages(textMessageDetails.getMessageIds());
			startActivity(MessageActivity.createIntent(this, textMessageDetails, textMessage));
			break;
		case TEXT_RESPONSE:
			textMessageDetails = (TextMessageDetails) messageDetails;
			Conversation conversation = Application.getAppDatabase().getConversationDao().getConversationById(textMessageDetails.getConversationId());
			if (textMessageDetails.getPreparedMessage() != null && textMessageDetails.getPreparedMessage().length() > 0) {
				conversation.setPreparedMessage(textMessageDetails.getPreparedMessage());
			}
			new HttpSender(this).sendMessage("db/conversation/updatemessagestatus.php",
					messageDetails.getContact(), messageDetails.getMessageId(), null,
					"messageType", MessageType.ADMIN.name(), "adminType", AdminType.MESSAGE_RECEIVED.name(), "conversationId",
					textMessageDetails.getConversationId().toString());
			Message receivedMessage = new Message(textMessageDetails.getMessageText(), false, textMessageDetails.getMessageId(),
					textMessageDetails.getConversationId(), textMessageDetails.getTimestamp(), MessageStatus.MESSAGE_RECEIVED);
			receivedMessage.store(conversation);
			Application.getAppDatabase().getMessageDao().acknowledgeMessages(textMessageDetails.getMessageIds());
			MessageFragment.sendBroadcast(this, MessageFragment.ActionType.TEXT_RESPONSE, messageDetails.getMessageId(),
					messageDetails.getContact(), conversation, receivedMessage);
			break;
		case TEXT_OWN:
			textMessageDetails = (TextMessageDetails) messageDetails;
			Conversation conversation2 = Application.getAppDatabase().getConversationDao().getConversationById(textMessageDetails.getConversationId());
			if (conversation2 == null && textMessageDetails.getMessageText() != null && textMessageDetails.getMessageText().length() > 0) {
				conversation2 = Conversation.createNewConversation(textMessageDetails);
				conversation2.insertIfNew(textMessageDetails.getMessageText());
				ConversationsFragment.sendBroadcast(this, ConversationsFragment.ActionType.CONVERSATION_ADDED, conversation2);
			}

			Message sentMessage = new Message(textMessageDetails.getMessageText(), true, textMessageDetails.getMessageId(),
					textMessageDetails.getConversationId(), textMessageDetails.getTimestamp(), MessageStatus.MESSAGE_SENT);
			if (sentMessage.getMessageText() != null && sentMessage.getMessageText().length() > 0) {
				conversation2.setPreparedMessage(null);
				sentMessage.store(conversation2);
				conversation2.updateWithNewMessage();
				ConversationsFragment.sendBroadcast(this, ConversationsFragment.ActionType.CONVERSATION_EDITED, conversation2);
			}
			Application.getAppDatabase().getMessageDao().acknowledgeMessages(textMessageDetails.getMessageIds());
			MessageFragment.sendBroadcast(this, MessageFragment.ActionType.MESSAGE_SENT, messageDetails.getMessageId(),
					messageDetails.getContact(), conversation2, sentMessage);
			break;
		case RANDOMIMAGE:
			RandomimageMessageDetails randomimageMessageDetails = (RandomimageMessageDetails) messageDetails;
			Intent randomImageIntent = new Intent("de.jeisfeld.randomimage.DISPLAY_RANDOM_IMAGE_FROM_EXTERNAL");
			switch (randomimageMessageDetails.getRandomImageOrigin()) {
			case NOTIFICATION:
				randomImageIntent.putExtra("de.eisfeldj.randomimage.NOTIFICATION_NAME",
						((RandomimageMessageDetails) messageDetails).getNotificationName());
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
			if (lutMessageDetails.getChannel() != null) {
				lutIntent.putExtra("de.jeisfeld.dsmessenger.lut.channel", lutMessageDetails.getChannel());
			}
			if (lutMessageDetails.getPower() != null) {
				lutIntent.putExtra("de.jeisfeld.dsmessenger.lut.power", lutMessageDetails.getPower());
			}
			if (lutMessageDetails.getPowerFactor() != null) {
				lutIntent.putExtra("de.jeisfeld.dsmessenger.lut.powerFactor", lutMessageDetails.getPowerFactor());
			}
			if (lutMessageDetails.getFrequency() != null) {
				lutIntent.putExtra("de.jeisfeld.dsmessenger.lut.frequency", lutMessageDetails.getFrequency());
			}
			if (lutMessageDetails.getWave() != null) {
				lutIntent.putExtra("de.jeisfeld.dsmessenger.lut.wave", lutMessageDetails.getWave());
			}
			sendBroadcast(lutIntent);
			break;
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
