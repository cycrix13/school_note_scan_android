package com.hien.schoolnotescan;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;

import com.hien.schoolnotescan.AddTagActivity.Listener;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.RemoveListener;

public class TagActivity extends Activity {
	
	// static fields
	private static Document 		sDoc;
	private static DocumentManager 	sDocManager;
	
	// Data
	private Document 		mDoc;
	private DocumentManager mDocManager;
	private List<String> 	mTagList;
	private TagListAdapter 	mAdapter;
	private boolean			mIsEditing;
	
	// GUI elements
	private DragSortListView mLst;
	private Button			mBtnEdit;

	///////////////////////////////////////////////////////////////////////////
	// Override methods
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tag);
		
		// Get data from static fields
		mDoc 		= sDoc;
		mDocManager = sDocManager;
		
		// Clean up static fields to avoid leak memory
		sDoc 		= null;
		sDocManager = null;
		
		// Set up listview
		mLst = (DragSortListView) findViewById(R.id.lst);
		mTagList = mDoc.mTagList;
		mAdapter = new TagListAdapter();
		mLst.setAdapter(mAdapter);
		
		// Set delete event
		mLst.setRemoveListener(new RemoveListener() {

			@Override
			public void remove(int which) {

				mAdapter.remove(mAdapter.getItem(which));
				try {
					mDocManager.save(TagActivity.this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// Get GUI elements
		mBtnEdit = (Button) findViewById(R.id.btnEdit);
		
		// Set back button event
		((Button) findViewById(R.id.btnBack))
		.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				onBackPressed();
			}
		});
		
		// Set edit button event
		mBtnEdit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				onEditClick();
			}
		});
		
		// Set add tag button event
		((Button) findViewById(R.id.btnAddTag))
		.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				AddTagActivity.newInstance(TagActivity.this, new Listener() {
					
					@Override
					public void onComplete(String tagName) {
					
						tagName = tagName.trim();
						if (tagName.length() == 0)
							return;
						DocumentManager.addIfNotExist(tagName, mTagList);
						try {
							mDocManager.save(TagActivity.this);
						} catch (Exception e) {
							e.printStackTrace();
						} 
					}
				});
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
		
		mIsEditing = !mIsEditing;
		
		if (mIsEditing) {
			mBtnEdit.setBackgroundResource(R.drawable.button_iphone_highlight);
			mBtnEdit.setText("Done");
		} else {
			mBtnEdit.setBackgroundResource(R.drawable.button_iphone_default);
			mBtnEdit.setText("Edit");
		}
		
		mAdapter.notifyDataSetChanged();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Adapter
	///////////////////////////////////////////////////////////////////////////
	
	private class TagListAdapter extends ArrayAdapter<String> {

		public TagListAdapter() {
			
			super(TagActivity.this, R.layout.tag_item, R.id.txtName, mTagList);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View v = super.getView(position, convertView, parent);

			// Set visibility
			((ImageView) v.findViewById(R.id.imgDelete)).setVisibility(
					mIsEditing ? View.VISIBLE : View.GONE);

			return v;
		}
	}
}
