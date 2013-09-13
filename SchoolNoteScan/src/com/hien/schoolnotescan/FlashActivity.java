package com.hien.schoolnotescan;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageButton;

public class FlashActivity extends Activity {
	
	public static final int REQUEST_CODE = GlobalVariable.FLASH_ACTIVITY_REQUEST_CODE;
	
	public static final int RESULT_CODE_RETAKE = REQUEST_CODE + 1;

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
	protected void onDestroy() {
		
		if (((CheckBox) findViewById(R.id.btnDontDisplay)).isChecked()) {
			ConfigHelper.instance().showSplash = false;
			ConfigHelper.instance().Save();
		}
	
		super.onDestroy();
	}
}
