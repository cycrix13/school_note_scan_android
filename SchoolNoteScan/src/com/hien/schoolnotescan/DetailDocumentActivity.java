package com.hien.schoolnotescan;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class DetailDocumentActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_document);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.detail_document, menu);
		return true;
	}

}
