package com.karics.library.zxing.android;

/**
 * Created by yang on 2017/7/19.
 */
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Locale;

public class ByteUtils
{
    private static final int DEFAULT_MASK = 255;
    private static final String FORMAT_NOSPACE = "%02x";
    private static final String FORMAT_SPACE = "%02x ";
    private static final int HEXA = 16;
    private static final int MAX_BIT_INTEGER = 31;

    public static int byteArrayToInt(byte[] paramArrayOfByte)
    {
        if (paramArrayOfByte == null)
            throw new IllegalArgumentException("Parameter 'byteArray' cannot be null");
        return byteArrayToInt(paramArrayOfByte, 0, paramArrayOfByte.length);
    }

    public static int byteArrayToInt(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    {
        if (paramArrayOfByte == null)
            throw new IllegalArgumentException("Parameter 'byteArray' cannot be null");
        if ((paramInt2 <= 0) || (paramInt2 > 4))
            throw new IllegalArgumentException("Length must be between 1 and 4. Length = " + paramInt2);
        if ((paramInt1 < 0) || (paramArrayOfByte.length < paramInt1 + paramInt2))
            throw new IllegalArgumentException("Length or startPos not valid");
        int i = 0;
        for (int j = 0; j < paramInt2; j++)
            i += ((0xFF & paramArrayOfByte[(paramInt1 + j)]) << 8 * (-1 + (paramInt2 - j)));
        return i;
    }

    public static int bytesToInt(byte[] paramArrayOfByte, int paramInt)
    {
        int i = 0;
        for (int j = paramInt; j < 4; j++)
            i = i << 8 | 0xFF & paramArrayOfByte[j];
        return i;
    }

    public static String bytesToString(byte[] paramArrayOfByte)
    {
        return formatByte(paramArrayOfByte, "%02x ", false);
    }

    public static String bytesToString(byte[] paramArrayOfByte, boolean paramBoolean)
    {
        return formatByte(paramArrayOfByte, "%02x ", paramBoolean);
    }

    public static String bytesToStringNoSpace(byte paramByte)
    {
        return formatByte(new byte[] { paramByte }, "%02x", false);
    }

    public static String bytesToStringNoSpace(byte[] paramArrayOfByte)
    {
        return formatByte(paramArrayOfByte, "%02x", false);
    }

    public static String bytesToStringNoSpace(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    {
        byte[] arrayOfByte = new byte[paramInt2];
        System.arraycopy(paramArrayOfByte, paramInt1, arrayOfByte, 0, paramInt2);
        return bytesToStringNoSpace(arrayOfByte);
    }

    public static String bytesToStringNoSpace(byte[] paramArrayOfByte, boolean paramBoolean)
    {
        return formatByte(paramArrayOfByte, "%02x", paramBoolean);
    }

    private static String formatByte(byte[] paramArrayOfByte, String paramString, boolean paramBoolean)
    {
        StringBuffer localStringBuffer = new StringBuffer();
        if (paramArrayOfByte == null)
            localStringBuffer.append("");

        int i = 0;
        int j = paramArrayOfByte.length;
        for (int k = 0; k < j; k++)
        {
            int m = paramArrayOfByte[k];
            if ((m != 0) || (!paramBoolean) || (i != 0))
            {
                i = 1;
                Object[] arrayOfObject = new Object[1];
                arrayOfObject[0] = Integer.valueOf(m & 0xFF);
                localStringBuffer.append(String.format(paramString, arrayOfObject));
            }
        }

        return localStringBuffer.toString().toUpperCase(Locale.getDefault()).trim();
    }

    public static byte[] fromString(String paramString)
    {
        if (paramString == null)
            throw new IllegalArgumentException("Argument can't be null");
        String str = paramString.replace(" ", "");
        if (str.length() % 2 != 0)
            throw new IllegalArgumentException("Hex binary needs to be even-length :" + paramString);
        byte[] arrayOfByte = new byte[Math.round(str.length() / 2.0F)];
        int i = 0;
        int j = 0;
        while (j < str.length())
        {
            Integer localInteger = Integer.valueOf(Integer.parseInt(str.substring(j, j + 2), 16));
            int k = i + 1;
            arrayOfByte[i] = localInteger.byteValue();
            j += 2;
            i = k;
        }
        return arrayOfByte;
    }

    public static byte[] intToBytes(int paramInt)
    {
        byte[] arrayOfByte = new byte[4];
        arrayOfByte[0] = ((byte)(paramInt >> 24));
        arrayOfByte[1] = ((byte)(paramInt << 8 >> 24));
        arrayOfByte[2] = ((byte)(paramInt << 16 >> 24));
        arrayOfByte[3] = ((byte)(paramInt << 24 >> 24));
        return arrayOfByte;
    }

    public static boolean matchBitByBitIndex(int paramInt1, int paramInt2)
    {
        if ((paramInt2 < 0) || (paramInt2 > 31))
            throw new IllegalArgumentException("parameter 'pBitIndex' must be between 0 and 31. pBitIndex=" + paramInt2);
        return (paramInt1 & 1 << paramInt2) != 0;
    }

    public static byte setBit(byte paramByte, int paramInt, boolean paramBoolean)
    {
        if ((paramInt < 0) || (paramInt > 7))
            throw new IllegalArgumentException("parameter 'pBitIndex' must be between 0 and 7. pBitIndex=" + paramInt);
        if (paramBoolean)
            return (byte)(paramByte | 1 << paramInt);
        return (byte)(paramByte & (0xFFFFFFFF ^ 1 << paramInt));
    }

    public static String toBinary(byte[] paramArrayOfByte)
    {
        String str = null;
        if (paramArrayOfByte != null)
        {
            int i = paramArrayOfByte.length;
            str = null;
            if (i > 0)
            {
                StringBuilder localStringBuilder = new StringBuilder(new BigInteger(bytesToStringNoSpace(paramArrayOfByte), 16).toString(2));
                for (int j = localStringBuilder.length(); j < 8 * paramArrayOfByte.length; j++)
                    localStringBuilder.insert(0, 0);
                str = localStringBuilder.toString();
            }
        }
        return str;
    }

    public static byte[] toByteArray(int paramInt)
    {
        byte[] arrayOfByte = new byte[4];
        arrayOfByte[0] = ((byte)(paramInt >> 24));
        arrayOfByte[1] = ((byte)(paramInt >> 16));
        arrayOfByte[2] = ((byte)(paramInt >> 8));
        arrayOfByte[3] = ((byte)paramInt);
        return arrayOfByte;
    }

    public  static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
    public  static String toHexString(String s)
    {
        String str="";
        for (int i=0;i<s.length();i++)
        {
            int ch = (int)s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }
    // 转化十六进制编码为字符串
    public  static String toStringHex(String s)
    {
        byte[] baKeyword = new byte[s.length()/2];
        for(int i = 0; i < baKeyword.length; i++)
        {
            try
            {
                baKeyword[i] = (byte)(0xff & Integer.parseInt(s.substring(i*2, i*2+2),16));
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            s = new String(baKeyword, "utf-8");//UTF-16le:Not
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
        return s;
    }
    /*
    * 16进制数字字符集
    */
    private static String hexString="0123456789ABCDEF";
    /*
    * 将字符串编码成16进制数字,适用于所有字符（包括中文）
    */
    public static String encode(String str)
    {
//根据默认编码获取字节数组
        byte[] bytes=str.getBytes();
        StringBuilder sb=new StringBuilder(bytes.length*2);
//将字节数组中每个字节拆解成2位16进制整数
        for(int i=0;i<bytes.length;i++)
        {
            sb.append(hexString.charAt((bytes[i]&0xf0)>>4));
            sb.append(hexString.charAt((bytes[i]&0x0f)>>0));
        }
        return sb.toString();
    }
    /*
    * 将16进制数字解码成字符串,适用于所有字符（包括中文）
    */
    public static String decode(String bytes)
    {
        ByteArrayOutputStream baos=new ByteArrayOutputStream(bytes.length()/2);
//将每2位16进制整数组装成一个字节
        for(int i=0;i<bytes.length();i+=2)
            baos.write((hexString.indexOf(bytes.charAt(i))<<4 |hexString.indexOf(bytes.charAt(i+1))));
        return new String(baos.toByteArray());
    }
}