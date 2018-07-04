package com.example.yang.myapplication.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;


/**
 * 播放声音
 */
public class SoundPlayUtils {

    private static SoundPlayUtils soundUtil;
    private MediaPlayer mediaPlayer;
    private Context context;

    private SoundPlayUtils(Context context) {
        mediaPlayer = new MediaPlayer();
        this.context = context;
    }

    public static SoundPlayUtils getInstance(Context context) {
        if (soundUtil == null) {
            //同步块，线程安全的创建实例
            synchronized (SoundPlayUtils.class) {
                //再次检查实例是否存在，如果不存在才真正的创建实例
                if (soundUtil == null) {
                    soundUtil = new SoundPlayUtils(context);
                }
            }
        }
        return soundUtil;
    }

    public synchronized void play(int index) {
        try {
            AssetFileDescriptor fileDescriptor = getAssetFileDescriptor(index);
            if(mediaPlayer!=null) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                        fileDescriptor.getStartOffset(), fileDescriptor.getLength());
                mediaPlayer.prepare();
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AssetFileDescriptor getAssetFileDescriptor(int index) {
        AssetFileDescriptor fileDescriptor = null;
        try {
            switch (index) {
                case 1:// 支付成功(提示音)
                    fileDescriptor = context.getAssets().openFd("pay_success.wav");
                    break;
                case 2:// 支付失败(提示音)
                    fileDescriptor = context.getAssets().openFd("pay_fail.wav");
                    break;
                case 3:// 请重新放卡(提示音)
                    fileDescriptor = context.getAssets().openFd("putcard.wav");
                    break;
                case 4:// 非法卡(提示音)
                    fileDescriptor = context.getAssets().openFd("illegality_card.mp3");
                    break;
                case 5:// 挂失卡(提示音)
                    fileDescriptor = context.getAssets().openFd("guashi_card.mp3");
                    break;
                case 6:// 余额不足(提示音)
                    fileDescriptor = context.getAssets().openFd("yue_fail.mp3");
                    break;
                case 7:// 领款失败(提示音)
                    fileDescriptor = context.getAssets().openFd("drawmoney_fail.mp3");
                    break;
                case 8:// 领款成功(提示音)
                    fileDescriptor = context.getAssets().openFd("drawmoney_success.mp3");
                    break;
                case 9:// 请输入密码(提示音)
                    fileDescriptor = context.getAssets().openFd("input_pwd.mp3");
                    break;
                case 10:// 扫码成功-滴(提示音)
                    fileDescriptor = context.getAssets().openFd("beep.ogg");
                    break;
                case 11:// 失效码(提示音)
                    fileDescriptor = context.getAssets().openFd("code_fail.mp3");
                    break;
                case 12:// 此码失效码(提示音)
                    fileDescriptor = context.getAssets().openFd("this_code_fail.mp3");
                    break;
                case 13:// 请输入金额(提示音)
                    fileDescriptor = context.getAssets().openFd("input_money.mp3");
                    break;
                case 14:// 等待超时(提示音)
                    fileDescriptor = context.getAssets().openFd("wait_timeout.mp3");
                    break;
                case 15:// 支付超时(提示音)
                    fileDescriptor = context.getAssets().openFd("pay_timeout.mp3");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileDescriptor;
    }

    public void close() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }


//	// SoundPool对象
//	public static SoundPool mSoundPlayer = new SoundPool(10,
//			AudioManager.STREAM_SYSTEM, 5);
//	public static SoundPlayUtils soundPlayUtils;
//	// 上下文
//	static Context mContext;
//
//	/**
//	 * 初始化
//	 *
//	 * @param context
//	 */
//	public static SoundPlayUtils init (Context context) throws Exception{
//		if (soundPlayUtils == null) {
//			soundPlayUtils = new SoundPlayUtils();
//		}
//
//		// 初始化声音
//		mContext = context;
//
//		mSoundPlayer.load(mContext, R.raw.pay_success, 1);// 1 支付成功(提示音)
//		mSoundPlayer.load(mContext, R.raw.pay_fail, 1);// 2 支付失败(提示音)
//		mSoundPlayer.load(mContext, R.raw.putcard, 1);// 3 请重新放卡(提示音)
//		mSoundPlayer.load(mContext, R.raw.illegality_card, 1);// 4 非法卡(提示音)
//		mSoundPlayer.load(mContext, R.raw.guashi_card, 1);// 5 挂失卡(提示音)
//		mSoundPlayer.load(mContext, R.raw.yue_fail, 1);// 6 余额不足(提示音)
//		mSoundPlayer.load(mContext, R.raw.drawmoney_fail, 1);// 7 领款失败(提示音)
//		mSoundPlayer.load(mContext, R.raw.drawmoney_success, 1);// 8 领款成功(提示音)
//		mSoundPlayer.load(mContext, R.raw.input_pwd, 1);// 9 请输入密码(提示音)
//		mSoundPlayer.load(mContext, R.raw.beep, 1);// 10 扫码成功-滴(提示音)
//		mSoundPlayer.load(mContext, R.raw.code_fail, 1);// 11 失效码(提示音)
//		mSoundPlayer.load(mContext, R.raw.this_code_fail, 1);// 12 此码失效码(提示音)
//		mSoundPlayer.load(mContext, R.raw.beep, 1);// 13 扫码滴(提示音)
//		mSoundPlayer.load(mContext, R.raw.wait_timeout, 1);// 14 等待超时(提示音)
//		mSoundPlayer.load(mContext, R.raw.pay_timeout, 1);// 15 支付超时(提示音)
////		mSoundPlayer.load(mContext, R.raw.pay_timeout, 1);// 16 请输入金额(提示音)
//		return soundPlayUtils;
//	}
//
//	/**
//	 * 播放声音
//	 */
//	public static void play(int soundID) {
//		mSoundPlayer.play(soundID, 1, 1, 0, 0, 1);
//	}
//    /**
//     * 释放资源
//     */
//    public static void close() {
//        mSoundPlayer.release();
//    }
}
