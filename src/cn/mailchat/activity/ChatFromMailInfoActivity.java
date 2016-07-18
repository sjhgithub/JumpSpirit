package cn.mailchat.activity;

import cn.mailchat.R;
import cn.mailchat.utils.StringUtil;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatFromMailInfoActivity extends BaseFragmentActivity {
	private static String MAIL_EMAIL = "mail_email";
	private static String MAIL_NICKNAME = "mail_nick_name";
	private static String MAIL_SUBJECT = "mail_subject";
	private static String MAIL_PREVIEW = "mail_preview";
	private TextView subjectTxt,previewTxt;
	private String nickName;
	private String email;
	public static void actionChatFromMailInfo(Context context, String emial, String nickName,
			String mailSubject, String mailPreview) {
		Intent mIntent = new Intent(context, ChatFromMailInfoActivity.class);
		mIntent.putExtra(MAIL_EMAIL, emial);
		mIntent.putExtra(MAIL_NICKNAME, nickName);
		mIntent.putExtra(MAIL_SUBJECT, mailSubject);
		mIntent.putExtra(MAIL_PREVIEW, mailPreview);
		context.startActivity(mIntent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置无标题
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatting_mail_info);
		initView();
		initData();
		initTitleBar();
	}
	
	private void initView() {
		subjectTxt = (TextView) findViewById(R.id.chat_mail_subject);
		previewTxt = (TextView) findViewById(R.id.chat_mail_preview);
	}

	private void initData() {
		Intent mIntent = getIntent();
		email = mIntent.getStringExtra(MAIL_EMAIL);
		nickName=mIntent.getStringExtra(MAIL_NICKNAME);
		subjectTxt.setText(mIntent.getStringExtra(MAIL_SUBJECT));
		previewTxt.setText(mIntent.getStringExtra(MAIL_PREVIEW));
	}

	private void initTitleBar() {
		ImageView imgBack = (ImageView) findViewById(R.id.back);
		TextView tvTitle = (TextView) findViewById(R.id.title);
		TextView tvSecTitle = (TextView) findViewById(R.id.tv_sec_title);
		TextView tvSure = (TextView) findViewById(R.id.tv_sure);
	
		tvTitle.setText(getString(R.string.choose_folder_title));
		imgBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		if(StringUtil.isEmpty(nickName)){
			nickName =StringUtil.getPrdfixStr(email);
		}
		tvTitle.setText(nickName);
		tvSecTitle.setVisibility(View.VISIBLE);
		tvSecTitle.setText(email);
	}
}
