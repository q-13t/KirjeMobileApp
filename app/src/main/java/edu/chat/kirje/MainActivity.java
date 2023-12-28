package edu.chat.kirje;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {
	private static final Object lock = new Object();
	private static final ArrayList<Uri> uris = new ArrayList<>();
	private static final int READ_REQUEST_CODE = 1;
	private static final ExecutorService service = Executors.newCachedThreadPool();
	private static MediaMetadataRetriever retriever;
	private static WebSocketClient WSC = null;
	private LinearLayout chatLayout;
	private LinearLayout FileListEL;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Uri uri = getIntent().getData();
		if (uri == null) {
			uri = Uri.parse(getIntent().getStringExtra("URI"));
		}
		URI server = null;
		if (uri != null) {
			List<String> params = uri.getPathSegments();
//			webSocketConnection = new WebSocketConnection();
			server = null;
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
				public void onOpen(ServerHandshake handshakedata) {
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
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										try {
											DisplayGenericMessage(jsonMessage.getString("Info"));
										} catch (JSONException e) {
											Log.e(TAG, "run: Json Parse UI ", e);
										}
									}
								});
							}
						} else {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									try {
										DisplayMessage(jsonMessage, true);
									} catch (JSONException | IOException e) {
										Log.e(TAG, "run: Json Parse UI ", e);
									}
								}
							});
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
					ex.printStackTrace();
				}


			};
			WSC.setConnectionLostTimeout(60_000);
			WSC.connect();
		}
		setContentView(R.layout.activity_main);
		chatLayout = findViewById(R.id.ChatSection);
		FileListEL = findViewById(R.id.FileList);
	}

	private void DisplayMessage(JSONObject jsonMessage, boolean incoming) throws JSONException, IOException {
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
						retriever = new MediaMetadataRetriever();
						retriever.setDataSource(this, Uri.fromFile(tempVideo));
						int height = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
						int width = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
						videoView.getLayoutParams().height = height;
						videoView.getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
						relativeLayout.getLayoutParams().height = height;
						relativeLayout.getLayoutParams().width = FrameLayout.LayoutParams.MATCH_PARENT;
						retriever.release();
						videoView.setOnErrorListener((mp, what, extra) -> {
							Log.e(TAG, "Video preparation error. What: " + what + ", Extra: " + extra);
							return false;
						});
						videoView.setVideoURI(Uri.fromFile(tempVideo));
						MediaController mediaController = new MediaController(this);
						mediaController.setAnchorView(relativeLayout);
						mediaController.setEnabled(true);
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
	}

	public void DisplayGenericMessage(String info) {
		TextView TV = ((TextView) ((LinearLayout) getLayoutInflater().inflate(R.layout.generic_message, chatLayout, true)).getChildAt(chatLayout.getChildCount() - 1));
		TV.setText(info);
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


	public void SendMessage(View view) throws JSONException, IOException {
		EditText editText = findViewById(R.id.editText);
		if (!editText.getText().toString().isEmpty() || !uris.isEmpty()) {//If user Entered message or selected Files to send
			DisplayMessage(convertAndSend(editText.getText().toString(), uris, this), false);
			editText.setText("");
			clearFileList();
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
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		intent.setType("*/*");
		startActivityForResult(intent, READ_REQUEST_CODE);
	}

	private Drawable getDrawableFromUri(Uri uri) {
		try (ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r")) {
			return new BitmapDrawable(getResources(), BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Retrieves and handles file uris
	 */
	@Override
	@SuppressLint("InflateParams")
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && requestCode == READ_REQUEST_CODE) {
			if (data != null) {
				ClipData clipData = data.getClipData();
				if (clipData != null) {
					for (int i = 0; i < clipData.getItemCount(); i++) {
						uris.add(clipData.getItemAt(i).getUri());
						LinearLayout fileSelection = (LinearLayout) getLayoutInflater().inflate(R.layout.selected_file_view, null);
						((TextView) fileSelection.getChildAt(0)).setText(clipData.getItemAt(i).getUri().toString());
						FileListEL.addView(fileSelection, 0);
					}
				} else if (data.getData() != null) {
					uris.add(data.getData());
					LinearLayout fileSelection = (LinearLayout) getLayoutInflater().inflate(R.layout.selected_file_view, null);
					((TextView) fileSelection.getChildAt(0)).setText(data.getData().toString());
					FileListEL.addView(fileSelection, 0);
				}

			}
		}
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