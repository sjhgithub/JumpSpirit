package cn.mailchat.activity;

import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;

import cn.mailchat.activity.MailChatActivityCommon.K9ActivityMagic;
import cn.mailchat.activity.misc.SwipeGestureDetector.OnSwipeGestureListener;


public class BaseFragmentActivity extends FragmentActivity implements K9ActivityMagic {

    private MailChatActivityCommon mBase;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        mBase = MailChatActivityCommon.newInstance(this);
        super.onCreate(savedInstanceState);
    }
	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        mBase.preDispatchTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void setupGestureDetector(OnSwipeGestureListener listener) {
        mBase.setupGestureDetector(listener);
    }
}
