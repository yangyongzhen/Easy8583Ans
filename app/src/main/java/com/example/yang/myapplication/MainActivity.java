package com.example.yang.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import okhttp3.OkHttpClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.lang.System.arraycopy;

//import com.google.zxing.integration.android.IntentIntegrator;
//import com.google.zxing.integration.android.IntentResult;
import com.karics.library.zxing.android.CaptureActivity;


public class MainActivity extends AppCompatActivity {

    public final String TAG = "MainActivity";
    private static final int REQUEST_CODE_SCAN = 0x0000;
    View btn1,btn2,btn3,btn4;

    //IntentIntegrator integrator;
    OkHttpClient client = OkHttp3Utils.getOkHttpSingletonInstance(MainActivity.this);
    String url = "https://140.207.168.62:30000/";
    //String url = "http://3s.dkys.org:16932";
    Retrofit retrofit = new Retrofit.Builder().client(client).baseUrl(url)
            .addConverterFactory(MyConverterFactory.create()).build();
    ApiManager apiService = retrofit.create(ApiManager.class);

    final My8583Ans myans = new My8583Ans();


    //myans.s
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1 = findViewById(R.id.button1);
        btn2 = findViewById(R.id.button2);
        btn3 = findViewById(R.id.button3);
        btn4 = findViewById(R.id.button4);

        My8583Ans.setMainKey("B9257F2A4C317675709EEF89D9D54A89");

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"btn1 clicked", Toast.LENGTH_SHORT).show();
                request1();
            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"btn3 clicked", Toast.LENGTH_SHORT).show();
                request2("6225621270761967496");
            }
        });
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this,
                        CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SCAN);
               /*
                integrator = new IntentIntegrator(MainActivity.this);
                // 设置要扫描的条码类型，ONE_D_CODE_TYPES：一维码，QR_CODE_TYPES-二维码
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setCaptureActivity(ScanActivity.class);
                integrator.setPrompt("请扫描"); //底部的提示文字，设为""可以置空
                integrator.setCameraId(0); //前置或者后置摄像头
                integrator.setBeepEnabled(true); //扫描成功的「哔哔」声，默认开启
                integrator.setOrientationLocked(true);
                integrator.setBarcodeImageEnabled(true);
                integrator.setTimeout(60*1000);
                integrator.initiateScan();

                Log.e("MainActivity", "显示扫一扫页面");
                */

            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"btn2 clicked", Toast.LENGTH_SHORT).show();

                My8583Ans mypack = new My8583Ans();
                mypack.frame8583QD(mypack.fieldsSend,mypack.pack);
                Log.d("8583:",mypack.pack.toString());
                Log.d("fields send",mypack.getFields(mypack.fieldsSend));

                String str ="0057600087000061310031110808000020000000C0001650001536313030303030313839383633303134313131313038350011000000000030002553657175656E6365204E6F31323330363036313030303030310003303031";
                byte[] bt = My8583Ans.hexStringToBytes(str);
               // mypack.ans8583Fields(bt,bt.length,mypack.fieldsRecv);
                mypack.ans8583QD(bt,bt.length);
                Log.d("fields recv",mypack.getFields(mypack.fieldsRecv));
            }
        });


    }

    public void request() {

        OkHttpClient client = OkHttp3Utils.getOkHttpSingletonInstance(MainActivity.this);
        Retrofit retrofit = new Retrofit.Builder().client(client).baseUrl("http://fy.iciba.com")
                .addConverterFactory(GsonConverterFactory.create()).build();

        ApiManager apiService = retrofit.create(ApiManager.class);

        Call<LoginResult> call = apiService.getCall();
        call.enqueue(new Callback<LoginResult>() {
            @Override
            public void onResponse(Call<LoginResult> call, Response<LoginResult> response) {
                if (response.isSuccessful()) {
                    // do SomeThing
                    Log.d("AA","成功");
                    response.body().show();
                    response.toString();
                } else {
                    //直接操作UI 返回的respone被直接解析成你指定的modle
                    response.body().show();
                    response.toString();

                }
            }

            @Override
            public void onFailure(Call<LoginResult> call, Throwable t) {

                // do onFailure代码
                Log.d("AA","失败");
            }
        });
    }

    public void request1() {

        /*
        OkHttpClient client = OkHttp3Utils.getOkHttpSingletonInstance(MainActivity.this);
        String url = "https://140.207.168.62:30000/";
        //String url = "http://3s.dkys.org:16932";
        Retrofit retrofit = new Retrofit.Builder().client(client).baseUrl(url)
                .addConverterFactory(MyConverterFactory.create()).build();
        ApiManager apiService = retrofit.create(ApiManager.class);

        //String qdstr ="0057600087000061310031110808000020000000C0001650001536313030303030313839383633303134313131313038350011000000000030002553657175656E6365204E6F31323330363036313030303030310003303031";
        final My8583Ans myans = new My8583Ans();
        //myans.s
        My8583Ans.setMainKey("B9257F2A4C317675709EEF89D9D54A89");
        */
        //签到组包
        myans.frame8583QD(myans.fieldsSend,myans.pack);
        byte[] send = new byte[myans.pack.txLen];
        arraycopy(myans.pack.txBuffer,0,send,0,myans.pack.txLen);
        String qdstr = My8583Ans.bytesToHexString(send);
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
                    System.out.println("开始解析...");
                    byte[] recv = My8583Ans.hexStringToBytes(strrecv);
                    int ret = myans.ans8583QD(recv,recv.length);
                    if(ret == 0){
                        //打印出解析成功的各个域
                        System.out.println("签到成功!");
                        System.out.println(myans.getFields(myans.fieldsRecv));
                    }
                        //byte[] arr = response.raw().toString();
                        //arr[0] = 0;
                    //response.body().show();
                } else {
                    //直接操作UI 返回的respone被直接解析成你指定的modle
                    Log.d("AA","失败");
                    //response.body().show();

                }
            }

            @Override
            public void onFailure(Call<byte[]> call, Throwable t) {

                // do onFailure代码
                Log.d("AA","失败", t);
            }
        });
    }

    public void request2(String qrcode) {
        //二维码交易
        //String qrcode = "6225621270761967496";
        int money = 1; //1分
        myans.frame8583Qrcode(qrcode,money,myans.fieldsSend,myans.pack);
        byte[] send = new byte[myans.pack.txLen];
        arraycopy(myans.pack.txBuffer,0,send,0,myans.pack.txLen);
        String sendstr = My8583Ans.bytesToHexString(send);
        DataBean data = new DataBean(sendstr);
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
                    System.out.println("开始解析...");
                    byte[] recv = My8583Ans.hexStringToBytes(strrecv);
                    int ret = myans.ans8583Qrcode(recv,recv.length);
                    if(ret == 0){
                        //打印出解析成功的各个域
                        System.out.println("成功!");
                        System.out.println(myans.getFields(myans.fieldsRecv));
                    }else{
                        System.out.println(myans.getFields(myans.fieldsRecv));
                    }
                    //byte[] arr = response.raw().toString();
                    //arr[0] = 0;
                    //response.body().show();
                } else {
                    //直接操作UI 返回的respone被直接解析成你指定的modle
                    Log.d("AA","失败");
                    //response.body().show();

                }
            }

            @Override
            public void onFailure(Call<byte[]> call, Throwable t) {

                // do onFailure代码
                Log.d("AA","失败", t);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra("codedContent");
                Log.i(TAG, "扫码结果:" + content);
                request2(content);
                //byte[] bytes = data.getByteArrayExtra(DECODED_RAWBYTES_KEY);
                //qrCoded.setText("解码结果： \n" +content, TextView.BufferType.SPANNABLE );
                //qrCoded.setText("解码结果1： \n" +  bytesToHexString(bytes), TextView.BufferType.SPANNABLE );

                }
                else{

                }
        }
        /*
        //super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                //payFrament.setShowLayout(2);
                Log.i(TAG, "扫码失败");
            } else {
                String qrcode = result.getContents();
                Log.i(TAG, "扫码结果:" + qrcode);
                request2(qrcode);
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }*/
    }
}
