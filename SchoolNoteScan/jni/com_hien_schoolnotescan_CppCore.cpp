#include <com_hien_schoolnotescan_CppCore.h>
#include <opencv2/opencv.hpp>
#include <vector>
#include <android/log.h>
#include "CppCore.h"


#define LOG_TAG "SchoolNoteScan"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

using namespace std;
using namespace cv;

/*
 * Class:     com_hien_schoolnotescan_CppCore
 * Method:    nativeInit
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_hien_schoolnotescan_CppCore_nativeInit
(JNIEnv *, jclass)
{
	return (jlong) new CppCore();
}

/*
 * Class:     com_hien_schoolnotescan_CppCore
 * Method:    nativeRelease
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_hien_schoolnotescan_CppCore_nativeRelease
(JNIEnv *, jclass, jlong thiz)
{
	delete (CppCore *) thiz;
}

/*
 * Class:     com_hien_schoolnotescan_CppCore
 * Method:    nativeDetectRect
 * Signature: (JJJ)V
 */
JNIEXPORT void JNICALL Java_com_hien_schoolnotescan_CppCore_nativeDetectRect
(JNIEnv *, jclass, jlong thiz, jlong img, jlong matOfRect)
{
	((CppCore *) thiz)->DetectRect((Mat *) img, (Mat *) matOfRect);
}
