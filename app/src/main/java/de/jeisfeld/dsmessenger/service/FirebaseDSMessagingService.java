package de.jeisfeld.dsmessenger.service;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.NonNull;
import de.jeisfeld.dsmessenger.Application;
import de.jeisfeld.dsmessenger.message.MessageActivity;
import de.jeisfeld.dsmessenger.message.MessageDetails;
import de.jeisfeld.dsmessenger.message.TextMessageDetails;

public class FirebaseDSMessagingService extends FirebaseMessagingService {
	@Override
	public void onMessageReceived(@NonNull RemoteMessage message) {
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
		case TEXT:
			startActivity(MessageActivity.createIntent(this, (TextMessageDetails) messageDetails));
			break;
		case UNKNOWN:
		default:
			break;
		}
	}

	@Override
	public void onNewToken(@NonNull String token) {
		Log.i(Application.TAG, "Received new messaging token: " + token);
	}
}
