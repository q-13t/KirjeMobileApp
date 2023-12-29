package edu.chat.kirje;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final ArrayList<Uri> uris = new ArrayList<>();
	private static WebSocketClient WSC = null;
	private static Executor executor;
	private static Handler handler;
	private LinearLayout chatLayout;
	private LinearLayout FileListEL;
	private final ActivityResultLauncher<String> SGetContent = registerForActivityResult(new ActivityResultContracts.GetMultipleContents() {
		@NonNull
		@Override
		public Intent createIntent(@NonNull Context context, @NonNull String input) {
			Intent intent = super.createIntent(context, input);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
			intent.setType(input);
			intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/jpg", "image/png", "video/mp4"});
			return intent;
		}
	}, result -> {
		if (result.size() == 0) return;

		for (int i = 0; i < result.size(); i++) {
			uris.add(result.get(i));
			LinearLayout fileSelection = (LinearLayout) getLayoutInflater().inflate(R.layout.selected_file_view, chatLayout, false);
			((TextView) fileSelection.getChildAt(0)).setText(result.get(i).toString());
			FileListEL.addView(fileSelection, 0);
		}

	});
	private MediaController mediaController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (executor == null) {
			executor = Executors.newSingleThreadExecutor();
			handler = new Handler(Looper.getMainLooper());
			mediaController = new MediaController(this);
		}
		Uri uri = getIntent().getData();
		if (uri == null) {
			uri = Uri.parse(getIntent().getStringExtra("URI"));
		}
		URI server = null;
		if (uri != null) {
			List<String> params = uri.getPathSegments();
			try {
				server = new URI("ws://" + params.get(0) + "/");
			} catch (Exception e) {
				Log.e(TAG, "onCreate: Uri Error", e);
			}
			Log.d(TAG, "onCreate: ServerURI " + server);
		}
		if (server != null && (WSC == null || WSC.isClosed())) {
			WSC = new WebSocketClient(server, new Draft_6455()) {

				@Override
				public void onOpen(ServerHandshake handShakeData) {
					Log.i(TAG, "onOpen: WebSocket Connected!");

				}

				@Override
				public void onMessage(String message) {
					System.out.println("received: " + message);
					try {
						JSONObject jsonMessage = new JSONObject(message);
						if (!jsonMessage.isNull("Info")) {
							if (jsonMessage.getString("Info").equals("Last Connection")) {
								changeToScanner();
							} else {
								handler.post(() -> {
									try {
										DisplayGenericMessage(jsonMessage.getString("Info"));
									} catch (JSONException e) {
										Log.e(TAG, "run: Json Parse UI ", e);
									}
								});

							}
						} else {
							handler.post(() -> DisplayMessage(jsonMessage, true));
						}
					} catch (JSONException e) {
						Log.e(TAG, "onMessage: Json Parse", e);
					}
				}

				@Override
				public void onClose(int code, String reason, boolean remote) {
					Log.wtf(TAG, "Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
					changeToScanner();
				}

				@Override
				public void onError(Exception ex) {
					Log.e(TAG, "onError: Connector Error", ex);
					changeToScanner();
				}
			};
			WSC.setConnectionLostTimeout(60_000);
			WSC.connect();
		}
		setContentView(R.layout.activity_main);
		chatLayout = findViewById(R.id.ChatSection);
		FileListEL = findViewById(R.id.FileList);
	}

	private void DisplayMessage(JSONObject jsonMessage, boolean incoming) {
		try {

			LinearLayout container;
			if (incoming) {
				container = ((LinearLayout) ((LinearLayout) getLayoutInflater().inflate(R.layout.message_in_container, chatLayout, true)).getChildAt(chatLayout.getChildCount() - 1));
			} else {
				container = ((LinearLayout) ((LinearLayout) getLayoutInflater().inflate(R.layout.message_out_container, chatLayout, true)).getChildAt(chatLayout.getChildCount() - 1));
			}

			if (!jsonMessage.isNull("Origin")) {
				TextView OR = ((TextView) ((LinearLayout) getLayoutInflater().inflate(R.layout.message_text, container, true)).getChildAt(container.getChildCount() - 1));
				OR.setText(jsonMessage.getString("Origin"));
			}
			if (!jsonMessage.isNull("Files")) {
				JSONArray files = jsonMessage.getJSONArray("Files");
				for (int i = 0; i < files.length(); i++) {
					JSONObject file = files.getJSONObject(i);
					String type = file.keys().next();
					String data = file.getString(type);
					if (data.contains("base64,")) {
						data = data.substring(data.indexOf(',') + 1);
					}
					byte[] bytes = Base64.getDecoder().decode(data);
					switch (type) {
						case "jpeg":
						case "jpg":
						case "png": {
							ImageView image = ((ImageView) ((LinearLayout) getLayoutInflater().inflate(R.layout.image_view, container, true)).getChildAt(container.getChildCount() - 1));
							image.setImageDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length)));
							break;
						}
						case "mp4": {
							FrameLayout relativeLayout = ((FrameLayout) ((LinearLayout) getLayoutInflater().inflate(R.layout.video_view_frame, container, true)).getChildAt(container.getChildCount() - 1));
							VideoView videoView = ((VideoView) relativeLayout.getChildAt(0));
							File tempVideo = File.createTempFile("tempVideo", ".mp4", getCacheDir());
							tempVideo.deleteOnExit();
							try (FileOutputStream FOS = new FileOutputStream(tempVideo)) {
								FOS.write(bytes);
							}
							try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
								retriever.setDataSource(this, Uri.fromFile(tempVideo));
								int height = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
								videoView.getLayoutParams().height = height;
								videoView.getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
								relativeLayout.getLayoutParams().height = height;
								relativeLayout.getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
								retriever.release();
							}
							videoView.setOnErrorListener((mp, what, extra) -> {
								Log.e(TAG, "Video preparation error. What: " + what + ", Extra: " + extra);
								return false;
							});
							videoView.setVideoURI(Uri.fromFile(tempVideo));
							if (mediaController == null) {
								mediaController = new MediaController(this);
							}
							mediaController.setAnchorView(relativeLayout);
							videoView.setMediaController(mediaController);
							videoView.start();
							break;
						}
					}
				}
			}
			if (!jsonMessage.isNull("Text")) {
				TextView TEXT = ((TextView) ((LinearLayout) getLayoutInflater().inflate(R.layout.message_text, container, true)).getChildAt(container.getChildCount() - 1));
				TEXT.setText(jsonMessage.getString("Text"));
			}
			handler.post(() -> ((ScrollView) findViewById(R.id.scrollPane)).fullScroll(View.FOCUS_DOWN));
		} catch (JSONException | IOException e) {
			Log.e(TAG, "DisplayMessage: ", e);
		}
	}

	public void DisplayGenericMessage(String info) {
		TextView TV = ((TextView) ((LinearLayout) getLayoutInflater().inflate(R.layout.generic_message, chatLayout, true)).getChildAt(chatLayout.getChildCount() - 1));
		TV.setText(info);
		handler.post(() -> ((ScrollView) findViewById(R.id.scrollPane)).fullScroll(View.FOCUS_DOWN));
	}

	private void changeToScanner() {
		startActivity(new Intent(this, ScannerActivity.class));
	}

	public JSONObject convertAndSend(String message, ArrayList<Uri> uris, MainActivity activity) {
		try {
			JSONObject jsonObject = new JSONObject();
			if (message != null && !message.isEmpty()) {
				jsonObject.put("Text", message);
			}
			if (uris != null && !uris.isEmpty()) {
				JSONArray Files = new JSONArray();
				for (Uri uri : uris) {
					JSONObject file = new JSONObject();
					file.put(activity.getFileType(uri).split("/")[1], Base64.getEncoder().encodeToString(activity.getFileBytes(uri)));
					Files.put(file);
				}
				jsonObject.put("Files", Files);
			}
			WSC.send(jsonObject.toString());
			return jsonObject;
		} catch (Exception e) {
			Log.e(TAG, "convertAndSend: Exception", e);
		}
		return null;
	}

	public void SendMessage(View view) {
		EditText editText = findViewById(R.id.editText);
		if (!editText.getText().toString().isEmpty() || !uris.isEmpty()) {//If user Entered message or selected Files to send
			handler.post(() -> {
				DisplayMessage(convertAndSend(editText.getText().toString(), uris, this), false);
				editText.setText("");
				clearFileList();
			});
		}
	}

	protected String getFileType(Uri uri) {
		return getContentResolver().getType(uri);
	}

	protected byte[] getFileBytes(Uri uri) {
		try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
			byte[] buffer = new byte[inputStream.available()];
			inputStream.read(buffer);
			return buffer;
		} catch (IOException e) {
			Log.e(TAG, "getFileBytes: Byte Read", e);
		}
		return null;
	}

	public void BrowseFiles(View view) {
		SGetContent.launch("*/*");
	}


	/**
	 * Removes single selected file
	 *
	 * @param view {@link android.widget.Button} that corresponds to selected file in the List.
	 */
	public void UnselectFile(View view) {
		LinearLayout container = (LinearLayout) view.getParent();
		uris.removeIf(x -> x.toString().equals(((TextView) container.getChildAt(0)).getText().toString()));
		((LinearLayout) container.getParent()).removeView(container);
	}

	private void clearFileList() {
		int childCount = FileListEL.getChildCount();
		for (int i = 0; i < childCount - 1; i++) {
			FileListEL.removeViewAt(0);
		}
		uris.clear();
		if (findViewById(R.id.FileList).getVisibility() == View.VISIBLE) {
			ExpandFileList(null);
		}
	}

	public void ExpandFileList(View view) {
		LinearLayout fileListEl = findViewById(R.id.FileList);
		if (fileListEl.getVisibility() == View.VISIBLE) {
			fileListEl.setVisibility(View.GONE);
		} else {
			fileListEl.setVisibility(View.VISIBLE);
		}
	}

}