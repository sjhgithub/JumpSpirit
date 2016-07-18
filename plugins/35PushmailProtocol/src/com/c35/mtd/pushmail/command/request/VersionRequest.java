package com.c35.mtd.pushmail.command.request;

/**
 * 
 * @Description:Version命令请求
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class VersionRequest extends BaseRequest {

	private String version;

	/*
	 * public VersionRequest(String version) { // TODO Auto-generated constructor stub this.version = version;
	 * }
	 */

	/*
	 * @Override public void creatRequestCommand(String ticket) { // TODO Auto-generated method stub
	 * setRequestCommand("version version 0 {\"version\":\"" + version + "\"}"); }
	 */

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
