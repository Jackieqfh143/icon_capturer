package com.lmy.iconcapturer.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.elvishew.xlog.XLog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.lmy.iconcapturer.R;
import com.lmy.iconcapturer.ShotApplication;
import com.lmy.iconcapturer.bean.IconImage;
import com.lmy.iconcapturer.fragment.UpdateDialogFragment;
import com.lmy.iconcapturer.service.CacheService;
import com.lmy.iconcapturer.service.CaptureScreenService;
import com.lmy.iconcapturer.service.DownloadBinder;
import com.lmy.iconcapturer.service.DownloadService;
import com.lmy.iconcapturer.service.SuspendwindowService;
import com.lmy.iconcapturer.utils.ConfigUtil;
import com.lmy.iconcapturer.utils.DAOHandler;
import com.lmy.iconcapturer.utils.DeviceInfoUtil;
import com.lmy.iconcapturer.utils.IconAdapter;
import com.lmy.iconcapturer.utils.LoggerUtil;
import com.lmy.iconcapturer.utils.UpdateTask;
import com.lmy.iconcapturer.utils.UpdateTaskListener;
import com.lmy.iconcapturer.utils.Utils;
import com.lmy.iconcapturer.utils.ViewModelMain;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity{
    private DrawerLayout mDrawerLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View floatRootView; // 悬浮窗View
    private BottomNavigationView bottomNavigationView;

    private List<IconImage> iconImageList =  new ArrayList<>();

    private IconAdapter iconAdapter;

    private int result = 0;
    private Intent intent = null;
    private MediaProjectionManager mMediaProjectionManager;

    private String orderStr = "save_time desc"; //desc 降序 || asc 升序

    private BroadcastReceiver broadcastReceiver;

    private RecyclerView recyclerView;

    private int currentPos;

    private String versionName;

    private DownloadBinder downloadBinder;

    private boolean isUserCheckUpdate;

    private MenuItem switch_item = null;

    private MenuItem sort_item = null;

    private static String deviceInfo;

    private static boolean isFloatWinShow = false;

    private String latestVersion;

    private String apkUrl;

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

    @SuppressLint({"ResourceType", "SetTextI18n"})
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoggerUtil.LoggerInit(this);
        XLog.e("Create Main activity");
        setContentView(R.layout.activity_main);

        //记录基础的设备信息
        deviceInfo = DeviceInfoUtil.getBasicInfo(this);

        //初始化配置
        ConfigUtil.initConfig(this);

        //获取当前版本号
        versionName = Utils.getCurrentVersion(this);

        //开启清理缓存服务
        startService(new Intent(this, CacheService.class));

        //开启软件更新服务
        startService(new Intent(this, DownloadService.class));
        Intent intent = new Intent(getBaseContext(), DownloadService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);

        // 创建数据库
        DAOHandler.makeDataBase();

        //读取设置的排序方式 (默认为时间倒序)
        orderStr = ConfigUtil.getSortType();

        // 读取数据库
        List<IconImage> tmpList = LitePal
                .order(orderStr)
                .where("isLike = ?", "0")
                .find(IconImage.class);
        iconImageList.addAll(tmpList);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        actionBarDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_log:
//                        shareLog();
                         showShareLogWin();
                         break;
                    case R.id.version:
                        showInfoWin();
                        break;
                    case R.id.menu_setting:
                        showSettingWin();
                    default:
                        break;
                }
                return true;
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        iconAdapter = new IconAdapter(iconImageList);
        recyclerView.setAdapter(iconAdapter);

        bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottom_menu_home:
                        refreshIcons();
                        return true;
                    case R.id.bottom_menu_like:
                        showLikeIcons();
                        return true;
                }
                return false;
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeColors(R.color.themeSelected);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onRefreshData();
            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                XLog.e( "main activity receive broadcast: " + intent.getAction());
                if ("com.lmy.iconcapturer.DELICON".equals(intent.getAction())) {
                    XLog.i("del icon");
                    currentPos = intent.getIntExtra("position", -1);
                    if (currentPos != -1){
                        removeIcon(currentPos);
                    }
                }else if ("com.lmy.iconcapturer.REFRESH_DATA".equals(intent.getAction())){
                    onRefreshData();
                }else if("com.lmy.iconcapturer.EDIT_MODE".equals(intent.getAction())){
                    XLog.d( "receive broadcast com.lmy.iconcapturer.EDIT_MODE");
                    enterEditMode();
                }else if ("com.lmy.iconcapturer.APK_READY".equals(intent.getAction())){
                    Utils.showText(MainActivity.this, "正在启动安装界面");
                    if(!getPackageManager().canRequestPackageInstalls()){
                        Utils.showText(MainActivity.this, "安装界面启动失败，请开启安装软件的权限后重试");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                checkInstallPermission();
                            }
                        }, 1000);
                    }else{
                        String apkFile = intent.getStringExtra("apkFile");
                        XLog.e("apkFile: " + apkFile);
                        Utils.installAPK(MainActivity.this, new File(apkFile));
                    }
                }else if ("com.lmy.iconcapturer.REFRESH_ORDER".equals(intent.getAction())){
                    updateOrder(sort_item, false);
                }else if ("com.lmy.iconcapturer.CHECK_FOR_UPDATE".equals(intent.getAction())){
                    checkForUpdate(false);
                }
                else if ("com.lmy.iconcapturer.CONFIRM_UPDATE".equals(intent.getAction())){
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
                }
            }
        };
        IntentFilter filters = new IntentFilter();
        filters.setPriority(90);
        filters.addAction("com.lmy.iconcapturer.DELICON");
        filters.addAction("com.lmy.iconcapturer.REFRESH_DATA");
        filters.addAction("com.lmy.iconcapturer.EDIT_MODE");
        filters.addAction("com.lmy.iconcapturer.APK_READY");
        filters.addAction("com.lmy.iconcapturer.REFRESH_ORDER");
        filters.addAction("com.lmy.iconcapturer.CHECK_FOR_UPDATE");
        filters.addAction("com.lmy.iconcapturer.CONFIRM_UPDATE");
        registerReceiver(broadcastReceiver, filters);
        boolean isFirstRun = ConfigUtil.preferences.getBoolean("isFirstRun", true);
        boolean isAllowNotify = Utils.checkNotificationPermission(this);
        if (isFirstRun){
            ConfigUtil.editor.putBoolean("isFirstRun", false);
            ConfigUtil.editor.commit();
        }

        if (!isFirstRun && !isAllowNotify && !ConfigUtil.isNotifyDontShowAgain()){
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("温馨提醒");
            dialog.setMessage("软件通知权限尚未开启，您将无法实时收到表情包捕获结果的提示信息，是否开启通知权限？");
            dialog.setCancelable(true);

            if (!ConfigUtil.isNotifyDontShowAgain()){
                View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_checkbox, null);
                CheckBox dontShowAgain = (CheckBox) dialogLayout.findViewById(R.id.skip_checkbox);
                dialog.setView(dialogLayout);
                dontShowAgain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        XLog.d("dontShowAgain.isChecked(): " + dontShowAgain.isChecked());
                        ConfigUtil.setNotifyDontShowAgain(dontShowAgain.isChecked());
                    }
                });
            }
            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Utils.openNotificationSettingsForApp(MainActivity.this);
                }
            });
            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            dialog.show();
        }


    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        XLog.e("call MainActivity onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        XLog.e("call MainActivity onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void enterEditMode(){
        Intent editIntent = new Intent(this, EditActivity.class);
        editIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        int isLike = bottomNavigationView.getSelectedItemId() == R.id.bottom_menu_home ? 0:1;
        editIntent.putExtra("isLike", String.valueOf(isLike));
        startActivity(editIntent);
    }

    private void updateOrder(MenuItem item, boolean updateDataBase){
        ImageView sortImageView = (ImageView) item.getActionView().findViewById(R.id.sort_btn_image);
        orderStr = ConfigUtil.getSortType();

        XLog.i( "orderStr: " + orderStr);
        if (updateDataBase){
            if (orderStr.contains("desc")){
                ConfigUtil.setSortType("save_time asc");
                Utils.showText(this, "时间升序排列");
            }else{
                ConfigUtil.setSortType("save_time desc");
                Utils.showText(this, "时间倒序排列");
            }
            orderStr = ConfigUtil.getSortType();
        }

        //更新recycleview
        onRefreshData();
        XLog.i( "orderStr: " + orderStr);
        if (orderStr.contains("desc")){
            sortImageView.setImageDrawable(getDrawable(R.drawable.sort_down_btn));
        }else{
            sortImageView.setImageDrawable(getDrawable(R.drawable.sort_up_btn));
        }
    }

    private void removeIcon(int position){
        IconImage iconImage = iconImageList.get(position);
        String filePath = iconImage.getPath();

        //删除数据库记录
        XLog.i( "del icon of uuid = " + iconImage.getUuid());
        List<IconImage> tmpList = LitePal.where("uuid = ?", iconImage.getUuid()).find(IconImage.class);
        XLog.i( "tmpList size " + tmpList.size());
        LitePal.deleteAll(IconImage.class, "uuid = ?", iconImage.getUuid());
        tmpList = LitePal.where("uuid = ?", iconImage.getUuid()).find(IconImage.class);
        XLog.i( "tmpList size " + tmpList.size());

        if (tmpList.size() > 0){
            Utils.showText(this, "删除失败");
            return;
        }

        //删除实际的文件
        boolean delFileRes = Utils.deleteFile(filePath);
        if (!delFileRes){
            Utils.showText(this, "删除失败");
            //数据库回溯
            iconImage.save();
            return;
        }

        iconImageList.remove(position);
        iconAdapter.updateDataList(iconImageList);
        iconAdapter.notifyItemRemoved(position);
    }

    private void refreshIcons(){
        swipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    iconImageList = LitePal
                            .where("isLike = ?", "0")
                            .order(orderStr)
                            .find(IconImage.class);
                    XLog.i( "iconImageList.size(): " + iconImageList.size());
                    Thread.sleep(500);
                } catch (InterruptedException e){
                    XLog.e(e);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        iconAdapter.updateDataList(iconImageList);
                        iconAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    private void showLikeIcons(){
        swipeRefreshLayout.setRefreshing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    iconImageList = LitePal
                            .where("isLike = ?", "1")
                            .order(orderStr)
                            .find(IconImage.class);
                    XLog.i( "iconImageList.size(): " + iconImageList.size());
                    Thread.sleep(500);
                } catch (InterruptedException e){
                    XLog.e(e);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        iconAdapter.updateDataList(iconImageList);
                        iconAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startIntent(){
        if(intent != null && result != 0){
            XLog.i( "user agree the application to capture screen");
            ((ShotApplication)getApplication()).setResult(result);
            ((ShotApplication)getApplication()).setIntent(intent);
        }else{
            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), Utils.REQUEST_MEDIA_PROJECTION);
        }
    }

    /**
     * 关闭所有悬浮窗
     */
    private void closeAllSuspendWindow() {
        if (floatRootView != null && floatRootView.getWindowToken() != null) {
            getWindowManager().removeView(floatRootView);
        }
        ViewModelMain.isShowSuspendWindow.postValue(false);
        stopAllService();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        XLog.e("onActivityResult is called");
        if (requestCode == Utils.REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                XLog.e("user refuse the application to capture screen");
                Utils.showText(this, "获取用户授权失败，无法启动悬浮窗");
                closeAllSuspendWindow();
                refreshSwitchStatus();
                return;
            } else if (data != null && resultCode != 0) {
                XLog.i("user agree the application to capture screen");
                result = resultCode;
                intent = data;
                ((ShotApplication) getApplication()).setResult(resultCode);
                ((ShotApplication) getApplication()).setIntent(data);
                ((ShotApplication) getApplication()).setMediaProjectionManager(mMediaProjectionManager);

                Intent intent = new Intent("com.lmy.iconcapture.MEDIA_PROJECTION_READY");
                sendBroadcast(intent);
            }
        }
        else if (requestCode == Utils.INSTALL_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                XLog.e("user refuse to install application");
                return;
            } else if (data != null && resultCode != 0) {
                XLog.i("user agree to install application");
                String apkFile = intent.getStringExtra("apkFile");
                Utils.installAPK(MainActivity.this, new File(apkFile));
            }
        }
    }

    private void onRefreshData(){
        if (bottomNavigationView.getSelectedItemId() == R.id.bottom_menu_home){
            refreshIcons();
        }else{
            showLikeIcons();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        XLog.e("Main Activity start");
        checkForUpdate(false);
    }

    private void checkForUpdate(boolean isUserCheckUpdate){
        XLog.d("checkForUpdate");
        XLog.d("isUserCheckUpdate: ", isUserCheckUpdate);
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
                        boolean dontShowDialog = ConfigUtil.isUpdateDontShowAgain();
                        String prefVersion = ConfigUtil.getVersionCode();
                        if (!isUserCheckUpdate && dontShowDialog && prefVersion.equals(latestVersion)){
                            return;
                        }
                        showUpdateDialog(latestVersion,"软件更新提醒",
                                String.format(Locale.CHINESE, "检测到软件有新版本%s（当前版本为: %s）是否更新？",
                                        latestVersion, versionName), updateLog, !isUserCheckUpdate);

                    }

                    else{
                        if (isUserCheckUpdate){
                            Utils.showText(MainActivity.this, "已是最新版本");
                        }
                    }
                }
            }

            @Override
            public void onFailed() {

            }
        });
        updateTask.execute(MainActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        XLog.e( "Main Activity Pause");
    }

    @Override
    protected void onResume() {
        super.onResume();
//        refreshIcons();
        onRefreshData();
        XLog.e( "Main Activity Resume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        XLog.e( "Main Activity Stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        XLog.e( "Main Activity Destroy");
        unbindService(connection);
        unregisterReceiver(broadcastReceiver);
//        ViewModelMain.isVisible.postValue(false);
        //清除缓存，减少内存占用
//        Utils.deleteCache(this);
//        Log.d( "Main Activity Destroy");
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        XLog.e( "Main Activity onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.main, menu);
        switch_item = menu.findItem(R.id.float_btn);
        ImageView imageView = (ImageView) switch_item.getActionView().findViewById(R.id.switch_btn_image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                onSwitchClicked(switch_item);
            }
        });
        switch_item.getActionView().setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(switch_item);
            }
        });

        refreshSwitchStatus();
        sort_item = menu.findItem(R.id.menu_sort_btn);
        ImageView sortImageView = (ImageView) sort_item.getActionView().findViewById(R.id.sort_btn_image);
        sortImageView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                updateOrder(sort_item, true);
            }
        });
        sort_item.getActionView().setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(sort_item);
            }
        });

        final MenuItem edit_item = menu.findItem(R.id.menu_edit_btn);
        ImageView editImageView = (ImageView) edit_item.getActionView().findViewById(R.id.menu_edit_image);
        editImageView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                enterEditMode();
            }
        });
        edit_item.getActionView().setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(sort_item);
            }
        });

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.float_btn:
                onSwitchClicked(item);
                break;
            case R.id.menu_sort_btn:
                updateOrder(item, true);
                break;
            case R.id.menu_edit_btn:
                enterEditMode();
            default:
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void onSwitchClicked(MenuItem item){
        if (!Utils.commonROMPermissionCheck(this)){
            Utils.showText(MainActivity.this, "请开启悬浮窗权限后重试");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).setData(Uri.parse("package:" + MainActivity.this.getPackageName())), Utils.REQUEST_FLOAT_CODE);
                }
            }, 1000);
            return;
        }

        boolean isWinShow = ((ShotApplication) getApplication()).isFloatWindowShow();
        if (!isWinShow){
            startAllService();
            ViewModelMain.isShowSuspendWindow.postValue(true);
            ImageView imageView = (ImageView) item.getActionView().findViewById(R.id.switch_btn_image);
            imageView.setImageResource(R.drawable.switch_on);
            TextView textView = (TextView) item.getActionView().findViewById(R.id.switch_btn_text);
            textView.setText("关闭悬浮窗");
            isFloatWinShow = true;
        }else{
            closeAllSuspendWindow();
            ImageView imageView = (ImageView) item.getActionView().findViewById(R.id.switch_btn_image);
            imageView.setImageResource(R.drawable.switch_off);
            TextView textView = (TextView) item.getActionView().findViewById(R.id.switch_btn_text);
            textView.setText("打开悬浮窗");
            isFloatWinShow = false;
        }
    }

    private void refreshSwitchStatus(){
        try{
            isFloatWinShow = isServiceRunning(SuspendwindowService.class);
            XLog.e("isFloatWinshow: " + isFloatWinShow);
            if (isFloatWinShow){
                ImageView imageView = (ImageView) switch_item.getActionView().findViewById(R.id.switch_btn_image);
                imageView.setImageResource(R.drawable.switch_on);
                TextView textView = (TextView) switch_item.getActionView().findViewById(R.id.switch_btn_text);
                textView.setText("关闭悬浮窗");
            }else{
                ImageView imageView = (ImageView) switch_item.getActionView().findViewById(R.id.switch_btn_image);
                imageView.setImageResource(R.drawable.switch_off);
                TextView textView = (TextView) switch_item.getActionView().findViewById(R.id.switch_btn_text);
                textView.setText("打开悬浮窗");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showSettingWin(){
        Intent intent = new Intent(this, SettingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showInfoWin(){
        Intent intent = new Intent(this, AboutActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showShareLogWin(){
        Intent intent = new Intent(this, LogReportActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void checkInstallPermission(){
        //installtion permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(!this.getPackageManager().canRequestPackageInstalls()){
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        .setData(Uri.parse(String.format("package:%s", getPackageName()))), Utils.INSTALL_REQUEST_CODE);
            }
        }
        //Storage Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startAllService(){
        mMediaProjectionManager = (MediaProjectionManager)getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startIntent();
        startService(new Intent(this, CaptureScreenService.class));
        startService(new Intent(this, SuspendwindowService.class));
    }

    private void stopAllService(){
        stopService(new Intent(this, SuspendwindowService.class));
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void showUpdateDialog(String latestVersion, String title, String content, String updateLog, boolean showCheckBox){
        DialogFragment newFragment = UpdateDialogFragment.newInstance(latestVersion, title, content, updateLog, showCheckBox);
        newFragment.show(getSupportFragmentManager(), "updateDialog");
    }
}
