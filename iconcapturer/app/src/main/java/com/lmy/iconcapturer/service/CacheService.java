package com.lmy.iconcapturer.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.annotation.Nullable;

import com.elvishew.xlog.XLog;
import com.lmy.iconcapturer.utils.Utils;

public class CacheService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        XLog.e("start cache clean service");
        new Thread(new Runnable() {
            @Override
            public void run() {
                //清除缓存，减少内存占用
                Utils.deleteCache(CacheService.this);
            }
        }).start();

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long fourtyEightHours = 60 * 60 * 1000 * 24;
        long triggerAtTime = SystemClock.elapsedRealtime() + fourtyEightHours;
        Intent intent1 = new Intent(this, CacheService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent1, 0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);

        return super.onStartCommand(intent, flags, startId);
    }
}
