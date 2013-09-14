package com.hien.schoolnotescan;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.renderscript.ProgramFragmentFixedFunction.Builder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import com.hien.schoolnotescan.CameraActivity.Listener;
import com.hien.schoolnotescan.LayerManager.BoxState;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;
import com.mobeta.android.dslv.DragSortListView.RemoveListener;

public class DetailDocumentActivity extends Activity implements Listener {
	
	final static private String APP_KEY = "eiqo4tec8eiygci";
	final static private String APP_SECRET = "fo7zeh2ntlad0bd";
	final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;;
	
	public static final int REQUEST_CODE = GlobalVariable.DETAIL_ACTIVITY_REQUEST_CODE;

	// static fields used to pass Document to this activity
	// NOTE: CLEAN UP after use!
	private static Document sDoc;
	private static DocumentManager sDocManager; 

	// Data
	private List<String> 	mBoxList;
	private BoxListAdapter 	mAdapter;
	private Document 		mDoc;
	private DocumentManager mDocManager;
	
	// Activity Status
	private boolean 		mIsEditing;
	
	// Layout elements
	private DragSortListView mLst;
	private TextView 		txtTitle;
	private TextView 		txtName;
	private TextView 		txtTag;
	private View			layoutExport;
	private ImageButton		btnEmail;
	private ImageButton		btnPrinter;
	private ImageButton		btnDropBox;
	private ImageButton		btnGoogleDrive;
	
	///////////////////////////////////////////////////////////////////////////
	// Override method
	///////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_document);
		
		// Get document item, document manager
		mDoc = sDoc;
		mDocManager = sDocManager;
		
		// Clean up static fields to avoid memory leak 
		sDoc = null;
		sDocManager = null;
		
		getLayoutElements();
		
		updateLayout();
		
		setButtonEvent();
		
		setUpListView();
	}
	
	@Override
	protected void onResume() {
	
		super.onResume();
		updateLayout();
		
		if (mDBApi == null)
			return;
		
	   AndroidAuthSession session = mDBApi.getSession();

        // The next part must be inserted in the onResume() method of the
        // activity from which session.startAuthentication() was called, so
        // that Dropbox authentication completes properly.
        if (session.authenticationSuccessful()) {
            try {
                // Mandatory call to complete the auth
                session.finishAuthentication();

                // Store it locally in our app for later use
                TokenPair tokens = session.getAccessTokenPair();
                final java.io.File file = new java.io.File(pdfPath);
				final FileInputStream inputStream = new FileInputStream(file);
				
				Thread t = new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							Entry response = mDBApi.putFile("/Document.pdf", inputStream, file.length(), null, null);
							Toast.makeText(DetailDocumentActivity.this, "Uploaded to Dropbox", Toast.LENGTH_SHORT).show();
						} catch (DropboxException e) {
							e.printStackTrace();
						}
					}
				});
				t.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}
	
	@Override
	public void onBackPressed() {
	
		finish();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	@Override
	public void newDocCameraCallback(List<BoxState> boxList, Bitmap bm) {
		
		addNewBoxes(boxList, bm);
	}

	@Override
	public void cancelCameraCallback() {
		
	}
	
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
		case REQUEST_ACCOUNT_PICKER:
			if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
				String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {
					credential.setSelectedAccountName(accountName);
					service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), 
							new GsonFactory(), credential).build();
					saveFileToDrive();
				}
			}
			break;
		case REQUEST_AUTHORIZATION:
			if (resultCode == Activity.RESULT_OK) {
				saveFileToDrive();
			} else {
				startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
			}
			break;
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// Public method
	///////////////////////////////////////////////////////////////////////////
	
	public static void newInstance(Document doc, DocumentManager docManager, MainActivity act) {
		
		Intent intent = new Intent(act, DetailDocumentActivity.class);
		sDoc = doc;
		sDocManager = docManager;
		act.startActivity(intent);
		act.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
	
	public void onEditClick() {
		
		mIsEditing = !mIsEditing;
		updateTitleBar();
		mAdapter.notifyDataSetChanged();
	}
	
	public void onAddBoxClick() {
		
		CameraActivity.newInstance(this, this, CameraActivity.MODE_CAMERA);
	}
	
	public void onInfoClick() {
		
		
	}
	
	public void onEditTagClick() {
		
		TagActivity.newInstance(this, mDoc);
	}
	
	public void onExportClick() {
		
		int visibility = layoutExport.getVisibility();
		switch (visibility) {
		case View.VISIBLE:
			visibility = View.GONE;
			break;
		case View.INVISIBLE:
		case View.GONE:
			visibility = View.VISIBLE;
			break;
		}
		layoutExport.setVisibility(visibility);
	}
	
	public void onEmailExport() {
		
		// Hide export menu
		layoutExport.setVisibility(View.GONE);
		
		// Render pdf
		java.io.File shareDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		shareDir.mkdirs();
		String pdfPath = shareDir.getAbsolutePath() + "/" + mDoc.mName + ".pdf";
//		PdfHelper.RenderPdf(mDoc.mNotePathArr, pdfPath, this, 0);
		
		// Call email intent
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_SUBJECT, "[School Note Scan] " + mDoc.mName);
		i.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + pdfPath));
		try {
		    startActivity(Intent.createChooser(i, "Send mail..."));
		} catch (android.content.ActivityNotFoundException ex) {
		    Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void onPrinterExport() {

		layoutExport.setVisibility(View.GONE);
		Toast.makeText(this, "Under construction!", Toast.LENGTH_SHORT).show();
	}

	
	private DropboxAPI<AndroidAuthSession> mDBApi;
	private String pdfPath;
	public void onDropBoxExport() {

		// Hide export menu
		layoutExport.setVisibility(View.GONE);
		
		// Render pdf
		java.io.File shareDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		shareDir.mkdirs();
		pdfPath = shareDir.getAbsolutePath() + "/" + mDoc.mName + ".pdf";
		PdfHelper.RenderPdf(mDoc.mNotePathArr, pdfPath, this, 0);
		
		// In the class declaration section:

		try {
			// And later in some initialization function:
			AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
			AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
			mDBApi = new DropboxAPI<AndroidAuthSession>(session);
			mDBApi.getSession().startAuthentication(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
         
//        Log.i("DbExampleLog", "The uploaded file's rev is: " + newEntry.rev);
	}

	public void onGoogleDriveExport() {

		// Hide export menu
		layoutExport.setVisibility(View.GONE);
		
		// Render pdf
		java.io.File shareDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		shareDir.mkdirs();
		String pdfPath = shareDir.getAbsolutePath() + "/" + mDoc.mName + ".pdf";
		PdfHelper.RenderPdf(mDoc.mNotePathArr, pdfPath, this, 0);
		
		upload2GoogleDive(pdfPath);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Private method
	///////////////////////////////////////////////////////////////////////////
	
	private void getLayoutElements() {
		
		txtName 	= (TextView) findViewById(R.id.txtName);
		txtTitle 	= (TextView) findViewById(R.id.txtTitle);
		txtTag 		= (TextView) findViewById(R.id.txtTag);
		mLst 		= (DragSortListView) findViewById(R.id.lstBox);
		layoutExport= findViewById(R.id.layoutExport);
		btnEmail	= (ImageButton) findViewById(R.id.btnEmail);
		btnPrinter	= (ImageButton) findViewById(R.id.btnPrinter);
		btnDropBox	= (ImageButton) findViewById(R.id.btnDropBox);
		btnGoogleDrive	= (ImageButton) findViewById(R.id.btnGoogleDrive);
	}
	
	private void updateLayout() {
		
		txtName.setText(mDoc.mName);
		txtTitle.setText(mDoc.mName);
		
		StringBuilder builder = new StringBuilder();
		if (mDoc.mTagList.size() == 0)
			builder.append("No tag");
		else
			for (int i = 0; i < mDoc.mTagList.size(); i++) {
				builder.append(mDoc.mTagList.get(i));
				if (i < mDoc.mTagList.size() - 1)
					builder.append(", ");
			}
			
		txtTag.setText(builder.toString());
	}
	
	private void setButtonEvent() {
		
		// Set back button event
		((Button) findViewById(R.id.btnBack))
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				onBackPressed();
			}
		});
		
		// Set edit button event
		((Button) findViewById(R.id.btnEdit))
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				onEditClick();
			}
		});
		
		// Set add box button event
		((ImageButton) findViewById(R.id.btnAddBox))
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				onAddBoxClick();
			}
		});
		
		// Set info button event
		((ImageButton) findViewById(R.id.btnInfo))
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				onInfoClick();
			}
		});
		
		// Set edit tag button event
		((ImageButton) findViewById(R.id.btnEditTag))
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				onEditTagClick();
			}
		});

		// Set export button event
		((ImageButton) findViewById(R.id.btnExport))
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				onExportClick();
			}
		});
		
		// Set export button event
		((ImageButton) findViewById(R.id.btnEmail))
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				onEmailExport();
			}
		});
		
		// Set export button event
		((ImageButton) findViewById(R.id.btnPrinter))
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				onPrinterExport();
			}
		});
		
		// Set export button event
		((ImageButton) findViewById(R.id.btnDropBox))
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				onDropBoxExport();
			}
		});
		
		// Set export button event
		((ImageButton) findViewById(R.id.btnGoogleDrive))
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				onGoogleDriveExport();
			}
		});
	}
	
	private void setUpListView() {
		
		mBoxList = mDoc.mNotePathArr;
		mAdapter = new BoxListAdapter();
		mLst.setAdapter(mAdapter);
		
		// Set drop event, reorder box list
		mLst.setDropListener(new DropListener() {

			@Override
			public void drop(int from, int to) {

				if (from == to)
					return;

				String box = mAdapter.getItem(from);
				mAdapter.remove(box);
				mAdapter.insert(box, to);
				
				try {
					mDocManager.save(DetailDocumentActivity.this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// Set delete event
		mLst.setRemoveListener(new RemoveListener() {

			@Override
			public void remove(int which) {

				mAdapter.remove(mAdapter.getItem(which));
				
				try {
					mDocManager.save(DetailDocumentActivity.this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		// Set click event to avoid unintended delete button click event
		mLst.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) { }
		});
	}
	
	private void updateTitleBar() {
		
		Button btnEdit = (Button) findViewById(R.id.btnEdit);
		if (mIsEditing) {
			btnEdit.setText("Done");
			btnEdit.setBackgroundResource(R.drawable.button_iphone_highlight);
			mLst.setDragEnabled(mIsEditing);
		} else {
			btnEdit.setText("Edit");
			btnEdit.setBackgroundResource(R.drawable.button_iphone_default);
		}
		
		mLst.setDragEnabled(mIsEditing);
	}
	
	private void addNewBoxes(List<BoxState> boxList, Bitmap bm) {
		
		// Add box bitmap
		for (BoxState box : boxList) {

			// Extract box bitmap
			Rect r 	 = new Rect();
			r.left 	 = Math.max(0, box.pos.x);
			r.top  	 = Math.max(0, box.pos.y);
			r.right  = Math.min(bm.getWidth(), r.left + box.size.x);
			r.bottom = Math.min(bm.getHeight(), r.top + box.size.y);

			Bitmap bmBox = Bitmap.createBitmap(bm, r.left, r.top, r.right - r.left, r.bottom - r.top);

			// Save to file
			String boxPath = null;
			try {
				boxPath = DocumentManager.saveBitmap(bmBox, this);
			} catch (IOException e) {
				e.printStackTrace();
			}

			mAdapter.add(boxPath);
		}
		
		// save
		try {
			mDocManager.save(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	private static Uri fileUri;
	private String path;
	private static Drive service;
	static final int REQUEST_ACCOUNT_PICKER = 1;
	static final int REQUEST_AUTHORIZATION = 2;
	
	private GoogleAccountCredential credential;
	
	private void upload2GoogleDive(String p) {
		
		path = p;
		credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(),
				Arrays.asList(DriveScopes.DRIVE));
	    startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
	}
	
	private void saveFileToDrive() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// File's binary content
					java.io.File fileContent = new java.io.File(path);
					FileContent mediaContent = new FileContent("image/jpeg", fileContent);

					// File's metadata.
					File body = new File();
					body.setTitle(fileContent.getName());
					body.setMimeType("application/pdf");

					File file = service.files().insert(body, mediaContent).execute();
					if (file != null) {
//						showToast("Photo uploaded: " + file.getTitle());
//						startCameraIntent();
					}
				} catch (UserRecoverableAuthIOException e) {
					startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// ListView adapter
	///////////////////////////////////////////////////////////////////////////
	
	public class BoxListAdapter extends ArrayAdapter<String> {
		
		private WeakHashMap<String, Bitmap> mBitmapCache = new WeakHashMap<String, Bitmap>();
		
		public BoxListAdapter() {
			
			super(DetailDocumentActivity.this, R.layout.box_item, R.id.txtDummy, mBoxList);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View v = super.getView(position, convertView, parent);
	
			// Update data for recycle view
			String path = getItem(position);
			ImageView img = (ImageView) v.findViewById(R.id.imgBox);
			
			// Check cache
			Bitmap bm = mBitmapCache.get(path);
			if (bm == null) {
				// Miss
				bm = BitmapFactory.decodeFile(getFileStreamPath(path).getAbsolutePath());
				mBitmapCache.put(path, bm);
			}
			img.setImageBitmap(bm);
						
			// Set visibility for reorder and delete button
			View imgReorder = v.findViewById(R.id.imgReorder);
			View imgDelete = v.findViewById(R.id.imgDelete);
			
			if (mIsEditing) {
				imgReorder.setVisibility(View.VISIBLE);
				imgDelete.setVisibility(View.VISIBLE);
			} else {
				imgReorder.setVisibility(View.GONE);
				imgDelete.setVisibility(View.GONE);
			}
			
			return v;
		}
	}
}
