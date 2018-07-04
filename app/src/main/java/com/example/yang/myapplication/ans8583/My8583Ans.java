package com.example.yang.myapplication.ans8583;

import android.util.Log;

import java.util.Arrays;

import static com.example.yang.myapplication.utils.DesUtil.DES_decrypt_3;
import static com.example.yang.myapplication.utils.DesUtil.DES_encrypt;
import static com.example.yang.myapplication.utils.DesUtil.DES_encrypt_3;
import static java.lang.System.arraycopy;

/**
 * Created by yangyongzhen on 2018/06/30
 * simple 8583 Protocol Analysis，业务处理
 */
public class My8583Ans extends Easy8583Ans {

    private static final String TAG= " My8583Ans";

    //通信涉及到的一些参数,内部使用
    private static long commSn = 1; //通讯流水号
    private static byte[] piciNum = new byte[3];//批次号
    private static byte[] licenceNum = {0x33,0x30,0x36,0x30};//入网许可证编号

    //需要外部设置的参数有：商户号，终端号，主秘钥，TPDU(以下的为默认值，并提供set和get方法)
    //需要持久化存储这些参数，每次使用时加载
    private static String manNum  = "898411341310014"; //商户号
    private static String posNum  = "73782214"; //终端号
    private static String mainKey = "258FB0Ab70D025CDB99DF2C4D302D646"; //主秘钥
    private static String TPDU    = "6005010000";
    private static long   posSn = 1; //终端交易流水
    public My8583Ans(){

        //通过子类修改父类的配置
        pack.tpdu = hexStringToBytes(TPDU);
    }
    /**
     * 签到报文组帧
     * @param field
     * @param tx
     */
    public void frame8583QD( __8583Fields[] field, Pack tx){

        init8583Fields(fieldsSend);
        //消息类型
        tx.msgType[0] = 0x08;
        tx.msgType[1] = 0x00;
        //11域，受卡方系统跟踪号BCD 通讯流水
        field[10].ishave = 1;
        field[10].len = 3;
        String tmp = String.format("%06d",commSn);
        field[10].data = hexStringToBytes(tmp);
        //41域，终端号
        field[40].ishave = 1;
        field[40].len = 8;
        field[40].data = posNum.getBytes();
        //42域，商户号
        field[41].ishave = 1;
        field[41].len = 15;
        field[41].data = manNum.getBytes();
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
        arraycopy(posNum.getBytes(),0,field[61].data,17,8);
        //63域
        field[62].ishave = 1;
        field[62].len = 0x03;
        field[62].data = new byte[3];
        field[62].data[0] = 0x30;
        field[62].data[1] = 0x30;
        field[62].data[2] = 0x31;
        /*报文组帧，自动组织这些域到Pack的TxBuffer中*/
        pack8583Fields(field,tx);
        commSn++; //通讯流水每次加一
    }

    /**
     * 8583签到的响应报文解析
     * @param rxbuf
     * @param rxlen
     * @return 0，成功 非0，失败
     */
    public int ans8583QD(byte[] rxbuf,int rxlen){

        int ret = 0;
        ret = ans8583Fields(rxbuf,rxlen,fieldsRecv);
        if(ret != 0) {
            //Log.d(TAG,"解析失败！");
            System.out.println("<-Er 解析失败！");
            return ret;
        }
        //Log.d(TAG,"解析成功！");
        System.out.println("->ok 解析成功！");
        //消息类型判断
        if((pack.msgType[0] != 0x08)||(pack.msgType[1]!= 0x10)) {
            //Log.d(TAG,"消息类型错！");
            return 2;
        }
        //应答码判断
        if((fieldsRecv[38].data[0] != 0x30)||(fieldsRecv[38].data[1] != 0x30)){
            //Log.d(TAG,"应答码不正确！");
            //Log.d(TAG,String.format("应答码:%02x%02x",fieldsRecv[38].data[0],fieldsRecv[38].data[1]));
            return 3;
        }
        //跟踪号比较
        if(!Arrays.equals(fieldsSend[10].data,fieldsRecv[10].data)){
            //return 4;
        }
        //终端号比较
        if(!Arrays.equals(fieldsSend[40].data,fieldsRecv[40].data)){
            //return 5;
        }
        //商户号比较
        if(!Arrays.equals(fieldsSend[41].data,fieldsRecv[41].data)){
            //return 6;
        }
        //3DES解密PIN KEY
        byte[] data = new byte[16];
        arraycopy(fieldsRecv[61].data,0,data,0,16);
        byte[] pinkey = DES_decrypt_3(data,hexStringToBytes(mainKey));
        //解密后的结果对8Byte全0做3DES加密运算
        System.out.println("pinkey:"+bytesToHexString(pinkey));
        byte[] tmp= {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
        byte[] out =  DES_encrypt_3(tmp,pinkey);
        //对比pincheck是否一致
        byte[] check = new byte[4];
        byte[] pincheck = new byte[4];
        arraycopy(out,0,check,0,4);
        arraycopy(fieldsRecv[61].data,16,pincheck,0,4);
        if(!Arrays.equals(check,pincheck)) {
            System.out.println("<-Er PIK错误");
            return 7;
        }
        else {
            System.out.println("<-ok PIK正确");
        }
        //3DES解密MAC KEY
        arraycopy(fieldsRecv[61].data,20,data,0,16);
        byte[] mackey = DES_decrypt_3(data,hexStringToBytes(mainKey));
        //解密后的结果对8Byte全0做DES加密运算
        System.out.println("mackey:"+bytesToHexString(mackey));
        out =  DES_encrypt(tmp,mackey);

        byte[] maccheck = new byte[4];
        arraycopy(out,0,check,0,4);
        arraycopy(fieldsRecv[61].data,36,maccheck,0,4);
        if(!Arrays.equals(check,maccheck)) {
            System.out.println("<-Er MAC错误");
            return 8;
        }
        else {
            System.out.println("<-ok MAC正确");
            setMacKey(bytesToHexString(mackey));
        }
        //签到成功
        return 0;
    }
    /**
     * 银联二维码组包
     * @param field
     * @param tx
     */
    public void frame8583Qrcode( String qrcode,int money,__8583Fields[] field, Pack tx){

        if(qrcode.length()!= 19){
            return;
        }
        init8583Fields(fieldsSend);
        //消息类型
        tx.msgType[0] = 0x02;
        tx.msgType[1] = 0x00;
        //3域 交易处理码
        field[2].ishave = 1;
        field[2].len = 3;
        field[2].data = new byte[] {0x00,0x00,0x00};
        //4域 交易金额
        field[3].ishave = 1;
        field[3].len = 6;
        String strtmp = String.format("%012d",money);
        field[3].data = hexStringToBytes(strtmp);

        //11域，pos终端交易流水
        field[10].ishave = 1;
        field[10].len = 3;
        String tmp = String.format("%06d",posSn);
        field[10].data = hexStringToBytes(tmp);
        //22域
        field[21].ishave = 1;
        field[21].len = 2;
        field[21].data = new byte[] {0x03,0x20};
        // 25域
        field[24].ishave = 1;
        field[24].len = 1;
        field[24].data = new byte[] {0x00};
        //
        //41域，终端号
        field[40].ishave = 1;
        field[40].len = 8;
        field[40].data = posNum.getBytes();
        //42域，商户号
        field[41].ishave = 1;
        field[41].len = 15;
        field[41].data = manNum.getBytes();

        //49域 交易货币代码
        field[48].ishave = 1;
        field[48].len = 3;
        field[48].data = new byte[]{0x31,0x35,0x36};

        //59域，扫码的数据
        field[58].ishave = 1;
        field[58].len = 0x24;
        field[58].data = new byte[24];
        field[58].data[0] = 'A'; //TAG+Len(019)
        field[58].data[1] = '3';
        field[58].data[2] = '0';
        field[58].data[3] = '1';
        field[58].data[4] = '9';
        arraycopy(qrcode.getBytes(),0,field[58].data,5,19);

        //60域
        field[59].ishave = 1;
        field[59].len = 0x13;
        field[59].data = new byte[7];
        field[59].data[0] = 0x22;
        arraycopy(piciNum,0,field[59].data,1,3);
        field[59].data[4] = 0x00;
        field[59].data[5] = 0x06;
        field[59].data[6] = 0x00;
        //MAC，64域
        field[63].ishave = 1;
        field[63].len = 0x08;
        field[63].data = new byte[8];
        //这个域要求填MAC，只需按这样填，MAC的计算在pack8583Fields自动完成了
        /*报文组帧，自动组织这些域到Pack的TxBuffer中*/
        pack8583Fields(field,tx);
    }

    public int ans8583Qrcode(byte[] rxbuf,int rxlen){

        int ret = 0;
        ret = ans8583Fields(rxbuf,rxlen,fieldsRecv);
        if(ret != 0) {
            //Log.d(TAG,"解析失败！");
            System.out.println("<-Er 解析失败！");
            return ret;
        }
        //Log.d(TAG,"解析成功！");
        System.out.println("->ok 解析成功！");
        //消息类型判断
        if((pack.msgType[0] != 0x02)||(pack.msgType[1]!= 0x10)) {
            //Log.d(TAG,"消息类型错！");
            System.out.println("消息类型错！");
            return 2;
        }
        //应答码判断
        if((fieldsRecv[38].data[0] != 0x30)||(fieldsRecv[38].data[1] != 0x30)){
            Log.d(TAG,"应答码不正确！");
            Log.d(TAG,String.format("应答码:%02x%02x",fieldsRecv[38].data[0],fieldsRecv[38].data[1]));
            return 3;
        }
        //跟踪号比较
        if(!Arrays.equals(fieldsSend[10].data,fieldsRecv[10].data)){
            return 4;
        }
        //终端号比较
        if(!Arrays.equals(fieldsSend[40].data,fieldsRecv[40].data)){
            return 5;
        }
        //商户号比较
        if(!Arrays.equals(fieldsSend[41].data,fieldsRecv[41].data)){
            return 6;
        }
        //成功
        return 0;
    }

    public static void setManNum(String manNum) {
        My8583Ans.manNum = manNum;
    }

    public static void setPosNum(String posNum) {
        My8583Ans.posNum = posNum;
    }

    public static void setMainKey(String mainKey) {
        My8583Ans.mainKey = mainKey;
    }

    public static void setTPDU(String TPDU) {
        My8583Ans.TPDU = TPDU;
    }

    public static String getManNum() {
        return manNum;
    }

    public static String getPosNum() {
        return posNum;
    }

    public static String getMainKey() {
        return mainKey;
    }

    public static String getTPDU() {
        return TPDU;
    }

    /**
     * 调用demo
     * @param args
     */
    public static void main(String[] args) {
        My8583Ans myans = new My8583Ans();
        //签到组包
        myans.frame8583QD(myans.fieldsSend,myans.pack);
        //打印出待发送的报文
        byte[] send = new byte[myans.pack.txLen];
        arraycopy(myans.pack.txBuffer,0,send,0,myans.pack.txLen);
        System.out.println("->send:");
        System.out.println(My8583Ans.bytesToHexString(send));
        System.out.println(myans.pack.toString());
        System.out.println(myans.getFields(myans.fieldsSend));
        //接收解析,假如收到的报文在recv中
        String recvstr ="007960000001386131003111080810003800010AC0001450021122130107200800085500323231333031343931333239303039393939393930363030313433303137303131393939390011000005190030004046F161A743497B32EAC760DF5EA57DF5900ECCE3977731A7EA402DDF0000000000000000CFF1592A";
        System.out.println("->recv:"+recvstr);
        byte[] recv = My8583Ans.hexStringToBytes(recvstr);
        // mypack.ans8583Fields(bt,bt.length,mypack.fieldsRecv);
        //解析
        System.out.println("开始解析...");
        int ret = myans.ans8583QD(recv,recv.length);
        if(ret == 0){
            //打印出解析成功的各个域
            System.out.println("签到成功!");
            System.out.println(myans.getFields(myans.fieldsRecv));
        }

        //二维码组包
        System.out.println("->二维码交易 demo");
        String qrcode = "6223360612494957384";
        int money = 1; //1分
        myans.frame8583Qrcode(qrcode,money,myans.fieldsSend,myans.pack);
        //打印出待发送的报文
        send = new byte[myans.pack.txLen];
        arraycopy(myans.pack.txBuffer,0,send,0,myans.pack.txLen);
        System.out.println("->send:");
        System.out.println(My8583Ans.bytesToHexString(send));
        System.out.println(myans.pack.toString());
        System.out.println(myans.getFields(myans.fieldsSend));
    }
}
