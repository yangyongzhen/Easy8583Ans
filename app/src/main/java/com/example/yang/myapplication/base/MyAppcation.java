package com.example.yang.myapplication.base;

import android.app.Application;
import android.content.Context;

public class MyAppcation extends Application {

    private static MyAppcation instance;
    public Context context;
    public static MyAppcation getInstance(){
        return instance;
    }

    public void onCreate() {
        super.onCreate();
        instance = this;
        context = getApplicationContext();
    }

}
