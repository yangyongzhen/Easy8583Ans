package com.example.yang.myapplication.base;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

public class MyApplication extends Application {

    private static MyApplication instance;
    public Context context;
    public static MyApplication getInstance(){
        return instance;
    }

    public void onCreate() {
        super.onCreate();
        instance = this;
        context = getApplicationContext();
        LitePal.initialize(this);
    }

}
