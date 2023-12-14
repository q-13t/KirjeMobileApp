package edu.chat.kirje;


import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Base64;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class STOMPClient {
	private static int reconnectionAttempts = 0;
	private StompClient client;

	@SuppressLint({"CheckResult"})
	public STOMPClient(String serverData) {
		try {
			String serverURI = "ws://" + serverData + "/kirje/websocket";
			Log.d("STOMPClient", "Server URI: " + serverURI);
			client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, serverURI);
			client.connect();
			client.send("/chat/WEBNotify", "QR Notify").subscribe();

			client.topic("/chat/APP").subscribe(topicMessage -> {
				Log.i(TAG, topicMessage.getPayload());
			}, error -> {
				Log.e(TAG, "Error", error);
			});

			client.lifecycle().subscribe(event -> {
				switch (event.getType()) {
					case OPENED:
						Log.i(TAG, "Stomp connection opened");
						reconnectionAttempts = 0;
						break;
					case ERROR:
						Log.e(TAG, "Stomp Error reconnecting...", event.getException());
						client.reconnect();
						break;
					case CLOSED:
						++reconnectionAttempts;
						Log.w(TAG, "Stomp connection closed");
						if (reconnectionAttempts <= 5) {
							Log.w(TAG, "STOMPClient: Attempting reconnect: " + reconnectionAttempts + "/5");
							client.connect();
						} else {
							Log.w(TAG, "STOMPClient: Failed to connect 5 times Dropping Connection.");
						}
						break;
				}
			}, error -> {
				Log.e(TAG, "Error", error);
			});
		} catch (Exception e) {
			Log.e(TAG, "ERROR at STOMP: ", e);
		}
	}

	public boolean sendMessage(String text, ArrayList<Uri> uris, MainActivity main) {
		try {
			JSONObject jsonObject = new JSONObject();
			if (!uris.isEmpty()) {
				JSONArray files = new JSONArray();
				for (Uri uri : uris) {
					JSONObject file = new JSONObject();
					file.put(
							main.getFileType(uri),
							Base64.getEncoder().encodeToString(main.getFileBytes(uri)));
					files.put(file);
				}
				jsonObject.put("Files", files);
			}
			jsonObject.put("Text", text);
			client.send("/chat/WEB", jsonObject.toString()).subscribe();
		} catch (JSONException e) {
			Log.e(TAG, "sendMessage: JSON Exception", e.getCause());
			return false;
		}
		return true;
	}
}

