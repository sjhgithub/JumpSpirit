package cn.mailchat.chatting.protocol;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTTPushMessage {
	public String topic;
	public MqttMessage message;
	public MQTTPushMessage(String topic,MqttMessage message){
		this.topic=topic;
		this.message=message;
	}
}
