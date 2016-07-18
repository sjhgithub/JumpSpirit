package com.c35.mtd.pushmail.command.response;

import com.c35.mtd.pushmail.exception.MessagingException;

/**
 * 
 * @Description:描述 服务器响应的基类
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class BaseResponse {

	int commandId = -1;
	String commandName = "";
	int commandCode = -1;
	String commandMessage = "";

	public int getCommandId() {
		return commandId;
	}

	public void setCommandId(int commandId) {
		this.commandId = commandId;
	}

	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}

	public int getCommandCode() {
		return commandCode;
	}

	public void setCommandCode(int commandCode) {
		this.commandCode = commandCode;
	}

	public String getCommandMessage() {
		return commandMessage;
	}

	public void setCommandMessage(String commandMessage) {
		this.commandMessage = commandMessage;
	}

	public BaseResponse() {
	}

	/**
	 * sequenceid 
	 * CommandName 
	 * resultCode: Result code 200=成功,其它为各自的错误编号 
	 * resultContent: Response result结果内容
	 * (明文：JSON格式 压缩：根据命令传入参数决定是否)
	 * 例：用,号分隔
	 * 1, Command,200,{aaa:’aaaa’,bbb:[‘aaa’,’bbb’,’ccc’]}
	 * CODE_OK	200	成功
	 * CODE_COMMAND_NO	404	命令没找到
	 * CODE_VERSION_NO	901	版本号不正确，不支持
	 * CODE_LOGIN_NO	902	登录不正确
	 * CODE_AUTH_NO	903	没有通过认证(ticket失效时)
	 * CODE_COMMAND_ERROR	904	命令格式错误
	 * CODE_DATA_ERROR	908	入参格式不正确
	 * @Description:
	 * @param response
	 * @throws MessagingException
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-5-16
	 */
	public void initFeild(String response) throws MessagingException {
		if (response != null && !response.trim().equals("") && response.contains(",")) {
			commandId = Integer.parseInt(response.substring(0, response.indexOf(",")));
			response = response.substring(response.indexOf(",") + 1);
			if (response.contains(",")) {
				commandName = response.substring(0, response.indexOf(","));
			}
			response = response.substring(response.indexOf(",") + 1);
			if (response.contains(",")) {
				commandCode = Integer.parseInt(response.substring(0, response.indexOf(",")));
			}
			commandMessage = response = response.substring(response.indexOf(",") + 1);
		}
	}

	@Override
	public String toString() {
		return "commandId = " + commandId + "  commandName =" + commandName + " commandCode = " + commandCode + "  commandMessage =  " + commandMessage;
	}
}