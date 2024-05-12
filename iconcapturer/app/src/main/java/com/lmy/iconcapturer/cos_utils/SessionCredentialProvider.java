package com.lmy.iconcapturer.cos_utils;

import android.content.Context;

import com.tencent.cloud.Response;
import com.tencent.qcloud.core.auth.BasicLifecycleCredentialProvider;
import com.tencent.qcloud.core.auth.QCloudLifecycleCredentials;
import com.tencent.qcloud.core.auth.SessionQCloudCredentials;
import com.tencent.qcloud.core.common.QCloudClientException;

public class SessionCredentialProvider
        extends BasicLifecycleCredentialProvider {

    private Context context;
    public SessionCredentialProvider(Context context){
        this.context = context;
    }

    @Override
    protected QCloudLifecycleCredentials fetchNewCredentials()
            throws QCloudClientException {

        // 首先从您的临时密钥服务器获取包含了密钥信息的响应
        Response response = com.lmy.iconcapturer.utils.Utils.requestForTmpKey(this.context);

        // 释放context
        this.context = null;

        if (response == null) {
            return null;
        }
        System.out.println(response.credentials.tmpSecretId);
        System.out.println(response.credentials.tmpSecretKey);
        System.out.println(response.credentials.sessionToken);
        // 然后解析响应，获取临时密钥信息
        String tmpSecretId = response.credentials.tmpSecretId; // 临时密钥 SecretId
        String tmpSecretKey = response.credentials.tmpSecretKey; // 临时密钥 SecretKey
        String sessionToken = response.credentials.sessionToken; // 临时密钥 Token
        long expiredTime = response.expiredTime;//临时密钥有效截止时间戳，单位是秒

        //建议返回服务器时间作为签名的开始时间，避免由于用户手机本地时间偏差过大导致请求过期
        // 返回服务器时间作为签名的起始时间
        long startTime = response.startTime; //临时密钥有效起始时间，单位是秒

        // 最后返回临时密钥信息对象
        return new SessionQCloudCredentials(tmpSecretId, tmpSecretKey,
                sessionToken, startTime, expiredTime);
    }

}
