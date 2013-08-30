package com.hien.schoolnotescan;

import java.util.ArrayList;
import java.util.List;

import com.hien.schoolnotescan.LayerManager.BoxState;
import com.mobeta.android.dslv.DragSortItemView;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;
import com.mobeta.android.dslv.DragSortListView.RemoveListener;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class DocumentFragment extends RootFragment {
	
	private DragSortListView 		mLstDoc;
	private DocumentListAdapter 	mAdapter;
	private List<Document> 			mDocList = new ArrayList<Document>();
	
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
		
		mLstDoc = (DragSortListView) getView().findViewById(R.id.lstDoc);
		
		// test data
		mDocList.add(new Document());
		mDocList.add(new Document());
		mDocList.add(new Document());
		mDocList.add(new Document());
		mDocList.add(new Document());
		
		for (int i = 0; i < mDocList.size(); i++) {
			mDocList.get(i).mName = "Note" + (i + 1);
		}
		
		// Set adapter for list view
		mAdapter = new DocumentListAdapter();
		mLstDoc.setAdapter(mAdapter);
		
		// Set document item click event
		mLstDoc.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
				DetailDocumentActivity.newInstance(mAdapter.getItem(position), (MainActivity) getActivity());
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
				
			}
		});
		
		// Set delete event
		mLstDoc.setRemoveListener(new RemoveListener() {
			
			@Override
			public void remove(int which) {
				
				mAdapter.remove(mAdapter.getItem(which));
			}
		});
		
		// Set take photo button
		((ImageButton) getView().findViewById(R.id.btnCamera))
		.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				((MainActivity) getActivity()).takePhoto(CameraActivity.RESULT_CODE_NEW_DOC);
			}
		});
	}
	
	@Override
	public boolean canEdit() {

		return true;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Public method
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	public void toggleEdit() {
		
		mIsEditing = !mIsEditing;
		mAdapter.notifyDataSetChanged();
		mLstDoc.setDragEnabled(mIsEditing);
		((MainActivity) getActivity()).updateEditButtonState();
	}
	
	public void addNewDoc(List<BoxState> boxList) {

		Document doc = new Document();
		doc.mName = "new note";
		
		// Add note bitmaps
		
		mAdapter.add(doc);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Adapter
	///////////////////////////////////////////////////////////////////////////
	
	public class DocumentListAdapter extends ArrayAdapter<Document> {
		
		private LayoutInflater mInflater;
		
		public DocumentListAdapter() {
			super(DocumentFragment.this.getActivity(), R.layout.document_item, R.id.txtName, mDocList);
			mInflater = DocumentFragment.this.getActivity().getLayoutInflater();
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
			txtTime.setText(doc.mDate.toString());
			txtDocNum.setText("" + doc.mBmNoteArr.size());
			if (doc.mBmDocument != null)
				imgPreview.setImageBitmap(doc.mBmDocument);
			
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