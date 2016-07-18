package cn.mailchat.chatting.beans;

import cn.mailchat.chatting.protocol.Command;

/**
 * HTTPS发送失败的命令缓存
 * 
 * @Description:
 * @param emial
 * @param group
 * @return
 * @see:
 * @since:
 * @author: shengli
 * @return 
 * @date:2015-1-8
 */
public class PendingHTTPSCommand {
	private int id;
	private Command command;
	private String parameters;
	
	public PendingHTTPSCommand(){
		super();
	}
	
	public PendingHTTPSCommand(int id,Command command,String parameters){
		this.id=id;
		this.command=command;
		this.parameters=parameters;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Command getCommand() {
		return command;
	}
	public void setCommand(Command command) {
		this.command = command;
	}
	public String getParameters() {
		return parameters;
	}
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
}
