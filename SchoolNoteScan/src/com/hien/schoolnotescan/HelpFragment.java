package com.hien.schoolnotescan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HelpFragment extends RootFragment{
	
	private String[] mQuesArr = new String[] {
			"How do I contact you?",
			"How can I improve the quality of my scans?",
			"How do I scan a document with School Note Scan?",
			"How do I delete a document?",
			"How do I send or share my scanned documents?",
			"How do I edit the title of a document?",
			"How can I export my scans to Google Drive?",
			"How can I export my scans to Dropbox?",
	};
	
	private String[] mAnsArr = new String[] {
			"Send an email to support@schoolnotescan.com.",
			"Try to take the picture under a bright environment and try to remain stable. Also make" +
			" use of the autofocus feature on your phone. We do not recommend using the flash direc" +
			"tly on the document as it would create a circle of light that often makes the scan har" +
			"der to read.",
			"Launch the app and choose \"camera\" (touch the camera icon) and take a picture of you" +
			"r document. Hit \"use\" and we will try to detect the highlighted areas on your document" +
			". You can adjust this manually to ensure all your highlighted areas are selected. Fina" +
			"lly select \"save\" and School Note Scan put the highlighted notes into bullets and save" +
			" it as a new document.",
			"Simply select the document and hit the \"trash\" icon.",
			"You can send your scans by email or export your documents to Google Drive, Evernote an" +
			"d Dropbox.",
			"Select the document and click on the \"edit\" icon next to the title.",
			"To export your scans to Google Drive, you'd need a Google Drive account.  Open your do" +
			"cument and select the \"Google Drive\" icon. It will ask for your login credentials. Log" +
			"in and you will be able to upload your document to Google Drive.",
			"To export your scans to Dropbox, you’d need a Dropbox account.  Open your document and" +
			" select the \"Dropbox\" icon. It will ask for your login credentials. Login and you wi" +
			"ll be able to upload your document to Dropbox.",
	};
	
	///////////////////////////////////////////////////////////////////////////
	// Override method
	///////////////////////////////////////////////////////////////////////////

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.help_frag, container);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		super.onViewCreated(view, savedInstanceState);
		ListView lst = (ListView) getView().findViewById(R.id.lst);
		lst.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.help_item, R.id.txt, mQuesArr));
		lst.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				HelpAnswerActivity.newInstance(getActivity(), mQuesArr[position], mAnsArr[position]);
			}
		});
	}
}