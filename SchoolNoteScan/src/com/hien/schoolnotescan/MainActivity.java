package com.hien.schoolnotescan;

import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hien.schoolnotescan.CameraActivity.Listener;
import com.hien.schoolnotescan.LayerManager.BoxState;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenListener;

public class MainActivity extends FragmentActivity implements Listener {
	
	private static final String  TAG = "MainActivity";
	
//	private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
//    	@Override
//    	public void onManagerConnected(int status) {
//    		switch (status) {
//    		case LoaderCallbackInterface.SUCCESS:
//    		{
//    			Log.i(TAG, "OpenCV loaded successfully");
//    			
//    			System.loadLibrary("SchoolNoteScan"); 
//    			mCore = new CppCore();
//
//    		} break;
//    		default:
//    		{
//    			super.onManagerConnected(status);
//    		} break;
//    		}
//    	}
//    };
    
	public static CppCore mCore;
    
    private ExpandableListView 	expListView;
    private SlidingMenu 		mSideMenu;
    private ExpandableListAdapter mSideMenuAdapter;
    
    private RootFragment[] 			mFragmentArr = new RootFragment[8];
    private DocumentFragment		mDocFrag;
    private TutorialFragment 		mTutorFrag;
    private WifiSharingFragment 	mWifiFrag;
    private AboutFragment 			mAboutFrag;
    private HelpFragment 			mHelpFrag;
    private RestorePurchaseFragment mRestoreFrag;
    private UnlockPremiumFragment 	mUnlockFrag;
    private TagDocumentFragment 	mTagFrag;
    private RootFragment			mActiveFrag;
    
    ///////////////////////////////////////////////////////////////////////////
    // Override method
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Init OpenCV
//        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        Mat mat = new Mat(10, 10, CvType.CV_32SC4);
        System.loadLibrary("SchoolNoteScan"); 
		mCore = new CppCore();
        
        // Load config
        ConfigHelper.InitInstance(this);
        
        if (ConfigHelper.instance().showSplash) {
        	
        	// Start flash screen
        	Intent intent = new Intent(this, FlashActivity.class);
        	startActivityForResult(intent, FlashActivity.REQUEST_CODE);
        	Log.d("CycrixDebug", "FlashActivity start");
        }
        
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
        mSideMenu.setOnOpenListener(new OnOpenListener() {
			
			@Override
			public void onOpen() {

				mSideMenuAdapter.SetTagList(mDocFrag.mDocManager.getTagList());
			}
		});
        
        // Set adapter for side menu
        expListView = (ExpandableListView) findViewById(R.id.lstMenu);
        mSideMenuAdapter = new ExpandableListAdapter(this, expListView); 
        expListView.setAdapter(mSideMenuAdapter);
        
        // Expand all menus
        for (int i = 0; i < mSideMenuAdapter.getGroupCount(); i++)
        	expListView.expandGroup(i);
	
		// Set menu button
		((ImageButton) findViewById(R.id.btnMenu))
		.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				mSideMenu.toggle();
			}
		});
		
		// Set edit button
		((Button) findViewById(R.id.btnEdit))
		.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				mActiveFrag.toggleEdit();
			}
		});
        
        // Create fragment list
        FragmentManager manager = getSupportFragmentManager();
        mFragmentArr[0] = mDocFrag 		= (DocumentFragment) 		manager.findFragmentById(R.id.fragDocument);
        mFragmentArr[1] = mTutorFrag 	= (TutorialFragment) 		manager.findFragmentById(R.id.fragTutor);
        mFragmentArr[2] = mWifiFrag 	= (WifiSharingFragment) 	manager.findFragmentById(R.id.fragWifi);
        mFragmentArr[3] = mAboutFrag 	= (AboutFragment) 			manager.findFragmentById(R.id.fragAbout);
        mFragmentArr[4] = mHelpFrag 	= (HelpFragment) 			manager.findFragmentById(R.id.fragHelp);
        mFragmentArr[5] = mRestoreFrag 	= (RestorePurchaseFragment) manager.findFragmentById(R.id.fragRestore);
        mFragmentArr[6] = mUnlockFrag 	= (UnlockPremiumFragment) 	manager.findFragmentById(R.id.fragUnlock);
        mFragmentArr[7] = mTagFrag 		= (TagDocumentFragment) 	manager.findFragmentById(R.id.fragTag);
        
        // Hide other fragments
        mActiveFrag = mDocFrag;
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        for (Fragment f : mFragmentArr)
        	if (f != mActiveFrag)
        		t.hide(f);
        t.commit();
    }
    
    @Override
    protected void onDestroy() {
    	
    	mCore.release();
    	mCore = null;
    	ConfigHelper.release();
    	super.onDestroy();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    
    	super.onActivityResult(requestCode, resultCode, data);
    	if (resultCode != Activity.RESULT_OK)
    		return;
    	
    	switch (requestCode) {
    	case FlashActivity.REQUEST_CODE:
    		Log.d("CycrixDebug", "FlashActivity.REQUEST_CODE recived");
    		if (ConfigHelper.instance().firstTime) {
    			
    			TutorialActivity.newInstance(this, new TutorialActivity.Listener() {
    				
    				@Override
    				public void onHelpClick() {
    				
//    					MainActivity.this.onHelpClick();
    				}
    			});
    			Log.d("CycrixDebug", "TutorialActivity.newInstance");
    			
    			ConfigHelper.instance().firstTime = false;
    			ConfigHelper.instance().Save();
    		}
    			
    		break;
    	}
    }
    
    @Override
	public void newDocCameraCallback(List<BoxState> boxList, Bitmap bm) {

		mDocFrag.addNewDoc(boxList, bm);
	}

	@Override
	public void cancelCameraCallback() {
		
	}

	///////////////////////////////////////////////////////////////////////////
	// Public method
	///////////////////////////////////////////////////////////////////////////

	public void updateEditButtonState() {
    	
    	Button btnEdit = (Button) findViewById(R.id.btnEdit);
    	TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
    	
    	if (mActiveFrag.canEdit()) {
    		
    		// Set visible and appropriate text
    		btnEdit.setVisibility(View.VISIBLE);
    		if (mActiveFrag.isEditing()) {
    			btnEdit.setText("Done");
    			btnEdit.setBackgroundResource(R.drawable.button_iphone_highlight);
    		} else {
    			btnEdit.setText("Edit");
    			btnEdit.setBackgroundResource(R.drawable.button_iphone_default);
    		}
    	} else {
    		
    		// Let the button vanish
    		btnEdit.setVisibility(View.GONE);
    	}
    	
    	// Set title text 
    	if (mActiveFrag == mDocFrag) {
    		txtTitle.setText("Documents");
    	} else if (mActiveFrag == mTutorFrag) {
    		txtTitle.setText("Tutorial");
    	} else if (mActiveFrag == mWifiFrag) {
    		txtTitle.setText("Wifi Sharing");
    	} else if (mActiveFrag == mAboutFrag) {
    		txtTitle.setText("About");
    	} else if (mActiveFrag == mHelpFrag) {
    		txtTitle.setText("Help");
    	} else if (mActiveFrag == mRestoreFrag) {
    		txtTitle.setText("Restore purchase");
    	} else if (mActiveFrag == mUnlockFrag) {
    		txtTitle.setText("Unlock Premium");
    	} else if (mActiveFrag == mTagFrag) {
    		txtTitle.setText("Documents");
    	}
    }

	///////////////////////////////////////////////////////////////////////////
	// side menu event handler
	///////////////////////////////////////////////////////////////////////////
    
    public void onDocumentsClick() {
    	
    	getSupportFragmentManager().beginTransaction().hide(mActiveFrag).show(mDocFrag).commit();
    	mActiveFrag = mDocFrag;
    	
    	mSideMenu.toggle();
    	updateEditButtonState();
    	mDocFrag.mAdapter.notifyDataSetChanged();
    }
    
    public void onTutorialClick() {
    	
    	getSupportFragmentManager().beginTransaction().hide(mActiveFrag).show(mTutorFrag).commit();
    	mActiveFrag = mTutorFrag;
    	
    	mSideMenu.toggle();
    	updateEditButtonState();
    }
    
    public void onWifiSharingClick() {
    	
    	getSupportFragmentManager().beginTransaction().hide(mActiveFrag).show(mWifiFrag).commit();
    	mActiveFrag = mWifiFrag;
    	
    	mSideMenu.toggle();
    	updateEditButtonState();
    }
    
    public void onAboutClick() {
    	
    	getSupportFragmentManager().beginTransaction().hide(mActiveFrag).show(mAboutFrag).commit();
    	mActiveFrag = mAboutFrag;
    	
    	mSideMenu.toggle();
    	updateEditButtonState();
    }
    
    public void onHelpClick() {
    	
    	getSupportFragmentManager().beginTransaction().hide(mActiveFrag).show(mHelpFrag).commit();
    	mActiveFrag = mHelpFrag;
    	
    	if (mSideMenu.isMenuShowing())
    		mSideMenu.toggle();
    	updateEditButtonState();
    }
    
    public void onRestorePurchaseClick() {
    	
    	getSupportFragmentManager().beginTransaction().hide(mActiveFrag).show(mRestoreFrag).commit();
    	mActiveFrag = mRestoreFrag;
    	
    	mSideMenu.toggle();
    	updateEditButtonState();
    }
    
    public void onUnlockPremium() {
    	
    	getSupportFragmentManager().beginTransaction().hide(mActiveFrag).show(mUnlockFrag).commit();
    	mActiveFrag = mUnlockFrag;
    	
    	mSideMenu.toggle();
    	updateEditButtonState();
    }
    
    public void onTagsClick(int index, String tag) {
    	
    	mTagFrag.setTag(mDocFrag.mDocManager, tag);
    	getSupportFragmentManager().beginTransaction().hide(mActiveFrag).show(mTagFrag).commit();
    	mActiveFrag = mTagFrag;
    	
    	mSideMenu.toggle();
    	updateEditButtonState();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Pivate method
    ///////////////////////////////////////////////////////////////////////////
    

}