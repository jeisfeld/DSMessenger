package de.jeisfeld.dsmessenger.http;

import android.util.Log;

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

import javax.net.ssl.HttpsURLConnection;

import de.jeisfeld.dsmessenger.Application;
import de.jeisfeld.dsmessenger.R;
import de.jeisfeld.dsmessenger.util.PreferenceUtil;

public class HttpSender {
	/**
	 * Send a POST message to Server.
	 *
	 * @param listener   The response listener.
	 * @param parameters The POST parameters.
	 */
	public void sendMessage(OnHttpResponseListener listener, String... parameters) {
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
					URL url = new URL(urlBase + "index.php");
					URLConnection urlConnection = url.openConnection();
					urlConnection.setDoOutput(true);
					((HttpsURLConnection) urlConnection).setRequestMethod("POST");
					urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					byte[] postDataBytes = getPostData(parameters).getBytes(StandardCharsets.UTF_8);
					urlConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
					urlConnection.getOutputStream().write(postDataBytes);

					in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
					StringBuilder result = new StringBuilder();
					for (int c; (c = in.read()) >= 0; ) {
						result.append((char) c);
					}
					if (listener != null) {
						listener.onHttpResponse(result.toString());
					}
				}
				catch (IOException e) {
					Log.e(Application.TAG, "Invalid URL", e);
				}
				finally {
					try {
						in.close();
					}
					catch (Exception e) {
						// ignore
					}
				}
			}
		}.start();
	}

	/**
	 * Get post data from the parameters, which are name value entries.
	 *
	 * @param parameters the name value entries.
	 */
	private String getPostData(String... parameters) throws UnsupportedEncodingException {
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
		return postData.toString();
	}

	/**
	 * Handler for HTTP/HTTPS response.
	 */
	public interface OnHttpResponseListener {
		/**
		 * Handle HTTP/HTTPS response.
		 *
		 * @param response The response.
		 */
		void onHttpResponse(String response);
	}

}
