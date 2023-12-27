//package edu.chat.kirje;
//
//
//import android.content.Context;
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//
//import org.java_websocket.client.WebSocketClient;
//import org.java_websocket.drafts.Draft;
//import org.java_websocket.handshake.ServerHandshake;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.Base64;
//import java.util.concurrent.ExecutorService;
//
//
//public class WebSocketConnection extends WebSocketClient {
//	private static final String TAG = WebSocketConnection.class.getSimpleName();
//
//	public WebSocketConnection(URI serverUri, Draft draft) {
//		super(serverUri, draft);
//		this.setConnectionLostTimeout(60_000);
//		this.connect();
//	}
//
//	public WebSocketConnection(URI serverURI) {
//		super(serverURI);
//		this.setConnectionLostTimeout(60_000);
//		this.connect();
//	}
//
//
//	@Override
//	public void onOpen(ServerHandshake handshakedata) {
//		Log.i(TAG, "onOpen: WebSocket Connected!");
//	}
//
//	@Override
//	public void onMessage(String message) {
//		System.out.println("received: " + message);
//		try {
//			JSONObject jsonMessage = new JSONObject(message);
//			if(!jsonMessage.has("Info")){
//				if(jsonMessage.getString("Info").equals("Last Connection")){
//
//				}else{
//
//					MainActivity.DisplayGenericMessage(jsonMessage.getString("Info"));
//				}
//			}else {
//
//			}
//		} catch (JSONException e) {
//			Log.e(TAG, "onMessage: Json Parse",e );
//		}
//	}
//
//	@Override
//	public void onClose(int code, String reason, boolean remote) {
//		System.out.println("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
//
//	}
//
//	@Override
//	public void onError(Exception ex) {
//		ex.printStackTrace();
//	}
//
//	public JSONObject convertAndSend(String message, ArrayList<Uri> uris, MainActivity activity) {
//		try {
//			JSONObject jsonObject = new JSONObject();
//			if (message != null && !message.isEmpty()) {
//				jsonObject.put("Text", message);
//			}
//			if (uris != null && !uris.isEmpty()) {
//				JSONArray Files = new JSONArray();
//				for (Uri uri : uris) {
//					JSONObject file = new JSONObject();
//					file.put(activity.getFileType(uri).split("/")[1], Base64.getEncoder().encodeToString(activity.getFileBytes(uri)));
//					Files.put(file);
//				}
//				jsonObject.put("Files", Files);
//			}
//			send(jsonObject.toString());
//			return jsonObject;
//		} catch (Exception e) {
//			Log.e(TAG, "convertAndSend: Exception", e);
//		}
//		return null;
//	}
//
//
//}
