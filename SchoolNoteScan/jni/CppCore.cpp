/*
 * CppCore.cpp
 *
 *  Created on: Aug 17, 2013
 *      Author: HIEN-PC
 */

#include "CppCore.h"
#include "HelperOpenCV.h"
#include <vector>
using namespace cv;
using namespace std;

CppCore::CppCore() {
	// TODO Auto-generated constructor stub

}

CppCore::~CppCore() {
	// TODO Auto-generated destructor stub
}

void CppCore::DetectRect(Mat * img, Mat * matOfRect)
{
	vector<Rect> rectList = HelperOpenCV::findHighLightArea(*img);

	// Convert vector to MatOfRect
	matOfRect->create(1, rectList.size(), CV_32SC4);
	for (int i = 0; i < rectList.size(); i++)
	{
		matOfRect->at<Rect>(i) = rectList[i];
	}
}
