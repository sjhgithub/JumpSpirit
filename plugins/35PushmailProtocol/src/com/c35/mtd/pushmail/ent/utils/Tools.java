package com.c35.mtd.pushmail.ent.utils;

import android.app.Activity;
import android.content.Context;
import android.view.Display;

import com.c35.mtd.pushmail.EmailApplication;


public class Tools {

	private static int[] screenSize;
	/**
	 * 
	 * @Description: dip 到 px 的转换
	 * @param context
	 * @param dipValue
	 * @return
	 * @see:
	 * @since:
	 * @author: 李光辉 (ligh@35.cn)
	 * @date:2012-3-2
	 */
	public static int dip2px(Context context, float dipValue) {
		final float scale = EmailApplication.density;
		return (int) (dipValue * scale + 0.5f);
	}

	/**
	 * 
	 * @Description: px 到 dip的转换
	 * @param context
	 * @param pxValue
	 * @return
	 * @see:
	 * @since:
	 * @author: 李光辉 (ligh@35.cn)
	 * @date:2012-3-2
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = EmailApplication.density;
		return (int) (pxValue / scale + 0.5f);
	}
	
	/**
	 * 
	 * @Description: 获取屏幕的宽高
	 * @param activity
	 * @return 屏幕宽高的数组 screenSize screenSize[0] 宽 screenSize[1]高
	 * @see:
	 * @since:
	 * @author: Guang Hui
	 * @date:2012-3-16
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
}
