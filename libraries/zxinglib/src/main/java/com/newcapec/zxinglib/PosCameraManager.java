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

package com.newcapec.zxinglib;

import java.io.IOException;

import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.client.android.camera.open.OpenCamera;
import com.google.zxing.client.android.camera.open.OpenCameraInterface;

import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The
 * implementation encapsulates the steps needed to take preview-sized images, which are used for
 * both preview and decoding.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
@SuppressWarnings("deprecation") // camera APIs
public final class PosCameraManager {

  private static final String TAG = PosCameraManager.class.getSimpleName();

//  private static final int MIN_FRAME_WIDTH = 240;
//  private static final int MIN_FRAME_HEIGHT = 240;
//  private static final int MAX_FRAME_WIDTH = 1200; // = 5/8 * 1920
//  private static final int MAX_FRAME_HEIGHT = 675; // = 5/8 * 1080

  private OpenCamera camera;
  //private boolean initialized;
  private boolean previewing;
  private int requestedCameraId = OpenCameraInterface.NO_REQUESTED_CAMERA;
  private int requestedFramingRectWidth;
  private int requestedFramingRectHeight;
  /**
   * Preview frames are delivered here, which we pass on to the registered handler. Make sure to
   * clear the handler so it will only receive one message.
   */
  private final PreviewCallback previewCallback;

  public PosCameraManager() {
    previewCallback = new PreviewCallback();
  }
  
  /**
   * Opens the camera driver and initializes the hardware parameters.
   *
   * @param holder The surface object which the camera will draw preview frames into.
   * @throws IOException Indicates the camera driver failed to open.
   */
  public synchronized void openDriver() throws IOException {
    OpenCamera theCamera = camera;
    if (theCamera == null) {
      theCamera = OpenCameraInterface.open(requestedCameraId);
      if (theCamera == null) {
        throw new IOException("Camera.open() failed to return object from driver");
      }
      camera = theCamera;
      Log.d(TAG, "openDriver");
    }
  }

  public synchronized boolean isOpen() {
    return camera != null;
  }

  /**
   * Closes the camera driver if still in use.
   */
  public synchronized void closeDriver() {
    if (camera != null) {
      camera.getCamera().release();
      camera = null;
      Log.d(TAG, "closeDriver");
    }
  }

  /**
   * Asks the camera hardware to begin drawing preview frames to the screen.
   */
  public synchronized void startPreview() {
    OpenCamera theCamera = camera;
    if (theCamera != null && !previewing) {
      theCamera.getCamera().startPreview();
      previewing = true;
      Log.d(TAG, "startPreview");
    }
  }

  /**
   * Tells the camera to stop drawing preview frames.
   */
  public synchronized void stopPreview() {
    if (camera != null && previewing) {
      camera.getCamera().stopPreview();
      previewCallback.setHandler(null, 0);
      previewing = false;
      Log.d(TAG, "stopPreview");
    }
  }
 
  public synchronized void requestPreviewFrame(Handler handler, int message) {
	    OpenCamera theCamera = camera;
	    if (theCamera != null && previewing) {
	      previewCallback.setHandler(handler, message);
	      theCamera.getCamera().setOneShotPreviewCallback(previewCallback);
	    }
	  }

  /**
   * Allows third party apps to specify the camera ID, rather than determine
   * it automatically based on available cameras and their orientation.
   *
   * @param cameraId camera ID of the camera to use. A negative value means "no preference".
   */
  public synchronized void setManualCameraId(int cameraId) {
    requestedCameraId = cameraId;
  }
  
  /**
   * @param width The width in pixels to scan.
   * @param height The height in pixels to scan.
   */
  public synchronized void setManualFramingRect(int width, int height) {
    requestedFramingRectWidth = width;
    requestedFramingRectHeight = height;
    previewCallback.setPreviewRect(requestedFramingRectWidth,requestedFramingRectHeight);
  }

  /**
   * A factory method to build the appropriate LuminanceSource object based on the format
   * of the preview buffers, as described by Camera.Parameters.
   *
   * @param data A preview frame.
   * @param width The width of the image.
   * @param height The height of the image.
   * @return A PlanarYUVLuminanceSource instance.
   */
  public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
    Rect rect = new Rect();
    rect.left = 0;
    rect.top = 0;
    rect.right = width;
    rect.bottom = height;

    // Go ahead and assume it's YUV rather than die.
    return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                                        rect.width(), rect.height(), false);
  }

}
