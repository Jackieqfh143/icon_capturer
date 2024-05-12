package com.lmy.iconcapturer.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.elvishew.xlog.XLog;
import com.flyjingfish.openimagelib.OpenImage;
import com.flyjingfish.openimagelib.transformers.ScaleInTransformer;
import com.lmy.iconcapturer.R;
import com.lmy.iconcapturer.bean.ImageEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HelpAdapter extends RecyclerView.Adapter<HelpAdapter.ViewHolder>{

    private Context mContext;

    private List<String> itemList;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            imageView = (ImageView) itemView.findViewById(R.id.help_imageview);
        }
    }

    public void getHelpImgs() throws Exception {
        AssetManager assetManager = mContext.getAssets();
        String[] folders = assetManager.list("app_help");
        if (folders == null || folders.length == 0) {
            throw new Exception("unable to find help files");
        }
        Arrays.sort(folders);
        String folderName = folders[folders.length - 1];
        File targetFolder = new File(mContext.getFilesDir(),"app_help" + File.separator + folderName);
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
            String[] helpFileNames = assetManager.list("app_help/" + folderName);
            if (helpFileNames == null || helpFileNames.length == 0) {
                throw new Exception("unable to find help files");
            }
            for (String fileName : helpFileNames) {
                InputStream inputStream = assetManager.open("app_help/" + folderName + "/" + fileName);
                File file = new File(targetFolder, fileName);
                OutputStream outputStream = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                inputStream.close();
                outputStream.close();
                itemList.add(file.getPath());
            }

        }else{
            File[] helpFiles = targetFolder.listFiles();
            if (helpFiles == null) throw new Exception("unable to find help files");
            for (File file : helpFiles){
                itemList.add(file.getPath());
            }
        }

    }

    public HelpAdapter(Context context, List<String> itemList){
        this.itemList = itemList;
        this.mContext = context;
        try{
            getHelpImgs();
        }catch (Exception e){
            e.printStackTrace();
            Utils.showText(mContext, "帮助文件加载失败");
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.help_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onShowImage(mContext, holder, itemList);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String resPath = itemList.get(position);
        Glide.with(mContext).load(resPath).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    private void onShowImage(Context context,
                             ViewHolder holder,
                             List<String> itemList){
        int position = holder.getAdapterPosition();
        XLog.d( "current position is: " + position);
        List<ImageEntity> datas = new ArrayList<>();
        for (int i = 0; i < itemList.size(); i++){
            String resPath = itemList.get(i);
//            String imgUrl = "android.resource://" + mContext.getPackageName() + "/" + resId;;
            ImageEntity imageEntity = new ImageEntity(resPath,resPath, 0);
            datas.add(imageEntity);
        }
        OpenImage.with(context)
                .setNoneClickView()
                //点击的ImageView的ScaleType类型（如果设置不对，打开的动画效果将是错误的）
                .setSrcImageViewScaleType(ImageView.ScaleType.CENTER_CROP,true)
                //RecyclerView的数据
                .setImageUrlList(datas)
                //点击的ImageView所在数据的位置
                .setClickPosition(position)
                .setShowClose()
                .setAutoScrollScanPosition(true)
                .addPageTransformer(new ScaleInTransformer())
                .show();
    }
}
