// The "Square Detector" program.
// It loads several images sequentially and tries to find squares in
// each image
#include "../include/squares.h"
#include "../include/utils.h"
#include <android/log.h>
#include <unordered_map>
using namespace cv;
using namespace std;

float Squares::MIN_SCALE = 0.5;
float Squares::MAX_HEIGHT = 0.5;
float Squares::MAX_WIDTH = 0.5;
float Squares::MIN_HEIGHT = 0.1;
float Squares::MIN_WIDTH = 0.1;
int Squares::FILTER_MODE = 0;

// finds a cosine of angle between vectors
// from pt0->pt1 and from pt0->pt2
 double Squares::angle( Point pt1, Point pt2, Point pt0 )
{
    double dx1 = pt1.x - pt0.x;
    double dy1 = pt1.y - pt0.y;
    double dx2 = pt2.x - pt0.x;
    double dy2 = pt2.y - pt0.y;
    return (dx1*dx2 + dy1*dy2)/sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
}

// returns sequence of squares detected on the image.
// the sequence is stored in the specified memory storage
 void Squares::findSquares(const Mat& image, vector<vector<Point> >& squares )
{
    squares.clear();
    Mat timg(image);
    Mat im_gray;
    cvtColor(timg,im_gray,CV_RGB2GRAY);
    Mat gray;

    vector<vector<Point> > contours;

    // find squares in every color plane of the image
    for( int c = 0; c < 3; c++ )
    {
        int ch[] = {c, 0};
        mixChannels(&timg, 1, &im_gray, 1, ch, 1);

        // try several threshold levels
        for( int l = 0; l < N; l++ )
        {
            // apply Canny. Take the upper threshold from slider
            // and set the lower to 0 (which forces edges merging)
            Canny(im_gray, gray, 5, Squares::thresh, 5);
//            imwrite(savePath + "canny.jpg", gray);
            // dilate canny output to remove potential
            // holes between edge segments
            dilate(gray, gray, Mat(), Point(-1,-1));
//            imwrite(savePath + "dilate.jpg", gray);

            // find contours and store them all as a list
            findContours(gray, contours, RETR_LIST, CHAIN_APPROX_SIMPLE);

            vector<Point> approx;

            // test each contour
            for( size_t i = 0; i < contours.size(); i++ )
            {
                // approximate contour with accuracy proportional
                // to the contour perimeter
                approxPolyDP(Mat(contours[i]), approx, arcLength(Mat(contours[i]), true)*0.02, true);

                // square contours should have 4 vertices after approximation
                // relatively large area (to filter out noisy contours)
                // and be convex.
                // Note: absolute value of an area is used because
                // area may be positive or negative - in accordance with the
                // contour orientation
                if( approx.size() == 4 &&
                    fabs(contourArea(Mat(approx))) > 1000 &&
                    isContourConvex(Mat(approx)) )
                {
                    double maxCosine = 0;

                    for( int j = 2; j < 5; j++ )
                    {
                        // find the maximum cosine of the angle between joint edges
                        double cosine = fabs(angle(approx[j%4], approx[j-2], approx[j-1]));
                        maxCosine = MAX(maxCosine, cosine);
                    }

                    // if cosines of all angles are small
                    // (all angles are ~90 degree) then write quandrange
                    // vertices to resultant sequence
                    if( maxCosine < 0.3 )
                        squares.push_back(approx);
                }
            }
        }
    }
}


// the function draws all the squares in the image
 void Squares::drawSquares(Mat& image, const vector<vector<Point> >& squares )
{
    for( size_t i = 0; i < squares.size(); i++ )
    {
        const Point* p = &squares[i][0];

        int n = (int)squares[i].size();
        //dont detect the border
        if (p-> x > 3 && p->y > 3)
          polylines(image, &p, &n, 1, true, Scalar(0,255,0), 3, LINE_AA);
    }

    std::string savePath = "/storage/emulated/0/Pictures/";
    imwrite(savePath + "withSquare.jpg", image);
}

// 比较函数用于按照面积大小排序
bool compareAreas(pair<size_t, double> a, pair<size_t, double> b) {
    return a.second < b.second;
}

double calculateArea(const vector<Point>& square) {
    Rect roi = boundingRect(square);
    return roi.width * roi.height;
}

void Squares::filterSquares(vector<vector<Point>>& squares,vector<vector<Point>>& filteredSquares) {
    if (squares.empty()) return;
    unordered_map<size_t, double> areaMap;
    // 计算每个正方形的面积并与下标关联存储在 HashMap 中
    for (size_t i = 0; i < squares.size(); ++i) {
        double area = calculateArea(squares[i]);
        areaMap[i] = area;
    }

    // 将 HashMap 转换为 vector<pair<size_t, double>> 以便排序
    vector<pair<size_t, double>> sortedAreas(areaMap.begin(), areaMap.end());

    // 根据面积大小对 vector 进行排序
    sort(sortedAreas.begin(), sortedAreas.end(), compareAreas);

    for (int i = 0; i < sortedAreas.size(); i++) {
        Rect roi_i = boundingRect(squares[sortedAreas[i].first]);
        int j = 0;
        for (; j < filteredSquares.size(); j++) {
            Rect roi_j = boundingRect(filteredSquares[j]);
            if (Utils::bb_intersection_over_union(roi_j, roi_i) >= 0.5) {
                break;
            }
        }
        if (j >= filteredSquares.size()) {
            filteredSquares.push_back(squares[sortedAreas[i].first]);
        }
    }
}

void Squares::extractSquaresImages(const cv::Mat& image, const vector<vector<cv::Point>>& squares,
                                   vector<cv::Mat>& extractedImages, vector<vector<int>>& rects){

    rects.clear();
    for(const auto & square : squares)
    {
        // Calculate bounding rectangle of the square
        Rect roi = boundingRect(square);

        Mat subImage = image(roi).clone();
        // Store the extracted sub-image in the vector
        extractedImages.push_back(subImage);

        int x = static_cast<int>(roi.x);
        int y = static_cast<int>(roi.y);
        int width = static_cast<int>(roi.width);
        int height = static_cast<int>(roi.height);
        vector<int> tmp {x, y, width, height};

        rects.push_back(tmp);
    }
//    drawSquares(const_cast<Mat &>(image), squares);
}

void Squares::filterSquaresByMode(const Mat &image, vector<std::vector<cv::Point>> &squares,
                                  vector<std::vector<cv::Point>> &filteredSquares,
                                  FilterMode filterMode) {
    float max_width = Squares::MAX_WIDTH;
    float max_height = Squares::MAX_HEIGHT;
    float min_width  = Squares::MIN_WIDTH;
    float min_height = Squares::MIN_HEIGHT;
    float min_scale = Squares::MIN_SCALE;
    if (filterMode == SMALL){
        max_width = 0.5;
        max_height = 0.5;
        min_width = 0.1;
        min_height = 0.1;
        min_scale = 0.5;
    }else if (filterMode == BIG){
        max_width = 1.0;
        max_height = 0.8;
        min_height = 0.5;
        min_width = 0.5;
        min_scale = 0.3;
    }
    filteredSquares.clear();
    for(const auto & square : squares)
    {
        // Calculate bounding rectangle of the square
        Rect roi = boundingRect(square);

        if (min(roi.width, roi.height) < max(roi.width, roi.height) * min_scale) {
            continue;
        }

        if (roi.width >= image.cols * min_width && roi.width <= image.cols * max_width &&
            roi.height >= image.rows * min_height && roi.height <= image.rows * max_height){
            filteredSquares.push_back(square);
        }
    }
//    drawSquares(const_cast<Mat &>(image), squares);
}








