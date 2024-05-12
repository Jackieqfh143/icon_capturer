package com.lmy.iconcapturer.service;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.Observer;

import com.elvishew.xlog.XLog;
import com.lmy.iconcapturer.R;
import com.lmy.iconcapturer.ShotApplication;
import com.lmy.iconcapturer.utils.ItemViewTouchListener;
import com.lmy.iconcapturer.utils.Utils;
import com.lmy.iconcapturer.utils.ViewModelMain;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class SuspendwindowService extends LifecycleService {
    private WindowManager windowManager;
    private View floatRootView; // Floating window view
    private static final String TAG = "qfh";
    private CaptureScreenService.IconCaptureBinder iconCaptureBinder;
    private BroadcastReceiver broadcastReceiver;
    private List<Integer> statusList = new ArrayList<>();
//    private View toastView;
//    private TextView textView;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            iconCaptureBinder = (CaptureScreenService.IconCaptureBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(getBaseContext(), CaptureScreenService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.lmy.iconcapturer.TASK_COMPLETED".equals(intent.getAction())) {
                    XLog.i("receive broadcast TASK_COMPLETED");
                    XLog.i( "statusList.size(): " + statusList.size());
                    int statusCode = intent.getIntExtra("statusCode", 0);
                    int total = intent.getIntExtra("total", 0);
                    int success = intent.getIntExtra("success", 0);
                    if (statusList.size() == 0){
                        statusList.add(statusCode);
                    }else{
                        return;
                    }
                    Handler handler = new Handler();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onTaskComplete(statusList.get(0), total, success);
                        }
                    });

                    floatRootView.setVisibility(View.VISIBLE);
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            toastView.setVisibility(View.GONE);
//
//                        }
//                    }, 2500);
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("com.lmy.iconcapturer.TASK_COMPLETED"));

        initObserve();

    }

    private void initObserve() {
        ViewModelMain.isVisible.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean it) {
                if (floatRootView != null) {
                    if (it) {
                        XLog.i( "floatRootView set visible = true");
                    }else{
                        XLog.i( "floatRootView set view gone");
                    }
                    floatRootView.setVisibility(it ? View.VISIBLE : View.GONE);
//                    toastView.setVisibility(View.GONE);
                }
            }
        });

        ViewModelMain.isShowSuspendWindow.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean it) {
                if (it) {
                    XLog.i( "showWindow!");
                    showWindow();
//                    showToastWindow();
                } else {
                    XLog.i( "closeFloatWindow!");
                    closeFloatWindow();
                }
                ((ShotApplication) getApplication()).setFloatWindowShow(it);
            }
        });
    }

    /**
     * 关闭悬浮窗口
     */
    private void closeFloatWindow() {
        if (floatRootView != null && floatRootView.getWindowToken() != null && windowManager != null) {
            windowManager.removeView(floatRootView);
        }
    }

    private void showText(String text){
        XLog.d( text);
        Utils.showText(this, text);
    }

    private void onTaskComplete(int statusCode, int total, int success){
        XLog.i("task finished in " + statusCode);
        if (statusCode == 0){
            showText("未检测到有效表情包，请重试");
        }else if (statusCode == 1){
            String text = String.format(Locale.CHINESE, "捕获到 %d 个表情包，成功 %d，失败 %d",total, success, total - success);
            showText(text);
        }
        else{
            showText("录屏开启失败，请重试");
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void showWindow() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);

        LayoutParams layoutParam = new LayoutParams();
        layoutParam.type = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
                LayoutParams.TYPE_APPLICATION_OVERLAY : LayoutParams.TYPE_PHONE;
        layoutParam.format = PixelFormat.RGBA_8888;
        layoutParam.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParam.width = LayoutParams.WRAP_CONTENT;
        layoutParam.height = LayoutParams.WRAP_CONTENT;
        layoutParam.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParam.x = outMetrics.widthPixels - layoutParam.width / 2;
        layoutParam.y = 4 * outMetrics.heightPixels / 5 - layoutParam.height / 2;


        XLog.d( "layoutParam.width: " + layoutParam.width);
        XLog.d( "layoutParam.height: " + layoutParam.height);
        XLog.d( "outMetrics.heightPixels: " + outMetrics.heightPixels);
        XLog.d( "outMetrics.widthPixels: " + outMetrics.widthPixels);

        floatRootView = LayoutInflater.from(this).inflate(R.layout.activity_float_item, null);
        floatRootView.setOnTouchListener(new ItemViewTouchListener(layoutParam, windowManager));
        floatRootView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                XLog.i( "Click the record button!");
                // hide the button
                floatRootView.setVisibility(View.INVISIBLE);
                Handler handler1 = new Handler();
                statusList.clear();
                handler1.post(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.Q)
                    public void run() {
                        iconCaptureBinder.startCapture();
                    }
                });
                return true;
            }
        });

        windowManager.addView(floatRootView, layoutParam);
    }

    private void showToastWindow(){
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
                LayoutParams.TYPE_APPLICATION_OVERLAY : LayoutParams.TYPE_PHONE;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.format = PixelFormat.TRANSLUCENT;
        params.windowAnimations = android.R.style.Animation_Toast;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = outMetrics.widthPixels / 2 - params.width / 2;
        params.y = 4 * outMetrics.heightPixels / 5 - params.height / 2;

//        toastView = LayoutInflater.from(this).inflate(R.layout.toast_view,
//                null);
//        textView = (TextView) toastView.findViewById(R.id.toast_text);
//
//        windowManager.addView(toastView, params);
    }

    private void stopIconCaptureService(){
        Intent intent = new Intent(this, CaptureScreenService.class);
        stopService(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        unbindService(connection);
        stopIconCaptureService();
    }
}
