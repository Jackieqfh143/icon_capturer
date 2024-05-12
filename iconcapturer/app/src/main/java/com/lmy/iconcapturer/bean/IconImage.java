package com.lmy.iconcapturer.bean;

import org.litepal.crud.LitePalSupport;

public class IconImage extends LitePalSupport {
    private int id;
    private int width;
    private int height;
    private String uuid;
    private boolean isLike;
    private boolean isGif;
    private String name;
    private String path;
    private long save_time;

    public boolean isGif() {
        return isGif;
    }

    public void setGif(boolean gif) {
        isGif = gif;
    }

    public boolean isLike() {
        return isLike;
    }

    public void setLike(boolean like) {
        isLike = like;
    }

    public String getUuid() {
        return uuid;
    }

    public long getSave_time() {
        return save_time;
    }

    public void setSave_time(long save_time) {
        this.save_time = save_time;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public IconImage() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
