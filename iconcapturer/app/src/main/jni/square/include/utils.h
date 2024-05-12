#include "../../opencv-mobile-3.4.18-android/sdk/native/jni/include/opencv2/core/core.hpp"
#include "../../opencv-mobile-3.4.18-android/sdk/native/jni/include/opencv2/imgproc/imgproc.hpp"
#include "../../opencv-mobile-3.4.18-android/sdk/native/jni/include/opencv2/highgui/highgui.hpp"

class Utils{
    Utils() = default;
    ~Utils() = default;

public:
    static cv::Mat makeMat(unsigned char* imgData, int rows, int cols, int channels);
    static unsigned char* getMatData(const cv::Mat& mat);
    static float bb_intersection_over_union(cv::Rect boxA, cv::Rect boxB);
};