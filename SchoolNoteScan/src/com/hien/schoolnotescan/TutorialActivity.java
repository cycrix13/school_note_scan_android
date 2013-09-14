package com.hien.schoolnotescan;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TutorialActivity extends Activity {
	
	private static Listener sListener;
	
	private Listener mListener;
	
	///////////////////////////////////////////////////////////////////////////
	// Override methods
	///////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tutorial);
		
		mListener = sListener;
		sListener = null;
		
		((Button) findViewById(R.id.btnGotIt))
		.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				finish();
				mListener.onGotItClick();
			}
		});
		
		((Button) findViewById(R.id.btnHelp))
		.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				finish();
				mListener.onHelpClick();
			}
		});
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Public methods
	///////////////////////////////////////////////////////////////////////////
	
	public static void newInstance(Activity act, Listener listener) {
		
		sListener = listener;
		
		Intent intent = new Intent(act, TutorialActivity.class);
		act.startActivity(intent);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////
	
	public static class Listener {
		public void onGotItClick() {}
		public void onHelpClick() {}
	}
}
