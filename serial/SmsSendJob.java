package serial;

import java.util.*;

/**
 * 作用 : smslib 发送短信,自动匹配串口 <br>
 * 类使用介绍:	获取实例:<code>getInstance</code>
 * 				设置波特率:<code>setBaudrate</code><br>
 * 				启动服务:<code>startService</code>已经启动短信接收事件<br>
 *				结束服务:<code>stopService</code><br>
 *				打印网络信息:<code>smsInfo</code><br>
 *				
 *				发送短信: <code>sendMessage</code><br>
 * 注意事项 : 无
 */
public class SmsSendJob{
	
	private static SmsSendJob smsSendJob = new SmsSendJob(); //本类单例
	/**
	 * 用于动态测试短信串口号
	 */
	private Serial_For_Smslib smslib_test = new Serial_For_Smslib();
	/**
	 * 发送短信的服务
	 */
	private SmsService smsService = new SmsService();
	/**
	 * 短信发送模块专用的端口名
	 */
	private static String SMSAPPNAME = "wavecom";//"sms_port";
	/**
	 * 波特率
	 */
	private int baudrate = 115200;
	/**
	 * 串口字符(如:COM1)
	 */
	private String comStr = "COM3";	
	
	/*---------------------------测试合适的串口号-------------------------------*/
	/**
	 * @describe: 获取SerialBeanl类单例
	 * @date:2009-11-5
	 */
	public static SmsSendJob getInstance() {
		return smsSendJob;
	}	
	
	/**
	 * @describe: 设置波特率	和 串口字符
	 * @param baudrate: 波特率
	 * @param comStr: 串口字符
	 * @date:2010-3-2
	 */
	public void initial(int baudrate, String comStr) {
		this.baudrate = baudrate;
		this.comStr = comStr;
	}

	/**
	 * @describe: 动态检测适合短信模块的-串口字符(如:COM1)
	 * @date:2009-11-22
	 */
	public String getRightComStr(){
		String rsCom = null;
		
		//获取final_Serial实例--扫描端口数量,并逐个测试
		List<String> portList = Level_Final_Serial.getAllComPorts();
		if (portList.size() <= 0){
			// 没有发现任何串口
		}else{
			// 逐个扫描测试连通性
			for (String portStr : portList) {
				// 测试串口的是否适合短信模块
				if (testSms(portStr)){
					rsCom = portStr;
					break;
				}
			}
		}
		return rsCom;
	}
	
	/**
	 * @describe: 测试串口的是否适合短信模块
	 * @param portStr: 串口号. 如:COM3
	 * @return: null:失败 其他:成功 
	 */
	public boolean testSms(String portStr){
		boolean rsBool = false;
		try {
			// ①  打开端口
			rsBool = smslib_test.openPort(portStr, baudrate, SMSAPPNAME);
			// ②  串口写
			String atCommand = "AT\r";		// 发送AT指令(加换行符号\r) 
			char[] atOrder = atCommand.toCharArray();
			if (rsBool) smslib_test.writeByte(atOrder);
			if (rsBool) {
				// ③  串口读(根据得到的数据,判断返回数据的连通性{返回字符包含OK表示成功})
				rsBool = smslib_test.readByte(portStr);
				// ④ 关闭串口
				smslib_test.closePort();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rsBool;
	}		
	
	/*---------------------------发送短信-------------------------------*/
	
	public char[] strToCharArrary(String str){
		char[] rsChar = str.toCharArray();
		return rsChar;
	}
	
	/**
	 * @describe: 为发送和接收短信做好准备
	 * @return: true:成功 false:失败
	 * @date:2010-3-2
	 */
	public boolean readyToSendMsg(){
		boolean rsbool = false;
		rsbool = smsService.initial(comStr, baudrate, "0000");
		if (rsbool) rsbool = smsService.startService();
		return rsbool;
	}
	
	/**
	 * @describe: 给指定的一组手机号码,发送短信
	 * @param phoneList 手机号码列表
	 * @param message 信息内容
	 * @return: true:成功 false:失败
	 * @date:2010-3-2
     */
	public boolean sendMessage(List<String> phoneList, String message){
		boolean rsbool = smsService.sendMessage(phoneList, message);
		return rsbool;
	}
	
	/**
	 * @describe: 打印sms信息	
	 * @date:2010-3-2
	 */
	public void printSmsInof() throws Exception{
		smsService.smsInfo();
	}
	
	/**
	 * @describe: 停止服务
	 * @return: true:成功 false:失败
	 * @date:2010-3-1
	 */	
	public void stopService(){
		smsService.stopService();
	}
	
	public static void main(String[] args) throws Exception {
		
		SmsSendJob smsSendJob = SmsSendJob.getInstance();	// 运行实例
		String comName =smsSendJob.getRightComStr(); 		// 获取合适短信模块的 串口字符
		
		if (comName != null){
			smsSendJob.initial(115200, comName);				// 设置波特率和串口字符
			if (smsSendJob.readyToSendMsg()){				// 准备 - ok
				// smsSendJob.printSmsInof();					// 打印sms信息
				List<String> phoneList = new ArrayList<String>();
				phoneList.add("17718472823");
				String message = "mlgb"; // 给10086发一条查询余额的短信						
				smsSendJob.sendMessage(phoneList, message);
				//Thread.sleep(60 * 1000);  						// 一分钟后,关闭短信服务
				// 接收短信在SmsService中已经注册,InboundNotification中process会处理
				// 收短信后,默认会删除收到的短信,也可以通过setRec_msg_remove(boolean)修改
			}
			smsSendJob.stopService();
		}else{
			System.out.println("没有找到合适的串口号");
		}
	}
	
}
