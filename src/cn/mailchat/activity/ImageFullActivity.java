package cn.mailchat.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.umeng.analytics.MobclickAgent;

import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.chatting.beans.CAttachment;
import cn.mailchat.chatting.beans.CMessage;
import cn.mailchat.chatting.beans.DAttachment;
import cn.mailchat.chatting.beans.DChatMessage;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.fragment.ImageFullFragment;
import cn.mailchat.utils.FileUtil;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.view.HackyViewPager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ImageFullActivity extends FragmentActivity  {
	private static final String ACCOUNTUUID = "accountUuid";
	private static final String CMESSAGELIST="cmessages";
	private static final String DMESSAGELIST="dmessages";
	private static final String CGROUP_ACTION="isCGroup";
	private static final String DCHAT_ACTION="isDChat";
	private static final String CURRENITEM ="currentItem";
	private Account account;
	public static LinearLayout titleLayout;
    private HackyViewPager mPager;
    private boolean isCMessage;
    private ArrayList<Serializable> messages;
    private TextView tvTitle;
    private int currentItem;
    private TextView saveView;
    private Serializable currentItemMessage;
    private MessagingController controller;
    private String attachmentId;
    private Handler handler = new Handler();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_full);
		initView();
		initData();
		initTitleBar();
	}
	/**
	 * 群聊查看大图跳转
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-18
	 */
	public static void actionCGroupImageFullActivity(Context context,String accountUid,List<CMessage>  cMessages,int currentItem){
		Intent intent =new Intent(context, ImageFullActivity.class);
		intent.putExtra(CMESSAGELIST, (Serializable)cMessages);
		intent.putExtra(ACCOUNTUUID, accountUid);
		intent.putExtra(CURRENITEM, currentItem);
		intent.setAction(CGROUP_ACTION);
		context.startActivity(intent);
	}
	
	/**
	 * 单聊查看大图跳转
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-18
	 */
	public static void actionDChatImageFullActivity(Context context,String accountUid,List<DChatMessage> dMessages,int currentItem){
		Intent intent =new Intent(context, ImageFullActivity.class);
		intent.putExtra(DMESSAGELIST, (Serializable)dMessages);
		intent.putExtra(ACCOUNTUUID, accountUid);
		intent.putExtra(CURRENITEM, currentItem);
		intent.setAction(DCHAT_ACTION);
		context.startActivity(intent);
	}
	
	private void initView(){
		titleLayout=(LinearLayout) findViewById(R.id.layout_imgfull_title);
		mPager = (HackyViewPager) findViewById(R.id.pager);
	}
	
	private void initData() {
		Intent intent =getIntent();
		account = Preferences.getPreferences(this).getAccount(intent.getStringExtra((ACCOUNTUUID)));
		if(intent.getAction().equals(CGROUP_ACTION)){
			isCMessage=true;
			messages=	(ArrayList<Serializable>) intent.getSerializableExtra(CMESSAGELIST);
		}else if(intent.getAction().equals(DCHAT_ACTION)){
			isCMessage=false;
			messages=(ArrayList<Serializable>) intent.getSerializableExtra(DMESSAGELIST);
		}
		ImagePagerAdapter imagePagerAdapter =new ImagePagerAdapter(getSupportFragmentManager(),messages);
		mPager.setAdapter(imagePagerAdapter);
		currentItem=intent.getIntExtra(CURRENITEM, 0);
		mPager.setCurrentItem(currentItem);
		currentItemMessage=messages.get(currentItem);
		controller = MessagingController.getInstance(getApplication());
		controller.addListener(listener);
		setCurrentItemMessageAttachmentId();
	}
	private void initTitleBar() {
		ImageView imgBack = (ImageView) findViewById(R.id.back);
		imgBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		tvTitle = (TextView) findViewById(R.id.title);
		tvTitle.setText(currentItem+1+"/"+messages.size());
		// 更新下标
		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				tvTitle.setText(arg0+1+"/"+messages.size());
				currentItemMessage = messages.get(arg0);
				setCurrentItemMessageAttachmentId();
			}

		});
		saveView = (TextView) findViewById(R.id.save);
		saveView.setVisibility(View.VISIBLE);
		saveView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String filePath ="";
				String fileName ="";
				if(currentItemMessage instanceof DChatMessage){
					DChatMessage dMessage = (DChatMessage)currentItemMessage;
					DAttachment dAttachment  =dMessage.getAttachments().get(0);
					fileName =dAttachment.getName();
					filePath =dAttachment.getFilePath();
					if(StringUtil.isEmpty(filePath)){
						filePath = MailChat.getInstance().getAttFilePath(MailChat.getInstance().getChatImageCacheDirectory(account),dAttachment.getAttchmentId(), fileName);
					}
				}else{
					CMessage cMessage = (CMessage)currentItemMessage;
					CAttachment cAttachment = cMessage.getAttachment();
					fileName =cAttachment.getName();
					filePath =cAttachment.getFilePath();
					if(StringUtil.isEmpty(filePath)){
						filePath = MailChat.getInstance().getAttFilePath(MailChat.getInstance().getChatImageCacheDirectory(account),cAttachment.getAttchmentId(), fileName);
					}
				}
				controller.saveImageToPhoto(account,filePath, fileName);
			}
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		controller.removeListener(listener);
	}
	public  static void setTitleHideAnimation() {
		if (titleLayout.getVisibility() == View.VISIBLE) {
			TranslateAnimation hideAnimation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,
					-1);
			AnimationSet hideSet = new AnimationSet(true);
			hideSet.addAnimation(hideAnimation);
			hideAnimation.setDuration(200);
			hideAnimation.setFillAfter(false);
			hideAnimation
					.setAnimationListener(new TranslateAnimation.AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}

						@Override
						public void onAnimationEnd(Animation animation) {
							titleLayout.setVisibility(View.INVISIBLE);
						}
					});
			titleLayout.startAnimation(hideSet);
		}
	}
	
	public static void setTitleShowAnimation() {
		if (titleLayout.getVisibility() == View.INVISIBLE) {
			TranslateAnimation hideAnimation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, -1, Animation.RELATIVE_TO_SELF,
					0.0f);
			AnimationSet hideSet = new AnimationSet(true);
			hideSet.addAnimation(hideAnimation);
			hideAnimation.setDuration(200);
			hideAnimation.setFillAfter(false);
			hideAnimation
					.setAnimationListener(new TranslateAnimation.AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}

						@Override
						public void onAnimationEnd(Animation animation) {
							titleLayout.setVisibility(View.VISIBLE);
						}
					});
			titleLayout.startAnimation(hideSet);
		}
	}
	
	private class ImagePagerAdapter extends FragmentStatePagerAdapter {

		public List<Serializable>  messages;

		public ImagePagerAdapter(FragmentManager fm, ArrayList<Serializable>  arrayList) {
			super(fm);
			this.messages = arrayList;
		}

		@Override
		public int getCount() {
			return messages == null ? 0 : messages.size();
		}

		@Override
		public Fragment getItem(int position) {
			messages.get(position);
			return ImageFullFragment.newInstance(messages.get(position),isCMessage, account.getUuid());
		}

	}
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("ImageFullActivity"); //统计页面
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("ImageFullActivity"); //统计页面
		MobclickAgent.onPause(this);
	}

	private MessagingListener listener =new MessagingListener(){
		public void fileDownloadProgress(Account acc, String id, final int progress) {
			if (account.getUuid().equals(acc.getUuid())
					&& attachmentId.equals(id)) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						saveView.setVisibility(View.GONE);
					}
				});
			}
		};

		public void fileDownloadFinished(Account acc, String id) {
			if (account.getUuid().equals(acc.getUuid())
					&& attachmentId.equals(id)) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						saveView.setVisibility(View.VISIBLE);
					}
				});

			}
		};

		public void fileDownloadFailed(Account acc, String id) {
			if (account.getUuid().equals(acc.getUuid())
					&& attachmentId.equals(id)) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						saveView.setVisibility(View.GONE);
					}
				});
			}
		};
		public void chattingSaveImageSuccess(Account acc){
			if (account.getUuid().equals(acc.getUuid())) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Toast.makeText(getApplicationContext(), R.string.save_image_succeed, Toast.LENGTH_SHORT).show();
					}
				});

			}
		}
		public void chattingSaveImageFail(Account acc){
			if (account.getUuid().equals(acc.getUuid())) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Toast.makeText(getApplicationContext(), R.string.save_image_failed, Toast.LENGTH_SHORT).show();
					}
				});

			}
		}
	};

	private void setCurrentItemMessageAttachmentId(){
		if(currentItemMessage instanceof DChatMessage){
			DChatMessage dMessage = (DChatMessage)currentItemMessage;
			DAttachment  dAttachment = dMessage.getAttachments().get(0);
			attachmentId=dAttachment.getAttchmentId();
		}else{
			CMessage cMessage = (CMessage)currentItemMessage;
			CAttachment  cAttachment =cMessage.getAttachment();
			attachmentId=cAttachment.getAttchmentId();
		}
	}
}
