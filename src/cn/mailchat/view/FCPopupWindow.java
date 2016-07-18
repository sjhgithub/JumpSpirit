package cn.mailchat.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import cn.mailchat.R;
import cn.mailchat.activity.MailDetialActivity;

/**
 * 
 * @Description:
 * @author:李光辉 (ligh@35.cn)
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-3-5
 */
public class FCPopupWindow {

	private Context mContext;
	private PopupWindow mPopupWindow;

	public FCPopupWindow(Context context) {
		this.mContext = context;

	}

	/**
	 * 
	 * @Description: 下拉弹出View
	 * @param contentView
	 *            要显示的View
	 * @param anchor
	 *            显示位置参照点
	 * @param xoff
	 *            x方向偏移量 根据anchor
	 * @param yoff
	 *            y方向偏移量 根据anchor
	 * @see:
	 * @since:
	 * @author: 李光辉 (ligh@35.cn)
	 * @date:2012-3-6
	 */
	public void showAsDropDown(View contentView, View anchor, int xoff, int yoff) {
		if (isShowing()) {
			mPopupWindow.dismiss();
		}
		if (mPopupWindow == null) {
			mPopupWindow = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
			mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
			mPopupWindow.update();
		}

		mPopupWindow.showAsDropDown(anchor, xoff, yoff);
	}

	public void showAsDropDown(View contentView, View anchor) {
		showAsDropDown(contentView, anchor, 0, 0);
	}

	/**
	 * 
	 * 设置isFocusable=true bitmapDrawable=new BitmapDrawable() 后按下返回键可以退出PopupWindow
	 * 
	 */
	public void showAtBottom(int widthLayoutParams, int heightLayoutParams, View contentView, View parentView, boolean isFocusable, BitmapDrawable bitmapDrawable) {
		if (isShowing()) {
			mPopupWindow.dismiss();
		}
		if (mPopupWindow == null) {
			mPopupWindow = new PopupWindow(contentView, widthLayoutParams, heightLayoutParams, isFocusable);
			mPopupWindow.setBackgroundDrawable(bitmapDrawable);
			mPopupWindow.setAnimationStyle(R.style.menu_popupwindow_style);
			mPopupWindow.update();

		}

		mPopupWindow.showAtLocation(parentView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

	}

	/**
	 * 
	 * 设置isFocusable=true bitmapDrawable=new BitmapDrawable() 后按下返回键可以退出PopupWindow
	 * 
	 */
	public void showAtRightBottom(int widthLayoutParams, int heightLayoutParams, View contentView, View parentView, boolean isFocusable, BitmapDrawable bitmapDrawable) {
		if (isShowing()) {
			mPopupWindow.dismiss();
		}
		if (mPopupWindow == null) {
			mPopupWindow = new PopupWindow(contentView, widthLayoutParams, heightLayoutParams, isFocusable);
			mPopupWindow.setBackgroundDrawable(bitmapDrawable);
			mPopupWindow.setAnimationStyle(R.style.menu_popupwindow_style);
			mPopupWindow.update();

		}
//		mPopupWindow.showAtLocation(parentView, Gravity.BOTTOM | Gravity.RIGHT, 0, (int) (MailDetialActivity.mBottomHeight));
		mPopupWindow.showAtLocation(parentView, Gravity.BOTTOM | Gravity.RIGHT, 0,0);
	}

	/**
	 * @Description:显示在底部
	 * @param contentView
	 * @param parent
	 * @param y
	 * @see:
	 * @since:
	 * @author: who
	 * @date:2012-7-2
	 */
	public void showAtBottom(View contentView, View parent, int y) {
		if (isShowing()) {
			mPopupWindow.dismiss();
		}
		if (mPopupWindow == null) {
			mPopupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
			mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
			mPopupWindow.update();

		}

		mPopupWindow.showAtLocation(parent, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, y);

	}

	/**
	 * 
	 * @Description: 从底部显示
	 * @param contentView
	 * @see:
	 * @since:
	 * @author: Guang Hui
	 * @date:2012-3-22
	 */
	public void showAtBottom(View contentView) {
		showAtBottom(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, contentView, contentView.getRootView(), true, new BitmapDrawable());
	}

	/**
	 * 
	 * @Description: 从右下角显示
	 * @param contentView
	 * @see:
	 * @since:
	 * @author: sunzhongquan
	 * @date:2014-2-27
	 */
	public void showAtRightBottom(View contentView) {
		showAtRightBottom(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, contentView, contentView.getRootView(), true, new BitmapDrawable());
	}

	public void setOnDismissListener(OnDismissListener onDismissListener) {
		mPopupWindow.setOnDismissListener(onDismissListener);
	}

	public boolean isShowing() {
		return mPopupWindow != null && mPopupWindow.isShowing();
	}

	public void close() {
		if (isShowing()) {
			this.mPopupWindow.dismiss();
		}
	}

	public Context getContext() {
		return mContext;
	}

}
