package com.hien.schoolnotescan;

import android.support.v4.app.Fragment;

public class RootFragment extends Fragment {
	
	protected boolean mIsEditing = false;
	
	public boolean canEdit() {
		return false;
	}
	
	public boolean isEditing() {
		return mIsEditing;
	}

	public void toggleEdit() { }
}
