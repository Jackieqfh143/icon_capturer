package com.lmy.iconcapturer.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.elvishew.xlog.XLog;
import com.lmy.iconcapturer.R;
import com.lmy.iconcapturer.ShotApplication;
import com.lmy.iconcapturer.bean.PlainImageBuffer;
import com.lmy.iconcapturer.utils.AnimatedGifEncoder;
import com.lmy.iconcapturer.utils.ConfigUtil;
import com.lmy.iconcapturer.utils.DAOHandler;
import com.lmy.iconcapturer.utils.IconDetecter;
import com.lmy.iconcapturer.utils.ThreadPoolManager;
import com.lmy.iconcapturer.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

public class CaptureScreenService extends Service
{
    private static final String TAG = "qfh";

    private MediaProjection mMediaProjection = null;
    private VirtualDisplay mVirtualDisplay = null;

    public static int mResultCode = 0;
    public static Intent mResultData = null;
    public static MediaProjectionManager mMediaProjectionManager1 = null;

    private WindowManager mWindowManager1 = null;
    private int windowWidth = 0;
    private int windowHeight = 0;
    private ImageReader mImageReader = null;
    private DisplayMetrics metrics = null;
    private int mScreenDensity = 0;

    private IconCaptureBinder iconCaptureBinder = new IconCaptureBinder();

    private IconDetecter iconDetecter = null;

    private BroadcastReceiver broadcastReceiver;

    private ArrayList<Bitmap> bitmapArrayList;

    private Thread bitmapWorker = null;

    private int totalIcon = 0;
    private int successIcon = 0;

    private boolean isRunning = false;

    class IconCaptureBinder extends Binder{
        @RequiresApi(api = Build.VERSION_CODES.Q)
        public void startCapture(){
            Handler handler1 = new Handler();
            handler1.post(new Runnable() {
                public void run() {
                    //start virtual
                    startVirtual();
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onCreate()
    {
        // TODO Auto-generated method stub
        super.onCreate();
        XLog.i( "Create capture screen service");
        // 设置前台服务通知
        createNotificationChannel();
        createVirtualEnvironment();
        Notification notification = createNotification("");

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.lmy.iconcapture.BITMAP_ARRAY_READY".equals(intent.getAction())){
                    onBitmapArrayReady();
                }else if ("com.lmy.iconcapture.RESET_DATA".equals(intent.getAction())){
                    XLog.d( "get reset data call");
                    clearData();
                }
            }
        };

        IntentFilter filter = new IntentFilter("com.lmy.iconcapture.BITMAP_ARRAY_READY");
        filter.addAction("com.lmy.iconcapture.RESET_DATA");
        registerReceiver(broadcastReceiver, filter);
        startForeground(Utils.NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
    }

    private void clearData(){
        bitmapWorker = null;
        bitmapArrayList.clear();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return iconCaptureBinder;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        XLog.i( "Start capture screen service");
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(Utils.CHANNEL_ID, "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            XLog.i( "CreateNotificationChannel");
        }
    }

    private Notification createNotification(String text) {
        if (text.isEmpty()){
            text = "正在运行...";
        }
        // 创建并配置通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Utils.CHANNEL_ID)
                .setContentTitle("表情包猎手")
                .setContentText(text)
                .setSmallIcon(R.drawable.logo_obj)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        XLog.i( "CreateNotification");
        return builder.build();
    }

    @SuppressLint({"WrongConstant", "SimpleDateFormat"})
    private void createVirtualEnvironment(){
        mWindowManager1 = (WindowManager)getApplication().getSystemService(Context.WINDOW_SERVICE);
        windowWidth = mWindowManager1.getDefaultDisplay().getWidth();
        windowHeight = mWindowManager1.getDefaultDisplay().getHeight();
        metrics = new DisplayMetrics();
        mWindowManager1.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mImageReader = ImageReader.newInstance(windowWidth, windowHeight, 0x1, 2);  //ImageFormat.RGB_565
        mImageReader.setOnImageAvailableListener(
                new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader) {
//                        XLog.i( "image is available");
                        if (bitmapWorker != null || bitmapArrayList.size() > 0) return;
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showText(CaptureScreenService.this, "开始检测，请勿触摸屏幕");
                                bitmapArrayList.clear();
                                bitmapWorker = getBitMapWorker();
                                bitmapWorker.start();
                                Timer timer = new Timer();
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        if (bitmapWorker != null && !bitmapWorker.isInterrupted()){
                                            XLog.i( "bitmapArrayList.size(): " + bitmapArrayList.size());
                                            bitmapWorker.interrupt();
                                        }
                                        timer.cancel();
                                    }
                                }, new Date(System.currentTimeMillis() + 4000));
                            }
                        }, 1000);
                    }
                }, null);
        iconDetecter = new IconDetecter();
        bitmapArrayList = new ArrayList<>();
        XLog.i( "prepared the virtual environment");
    }

    public void startVirtual(){
        XLog.i( "start screen capture intent");
        XLog.i( "want to build media projection and display virtual");
        setUpMediaProjection();
    }

    public void setUpMediaProjection(){
        mResultData = ((ShotApplication)getApplication()).getIntent();
        mResultCode = ((ShotApplication)getApplication()).getResult();
        XLog.i( "mResultData address: " + System.identityHashCode(mResultData));
        if (mResultData == null || mResultCode == 0){
            mMediaProjection = null;
            XLog.e( "user refuse to get screen!");
            onTaskFinished(-1);
            return;
        }
        mMediaProjectionManager1 = ((ShotApplication)getApplication()).getMediaProjectionManager();
        if (mMediaProjectionManager1 == null){
            mMediaProjection = null;
            XLog.e( "mMediaProjectionManager1 is null");
            onTaskFinished(-1);
            return;
        }
        mMediaProjection = mMediaProjectionManager1.getMediaProjection(mResultCode, mResultData);
        XLog.i( "create new mMediaProjection");
        clearData();
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                virtualDisplay();
            }
        });
    }

    private void virtualDisplay(){
        if (mMediaProjection != null){
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                    windowWidth, windowHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mImageReader.getSurface(), null, null);
            XLog.i( "virtual displayed");
            if (mVirtualDisplay == null){
                XLog.i( "mVirtualDisplay is null");
                onTaskFinished(-1);
            }else{
                try{
                    Image image = mImageReader.acquireLatestImage();
                    if (image != null){
                        image.close();
                    }
                }catch (Exception e){
                    XLog.e(e);
                }
            }

        }else{
            XLog.i( "mMediaProjection is null");
            onTaskFinished(-1);
        }
    }

    private Bitmap img2Bitmap(Image image, boolean compress){
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width+rowPadding/pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0,width, height);
        //压缩图像
        if (compress){
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outputStream);
            Bitmap compressBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(outputStream.toByteArray()));
            
            return compressBitmap;
        }
       
        return bitmap;
    }

    private Thread getBitMapWorker(){
        Thread threadLocal = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()){
                    try{
                        Image image = mImageReader.acquireNextImage();
                        if (image != null) {
//                            Log.d( "Capture the No. frame " + bitmapArrayList.size() + " image");
                            //用于检测表情包的首帧画面不压缩
                            Bitmap bitmap = img2Bitmap(image, bitmapArrayList.size() != 3);
                            image.close();
                            bitmapArrayList.add(bitmap);

                            if (bitmapArrayList.size() == 10){
                                sendBroadcast(new Intent("com.lmy.iconcapture.BITMAP_ARRAY_READY"));
                            }
                            if (bitmapArrayList.size() >= 75){
                                break;
                            }
                        }
                    }catch (Exception e){
                        XLog.e(e);
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });

        return threadLocal;
    }

    private void debugSaveImage(Bitmap bitmap){
        String imgName = "debug" + DAOHandler.getTimeStr();
        String folder_name = Environment.getExternalStorageDirectory().getPath()+"/Pictures/";
        imgName += ".png";
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
            sendBroadcast(media);
        } catch (Exception e) {
            XLog.e(e);
        }
    }
    
    private long getFrameDiff(Bitmap bitmap1, Bitmap bitmap2){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap1.compress(Bitmap.CompressFormat.PNG, 50, stream);
        byte[] A = stream.toByteArray();

        ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
        bitmap2.compress(Bitmap.CompressFormat.PNG, 50, stream1);
        byte[] B = stream1.toByteArray();
//        Log.i( "A length " + A.length);
//        Log.i( "B length " + B.length);
        //        int length = Math.min(A.length, B.length);
//        for (int i = 0; i < length; i++) {
//            diff_AB += Math.abs(A[i] - B[i]);
//        }
//        Log.i( "diff_AB " + diff_AB);
        return A.length == B.length ? 0 : 1;
    }

    private boolean isGif(int[] rect){
        if (bitmapArrayList.size() > 10){
            Random r = new Random();
            int randId = r.nextInt(bitmapArrayList.size() / 3) + bitmapArrayList.size() / 3;
            XLog.i( "Pick the No. " + randId + "frame");
            Bitmap tmpBitmap = Bitmap.createBitmap(bitmapArrayList.get(randId),rect[0], rect[1], rect[2], rect[3]);
            Bitmap tmpBitmap1 = Bitmap.createBitmap(bitmapArrayList.get(randId + 2), rect[0], rect[1], rect[2], rect[3]);
            Bitmap tmpBitmap2 = Bitmap.createBitmap(bitmapArrayList.get(randId - 2), rect[0], rect[1], rect[2], rect[3]);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            tmpBitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            byte[] B = stream.toByteArray();
            tmpBitmap.recycle();

            ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
            tmpBitmap1.compress(Bitmap.CompressFormat.PNG, 50, stream1);
            byte[] C = stream1.toByteArray();
            tmpBitmap1.recycle();

            ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
            tmpBitmap2.compress(Bitmap.CompressFormat.PNG, 50, stream2);
            byte[] A = stream2.toByteArray();
            tmpBitmap2.recycle();
            XLog.i( "A length " + A.length);
            XLog.i( "B length " + B.length);
            XLog.i( "C length " + C.length);

            long diff_AB = 0;
            long diff_AC = 0;
            long diff_BC = 0;
            int length = Math.min(Math.min(A.length, B.length), C.length);
            for (int i = 0; i < length; i++) {
                diff_AB += Math.abs(A[i] - B[i]);
                diff_AC += Math.abs(A[i] - C[i]);
                diff_BC += Math.abs(B[i] - C[i]);
                if (diff_AB > 1000 || diff_BC > 1000 || diff_AC > 1000 || i >= length / 2){
                    break;
                }
            }
            XLog.i( "diff_AB " + diff_AB);
            XLog.i( "diff_BC " + diff_BC);
            XLog.i( "diff_AC " + diff_AC);

            return diff_AB > 0 || diff_BC > 0 || diff_AC > 0;
        }

        return false;
    }

    private byte[] saveAsGif(int[] rect){
        XLog.i( "bitmapArrayList.size(): " + bitmapArrayList.size());
        if (bitmapArrayList.size() > 1){
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            AnimatedGifEncoder encoder = new AnimatedGifEncoder();
//            int fps = 15;
            int fps = ConfigUtil.getGifFrameRate();
            // 设置gif的帧率为24帧每秒
            encoder.setFrameRate(fps);
            encoder.start(bos);

            Bitmap tmpBitmap = Bitmap.createBitmap(bitmapArrayList.get(4),rect[0], rect[1], rect[2], rect[3]);
            tmpBitmap = getScaledBitmap(tmpBitmap, 250);
            int targetWidth = tmpBitmap.getWidth();
            int targetHeight = tmpBitmap.getHeight();
            XLog.i( "scaledBitmap width: " + tmpBitmap.getWidth());
            XLog.i( "scaledBitmap Height: "+ tmpBitmap.getHeight());
            
            Bitmap firstFrame = null;
            for (int i = 4; i < bitmapArrayList.size(); i++) {
                Bitmap bitmap = bitmapArrayList.get(i);
                tmpBitmap = Bitmap.createBitmap(bitmap,rect[0], rect[1], rect[2], rect[3]);
                if (tmpBitmap.getWidth() != targetWidth || tmpBitmap.getHeight() != targetHeight){
                    tmpBitmap = Bitmap.createScaledBitmap(tmpBitmap, targetWidth, targetHeight, true);
                }
                
                if (i == 4){
                    firstFrame = Bitmap.createBitmap(tmpBitmap);
                }else{
                    long frameDiff = getFrameDiff(firstFrame, tmpBitmap);
                    encoder.addFrame(tmpBitmap);
                    if (frameDiff == 0) break;
                }
            }
            encoder.finish();
            return bos.toByteArray();
        }

        return new byte[0];
    }

    private Bitmap getScaledBitmap(Bitmap bitmap, int targetSize){
        float scaledFactor = (float) targetSize / Math.min(bitmap.getWidth(), bitmap.getHeight());
        Matrix matrix = new Matrix();
        matrix.postScale(scaledFactor, scaledFactor);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private Thread getSaveImgWorker(PlainImageBuffer plainImageBuffer){
        Thread threadLocal = new Thread(new Runnable() {
            @Override
            public void run() {
                // 生成一个随机的 UUID
                String uuid = UUID.randomUUID().toString();
                String timeStr = DAOHandler.getTimeStr();
                String imgName = timeStr + "_" + uuid + ".png";
                long save_time = DAOHandler.getTimeSecs();
                File fileImage = null;
                Bitmap tmpBitmap = null;
                boolean is_gif = false;
                if (isGif(plainImageBuffer.rect)){
                    XLog.i( "is a gif");
                    try{
                        if (bitmapWorker != null){
                            bitmapWorker.join();
                        }
                    }catch (Exception e){
                        XLog.e(e);
                    }
                    byte[] gifDatas = saveAsGif(plainImageBuffer.rect);
                    if (gifDatas.length > 0){
                        imgName = imgName.replace(".png", ".gif");
                        fileImage  = DAOHandler.saveGif(gifDatas, getApplicationContext(), imgName);
                    }
//                    try{
//                        imgName = imgName.replace(".png", ".gif");
//                        fileImage = saveGif(plainImageBuffer.rect, imgName);
//                    }catch (Exception e){
//                        XLog.e(e);
//                    }
                    is_gif = true;
                }else{
                    XLog.i( "is not a gif");
                    tmpBitmap = Utils.byteArray2Bitmap(plainImageBuffer.imgData, plainImageBuffer.width, plainImageBuffer.height,
                            Bitmap.Config.ARGB_8888.name());
                    fileImage = DAOHandler.saveBitmap(tmpBitmap, getApplicationContext(), imgName);

                    //额外保存一个略缩图
                    Bitmap smallBitmap = Bitmap.createScaledBitmap(tmpBitmap, tmpBitmap.getWidth() / 10,
                            tmpBitmap.getHeight() / 10, false);
                    XLog.i( "smallBitmap Width " + smallBitmap.getWidth());
                    XLog.i( "smallBitmap Height " + smallBitmap.getHeight());
                    DAOHandler.saveBitmap(smallBitmap, getApplicationContext(), imgName.replace(".png", "_small.png"));
                }

                if (fileImage != null){
                    Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(fileImage);
                    media.setData(contentUri);
                    sendBroadcast(media);
                    //添加到数据库
                    if (is_gif){
                        DAOHandler.addGifIcon(plainImageBuffer.rect,fileImage.getPath(),timeStr,uuid, save_time);
                        //压缩gif
//                        gifCompress(fileImage, uuid);
                    }else{
                        DAOHandler.addIcon(tmpBitmap, fileImage.getPath(),timeStr,uuid, save_time);
                    }

                    synchronized (CaptureScreenService.class){
                        XLog.d( "Current Thread ID " + Thread.currentThread().getName());
                        successIcon++;
                    }
                }else{
                    XLog.d( "表情包保存失败");
                }
            }
        });

        return threadLocal;
    }

    private void startCaptureIcon(Bitmap bitmap){
        successIcon = 0;
        totalIcon = 0;
        if (mVirtualDisplay == null){
            onTaskFinished(-1);
            return;
        }

        float[] configs = new float[]{ConfigUtil.getMaxImageHeightRatio(), ConfigUtil.getMaxImageWidthRatio(),
                ConfigUtil.getMinImageHeightRatio(), ConfigUtil.getMinImageWidthRatio(),
                ConfigUtil.getMinImageScaleRatio(), (float) ConfigUtil.getCaptureType().ordinal()};
        iconDetecter.initConfig(configs);
        XLog.i( "image data captured");
        XLog.i( "input image width: " + bitmap.getWidth());
        XLog.i( "input image height: " + bitmap.getHeight());

        if (iconDetecter != null){
            byte[] imgData = Utils.bitmap2ByteArray(bitmap);
            PlainImageBuffer[] resData = iconDetecter.getIconStandard(imgData,bitmap.getWidth(), bitmap.getHeight(), 4);
//            debugSaveImage(bitmap);
            if (resData.length == 0){
                onTaskFinished(0);
                return;
            }

//            showText("检测到 " + resData.length + "个表情包，正在提取请稍等");

            totalIcon = resData.length;

            ThreadPoolExecutor executor = ThreadPoolManager.getThreadPool();

            for (PlainImageBuffer plainImageBuffer : resData) {
                XLog.i( "plainImageBuffer.rect[0]: " + plainImageBuffer.rect[0]);
                XLog.i( "plainImageBuffer.rect[1]: " + plainImageBuffer.rect[1]);
                XLog.i( "plainImageBuffer.rect[2]: " + plainImageBuffer.rect[2]);
                XLog.i( "plainImageBuffer.rect[3]: " + plainImageBuffer.rect[3]);

                //执行保存图像的线程
                Thread thread = getSaveImgWorker(plainImageBuffer);
                executor.execute(thread);
            }

            //终止线程池
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
            XLog.d( "Finish all thread.");
//            String text = String.format(Locale.CHINESE, "捕获到 %d 个表情包，成功 %d，失败 %d",totalIcon, successIcon, totalIcon - successIcon);
//            showText(text);

            onTaskFinished(1);
        }
    }

    private void onBitmapArrayReady(){
        XLog.i( "Bitmap array is ready.");
        if (bitmapArrayList.size() < 3) return;
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
//                Bitmap bitmap = bitmapArrayList.get(bitmapArrayList.size() - 1);
                //取不压缩的首帧画面进行表情包检测
                Bitmap bitmap = bitmapArrayList.get(3);
                startCaptureIcon(bitmap);
            }
        });
    }

    private void onTaskFinished(int statusCode){
        if (bitmapWorker != null){
            Intent broadcastIntent = new Intent("com.lmy.iconcapturer.TASK_COMPLETED");
            broadcastIntent.putExtra("statusCode", statusCode);
            broadcastIntent.putExtra("total", totalIcon);
            broadcastIntent.putExtra("success", successIcon);
            sendBroadcast(broadcastIntent);
            stopVirtual();
            tearDownMediaProjection();
        }
    }

    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        if (bitmapWorker != null){
            bitmapWorker = null;
        }

        XLog.i("mMediaProjection undefined");
    }

    private void stopVirtual() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
        XLog.i("virtual display stopped");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        XLog.i( "Capture screen service unbinded.");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy()
    {
        stopVirtual();
        unregisterReceiver(broadcastReceiver);
        stopForeground(true);
        tearDownMediaProjection();
        XLog.i( "Capture screen service destroy");
    }


}