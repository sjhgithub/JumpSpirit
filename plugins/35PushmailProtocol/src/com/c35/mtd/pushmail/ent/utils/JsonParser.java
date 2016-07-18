package com.c35.mtd.pushmail.ent.utils;

import com.google.gson.Gson;

/**
 * 
 * @Description: 解析json
 * @author: liguanghui
 * @see:   
 * @since:      
 * @copyright ligh 35.com
 * @Date:2012-5-23
 */
public class JsonParser {
	
		//Google Gson类
		private static final Gson gson=new Gson();
		
		
		/**
		 * 
		 * @Description: 将json字符串转为 java对象
		 * @param <T>
		 * @param json
		 * @param classOfT
		 * @return
		 * @see: 
		 * @since: 
		 * @author: liguanghui
		 * @date:2012-5-23
		 */
		synchronized public static <T>T parse(String json,Class<T> classOfT){
			return gson.fromJson(json, classOfT);
		}

		/**
		 * 
		 * @Description:将obj对象转为json格式数据
		 * @param obj
		 * @return
		 * @see: 
		 * @since: 
		 * @author: liguanghui
		 * @date:2012-5-23
		 */
		synchronized public static String toJson(Object obj){
			return gson.toJson(obj);
		}
}
