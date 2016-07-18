package cn.mailchat.activity.setup;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.activity.Accounts;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.service.PendingService;
import cn.mailchat.utils.StringUtil;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

/**
 * 程序入口
 * 
 * @author liwt
 * 
 */
public class WelcomeActivity extends Activity {
	private ImageView welcomeBg, cnEn;
	private Timer timer;
	private boolean isShowSplash;
	private LinearLayout view;
	private long time =2000;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(!isTaskRoot()){
		    finish();
		    return;
		}
		view = (LinearLayout) getLayoutInflater().inflate(
				R.layout.activity_welcome, null);
		setContentView(view);
		
		initView();
		initData();
		Intent serviceIntent = new Intent(this, PendingService.class);
		serviceIntent.setAction(MessagingController.CHAT_PENDING);
		startService(serviceIntent);
	}
	//应用内替换闪屏
	private void setData() {
		welcomeBg.setBackgroundResource(R.drawable.bg_welcome);
		// 渐变展示启动屏
		AlphaAnimation aa = new AlphaAnimation(0.1f, 1.0f);
		aa.setDuration(3000);
//		aa.setDuration(1000);
		view.startAnimation(aa);
		aa.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {
				jumpToActivity();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}

		});
	}

	private void initView() {
		welcomeBg = (ImageView) findViewById(R.id.welcome_bg);
	}
	private void initData(){
		init();
	}
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0x0000:
				if(isShowSplash){
					setData();
				}else{
					jumpToActivity();
				}
				break;
			case 0x0001:

				break;
			}

		};
	};

	public boolean isLunarSetting() {
		String language = getLanguageEnv();
		if (language != null
				&& (language.trim().equals("zh-CN") || language.trim().equals(
						"zh-TW")))
			return true;
		else
			return false;
	}

	private String getLanguageEnv() {
		Locale locale = Locale.getDefault();
		String language = locale.getLanguage();
		String country = locale.getCountry().toLowerCase(Locale.US);
		if ("zh".equals(language)) {
			if ("cn".equals(country)) {
				language = "zh-CN";
			} else if ("tw".equals(country)) {
				language = "zh-TW";
			}
		} else if ("pt".equals(language)) {
			if ("br".equals(country)) {
				language = "pt-BR";
			} else if ("pt".equals(country)) {
				language = "pt-PT";
			}
		}
		return language;
	}
	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	private void init(){
//		timer = new Timer();
//		timer.schedule(new TimerTask() {
//
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
				Message message = new Message();
				message.what = 0x0000;
				handler.sendMessage(message);
				isShowSplash=true;
//			}
//		}, time);
	}
	public DisplayImageOptions initImageLoaderOptions() {
		DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisk(true).showImageOnLoading(R.drawable.launch_bg).bitmapConfig(Bitmap.Config.RGB_565).build();
		return options;
    }
	private void jumpToActivity(){
		int guideVersionCode = MailChat.getGuideVersionCode();
		String language = MailChat.application.getLanguage();
		if(guideVersionCode < GlobalConstants.guideVersionCode &&
				(language.equals(Locale.SIMPLIFIED_CHINESE.toString())||language.equals(Locale.TAIWAN.toString()))){
			GuideActivity.actionGuide(WelcomeActivity.this);
		}else{
			Accounts.showAccounts(WelcomeActivity.this);
		}
		finish();
	}
}
