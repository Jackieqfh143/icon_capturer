package com.lmy.iconcapturer.bean;

public class PlainImageBuffer {
    public int width;
    public int height;
    public byte[] imgData;
    public int[] rect;

    public PlainImageBuffer(byte[] imgData, int width, int height, int[] rect){
        this.imgData = imgData;
        this.width = width;
        this.height = height;
        this.rect = rect;
    }
}
