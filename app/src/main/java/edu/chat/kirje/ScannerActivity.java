package edu.chat.kirje;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Collections;

public class ScannerActivity extends AppCompatActivity {
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
		// if the intentResult is null then
		// toast a message as "cancelled"
		if (intentResult != null) {
			if (intentResult.getContents() == null) {
				Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
			} else {
				// if the intentResult is not null we'll set
				// the content and format of scan message
//				messageText.setText(intentResult.getContents());
//				messageFormat.setText(intentResult.getFormatName());
				Toast.makeText(this, intentResult.getContents()+" "+intentResult.getFormatName(), Toast.LENGTH_LONG).show();
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
}
