package com.lmy.iconcapturer.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import com.elvishew.xlog.XLog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lmy.iconcapturer.R;
import com.lmy.iconcapturer.bean.Result;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Utils {
    public static final int REQUEST_FLOAT_CODE = 1001;
    public static final int REQUEST_MEDIA_PROJECTION = 1003;
    public static final int NOTIFICATION_ID = 1004;
    public static final int INSTALL_REQUEST_CODE = 1005;
    private static final String TAG = "qfh";
    public static final String CHANNEL_ID = "icon_capture";


    /**
     * 跳转到设置页面申请打开无障碍辅助功能
     */
    private static void accessibilityToSettingPage(Context context) {
        //开启辅助功能页面
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            XLog.e(e);
        }
    }

    /**
     * 判断Service是否开启
     */
    public static boolean isServiceRunning(Context context, String serviceName) {
        if (TextUtils.isEmpty(serviceName)) {
            return false;
        }
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(1000);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : runningService) {
            if (runningServiceInfo.service.getClassName().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断悬浮窗权限权限
     */
    public static boolean commonROMPermissionCheck(Context context) {
        boolean result = true;
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class<?> clazz = Settings.class;
                java.lang.reflect.Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                result = (boolean) canDrawOverlays.invoke(null, context);
            } catch (Exception e) {
                XLog.e("ServiceUtils", e);
            }
        }
        return result;
    }

    /**
     * 检查悬浮窗权限是否开启
     */
    public static void checkSuspendedWindowPermission(Activity context, Runnable block) {
        if (commonROMPermissionCheck(context)) {
            block.run();
        } else {
            Toast.makeText(context, "请开启悬浮窗权限", Toast.LENGTH_SHORT).show();
            context.startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).setData(Uri.parse("package:" + context.getPackageName())), REQUEST_FLOAT_CODE);
        }
    }

//    /**
//     * 检查无障碍服务权限是否开启
//     */
//    public static void checkAccessibilityPermission(Activity context, Runnable block) {
//        if (isServiceRunning(context, WorkAccessibilityService.class.getCanonicalName())) {
//            block.run();
//        } else {
//            accessibilityToSettingPage(context);
//        }
//    }

    public static boolean isNull(Object any) {
        return any == null;
    }


    public static byte[] bitmap2ByteArray(Bitmap bitmap){
        int size = bitmap.getRowBytes() * bitmap.getHeight();
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        bitmap.copyPixelsToBuffer(byteBuffer);
        return byteBuffer.array();
    }

    public static Bitmap byteArray2Bitmap(byte[] byteArray, int width, int height, String format){
        Bitmap.Config configBmp = Bitmap.Config.valueOf(format);
        Bitmap bitmap_tmp = Bitmap.createBitmap(width, height, configBmp);
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        bitmap_tmp.copyPixelsFromBuffer(buffer);
        return bitmap_tmp;
    }

    public static boolean deleteFile(String path){
        boolean res = false;
        try{
            File file = new File(path);
            if (file.exists()){
                res = file.delete();
            }
        }catch (Exception e){
            XLog.e(e);
        }

        return res;
    }

    public static void deleteCache(Context context) {
        try {
            // 清理缓存
            File dir = context.getCacheDir();
            deleteDir(dir);

            // 清理过期apk文件
            File apk_dir = new File(context.getFilesDir(), "apks");
            if (apk_dir.exists()){
                deleteDir(apk_dir);
            }

            XLog.e("Success to clean cache");
        } catch (Exception e) {
            XLog.e(e);
            XLog.d( "Failed to delete cache.");
        }
    }

    public static boolean deleteDir(File dir) {
//        XLog.d( "try to delete file/dir " + dir);
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public static boolean checkNotificationPermission(Context context){
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        boolean isEnable =  notificationManagerCompat.areNotificationsEnabled();

        return isEnable;
    }

    /**
     * 打开通知权限
     *
     * @param context
     */
    public static void openNotificationSettingsForApp(Context context) {
        // Links to this app's notification settings.
        Intent intent = new Intent();
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
        intent.putExtra("app_package", context.getPackageName());
        intent.putExtra("app_uid", context.getApplicationInfo().uid);
        // for Android 8 and above
        intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());
        context.startActivity(intent);
    }

    public static void showText(Context context, String text){
        if (!Utils.commonROMPermissionCheck(context)){
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }else{
            ToastUtil.makeText(context, text, ToastUtil.LENGTH_SHORT).show();
        }
    }

    public static String getRandomName(){
        String uuid = UUID.randomUUID().toString();
        String timeStr = DAOHandler.getTimeStr();
        return timeStr + "_" + uuid;
    }

    public static String getCurrentVersion(Context context){
        try{
            String versionName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
            XLog.d("当前软件版本为: " + versionName);
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            XLog.e("获取版本号失败: ", e);
            return null;
        }
    }

    public static List<String> checkAppUpdateInfo(Context context, boolean showErrorText){
        XLog.d("检查软件更新");
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("packageName", context.getPackageName())
                .add("deviceInfo", DeviceInfoUtil.getBasicInfo(context))
                .add("currentVersion", Utils.getCurrentVersion(context))
                .build();
        Request request = new Request.Builder()
                .url(context.getString(R.string.server_url) + ":8080/app_info")
                .post(requestBody)
                .build();
        try{
            Response response = client.newCall(request).execute();
            if (response.body() != null){
                Gson gson = new Gson();
                Result result  = gson.fromJson(response.body().string(), Result.class);
                List<String> string_array = (List<String>)(result.getData());
                return string_array;
            }else{
                throw new Exception("response 为null");
            }
        }catch (Exception e){
            XLog.e("检查软件更新失败：", e);
            if (showErrorText){
                Utils.showText(context, "检查软件更新失败, 请稍后重试");
            }
            return null;
        }
    }

    public static Intent getInstallIntent(Context context, File apkFile){
        if(apkFile!= null && apkFile.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uriFromFile(context.getApplicationContext(), apkFile), "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            return intent;
        }
        return null;
    }

    public static void installAPK(Context context, File apkFile){
        Intent intent = getInstallIntent(context, apkFile);
        if (intent == null){
            Utils.showText(context, "软件更新失败");
            return;
        }
        try {
            context.getApplicationContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            XLog.e("apk安装失败", e);
        }
    }

    public static Uri uriFromFile(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, "com.lmy.iconcapturer.file.provider", file);
        } else {
            return Uri.fromFile(file);
        }
    }

    public static com.tencent.cloud.Response requestForTmpKey(Context context){
        XLog.d("检查软件更新");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(context.getString(R.string.server_url) + ":8080/cos_key").build();
        try{
            Response response = client.newCall(request).execute();
            if (response.body() != null){
                Gson gson = new Gson();
                JsonObject jsonObject  = gson.fromJson(response.body().string(), JsonObject.class);
                return gson.fromJson(jsonObject.getAsJsonObject("data"),com.tencent.cloud.Response.class);
            }else{
                throw new Exception("response 为null");
            }
        }catch (Exception e){
            XLog.e("获取临时key失败：", e);
            return null;
        }
    }

    public static void notifyAuthor(Context context, String title, String content){
        XLog.d("通知作者用户反馈");
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("emailTitle", title)
                .add("emailContent", content)
                .build();
        Request request = new Request.Builder()
                .url( context.getString(R.string.server_url) +":8080/user_response")
                .post(requestBody)
                .build();
        try{
            Response response = client.newCall(request).execute();
            if (response.body() != null){
                Gson gson = new Gson();
                Result result  = gson.fromJson(response.body().string(), Result.class);
                int resCode = result.getCode();
                if (resCode == 1){
                    XLog.d("通知作者用户反馈成功");
                }else{
                    XLog.d("通知作者用户反馈失败");
                }
            }else{
                throw new Exception("response 为null");
            }

        }catch (Exception e){
            XLog.e("通知作者用户反馈失败：", e);
        }
    }

    public static long getFileSize(File file) {
        if (!file.exists() || !file.isFile()) {
            System.out.println("文件不存在");
            return -1;
        }

        long fileSize = file.length() / (1024 * 1024);
        XLog.d(String.format(Locale.CHINESE, "文件大小为: %d Mb", fileSize));
        return fileSize;   // 1Mb = 1024 * 1024 byte
    }



}
