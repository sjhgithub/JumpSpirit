package com.c35.mtd.pushmail.util;

import java.util.List;

import android.content.pm.PackageInfo;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.beans.C35AppBean;

/**
 * 判断设备中安装软件的信息
 * @author:hanlixia  
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2012-11-14
 */
public class APPInstall {
	public static final String TAG = "APPInstall";
	//其它35应用包名与启动类名
	public static final String PACKAGE_NAME_OA = "com.c35.mtd.oa";
	public static final String CLASS_NAME_OA_START = "com.c35.mtd.oa.activity.SwitcherActivity";//OA的启动Activity
	
	public static final String PACKAGE_NAME_EWAVE = "com.c35.ptc.ewave";
	public static final String CLASS_NAME_EWAVE_START = "com.c35.ptc.ewave.Splash";//EWAVE的启动Activity
	
	public static final String PACKAGE_NAME_EQ = "com.c35.eq";
	public static final String CLASS_NAME_EQ_START = "com.c35.eq.activity.MainActivity";//EQ的启动Activity
	
	public static final String PACKSE_NAME_PRM = "com.c35.ptc.prm";
	public static final String CLASS_NAME_PRM_STAT = "com.c35.ptc.prm.activities.WelcomeActivity";//prm 的启动Activity
	
	public static final String PACKSE_NAME_EMEETING = "com.c35.nmt";
	public static final String CLASS_NAME_EMEETING_STAT = "com.c35.nmt.components.NmtLoginActivity";
	
	
	
	/**
	 * 判断设备是否安装某应用
	 * @param packageName
	 * @return
	 * @see: 
	 * @since: 
	 * @author: hanlixia
	 * @date:2012-11-14
	 */
	public static boolean isInstall(String packageName){
		List<PackageInfo> packages = EmailApplication.getInstance().getPackageManager().getInstalledPackages(0); 
        for(int i= 0; i < packages.size(); i++){
        	if(packages.get(i).packageName.equals(packageName)){
        		return true;
        	}
        	
        }
        return false;
	}
	
	/**
	 * 判断给定的一组应用是否已安装，返回安装信息
	 * @param appList
	 * @return
	 * @see: 
	 * @since: 
	 * @author: cuiwei
	 * @date:2013-12-5
	 */
	public static List<C35AppBean> isC35ListInstall(List<C35AppBean> appList){
		List<PackageInfo> packages = EmailApplication.getInstance().getPackageManager().getInstalledPackages(0); 
		int j = 0;
        for(int i = 0; i < appList.size(); i++){
        	C35AppBean bean = appList.get(i);
        	for(j = 0; j < packages.size(); j ++){
        		if(packages.get(j).packageName.equals(bean.getAppPackage())){
        			bean.setVersionCode( packages.get(j).versionCode);
        			bean.setLastestVerName(packages.get(j).versionName);
        			bean.setInstall(true);
            		break;
            	}
        	}
        	Debug.d("TAG",bean.getName() + "  " + bean.getLastestVerName());
        }
		return appList;
	}

}
