package com.lmy.iconcapturer.utils;

import java.io.File;

public interface DownloadTaskListener {
    void onProgress(int progress);

    void onSuccess(File file);

    void onFailed();
}
