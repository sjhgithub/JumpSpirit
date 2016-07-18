package cn.mailchat.activity;

import com.umeng.analytics.MobclickAgent;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.AdapterView;
import android.widget.ListView;

import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.activity.MailChatActivityCommon.K9ActivityMagic;
import cn.mailchat.activity.misc.SwipeGestureDetector.OnSwipeGestureListener;


public class MailChatListActivity extends ListActivity implements K9ActivityMagic {

    private MailChatActivityCommon mBase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mBase = MailChatActivityCommon.newInstance(this);
//        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_bg));
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Shortcuts that work no matter what is selected
        if (MailChat.useVolumeKeysForListNavigationEnabled() &&
                (keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
                keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {

            final ListView listView = getListView();

            int currentPosition = listView.getSelectedItemPosition();
            if (currentPosition == AdapterView.INVALID_POSITION || listView.isInTouchMode()) {
                currentPosition = listView.getFirstVisiblePosition();
            }

            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && currentPosition > 0) {
                listView.setSelection(currentPosition - 1);
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN &&
                    currentPosition < listView.getCount()) {
                listView.setSelection(currentPosition + 1);
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Swallow these events too to avoid the audible notification of a volume change
        if (MailChat.useVolumeKeysForListNavigationEnabled() &&
                (keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
                keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("MailChatListActivity"); //统计页面
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("MailChatListActivity"); //统计页面
		MobclickAgent.onPause(this);
	}
}
