package com.c35.mtd.pushmail.ent.protocol;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.text.TextUtils;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.ent.utils.EntSettingUtil;
import com.c35.mtd.pushmail.ent.utils.HttpUtil;
import com.c35.mtd.pushmail.ent.utils.JsonParser;
import com.c35.mtd.pushmail.util.StringUtil;

/**
 * http协议 主要完成http请求操作
 * @Description:
 * @author:huangyx2  
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2013-5-21
 */
public class SyncEntProtocol{
	
	// session过期或不存在产生的错误
	public static final int SESSION_ERROR = 10002;
	
	public static final String TAG = "SyncEntProtocol";

	// 网络访问工具类
	private static HttpUtil httpUtil = HttpUtil.getInstance();

	// 用来获取restUrl和syncUrl的路径
	private static final String BASEURL = "http://prm.35.com:9012/system/get_prm_base_url";

	private static SyncEntProtocol instance = new SyncEntProtocol();
	
	public static SyncEntProtocol getInstance() {
		return instance;
	}

	/**
	 * 获取请求域
	 * @Description:
	 * @return
	 * @see: 
	 * @since: 
	 * @author: huangyongxing
	 * @date:2012-7-27
	 */
	private String getRestUrl(Account account){
		Debug.e(TAG, EntSettingUtil.getInstance().getRestUrl(account));
		String restUrl = EntSettingUtil.getInstance().getRestUrl(account);
		// 本地没有保存的情况下，需要向网络获取
		if(!StringUtil.isNotEmpty(restUrl)){
			BaseUrlResponse url = getRequestBaseUrl(account.getEmail());
			if(url != null){
				restUrl = url.restBaseUrl;
				EntSettingUtil.getInstance().setRestUrl(restUrl, account);
			}
		}
		Debug.i("ent_sync", "restUrl:" + restUrl);
		return restUrl;
	}


	/**
	 * 更新企业联系人或企业部门参数
	 * @Description:
	 * @param sync_type 同步类型：100是企业联系人，200是部门
	 * @param lastUpdateTime 上次同步时间，为空表示全同步
	 * @return
	 * @see: 
	 * @since: 
	 * @author: huangyongxing
	 * @date:2012-8-27
	 */
	private List<NameValuePair> createEntSyncParams(Account account, String sync_type, String lastUpdateTime){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		// session_key
		String session_key = EntSettingUtil.getInstance().getSessionKey(account);
		// session key 为空需要重新登录
		if(!StringUtil.isNotEmpty(session_key)){
			reLogined(account);
			session_key = EntSettingUtil.getInstance().getSessionKey(account);
		}
		Debug.i("ent_sync", "session_key:" + session_key);
		params.add(new BasicNameValuePair("session_key", session_key));
		// 同步类型：雇员信息
		params.add(new BasicNameValuePair("sync_type", sync_type));
		// 同步时间戳
		params.add(new BasicNameValuePair("last_update_time", lastUpdateTime));
		
		return params;
	}
	
	/**
	 * 更新联系人信息
	 * @Description:
	 * @return
	 * @see: 
	 * @since: 
	 * @author: huangyongxing
	 * @date:2012-8-27
	 */
	public SyncEntContactsResponse requestEntContacts(Account account, String lastSyncTime){
		String url = getRestUrl(account) + "sync/get_sync_items";
		// 同步类型：雇员信息
		String sync_type = "100";
		// 同步时间戳
		String jsonData = httpUtil.sendRequest(url, createEntSyncParams(account, sync_type, lastSyncTime), true);
		Debug.i("ent_sync", "requestEntContacts:" + jsonData);
		Debug.e("ent_sync", "params:" + createEntSyncParams(account, sync_type, lastSyncTime));
		if (TextUtils.isEmpty(jsonData)) {
			return null;
		} else {
			SyncEntContactsResponse result =  JsonParser.parse(jsonData, SyncEntContactsResponse.class);
			if(isNeedReLogin(result.error_code)){
				if(reLogined(account)){
					return requestEntContacts(account, lastSyncTime);
				}
			}
			return result;
		}
	}
	
	/**
	 * 
	 * @Description: 登录
	 * @return
	 * @see:
	 * @since:
	 * @author: liguanghui
	 * @date:2012-5-23
	 */
	public PrmUserResponse requestLogin(Account account) {

		String url = getRestUrl(account) + "account/ent_login";

		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("ent_account", account.getEmail()));
		params.add(new BasicNameValuePair("ent_password", account.getPassword()));

		String jsonData = httpUtil.sendRequest(url, params, true);
		
		Debug.d("ent_sync", "requestLogin="+jsonData);

		return JsonParser.parse(jsonData, PrmUserResponse.class);
	}

	/**
	 * 判断是否需要重新登录
	 * @Description:
	 * @param errorCode
	 * @return
	 * @see: 
	 * @since: 
	 * @author: huangyongxing
	 * @date:2012-8-27
	 */
	private boolean isNeedReLogin(int errorCode){
		if(errorCode == SESSION_ERROR){
			return true;
		}
		return false;
	}
	/**
	 * 重新登录
	 * @Description:
	 * @return
	 * @see: 
	 * @since: 
	 * @author: huangyongxing
	 * @date:2012-8-27
	 */
	private boolean reLogined(Account account){
		try {
			PrmUserResponse user = requestLogin(account);
			if(!StringUtil.isNotEmpty(user.userId)){
				return false;
			}
			EntSettingUtil.getInstance().setSessionKey(user.session_key, account);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Debug.w("failfast", e.getMessage());
			return false;
		}
	}
	
	/**
	 * 获取请求地址
	 * @Description:
	 * @param userName
	 * @return
	 * @see: 
	 * @since: 
	 * @author: huangyongxing
	 * @date:2012-9-7
	 */
	public BaseUrlResponse getRequestBaseUrl(String userName) {

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("account", userName));

		String jsonData = httpUtil.sendRequest(BASEURL, params, false);

		Debug.i("ent_sync", "getRequestBaseUrl = " + jsonData);

		return JsonParser.parse(jsonData, BaseUrlResponse.class);
	}

}
