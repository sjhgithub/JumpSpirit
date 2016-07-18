package cn.mailchat.activity;

import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.a;

import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.setup.AccountSetupBasics;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.utils.NetUtil;
import cn.mailchat.utils.SystemUtil;
import cn.mailchat.view.ShareDialog;
import cn.mailchat.view.pager.r;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RecommendUseMailChatActivity extends BaseActionbarFragmentActivity
		implements OnClickListener {

	public static void startActivity(Context context) {
		Intent intent = new Intent(context, RecommendUseMailChatActivity.class);
		context.startActivity(intent);
	}

	private View mCustomActionbarView;
	private TextView mTitle,tvInvationCode,mBtnReload;
	private Button btnInviteContacts;
	private Account mAccount;
	private LinearLayout layout_icode,layoutLoadFailed;
	private MessagingController mController;
	private MessagingListener listener = new MessagingListener() {
		@Override
		public void getInvitationCodeStart(Account account) {
		}
		@Override
		public void getInvitationCodeSuccess(Account account,final String iCode) {
			if (mAccount.getEmail().equals(account.getEmail())) {
				runOnUiThread(new Runnable() {
					public void run() {
						layoutLoadFailed.setVisibility(View.GONE);
						tvInvationCode.setVisibility(View.VISIBLE);
						tvInvationCode.setText(iCode);
					}
				});
			}
		}
		@Override
		public void getInvitationCodeFailed(Account account) {
			if (mAccount.getEmail().equals(account.getEmail())) {
				runOnUiThread(new Runnable() {
					public void run() {
						tvInvationCode.setVisibility(View.GONE);
						layoutLoadFailed.setVisibility(View.VISIBLE);
					}
				});
			}
		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recommend);
		initializeActionBar();
		initView();
		initData();
		setListener();
	}

	private void setListener() {
		btnInviteContacts.setOnClickListener(this);
	}

	private void initData() {
		mController = MessagingController.getInstance(getApplication());
		mController.addListener(listener);
		mAccount = Preferences
				.getPreferences(RecommendUseMailChatActivity.this)
				.getDefaultAccount();
		if (mAccount.getEmail().endsWith("@35.cn")) {
			layout_icode.setVisibility(View.VISIBLE);
			mController.getInvitationCode(mAccount,SystemUtil.getCliendId(RecommendUseMailChatActivity.this),listener);
		}else {
			layout_icode.setVisibility(View.GONE);
		}
	}

	private void initView() {
		btnInviteContacts = (Button) findViewById(R.id.btn_invite_contacts_to_use);
		tvInvationCode=(TextView) findViewById(R.id.tv_invation_code);
		layout_icode=(LinearLayout) findViewById(R.id.layout_icode);
		layoutLoadFailed=(LinearLayout) findViewById(R.id.layout_load_failed);
		mBtnReload=(TextView) findViewById(R.id.tv_reload);
		mBtnReload.setOnClickListener(this);
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
		setActionbarCenterTitle(mCustomActionbarView, mTitle,
				getString(R.string.recommend_friends));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_invite_contacts_to_use:
//			InviteContactActivity.actionContactInvite(
//					RecommendUseMailChatActivity.this, mAccount, true);
//			ChooseContactsActivity.startActivityToInviteChat(RecommendUseMailChatActivity.this,true, true);
//			MobclickAgent.onEvent(RecommendUseMailChatActivity.this, "recommend_use_act_jump_to_invite_contact_act");
			handleShare();
			break;
		case R.id.tv_reload:
			if (!NetUtil.isActive()) {
				NetUtil.showNoConnectedAlertDlg(RecommendUseMailChatActivity.this);
				return;
			}
				mController.getInvitationCode(mAccount,SystemUtil.getCliendId(RecommendUseMailChatActivity.this),listener);
			break;
		default:
			break;
		}
	}
    // 分享
    public void handleShare() {
        final ShareDialog dialog = new ShareDialog(RecommendUseMailChatActivity.this);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setTitle(R.string.share_to);
        dialog.setShareInfo(getString(R.string.share_title),getString(R.string.share_sub_title),GlobalConstants.SHARE_URL);
        dialog.show();
    }
	@Override
	protected void onDestroy() {
		if (mController!=null) {
			mController.removeListener(listener);
		}
		super.onDestroy();
	}
	
}
