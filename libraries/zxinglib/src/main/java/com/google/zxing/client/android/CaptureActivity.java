/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.client.android.camera.CameraManager;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public abstract class CaptureActivity extends Activity implements SurfaceHolder.Callback {

  private static final String TAG = CaptureActivity.class.getSimpleName();

  private static final long BULK_MODE_SCAN_DELAY_MS = 100L;

  private CameraManager cameraManager;
  private CaptureActivityHandler handler;
  private ViewfinderView viewfinderView;
  private boolean hasSurface;
  private Collection<BarcodeFormat> decodeFormats;
  private Map<DecodeHintType,?> decodeHints;
  private String characterSet;
//  private InactivityTimer inactivityTimer;
//  private BeepManager beepManager;

  private int viewfinder_view_id;
  private int preview_view_id;

  public void setViewFindViewId(int id) {
    viewfinder_view_id = id;
  }

  public void setPreviewViewId(int id) {
    preview_view_id = id;
  }

  ViewfinderView getViewfinderView() {
    return viewfinderView;
  }

  public Handler getHandler() {
    return handler;
  }

  CameraManager getCameraManager() {
    return cameraManager;
  }

  @Override
  protected void onCreate(Bundle icicle) {
    super.onCreate(icicle);

    Window window = getWindow();
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//    setContentView(R.layout.capture);

    hasSurface = false;
//    inactivityTimer = new InactivityTimer(this);
//    beepManager = new BeepManager(this);
  }

  @Override
  protected void onResume() {
    super.onResume();

    // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
    // want to open the camera driver and measure the screen size if we're going to show the help on
    // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
    // off screen.
    cameraManager = new CameraManager(getApplication());

    viewfinderView = (ViewfinderView) findViewById(viewfinder_view_id);
    viewfinderView.setCameraManager(cameraManager);

    //statusView = (TextView) findViewById(R.id.status_view);

    handler = null;
//    lastResult = null;


//    beepManager.updatePrefs();
//    ambientLightManager.start(cameraManager);

//    inactivityTimer.onResume();

    Intent intent = getIntent();

    decodeFormats = null;
    characterSet = null;

    if (intent != null) {

      String action = intent.getAction();
//      String dataString = intent.getDataString();

      if (Intents.Scan.ACTION.equals(action)) {

        //只有当ACTION=SCAN时才可以设置各种额外信息

        // Scan the formats the intent requested, and return the result to the calling activity.
//        source = IntentSource.NATIVE_APP_INTENT;
        decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
        decodeHints = DecodeHintManager.parseDecodeHints(intent);

        if (intent.hasExtra(Intents.Scan.WIDTH) && intent.hasExtra(Intents.Scan.HEIGHT)) {
          int width = intent.getIntExtra(Intents.Scan.WIDTH, 0);
          int height = intent.getIntExtra(Intents.Scan.HEIGHT, 0);
          if (width > 0 && height > 0) {
            cameraManager.setManualFramingRect(width, height);
          }
        }

        if (intent.hasExtra(Intents.Scan.CAMERA_ID)) {
          int cameraId = intent.getIntExtra(Intents.Scan.CAMERA_ID, -1);
          if (cameraId >= 0) {
            cameraManager.setManualCameraId(cameraId);
          }
        }

//        String customPromptMessage = intent.getStringExtra(Intents.Scan.PROMPT_MESSAGE);
//        if (customPromptMessage != null) {
//          statusView.setText(customPromptMessage);
//        }

      }
      characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);
    }

    SurfaceView surfaceView = (SurfaceView) findViewById(preview_view_id);
    SurfaceHolder surfaceHolder = surfaceView.getHolder();
    if (hasSurface) {
      // The activity was paused but not stopped, so the surface still exists. Therefore
      // surfaceCreated() won't be called, so init the camera here.
      initCamera(surfaceHolder);
    } else {
      // Install the callback and wait for surfaceCreated() to init the camera.
      surfaceHolder.addCallback(this);
    }
  }

  @Override
  protected void onPause() {
    if (handler != null) {
      handler.quitSynchronously();
      handler = null;
    }
//    inactivityTimer.onPause();
//    beepManager.close();
    cameraManager.closeDriver();
    if (!hasSurface) {
      SurfaceView surfaceView = (SurfaceView) findViewById(preview_view_id);
      SurfaceHolder surfaceHolder = surfaceView.getHolder();
      surfaceHolder.removeCallback(this);
    }
    super.onPause();
  }

  @Override
  protected void onDestroy() {
//    inactivityTimer.shutdown();
    super.onDestroy();
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    if (holder == null) {
      Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
    }
    if (!hasSurface) {
      hasSurface = true;
      initCamera(holder);
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    hasSurface = false;
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    // do nothing
  }

  /**
   * A valid barcode has been found, so give an indication of success and show the results.
   *
   * @param rawResult The contents of the barcode.
   * @param scaleFactor amount by which thumbnail was scaled
   * @param barcode   A greyscale bitmap of the camera data which was decoded.
   */
  public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
//    inactivityTimer.onActivity();
//    lastResult = rawResult;

//    boolean fromLiveScan = barcode != null;
//    if (fromLiveScan) {
//      beepManager.playBeepSoundAndVibrate();
//    }

    onDecoded(rawResult.getText());
    onDecoded(rawResult);

    Log.w(TAG, rawResult.getText());
    //restartPreviewAfterDelay(10000);
    restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
  }

  public void handleDecodeFailed() {
//	    inactivityTimer.onActivity();
    onFailed();
  }

  private void initCamera(SurfaceHolder surfaceHolder) {
    if (surfaceHolder == null) {
      throw new IllegalStateException("No SurfaceHolder provided");
    }
    if (cameraManager.isOpen()) {
      Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
      return;
    }
    try {
      cameraManager.openDriver(surfaceHolder);
      // Creating the handler starts the preview, which can also throw a RuntimeException.
      if (handler == null) {
        handler = new CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
      }
    } catch (IOException ioe) {
      Log.w(TAG, ioe);
      //displayFrameworkBugMessageAndExit();
    } catch (RuntimeException e) {
      // Barcode Scanner has seen crashes in the wild of this variety:
      // java.?lang.?RuntimeException: Fail to connect to camera service
      Log.w(TAG, "Unexpected error initializing camera", e);
      //displayFrameworkBugMessageAndExit();
    }
  }

//  private void displayFrameworkBugMessageAndExit() {
//    AlertDialog.Builder builder = new AlertDialog.Builder(this);
//    builder.setTitle(getString(R.string.app_name));
//    builder.setMessage(getString(R.string.msg_camera_framework_bug));
//    builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
//    builder.setOnCancelListener(new FinishListener(this));
//    builder.show();
//  }

  public void restartPreviewAfterDelay(long delayMS) {
    if (handler != null) {
      handler.sendEmptyMessageDelayed(MsgIds.restart_preview, delayMS);
    }
  }

  public void drawViewfinder() {
    viewfinderView.drawViewfinder();
  }

  public void onDecoded(String resString) {
  }

  public void onDecoded(Result rawResult) {
  }

  public void onFailed() {
  }
}
