package com.example.yang.myapplication.model;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.example.yang.myapplication.ans8583.My8583Ans;
import com.example.yang.myapplication.base.MyAppcation;
import com.example.yang.myapplication.ilistener.MainListener;
import com.example.yang.myapplication.retrofit.ApiManager;
import com.example.yang.myapplication.retrofit.DataBean;
import com.example.yang.myapplication.retrofit.RetrofitInstance;
import com.example.yang.myapplication.rxbus.Constant;
import com.example.yang.myapplication.rxbus.EventMsg;
import com.example.yang.myapplication.rxbus.RxBus;
import com.example.yang.myapplication.utils.MyUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static java.lang.System.arraycopy;

public class MainModelImpl implements MainModel {

    protected Context context = MyAppcation.getInstance().context;
    private MainListener listener;

    private final String url = "https://140.207.168.62:30000/";
    Retrofit retrofit;
    final My8583Ans myans = new My8583Ans();

    ApiManager apiService ;
    StringBuffer strmsg = new StringBuffer();

    public MainModelImpl(MainListener listener) {
        this.listener = listener;
        retrofit = RetrofitInstance.getInstance().getRetrofit();
        apiService = retrofit.create(ApiManager.class);

    }

    public void doRxbus(EventMsg eventMsg) {

        if (Constant.LOG.equals(eventMsg.getTag())) {
            Toast.makeText(context, "btn log", Toast.LENGTH_LONG).show();
        }

        if (Constant.ON.equals(eventMsg.getTag())) {
            Toast.makeText(context, "on...", Toast.LENGTH_LONG).show();
        }
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
}
