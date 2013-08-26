//
//  HelperOpenCV.cpp
//  schoolnote
//
//  Created by Hien on 8/15/13.
//  Copyright (c) 2013 Hai. All rights reserved.
//

#include "HelperOpenCV.h"


std::vector<cv::Rect> HelperOpenCV::findHighLightArea(cv::Mat inputImage) {
    std::vector<cv::Rect> vectorRect;
    vectorRect.clear();
    cv::Mat inputMat = inputImage.clone();
    //cv::resize(inputImage, inputMat, cv::Size(1280, 1280));
    cv::Rect pageRect = findPage(inputMat);
    
    int flags = 4 + (255 << 8) +
    (1 == 1 ? CV_FLOODFILL_FIXED_RANGE : 0);
    cv::Vec3b back = inputMat.at<cv::Vec3b>(0, 0);
    cv::Mat mask;
	mask.create(inputMat.rows + 2, inputMat.cols + 2, CV_8UC1);
	mask = cv::Scalar::all(0);
    
    cv::Mat dst = inputMat.clone();
    cv::cvtColor(inputMat, dst, CV_BGRA2BGR);
    cv::Rect rectTop;
    
    floodFill(dst, cv::Point(0,0), cv::Scalar(255, 255, 255), &rectTop, cv::Scalar(20, 20, 20),cv::Scalar(70, 70, 70), flags);
    floodFill(dst, cv::Point(dst.cols - 1,0), cv::Scalar(255, 255, 255), &rectTop, cv::Scalar(20, 20, 20),cv::Scalar(70, 70, 70), flags);
    floodFill(dst, cv::Point(0,dst.rows - 1), cv::Scalar(255, 255, 255), &rectTop, cv::Scalar(20, 20, 20),cv::Scalar(70, 70, 70), flags);
    floodFill(dst, cv::Point(dst.cols - 1,dst.rows - 1), cv::Scalar(255, 255, 255), &rectTop, cv::Scalar(20, 20, 20),cv::Scalar(70, 70, 70), flags);
    
    
	cv::Rect ccomp;
    cv::Mat mask1;
    cv::Mat maskDark;
    cv::Mat maskWhite;
	cv::inRange(dst, cv::Scalar(150, 150, 150, 0), cv::Scalar(255, 255, 255, 255), mask1);
	cv::bitwise_not(mask1, mask1);
    
    cv::Mat element = getStructuringElement( cv::MORPH_RECT,
                                            cv::Size( 13, 13 ),
                                            cv::Point( 6, 6) );
	cv::erode(mask1, mask1, element);
    
    cv::Mat element1 = getStructuringElement( cv::MORPH_RECT,
                                             cv::Size( 5, 5 ),
                                             cv::Point( 2, 2) );
	cv::dilate(mask1, mask1, element1);
    
    cv::Mat dest;
    dest.create(inputMat.rows, inputMat.cols, inputMat.type());
    dest = cv::Scalar(255, 255, 255);
    inputMat.copyTo(dest, mask1);
    
    cv::Mat mask3;
    cv::inRange(dest, cv::Scalar(0, 0, 0, 0), cv::Scalar(50, 50, 50, 255), mask3);
    cv::bitwise_not(mask3, mask3);
    
    
    cv::Rect rect;
    cv::Mat mask2;
    cv::Mat result;
    result = inputMat.clone();
    cv::Mat resized;
    cv::resize(inputMat, resized, cv::Size(inputMat.cols + 2, inputMat.rows + 2));
    mask2.create(inputMat.rows + 2, inputMat.cols + 2, CV_8UC1);
    mask2 = cv::Scalar::all(0);
    
    for (int i = 0; i < mask1.rows; i++)
        for (int j = 0; j < mask1.cols; j++) {
            uchar k = mask1.at<uchar>(i, j);
            uchar k1 = mask3.at<uchar>(i,j);
            if (pageRect.width != 0)
                if (!(i > pageRect.y && j > pageRect.x && i < pageRect.y + pageRect.height && j < pageRect.x + pageRect.width))
                    continue;
            if (k == 255 && k1 == 255) {
                mask2 = cv::Scalar::all(0);
                int area = cv::floodFill(mask1, mask2, cv::Point(j, i), cv::Scalar(0), &rect, cv::Scalar(0), cv::Scalar(20), flags);
                if (area > 2000) {
                    cv::rectangle(result, rect, cv::Scalar(0, 255,0), 2);
                    cv::Mat m;
                    m.create(inputMat.rows + 2, inputMat.cols + 2, inputMat.type());
                    m = cv::Scalar(255, 255, 255);
                    resized.copyTo(m, mask2);
                    int dmax = 5;
                    int k = 1;
                    for (int l = 0; l < dmax; l++) {
                        if ((rect.x - k >=0 && rect.x + rect.width + k < m.cols)) {
                            rect.x = rect.x - k;
                            rect.width += 2*k;
                        }
                        
                        if ((rect.y - k >=0 && rect.y + rect.height + k < m.rows)) {
                            rect.y = rect.y - k;
                            rect.height += 2*k;
                        }
                    }
                    vectorRect.push_back(rect);
                    //cv::rectangle(inputImage, cv::Rect(rect.x , rect.y , (int)(rect.width ), (int)(rect.height )), cv::Scalar(0, 255,0), 2);
                }
            }
        }
    return vectorRect;
}

cv::Rect HelperOpenCV::findPage(cv::Mat inputMat) {
    cv::Mat resizeMat;
    int flags = 4 + (255 << 8) +
    (1 == 1 ? CV_FLOODFILL_FIXED_RANGE : 0);
    
    cv::resize(inputMat, resizeMat, cv::Size(300, 300));
    
    cv::Mat mask;
    cv::inRange(resizeMat, cv::Scalar(150, 150, 150, 0), cv::Scalar(255, 255, 255, 255), mask);
    cv::Mat mask2;
    
    cv::Mat element3 = getStructuringElement( cv::MORPH_RECT,
                                             cv::Size( 3, 3 ),
                                             cv::Point( 1, 1) );
	cv::dilate(mask, mask, element3);
    
    cv::resize(mask, mask2, cv::Size(50, 50));
    
    cv::Rect rect, maxRect;
    float rh = (float)inputMat.rows / resizeMat.rows;
    float rw = (float)inputMat.cols / resizeMat.cols;
    for (int i = 0; i < 50; i++)
        for (int j = 0 ;j < 50; j++){
            uchar k = mask2.at<uchar>(i, j);
            if (k == 255) {
                int area1 = cv::floodFill(mask2, cv::Point(j,i), cv::Scalar(255), &rect, cv::Scalar(0), cv::Scalar(20), flags);
                if (area1 > 200) {
                    return cv::Rect(rect.x * 6 * rw, rect.y * 6 * rh, (int)(rect.width * 6 * rw), (int)(rect.height * 6 * rh));
                    break;
                }
            }
        }
    return cv::Rect(0,0,0,0);
}