package cn.mailchat.activity;

import com.umeng.analytics.MobclickAgent;

import cn.mailchat.Account;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.utils.ImageUtils;
import cn.mailchat.view.RoundImageView;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class DChatInvitationActivity extends BaseActionbarFragmentActivity  implements OnClickListener{
	
	public static final String TOEMAIL = "toEmail";
	public static final String TO_NICKNAME= "toNickname";
	public static final String ACCOUNTUUID = "accountUuid";
	private RoundImageView roundImageView;
	private TextView invitationNameView;
	private TextView sendInvitationView;
	private Account account;
	private String toEmail;
	private String toNickname;
	
	private MessagingController controller;
	private ProgressDialog dialog;
	private Handler mHandler =new Handler();
	private View mCustomActionbarView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dchat_invitation);
		initView();
		initializeActionBar();
		initData();
		initEvent();
	}
	
	public static void actionDChatInvitationActivity(Context mContext,
			String toEmail,String nickname, Account mAccount) {
		Intent tIntent = new Intent(mContext, DChatInvitationActivity.class);
		tIntent.putExtra(ACCOUNTUUID, mAccount.getUuid());
		tIntent.putExtra(TOEMAIL, toEmail);
		tIntent.putExtra(TO_NICKNAME, nickname);
		mContext.startActivity(tIntent);
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
		TextView mTitle = (TextView) mCustomActionbarView.findViewById(R.id.tv_title);
		setActionbarCenterTitle(mCustomActionbarView, mTitle, getResources()
				.getString(R.string.send_dchat_invitation_title));
	}
	private void initView(){
		dialog =new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setMessage(getString(R.string.send_dchat_invitation_message));
		roundImageView = (RoundImageView) findViewById(R.id.tv_dchat_invitation_img);
		invitationNameView =(TextView) findViewById(R.id.tv_dchat_invitation_name);
		sendInvitationView = (TextView) findViewById(R.id.tv_send_invitation);
	}
	
	private void initData(){
		controller=MessagingController.getInstance(getApplication());
		controller.addListener(listener);
		account = Preferences.getPreferences(this).getAccount(getIntent().getStringExtra(ACCOUNTUUID));
		toEmail=getIntent().getStringExtra(TOEMAIL);
		toNickname=getIntent().getStringExtra(TO_NICKNAME);
		Bitmap bm =ImageUtils.getUserFirstTextBitmap(this, toNickname);
		roundImageView.setImageBitmap(bm);
		invitationNameView.setText(toNickname);
	}
	private void initEvent(){
		sendInvitationView.setOnClickListener(this);
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.tv_send_invitation:	
			dialog.show();
			controller.sendInvitationEmail(account, toEmail, listener);
			break;
		default:
			break;
		}
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		controller.removeListener(listener);
		dialog.dismiss();
	}
	private MessagingListener listener =new MessagingListener(){
			
		public void sendInvitationSuccess(Account acc, String invitationEmail) {
			if(account.getUuid().equals(acc.getUuid())){
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						dialog.hide();
						Toast.makeText(DChatInvitationActivity.this, R.string.send_dchat_invitation_message_success, Toast.LENGTH_SHORT).show();
						finish();
					}
				});
			}
		}

		public void sendInvitationFailed(Account acc, String invitationEmail) {
			if(account.getUuid().equals(acc.getUuid())){
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						dialog.hide();
						// TODO Auto-generated method stub
						Toast.makeText(DChatInvitationActivity.this, R.string.send_dchat_invitation_message_failure,Toast.LENGTH_SHORT).show();
					}
				});
			}	
		}
	
	};
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
