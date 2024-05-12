package com.lmy.iconcapturer.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.flyjingfish.openimagelib.OpenImage;
import com.flyjingfish.openimagelib.transformers.ScaleInTransformer;
import com.lmy.iconcapturer.R;
import com.lmy.iconcapturer.bean.FileItem;
import com.lmy.iconcapturer.bean.ImageEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private Context mContext;

    private List<FileItem> mFileList;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView fileImage;
        TextView fileName;
        ImageButton delBtn;

        public ViewHolder(View view){
            super(view);
            cardView = (CardView) view;
            fileImage = (ImageView) view.findViewById(R.id.file_imageview);
            fileName = (TextView) view.findViewById(R.id.file_name_textview);
            delBtn = (ImageButton) view.findViewById(R.id.file_del_btn);
        }
    }

    public FileAdapter(List<FileItem> fileList){
        mFileList = fileList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.file_item, parent, false);

        final ViewHolder holder = new ViewHolder(view);

        holder.delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRemoveItem(holder);
            }
        });

        holder.fileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onShowImage(mContext, holder);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FileItem fileItem = mFileList.get(position);
        holder.fileName.setText(fileItem.getName());
        if (fileItem.getShowImagePath() != null){
            Glide.with(mContext).load(Uri.fromFile(new File(fileItem.getShowImagePath()))).into(holder.fileImage);
        }else if (fileItem.getType().equals(FileItem.FileType.TXT) || fileItem.getType().equals(FileItem.FileType.COMPRESS)){
            Glide.with(mContext).load(mContext.getDrawable(R.drawable.file_clip)).into(holder.fileImage);
        }
    }

    @Override
    public int getItemCount() {
        return mFileList.size();
    }

    private void onRemoveItem(ViewHolder holder){
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle("删除确认");
        dialog.setMessage("确认删除该附件吗？");
        dialog.setCancelable(true);
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent("com.lmy.iconcapturer.FILE_DEL");
                int position = holder.getAdapterPosition();
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
                             ViewHolder holder){

        int position = holder.getAdapterPosition();
        List<ImageEntity> datas = new ArrayList<>();
        FileItem fileItem = mFileList.get(position);

        if (!fileItem.getType().equals(FileItem.FileType.VIDEO) &&
        !fileItem.getType().equals(FileItem.FileType.IMAGE)){
            return;
        }
        ImageEntity imageEntity = new ImageEntity(fileItem.getPath(),fileItem.getPath(), 0);
        datas.add(imageEntity);

        OpenImage.with(context)
                .setNoneClickView()
                //点击的ImageView的ScaleType类型（如果设置不对，打开的动画效果将是错误的）
                .setSrcImageViewScaleType(ImageView.ScaleType.CENTER_CROP,true)
                //RecyclerView的数据
                .setImageUrlList(datas)
                //点击的ImageView所在数据的位置
                .setClickPosition(0)
                .setShowClose()
                .setAutoScrollScanPosition(true)
                .addPageTransformer(new ScaleInTransformer())
                .show();
    }

}
