/**
 *
 */
package com.newcapec.zxinglib;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * @author 谢琦
 *
 */
public final class PosDecoder extends Handler {
	private static final String TAG = PosDecoder.class.getSimpleName();

	private static final long BULK_MODE_SCAN_DELAY_MS = 100L;

	private IDecoderAcquirer decoderAcquirer;

	private DecodeThread decodeThread;
	private State state;
	private PosCameraManager cameraManager;

	private Collection<BarcodeFormat> decodeFormats;
	private Map<DecodeHintType, ?> decodeHints;

	private String characterSet = "UTF-8";

	private int width = 640, height = 480;

	private enum State {
		PREVIEW, SUCCESS, DONE
	}

	public PosDecoder(IDecoderAcquirer da) {
		decoderAcquirer = da;
		decodeThread = null;
		cameraManager = null;
		writeLinuxParams("20");
	}

	protected void finalize() {
		writeLinuxParams("0");
		Log.i(TAG, "PosDecoder Exit!");
	}

	public PosCameraManager getCameraManager() {
		return cameraManager;
	}

	public Handler getHandler() {
		return this;
	}

	public void onResume() {
		cameraManager = new PosCameraManager();

		decodeFormats = null;
		characterSet = null;

		cameraManager.setManualFramingRect(width, height);
		initCamera();

		Log.d(TAG, "onResume");
	}

	public void onPause() {
		quitSynchronously();
		cameraManager.closeDriver();
		Log.d(TAG, "onPause");
	}

	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
		if (decoderAcquirer != null) {
			decoderAcquirer.onDecoded(rawResult.getText());
			decoderAcquirer.onDecoded(rawResult);
		}

		//Log.w(TAG, rawResult.getText());
		restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
	}

	public void handleDecodeFailed() {
		if (decoderAcquirer != null) {
			decoderAcquirer.onFailed();
		}
	}

	public void restartPreviewAfterDelay(long delayMS) {
		sendEmptyMessageDelayed(MsgIds.restart_preview, delayMS);
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
			case MsgIds.restart_preview:
				restartPreviewAndDecode();
				break;
			case MsgIds.decode_succeeded:
				state = State.SUCCESS;
				Bitmap barcode = null;
				float scaleFactor = 1.0f;
				handleDecode((Result) message.obj, barcode, scaleFactor);
				break;
			case MsgIds.decode_failed:
				// We're decoding as fast as possible, so when one decode fails, start another.
				state = State.PREVIEW;
				cameraManager.requestPreviewFrame(decodeThread.getHandler(), MsgIds.decode);
				break;
			case MsgIds.return_scan_result:
				break;
			case MsgIds.launch_product_query:

				break;
		}
	}

	private void initCamera() {
		if (cameraManager.isOpen()) {
			Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
			return;
		}

		try {
			cameraManager.openDriver();

			decodeThread = new DecodeThread(this, decodeFormats, decodeHints, characterSet);
			decodeThread.start();
			state = State.SUCCESS;

			// Start ourselves capturing previews and decoding.
			//this.cameraManager = cameraManager;
			cameraManager.startPreview();
			restartPreviewAndDecode();
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
		} catch (RuntimeException e) {
			Log.w(TAG, "Unexpected error initializing camera", e);
		}
	}

	public void quitSynchronously() {
		state = State.DONE;
		cameraManager.stopPreview();
		Message quit = Message.obtain(decodeThread.getHandler(), MsgIds.quit);
		quit.sendToTarget();
		try {
			// Wait at most half a second; should be enough time, and onPause() will timeout
			// quickly
			decodeThread.join(500L);
		} catch (InterruptedException e) {
			// continue
		}

		// Be absolutely sure we don't send any queued up messages
		removeMessages(MsgIds.decode_succeeded);
		removeMessages(MsgIds.decode_failed);
	}

	private void restartPreviewAndDecode() {
		if (state == State.SUCCESS) {
			state = State.PREVIEW;
			cameraManager.requestPreviewFrame(decodeThread.getHandler(), MsgIds.decode);
		}
	}

	private void writeLinuxParams(String param) {
		try {
			@SuppressWarnings("resource")
			FileOutputStream fos = new FileOutputStream("/sys/module/gc0308/parameters/exposure");
			fos.write(param.getBytes());
			Log.d(TAG, "写参数成功");
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "写参数失败：" + e.getMessage());
		}
	}
}
