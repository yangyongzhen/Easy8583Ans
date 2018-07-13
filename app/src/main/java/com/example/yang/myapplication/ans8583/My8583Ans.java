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
    private static byte[] licenceNum = {0x33,0x30,0x36,0x30};//入网许可证编号

    private String upBinNumber;  //银行卡卡号
    private String cardSnNumber; //持卡序号
    private String cardValiteDate; //卡有效期
    private String field35Data; //二磁道数据

    //需要外部设置的参数有：商户号，终端号，主秘钥，TPDU(以下的为默认值，并提供set和get方法)
    //需要持久化存储这些参数，每次使用时加载
    private static String manNum  = "898411341310014"; //商户号
    private static String posNum  = "73782214"; //终端号
    private static String mainKey = "31313131313131313131313131313131"; //主秘钥
    private static String TPDU    = "6005010000";
    private static byte[] piciNum = new byte[3];//批次号
    private static long   posSn = 1; //终端交易流水,每笔交易加一

    public My8583Ans(){

        //通过子类修改父类的配置
        pack.tpdu = hexStringToBytes(TPDU);
        upBinNumber = "";
        cardSnNumber = "";
        cardValiteDate="";
        field35Data = "";
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
        //保存批次号
        arraycopy(fieldsRecv[59].data,1,piciNum,0,3);
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
        commSn++; //通讯流水每次加一
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

    /**
     * 从传入参数的TLV格式的数据源中解析出55域需要的数据
     * @param src 需要解析的数据源(TLV格式)
     * @param money 交易金额
     * @param date 交易日期 格式如:"180707",必须跟卡指令中传的金额和日期一致
     * @return
     */
    byte[] getFieldData55(byte[] src ,int money,String date){

        return null;

    }
    int intToHex(int d){
        int out = 0;
        out = ((d/10)<<4);
        out |= (d%10);
        return out;
    }
    /**
     * 银联小额双免组包
     * @param carddata 卡上数据
     * @param money 交易金额(单位分)
     * @param date 日期（长度为6） 格式如：180707
     * @param field
     * @param tx
     */
    public void frameShuangMian( byte[] carddata,int money,String date,__8583Fields[] field, Pack tx){

        init8583Fields(fieldsSend);
        if(date.length() != 6) {
            return;
        }
        byte[] field55 = getFieldData55(carddata,money,date);
        if(upBinNumber.equals("") || field35Data.equals("")){
            return;
        }
        //消息类型
        tx.msgType[0] = 0x02;
        tx.msgType[1] = 0x00;
        //2域 银行卡号
        field[1].ishave = 1;
        String strtemp = String.format("%02d",upBinNumber.length());
        byte[] tmp = hexStringToBytes(strtemp);
        field[1].len = tmp[0];
        strtemp = upBinNumber;
        if(upBinNumber.length()%2 != 0) {
            strtemp += "0";
        }
        field[1].data = hexStringToBytes(strtemp);

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
        String tmp1 = String.format("%06d",posSn);
        field[10].data = hexStringToBytes(tmp1);

        //14域，卡有效期
        if(cardValiteDate.equals("")){
        }else {
            field[13].ishave = 1;
            field[13].len = 2;
            field[13].data = hexStringToBytes(cardValiteDate);
        }
        //22域
        field[21].ishave = 1;
        field[21].len = 2;
        field[21].data = new byte[] {0x07,0x20};

        //23域卡片序列号
        if(cardSnNumber.equals("")){
            field[22].ishave = 1;
            field[22].len = 2;
            field[22].data = new byte[]{0x00,0x00};
        }else {
            field[22].ishave = 1;
            field[22].len = 2;
            field[22].data = new byte[]{0x00,0x00};
            if(cardSnNumber.equals("01")){
                field[22].data[1] = 0x01;
            }
        }
        // 25域
        field[24].ishave = 1;
        field[24].len = 1;
        field[24].data = new byte[] {0x00};
        //35域，二磁道数据
        field[34].ishave = 1;
        int tmplen = field35Data.length();
        if( field35Data.charAt(tmplen-1) == 'f'){
            tmplen =intToHex(tmplen);//转为HEX
            field[34].len = tmplen -1;
        }else{
            tmplen =intToHex(tmplen);//转为HEX
            field[34].len = tmplen;
        }
        field[34].data = hexStringToBytes(field35Data);

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

        //55域，卡上TLV数据组包
        field[54].ishave = 1;
        //field[54].len = 3;
        String tmplen1 = String.format("%04d",field55.length);
        byte[] tmpby = hexStringToBytes(tmplen1);
        field[54].len = (((tmpby[0]&0xff)<<8)|(tmpby[1]&0xff));
        field[54].data = field55.clone();

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
        commSn++; //通讯流水每次加一
    }

    /**
     * 双免收到的应答报文解析
     * @param rxbuf
     * @param rxlen
     * @return
     */
    public int ansShuangMian(byte[] rxbuf,int rxlen){

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

    /**
     * 批结算组包，当后台报交易流水重复，响应码94时，批结算后允许交易流水从一开始
     * @param field
     * @param tx
     */
    public void frame8583Batch( __8583Fields[] field, Pack tx){

        init8583Fields(fieldsSend);
        //消息类型
        tx.msgType[0] = 0x05;
        tx.msgType[1] = 0x00;

        //11域，通信流水
        field[10].ishave = 1;
        field[10].len = 3;
        String tmp = String.format("%06d",commSn);
        field[10].data = hexStringToBytes(tmp);
        //
        //41域，终端号
        field[40].ishave = 1;
        field[40].len = 8;
        field[40].data = posNum.getBytes();
        //42域，商户号
        field[41].ishave = 1;
        field[41].len = 15;
        field[41].data = manNum.getBytes();

        //48域
        field[47].ishave = 1;
        field[47].len = 0x62;
        field[47].data = new byte[31];
        Arrays.fill(field[47].data,(byte)0);
        //49域 交易货币代码
        field[48].ishave = 1;
        field[48].len = 3;
        field[48].data = new byte[]{0x31,0x35,0x36};

        //60域
        field[59].ishave = 1;
        field[59].len = 0x11;
        field[59].data = new byte[6];
        field[59].data[0] = 0x00;
        arraycopy(piciNum,0,field[59].data,1,3);
        field[59].data[4] = 0x20;
        field[59].data[5] = 0x10;

        //63域
        field[62].ishave = 1;
        field[62].len = 0x23;
        field[62].data = new byte[23];
        Arrays.fill(field[62].data,(byte)'X');
        field[62].data[0] = 0x01;
        field[62].data[1] = 0x01;
        field[62].data[2] = 0x01;
        /*报文组帧，自动组织这些域到Pack的TxBuffer中*/
        pack8583Fields(field,tx);
        commSn++; //通讯流水每次加一
    }

    /**
     * 8583批结算响应报文解析
     * @param rxbuf
     * @param rxlen
     * @return 0，成功 非0，失败
     */
    public int ans8583Batch(byte[] rxbuf,int rxlen){

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
        if((pack.msgType[0] != 0x05)||(pack.msgType[1]!= 0x10)) {
            //Log.d(TAG,"消息类型错！");
            return 2;
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
        //批结算成功
        return 0;
    }

    /**
     * 批结算结束报文组帧
     * @param field
     * @param tx
     */
    public void frame8583BatchOver( __8583Fields[] field, Pack tx){

        init8583Fields(fieldsSend);
        //消息类型
        tx.msgType[0] = 0x03;
        tx.msgType[1] = 0x20;

        //11域，通信流水
        field[10].ishave = 1;
        field[10].len = 3;
        String tmp = String.format("%06d",commSn);
        field[10].data = hexStringToBytes(tmp);
        //
        //41域，终端号
        field[40].ishave = 1;
        field[40].len = 8;
        field[40].data = posNum.getBytes();
        //42域，商户号
        field[41].ishave = 1;
        field[41].len = 15;
        field[41].data = manNum.getBytes();

        //48域
        field[47].ishave = 1;
        field[47].len = 0x04;
        field[47].data = new byte[2];
        Arrays.fill(field[47].data,(byte)0);

        //60域
        field[59].ishave = 1;
        field[59].len = 0x11;
        field[59].data = new byte[6];
        field[59].data[0] = 0x00;
        arraycopy(piciNum,0,field[59].data,1,3);
        field[59].data[4] = 0x20;
        field[59].data[5] = 0x70;

        /*报文组帧，自动组织这些域到Pack的TxBuffer中*/
        pack8583Fields(field,tx);
        commSn++; //通讯流水每次加一
    }

    /**
     * 批结结束应答报文解析
     * @param rxbuf
     * @param rxlen
     * @return
     */
    public int ans8583BatchOver(byte[] rxbuf,int rxlen){

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
        if((pack.msgType[0] != 0x03)||(pack.msgType[1]!= 0x30)) {
            Log.d(TAG,"消息类型错！");
            //System.out.println("消息类型错！");
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

        //双免组包测试
        System.out.println("->银行卡双免交易 demo");
        String cardData = "9f370400000001776882027C009F3602004E57136212264200008234827D25062204329991635F9F101307010103A00000010A01000000000079B281999F2608DE242036EBF1B10B5F3401019F6C0220009F5D060000000000005F20142020202020202020202020202020202020202020";

        byte[] hexdata = hexStringToBytes(cardData);
        myans.frameShuangMian(hexdata,1,"180707",myans.fieldsSend,myans.pack);

        //打印出待发送的报文
        send = new byte[myans.pack.txLen];
        arraycopy(myans.pack.txBuffer,0,send,0,myans.pack.txLen);
        System.out.println("->send:");
        System.out.println(My8583Ans.bytesToHexString(send));
        System.out.println(myans.pack.toString());
        System.out.println(myans.getFields(myans.fieldsSend));


        //批结算组包
        System.out.println("->批结算 demo");
        myans.frame8583Batch(myans.fieldsSend,myans.pack);
        //打印出待发送的报文
        byte[] send2 = new byte[myans.pack.txLen];
        arraycopy(myans.pack.txBuffer,0,send2,0,myans.pack.txLen);
        System.out.println("->send:");
        System.out.println(My8583Ans.bytesToHexString(send2));
        System.out.println(myans.pack.toString());
        System.out.println(myans.getFields(myans.fieldsSend));

        //mac算法测试
        String mackey = "313215FDA2C4AB4C";
        String data="0118600501000061310031110802007024068020C08211196210810840003424940000000000000000000100002027060720000100376210810840003424940D27062207001000100F373337383232313438393834313133343133313030313431353601649F2608ED1D6BA78357FB259F2701809F101307010103A00000010A0100000000003B1637329F3704591D37169F360201C0950500000000009A031807119C01009F02060000000000015F2A02015682027C009F1A0201569F03060000000000009F3303E0E1C89F34030200009F3501229F1E0830303030303030308408A0000003330101019F0902008C9F4104000000209F6310303130353030303000000000000000000013220007570006004237313333423537";
        byte[] hexdt = hexStringToBytes(data);
        System.out.println("->mac check:");
        byte[] mac = myans.upGetMac(hexdt,13,hexdt.length-13-8,hexStringToBytes(mackey));
        String strmac = bytesToHexString(mac);
        System.out.println("->mac is:"+strmac);
    }
}
