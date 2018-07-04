package com.example.yang.myapplication.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

public class QRCodeUtil {
	/**
	 * 生成指定大小不带图片的二维码 w 宽 h 高
	 */
	public static Bitmap createImage(String text, int w, int h) {
		try {
			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			// 图像数据转换，使用了矩阵转换
			BitMatrix bitMatrix = new QRCodeWriter().encode(text,
					BarcodeFormat.QR_CODE, w, h, hints);
			int[] pixels = new int[w * h];
			// 下面这里按照二维码的算法，逐个生成二维码的图片，
			// 两个for循环是图片横列扫描的结果
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * w + x] = 0xff000000;
					} else {
						pixels[y * w + x] = 0xffffffff;
					}
				}
			}
			// 生成二维码图片的格式，使用ARGB_8888
			Bitmap bitmap = null;
			bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 生成带图片的二维码
	 */
	public static Bitmap createImage(String text, int w, int h, Bitmap logo) {
//		try {
//			Bitmap scaleLogo = getScaleLogo(logo, w, h);
//			int offsetX = (w - scaleLogo.getWidth()) / 2;
//			int offsetY = (h - scaleLogo.getHeight()) / 2;
//			Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
//			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
//			BitMatrix bitMatrix = new QRCodeWriter().encode(text,
//					BarcodeFormat.QR_CODE, w, h, hints);
//			int[] pixels = new int[w * h];
//			for (int y = 0; y < h; y++) {
//				for (int x = 0; x < w; x++) {
//					if (x >= offsetX && x < offsetX + scaleLogo.getWidth()
//							&& y >= offsetY
//							&& y < offsetY + scaleLogo.getHeight()) {
//						int pixel = scaleLogo
//								.getPixel(x - offsetX, y - offsetY);
//						if (pixel == 0) {
//							if (bitMatrix.get(x, y)) {
//								pixel = 0xff000000;
//							} else {
//								pixel = 0xffffffff;
//							}
//						}
//						pixels[y * w + x] = pixel;
//					} else {
//						if (bitMatrix.get(x, y)) {
//							pixels[y * w + x] = 0xff000000;
//						} else {
//							pixels[y * w + x] = 0xffffffff;
//						}
//					}
//				}
//			}
//			Bitmap bitmap = null;
//			bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//			bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
//			return bitmap;
//		} catch (WriterException e) {
//			e.printStackTrace();
//		}
		return null;
	}

	private static Bitmap getScaleLogo(Bitmap logo, int w, int h) {
		if (logo == null)
			return null;
		Matrix matrix = new Matrix();
		float scaleFactor = Math.min(w * 1.0f / 5 / logo.getWidth(), h * 1.0f
				/ 5 / logo.getHeight());
		matrix.postScale(scaleFactor, scaleFactor);
		Bitmap result = null;
		result = Bitmap.createBitmap(logo, 0, 0, logo.getWidth(),
				logo.getHeight(), matrix, true);
		return result;
	}

}