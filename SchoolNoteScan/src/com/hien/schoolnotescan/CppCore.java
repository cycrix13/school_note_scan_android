package com.hien.schoolnotescan;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

public class CppCore {
	
	private long thiz = 0;
	
	// Init core c++
	public CppCore() {
		
		thiz = nativeInit();
	}
	
	// clean up
	public void release() {
		
		nativeRelease(thiz);
	}
	
	public void detectRect(Mat img, List<Rect> rectList) {
		
		// Allocate rect list
		if (rectList == null)
			return;
		
		if (rectList.size() != 0)
			rectList.clear();
		
		// Detect rect
		MatOfRect rectMat = new MatOfRect();
		nativeDetectRect(thiz, img.nativeObj, rectMat.nativeObj);
		
		// Convert MatOfRect to List<Rect>
		rectList.addAll(rectMat.toList());
	}
	
	private static native long nativeInit();
	private static native void nativeRelease(long thiz);
	private static native void nativeDetectRect(long thiz, long img, long matOfRect);
}
