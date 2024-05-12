package com.lmy.iconcapturer.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.elvishew.xlog.XLog;
import com.flyjingfish.openimagelib.OpenImage;
import com.flyjingfish.openimagelib.transformers.ScaleInTransformer;
import com.google.android.material.snackbar.Snackbar;
import com.lmy.iconcapturer.R;
import com.lmy.iconcapturer.bean.IconImage;
import com.lmy.iconcapturer.bean.ImageEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.ViewHolder> {

    private final String TAG = "qfh";

    private Context mContext;

    private List<IconImage> mIconList;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView iconImage;
        TextView iconName;
        ImageButton saveBtn;
        ImageButton delBtn;
        ImageButton likeBtn;
        ImageButton shareBtn;

        public ViewHolder(View view){
            super(view);
            cardView = (CardView) view;
            iconImage = (ImageView) view.findViewById(R.id.icon_image);
            iconName = (TextView) view.findViewById(R.id.icon_name);
            saveBtn = (ImageButton) view.findViewById(R.id.download_btn);
            delBtn = (ImageButton) view.findViewById(R.id.del_btn);
            likeBtn = (ImageButton) view.findViewById(R.id.like_btn);
            shareBtn = (ImageButton) view.findViewById(R.id.share_btn);
        }
    }

    public IconAdapter(List<IconImage> iconList){
        mIconList = iconList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.icon_item, parent, false);

        final ViewHolder holder = new ViewHolder(view);

        holder.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                IconImage iconImage = mIconList.get(position);

                if (iconImage.isGif()){
                    Drawable drawable = holder.iconImage.getDrawable();
                    try{
                        GifDrawable gifDrawable = (GifDrawable) drawable;
                        ByteBuffer byteBuffer = gifDrawable.getBuffer();
                        saveGifToGallery(byteBuffer, iconImage.getName() + ".gif", view);
                    }catch (Exception e){
                        XLog.e(e);
                        Utils.showText(mContext, "表情包保存失败");
                    }
                }else{
                    Glide.with(mContext)
                            .asBitmap()
                            .load(iconImage.getPath())
                            .into(new SimpleTarget<Bitmap>(iconImage.getWidth(),iconImage.getHeight()) {
                                @Override
                                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                    saveImgToGallery(resource, iconImage.getName() + ".png", view);
                                }
                            });
                }
            }
        });


        holder.delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRemoveItem(holder);
            }
        });

        holder.iconImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onShowImage(mContext, holder, parent, mIconList);
            }
        });


        holder.iconImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                enterEditMode();
                return false;
            }
        });

        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                IconImage iconImage = mIconList.get(position);
                String toast = !iconImage.isLike() ? "已成功添加到收藏" : "已从收藏中移除";
                iconImage.setLike(!iconImage.isLike());
                //更新数据
                try{
                    iconImage.save();
                }catch (Exception e){
                    XLog.e(e);
                    toast = "操作失败";
                }

                Utils.showText(mContext, toast);

                //更新图标状态
//                updateLikeIcon(holder, position);

                Intent intent = new Intent("com.lmy.iconcapturer.REFRESH_DATA");
                mContext.sendBroadcast(intent);
            }

        });

        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareImage(holder);
            }
        });

        return holder;
    }

    private void updateLikeIcon(ViewHolder holder, int position){
        IconImage iconImage = mIconList.get(position);
        if (iconImage.isLike()){
            holder.likeBtn.setImageDrawable(mContext.getResources().getDrawable( R.drawable.heart_filled));
        }else{
            holder.likeBtn.setImageDrawable(mContext.getResources().getDrawable( R.drawable.heart));
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        IconImage iconImage = mIconList.get(position);
        holder.iconName.setText(iconImage.getName());
        if (iconImage.isGif()){
            Glide.with(mContext).asGif().diskCacheStrategy(DiskCacheStrategy.RESOURCE).load(iconImage.getPath()).into(holder.iconImage);
        }else{
            Glide.with(mContext).load(iconImage.getPath()).into(holder.iconImage);
        }
        updateLikeIcon(holder, position);
    }

    public void updateDataList(List<IconImage> dataNew){
        mIconList = dataNew;
        XLog.i( "mIconList size: " + mIconList.size());
    }

    @Override
    public int getItemCount() {
        return mIconList.size();
    }

    private void saveGifToGallery(ByteBuffer byteBuffer, String imgName, View view) throws Exception{
        byte[] bytes = new byte[byteBuffer.capacity()];
        byteBuffer.rewind();
        byteBuffer.get(bytes);
        String folder_name = Environment.getExternalStorageDirectory().getPath()+"/Pictures/";
        File dest = new File(folder_name, imgName);
        FileOutputStream out = new FileOutputStream(dest);
        out.write(bytes);
        out.flush();
        out.close();
        XLog.i( "gif image saved");

        Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(dest);
        media.setData(contentUri);
        mContext.sendBroadcast(media);
        Snackbar snackbar = Snackbar.make(view, "表情包已保存至相册", Snackbar.LENGTH_SHORT)
                .setAction("点击查看", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
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

    private void saveImgToGallery(Bitmap bitmap, String imgName, View view){
        String folder_name = Environment.getExternalStorageDirectory().getPath()+"/Pictures/";
        File dest = new File(folder_name, imgName);
        try {
            FileOutputStream out = new FileOutputStream(dest);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            XLog.i( "screen image saved");

            Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(dest);
            media.setData(contentUri);
            mContext.sendBroadcast(media);
            Snackbar snackbar = Snackbar.make(view, "表情包已保存至相册", Snackbar.LENGTH_SHORT)
                    .setAction("点击查看", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            });
            CoordinatorLayout.LayoutParams params=(CoordinatorLayout.LayoutParams)snackbar.getView().getLayoutParams();
            params.setAnchorId(R.id.bottom_nav_view);
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            params.anchorGravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            snackbar.getView().setLayoutParams(params);
            snackbar.show();
        } catch (Exception e) {
            XLog.e(e);
        }

    }

    private void onRemoveItem(ViewHolder holder){
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle("删除确认");
        dialog.setMessage("此操作无法撤销，确认删除该表情包吗？");
        dialog.setCancelable(true);
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int position = holder.getAdapterPosition();
                Intent intent = new Intent("com.lmy.iconcapturer.DELICON");
                intent.putExtra("position", position);
                mContext.sendBroadcast(intent);
            }
        });

        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.show();
    }

    private void onShowImage(Context context,
                             ViewHolder holder, ViewGroup recyclerView,
                             List<IconImage> iconImageList){
        int position = holder.getAdapterPosition();
        XLog.d( "current position is: " + position);
        List<ImageEntity> datas = new ArrayList<>();
        for (int i = 0; i < iconImageList.size(); i++){
            IconImage iconImage = iconImageList.get(i);
            String imgUrl = iconImage.getPath();
            String smallImgUrl = imgUrl.contains(".png") ? imgUrl.replace(".png", "_small.png") : imgUrl;
            ImageEntity imageEntity = new ImageEntity(imgUrl,smallImgUrl, 0);
            datas.add(imageEntity);
        }
//        int adjustPos = position >= 1 ? position - 1 : position;
        int adjustPos = position;
        XLog.d( "adjustPos: " + adjustPos);
        OpenImage.with(context)
                //点击ImageView所在的RecyclerView（也支持设置setClickViewPager2，setClickViewPager，setClickGridView，setClickListView，setClickImageView，setClickWebView）
//                .setClickRecyclerView((RecyclerView) recyclerView,new SourceImageViewIdGet() {
//                    @Override
//                    public int getImageViewId(OpenImageUrl data, int position) {
//                        return R.id.icon_image;//点击的ImageView的Id或者切换图片后对应的ImageView的Id
//                    }
//                })
                .setNoneClickView()
                //点击的ImageView的ScaleType类型（如果设置不对，打开的动画效果将是错误的）
                .setSrcImageViewScaleType(ImageView.ScaleType.CENTER_CROP,true)
                //RecyclerView的数据
                .setImageUrlList(datas)
                //点击的ImageView所在数据的位置
                .setClickPosition(adjustPos)
                .setShowClose()
                .setAutoScrollScanPosition(true)
                .addPageTransformer(new ScaleInTransformer())
                .show();
    }

    private void shareImage(ViewHolder holder){
        XLog.e( "fileDir is: " + mContext.getFilesDir().toString());
        XLog.e( "dataDir is: " + mContext.getDataDir().toString());
        IconImage iconImage = mIconList.get(holder.getAdapterPosition());
        File file = new File(iconImage.getPath());
        Uri imageUri = FileProvider.getUriForFile(mContext, "com.lmy.iconcapturer.file.provider", file);
        // 创建分享Intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*"); // 设置MIME类型为image/*表示分享图片
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri); // 使用EXTRA_STREAM传递图片的Uri
        mContext.startActivity(Intent.createChooser(shareIntent, "表情包分享"));
    }

    private void enterEditMode(){
        XLog.d( "enterEditMode is called");
        Intent intent = new Intent("com.lmy.iconcapturer.EDIT_MODE");
        mContext.sendBroadcast(intent);
    }



}
