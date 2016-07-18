package cn.mailchat.activity;

import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.R;
import cn.mailchat.chatting.beans.CGroup;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.utils.CommonUtils;

/**
 * 
 * @copyright © 35.com
 * @file name ：GroupChattingInfoChangeActivity.java
 * @author ：zhangjx
 * @create Data ：2014-11-6下午7:12:12 
 * @Current Version：v1.0 
 * @History memory :
 * @Date : 2014-11-6下午7:12:12 
 * @Modified by：zhangjx
 * @Description :群名称等设置
 */
public class GroupChattingInfoChangeActivity extends BaseActionbarFragmentActivity implements OnClickListener {
	// 群名称修改
	public static final int GROUPCHAT_EDITOR = 0;
	public static final int RESULT_CHANGE_MAIL_SIGN = 101;
	public final static String CHANGE_MAIL_SIGN = "change_mail_sign";
	// 编辑类型
	private final static String GROUP_NICK_NAME = "groupNickName";
	private static final String CONTENT = "content";
	private static final String CONTENT_TYPE = "type";

	private String content = "";
	private String type = "";

	private EditText mEditText;
	private LinearLayout mSaveTextView;
	private View mCustomActionbarView;
	private TextView mActionbarTitle;
	private TextView mActionbarSure;
	public static String title;
	public static boolean isfirst;
	//群名
	public static String GROUP_NAMES = "";
	
	public static void forwardContentEditActivity(Context context, String content, String type) {
		Intent tIntent = new Intent();
		tIntent.putExtra(CONTENT, content);
		tIntent.putExtra(CONTENT_TYPE, type);
		tIntent.setClass(context, GroupChattingInfoChangeActivity.class);
		if (type.equals(CHANGE_MAIL_SIGN)) {
			((Activity) context).startActivityForResult(tIntent, RESULT_CHANGE_MAIL_SIGN);
		}else {
			((Activity) context).startActivityForResult(tIntent, GROUPCHAT_EDITOR);
		}
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_group_chatting_info_change);
	
		initializeActionBar();
		initActionbarView();
		initView();
		initData();
		initEvent();

	}
	/** 
	 * method name: initData 
	 * function @Description: TODO
	 * Parameters and return values description：    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-11-6 下午7:19:23	@Modified by：zhangjx
	 *     @Description：
	 */
	private void initData() {
		content = getIntent().getStringExtra(CONTENT);
		type = getIntent().getStringExtra(CONTENT_TYPE);
		if (type.equals(ChattingGroupSettingActivity.GROUP_NAME)) {
			//修改群名称
			setTitle (getResources().getString(R.string.change_group_name));
		} else if (type.equals(ChattingGroupSettingActivity.GROUP_DESCRIPTION)) {
			//修改群描述
			setTitle (getResources().getString(R.string.change_group_describe));
		} else if (type.equals(CHANGE_MAIL_SIGN)) {
			//修改邮件签名
			setTitle (getString(R.string.setting_account_change_mail_sign_title));
		}
//		else if (type.equals(CHANGE_USER_NAME)) {
//			//修改姓名
//			setTitle (getString(R.string.setting_change_username_title));
//			mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(36)}); 
//		}
		
		if (content.equals(getResources().getString(R.string.no_group_name))) {
			mEditText.setText("");
		}else{
			mEditText.setText(content);
		}
		mEditText.setSelection(mEditText.getText().toString().length());
		mEditText.requestFocus();
		openInputMethod(mEditText);
	}
	private void setTitle(String titleName){
		 setActionbarCenterTitle(mCustomActionbarView, mActionbarTitle,titleName);
	}
	private void initView() {
		mEditText = (EditText) findViewById(R.id.edit_view_chat_edit);
	}
	private void initializeActionBar() {
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayUseLogoEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setCustomView(R.layout.actionbar_custom_right_btn);
		mCustomActionbarView = mActionBar.getCustomView();
		mActionBar.setTitle(null);
	}

	private void initActionbarView() {
		 mActionbarTitle = (TextView) mCustomActionbarView
					.findViewById(R.id.tv_title);
		 mActionbarSure = (TextView) mCustomActionbarView
					.findViewById(R.id.tv_sure);
	}
	

	private void initEvent() {
		 mActionbarSure.setOnClickListener(this);
	}

	/**
	 * 
	 * method name: onSaveSignature 
	 * function @Description: TODO
	 * Parameters and return values description：    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-11-6 下午7:25:12	@Modified by：zhangjx
	 *     @Description：保存群描述
	 */
	private void onSaveName() {
		Intent mIntent = new Intent();
		mIntent.putExtra(CONTENT, mEditText.getText().toString());
		if (type.equals(ChattingGroupSettingActivity.GROUP_NAME)) {
			title = mEditText.getText().toString();
			isfirst = true;
		}
		mIntent.putExtra(CONTENT_TYPE, type);
		setResult(RESULT_OK, mIntent);
		closeInputMethod(mEditText);
		finish();
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_sure:
			if (type.equals(ChattingGroupSettingActivity.GROUP_NAME)) {
				//修改群名称
				if(mEditText.getText().toString().trim().equals("")){
					Toast.makeText(GroupChattingInfoChangeActivity.this,getString(R.string.chatting_no_content), 0).show();
					return;
				}else if(mEditText.getText().toString().trim().length()>100){
					Toast.makeText(GroupChattingInfoChangeActivity.this,getString(R.string.group_name_modify), 0).show();
					return;
				}
				// 确定
				onSaveName();
			} else if (type.equals(CHANGE_MAIL_SIGN)) {
				//修改邮件签名/姓名
				closeInputMethod(mEditText);
				onSaveMailSign();
			}
			break;
		default:
			break;
		}
	}
	private void onSaveMailSign() {
		Intent mIntent = new Intent();
		mIntent.putExtra(CONTENT, mEditText.getText().toString());
		setResult(RESULT_OK, mIntent);
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

	private void closeInputMethod(View view) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	private void openInputMethod(View view){
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(view, InputMethodManager.RESULT_SHOWN);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		closeInputMethod(mEditText);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			closeInputMethod(mEditText);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
