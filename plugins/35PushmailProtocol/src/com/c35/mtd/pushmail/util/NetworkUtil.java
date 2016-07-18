package com.c35.mtd.pushmail.util;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

/**
 * 判断网络情况工具类
 * @author:gongfacun
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class NetworkUtil {

	/**
	 * wifi 是否连接或正在连接
	 * @return
	 * @see: 
	 * @since: 
	 * @author: cuiwei
	 * @date:2013-12-5
	 */
	public static boolean isWifi() {
		try {
			ConnectivityManager conMan = (ConnectivityManager) EmailApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
			State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
			if (wifi == State.CONNECTING || wifi == State.CONNECTED) {
				return true;
			}
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
			return false;
		}
		return false;
	}

	/**
	 * 判断连接的网络是否是2g/3g
	 * @return
	 */
	public static boolean isMobile() {
		try {
			ConnectivityManager conMan = (ConnectivityManager) EmailApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
			State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
			if (wifi != State.CONNECTED) {
				State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
				if (mobile == State.CONNECTED) {
					return true;
				}
			}
		} catch (Exception e) {//java.lang.NullPointerException
			Debug.e("failfast", "failfast_AA", e);
			return false;
		}
		return false;

	}

	/**
	 * 判断是否有网络连接
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-10-17
	 */
	public static boolean isNetworkAvailable() {
		try {
			ConnectivityManager connect = (ConnectivityManager) EmailApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connect == null) {
				return false;
			} else {
				NetworkInfo[] info = connect.getAllNetworkInfo();
				if (info != null) {
					for (int i = 0; i < info.length; i++) {
						if (info[i].getState() == NetworkInfo.State.CONNECTED) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
			return false;
		}
		return false;
	}
}
