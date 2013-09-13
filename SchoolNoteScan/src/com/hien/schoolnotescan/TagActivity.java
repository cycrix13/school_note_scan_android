package com.hien.schoolnotescan;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class TagActivity extends Activity {
	
	// static fields
	private static Document sDoc;
	
	// Data
	private Document 				mDoc;
	private List<String> 			mTagList;
	private ArrayAdapter<String>	mAdapter;
	
	// GUI elements
	private ListView 				mLst;

	///////////////////////////////////////////////////////////////////////////
	// Override methods
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag);
		
		// Get data from static fields
		mDoc = sDoc;
		
		// Clean up static fields to avoid leak memory
		sDoc = null;
		
		// Set up listview
		mLst = (ListView) findViewById(R.id.lst);
		mTagList = mDoc.mTagList;
		mAdapter = new ArrayAdapter<String>(this, R.layout.tag_item, R.id.txtName, mTagList);
		mLst.setAdapter(mAdapter);
		
		// Set back button event
		((Button) findViewById(R.id.btnBack))
		.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				onBackPressed();
			}
		});
		
		// Set edit button event
		((Button) findViewById(R.id.btnEdit))
		.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				onEditClick();
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
	
	public static void newInstance(Activity act, Document doc) {
		
		sDoc = doc;
		Intent intent = new Intent(act, TagActivity.class);
		act.startActivity(intent);
		act.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
	
	public void onEditClick() {
		
	}
}
