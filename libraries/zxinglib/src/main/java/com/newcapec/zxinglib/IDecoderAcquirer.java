package com.newcapec.zxinglib;

import com.google.zxing.Result;

public interface IDecoderAcquirer {
	public void onDecoded(String resString);
	public void onDecoded(Result rawResult);
	public void onFailed();
}
