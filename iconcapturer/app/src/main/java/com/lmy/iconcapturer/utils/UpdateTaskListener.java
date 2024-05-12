package com.lmy.iconcapturer.utils;

import java.util.List;

public interface UpdateTaskListener {
    void onProgress(int progress);

    void onSuccess(List<String> res);

    void onFailed();
}
