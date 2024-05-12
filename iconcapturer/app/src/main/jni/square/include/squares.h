#include "../../opencv-mobile-3.4.18-android/sdk/native/jni/include/opencv2/core/core.hpp"
#include "../../opencv-mobile-3.4.18-android/sdk/native/jni/include/opencv2/imgproc/imgproc.hpp"
#include "../../opencv-mobile-3.4.18-android/sdk/native/jni/include/opencv2/highgui/highgui.hpp"

#include "../../../../../../../../../Users/52896/AppData/Local/Android/Sdk/ndk/21.4.7075529/toolchains/llvm/prebuilt/windows-x86_64/sysroot/usr/include/c++/v1/iostream"
#include "../../../../../../../../../Users/52896/AppData/Local/Android/Sdk/ndk/21.4.7075529/toolchains/llvm/prebuilt/windows-x86_64/sysroot/usr/include/c++/v1/math.h"
#include "../../../../../../../../../Users/52896/AppData/Local/Android/Sdk/ndk/21.4.7075529/toolchains/llvm/prebuilt/windows-x86_64/sysroot/usr/include/c++/v1/string.h"
#include "../../../../../../../../../Users/52896/AppData/Local/Android/Sdk/ndk/21.4.7075529/toolchains/llvm/prebuilt/windows-x86_64/sysroot/usr/include/c++/v1/vector"

enum FilterMode {AUTO=0, SMALL, BIG, CUSTOM};

class Squares{
public:
    Squares() = default;
    ~Squares() = default;

    static const int thresh = 50,N = 5;
    static float MAX_HEIGHT, MAX_WIDTH, MIN_HEIGHT, MIN_WIDTH, MIN_SCALE;
    static int FILTER_MODE;
    static double angle( cv::Point pt1, cv::Point pt2, cv::Point pt0 );
    static void findSquares( const cv::Mat& image, std::vector<std::vector<cv::Point> >& squares );
    static void drawSquares( cv::Mat& image, const std::vector<std::vector<cv::Point> >& squares );
    static void filterSquares(std::vector<std::vector<cv::Point>>& squares,std::vector<std::vector<cv::Point>>& filteredSquares);
    static void filterSquaresByMode(const cv::Mat& image,
                                    std::vector<std::vector<cv::Point>>& squares, std::vector<std::vector<cv::Point>>& filteredSquares, FilterMode filterMode);
    static void extractSquaresImages(const cv::Mat& image, const std::vector<std::vector<cv::Point>>& squares,
                                     std::vector<cv::Mat>& extractedImages, std::vector<std::vector<int>>& rects);
};