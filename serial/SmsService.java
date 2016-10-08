package serial;

import java.util.*;

import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.ICallNotification;
import org.smslib.IGatewayStatusNotification;
import org.smslib.IInboundMessageNotification;
import org.smslib.IOrphanedMessageNotification;
import org.smslib.IOutboundMessageNotification;
import org.smslib.InboundMessage;
import org.smslib.Library;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.AGateway.GatewayStatuses;
import org.smslib.AGateway.Protocols;
import org.smslib.Message.MessageEncodings;
import org.smslib.Message.MessageTypes;
import org.smslib.modem.SerialModemGateway;

/**
 * @ SmsService.java
 * 作用 : smslib 应用- 发送短信(可以群发) 服务<br>
 * 类使用介绍:	初始化: <code>initial</code><br>
 * 				启动服务:<code>startService</code>已经启动短信接收事件<br>
 *				结束服务:<code>stopService</code><br>
 *				打印网络信息:<code>smsInfo</code><br>
 *				
 *				发送短信: <code>sendMessage</code><br>
 * 注意事项 : 无
 * VERSION       DATE            BY       CHANGE/COMMENT
 * 1.0          2010-3-1     YANGZHONLI       create
 */
public class SmsService {

	private static String SEND_SMS_GROUP = "smsgruop";	// 用于发送短信的组名
	private Service srv; 								// 短信服务对象
	private SerialModemGateway gateway; 				// 网关
	private boolean rec_msg_remove = true;				// 收到,处理短信之后,是否删除当前短信.默认删除
	
	InboundNotification inbound = 
		new InboundNotification(); 						// 接收短信的监听
	OrphanedMessageNotification Orphaned = 
		new OrphanedMessageNotification();				// 中断短信处理(短信字数较多时,会用2条以上是短信,这时候就用到这个了)-好像有点问题,还需要测试

	/**
	 * @describe: 设置:收到,处理短信之后,是否删除当前短信.默认删除
	 * @param rec_msg_flag: true:删除 false:不删除
	 * @date:2010-3-2
	 */
	public void setRec_msg_remove(boolean rec_msg_remove) {
		this.rec_msg_remove = rec_msg_remove;
	}
		
	/**
	 * @describe: 初始化短信模块
	 * @param com 串口号
	 * @param baudRate 波特率
	 * @param pin pin值
	 * @return: true:成功 false:失败
	 * @date:2010-3-1
	 */
	public boolean initial(String com, int baudRate, String pin) {
		boolean rsbool = true;

		this.srv = new Service();
		this.gateway = new SerialModemGateway("modem.com3",com,baudRate, "wavecom", "");
		this.gateway.setOutbound(true);
		this.gateway.setInbound(true);
		this.gateway.setProtocol(Protocols.PDU);
		this.gateway.setSimPin(pin);

		try {
			this.srv.addGateway(gateway);
		} catch (GatewayException e) {
			rsbool = false;
			e.printStackTrace();
		}
		return rsbool;
	}

	/**
	 * @describe: 
	 *	①启动服务 
	 * 	②创建一个用于发送短信的组(名为: smsgruop) 
	 *	③创建事件监听(接收和中断短信处理)
	 * @return: true:成功 false:失败
	 * @date:2010-3-1
	 */
	public boolean startService() {
		boolean rsbool = true;
		try {
			this.srv.startService();
			this.srv.createGroup(SEND_SMS_GROUP);
			// 注册启动短信接收事件 -- 小短信
			this.srv.setInboundMessageNotification(inbound);	
			// 注册启动短信接收事件 -- 大短信
			this.srv.setOrphanedMessageNotification(Orphaned);
			// ... 还可以注册其他事件
		} catch (Exception e) {
			rsbool = false;
			e.printStackTrace();
		}
		return rsbool;
	}

	/**
	 * @describe: 停止服务
	 * @return: true:成功 false:失败
	 * @date:2010-3-1
	 */
	public boolean stopService() {
		boolean rsbool = true;
		try {
			this.srv.stopService();
		} catch (Exception e) {
			rsbool = false;
			e.printStackTrace();
		}
		return rsbool;
	}
	
	/**
	 * @describe: 打印sms信息	
	 * @date:2010-3-1
	 */
	public void smsInfo() throws Exception{
		System.out.println();
		System.out.println("smslib Version: " + Library.getLibraryVersion());		
		System.out.println("Modem Information:");
		System.out.println("  Manufacturer: " + gateway.getManufacturer());
		System.out.println("  Model: " + gateway.getModel());
		System.out.println("  Serial No: " + gateway.getSerialNo());
		System.out.println("  SIM IMSI: " + gateway.getImsi());
		System.out.println("  Signal Level: " + gateway.getSignalLevel() + "%");
		System.out.println("  Battery Level: " + gateway.getBatteryLevel() + "%");
		System.out.println("  SmscNumber: " + gateway.getSmscNumber());
		System.out.println();		
	}		

	/**
	 * @describe: 给指定的一组手机号码,发送短信
	 * @param phoneList 手机号码列表
	 * @param message 信息内容
	 * @return: true:成功 false:失败
	 * @date:2010-3-1
     */
	public boolean sendMessage(List<String> phoneList, String message) {
		boolean rsbool = true;

		// 把手机号码逐个加入到短信发送组中
		for (String phone : phoneList) {
			this.srv.addToGroup(SEND_SMS_GROUP, phone);
		}
		OutboundMessage msg = new OutboundMessage(SEND_SMS_GROUP, message);
		msg.setEncoding(MessageEncodings.ENCUCS2);
		
		try {
			this.srv.sendMessage(msg);

			// 发送完短信,把手机号码逐个从短信发送组中移除
			for (String phone : phoneList) {
				this.srv.removeFromGroup(SEND_SMS_GROUP, phone);
			}
		} catch (Exception e) {
			rsbool = false;
			e.printStackTrace();
		}
		return rsbool;
	}

	/**
	 * 作用 : 收短信的监听,并删除收到的短信
	 */
	public class InboundNotification implements IInboundMessageNotification {
		public void process(String gatewayId, MessageTypes msgType,InboundMessage msg) {
			if (msgType == MessageTypes.INBOUND)
				System.out.println(">>>收到短信① New Inbound message detected from Gateway: " + gatewayId);
			else if (msgType == MessageTypes.STATUSREPORT)
				System.out.println(">>>收到短信② New Inbound Status Report message detected from Gateway: " + gatewayId);
			System.out.println(msg);
			//System.out.println("时间:" +  msg.getDate().toLocaleString());
			//System.out.println("短信内容:" + msg.getText());
			//System.out.println("发件号码:" + msg.getOriginator());
			try {
				if (rec_msg_remove){ // 删除收到的短信
					srv.deleteMessage(msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		
	}
	
	/**
	 * 作用 : 当有人向您发送一个大消息，这个消息来的部分。有时，网络的原因不明，<br>
	 * 	一个大消息的某些部分没有到达你的，所以这个问题的消息是完全没有收到。<br>
	 *  这些孤儿的消息部分留在您的手机，消耗内存。 <br>
	 *  如果您收到太多的“不完整”的消息部分，这些都可能需要您的调制解调器的内存 <br> 
	 *  有效地禁用接收它的任何其他消息。<br>
	 *  
	 *  具体通知方法需要返回<code>true或false</code> ：如果您返回true ，该邮件的部分将被删除 ，以夺回失去的调制解调器内存<br>
	 */
	public class OrphanedMessageNotification implements	IOrphanedMessageNotification {
		public boolean process(String gatewayId, InboundMessage msg) {
			System.out.println(">>> 大消息接收 Orphaned message part detected from " + gatewayId);
			System.out.println(msg);
			// Since we are just testing, return FALSE and keep the orphaned
			// message part.
			return false;
		}

		
	}
		
	/**
	 * 作用 : 发短信的监测
	 */
	public class OutboundNotification implements IOutboundMessageNotification{
		public void process(String gatewayId, OutboundMessage msg){
			System.out.println("Outbound handler called from Gateway: " + gatewayId);
			System.out.println(msg);
		}

		
	}		
	
	/**
	 * 作用 : 接到电话的监听
	 */
	public class CallNotification implements ICallNotification {
		public void process(String gatewayId, String callerId) {
			System.out.println(">>> New call detected from Gateway: " + gatewayId + " : " + callerId);
		}

		
	}

	/**
	 * 作用 : 网关变动的监听
	 */
	public class GatewayStatusNotification implements IGatewayStatusNotification {
		public void process(String gatewayId, GatewayStatuses oldStatus, GatewayStatuses newStatus) {
			System.out.println(">>> Gateway Status change for " + gatewayId + ", OLD: " + oldStatus + " -> NEW: " + newStatus);
		}

		
	}

}
