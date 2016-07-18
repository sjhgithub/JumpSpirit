package cn.mailchat.activity;

import com.umeng.analytics.MobclickAgent;

import cn.mailchat.Account;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.fragment.MessageListFragment;
import cn.mailchat.search.LocalSearch;
import cn.mailchat.utils.ActivityManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
/**
 * 该界面是解决串号问题，在该界面切换默认账户，及传递参数
 *
 * @Description:
 * @see:
 * @since:
 * @author: shengli
 * @date:2015-1-30
 */
public class MailNotifyPendingActivity extends Activity {
	private Account mAccount;
	private LocalSearch search;
	public static Intent actionMailNotify(Context context,LocalSearch search,boolean isChat){
		Intent intent =new Intent(context,MailNotifyPendingActivity.class);
		intent.putExtra("search", search);
		intent.putExtra("isChat", isChat);
		return intent;
	}
	
	
	public static Intent actionMailFor2Notify(Context context,LocalSearch search,boolean isChat,boolean is35Mail){
		Intent intent =new Intent(context,MailNotifyPendingActivity.class);
		intent.putExtra("search", search);
		intent.putExtra("isChat", isChat);
		intent.putExtra("is35Mail2", is35Mail);
		return intent;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		RelativeLayout re =new RelativeLayout(this);
		re.setLayoutParams(new LinearLayout.LayoutParams(  
		        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)); 
		setContentView(re);
		setDefaultData();
	}
	private void setDefaultData(){
		if(getIntent().getBooleanExtra("isChat", false)){
			MailChat.isChat=true;
		}else{
			MailChat.isMail=true;
		}
		
		if(getIntent().getBooleanExtra("is35Mail2", false)){
		    MailChat.forceRefresh = true;
		}
		Preferences prefs = Preferences.getPreferences(this);
		search=getIntent().getParcelableExtra("search");
		mAccount = prefs.getAccount(search.getAccountUuids()[0]);
		
		// 防止账号已被删除
		if (mAccount != null) {
			if (mAccount.getIsHaveUnreadMsg()) {
				mAccount.setmIsHaveUnreadMsg(false);
			}
//			mAccount.setName(mAccount.getEmail().substring(0, mAccount.getEmail().indexOf("@")));
			mAccount.save(Preferences.getPreferences(this));
			Preferences.getPreferences(this).setDefaultAccount(mAccount);
			ActivityManager.popAll();
			Main4TabActivity.actionDisplaySearch(this, search, false, true);
		}
		finish();
	}
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
