package cn.mailchat.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 
 * @copyright © 35.com
 * @file name ：CustomViewPager.java
 * @author ：zhangjx
 * @create Data ：2014-9-17下午8:53:43 
 * @Current Version：v1.0 
 * @History memory :
 * @Date : 2014-9-17下午8:53:43 
 * @Modified by：zhangjx
 * @Description :自定义ViewPager
 */
public class CustomViewPager extends ViewPager {
	/**
	 * 可滑动标志位
	 */
	private boolean isSlipping = true;

	public CustomViewPager(Context context) {
		super(context);
	}

	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		if (!isSlipping) {
			return false;
		}
		return super.onInterceptTouchEvent(arg0);
	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		if (!isSlipping) {
			return false;
		}
		return super.onTouchEvent(arg0);
	}

	/**
	 * @Title: setSlipping
	 * @Description: TODO设置ViewPager是否可滑动
	 * @param isSlipping
	 */
	public void setSlipping(boolean isSlipping) {
		this.isSlipping = isSlipping;
	}
}
