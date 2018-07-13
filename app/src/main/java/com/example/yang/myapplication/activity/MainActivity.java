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

    private MainBean mainBean;
    private MainViewModel mainViewModel;
    //View btn1,btn2,btn3,btn4,btn5,btn6;
    //EditText edt_log;
    @Override
    protected void initView() {
        super.initView();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mainBean = new MainBean();
        mainViewModel = new MainViewModel(mainBean);

        mainBean.log_txt.set("this is log");
        //mainBean.
        binding.editText.setFocusableInTouchMode(false);
        binding.editText.setFocusable(false);

        binding.setMainBean(mainBean);
        binding.setMyClick(mainViewModel);
    }
    @Override
    protected int getLayResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
    }
    @Override
    protected void registerRxbus() {
        super.registerRxbus();
        mainViewModel.initRxbus(observables);
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
