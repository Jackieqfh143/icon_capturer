package com.lmy.iconcapturer.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.elvishew.xlog.XLog;
import com.lmy.iconcapturer.bean.IconImage;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DAOHandler {

    private static final String saveFolder = "images";
    private static final SimpleDateFormat ft = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
    private static final String TAG  = "qfh";

    public static void makeDataBase(){
        LitePal.getDatabase();
    }

    public static void addIcon(Bitmap bitmap, String savePath, String timeStr, String uuid, long save_time){
        IconImage iconImage = new IconImage();
        iconImage.setSave_time(save_time);
        iconImage.setHeight(bitmap.getHeight());
        iconImage.setWidth(bitmap.getWidth());
        iconImage.setUuid(uuid);
        String name = timeStr.replace("_", "") + "_" + uuid.substring(0,6);
        iconImage.setName(name);
        iconImage.setPath(savePath);
        iconImage.setGif(false);
        iconImage.save();

        XLog.i( "savePath " + savePath);
    }

    public static void addGifIcon(int[] rect, String savePath, String timeStr, String uuid, long save_time){
        IconImage iconImage = new IconImage();
        iconImage.setSave_time(save_time);
        iconImage.setHeight(rect[3]);
        iconImage.setWidth(rect[2]);
        iconImage.setUuid(uuid);
        String name = timeStr.replace("_", "") + "_" + uuid.substring(0,6);
        iconImage.setName(name);
        iconImage.setPath(savePath);
        iconImage.setGif(true);
        iconImage.save();

        XLog.i( "savePath " + savePath);
    }

    public static void updateGifIcon(String uuid, String savePath){
        XLog.d( "更新 gif 为压缩版本, 新保存路径为: " + savePath);
        IconImage iconImage = new IconImage();
        iconImage.setPath(savePath);
        iconImage.updateAll("uuid = ?", uuid);

        List<IconImage> iconImageList = LitePal
                .where("uuid = ?", uuid)
                .find(IconImage.class);

        if (iconImageList.size() > 0){
            XLog.d( "------------------------------------------------");
            XLog.d( "the debug save path is " + iconImageList.get(0).getPath());
            XLog.d( "------------------------------------------------");
        }
    }

    public static long getTimeSecs(){
        return System.currentTimeMillis();
    }

    public static String getTimeStr(){
        Date dNow = new Date();
        return ft.format(dNow);
    }

    private Date str2Time(String time_str){
        Date t = null;
        try{
            t = ft.parse(ft.format(time_str));
        }catch (ParseException e){
            XLog.e(e);
        }
        return t;
    }

    public static File getFile(Context context, String filename){
        File folder = new File(context.getFilesDir() + File.separator + saveFolder);
        if (!folder.exists()){
            folder.mkdirs();
        }
        return new File(folder, filename);
    }

    public static File saveBitmap(Bitmap bitmap, Context context, String filename){
        if (bitmap == null) return null;
        File dest = getFile(context, filename);
        try {
            FileOutputStream out = new FileOutputStream(dest);
            bitmap.compress(Bitmap.CompressFormat.PNG, ConfigUtil.getImageQuality(), out);
            out.flush();
            out.close();
            XLog.i( "screen image saved");
        } catch (Exception e) {
            XLog.e(e);
            return null;
        }
        return dest;
    }

    public static File saveGif(byte[] bitmapArray, Context context, String filename){
        File folder = new File(context.getFilesDir() + File.separator + saveFolder);
        if (!folder.exists()){
            folder.mkdirs();
        }
        File dest = new File(folder, filename);
        try {
            FileOutputStream outStream = new FileOutputStream(dest);
            outStream.write(bitmapArray);
            outStream.close();

            XLog.d( "GifName " + dest.toString());
        } catch(Exception e) {
            XLog.d( "gif save error ");
            XLog.e(e);
            return null;
        }

        return dest;
    }


}
