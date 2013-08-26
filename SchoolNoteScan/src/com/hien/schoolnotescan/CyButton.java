package com.hien.schoolnotescan;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;

public class CyButton extends CyView {

	protected int 		mRID, mRID_hl;
	protected Resources mRes;
	protected Bitmap 	mBitmap;
	protected Bitmap 	mBitmap_hl;
	protected Point 	mPosOffset;
	protected float		mAngle;
	protected boolean 	mHighLight = false;
//	protected TouchStrategy mStrategy;
	
	protected int 	mTrackPointerID = -1; 
	protected Point mStartTrackPoint;
	protected Point mOrgPos;
	private GestureListener mListenner = new GestureListener();

	public static class GestureListener {
		public void onDown(CyButton button) {}
		public void onMove(CyButton button, Point pos, Point orgPos, Point realPos) {}
		public void onUp(CyButton button) {}
		public void onDoubleTap(CyButton button) {}
	}
	
	public CyButton(int RID, int RID_hl, Resources res) {
		
		mRID = RID;
		mRID_hl = RID_hl;
		mRes = res;
		mBitmap = BitmapFactory.decodeResource(res, mRID);
		mBitmap_hl = BitmapFactory.decodeResource(res, mRID_hl);
		mPosOffset = new Point(-mBitmap.getWidth()/2, -mBitmap.getHeight()/2);
	}
	
	@Override
	public CyButton setParent(CyView parent) {
		
		mParent = parent;
		return this;
	}
	
	public void setListener(GestureListener listener) {
		
		mListenner = listener;
	}
	
	public void setAngle(float angle) {
		
		mAngle = angle;
	}

	@Override
	public void draw(Canvas canvas, Point parentPos) {
		
		int x = parentPos.x + mPos.x + mPosOffset.x;
		int y = parentPos.y + mPos.y + mPosOffset.y;
		canvas.drawBitmap(mHighLight ? mBitmap_hl : mBitmap, x, y, null);
	}

	@Override
	public boolean onTouch(MotionEvent touchEvent, Point parentPos) {
		
		if (mParent.getLock() == this) {	// I'm locking
			
			int pointerIndex = touchEvent.findPointerIndex(mTrackPointerID);
			
			// Always consume motion event
			handleProcess(touchEvent, pointerIndex, parentPos);
			
		} else {	// I'm idle
			for (int i = 0; i < touchEvent.getPointerCount(); i++) {
				if (touchEvent.getActionMasked() == MotionEvent.ACTION_DOWN ||
					touchEvent.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
					
					Point pos = new Point((int) touchEvent.getX(i), (int)touchEvent.getY(i));
					pos = CyView.subPoint(pos, parentPos);
					if (canProcess(pos, parentPos)) { // Can process
						
						beginProcess(touchEvent, i, pos);
						
						break;
					}
				}
			}
		}
		return false;
	}
	
	public void beginProcess(MotionEvent touchEvent, int i, Point pos) {

		if (!mParent.acquireLock(this)) {
			debugLog("Acquired log failed");
			return;
		} else
			debugLog("Acquired log successfully, tracking...");
		
		mTrackPointerID = touchEvent.getPointerId(i);
		mOrgPos = mPos;
		mStartTrackPoint = pos;
		mHighLight = true;
		
		mListenner.onDown(this);
	}
	
	public void handleProcess(MotionEvent touchEvent, 
			int pointerIndex, Point parentPos) {
		// TODO Auto-generated method stub
		
		if (touchEvent.getActionMasked() == MotionEvent.ACTION_UP ||
			touchEvent.getActionMasked() == MotionEvent.ACTION_POINTER_UP &&
			touchEvent.getActionIndex() == mTrackPointerID) {
			
			// end tracking
			mParent.releaseLock(this);
			mHighLight = false;
			debugLog("Release lock and finish tracking");
			
			mListenner.onUp(this);
			
		} else {
			// Move
			Point realPos = new Point();
			realPos.x = (int) touchEvent.getX(pointerIndex);
			realPos.y = (int) touchEvent.getY(pointerIndex);
			Point pos = CyView.subPoint(realPos, parentPos);
			
			mListenner.onMove(this, pos, mOrgPos, realPos);
//			mPos = CyView.addPoint(mOrgPos, CyView.subPoint(pos, mStartTrackPoint));
		}
	}
	
	public boolean canProcess(Point pos, Point parentPos) {
		
		return contain(pos);
	}

	@Override
	public boolean acquireLock(CyView lockedView) {
		
		throw new RuntimeException("Forbidden method: CyButton has no child, therefore cannot be acquired lock");
	}

	@Override
	public void releaseLock(CyView lockedView) {
		
		throw new RuntimeException("Forbidden method: CyButton has no child, therefore cannot be released lock");
	}
	
	private boolean contain(Point pos) {
		
		return 
				pos.x >= (mPos.x + mPosOffset.x) &&
				pos.x < (mPos.x + mPosOffset.x + mBitmap.getWidth()) &&
				pos.y >= (mPos.y + mPosOffset.y) &&
				pos.y < (mPos.y + mPosOffset.y + mBitmap.getHeight());
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Debug stuff
	///////////////////////////////////////////////////////////////////////////
	static boolean DEBUG = true;
	static String HEADER = "CyButton";
	static String TAG 	 = "CyDebug";

	static void debugLog(String content) {

		if (DEBUG) Log.d(TAG, HEADER + ": " + content);
	}
}