package com.example.yang.myapplication.retrofit;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

import static com.example.yang.myapplication.utils.MyUtil.hexStringToBytes;

public class DataBean {

    public byte[] data;
    public RequestBody bodyhex;
    public DataBean(String strdata)
    {
        data = hexStringToBytes(strdata);
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
