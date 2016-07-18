package cn.mailchat.chatting.beans;

import cn.mailchat.chatting.protocol.ActionListener.Action;
import cn.mailchat.chatting.protocol.MQTTCommand;

/**
 * MQTT发送失败的命令缓存
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
public class PendingMQTTConmmand {
	private int id;
	private Action action;
	private MQTTCommand command;
	private String topic;
	private String content;
	public PendingMQTTConmmand(){
		super();
	}
	public PendingMQTTConmmand(int id,Action action,MQTTCommand command,String topic,String content){
		this.id=id;
		this.action=action;
		this.command=command;
		this.topic=topic;
		this.content=content;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Action getAction() {
		return action;
	}
	public void setAction(Action action) {
		this.action = action;
	}
	public MQTTCommand getCommand() {
		return command;
	}
	public void setCommand(MQTTCommand command) {
		this.command = command;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

}
