package com.example.yang.myapplication.model;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import com.example.yang.myapplication.ans8583.My8583Ans;
import com.example.yang.myapplication.base.MyApplication;
import com.example.yang.myapplication.ilistener.MainListener;
import com.example.yang.myapplication.retrofit.ApiManager;
import com.example.yang.myapplication.retrofit.DataBean;
import com.example.yang.myapplication.retrofit.RetrofitInstance;
import com.example.yang.myapplication.rxbus.RxBus;
import com.example.yang.myapplication.utils.MyUtil;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static java.lang.System.arraycopy;

public class MainModelImpl implements MainModel {

    protected Context context = MyApplication.getInstance();
    private MainListener listener;
    private Observable<String> testObservable;

    Retrofit retrofit;
    ApiManager apiService ;
    final My8583Ans myans = new My8583Ans();

    StringBuffer strmsg = new StringBuffer();

    public MainModelImpl(MainListener listener) {
        this.listener = listener;
        retrofit = RetrofitInstance.getInstance().getRetrofit();
        apiService = retrofit.create(ApiManager.class);

    }

    @Override
    public void initRxbus(ArrayMap<Object, Observable> observables) {
       testObservable = RxBus.get().register("test", String.class);
        observables.put("test",testObservable);
        testObservable
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String arg0) {
                        if (arg0 != null && !arg0.trim().equals("")) {
                            Log.d("TAG", "收到test");
                            //listener.badRequest(arg0);
                            //listener.initPayStatus();
                            listener.setViewLog(arg0);
                        }
                    }
                });
    }

    public void postQdData() {

        //签到组包
        strmsg.setLength(0);
        My8583Ans.setMainKey("B9257F2A4C317675709EEF89D9D54A89");
        myans.frame8583QD(myans.fieldsSend,myans.pack);
        byte[] send = new byte[myans.pack.txLen];
        arraycopy(myans.pack.txBuffer,0,send,0,myans.pack.txLen);
        String qdstr = My8583Ans.bytesToHexString(send);
        strmsg.append("->send:");
        strmsg.append(qdstr);
        Log.d("Send:",strmsg.toString());
        DataBean data = new DataBean(qdstr);
        Call<byte[]> call = apiService.postData(data.bodyhex);
        call.enqueue(new Callback<byte[]>() {
            @Override
            public void onResponse(Call<byte[]> call, Response<byte[]> response) {
                if (response.isSuccessful()) {
                    // do SomeThing
                    Log.d("AA","成功");
                    Log.d("AA",response.raw().toString());
                    String strrecv = MyUtil.bytesToHexString(response.body());
                    Log.d("respondAA:",strrecv);
                    //解析
                    //System.out.println("开始解析...");
                    strmsg.append("接收响应成功!\n");
                    strmsg.append("<-begin ans:\n");
                    byte[] recv = My8583Ans.hexStringToBytes(strrecv);
                    int ret = myans.ans8583QD(recv,recv.length);
                    strmsg.append(myans.getFields(myans.fieldsRecv));
                    if(ret == 0){
                        //打印出解析成功的各个域
                        Log.d("respondAA OK:","签到成功!");
                        strmsg.append("\n签到成功!\n");
                        //System.out.println("签到成功!");
                        //System.out.println(myans.getFields(myans.fieldsRecv));
                        Log.d("respondAA OK:",myans.getFields(myans.fieldsRecv));
                    }
                } else {
                    //直接操作UI 返回的respone被直接解析成你指定的modle
                    Log.d("AA","失败");
                    strmsg.append("\n失败!\n");

                }
                listener.setViewLog(strmsg.toString());
                //RxBus.getInstance().post(new EventMsg<String>(Constant.LOG, strmsg.toString()));
            }
            @Override
            public void onFailure(Call<byte[]> call, Throwable t) {
                // do onFailure代码
                Log.d("AA","失败", t);
                //RxBus.getInstance().post(new EventMsg<String>(Constant.LOG, strmsg.toString()));
                listener.setViewLog(strmsg.toString());
            }
        });
    }

    public void postBatchData() {

        //签到组包
        strmsg.setLength(0);
        //My8583Ans.setMainKey("B9257F2A4C317675709EEF89D9D54A89");
        myans.frame8583Batch(myans.fieldsSend,myans.pack);
        byte[] send = new byte[myans.pack.txLen];
        arraycopy(myans.pack.txBuffer,0,send,0,myans.pack.txLen);
        String qdstr = My8583Ans.bytesToHexString(send);
        strmsg.append("->send:");
        strmsg.append(qdstr);
        Log.d("Send:",strmsg.toString());
        DataBean data = new DataBean(qdstr);
        Call<byte[]> call = apiService.postData(data.bodyhex);
        call.enqueue(new Callback<byte[]>() {
            @Override
            public void onResponse(Call<byte[]> call, Response<byte[]> response) {
                if (response.isSuccessful()) {
                    // do SomeThing
                    Log.d("AA","成功");
                    Log.d("AA",response.raw().toString());
                    String strrecv = MyUtil.bytesToHexString(response.body());
                    Log.d("respondAA:",strrecv);
                    //解析
                    //System.out.println("开始解析...");
                    strmsg.append("接收响应成功!\n");
                    strmsg.append("<-begin ans:\n");
                    byte[] recv = My8583Ans.hexStringToBytes(strrecv);
                    int ret = myans.ans8583Batch(recv,recv.length);
                    strmsg.append(myans.getFields(myans.fieldsRecv));
                    if(ret == 0){
                        //打印出解析成功的各个域
                        Log.d("respondAA OK:","批结成功!");
                        strmsg.append("\n批结成功!\n");
                        //System.out.println("签到成功!");
                        //System.out.println(myans.getFields(myans.fieldsRecv));
                        Log.d("respondAA OK:",myans.getFields(myans.fieldsRecv));
                    }
                } else {
                    //直接操作UI 返回的respone被直接解析成你指定的modle
                    Log.d("AA","失败");
                    strmsg.append("\n失败!\n");

                }
                listener.setViewLog(strmsg.toString());
                //RxBus.getInstance().post(new EventMsg<String>(Constant.LOG, strmsg.toString()));
            }
            @Override
            public void onFailure(Call<byte[]> call, Throwable t) {
                // do onFailure代码
                Log.d("AA","失败", t);
                //RxBus.getInstance().post(new EventMsg<String>(Constant.LOG, strmsg.toString()));
                listener.setViewLog(strmsg.toString());
            }
        });
    }
}
