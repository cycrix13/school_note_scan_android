package com.hien.schoolnotescan;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class AddTagActivity extends Activity {
	
	private static Listener sListener; 
	
	private Listener mListener = new Listener();
	
	///////////////////////////////////////////////////////////////////////////
	// Override methods
	///////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_tag);
		
		// Get params
		mListener = sListener;
		
		// Clear stactic fields to avoid memory leak;
		sListener = null;
		
		// Set edit button event
		((Button) findViewById(R.id.btnEdit)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				onEditClick();
			}
		});
		
		// Set back button event
		((Button) findViewById(R.id.btnBack)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				onBackPressed();
			}
		});
		
		((EditText) findViewById(R.id.edtTag)).setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {

				onEditClick();
				return false;
			}
		});
	}
	
	@Override
	public void onBackPressed() {

		mListener.onCancel();
		finish();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Public methods
	///////////////////////////////////////////////////////////////////////////
	
	public static void newInstance(Activity act, Listener listener) {
		
		sListener = listener;
		Intent intent = new Intent(act, AddTagActivity.class);
		act.startActivity(intent);
		act.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
	
	public void onEditClick() {
		
		EditText edtTag = (EditText) findViewById(R.id.edtTag);
		String tagName = edtTag.getText().toString();
		
		finish();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		mListener.onComplete(tagName);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////
	
	public static class Listener {
		public void onComplete(String tagName) {}
		public void onCancel() {}
	}
}
