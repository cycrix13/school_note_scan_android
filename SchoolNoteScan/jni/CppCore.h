/*
 * CppCore.h
 *
 *  Created on: Aug 17, 2013
 *      Author: HIEN-PC
 */

#ifndef CPPCORE_H_
#define CPPCORE_H_

#include <opencv2/opencv.hpp>

class CppCore {
public:
	CppCore();
	virtual ~CppCore();

	void DetectRect(cv::Mat * img, cv::Mat * matOfRect);
};

#endif /* CPPCORE_H_ */
