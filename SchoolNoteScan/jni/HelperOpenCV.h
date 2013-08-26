//
//  HelperOpenCV.h
//  schoolnote
//
//  Created by Hien on 8/15/13.
//  Copyright (c) 2013 Hai. All rights reserved.
//

#ifndef __schoolnote__HelperOpenCV__
#define __schoolnote__HelperOpenCV__

#include <opencv2/opencv.hpp>

class HelperOpenCV 	{
    public: static std::vector<cv::Rect> findHighLightArea(cv::Mat img);
    public: static cv::Rect findPage(cv::Mat inputMat);
};

#endif /* defined(__schoolnote__HelperOpenCV__) */
