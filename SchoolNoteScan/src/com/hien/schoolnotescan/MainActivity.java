package com.hien.schoolnotescan;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ImageButton;

public class MainActivity extends FragmentActivity {
	
	private static final String  TAG = "MainActivity";
	
	public static CppCore mCore;
	
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	public static final int MEDIA_TYPE_IMAGE = 1;
//	public static final int MEDIA_TYPE_VIDEO = 2;
	
	private Uri fileUri;
	
	private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
    	@Override
    	public void onManagerConnected(int status) {
    		switch (status) {
    		case LoaderCallbackInterface.SUCCESS:
    		{
    			Log.i(TAG, "OpenCV loaded successfully");
    			
    			System.loadLibrary("SchoolNoteScan"); 
    			mCore = new CppCore();

    		} break;
    		default:
    		{
    			super.onManagerConnected(status);
    		} break;
    		}
    	}
    };
    
    private ExpandableListView 	expListView;
    private SlidingMenu 		mSideMenu;
    
    private Fragment[] 				mFragmentArr = new Fragment[7];
    private DocumentFragment		mDocFrag;
    private TutorialFragment 		mTutorFrag;
    private WifiSharingFragment 	mWifiFrag;
    private AboutFragment 			mAboutFrag;
    private HelpFragment 			mHelpFrag;
    private RestorePurchaseFragment mRestoreFrag;
    private UnlockPremiumFragment 	mUnlockFrag;
    private Fragment				mActiveFrag;
    
    ///////////////////////////////////////////////////////////////////////////
    // Override method
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Start flash screen
        Intent intent = new Intent(this, FlashActivity.class);
        startActivityForResult(intent, FlashActivity.REQUEST_CODE);
        
        // Create side menu
        mSideMenu = new SlidingMenu(this);
        mSideMenu.setMode(SlidingMenu.LEFT);
        mSideMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        mSideMenu.setShadowWidth(8);
		mSideMenu.setShadowDrawable(R.drawable.side_menu_shadow);
        mSideMenu.setBehindOffset(100);
        mSideMenu.setFadeDegree(0.35f);
        mSideMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        mSideMenu.setMenu(R.layout.side_menu);
	
		// Set menu button
		((ImageButton) findViewById(R.id.btnMenu))
		.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				mSideMenu.toggle();
			}
		});
        
        // Set adapter for side menu
        expListView = (ExpandableListView) findViewById(R.id.lstMenu);
        ExpandableListAdapter adapter = new ExpandableListAdapter(this, expListView); 
        expListView.setAdapter(adapter);
        
        // Expand all menu
        for (int i = 0; i < adapter.getGroupCount(); i++)
        	expListView.expandGroup(i);
        
        // Create fragment list
        FragmentManager manager = getSupportFragmentManager();
        mFragmentArr[0] = mDocFrag 		= (DocumentFragment) 		manager.findFragmentById(R.id.fragDocument);
        mFragmentArr[1] = mTutorFrag 	= (TutorialFragment) 		manager.findFragmentById(R.id.fragTutor);
        mFragmentArr[2] = mWifiFrag 	= (WifiSharingFragment) 	manager.findFragmentById(R.id.fragWifi);
        mFragmentArr[3] = mAboutFrag 	= (AboutFragment) 			manager.findFragmentById(R.id.fragAbout);
        mFragmentArr[4] = mHelpFrag 	= (HelpFragment) 			manager.findFragmentById(R.id.fragHelp);
        mFragmentArr[5] = mRestoreFrag 	= (RestorePurchaseFragment) manager.findFragmentById(R.id.fragRestore);
        mFragmentArr[6] = mUnlockFrag 	= (UnlockPremiumFragment) 	manager.findFragmentById(R.id.fragUnlock);
        
        // Hide other fragments
        mActiveFrag = mDocFrag;
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        for (Fragment f : mFragmentArr)
        	if (f != mActiveFrag)
        		t.hide(f);
        t.commit();
        
        // Add document fragment as default
//        getSupportFragmentManager().beginTransaction().add(R.id.layoutContent, mDocFrag).commit();
    }

    @Override
    protected void onResume() {
    
    	super.onResume();
    	OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }
    
    @Override
    protected void onDestroy() {
    	
    	mCore.release();
    	super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        
        return true;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	switch (requestCode) {
    	case FlashActivity.REQUEST_CODE:
    		if (resultCode == FlashActivity.RESULT_CODE_RETAKE) 
    			takePhoto();
    		break;
    		
    	case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
    		if (resultCode == RESULT_OK) {
                // Image captured and saved to fileUri specified in the Intent
                String path = fileUri.getPath();
                Intent i = new Intent(this, CameraActivity.class);
                i.putExtra("path", path);
                startActivityForResult(i, CameraActivity.REQUEST_CODE);
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
            }
    		break;
    		
    	case CameraActivity.REQUEST_CODE:
    		if (resultCode == CameraActivity.RESULT_CODE_RETAKE)
    			takePhoto();
    		break;
    	}
    }
    
	///////////////////////////////////////////////////////////////////////////
	// Public method
	///////////////////////////////////////////////////////////////////////////
    
    public void takePhoto() {
    	
    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

        // start the image capture Intent
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }
    
	///////////////////////////////////////////////////////////////////////////
	// side menu event handler
	///////////////////////////////////////////////////////////////////////////
    
    public void onDocumentsClick() {
    	
    	getSupportFragmentManager().beginTransaction().hide(mActiveFrag).show(mDocFrag).commit();
    	mActiveFrag = mDocFrag;
    	
    	mSideMenu.toggle();
    }
    
    public void onTutorialClick() {
    	
    	getSupportFragmentManager().beginTransaction().hide(mActiveFrag).show(mTutorFrag).commit();
    	mActiveFrag = mTutorFrag;
    	
    	mSideMenu.toggle();
    }
    
    public void onWifiSharingClick() {
    	
    	getSupportFragmentManager().beginTransaction().hide(mActiveFrag).show(mWifiFrag).commit();
    	mActiveFrag = mWifiFrag;
    	
    	mSideMenu.toggle();
    }
    
    public void onAboutClick() {
    	
    	getSupportFragmentManager().beginTransaction().hide(mActiveFrag).show(mAboutFrag).commit();
    	mActiveFrag = mAboutFrag;
    	
    	mSideMenu.toggle();
    }
    
    public void onHelpClick() {
    	
    	getSupportFragmentManager().beginTransaction().hide(mActiveFrag).show(mHelpFrag).commit();
    	mActiveFrag = mHelpFrag;
    	
    	mSideMenu.toggle();
    }
    
    public void onRestorePurchaseClick() {
    	
    	getSupportFragmentManager().beginTransaction().hide(mActiveFrag).show(mRestoreFrag).commit();
    	mActiveFrag = mRestoreFrag;
    	
    	mSideMenu.toggle();
    }
    
    public void onUnlockPremium() {
    	
    	getSupportFragmentManager().beginTransaction().hide(mActiveFrag).show(mUnlockFrag).commit();
    	mActiveFrag = mUnlockFrag;
    	
    	mSideMenu.toggle();
    }
    
    public void onTagsClick(int index) {
    	
    	mSideMenu.toggle();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Minor stuff
    ///////////////////////////////////////////////////////////////////////////
    
    /** Create a file Uri for saving an image or video */
    public static Uri getOutputMediaFileUri(int type){
          return Uri.fromFile(getOutputMediaFile(type));
    }
    
    /** Create a File for saving an image or video */
    public static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                  Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }
}