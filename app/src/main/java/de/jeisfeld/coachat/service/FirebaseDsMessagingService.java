package de.jeisfeld.coachat.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.UUID;

import androidx.annotation.NonNull;
import de.jeisfeld.coachat.Application;
import de.jeisfeld.coachat.R;
import de.jeisfeld.coachat.entity.Conversation;
import de.jeisfeld.coachat.entity.ConversationFlags;
import de.jeisfeld.coachat.entity.Message;
import de.jeisfeld.coachat.http.HttpSender;
import de.jeisfeld.coachat.main.account.AccountFragment;
import de.jeisfeld.coachat.main.account.AccountFragment.ActionType;
import de.jeisfeld.coachat.main.account.ContactRegistry;
import de.jeisfeld.coachat.main.lut.LutFragment;
import de.jeisfeld.coachat.main.message.ConversationsFragment;
import de.jeisfeld.coachat.main.message.MessageFragment;
import de.jeisfeld.coachat.main.message.MessageFragment.MessageStatus;
import de.jeisfeld.coachat.message.AdminMessageDetails;
import de.jeisfeld.coachat.message.AdminMessageDetails.AdminType;
import de.jeisfeld.coachat.message.LutMessageDetails;
import de.jeisfeld.coachat.message.MessageActivity;
import de.jeisfeld.coachat.message.MessageDetails;
import de.jeisfeld.coachat.message.MessageDetails.MessageType;
import de.jeisfeld.coachat.message.MessageDisplayStrategy.MessageDisplayType;
import de.jeisfeld.coachat.message.TextMessageDetails;
import de.jeisfeld.coachat.util.PreferenceUtil;

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
			case PASSWORD_CHANGED:
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
					if (adminDetails.getValue("conversationFlags") != null) {
						editedConversation.setConversationFlags(ConversationFlags.fromString(adminDetails.getValue("conversationFlags")));
					}
					if (adminDetails.getValue("archived") != null) {
						editedConversation.setArchived(Boolean.parseBoolean(adminDetails.getValue("archived")));
					}
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
					cancelNotification(deletedConversation.getRelationId());
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
			case MESSAGE_DELETED:
				Message deletedMessage =
						Application.getAppDatabase().getMessageDao().getMessageById(adminDetails.getMessageId());
				if (deletedMessage != null) {
					Conversation affectedConversation =
							Application.getAppDatabase().getConversationDao().getConversationById(adminDetails.getValue("conversationId"));
					Application.getAppDatabase().getMessageDao().delete(deletedMessage);
					MessageFragment.sendBroadcast(this, MessageFragment.ActionType.MESSAGE_DELETED,
							null, null, affectedConversation, deletedMessage);
					MessageActivity.sendBroadcast(this, MessageActivity.ActionType.MESSAGE_DELETED, deletedMessage.getMessageId(), affectedConversation);
				}
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
			MessageDisplayType displayType = textMessageDetails.getDisplayStrategy().getMessageDisplayType();
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
			if (displayType == MessageDisplayType.ACTION) {
				startActivity(MessageActivity.createIntent(this, textMessageDetails, textMessage));
			}
			displayNotification(textMessageDetails, textMessage);
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
			displayNotification(textMessageDetails, receivedMessage);
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
		case LUT:
			LutMessageDetails lutMessageDetails = (LutMessageDetails) messageDetails;
			Intent lutIntent = new Intent("de.jeisfeld.coachat.TRIGGER_LUT");
			lutIntent.putExtra("de.jeisfeld.coachat.lut.messageType", lutMessageDetails.getLutMessageType().name());
			if (lutMessageDetails.getDuration() != null) {
				lutIntent.putExtra("de.jeisfeld.coachat.lut.duration", lutMessageDetails.getDuration());
			}
			if (lutMessageDetails.getChannel() != null) {
				lutIntent.putExtra("de.jeisfeld.coachat.lut.channel", lutMessageDetails.getChannel());
			}
			if (lutMessageDetails.getPower() != null) {
				lutIntent.putExtra("de.jeisfeld.coachat.lut.power", lutMessageDetails.getPower());
			}
			if (lutMessageDetails.getPowerFactor() != null) {
				lutIntent.putExtra("de.jeisfeld.coachat.lut.powerFactor", lutMessageDetails.getPowerFactor());
			}
			if (lutMessageDetails.getFrequency() != null) {
				lutIntent.putExtra("de.jeisfeld.coachat.lut.frequency", lutMessageDetails.getFrequency());
			}
			if (lutMessageDetails.getWave() != null) {
				lutIntent.putExtra("de.jeisfeld.coachat.lut.wave", lutMessageDetails.getWave());
			}
			sendBroadcast(lutIntent);
			break;
		case UNKNOWN:
		default:
			break;
		}
	}

	/**
	 * Display a notification.
	 *
	 * @param textMessageDetails The text message details.
	 * @param textMessage        The text message.
	 */
	public void displayNotification(final TextMessageDetails textMessageDetails, final Message textMessage) {
		String message = textMessageDetails.getMessageText();
		String title = getString(R.string.notification_title, textMessageDetails.getContact().getName());

		Notification.Builder notificationBuilder;
		notificationBuilder = new Notification.Builder(this, "MessageNotification");
		notificationBuilder.setSmallIcon(R.drawable.ic_notification)
				.setContentTitle(title)
				.setContentText(message)
				.setChannelId(Application.NOTIFICATION_CHANNEL_ID)
				.setStyle(new Notification.BigTextStyle().bigText(message));

		Intent actionIntent = MessageActivity.createIntent(this, textMessageDetails, textMessage);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, textMessageDetails.getContact().getRelationId(), actionIntent,
				PendingIntent.FLAG_CANCEL_CURRENT | (VERSION.SDK_INT >= VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE : 0));
		notificationBuilder.setContentIntent(pendingIntent);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(textMessageDetails.getContact().getRelationId(), notificationBuilder.build());
	}

	/**
	 * Cancel a notification.
	 *
	 * @param relationId The relationId
	 */
	public void cancelNotification(final int relationId) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(relationId);
	}

	@Override
	public final void onNewToken(@NonNull final String token) {
		Log.i(Application.TAG, "Received new messaging token: " + token);
		updateToken(this, token);
	}
}
