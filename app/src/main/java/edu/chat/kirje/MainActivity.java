package edu.chat.kirje;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
	LinearLayout chatLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		chatLayout = findViewById(R.id.ChatSection);
	}


	public void SendMessage(View view) {
	EditText editText = findViewById(R.id.editText);
	if(!"".equals(editText.getText().toString())){
		LinearLayout container = (LinearLayout) getLayoutInflater().inflate(R.layout.message_out_container, chatLayout,true);
		LinearLayout layout = (LinearLayout) container.getChildAt(chatLayout.getChildCount() - 1);
		TextView view1 = (TextView) layout.getChildAt(layout.getChildCount() - 1);
		view1.setText(editText.getText());
		editText.setText("");
	}
	}
}