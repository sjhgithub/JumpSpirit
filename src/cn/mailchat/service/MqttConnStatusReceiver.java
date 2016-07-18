package cn.mailchat.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import cn.mailchat.chatting.protocol.Connection;

/**
 * @author ：zhangjx
 * @copyright © mailchat.cn
 * @create Date: 2016-04-25
 * @Time: 17:18
 * @Modified by：zhangjx
 * @Description :mqtt连接状态广播
 */
public class MqttConnStatusReceiver extends BroadcastReceiver {
	private List<StatusHandler> statusHandlers = new ArrayList<StatusHandler>();

	public void registerHandler(StatusHandler handler) {
		if (!statusHandlers.contains(handler)) {
			statusHandlers.add(handler);
		}
	}

	public void unregisterHandler(StatusHandler handler) {
		if (statusHandlers.contains(handler)) {
			statusHandlers.remove(handler);
		}
	}

	public void clearHandlers() {

		statusHandlers.clear();
	}

	public boolean hasHandlers() {
		return statusHandlers.size() > 0;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle notificationData = intent.getExtras();
		Connection.ConnectionStatus statusCode = Connection.ConnectionStatus.class
				.getEnumConstants()[notificationData
				.getInt(Connection.MQTT_STATUS_CODE)];
		String statusMsg = notificationData
				.getString(Connection.MQTT_STATUS_MSG);
		for (StatusHandler statusHandler : statusHandlers) {
			statusHandler.handleStatus(statusCode, statusMsg);
		}
	}

	public interface StatusHandler {
		public void handleStatus(Connection.ConnectionStatus status,
				String reason);
	}
}