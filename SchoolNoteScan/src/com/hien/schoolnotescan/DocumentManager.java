package com.hien.schoolnotescan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

public class DocumentManager {
	
	public List<Document> mDocList = new ArrayList<Document>();
	
	///////////////////////////////////////////////////////////////////////////
	// Public method
	///////////////////////////////////////////////////////////////////////////
	
	public DocumentManager(MainActivity act) {
		
		File f = act.getFileStreamPath(GlobalVariable.INDEX_FILE);
		
		try {
			if (f != null && f.isFile())
				load(act);
			else
				save(act);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String generateRandomImageFileName(Activity act) {
		
		Random random = new Random(System.currentTimeMillis());
		String name = null;
		File f = null;
		do {
			// generate file name
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < GlobalVariable.IMAGE_FILE_NAME_LENGTH; i++) {
				builder.append((char) ('a' + random.nextInt('z' - 'a')));
			}
			
			// check existence
			name = builder.toString();
			f = act.getFileStreamPath(name);
		} while (f.isFile());
		
		return name + GlobalVariable.IMAGE_FILE_FORMAT;
	}
	
	public void save(Activity act) throws JSONException, IOException {
		
		// build json object 
		JSONObject root = new JSONObject();
		root.put("docNum", (int) mDocList.size());
		JSONArray jDocArr = new JSONArray();
		root.put("docArr", jDocArr);
		for (Document doc : mDocList) {
			JSONObject jDoc = new JSONObject();
			jDocArr.put(jDoc);
			jDoc.put("name", doc.mName);
			jDoc.put("date", date2String(doc.mDate));
			jDoc.put("previewPath", doc.mPreviewPath);
			JSONArray jNotepathArr = new JSONArray();
			jDoc.put("notePathArr", jNotepathArr);
			for (String path : doc.mNotePathArr)
				jNotepathArr.put(path);
		}
		
		// Write to file
		FileOutputStream outStream = act.openFileOutput(GlobalVariable.INDEX_FILE, Context.MODE_PRIVATE);
		OutputStreamWriter writer = new OutputStreamWriter(outStream);
		writer.write(root.toString());
		
		// Close output file
		writer.close();
		outStream.close();
	}
	
	public void load(MainActivity act) throws IOException, JSONException {
		
		// Read from file
		FileInputStream inStream = act.openFileInput(GlobalVariable.INDEX_FILE);
		InputStreamReader reader = new InputStreamReader(inStream);
		
		// Read all character
		char[] charBuffer = new char[1000];
		StringBuilder builder = new StringBuilder();
		int charRead = 0;
		do {
			charRead = reader.read(charBuffer);
			builder.append(charBuffer, 0, charRead);
		} while (charRead == 1000);
		
		// Close input file
		reader.close();
		inStream.close();
		
		// Clear old data
		mDocList.clear();
		
		// Parse json to java object
		JSONObject jRoot = new JSONObject(builder.toString());
		JSONArray jDocArr = jRoot.getJSONArray("docArr");
		for (int i = 0; i < jDocArr.length(); i++) {
			JSONObject jDoc = jDocArr.getJSONObject(i);
			Document doc = new Document();
			doc.mName = jDoc.getString("name");
			doc.mPreviewPath = jDoc.getString("previewPath");
			doc.mDate = string2Date(jDoc.getString("date"));
			JSONArray jBoxArr = jDoc.getJSONArray("notePathArr");
			doc.mNotePathArr.clear();
			for (int j = 0; j < jBoxArr.length(); j++)
				doc.mNotePathArr.add(jBoxArr.getString(j));
			
			mDocList.add(doc);
		}
	}
	
	public static String saveBitmap(Bitmap bm, Context ct) throws IOException {
		
		String fileName = DocumentManager.generateRandomImageFileName((Activity) ct);
		FileOutputStream out = ct.openFileOutput(fileName, Context.MODE_PRIVATE);
		bm.compress(CompressFormat.JPEG, 90, out);
		out.close();
		
		return fileName;
	}
	
	public void ClearCache() {
		
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Private method
	///////////////////////////////////////////////////////////////////////////
	
	public static String date2String(Date date) {
		
		return new SimpleDateFormat("dd.mm.yy hh:mm:ss", Locale.US).format(date);
	}
	
	public static Date string2Date(String dateStr) {
		  
		try {
			return new SimpleDateFormat("dd.mm.yy hh:mm:ss", Locale.US).parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}