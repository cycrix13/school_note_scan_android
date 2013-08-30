package com.hien.schoolnotescan;

import com.mobeta.android.dslv.DragSortListView;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;

public class DetailDocumentActivity extends Activity {
	
	public static final int REQUEST_CODE = GlobalVariable.DETAIL_ACTIVITY_REQUEST_CODE;
	
	private DragSortListView mLst;
	private ArrayAdapter<Box> mAdapter;
	
	///////////////////////////////////////////////////////////////////////////
	// Override method
	///////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_document);
		
		mLst = (DragSortListView) findViewById(R.id.lstBox);
		
		// Set back button event
		((Button) findViewById(R.id.btnBack))
		.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
			
				onBackPressed();
			}
		});
	}

	///////////////////////////////////////////////////////////////////////////
	// Public method
	///////////////////////////////////////////////////////////////////////////
	
	public static void newInstance(Document doc, MainActivity act) {
		
		Intent intent = new Intent(act, DetailDocumentActivity.class);
		act.startActivity(intent);
		act.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
	
	@Override
	public void onBackPressed() {
	
		finish();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	public class Box {
		
		public Bitmap mBitmap;
	}
}
