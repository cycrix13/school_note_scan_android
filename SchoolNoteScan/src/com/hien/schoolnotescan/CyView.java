package com.hien.schoolnotescan;

import android.graphics.Canvas;
import android.graphics.Point;
import android.view.MotionEvent;

public abstract class CyView {
	
	protected CyView 	mLockView;
	protected CyView 	mParent;	
	protected Point		mPos = new Point();
	
	public CyView setPos(Point pos) {
		mPos.x = pos.x;
		mPos.y = pos.y;
		return this;
	}
	
	public Point getPos() {
		return new Point(mPos); 
	}
	
	public CyView setParent(CyView parent) {
		
		mParent = parent;
		return this;
	}
	
	/**
	 * 
	 * @param canvas
	 * @param parentPos
	 * parent's position being relative to canvas
	 */
	public abstract void draw(Canvas canvas, Point parentPos);
	
	/**
	 * 
	 * @param touchEvent
	 * @param parentPos parent's position being relative to canvas
	 * @return has comsume this event
	 */
	public abstract boolean onTouch(MotionEvent touchEvent, Point parentPos);
	
	/**
	 * 
	 * @param pos position of point being relative to parent
	 * @param parentPos parent's position being relative to canvas
	 * @return can process
	 */
	public abstract boolean canProcess(Point pos, Point parentPos);
	
	public CyView getLock() {
		return mLockView;
	}
	
	public boolean acquireLock(CyView lockedView) {
		
		return false;
	}
	
	public void releaseLock(CyView lockedView) { }

	public static Point addPoint(Point p1, Point p2) {
		
		return new Point(p1.x + p2.x, p1.y + p2.y);
	}
	
	public static Point subPoint(Point p1, Point p2) {
		
		return new Point(p1.x - p2.x, p1.y - p2.y);
	}
	
	public static Point divPoint(Point p1) {
		
		return new Point(p1.x / 2, p1.y / 2);
	}
	
	public static Point rotatePoint(Point p, float angle) {
		
		double angle2 = angle / 180 * Math.PI;
		double sin = Math.sin(angle2);
		double cos = Math.cos(angle2);
		
		return new Point(
				(int) (p.x * cos - p.y * sin),
				(int) (p.x * sin + p.y * cos));
	}
	
	public static double distance(Point p) {
		
		return Math.sqrt(p.x * p.x + p.y * p.y);
	}
	
	public static double distance(Point p1, Point p2) {
		
		int dx = p1.x - p2.x;
		int dy = p1.y - p2.y;
		return Math.sqrt(dx * dx + dy * dy);
	}
}