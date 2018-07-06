package com.example.yang.myapplication.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

import javax.net.SocketFactory;

import android.util.Log;

/**
 * 客户端套接字（socket）的封装
 *
 * @author WangGY
 */
public class Client {

	private static Client client;

	/**
	 *  套接字
	 */
	private Socket socket;

	/**
	 * ip
	 */
	private String ip;

	/**
	 * 端口号
	 */
	private int port;

	/**
	 * 消息字符编码
	 */
	// private String charset = "utf-8";

	/**
	 * 构造方法
	 */
	private Client() {

		// 创建未连接的 socket
		try {
			this.socket = SocketFactory.getDefault().createSocket();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Client getInstance() {

		if (client == null) {
			client = new Client();
		}

		return client;
	}

	/**
	 * 开始连接socket
	 *
	 * @param ip
	 * @param port
	 */
	public void connect(String ip, int port) {

		this.ip = ip;
		this.port = port;

		// 判断socket是否连接
//		if (this.socket.isConnected() && !this.socket.isClosed()) {

		// 关闭 socket，这里就不用判断是否连接了，
		// 如果未连接，关闭报异常，不影响代码运行
		close();

//		}

		// 创建未连接的 socket
		try {
			socket = SocketFactory.getDefault().createSocket();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		creatSocket();
	}

	/**
	 * 创建socket连接
	 */
	private void creatSocket() {
		if (!this.socket.isConnected() || this.socket.isClosed()) {
			new Thread() {
				public void run() {
					try {
						Log.e("Client", ip + ":" + port + "准备连接");
						// 开始连接
						socket.connect(new InetSocketAddress(ip, port));
//						socket.connect(new InetSocketAddress(ip, port), 6000);
						Log.e("Client", ip + ":" + port + "已连接");
						// TODO 开始接收消息
						new Thread() {
							public void run() {
								receiveMsg();
							};
						}.start();
					} catch (IOException e) {
						e.printStackTrace();
						Log.e("Client", ip + ":" + port + "服务器连接不上");
					}
				}
			}.start();
		}
	}

	/**
	 * 关闭socket连接
	 */
	public void close() {
		try {
			// 关闭套接字连接
			this.socket.close();
			Log.e("Client", ip + ":" + port + "已断开连接");
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("Client", "关闭socket出现异常");
		}
	}

	/**
	 * 向服务器发送消息
	 *
	 * @param msg
	 * @throws IOException
	 */
	public void sendMsg(byte[] msg) throws IOException {
		this.output(msg);
	}

	/**
	 * 输出消息
	 *
	 * @param msg
	 * @throws IOException
	 */
	private void output(byte[] msg) throws IOException {

		BufferedOutputStream bos = new BufferedOutputStream(
				this.socket.getOutputStream());
		bos.write(msg);
		bos.flush();

	}

	/**
	 * 接收消息
	 */
	public void receiveMsg() {
		try {
			BufferedInputStream bis = new BufferedInputStream(
					this.socket.getInputStream());
			byte[] msg = new byte[1024];
			int len = -1;
			while ((len = bis.read(msg)) != -1) {
				if (receivebyte != null) {
					receivebyte.receivebyte(Arrays.copyOfRange(msg, 0, len));
				}
			}
			Log.e("client", "socket连接断开了，测试-1");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public Receivebyte receivebyte;

	public void setReceivebyte(Receivebyte receivebyte) {
		this.receivebyte = receivebyte;
	}

	/**
	 * 监听回调接口
	 */
	public interface Receivebyte {
		void receivebyte(byte[] subarray);
	}

	/**
	 * 设置消息字符编码 用socket与服务器交互，就直接用字节交互
	 *
	 * @param charset
	 */
	// public void setCharset(String charset) {
	// this.charset = charset;
	// }

	/**
	 * 向服务器发送消息
	 *
	 * @param msg
	 * @throws IOException
	 */
	// public void sendMsg(int msg) throws IOException {
	// this.output(String.valueOf(msg).getBytes(charset));
	// }

	/**
	 * 向服务器发送消息
	 *
	 * @param msg
	 * @throws IOException
	 */
	// public void sendMsg(char[] msg) throws IOException {
	// this.output(String.valueOf(msg).getBytes(charset));
	// }

	/**
	 * 向服务器发送消息
	 *
	 * @param msg
	 * @throws IOException
	 */
	// public void sendMsg(String msg) throws IOException {
	// this.output(msg.getBytes(charset));
	// }

}
