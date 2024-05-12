package com.lmy.iconcapturer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.elvishew.xlog.XLog;
import com.lmy.iconcapturer.R;
import com.lmy.iconcapturer.cos_utils.CosUtils;
import com.lmy.iconcapturer.utils.DownloadTaskListener;
import com.lmy.iconcapturer.utils.Utils;
import com.tencent.cos.xml.transfer.TransferState;

import net.lingala.zip4j.ZipFile;

import java.io.File;

public class DownloadService extends Service {

    public File apkFile = null;

    public TransferState downloadState;

    public int progress;

    private String notifyTitle;

    public DownloadTaskListener listener = new DownloadTaskListener() {
        @Override
        public void onProgress(int progress) {
            XLog.d("下载进度: " + progress);
            getNotificationManger().notify(Utils.NOTIFICATION_ID, createNotification(progress));
        }

        @Override
        public void onSuccess(File file) {
            downloadState = TransferState.COMPLETED;
            XLog.e("apk下载完成");
            XLog.e("apk file path " + file.getPath());
            stopForeground(true);
            try{
                File saveFolder = new File(file.getParent(), "apks");
                if (saveFolder.exists()){
                    //清理上次的安装包
                    Utils.deleteDir(saveFolder);
                    saveFolder.mkdirs();
                }
                new ZipFile(file).extractAll(saveFolder.getPath());
                File[] apkFiles = saveFolder.listFiles();
                if (apkFiles!= null && apkFiles.length > 0){
                    XLog.d("解压完成");
                    Intent intent = new Intent("com.lmy.iconcapturer.APK_READY");
                    intent.putExtra("apkFile", apkFiles[0].getPath());
                    apkFile = apkFiles[0];
                    sendBroadcast(intent);
                    getNotificationManger().notify(Utils.NOTIFICATION_ID, createNotification(100));
                }
            }catch (Exception e){
                XLog.e("软件更新失败: ", e);
                Utils.showText(getApplicationContext(), "软件更新失败");
            }
        }

        @Override
        public void onFailed() {
            downloadState = TransferState.FAILED;
            stopForeground(true);
            getNotificationManger().notify(Utils.NOTIFICATION_ID, createNotification(-1));
            Utils.showText(getApplicationContext(), "软件更新失败");
        }
    };

    private DownloadBinder mBinder = new DownloadBinder(this);

    private BroadcastReceiver broadcastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                XLog.e("download service receive broadcast " + intent.getAction());
                if ("com.lmy.iconcapturer.DOWNLOAD_STATE_CHANGE".equals(intent.getAction())){
                    handleDownStateChange();
                }else if ("com.lmy.iconcapturer.DOWNLOAD_PROGRESS".equals(intent.getAction())){
                    progress = intent.getIntExtra("progress", 0);
                    listener.onProgress(progress);
                    abortBroadcast();
                }else if ("com.lmy.iconcapturer.DOWNLOAD_SUCCESS".equals(intent.getAction())){
                    String apkFilePath = intent.getStringExtra("apkFilePath");
                    XLog.e("apkFilePath" + apkFilePath);
                    listener.onSuccess(new File(apkFilePath));
                    abortBroadcast();
                }else if ("com.lmy.iconcapturer.DOWNLOAD_FAILED".equals(intent.getAction())){
                    listener.onFailed();
                    abortBroadcast();
                }else if ("com.lmy.iconcapturer.DOWNLOAD_STATE_UPDATE".equals(intent.getAction())){
                    downloadState = CosUtils.curDownloadState;
                    if (downloadState == null) return;
                    if (downloadState.equals(TransferState.PAUSED)){
                        notifyTitle = "点击继续";
                    }else if (downloadState.equals(TransferState.IN_PROGRESS)){
                        notifyTitle = "点击暂停";
                    }else if (downloadState.equals(TransferState.COMPLETED)){
                        NotificationManagerCompat.from(context).cancelAll();
                        getNotificationManger().notify(Utils.NOTIFICATION_ID, createNotification(100));
                    }
                }
            }
        };
        IntentFilter intentFilters = new IntentFilter();
        intentFilters.addAction("com.lmy.iconcapturer.DOWNLOAD_PROGRESS");
        intentFilters.addAction("com.lmy.iconcapturer.DOWNLOAD_STATE_CHANGE");
        intentFilters.addAction("com.lmy.iconcapturer.DOWNLOAD_SUCCESS");
        intentFilters.addAction("com.lmy.iconcapturer.DOWNLOAD_FAILED");
        intentFilters.addAction("com.lmy.iconcapturer.DOWNLOAD_STATE_UPDATE");
        registerReceiver(broadcastReceiver,intentFilters);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public DownloadService(){}

    public void startDownload(String latestVersion, String apkUrl){
        Utils.showText(this, "开始下载请稍等");
        File saveFile = new File(latestVersion + ".zip");
        try{
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    CosUtils.downloadFile(DownloadService.this, apkUrl, getFilesDir().getPath(), saveFile.getPath());
                }
            });
            thread.start();
        }catch (Exception e){
            XLog.e("下载错误: " + e);
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        return super.onStartCommand(intent, flags, startId);
    }

    private NotificationManager getNotificationManger(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(Utils.CHANNEL_ID, "IconCapturer", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            XLog.i( "CreateNotificationChannel");
        }
    }

    Notification createNotification(int progress) {
        // 创建并配置通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Utils.CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_obj)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent("com.lmy.iconcapturer.DOWNLOAD_STATE_CHANGE");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(DownloadService.this, Utils.NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        XLog.i( "CreateNotification");
        if (progress >= 0 && progress < 100){
            builder.setContentText("下载进度 " + progress + "%");
            builder.setProgress(100, progress, false);
            builder.setContentTitle(notifyTitle);
        }else if (progress >= 100){
            builder.setContentTitle("下载完成，点击安装");
        }
        else{
            builder.setContentTitle("下载失败");
        }

        return builder.build();
    }

    private void handleDownStateChange(){
        if (downloadState == null) return;
        if (downloadState.equals(TransferState.COMPLETED)){
            Intent intent = new Intent("com.lmy.iconcapturer.APK_READY");
            intent.putExtra("apkFile", apkFile.getPath());
            sendBroadcast(intent);
            return;
        }
        if (downloadState.equals(TransferState.PAUSED)){
            CosUtils.resumeDownload();
            Utils.showText(DownloadService.this, "继续下载");
            getNotificationManger().notify(Utils.NOTIFICATION_ID, createNotification(progress));
        } else if (downloadState.equals(TransferState.IN_PROGRESS) || downloadState.equals(TransferState.WAITING)){
            CosUtils.pauseDownload();
            Utils.showText(DownloadService.this, "暂停下载");
            getNotificationManger().notify(Utils.NOTIFICATION_ID, createNotification(progress));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }


}
