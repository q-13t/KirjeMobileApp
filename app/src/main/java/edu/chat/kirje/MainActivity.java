package edu.chat.kirje;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {
	private  LinearLayout chatLayout;
	private  LinearLayout FileListEL;
	private static final ArrayList<Uri> uris = new ArrayList<>();
	private static final int READ_REQUEST_CODE = 1;
	private static final ExecutorService service = Executors.newCachedThreadPool();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		chatLayout = findViewById(R.id.ChatSection);
		FileListEL = findViewById(R.id.FileList);
		service.submit(() -> {
			UDPOperator.startServer();
		});
	}


	public void SendMessage(View view) {
		EditText editText = findViewById(R.id.editText);
		if(!editText.getText().toString().isEmpty() || !uris.isEmpty()){//If user Entered message or selected Files to send
			LinearLayout container = (LinearLayout) getLayoutInflater().inflate(R.layout.message_out_container, chatLayout,true);//OUT MESSAGE appended to Chat Layout (chat Layout returned)
			for (Uri uri: uris) {
				String fileType = getFileType(uri);
				if(fileType.matches("image/.*")){
					addImageTo(uri, ((LinearLayout) container.getChildAt(container.getChildCount()-1)));//OutMessage is passed to function
				}else if(fileType.matches("video/.*")){
					addVideoTo(uri,((LinearLayout) container.getChildAt(container.getChildCount()-1)));
				}
			}

		service.submit(()->{
			UDPOperator.sendMessage(editText.getText().toString());
		});
			LinearLayout inflated = (LinearLayout) getLayoutInflater().inflate(R.layout.message_text, (LinearLayout)container.getChildAt(container.getChildCount()-1), true);
//			LinearLayout layout = (LinearLayout) container.getChildAt(chatLayout.getChildCount() - 1);
		TextView view1 = (TextView) inflated.getChildAt(inflated.getChildCount() - 1);
		view1.setText(editText.getText());
		editText.setText("");
		clearFileList();
		}
	}

	//TODO: MAKE VIDE PLAYABLE
	private void addVideoTo(Uri uri, LinearLayout container) {
//		LinearLayout LinearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.video_view_frame, container, true);
//		FrameLayout frameLayout = (FrameLayout) LinearLayout.getChildAt(LinearLayout.getChildCount() - 1);
//		VideoView videoView = (VideoView) frameLayout.getChildAt(0);
////		VideoView videoView = (VideoView)((LinearLayout) getLayoutInflater().inflate(R.layout.video_view_frame, container, true)).getChildAt(container.getChildCount()-1);
//		videoView.setVideoURI(Uri.parse("https://media.geeksforgeeks.org/wp-content/uploads/20201217192146/Screenrecorder-2020-12-17-19-17-36-828.mp4?_=1"));
//		videoView.setOnPreparedListener(mp -> videoView.start());
//		MediaController mc = new MediaController(this);
//		mc.setAnchorView(videoView);
//		mc.setMediaPlayer(videoView);
//		videoView.setMediaController(mc);
	}

	private void addImageTo(Uri uri, LinearLayout container) {
		ImageView imageView = (ImageView) ((LinearLayout) getLayoutInflater().inflate(R.layout.image_view, container, true)).getChildAt(container.getChildCount()-1);
		imageView.setImageDrawable(getDrawableFromUri(uri));
	}

	private String getFileType(Uri uri){
		return  getContentResolver().getType(uri);
	}

	public void BrowseFiles(View view) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
		intent.setType("*/*");
		startActivityForResult(intent,READ_REQUEST_CODE);
	}

	private Drawable getDrawableFromUri(Uri uri){
		try (ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri,"r")) {
			return new BitmapDrawable(getResources(), BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor()));
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	@Override
	@SuppressLint("InflateParams")
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && requestCode == READ_REQUEST_CODE) {
			if(data != null){
				ClipData clipData = data.getClipData();
				if(clipData!=null){
					for (int i = 0; i < clipData.getItemCount(); i++) {
						uris.add(clipData.getItemAt(i).getUri())  ;
						 LinearLayout fileSelection = (LinearLayout) getLayoutInflater().inflate(R.layout.selected_file_view,null);
						((TextView) fileSelection.getChildAt(0)).setText(clipData.getItemAt(i).getUri().toString());
						FileListEL.addView(fileSelection,0);
					}
				}else if(data.getData()!=null) {
					uris.add(data.getData())  ;
					LinearLayout fileSelection = (LinearLayout) getLayoutInflater().inflate(R.layout.selected_file_view,null);
					((TextView) fileSelection.getChildAt(0)).setText(data.getData().toString());
					FileListEL.addView(fileSelection,0);
				}

			}
		}
	}

	public void UnselectFile(View view) {
		LinearLayout container = (LinearLayout) view.getParent();
		uris.removeIf(x-> x.toString().equals(((TextView) container.getChildAt(0)).getText().toString()));
		((LinearLayout) container.getParent()).removeView(container);
	}

	private void clearFileList(){
		int childCount = FileListEL.getChildCount();
		for (int i = 0; i < childCount-1; i++) {
			FileListEL.removeViewAt(0);
		}
		uris.clear();
		ExpandFileList(null);
	}

	public void ExpandFileList(View view) {
		LinearLayout fileListEl = findViewById(R.id.FileList);
		if(fileListEl.getVisibility()==View.VISIBLE) {
			fileListEl.setVisibility(View.GONE);
		} else {
			fileListEl.setVisibility(View.VISIBLE);
		}
	}
}