package com.hien.schoolnotescan;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DocumentFragment extends Fragment{
	
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
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Public method
	///////////////////////////////////////////////////////////////////////////
	
}
