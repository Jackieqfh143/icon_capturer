#include "../include/utils.h"
#include <android/log.h>
#include <iostream>
#include <fstream>
unsigned char* Utils::getMatData(const cv::Mat& mat){
    return mat.data;
}

cv::Mat Utils::makeMat(unsigned char* imgData, int rows, int cols, int channels){
    if (channels > 3){
        return cv::Mat(rows, cols, CV_8UC4, imgData);
    }
    return cv::Mat(rows, cols, CV_8UC3, imgData);
}

float Utils::bb_intersection_over_union(cv::Rect boxA, cv::Rect boxB) {

//    __android_log_print(ANDROID_LOG_DEBUG, "qfh", "boxA.x: %d", boxA.x);
//    __android_log_print(ANDROID_LOG_DEBUG, "qfh", "boxA.y: %d", boxA.y);
//    __android_log_print(ANDROID_LOG_DEBUG, "qfh", "boxA.width: %d", boxA.width);
//    __android_log_print(ANDROID_LOG_DEBUG, "qfh", "boxA.height: %d", boxA.height);
//
//    __android_log_print(ANDROID_LOG_DEBUG, "qfh", "boxB.x: %d", boxB.x);
//    __android_log_print(ANDROID_LOG_DEBUG, "qfh", "boxB.y: %d", boxB.y);
//    __android_log_print(ANDROID_LOG_DEBUG, "qfh", "boxB.width: %d", boxB.width);
//    __android_log_print(ANDROID_LOG_DEBUG, "qfh", "boxB.height: %d", boxB.height);

    // determine the (x, y)-coordinates of the intersection rectangle
    int xA = std::max(boxA.x, boxB.x);
    int yA = std::max(boxA.y, boxB.y);
    int xB = std::min(boxA.x + boxA.width, boxB.x + boxB.width);
    int yB = std::min(boxA.y + boxA.height, boxB.y + boxB.height);
    // compute the area of intersection rectangle
    float interArea = std::max(0, xB - xA + 1) * std::max(0, yB - yA + 1);
    // compute the area of both the prediction and ground-truth rectangles
    float boxAArea = boxA.width * boxA.height;
    float boxBArea = boxB.width * boxB.height;
    // compute the intersection over union by taking the intersection area
    // and dividing it by the sum of prediction + ground-truth areas - the intersection area
    float iou = interArea / float(boxAArea + boxBArea - interArea);
    // return the intersection over union value

//    __android_log_print(ANDROID_LOG_DEBUG, "qfh", "iou: %f", iou);
    return iou;
}

