package com.semptian.serial;

import java.util.List;

public class SMSBean {
	private List<String> numbers;
	private String msg;
	public List<String> getNumbers() {
		return numbers;
	}
	public void setNumbers(List<String> numbers) {
		this.numbers = numbers;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public SMSBean() {
		super();
		// TODO Auto-generated constructor stub
	}
	public SMSBean(List<String> numbers, String msg) {
		super();
		this.numbers = numbers;
		this.msg = msg;
	}
	
}
