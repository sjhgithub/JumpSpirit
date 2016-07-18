package cn.mailchat.view;

/**
 * 操作项内容
 * 
 * @Description:
 * @author: huangyongxing
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-9-5
 */
public class FCMenuItem {

	private int opID;
	private int iconRes;
	private int textRes;

	public FCMenuItem() {
	}

	public FCMenuItem(int opID, int iconRes, int textRes) {
		this.opID = opID;
		this.iconRes = iconRes;
		this.textRes = textRes;
	}

	public int getOpID() {
		return opID;
	}

	public void setOpID(int opID) {
		this.opID = opID;
	}

	public int getIconRes() {
		return iconRes;
	}

	public void setIconRes(int iconRes) {
		this.iconRes = iconRes;
	}

	public int getTextRes() {
		return textRes;
	}

	public void setTextRes(int textRes) {
		this.textRes = textRes;
	}
}
