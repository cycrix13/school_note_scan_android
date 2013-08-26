package com.hien.schoolnotescan;

import com.hien.schoolnotescan.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class FlashActivity extends Activity {
	
	public static final int REQUEST_CODE = 101;
	
	public static final int RESULT_CODE_RETAKE = 102;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flash);
		
		ImageButton btnCamera = (ImageButton) findViewById(R.id.btnCamera);
		btnCamera.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				setResult(RESULT_CODE_RETAKE);
				finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.flash, menu);
		return true;
	}
	
	

}
