package cn.mailchat.drag;

import android.app.Activity;
import android.view.View;

public interface ViewDragWare {

	/**
	 * 被拖拽视图 
	 */
	View getDragView();
	
	/**
	 * 拖拽完成(偏离原点较远)的回调
	 */
	void doOnActionSuccess();
	
	/**
	 * 拖拽取消(偏离原点较近)，原视图回复
	 */
	void reShowOrgView();
	
	/**
	 * 拖拽是隐藏原视图
	 */
	void hideOrgView();
	
	/**
	 * 拖拽视图所在activity
	 */
	Activity getActivity();
}
