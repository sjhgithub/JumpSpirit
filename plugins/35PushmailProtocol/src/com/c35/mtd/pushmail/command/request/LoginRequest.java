package com.c35.mtd.pushmail.command.request;

/**
 * 
 * @Description:Login命令请求
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class LoginRequest extends BaseRequest {

	private String pwd;
	private String clientProductType;
	private int screenWidth;
	private int screenHeight;

	public String getClientProductType() {
		return clientProductType;
	}

	public void setClientProductType(String clientProductType) {
		this.clientProductType = clientProductType;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
}
