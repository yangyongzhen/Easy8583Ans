package com.example.yang.myapplication.retrofit;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiManager {

    @Headers({"User-Agent: Donjin Http 0.1","Cache-Control: no-cache","Content-Type: x-ISO-TPDU/x-auth","Accept-Encoding: *"})//需要添加头
    @POST("/")
    Call<byte[]> postData(@Body  RequestBody bodyhex);
}
