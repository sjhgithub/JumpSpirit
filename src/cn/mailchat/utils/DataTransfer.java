package cn.mailchat.utils;

import java.util.HashMap;

/**
 * @Description: 数据传递对象 （主要用于Activity之间进行传递数据)
 * @author:huangyx2
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-5-23
 */
public class DataTransfer extends HashMap<String, Object> {
	// 签到地址列表
	public static final String KEY_LOC_LIST = "loc_list";
	
	private static final long serialVersionUID = -9132168521127036380L;

	private DataTransfer() { }

	private static final DataTransfer instance = new DataTransfer();

	public static DataTransfer getInstance() {
		return instance;
	}

	public Object pop(String key) {
		Object object = remove(key);
		return object;
	}
	
	@Override
	public Object get(Object key) {
		Object obj = get(key);
		return obj;
	}
}
