package com.lmy.iconcapturer.utils;

import com.lmy.iconcapturer.bean.PlainImageBuffer;

public class IconDetecter {

    public native void initConfig(float[] configs);

    public PlainImageBuffer[] getIconStandard(byte[] imgData, int imgWidth, int imgHeight, int imgChannels){
        return findIconStandard(imgData, imgWidth, imgHeight, imgChannels);
    }
    public native PlainImageBuffer[] findIconStandard(byte[] imgData, int imgWidth, int imgHeight, int imgChannels);

    static {
        System.loadLibrary("findSquare");
    }
}
