package de.jeisfeld.dsmessenger.message;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import de.jeisfeld.dsmessenger.databinding.ActivityMessageBinding;
import de.jeisfeld.dsmessenger.http.HttpSender;
import de.jeisfeld.dsmessenger.main.account.Contact;
import de.jeisfeld.dsmessenger.message.AdminMessageDetails.AdminType;
import de.jeisfeld.dsmessenger.message.MessageDetails.MessageType;

/**
 * Activity to display messages.
 */
public class MessageActivity extends AppCompatActivity {
	/**
	 * The intent action for broadcast to this fragment.
	 */
	private static final String BROADCAST_ACTION = "de.jeisfeld.dsmessenger.message.MessageActivity";
	/**
	 * The resource key for the message.
	 */
	private static final String STRING_EXTRA_MESSAGE_DETAILS = "de.jeisfeld.dsmessenger.MESSAGE_DETAILS";
	/**
	 * String for storing message in instance state.
	 */
	private static final String STRING_MESSAGE = "MESSAGE";
	/**
	 * The contact currently on top.
	 */
	private static Contact currentTopContact = null;
	/**
	 * The binding of the activity.
	 */
	private ActivityMessageBinding binding;
	/**
	 * The message text.
	 */
	private String messageText;
	/**
	 * The message vibration.
	 */
	private MessageVibration messageVibration = null;
	/**
	 * The last received messageId.
	 */
	private TextMessageDetails lastTextMessageDetails;

	/**
	 * The local broadcast receiver to do actions sent to this fragment.
	 */
	private final BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (intent != null) {
				ActionType actionType = (ActionType) intent.getSerializableExtra("actionType");
				switch (actionType) {
				case MESSAGE_ACKNOWLEDGED:
					UUID messageId = (UUID) intent.getSerializableExtra("messageId");
					if (messageId != null && lastTextMessageDetails != null && messageId.equals(lastTextMessageDetails.getMessageId())) {
						cancelLastIntentEffects();
						binding.buttonAcknowledge.setVisibility(View.INVISIBLE);
					}
					break;
				default:
					break;
				}
			}
		}
	};
	/**
	 * Broadcastmanager to update fragment from external.
	 */
	private LocalBroadcastManager broadcastManager;

	/**
	 * Send a broadcast to this fragment.
	 *
	 * @param context    The context.
	 * @param actionType The action type.
	 * @param messageId  The messageId.
	 * @param parameters The parameters.
	 */
	public static void sendBroadcast(final Context context, final ActionType actionType, final UUID messageId, final String... parameters) {
		final Intent intent = new Intent(BROADCAST_ACTION);
		final Bundle bundle = new Bundle();
		bundle.putSerializable("actionType", actionType);
		bundle.putSerializable("messageId", messageId);
		int i = 0;
		while (i < parameters.length - 1) {
			String key = parameters[i++];
			String value = parameters[i++];
			bundle.putString(key, value);
		}
		intent.putExtras(bundle);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	/**
	 * Static helper method to create an intent for this activity.
	 *
	 * @param context            The context in which this activity is started.
	 * @param textMessageDetails The details of the message to be displayed
	 * @return the intent.
	 */
	public static Intent createIntent(final Context context, final TextMessageDetails textMessageDetails) {
		Intent intent = new Intent(context, MessageActivity.class);
		intent.putExtra(STRING_EXTRA_MESSAGE_DETAILS, textMessageDetails);
		if (textMessageDetails.getContact() != null && currentTopContact != null
				&& textMessageDetails.getContact().getRelationId() != currentTopContact.getRelationId()) {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
		}
		else {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
		}
		return intent;
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		binding = ActivityMessageBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		if (savedInstanceState != null && savedInstanceState.getCharSequence(STRING_MESSAGE) != null) {
			messageText = savedInstanceState.getString(STRING_MESSAGE);
		}
		else {
			messageText = extractMessageText(getIntent());
		}

		handleIntentData(getIntent());

		broadcastManager = LocalBroadcastManager.getInstance(this);
		IntentFilter actionReceiver = new IntentFilter();
		actionReceiver.addAction(BROADCAST_ACTION);
		broadcastManager.registerReceiver(localBroadcastReceiver, actionReceiver);
	}

	@Override
	protected final void onDestroy() {
		super.onDestroy();
		broadcastManager.unregisterReceiver(localBroadcastReceiver);
	}

	@Override
	protected final void onStop() {
		super.onStop();
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	protected final void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		if (messageText.length() > 0 && messageText.charAt(messageText.length() - 1) != '\n') {
			messageText += "\n";
		}
		messageText += extractMessageText(intent);
		handleIntentData(intent);
		binding.buttonAcknowledge.setVisibility(View.VISIBLE);
	}

	@Override
	protected final void onSaveInstanceState(@NonNull final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STRING_MESSAGE, messageText);
	}

	/**
	 * Extract the message text form an intent.
	 *
	 * @param intent The intent.
	 * @return The message text.
	 */
	private String extractMessageText(final Intent intent) {
		return ((TextMessageDetails) intent.getSerializableExtra(STRING_EXTRA_MESSAGE_DETAILS)).getMessageText();
	}

	/**
	 * Handle the data of a new intent.
	 *
	 * @param intent The intent.
	 */
	private void handleIntentData(final Intent intent) {
		cancelLastIntentEffects();
		binding.textviewMessage.setText(messageText);

		TextMessageDetails textMessageDetails = (TextMessageDetails) intent.getSerializableExtra(STRING_EXTRA_MESSAGE_DETAILS);
		MessageDisplayStrategy displayStrategy = textMessageDetails.getDisplayStrategy();
		MessageActivity.currentTopContact = textMessageDetails.getContact();

		if (displayStrategy.isVibrate()) {
			messageVibration = new MessageVibration(this);
			messageVibration.vibrate(displayStrategy);
		}
		if (displayStrategy.isDisplayOnLockScreen()) {
			displayOnLockScreen(true);
		}
		if (displayStrategy.isLockMessage()) {
			startLockTask();
		}
		if (displayStrategy.isKeepScreenOn()) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		sendConfirmation(AdminType.MESSAGE_RECEIVED, textMessageDetails.getMessageId(), textMessageDetails.getContact());

		binding.buttonAcknowledge.setOnClickListener(v -> {
			if (messageVibration != null) {
				messageVibration.cancelVibration();
			}
			sendConfirmation(AdminType.MESSAGE_ACKNOWLEDGED, textMessageDetails.getMessageId(), textMessageDetails.getContact());
			new HttpSender(this).sendSelfMessage(textMessageDetails.getMessageId(), null,
					"messageType", MessageType.ADMIN.name(), "adminType", AdminType.MESSAGE_SELF_ACKNOWLEDGED.name());
			binding.buttonAcknowledge.setVisibility(View.INVISIBLE);
		});

		lastTextMessageDetails = textMessageDetails;
	}

	/**
	 * Cancel the effects of the last text message.
	 */
	private void cancelLastIntentEffects() {
		if (lastTextMessageDetails != null) {
			MessageDisplayStrategy lastDisplayStrategy = lastTextMessageDetails.getDisplayStrategy();
			if (lastDisplayStrategy.isVibrate()) {
				if (messageVibration != null) {
					messageVibration.cancelVibration();
				}
			}
			if (lastDisplayStrategy.isDisplayOnLockScreen()) {
				displayOnLockScreen(false);
			}
			if (lastDisplayStrategy.isLockMessage()) {
				stopLockTask();
			}
			if (lastDisplayStrategy.isKeepScreenOn()) {
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			}
		}
	}

	/**
	 * Send confirmation for the message.
	 *
	 * @param adminType The type of confirmation.
	 * @param messageId The messageId.
	 * @param contact   The contact.
	 */
	private void sendConfirmation(final AdminType adminType, final UUID messageId, final Contact contact) {
		new HttpSender(this).sendMessage(contact, messageId, null,
				"messageType", MessageType.ADMIN.name(), "adminType", adminType.name());
	}

	/**
	 * Ensure that the message is displayed on top of lock screen.
	 *
	 * @param displayOnLockScreen Flag indicating if the effect should be added or removed.
	 */
	private void displayOnLockScreen(final boolean displayOnLockScreen) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
			setTurnScreenOn(displayOnLockScreen);
			setShowWhenLocked(displayOnLockScreen);
		}
		else {
			Window window = getWindow();
			if (displayOnLockScreen) {
				window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
				window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
			}
			else {
				window.clearFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
				window.clearFlags(LayoutParams.FLAG_TURN_SCREEN_ON);
			}
		}
	}

	/**
	 * Action that can be sent to this fragment.
	 */
	public enum ActionType {
		/**
		 * Inform about message acknowledged.
		 */
		MESSAGE_ACKNOWLEDGED
	}
}
