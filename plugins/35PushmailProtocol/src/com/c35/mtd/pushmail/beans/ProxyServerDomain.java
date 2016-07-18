package com.c35.mtd.pushmail.beans;

import org.json.JSONObject;

/**
 * ProxyServer返回参数bean
 * 
 * @Description:
 * @author: cuiwei
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-10-8
 */
public class ProxyServerDomain {
	private String domain;// 域名(必填)
	private int domaintype;// 域类型：(必填)1:代收; 2:sofia2; 3:sofia3
	private int aliastype;// 域别名类型： (必填)0:主域名； 1域别名
	private String host;// ProxyServer的主机(必填)，同时也是PushAction的地址，验证push也用
	private int port;// 普通命令端口（选填）
	private int updownport;// 上传下载端口（选填）
	private String pushAction; // mail的PushAction验证


	// JSONObject jsonObject = new
	// JSONObject(commandMessage);//{"port":9989,"updownport":9988,"host":"mail.szdev.com","domain":"35.cn","aliastype":0,"domaintype":1}
	// proxyServerDomain.setDomain(jsonObject.getString("domain"));//35.cn
	// proxyServerDomain.setDomaintype(jsonObject.getInt("domaintype"));//3
	// proxyServerDomain.setAliastype(jsonObject.getInt("aliastype"));//1
	// proxyServerDomain.setHost(jsonObject.getString("host"));//mail.china-channel.com
	// proxyServerDomain.setPort(jsonObject.getInt("port"));//9999
	// proxyServerDomain.setUpdownport(jsonObject.getInt("updownport"));//9998
	public String getDomain() {
		return domain;
	}
	
	public void setDomain(String domain) {
		this.domain = domain;
	}

	public int getDomaintype() {
		return domaintype;
	}

	public void setDomaintype(int domaintype) {
		this.domaintype = domaintype;
	}

	public int getAliastype() {
		return aliastype;
	}

	public void setAliastype(int aliastype) {
		this.aliastype = aliastype;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getUpdownport() {
		return updownport;
	}

	public void setUpdownport(int updownport) {
		this.updownport = updownport;
	}
	
	public String getPushAction() {
		return pushAction;
	}

	public void setPushAction(String pushAction) {
		this.pushAction = pushAction;
	}

}
