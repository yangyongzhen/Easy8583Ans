package com.example.yang.myapplication;

import java.io.UnsupportedEncodingException;

import static java.lang.System.arraycopy;
/**
 * Created by yangyongzhen on 2018/06/27
 * simple 8583 Protocol Analysis
 */
public class Easy8583Ans {

    //通信涉及到的一些参数,内部使用
    private static long commSn = 1; //通讯流水号
    private static byte[] piciNum = new byte[3];//批次号
    private static byte[] licenceNum = {0x33,0x30,0x36,0x30};//入网许可证编号

    //需要外部设置的参数有：商户号，终端号，主秘钥，TPDU(以下的为默认值，并提供set和get方法)
    private static String ManNum  = "808411341310014"; //商户号
    private static String PosNum  = "70782214"; //终端号
    private static String MainKey = "31313131313131313131313131313131"; //主秘钥
    private static String TPDU    = "6005010000";
    /**
     * 定义8583的报文协议包结构 Len+Tpdu+Head+MsgType+BitMap+Data
     */
    public class Pack {

        public byte[] Len;
        public byte[] Tpdu;
        public byte[] Head;
        public byte[] MsgType;
        public byte[] BitMap;
        public int TxLen;
        public byte[] TxBuffer;
        public int RxLen;
        public byte[] RxBuffer;

        public Pack() {
            Len = new byte[2];
            Tpdu = hexStringToBytes(TPDU);
            Head = new byte[]{0x61, 0x31, 0x00, 0x31, 0x11,0x08};//这几项一般固定，不会变动
            MsgType = new byte[2];
            BitMap = new byte[8];
            TxLen = 0;
            TxBuffer = new byte[1024];
            RxLen = 0;
            RxBuffer = new byte[1024];
        }

        @Override
        public String toString() {
            return "Pack{" +
                    "Len=" + bytesToHexString(Len) +
                    ", Tpdu=" + bytesToHexString(Tpdu) +
                    ", Head=" + bytesToHexString(Head) +
                    ", MsgType=" + bytesToHexString(MsgType) +
                    ", BitMap=" + bytesToHexString(BitMap) +
                    ", TxLen=" + TxLen +
                    ", TxBuffer=" + bytesToHexString(TxBuffer) +
                    ", RxLen=" + RxLen +
                    ", RxBuffer=" + bytesToHexString(RxBuffer) +
                    '}';
        }
    }

    /**
     * 域定义
     */
    public class __8583Fields {
        int ishave;   /*是否存在*/
        int type;    /*类型(取值范围0-2，组帧时0:表示不计长度 1:表示长度占1字节，2:表示2长度占2字节)*/
        int len;     /*域长度（需根据各个域的定义要求正确取值）*/
        byte[] data; /*域内容*/
        __8583Fields()
        {
            ishave = 0;
            type = 0;
            len = 0;
            data =null;
        }
    }

    public __8583Fields[] fieldsSend; //发送的域
    public __8583Fields[] fieldsRecv; //接收的域
    public Pack pack;

    public Easy8583Ans()
    {
        fieldsSend = new __8583Fields[64];
        fieldsRecv = new __8583Fields[64];
        pack = new Pack();
        init8583Fields(fieldsSend);
        init8583Fields(fieldsRecv);
    }

    /**
     * 各个域的配置，初始化
     * @param field
     */
    private void init8583Fields(__8583Fields[] field) {

        for(int i = 0; i < 64; i++)
        {
            field[i] = new __8583Fields();
        }
        field[0].type = 0;
        field[1].type = 1;//LLVAR

        field[2].type = 0;
        field[2].len = 3;

        field[3].type = 0;
        field[3].len = 6;

        field[10].type = 0;
        field[10].len = 3;

        field[11].type = 0;
        field[11].len = 3;

        field[12].type = 2;//LLLVAR
        field[13].type = 2;
        field[14].type = 2;

        field[21].type = 2;
        field[22].type = 2;

        field[24].type = 0;
        field[24].len = 1;
        field[25].type = 0;
        field[25].len = 1;

        field[31].type = 1;//LLVAR

        field[34].type = 1;//LLVAR

        field[36].type = 0;
        field[36].len = 12;

        field[37].type = 0;
        field[37].len = 6;
        field[38].type = 2;
        field[39].type = 1;

        field[40].type = 0;
        field[40].len = 8;
        field[41].type = 0;
        field[41].len = 15;

        field[43].type = 1;

        field[47].type = 2;
        field[48].type = 0;
        field[48].len = 3;
        field[51].type = 0;
        field[51].len = 8;
        field[52].type = 0;
        field[52].len = 8;

        field[54].type = 2;
        field[58].type = 2;

        field[59].type = 2;
        field[60].type = 2;
        field[61].type = 2;
        field[62].type = 2;

        field[63].type = 0;
        field[63].len = 8;

    }
    /**
     * 该方法不需要外部调用，该方法自动完成各个域的组包和BitMap的形成及报文长度的计算
     * 该方法最终组织各个域中的内容到 Pack的TxBuffer中，形成一完整报文
     * @param field
     * @param pk
    */
    private void pack8583Fields( __8583Fields[] field, Pack pk)
    {
        int j = 0;
        int len = 23;
        int tmplen = 0;
        int seat = 0x80;
        for(int i = 0;i < 64; i++) {
            seat = (seat >>1 );
            if((i%8) == 0) {
                j++;
                seat = 0x80;
            }
            if(field[i].ishave == 1) {
                pk.BitMap[j-1] |= seat;//根据每个filed中的ishave是否为1，自动计算BitMap
                if(field[i].type == 0){
                    //根据每个域的数据类型，自动截取长度组包
                    arraycopy(field[i].data,0,pk.TxBuffer,len,field[i].len);//数据
                    len += field[i].len;
                }
                else if(field[i].type == 1){
                    //域长度
                    pk.TxBuffer[len] = (byte)field[i].len;
                    tmplen = Integer.parseInt(String.format("%02x",pk.TxBuffer[len]),10);
                    //域数据
                    if((i==1)||(i==31)||(i==34)||(i==47)||(i==59)||(i==60))
                    {
                        tmplen = ((tmplen/2) + (tmplen%2));
                    }
                    len += 1;
                    arraycopy(field[i].data,0,pk.TxBuffer,len,tmplen);//数据
                    len += tmplen;
                }
                else if(field[i].type == 2){
                    pk.TxBuffer[len] = (byte)(field[i].len>>8);
                    pk.TxBuffer[len+1] = (byte)field[i].len;
                    tmplen = Integer.parseInt(String.format("%02x%02x",pk.TxBuffer[len],pk.TxBuffer[len+1]),10);
                    if((i==1)||(i==31)||(i==34)||(i==47)||(i==59)||(i==60))
                    {
                        tmplen = ((tmplen/2) + (tmplen%2));
                    }
                    len += 2;
                    arraycopy(field[i].data,0,pk.TxBuffer,len,tmplen);//数据
                    len += tmplen;
                }

            }
        }
        pk.TxLen = len;
        pk.Len[0] = (byte)((len-2) << 8);
        pk.Len[1] = (byte)(len-2);
        arraycopy(pk.Len,0,pk.TxBuffer,0,2);
        arraycopy(pk.Tpdu,0,pk.TxBuffer,2,5);
        arraycopy(pk.Head,0,pk.TxBuffer,7,6);
        arraycopy(pk.MsgType,0,pk.TxBuffer,13,2);
        arraycopy(pk.BitMap,0,pk.TxBuffer,15,8);
        commSn++; //通讯流水每次加一
    }

    /**
     * 解析8583报文，解析成功后，可在fieldRecv中查看各个域
     * @param rxbuf
     * @param rxlen
     * @param fieldrcv
     * @return
     */
    public int ans8583Fields( byte[] rxbuf,int rxlen,__8583Fields[] fieldrcv)
    {
        int len = 0;
        int tmplen = 0;
        long buf = 0,seat=1;
        byte[] bitMap = new byte[8];

        arraycopy(rxbuf,15,bitMap,0,8);
        len += 23;
        for(int i = 0;i < 8;i++) {
            buf = ((buf<<8) | (bitMap[i]&0xff));
        }
        for(int i = 0; i < 64; i++) {
            if ((buf & (seat << (63 - i))) > 0) {
                fieldrcv[i].ishave = 1;
                if(fieldrcv[i].type == 0){
                    fieldrcv[i].data = new byte[fieldrcv[i].len];
                    arraycopy(rxbuf,len,fieldrcv[i].data,0,fieldrcv[i].len);
                    len += fieldrcv[i].len;
                }
                else if(fieldrcv[i].type == 1){
                    fieldrcv[i].len = rxbuf[len];
                    tmplen = Integer.parseInt(String.format("%02x",rxbuf[len]),10);
                    if((i==1)||(i==31)||(i==47)||(i==59)||(i==60))
                    {
                        tmplen = ((tmplen/2) + (tmplen%2));
                    }
                    len += 1;
                    fieldrcv[i].data = new byte[tmplen];
                    arraycopy(rxbuf,len,fieldrcv[i].data,0,tmplen);
                    len += tmplen;

                }
                else if(fieldrcv[i].type == 2){
                    fieldrcv[i].len = ((rxbuf[len]<<8) | rxbuf[len+1]);
                    tmplen = Integer.parseInt(String.format("%02x%02x",rxbuf[len],rxbuf[len+1]),10);
                    if((i==1)||(i==31)||(i==47)||(i==59)||(i==60))
                    {
                        tmplen = ((tmplen/2) + (tmplen%2));
                    }
                    len += 2;
                    fieldrcv[i].data = new byte[tmplen];
                    arraycopy(rxbuf,len,fieldrcv[i].data,0,tmplen);
                    len += tmplen;
                }

            }
        }
        if(len > rxlen)
        {
            return 1;
        }
        return 0;
    }

    public String getFields( __8583Fields[] field){

        StringBuffer str= new StringBuffer();
        for(int i = 0; i < 64; i++) {
            if(field[i].ishave == 1) {
                str.append(String.format("[field:%d] ", i+1));
                if(field[i].type == 1) {
                    str.append(String.format("[len:%02x] ", field[i].len));
                }else if(field[i].type == 2){
                    str.append(String.format("[len:%04x] ", field[i].len));
                }
                str.append(String.format("[%s]", bytesToHexString(field[i].data)));
                str.append("\n-------------------------------------------------\n");
            }
        }
        return  str.toString();

    }

    /**
     * 签到报文组帧
     * @param field
     * @param tx
     */
    public void frame8583QD( __8583Fields[] field, Pack tx){

        //消息类型
        tx.MsgType[0] = 0x08;
        tx.MsgType[1] = 0x00;
        //11域，受卡方系统跟踪号BCD 通讯流水
        field[10].ishave = 1;
        field[10].len = 3;
        String tmp = String.format("%06d",commSn);
        field[10].data = hexStringToBytes(tmp);
        //41域，终端号
        field[40].ishave = 1;
        field[40].len = 8;
        field[40].data = PosNum.getBytes();
        //42域，商户号
        field[41].ishave = 1;
        field[41].len = 15;
        field[41].data = ManNum.getBytes();
        //60域
        field[59].ishave = 1;
        field[59].len = 0x11;
        field[59].data = new byte[6];
        field[59].data[0] = 0x00;
        arraycopy(piciNum,0,field[59].data,1,3);
        field[59].data[4] = 0x00;
        field[59].data[5] = 0x30;
        //62域
        field[61].ishave = 1;
        field[61].len = 0x25;
        field[61].data = new byte[25];
        String str = "Sequence No12";
        arraycopy(str.getBytes(),0,field[61].data,0,13);
        arraycopy(licenceNum,0,field[61].data,13,4);
        arraycopy(PosNum.getBytes(),0,field[61].data,17,8);
        //63域
        field[62].ishave = 1;
        field[62].len = 0x03;
        field[62].data = new byte[3];
        field[62].data[0] = 0x30;
        field[62].data[1] = 0x30;
        field[62].data[2] = 0x31;
        /*报文组帧，自动组织这些域到Pack的TxBuffer中*/
        pack8583Fields(field,tx);
    }



    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String bytesToHexString(byte[] src){
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

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));

        }
        return d;
    }

    public static void setPosNum(String posNum) {
        PosNum = posNum;
    }
    public static void setMainKey(String mainKey) {
        MainKey = mainKey;
    }
    public static void setTPDU(String TPDU) {
        Easy8583Ans.TPDU = TPDU;
    }
    public static String getPosNum() {
        return PosNum;
    }
    public static String getMainKey() {
        return MainKey;
    }
    public static String getTPDU() {
        return TPDU;
    }
    public static String getManNum() {
        return ManNum;
    }
    public static void setManNum(String manNum) {
        ManNum = manNum;
    }


}
