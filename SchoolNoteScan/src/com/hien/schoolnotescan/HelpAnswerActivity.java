package com.hien.schoolnotescan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class HelpAnswerActivity extends Activity {
	
	private static String sQuestion;
	private static String sAnswer;
	
	private String mQuestion;
	private String mAnswer;
	
	///////////////////////////////////////////////////////////////////////////
	// Override methods
	///////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help_answer);
		
		mQuestion = sQuestion;
		mAnswer = sAnswer;
		
		sQuestion = null;
		sAnswer = null;
		
		TextView txtQuestion = (TextView) findViewById(R.id.txtQuestion);
		TextView txtAnswer = (TextView) findViewById(R.id.txtAnswer);
		Button btnBack = (Button) findViewById(R.id.btnBack); 
		
		txtQuestion.setText(mQuestion);
		txtAnswer.setText(mAnswer);
		
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
			
				onBackPressed();
			}
		});
	}
	
	@Override
	public void onBackPressed() {
	
		finish();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	///////////////////////////////////////////////////////////////////////////
	// Public methods
	///////////////////////////////////////////////////////////////////////////
	
	public static void newInstance(Activity act, String question, String answer) {
		
		sQuestion = question;
		sAnswer = answer;
		
		Intent intent = new Intent(act, HelpAnswerActivity.class);
		act.startActivity(intent);
		act.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
}