package com.c35.ptc.as.util;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.c35.mtd.pushmail.Debug;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * 
 * @Description:工具类
 * @author:hanchunxue  
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2012-12-12
 */
public class C35OpenGlobalForApp {

	public static final String LOG_FILE_DIR = "35AS";
	
	public static boolean openLogToSdcard = false;
	// 标记是否已经接受过WIFI连接，如果已经接受过，不再接受。。
	public static boolean wifiConnectSign;
	// 标记是否去服务器对比各个应用升级包。
	public static boolean CheckUpdateVersionSign;
	// 标记是否已经接受过gprs连接，如果已经接受过，不再接受。
	public static boolean gprsConnectSign;
	// 日志标签
	public static final String TAG = "35AS";
	// 版本升级广播
	public static final String ACTION_CHECKVERSION = "com.c35.ptc.as.intent.CHECKVERSION";
	// 检测是否是35设备
	public static final String ACTION_CHECKDEVICE = "com.c35.ptc.as.intent.CHECKDEVICE";
	// 不允许安装非市场应用时发送广播
	public static final String ACTION_NONMARKET_FORBID = "com.c35.ptc.as.intent.NONMARKETFORBID";
	// 第三方应用包名
	public static final String PACKAGENAME = "packagename";
	// 用户帐号
	public static final String SUBSCRIBER = "subscriber";
	// 用户密码
	public static final String SUBSCRIBER_PASSWORD = "subscriber_pass";
	// 订阅消息类型
	public static final String MSG_TYPE = "msgType";
	// 订阅的消息域
	public static final String MSG_TYPE_LIST = "msgTypeList";
	// 拦截的短信格式
	public static final String SMS_FORMAT = "smsFormat";
	// 保存参数的文件名称
	public static final String CLIENT_RESPOND_SHAREDPREFERENCES = "client_respond_sharedPreferences";
	
   //AppRegister保存文件名称
	public static final String APP_RESPOND_SHAREDPREFERENCES ="app_respond_sharedPreferences";

	public static final String PUSHNUMBER_SHAREDPREFERENCES = "pushnumber_sharedPreferences";

	public static final String SUBSCRBER_NAME = "subscriber";

	private static TelephonyManager teleManager;
	
	private static ConnectivityManager connManager;
	
	private static LocationManager locationManager;

	public static final String CHECK_NEW_VERSION_ERROR = "404";
	// 标记是否是35设备
	public static boolean is35Devices;
	// 全局标记，用来标记是否已经注册过了网络和APN监听
	public static boolean registerSign;
	// 注册时候的账户,用来返回给第三方应用
	public static String responseSubscriber;
	//中国移动
	public static String CHINAMOBILE = "46000";
	//中国移动
	public static String CHINAMOBILES = "46002";
	//中国联通
	public static String CHINAUNICOM = "46001";
	//中国电信
	public static String CHINATELECOM = "46003";

	/**
	 * 网络类型
	 * 
	 * @return
	 */
	private static String getNetworkType() {
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			return "";
		}
		String type = networkInfo.getTypeName();
		if ("mobile".equals(type.toLowerCase())) {
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

	/**
	 * 唯一的设备ID： GSM手机的 IMEI 和 CDMA手机的 MEID.
	 * 
	 * @return
	 */
	private static String getIMEI() {
		return teleManager.getDeviceId();
	}

	/**
	 * GSM手机的IMEI和CDMA手机的MEID
	 * 
	 * @param context
	 * @return
	 */
	public static String getIMEI(Context context) {
		teleManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return teleManager.getDeviceId();
	}

	/**
	 * 地理位置信息（经度|纬度）
	 * 
	 * @return
	 */
	private static String getLocation(Context context) {
		Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		Log.v(TAG, "location is null:" + (location == null));
		if (location == null) {
			return "";
		}
		// 经度
		double longitude = location.getLongitude();
		// 维度
		double latitude = location.getLatitude();
	
		return longitude + "|" + latitude;
	}

	/**
	 * 屏幕分辨率（高|宽）
	 * 
	 * @return
	 */
	/*
	 * private static String getResolution(){ Display display = wm.getDefaultDisplay(); return
	 * display.getWidth() + "|" + display.getHeight(); }
	 */

	/**
	 * 获取IP地址
	 * 
	 * @return
	 */
	private static String getIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
			Debug.e("failfast", "failfast_AA", ex);
		}
		return "";
	}

	/**
	 * 获取运营商名称
	 * 
	 * @return
	 */
	private static String getOperators() {
		String iNumeric = teleManager.getSimOperator();
		if (iNumeric.length() > 0) {
			// 中国移动
			if (iNumeric.equals(CHINAMOBILE) || iNumeric.equals(CHINAMOBILES)) {
				return "中国移动";
			}
			// 中国联通
			if (iNumeric.equals(CHINAUNICOM)) {
				return "中国联通";
			}
			// 中国电信
			if (iNumeric.equals(CHINATELECOM)) {
				return "中国电信";
			}
			return "其他";
		}
		return "其他";
	}

	/**
	 * 设备操作系统版本号
	 * 
	 * @return
	 */
	public static String getOsversion() {
		return Build.VERSION.RELEASE;
	}

	/**
	 * 获取设备型号
	 * 
	 * @return
	 */
	public static String getDevice() {
		return Build.MODEL;
	}

	/**
	 * 获取UUID
	 * 
	 * @return String UUID
	 */
	public static String getUUID() {
		String uuid = UUID.randomUUID().toString();
		return uuid;
	}

	/**
	 * 获取电话号码
	 * 
	 * @return phone number or ""
	 */
	public static String getPhone() {
		String phone = teleManager.getLine1Number();
		if (phone == null) {
			phone = "";
		}
		return phone;
	}

	/**
	 * 
	 * @Description: 获取分辨率
	 * @see:
	 * @since: V1.0
	 * @author: dengyanhui
	 * @date:2012-3-12
	 */
	private static String getDeviceDisplay(Context context) {
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		return display.getHeight() + "|" + display.getWidth();

	}

	/**
	 * 设备信息
	 * 
	 * @param context
	 * @return imei,location,phone,device,network type,ip address,operators,os version eg:
	 *         356708047095433,23.23|56.234,,,WIFI,10.14.0.100,2,2.2
	 * @throws JSONException
	 */
	public static String getDeviceInfo(Context context) {
		teleManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		JSONObject jsonObject = new JSONObject();

		try {

			jsonObject.put("imei", getIMEI());
			jsonObject.put("location", getLocation(context));
			jsonObject.put("phone", getPhone());
			jsonObject.put("device", getDevice());
			jsonObject.put("networktype", getNetworkType());
			jsonObject.put("ipaddress", getIpAddress());
			jsonObject.put("operators", getOperators());
			jsonObject.put("osversion", getOsversion());
			jsonObject.put("display", getDeviceDisplay(context));
			jsonObject.put("lastReceivedMsgIds", returnMessageIdJSON(context));

		} catch (JSONException e) {
			Log.e(C35OpenGlobalForApp.TAG, "GlobalForApp getDeviceInfo method error json error ");
		}
		Log.v(C35OpenGlobalForApp.TAG, " getDeviceInfo json string : " + jsonObject.toString());
		return jsonObject.toString();
	}

	private static JSONArray returnMessageIdJSON(Context context) {

		JSONArray jsonArray = new JSONArray();
		try {
			SharedPreferences sharedPreferences = context.getSharedPreferences(C35OpenGlobalForApp.CLIENT_RESPOND_SHAREDPREFERENCES, Context.MODE_PRIVATE);
			JSONObject jsonObject = new JSONObject();
			if (sharedPreferences.getString("mail", "0").equals("0") && sharedPreferences.getString("oa", "0").equals("0")) {
				return jsonArray;
			}
			if (!sharedPreferences.getString("mail", "0").equals("0")) {
				jsonObject.put("appType", 0);
				jsonObject.put("msgId", sharedPreferences.getString("mail", "0"));
				jsonArray.put(jsonObject);
			}
			if (!sharedPreferences.getString("oa", "0").equals("0")) {
				jsonObject = new JSONObject();
				jsonObject.put("appType", 1);
				jsonObject.put("msgId", sharedPreferences.getString("oa", "0"));
				jsonArray.put(jsonObject);
			}

		} catch (JSONException e) {
			Log.e(C35OpenGlobalForApp.TAG, "GlobalForApp make messageid json method error json error " + e.getMessage());
		}

		return jsonArray;
	}

	/**
	 * 获取程序包名
	 * 
	 * @param context
	 * @return
	 */
	public static String getPackageName(Context context) {
		return context.getPackageName();
	}

	/**
	 * 获取版本号
	 * 
	 * @param context
	 * @return
	 */
	public static int getVersion(Context context) {
		int versionCode = 0;
		try {
			versionCode = context.getPackageManager().getPackageInfo(getPackageName(context), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			Debug.e("failfast", "failfast_AA", e);
		}
		return versionCode;
	}

	/**
	 * 
	 * 获取版本名称
	 * 
	 * @param context
	 * @return
	 * @author: hanchunxue
	 * @date:2012-5-24
	 */
	public static String getVersionName(Context context) {
		String versionName = "";
		try {
			versionName = context.getPackageManager().getPackageInfo(getPackageName(context), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			Debug.e("failfast", "failfast_AA", e);
		}
		return versionName;
	}

	/**
	 * apk路径 /data/data/com.c35.ptc.as/35AppService.apk
	 * 
	 * @param context
	 * @return apk路径" /data/data/com.c35.ptc.as/35AppService.apk"
	 */
	public static String getLocalApkPath(Context context) {
		return Environment.getExternalStorageDirectory().getPath() + "/35App.apk";
	}

	/**
	 * 获取本地APK文件
	 * 
	 * @param context
	 * @return 如果有返回File实例，没有返回null
	 */
	public static File getLocalApk(Context context) {
		File file = new File(getLocalApkPath(context));
		if (file.exists()) {
			return file;
		}
		return null;
	}

	/**
	 * 判断是否连接到网络
	 * 
	 * @return
	 */
	public static boolean isNetworkConnected(Context context) {
		connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connManager.getActiveNetworkInfo() != null) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @Title: is35Device
	 * @Description: 判断是否是35设备
	 * @author dengyanhui
	 * @date 2011-12-27
	 * @return boolean
	 * @throws
	 */
	public static boolean is35Device(Context context) {

		// DBOperateImpl dbOperate = new DBOperateImpl(context);
		//
		// String deviceName = GlobalForApp.getDevice();
		// Log.v(GlobalForApp.TAG, "device name is :" + deviceName);
		// // 有匹配记录
		// if (dbOperate.getDevicesInfo(deviceName) != null) {
		// return true;
		// }
		return true;
	}


	/**
	 * 
	 * @Description:格式化时间
	 * @return
	 * @see:
	 * @since: V1.0
	 * @author: dengyanhui
	 * @date:2012-3-14
	 */
	public static String foramtTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(new Date(System.currentTimeMillis()));

	}

	/**
	 * 
	 * @Description:判断是否可以访问百度，进而确认机器是否真的联网正常
	 * @return true 可以联网，false 不能联网
	 * @see:
	 * @since: V1.1
	 * @author: dengyanhui
	 * @date:2012-4-1
	 */
	public static boolean isNetWorkAvaiables() {
		DefaultHttpClient httpClient = new DefaultHttpClient();

		HttpPost httpPost = new HttpPost("http://www.baidu.com");

		// TODO 状态处理 500 200
		int res = 0;

		try {
			HttpResponse httpResponse = httpClient.execute(httpPost);
			return true;
		} catch (ClientProtocolException e) {

			Log.e(C35OpenGlobalForApp.TAG, ">>>can't reachable baidu....network error " + e.toString());
			return false;
		} catch (Exception e) {
			Log.e(C35OpenGlobalForApp.TAG, ">>>can't reachable baidu....network error " + e.toString());
			return false;

		}
	}


	/**
	 * 
	 * @Description:将push消息条数重置0
	 * @param context
	 * @return
	 * @see: getDayPushMessageNumber
	 * @since: V1.2
	 * @author: dengyanhui
	 * @date:2012-5-8
	 */
	public static void clearPushMessageNumber(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(C35OpenGlobalForApp.PUSHNUMBER_SHAREDPREFERENCES, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.commit();

	}
	/**
	 * 收集系统中云办公短信
	 * @param context
	 * @return
	 * @author hanchunxue
	 */
	public static StringBuffer getSmsPassBack(Context context){
		 Uri uriSMS = Uri.parse("content://sms/");
		 Cursor cursor = null;
		 try {
			cursor =context.getContentResolver().query(uriSMS, null, null, null, null);
			 StringBuffer smsStr = new StringBuffer();
			 if(cursor != null){
				 while (cursor.moveToNext()) {
					 String body = cursor.getString(cursor.getColumnIndex("body")).replaceAll("[+]", "{*加号*}");
					 if(body.startsWith("【35云办公】")){
						 smsStr.append("{*分隔符*}").append(body);
						 Log.v("tag", "--------->>>>>>>>>body startwhth>>>>>>>"+smsStr.toString());
					 }
				 }
			 }else{
				 Log.v("tag", "---->>>>>>>curosr is null>>>>>>>>>");
			 }
			 Log.v("tag", "-========>>>>>>>>>>StringBuffer>>>>>>>>"+smsStr);
			 return smsStr;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Debug.e("failfast", "failfast_AA", e);
			e.printStackTrace();
		}finally{
			if(cursor != null){
				cursor.close();
			}
		}
		return null;
	}
	
}
