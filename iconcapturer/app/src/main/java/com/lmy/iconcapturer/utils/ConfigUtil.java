package com.lmy.iconcapturer.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.elvishew.xlog.XLog;

import java.util.Locale;

public class ConfigUtil {

    private static final String CONFIG_PATH = "config";

    public static float MAX_IMAGE_HEIGHT_RATIO = 0.5F;

    public static float MAX_IMAGE_WIDTH_RATIO = 0.5F;

    public static float MIN_IMAGE_HEIGHT_RATIO = 0.1F;

    public static float MIN_IMAGE_WIDTH_RATIO = 0.1F;

    public static float MIN_IMAGE_SCALE_RATIO = 0.5f;       //短边:长边 的最小比例

    public static int GIF_FRAME_RATE = 15;

    public static int IMAGE_QUALITY = 80;

    public static String SORT_TYPE = "save_time desc";

    public static boolean UPDATE_DONT_SHOW_AGAIN = false;

    public static boolean NOTIFY_DONT_SHOW_AGAIN = false;

    public static String VERSION_CODE = "1.0-beta";

    public static SharedPreferences preferences;

    public static SharedPreferences.Editor editor;

    public static CaptureType captureType = CaptureType.AUTO;

    public enum CaptureType{
        AUTO,SMALL,BIG,CUSTOM
    }

    public static String showAllConfig(){
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(Locale.ENGLISH, "MAX_IMAGE_HEIGHT_RATIO: %.1f \n", MAX_IMAGE_HEIGHT_RATIO));
        builder.append(String.format(Locale.ENGLISH, "MAX_IMAGE_WIDTH_RATIO: %.1f \n", MAX_IMAGE_WIDTH_RATIO));
        builder.append(String.format(Locale.ENGLISH, "MIN_IMAGE_HEIGHT_RATIO: %.1f \n", MIN_IMAGE_HEIGHT_RATIO));
        builder.append(String.format(Locale.ENGLISH, "MIN_IMAGE_WIDTH_RATIO: %.1f \n", MIN_IMAGE_WIDTH_RATIO));
        builder.append(String.format(Locale.ENGLISH, "MAX_IMAGE_SCALE_RATIO: %.1f \n", MIN_IMAGE_SCALE_RATIO));
        builder.append(String.format(Locale.ENGLISH, "GIF_FRAME_RATE: %d \n", GIF_FRAME_RATE));
        builder.append(String.format(Locale.ENGLISH, "SORT_TYPE: %s \n", SORT_TYPE));
        builder.append(String.format(Locale.ENGLISH, "DONT_SHOW_AGAIN: %s \n", UPDATE_DONT_SHOW_AGAIN));
        builder.append(String.format(Locale.ENGLISH, "VERSION_CODE: %s \n", VERSION_CODE));
        return builder.toString();
    }

    public static void initConfig(Context context){
        preferences = context.getSharedPreferences(CONFIG_PATH, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public static float getMaxImageHeightRatio() {
        return preferences.getFloat("MAX_IMAGE_HEIGHT_RATIO", MAX_IMAGE_HEIGHT_RATIO);
    }

    public static void setMaxImageHeightRatio(float maxImageHeightRatio) {
        MAX_IMAGE_HEIGHT_RATIO = maxImageHeightRatio;
        editor.putFloat("MAX_IMAGE_HEIGHT_RATIO", MAX_IMAGE_HEIGHT_RATIO);
        editor.commit();
        XLog.d(showAllConfig());
    }

    public static float getMaxImageWidthRatio() {
        return preferences.getFloat("MAX_IMAGE_WIDTH_RATIO", MAX_IMAGE_WIDTH_RATIO);
    }

    public static void setMaxImageWidthRatio(float maxImageWidthRatio) {
        MAX_IMAGE_WIDTH_RATIO = maxImageWidthRatio;
        editor.putFloat("MAX_IMAGE_WIDTH_RATIO", MAX_IMAGE_WIDTH_RATIO);
        editor.commit();
        XLog.d(showAllConfig());
    }

    public static float getMinImageScaleRatio() {
        return preferences.getFloat("MAX_IMAGE_SCALE_RATIO", MIN_IMAGE_SCALE_RATIO);
    }

    public static void setMinImageScaleRatio(float minImageScaleRatio) {
        MIN_IMAGE_SCALE_RATIO = minImageScaleRatio;
        editor.putFloat("MAX_IMAGE_SCALE_RATIO", MIN_IMAGE_SCALE_RATIO);
        editor.commit();
        XLog.d(showAllConfig());
    }

    public static int getGifFrameRate() {
        return preferences.getInt("GIF_FRAME_RATE", GIF_FRAME_RATE);
    }

    public static void setGifFrameRate(int gifFrameRate) {
        GIF_FRAME_RATE = gifFrameRate;
        editor.putInt("GIF_FRAME_RATE", GIF_FRAME_RATE);
        editor.commit();
        XLog.d(showAllConfig());
    }

    public static String getSortType() {
        return preferences.getString("SORT_TYPE", SORT_TYPE);
    }

    public static void setSortType(String sortType) {
        SORT_TYPE = sortType;
        editor.putString("SORT_TYPE", SORT_TYPE);
        editor.commit();
        XLog.d(showAllConfig());
    }

    public static boolean isUpdateDontShowAgain() {
        return preferences.getBoolean("UPDATE_DONT_SHOW_AGAIN", UPDATE_DONT_SHOW_AGAIN);
    }

    public static void setUpdateDontShowAgain(boolean updateDontShowAgain) {
        UPDATE_DONT_SHOW_AGAIN = updateDontShowAgain;
        editor.putBoolean("UPDATE_DONT_SHOW_AGAIN", UPDATE_DONT_SHOW_AGAIN);
        editor.commit();
        XLog.d(showAllConfig());
    }

    public static String getVersionCode() {
        return preferences.getString("VERSION_CODE", VERSION_CODE);
    }

    public static void setVersionCode(String versionCode) {
        VERSION_CODE = versionCode;
        editor.putString("VERSION_CODE", VERSION_CODE);
        editor.commit();
        XLog.d(showAllConfig());
    }

    public static CaptureType getCaptureType() {
        return CaptureType.values()[preferences.getInt("captureType", ConfigUtil.captureType.ordinal())];
    }

    public static void setCaptureType(CaptureType captureType) {
        ConfigUtil.captureType = captureType;
        editor.putInt("captureType", captureType.ordinal());
        editor.commit();
        XLog.d(showAllConfig());
    }

    public static int getImageQuality() {
        return preferences.getInt("IMAGE_QUALITY", IMAGE_QUALITY);
    }

    public static void setImageQuality(int imageQuality) {
        IMAGE_QUALITY = imageQuality;
        editor.putInt("IMAGE_QUALITY", IMAGE_QUALITY);
        editor.commit();
        XLog.d(showAllConfig());
    }

    public static float getMinImageHeightRatio() {
        return preferences.getFloat("MIN_IMAGE_HEIGHT_RATIO", MIN_IMAGE_HEIGHT_RATIO);
    }

    public static void setMinImageHeightRatio(float minImageHeightRatio) {
        MIN_IMAGE_HEIGHT_RATIO = minImageHeightRatio;
        editor.putFloat("MIN_IMAGE_HEIGHT_RATIO", MIN_IMAGE_HEIGHT_RATIO);
        editor.commit();
        XLog.d(showAllConfig());
    }

    public static float getMinImageWidthRatio() {
        return preferences.getFloat("MIN_IMAGE_WIDTH_RATIO", MIN_IMAGE_WIDTH_RATIO);
    }

    public static void setMinImageWidthRatio(float minImageWidthRatio) {
        MIN_IMAGE_WIDTH_RATIO = minImageWidthRatio;
        editor.putFloat("MIN_IMAGE_WIDTH_RATIO", MIN_IMAGE_WIDTH_RATIO);
        editor.commit();
        XLog.d(showAllConfig());
    }

    public static void restoreDefaultSetting(){
        captureType = CaptureType.AUTO;
        GIF_FRAME_RATE = 15;
        IMAGE_QUALITY = 80;
        SORT_TYPE = "save_time desc";
        setCaptureType(captureType);
        setGifFrameRate(GIF_FRAME_RATE);
        setImageQuality(IMAGE_QUALITY);
        setSortType(SORT_TYPE);
    }

    public static boolean isNotifyDontShowAgain() {
        return preferences.getBoolean("NOTIFY_DONT_SHOW_AGAIN", NOTIFY_DONT_SHOW_AGAIN);
    }

    public static void setNotifyDontShowAgain(boolean notifyDontShowAgain) {
        NOTIFY_DONT_SHOW_AGAIN = notifyDontShowAgain;
        editor.putBoolean("NOTIFY_DONT_SHOW_AGAIN", NOTIFY_DONT_SHOW_AGAIN);
        editor.commit();
        XLog.d(showAllConfig());
    }
}
