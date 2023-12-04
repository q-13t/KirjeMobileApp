package edu.chat.kirje;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
	private  LinearLayout chatLayout;
	private  LinearLayout FileListEL;
	private static final ArrayList<Uri> uris = new ArrayList<>();
	private static final int READ_REQUEST_CODE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		chatLayout = findViewById(R.id.ChatSection);
		FileListEL = findViewById(R.id.FileList);
	}


	public void SendMessage(View view) {
	EditText editText = findViewById(R.id.editText);
	if(!"".equals(editText.getText().toString())){
//		LinearLayout container = (LinearLayout) getLayoutInflater().inflate(R.layout.message_out_container, chatLayout,true);
//		LinearLayout layout = (LinearLayout) container.getChildAt(chatLayout.getChildCount() - 1);
//		TextView view1 = (TextView) layout.getChildAt(layout.getChildCount() - 1);
//		view1.setText(editText.getText());
//		editText.setText("");
	}
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
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && requestCode == READ_REQUEST_CODE) {
			if(data != null){
				ClipData clipData = data.getClipData();
				if(clipData!=null){
//					Uri[] uris = new Uri[clipData.getItemCount()];
					for (int i = 0; i < clipData.getItemCount(); i++) {
						uris.add(clipData.getItemAt(i).getUri())  ;
						LinearLayout fileSelection = (LinearLayout) getLayoutInflater().inflate(R.layout.selected_file_view,null);
						((TextView) fileSelection.getChildAt(0)).setText(clipData.getItemAt(i).getUri().toString());
						FileListEL.addView(fileSelection,0);
//						System.out.println(getContentResolver().getType(uris[i]));
//						if(getContentResolver().getType(uris[i]).matches("image/.*")){
//							ImageView imageView = new ImageView(this);
//							imageView.setImageDrawable(getDrawableFromUri(uris[i]));
//							chatLayout.addView(imageView);
//						}else if(getContentResolver().getType(uris[i]).matches("video/.*")){
//							VideoView videoView = (VideoView) getLayoutInflater().inflate(R.layout.video_view_frame,chatLayout,true);
//							MediaController mc = new MediaController(this);
//							mc.setAnchorView(videoView);
//							mc.setMediaPlayer(videoView);
//							videoView.setMediaController(mc);
//							videoView.setVideoURI(uris[i]);
////							chatLayout.addView(videoView);
//							videoView.start();
//						}
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
		String uri = ((TextView) container.getChildAt(0)).getText().toString();
		uris.removeIf(x->{return x.toString().equals(uri);});
		((LinearLayout) container.getParent()).removeView(container);
	}

	public void ExpandFileList(View view) {
		LinearLayout fileListEl = findViewById(R.id.FileList);
		if(fileListEl.getVisibility()==View.VISIBLE)
			fileListEl.setVisibility(View.GONE);
		else
			fileListEl.setVisibility(View.VISIBLE);
	}
}