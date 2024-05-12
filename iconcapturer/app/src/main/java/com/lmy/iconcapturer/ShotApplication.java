package com.lmy.iconcapturer;

import android.app.Application;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;

import org.litepal.LitePal;


public class ShotApplication extends Application {
    private int result;
    private Intent intent;
    private MediaProjectionManager mMediaProjectionManager;

    private boolean isFloatWindowShow = false;

    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
    }
    public int getResult(){
        return result;
    }

    public Intent getIntent(){
        return intent;
    }

    public MediaProjectionManager getMediaProjectionManager(){
        return mMediaProjectionManager;
    }

    public void setResult(int result1){
        this.result = result1;
    }

    public void setIntent(Intent intent1){
        this.intent = intent1;
    }

    public void setMediaProjectionManager(MediaProjectionManager mMediaProjectionManager){
        this.mMediaProjectionManager = mMediaProjectionManager;
    }

    public boolean isFloatWindowShow() {
        return isFloatWindowShow;
    }

    public void setFloatWindowShow(boolean floatWindowShow) {
        isFloatWindowShow = floatWindowShow;
    }
}
