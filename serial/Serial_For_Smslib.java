package serial;

import gnu.io.*;

/**
 * ���� : ���ڲ���:��װ�ײ�
 * ��ʹ�ý���:
 * 		��ȡʵ����getInstance
 * 		�򿪴��ڣ�openPort
 * 		��д���ݣ�writeByte->readByte
 * 		�رմ��ڣ�closePort
 * ע������ : ��
 */
public class Serial_For_Smslib {
	public static Serial_For_Smslib sms_serial;
	public static Level_Final_Serial final_Level;
	
	//��Ҫ���õĲ���
	int portId; 					// ���ںţ��磺com1����portIdΪ1
	int baudrate;					// ������
	
	//	��һ��Ҫ�趨�Ĳ���(��Ĭ��ֵ)
	int timeOut; 					// �ӳ�ʱ��(������)
	int dataBits; 					// ����λ
	int stopBits; 					// ֹͣλ
	int parity; 					// ��ż����
	int funCode; 					// ������	
	int dataLen;					// ���ݳ���
	int appendMillsec; 				// ���㷢�ͼ����---���Ӻ�����
	int bytes;						// ���㷢�ͼ����---�����ֽ���	

	// �Զ�����--���ͼ��
	int frameInterval; 				// ���ݲ����ʣ����ݱ��ʺ����������Զ����÷��ͼ��
	
	// ���췽��
	public Serial_For_Smslib() {
		final_Level = new Level_Final_Serial();
		timeOut = 10;						// �ӳ�ʱ��(������)
		dataBits = SerialPort.DATABITS_8;	// ����λ
		stopBits = SerialPort.STOPBITS_1;	// ֹͣλ
		parity = SerialPort.PARITY_NONE;	// ��ż����
		funCode = 3;						//	��ȡ��ǰ�Ĵ�����һ������������ֵ
		dataLen = 4;						//	���� ��Ҫ��ȡ4������
		appendMillsec = 38;					//	���Ӻ�����(��Ҫ�Լ����Ե���)
		bytes = 20;							//  �������ֽ���
	}

	/**
	 * @describe:  ��ȡ������
	 * @date:2009-11-5
	 */
	public static Serial_For_Smslib getInstance() {
		if (sms_serial == null) {
			sms_serial = new Serial_For_Smslib();
		}
		return sms_serial;
	}

	/**
	 * @describe: �򿪴���
	 * @param portStr ���ں�. ��: COM3
	 * @param baudrate ������
	 * @param appName ����ռ�ó��������
	 * @return: true:�򿪴������� false:�򿪴����쳣
	 */
	public boolean openPort(String portStr, int baudrate, String appName) {
		boolean rsBool = false;
		
		// ��ʼ������
		final_Level.initialize(timeOut, baudrate, dataBits, stopBits, parity);
		final_Level.setAppname(appName.toUpperCase());
		// �򿪴���
		if (final_Level.openPort( portStr)) {
			rsBool = true;
			// ����֮֡��ķ��ͼ��
			this.frameInterval = getFrameInterval(appendMillsec, bytes, baudrate);		
		}
		return rsBool;
	}	
	
	
	/**
	 * @describe: д�������� - ����AT���ָ��
	 * @param rs ���͵�����
	 */
	public void writeByte(char[] rs) throws Exception{
		final_Level.writePort(rs);
		// ��ӡ���͵Ĵ�������-16������ʾ
		// System.out.println(bytesToHexString(rs));
		
		//�ȴ�һ��ʱ��, �Ա�֤����,���㹻��ʱ�䷢�ͺͽ���
		//Thread.sleep(frameInterval);			
		Thread.sleep(frameInterval);			
	}	
	
	/**
	 * @describe: .���������� - �Է���AT���ָ��,����OK���ǳɹ�
	 * @return: true:�ɹ� false:ʧ�� 
	 */
	public boolean readByte(String portStr) throws Exception{
		boolean rsbool = false;
		String rsStr = "";
		
		// ��ȡ����
		char[] rsByte = final_Level.readPackData();
		if (rsByte != null){
			// ��ӡ�յ��Ĵ�������-16������ʾ
			for (char c : rsByte) {
				rsStr += c; 
			}
			if (rsStr.indexOf("OK")>0){
				System.out.println("�ҵ�" + portStr + ":����ģ�鴮��");
				rsbool = true;
			}
		}else{
			System.out.println(portStr + ":���Ƕ���ģ�鴮��");
		}
		// �����յ�������
		
		return rsbool;
	}
	
	/**
	 * @describe: �رմ��ڣ��ͷ���Դ
	 * @date:2009-11-5
	 */
	public void closePort() {
		final_Level.closePort();
	}	
	
	//---------------���߷���---------------//
	/**
	 * @describe: ��ȡ��Ҫ֮֡����Ҫ�����ʱ��(����) ���ܹ�ʽ(1*12(λ)*���ݳ���*1000/������ + ���Ӻ�����)--�����Լ��ĳ���̬����
	 * @param appendMillsec	���Ӻ�����
	 * @param dataLen	���������ݳ���
	 * @param baudrate	������
	 * @return �õ����ʵ�֡����,���������
	 * @date:2009-11-5
	 */
	public static int getFrameInterval(int appendMillsec, int dataLen, int baudrate) {
		int rsInt = (int) Math.ceil(1 * 12 * (dataLen + 4) * 1000 / (float) baudrate) + appendMillsec;
		return rsInt;
	}
	
	/**
	 * @describe:	��char����ת����16�����ַ���
	 * @param bArray  char��������
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

