package com.c35.mtd.pushmail.beans;

/**
 * 
 * @Description:35云办公软件的bean
 * @author:hanlixia  
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2012-11-6
 */
public class C35AppBean {
	private String name; //35应用的名字
	private int icon; //35应用的图标id
	private String desc;//35应用简介
	private String downLoadUrl;//下载的地址
	private String updateUrl;//更新的地址
	private String appPackage;//35应用的包名
	private String appMainActivity;//35应用开启activity
	private boolean isInstall;//是否安装
	private boolean isUpdate;//是否是最新版本
	private String lastestVerName; //最新版本的版本名称；
	private int versionCode; //安装版本的版本号
	private String serverResult; // 取服务器上安装包详情的返回码
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getIcon() {
		return icon;
	}
	
	public void setIcon(int icon) {
		this.icon = icon;
	}
	
	public String getDownLoadUrl() {
		return downLoadUrl;
	}
	
	public void setDownLoadUrl(String downLoadUrl) {
		this.downLoadUrl = downLoadUrl;
	}
	
	public String getUpdateUrl() {
		return updateUrl;
	}
	
	public void setUpdateUrl(String updateUrl) {
		this.updateUrl = updateUrl;
	}
	
	public String getAppPackage() {
		return appPackage;
	}
	
	public void setAppPackage(String appPackage) {
		this.appPackage = appPackage;
	}
	
	public String getAppMainActivity() {
		return appMainActivity;
	}
	
	public void setAppMainActivity(String appMainActivity) {
		this.appMainActivity = appMainActivity;
	}
	
	public boolean isInstall() {
		return isInstall;
	}
	
	public void setInstall(boolean isInstall) {
		this.isInstall = isInstall;
	}
	
	public boolean isUpdate() {
		return isUpdate;
	}
	
	public void setUpdate(boolean isUpdate) {
		this.isUpdate = isUpdate;
	}

	
	public String getDesc() {
		return desc;
	}

	
	public void setDesc(String desc) {
		this.desc = desc;
	}

	
	public String getLastestVerName() {
		return lastestVerName;
	}

	
	public void setLastestVerName(String versionName) {
		this.lastestVerName = versionName;
	}

	
	public int getVersionCode() {
		return versionCode;
	}

	
	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	
	public String getServerResult() {
		return serverResult;
	}

	
	public void setServerResult(String serverResult) {
		this.serverResult = serverResult;
	}
}
