package com.hien.schoolnotescan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnLayoutChangeListener;

public class CoreCanvas extends SurfaceView implements SurfaceHolder.Callback, OnLayoutChangeListener{
	
	private Bitmap 		mBmBuffer; // double buffer
	private Bitmap 		mBg;
	private Paint 		mGreenPaint;
	private Paint 		mBlackPaint;
	private boolean 	mCreated;
	private Point		mParentSize;
	public LayerManager mLayerManager;

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

	public void setBg(Bitmap bg) {
		
		mBg = bg;
		if (mCreated)
			setSize();		
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
		
		if (!mCreated && mBg != null) {
			mParentSize = new Point(right - left, bottom - top);
			mCreated = true;
			setSize();
		}
	}
	
	private void setSize() {
		
		float parentRatio = (float) mParentSize.x / mParentSize.y;
		float canvasRatio = (float) mBg.getWidth() / mBg.getHeight();
		
		int h, w;
		if (parentRatio > canvasRatio) {
			// dock height
			h = mParentSize.y;
			w = h * mBg.getWidth() / mBg.getHeight();
		} else {
			// dock width
			w = mParentSize.x;
			h = w * mBg.getHeight() / mBg.getWidth();
		}
		
		getHolder().setFixedSize(w, h);
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
		
		if (mBg != null)
			canvas.drawBitmap(mBg, 
					new Rect(0, 0, mBg.getWidth(), mBg.getHeight()),
					new Rect(0, 0, getWidth(), getHeight()), null);

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
		Render();
		Draw();
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
