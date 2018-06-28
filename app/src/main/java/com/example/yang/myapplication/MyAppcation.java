package com.example.yang.myapplication;

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
