package cn.mailchat.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;

import cn.mailchat.MailChat;
import cn.mailchat.activity.MailChatActivityCommon.K9ActivityMagic;
import cn.mailchat.activity.misc.SwipeGestureDetector.OnSwipeGestureListener;


public class MailChatFragmentActivity extends Activity implements K9ActivityMagic {

    private MailChatActivityCommon mBase;

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(MailChat.isGesture()&&MailChat.isGestureUnclock()&&SetPasswordActivity.ifHasGPassword()){
			SetPasswordActivity.startActivity(this,true,false,false,false,false);
		}
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mBase = MailChatActivityCommon.newInstance(this);
        super.onCreate(savedInstanceState);
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
