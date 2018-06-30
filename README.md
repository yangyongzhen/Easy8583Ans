# Easy8583Ans
easy and simple 8583 Protocol Analysis  for java
我的QQ： 534117529
有任何问题，请邮件联系反馈
目标是让8583协议更简单，更直观，更好用
有多简单？有多直观？
参见博客：https://blog.csdn.net/yyz_1987/article/details/80838764
以一个签到报文举例：

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
	
最终组织好的报文在哪呢？在报文结构类Pack中的Txbuffer中，长度为Txlen

需要外部设置的参数在哪放？

外部需要配置的有：终端号，商户号，主秘钥，TPDU。这些在类里定义的有set和get方法。
如何使用？
如何发送报文给银联后台呢？

假如你通信有Send（byte[] sendbuf, int len） 方法

那么只需    Send（pack.TxBuffer,pack.TxLen）

想打印出来报文日志在哪看？ 直接pack.ToString()即可

附带一个调用的demo:

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
	}
	
	
->send:
0057600501000061310031110808000020000000c0001600000139393939393930363030313433303137303131393939390011000000000030002553657175656e6365204e6f31323330363039393939393930360003303031
Pack{len=0057, tpdu=6005010000, head=613100311108, msgType=0800, bitMap=0020000000c00016, txLen=89, txBuffer=0057600501000061310031110808000020000000c0001600000139393939393930363030313433303137303131393939390011000000000030002553657175656e6365204e6f31323330363039393939393930360003303031}
Len:	0057
TPDU:	6005010000
Head:	613100311108
MsgType:	0800
BitMap:	0020000000c00016
-------------------------------------------------
[field:11] [000001]
-------------------------------------------------
[field:41] [3939393939393036]
-------------------------------------------------
[field:42] [303031343330313730313139393939]
-------------------------------------------------
[field:60] [len:0011] [000000000030]
-------------------------------------------------
[field:62] [len:0025] [53657175656e6365204e6f3132333036303939393939393036]
-------------------------------------------------
[field:63] [len:0003] [303031]
-------------------------------------------------

->recv:007960000001386131003111080810003800010AC0001450021122130107200800085500323231333031343931333239303039393939393930363030313433303137303131393939390011000005190030004046F161A743497B32EAC760DF5EA57DF5900ECCE3977731A7EA402DDF0000000000000000CFF1592A
开始解析...
->ok 解析成功！
pinkey:d931648f3de313a4a22c15dca4f4299e
<-ok PIK正确
mackey:ab7c577cc7a180455fc2a085d7208a04
<-ok MAC正确
签到成功!
Len:	0079
TPDU:	6005010000
Head:	613100311108
MsgType:	0810
BitMap:	003800010ac00014
-------------------------------------------------
[field:11] [500211]
-------------------------------------------------
[field:12] [221301]
-------------------------------------------------
[field:13] [0720]
-------------------------------------------------
[field:32] [len:08] [00085500]
-------------------------------------------------
[field:37] [323231333031343931333239]
-------------------------------------------------
[field:39] [3030]
-------------------------------------------------
[field:41] [3939393939393036]
-------------------------------------------------
[field:42] [303031343330313730313139393939]
-------------------------------------------------
[field:60] [len:0011] [000005190030]
-------------------------------------------------
[field:62] [len:0040] [46f161a743497b32eac760df5ea57df5900ecce3977731a7ea402ddf0000000000000000cff1592a]
-------------------------------------------------
