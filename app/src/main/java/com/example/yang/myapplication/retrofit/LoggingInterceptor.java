package com.example.yang.myapplication.retrofit;

import android.util.Log;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class LoggingInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        //if (BuildConfig.DEBUG) {
            Log.i("LOG",String.format("发送请求 %s on %s%n%s",
                    request.url(), chain.connection(), request.headers()));
        return chain.proceed(request);
        }
}

