package cn.mailchat.service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.eclipse.paho.client.mqttv3.MqttException;

import cn.mailchat.MailChat;
import cn.mailchat.activity.SetPasswordActivity;
import cn.mailchat.chatting.protocol.ActionListener;
import cn.mailchat.chatting.protocol.MQTTCommand;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.utils.NetUtil;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.text.TextUtils;

public class PendingService extends Service {
	private NetworkConnectionIntentReceiver networkConnectionMonitor;
	private MessagingController controller;
	private HomeKeyEventReceiver mHomeKeyEventReceiver;
	//屏幕点亮,关闭广播监听
	private ScreenReceiver screenReceiver;
	public static boolean screenOn = true;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		controller=MessagingController.getInstance(getApplication());
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		registerBroadcastReceivers();
		if(intent!=null){
			String action = intent.getAction();
			if(action.equals(MessagingController.CHAT_PENDING)){
				controller.executePending();
			}else if(action.equals(MessagingController.NULL_MQTT_SERVICE)){
				reConnection(MQTTCommand.FIRST_CONNECT);
			}else if(action.equals(MessagingController.CHECK_PUSH_STATA)){
				controller.checkAllRegisterPush();
			}else if(action.equals(MessagingController.CHAT_UNSUBSCRIBE_PENDING)){
				controller.executeUnsubscribeAccountPending();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private class NetworkConnectionIntentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(NetUtil.isActive()){
				controller.executePending();
				if(controller.connection.getClient().getClientHandle() == null){
					// MQTT连接服务没有开启，再次联网时开启服务连接
						reConnection(MQTTCommand.FIRST_CONNECT);
				}
				controller.checkAllRegisterPush();
			}
		}
	}
	
	private void registerBroadcastReceivers() {
		if (networkConnectionMonitor == null) {
			networkConnectionMonitor = new NetworkConnectionIntentReceiver();
			registerReceiver(networkConnectionMonitor, new IntentFilter(
					ConnectivityManager.CONNECTIVITY_ACTION));
		}
		if(mHomeKeyEventReceiver==null){
			mHomeKeyEventReceiver=new HomeKeyEventReceiver();
			registerReceiver(mHomeKeyEventReceiver, new IntentFilter(
					 Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		}
		if(screenReceiver==null){
			screenReceiver =new ScreenReceiver();
			registerReceiver(screenReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
			registerReceiver(screenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
			registerReceiver(screenReceiver, new IntentFilter(Intent.ACTION_USER_PRESENT));
		}
  }
	private void unregisterBroadcastReceivers() {
		if (networkConnectionMonitor != null) {
			unregisterReceiver(networkConnectionMonitor);
			networkConnectionMonitor = null;
		}
		if(mHomeKeyEventReceiver !=null){
			unregisterReceiver(mHomeKeyEventReceiver);
			mHomeKeyEventReceiver = null;
		}
		if (screenReceiver != null) {
			unregisterReceiver(screenReceiver);
			screenReceiver = null;
		}
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterBroadcastReceivers();
	}
	private void reConnection(MQTTCommand command){
		try {
			controller.connection.addConnectionOptions(controller.readSSL(false));
			controller.connection.getClient().connect(
					controller.readSSL(false),
					null,
					new ActionListener(
							ActionListener.Action.CONNECT,
							controller,
							null, command,
							null));
		} catch (MqttException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException | NotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private class HomeKeyEventReceiver extends BroadcastReceiver  {
		String SYSTEM_REASON = "reason";
		String SYSTEM_HOME_KEY = "homekey";
		String SYSTEM_HOME_KEY_LONG = "recentapps";
		 
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra(SYSTEM_REASON);
				if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {
					 //表示按了home键,程序到了后台
					if(MailChat.isGesture()&&SetPasswordActivity.ifHasGPassword()){
						SetPasswordActivity.saveGestureUnclock(true);
					}
				}else if(TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG)){
					//表示长按home键,显示最近使用的程序列表
				}
			} 
		}
	};

	private class ScreenReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			if (intent != null) {
				String action = intent.getAction();
				if (Intent.ACTION_SCREEN_ON.equals(action)) {
					screenOn = true;
				} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
					screenOn = false;
					if(MailChat.isGesture()&&SetPasswordActivity.ifHasGPassword()){
						SetPasswordActivity.saveGestureUnclock(true);
					}
				} else if (Intent.ACTION_USER_PRESENT.equals(action)) {
					screenOn = true;
				}
			}
		}
	}
}
