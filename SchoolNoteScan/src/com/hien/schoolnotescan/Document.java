package com.hien.schoolnotescan;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.graphics.Bitmap;

public class Document {
	
	private Bitmap 	mBmDocument ;
	private String 	mName;
	private Date 	mDate;
	private List<Bitmap> mBmNoteArr = new ArrayList<Bitmap>();
	
	public Document() {
		
		
	}
}
