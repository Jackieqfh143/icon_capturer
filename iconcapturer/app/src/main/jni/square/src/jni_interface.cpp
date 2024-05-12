#include <jni.h>
#include <android/log.h>
#include "../include/squares.h"
#include "../include/utils.h"

extern "C" JNIEXPORT jobjectArray Java_com_lmy_iconcapturer_utils_IconDetecter_findIconStandard(JNIEnv *env, jobject thiz,
                             jbyteArray imgData, jint img_width, jint img_height, jint img_channels) {
    // TODO: implement findIconStandard()

    // java jbyteArray to unsign char*
    auto* img_data = reinterpret_cast<uchar *>(env->GetByteArrayElements(imgData, 0));
    env->ReleaseByteArrayElements(imgData, reinterpret_cast<jbyte *>(img_data), JNI_COMMIT);

    //process the image
    cv::Mat img = Utils::makeMat(img_data,img_height, img_width, img_channels);
    cv::Mat img_copy = img.clone();
    std::vector<std::vector<cv::Point> > squares;
    std::vector<cv::Mat> croppedImgs;
    Squares::findSquares(img_copy, squares);
//    Squares::drawSquares(img, squares);

//    __android_log_print(ANDROID_LOG_DEBUG, "qfh", "squares size: %lu", squares.size());
    std::vector<std::vector<cv::Point> > filteredsquares;
    std::vector<std::vector<cv::Point> > filteredModeSquares;
    Squares::filterSquares(squares, filteredsquares);

    if (Squares::FILTER_MODE == AUTO){
        Squares::filterSquaresByMode(img_copy, filteredsquares, filteredModeSquares, SMALL);
        if (filteredModeSquares.empty()){
            Squares::filterSquaresByMode(img_copy, filteredsquares, filteredModeSquares, BIG);
        }
        if (filteredModeSquares.empty()){
            Squares::MAX_WIDTH = 1.0;
            for (float s = 0.5; s >= 0.3; s -= 0.1){
                for (float h = 0.5; h < 0.9; h += 0.1){
                    Squares::MAX_HEIGHT = h;
                    Squares::MIN_SCALE = s;
                    Squares::filterSquaresByMode(img_copy, filteredsquares, filteredModeSquares, CUSTOM);
                    if (!filteredModeSquares.empty()) break;
                }
                if (!filteredModeSquares.empty()) break;
            }
        }
        
    }else{
        Squares::filterSquaresByMode(img_copy, filteredsquares, filteredModeSquares,
                                     static_cast<FilterMode>(Squares::FILTER_MODE));
    }

    std::vector<std::vector<int>> rects;
//    __android_log_print(ANDROID_LOG_DEBUG, "qfh", "filteredsquares size: %lu", filteredsquares.size());

    Squares::extractSquaresImages(img,filteredModeSquares,croppedImgs, rects);
    int res_num = croppedImgs.size();
//    __android_log_print(ANDROID_LOG_DEBUG, "qfh", "croppedImgs.size(): %lu", croppedImgs.size());
//    __android_log_print(ANDROID_LOG_DEBUG, "qfh", "rects.size(): %lu", rects.size());
//    __android_log_print(ANDROID_LOG_DEBUG, "qfh", "the &rects[0] is: %p", &rects[0]);

    //获取PlainImageBuffer类的引用
    jclass cls = env->FindClass("com/lmy/iconcapturer/bean/PlainImageBuffer");
    //获取 PlainImageBuffer 类的构造方法 ID
    jmethodID constructor = env->GetMethodID(cls, "<init>", "([BII[I)V");
    jobjectArray resArray = env->NewObjectArray(res_num, cls, nullptr);
    for (int i = 0; i < res_num; i++){
        auto* tmp_img_data = Utils::getMatData(croppedImgs[i]);
        jsize tmp_img_width = croppedImgs[i].cols;
        jsize tmp_img_height = croppedImgs[i].rows;
        jsize tmp_data_len = tmp_img_width * tmp_img_height * img_channels;

        // unsign char* to jbyteArray
        jbyteArray byteArray = env->NewByteArray(tmp_data_len);
        env->SetByteArrayRegion(byteArray, 0, tmp_data_len, (jbyte*)tmp_img_data);

        jintArray jintArray1 = env->NewIntArray(4);

//        __android_log_print(ANDROID_LOG_DEBUG, "qfh", "i: %d", i);
//        __android_log_print(ANDROID_LOG_DEBUG, "qfh", "rects[i][0]: %d", rects[i][0]);
//        __android_log_print(ANDROID_LOG_DEBUG, "qfh", "rects[i][1]: %d", rects[i][1]);
//        __android_log_print(ANDROID_LOG_DEBUG, "qfh", "rects[i][2]: %d", rects[i][2]);
//        __android_log_print(ANDROID_LOG_DEBUG, "qfh", "rects[i][3]: %d", rects[i][3]);
        env->SetIntArrayRegion(jintArray1, 0, 4, (int*)rects[i].data());

        // 调用构造方法创建 PlainImageBuffer 对象
        jobject plainImageBuffer = env->NewObject(cls, constructor, byteArray, tmp_img_width, tmp_img_height, jintArray1);

        // 将 plainImageBuffer 存储到 jobjectArray 中
        env->SetObjectArrayElement(resArray, i, plainImageBuffer);

        // 释放临时变量的内存
        env->DeleteLocalRef(byteArray);
        env->DeleteLocalRef(jintArray1);
    }

    return resArray;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lmy_iconcapturer_utils_IconDetecter_initConfig(JNIEnv *env, jobject thiz,
                                                        jfloatArray configs) {
    jfloat* config_data = env->GetFloatArrayElements(configs, 0);
    Squares::MAX_HEIGHT = config_data[0];
    Squares::MAX_WIDTH = config_data[1];
    Squares::MIN_HEIGHT = config_data[2];
    Squares::MIN_WIDTH = config_data[3];
    Squares::MIN_SCALE = config_data[4];
    Squares::FILTER_MODE = static_cast<int>(config_data[5]);

    env->ReleaseFloatArrayElements(configs, reinterpret_cast<jfloat *>(config_data), JNI_COMMIT);
    delete config_data;
}