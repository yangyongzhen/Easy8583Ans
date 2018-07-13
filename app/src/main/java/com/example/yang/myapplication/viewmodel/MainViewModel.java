package com.example.yang.myapplication.viewmodel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.yang.myapplication.R;
import com.example.yang.myapplication.base.MyApplication;
import com.example.yang.myapplication.bean.mvvm.MainBean;
import com.example.yang.myapplication.database.Album;
import com.example.yang.myapplication.ilistener.MainListener;
import com.example.yang.myapplication.model.MainModel;
import com.example.yang.myapplication.model.MainModelImpl;
import com.example.yang.myapplication.rxbus.RxBus;

import org.litepal.LitePal;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainViewModel implements MainListener {

    private MainModel mainModel;
    private MainBean mainBean;
    private Context context;
    public MainViewModel(MainBean bean){
        mainModel = new MainModelImpl(this);
        mainBean = bean;
        context = MyApplication.getInstance();
    }

    public void initRxbus(ArrayMap<Object, Observable> observables) {
        mainModel.initRxbus(observables);
    }

    @Override
    public void setViewLog(String log) {
        mainBean.log_txt.set(log);
    }

    public void mainOnClick(View view) {
        switch (view.getId()) {
            case R.id.button1:{
                Toast.makeText(context,"btn1 clicked", Toast.LENGTH_SHORT).show();
                //RxBus.getInstance().post(new EventMsg<String>(Constant.ON, "\n来自btn5的消息！\n"));
                Observable.just("Hello")
                        .subscribeOn(Schedulers.newThread())//指定：在新的线程中发起
                        .observeOn(Schedulers.io())         //指定：在io线程中处理
                        .map(new Function<String, String>() {
                            @Override
                            public String apply(String s) throws Exception {
                                Thread.sleep(3000);
                                return s+"延时3s";
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())//指定：在主线程中处理
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String inf) {
                                //消费事件
                                Toast.makeText(context, "结果:", Toast.LENGTH_LONG).show();
                                // edt_log.setText(strlog.toString());
                                Log.d("TAG",inf);
                            }
                        });
            }
            break;
            case R.id.button2:{
                //RxBus.getInstance().post(new EventMsg<String>(Constant.LOG, "hello btn2"));
                mainModel.postQdData();
            }
            break;

            case R.id.button3:{
                //RxBus.getInstance().post(new EventMsg<String>(Constant.LOG, "hello btn2"));
                SQLiteDatabase db = LitePal.getDatabase();
                Album album = new Album();
                album.setName("album2");
                album.setPrice(10.99f);
                //album.setCover(getCoverImageBytes());
                album.save();
                Album falbum = LitePal.find(Album.class, 1);
                Log.d("TAG",falbum.getName());
                falbum = LitePal.findFirst(Album.class);
            }
            break;
            case R.id.button4:{
                String str = "rxbus demo...";
                RxBus.get().post("test",str);
            }
            break;
            case R.id.button5:{
                //RxBus.getInstance().post(new EventMsg<String>(Constant.LOG, "hello btn2"));
                mainModel.postBatchData();
            }
            break;

        }
    }
}
