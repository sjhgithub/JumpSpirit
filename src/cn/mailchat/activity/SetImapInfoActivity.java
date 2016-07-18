package cn.mailchat.activity;

import cn.mailchat.R;
import cn.mailchat.fragment.SetIMapInfoViewPagerFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class SetImapInfoActivity extends BaseActionbarFragmentActivity {
	
	private View mCustomActionbarView;
	private TextView mTitle;
	
	public static void actionDisplaySetImapInfo(Context context) {
		Intent intent = new Intent(context, SetImapInfoActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}


	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setimapinfo);
		initializeActionBar();
        
		  if (savedInstanceState == null) {
	            getSupportFragmentManager().beginTransaction()
	                    .add(R.id.container, new SetIMapInfoViewPagerFragment())
	                    .commit();
	        }
	}
	
	public void initializeActionBar() {
		mActionBar.setTitle(null);
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayUseLogoEnabled(true);
		// 返回按钮
		mActionBar.setDisplayHomeAsUpEnabled(true);
		// Inflate the custom view
		LayoutInflater inflater = LayoutInflater.from(this);
		mCustomActionbarView = inflater.inflate(
				R.layout.actionbar_custom_only_title, null);
		mActionBar.setCustomView(mCustomActionbarView);
		mTitle = (TextView) mCustomActionbarView.findViewById(R.id.tv_title);
		setActionbarCenterTitle(mCustomActionbarView, mTitle, getResources()
				.getString(R.string.set_imap_title));
	}
}
