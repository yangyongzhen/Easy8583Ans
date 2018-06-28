# Easy8583Ans
easy and simple 8583 Protocol Analysis  for java
我的QQ： 534117529
有任何问题，请邮件联系反馈
目标是让8583协议更简单，更直观，更好用
有多简单？有多直观？
参见博客：https://blog.csdn.net/yyz_1987/article/details/80838764
以一个签到报文举例：
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

最终组织好的报文在哪呢？在报文结构类Pack中的Txbuffer中，长度为Txlen

需要外部设置的参数在哪放？

外部需要配置的有：终端号，商户号，主秘钥，TPDU。这些在类里定义的有set和get方法。
如何使用？
如何发送报文给银联后台呢？

假如你通信有Send（byte[] sendbuf, int len） 方法

那么只需 Send（pack.TxBuffer,pack.TxLen）

想打印出来报文日志在哪看？ 直接pack.ToString()即可

附带一个调用的demo:

Easy8583Ans mypack = new Easy8583Ans();
mypack.frame8583QD(mypack.fieldSend,mypack.pack);
msocket.send(mypack.pack.TxBuffer,mypack.pack.TxLen)
输出结果如下，连带每个域的日志都有了，够简单直观了吧：
D/8583：: Pack{Len=0057, Tpdu=6005010000, Head=613100311108, MsgType=0800, BitMap=0020000000c00016, TxLen=89, TxBuffer=0057600501000061310031110808000020000000c0001600000137303738323231343830383431313334313331303031340011000000000030002553657175656e6365204e6f31320000000037303738323231340003303031, RxLen=0, RxBuffer=
D/fields：:

           [field:11][000001]

           -------------------------
           [field:41][3730373832323134]
           -------------------------
           [field:42][383038343131333431333130303134]
           -------------------------
           [field:60][len:0011][000000000030]
           -------------------------
           [field:62][len:0025][53657175656e6365204e6f3132000000003730373832323134]
           -------------------------
           [field:63][len:0003][303031]
           -------------------------
