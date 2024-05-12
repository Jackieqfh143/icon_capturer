package com.lmy.iconcapturer.bean;

import com.elvishew.xlog.XLog;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FileItem {
    private String name;
    private String path;
    private FileType type;
    private List<String> ImageTypeFilter = Arrays.asList(".png", ".jpg", ".jpeg", ".gif", ".bmp");
    private List<String> VideoTypeFilter = Arrays.asList(".mp4", ".avi", ".rmvb");
    private List<String> TextTypeFilter = Arrays.asList(".txt", ".html", ".log", ".doc", ".docx", ".pdf", ".pptx");
    private List<String> CompressTypeFilter = Arrays.asList(".zip", ".rar", ".7z", ".gz");
    private String showImagePath = null;

    public String getShowImagePath() {
        return showImagePath;
    }

    public void setShowImagePath(String showImagePath) {
        this.showImagePath = showImagePath;
    }

    public enum FileType{
        IMAGE,VIDEO,TXT,COMPRESS, UNKNOWN
    }

    public FileItem(String path) {
        XLog.d("path is : " + path);
        this.path = path;
        this.name = parseName(path);
        this.type = parseType(path);
        if (this.type.equals(FileType.IMAGE) || this.type.equals(FileType.VIDEO)){
            this.showImagePath = path;
        }
    }

    public FileType parseType(String path){
        String suffixName = path.substring(path.lastIndexOf("."));
        suffixName = suffixName.toLowerCase(Locale.ENGLISH);
        XLog.d("suffixName is : " + suffixName);
        if (ImageTypeFilter.contains(suffixName)){
            return FileType.IMAGE;
        }else if (VideoTypeFilter.contains(suffixName)){
            return FileType.VIDEO;
        }else if(TextTypeFilter.contains(suffixName)){
            return FileType.TXT;
        }else if (CompressTypeFilter.contains(suffixName)){
            return FileType.COMPRESS;
        }
        return FileType.UNKNOWN;
    }

    public String parseName(String path){
        String Name = path.substring(path.lastIndexOf("/") + 1);
        Name = Name.substring(0, Name.lastIndexOf("."));
        XLog.d("Name is : " + Name);
        return Name;
    }

    @Override
    public String toString() {
        return "FileItem{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", type=" + type +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }
}
