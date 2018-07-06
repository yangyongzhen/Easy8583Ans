package com.example.yang.myapplication.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;
//import android.support.v4.util.ArrayMap;

import com.example.yang.myapplication.model.MainModel;
import com.example.yang.myapplication.model.MainModelImpl;
import com.example.yang.myapplication.rxbus.Constant;
import com.example.yang.myapplication.rxbus.EventMsg;
import com.example.yang.myapplication.rxbus.RxBus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public abstract class BaseActivity extends FragmentActivity {

    //private Disposable disposable;
    //protected ArrayMap<Object, Disposable> disposables;
    protected Disposable disposable;
    protected Context context;
    protected MyAppcation sysApplication;
    protected MainModel mainModel;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        if (getLayResId() > 0) {
            setContentView(getLayResId());
        }
        //mainModel = new MainModelImpl();
        initView();
        registerRxbus();

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
        //mainmodel.initRxbus(disposable);

        disposable = RxBus.getInstance()
                .toObservable(
                        EventMsg.class, Schedulers.io(),
                        AndroidSchedulers.mainThread(),
                        new Consumer<EventMsg>() {
                            @Override
                            public void accept(EventMsg eventMsg) throws Exception {
                                //edt_log.setSelection(edt_log.getText().length(),edt_log.getText().length());
                                mainModel.doRxbus(eventMsg);
                            }
                        });
    }
    protected void unRegisterRxBus() {
        if (RxBus.getInstance().isObserver()) {
            RxBus.getInstance().unregister(disposable);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterRxBus();
    }
}
