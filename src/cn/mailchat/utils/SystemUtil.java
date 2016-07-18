package cn.mailchat.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

import cn.mailchat.MailChat;
import cn.mailchat.helper.BuildProperties;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * 
 * @copyright © 35.com
 * @file name ：SystemUtil.java
 * @author ：zhangjx
 * @create Data ：2014-10-31上午11:01:22
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2014-10-31上午11:01:22
 * @Modified by：zhangjx
 * @Description :系统工具类
 */
public class SystemUtil {
	private SystemUtil() {
	}

	/**
	 * 获得当前系统的sdk版本的int值
	 * 
	 * @return
	 */
	public static int getCurrentSdkInt() {
		return Build.VERSION.SDK_INT;
	}

	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public static int getScreenHeight(Activity activity) {
		// 获得屏幕宽高
		WindowManager w = activity.getWindowManager();
		Display d = w.getDefaultDisplay();
		int height = 0;
		if (SystemUtil.getCurrentSdkInt() >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			Point point = new Point();
			d.getSize(point);
			height = point.y;
		} else {
			height = d.getHeight();
		}
		return height;
	}

	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public static int getScreenWidth(Activity activity) {
		// 获得屏幕宽高
		WindowManager w = activity.getWindowManager();
		Display d = w.getDefaultDisplay();
		int width = 0;
		if (SystemUtil.getCurrentSdkInt() >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			Point point = new Point();
			d.getSize(point);
			width = point.x;
		} else {
			width = d.getWidth();
		}
		return width;
	}

	/**
	 * 获得控件的坐标
	 * 
	 * @param view
	 *            要获取坐标的控件
	 * @return 控件的坐标，[0]表示x轴，[1]表示y轴
	 */
	public static int[] getViewLocation(View view) {
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		return location;
	}

	/**
	 * 获取控件的大小
	 * 
	 * @param view
	 *            要获取尺寸的控件
	 * @return 控件的尺寸,[0]表示宽度, [1]表示高度
	 */
	public static int[] getViewSize(final View view) {
		int w = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		view.measure(w, h);
		int[] size = new int[2];
		size[0] = view.getMeasuredWidth();
		size[1] = view.getMeasuredHeight();
		return size;
	}

	/**
	 * 获得状态栏的高度
	 * 
	 * @param context
	 * @return 状态栏的高度
	 */
	public static int getStatusBarHeight(Context context) {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0;
		int statusBarHeight = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = context.getResources().getDimensionPixelSize(x);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return statusBarHeight;
	}

	/**
	 * 获得ActionBar的高度
	 * 
	 * @param context
	 * @return ActionBar的高度
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static int getActionBarHeight(Context context) {
		TypedValue tv = new TypedValue();
		int resId = 0;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			resId = android.R.attr.actionBarSize;
		} else {
			resId = android.support.v7.appcompat.R.attr.actionBarSize;
		}
		if (context.getTheme().resolveAttribute(resId, tv, true)) {
			return TypedValue.complexToDimensionPixelSize(tv.data, context
					.getResources().getDisplayMetrics());
		}
		return 0;
	}

	/**
	 * 获取设备ID,如果获取不到随机生成一个到本地做唯一标识
	 * 
	 * @param context
	 * @return cliendId
	 */
	public static String getCliendId(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String clientId = tm.getDeviceId();
		// 如果获取不到设备号
		if (clientId == null || clientId.equals("000000000000000")) {
			File localClendIdFile = new File(
					MailChat.application.getMailchatDirectory()
							+ EncryptUtil.getMd5("000000000000000"));
			if (localClendIdFile.exists()) {
				InputStream in = null;
				try {
					in = new FileInputStream(
							MailChat.application.getMailchatDirectory()
									+ EncryptUtil.getMd5("000000000000000"));
					int size = (int) localClendIdFile.length();
					byte[] bytes = new byte[size];
					in.read(bytes);
					clientId = new String(bytes, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

			} else {
				OutputStream out = null;
				try {
					out = new FileOutputStream(new File(
							MailChat.application.getMailchatDirectory()
									+ EncryptUtil.getMd5("000000000000000")));
					clientId = UUID.randomUUID().toString();
					out.write(clientId.getBytes("UTF-8"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		return clientId;
	}

	private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
	private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
	private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
	private static String mobileBrand;// 手机品牌
	private static String mobileRelease;// 手机系统版本号
	private static String model;// 手机型号

	/**
	 * 
	 * method name: isMIUI function @Description: TODO Parameters and return
	 * values description：
	 * 
	 * @return field_name boolean return type
	 * @History memory：
	 * @Date：2015-2-11 下午1:46:51 @Modified by：zhangjx
	 * @Description：判断是否为小米系统
	 */
	public static boolean isMIUI() {
		try {
			final BuildProperties prop = BuildProperties.newInstance();
			boolean isMiui=prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
					|| prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
					|| prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
			prop.removeImputStream();
			return isMiui;
		} catch (final IOException e) {
			return false;
		}
	}

	/**
	 * 
	 * method name: isFlyme function @Description: TODO Parameters and return
	 * values description：
	 * 
	 * @return field_name boolean return type
	 * @History memory：
	 * @Date：2015-2-11 下午1:46:36 @Modified by：zhangjx
	 * @Description：判断是否为魅族系统
	 */
	public static boolean isFlyme() {
		try {
			// Invoke Build.hasSmartBar()
			final Method method = Build.class.getMethod("hasSmartBar");
			return method != null;
		} catch (final Exception e) {
			return false;
		}
	}

	/**
	 * 
	 * method name: isHuaWei function @Description: TODO Parameters and return
	 * values description：
	 * 
	 * @return field_name boolean return type
	 * @History memory：
	 * @Date：2015-2-11 下午1:46:17 @Modified by：zhangjx
	 * @Description：判断是否为华为系统
	 */
	public static boolean isHuaWei() {
		try {
			// "手机型号: " + android.os.Build.MODEL
			// ",\nSDK版本:" + android.os.Build.VERSION.SDK
			// ",\n系统版本:" + android.os.Build.VERSION.RELEASE
			return getRelease().startsWith("Emotion")
					|| getModel().startsWith("HUAWEI")||getBrand().startsWith("Huawei");
		} catch (final Exception e) {
			return false;
		}
	}

	/**
	 * 
	 * method name: getRelease function @Description: TODO Parameters and return
	 * values description：
	 * 
	 * @return field_name String return type
	 * @History memory：
	 * @Date：2015-2-11 下午1:43:12 @Modified by：zhangjx
	 * @Description：获取系统版本号
	 */
	public static String getRelease() {
		if (mobileRelease != null) {
			return mobileRelease;
		} else {
			mobileRelease = android.os.Build.VERSION.RELEASE;
			return mobileRelease;
		}
	}

	/**
	 * 
	 * method name: getModel function @Description: TODO Parameters and return
	 * values description：
	 * 
	 * @return field_name String return type
	 * @History memory：
	 * @Date：2015-2-11 下午1:43:31 @Modified by：zhangjx
	 * @Description：返回手机型号
	 */
	public static String getModel() {
		if (model != null) {
			return model;
		} else {
			model = android.os.Build.MODEL;
			return model;
		}
	}

	/**
	 * 
	 * method name: getBrand function @Description: TODO Parameters and return
	 * values description：
	 * 
	 * @return field_name String return type
	 * @History memory：
	 * @Date：2015-2-11 下午1:51:17 @Modified by：zhangjx
	 * @Description：获取手机品牌
	 */
	public static String getBrand() {

		if (mobileBrand != null) {
			return mobileBrand;
		} else {
			mobileBrand = android.os.Build.BRAND;
			return mobileBrand;
		}
	}
	/**
	 *
	 * method name: isOrientation 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param context
	 *      @return    field_name
	 *      boolean    return type
	 *  @History memory：
	 *     @Date：2015-4-9 下午5:33:11	@Modified by：zhangjx
	 *     @Description：判断是横屏还是竖屏
	 */
	public static boolean isOrientation(Context context) {
		boolean isOrien = false;
		Configuration cf = context.getResources().getConfiguration(); // 获取设置的配置信息
		int ori = cf.orientation; // 获取屏幕方向
		if (ori == cf.ORIENTATION_LANDSCAPE) {
			// 横屏
			isOrien = true;
		} else if (ori == cf.ORIENTATION_PORTRAIT) {
			// 竖屏
			isOrien = false;
		}
		return isOrien;
	}
}
