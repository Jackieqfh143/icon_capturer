package com.lmy.iconcapturer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.lmy.iconcapturer.R;

/**
 * 吐司提示的工具类，能够控制吐司的显示和隐藏
 */
public class ToastUtil {
    public static final int LENGTH_SHORT = 0;
    public static final int LENGTH_LONG = 1;
    private static View toastView;
    private WindowManager mWindowManager;
    private static int mDuration;
    private final int WHAT = 100;
    private static View oldView;
    private static Toast toast;
    private static CharSequence oldText;
    private static CharSequence currentText;
    private static ToastUtil instance = null;
    private static TextView textView;
    private static final String TAG = "qfh";

    private ToastUtil(Context context) {
        mWindowManager = (WindowManager) context.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        toastView = LayoutInflater.from(context).inflate(R.layout.toast_view,
                null);
        textView = (TextView) toastView.findViewById(R.id.toast_text);
        toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
    }
    private static ToastUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (ToastUtil.class) {
                if (instance == null)
                    instance = new ToastUtil(context);
            }
        }
        return instance;
    }
    public static ToastUtil makeText(Context context, CharSequence text,
                                     int duration) {
        ToastUtil util = getInstance(context);
        mDuration = duration;
        toast.setText(text);
        currentText = text;
        textView.setText(text);
        return util;
    }
    public static ToastUtil makeText(Context context, int resId, int duration){
        ToastUtil util = getInstance(context);
        mDuration = duration;
        toast.setText(resId);
        currentText = context.getResources().getString(resId);
        textView.setText(context.getResources().getString(resId));
        return util;
    }
    /**
     * 进行Toast显示，在显示之前会取消当前已经存在的Toast
     */
    public void show() {
        long time = 0;
        switch (mDuration) {
            case LENGTH_SHORT:
                time = 2000;
                break;
            case LENGTH_LONG:
                time = 3500;
                break;
            default:
                time = 2000;
                break;
        }
        if (currentText.equals(oldText) && oldView.getParent() != null) {
            toastHandler.removeMessages(WHAT);
            toastView = oldView;
            oldText = currentText;
            toastHandler.sendEmptyMessageDelayed(WHAT, time);
            return;
        }
        cancelOldAlert();
        toastHandler.removeMessages(WHAT);
        DisplayMetrics outMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(outMetrics);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.format = PixelFormat.TRANSLUCENT;
        params.windowAnimations = android.R.style.Animation_Toast;
        params.type = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        params.setTitle("Toast");

        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
//        params.x = (windowWidth - toastWidth) / 2;
        params.y = 6 * outMetrics.heightPixels / 7 - params.height / 2;

        if (toastView.getParent() == null) {
            mWindowManager.addView(toastView, params);
        }
        oldView = toastView;
        oldText = currentText;
        toastHandler.sendEmptyMessageDelayed(WHAT, time);
    }
    @SuppressLint("HandlerLeak")
    private Handler toastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            cancelOldAlert();
            int id = msg.what;
            if (WHAT == id) {
                cancelCurrentAlert();
            }
        }
    };
    private void cancelOldAlert() {
        if (oldView != null && oldView.getParent() != null) {
            mWindowManager.removeView(oldView);
        }
    }
    public void cancelCurrentAlert() {
        if (toastView != null && toastView.getParent() != null) {
            mWindowManager.removeView(toastView);
        }
    }
}