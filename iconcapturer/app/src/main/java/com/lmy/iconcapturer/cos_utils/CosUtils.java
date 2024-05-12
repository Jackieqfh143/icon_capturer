package com.lmy.iconcapturer.cos_utils;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.elvishew.xlog.XLog;
import com.lmy.iconcapturer.R;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.CosXmlSimpleService;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.tag.InitiateMultipartUpload;
import com.tencent.cos.xml.transfer.COSXMLDownloadTask;
import com.tencent.cos.xml.transfer.COSXMLUploadTask;
import com.tencent.cos.xml.transfer.InitMultipleUploadListener;
import com.tencent.cos.xml.transfer.TransferConfig;
import com.tencent.cos.xml.transfer.TransferManager;
import com.tencent.cos.xml.transfer.TransferState;
import com.tencent.cos.xml.transfer.TransferStateListener;
import com.tencent.qcloud.core.auth.QCloudCredentialProvider;

import java.io.File;


public class CosUtils {

    private static QCloudCredentialProvider credentialProvider;

    private static CosXmlSimpleService cosXmlService = null;

    private static COSXMLUploadTask cosxmlUploadTask;

    private static COSXMLDownloadTask cosxmlDownloadTask;

    public static TransferState curDownloadState;

    public static void initEnv(Context context){
        if (cosXmlService == null){
            try{
                credentialProvider = new SessionCredentialProvider(context);
            }catch (Exception e){
                e.printStackTrace();
                return;
            }
            // 创建 CosXmlServiceConfig 对象，根据需要修改默认的配置参数
            CosXmlServiceConfig serviceConfig = new CosXmlServiceConfig.Builder()
                    .setRegion(context.getString(R.string.cos_region))
                    .isHttps(true) // 使用 HTTPS 请求, 默认为 HTTP 请求
                    .builder();

            // 初始化 COS Service，获取实例
            cosXmlService = new CosXmlSimpleService(context,
                    serviceConfig, credentialProvider);
        }
    }

    public static void uploadFile(Context context, String filePath, String saveName){

        //写入日志文件夹
        saveName = "logs/" + saveName;
        initEnv(context);
        if (cosXmlService == null) {
            XLog.e("连接服务器失败!");
            com.lmy.iconcapturer.utils.Utils.showText(context, "连接服务器失败");
            return;
        }
        // 初始化 TransferConfig，这里使用默认配置，如果需要定制，请参考 SDK 接口文档
        TransferConfig transferConfig = new TransferConfig.Builder()
                // 设置启用分块上传的最小对象大小 默认为2M
                .setDivisionForUpload(2097152)
                // 设置分块上传时的分块大小 默认为1M
                .setSliceSizeForUpload(1048576)
                // 设置是否强制使用简单上传, 禁止分块上传
                .setForceSimpleUpload(false)
                .build();
        // 初始化 TransferManager
        TransferManager transferManager = new TransferManager(cosXmlService,
                transferConfig);

        // 存储桶名称，由 bucketname-appid 组成，appid 必须填入，可以在 COS 控制台查看存储桶名称。 https://console.cloud.tencent.com/cos5/bucket
        //若存在初始化分块上传的 UploadId，则赋值对应的 uploadId 值用于续传；否则，赋值 null
        String uploadId = null;

        // 上传文件
        cosxmlUploadTask = transferManager.upload(context.getString(R.string.cos_bucket), saveName,
                filePath, uploadId);

        //设置初始化分块上传回调，用于获取uploadId (5.9.7版本以及后续版本支持)
        cosxmlUploadTask.setInitMultipleUploadListener(new InitMultipleUploadListener() {
            @Override
            public void onSuccess(InitiateMultipartUpload initiateMultipartUpload) {
                //用于下次续传上传的 uploadId
                String uploadId = initiateMultipartUpload.uploadId;
            }
        });
        //设置上传进度回调
        cosxmlUploadTask.setCosXmlProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long complete, long target) {
                // todo Do something to update progress...
                int progress = (int) (100 * ((double) complete / target));
//                XLog.d("上传进度: "+ progress + "%");
                Intent intent = new Intent("com.lmy.iconcapturer.UPLOAD_PROGRESS");
                intent.putExtra("progress", progress);
                context.sendBroadcast(intent);
            }
        });
        //设置返回结果回调
        cosxmlUploadTask.setCosXmlResultListener(new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                COSXMLUploadTask.COSXMLUploadTaskResult uploadResult =
                        (COSXMLUploadTask.COSXMLUploadTaskResult) result;
                XLog.d("上传成功!");
                Intent intent = new Intent("com.lmy.iconcapturer.UPLOAD_SUCCESS");
                context.sendBroadcast(intent);
            }

            // 如果您使用 kotlin 语言来调用，请注意回调方法中的异常是可空的，否则不会回调 onFail 方法，即：
            // clientException 的类型为 CosXmlClientException?，serviceException 的类型为 CosXmlServiceException?
            @Override
            public void onFail(CosXmlRequest request,
                               @Nullable CosXmlClientException clientException,
                               @Nullable CosXmlServiceException serviceException) {
                if (clientException != null) {
                    clientException.printStackTrace();
                    XLog.e("文件上传失败 " + clientException);
                } else {
                    serviceException.printStackTrace();
                    XLog.e("文件上传失败 " + serviceException);
                }
                Intent intent = new Intent("com.lmy.iconcapturer.UPLOAD_FAILED");
                intent.putExtra("msg", "连接服务器失败");
                context.sendBroadcast(intent);
            }
        });
        //设置任务状态回调, 可以查看任务过程
        cosxmlUploadTask.setTransferStateListener(new TransferStateListener() {
            @Override
            public void onStateChanged(TransferState state) {
                // todo notify transfer state
            }
        });
    }

    public static void cancelUpload(){
        cosxmlUploadTask.cancel();
    }


    public static void downloadFile(Context context, String resName, String saveDir, String saveName){
        initEnv(context);
        if (cosXmlService == null) {
            XLog.e("连接服务器失败!");
            com.lmy.iconcapturer.utils.Utils.showText(context, "连接服务器失败");
        }
        XLog.d("下载资源: " + resName);
        // 高级下载接口支持断点续传，所以会在下载前先发起 HEAD 请求获取文件信息。
        // 如果您使用的是临时密钥或者使用子账号访问，请确保权限列表中包含 HeadObject 的权限。

        // 初始化 TransferConfig，这里使用默认配置，如果需要定制，请参考 SDK 接口文档
        TransferConfig transferConfig = new TransferConfig.Builder().build();
        //初始化 TransferManager
        TransferManager transferManager = new TransferManager(cosXmlService,
                transferConfig);

        // 存储桶名称，由 bucketname-appid 组成，appid 必须填入，可以在 COS 控制台查看存储桶名称。 https://console.cloud.tencent.com/cos5/bucket
        //本地目录路径
        Context applicationContext = context.getApplicationContext(); // application
        // context
        cosxmlDownloadTask =
                transferManager.download(applicationContext,
                        context.getString(R.string.cos_bucket), resName, saveDir, saveName);

        //设置下载进度回调
        cosxmlDownloadTask.setCosXmlProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long complete, long target) {
                // todo Do something to update progress...
                int progress = (int) (100 * ((double) complete / target));
                Intent intent = new Intent("com.lmy.iconcapturer.DOWNLOAD_PROGRESS");
                intent.putExtra("progress", progress);
                context.sendBroadcast(intent);
            }
        });
        //设置返回结果回调
        cosxmlDownloadTask.setCosXmlResultListener(new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                COSXMLDownloadTask.COSXMLDownloadTaskResult downloadTaskResult =
                        (COSXMLDownloadTask.COSXMLDownloadTaskResult) result;
                XLog.d("下载完成");
                Intent intent = new Intent("com.lmy.iconcapturer.DOWNLOAD_SUCCESS");
                intent.putExtra("apkFilePath", new File(saveDir, saveName).getPath());
                context.sendBroadcast(intent);
            }

            // 如果您使用 kotlin 语言来调用，请注意回调方法中的异常是可空的，否则不会回调 onFail 方法，即：
            // clientException 的类型为 CosXmlClientException?，serviceException 的类型为 CosXmlServiceException?
            @Override
            public void onFail(CosXmlRequest request,
                               @Nullable CosXmlClientException clientException,
                               @Nullable CosXmlServiceException serviceException) {
                if (clientException != null) {
                    clientException.printStackTrace();
                    XLog.e("下载失败: ", clientException);
                } else {
                    serviceException.printStackTrace();
                    XLog.e("下载失败: ", serviceException);
                }
                Intent intent = new Intent("com.lmy.iconcapturer.DOWNLOAD_FAILED");
                context.sendBroadcast(intent);
            }
        });
        //设置任务状态回调，可以查看任务过程
        cosxmlDownloadTask.setTransferStateListener(new TransferStateListener() {
            @Override
            public void onStateChanged(TransferState state) {
                // todo notify transfer state
                curDownloadState = state;
                Intent intent = new Intent("com.lmy.iconcapturer.DOWNLOAD_STATE_UPDATE");
                context.sendBroadcast(intent);
            }
        });
    }

    public static void resumeDownload(){
        cosxmlDownloadTask.resume();
    }

    public static void pauseDownload(){
        cosxmlDownloadTask.pause(true);
    }

    public static void cancelDownload(){
        cosxmlDownloadTask.cancel(true);
    }


}
