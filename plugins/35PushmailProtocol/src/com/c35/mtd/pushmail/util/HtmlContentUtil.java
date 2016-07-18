package com.c35.mtd.pushmail.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import com.c35.mtd.pushmail.Debug;

/**
 * 关于解析OA、EWAVE邮件，点击超级链接自动打开软件
 * 
 * @Description:
 * @author: zhuanggy
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-14
 */
public class HtmlContentUtil {

	// 由于发件人email不确定，故此email暂不需要
	// public static final String SENDER_EWAVE = "ewave@t.35.com";//ewave邮件的发件人email
	// public static final String SENDER_OA = "";//oa邮件的发件人email

	public static final String HTML_TAG_HERF = "发自微妹手机邮箱客户端（支持qq\\163\\Gmail等邮箱）<a href=\"http://d.35.com/4Ki\">安卓客户端 </a>| <a href=\"http://d.35.com/4Kj\">iPhone客户端</a>";// 发邮件时，没变的话替换成此内容
	public static final String HTML_DOWNLOAD35MAIL_ANDROID_URL_HERF = "href[ ]*=[ ]*\"http://d.35.com/4Ki\"";
	public static final String HTML_DOWNLOAD35MAIL_ANDROID_URL = "http://d.35.com/4Ki";
	public static final String HTML_DOWNLOAD35MAIL_IOS_URL_HERF = "href[ ]*=[ ]*\"http://d.35.com/4Kj\"";
	public static final String HTML_DOWNLOAD35MAIL_IOS_URL = "http://d.35.com/4Kj";

	// 型如：href="http://t.35.com/35.cn/#!/users/linshsh"
	// public static final String HTML_REGEX_EWAVE_URL_HERF =
	// "href[ ]*=[ ]*\"http://t.35.com/35.cn[/#!%?=A-Za-z0-9-_.]*\"";// EwaveURL超链接的正则表达式
	public static final String HTML_REGEX_EWAVE_URL_HERF = "href[ ]*=[ ]*\"http://t.35.com[/#!%?=A-Za-z0-9-_.]*\"";// EwaveURL超链接的正则表达式
	public static final String HTML_REGEX_OA_URL_HERF = "href[ ]*=[ ]*\"http://oa.35.cn[/&#%?=A-Za-z0-9-_.;]*\"";;// OAURL超链接的正则表达式

	// public static final String REGEX_EWAVE_URL = "http://t.35.com/35.cn[/#!%?=A-Za-z0-9-_.]*";//
	// EwaveURL的正则表达式
	public static final String REGEX_EWAVE_URL = "http://t.35.com[/#!%?=A-Za-z0-9-_.]*";// EwaveURL的正则表达式
	public static final String REGEX_OA_URL = "http://oa.35.cn[/&#%?=A-Za-z0-9-_.]*";// OAURL的正则表达式

	// 以下变量用于区分跳转到应用的哪个入口；现在的想法是直接将当前用户的email和所获得的URL传给相应应用，其自行判断入口
	// // 型如：http://t.35.com/35.cn#users/linshsh
	// public static final String REGEX_EWAVE_URL_SENDER = "http://t.35.com/35.cn#users/[#%?=A-Za-z0-9-_.]";//
	// Ewave发消息人的URL正则表达式
	// // 型如：http://t.35.com/35.cn/threads/50a1af5c1d41c80caa001d84
	// public static final String REGEX_EWAVE_URL_SINGLEMSG = "http://t.35.com/35.cn/threads/[A-Za-z0-9]";//
	// Ewave消息列表URL正则表达式
	// // 型如：http://t.35.com/35.cn/groups/4eafc2d3c2fc3419d400005e
	// public static final String REGEX_EWAVE_URL_GROUPMSG = "http://t.35.com/35.cn/groups/[A-Za-z0-9]";//
	// Ewave群组消息URL的正则表达式
	//

	private static Pattern p_script;
	private static Matcher matcher;
	// 判断是否是35微博或OA的应用，进行URL替换，来触发直接打开相关应用
	private static final String REPLACE_STR_FIRST = "href='javascript:pushmailjs.getClickedUrl(";
	private static final String REPLACE_STR_END = ")'";

	/**
	 * 替换herf字段为调用js脚本 判断是否是35微博或OA的应用，进行URL替换，来触发直接打开相关应用
	 * 
	 * @Description:
	 * @param strhtml
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-11-14
	 */
	public static String doUrlChange(String strhtml) {
		String result = strhtml;
		result = change(result, HTML_REGEX_EWAVE_URL_HERF);
		result = change(result, HTML_REGEX_OA_URL_HERF);
		result = change(result, HTML_DOWNLOAD35MAIL_ANDROID_URL_HERF);
		result = change(result, HTML_DOWNLOAD35MAIL_IOS_URL_HERF);
//		Debug.v("HtmlUtil", result);
		return result;
	}

	/**
	 * 判断是否是35微博或OA的应用，进行URL替换，来触发直接打开相关应用
	 * 
	 * @Description:
	 * @param htmlStr
	 * @param regex
	 * @return
	 * @see:
	 * @since:
	 * @author: cuiwei
	 * @date:2013-5-23
	 */
	private static String change(String htmlStr, String regex) {
		String result = htmlStr;
		p_script = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		matcher = p_script.matcher(result);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, REPLACE_STR_FIRST + getURLFromHerfString(matcher.group()) + REPLACE_STR_END);
		}
		matcher.appendTail(sb);
		result = sb.toString();

		return result;
	}

	// /**
	// * 获得url 中，以 /划分 的最后一段
	// *
	// * @Description:
	// * @param url
	// * @return
	// * @see:
	// * @since:
	// * @author: zhuanggy
	// * @date:2012-11-14
	// */
	// public static String getLastInfoOfURL(String url) {
	// String result = url;
	// String arry[] = result.split("/");
	// if (arry[arry.length - 1] != null) {
	// result = arry[arry.length - 1];
	// }
	// return result;
	// }

	/**
	 * 获得含herf的串中的url
	 * 
	 * @Description:
	 * @param url
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-11-14
	 */
	private static String getURLFromHerfString(String str) {
		String result = str;
		String arry[] = result.split("http://");
		if (arry[arry.length - 1] != null) {
			result = "\"http://" + arry[arry.length - 1];
		}
		return result;
	}
	
	
	/**
	 * 
	 * @Description:多个空格过滤成一个 多个回车过滤成一个
	 * @param inputString
	 * @return
	 * @see: 
	 * @since: 
	 * @author: 温楠
	 * @date:2013-7-31
	 */
	public static String HtmltoText(String inputString) {
		String htmlStr = inputString; // 含html标签的字符串
		String textStr = "";
		String newStr = "";

		java.util.regex.Pattern p_script;
		java.util.regex.Matcher m_script;
		java.util.regex.Pattern p_style;
		java.util.regex.Matcher m_style;
		java.util.regex.Pattern p_html;
		java.util.regex.Matcher m_html;

		// StringBuilder stringBuilder = null;
		try {
			String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>"; // 定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script>
			// }
			String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>"; // 定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style>
			// }
			String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式

			String regex = "[\n\r]+";

			p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
			m_script = p_script.matcher(htmlStr);
			htmlStr = m_script.replaceAll(""); // 过滤script标签

			p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
			m_style = p_style.matcher(htmlStr);
			htmlStr = m_style.replaceAll(""); // 过滤style标签

			p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
			m_html = p_html.matcher(htmlStr);
			htmlStr = m_html.replaceAll(""); // 过滤html标签

			htmlStr = htmlStr.replaceAll("&nbsp;", "");
			htmlStr = htmlStr.replaceAll("&rsaquo;", "");
			htmlStr = htmlStr.replaceAll("&gt;", "");
			htmlStr = htmlStr.replaceAll("&lt;", "");
			htmlStr = htmlStr.replaceAll("&#", "");
			// htmlStr = getString(htmlStr);

			htmlStr = htmlStr.replaceAll(" {2,}", " ");
			htmlStr = htmlStr.replaceAll(regex, "\n");
		} catch (Exception e) {
			// ("Html2Text: " + e.getMessage());
			Debug.e("failfast", "failfast_AA", e);
		}
		return htmlStr;// 返回文本字符串
	}

	/**
	 * 
	 * @Description:过滤半角空格
	 * @param newStr
	 * @return
	 * @see: 
	 * @since: 
	 * @author: 温楠
	 * @date:2013-7-31
	 */
	public static String getString(String newStr) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < newStr.length(); i++) {
			Character c = newStr.charAt(i);
			if (!" ".equals(c.toString())) {
				stringBuilder.append(c);
			}
		}
		return stringBuilder.toString();
	}

}
