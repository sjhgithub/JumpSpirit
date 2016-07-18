package com.c35.mtd.pushmail.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.GlobalConstants;
import com.c35.mtd.pushmail.R;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.logic.C35AccountManager;

/**
 * 
 * @Description:Ippush服务工具类，提供注册push，注销push和向sdcard写入信息的功能。
 * @author:gongfacun
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class C35AppServiceUtil {

	private static final String TAG = "C35AppServiceUtil";
	private static boolean isEnterHead = true; // 标志是否为开始
	/**
	 * 向AS注册push
	 * @param username
	 * @param password
	 * @see: 
	 * @since: 
	 * @author: cuiwei
	 * @date:2013-11-25
	 */
	public static void registerIPPush(String username, String password) {
		Debug.d(TAG, "registerIPPush");
		Intent subscribeIntent = new Intent();
		username = MailUtil.convert35CNToChinaChannel(username);
		// 指定订阅服务入口，不可缺少，否则不能向35App Service发起订阅请求
		subscribeIntent.setClassName(EmailApplication.getInstance().getPackageName(), "com.c35.ptc.as.activity.RegisterIPPush");
		// 指定35Android应用包名，不可缺少，目的是保证35App Service将消息广播到您的应用
		subscribeIntent.putExtra("packagename", PendingIntent.getBroadcast(EmailApplication.getInstance(), 0, new Intent(), 0));
		subscribeIntent.putExtra("subscriber", username);
		subscribeIntent.putExtra("subscriber_pass", password);
		subscribeIntent.putExtra("msgType", "0");
		EmailApplication.getInstance().startService(subscribeIntent);
	}
	/**
	 * 向AS注销push
	 * @param username
	 * @see: 
	 * @since: 
	 * @author: cuiwei
	 * @date:2013-11-25
	 */
	public static void unregisterIPPush(String username) {
		Intent subscribeIntent = new Intent();
		username = MailUtil.convert35CNToChinaChannel(username);
		// 指定退订服务入口，不可缺少，否则不能向35App Service发起退订请求
		subscribeIntent.setClassName(EmailApplication.getInstance().getPackageName(), "com.c35.ptc.as.activity.UnRegisterIPPush");
		// 指定35Android应用包名，不可缺少，否则35App Service不会执行退订操作
		subscribeIntent.putExtra("packagename", PendingIntent.getBroadcast(EmailApplication.getInstance(), 0, new Intent(), 0));
		subscribeIntent.putExtra("subscriber", username);
		EmailApplication.getInstance().startService(subscribeIntent);
	}

	/**
	 * 彩蛋功能，写入push日志
	 * 
	 * @Description:
	 * @param information
	 * @see:
	 * @since:
	 * @author: CuiWei
	 * @date:2012-11-12
	 */
	public static void writeSubscribeInformationToSdcard(String information) {
	    // Modified by LL
	    // BEGIN
	    /*
		if(information!=null){
			information=information.substring(0,information.length()<1000?information.length():1000);
		}
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			if (android.os.Environment.getExternalStorageDirectory().canWrite()) {
				try {
					Date date = new Date(System.currentTimeMillis());
//					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String time = EmailApplication.DateFormatYMDHMS.format(date);
					byte[] times = time.getBytes("utf-8");
					byte[] bytes = information.getBytes("utf-8");
					byte[] marking = "<br/>".getBytes("utf-8");
					String path =  GlobalConstants.EGG_SHELL_PATH;
					File file = new File(path);
					if (file != null && file.exists() && file.length() >= 64 * 1024) {
						file.delete();
						file = new File(path);
						isEnterHead = true;
					}
					FileOutputStream out = new FileOutputStream(file, true);
					if (isEnterHead) {// 加入meta标签用于显示使用utf-8码表解码
						out.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />".getBytes("utf-8"));
						out.write(marking);
						out.write(marking);
						out.write(marking);
						out.write(("<font color='green' >" + EmailApplication.getInstance().getResources().getString(R.string.version_name) + "</font> ").getBytes("utf-8"));// 加入版本号
																																						// 和手机机型信息
						out.write(marking);
						out.write(("<font color='green' > Model : " + Build.MODEL + "</font> ").getBytes("utf-8"));
						out.write(marking);
						List<Account> accounts = C35AccountManager.getInstance().getAccountsFromSP();
						if(accounts != null && !accounts.isEmpty()){
							for(Account account : accounts){
								out.write((account.getEmail()+",   ").getBytes("utf-8"));
							}
						}
						isEnterHead = false;
					}
					out.write(marking);
					out.write(times);
					out.write(bytes);
					out.write(marking);
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
					Debug.e("failfast", "failfast_AA", e);
				} catch (Exception e) {
					e.printStackTrace();
					Debug.e("failfast", "failfast_AA", e);
				}
			}
		}
		*/
	    // END
	}

	/**
	 * 根据接收as返回的注册结果code对应原因文字
	 * 
	 * @Description:
	 * @param code
	 * @return
	 * @see:
	 * @since:
	 * @author: CuiWei
	 * @date:2012-11-12
	 */
	public static String getReasonByCode(int code) {
		switch (code) {
		case 100:
			return "100:成功\n";
		case 300:
			return "300:账号不存在或被冻结\n";
		case 301:
			return "301:账号密码错误\n";
		case 302:
			return "302:账号没有使用IPPush的权限\n";
		case 303:
			return "303:账号没有使用App服务的权限\n";
		case 310:
			return "310:账号所在的域未开启试用,故账号无法试用PUSH\n";
		case 311:
			return "311:试用已到期\n";
		case 400:
			return "400:未创建会话或SessionId已过期失效\n";
		case 500:
			return "500:AppID或AuthKey验证失败\n";
		case 501:
			return "501:订阅失败\n";
		case 502:
			return "502:退订失败\n";
		case 600:
			return "600:不支持的协议版本（客户端协议版本太低）\n";
		case 701:
			return "701:非35设备\n";
		case 702:
			return "702:没可用网络\n";
		case 703:
			return "703:用户没在该设备上订阅过消息\n";
		case 704:
			return "704:参数错误-没有提供应用程序包名\n";
		case 705:
			return "705:参数错误-没有提供订阅者用户名或密码\n";
		case 706:
			return "706:参数错误-没有提供退订者用户名\n";
		case 707:
			return "707:参数错误-没有提供订阅类型或订阅类型错误\n";
		case 708:
			return "708:服务器错误，有可能是服务器宕机";
		case 900:
			return "900:服务器内部错误\n";
		case 901:
			return "901:当前连接被新建连接关闭\n";
		default:
			return "未知异常" + code;
		}
	}
}
