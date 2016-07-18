package com.c35.ptc.as.util;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.c35.mtd.pushmail.Debug;

import android.content.Context;
import android.util.Log;


/**
 * 
 * @Description:上传装机量
 * @author:hanchunxue
 * @see:
 * @since:
 * @copyright © 35.com
 */
public class C35OpenHttpPostFile {
	/**
	 * 
	 * @Description:
	 * @param context
	 * @param appAccount  当前账户，第一次调用时可以传""
	 * @return  true 成功， false 失败
	 * @see: 
	 * @since: 
	 * @author: cuiwei
	 * @date:2012-12-11
	 */
	public boolean upload35AppInfos(Context context, String appAccount) {
		boolean uploadSuccess = false;
		try {
			String url = "http://ota.35.com:8080/35OTA/activity/install";

			Log.i(C35OpenGlobalForApp.TAG, "before upload");

			JSONObject json = new JSONObject();
			json.put("model", C35OpenGlobalForApp.getDevice());
			json.put("imei", C35OpenGlobalForApp.getIMEI(context));
			json.put("osVer", C35OpenGlobalForApp.getOsversion());
			json.put("appName", C35OpenGlobalForApp.getPackageName(context));
			json.put("appVer", C35OpenGlobalForApp.getVersion(context));
			json.put("appAccount", appAccount);
			json.put("mobile", C35OpenGlobalForApp.getPhone());
			json.put("sms", C35OpenGlobalForApp.getSmsPassBack(context));

			if (C35OpenGlobalForApp.isNetworkConnected(context)) {
				String result = sendJsonDateToServer(url, json.toString());
				JSONObject resJson = new JSONObject(result);
				String res = (String) resJson.get("success");
				Log.v("hao", "------>>>>>success>>>>>>>" + resJson.get("success"));
				if ("0".equals(res)) {
					uploadSuccess = true;
					Log.i("hao", result + "");
				}
			} else {
				return uploadSuccess;
			}

		} catch (Exception e) {
			e.printStackTrace();
			Debug.e("failfast", "failfast_AA", e);
		}
		return uploadSuccess;
	}

	/**
	 * 
	 * 发送服务器需要格式的数据到服务器端，接受服务器返回的数据
	 * 
	 * @author hanchunxue
	 * @param url
	 *            服务器地址
	 * @param parme
	 *            List<NameValuePair>数据参数
	 * @return String 服务器返回的数据(json格式)
	 * @throws Exception
	 */
	private String sendJsonDateToServer(String url, String parme) throws Exception {
		Log.v("hao", "--------->>>>>>>>>parme>>>>>>" + parme);

		DefaultHttpClient httpClient = new DefaultHttpClient();
		Log.v("hao", "--------->>>>>>>>>httpClient>>>>>>");

		HttpPost httpPost = new HttpPost(url);
		StringEntity entity = new StringEntity(parme, "UTF-8");
		httpPost.setEntity(entity);
		HttpResponse response = httpClient.execute(httpPost);
		int res = 0;
		Log.v("hao", "server url is :>>" + url);
		res = response.getStatusLine().getStatusCode();
		if (res == HttpStatus.SC_OK) {
			return EntityUtils.toString(response.getEntity());

		}
		return null;
	}
}
