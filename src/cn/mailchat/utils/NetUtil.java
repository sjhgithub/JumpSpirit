package cn.mailchat.utils;

import java.util.Locale;

import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.view.MailDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.provider.Settings;
import android.telephony.TelephonyManager;

public class NetUtil {

	public static boolean isActive() {
		NetworkInfo info = getActiveNetworkInfo();
		return info != null && info.isConnected();
	}

	public static boolean isWifiActive() {
		NetworkInfo info = getActiveNetworkInfo();
		return info != null && info.getType() == ConnectivityManager.TYPE_WIFI;
	}

	public static boolean isMobileActive() {
		NetworkInfo info = getActiveNetworkInfo();
		return info != null
				&& info.getType() == ConnectivityManager.TYPE_MOBILE;
	}

	public static int getNetType() {
		String netType = getNetName();
		int type = 0;
		if ("wifi".equalsIgnoreCase(netType)) {
			type = 1;
		} else if ("2G".equals(netType)) {
			type = 2;
		} else if ("3G".equals(netType)) {
			type = 3;
		}
		return type;
	}

	public static String getNetName() {
		NetworkInfo info = getActiveNetworkInfo();
		Context context = MailChat.app;
		if (info == null || context == null) {
			return "";
		}
		String type = info.getTypeName();
		if ("mobile".equals(type.toLowerCase(Locale.US))) {
			TelephonyManager teleManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			switch (teleManager.getNetworkType()) {
			case TelephonyManager.NETWORK_TYPE_UNKNOWN:
				type = "UNKNOWN"; // 未知网络类型
				break;
			case TelephonyManager.NETWORK_TYPE_GPRS:
				// type = "GPRS"; //GPRS（2G）
				type = "2G";
				break;
			case TelephonyManager.NETWORK_TYPE_EDGE:
				// type = "EDGE"; //通用分组无线服务（2G）
				type = "2G";
				break;
			case TelephonyManager.NETWORK_TYPE_UMTS:
				// type = "UMTS"; //通用移动通信系统 3G（3G）
				type = "3G";
				break;
			case TelephonyManager.NETWORK_TYPE_CDMA:
				// type = "CDMA"; //CDMA网络（电信2G）
				type = "2G";
				break;
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
				// type = "EVDO_0"; //CDMA2000 1xEV-DO revision 0（3G）
				type = "3G";
				break;
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
				// type = "EVDO_A"; //CDMA2000 1xEV-DO revision A（3G）
				type = "3G";
				break;
			case TelephonyManager.NETWORK_TYPE_1xRTT:
				// type = "1xRTT"; //CDMA2000 1xRTT（2G）
				type = "2G";
				break;
			case TelephonyManager.NETWORK_TYPE_HSDPA:
				// type = "HSDPA"; //高速下行分组接入技术（3G）
				type = "3G";
				break;
			case TelephonyManager.NETWORK_TYPE_HSUPA:
				// type = "HSUPA"; //高速上行链路分组接入技术（3G）
				type = "3G";
				break;
			case TelephonyManager.NETWORK_TYPE_HSPA:
				// type = "HSPA"; //高速上行行链路分组接入技术（3G）
				type = "3G";
				break;
			case TelephonyManager.NETWORK_TYPE_IDEN:
				// type = "IDEN"; //集成数字增强型网络
				type = "3G";
				break;
			}
		}
		return type;
	}

	public static String getImsi() {
		String imsi = "";
		Context context =MailChat.app;
		if (context != null) {
			TelephonyManager manager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			imsi = manager.getSubscriberId();
		}
		return imsi;
	}

	private static NetworkInfo getActiveNetworkInfo() {
		NetworkInfo info = null;
		Context context = MailChat.app;
		if (context != null) {
			ConnectivityManager manager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			return manager.getActiveNetworkInfo();
		}
		return info;
	}

	/**
	 * 无网络提示框
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2013-8-19
	 */
	public static void showNoConnectedAlertDlg(final Context context) {
		MailDialog.Builder builder = new MailDialog.Builder(context);
		builder.setTitle(context.getString(R.string.operate_notice));
		builder.setMessage(context
				.getString(R.string.show_no_connection_activity_tv_prompt));
		builder.setPositiveButton(
				context.getString(R.string.show_no_connection_activity_btn_setting_text),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						// 跳转到网络设置页面
//						context.startActivity(new Intent(
//								Settings.ACTION_WIRELESS_SETTINGS));
						showSystemSettingView(context);
					}
				}).setNeutralButton(context.getString(R.string.cancel_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	/**
	 * @author qxian 获取当前的网络状态 -1：没有网络 1：WIFI网络2：wap网络3：net网络
	 * @param context
	 * @return
	 */

	public static int getAPNType(Context context) {
		int netType = -1;
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			if (networkInfo.getExtraInfo().toLowerCase().equals("cmnet")) {
				netType = 3;
			} else {
				netType = 2;
			}
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = 1;
		}
		return netType;
	}
	/**
	 * 获取WIFI连接状态是否已连接上
	 */
	public static boolean wifiConected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		State wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		return wifi == State.CONNECTED ? true : false;
	}
	public static void showSystemSettingView(Context context){
		context.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
	}
}
