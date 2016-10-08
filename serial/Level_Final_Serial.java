package serial;

import gnu.io.*;
import java.io.*;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ����: ���ڶ�д����(�ײ�)
 * �ٴ����ļ�λ�÷���: rxtxSerial.dll ����%java_home%\bin��<br>
 * ����ʹ�ý���:	��ʼ��:getInstance->setPortName->initialize
 * 				�򿪴���:openPort
 *				��д����:writePort->readPackData
 *				�رմ���:ClosePort
 *				ö��ȫ����������:getAllComPorts
 * ע������: ��
 */
public class Level_Final_Serial {
	/**
	 * ���ݰ�����
	 */
	public static final int PACKET_LENGTH = 500;
	
	private Log log = LogFactory.getLog(Level_Final_Serial.class);
	public static Level_Final_Serial final_Level;

	SerialPort serialPort;
	CommPortIdentifier identifier;
	String PortName;				// ������.��:COM3
	OutputStream out;				// ���������
	InputStream in;					// ����������
	String appname = "SerialBean"; 	// ������
	int timeOut; 					// �ӳ�ʱ��(������)
	int baudrate; 					// ������
	int dataBits; 					// ����λ
	int stopBits; 					// ֹͣλ
	int parity; 					// ��ż����

	/**
	 * @describe: ��ȡSerialBeanl�൥��
	 * @date:2009-11-5
	 */
	public static Level_Final_Serial getInstance() {
		if (final_Level == null) {
			final_Level = new Level_Final_Serial();
		}
		return final_Level;
	}	
	
	/**
	 * ���캯��
	 */
	public Level_Final_Serial() {
	}
	
	/**
	 * @describe: ���� ���ڳ�����
	 * @date:2010-3-2
	 */
	public void setAppname(String appname) {
		this.appname = appname;
	}	

	/**
	 * @describe: ��ʼ����
	 * @param timeOut  �ȴ�ʱ��
	 * @param baudrate	������
	 * @param dataBits	����λ
	 * @param stopBits	ֹͣλ
	 * @param parity	��ż����
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
	 * @describe: ��ʼ��������ϸ��Ϣ
	 * @return true : ��ʼ�����ڳɹ� false: ��ʼ������ʧ�� 
	 * @date:2009-11-5
	 */
	public boolean openPort(String portName) {
		boolean rsBool = false;
		this.PortName = portName;
		
		try {
			//��ȡ����
			identifier = getCommPort();

			if (identifier == null) {
				// null
			} else {
				if (identifier.isCurrentlyOwned()){
					log.info(PortName+ ": �����Ѿ���" + identifier.getCurrentOwner()+ "ռ��...");
				}else{
					// open������ͨѶ�˿�
					serialPort = (SerialPort) identifier.open(appname, timeOut);
					
					// ��ȡ�˿ڵ�����,��������� 
					in = serialPort.getInputStream();
					out = serialPort.getOutputStream();
					
					// ���ô��ڳ�ʼ�������������ǲ����ʣ�����λ��ֹͣλ��У�� 
					serialPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
					serialPort.setDTR(true);
					serialPort.setRTS(true);
					
					rsBool = true;
				}
			}
		} catch (PortInUseException e) {
			log.info(PortName+ ": �����Ѿ���" + identifier.getCurrentOwner()+ "ռ��...");
		} catch (Exception e) {
			log.info(PortName+ "--��ʼ�����ڳ���:" + e.toString());
		}

		return rsBool;
	}
	
	/**
	 * @describe: �оٲ��õ���Ҫ�ô���
	 * @date:2009-11-5
	 */
	public CommPortIdentifier getCommPort() throws Exception {
		CommPortIdentifier portIdRs = null;
		portIdRs = CommPortIdentifier.getPortIdentifier(PortName);
		return portIdRs;
	}	

	/**
	 * @describe: ��ȡ��������
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
	 * @describe: �򴮿�д���� char[] bytes
	 * @date:2009-11-5
	 */
	public void writePort(char[] bytes) throws IOException {
		for (char b : bytes) {
			writePort(b);
		}
	}

	/**
	 * @describe: �򴮿�д���� char bytes
	 * @date:2009-11-5
	 */
	public void writePort(char b) throws IOException {
		out.write(b);
		out.flush();
	}

	/**
	 * @describe: �رմ���,�ͷ���Դ
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
				log.info("�رմ���ʱ����:" + e.toString());
			}
		}
		if (serialPort != null) {
			serialPort.close();
			serialPort = null;
		}
	}
	
	/**
	 * @describe: �о�ȫ����������
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
