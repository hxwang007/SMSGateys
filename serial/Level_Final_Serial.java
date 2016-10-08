package serial;

import gnu.io.*;
import java.io.*;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 作用: 串口读写数据(底层)
 * ①串口文件位置放置: rxtxSerial.dll 放在%java_home%\bin下<br>
 * ②类使用介绍:	初始化:getInstance->setPortName->initialize
 * 				打开串口:openPort
 *				读写数据:writePort->readPackData
 *				关闭串口:ClosePort
 *				枚举全部串口名称:getAllComPorts
 * 注意事项: 无
 */
public class Level_Final_Serial {
	/**
	 * 数据包长度
	 */
	public static final int PACKET_LENGTH = 500;
	
	private Log log = LogFactory.getLog(Level_Final_Serial.class);
	public static Level_Final_Serial final_Level;

	SerialPort serialPort;
	CommPortIdentifier identifier;
	String PortName;				// 串口名.如:COM3
	OutputStream out;				// 串口输出流
	InputStream in;					// 串口输入流
	String appname = "SerialBean"; 	// 程序名
	int timeOut; 					// 延迟时间(毫秒数)
	int baudrate; 					// 波特率
	int dataBits; 					// 数据位
	int stopBits; 					// 停止位
	int parity; 					// 奇偶检验

	/**
	 * @describe: 获取SerialBeanl类单例
	 * @date:2009-11-5
	 */
	public static Level_Final_Serial getInstance() {
		if (final_Level == null) {
			final_Level = new Level_Final_Serial();
		}
		return final_Level;
	}	
	
	/**
	 * 构造函数
	 */
	public Level_Final_Serial() {
	}
	
	/**
	 * @describe: 设置 串口程序名
	 * @date:2010-3-2
	 */
	public void setAppname(String appname) {
		this.appname = appname;
	}	

	/**
	 * @describe: 初始化类
	 * @param timeOut  等待时间
	 * @param baudrate	波特率
	 * @param dataBits	数据位
	 * @param stopBits	停止位
	 * @param parity	奇偶检验
	 * @date:2009-11-5
	 */
	public void initialize(int timeOut, int baudrate, int dataBits, int stopBits, int parity) {
		this.timeOut = timeOut;
		this.baudrate = baudrate;
		this.dataBits = dataBits;
		this.stopBits = stopBits;
		this.parity = parity;
	}	

	/**
	 * @describe: 初始化串口详细信息
	 * @return true : 初始化串口成功 false: 初始化串口失败 
	 * @date:2009-11-5
	 */
	public boolean openPort(String portName) {
		boolean rsBool = false;
		this.PortName = portName;
		
		try {
			//获取串口
			identifier = getCommPort();

			if (identifier == null) {
				// null
			} else {
				if (identifier.isCurrentlyOwned()){
					log.info(PortName+ ": 串口已经被" + identifier.getCurrentOwner()+ "占用...");
				}else{
					// open方法打开通讯端口
					serialPort = (SerialPort) identifier.open(appname, timeOut);
					
					// 获取端口的输入,输出流对象 
					in = serialPort.getInputStream();
					out = serialPort.getOutputStream();
					
					// 设置串口初始化参数，依次是波特率，数据位，停止位和校验 
					serialPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
					serialPort.setDTR(true);
					serialPort.setRTS(true);
					
					rsBool = true;
				}
			}
		} catch (PortInUseException e) {
			log.info(PortName+ ": 串口已经被" + identifier.getCurrentOwner()+ "占用...");
		} catch (Exception e) {
			log.info(PortName+ "--初始化串口出错:" + e.toString());
		}

		return rsBool;
	}
	
	/**
	 * @describe: 列举并得到需要用串口
	 * @date:2009-11-5
	 */
	public CommPortIdentifier getCommPort() throws Exception {
		CommPortIdentifier portIdRs = null;
		portIdRs = CommPortIdentifier.getPortIdentifier(PortName);
		return portIdRs;
	}	

	/**
	 * @describe: 读取串口数据
	 * @date:2009-11-5
	 */
	public char[] readPackData() throws Exception {
		byte[] readBuffer = new byte[PACKET_LENGTH];
		char[] msgPack = null;
		int numBytes = 0;

		while (in.available() > 0) {
			numBytes = in.read(readBuffer);
			msgPack = null;
			msgPack = new char[numBytes];
			for (int i = 0; i < numBytes; i++) {
				msgPack[i] = (char) (readBuffer[i] & 0xFF);
			}
		}
		return msgPack;
	}

	/**
	 * @describe: 向串口写数据 char[] bytes
	 * @date:2009-11-5
	 */
	public void writePort(char[] bytes) throws IOException {
		for (char b : bytes) {
			writePort(b);
		}
	}

	/**
	 * @describe: 向串口写数据 char bytes
	 * @date:2009-11-5
	 */
	public void writePort(char b) throws IOException {
		out.write(b);
		out.flush();
	}

	/**
	 * @describe: 关闭串口,释放资源
	 * @date:2009-11-5
	 */
	public void closePort() {
		if (out != null) {
			try {
				out.close();
				in.close();
				out = null;
				in = null;
			} catch (IOException e) {
				log.info("关闭串口时出错:" + e.toString());
			}
		}
		if (serialPort != null) {
			serialPort.close();
			serialPort = null;
		}
	}
	
	/**
	 * @describe: 列举全部串口名称
	 * @date:2009-11-22
	 */
	public static List<String> getAllComPorts(){
		List<String> comList = new ArrayList<String>();
		Enumeration en = CommPortIdentifier.getPortIdentifiers();
		CommPortIdentifier portIdRs = null;
		
		while (en.hasMoreElements()) {
			portIdRs = (CommPortIdentifier) en.nextElement();
			if (portIdRs.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				comList.add(portIdRs.getName());
			}
		}
		return comList;
	}

}
