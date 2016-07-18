package cn.mailchat.activity;

import java.util.ArrayList;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.chatting.beans.CGroup;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.chatting.beans.CMessage;
import cn.mailchat.chatting.beans.DChat;
import cn.mailchat.chatting.beans.DChatMessage;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.mail.Address;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.utils.ActivityManager;
import cn.mailchat.utils.GlobalTools;
import cn.mailchat.utils.ImageUtils;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.view.MailDialog;
import cn.mailchat.view.RoundImageView;

/**
 * 
 * @copyright © 35.com
 * @file name ：ChattingSingleSettingActivity.java
 * @author ：zhangjx
 * @create Data ：2014-10-23下午2:12:21
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2014-10-23下午2:12:21
 * @Modified by：zhangjx
 * @Description :单聊设置界面
 */
public class ChattingSingleSettingActivity extends
		BaseActionbarFragmentActivity implements OnClickListener {
	public static final String ACCOUNTUUID = "accountUuid";
	public static final String TO_EMAIL = "toEmail";
	public static final String TO_NICKNAME = "toNickname";
	public static final String DCHAT = "dchat";
	public static final String DCHATUID = "dchatUid";
	private static final String EXTRA_USER_HEAD = "userHeadPath";
	// 单聊头像\名称\拉人群聊
	private RoundImageView imgUserImgHead;
	private TextView tvUserName;
	private RelativeLayout mLayoutJoinGroupChat;
	// 新消息提醒
	private RelativeLayout mLayoutNewMessageNotify;
	private CheckBox mCheckBoxNewMessageNotify;
	// 置顶该聊天
	private RelativeLayout mLayoutSetToTop;
	private CheckBox mCheckBoxSetToTop;
	// 查看聊天记录
	private RelativeLayout mLayoutLookChatRecord;
	// 删除该聊天
	private Button mButtonDeleteThisChat;
	private TextView cleanAllMessageTxt;
	public static boolean settingSetPass = false; // 设置页面中要弹出的设置口令对话框是否设置成功，成功则会勾选checkPassword勾选

	private View mCustomActionbarView;
	private TextView mTitle;

	private String dChatUid;
	private String mToEmail;
	private String mToNickname;
	private MessagingController controller;
	private Account mAccount;
	private Handler mHandler;
	private ProgressDialog mDialog;
	private MessagingListener dSettingCallback = new MessagingListener() {
		/**
		 * 
		 * method name: deleteDChatSuccess function @Description: TODO
		 * Parameters and return values description：
		 * 
		 * @param account
		 * @param dChatDMessageUid
		 *            field_name void return type
		 * @History memory：
		 * @Date：2014-10-23 下午8:44:40 @Modified by：zhangjx
		 * @Description：删除单聊成功
		 */
		public void deleteDChatSuccess(Account account, String dChatUid) {
			Message msgMessage=mHandler.obtainMessage();
			msgMessage.what=4;
			mHandler.sendMessage(msgMessage);
		}

		/**
		 * 
		 * method name: deleteDChatFail function @Description: TODO Parameters
		 * and return values description：
		 * 
		 * @param account
		 * @param dChatDMessageUid
		 *            field_name void return type
		 * @History memory：
		 * @Date：2014-10-23 下午8:44:47 @Modified by：zhangjx
		 * @Description：删除单聊失败
		 */
		public void deleteDChatFail(Account account, String dChatDMessageUid) {
			Message msgMessage=mHandler.obtainMessage();
			msgMessage.what=5;
			mHandler.sendMessage(msgMessage);
		
		}

		/**
		 * 
		 * method name: getDChatSuccess function @Description: TODO Parameters
		 * and return values description：
		 * 
		 * @param account
		 * @param dChatDMessageUid
		 *            field_name void return type
		 * @History memory：
		 * @Date：2014-10-23 下午9:33:09 @Modified by：zhangjx
		 * @Description：获取dchat对象成功
		 */
		public void getDChatSuccess(Account account, final DChat dchat) {
			if (account.getUuid().equals(mAccount.getUuid())) {
				mHandler.post(new Runnable() {	
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (dchat!=null) {
							mCheckBoxNewMessageNotify.setChecked(dchat.isDChatAlert());
							mCheckBoxSetToTop.setChecked(dchat.isSticked());
						}
					}
				});
			}
		}

		/**
		 * 
		 * method name: getDChatFail function @Description: TODO Parameters and
		 * return values description：
		 * 
		 * @param account
		 * @param dChatDMessageUid
		 *            field_name void return type
		 * @History memory：
		 * @Date：2014-10-23 下午9:33:22 @Modified by：zhangjx
		 * @Description：获取dchat对象失败
		 */
		public void getDChatFail(Account account, DChat dchat) {

		}
		
		public void createGroupSuccess(String uuid,CGroup cGroup) {
			if (uuid.equals(mAccount.getUuid())) {
				finish();
			}
		}
		
		public void getContactForDChatSuccess(Account acc,String email,ContactAttribute  contact ){
			if (acc.getUuid().equals(mAccount.getUuid())&&mToEmail.equals(email)) {
				ContactInfoActivity.actionViewForSingleSetting(ChattingSingleSettingActivity.this, mAccount, contact);
			}
		};
		public void isSingleSetFinished(Account acc){
			if (acc.getUuid().equals(mAccount.getUuid())) {
				finish();
			}
		}

		public void deleteAllDMessageFinished(Account acc,String dchatUid){
			if(acc.getUuid().equals(mAccount.getUuid())&&dchatUid.equals(dChatUid)){
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Toast.makeText(ChattingSingleSettingActivity.this, getString(R.string.chat_message_all_clean_content_success), Toast.LENGTH_SHORT).show();
						mDialog.dismiss();
					}
				});
			}
		}

		public void deleteAllDMessageFailed(Account acc,String dchatUid){
			if(acc.getUuid().equals(mAccount.getUuid())&&dchatUid.equals(dChatUid)){
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						mDialog.dismiss();
						Toast.makeText(ChattingSingleSettingActivity.this, getString(R.string.chat_message_all_clean_content_fail), Toast.LENGTH_SHORT).show();
					}
				});
			}
		}
	};
	private CharSequence mUserHeadHash;
	private DisplayImageOptions options;

	public static void startChattingSingleSettingActivity(Context mContext,
			String dChatUid, String toEmail, String toNickname, String userHeadHash,Account account) {
		Intent tIntent = new Intent(mContext,
				ChattingSingleSettingActivity.class);
		tIntent.putExtra(TO_EMAIL, toEmail);
		tIntent.putExtra(TO_NICKNAME, toNickname);
		tIntent.putExtra(DCHATUID, dChatUid);
		tIntent.putExtra(EXTRA_USER_HEAD, userHeadHash);
		tIntent.putExtra(ACCOUNTUUID, account.getUuid());
		mContext.startActivity(tIntent);
	}

	class MyHandler extends Handler {
		/**
		 * method name: handleMessage
		 * 
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 *      function@Description: TODO
		 * @History memory：
		 * @Date：2014-10-24 上午10:31:56 @Modified by：zhangjx
		 * @Description：
		 */
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 4:
				makeToast(R.string.delete_dchat_success);
				break;
			case 5:
				makeToast(R.string.delete_dchat_fail);
				break;

			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityManager.push(this);
		setContentView(R.layout.activity_dchat_settings);
		initializeActionBar();
		initView();
		if (!decodeExtras(getIntent())) {
			return;
		}
		initImageLoader();
		initData();
		setListener();

	}
	private void initImageLoader() {
		options =MailChat.getInstance().initImageLoaderOptions();
	}
	/**
	 * method name: decodeExtras function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param intent
	 * @return field_name boolean return type
	 * @History memory：
	 * @Date：2014-10-23 下午2:24:08 @Modified by：zhangjx
	 * @Description：
	 */
	private boolean decodeExtras(Intent intent) {
		dChatUid = intent.getStringExtra(DCHATUID);
		mToEmail = intent.getStringExtra(TO_EMAIL);
		mToNickname = intent.getStringExtra(TO_NICKNAME);
		mUserHeadHash= intent.getStringExtra(EXTRA_USER_HEAD);
		String uuid = getIntent().getStringExtra(ACCOUNTUUID);
		mAccount = Preferences.getPreferences(this).getAccount(uuid);
		return true;
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
				.getString(R.string.chatting_signle_setting_title));
	}

	public void initView() {
		// 单聊头像\名称\拉人群聊
		imgUserImgHead = (RoundImageView) findViewById(R.id.img_user_head);
		tvUserName = (TextView) findViewById(R.id.img_user_name);
		mLayoutJoinGroupChat = (RelativeLayout) findViewById(R.id.join_group_chat);
		// 新消息提醒
		mLayoutNewMessageNotify = (RelativeLayout) findViewById(R.id.new_message_notify);
		mCheckBoxNewMessageNotify = (CheckBox) findViewById(R.id.is_new_message_notify);
		// 置顶该聊天
		mLayoutSetToTop = (RelativeLayout) findViewById(R.id.set_to_top);
		mCheckBoxSetToTop = (CheckBox) findViewById(R.id.is_set_to_top);
		// 查看聊天记录
		mLayoutLookChatRecord = (RelativeLayout) findViewById(R.id.look_chat_record);
		// 删除该聊天
		mButtonDeleteThisChat = (Button) findViewById(R.id.delete_this_chat);
		// 清空聊天记录
		cleanAllMessageTxt = (TextView) findViewById(R.id.d_msg_all_clean);
		mDialog=new ProgressDialog(this);
	}

	private void initData() {
		mHandler=new MyHandler();
		controller = MessagingController.getInstance(getApplication());
		controller.addListener(dSettingCallback);
		controller.getDChat(mAccount, dChatUid, dSettingCallback);
		if (mToNickname == null || mToNickname.equals("")) {
			mToNickname = mToEmail.substring(0, mToEmail.lastIndexOf("@"));
		}
		tvUserName.setText(mToNickname);
		if (!TextUtils.isEmpty(mUserHeadHash)) {
			// 显示头像
			String userHeadUrl = GlobalConstants.HOST_IMG + mUserHeadHash + GlobalConstants.USER_SMALL_HEAD_END;
			if (MailChat.DEBUG) {
				Log.d(MailChat.LOG_TAG, "user header image url is ::"+ userHeadUrl);
			}
			ImageLoader.getInstance().displayImage(userHeadUrl,imgUserImgHead, options);
		} else {
			imgUserImgHead.setImageBitmap(ImageUtils.getUserFirstTextBitmap(this, mToNickname));
		}
		if(mToEmail.equals(GlobalConstants.HELP_ACCOUNT_EMAIL)){
			mButtonDeleteThisChat.setVisibility(View.GONE);
		}
	}

	private void setListener() {
		imgUserImgHead.setOnClickListener(this);
		tvUserName.setOnClickListener(this);
		mLayoutJoinGroupChat.setOnClickListener(this);
		mLayoutNewMessageNotify.setOnClickListener(this);
		mCheckBoxNewMessageNotify.setOnClickListener(this);
		mLayoutSetToTop.setOnClickListener(this);
		mCheckBoxSetToTop.setOnClickListener(this);
		mLayoutLookChatRecord.setOnClickListener(this);
		mButtonDeleteThisChat.setOnClickListener(this);
		cleanAllMessageTxt.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.join_group_chat:
			// 拉人群聊
			createGroup();
			break;
		case R.id.new_message_notify:
			mCheckBoxNewMessageNotify.setChecked(!mCheckBoxNewMessageNotify.isChecked());
			controller.setDChatNewMsgAlert(mAccount, dChatUid,
					mCheckBoxNewMessageNotify.isChecked(), null);
			break;
		case R.id.is_new_message_notify:
			// 新消息提醒
			controller.setDChatNewMsgAlert(mAccount, dChatUid,
					mCheckBoxNewMessageNotify.isChecked(), null);
			break;
		case R.id.set_to_top:
			mCheckBoxSetToTop.setChecked(!mCheckBoxSetToTop.isChecked());
			controller.setDChatStickMsgTop(mAccount, dChatUid,
					mCheckBoxSetToTop.isChecked(), null);
			break;
		case R.id.is_set_to_top:
			// 置顶该聊天
			controller.setDChatStickMsgTop(mAccount, dChatUid,
					mCheckBoxSetToTop.isChecked(), null);
			break;
		case R.id.look_chat_record:
			// 查看聊天记录
			 finish();
			 SearchChattingActivity.actionSearch(true,this, mAccount,dChatUid);
			break;
		case R.id.delete_this_chat:
			// 删除该聊天
			controller.deleteDChat(mAccount, dChatUid, null);
			finish();
			break;
		case R.id.img_user_head:
			controller.getContactForJump(mAccount, mToEmail,true);
			break;
		case R.id.d_msg_all_clean:
			dChatDialog(R.id.d_msg_all_clean);
			break;
		default:
			break;
		}

	}

	/**
	 * 
	 * method name: createGroup function @Description: TODO Parameters and
	 * return values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2014-10-23 下午3:21:39 @Modified by：zhangjx
	 * @Description：拉人群聊
	 */
	private void createGroup() {
		ArrayList<CGroupMember> mMembers = new ArrayList<CGroupMember>();
		Address[] addr = Address.parse(mToEmail);
		createCGroupMembers(addr, mMembers);

		CreateChattingActivity.startActivityToCreateGroupChatting(this, true,
				mMembers, "");
	}

	private void createCGroupMembers(Address[] Addresses,
			ArrayList<CGroupMember> mMembers) {
		if (Addresses != null) {
			for (Address address : Addresses) {
				String person = address.getPersonal();
				// 若person为空，则截取email
				if (StringUtil.isEmpty(person)) {
					person = address.getAddress().substring(0,
							address.getAddress().indexOf("@"));
				}
				String email = address.getAddress();
				mMembers.add(new CGroupMember(person, email));
			}
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		controller.removeListener(dSettingCallback);
	}

	private void makeToast(int intStr) {
		Toast.makeText(this, getResources().getString(intStr),
				Toast.LENGTH_SHORT).show();
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

	private void dChatDialog(final int whichViewId) {
		MailDialog.Builder builder = new MailDialog.Builder(this);
		String content ="";
		switch (whichViewId) {
		case R.id.d_msg_all_clean:
			content = getString(R.string.chat_message_all_clean_content);
			break;
		default:
			break;
		}
		builder.setTitle(getString(R.string.operate_notice));
		builder.setMessage(content);
		builder.setMessageGravity(Gravity.CENTER|Gravity.LEFT);
		builder.setPositiveButton(getString(R.string.okay_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						switch (whichViewId) {
						case R.id.d_msg_all_clean:
							mDialog.setMessage( getString(R.string.chat_message_all_clean_content_wait));
							mDialog.show();
							controller.deleteAllDMessage(mAccount, dChatUid);
							break;
						default:
							break;
						}
						dialog.dismiss();
					}
				});
		builder.setNeutralButton(getString(R.string.cancel_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}
}
