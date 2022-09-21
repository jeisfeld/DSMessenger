package de.jeisfeld.dsmessenger;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.NonNull;
import de.jeisfeld.dsmessenger.util.Logger;

public class FirebaseDSMessagingService extends FirebaseMessagingService {
	@Override
	public void onMessageReceived(@NonNull RemoteMessage message) {
		super.onMessageReceived(message);

		Logger.log("Received message from: " + message.getFrom());

		if (message.getData().size() > 0) {
			Logger.log("Message data: " + message.getData());
		}
	}

	@Override
	public void onNewToken(@NonNull String token) {
		Logger.log("Received new messaging token: " + token);
	}
}
