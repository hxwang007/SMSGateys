package com.semptian;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.apache.log4j.Logger;

import com.semptian.serial.SMSBean;
import com.semptian.serial.SmsSendJob;

@WebService
public class sendService  {
	static SmsSendJob smsSendJob;
	static boolean isReady;
	/**
	 * 供客户端调用的方法
	 * @param name
	 * @return
	 */
    public void send(List<String> numbers,String msg){
    	if(isReady){
    		SMSBean bean = new SMSBean(numbers,msg);
    		smsSendJob.sendQueue.offer(bean);
    		System.out.println(msg);
    	}else{
    		
    	}
    }
    
	public static void main(String[] args) {
		smsSendJob=SmsSendJob.getInstance();
		String comName =smsSendJob.getRightComStr(); 		// 获取合适短信模块的 串口字符
		
		if (comName != null){
			smsSendJob.initial(115200, comName);				// 设置波特率和串口字符
			if (smsSendJob.readyToSendMsg()){				// 准备 - ok
				 isReady = true;
			}
		}
		Endpoint.publish("http://0.0.0.0:9999/Service/sendService", new sendService());
		System.out.println("service success");
		//smsSendJob.stopService();
		ExecutorService threadPool
		=Executors.newFixedThreadPool(1);
		Handler handler = new Handler(smsSendJob);
		threadPool.execute(handler);
		
		
	}

	
}

