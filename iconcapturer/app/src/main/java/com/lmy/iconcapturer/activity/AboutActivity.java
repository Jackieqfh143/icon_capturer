package com.lmy.iconcapturer.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.elvishew.xlog.XLog;
import com.lmy.iconcapturer.R;
import com.lmy.iconcapturer.fragment.HelpFragment;
import com.lmy.iconcapturer.fragment.InfoFragment;
import com.lmy.iconcapturer.fragment.UpdateDialogFragment;
import com.lmy.iconcapturer.service.DownloadBinder;
import com.lmy.iconcapturer.service.DownloadService;
import com.lmy.iconcapturer.utils.UpdateTask;
import com.lmy.iconcapturer.utils.UpdateTaskListener;
import com.lmy.iconcapturer.utils.Utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

public class AboutActivity extends AppCompatActivity {

    private String versionName;

    private BroadcastReceiver broadcastReceiver;

    private String latestVersion;

    private String apkUrl;

    private DownloadBinder downloadBinder;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            XLog.d("onServiceConnected");
            downloadBinder = (DownloadBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        versionName = Utils.getCurrentVersion(this);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView versionTextView = (TextView) findViewById(R.id.about_version_text);
        LinearLayout infoBtn = (LinearLayout) findViewById(R.id.info_btn_layout);
        LinearLayout helpBtn = (LinearLayout) findViewById(R.id.help_btn_layout);
        LinearLayout updateBtn = (LinearLayout) findViewById(R.id.update_btn_layout);

        versionTextView.setText("Version " + versionName);

        infoBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                ImageView imageView = (ImageView) view.findViewById(R.id.info_btn_imageView);
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        imageView.setImageDrawable(getDrawable(R.drawable.info_filled));
                    case MotionEvent.ACTION_UP:
                        imageView.setImageDrawable(getDrawable(R.drawable.info));
                }
                return false;
            }
        });

        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onInfoClicked();
            }
        });

        helpBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                ImageView imageView = (ImageView) view.findViewById(R.id.help_btn_image_view);
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        imageView.setImageDrawable(getDrawable(R.drawable.book_filled));
                    case MotionEvent.ACTION_UP:
                        imageView.setImageDrawable(getDrawable(R.drawable.book));

                }
                return false;
            }
        });

        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onHelpClicked();
            }
        });

        updateBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                ImageView imageView = (ImageView) view.findViewById(R.id.update_btn_imageview);
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        imageView.setImageDrawable(getDrawable(R.drawable.cloud_filled));
                    case MotionEvent.ACTION_UP:
                        imageView.setImageDrawable(getDrawable(R.drawable.cloud));
                }
                return false;
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUpdateClicked();
            }
        });

        bindService(new Intent(getBaseContext(), DownloadService.class), connection, BIND_AUTO_CREATE);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                XLog.e("about activity receive broadcast of " + intent.getAction());
                if ("com.lmy.iconcapturer.CONFIRM_UPDATE".equals(intent.getAction())){
                    boolean confirmUpdate = intent.getBooleanExtra("confirmUpdate", false);
                    if (confirmUpdate){
                        downloadBinder.startDownload(latestVersion, apkUrl);
                    }
                    Fragment prev = getSupportFragmentManager().findFragmentByTag("updateDialog");
                    if (prev != null) {
                        DialogFragment df = (DialogFragment) prev;
                        df.dismiss();
                    }
                    abortBroadcast();
                }else if ("com.lmy.iconcapturer.APK_READY".equals(intent.getAction())){
                    Utils.showText(AboutActivity.this, "软件更新包下载完成");
                    finish();
                }
            }
        };

        IntentFilter filters = new IntentFilter();
        filters.setPriority(100);
        filters.addAction("com.lmy.iconcapturer.CONFIRM_UPDATE");
        filters.addAction("com.lmy.iconcapturer.APK_READY");
        registerReceiver(broadcastReceiver, filters);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    private void onHelpClicked(){
        HelpFragment newFragment = HelpFragment.newInstance();
        newFragment.show(getSupportFragmentManager(), "helpDialog");
    }

    private void onInfoClicked(){
        InputStream is = getResources().openRawResource(R.raw.update_log);

        StringBuilder builder = new StringBuilder();
        try{
            InputStreamReader inputStreamReader = new InputStreamReader(is, "utf-8");
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = reader.readLine()) != null){
                builder.append(line);
            }
            reader.close();
            inputStreamReader.close();
            is.close();
            InfoFragment newFragment = InfoFragment.newInstance(builder.toString());
            newFragment.show(getSupportFragmentManager(), "infoDialog");
        }catch (Exception e){
            e.printStackTrace();
            Utils.showText(this, "读取更新日志失败");
        }
    }

    private void checkForUpdate(){
        //检查版本更新
        UpdateTask updateTask = new UpdateTask(new UpdateTaskListener() {
            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onSuccess(List<String> res) {
                if (res != null && res.size() > 1){
                    latestVersion = res.get(0);
                    apkUrl = res.get(1);
                    String updateLog = res.get(2);
                    if (!versionName.equals(latestVersion)){
                        DialogFragment newFragment = UpdateDialogFragment.newInstance(latestVersion, "软件更新提醒", String.format(Locale.CHINESE, "检测到软件有新版本%s（当前版本为: %s）是否更新？",
                                latestVersion, versionName), updateLog, false);
                        newFragment.show(getSupportFragmentManager(), "updateDialog");
                    }
                    else{
                        Utils.showText(AboutActivity.this, "已是最新版本");
                    }
                }
            }

            @Override
            public void onFailed() {

            }
        });
        updateTask.execute(AboutActivity.this);
    }

    private void onUpdateClicked(){
        Utils.showText(this, "正在检查请稍等");
        checkForUpdate();
    }


    @Override
    protected void onDestroy() {
        unbindService(connection);
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}
