package com.hien.schoolnotescan;

import java.io.IOException;
import java.util.List;
import java.util.WeakHashMap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.hien.schoolnotescan.CameraActivity.Listener;
import com.hien.schoolnotescan.LayerManager.BoxState;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;
import com.mobeta.android.dslv.DragSortListView.RemoveListener;

public class DocumentFragment extends RootFragment implements Listener {
	
	private DragSortListView 	mLstDoc;
	public  DocumentListAdapter	mAdapter;
	public  DocumentManager		mDocManager;
	
	///////////////////////////////////////////////////////////////////////////
	// Override method
	///////////////////////////////////////////////////////////////////////////

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.document_frag, container);
		
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mDocManager = new DocumentManager((MainActivity) getActivity());
		
		mLstDoc = (DragSortListView) getView().findViewById(R.id.lstDoc);
		
		// Set adapter for list view
		mAdapter = new DocumentListAdapter(mDocManager.mDocList);
		mLstDoc.setAdapter(mAdapter);
		
		// Set document item click event
		mLstDoc.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
				DetailDocumentActivity.newInstance(mAdapter.getItem(position),
						mDocManager, (MainActivity) getActivity());
			}
		});

		// Set drop event, reorder document list
		mLstDoc.setDropListener(new DropListener() {
			
			@Override
			public void drop(int from, int to) {
				
				if (from == to)
					return;
				
				Document doc = mAdapter.getItem(from);
				mAdapter.remove(doc);
				mAdapter.insert(doc, to);
				try {
					mDocManager.save(getActivity());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		// Set delete event
		mLstDoc.setRemoveListener(new RemoveListener() {
			
			@Override
			public void remove(int which) {
				
				mAdapter.remove(mAdapter.getItem(which));
				try {
					mDocManager.save(getActivity());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		// Set take photo button
		((ImageButton) getView().findViewById(R.id.btnCamera))
		.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setCancelable(true);
				builder.setNegativeButton("Camera", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
					
						CameraActivity.newInstance(getActivity(), DocumentFragment.this, CameraActivity.MODE_CAMERA);
					}
				});
				
				builder.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
					
						CameraActivity.newInstance(getActivity(), DocumentFragment.this, CameraActivity.MODE_FILE);
					}
				});
				builder.create().show();
			}
		});
	}
	
	@Override
	public void onResume() {
	
		super.onResume();
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	public boolean canEdit() {

		return true;
	}
	
	@Override
	public void newDocCameraCallback(List<BoxState> boxList, Bitmap bm) {
		
		addNewDoc(boxList, bm);
	}

	@Override
	public void cancelCameraCallback() {
		
	}
	
	@Override
	public void toggleEdit() {
		
		mIsEditing = !mIsEditing;
		mAdapter.notifyDataSetChanged();
		mLstDoc.setDragEnabled(mIsEditing);
		((MainActivity) getActivity()).updateEditButtonState();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Public method
	///////////////////////////////////////////////////////////////////////////

	public void addNewDoc(List<BoxState> boxList, Bitmap bm) {

		Document doc = new Document();
		doc.mName = mDocManager.generateDocName();
		Bitmap bmPreview = makePreview(bm);
		try {
			doc.mPreviewPath = DocumentManager.saveBitmap(bmPreview, getActivity());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Add preview bitmaps
		mAdapter.add(doc);
		
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
				boxPath = DocumentManager.saveBitmap(bmBox, getActivity());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// Add to document
			doc.mNotePathArr.add(boxPath);
		}
		
		// Save
		try {
			mDocManager.save(getActivity());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Private method
	///////////////////////////////////////////////////////////////////////////
	
	private Bitmap makePreview(Bitmap bm) {
		
		int newW = 0;
		int newH = 0;
		if (bm.getWidth() > bm.getHeight()) {
			// scale as width
			newW = newH = bm.getHeight();
		} else {
			// scale as height
			newW = newH = bm.getWidth();
		}
		
		Bitmap newBm = Bitmap.createBitmap(
				GlobalVariable.PREVIEW_SIZE, GlobalVariable.PREVIEW_SIZE, Config.ARGB_8888);
		Canvas canvas = new Canvas(newBm);
		canvas.drawBitmap(bm, 
				new Rect((bm.getWidth() - newW) / 2, (bm.getHeight() - newH) / 2, newW, newH),
				new Rect(0, 0, newBm.getWidth(), newBm.getHeight()), null);
		
		return newBm;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Adapter
	///////////////////////////////////////////////////////////////////////////
	
	public class DocumentListAdapter extends ArrayAdapter<Document> {
		
		private WeakHashMap<String, Bitmap> mBitmapCache;
		
		public DocumentListAdapter(List<Document> docList) {
			super(DocumentFragment.this.getActivity(), R.layout.document_item, R.id.txtName, docList);
			mBitmapCache = new WeakHashMap<String, Bitmap>();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View v = super.getView(position, convertView, parent);
	
			// Update data for recycle view
			Document doc = getItem(position);
			TextView txtName = (TextView) v.findViewById(R.id.txtName);
			TextView txtTime = (TextView) v.findViewById(R.id.txtTime);
			TextView txtDocNum = (TextView) v.findViewById(R.id.txtDocNum);
			ImageView imgPreview = (ImageView) v.findViewById(R.id.imgPreview);
			
			txtName.setText(doc.mName);
			txtTime.setText(DocumentManager.date2String(doc.mDate));
			txtDocNum.setText("" + doc.mNotePathArr.size());
			if (doc.mPreviewPath != null) {
				// check cache
				Bitmap bm = mBitmapCache.get(doc.mPreviewPath);
				
				if (bm == null) {
					// miss
					bm = BitmapFactory.decodeFile(getActivity()
							.getFileStreamPath(doc.mPreviewPath).getAbsolutePath());
					mBitmapCache.put(doc.mPreviewPath, bm);
				}
				imgPreview.setImageBitmap(bm);
			}
			
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