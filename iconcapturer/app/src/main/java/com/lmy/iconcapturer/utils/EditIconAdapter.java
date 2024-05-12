package com.lmy.iconcapturer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.elvishew.xlog.XLog;
import com.lmy.iconcapturer.R;
import com.lmy.iconcapturer.bean.IconImage;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EditIconAdapter extends RecyclerView.Adapter<EditIconAdapter.ViewHolder> {
    private final String TAG = "qfh";

    private Context mContext;

    private List<IconImage> mIconList;

    private List<IconImage> selectIconList = new ArrayList<>();

    private HashMap<Integer, ViewHolder> selectedHolders = new HashMap<>();

    public void addSelectPosition(int position){
        selectIconList.add(mIconList.get(position));
    }

    public List<IconImage> getSelectIconList() {
        return selectIconList;
    }

    public HashMap<Integer, ViewHolder> getSelectedHolders() {
        return selectedHolders;
    }

    public void clearData(){
        selectIconList.clear();
        selectedHolders.clear();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView iconImage;
        TextView iconName;

        public ViewHolder(View view){
            super(view);
            cardView = (CardView) view;
            iconImage = (ImageView) view.findViewById(R.id.edit_icon_image);
            iconName = (TextView) view.findViewById(R.id.edit_icon_name);
        }
    }

    public EditIconAdapter(List<IconImage> iconImageList){
        mIconList = iconImageList;
    }

    @NonNull
    @Override
    public EditIconAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mContext == null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.edit_icon_item, parent, false);

        final EditIconAdapter.ViewHolder holder = new EditIconAdapter.ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onImageSelected(holder);
            }
        });

//        holder.iconImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onImageSelected(holder);
//            }
//        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IconImage iconImage = mIconList.get(position);
        holder.iconName.setText(iconImage.getName());
        if (iconImage.isGif()){
            Glide.with(mContext).asGif().diskCacheStrategy(DiskCacheStrategy.RESOURCE).load(iconImage.getPath()).into(holder.iconImage);
        }else{
            Glide.with(mContext).load(iconImage.getPath()).into(holder.iconImage);
        }

        if (selectIconList.contains(mIconList.get(position))){
            ColorStateList stateList = ColorStateList.valueOf(mContext.getResources().getColor(R.color.red));
            holder.iconImage.setBackgroundTintList(stateList);
            selectedHolders.put(position, holder);
        }else{
            ColorStateList stateList = ColorStateList.valueOf(mContext.getResources().getColor(R.color.gray));
            holder.iconImage.setBackgroundTintList(stateList);
        }
    }

    @Override
    public int getItemCount() {
        return mIconList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void selectItem(ViewHolder holder){
        if (holder == null) return;
        int position = holder.getAdapterPosition();
        IconImage iconImage = mIconList.get(position);
        ColorStateList stateList = ColorStateList.valueOf(mContext.getResources().getColor(R.color.red));
        holder.iconImage.setBackgroundTintList(stateList);
        selectedHolders.put(position, holder);
        selectIconList.add(iconImage);
    }

    public void unSelectItem(ViewHolder holder){
        if (holder == null) return;
        ColorStateList stateList = ColorStateList.valueOf(mContext.getResources().getColor(R.color.gray));
        holder.iconImage.setBackgroundTintList(stateList);
        int position = holder.getAdapterPosition();
        IconImage iconImage = mIconList.get(position);
        selectIconList.remove(iconImage);
    }

    private void onImageSelected(ViewHolder holder){
        if (holder == null) return;
        int position = holder.getAdapterPosition();
        if (!selectIconList.contains(mIconList.get(position))){
            ColorStateList stateList = ColorStateList.valueOf(mContext.getResources().getColor(R.color.red));
            holder.iconImage.setBackgroundTintList(stateList);
            selectedHolders.put(position, holder);
            selectIconList.add(mIconList.get(position));
        }else{
            ColorStateList stateList = ColorStateList.valueOf(mContext.getResources().getColor(R.color.gray));
            holder.iconImage.setBackgroundTintList(stateList);
            selectedHolders.remove(position);
            selectIconList.remove(mIconList.get(position));
        }
    }

    public boolean saveImage(ViewHolder holder){
        if (holder == null) return false;
        int position = holder.getAdapterPosition();
        IconImage iconImage = mIconList.get(position);
        ByteBuffer byteBuffer;
        String imgName = iconImage.getName();
        try{
            if (iconImage.isGif()){
                imgName += ".gif";
                Drawable drawable = holder.iconImage.getDrawable();
                GifDrawable gifDrawable = (GifDrawable) drawable;
                byteBuffer = gifDrawable.getBuffer();
                saveImgToGallery(byteBuffer, imgName);
            }else {
                imgName += ".png";
                Bitmap bitmap = ((BitmapDrawable) holder.iconImage.getDrawable()).getBitmap();
                saveBitmap(bitmap, imgName);
            }
        }catch (Exception e){
            XLog.e(e);
            return false;
        }
        return true;
    }

    private void saveBitmap(Bitmap bitmap, String imgName) throws Exception{
        File dest = new File(FileUtil.AlbumFolder, imgName);
        FileOutputStream out = new FileOutputStream(dest);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.flush();
        out.close();
        XLog.i( "image saved");

        Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(dest);
        media.setData(contentUri);
        mContext.sendBroadcast(media);
    }

    private void saveImgToGallery(ByteBuffer byteBuffer, String imgName) throws Exception{
        byte[] bytes = new byte[byteBuffer.capacity()];
        byteBuffer.rewind();
        byteBuffer.get(bytes);
        File dest = new File(FileUtil.AlbumFolder, imgName);
        FileOutputStream out = new FileOutputStream(dest);
        out.write(bytes);
        out.flush();
        out.close();
        XLog.i( "image saved");

        Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(dest);
        media.setData(contentUri);
        mContext.sendBroadcast(media);
    }

}
