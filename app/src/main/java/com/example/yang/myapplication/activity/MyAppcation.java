package com.example.yang.myapplication.activity;

import android.app.Application;

public class MyAppcation extends Application {

    private static MyAppcation instance;

    public static MyAppcation getInstance(){
        return instance;
    }

    public void onCreate() {
        super.onCreate();
        instance = this;
    }

}
