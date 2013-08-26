package com.hien.schoolnotescan;

import com.hien.schoolnotescan.CyButton.GestureListener;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;

public class Layer extends CyView {
	
	protected Point mSize = new Point();
	protected float mAngle = 0; 
	
	protected CyButton[] mButtonArr = new CyButton[8];
	protected CyButton mBtnLT;
	protected CyButton mBtnMT;
	protected CyButton mBtnClose;
	protected CyButton mBtnLM;
	protected CyButton mBtnRM;
	protected CyButton mBtnLB;
	protected CyButton mBtnMB;
	protected CyButton mBtnRB;
	
	private int   mTrackPointerID = -1; 
	private Point mStartTrackPoint;
	private Point mOrgPos;
	private Point mOrgSize;
	
	private Paint mWhitePaint;
	
	///////////////////////////////////////////////////////////////////////////
	// Public method
	///////////////////////////////////////////////////////////////////////////
	public Layer(Point pos, Point size, Resources res) {

		setPos(pos);
		setSize(size);
		
		// Create 9 button
		mButtonArr[0] = mBtnLT = new CyButton(R.drawable.bullet_black, R.drawable.bullet_black, res);
		mButtonArr[1] = mBtnMT = new CyButton(R.drawable.bullet_black, R.drawable.bullet_black, res);
		mButtonArr[2] = mBtnClose = new CyButton(R.drawable.xicon, R.drawable.xicon, res);
		mButtonArr[3] = mBtnLM = new CyButton(R.drawable.bullet_black, R.drawable.bullet_black, res);
		mButtonArr[4] = mBtnRM = new CyButton(R.drawable.bullet_black, R.drawable.bullet_black, res);
		mButtonArr[5] = mBtnLB = new CyButton(R.drawable.bullet_black, R.drawable.bullet_black, res);
		mButtonArr[6] = mBtnMB = new CyButton(R.drawable.bullet_black, R.drawable.bullet_black, res);
		mButtonArr[7] = mBtnRB = new CyButton(R.drawable.bullet_black, R.drawable.bullet_black, res);
		
		// Set event for 8 buttons
		for (int i = 0; i < 8; i++) {
			CyButton btn = mButtonArr[i];
			btn.setListener(new GestureListener() {
				
				private int mIndex;
				public GestureListener init(int index) {
					mIndex = index;
					return this;
				}
				
				@Override
				public void onDown(CyButton button) {
					onResizeBegin(button);
				}
				
				@Override
				public void onMove(CyButton button, Point pos, Point orgPos, Point realPos) {
					onResizeMove(button, pos, orgPos, mIndex, realPos);
				}
				
				@Override
				public void onUp(CyButton button) {
					// TODO Auto-generated method stub
					onResizeFinish(button);
				}
			}.init(i));
		}
		
		// Set parent for child button
		for (CyButton btn : mButtonArr)
			btn.setParent(this);
		
		// Set position for child button
		setChildPos();
		
		// Create paint for rectangle
		mWhitePaint = new Paint();
		mWhitePaint.setARGB(255, 0, 0, 255);
		mWhitePaint.setStyle(Paint.Style.STROKE);
		mWhitePaint.setStrokeWidth(2);
	}

	@Override
	public Layer setParent(CyView parent) {
		
		mParent = parent;
		return this;
	}
	
	public void setSize(Point size) {
		
		mSize.x = size.x;
		mSize.y = size.y;
	}
	
	public void setAngle(float angle) {
		
		mAngle = angle;
		setChildPos();
	}
	
	private CyView mCadidateView;
	@Override
	public boolean canProcess(Point pos, Point parentPos) {
		
		Point realParentPos = CyView.addPoint(parentPos, mPos);
		Point childPos = CyView.subPoint(pos, mPos);
		
		mCadidateView = null;
		
		// Test 8 buttons
		for (CyButton btn : mButtonArr) 
			if (btn.canProcess(childPos, realParentPos)) {
				mCadidateView = btn;
				break;
			}
		
		// Test myself
		if (mCadidateView == null &&
				contain(pos, mPos, mSize))
			mCadidateView = this;
		
	 	return mCadidateView != null;		
	}
	
	@Override
	public boolean onTouch(MotionEvent touchEvent, Point parentPos) {
		
		if (mParent.getLock() == this) {	// I'm locking
			
			int pointerIndex = touchEvent.findPointerIndex(mTrackPointerID);
			
			// Always consume motion event
			handleProcess(touchEvent, pointerIndex, parentPos);
			
			return true;
			
		} else {	// I'm idle

			// Scan all pointer
			for (int i = 0; i < touchEvent.getPointerCount(); i++) {
				if (touchEvent.getActionMasked() == MotionEvent.ACTION_DOWN ||
					touchEvent.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
					
					Point pos = new Point((int) touchEvent.getX(i), (int)touchEvent.getY(i));
					pos = CyView.subPoint(pos, parentPos);
					if (canProcess(pos, parentPos)) { // Can process
						
						beginProcess(touchEvent, i, pos, parentPos);
						
						break;
					}
				}
			}
		}
		
		return false;
	}
	
	@Override
	public boolean acquireLock(CyView lockedView) {
		
		if (this.mLockView != null)
			return false;
		
		if (!mParent.acquireLock(this))
			return false;
		
		this.mLockView = lockedView;
		
		return true;
	}
	
	@Override
	public void releaseLock(CyView lockedView) {

		mParent.releaseLock(this);
		this.mLockView = null;
	}
	
	@Override
	public void draw(Canvas canvas, Point parentPos) {
		
		Point realPos = CyView.addPoint(parentPos, mPos);
		
		Rect r = new Rect(
				realPos.x, realPos.y,
				realPos.x + mSize.x, realPos.y + mSize.y);
				
		canvas.drawRect(r, mWhitePaint);
		
		for (CyButton b : mButtonArr)
			b.draw(canvas, realPos);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Private method
	///////////////////////////////////////////////////////////////////////////

	private void onResizeBegin(CyButton button) {

		mOrgSize = mSize;
		mOrgPos  = mPos;
		
		if (button != mBtnClose)
			((LayerManager) mParent).pushStage();
	}
	
	private void onResizeMove(CyButton button, Point pos, Point orgPos, int index, Point realPos) {

		switch (index) {
		case 0: {
			Point newSize = CyView.addPoint(mOrgSize, CyView.subPoint(mOrgPos, realPos));
			mSize = new Point(Math.max(newSize.x, 40), Math.max(newSize.y, 40));
			mPos = CyView.subPoint(CyView.addPoint(mOrgPos, mOrgSize), mSize);
			break;
		}
		
		case 1: {
			Point newSize = new Point(mOrgSize.x, mOrgSize.y + mOrgPos.y - realPos.y);
			mSize = new Point(Math.max(newSize.x, 40), Math.max(newSize.y, 40));
			mPos = CyView.subPoint(CyView.addPoint(mOrgPos, mOrgSize), mSize);
			break;
		}
		
		case 3: {
			Point newSize = new Point(mOrgSize.x + mOrgPos.x - realPos.x, mOrgSize.y);
			mSize = new Point(Math.max(newSize.x, 40), Math.max(newSize.y, 40));
			mPos = new Point(mOrgPos.x + mOrgSize.x - mSize.x, mOrgPos.y);
			break;
		}
		
		case 4: {
			mSize = new Point(Math.max(pos.x, 40), mOrgSize.y);
			break;
		}
		
		case 5: {
			Point orgLB = new Point(mOrgPos.x, mOrgPos.y + mOrgSize.y);
			Point extendVec = CyView.subPoint(realPos, orgLB);
			Point offsetSize = new Point(-extendVec.x, extendVec.y);
			Point newSize = CyView.addPoint(mOrgSize, offsetSize);
			mSize = new Point(Math.max(newSize.x, 40), Math.max(newSize.y, 40));
			mPos = new Point(mOrgPos.x + mOrgSize.x - mSize.x, mOrgPos.y);
			break;
		}
		
		case 6: {
			mSize = new Point(mOrgSize.x, Math.max(pos.y, 40));
			break;
		}
		
		case 7: {
			mSize = new Point(Math.max(pos.x, 40), Math.max(pos.y, 40));
			break;
		}	
		}
		
		setChildPos();	
	}
	
	private void onResizeFinish(CyButton button) {

		if (button == mBtnClose)
			((LayerManager) mParent).removeBox(this);
	}

	private void setChildPos() {
		
		Point halfSize = CyView.divPoint(mSize);
		
		mBtnLT.setPos(CyView.rotatePoint(new Point(), mAngle));
		mBtnMT.setPos(CyView.rotatePoint(new Point(halfSize.x, 0), mAngle));
		mBtnClose.setPos(CyView.rotatePoint(new Point(mSize.x, 0), mAngle));
		mBtnLM.setPos(CyView.rotatePoint(new Point(0, halfSize.y), mAngle));
		mBtnRM.setPos(CyView.rotatePoint(new Point(mSize.x, halfSize.y), mAngle));
		mBtnLB.setPos(CyView.rotatePoint(new Point(0, mSize.y), mAngle));
		mBtnMB.setPos(CyView.rotatePoint(new Point(halfSize.x, mSize.y), mAngle));
		mBtnRB.setPos(CyView.rotatePoint(new Point(mSize.x, mSize.y), mAngle));
	}
	
	private void beginProcess(MotionEvent touchEvent, int i, Point pos, Point parentPos) {
		
		if (mCadidateView == this) {
			
			debugLog("begin pointer down");
			if (!mParent.acquireLock(this))
				return;
			
			((LayerManager) mParent).pushStage();
			
			mTrackPointerID = touchEvent.getPointerId(i);
			mOrgPos = mPos;
			mStartTrackPoint = pos;
			 
		} else if (mCadidateView != null) {
			debugLog("Deliver touch event to child");
			mCadidateView.onTouch(touchEvent, CyView.addPoint(parentPos, mPos));
		}
	}
	
	private void handleProcess(MotionEvent touchEvent, int pointerIndex, Point parentPos) {
		
		if (this.mLockView == null) {	// I lock it myself
			
			Point pos = new Point();
			
			if (touchEvent.getActionMasked() == MotionEvent.ACTION_UP ||
				touchEvent.getActionMasked() == MotionEvent.ACTION_POINTER_UP &&
				touchEvent.getActionIndex() == mTrackPointerID) {
				
				// end tracking
				mParent.releaseLock(this);
				debugLog("finish pointer down");
				
			} else {
				
				// Move
				pos.x = (int) touchEvent.getX(pointerIndex);
				pos.y = (int) touchEvent.getY(pointerIndex);
				pos = CyView.subPoint(pos, parentPos);

				mPos = CyView.addPoint(mOrgPos, CyView.subPoint(pos, mStartTrackPoint));
			}
			
		} else {	// My child lock it
			
			// Deliver to child
			mLockView.onTouch(touchEvent, CyView.addPoint(parentPos, mPos));
		}
	}
	
	private boolean contain(Point pos, Point dock, Point size) {
		
		int l = dock.x;
		int r = dock.x + size.x;
		int t = dock.y;
		int b = dock.y + size.y;
		
		return 
				pos.x >= l && pos.x < r &&
				pos.y >= t && pos.y < b;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Debug stuff
	///////////////////////////////////////////////////////////////////////////
	static boolean DEBUG = true;
	static String HEADER = "Layer";
	static String TAG = "CyDebug";
	
	static void debugLog(String content) {
		
		if (DEBUG) Log.d(TAG, HEADER + ": " + content);
	}
}