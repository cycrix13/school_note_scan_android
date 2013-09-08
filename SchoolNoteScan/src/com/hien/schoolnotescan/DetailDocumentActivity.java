package com.hien.schoolnotescan;

import java.io.IOException;
import java.util.List;
import java.util.WeakHashMap;

import org.json.JSONException;

import com.hien.schoolnotescan.CameraActivity.Listener;
import com.hien.schoolnotescan.LayerManager.BoxState;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;
import com.mobeta.android.dslv.DragSortListView.RemoveListener;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailDocumentActivity extends Activity implements Listener {
	
	public static final int REQUEST_CODE = GlobalVariable.DETAIL_ACTIVITY_REQUEST_CODE;

	// static fields used to pass Document to this activity
	// NOTE: CLEAN UP after use!
	private static Document sDoc;
	private static DocumentManager sDocManager; 

	// Data
	private List<String> 	mBoxList;
	private BoxListAdapter 	mAdapter;
	private Document 		mDoc;
	private DocumentManager mDocManager;
	
	// Activity Status
	private boolean 		mIsEditing;
	
	// Layout elements
	private DragSortListView mLst;
	private TextView 		txtTitle;
	private TextView 		txtName;
	private TextView 		txtTag;
	
	///////////////////////////////////////////////////////////////////////////
	// Override method
	///////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_document);
		
		// Get document item, document manager
		mDoc = sDoc;
		mDocManager = sDocManager;
		
		// Clean up static fields to avoid memory leak 
		sDoc = null;
		sDocManager = null;
		
		getLayoutElements();
		
		updateLayout();
		
		setButtonEvent();
		
		setUpListView();
	}
	

	@Override
	public void newDocCameraCallback(List<BoxState> boxList, Bitmap bm) {
		
		addNewBoxes(boxList, bm);
	}

	@Override
	public void cancelCameraCallback() {
		
	}

	///////////////////////////////////////////////////////////////////////////
	// Public method
	///////////////////////////////////////////////////////////////////////////
	
	public static void newInstance(Document doc, DocumentManager docManager, MainActivity act) {
		
		Intent intent = new Intent(act, DetailDocumentActivity.class);
		sDoc = doc;
		sDocManager = docManager;
		act.startActivity(intent);
		act.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
	
	@Override
	public void onBackPressed() {
	
		finish();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	public void onEditClick() {
		
		mIsEditing = !mIsEditing;
		updateTitleBar();
		mAdapter.notifyDataSetChanged();
	}
	
	public void onAddBoxClick() {
		
		CameraActivity.newInstance(this, this);
	}
	
	public void onExportClick() {
		
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Private method
	///////////////////////////////////////////////////////////////////////////
	
	private void getLayoutElements() {
		
		txtName 	= (TextView) findViewById(R.id.txtName);
		txtTitle 	= (TextView) findViewById(R.id.txtTitle);
		txtTag 		= (TextView) findViewById(R.id.txtTag);
		mLst 		= (DragSortListView) findViewById(R.id.lstBox);
	}
	
	private void updateLayout() {
		
		txtName.setText(mDoc.mName);
		txtTitle.setText(mDoc.mName);
//		txtTag.setText();
	}
	
	private void setButtonEvent() {
		
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
		
		// Set add box button event
		((ImageButton) findViewById(R.id.btnAddBox))
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				onAddBoxClick();
			}
		});

		// Set export button event
		((ImageButton) findViewById(R.id.btnExport))
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				onExportClick();
			}
		});
	}
	
	private void setUpListView() {
		
		mBoxList = mDoc.mNotePathArr;
		mAdapter = new BoxListAdapter();
		mLst.setAdapter(mAdapter);
		
		// Set drop event, reorder box list
		mLst.setDropListener(new DropListener() {

			@Override
			public void drop(int from, int to) {

				if (from == to)
					return;

				String box = mAdapter.getItem(from);
				mAdapter.remove(box);
				mAdapter.insert(box, to);
				
				try {
					mDocManager.save(DetailDocumentActivity.this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// Set delete event
		mLst.setRemoveListener(new RemoveListener() {

			@Override
			public void remove(int which) {

				mAdapter.remove(mAdapter.getItem(which));
				
				try {
					mDocManager.save(DetailDocumentActivity.this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		// Set click event to avoid unintended delete button click event
		mLst.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) { }
		});
	}
	
	private void updateTitleBar() {
		
		Button btnEdit = (Button) findViewById(R.id.btnEdit);
		if (mIsEditing) {
			btnEdit.setText("Done");
			btnEdit.setBackgroundResource(R.drawable.button_iphone_highlight);
			mLst.setDragEnabled(mIsEditing);
		} else {
			btnEdit.setText("Edit");
			btnEdit.setBackgroundResource(R.drawable.button_iphone_default);
		}
		
		mLst.setDragEnabled(mIsEditing);
	}
	
	private void addNewBoxes(List<BoxState> boxList, Bitmap bm) {
		
		// Add box bitmap
		for (BoxState box : boxList) {

			// Extract box bitmap
			Rect r 	 = new Rect();
			r.left 	 = Math.max(0, box.pos.x);
			r.top  	 = Math.max(0, box.pos.y);
			r.right  = Math.min(bm.getWidth(), r.left + box.size.x);
			r.bottom = Math.min(bm.getHeight(), r.top + box.size.y);

			Bitmap bmBox = Bitmap.createBitmap(bm, r.left, r.top, r.right - r.left, r.bottom - r.top);

			// Save to file
			String boxPath = null;
			try {
				boxPath = DocumentManager.saveBitmap(bmBox, this);
			} catch (IOException e) {
				e.printStackTrace();
			}

			mAdapter.add(boxPath);
		}
		
		// save
		try {
			mDocManager.save(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// ListView adapter
	///////////////////////////////////////////////////////////////////////////
	
	public class BoxListAdapter extends ArrayAdapter<String> {
		
		private WeakHashMap<String, Bitmap> mBitmapCache = new WeakHashMap<String, Bitmap>();
		
		public BoxListAdapter() {
			
			super(DetailDocumentActivity.this, R.layout.box_item, R.id.txtDummy, mBoxList);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View v = super.getView(position, convertView, parent);
	
			// Update data for recycle view
			String path = getItem(position);
			ImageView img = (ImageView) v.findViewById(R.id.imgBox);
			
			// Check cache
			Bitmap bm = mBitmapCache.get(path);
			if (bm == null) {
				// Miss
				bm = BitmapFactory.decodeFile(getFileStreamPath(path).getAbsolutePath());
				mBitmapCache.put(path, bm);
			}
			img.setImageBitmap(bm);
						
			// Set visibility for reorder and delete button
			View imgReorder = v.findViewById(R.id.imgReorder);
			View imgDelete = v.findViewById(R.id.imgDelete);
			
			if (mIsEditing) {
				imgReorder.setVisibility(View.VISIBLE);
				imgDelete.setVisibility(View.VISIBLE);
			} else {
				imgReorder.setVisibility(View.GONE);
				imgDelete.setVisibility(View.GONE);
			}
			
			return v;
		}
	}
}
