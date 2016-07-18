package cn.mailchat.helper;

import cn.mailchat.R;
import cn.mailchat.activity.ChattingActivity;
import cn.mailchat.activity.ChattingSingleActivity;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.widget.Toast;


/**
 * 
 * @copyright © 35.com
 * @file name ：DoubleClickExitHelper.java
 * @author ：zhangjx
 * @create Data ：2014-8-22下午6:32:31 
 * @Current Version：v1.0 
 * @History memory :
 * @Date : 2014-8-22下午6:32:31 
 * @Modified by：zhangjx
 * @Description :双击退出
 */
public class DoubleClickExitHelper {

	private final Activity mActivity;
	
	private boolean isOnKeyBacking;
	private Handler mHandler;
	private Toast mBackToast;
	
	public DoubleClickExitHelper(Activity activity) {
		mActivity = activity;
		mHandler = new Handler(Looper.getMainLooper());
	}
	
	/**
	 * Activity onKeyDown事件
	 * */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode != KeyEvent.KEYCODE_BACK) {
			return false;
		}
		if(isOnKeyBacking) {
			mHandler.removeCallbacks(onBackTimeRunnable);
			if(mBackToast != null){
				mBackToast.cancel();
			}
			mActivity.finish();
			return true;
		} else {
			isOnKeyBacking = true;
			if(mBackToast == null) {
				mBackToast = Toast.makeText(mActivity, mActivity.getString(R.string.click_back_again_to_exist), Toast.LENGTH_SHORT);
			}
			mBackToast.show();
			mHandler.postDelayed(onBackTimeRunnable, 3000);
			return true;
		}
	}
	
	private Runnable onBackTimeRunnable = new Runnable() {
		
		@Override
		public void run() {
			isOnKeyBacking = false;
			if(mBackToast != null){
				mBackToast.cancel();
			}
		}
	};
}
