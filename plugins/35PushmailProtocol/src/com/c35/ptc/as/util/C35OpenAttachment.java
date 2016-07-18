package com.c35.ptc.as.util;

import java.io.File;
import java.io.Serializable;

import android.app.Notification;
import android.app.NotificationManager;

/**
 * 
 * @Description:服务器返回数据封装成对象
 * @author:hanchunxue  
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2012-11-19
 */
public class C35OpenAttachment implements Serializable{

	private String fileName = ""; // 文件名
	private String softVer = "" ;//version
	private String desc = "";//升级日志
	private String ver ;
	private String force ;//是否强制升级
	private String resultCode ;//返回码

	private String downloadUrl = "";// 下载地址
	private String displayName = "";// 显示名称
	private boolean isAutoInstall = false;// 是否自动安装apk
	private boolean isToastOff = false;// 是否显示Toast提示下载
	private File downloadFile = null; // 下载的文件
	private String downloadDir = null; // 文件所在路径
	private int notificationId = 0; // notificationId
	private boolean result = false; // 下载结果
	private Notification updateNotification = null;
	private NotificationManager updateNotificationManager = null;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public boolean isAutoInstall() {
		return isAutoInstall;
	}
	public String getSoftVer() {
		return softVer;
	}

	
	public void setSoftVer(String softVer) {
		this.softVer = softVer;
	}

	
	public String getDesc() {
		return desc;
	}

	
	public void setDesc(String desc) {
		this.desc = desc;
	}


	
	public String getVer() {
		return ver;
	}


	
	public void setVer(String ver) {
		this.ver = ver;
	}


	
	public String getForce() {
		return force;
	}


	
	public void setForce(String force) {
		this.force = force;
	}


	
	public String getResultCode() {
		return resultCode;
	}


	
	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}
	public void setAutoInstall(boolean isAutoInstall) {
		this.isAutoInstall = isAutoInstall;
	}

	public boolean isToastOff() {
		return isToastOff;
	}

	public void setToastOff(boolean isToastOff) {
		this.isToastOff = isToastOff;
	}

	public File getDownloadFile() {
		return downloadFile;
	}

	public void setDownloadFile(File downloadFile) {
		this.downloadFile = downloadFile;
	}

	public String getDownloadDir() {
		return downloadDir;
	}

	public void setDownloadDir(String downloadDir) {
		this.downloadDir = downloadDir;
	}

	public int getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(int notificationId) {
		this.notificationId = notificationId;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public Notification getUpdateNotification() {
		return updateNotification;
	}

	public void setUpdateNotification(Notification updateNotification) {
		this.updateNotification = updateNotification;
	}

	public NotificationManager getUpdateNotificationManager() {
		return updateNotificationManager;
	}

	public void setUpdateNotificationManager(NotificationManager updateNotificationManager) {
		this.updateNotificationManager = updateNotificationManager;
	}

	@Override
	public String toString() {
		return "Attachment [fileName=" + fileName + ", downloadUrl=" + downloadUrl + ", displayName=" + displayName + ", isAutoInstall=" + isAutoInstall + ", isToastOff=" + isToastOff + ", downloadFile=" + downloadFile + ", downloadDir=" + downloadDir + ", notificationId=" + notificationId + ", result=" + result + ", updateNotification=" + updateNotification + ", updateNotificationManager=" + updateNotificationManager + "]";
	}
}
