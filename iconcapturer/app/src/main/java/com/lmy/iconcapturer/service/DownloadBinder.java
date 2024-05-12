package com.lmy.iconcapturer.service;
import android.app.Service;
import android.os.Binder;

public class DownloadBinder extends Binder {
    private final DownloadService downloadService;

    public DownloadBinder(Service downloadService){
        this.downloadService = (DownloadService) downloadService;
    }

    public void startDownload(String latestVersion, String apkUrl){
        downloadService.startDownload(latestVersion, apkUrl);
    }
}
