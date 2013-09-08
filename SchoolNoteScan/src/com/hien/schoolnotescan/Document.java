package com.hien.schoolnotescan;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.graphics.Bitmap;

public class Document {
	
	public String 		mPreviewPath;
	public String 		mName;
	public Date 		mDate = new Date();
	public List<String> mNotePathArr = new ArrayList<String>();
	
	public Document() {
		
	}
	
	@Override
	public String toString() {

		return mName;
	}
}
