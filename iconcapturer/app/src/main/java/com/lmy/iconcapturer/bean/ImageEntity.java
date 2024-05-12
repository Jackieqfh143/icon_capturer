package com.lmy.iconcapturer.bean;

import com.flyjingfish.openimagelib.beans.OpenImageUrl;
import com.flyjingfish.openimagelib.enums.MediaType;

public class ImageEntity implements OpenImageUrl {
    public String photoUrl;//图片大图
    public String smallPhotoUrl;//图片小图
    public String coverUrl;//视频封面大图
    public String smallCoverUrl;//视频封面小图
    public String videoUrl;//视频链接
    public int resouceType; //0图片1视频

    public ImageEntity(String photoUrl, String smallPhotoUrl, int resouceType) {
        this.photoUrl = photoUrl;
        this.smallPhotoUrl = smallPhotoUrl;
        this.resouceType = resouceType;
    }

    @Override
    public String getImageUrl() {
        return resouceType == 1 ? coverUrl : photoUrl;//大图链接（或视频的封面大图链接）
    }

    @Override
    public String getVideoUrl() {
        return videoUrl;//视频链接
    }

    @Override
    public String getCoverImageUrl() {//这个代表前边列表展示的图片（即缩略图）
        return resouceType == 1 ? smallCoverUrl : smallPhotoUrl;//封面小图链接（或视频的封面小图链接）
    }

    @Override
    public MediaType getType() {
        return resouceType == 1 ? MediaType.VIDEO : MediaType.IMAGE;//数据是图片还是视频
    }
}
