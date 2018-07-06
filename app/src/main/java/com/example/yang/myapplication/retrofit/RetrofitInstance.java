package com.example.yang.myapplication.retrofit;

import android.util.Log;

import com.example.yang.myapplication.base.MyAppcation;

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
import retrofit2.Retrofit;

public class RetrofitInstance {

    private static final String TAG = "RetrofitInstance";
    //日志拦截器
    private HttpLoggingInterceptor loggingInterceptor;

    private Retrofit retrofit;
    private OkHttpClient client;
    private String url="https://140.207.168.62:30000/";

    private static volatile RetrofitInstance instance = null;

    private RetrofitInstance() {
        initRetrofit(url);
    }
    public static RetrofitInstance getInstance() {
        if (instance == null) {
            synchronized (RetrofitInstance.class) {
                if (instance == null) {
                    instance = new RetrofitInstance();
                }
            }
        }
        return instance;
    }

    /**
     * 改变请求的url；
     */
    public void initRetrofit(String url) {
        this.url = url;
        try {
            loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    Log.i(TAG, message);
                }
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .hostnameVerifier(new HostnameVerifier() {
                        //不验证服务器
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });

            //加载公钥证书
            try {
                //AssetManager assetManager =  AssetManager.class.newInstance();
                InputStream is = MyAppcation.getInstance().getAssets().open("UP.pem");
                setCertificates(httpBuilder,is);
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.client =  httpBuilder.build();
            this.retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .client(client)
                    .addConverterFactory(MyConverterFactory.create())
                    //.addConverterFactory(ScalarsConverterFactory.create())
                    .build();

            Log.i(TAG, retrofit.toString());

        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
        }
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

    public Retrofit getRetrofit() {
        return retrofit;
    }
}
