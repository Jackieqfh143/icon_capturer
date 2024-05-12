package com.lmy.iconcapturer.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.viewpager2.widget.ViewPager2;

import com.flyjingfish.openimagelib.OpenImageActivity;
import com.flyjingfish.openimagelib.widget.TouchCloseLayout;
import com.lmy.iconcapturer.R;

public class BigImageActivity extends OpenImageActivity {

    private TouchCloseLayout rootView;
    private View bgView;
    private FrameLayout viewPager2Container;
    private ViewPager2 viewPager2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_image_activity_viewpager);

        rootView = findViewById(R.id.root_layout);
        bgView = findViewById(R.id.v_bg);
        viewPager2Container = findViewById(R.id.fl_touch_view);
        viewPager2 = findViewById(R.id.viewPager);
    }

    @Override
    public View getContentView() {
        return rootView;
    }

    @Override
    public View getBgView() {
        return bgView;
    }

    @Override
    public FrameLayout getViewPager2Container() {
        return viewPager2Container;
    }

    @Override
    public ViewPager2 getViewPager2() {
        return viewPager2;
    }

    @Override
    public TouchCloseLayout getTouchCloseLayout() {
        return rootView;
    }
}
