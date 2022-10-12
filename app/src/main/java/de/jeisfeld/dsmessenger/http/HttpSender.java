package de.jeisfeld.dsmessenger.http;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import de.jeisfeld.dsmessenger.Application;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.main.account.Contact;
import de.jeisfeld.dsmessenger.main.account.Contact.ContactStatus;
import de.jeisfeld.dsmessenger.main.account.Device;
import de.jeisfeld.dsmessenger.util.DateUtil;
import de.jeisfeld.dsmessenger.util.PreferenceUtil;

/**
 * Helper class for sending http(s) messages to server.
 */
public class HttpSender {
	/**
	 * The context.
	 */
	private final Context context;

	/**
	 * Constructor.
	 *
	 * @param context The context.
	 */
	public HttpSender(Context context) {
		this.context = context;
	}

	/**
	 * Send a POST message to Server.
	 *
	 * @param urlPostfix     The postfix of the URL.
	 * @param addCredentials Flag indicating if username, password should be added.
	 * @param contact        The contact.
	 * @param messageId      The messageId
	 * @param listener       The response listener.
	 * @param parameters     The POST parameters.
	 */
	public void sendMessage(final String urlPostfix, final boolean addCredentials, final Contact contact, final UUID messageId,
							final OnHttpResponseListener listener, final String... parameters) {
		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(HttpCredentials.USERNAME, HttpCredentials.PASSWORD.toCharArray());
			}
		});
		String urlBase;
		if (PreferenceUtil.getSharedPreferenceBoolean(R.string.key_pref_use_test_server)) {
			urlBase = "https://pc-joerg:8101/";
		}
		else {
			urlBase = "https://jeisfeld.de/dsmessenger/";
		}

		new Thread(() -> {
			Reader in = null;
			try {
				URL url = new URL(urlBase + urlPostfix);
				URLConnection urlConnection = url.openConnection();
				urlConnection.setDoOutput(true);
				((HttpsURLConnection) urlConnection).setRequestMethod("POST");
				urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				byte[] postDataBytes = getPostData(addCredentials, contact, messageId, parameters).getBytes(StandardCharsets.UTF_8);
				urlConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
				urlConnection.getOutputStream().write(postDataBytes);
				in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
				StringBuilder result = new StringBuilder();
				for (int c; (c = in.read()) >= 0; ) {
					result.append((char) c);
				}
				if (listener != null) {
					ResponseData responseData = ResponseData.extractResponseData(context, result.toString());
					listener.onHttpResponse(result.toString(), responseData);
				}
			}
			catch (IOException e) {
				Log.e(Application.TAG, "Invalid URL", e);
			}
			finally {
				if (in != null) {
					try {
						in.close();
					}
					catch (IOException e) {
						// ignore
					}
				}
			}
		}).start();
	}

	/**
	 * Send a POST message to Server, including credentials.
	 *
	 * @param urlPostfix The postfix of the URL.
	 * @param contact    The contact.
	 * @param messageId  The messageId.
	 * @param listener   The response listener.
	 * @param parameters The POST parameters.
	 */
	public void sendMessage(final String urlPostfix, final Contact contact, final UUID messageId,
							final OnHttpResponseListener listener, final String... parameters) {
		sendMessage(urlPostfix, true, contact, messageId, listener, parameters);
	}

	/**
	 * Send a POST message to Server, including credentials.
	 *
	 * @param contact    The contact.
	 * @param messageId  The messageId.
	 * @param listener   The response listener.
	 * @param parameters The POST parameters.
	 */
	public void sendMessage(final Contact contact, final UUID messageId, final OnHttpResponseListener listener, final String... parameters) {
		sendMessage("firebase/sendmessage.php", contact, messageId, listener, parameters);
	}

	/**
	 * Send a POST message to Server, including credentials.
	 *
	 * @param messageId  The messageId.
	 * @param listener   The response listener.
	 * @param parameters The POST parameters.
	 */
	public void sendSelfMessage(final UUID messageId, final OnHttpResponseListener listener, final String... parameters) {
		sendMessage("firebase/sendselfmessage.php", new Contact(-1, null, null, 0, false, null, null),
				messageId, listener, parameters);
	}

	/**
	 * Send a POST message to Server, including credentials but without contact.
	 *
	 * @param urlPostfix The postfix of the URL.
	 * @param listener   The response listener.
	 * @param parameters The POST parameters.
	 */
	public void sendMessage(final String urlPostfix, final OnHttpResponseListener listener, final String... parameters) {
		sendMessage(urlPostfix, null, null, listener, parameters);
	}

	/**
	 * Get post data from the parameters, which are name value entries.
	 *
	 * @param addCredentials Flag indicating if username, password should be added.
	 * @param contact        The related contact.
	 * @param messageId      The unique message id.
	 * @param parameters     the name value entries.
	 * @return The data to be posted.
	 */
	private String getPostData(final boolean addCredentials, final Contact contact,
							   final UUID messageId, final String... parameters) throws UnsupportedEncodingException {
		StringBuilder postData = new StringBuilder();
		postData.append("messageTime=");
		postData.append(DateUtil.instantToJsonDate(Instant.now()));
		if (messageId != null) {
			postData.append("&messageId=");
			postData.append(messageId);
		}
		int i = 0;
		while (i < parameters.length - 1) {
			final String name = parameters[i++];
			final String value = parameters[i++];
			if (value != null) {
				postData.append('&');
				postData.append(URLEncoder.encode(name, StandardCharsets.UTF_8.name()));
				postData.append('=');
				postData.append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
			}
		}
		if (addCredentials) {
			postData.append("&username=");
			postData.append(URLEncoder.encode(PreferenceUtil.getSharedPreferenceString(R.string.key_pref_username), StandardCharsets.UTF_8.name()));
			postData.append("&password=");
			postData.append(URLEncoder.encode(PreferenceUtil.getSharedPreferenceString(R.string.key_pref_password), StandardCharsets.UTF_8.name()));
		}
		if (contact != null) {
			if (contact.getRelationId() > 0) {
				postData.append("&relationId=");
				postData.append(contact.getRelationId());
				postData.append("&contactId=");
				postData.append(contact.getContactId());
				postData.append("&isSlave=");
				postData.append(contact.isSlave() ? "1" : "");
				postData.append("&isConnected=");
				postData.append(contact.getStatus() == ContactStatus.CONNECTED ? "1" : "");
			}
			else { // self sending
				postData.append("&deviceId=");
				postData.append(PreferenceUtil.getSharedPreferenceInt(R.string.key_pref_device_id, -1));
			}
		}
		return postData.toString();
	}

	/**
	 * Handler for HTTP/HTTPS response.
	 */
	public interface OnHttpResponseListener {
		/**
		 * Handle HTTP/HTTPS response.
		 *
		 * @param response     The response as String.
		 * @param responseData The response as data.
		 */
		void onHttpResponse(String response, ResponseData responseData);
	}

	/**
	 * Response data from server.
	 */
	public static final class ResponseData {
		/**
		 * Success status of the call.
		 */
		private final boolean success;
		/**
		 * Error code (if not success).
		 */
		private final int errorCode;
		/**
		 * Error message (if not success).
		 */
		private final String errorMessage;
		/**
		 * Response data (if success).
		 */
		private final Map<String, Object> data;

		private ResponseData(final boolean success, final int errorCode, final String errorMessage, final Map<String, Object> data) {
			this.success = success;
			this.errorCode = errorCode;
			this.errorMessage = errorMessage;
			this.data = data;
		}

		/**
		 * Extract response data from server response.
		 *
		 * @param context  The context
		 * @param response The server response.
		 * @return The response data.
		 */
		private static ResponseData extractResponseData(final Context context, final String response) {
			try {
				JSONObject jsonObject = new JSONObject(response);
				boolean success = "success".equals(jsonObject.getString("status"));
				Map<String, Object> data = new HashMap<>();
				if (success) {
					for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
						String key = it.next();
						// noinspection StatementWithEmptyBody
						if ("status".equals(key)) {
							// do nothing
						}
						else if ("contacts".equals(key)) {
							SparseArray<Contact> contacts = new SparseArray<>();
							JSONArray jsonArray = jsonObject.getJSONArray(key);
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject jsonContact = jsonArray.getJSONObject(i);
								int relationId = jsonContact.getInt("relationId");
								String connectionCode = jsonContact.getString("connectionCode");
								String contactName = jsonContact.getString("contactName");
								String myName = jsonContact.getString("myName");
								int contactId = jsonContact.getInt("contactId");
								boolean isSlave = jsonContact.getBoolean("isSlave");
								boolean isConfirmed = jsonContact.getBoolean("isConfirmed");
								Contact contact = new Contact(relationId, contactName, myName, contactId, isSlave, connectionCode,
										isConfirmed ? ContactStatus.CONNECTED : ContactStatus.INVITED);
								contacts.put(relationId, contact);
							}
							data.put(key, contacts);
						}
						else if ("devices".equals(key)) {
							List<Device> devices = new ArrayList<>();
							JSONArray jsonArray = jsonObject.getJSONArray(key);
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject jsonContact = jsonArray.getJSONObject(i);
								int deviceId = jsonContact.getInt("deviceId");
								String deviceName = jsonContact.getString("deviceName");
								boolean muted = jsonContact.getBoolean("muted");
								boolean isThis = jsonContact.getBoolean("isClient");
								Device device = new Device(deviceId, deviceName, muted, isThis);
								devices.add(device);
							}
							data.put(key, devices);
						}
						else if (jsonObject.get(key) instanceof Integer) {
							data.put(key, jsonObject.getInt(key));
						}
						else if (jsonObject.get(key) instanceof Boolean) {
							data.put(key, jsonObject.getBoolean(key));
						}
						else {
							data.put(key, jsonObject.getString(key));
						}
					}
					return new ResponseData(true, 0, "", data);
				}
				else {
					int errorCode = jsonObject.getInt("errorcode");
					String errorMessage = jsonObject.getString("errormessage");
					for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
						String key = it.next();
						if (!"status".equals(key) && !"errorcode".equals(key) && !"errormessage".equals(key)) {
							data.put(key, jsonObject.getString(key));
						}
					}
					return new ResponseData(false, errorCode, errorMessage, data);
				}
			}
			catch (Exception e) {
				Log.e(Application.TAG, "Failed to extract response data from " + response, e);
				return new ResponseData(false, 900, "Error parsing JSON: " + e.getMessage(), new HashMap<>());
			}
		}

		public boolean isSuccess() {
			return success;
		}

		public int getErrorCode() {
			return errorCode;
		}

		/**
		 * Get the error String, mapping error code if applicable.
		 *
		 * @param context The context.
		 * @return The mapped error String.
		 */
		public String getMappedErrorMessage(final Context context) {
			if (context == null) {
				return getErrorMessage();
			}
			switch (getErrorCode()) {
			case 101:
				return context.getString(R.string.error_connection_failed);
			case 105:
				return context.getString(R.string.error_invalid_credentials);
			case 106:
				return context.getString(R.string.error_contact_not_connected);
			case 111:
				return context.getString(R.string.error_missing_username);
			case 112:
				return context.getString(R.string.error_password_too_short);
			case 113:
				return context.getString(R.string.error_user_already_exists, data.get("username"));
			case 114:
				return context.getString(R.string.error_invalid_connectioncode);
			case 115:
				return context.getString(R.string.error_user_already_logged_in, data.get("username"));
			case 116:
				return context.getString(R.string.error_device_already_exists, data.get("deviceName"));
			default:
				return getErrorMessage();
			}
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		public Map<String, Object> getData() {
			return data;
		}
	}

}
