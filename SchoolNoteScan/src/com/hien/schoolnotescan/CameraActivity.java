package com.hien.schoolnotescan;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;

import com.hien.schoolnotescan.LayerManager.BoxState;

public class CameraActivity extends Activity {
	
	public static final int SELECT_PICTURE = 201;
	
	public static final int MEDIA_TYPE_IMAGE = 1;
	
	public static final int REQUEST_CODE = GlobalVariable.CAMERA_ACTIVITY_REQUEST_CODE;

	public static final int MODE_CAMERA = 1;
	public static final int MODE_FILE = 2;
	
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = REQUEST_CODE + 3;
	
	private static Listener sListener;
	private static int 		sMode;
	
	private CoreCanvas 	mCanvas;
	private Listener 	mListener;
	private int 		mMode;
	
	private Uri fileUri;
	
	///////////////////////////////////////////////////////////////////////////
	// Override method
	///////////////////////////////////////////////////////////////////////////
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		
		// Get main activity + listener
		mListener = sListener;
		mMode = sMode;
		
		// Clean up static fields to avoid memory leak
		sListener = null;
		
		// Get CoreCanvas
		mCanvas = (CoreCanvas) findViewById(R.id.canvas);
		
		// Set parent size change event
		findViewById(R.id.layoutCanvas).addOnLayoutChangeListener(mCanvas);
				
		// Call camera intent
		switch (mMode) {
		case MODE_CAMERA:
			takePhoto();
			break;
			
		case MODE_FILE:
			openGallary();
			break;
		}
		
		
		// Set add box event
		((Button) findViewById(R.id.btnAddBox)).setOnClickListener(new OnClickListener() {
			
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
				
				takePhoto();
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
	protected void onResume() {
	
		super.onResume();
		Log.d("CycrixDebug", "CameraActivity resume");
	}
	
	@Override
	protected void onPause() {
	
		super.onPause();
		Log.d("CycrixDebug", "CameraActivity pause");
	}
	
	@Override
	protected void onDestroy() {
	
		super.onDestroy();
		Log.d("CycrixDebug", "CameraActivity destroy");
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	switch (requestCode) {
    	
    	case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
    		if (resultCode == RESULT_OK) {
    			
    			String path = fileUri.getPath();
    			setImage(path);
    			
            } else if (resultCode == RESULT_CANCELED) {
            	
                // User cancelled the image capture
            	mListener.cancelCameraCallback();
            	finish();
            } else {
            	
                // Image capture failed, advise user
            	mListener.cancelCameraCallback();
            	finish();
            }
    		break;
    	
    	case SELECT_PICTURE:
    		if (resultCode == RESULT_OK) {
    			
	    		Uri selectedImageUri = data.getData();
	            String strPath = getPath(selectedImageUri);
	            setImage(strPath);
    		} else {
    			
    			mListener.cancelCameraCallback();
    			finish();
    		}
    		break;
    	}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Public method
	///////////////////////////////////////////////////////////////////////////

	/**
	 * 
	 * @param act
	 * @param mode CameraActivity.RESULT_CODE_NEW_DOC or CameraActivity.RESULT_CODE_ADD_DOC 
	 */
	public static void newInstance(Activity act, Listener listener, int mode) {
		
		Intent i = new Intent(act, CameraActivity.class);
		sListener = listener;
		sMode = mode;
		act.startActivityForResult(i, CameraActivity.REQUEST_CODE);
	}
	
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
	
	private void onFinishClick() {
		
		List<BoxState> boxList = mCanvas.mLayerManager.new Stage().mBoxStateList;

		mListener.newDocCameraCallback(boxList, mCanvas.mBg);
		finish();
	}
	
	private void setImage(String imagePath) {
		
		// calculate optimize scale
		Display display = getWindowManager().getDefaultDisplay();
		int w = display.getWidth();
		int h = display.getHeight();
		int scale = 1;
		Options opt = new Options();
		opt.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(imagePath, opt);
		
		while (opt.outWidth / 2 > h && opt.outHeight / 2 > w) {
			scale = scale << 1;
			opt.outWidth = opt.outWidth >> 1;
			opt.outHeight = opt.outHeight >> 1;
		}
		
		// Decode photo with scale
		opt = new Options();
		opt.inSampleSize = scale;
		bitmap = BitmapFactory.decodeFile(imagePath, opt);

		if (mMode == MODE_CAMERA && bitmap.getWidth() > bitmap.getHeight()) {

			// rotate bitmap 90 degrees
			Mat mat = new Mat();
			Utils.bitmapToMat(bitmap, mat);
			Mat matT = mat.t();
			mat.release();
			Mat matFlip = new Mat(matT.cols(), matT.rows(), CvType.CV_8UC4);
			Core.flip(matT, matFlip, 1);
			matT.release();
			Bitmap bitmap2 = Bitmap.createBitmap(matFlip.cols(), matFlip.rows(), Config.ARGB_8888);
			Utils.matToBitmap(matFlip, bitmap2);
			matFlip.release();
			bitmap.recycle();
			bitmap = bitmap2;
		}

		mCanvas.setBg(bitmap);
	}
	
	private void openGallary() {
		
		Intent intent = new Intent();
		intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, SELECT_PICTURE);
	}
	
	private String getPath(Uri uri) {
		
		String[] projection = { MediaStore.Images.Media.DATA };
		
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if(cursor!=null)
		{
			//HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
			//THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		else return null;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Inner class
	///////////////////////////////////////////////////////////////////////////
	
	public interface Listener {
		
		public void newDocCameraCallback(List<BoxState> boxList, Bitmap bm);
		public void cancelCameraCallback();
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