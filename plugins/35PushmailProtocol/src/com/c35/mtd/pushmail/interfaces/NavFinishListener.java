package com.c35.mtd.pushmail.interfaces;

/**
 * 
 * @Description:新手引导监听
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-4-17
 */
public class NavFinishListener {

	private SlidePageFinishListener listener = null;

	private static NavFinishListener instance = new NavFinishListener();

	public static NavFinishListener getInstance() {
		return instance;
	}

	public NavFinishListener() {

	}

	public interface SlidePageFinishListener {

		public void isNavFinished(boolean isFinished);
	}

	public void setOnViewFinishListener(SlidePageFinishListener slidePageFinishListener) {
		listener = slidePageFinishListener;
	}

	public void removeOnViewFinishListener() {
		listener = null;
	}

	public void navFinish(boolean isFinish) {
		if (listener != null) {
			listener.isNavFinished(isFinish);
		}

	}

}
