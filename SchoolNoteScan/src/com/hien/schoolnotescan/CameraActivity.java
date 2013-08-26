package com.hien.schoolnotescan;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class CameraActivity extends Activity {
	
	public static final int REQUEST_CODE = 111;
	
	public static final int RESULT_CODE_RETAKE = 113;
	
	private CoreCanvas mCanvas;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		
		String path = getIntent().getStringExtra("path");
		Bitmap bitmap = BitmapFactory.decodeFile(path);
		
		// rotate bitmap 90 degrees
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
				
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.camera, menu);
		return true;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Detect rectangle
	///////////////////////////////////////////////////////////////////////////
	public List<Rect> detectRect(Mat img) {
		
		List<Rect> rectList = new ArrayList<Rect>();
		MainActivity.mCore.detectRect(img, rectList);
		return rectList;
	}
	
	public void drawRectOnImg(Mat img, List<Rect> rectList) {
		
		for (Rect rect : rectList) {
			
			mCanvas.addBoxWithoutStage(new Point(rect.x, rect.y), 
					new Point(rect.width, rect.height));
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Waiting icon
	///////////////////////////////////////////////////////////////////////////
	public void showWaitingIcon(boolean show) {
		
		ProgressBar prg = (ProgressBar) findViewById(R.id.prgWaiting);
		if (show)
			prg.setVisibility(View.VISIBLE);
		else
			prg.setVisibility(View.GONE);
	}
}