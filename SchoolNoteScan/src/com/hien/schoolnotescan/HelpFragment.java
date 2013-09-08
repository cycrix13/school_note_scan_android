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

				HelpAnswerActivity.newInstance(getActivity(), mQuesArr[position], mQuesArr[position]);
			}
		});
	}
}