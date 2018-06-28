package com.example.yang.myapplication;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

import static com.example.yang.myapplication.MyUtil.hexStringToBytes;

public class DataBean {

    public byte[] data;
    public String QD = "005B600501000060310031131208000020000000c0001600000131303030303135393838303231303031303231303136300011000000010030002953657175656e6365204e6f3136333135305358582d34433330343131390003303120";
    public RequestBody bodyhex;
    public DataBean()
    {
        data = hexStringToBytes(QD);
        bodyhex =  new RequestBody() {
            @Override
            public MediaType contentType() {
                return null;
            }
            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                sink.write(data);
            }
            @Override
            public long contentLength() {
                return data.length;
            }
        };
    };
}
