package com.c35.mtd.pushmail.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;

/**
 * 
 * @Description:手机晃动处理
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-1-7
 */
public class ShakeHanlder {

	private static final String TAG = "ShakeHanlder";
	private SensorManager sensorMgr;// 传感器管理器
	private Sensor sensor;// 传感器
	private SensorEventListener mailSensorEventListener = null;// 传感器监听
	private long lastUpdate = -1;// 时间初始值
	private float x, y, z;// 三维坐标初始值
	private float last_x, last_y, last_z;// 变化后的坐标
	private static final int SHAKE_THRESHOLD = 2000;// 自定义速度，超过这个速度，触发事件
	// boolean isShake = Logic.getConfig().isShakeVibrate();
	private ShakeListener shakeListener = null;

	public ShakeHanlder(ShakeListener listtener) {
		this.shakeListener = listtener;
		this.sensorMgr = (SensorManager) EmailApplication.getInstance().getSystemService(Context.SENSOR_SERVICE);// 获取传感器服务
		this.sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// 取得加速度传感器
		this.mailSensorEventListener = new MailSensorEventListener();

	}

	public interface ShakeListener {

		/**
		 * 
		 * @Description:过滤未读邮件
		 * @see:
		 * @since:
		 * @author: 温楠
		 * @date:2013-1-7
		 */
		public void filterUnreadMessages();
	}

	class MailSensorEventListener implements SensorEventListener {

		@Override
		public void onSensorChanged(SensorEvent event) {

			x = event.values[SensorManager.DATA_X];
			y = event.values[SensorManager.DATA_Y];
			z = event.values[SensorManager.DATA_Z];
			if (sensor != null) {
				// 当前时间
				long curTime = System.currentTimeMillis();
				// 每隔1000ms检测一次
				if ((curTime - lastUpdate) > 500) {
					// 当前与上次变化的时间间隔
					long diffTime = (curTime - lastUpdate);
					lastUpdate = curTime;

					x = event.values[SensorManager.DATA_X];
					y = event.values[SensorManager.DATA_Y];
					z = event.values[SensorManager.DATA_Z];

					// 取得平均加速度
					float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 80000;

					// 如果当前速度大于约定速度，过滤未读邮件
					if (speed > SHAKE_THRESHOLD) {
						if (shakeListener != null) {
							shakeListener.filterUnreadMessages();
							Debug.d(TAG, "You shake the phone!");
						}
					}
					last_x = x;
					last_y = y;
					last_z = z;
				}
			}

		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}

	}

	/**
	 * 
	 * @Description:注销监听
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2013-1-7
	 */
	public void unregistSensor() {
		sensorMgr.unregisterListener(mailSensorEventListener);
	}

	/**
	 * 
	 * @Description:注册监听
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2013-1-7
	 */
	public void registSensor() {
		sensorMgr.registerListener(mailSensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

}
