package com.semptian;

import java.util.Queue;

import org.apache.log4j.Logger;

import com.semptian.serial.SMSBean;
import com.semptian.serial.SmsSendJob;

public class Handler extends Thread {
	private Queue<SMSBean> queue;
	private SmsSendJob smsSendJob;
	public Handler(SmsSendJob smsSendJob){
		this.queue=smsSendJob.sendQueue;
		this.smsSendJob = smsSendJob;
	}
   @Override
   public void run(){
	   while(true){
			SMSBean bean =queue.poll();
			if(bean != null){
				String num = "";
	    		for(String str:bean.getNumbers()){
	    			num+=str+" ";
	    		}
	    		long time1 = System.currentTimeMillis();
	    		smsSendJob.sendMessage(bean.getNumbers(), bean.getMsg());
	    		long time2 = System.currentTimeMillis();
	    		Logger.getLogger(sendService.class).info("[" + (time2 - time1) + "ms] ["+num+"]["+bean.getMsg()+"]");
			}
		}
   }
}
