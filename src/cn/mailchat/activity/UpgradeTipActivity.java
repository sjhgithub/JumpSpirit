package cn.mailchat.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.R;
import cn.mailchat.upgrade.UpgradeInfo;

public class UpgradeTipActivity extends Activity implements OnClickListener {
	
	public static final String INTENT_EXTRA_UPGRADE_INFO = "intent_extra_upgrade_info";
	
	private UpgradeInfo mUpgradeInfo;
	
	private TextView mTitleTextView;
	
	private TextView mMessageTextView;
	
	private Button mConfirmButton;
	
	private Button mCancelButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setTheme(R.style.customDialogActivityStyle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		setContentView(R.layout.activity_upgrade_tip);
		
		setUpTestInfo();
		initView();
	}
	
	private void initView (){
		mTitleTextView  = (TextView) findViewById(R.id.title);
		mMessageTextView = (TextView) findViewById(R.id.message);
		mConfirmButton = (Button) findViewById(R.id.positiveButton);
		mCancelButton = (Button) findViewById(R.id.negativeButton);
		
		mTitleTextView.setText("更新提示标题:"+mUpgradeInfo.getVersion());
		mMessageTextView.setText(""+mUpgradeInfo.getDescription());
		
		mConfirmButton.setOnClickListener(this);
		mCancelButton.setOnClickListener(this);
		
		if (mUpgradeInfo.isForceUpgrade()){
			mCancelButton.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onBackPressed() {
		if (!mUpgradeInfo.isForceUpgrade()){
			super.onBackPressed();
		}
	}
	
	@Override
	protected void onDestroy() {
		if (mUpgradeInfo != null && mUpgradeInfo.isForceUpgrade()){
			//强制升级时，退出升级提示页面，则关闭应用
			cn.mailchat.utils.ActivityManager.popAll();
			android.os.Process.killProcess(android.os.Process.myPid());
		}
		super.onDestroy();
	}
	
	private void setUpTestInfo (){
		mUpgradeInfo = new UpgradeInfo("2.0", false, "", "这里是版本更新信息 \n1.adfasgdadasfa");
	}

	@Override
	public void onClick(View v) {
		if (v == mConfirmButton){
			Toast.makeText(this, "begin upgrade ...", Toast.LENGTH_SHORT).show();
		}
		
		if (v == mCancelButton){
			finish();
		}
	}
	
}

