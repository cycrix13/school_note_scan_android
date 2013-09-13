package com.hien.schoolnotescan;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnLayoutChangeListener;

public class CoreCanvas extends SurfaceView implements SurfaceHolder.Callback, OnLayoutChangeListener{
	
	private Bitmap 		mBmBuffer; // double buffer
	public Bitmap 		mBg;
	private Paint 		mGreenPaint;
	private Paint 		mBlackPaint;
	private boolean 	mCreated;
	private Point		mParentSize;
	public LayerManager mLayerManager;
	
	///////////////////////////////////////////////////////////////////////////
	// Public method
	///////////////////////////////////////////////////////////////////////////

	public void setBg(Bitmap bg) {
		
		mBg = bg;
		if (mCreated) {
			mBg = processBg(bg);
			setSize();
			Mat mat = new Mat(); 
			Utils.bitmapToMat(mBg, mat);
			drawRectOnImg(mat, detectRect(mat));
			mat.release();
		}
	}
	
	public void addBox(Point pos, Point size) {
		
		mLayerManager.addBox(pos, size);
		Render();
		Draw();
	}
	
	public void addBoxWithoutStage(Point pos, Point size) {
		
		mLayerManager.addBoxWithoutStage(pos, size);
		Render();
		Draw();
	}
	
	public void undo() {
		
		mLayerManager.undo();
		Render();
		Draw();
	}
	
	@Override
	public void onLayoutChange(View v, int left, int top, int right,
			int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
		
//		if (!mCreated) {
			mParentSize = new Point(right - left, bottom - top);
//			mBg = processBg(mBg);
//			setSize();
//			Mat mat = new Mat(); 
//			Utils.bitmapToMat(mBg, mat);
//			drawRectOnImg(mat, detectRect(mat));
//			mat.release();
//		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Private method
	///////////////////////////////////////////////////////////////////////////
	
	private void init() {
		
		mGreenPaint = new Paint();
		mGreenPaint.setARGB(255, 0, 255, 0);
		mBlackPaint = new Paint();
		mBlackPaint.setARGB(255, 0, 0, 0);
		
		mGreenPaint.setTextSize(40);
		mGreenPaint.setAntiAlias(true);
		
		getHolder().addCallback(this);
		
		mLayerManager = new LayerManager(getResources());
	}
	
//	private void setSize() {
//		
//		float parentRatio = (float) mParentSize.x / mParentSize.y;
//		float canvasRatio = (float) mBg.getWidth() / mBg.getHeight();
//		
//		int h, w;
//		if (parentRatio > canvasRatio) {
//			// dock height
//			h = mParentSize.y;
//			w = h * mBg.getWidth() / mBg.getHeight();
//		} else {
//			// dock width
//			w = mParentSize.x;
//			h = w * mBg.getHeight() / mBg.getWidth();
//		}
//		
//		getHolder().setFixedSize(w, h);
//	}
	
	private void setSize() {
		
		getHolder().setFixedSize(mBg.getWidth(), mBg.getHeight());
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		super.onTouchEvent(event);
		
		mLayerManager.onTouch(event, new Point());
		Render();
		Draw();
		
		return true;
	}
	
	private void Render() {
		
		if (mBmBuffer == null)
			return;
		
		Canvas canvas = new Canvas(mBmBuffer);
		
		if (mBg != null) {
			canvas.drawRGB(255, 255, 255);
			canvas.drawBitmap(mBg, 0, 0, null);
		}

		mLayerManager.draw(canvas, new Point());
	}
	
	private void Draw() {
		
		Canvas canvas = null;
		canvas = getHolder().lockCanvas();
		if (canvas != null) {
			Rect dstRect = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
			
			canvas.drawBitmap(mBmBuffer, null, dstRect, null);	// Flush double buffer
			
			getHolder().unlockCanvasAndPost(canvas);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		
		mBmBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		
		if (!mCreated && mBg != null) {
			mBg = processBg(mBg);
			setSize();
			Mat mat = new Mat(); 
			Utils.bitmapToMat(mBg, mat);
			drawRectOnImg(mat, detectRect(mat));
			mat.release();
		}
		mCreated = true;
		Render();
		Draw();
	}
	
	private Bitmap processBg(Bitmap inBg) {
		
		// Calculate optimize background size
		
		double parentSizeRatio = (double) mParentSize.x / mParentSize.y;
		double inBgRate = (double) inBg.getWidth() / inBg.getHeight();
		
		int newW = 0;
		int newH = 0;
		if (inBgRate > parentSizeRatio) {
			// scale as width
			newW = mParentSize.x;
			newH = mParentSize.x * inBg.getHeight() / inBg.getWidth();
		} else {
			// scale as height
			newH = mParentSize.y;
			newW = mParentSize.y * inBg.getWidth() / inBg.getHeight();
		}
		
		Bitmap outBg = Bitmap.createBitmap(newW, newH, Config.ARGB_8888);
		Canvas canvas = new Canvas(outBg);
		canvas.drawBitmap(inBg, 
				new Rect(0, 0, inBg.getWidth(), inBg.getHeight()),
				new Rect(0, 0, newW, newH), null);
		
		inBg.recycle();
		
		return outBg;
	}
	
	private void drawRectOnImg(Mat img, List<org.opencv.core.Rect> rectList) {
		
		for (org.opencv.core.Rect rect : rectList) {
			
			addBoxWithoutStage(new Point(rect.x, rect.y), 
					new Point(rect.width, rect.height));
		}
	}
	
	private List<org.opencv.core.Rect> detectRect(Mat img) {
		
		List<org.opencv.core.Rect> rectList = new ArrayList<org.opencv.core.Rect>();
		MainActivity.mCore.detectRect(img, rectList);
		return rectList;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Useless stuff
	// Their existences just to make Java happy 
	///////////////////////////////////////////////////////////////////////////
	
	public CoreCanvas(Context context) {
		super(context);
		init();
	}
	
	public CoreCanvas(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CoreCanvas(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) { }

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) { }	
}