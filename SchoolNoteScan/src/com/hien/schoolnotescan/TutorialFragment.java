package com.hien.schoolnotescan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TutorialFragment extends RootFragment{
	
	///////////////////////////////////////////////////////////////////////////
	// Override method
	///////////////////////////////////////////////////////////////////////////

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.tutorial_frag, container);
	}
}
