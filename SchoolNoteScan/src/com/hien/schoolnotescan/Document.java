package com.hien.schoolnotescan;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Document {
	
	public static class Tag {
		
		public Tag(String tag) {
			this.tag = tag;
		}
		
		public String tag;
		public boolean enable = true;
	}
	
	public String 		mPreviewPath;
	public String 		mName;
	public Date 		mDate 		= new Date();
	public List<String> mNotePathArr= new ArrayList<String>();
	public List<Tag> 	mTagList	= new ArrayList<Tag>();
	
	public Document() {

	}
	
	@Override
	public String toString() {

		return mName;
	}
	
	public boolean containTag(String tag) {
		
		for (Tag t : mTagList)
			if (t.enable && t.tag.equals(tag))
				return true;
		return false;
	}
}
