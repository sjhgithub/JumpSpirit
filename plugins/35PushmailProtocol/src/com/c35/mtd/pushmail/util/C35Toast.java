package com.c35.mtd.pushmail.util;
/**
 * 
 * @Description:自定义35Toast
 * @author:gongfacun
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public interface C35Toast {

	// public void show(int resId, int duration);
	public void show(String text, int duration);

	public void hide();
}
