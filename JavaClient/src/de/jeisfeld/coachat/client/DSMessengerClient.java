package de.jeisfeld.coachat.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

public class DSMessengerClient {

	public static void main(final String[] args) {
		new DSMessengerClient().sendMessageViaFirebase();
	}

	private void sendMessageViaFirebase() {
		try {
			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(TestKeys.AUTHKEY_JSON.getBytes(StandardCharsets.UTF_8))))
					.build();
			FirebaseApp.initializeApp(options);

			Message message = Message.builder()
					.putData("relationId", "3")
					.putData("messageType", "TEXT")
					.putData("messageText", "Test")
					.putData("priority", "HIGH")
					.putData("conversationId", UUID.randomUUID().toString())
					.putData("messageId", UUID.randomUUID().toString())
					.putData("messageTime", "2022-10-21T22:38:51.266Z")
					.putData("timestamp", "" + System.currentTimeMillis())
					.setToken(TestKeys.CLIENT_TOKEN)
					.build();

			String response = FirebaseMessaging.getInstance().send(message);

			log("Successfully sent message " + message);
			log("MessageId: " + response);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (FirebaseMessagingException e) {
			e.printStackTrace();
		}
	}

	private void log(final String message) {
		System.out.println(message);
	}
}
