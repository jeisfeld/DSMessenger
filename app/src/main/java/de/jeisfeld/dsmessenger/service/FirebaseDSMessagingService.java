package de.jeisfeld.dsmessenger.service;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.NonNull;
import de.jeisfeld.dsmessenger.Application;
import de.jeisfeld.dsmessenger.message.MessageActivity;
import de.jeisfeld.dsmessenger.message.MessageDetails;
import de.jeisfeld.dsmessenger.message.RandomimageMessageDetails;
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
		case RANDOMIMAGE:
			RandomimageMessageDetails randomimageMessageDetails = (RandomimageMessageDetails) messageDetails;
			Intent intent = new Intent("de.jeisfeld.randomimage.DISPLAY_RANDOM_IMAGE_FROM_EXTERNAL");
			switch(randomimageMessageDetails.getRandomImageOrigin()) {
			case NOTIFICATION:
				intent.putExtra("de.eisfeldj.randomimage.NOTIFICATION_NAME", ((RandomimageMessageDetails) messageDetails).getNotificationName());
				sendBroadcast(intent);
				break;
			case WIDGET:
				intent.putExtra("de.eisfeldj.randomimage.WIDGET_NAME", ((RandomimageMessageDetails) messageDetails).getWidgetName());
				sendBroadcast(intent);
				break;
			case UNKNOWN:
			default:
				break;
			}
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
