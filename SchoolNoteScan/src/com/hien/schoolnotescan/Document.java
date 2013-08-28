package com.hien.schoolnotescan;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.graphics.Bitmap;

public class Document {
	
	public Bitmap 		mBmDocument;
	public String 		mName;
	public Date 		mDate = new Date();
	public List<Bitmap> mBmNoteArr = new ArrayList<Bitmap>();
	
	public Document() {
		
		
	}
	
	@Override
	public String toString() {

		return mName;
	}
}
