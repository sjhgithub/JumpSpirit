package com.c35.ptc.as.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ScrollView;
/**
 * 
 * @Description:上下滑动效果
 * @author:hanchunxue  
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2012-12-12
 */
public class C35OpenTextScrollView extends ScrollView {

	GestureDetector gestureDetector;

	public C35OpenTextScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public C35OpenTextScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public C35OpenTextScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public void setGestureDetector(GestureDetector gestureDetector) {
		this.gestureDetector = gestureDetector;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		return gestureDetector.onTouchEvent(event);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		gestureDetector.onTouchEvent(ev);
		super.dispatchTouchEvent(ev);
		return true;
	}
}
