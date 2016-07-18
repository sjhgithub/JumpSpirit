package com.c35.mtd.pushmail.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.R;

/**
 * Mail服务器地址全局变量与域名转换类
 * @author:gongfacun
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class MailUtil {

	// 35域名
	public static final String DOMAIN_35CN = "35.cn";
	// china-channel域名
	public static final String DOMAIN_CHINACHANNEL = "china-channel.com";
	// 35邮箱后缀
	public static final String EMAIL_SUFFIX_35CN = "@" + DOMAIN_35CN;
	// china-channel邮箱后缀
	public static final String EMAIL_SUFFIX_CHINACHANNEL = "@" + DOMAIN_CHINACHANNEL;
	// 代理服务器Ip地址 CHINANET fujian 这个地址没有意义，只是在初始化地址的时候，保证mhost不为空，测试账号（sofia3.com）的push验证地址
	public static final String PROXY_SERVER_HOST = "DOMAIN_TOBE_REPLACED";//"59.61.77.4";ProxyDP HOST替换
	// 测试代理服务器时使用的域名(测试用域名)
	public static final String DOMAIN_SZDEP = "szdep.com";
	public static final String DOMAIN_TEST35DOMAIN ="@mail18.35domain.net";
	// 代理服务器的URI scheme  dP服务器也用这个
	public static final String STORE_SCHEME_C35PROXY = "c35proxy";
	// 代理服务器邮件端口号
	public static final int PROXY_SERVER_MAIL_PORT = 9999;
	// 代理服务器附件端口号
	public static final int PROXY_SERVER_ATTACHMENT_PORT = 9998;
	// 邮件服务器域名
	public static final String DOMAIN_35MAIL = "mail." + DOMAIN_CHINACHANNEL;
	// 账号验证相对地址
	public static final String CHECK_ACCOUNT_RELADR = "/servlet/PushAction";
	// 35账号验证地址
//	public static final String CHECK_ACCOUNT_ADRESSE = "http://" + PROXY_SERVER_HOST + ":8088" + CHECK_ACCOUNT_RELADR;
	public static final String CHECK_ACCOUNT_ADRESSE = "http://" + PROXY_SERVER_HOST + CHECK_ACCOUNT_RELADR;
	// 校验域名(测试用域名)
	public static final String DOMAIN_CHECK = "sofia3.com";
	// sofia3邮箱后缀
	public static final String EMAIL_SUFFIX_SOFIA3 = "@" + DOMAIN_CHECK;
	// 35提供意见反馈和应用升级检测的地址
	public static final String SERVER_FOR_FB_UPDATE = "http://ota.35.com:8080";
	// 35提供意见反馈的网址
	public static final String SERVER_FOR_FEEDBACK = SERVER_FOR_FB_UPDATE + "/35OTA/feedback/saveApp.html";
	// // 35提供Pushmail应用检测升级的地址
	// public static final String SERVER_FOR_UPDATE = SERVER_FOR_FB_UPDATE + "/35OTA/getUpgradeApp";
	// // AS下载地址
	// public static final String APP_UPDATE_ADRESSE = SERVER_FOR_UPDATE +
	// "?pkgName=com.c35.ptc.as&pkgVersion=0&mod=T35s&meid=123456789012347";
	// 外部邮箱转换为内部邮箱服务器地址 (mail.try.35.cn已经映射到218.5.76.54了)
	public static final String GET_MAIL_HOST = "mail.try.35.cn";
	// 连接邮件名称转换的端口
	public static final int GET_MAIL_HOST_PORT = 9999;
	// 外部邮箱转换为内部邮箱超时时间，连接超时时间
	public static final int SOCKET_CONN_TIMEOUT_TIME = 5 * 1000;//
	// 读取超时时间
	public static final int SOCKET_READ_TIMEOUT_TIME = 60 * 1000;//15x
	// 读取超时时间
	public static final int SOCKET_ATT_READ_TIMEOUT_TIME = 100 * 1000;//15x
	// push试用期过期时拨打电话
	public static final String PHONE_TRIAL_EXPIRED = "tel:4008850035";
	// push试用期过期时访问网址
	public static final String URL_HTTP_TRIAL_EXPIRED = "http://www.35.com/mail/pushmail.php";
	// 验证账号是内部账号还是外部账号时调用的网址
//	public static final String GET_MAILADRESS_STATES_HTTP = "http://push.35.com/ippush/ServiceInfo";//新dp版不推荐用这个地址了
	// 搜索条件标示 4：全部 0： 邮件内容 1：主题 2：收件人 3：发件人 5:往来邮件
	public static final int SEARCH_MAIL_ALL = 4;
	public static final int SEARCH_MAIL_CONTEXT = 0;
	public static final int SEARCH_MAIL_SUBJECT = 1;
	public static final int SEARCH_MAIL_RECEIVER = 2;
	public static final int SEARCH_MAIL_SENDER = 3;
	public static final int SEARCH_MAIL_BETWEEN = 5;
	public static final String DP_SERVER_DOMAIN_PROTOCOLKEY = "1234567890abcDEF";// 连接分发中心（dp）protocolkey
//	public static final String DP_SERVER_DOMAIN_HOST = "mail.magic.35.com";// 分发中心地址临时
//	public static final String DP_SERVER_DOMAIN_HOST = "35proxydp.35domain.net";// 分发中心地址临时==wmail215.cn4e.com
//	public static final String DP_SERVER_DOMAIN_HOST = "192.168.1.112";// 分发中心地址北研测试用
	public static final String DP_SERVER_DOMAIN_HOST = "wmail215.cn4e.com";// 分发中心地址
	
	public static final int DP_SERVER_DOMAIN_PORT = 5566;// 分发中心地址端口
	public static final String PROXY_SERVER_IF_SAVE="proxy_server_if_save";
	public static final String TRY_ACCOUNT_CHECK_URL_EIS=":7799/eis/GetCheck?method=";//http://{EIS服务器IP或对应域名}:7799/eis/GetCheck?method={方法名}&data={加密后数据}&checkStr={请求验证字符串}。
	/**
	 * 将35.cn域的邮件地址转换为china-channel域的地址，非35.cn域的直接返回
	 * @param email
	 *            ：要转换的邮件地址
	 * @return 转换后的邮件地址
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Aug 14, 2012
	 */
	public static String convert35CNToChinaChannel(String email) {
		if (email != null && email.endsWith(EMAIL_SUFFIX_35CN)) {
			return email.replace(EMAIL_SUFFIX_35CN, EMAIL_SUFFIX_CHINACHANNEL);
		} else {
			return email;
		}
	}

	/**
	 * 将china-channel域的邮件地址转换为35.cn域的地址，非china-channel域的直接返回
	 * @param email
	 *            ： 要转换的邮件地址
	 * @return 转换后的邮件地址
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Aug 14, 2012
	 */
	public static String convertChinaChannelTo35CN(String email) {
		if (email != null && email.endsWith(EMAIL_SUFFIX_CHINACHANNEL)) {
			return email.replace(EMAIL_SUFFIX_CHINACHANNEL, EMAIL_SUFFIX_35CN);
		} else {
			return email;
		}
	}

	/**
	 * 获取当前显示的app是否是pushmail，如果是返回true，如果否返回false
	 * @param:
	 * @return:
	 * @throws:
	 * @see:
	 * @since:
	 * @author: gongfc
	 * @date:2012-10-12
	 */
	public static boolean getRuningPkg() {
		ActivityManager am = (ActivityManager) EmailApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
		if (EmailApplication.getInstance().getPackageName().equals(am.getRunningTasks(1).get(0).topActivity.getPackageName())) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否符合邮件格式
	 * @Description 判断是否符合邮件格式
	 * @param email 邮箱名称
	 * @return  true,false
	 * @see  
	 * @since  
	 * @author ChenLong
	 * @date 2014-1-15
	 */
	public static boolean isEmail(String email) {
		String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(email);
		return m.matches();
	}
}
