package com.example.yang.myapplication.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.ArrayMap;

import com.example.yang.myapplication.rxbus.RxBus;

import java.util.Map;

import io.reactivex.Observable;

public abstract class BaseActivity extends FragmentActivity {

    protected Context context;
    protected MyApplication myApplication;
    protected ArrayMap<Object, Observable> observables;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        if (getLayResId() > 0) {
            setContentView(getLayResId());
        }
        initView();
        registerRxbus();
        myApplication = (MyApplication)getApplication();
    }
    /**
     * 获取layout资源文件id;
     *
     * @return
     */
    protected abstract int getLayResId();

    protected void initView() {
    }
    protected void registerRxbus() {
        observables = new ArrayMap<>();
    }
    protected void unRegisterRxBus() {
        if (observables != null && observables.size() > 0) {
            for (Map.Entry<Object, Observable> entity : observables.entrySet()) {
                if (entity != null) {
                    RxBus.get().unregister(entity.getKey(), entity.getValue());
                }
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterRxBus();
    }
}
