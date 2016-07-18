package com.c35.ptc.as.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import com.c35.mtd.pushmail.Debug;

import android.util.Log;
/**
 * 
 * @Description:上传数据到服务器并得到服务器返回数据
 * @author:hanchunxue  
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2012-12-11
 */

public class C35OpenHttpUtilForApp {
  
	public static String sendDateToServer(String url, List<NameValuePair> nameValuePair) throws Exception {

		DefaultHttpClient httpClient = new DefaultHttpClient();

		HttpPost httpPost = new HttpPost(url);
		try{
//			int i=2/0;//for test
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair, "UTF-8"));
			// TODO 状态处理 500 200
			int res = 0;
			HttpResponse httpResponse ;

			httpResponse = httpClient.execute(httpPost);
			res = httpResponse.getStatusLine().getStatusCode();
	
	
			if (res == 200) {
				/*
				 * 当返回码为200时，做处理 得到服务器端返回json数据，并做处理
				 */
				StringBuilder builder = new StringBuilder();
				BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
				for (String s = bufferedReader2.readLine(); s != null; s = bufferedReader2.readLine()) {
					builder.append(s);
				}
	
				Log.i("tag", ">>>>>>" + builder.toString());
				return builder.toString();
	
			}
		} catch (org.apache.http.conn.HttpHostConnectException e) {
			Debug.w("failfast", "HttpHostConnectException:"+e.getMessage());
			throw e;
		} catch (Exception e) {
			Debug.w("failfast", "failfastAA",e);
			throw e;
		}
		
		return null;
	}
}
