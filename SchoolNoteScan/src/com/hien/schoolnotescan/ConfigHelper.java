package com.hien.schoolnotescan;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class ConfigHelper {

	private static final String CONFIG_FILE_NAME = "config.json"; 
	
	// Json fields name
	private static final String FIRST_TIME 	= "firstTime";
	private static final String SHOW_SPLASH	= "showSplash";
	
	// data
	public boolean firstTime = true;
	public boolean showSplash = true;

	// Instance member
	
	public ConfigHelper() {
		Load();
	}
		
	public synchronized void Save() {
		
		JSONObject root = new JSONObject();

		// Write fields into root
		try {
			root.put(FIRST_TIME, firstTime);
			root.put(SHOW_SPLASH, showSplash);
			// ....
		} catch (JSONException e1) {
			return;
		}
		
		// ....

		// Write json into file
		FileOutputStream ostream = null;
		try {
			ostream = _ct.openFileOutput(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
			OutputStreamWriter writer = new OutputStreamWriter(ostream, Charset.defaultCharset());
			writer.write(root.toString());
			writer.close();
			ostream.close();
			
		} catch (Exception e) {
			return;
		}
	}

	public synchronized void Load() {

		// open file
		FileInputStream istream = null;
		try {
			istream = _ct.openFileInput(CONFIG_FILE_NAME);
		} catch (FileNotFoundException e) {
			Save();
			try {
				istream = _ct.openFileInput(CONFIG_FILE_NAME);
			} catch (FileNotFoundException e1) {
				return;
			}
		}

		// read file
		InputStreamReader reader = new InputStreamReader(istream, Charset.defaultCharset());
		char[] charBuffer = new char[1024];
		String jsonString = "";
		try {
			while (reader.read(charBuffer) != -1) {
				jsonString += new String(charBuffer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			reader.close();
			istream.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// parse json
		JSONObject root = null;
		try {
			root = new JSONObject(jsonString);
			
			// read value
			firstTime 	= root.getBoolean(FIRST_TIME);
			showSplash 	= root.getBoolean(SHOW_SPLASH);
			// ....
		} catch (JSONException e) {
			return;
		}
	}

	// Singletance member
	private static ConfigHelper _instance = null;
	private static Context _ct = null;
	public static void InitInstance(Context ct) {
		_ct = ct;
		if (_instance == null)
			_instance = new ConfigHelper();
	}
	
	public static void release() {

		_instance = null;
		_ct = null;
	}

	public static ConfigHelper instance() {
		return _instance;
	}
}