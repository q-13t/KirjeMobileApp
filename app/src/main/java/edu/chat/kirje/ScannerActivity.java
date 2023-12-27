package edu.chat.kirje;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.java_websocket.drafts.Draft_6455;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

public class ScannerActivity extends AppCompatActivity {
	private static final String TAG = ScannerActivity.class.getSimpleName();

	public void startActivity() {
		startActivity(new Intent(this, ScannerActivity.class));
	}


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		IntentIntegrator intentIntegrator = new IntentIntegrator(this);
		intentIntegrator.setBeepEnabled(true);
		intentIntegrator.setOrientationLocked(false);
		intentIntegrator.initiateScan(Collections.singleton(IntentIntegrator.QR_CODE));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (intentResult != null) {
			if (intentResult.getContents() == null) {
				Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
			} else {

//				URI uri = null;
//				try {
////					 params.get(0)
//					uri = new URI("ws://" + intentResult.getContents().split("/")[3] + "/");
//				} catch (URISyntaxException e) {
//					Log.e(TAG, "onActivityResult: Uri parse", e);
//				}
//				new WebSocketConnection(uri,new Draft_6455(),getApplicationContext());

				Intent intent = new Intent(this, MainActivity.class);
				intent.putExtra("URI", intentResult.getContents());
				startActivity(intent);

			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
}
