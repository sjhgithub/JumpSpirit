package com.c35.mtd.pushmail.beans;


public class RecallMailResult {
	private String email;
	private String inGroupEmail;
	private int code;
	public static final int SUCCESS =200;//召回成功
	public static final int FAIL_MAIL_NOEXIST = 100319;//召回邮件失败：邮件不存在
	public static final int FAIL_MAIL_READ = 100320;//召回邮件失败：邮件已阅读，不能召回
	public static final int FAIL_SOME = 100347;//召回部分邮件失败
	public static final int FAIL_RESOURCE_NOEXIST = 100348;//召回邮件失败：原邮件不存在
	public static final int FAIL_DELETE = 100349;//召回邮件失败：删除邮件文件失败
	public static final int FAIL_OTHER_DOMAIN = 100362;//召回邮件异常：外域邮件不能召回

	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getInGroupEmail() {
		return inGroupEmail;
	}
	
	public void setInGroupEmail(String inGroupEmail) {
		this.inGroupEmail = inGroupEmail;
	}
	
	public int getCode() {
		return code;
	}
	
	public void setCode(int code) {
		this.code = code;
	}
	
	

}
