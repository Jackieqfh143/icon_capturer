package com.lmy.iconcapturer.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elvishew.xlog.XLog;
import com.lmy.iconcapturer.R;
import com.lmy.iconcapturer.bean.FileItem;
import com.lmy.iconcapturer.cos_utils.CosUtils;
import com.lmy.iconcapturer.utils.DeviceInfoUtil;
import com.lmy.iconcapturer.utils.FileAdapter;
import com.lmy.iconcapturer.utils.FileUtil;
import com.lmy.iconcapturer.utils.Utils;

import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


public class LogReportActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private EditText titleText;

    private EditText contentText;

    private FileAdapter fileAdapter;

    private List<FileItem> fileItemList;

    private LinearLayout progressView;

    private ProgressBar progressBar;

    private TextView progressText;

    private final static int FILE_REQUEST_CODE = 1001;

    private BroadcastReceiver broadcastReceiver;

    private File zipFile;

    private Thread uploadThread = null;

    private boolean isCanceled;

    enum UploadStatus{
        SUCCESS,FAILED,CANCEL
    }

    @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_report);

        fileItemList = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = (RecyclerView) findViewById(R.id.log_recycle_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        fileAdapter = new FileAdapter(fileItemList);
        recyclerView.setAdapter(fileAdapter);

        titleText = (EditText) findViewById(R.id.title_textview);
        contentText = (EditText) findViewById(R.id.content_textview);
        progressView = findViewById(R.id.upload_progress_view);
        progressBar = findViewById(R.id.upload_progress_bar);
        progressText = findViewById(R.id.progress_text);

//


        ImageButton addFileBtn = (ImageButton) findViewById(R.id.add_file_btn);
        addFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileManager();
            }
        });

        ImageButton cancelBtn  = (ImageButton) findViewById(R.id.upload_cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCanceled = true;
                cancelUpload();
            }
        });

        LinearLayout sendBtn = (LinearLayout) findViewById(R.id.log_send_btn_layout);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                XLog.i("点击发送按钮");
                startUpload();
            }
        });

        sendBtn.setOnTouchListener((view, motionEvent) -> {
            ImageButton imageButton = (ImageButton) sendBtn.findViewById(R.id.send_image_btn);
            switch(motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // PRESSED
                    imageButton.setImageDrawable(getDrawable(R.drawable.send_filled));
                    break;
                case MotionEvent.ACTION_UP:
                    // RELEASED
                    imageButton.setImageDrawable(getDrawable(R.drawable.send));
                    break;
            }
            return false;
        });


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.lmy.iconcapturer.UPLOAD_PROGRESS".equals(intent.getAction())) {
                    int progress = intent.getIntExtra("progress", 0);
                    if (progress >= 0 ){
                        onProgressUpdate(progress);
                    }
                }else if ("com.lmy.iconcapturer.FILE_DEL".equals(intent.getAction())){
                    XLog.i("del file");
                    int position = intent.getIntExtra("position", -1);
                    if (position != -1){
                        onFileRemove(position);
                    }
                }else if ("com.lmy.iconcapturer.UPLOAD_SUCCESS".equals(intent.getAction())){
                    onTaskComplete(UploadStatus.SUCCESS, "");
                }
                else if ("com.lmy.iconcapturer.UPLOAD_FAILED".equals(intent.getAction())){
                    if (isCanceled){
                        onTaskComplete(UploadStatus.CANCEL, "");
                    }else{
                        String msg = intent.getStringExtra("msg");
                        onTaskComplete(UploadStatus.FAILED, msg);
                    }
                }
            }
        };
        IntentFilter filters = new IntentFilter();
        filters.addAction("com.lmy.iconcapturer.UPLOAD_PROGRESS");
        filters.addAction("com.lmy.iconcapturer.UPLOAD_SUCCESS");
        filters.addAction("com.lmy.iconcapturer.UPLOAD_FAILED");
        filters.addAction("com.lmy.iconcapturer.FILE_DEL");
        registerReceiver(broadcastReceiver, filters);
    }

    private void openFileManager() {
        if (fileItemList.size() >= 5){
            Utils.showText(this, "最多只能添加5个附件");
            return;
        }
        // 打开文件管理器选择文件
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//无类型限制
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, FILE_REQUEST_CODE);
    }


    private File makeUserResponseFile() {
        File folder = new File(getFilesDir().getPath() + File.separator + "response");
        if (!folder.exists()){
            folder.mkdirs();
        }
        File file = new File(folder, Utils.getRandomName() + "_user_response.txt");
        StringBuilder content = new StringBuilder();
        content.append(String.format(Locale.CHINESE, "标题: %s\n", titleText.getText()));
        content.append(String.format(Locale.CHINESE, "内容: %s\n", contentText.getText()));
        try {
            // 创建 FileWriter 对象
            FileWriter writer = new FileWriter(file);

            // 写入内容到文件
            writer.write(content.toString());

            // 关闭写入流
            writer.close();

            XLog.d("用户反馈文件创建成功");

        } catch (IOException e) {
            XLog.e("用户反馈文件创建失败 \n" + e.getMessage());
            onTaskComplete(UploadStatus.FAILED, "用户反馈文件创建失败");
            return null;
        }

        return file;
    }


    private File getLogFile() {
        String logPath = getFilesDir().getPath();
        File folder = new File(logPath + File.separator + "log");
        File[] log_files = folder.listFiles();
        if (log_files.length == 0) {
//            Utils.showText(this, "没有日志可以分享");
            onTaskComplete(UploadStatus.FAILED, "没有日志可以分享");
            return null;
        }
        List<File> fileList = Arrays.asList(log_files);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            }
        });

        return fileList.get(fileList.size() - 1);
    }

    private File collectFiles() {
        //搜集日志文件
        File logFile = getLogFile();
        if (logFile == null) return null;

        XLog.d("日志读取成功!");
        //创建文件保存用户反馈
        File responseFile = makeUserResponseFile();
        if (responseFile == null) return null;

        String appName = getPackageName();
        String targetFilePath = getCacheDir().getPath() + File.separator + appName + Utils.getRandomName() + ".zip";
        XLog.d("targetFilePath: " + targetFilePath);
        //收集附件 && 打包压缩
        try{
            ZipFile zipFile = new ZipFile((targetFilePath));
            zipFile.addFile(logFile);
            zipFile.addFile(responseFile);
            for (FileItem item: fileItemList){
                zipFile.addFile(new File(item.getPath()));
            }
        }catch (Exception e){
            XLog.e("文件压缩失败: " + e.getMessage());
            onTaskComplete(UploadStatus.FAILED, "文件压缩失败");
            return null;
        }

        XLog.d("创建压缩包成功");
        return new File(targetFilePath);
    }

    @SuppressLint("SetTextI18n")
    public void onProgressUpdate(int progress){
        if (progressView.getVisibility() != View.VISIBLE){
            progressView.setVisibility(View.VISIBLE);
        }
        progressBar.setProgress(progress);
        progressText.setText("进度: " + progress + "%");
    }

    private boolean sanityCheck(){
        if (titleText.getText().toString().isEmpty() ||
        contentText.getText().toString().isEmpty()){
            Utils.showText(LogReportActivity.this, "标题或内容不能为空!");
            return false;
        }
        return true;
    }

    private void startUpload(){
        isCanceled = false;
        if (!sanityCheck()) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressView.setVisibility(View.VISIBLE);
            }
        });
        uploadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                zipFile = collectFiles();
                if (zipFile != null && zipFile.exists()){
                    CosUtils.uploadFile(LogReportActivity.this, zipFile.getPath(), zipFile.getName());
                }
            }
        });
        uploadThread.start();
    }

    private void cancelUpload(){
        if (uploadThread != null && !uploadThread.isInterrupted()){
            CosUtils.cancelUpload();
            uploadThread.interrupt();
        }
    }

    private void onTaskComplete(UploadStatus statusCode, String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressView.setVisibility(View.GONE);
            }
        });

        if (statusCode.equals(UploadStatus.SUCCESS)) {
            String title = getString(R.string.app_name) + "用户反馈提醒";
            StringBuilder content = new StringBuilder();
            content.append(String.format(Locale.CHINESE, "标题：%s\n", titleText.getText()));
            content.append(String.format(Locale.CHINESE, "内容：%s\n", contentText.getText()));
            content.append(String.format(Locale.CHINESE, "用户信息：\n版本信息：%s \n设备信息： %s \n",
                    Utils.getCurrentVersion(this),
                    DeviceInfoUtil.getBasicInfo(this)));
            content.append(String.format(Locale.CHINESE, "附件名： %s \n", zipFile.getName()));
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Utils.notifyAuthor(LogReportActivity.this, title, content.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            Utils.showText(this, "发送成功!");
        }
        else if (statusCode.equals(UploadStatus.CANCEL)){
            Utils.showText(this, "已取消上传!");
        }
        else {
            Utils.showText(this, String.format(Locale.CHINESE, "发送失败！%s, 请稍后重试", msg));
        }
        progressView.setVisibility(View.GONE);
        if (uploadThread != null && !uploadThread.isInterrupted()){
            uploadThread.interrupt();
        }
        uploadThread = null;
    }

    private void onFileRemove(int position){
        fileItemList.remove(position);
        fileAdapter.notifyItemRemoved(position);
    }

    private void onFileAdd(File file){
        long fileSize = Utils.getFileSize(file);
        if (fileSize >= 100){
            Utils.showText(this, "单个文件的大小不能超过100Mb");
            return;
        }
        FileItem fileItem = new FileItem(file.getPath());
        if (fileItem.getType() == FileItem.FileType.UNKNOWN){
            Utils.showText(this,"不支持该文件格式，请重新选择附件");
            return;
        }
        fileItemList.add(fileItem);
        fileAdapter.notifyItemInserted(fileItemList.size() - 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == FILE_REQUEST_CODE) {
                File file = null;
                Uri uri = data.getData();
                if (uri != null) {
                    try {
                        file = FileUtil.from(this, uri);
                    } catch (IOException e) {
                        XLog.e("文件打开失败: " + e);
                        return;
                    }
                    XLog.d("选择: " + file.getPath());
                    if (!file.exists()) {
                        Utils.showText(this, "所选文件不存在");
                        return;
                    }
                    onFileAdd(file);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}