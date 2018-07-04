package com.example.yang.myapplication.utils;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import static com.example.yang.myapplication.utils.MyUtil.bytesToHexString;
import static com.example.yang.myapplication.utils.MyUtil.hexStringToBytes;
import static java.lang.System.arraycopy;

/**
 * DES 加密算法 DES和3DES ECB模式的加解密
 * @author yangyongzhen
 *
 */
public class DesUtil {

    public final static String DES = "DES";
    /**
     * 加密
     * @param data byte[]
     * @param key byte[]
     * @return byte[]
     */
    public static byte[] DES_encrypt(byte[] data, byte[] key) {
        try{
            // 生成一个可信任的随机数源
            SecureRandom sr = new SecureRandom();

// 从原始密钥数据创建DESKeySpec对象
            DESKeySpec dks = new DESKeySpec(key);

// 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
            SecretKey securekey = keyFactory.generateSecret(dks);

// Cipher对象实际完成加密操作
            Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");

// 用密钥初始化Cipher对象
            cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);

            return cipher.doFinal(data);
        }catch(Throwable e){
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 解密
     * @param data byte[]
     * @param key byte[]
     * @return byte[]
     */
    public static byte[] DES_decrypt(byte[] data, byte[] key) {
        try{
            // 生成一个可信任的随机数源
            SecureRandom sr = new SecureRandom();

// 从原始密钥数据创建DESKeySpec对象
            DESKeySpec dks = new DESKeySpec(key);
// 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
            SecretKey securekey = keyFactory.generateSecret(dks);
// Cipher对象实际完成解密操作
            Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
// 用密钥初始化Cipher对象
            cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
            return cipher.doFinal(data);
        }catch(Throwable e){
            e.printStackTrace();
        }
        return null;

    }

    public static byte[] DES_encrypt_3(byte[] datasource, byte[] key){

        if ((key.length!= 16) || ((datasource.length%8 )!= 0)){
            return null;
        }
        byte[] Lkey = new byte[8];
        byte[] Rkey = new byte[8];
        arraycopy(key,0,Lkey,0,8);
        arraycopy(key,8,Rkey,0,8);

        byte[] outdata,tmpdata;

        outdata = DES_encrypt(datasource,Lkey);//加
        tmpdata = DES_decrypt(outdata,Rkey);//解
        outdata = DES_encrypt(tmpdata,Lkey);//加

        return outdata;

    }

    public static byte[] DES_decrypt_3(byte[] datasource, byte[] key){

        if ((key.length!= 16) || ((datasource.length%8 )!= 0)) {
            return null;
        }
        byte[] Lkey = new byte[8];
        byte[] Rkey = new byte[8];
        arraycopy(key,0,Lkey,0,8);
        arraycopy(key,8,Rkey,0,8);
        byte[] outdata,tmpdata;

        outdata = DES_decrypt(datasource,Lkey);//解
        tmpdata = DES_encrypt(outdata,Rkey);//加
        outdata = DES_decrypt(tmpdata,Lkey);//解
        return outdata;
    }
    public static void main(String[] args) {

        String strkey = "258FB0Ab70D025CDB99DF2C4D302D646";
        String K1 = strkey.substring(0, strkey.length() / 2);
        String K2 = strkey.substring(strkey.length() / 2);
        String strdata = "46F161A743497B32EAC760DF5EA57DF5";
        String D1 = strdata.substring(0, strdata.length() / 2);
        String D2 = strdata.substring(strdata.length() / 2);

        byte[] bt;
        String out;
        bt = DES_decrypt(hexStringToBytes(D1),hexStringToBytes(K1));
        out = bytesToHexString(bt);
        System.out.println("discryption结果为："+out);
        bt = DES_encrypt(hexStringToBytes(D1),hexStringToBytes(K1));
        out = bytesToHexString(bt);
        System.out.println("encryption结果为："+out);

        bt = DES_encrypt_3(hexStringToBytes(strdata),hexStringToBytes(strkey));
        out = bytesToHexString(bt);
        System.out.println("encryption_3结果为："+out);

        bt = DES_decrypt_3(hexStringToBytes(strdata),hexStringToBytes(strkey));
        out = bytesToHexString(bt);
        System.out.println("decryption_3结果为："+out);
    }
}

