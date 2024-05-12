package com.lmy.iconcapturer.utils;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ViewModelMain extends ViewModel {
    //悬浮窗口创建 移除
    public static MutableLiveData<Boolean> isShowSuspendWindow = new MutableLiveData<>();

    //悬浮窗口显示 隐藏
    public static MutableLiveData<Boolean> isVisible = new MutableLiveData<>();

    public static MutableLiveData<Integer> totalIcon = new MutableLiveData<>();

    public static MutableLiveData<Integer> successIcon = new MutableLiveData<>();
}
