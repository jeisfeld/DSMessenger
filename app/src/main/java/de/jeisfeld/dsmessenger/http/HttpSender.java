package de.jeisfeld.dsmessenger.http;

import android.util.Log;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import de.jeisfeld.dsmessenger.Application;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.util.PreferenceUtil;

public class HttpSender {
	/**
	 * Send a POST message to Server.
	 *
	 * @param urlPostfix     The postfix of the URL.
	 * @param addCredentials Flag indicating if username, password should be added.
	 * @param listener       The response listener.
	 * @param parameters     The POST parameters.
	 */
	public void sendMessage(String urlPostfix, boolean addCredentials, OnHttpResponseListener listener, String... parameters) {
		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(HttpCredentials.USERNAME, HttpCredentials.PASSWORD.toCharArray());
			}
		});
		String urlBase;
		if (PreferenceUtil.getSharedPreferenceBoolean(R.string.key_pref_use_test_server)) {
			urlBase = "https://pc-joerg:8101/dsmessenger/";
		}
		else {
			urlBase = "https://jeisfeld.de/dsmessenger/";
		}

		new Thread() {
			@Override
			public void run() {
				Reader in = null;
				try {
					URL url = new URL(urlBase + urlPostfix);
					URLConnection urlConnection = url.openConnection();
					urlConnection.setDoOutput(true);
					((HttpsURLConnection) urlConnection).setRequestMethod("POST");
					urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					byte[] postDataBytes = getPostData(addCredentials, parameters).getBytes(StandardCharsets.UTF_8);
					urlConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
					urlConnection.getOutputStream().write(postDataBytes);

					in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
					StringBuilder result = new StringBuilder();
					for (int c; (c = in.read()) >= 0; ) {
						result.append((char) c);
					}
					if (listener != null) {
						ResponseData responseData = ResponseData.extractResponseData(result.toString());
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
			}
		}.start();
	}

	/**
	 * Send a POST message to Server, including credentials.
	 *
	 * @param urlPostfix The postfix of the URL.
	 * @param listener   The response listener.
	 * @param parameters The POST parameters.
	 */
	public void sendMessage(String urlPostfix, OnHttpResponseListener listener, String... parameters) {
		sendMessage(urlPostfix, true, listener, parameters);
	}

	/**
	 * Get post data from the parameters, which are name value entries.
	 *
	 * @param addCredentials Flag indicating if username, password should be added.
	 * @param parameters     the name value entries.
	 */
	private String getPostData(boolean addCredentials, String... parameters) throws UnsupportedEncodingException {
		int i = 0;
		StringBuilder postData = new StringBuilder();
		while (i < parameters.length - 1) {
			String name = parameters[i++];
			String value = parameters[i++];
			if (postData.length() > 0) {
				postData.append('&');
			}
			postData.append(URLEncoder.encode(name, StandardCharsets.UTF_8.name()));
			postData.append('=');
			postData.append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
		}
		if (addCredentials) {
			if (postData.length() > 0) {
				postData.append('&');
			}
			postData.append("username=");
			postData.append(URLEncoder.encode(PreferenceUtil.getSharedPreferenceString(R.string.key_pref_username), StandardCharsets.UTF_8.name()));
			postData.append("&password=");
			postData.append(URLEncoder.encode(PreferenceUtil.getSharedPreferenceString(R.string.key_pref_password), StandardCharsets.UTF_8.name()));
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
	public static class ResponseData {
		private final boolean success;
		private final int errorCode;
		private final String errorMessage;
		private final Map<String, String> data;

		/**
		 * Extract response data from server response.
		 *
		 * @param response The server response.
		 * @return The response data.
		 */
		private static ResponseData extractResponseData(final String response) {
			try {
				JSONObject jsonObject = new JSONObject(response);
				boolean success = "success".equals(jsonObject.getString("status"));
				Map<String, String> data = new HashMap<>();
				if (success) {
					for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
						String key = it.next();
						if (!"status".equals(key)) {
							data.put(key, jsonObject.getString(key));
						}
					}
					return new ResponseData(true, 0, "", data);
				}
				else {
					int errorCode = jsonObject.getInt("errorcode");
					String errorMessage = jsonObject.getString("errormessage");
					return new ResponseData(false, errorCode, errorMessage, data);
				}
			}
			catch (Exception e) {
				return null;
			}
		}

		private ResponseData(boolean success, int errorCode, String errorMessage, Map<String, String> data) {
			this.success = success;
			this.errorCode = errorCode;
			this.errorMessage = errorMessage;
			this.data = data;
		}

		public boolean isSuccess() {
			return success;
		}

		public int getErrorCode() {
			return errorCode;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		public Map<String, String> getData() {
			return data;
		}
	}

}
