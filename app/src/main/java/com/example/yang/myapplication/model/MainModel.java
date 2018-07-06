package com.example.yang.myapplication.model;

import android.support.v4.util.ArrayMap;

import com.example.yang.myapplication.base.BaseLoadListener;
import com.example.yang.myapplication.rxbus.EventMsg;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public interface MainModel {

    //RxBus响应
    void doRxbus(EventMsg eventMsg);

    //银联签到
    void postQdData();
}
