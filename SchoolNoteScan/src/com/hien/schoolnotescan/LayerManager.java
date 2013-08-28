package com.hien.schoolnotescan;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;

public class LayerManager extends CyView {
	
	public List<Layer> 	mLayerList = new ArrayList<Layer>();
	public Resources 	mRes;
	public Deque<Stage>	mStageArray = new LinkedList<Stage>(); 
	
	public LayerManager(Resources res) {
		mRes = res;
	}
	
	@Override
	public LayerManager setParent(CyView parent) {
		throw new RuntimeException("Forbidden method: LayerManager has no parent");
	}

	@Override
	public void draw(Canvas canvas, Point parentPos) {
		
		for (Layer layer : mLayerList) {
			layer.draw(canvas, parentPos);
		}
	}

	@Override
	public boolean onTouch(MotionEvent touchEvent, Point parentPos) {
		
		if (mLockView != null) {
			
			mLockView.onTouch(touchEvent, parentPos);
			
		} else {
			for (Layer layer : mLayerList) 
				if (layer.onTouch(touchEvent, parentPos))
					break;
		}
	
		return false;
	}
	
	@Override
	public boolean canProcess(Point pos, Point parentPos) {
		
		return true;
	}
	
	@Override
	public boolean acquireLock(CyView lockedView) {

		if (mLockView != null)
			return false;
		
		mLockView = lockedView;
		
		return true;
	}
	
	@Override
	public void releaseLock(CyView lockedView) {

		mLockView = null;
	}
	
	public void addBox(Point pos, Point size) {
		
		pushStage();
		
		Layer l = new Layer(pos, size, mRes);
		l.setParent(this);
		mLayerList.add(l);
	}
	
	public void addBoxWithoutStage(Point pos, Point size) {
		
		Layer l = new Layer(pos, size, mRes);
		l.setParent(this);
		mLayerList.add(l);
	}
	
	public void removeBox(Layer layer) {
		
		pushStage();
		
		mLayerList.remove(layer);
	}
	
	public void pushStage() {
		// Push this stage for undo purpose
		mStageArray.addFirst(new Stage());
		debugLog("push stage n=" + mStageArray.size());
	}

	public void undo() {
		
		if (mStageArray.size() != 0)
			mStageArray.removeFirst().restore();
		
		debugLog("undo stage n=" + mStageArray.size());
	}
	
	/**
	 * 
	 * @author HIEN-PC
	 * Stage represent every step of box's changes
	 * For undo purpose
	 */
	public class Stage {
		
		public List<BoxState> mBoxStateList = new ArrayList<BoxState>();
		
		public Stage() {
			snapshot();
		}
		
		public void snapshot() {
			
			mBoxStateList.clear();
			for (Layer layer : mLayerList) {
				BoxState box = new BoxState();
				box.pos = new Point(layer.mPos);
				box.size = new Point(layer.mSize);
				mBoxStateList.add(box);
			}
		}
		
		public void restore() {
			
			mLayerList.clear();
			for (BoxState box : mBoxStateList) {
				Layer l = new Layer(new Point(box.pos), new Point(box.size), mRes);
				l.setParent(LayerManager.this);
				mLayerList.add(l);
			}
		}
	}
	
	/**
	 * 
	 * @author HIEN-PC
	 * Status of a box
	 */
	public static class BoxState {
		
		public Point pos, size;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Debug stuff
	///////////////////////////////////////////////////////////////////////////
	static boolean DEBUG = true;
	static String HEADER = "LayerManager";
	static String TAG = "CyDebug";

	static void debugLog(String content) {

		if (DEBUG) Log.d(TAG, HEADER + ": " + content);
	}
}
