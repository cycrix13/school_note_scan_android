package com.hien.schoolnotescan;

import java.util.List;
import java.util.WeakHashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;
import com.mobeta.android.dslv.DragSortListView.RemoveListener;

public class TagDocumentFragment extends RootFragment {
	
	private DragSortListView 	mLstDoc;
	private TagDocumentListAdapter mAdapter;
	public  DocumentManager		mDocManager;
	
	///////////////////////////////////////////////////////////////////////////
	// Override method
	///////////////////////////////////////////////////////////////////////////

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.tag_document_fragment, container);
		
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mLstDoc = (DragSortListView) getView().findViewById(R.id.lstDoc);
		
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
			public void drop(int from, int to) { }
		});
		
		// Set delete event
		mLstDoc.setRemoveListener(new RemoveListener() {
			
			@Override
			public void remove(int which) {
				
				mDocManager.mDocList.remove(mAdapter.getItem(which));
				mAdapter.remove(mAdapter.getItem(which));
				try {
					mDocManager.save(getActivity());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	public boolean canEdit() {

		return true;
	}
	
	@Override
	public void toggleEdit() {
		
		mIsEditing = !mIsEditing;
		mAdapter.notifyDataSetChanged();
		((MainActivity) getActivity()).updateEditButtonState();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Public method
	///////////////////////////////////////////////////////////////////////////
	
	public void setTag(DocumentManager docManager, String tag) {
		
		mDocManager = docManager;
		mAdapter = new TagDocumentListAdapter(docManager.getDocByTag(tag));
		mLstDoc.setAdapter(mAdapter);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Private method
	///////////////////////////////////////////////////////////////////////////
		
	///////////////////////////////////////////////////////////////////////////
	// Adapter
	///////////////////////////////////////////////////////////////////////////
	
	public class TagDocumentListAdapter extends ArrayAdapter<Document> {
		
		private WeakHashMap<String, Bitmap> mBitmapCache;
		
		public TagDocumentListAdapter(List<Document> docList) {
			super(TagDocumentFragment.this.getActivity(), R.layout.tag_document_item, R.id.txtName, docList);
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
			View imgDelete = v.findViewById(R.id.imgDelete);
			
			if (mIsEditing) {
				imgDelete.setVisibility(View.VISIBLE);
			} else {
				imgDelete.setVisibility(View.GONE);
			}
			
			return v;
		}
	}
}