package com.hien.schoolnotescan;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import com.hien.schoolnotescan.LayerManager.BoxState;
import com.hien.schoolnotescan.LayerManager.Stage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class CameraActivity extends Activity {
	
	public static final int MEDIA_TYPE_IMAGE = 1;
	
	public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	
	public static final int REQUEST_CODE = 111;
	
	public static final int RESULT_CODE_RETAKE = 113;
	public static final int RESULT_CODE_NEW_DOC = 114;
	public static final int RESULT_CODE_ADD_DOC = 115;
	
	private CoreCanvas mCanvas;
	private int mRequestMode = RESULT_CODE_NEW_DOC;
	
	private Uri fileUri;
	
	// Static fields for passing object between activity
	public static List<BoxState> boxList;
	
	///////////////////////////////////////////////////////////////////////////
	// Override method
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		
		// Get request mode
		mRequestMode = getIntent().getIntExtra("mode", 0);
		
		// If request mode is not specified, then error
		if (mRequestMode == 0)
			throw new RuntimeException("parameter \"mode\" not found, do you forget to pass it?");
		
		// Call camera intent
		takePhoto();	
		
		// Set add box event
		((Button) findViewById(R.id.btnAddbox)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				mCanvas.addBox(new Point(100, 100), new Point(100, 100));
			}
		});

		// Set undo event
		((Button) findViewById(R.id.btnUndo)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				mCanvas.undo();
			}
		});
		
		// Set retake event
		((Button) findViewById(R.id.btnRetake)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				setResult(RESULT_CODE_RETAKE);
				finish();
			}
		});
		
		// Set finish event
		((Button) findViewById(R.id.btnFinished)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				onFinishClick();
			}
		});
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	switch (requestCode) {
    	
    	case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
    		if (resultCode == RESULT_OK) {
    			
    			// rotate bitmap 90 degrees
    			String path = fileUri.getPath();
    			Bitmap bitmap = BitmapFactory.decodeFile(path);
    			Mat mat = new Mat();
    			Utils.bitmapToMat(bitmap, mat);
    			Mat matT = mat.t();
    			Mat matFlip = new Mat(matT.cols(), matT.rows(), CvType.CV_8UC4);
    			Core.flip(matT, matFlip, 1);
    			
    			Bitmap bitmap2 = Bitmap.createBitmap(matT.cols(), matT.rows(), Config.ARGB_8888);
    			Utils.matToBitmap(matFlip, bitmap2);
    			mCanvas = (CoreCanvas) findViewById(R.id.canvas);
    			mCanvas.setBg(bitmap2);
    			
    			// detect
    			drawRectOnImg(matFlip, detectRect(matFlip));
    			
    			// cleanup
    			mat.release();
    			matT.release();
    			matFlip.release();
    			bitmap.recycle();
            } else if (resultCode == RESULT_CANCELED) {
            	
                // User cancelled the image capture
            } else {
            	
                // Image capture failed, advise user
            }
    		break;
    	}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Public method
	///////////////////////////////////////////////////////////////////////////

	
	
	///////////////////////////////////////////////////////////////////////////
	// Private method
	///////////////////////////////////////////////////////////////////////////
	
	private void takePhoto() {
    	
    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

        // start the image capture Intent
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }
	
	private void showWaitingIcon(boolean show) {
		
		ProgressBar prg = (ProgressBar) findViewById(R.id.prgWaiting);
		if (show)
			prg.setVisibility(View.VISIBLE);
		else
			prg.setVisibility(View.GONE);
	}
	
	private void drawRectOnImg(Mat img, List<Rect> rectList) {
		
		for (Rect rect : rectList) {
			
			mCanvas.addBoxWithoutStage(new Point(rect.x, rect.y), 
					new Point(rect.width, rect.height));
		}
	}
	
	private List<Rect> detectRect(Mat img) {
		
		List<Rect> rectList = new ArrayList<Rect>();
		MainActivity.mCore.detectRect(img, rectList);
		return rectList;
	}
	
	private void onFinishClick() {
		
		switch (mRequestMode) {

			// create new document after finish this activity
			// used in Flash screen and Document screen scenarios
			case RESULT_CODE_NEW_DOC:
				setResult(mRequestMode);
				// assign static field to pass boxList to MainActivity 
				boxList = mCanvas.mLayerManager.new Stage().mBoxStateList;
				finish();
				break;
				
			// create boxes into an existed document after finish this activity
			// used in DocumentDetail screen scenario
			case RESULT_CODE_ADD_DOC:
				setResult(mRequestMode);
				// assign static field to pass boxList to MainActivity
				boxList = mCanvas.mLayerManager.new Stage().mBoxStateList;
				finish();
				break;
		}
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