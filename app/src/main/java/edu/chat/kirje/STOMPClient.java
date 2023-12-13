package edu.chat.kirje;


import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.util.Log;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class STOMPClient {
	private StompClient client;

	@SuppressLint({"CheckResult"})
	public STOMPClient(String serverData) {
		try {
			String serverURI = "ws://" + serverData + "/kirje/websocket";
			Log.d("STOMPClient", "Server URI: " + serverURI);
			client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, serverURI);
			client.connect();
			client.send("/chat/notify", "message").subscribe(() -> {
				Log.i(TAG, "STOMPClient: Connected!");
			}, (error) -> {
				Log.e(TAG, "STOMPClient: Failed To Connect", error);
			});
			client.topic("/chat/APP").subscribe(topicMessage -> {
				Log.i(TAG, topicMessage.getPayload());
			}, error -> {
				Log.e(TAG, "Error", error);
			});
			client.lifecycle().subscribe(event -> {
				switch (event.getType()) {
					case OPENED:
						Log.i(TAG, "Stomp connection opened");
						break;
					case ERROR:
						Log.e(TAG, "Error", event.getException());
						break;
					case CLOSED:
						Log.w(TAG, "Stomp connection closed");
						break;
				}
			}, error -> {
				Log.e(TAG, "Error", error);
			});
		} catch (Exception e) {
			Log.e(TAG, "ERROR at STOMP: ", e);
		}
	}
}

