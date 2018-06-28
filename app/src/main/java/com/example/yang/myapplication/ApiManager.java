package com.example.yang.myapplication;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiManager {

    @GET("/ajax.php?a=fy&f=auto&t=auto&w=hello%20world")
    Call<LoginResult> getCall();

    @GET("login/")
    Call<LoginResult> getData(@Query("name") String name, @Query("password") String pw);

    @Headers({"User-Agent: Donjin Http 0.1","Cache-Control: no-cache","Content-Type: x-ISO-TPDU/x-auth","Accept-Encoding: *"})//需要添加头
   // @Multipart
    @POST("/")
    Call<LoginResult> postData(@Body  RequestBody bodyhex);
}
