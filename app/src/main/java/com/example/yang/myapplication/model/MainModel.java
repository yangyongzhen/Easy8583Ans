package com.example.yang.myapplication.model;

import android.support.v4.util.ArrayMap;

import io.reactivex.Observable;

public interface MainModel {

    void initRxbus(ArrayMap<Object, Observable> observables);
    //银联签到
    void postQdData();
    void postBatchData();
}
