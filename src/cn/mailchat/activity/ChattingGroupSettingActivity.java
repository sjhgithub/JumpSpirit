package cn.mailchat.activity;

import java.util.ArrayList;
import java.util.List;

import com.umeng.analytics.MobclickAgent;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.chatting.beans.CGroup;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.helper.StringUtils;
import cn.mailchat.mail.Address;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.mail.store.LocalStore;
import cn.mailchat.utils.ActivityManager;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.view.MailDialog;

/**
 * 
 * @copyright © 35.com
 * @file name ：ChattingSettingActivity.java
 * @author ：zhangjx
 * @create Data ：2014-10-22下午3:41:16
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2014-10-22下午3:41:16
 * @Modified by：zhangjx
 * @Description :群聊设置界面
 */
public class ChattingGroupSettingActivity extends BaseActionbarFragmentActivity
		implements OnClickListener {
	private static final String TAG = "ChattingGroupSettingActivity";
	public static final String ACCOUNTUUID = "accountUuid";
	public static final String CGOUP = "group";
	// 查看群成员
	private RelativeLayout findMemberLayout;
	// 添加群成员
	// private LinearLayout addMemberLayout;
	private ImageView addMemberiv;
	// 群名称layout
	private RelativeLayout groupNameLayout;
	// 群描述layout
	private RelativeLayout groupDescriptionLayout;
	// 群描述
	private TextView groupDescriptionTxt;
	// 检索聊天记录
	private RelativeLayout searchGroupChatLayout;
	// q群成员
	private TextView membersTxtView;
	// 退出或者删除群组（成员显示为退出、管理员为删除）
	private LinearLayout delOrLeaveLayout;
	private RelativeLayout stickLayoutOperate;
	private CheckBox stickImgOperate;

	// 是否关闭群消息提醒
	private RelativeLayout msgAlertLayoutOperate;
	private CheckBox msgAlertImgOperate;

	// 声音提醒
	private RelativeLayout layoutVoiceReminder;
	private CheckBox imgVoiceReminder;

	// 编辑类型
	public final static String GROUP_NAME = "groupName";
	public final static String GROUP_DESCRIPTION = "groupDescription";
	private static final String EXTRA_GOUP_UID = "group_uid";
	private static final String CONTENT = "content";
	private static final String CONTENT_TYPE = "type";
	// 群文本资料修改
	private static final int GROUPCHAT_EDITOR = 0;
	// 群成员管理
	private static final int GROUPCHAT_MEMBER = 1;
	// 添加群成员
	private static final int GROUPCHAT_ADDMEMBER = 2;

	private String mGroupUid;

	private LruCache<String, Bitmap> mMemoryCache;
	private View mCustomActionbarView;
	private TextView mTitle;
	private TextView groupNameTxt;
	private ImageView groupNemeEdit;
	private TextView groupMemberTxt;
	private MessagingController controller;
	private Account  account ;
	private CGroup mGroup;
	private LocalStore localStore;
	private Handler mHandler =new Handler(); 
	private TextView cleanAllMessageTxt;
	private ProgressDialog mDialog;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ActivityManager.push(this);
		setContentView(R.layout.activity_chatting_group_setting);
		initializeActionBar();
		initWidget();
		initEvent();
		initData();
	}
	
	public static void actionGroupSetting(Context mContext, CGroup cGroup,Account mAccount,String content) {
		Intent tIntent = new Intent(mContext, ChattingGroupSettingActivity.class);
		tIntent.putExtra(ACCOUNTUUID, mAccount.getUuid());
		tIntent.putExtra(CGOUP, cGroup);
		tIntent.putExtra(CONTENT, content);
       // tIntent.putParcelableArrayListExtra(CGOUPMEMBER, (ArrayList<? extends Parcelable>) cGroupMembers);
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
		mTitle = (TextView) mCustomActionbarView.findViewById(R.id.tv_title);
		setActionbarCenterTitle(mCustomActionbarView, mTitle, getResources()
				.getString(R.string.chatting_group_setting_title));
	}

	private void initWidget() {
		membersTxtView = (TextView) findViewById(R.id.txtView_members);
		// addMemberLayout = (LinearLayout)
		// findViewById(R.id.layoutView_add_member);
		addMemberiv = (ImageView) findViewById(R.id.imgeView_add_member);
		findMemberLayout = (RelativeLayout) findViewById(R.id.layoutView_find_member);
		msgAlertLayoutOperate = (RelativeLayout) findViewById(R.id.layoutView_msg_alert);
		msgAlertImgOperate = (CheckBox) findViewById(R.id.ivView_msg_alert);
//		layoutVoiceReminder = (RelativeLayout) findViewById(R.id.layoutView_msg_voiceReminder);
//		imgVoiceReminder = (CheckBox) findViewById(R.id.ivView_msg_voiceReminder);
		stickLayoutOperate = (RelativeLayout) findViewById(R.id.layoutView_stick_operate);
		stickImgOperate = (CheckBox) findViewById(R.id.ivView_stick_operate);
		searchGroupChatLayout = (RelativeLayout) findViewById(R.id.layout_view_search_group_chat);
		delOrLeaveLayout = (LinearLayout) findViewById(R.id.main_del_message_ly);
		groupNameLayout = (RelativeLayout) findViewById(R.id.layoutView_group_name);
		groupNameTxt = (TextView) findViewById(R.id.txtView_group_name);
		groupDescriptionLayout = (RelativeLayout) findViewById(R.id.layout_view_group_description);
		//暂时隐藏群描述
		groupDescriptionLayout.setVisibility(View.GONE);
		groupDescriptionTxt = (TextView) findViewById(R.id.txt_view_group_description);
		groupNemeEdit = (ImageView) findViewById(R.id.img_group_name_edit);
		groupMemberTxt = (TextView) findViewById(R.id.textView_find_member);
		cleanAllMessageTxt = (TextView) findViewById(R.id.group_msg_all_clean);
		mDialog=new ProgressDialog(this);
	}

	private void initEvent() {
		stickLayoutOperate.setOnClickListener(this);
		stickImgOperate.setOnClickListener(this);
		msgAlertLayoutOperate.setOnClickListener(this);
		msgAlertImgOperate.setOnClickListener(this);
		// addMemberLayout.setOnClickListener(this);
		addMemberiv.setOnClickListener(this);
		findMemberLayout.setOnClickListener(this);
		msgAlertLayoutOperate.setOnClickListener(this);
		//layoutVoiceReminder.setOnClickListener(this);
		searchGroupChatLayout.setOnClickListener(this);
		groupNameLayout.setOnClickListener(this);
		groupDescriptionLayout.setOnClickListener(this);
		delOrLeaveLayout.setOnClickListener(this);
		cleanAllMessageTxt.setOnClickListener(this);
	}
	private void initData(){
		controller = MessagingController.getInstance(getApplication());
	    String 	content =getIntent().getStringExtra(CONTENT);
	    mGroup = (CGroup) getIntent().getSerializableExtra(CGOUP);
		if(content!=null){
			membersTxtView.setText(content);
		}
		if (!mGroup.isReName()) {
			groupNameTxt.setText(getResources().getString(R.string.no_group_name));
		}else{
			groupNameTxt.setText(mGroup.getGroupName());
		}
		if(mGroup.getIsAdmin()){
			groupNemeEdit.setVisibility(View.VISIBLE);
		}else{
			groupNemeEdit.setVisibility(View.GONE);
		}
		setMemberCount(mGroup.getMembers());
		account = Preferences.getPreferences(this).getAccount(getIntent().getStringExtra(ACCOUNTUUID));
		msgAlertImgOperate.setChecked(mGroup.getIsMessageAlert());
		stickImgOperate.setChecked(mGroup.getIsSticked());	
		controller.getCGroupInfo(account, mGroup, listener,true);
		controller.addListener(listener);

		try {
			localStore = account.getLocalStore();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			// if (data != null) {
			switch (requestCode) {

			case GROUPCHAT_EDITOR:
				String content = data.getStringExtra(CONTENT);
				String type = data.getStringExtra(CONTENT_TYPE);
				if (type.equals(GROUP_NAME)) {
					mGroup.setGroupName(content);
//					groupNameTxt.setText(StringUtil.isEmpty(content) ? "无" : content);
				} 
//				else if (type.equals(GROUP_DESCRIPTION)) {
//					mGroup.setDesc(content);
//					groupDescriptionTxt.setText(StringUtil.isEmpty(content) ? "无" : content);
//				}
				controller.updateGroupName(account, mGroup, null);
				break;
//			case GROUPCHAT_MEMBER:
//				controller.getGroupWithMembers(mAccount, mGroupUid, null, false);
//				break;
//			case GROUPCHAT_ADDMEMBER:
//				controller.getGroupWithMembers(mAccount, mGroupUid, null, false);
//				break;
			default:
				break;
			}
			// }
		}
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.layoutView_msg_alert:
			msgAlertImgOperate.setChecked(!msgAlertImgOperate.isChecked());
			controller.msgAlertGroup(account, mGroup.getUid(), msgAlertImgOperate.isChecked());
			break;
		case R.id.ivView_msg_alert:
			controller.msgAlertGroup(account,  mGroup.getUid(), msgAlertImgOperate.isChecked());		
			break;
		case R.id.layoutView_stick_operate:
			stickImgOperate.setChecked(!stickImgOperate.isChecked());
			controller.stickGroup(account, mGroup.getUid(), stickImgOperate.isChecked(),null);
			break;
		case R.id.ivView_stick_operate:
			controller.stickGroup(account, mGroup.getUid(), stickImgOperate.isChecked(),null);
			break;
			// 查看群成员
		case R.id.layoutView_find_member:
			GroupMembersActivity.actionView(ChattingGroupSettingActivity.this, mGroup, GROUPCHAT_MEMBER, account);
			break;
			// 添加群成员
		case R.id.imgeView_add_member:
			addMember();
			break;
			//查看聊天记录
		case R.id.layout_view_search_group_chat:
			finish();
			SearchChattingActivity.actionSearch(this, account, mGroup);
			break;
			//修改群名称
		case R.id.layoutView_group_name:
			if (mGroup.getIsAdmin()) {
				GroupChattingInfoChangeActivity.forwardContentEditActivity(ChattingGroupSettingActivity.this, groupNameTxt.getText().toString(), GROUP_NAME);
			}
			break;
			//修改群描述
//		case R.id.layout_view_group_description:
//			GroupChattingInfoChangeActivity.forwardContentEditActivity(ChattingGroupSettingActivity.this, groupDescriptionTxt.getText().toString(), GROUP_DESCRIPTION);
//			break;
		case R.id.main_del_message_ly:
		    leaveOrDelGroup();
			break;	
		case R.id.group_msg_all_clean:
			cGroupDialog(R.id.group_msg_all_clean);
			break;
		default:
			break;
		}
	}
	
	// 添加群成员
	private void addMember() {
		List<CGroupMember> mMembers = new ArrayList<CGroupMember>();
		String nickName = "";
		String email = "";
		try {
			List<CGroupMember> memberList = localStore.getCMembers(mGroup.getUid());
			for (CGroupMember cGroupMember : memberList) {
				email = cGroupMember.getEmail();
				nickName = cGroupMember.getNickName();
				if (StringUtil.isEmpty(cGroupMember.getNickName())) {
					nickName = email.substring(0, email.indexOf("@"));
				}
				mMembers.add(new CGroupMember(nickName, email));
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		CreateChattingActivity.startActivityToAddGroupMembers(this, true, true, mMembers, "", mGroup.getUid());
	}
	
	private MessagingListener listener =new MessagingListener(){
		
		public void getGroupInfoSuccess(Account acc,final CGroup cGroup,final List<CGroupMember> members) {
			if(account.getUuid().equals(acc.getUuid())&&mGroup.getUid().equals(cGroup.getUid())){
				mHandler.post(new Runnable() {				
					@Override
					public void run() {
						mGroup=cGroup;
						String cGroupName=null;
						for(CGroupMember cGroupMember :members){
							if(cGroupName==null){
								cGroupName =cGroupMember.getNickName();
							}else{
								cGroupName+=" , "+cGroupMember.getNickName();
							}
						}			
						if(cGroupName!=null){
							membersTxtView.setText(cGroupName);
						}
						if (cGroup!=null) {
							//设置群名称
							if (!mGroup.isReName()) {
								groupNameTxt.setText(getResources().getString(R.string.no_group_name));
							}else{
								groupNameTxt.setText(mGroup.getGroupName());
							}
							//群描述
//							groupDescriptionTxt.setText("");
						}
						setMemberCount(members);
					}
				});
			}
	
		}

		/** 
		 * method name: changeGroupSuccess 
		 * function @Description: TODO
		 * Parameters and return values description：
		 *      @param group    field_name
		 *      void    return type
		 *  @History memory：
		 *     @Date：2014-11-6 下午8:07:20	@Modified by：zhangjx
		 *     @Description：修改群名称成功
		 */
		public void changeGroupSuccess(Account acc,final CGroup group) {
			// TODO Auto-generated method stub
			if(account.getUuid().equals(acc.getUuid())&&group.getUid().equals(mGroup.getUid())){
				runOnUiThread(new Runnable() {
					public void run() {
						if (group!=null) {
							//设置群名称
							if (!group.isReName()) {
								groupNameTxt.setText(getResources().getString(R.string.no_group_name));
							}else{
								groupNameTxt.setText(group.getGroupName());
							}
						}
						if(group.getIsAdmin()){
							Toast.makeText(ChattingGroupSettingActivity.this, getString(R.string.change_group_name_success), Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		}


		/** 
		 * method name: changeGroupFailed 
		 * function @Description: TODO
		 * Parameters and return values description：
		 *      @param group
		 *      @param string    field_name
		 *      void    return type
		 *  @History memory：
		 *     @Date：2014-11-6 下午8:07:25	@Modified by：zhangjx
		 *     @Description：修改群名称失败
		 */
		public void changeGroupFailed(Account acc,CGroup group, String string) {
			if(account.getUuid().equals(acc.getUuid())){
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(ChattingGroupSettingActivity.this, getString(R.string.change_group_name_failed), Toast.LENGTH_SHORT).show();
					}
				});
			}
		}
		// 移除群成员成功后群设置页面的群成员显示处理
		public void delGroupMemberSuccess(Account acc,CGroupMember member) {
			if(account.getUuid().equals(acc.getUuid())){
				controller.listLocalCGroupMember(account, mGroup.getUid(), null);
			}
		};
	
		public void leaveCGroupSuccess(Account acc) {
			if (acc.getUuid().equals(account.getUuid())) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(ChattingGroupSettingActivity.this, getString(R.string.exit_group_success), Toast.LENGTH_SHORT).show();
						finish();
					}
				});
			}
		}

		public void leaveCGroupFail(Account acc) {
			if (acc.getUuid().equals(account.getUuid())) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(ChattingGroupSettingActivity.this, getString(R.string.exit_group_failed), Toast.LENGTH_SHORT).show();
					}
				});
			}	
		}
		
	
		public void deleteCGroupSuccess(Account acc) {
			if (acc.getUuid().equals(account.getUuid())) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(ChattingGroupSettingActivity.this, getString(R.string.delete_group_success), Toast.LENGTH_SHORT).show();
						finish();
					}
				});
			}	
		}
	
		public void deleteCGroupFail(Account acc) {
			if (acc.getUuid().equals(account.getUuid())) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(ChattingGroupSettingActivity.this, getString(R.string.delete_group_failed), Toast.LENGTH_SHORT).show();
					}
				});
			}	
		}
		public void listLocalGroupMemberSuccess(Account acc,String cGroupUid,final String membersName,final List<CGroupMember> cGroupMembers) {
			if (acc.getUuid().equals(account.getUuid())) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						membersTxtView.setText(membersName);
						setMemberCount(cGroupMembers);
						mGroup.setMembers(cGroupMembers);
					}
				});
			}	
		}

		public void deleteAllCMessageFinished(Account acc,String cGroupUid){
			if(acc.getUuid().equals(account.getUuid())&&cGroupUid.equals(mGroup.getUid())){
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Toast.makeText(ChattingGroupSettingActivity.this, getString(R.string.chat_message_all_clean_content_success), Toast.LENGTH_SHORT).show();
						mDialog.dismiss();
					}
				});
			}
		}

		public void deleteAllCMessageFailed(Account acc,String cGroupUid){
			if(acc.getUuid().equals(account.getUuid())&&cGroupUid.equals(mGroup.getUid())){
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						mDialog.dismiss();
						Toast.makeText(ChattingGroupSettingActivity.this, getString(R.string.chat_message_all_clean_content_fail), Toast.LENGTH_SHORT).show();
					}
				});
			}
		}

		public void delteGroupInfoByMemberSuccess(Account acc,CGroup cGroup) {
			if (account.getUuid().equals(acc.getUuid()) && cGroup.getUid().equals(mGroup.getUid())) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						finish();
					}
				});
			}
		}

		public void kickedOutGroupByMemberSuccess(Account acc,final CGroup cGroup) {
			if (account.getUuid().equals(acc.getUuid()) && cGroup.getUid().equals(mGroup.getUid())) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						finish();
					}
				});
			}
		}
	};
	protected void onDestroy() {	
		super.onDestroy();
		controller.removeListener(listener);
	};
	
	public void leaveOrDelGroup() {
		MailDialog.Builder builder = new MailDialog.Builder(this);
		builder.setTitle(R.string.notice);
		builder.setMessage(getString(R.string.del_room_mem_comfirm));
		builder.setPositiveButton(getString(R.string.okay_action), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
				if (mGroup.getIsAdmin()) {
					controller.deleteCGroup(account, mGroup, listener);
				}else{
					controller.leaveCGroup(account, mGroup, listener);
				}
			}
		});
		builder.setNeutralButton(getString(R.string.cancel_action), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		builder.create().show();
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
	/**
	 * 设置群成员数量显示
	 * @Description:
	 * @param List<CGroupMember> cGroupMembers
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-7-31
	 */
	private void setMemberCount(List<CGroupMember> cGroupMembers){
		String membertxt = getString(R.string.group_set_gfind_member);
		if(cGroupMembers!=null&&cGroupMembers.size()>0){
			groupMemberTxt.setText(String.format(membertxt,cGroupMembers.size()));
		}else{
			membertxt = membertxt.substring(0, membertxt.indexOf("("));
			groupMemberTxt.setText(membertxt);
		}
	}

	private void cGroupDialog(final int whichViewId) {
		MailDialog.Builder builder = new MailDialog.Builder(this);
		String content ="";
		switch (whichViewId) {
		case R.id.group_msg_all_clean:
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
						case R.id.group_msg_all_clean:
							mDialog.setMessage( getString(R.string.chat_message_all_clean_content_wait));
							mDialog.show();
							controller.deleteAllCMessage(account, mGroup.getUid());
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
