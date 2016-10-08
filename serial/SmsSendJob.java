package serial;

import java.util.*;

/**
 * ���� : smslib ���Ͷ���,�Զ�ƥ�䴮�� <br>
 * ��ʹ�ý���:	��ȡʵ��:<code>getInstance</code>
 * 				���ò�����:<code>setBaudrate</code><br>
 * 				��������:<code>startService</code>�Ѿ��������Ž����¼�<br>
 *				��������:<code>stopService</code><br>
 *				��ӡ������Ϣ:<code>smsInfo</code><br>
 *				
 *				���Ͷ���: <code>sendMessage</code><br>
 * ע������ : ��
 */
public class SmsSendJob{
	
	private static SmsSendJob smsSendJob = new SmsSendJob(); //���൥��
	/**
	 * ���ڶ�̬���Զ��Ŵ��ں�
	 */
	private Serial_For_Smslib smslib_test = new Serial_For_Smslib();
	/**
	 * ���Ͷ��ŵķ���
	 */
	private SmsService smsService = new SmsService();
	/**
	 * ���ŷ���ģ��ר�õĶ˿���
	 */
	private static String SMSAPPNAME = "wavecom";//"sms_port";
	/**
	 * ������
	 */
	private int baudrate = 115200;
	/**
	 * �����ַ�(��:COM1)
	 */
	private String comStr = "COM3";	
	
	/*---------------------------���Ժ��ʵĴ��ں�-------------------------------*/
	/**
	 * @describe: ��ȡSerialBeanl�൥��
	 * @date:2009-11-5
	 */
	public static SmsSendJob getInstance() {
		return smsSendJob;
	}	
	
	/**
	 * @describe: ���ò�����	�� �����ַ�
	 * @param baudrate: ������
	 * @param comStr: �����ַ�
	 * @date:2010-3-2
	 */
	public void initial(int baudrate, String comStr) {
		this.baudrate = baudrate;
		this.comStr = comStr;
	}

	/**
	 * @describe: ��̬����ʺ϶���ģ���-�����ַ�(��:COM1)
	 * @date:2009-11-22
	 */
	public String getRightComStr(){
		String rsCom = null;
		
		//��ȡfinal_Serialʵ��--ɨ��˿�����,���������
		List<String> portList = Level_Final_Serial.getAllComPorts();
		if (portList.size() <= 0){
			// û�з����κδ���
		}else{
			// ���ɨ�������ͨ��
			for (String portStr : portList) {
				// ���Դ��ڵ��Ƿ��ʺ϶���ģ��
				if (testSms(portStr)){
					rsCom = portStr;
					break;
				}
			}
		}
		return rsCom;
	}
	
	/**
	 * @describe: ���Դ��ڵ��Ƿ��ʺ϶���ģ��
	 * @param portStr: ���ں�. ��:COM3
	 * @return: null:ʧ�� ����:�ɹ� 
	 */
	public boolean testSms(String portStr){
		boolean rsBool = false;
		try {
			// ��  �򿪶˿�
			rsBool = smslib_test.openPort(portStr, baudrate, SMSAPPNAME);
			// ��  ����д
			String atCommand = "AT\r";		// ����ATָ��(�ӻ��з���\r) 
			char[] atOrder = atCommand.toCharArray();
			if (rsBool) smslib_test.writeByte(atOrder);
			if (rsBool) {
				// ��  ���ڶ�(���ݵõ�������,�жϷ������ݵ���ͨ��{�����ַ�����OK��ʾ�ɹ�})
				rsBool = smslib_test.readByte(portStr);
				// �� �رմ���
				smslib_test.closePort();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rsBool;
	}		
	
	/*---------------------------���Ͷ���-------------------------------*/
	
	public char[] strToCharArrary(String str){
		char[] rsChar = str.toCharArray();
		return rsChar;
	}
	
	/**
	 * @describe: Ϊ���ͺͽ��ն�������׼��
	 * @return: true:�ɹ� false:ʧ��
	 * @date:2010-3-2
	 */
	public boolean readyToSendMsg(){
		boolean rsbool = false;
		rsbool = smsService.initial(comStr, baudrate, "0000");
		if (rsbool) rsbool = smsService.startService();
		return rsbool;
	}
	
	/**
	 * @describe: ��ָ����һ���ֻ�����,���Ͷ���
	 * @param phoneList �ֻ������б�
	 * @param message ��Ϣ����
	 * @return: true:�ɹ� false:ʧ��
	 * @date:2010-3-2
     */
	public boolean sendMessage(List<String> phoneList, String message){
		boolean rsbool = smsService.sendMessage(phoneList, message);
		return rsbool;
	}
	
	/**
	 * @describe: ��ӡsms��Ϣ	
	 * @date:2010-3-2
	 */
	public void printSmsInof() throws Exception{
		smsService.smsInfo();
	}
	
	/**
	 * @describe: ֹͣ����
	 * @return: true:�ɹ� false:ʧ��
	 * @date:2010-3-1
	 */	
	public void stopService(){
		smsService.stopService();
	}
	
	public static void main(String[] args) throws Exception {
		
		SmsSendJob smsSendJob = SmsSendJob.getInstance();	// ����ʵ��
		String comName =smsSendJob.getRightComStr(); 		// ��ȡ���ʶ���ģ��� �����ַ�
		
		if (comName != null){
			smsSendJob.initial(115200, comName);				// ���ò����ʺʹ����ַ�
			if (smsSendJob.readyToSendMsg()){				// ׼�� - ok
				// smsSendJob.printSmsInof();					// ��ӡsms��Ϣ
				List<String> phoneList = new ArrayList<String>();
				phoneList.add("17718472823");
				String message = "mlgb"; // ��10086��һ����ѯ���Ķ���						
				smsSendJob.sendMessage(phoneList, message);
				//Thread.sleep(60 * 1000);  						// һ���Ӻ�,�رն��ŷ���
				// ���ն�����SmsService���Ѿ�ע��,InboundNotification��process�ᴦ��
				// �ն��ź�,Ĭ�ϻ�ɾ���յ��Ķ���,Ҳ����ͨ��setRec_msg_remove(boolean)�޸�
			}
			smsSendJob.stopService();
		}else{
			System.out.println("û���ҵ����ʵĴ��ں�");
		}
	}
	
}
