package de.jeisfeld.dsmessenger.service;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.NonNull;
import de.jeisfeld.dsmessenger.Application;
import de.jeisfeld.dsmessenger.message.MessageActivity;
import de.jeisfeld.dsmessenger.message.MessageDetails;

public class FirebaseDSMessagingService extends FirebaseMessagingService {
	@Override
	public void onMessageReceived(@NonNull RemoteMessage message) {
		super.onMessageReceived(message);

		Log.d(Application.TAG,"Received message from " + message.getFrom() + " with priority " + message.getPriority());

		if (message.getData().size() > 0) {
			Log.i(Application.TAG,"Message data: " + message.getData());
		}

		startActivity(MessageActivity.createIntent(this, MessageDetails.fromRemoteMessage(message)));
	}

	@Override
	public void onNewToken(@NonNull String token) {
		Log.i(Application.TAG,"Received new messaging token: " + token);
	}
}
