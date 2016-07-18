package cn.mailchat.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.os.IBinder;
import android.view.Display;
import android.view.inputmethod.InputMethodManager;
import cn.mailchat.R;

/**
 * 常用工具类
 * 
 * @Description:
 * @author:huangyx2
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-7-30
 */
public class GlobalTools {

	/**
	 * 使用32位MD5加密算法进行加密
	 * 
	 * @Description:
	 * @param text
	 *            要加密的字符串
	 * @return 加密后字符串
	 * @see:
	 * @since:
	 * @author: 黄永兴 (huangyx2@35.cn)
	 * @date:2012-2-24
	 */
	public static String md5Encrypt(String text) {
		// 空串就不用加密了
		if (text == null) {
			return text;
		}
		try {
			MessageDigest md5 = MessageDigest.getInstance("md5");
			char[] charArr = text.toCharArray();
			byte[] byteArr = new byte[charArr.length];
			for (int i = 0; i < charArr.length; i++) {
				byteArr[i] = (byte) charArr[i];
			}
			return StringUtil.bytes2HexString(md5.digest(byteArr));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 16位 32位MD5加密
	 * @author liwent
	 * @date 2014年8月18日
	 * @param unencrypted
	 * @param restraint？32：16.
	 * @return
	 */
	public static String MD5Encryption(byte[] unencrypted, boolean restraint) {
		String ciphertext = "";
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(unencrypted);
			byte[] b = digest.digest();
			int i;
			StringBuilder builder = new StringBuilder("");
			for (int offset = 0; offset >= b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					builder.append("0");
				builder.append(Integer.toHexString(i));
			}
			if (restraint) {
				ciphertext = builder.toString();
			} else {
				ciphertext = builder.toString().substring(8, 24);
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return ciphertext.toUpperCase(Locale.US);
	}
	/**
	 * dip转换成Px
	 * 
	 * @Description:
	 * @param context
	 * @param dipValue
	 * @return
	 * @see:
	 * @since:
	 * @author: huangyx2
	 * @date:2013-7-30
	 */
	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	/**
	 * 隐藏软键盘
	 * 
	 * @Description:
	 * @param context
	 * @return
	 * @see:
	 * @since:
	 * @author: huangyx2
	 * @date:2013-7-30
	 */
	public static boolean hideSoftInput(Activity context) {
		try {
			InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (inputMethodManager != null && context.getCurrentFocus() != null && context.getCurrentFocus().getWindowToken() != null) {
				return inputMethodManager.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		} catch (Exception e) {
		}
		return false;
	}

	/**
	 * 切换输入法显示隐藏状态
	 * 
	 * @Description:
	 * @param context
	 * @see:
	 * @since:
	 * @author: huangyx2
	 * @date:2013-7-30
	 */
	public static void toggleSoftInput(Context context) {
		InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}

	/**
	 * 隐藏输入法
	 * 
	 * @Description:
	 * @param context
	 * @param binder
	 *            输入法所在控件的token
	 * @see:
	 * @since:
	 * @author: huangyx2
	 * @date:2013-7-30
	 */
	public static void hideSoftInput(Context context, IBinder binder) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(binder, 0);
	}

	/**
	 * px转换成dp
	 * 
	 * @Description:
	 * @param context
	 * @param pxValue
	 * @return
	 * @see:
	 * @since:
	 * @author: huangyx2
	 * @date:2013-7-30
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 存储屏幕高宽的数组
	 */
	private static int[] screenSize = null;

	/**
	 * 获取屏幕高宽
	 * 
	 * @Description:
	 * @param activity
	 * @return 屏幕宽高的数组 [0]宽， [1]高
	 * @see:
	 * @since:
	 * @author: huangyx2
	 * @date:2013-7-30
	 */
	public static int[] getScreenSize(Activity activity) {
		if (screenSize == null) {
			Display display = activity.getWindowManager().getDefaultDisplay();
			screenSize = new int[2];
			screenSize[0] = display.getWidth();
			screenSize[1] = display.getHeight();
		}
		return screenSize;
	}

	/**
	 * 清除List内容，并置为null
	 * 
	 * @Description:
	 * @param list
	 * @see:
	 * @since:
	 * @author: huangyx2
	 * @date:2013-8-2
	 */
	public static void clearList(Collection<?> list) {
		if (list != null) {
			list.clear();
			list = null;
		}
	}

	/**
	 * 关闭cursor
	 * 
	 * @Description:
	 * @param cursor
	 * @see:
	 * @since:
	 * @author: huangyx2
	 * @date:2013-8-2
	 */
	public static void closeCursor(Cursor cursor) {
		if (cursor != null) {
			cursor.close();
		}
	}

	/**
	 * 取两个集合的并集
	 * 
	 * @Description:
	 * @param c1
	 * @param c2
	 * @return
	 * @see:
	 * @since:
	 * @author: huangyx2
	 * @date:2013-8-9
	 */
	public static Collection<String> mixedList(Collection<String> c1, Collection<String> c2) {
		// 定义两个空的集合，分别存放最大和最小的集合，用来取交集
		Collection<String> tmpBig = new ArrayList<String>();
		Collection<String> tmpSmall = new ArrayList<String>();
		// 为最大和最小集合赋值
		if (c1.size() > c2.size()) {
			tmpBig.addAll(c1);
			tmpSmall.addAll(c2);
		} else {
			tmpBig.addAll(c2);
			tmpSmall.addAll(c1);
		}
		tmpBig.retainAll(tmpSmall);
		tmpSmall = null;
		return tmpBig;
	}

	/**
	 * 将sp值转换为px值，保证文字大小不变
	 * 
	 * @param spValue
	 * @param fontScale
	 *            （DisplayMetrics类中属性scaledDensity）
	 * @return
	 */
	public static float sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return spValue * fontScale + 0.5f;
	}
	
	/**
	 * 获取所有支持的域名（邮箱后缀）
	 * @param context
	 * @return
	 */
	public static List<String> getSupportDomains(Context context) {
		List<String> results = new ArrayList<String>();
		try {
			XmlResourceParser xml = context.getResources().getXml(R.xml.providers);
			int xmlEventType;
			while ((xmlEventType = xml.next()) != XmlResourceParser.END_DOCUMENT) {
				if (xmlEventType == XmlResourceParser.START_TAG && "provider".equals(xml.getName())) {
					results.add(getXmlAttribute(context, xml, "domain"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		results.add("eyou.net");
		results.add("aliyun.com");
		results.add("mailchat.cn");
		return results;
	}
	
	private static String getXmlAttribute(Context context, XmlResourceParser xml, String name) {
		int resId = xml.getAttributeResourceValue(null, name, 0);
		if (resId == 0) {
			return xml.getAttributeValue(null, name);
		} else {
			return context.getString(resId);
		}
	}
}
