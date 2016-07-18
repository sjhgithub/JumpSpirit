package cn.mailchat.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import cn.mailchat.MailChat;
import cn.mailchat.beans.AddrInfo;
import cn.mailchat.service.LocationService;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

/**
 * 
 * @copyright © 35.com
 * @file name ：LocationBaiduUtil.java
 * @author ：zhangjx
 * @create Data ：2016-1-11下午2:36:09
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2016-1-11下午2:36:09
 * @Modified by：zhangjx
 * @Description :定位工具
 */
public class BaiduLocationUtil implements BDLocationListener {
	private LocationService mLocationService;
	public Vibrator mVibrator;
	private Context context;
	private double mLatitude;
	private double mLongitude;
	private String mAddress;
	private ReceiveBaiduLocationListener mReceiveBaiduLocationListener;

	public BaiduLocationUtil(Context context,
			ReceiveBaiduLocationListener receiveBaiduLocationListener) {
		this.context = context;
		this.mReceiveBaiduLocationListener = receiveBaiduLocationListener;
		mLocationService = MailChat.locationService;
		mVibrator = MailChat.mVibrator;
		// 获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
		mLocationService.registerListener(this);
		mLocationService.setLocationOption(mLocationService
				.getDefaultLocationClientOption());
		mLocationService.start();
	}

	@Override
	public void onReceiveLocation(BDLocation location) {
		// Receive Location
		StringBuffer sb = new StringBuffer(256);
		// sb.append("time : ");
		// sb.append(location.getTime());
		// sb.append("\nerror code : ");
		// sb.append(location.getLocType());
		// sb.append("\nlatitude : ");
		// sb.append(location.getLatitude());
		// sb.append("\nlontitude : ");
		// sb.append(location.getLongitude());
		// sb.append("\nradius : ");
		// sb.append(location.getRadius());
		if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
			// sb.append("\nspeed : ");
			// sb.append(location.getSpeed());// 单位：公里每小时
			// sb.append("\nsatellite : ");
			// sb.append(location.getSatelliteNumber());
			// sb.append("\nheight : ");
			// sb.append(location.getAltitude());// 单位：米
			// sb.append("\ndirection : ");
			// sb.append(location.getDirection());
			// sb.append("\naddr : ");
			// sb.append(location.getAddrStr());
			// sb.append("\ndescribe : ");
			sb.append("gps定位成功");
		} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
			// sb.append("\naddr : ");
			// sb.append(location.getAddrStr());
			// //运营商信息
			// sb.append("\noperationers : ");
			// sb.append(location.getOperators());
			// sb.append("\ndescribe : ");
			sb.append("网络定位成功");
		} else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
			// sb.append("\ndescribe : ");
			sb.append("离线定位成功，离线定位结果也是有效的");
		} else if (location.getLocType() == BDLocation.TypeServerError) {
			// sb.append("\ndescribe : ");
			sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
		} else if (location.getLocType() == BDLocation.TypeNetWorkException) {
			// sb.append("\ndescribe : ");
			sb.append("网络不同导致定位失败，请检查网络是否通畅");
		} else if (location.getLocType() == BDLocation.TypeCriteriaException) {
			// sb.append("\ndescribe : ");
			sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
		}
		Log.i(MailChat.LOG_COLLECTOR_TAG, "***外勤签到定位***\n" + sb.toString()
				+ "\n***address***\n" + location.getAddrStr());
		uploadLocation(location);
	}

	private void uploadLocation(BDLocation location) {
		if (location != null) {
			mLatitude = location.getLatitude();
			mLongitude = location.getLongitude();
			mAddress = location.getAddrStr();
			AddrInfo info = new AddrInfo();
			info.setAddr(mAddress);
			info.setmLatitude(mLatitude);
			info.setmLongitude(mLongitude);
			List<AddrInfo> list = new ArrayList<>();
			list.add(info);
			mReceiveBaiduLocationListener.onReceiveLocationSuccess(list);
		} else {
			mReceiveBaiduLocationListener.onReceiveLocationFailed();
		}
		locationStop();
	}

	/**
	 * 定位结束
	 */
	private void locationStop() {
		mLocationService.unregisterListener(this); // 注销掉监听
		mLocationService.stop(); // 停止定位服务
	}

	public interface ReceiveBaiduLocationListener {
		void onReceiveLocationSuccess(List<AddrInfo> list);

		void onReceiveLocationFailed();
	}
}
