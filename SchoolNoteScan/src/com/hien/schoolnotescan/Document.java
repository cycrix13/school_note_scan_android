package com.hien.schoolnotescan;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Document {
	
	public String 		mPreviewPath;
	public String 		mName;
	public Date 		mDate 			= new Date();
	public List<String> mNotePathArr 	= new ArrayList<String>();
	public List<String> mTagList 		= new ArrayList<String>();
	
	public Document() {
		
		mTagList.add("Tag 1");
		mTagList.add("Tag 2");
		mTagList.add("Tag 3");
	}
	
	@Override
	public String toString() {

		return mName;
	}
}
