package serial;

import gnu.io.*;

/**
 * 作用 : 串口操作:封装底层
 * 类使用介绍:
 * 		获取实例：getInstance
 * 		打开串口：openPort
 * 		读写数据：writeByte->readByte
 * 		关闭串口：closePort
 * 注意事项 : 无
 */
public class Serial_For_Smslib {
	public static Serial_For_Smslib sms_serial;
	public static Level_Final_Serial final_Level;
	
	//需要设置的参数
	int portId; 					// 串口号：如：com1，则portId为1
	int baudrate;					// 波特率
	
	//	不一定要设定的参数(有默认值)
	int timeOut; 					// 延迟时间(毫秒数)
	int dataBits; 					// 数据位
	int stopBits; 					// 停止位
	int parity; 					// 奇偶检验
	int funCode; 					// 功能码	
	int dataLen;					// 数据长度
	int appendMillsec; 				// 计算发送间隔用---附加毫秒数
	int bytes;						// 计算发送间隔用---发送字节数	

	// 自动计算--发送间隔
	int frameInterval; 				// 根据波特率，数据倍率和数据量，自动设置发送间隔
	
	// 构造方法
	public Serial_For_Smslib() {
		final_Level = new Level_Final_Serial();
		timeOut = 10;						// 延迟时间(毫秒数)
		dataBits = SerialPort.DATABITS_8;	// 数据位
		stopBits = SerialPort.STOPBITS_1;	// 停止位
		parity = SerialPort.PARITY_NONE;	// 奇偶检验
		funCode = 3;						//	读取当前寄存器内一个或多个二进制值
		dataLen = 4;						//	假设 需要获取4个数据
		appendMillsec = 38;					//	附加毫秒数(需要自己测试调整)
		bytes = 20;							//  发送是字节数
	}

	/**
	 * @describe:  获取程序单例
	 * @date:2009-11-5
	 */
	public static Serial_For_Smslib getInstance() {
		if (sms_serial == null) {
			sms_serial = new Serial_For_Smslib();
		}
		return sms_serial;
	}

	/**
	 * @describe: 打开串口
	 * @param portStr 串口号. 如: COM3
	 * @param baudrate 波特率
	 * @param appName 串口占用程序的命名
	 * @return: true:打开串口正常 false:打开串口异常
	 */
	public boolean openPort(String portStr, int baudrate, String appName) {
		boolean rsBool = false;
		
		// 初始化串口
		final_Level.initialize(timeOut, baudrate, dataBits, stopBits, parity);
		final_Level.setAppname(appName.toUpperCase());
		// 打开串口
		if (final_Level.openPort( portStr)) {
			rsBool = true;
			// 设置帧之间的发送间隔
			this.frameInterval = getFrameInterval(appendMillsec, bytes, baudrate);		
		}
		return rsBool;
	}	
	
	
	/**
	 * @describe: 写串口命令 - 发送AT这个指令
	 * @param rs 发送的数据
	 */
	public void writeByte(char[] rs) throws Exception{
		final_Level.writePort(rs);
		// 打印发送的串口数据-16进制显示
		// System.out.println(bytesToHexString(rs));
		
		//等待一段时间, 以保证数据,有足够的时间发送和接收
		//Thread.sleep(frameInterval);			
		Thread.sleep(frameInterval);			
	}	
	
	/**
	 * @describe: .读串口命令 - 对发送AT这个指令,返回OK就是成功
	 * @return: true:成功 false:失败 
	 */
	public boolean readByte(String portStr) throws Exception{
		boolean rsbool = false;
		String rsStr = "";
		
		// 读取数据
		char[] rsByte = final_Level.readPackData();
		if (rsByte != null){
			// 打印收到的串口数据-16进制显示
			for (char c : rsByte) {
				rsStr += c; 
			}
			if (rsStr.indexOf("OK")>0){
				System.out.println("找到" + portStr + ":短信模块串口");
				rsbool = true;
			}
		}else{
			System.out.println(portStr + ":不是短信模块串口");
		}
		// 处理收到的数据
		
		return rsbool;
	}
	
	/**
	 * @describe: 关闭串口，释放资源
	 * @date:2009-11-5
	 */
	public void closePort() {
		final_Level.closePort();
	}	
	
	//---------------工具方法---------------//
	/**
	 * @describe: 获取需要帧之间需要间隔的时间(毫秒) 功能公式(1*12(位)*数据长度*1000/波特率 + 附加毫秒数)--根据自己的程序动态调整
	 * @param appendMillsec	附加毫秒数
	 * @param dataLen	数据区数据长度
	 * @param baudrate	波特率
	 * @return 得到合适的帧发送,间隔毫秒数
	 * @date:2009-11-5
	 */
	public static int getFrameInterval(int appendMillsec, int dataLen, int baudrate) {
		int rsInt = (int) Math.ceil(1 * 12 * (dataLen + 4) * 1000 / (float) baudrate) + appendMillsec;
		return rsInt;
	}
	
	/**
	 * @describe:	把char类型转换成16进制字符串
	 * @param bArray  char类型数组
	 * @date:2009-11-7
	 */
	public static final String bytesToHexString(char[] bArray) {
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2) {
				sb.append(0);
			}
			sb.append(sTemp.toUpperCase() + " ");
		}
		return sb.toString();
	}
	
}

