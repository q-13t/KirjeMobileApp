package edu.chat.kirje;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
	LinearLayout chatLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		Objects.requireNonNull(getSupportActionBar()).hide();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		chatLayout = findViewById(R.id.ChatSection);
	}


	public void SendMessage(View view) {
	EditText editText = findViewById(R.id.editText);
	if(!"".equals(editText.getText().toString())){
		ContextThemeWrapper newContext = new ContextThemeWrapper(this, R.style.Theme_Kirje);
		LinearLayout messageLayout = new LinearLayout(newContext);
		TextView message = new TextView(this);
		message.setText(editText.getText());
		editText.setText("");
		messageLayout.addView(message);

		chatLayout.addView(messageLayout);
	}
	}
}