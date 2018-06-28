package com.example.yang.myapplication;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
public class OkHttp3Utils {

    private static OkHttpClient okHttpClient = null;
    private static final int DEFAULT_TIMEOUT = 30;
    private static Context mContext;

    public  static OkHttpClient getOkHttpSingletonInstance( Context ct) {
        mContext = ct;
        if (okHttpClient == null) {
            synchronized (OkHttpClient.class) {
                if (okHttpClient == null) {
                    okHttpClient = new OkHttpClient();
                    //设置合理的超时
                    OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder()
                            .readTimeout(3, TimeUnit.SECONDS)
                            .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS) //设置连接超时 30秒
                            .writeTimeout(3, TimeUnit.MINUTES)
                            //.addInterceptor(new LoggingInterceptor())//添加请求拦截
                            .addNetworkInterceptor(
                                    new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
                            .hostnameVerifier(new HostnameVerifier() {
                                @Override
                                public boolean verify(String hostname, SSLSession session) {
                                    return true;
                                }
                            })
                            //.sslSocketFactory(sslParams.sSLSocketFactory,sslParams.trustManager)
                            .retryOnConnectionFailure(true);
                    //InputStream in = new InputStream()
                    try {
                        //AssetManager assetManager =  AssetManager.class.newInstance();
                        InputStream is = MyAppcation.getInstance().getAssets().open("UP.pem");
                        OkHttp3Utils.setCertificates( httpBuilder,is);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    okHttpClient = httpBuilder.build();

                }
            }
        }
        return okHttpClient;
    }

    /**
     * 通过okhttpClient来设置证书
     * @param clientBuilder OKhttpClient.builder
     * @param certificates 读取证书的InputStream
     */
    public static void setCertificates(OkHttpClient.Builder clientBuilder, InputStream... certificates) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory
                        .generateCertificate(certificate));
                try {
                    if (certificate != null)
                        certificate.close();
                } catch (IOException e) {
                }
            }
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            clientBuilder.sslSocketFactory(sslSocketFactory,trustManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
