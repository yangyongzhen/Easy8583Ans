package com.example.yang.myapplication.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;

import com.example.yang.myapplication.R;
import com.example.yang.myapplication.base.BaseActivity;
import com.example.yang.myapplication.bean.mvvm.MainBean;
import com.example.yang.myapplication.databinding.ActivityMainBinding;
import com.example.yang.myapplication.viewmodel.MainViewModel;

public class MainActivity extends BaseActivity {

    public final String TAG = "MainActivity";
    private static final int REQUEST_CODE_SCAN = 0x0000;

    protected ActivityMainBinding binding;

    private MainBean mainBean = new MainBean();
    //View btn1,btn2,btn3,btn4,btn5,btn6;
    //EditText edt_log;
    @Override
    protected void initView() {
        super.initView();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mainBean.log_txt.set("this is log");
        //mainBean.
        binding.editText.setFocusableInTouchMode(false);
        binding.editText.setFocusable(false);
        binding.setMainBean(mainBean);
        binding.setMyClick(new MainViewModel(mainBean));
    }
    @Override
    protected int getLayResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        //edt_log = (EditText) findViewById(R.id.editText);
        //edt_log.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        //文本显示的位置在EditText的最上方
        //edt_log.setGravity(Gravity.TOP);
        //edt_log.setSingleLine(false);
        //edt_log.setCursorVisible(false);
        //edt_log.setFocusable(false);
        //edt_log.setFocusableInTouchMode(false);
        //水平滚动设置为False
        //edt_log.setHorizontallyScrolling(false);
        //edt_log.setVerticalScrollBarEnabled(true);
        //edt_log.setMovementMethod(ScrollingMovementMethod.getInstance());

        //My8583Ans.setMainKey("B9257F2A4C317675709EEF89D9D54A89");
        /*
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"btn1 clicked", Toast.LENGTH_SHORT).show();

            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"btn3 clicked", Toast.LENGTH_SHORT).show();
            }
        });
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this,
                        CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SCAN);
            }
        });
        btn5.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CheckResult")
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"btn5 clicked", Toast.LENGTH_SHORT).show();
                RxBus.getInstance().post(new EventMsg<String>(Constant.ON, "\n来自btn5的消息！\n"));
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
                                Toast.makeText(MainActivity.this, "结果:", Toast.LENGTH_LONG).show();
                               // edt_log.setText(strlog.toString());
                                Log.d("TAG",inf);
                            }
                        });
            }
        });
        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"btn6 clicked", Toast.LENGTH_SHORT).show();
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
      */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra("codedContent");
                Log.i(TAG, "扫码结果:" + content);
                //request2(content);
                //byte[] bytes = data.getByteArrayExtra(DECODED_RAWBYTES_KEY);
                //qrCoded.setText("解码结果： \n" +content, TextView.BufferType.SPANNABLE );
                //qrCoded.setText("解码结果1： \n" +  bytesToHexString(bytes), TextView.BufferType.SPANNABLE );
                }
                else{
                }
        }
    }

}
