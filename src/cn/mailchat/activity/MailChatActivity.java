package cn.mailchat.activity;

import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;

import cn.mailchat.MailChat;
import cn.mailchat.activity.MailChatActivityCommon.K9ActivityMagic;
import cn.mailchat.activity.misc.SwipeGestureDetector.OnSwipeGestureListener;


public class MailChatActivity extends Activity implements K9ActivityMagic {

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
		if(MailChat.isGesture()&&MailChat.isGestureUnclock()&&SetPasswordActivity.ifHasGPassword()){
			SetPasswordActivity.startActivity(this,true,false,false,false,false);
		}
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
