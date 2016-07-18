package cn.mailchat.beans;

/**
 * 位置
 * 
 * @Description:
 * @author:xuqq
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-12-16
 */
public class AddrInfo {

	private String addr;// 地址
	private String name;// 名称
	/**
	 * 纬度
	 */
	private double mLatitude;
	/**
	 * 经度
	 */
	private double mLongitude;
	private String mAddress;
	private String dis;

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getmLatitude() {
		return mLatitude;
	}

	public void setmLatitude(double mLatitude) {
		this.mLatitude = mLatitude;
	}

	public double getmLongitude() {
		return mLongitude;
	}

	public void setmLongitude(double mLongitude) {
		this.mLongitude = mLongitude;
	}

	public String getmAddress() {
		return mAddress;
	}

	public void setmAddress(String mAddress) {
		this.mAddress = mAddress;
	}

	public String getDis() {
		return dis;
	}

	public void setDis(String dis) {
		this.dis = dis;
	}

}
