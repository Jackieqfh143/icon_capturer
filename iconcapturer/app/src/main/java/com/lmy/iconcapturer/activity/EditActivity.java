package com.lmy.iconcapturer.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.elvishew.xlog.XLog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.lmy.iconcapturer.R;
import com.lmy.iconcapturer.bean.IconImage;
import com.lmy.iconcapturer.utils.ConfigUtil;
import com.lmy.iconcapturer.utils.EditIconAdapter;
import com.lmy.iconcapturer.utils.FileUtil;
import com.lmy.iconcapturer.utils.Utils;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EditActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<IconImage> iconImageList =  new ArrayList<>();

    private EditIconAdapter iconAdapter;

    private String orderStr = "save_time desc"; //desc 降序 || asc 升序

    private BroadcastReceiver broadcastReceiver;

    private BottomNavigationView bottomNavigationView;

    private RecyclerView recyclerView;

    private String isLike = "0";

    private MenuItem likeItem;


    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        XLog.d( "Create edit activity!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        //获取intent数据
        Intent intent = getIntent();
        isLike = intent.getStringExtra("isLike");

        XLog.d( "isLike: " + isLike);

        //读取设置的排序方式 (默认为时间倒序)
        orderStr = ConfigUtil.getSortType();

        // 读取数据库
        List<IconImage> tmpList = LitePal
                .order(orderStr)
                .where("isLike = ?", isLike)
                .find(IconImage.class);

        iconImageList.addAll(tmpList);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        iconAdapter = new EditIconAdapter(iconImageList);
        recyclerView.setAdapter(iconAdapter);

        bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setSelected(false);
        @SuppressLint("ResourceType") ColorStateList stateList = getColorStateList(R.drawable.bottom_text_selector);
        bottomNavigationView.setItemTextColor(stateList);

        Menu menu = bottomNavigationView.getMenu();
        likeItem = menu.findItem(R.id.bottom_menu_like);
        if (isLike.equals("0")){
            likeItem.setTitle("加入收藏");
        }else{
            likeItem.setTitle("移除收藏");
            likeItem.setIcon(R.drawable.heart_filled);
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (iconAdapter.getSelectIconList().size() == 0){
                    Utils.showText(EditActivity.this, "未选中任何表情包");
                    return true;
                }
                switch (item.getItemId()) {
                    case R.id.bottom_menu_download:
                        Handler handler = new Handler();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(true);
                            }
                        });
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                downloadIcon();
                            }
                        }, 200);

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }, 1000);
                        break;
                    case R.id.bottom_menu_del:
                        onConfirmDelAction();
                        break;

                    case R.id.bottom_menu_like:
                        likeIcon();
                        break;
                    case R.id.bottom_menu_share:
                        shareIcons();
                        break;

                    default:
                        break;
                }
                return true;
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeColors(R.color.themeSelected);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                refreshIcons();
                reLoad();
            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };
        IntentFilter filters = new IntentFilter();
        registerReceiver(broadcastReceiver, filters);
    }

    private void updateOrder(MenuItem item, boolean updateDataBase){
        orderStr = ConfigUtil.getSortType();
        XLog.i( "orderStr: " + orderStr);
        if (updateDataBase){
            if (orderStr.contains("desc")){
                ConfigUtil.setSortType("save_time asc");
                Utils.showText(this,"时间升序排列" );
            }else{
                ConfigUtil.setSortType("save_time desc");
                Utils.showText(this, "时间倒序排列");
            }
            orderStr = ConfigUtil.getSortType();
        }
        reLoad();
    }

    private EditIconAdapter.ViewHolder getHolder(int position){
        return (EditIconAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void selectAll(){
        iconAdapter.clearData();
        iconAdapter.notifyDataSetChanged();
        for (int i = 0; i < iconImageList.size(); i++) {
            EditIconAdapter.ViewHolder holder = getHolder(i);
            if (holder != null){
                iconAdapter.selectItem(holder);
            }else{
                iconAdapter.addSelectPosition(i);
            }
        }
    }

    private void unSelectAll(){
        iconAdapter.clearData();
        iconAdapter.notifyDataSetChanged();
        for (int i = 0; i < iconImageList.size(); i++) {
            iconAdapter.unSelectItem(getHolder(i));
        }

        XLog.d( "selected icon list size is " +  iconAdapter.getSelectIconList().size());
        XLog.d( "iconImageList.size() is " + iconImageList.size());
    }

    private void onConfirmDelAction(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("删除确认");
        dialog.setMessage("此操作无法撤销，确认删除选中的表情包吗？");
        dialog.setCancelable(true);
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                removeIcon();
            }
        });

        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.show();
    }

    private boolean isAllSelected(){
        List<IconImage> selectedIconList = iconAdapter.getSelectIconList();
        HashMap<Integer, EditIconAdapter.ViewHolder> holderHashMap = iconAdapter.getSelectedHolders();
        XLog.d( "holderHashMap.size() is " + holderHashMap.size());
        XLog.d( "iconImageList.size() is " + iconImageList.size());
        XLog.d( "selectedIconList.size() is " + selectedIconList.size());
        if (selectedIconList.size() != holderHashMap.size() || iconImageList.size() == selectedIconList.size()){
            return true;
        }
        return false;
    }

    private void removeAllIcon(){
        XLog.d( "删除所有isLike = " + isLike + " Icons");
        XLog.i( "islike = " + isLike);
        List<IconImage> selectIconList = iconAdapter.getSelectIconList();
        int total = selectIconList.size();
        int fail = 0;
        int success = 0;
        for (IconImage iconImage : selectIconList){
            boolean delRes = delItem(iconImage);
            if (delRes){
                success++;
            }else{
                fail++;
            }
        }
        String text = "批量删除 " + total + " 个表情包, " + success + " 成功 " + fail + " 失败";
        Utils.showText(this, text);
        //清除缓存，减少内存占用
        Utils.deleteCache(this);
        reLoad();
    }

    private void removeIcon(){
        XLog.d( "准备删除icons");
        List<IconImage> selectIconList = iconAdapter.getSelectIconList();
        XLog.i( "删除部分icon");
        int total = selectIconList.size();
        int success = 0;
        int fail = 0;
        for (IconImage iconImage : selectIconList){
            boolean delRes = delItem(iconImage);
            if (!delRes){
                fail++;
                continue;
            }
            int pos = iconImageList.indexOf(iconImage);
            iconImageList.remove(iconImage);
            iconAdapter.notifyItemRemoved(pos);
            success++;
        }
        unSelectAll();
        String text = "批量删除 " + total + " 个表情包, " + success + " 成功 " + fail + " 失败";
        XLog.d( text);
        Utils.showText(this, text);

        if (iconImageList.isEmpty()){
            finish();
        }
//        reLoad();
    }

    private boolean delItem(IconImage iconImage){
        try{
            XLog.i( "del icon of uuid = " + iconImage.getUuid());
            List<IconImage> tmpList = LitePal.where("uuid = ?", iconImage.getUuid()).find(IconImage.class);
            XLog.i( "tmpList size " + tmpList.size());
            LitePal.deleteAll(IconImage.class, "uuid = ?", iconImage.getUuid());
            tmpList = LitePal.where("uuid = ?", iconImage.getUuid()).find(IconImage.class);
            XLog.i( "tmpList size " + tmpList.size());

            if (tmpList.size() > 0){
                return false;
            }

            boolean delFileRes = Utils.deleteFile(iconImage.getPath());

            //略缩图也需要删除
            Utils.deleteFile(iconImage.getPath().replace(".png", "_small.png"));

            if (!delFileRes){
                //数据库回溯
                iconImage.save();
                return false;
            }
        }catch (Exception e){
            XLog.e(e);
            return false;
        }
        return true;
    }

    private void downloadIcon(){
        XLog.d("download all icons");
        List<IconImage> iconImageList = iconAdapter.getSelectIconList();
        int total = iconImageList.size();
        int fail = 0;
        int success = 0;
        for (IconImage iconImage : iconImageList){
            String srcPath = iconImage.getPath();
            File dstFile = new File(FileUtil.AlbumFolder, FileUtil.getFileNameByPath(srcPath));
            try{
                FileUtil.copyFileUsingStream(new File(srcPath), dstFile);
            }catch (Exception e){
                e.printStackTrace();
                XLog.e("图像下载失败!", e);
                fail++;
            }

            Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(dstFile);
            media.setData(contentUri);
            sendBroadcast(media);
            success++;
        }
        unSelectAll();

        Snackbar snackbar = Snackbar.make(recyclerView, "批量下载 " + total + " 个表情包, " + success + " 成功 " + fail + " 失败", Snackbar.LENGTH_SHORT)
                .setAction("点击查看", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
        CoordinatorLayout.LayoutParams params=(CoordinatorLayout.LayoutParams)snackbar.getView().getLayoutParams();
        params.setAnchorId(R.id.bottom_nav_view);
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.anchorGravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        snackbar.getView().setLayoutParams(params);
        snackbar.show();
    }

    private void likeIcon(){
        List<IconImage> selectedIconList = iconAdapter.getSelectIconList();
        int total = selectedIconList.size();
        int fail = 0;
        int success = 0;
        for (IconImage iconImage : selectedIconList){
            boolean status = isLike.equals("0")? true:false;
            iconImage.setLike(status);
            try{
                iconImage.save();
            }catch (Exception e){
                XLog.e(e);
                fail++;
            }
            success++;
        }
        unSelectAll();
        String text = "批量" + likeItem.getTitle() + " " + total + " 个表情包, " + success + " 成功 " + fail + " 失败";
        XLog.d(text);
        Utils.showText(this, text);
        List<IconImage> tmpList = LitePal
                .where("isLike = ?", isLike)
                .find(IconImage.class);
        if (tmpList.isEmpty()){
            finish();
        }else{
            reLoad();
        }
    }


    private void shareIcons(){
        List<IconImage> selectedIcons = iconAdapter.getSelectIconList();
        // 创建分享Intent
        Intent shareIntent = new Intent();
        shareIntent.setType("image/*");// 设置MIME类型为image/*表示分享图片
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        ArrayList<Uri> imageUris = new ArrayList<>();
        for (IconImage iconImage : selectedIcons){
            File file = new File(iconImage.getPath());
            Uri imageUri = FileProvider.getUriForFile(this, "com.lmy.iconcapturer.file.provider", file);
            imageUris.add(imageUri);
        }
        if (imageUris.isEmpty()){
            Utils.showText(this, "启动分享失败");
            return;
        }
        if (imageUris.size() > 1){
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris); // 使用EXTRA_STREAM传递图片的Uri
        }else{
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUris.get(0)); // 使用EXTRA_STREAM传递图片的Uri
        }
        // 启动分享操作
        startActivity(Intent.createChooser(shareIntent, "表情包分享"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);

        final MenuItem checkall_item = menu.findItem(R.id.menu_checkall_btn);
        ImageView checkAllImageView = (ImageView) checkall_item.getActionView().findViewById(R.id.menu_checkall_image);
        checkAllImageView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                selectAll();
            }
        });
        checkall_item.getActionView().setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(checkall_item);
            }
        });

        final MenuItem decheckall_item = menu.findItem(R.id.menu_decheckall_btn);
        ImageView decheckAllImageView = (ImageView) decheckall_item.getActionView().findViewById(R.id.menu_decheckall_image);
        decheckAllImageView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                unSelectAll();
            }
        });
        decheckall_item.getActionView().setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(decheckall_item);
            }
        });

        final MenuItem sort_item = menu.findItem(R.id.edit_menu_sort_btn);
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

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.edit_menu_sort_btn:
                updateOrder(item, true);
                return true;
            case R.id.menu_decheckall_btn:
                unSelectAll();
                return true;
            case R.id.menu_checkall_btn:
                selectAll();
                return true;
            default:
        }
        return true;
    }


    private void reLoad(){
        XLog.d( "重新加载 activity");
        startActivity(getIntent());
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent("com.lmy.iconcapturer.REFRESH_DATA"));
        XLog.d( "Destroy edit activity!");
    }
}