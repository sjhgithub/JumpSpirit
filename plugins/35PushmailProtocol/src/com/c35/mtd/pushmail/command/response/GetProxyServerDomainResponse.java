package com.c35.mtd.pushmail.command.response;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.beans.ProxyServerDomain;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.util.C35AppServiceUtil;
import com.c35.mtd.pushmail.util.JsonUtil;
/**
 * ProxyDP返回参数bean
 * @Description:
 * @author:  cuiwei
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2013-10-8
 */
public class GetProxyServerDomainResponse extends BaseResponse {

	private ProxyServerDomain proxyServerDomain;

	@Override
	public void initFeild(String response) throws MessagingException {
		super.initFeild(response);
		try {
			Log.d("TAG", commandMessage);
			proxyServerDomain = new ProxyServerDomain();
			JSONObject jsonObject = new JSONObject(commandMessage);//{"port":9989,"updownport":9988,"host":"mail.szdev.com","domain":"35.cn","aliastype":0,"domaintype":1}
			proxyServerDomain.setDomain(jsonObject.getString("domain"));//35.cn
			Log.d("TAG", "设置的域为："+proxyServerDomain.getDomain());
			proxyServerDomain.setDomaintype(jsonObject.getInt("domaintype"));//3
			proxyServerDomain.setAliastype(jsonObject.getInt("aliastype"));//1
			proxyServerDomain.setHost(jsonObject.getString("host"));//mail.china-channel.com
			Log.d("TAG", "主机地址为："+proxyServerDomain.getHost());
			proxyServerDomain.setPort(jsonObject.getInt("port"));//9999
			Log.d("TAG", "端口号为："+proxyServerDomain.getPort());
			proxyServerDomain.setUpdownport(jsonObject.getInt("updownport"));//9998
			proxyServerDomain.setPushAction(jsonObject.getString("pushAction"));// 
//			if(proxyServerDomain.getDomain().equals("gzsq.com")){
//				Log.d("TAG", "特殊登陆");
//				proxyServerDomain.setHost("mail.gzsq.com");
//				proxyServerDomain.setPort(88);
//			}
			C35AppServiceUtil.writeSubscribeInformationToSdcard("jsonObject.getString(pushAction)"+jsonObject.getString("pushAction"));// 彩蛋log写入
		} catch (JSONException e) {
			Debug.w("C35", "JsonExp", e);
			throw new MessagingException(MessagingException.RETURN_COMMAND_ERROR_GETMAILSSTATUS, e.getMessage());
		}
	}

	public ProxyServerDomain getProxyServerDomain() {
		return proxyServerDomain;
	}

	public void setProxyServerDomain(ProxyServerDomain proxyServerDomain) {
		this.proxyServerDomain = proxyServerDomain;
	}

}
