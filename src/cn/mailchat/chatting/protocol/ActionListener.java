/*******************************************************************************
 * Copyright (c) 1999, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 */
package cn.mailchat.chatting.protocol;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttToken;

import cn.mailchat.Account;
import cn.mailchat.MailChat;
import cn.mailchat.chatting.beans.CMessage;

import android.util.Log;

public class ActionListener implements IMqttActionListener {

	private Action action;
	private SuccessfulFailureStateCallBack callBack;
	private Account account;
	private MQTTCommand command;
	private Object data;
	private Connection mConnection;

	public enum Action {
		/** Connect Action **/
		CONNECT,
		/** Disconnect Action **/
		DISCONNECT,
		/** Subscribe Action **/
		SUBSCRIBE,
		/** Publish Action **/
		PUBLISH,
		/** unSubscribe Action **/
		UNSUBSCRIBE
	}

	public ActionListener(Action action,
			SuccessfulFailureStateCallBack callBack, Account account,
			MQTTCommand command, Object data) {
		this.action = action;
		this.callBack = callBack;
		this.account = account;
		this.command = command;
		this.data = data;
		mConnection = Connection.getInstance(MailChat.app);
	}

	@Override
	public void onSuccess(IMqttToken asyncActionToken) {
		switch (action) {
		case CONNECT:
			connectSuccess();
			callBack.ConnectSuccess(command);
			break;
		case DISCONNECT:
			disconnectSuccess();
			callBack.DisConnectSuccess();
			break;
		case SUBSCRIBE:
			callBack.SubscribeSuccess(account, command, data);
			break;
		case PUBLISH:
			callBack.PublishSuccess(account, command, data);
			break;
		case UNSUBSCRIBE:
			callBack.unSubscribeSuccess(account, command, data);
			break;
		default:
			break;
		}
	}

	@Override
	public void onFailure(IMqttToken token, Throwable exception) {

		switch (action) {
		case CONNECT:
			connectFailed(exception);
			callBack.ConnectFail(command);
			break;
		case DISCONNECT:
			disconnectFailed(exception);
			callBack.DisConnectFail();
			break;
		case SUBSCRIBE:
			callBack.SubscribeFail(account, command, data);
			break;
		case PUBLISH:
			callBack.PublishFail(account, command, data);
			break;
		case UNSUBSCRIBE:
			callBack.unSubscribeFail(account, command, data);
			break;
		default:
			break;
		}
	}

	@Override
	public void onSuccessByTime(IMqttToken asyncActionToken, long time) {
		// TODO Auto-generated method stub
		switch (action) {
		case PUBLISH:
			callBack.PublishSuccessByTime(account, command, data, time);
			break;
		default:
			break;
		}
	}

	/**
	 * A disconnect action was unsuccessful, notify user and update client
	 * history
	 * 
	 * @param exception
	 *            This argument is not used
	 */
	private void disconnectFailed(Throwable exception) {
		Log.e("qxian", ">>>>" + exception.toString());
		mConnection
				.changeAndSendbroadcastStatus(Connection.ConnectionStatus.DISCONNECTED);
	}

	/**
	 * A connect action was unsuccessful, notify the user and update client
	 * history
	 * 
	 * @param exception
	 *            This argument is not used
	 */
	private void connectFailed(Throwable exception) {
		mConnection.getClient().unregisterResources();
		mConnection
				.changeAndSendbroadcastStatus(Connection.ConnectionStatus.ERROR);
	}

	/**
	 * A connection action has been successfully completed, update the
	 * connection object associated with the client this action belongs to and
	 * then notify the user of success.
	 */
	private void connectSuccess() {
		mConnection
				.changeAndSendbroadcastStatus(Connection.ConnectionStatus.CONNECTED);
	}

	/**
	 * A disconnection action has been successfully completed, update the
	 * connection object associated with the client this action belongs to and
	 * then notify the user of success.
	 */
	private void disconnectSuccess() {
		mConnection
				.changeAndSendbroadcastStatus(Connection.ConnectionStatus.DISCONNECTED);
	}
}