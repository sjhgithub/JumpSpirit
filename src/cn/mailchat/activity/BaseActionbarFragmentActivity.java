package cn.mailchat.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.activity.MailChatActivityCommon.K9ActivityMagic;
import cn.mailchat.activity.misc.SwipeGestureDetector.OnSwipeGestureListener;
import cn.mailchat.utils.CommonUtils;

public class BaseActionbarFragmentActivity extends ActionBarActivity implements
		K9ActivityMagic {

	private MailChatActivityCommon mBase;
	private int leftSpace = 0;
	protected ActionBar mActionBar;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mBase = MailChatActivityCommon.newInstance(this);
		super.onCreate(savedInstanceState);
		initializeActionBar();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(MailChat.isGesture()&&MailChat.isGestureUnclock()&&SetPasswordActivity.ifHasGPassword()){
			SetPasswordActivity.startActivity(this,true,false,false,false,false);
		}
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	private void initializeActionBar() {
		mActionBar = getSupportActionBar();
//		mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#f5f5f5")));
		mActionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_bg));
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

	public void setActionbarCenterTitle(View mCustomActionbarView, TextView titleView, String titleString) {
		// Here do whatever you need to do with the view (set text if it's a
		// textview or whatever)
		titleView.setText(titleString);
		// Magic happens to center it.
		DisplayMetrics dm = new DisplayMetrics();// 获得屏幕分辨率
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int actionBarWidth = dm.widthPixels;
		titleView.measure(0, 0);
		int tvSize = titleView.getMeasuredWidth();

		int w = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED);
		View homeButton = findViewById(android.R.id.home);
		final ViewGroup holder = (ViewGroup) homeButton.getParent();
		List<View> list = new ArrayList<View>();
		for (int i = 0; i < holder.getChildCount(); i++) {
			View child = holder.getChildAt(i);
			list.add(child);
		}
		for (int j = 0; j < list.size(); j++) {
			list.get(j).measure(w, h);
			leftSpace = leftSpace + list.get(j).getMeasuredWidth();
		}

		if (null != mCustomActionbarView) {
			LayoutParams params = (LayoutParams) mCustomActionbarView
					.getLayoutParams();
			if (null != params) {
				int leftMargin = (actionBarWidth / 2 - (leftSpace))
						- (tvSize / 2);
				params.leftMargin = 0 >= leftMargin ? 0 : leftMargin;
			}
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			finish();
			CommonUtils.hideSoftInput(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
