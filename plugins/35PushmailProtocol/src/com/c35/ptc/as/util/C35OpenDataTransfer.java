package com.c35.ptc.as.util;

import java.util.HashMap;


public class C35OpenDataTransfer extends HashMap<Object, Object> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1067777657606960361L;
	private static final C35OpenDataTransfer mInstance=new C35OpenDataTransfer();
	
	public static C35OpenDataTransfer getInstance(){
		return mInstance;
	}

	public static Object pop(Object key){
		return getInstance().remove(key);
	}
}
