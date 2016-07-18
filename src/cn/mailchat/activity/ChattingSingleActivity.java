package cn.mailchat.activity;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.LruCache;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.LogCollector;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.setup.AccountSetupBasics;
import cn.mailchat.adapter.DChattingAdapter;
import cn.mailchat.beans.AddrInfo;
import cn.mailchat.beans.PickedFileInfo;
import cn.mailchat.chatting.beans.CAttachment;
import cn.mailchat.chatting.beans.CGroup;
import cn.mailchat.chatting.beans.CMessage;
import cn.mailchat.chatting.beans.DAttachment;
import cn.mailchat.chatting.beans.DChat;
import cn.mailchat.chatting.beans.DChatMessage;
import cn.mailchat.chatting.beans.CMessage.State;
import cn.mailchat.chatting.beans.DChatMessage.Type;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.controller.NotificationCenter;
import cn.mailchat.helper.StringUtils;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.mail.store.LocalStore;
import cn.mailchat.search.LocalSearch;
import cn.mailchat.surprise.SurpriseAnimationManager;
import cn.mailchat.utils.ActivityManager;
import cn.mailchat.utils.BaiduLocationUtil;
import cn.mailchat.utils.CommonUtils;
import cn.mailchat.utils.DateUtil;
import cn.mailchat.utils.EncryptUtil;
import cn.mailchat.utils.FileUtil;
import cn.mailchat.utils.ImageUtils;
import cn.mailchat.utils.NetUtil;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.utils.SystemUtil;
import cn.mailchat.utils.Utility;
import cn.mailchat.view.FaceRelativeLayout;
import cn.mailchat.view.OverflowMenuPopo;
import cn.mailchat.view.OverflowMenuPopo.OverflowMenuPopoListener;

/**
 * 
 * @copyright © 35.com
 * @file name ：ChattingSingleActivity.java
 * @author ：zhangjx
 * @create Data ：2014-8-19下午2:03:47
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2014-8-19下午2:03:47
 * @Modified by：zhangjx
 * @Description :单聊界面
 */
public class ChattingSingleActivity extends BaseActionbarFragmentActivity {
	public static final String TAG_NOTIFY_SINGLE_CHATTING_EXIST_FLAG = "notify_into_SingleChatting_ContactAttributeExist";
	public static final String TAG_NOTIFY_SINGLE_CHATTING_FLAG = "notify_into_SingleChatting";
	public static final String TAG_SINGLE_CHATTING_EXIST_FLAG = "singleChatting_ContactAttributeExist";
	public static final String DCHAT = "dchat";
	public static final String LOGIN_FAULED_EMAIL = "login_failed_email";//登录失败输入框的email
	public static final String DMESSAGE = "dmessage";
	public static final String TOEMAIL = "toEmail";
	public static final String TO_NICKNAME= "toNickname";
	public static final String BACKLAST = "backLast";
	public static final String ACCOUNTUUID = "accountUuid";
	private static final String ACTION_CHATLIST = "cn.mailchat.intent.action.CHAT_LIST";
	public static final String ACTION_DNOTIFY = "dnotify";
	private static final String ACTION_FORWARD = "singleForward";
	private static final String ACTION_DCHAT_FROM_MAIL_INFO = "dchat_from_mail_info";//消息透传
	private static final int ACTIVITY_REQUEST_PICK_ATTACHMENT = 1;// 文件的requestcode
	private static final int CAMERA_WITH_DATA = 2; // 照相的requestCode;
	private static final int PHOTO_PICKED_WITH_DATA = 3; // 相册的requestCode;
	private static final int LOCATION_WITH_DATA = 4; // 定位的requestCode;
	public static final int FILE_DOWNLOAD = 5;
	
	private static final int CLOSE_LAYOUT_VIEW_TOOSHORT = 8;// 关闭录音时间太短的提示
	private static final int SHOW_MIC = 9; // 显示录音提示
	private static final int CLOSE_MIC = 10; // 关闭录音提示
	private static final int VOICE1 = 11;
	private static final int VOICE2 = 12;
	private static final int VOICE3 = 13;
	private static final int VOICE4 = 14;
	private static final int VOICE5 = 15;
	private static final int VOICE6 = 16;
	
	
	/**
	 * 标题
	 */
	//private TextView title;
	/**
	 * 返回按钮
	 */
//	private ImageView layoutBack;
	/**
	 * 标题右侧按钮
	 */
	//private ImageView chattingSet;
	/**
	 * 文本输入框
	 */
	private EditText chattingTitleTxt;
	/**
	 * 输入框左侧模式切换按钮
	 */
	private ImageButton changeChattingState;
	/**
	 * 文字输入框 父布局
	 */
	private RelativeLayout chattingContentLayout;
	/**
	 * 表情
	 */
	private ImageButton panelEmojiLayout;
	/**
	 * 附件区，拍照
	 */
	private RelativeLayout panelphotographLayout;
	/**
	 * 相册
	 */
	private RelativeLayout panelalbumLayout;
	/**
	 * 文件
	 */
	private RelativeLayout panelfileLayout;
	/**
	 * 日志
	 */
	private RelativeLayout panellogLayout;
	/**
	 * 日志按键
	 */
	private Button panellogBt;
	/**
	 * 表情
	 */
	private FaceRelativeLayout faceLayout;
	private RelativeLayout panellocationLayout;
	private FrameLayout attachmentPanel;
	private ImageButton showAttachmentPanel;
	private LinearLayout tooshortLayout;
	
	private LinearLayout cancleLayout;
	private ImageView canlVoice;
	private View ivMic;
	private LinearLayout voiceRecord;
	// 录音时波纹图片刷新
	private ComposeHandler mHandler;
	// 录音机
	private MediaRecorder recorder;
	// 录音文件
	private File recordFile;
	
	private ListView mSingleChattingView;
	private Button sendTxt;
	private LinearLayout chatting_bottom_util;
	private RelativeLayout loading_layout;
	private RelativeLayout pushMessageLayout;
	private TextView pushhMessageText;
	private TextView pushhMessageCount;

	private int mode = 1;// 输入框状态 1：文字 2：语音
	// 照片路径
	private File mPhotoFile;

	// 图片缓存
	private LruCache<String, Bitmap> mMemoryCache;
	private Map<Integer, Integer> progressMap;
	private Map<String, Boolean> suspendedMap;// 附件暂停
	// 消息
	private List<DChatMessage> pushMesssage;
	private DChattingAdapter dChatAdapter;
	private MessagingController controller;
	private Account account;
	private MediaPlayer mMediaPlayer;
	private boolean isOnlyRead = false;
	private DChatMessage message;
	private boolean hasMore = true;
	private boolean isLoadMore = false;
	private int showedCount = 0;
	// 每次加载条数
	private int loadLimit = 10;
	private String dChatUid;
	private String email;
	private boolean isHaveForwardMessage;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS",Locale.CHINA);
	private View mCustomActionbarView;

	private TextView mActionBarTitleName, mActionBarTitleNameSub;
	private String nickname;
	
	private NotificationCenter notificationCenter;
	
	private LinearLayout noNetLinearLayoutView;
	private TextView setNetTextView;
	private DChat dChat;
	private String mAccountUuid;
	//登录失败输入框的email
	private String 	mLoginFailedEmail;
	private FragmentManager mFragmentManager;
	private String toTopic;
	private boolean isHelpAccount;
	private TextView tvTitle ;

	private RelativeLayout operationFooterView;
	private RelativeLayout operationOAFooterView;
	private TextView oaRemainTxtView,oaInitiatedTxtView,oaProcessedTxtView;
	private ImageView oaHomeImgView;
	private boolean isOAdChat;
	private boolean isMailInfoToDChatMessage;
	private boolean isMailInfoAction;

	public static void actionChatList(Context mContext,DChat dchat, Account mAccount,String loginFailedEmail) {
		Intent tIntent = new Intent(mContext, ChattingSingleActivity.class);
		tIntent.putExtra(ACCOUNTUUID, mAccount.getUuid());
		tIntent.putExtra(DCHAT, dchat);
		tIntent.putExtra(LOGIN_FAULED_EMAIL, loginFailedEmail);
		tIntent.setAction(ACTION_CHATLIST);
		mContext.startActivity(tIntent);
	}

	public static void actionChatList(Context mContext,DChat dchat, Account mAccount) {
		Intent tIntent = new Intent(mContext, ChattingSingleActivity.class);
		tIntent.putExtra(ACCOUNTUUID, mAccount.getUuid());
		tIntent.putExtra(DCHAT, dchat);
		tIntent.setAction(ACTION_CHATLIST);
		mContext.startActivity(tIntent);
	}

	public static void actionChatList(Context mContext,DChat dchat, Account mAccount,boolean isContactAttributeExist) {
		Intent tIntent = new Intent(mContext, ChattingSingleActivity.class);
		tIntent.putExtra(ACCOUNTUUID, mAccount.getUuid());
		tIntent.putExtra(DCHAT, dchat);
		tIntent.putExtra(TAG_SINGLE_CHATTING_EXIST_FLAG, isContactAttributeExist);
		tIntent.setAction(ACTION_CHATLIST);
		mContext.startActivity(tIntent);
	}

	public static Intent actionNotifyDMessage(Context mContext,
			DChatMessage message,DChat dchat,Account mAccount,int flg,boolean isContactAttributeExist) {
		Intent tIntent = new Intent(mContext, ChattingSingleActivity.class);
		tIntent.putExtra(ACCOUNTUUID, mAccount.getUuid());
		tIntent.putExtra(DMESSAGE, message);
		tIntent.putExtra(DCHAT, dchat);
		tIntent.putExtra(TAG_NOTIFY_SINGLE_CHATTING_FLAG, flg);
		tIntent.putExtra(TAG_NOTIFY_SINGLE_CHATTING_EXIST_FLAG, isContactAttributeExist);
		tIntent.setAction(ACTION_DNOTIFY);
		return tIntent;
	}
	public static void actionForwardDMessage(Context mContext, DChatMessage message,DChat dchat,Account mAccount) {
		Intent tIntent = new Intent(mContext, ChattingSingleActivity.class);
		tIntent.putExtra(ACCOUNTUUID, mAccount.getUuid());
		tIntent.putExtra(DMESSAGE, message);
		tIntent.putExtra(DCHAT, dchat);
		tIntent.setAction(ACTION_FORWARD);
		mContext.startActivity(tIntent);
	}
	/**
	 * 消息透传
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2016-3-3
	 */
	public static void actionDChatFromMailInfo(Context mContext, DChatMessage message,DChat dchat,Account mAccount,boolean isContactAttributeExist){
		Intent tIntent = new Intent(mContext, ChattingSingleActivity.class);
		tIntent.putExtra(ACCOUNTUUID, mAccount.getUuid());
		tIntent.putExtra(DMESSAGE, message);
		tIntent.putExtra(DCHAT, dchat);
		tIntent.putExtra(TAG_NOTIFY_SINGLE_CHATTING_EXIST_FLAG, isContactAttributeExist);
		tIntent.setAction(ACTION_DCHAT_FROM_MAIL_INFO);
		mContext.startActivity(tIntent);
	}
//	public static void actionPositionDMessage(Context mContext, CMessage message, CGroup cGroup,Account mAccount) {
//		Intent tIntent = new Intent(mContext, ChattingActivity.class);
//		tIntent.putExtra(ACCOUNTUUID, mAccount.getUuid());
//		tIntent.putExtra(CMESSAGE, message);
//		tIntent.putExtra(CGOUP, cGroup);
//		tIntent.setAction(ACTION_POSITION);
//		mContext.startActivity(tIntent);
//	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityManager.push(this);
		setContentView(R.layout.activity_chatting);
		if (!isHelpAccount) {
		initializeActionBar();
		}
		initView();
		initData();
		if (!isHelpAccount) {
			initActionbarView();
		}else {
			mActionBar.hide();
			initTitleBar();
		}
		initListener();
		registerNetWorkReceiver();
		notificationCenter =NotificationCenter.getInstance();
		MobclickAgent.onEvent(getApplicationContext(),"into_single_chat_act");
		
		surpriseAnimationManager = new SurpriseAnimationManager(this, dChatAdapter);
	}
	
	SurpriseAnimationManager surpriseAnimationManager;

	private void initTitleBar() {
		ImageView imgBack = (ImageView) findViewById(R.id.back);
		tvTitle = (TextView) findViewById(R.id.title);
		TextView tvSecTitle = (TextView) findViewById(R.id.tv_sec_title);
		RelativeLayout layout_title_bar = (RelativeLayout) findViewById(R.id.layout_title_bar);
		layout_title_bar.setVisibility(View.VISIBLE);
		tvSecTitle.setVisibility(View.VISIBLE);
		tvTitle.setText(nickname);
		tvSecTitle.setText(email);
		imgBack.setClickable(true);
		imgBack.setOnClickListener(myClickListener());
	}
	private void initializeActionBar() {
		// add by zhangjx start hide icon
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(true);
		// add by zhangjx end
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setCustomView(R.layout.actionbar_custom_center_titles);
		// mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayUseLogoEnabled(true);
		mCustomActionbarView = mActionBar.getCustomView();
		// 返回按钮
		mActionBar.setDisplayHomeAsUpEnabled(true);
	}

	private void initActionbarView() {
		mActionBarTitleName = (TextView) mCustomActionbarView
				.findViewById(R.id.actionbar_title_name);
		mActionBarTitleNameSub = (TextView) mCustomActionbarView
				.findViewById(R.id.actionbar_title_sub);
		//昵称
		mActionBarTitleName.setText(nickname);
		if(email.endsWith(GlobalConstants.DCHAT_OA)){
			mActionBarTitleNameSub.setText(account.getEmail());
		}else{
			//email
			mActionBarTitleNameSub.setText(email);
		}
	}
	@Override
	protected void onResume() {
		super.onResume();	
		notificationCenter.notifyClean(account);
		MobclickAgent.onPageStart("ChattingSingleActivity"); //统计页面
		MobclickAgent.onResume(this);

        if (NetUtil.isActive()) {
            noNetLinearLayoutView.setVisibility(View.GONE);
        } else {
            noNetLinearLayoutView.setVisibility(View.VISIBLE);
        }
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPageEnd("ChattingSingleActivity"); //统计页面
		MobclickAgent.onPause(this); 
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		controller.removeListener(dCallback);
		unRegisterNetWorkReceiver();
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			if (getIntent().getIntExtra(TAG_NOTIFY_SINGLE_CHATTING_FLAG, 0) == 1&&!isHelpAccount) {
				MailChat.isChat=true;
				changeAccount();
				ActivityManager.popAll();
				jumpToMain();
			}
			updateDraft();
			dChatAdapter.stopPlay();
			finish();
			CommonUtils.hideSoftInput(this);
			return true;
		case R.id.action_DChat_setting:
			if(isOAdChat){
				new OverflowMenuPopo(ChattingSingleActivity.this,  getResources().getStringArray(R.array.sign_oa_chat_right_menu), new OverflowMenuPopoListener() {
					@Override
					public void onMenuItemClick(int position) {
						switch (position) {
						case 0:
							// 跳转发起事务
						String url=GlobalConstants.OA_BASE_URL_START+(account.getoAHost()!=null?account.getoAHost():"oa."+Utility.getEmailDomain(account.getEmail()))
								        + GlobalConstants.OA_BASE_NEW_URL_END
								        + "&p=" + Utility.getOAUserParam(account)+"&type="+account.getoALoginType();
									jumpToWebView(url);
							break;
						case 1:
							// 签到   获取经纬度
							if (!NetUtil.isActive()) {
								NetUtil.showNoConnectedAlertDlg(ChattingSingleActivity.this);
								return;
							}
							getLocation();
							break;
						}
					}
				}).showMoreOptionMenu(findViewById(item.getItemId()));
			
			}else{
				ChattingSingleSettingActivity.startChattingSingleSettingActivity(this, dChatUid, email,nickname,dChat.getImgHeadHash(),account);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	private void getLocation(){
		 new BaiduLocationUtil(ChattingSingleActivity.this, new cn.mailchat.utils.BaiduLocationUtil.ReceiveBaiduLocationListener() {
			
			@Override
			public void onReceiveLocationSuccess(List<AddrInfo> list) {
				String gpsString="";
				if (list!=null&&list.size()>0) {
					AddrInfo addrInfo=list.get(0);
					gpsString=addrInfo.getmLongitude()+","+addrInfo.getmLatitude();
				}
				String url=   GlobalConstants.OA_BASE_URL_START+(account.getoAHost()!=null?account.getoAHost():"oa."+Utility.getEmailDomain(account.getEmail()))
				        + GlobalConstants.OA_SING_URL_END
				        + "&p="+Utility.getOAUserParam(account)
				        + "&device=" + SystemUtil.getCliendId(MailChat.app)
				        + "&gps=" + gpsString+"&type="+account.getoALoginType();
					jumpToWebView(url);
			}
			
			@Override
			public void onReceiveLocationFailed() {
				Toast.makeText(ChattingSingleActivity.this,"定位失败,请稍候重试", Toast.LENGTH_SHORT)
						.show();
			}
		});
	}
	private void initListener() {
		changeChattingState.setOnClickListener(myClickListener());
		sendTxt.setOnClickListener(myClickListener());
		changeChattingState.setOnClickListener(myClickListener());
		voiceRecord.setOnClickListener(myClickListener());
		panelEmojiLayout.setOnClickListener(myClickListener());
		panelalbumLayout.setOnClickListener(myClickListener());
		panelfileLayout.setOnClickListener(myClickListener());
		panellocationLayout.setOnClickListener(myClickListener());
		showAttachmentPanel.setOnClickListener(myClickListener());
		pushMessageLayout.setOnClickListener(myClickListener());
		panelphotographLayout.setOnClickListener(myClickListener());
		panellogBt.setOnClickListener(myClickListener());
		//OA相关点击
		oaRemainTxtView.setOnClickListener(myClickListener());
		oaInitiatedTxtView.setOnClickListener(myClickListener());
		oaProcessedTxtView.setOnClickListener(myClickListener());
		oaHomeImgView.setOnClickListener(myClickListener());
		chattingTitleTxt.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					chattingContentLayout
							.setBackgroundResource(R.drawable.bg_edittext_p);
				} else {
					chattingContentLayout
							.setBackgroundResource(R.drawable.bg_edittext_n);
					panelEmojiLayout.setImageResource(R.drawable.icon_emoji);
				}
			}
		});

		chattingTitleTxt.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				if (!StringUtil.isEmpty(chattingTitleTxt.getText().toString()
						.trim())) {
					showAttachmentPanel.setVisibility(View.GONE);
					sendTxt.setVisibility(View.VISIBLE);
				} else {
					showAttachmentPanel.setVisibility(View.VISIBLE);
					sendTxt.setVisibility(View.GONE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub

			}

		});
		chattingTitleTxt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				faceLayout.setVisibility(View.GONE);
				chatting_bottom_util.setVisibility(View.VISIBLE);
				attachmentPanel.setVisibility(View.GONE);
				panelEmojiLayout.setImageResource(R.drawable.icon_emoji);
				// 弹出软键盘
				chattingTitleTxt.requestFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(chattingTitleTxt, 0);
			}
		});
		

		mSingleChattingView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				// 当不滚动时
				case OnScrollListener.SCROLL_STATE_IDLE:
					// 判断滚动到顶部
					if (view.getFirstVisiblePosition() == 0) {
						// 自动加载更多
						if (hasMore && !isLoadMore) {
							loadingMore();
						}
					} else if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
						pushMesssage.clear();
						pushMessageLayout.setVisibility(View.GONE);
					}
					break;
				}
			}
		});
		voiceRecord.setOnTouchListener(new OnTouchListener() {
			long startTime;
			long endTime;
			float y;
			boolean isCancle = false;
			boolean isRecord = false;// 是否在录音中
			Thread thread;

			Runnable r = new Runnable() {

				@Override
				public void run() {
					// 获得MIC声音大小
					while (isRecord) {
						int amp = recorder.getMaxAmplitude();
						if (amp != 0) {
							// System.out.println("tMaxAmplitude "+amp);
							if (amp <= 2000) {
								mHandler.sendEmptyMessage(VOICE1);
							} else if (amp < 4000) {
								mHandler.sendEmptyMessage(VOICE2);
							} else if (amp < 8000) {
								mHandler.sendEmptyMessage(VOICE3);
							} else if (amp < 12000) {
								mHandler.sendEmptyMessage(VOICE4);
							} else if (amp < 20000) {
								mHandler.sendEmptyMessage(VOICE5);
							} else {
								mHandler.sendEmptyMessage(VOICE6);
							}
						}
					}
					MobclickAgent.onEvent(ChattingSingleActivity.this,"Use_Voice_Record");
				}
			};

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					dChatAdapter.stopPlay();
					voiceRecord
							.setBackgroundResource(R.drawable.bg_btn_voice_rcd_p);
					isCancle = false;
					try {
						recorder = new MediaRecorder();
						recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
						recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
						recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
						// recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
						// recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
						// recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
						recorder.setMaxDuration(60 * 1000);// 最多1分种
						String path = MailChat.getInstance().getChatVoiceDirectory(account)+ 
								DateUtil.dateToString(new Date(),
										DateUtil.SHORT_LINE_FORMAT_TWO)+".amr";
						recordFile = new File(path);
						recorder.setOutputFile(path);
						recorder.prepare();// 准备
						recorder.start();// 开始录音
						mHandler.sendEmptyMessage(SHOW_MIC);
						startTime = System.currentTimeMillis();
						isRecord = true;
						thread = new Thread(r);
						thread.start();
						recorder.setOnInfoListener(new OnInfoListener() {

							@Override
							public void onInfo(MediaRecorder mr, int what, int extra) {
								// TODO Auto-generated method stub
//								MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN
//								MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED //最大时间监听
//								MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED //最大文件大小监听
								if(what==MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
									endTime = System.currentTimeMillis();
									if (endTime - startTime > 60 * 1000) {
										mHandler.removeMessages(SHOW_MIC);
										mHandler.sendEmptyMessage(CLOSE_MIC);
										cancleLayout.setVisibility(View.GONE);
										voiceRecord.setBackgroundResource(R.drawable.bg_btn_voice_rcd_n);
										mHandler.sendEmptyMessage(VOICE1);
										isRecord = false;
										if (recorder != null) {
											recorder.stop();
											recorder.release();
											recorder = null;
											DChatMessage message = new DChatMessage(DChatMessage.Type.VOICE);
											message.setUuid(UUID.randomUUID().toString());
											message.setReceiverEmail(email);
											message.setDchatUid(dChatUid);
											message.setSenderEmail(account.getEmail());
											message.setTime(System.currentTimeMillis());
											contrastLastTime(message);
											List<DAttachment> attachments =new ArrayList<DAttachment>();
											DAttachment attachment = new DAttachment();
											attachment.setMessageUid(message.getUuid());
											attachment.setAttchmentId(UUID.randomUUID().toString());
											attachment.setVoiceLength((int) ((endTime - startTime) / 1000));
											String newPath = MailChat.getInstance().getChatVoiceDirectory(account) + EncryptUtil.getMd5(attachment.getAttchmentId())+ ".amr";	
											recordFile.renameTo(new File(newPath));
											attachment.setFilePath(newPath);
											attachments.add(attachment);
											message.setAttachments(attachments);
											contrastLastTime(message);
											dChatAdapter.addOrUpdateMessage(message);
											mSingleChattingView.setSelection(dChatAdapter.getCount());
											sendMailInfoToDChatMessage();
											controller.publishDMessage(toTopic, message, account, null);
										}
									}
								}
							}
						});
					} catch (IllegalStateException e1) {
						e1.printStackTrace();
					} catch (Exception e1) {
						e1.printStackTrace();
						recorder.reset();
						recorder.release();
						recorder = null;
						isRecord = false;
					}
				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					mHandler.removeMessages(SHOW_MIC);
					mHandler.sendEmptyMessage(CLOSE_MIC);
					cancleLayout.setVisibility(View.GONE);
					voiceRecord.setBackgroundResource(R.drawable.bg_btn_voice_rcd_n);
					mHandler.sendEmptyMessage(VOICE1);
					isRecord = false;
					try {
						if (recorder != null) {
							recorder.stop();
							endTime = System.currentTimeMillis();
							// recorder.reset();
							recorder.release();
							recorder = null;
							if (endTime - startTime < 1000) {// 时长不够
								tooshortLayout.setVisibility(View.VISIBLE);
								mHandler.sendEmptyMessageDelayed(CLOSE_LAYOUT_VIEW_TOOSHORT, 800);
							} else if (isCancle) {

							} else {
								DChatMessage message = new DChatMessage(DChatMessage.Type.VOICE);
								message.setUuid(UUID.randomUUID().toString());
								message.setReceiverEmail(email);
								message.setDchatUid(dChatUid);
								message.setSenderEmail(account.getEmail());
								message.setTime(System.currentTimeMillis());
								contrastLastTime(message);
								List<DAttachment> attachments =new ArrayList<DAttachment>();
								DAttachment attachment = new DAttachment();
								attachment.setMessageUid(message.getUuid());
								attachment.setAttchmentId(UUID.randomUUID().toString());
								attachment.setVoiceLength((int) ((endTime - startTime) / 1000));
								String newPath = MailChat.getInstance().getChatVoiceDirectory(account) + EncryptUtil.getMd5(attachment.getAttchmentId())+ ".amr";	
								recordFile.renameTo(new File(newPath));
								attachment.setFilePath(newPath);
								attachments.add(attachment);
								message.setAttachments(attachments);
								contrastLastTime(message);
								dChatAdapter.addOrUpdateMessage(message);
								mSingleChattingView.setSelection(dChatAdapter.getCount());
								sendMailInfoToDChatMessage();
								controller.publishDMessage(toTopic, message, account, null);
							}
						}
					} catch (IllegalStateException e) {
						e.printStackTrace();
					}
				}
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					if (y == 0) {
						y = event.getY();// 原点
					}
					float yy = event.getY();
					if (yy - y < -70 && ivMic.getVisibility() == View.VISIBLE) {// 打开取消提示
						mHandler.sendEmptyMessage(CLOSE_MIC);
						cancleLayout.setVisibility(View.VISIBLE);
						isCancle = true;
					}
				}
				return false;
			}
		});
	}

	private void doBackBtn() {
		if (getIntent().getIntExtra(TAG_NOTIFY_SINGLE_CHATTING_FLAG, 0) == 1
				&& !isHelpAccount) {
			MailChat.isChat = true;
			changeAccount();
			ActivityManager.popAll();
			jumpToMain();
		}
		updateDraft();
		dChatAdapter.stopPlay();
		finish();
	}
	private OnClickListener myClickListener() {
		return new OnClickListener() {
			private String url;

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.back:
					doBackBtn();
					CommonUtils.hideSoftInput(ChattingSingleActivity.this);
					finish();
					break;
				case R.id.ibtn_view_chatting_attach_btn:
					chattingTitleTxt.requestFocus();
					// 点击加号
					if (attachmentPanel.getVisibility() == View.GONE) {
						attachmentPanel.setVisibility(View.VISIBLE);
						if (faceLayout.getVisibility() == View.VISIBLE) {
							faceLayout.setVisibility(View.GONE);
							chatting_bottom_util.setVisibility(View.VISIBLE);
						}

						mode = 2;
						voiceRecord.setVisibility(View.GONE);
						chattingContentLayout.setVisibility(View.VISIBLE);
						changeChattingState
								.setImageResource(R.drawable.selector_btn_chatting_setmode_voice);

					} else {
						if (faceLayout.getVisibility() == View.VISIBLE) {
							faceLayout.setVisibility(View.GONE);
							chatting_bottom_util.setVisibility(View.VISIBLE);
						} else {
							attachmentPanel.setVisibility(View.GONE);
						}

					}
					panelEmojiLayout.setImageResource(R.drawable.icon_emoji);
//					chattingTitleTxt
//							.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
//									| InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
//									| InputType.TYPE_TEXT_FLAG_MULTI_LINE
//									| InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
					closeInputMethod();
					break;
				case R.id.ibtn_view_chatting_mm:
					// 表情按钮点击
					if (faceLayout.getVisibility() == View.VISIBLE) {
						faceLayout.setVisibility(View.GONE);
						chatting_bottom_util.setVisibility(View.VISIBLE);
						attachmentPanel.setVisibility(View.GONE);
						panelEmojiLayout
								.setImageResource(R.drawable.icon_emoji);
						// 弹出软键盘
						chattingTitleTxt.requestFocus();
//						chattingTitleTxt
//								.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
//										| InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
//										| InputType.TYPE_TEXT_FLAG_MULTI_LINE
//										| InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
						InputMethodManager imm = (InputMethodManager) chattingTitleTxt
								.getContext().getSystemService(
										Context.INPUT_METHOD_SERVICE);
						imm.showSoftInput(chattingTitleTxt, 0);
					} else {
						faceLayout.setVisibility(View.VISIBLE);
						attachmentPanel.setVisibility(View.VISIBLE);
						chatting_bottom_util.setVisibility(View.GONE);

						panelEmojiLayout
								.setImageResource(R.drawable.icon_emoji_p);
						// 获取焦点
						chattingTitleTxt.requestFocus();
						//chattingTitleTxt.setInputType(InputType.TYPE_NULL);
						closeInputMethod();
					}
					break;
				case R.id.layoutView_chatting_bottom_panel_location:
					panelButOnclick(R.id.layoutView_chatting_bottom_panel_location);

					break;
				case R.id.layoutView_chatting_panel_bottom_photograph:
					panelButOnclick(R.id.layoutView_chatting_panel_bottom_photograph);
					break;
				case R.id.layoutView_chatting_panel_bottom_album:
					panelButOnclick(R.id.layoutView_chatting_panel_bottom_album);
					break;
				case R.id.layoutView_chatting_panel_bottom_file:
					panelButOnclick(R.id.layoutView_chatting_panel_bottom_file);
					break;
				case R.id.ibtn_view_chatting_mode_btn:
					// 输入模式切换
//					if (mode == 1) {
//						mode = 2;
//					} else {
//						mode = 1;
//					}
//					changeMode();
//					controller.updateDChatInputMode(account,dChatUid,mode);
					surpriseAnimationManager.startAnim();
					break;
				case R.id.btn_view_chatting_send:
					String txtContent = chattingTitleTxt.getText().toString().trim();
					if (txtContent.length() == 0) {
						Toast.makeText(ChattingSingleActivity.this,
								getString(R.string.chatting_no_content), 0)
								.show();
						return;
					}
					sendTextMsg(txtContent);
					break;
				case R.id.layout_push_message:
					pushMesssage.clear();
					pushMessageLayout.setVisibility(View.GONE);
					mSingleChattingView.setSelection(dChatAdapter.getCount());
					mSingleChattingView.post(new Runnable() {
						 @Override
						 public void run() {
							 mSingleChattingView.smoothScrollToPosition(dChatAdapter.getCount()-1);
						 }
					 });
					mSingleChattingView.setSelection(dChatAdapter.getCount() - 1);
					break;
				case R.id.tv_oa_remain:
					 url=  GlobalConstants.OA_BASE_URL_START+(account.getoAHost()!=null?account.getoAHost():"oa."+Utility.getEmailDomain(account.getEmail()))
						        + GlobalConstants.OA_BASE_NEW_TRANS_URL_END
						        + "&p=" + Utility.getOAUserParam(account)+"&type="+account.getoALoginType();
							jumpToWebView(url);
					break;
				case R.id.tv_oa_initiated:
					 url=GlobalConstants.OA_BASE_URL_START+(account.getoAHost()!=null?account.getoAHost():"oa."+Utility.getEmailDomain(account.getEmail()))
					        + GlobalConstants.OA_BASE_MYCREATE_URL_END
					        + "&p=" + Utility.getOAUserParam(account)+"&type="+account.getoALoginType();
						jumpToWebView(url);
					break;
				case R.id.tv_oa_processed:
					 url= GlobalConstants.OA_BASE_URL_START+(account.getoAHost()!=null?account.getoAHost():"oa."+Utility.getEmailDomain(account.getEmail()))
					        + GlobalConstants.OA_BASE_MYPASS_URL_END
					        + "&p=" + Utility.getOAUserParam(account)+"&type="+account.getoALoginType();
						jumpToWebView(url);
					break;
				case R.id.bt_chatting_log:
					LogCollector.saveLog(new java.io.File(MailChat.app.getFilesDir(), LogCollector.LOG_FILE));
					File logFile =new File(MailChat.app.getFilesDir().getPath()+"/"+LogCollector.LOG_FILE);
					if(!logFile.exists()){
						Toast.makeText(ChattingSingleActivity.this, getString(R.string.send_log_fail), Toast.LENGTH_SHORT).show();
						return;
					}
					Toast.makeText(ChattingSingleActivity.this, getString(R.string.send_log_sending), Toast.LENGTH_SHORT).show();
					panellogBt.setClickable(false);
					panellogBt.setBackgroundResource(R.drawable.btn_chatting_footer_log_icon_p);
					//生成消息
					ArrayList<DAttachment> dAttachments = new ArrayList<DAttachment>();
					DChatMessage message = new DChatMessage(DChatMessage.Type.ATTACHMENT);
					message.setUuid(UUID.randomUUID().toString()+GlobalConstants.LOG_MESSAGE_UID_SUFFIX);//标记为日志信息
					message.setTime(System.currentTimeMillis());
					message.setSenderEmail(account.getEmail());
					message.setReceiverEmail(email);
					message.setDchatUid(dChatUid);
					message.setMessageState(2);
					DAttachment attachment = new DAttachment();
					attachment.setMessageUid(message.getUuid());
					attachment.setAttchmentId(UUID.randomUUID().toString());
					attachment.setFilePath(logFile.getPath());
					attachment.setAttchmentId(UUID.randomUUID().toString());
					attachment.setName("log");
					attachment.setSize(logFile.length());
					attachment.setLocalPathFlag(1);
					dAttachments.add(attachment);
					message.setAttachments(dAttachments);

					controller.publishDMessage(toTopic, message, account, null);
					break;
				case R.id.img_oa_home:
					 url= GlobalConstants.OA_BASE_URL_START+(account.getoAHost()!=null?account.getoAHost():"oa."+Utility.getEmailDomain(account.getEmail()))
						        + GlobalConstants.OA_BASE_MAIN_URL_END
						        + "&p=" + Utility.getOAUserParam(account)+"&type="+account.getoALoginType();
							jumpToWebView(url);
					break;
				default:
					break;
				}
			}


		};
	}

	private void jumpToWebView(String url) {
		// Log.d("qxian", ">>>"+url);
		WebViewWithErrorViewActivity.forwardOpenUrlActivity(
				ChattingSingleActivity.this, url, null, account.getUuid(), -1,
				true);
	}
	/**
	 * 
	 * method name: sendTextMsg function @Description: TODO Parameters and
	 * return values description： field_name void return type
	 * 
	 * @History memory：
	 * @Date：2014-8-19 下午4:43:09 @Modified by：zhangjx
	 * @Description：发送文本消息
	 */
	private void sendTextMsg(String txtContent) {
		DChatMessage message = createDChatMessage(txtContent);
		dChatAdapter.addOrUpdateMessage(message);
		controller.publishDMessage(toTopic, message, account, null);
		mSingleChattingView.setSelection(dChatAdapter.getCount());
		chattingTitleTxt.setText("");
		MobclickAgent.onEvent(ChattingSingleActivity.this, "send_d_message");
	}
	private DChatMessage sendTextMsgNoSave(String txtContent){
		DChatMessage message = createDChatMessage(txtContent);
		message.setDeleteflag(1);
		controller.publishDMessage(toTopic, message, account, null);
		MobclickAgent.onEvent(ChattingSingleActivity.this, "send_d_message");
		return message;
	}
	private DChatMessage createDChatMessage(String txtContent) {
		DChatMessage message = new DChatMessage();
		message.setUuid(UUID.randomUUID().toString());
		message.setTime(System.currentTimeMillis());
		message.setMessageContent(txtContent);	
		message.setSenderEmail(account.getEmail());
		message.setReceiverEmail(email);
		message.setDchatUid(dChatUid);	
		message.setMessageState(2);
		message.setMessageType(DChatMessage.Type.TEXT);
		contrastLastTime(message);
		dChatAdapter.addOrUpdateMessage(message);
		mSingleChattingView.setSelection(dChatAdapter.getCount());
		sendMailInfoToDChatMessage();
		chattingTitleTxt.setText("");

		return message;
	}
	
	private void initData() {
		pushMesssage =new ArrayList<DChatMessage>();
		controller = MessagingController.getInstance(getApplication());
		controller.addListener(dCallback);
		mAccountUuid= getIntent().getStringExtra(ACCOUNTUUID);
		mLoginFailedEmail= getIntent().getStringExtra(LOGIN_FAULED_EMAIL);
		account = Preferences.getPreferences(this).getAccount(mAccountUuid);
		dChat =(DChat) getIntent().getSerializableExtra(DCHAT);
		email=dChat.getEmail();
		if(email.equals(GlobalConstants.HELP_ACCOUNT_EMAIL)){
			panellogLayout.setVisibility(View.VISIBLE);
		}
		nickname=dChat.getNickName();
		if(StringUtil.isEmpty(nickname)){
			nickname=email.substring(0, email.indexOf("@"));
		}
		switch (dChat.getdChatType()) {
		case OA:
			nickname =getString(R.string.dchat_oa_nike_name);
			operationFooterView.setVisibility(View.GONE);
			operationOAFooterView.setVisibility(View.VISIBLE);
			isOAdChat=true;
			break;
		default:
			operationFooterView.setVisibility(View.VISIBLE);
			operationOAFooterView.setVisibility(View.GONE);
			break;
		}
		if(email.endsWith(GlobalConstants.HIDE_ACCOUNT_SUFFIX)){
			nickname=getString(R.string.chat_anonymous);
			toTopic=email;
		}else{
			toTopic=email+"/1";
		}
		if(account.getEmail().endsWith(GlobalConstants.HIDE_ACCOUNT_SUFFIX)){
			isHelpAccount=true;
		}
		dChatUid =dChat.getUid();
		if(dChat.isDraft()){
			chattingTitleTxt.setText(dChat.getDraftContent());
			chattingTitleTxt.setSelection(dChat.getDraftContent().length());
		}else{
			chattingTitleTxt.setText("");
		}
//		toEmail=getIntent().getStringExtra(TOEMAIL);
//		toNickname=getIntent().getStringExtra(TO_NICKNAME);
//		dChatUid =DChat.getDchatUid(toEmail+","+account.getEmail());
		//title.setText(toEmail);
		if (ACTION_FORWARD.equals(getIntent().getAction())) {
			message = (DChatMessage) getIntent().getSerializableExtra(DMESSAGE);		
			isHaveForwardMessage = true;
		}else if(ACTION_DCHAT_FROM_MAIL_INFO.equals(getIntent().getAction())){
			message = (DChatMessage) getIntent().getSerializableExtra(DMESSAGE);
			isMailInfoToDChatMessage =true;
			isMailInfoAction =true;
		}
		// isBackLast = getIntent().getBooleanExtra(BACKLAST, false);
		mMediaPlayer = new MediaPlayer();
		dChatAdapter = new DChattingAdapter(this, mSingleChattingView,
				account, null, mMediaPlayer,dChat);
		mSingleChattingView.setAdapter(dChatAdapter);
		mode =dChat.getInputType();
		changeMode();
		if (getIntent().getIntExtra(TAG_NOTIFY_SINGLE_CHATTING_FLAG, 0) == 1) {
			if(!Preferences.getPreferences(this).getDefaultAccount().getEmail().equals(account.getEmail())){
				showCoustomToast(account.getName(), account.getEmail());
			}
			if(!getIntent().getBooleanExtra(TAG_NOTIFY_SINGLE_CHATTING_EXIST_FLAG, true)){
				controller.syncRemoteDChatUserInfo(account, email);
			}
		}
		if(!getIntent().getBooleanExtra(TAG_SINGLE_CHATTING_EXIST_FLAG, true)){
			controller.syncRemoteDChatUserInfo(account, email);
		}
		controller.listDMessages(account, dChatUid, 30,null);
		controller.updateDChatReadState(account, dChatUid,0,null);
		// 在用户从登录页面点击到小助手页面时，发送下他在登录输入框输入的邮箱账号
		String loginEmail = "";
		if (!StringUtils.isNullOrEmpty(mLoginFailedEmail)
				&& !mLoginFailedEmail.startsWith("@")) {
			loginEmail = mLoginFailedEmail;
		}
		if (email.equals(GlobalConstants.HELP_ACCOUNT_EMAIL)) {
			controller.publishUserMsgToHelpAccount(account, toTopic, loginEmail, UUID
					.randomUUID().toString(), getString(R.string.version_name));
		}
	}
	private void showCoustomToast(String nickName, String email) {
		View layout = getLayoutInflater().inflate(R.layout.layout_toast,
				(ViewGroup) findViewById(R.id.toast_layout_root));

		TextView textNickname = (TextView) layout
				.findViewById(R.id.tv_nickname);
		TextView tvEmail = (TextView) layout.findViewById(R.id.tv_email);
		textNickname.setText(nickName);
		tvEmail.setText(email);

		Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.TOP, 0, 300);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}
	private void initView() {
		chattingTitleTxt = (EditText) findViewById(R.id.edit_view_chatting_content_et);
		changeChattingState = (ImageButton) findViewById(R.id.ibtn_view_chatting_mode_btn);
		chattingContentLayout = (RelativeLayout) findViewById(R.id.layout_view_text_panel_ll);

		panelEmojiLayout = (ImageButton) findViewById(R.id.ibtn_view_chatting_mm);
		// 附件布局
		attachmentPanel = (FrameLayout) findViewById(R.id.frameView_chatting_bottom_panel);
		// 拍照
		panelphotographLayout = (RelativeLayout) findViewById(R.id.layoutView_chatting_panel_bottom_photograph);
		// 相册
		panelalbumLayout = (RelativeLayout) findViewById(R.id.layoutView_chatting_panel_bottom_album);
		// 文件
		panelfileLayout = (RelativeLayout) findViewById(R.id.layoutView_chatting_panel_bottom_file);
		//日志
		panellogLayout = (RelativeLayout) findViewById(R.id.layoutView_chatting_bottom_panel_log);
		panellogBt = (Button) findViewById(R.id.bt_chatting_log);
		// 表情
		faceLayout = (FaceRelativeLayout) findViewById(R.id.facechoose);
		faceLayout.setEt_sendmessage(chattingTitleTxt);
		// 定位
		panellocationLayout = (RelativeLayout) findViewById(R.id.layoutView_chatting_bottom_panel_location);
		// 加号
		showAttachmentPanel = (ImageButton) findViewById(R.id.ibtn_view_chatting_attach_btn);
		mHandler = new ComposeHandler(ChattingSingleActivity.this);
		// 提示录音太短
		tooshortLayout = (LinearLayout) findViewById(R.id.layout_view_tooshort);
		// 取消录音
		cancleLayout = (LinearLayout) findViewById(R.id.layout_view_cancle_mic);
		// 录音提示
		canlVoice = (ImageView) findViewById(R.id.img_view_canl_voice);
		// 录音提示父布局
		ivMic = (RelativeLayout) findViewById(R.id.layout_view_mic);
		// 聊天列表
		mSingleChattingView = (ListView) findViewById(R.id.lv_view_chatting_content);
		// 录音按钮
		voiceRecord = (LinearLayout) findViewById(R.id.btn_view_voice_record);
		// 发送按钮
		sendTxt = (Button) findViewById(R.id.btn_view_chatting_send);
		// 底部隐藏布局
		chatting_bottom_util = (LinearLayout) findViewById(R.id.chatting_bottom_util);
		// 进度条
		loading_layout = (RelativeLayout) findViewById(R.id.loading_layout);

		pushMessageLayout = (RelativeLayout) findViewById(R.id.layout_push_message);
		pushhMessageText = (TextView) findViewById(R.id.push_message_txt);
		pushhMessageCount = (TextView) findViewById(R.id.push_message_count);
		pushMessageLayout.setVisibility(View.GONE);
		//chattingSet.setVisibility(View.VISIBLE);
		noNetLinearLayoutView =	(LinearLayout) findViewById(R.id.item_net);
		noNetLinearLayoutView.setVisibility(View.GONE);
		setNetTextView = (TextView) findViewById(R.id.net_set);
		operationFooterView =(RelativeLayout) findViewById(R.id.operation_footer);
		operationOAFooterView =(RelativeLayout) findViewById(R.id.operation_footer_oa);
		oaRemainTxtView=(TextView) findViewById(R.id.tv_oa_remain);
		oaInitiatedTxtView=(TextView) findViewById(R.id.tv_oa_initiated);
		oaProcessedTxtView=(TextView) findViewById(R.id.tv_oa_processed);
		oaHomeImgView = (ImageView) findViewById(R.id.img_oa_home);
		setNetTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
//				startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
				NetUtil.showSystemSettingView(ChattingSingleActivity.this);
			}
		});
	}
	/**
	 * 音频播放波纹图片的变换
	 * 
	 * @Description:
	 * @author:Zhonggaoyong
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:2013-11-4
	 */
	static class ComposeHandler extends Handler {

		WeakReference<ChattingSingleActivity> mReference;

		public ComposeHandler(ChattingSingleActivity chattingSingleActivity) {
			mReference = new WeakReference<ChattingSingleActivity>(chattingSingleActivity);
		}

		@Override
		public void handleMessage(Message msg) {
			if (mReference.get() != null) {
				switch (msg.what) {

				case CLOSE_LAYOUT_VIEW_TOOSHORT:
					mReference.get().tooshortLayout.setVisibility(View.GONE);
					break;
				case SHOW_MIC:
					mReference.get().ivMic.setVisibility(View.VISIBLE);
					break;
				case CLOSE_MIC:
					mReference.get().ivMic.setVisibility(View.GONE);
					break;
				case VOICE1:
					mReference.get().canlVoice.setImageResource(R.drawable.voice1);
					break;
				case VOICE2:
					mReference.get().canlVoice.setImageResource(R.drawable.voice2);
					break;
				case VOICE3:
					mReference.get().canlVoice.setImageResource(R.drawable.voice3);
					break;
				case VOICE4:
					mReference.get().canlVoice.setImageResource(R.drawable.voice4);
					break;
				case VOICE5:
					mReference.get().canlVoice.setImageResource(R.drawable.voice5);
					break;
				case VOICE6:
					mReference.get().canlVoice.setImageResource(R.drawable.voice6);
					break;
				}
			}
		}

	}

	/**
	 * 
	 * method name: onActivityResult 
	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
	 * function@Description: TODO
	 * @History memory：
	 * @Date：2014-11-13 下午1:29:11	@Modified by：zhangyq
	 * @Description：
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case CAMERA_WITH_DATA:
			// 拍照获得附件
			if (resultCode == Activity.RESULT_OK) {
				if (mPhotoFile != null) {
					String gotPhotoPath = mPhotoFile.getPath();
					String picPath = gotPhotoPath;
					DChatMessage message = new DChatMessage(DChatMessage.Type.IMAGE);
					message.setUuid(UUID.randomUUID().toString());
					message.setTime(System.currentTimeMillis());
					message.setSenderEmail(account.getEmail());
					message.setReceiverEmail(email);
					message.setDchatUid(dChatUid);
					message.setMessageState(2);
					DAttachment attachment = new DAttachment();
					attachment.setAttchmentId(UUID.randomUUID().toString());
					attachment.setMessageUid(message.getUuid());
					String[] filePathDebris =gotPhotoPath.split("/");
					attachment.setName(filePathDebris[filePathDebris.length-1]);
					attachment.setFilePath(picPath);
					attachment.setLocalPathFlag(1);
				    ArrayList<DAttachment> dAttachments = new ArrayList<DAttachment>();
				    dAttachments.add(attachment);
					message.setAttachments(dAttachments);	
					contrastLastTime(message);
					//由于先要在界面上显示，所以不能等压缩后，再设置上传压缩图片宽高，直接设置本地原图宽高（基本不会有什么影响）。
					int[] size = ImageUtils.getNativeImageSize(attachment.getFilePath());
					if(size!=null&&size.length==2){
						attachment.setImageWidth(size[0]);
						attachment.setImageHeight(size[1]);
					}
					dChatAdapter.addOrUpdateMessage(message);
					mSingleChattingView.setSelection(dChatAdapter.getCount());
					sendMailInfoToDChatMessage();
					controller.publishDMessage(toTopic, message, account, null);
				}
			}
			break;
		case PHOTO_PICKED_WITH_DATA:

			// 选择相册获得附件
			if (resultCode == Activity.RESULT_OK) {
//				String picPath = FileUtil.getMediaFilePathByUri(ChattingActivity.this, data.getData());
				//Rom 4.4获取图片无法获取到路径
				String picPath= FileUtil.getPath(ChattingSingleActivity.this, data.getData());
				DChatMessage message = new DChatMessage(DChatMessage.Type.IMAGE);
				message.setUuid(UUID.randomUUID().toString());
				message.setTime(System.currentTimeMillis());
				message.setSenderEmail(account.getEmail());
				message.setReceiverEmail(email);
				message.setDchatUid(dChatUid);
				message.setMessageState(2);
				DAttachment attachment = new DAttachment();
				attachment.setAttchmentId(UUID.randomUUID().toString());
				attachment.setMessageUid(message.getUuid());
				attachment.setFilePath(picPath);
				String[] filePathDebris =picPath.split("/");
				attachment.setName(filePathDebris[filePathDebris.length-1]);
				attachment.setLocalPathFlag(1);
			    ArrayList<DAttachment> dAttachments = new ArrayList<DAttachment>();
			    dAttachments.add(attachment);
				message.setAttachments(dAttachments);	
				contrastLastTime(message);

				//TODO:这里需要将特殊手机（如浩哥的手机）图片角度旋转正确，再设置宽高，建议再写一个子线程处理图片旋转设置宽高，设置好了chattingAdapter.addOrUpdateMessage(message)去更新界面，再调用发送

				//由于先要在界面上显示，所以不能等压缩后，再设置上传压缩图片宽高，直接设置本地原图宽高（基本不会有什么影响）。
				int[] size = ImageUtils.getNativeImageSize(attachment.getFilePath());
				if(size!=null&&size.length==2){
					attachment.setImageWidth(size[0]);
					attachment.setImageHeight(size[1]);
				}
				dChatAdapter.addOrUpdateMessage(message);
				mSingleChattingView.setSelection(dChatAdapter.getCount());
				sendMailInfoToDChatMessage();
				controller.publishDMessage(toTopic, message, account, null);
			}
			break;
		case ACTIVITY_REQUEST_PICK_ATTACHMENT:

			// 选择文件返回结果
			if (data == null) {
				return;
			}
			/**
			 * 解析附件信息 [从选择文件页面回来]
			 * 
			 * @Description:
			 * @param data
			 * @see:
			 * @since:
			 * @author: zhuanggy
			 * @date:2013-9-23
			 */
			Map<String, PickedFileInfo> checkedFileMap = (Map<String, PickedFileInfo>) data.getSerializableExtra("checkedFileMap");
			for (Entry<String, PickedFileInfo> Entry : checkedFileMap.entrySet()) {
				ArrayList<DAttachment> dAttachments = new ArrayList<DAttachment>();
				DChatMessage message = new DChatMessage(DChatMessage.Type.ATTACHMENT);
				message.setUuid(UUID.randomUUID().toString());
				message.setTime(System.currentTimeMillis());
				message.setSenderEmail(account.getEmail());
				message.setReceiverEmail(email);
				message.setDchatUid(dChatUid);
				message.setMessageState(2);
				PickedFileInfo file = Entry.getValue();
				DAttachment attachment = new DAttachment();
				attachment.setMessageUid(message.getUuid());
				attachment.setAttchmentId(UUID.randomUUID().toString());
				attachment.setFilePath(file.mContentUri.replace("file://", ""));
				attachment.setAttchmentId(UUID.randomUUID().toString());
				attachment.setName(file.mName);
				attachment.setContentType(file.mContentType);
				attachment.setSize(file.mSize);
				attachment.setLocalPathFlag(1);
				dAttachments.add(attachment);
				message.setAttachments(dAttachments);
				contrastLastTime(message);
				dChatAdapter.addOrUpdateMessage(message);
				mSingleChattingView.setSelection(dChatAdapter.getCount());
				sendMailInfoToDChatMessage();
				controller.publishDMessage(toTopic, message, account, null);
			}
			break;
//		case LOCATION_WITH_DATA:
//			if (resultCode == Activity.RESULT_OK) {
//				message = new DChatMessage(DChatMessage.Type.LOCATION);
//				message.setUuid(UUID.randomUUID().toString());
//				message.setTime(getSendTime());
//				message.setFromEmail(account.getEmail());
//				message.setToEmail(toEmail);
//				message.setDchatUid(dChatUid);
//				message.setLocationName(data.getStringExtra("name"));
//				message.setAddress(data.getStringExtra("addr"));
//				message.setLongitude(data.getDoubleExtra("x", 0));
//				message.setLatitude(data.getDoubleExtra("y", 0));
//				if(data.getBooleanExtra("type", true)){
//					message.setLocationType("baidu");
//				}else{
//					message.setLocationType("google");
//				}
//				if (!NetUtil.isActive()) {
//					message.setMessageState("1");
//				}
//				contrastLastTime(message);
//				controller.sendDMessage(account, message);
//				dChatAdapter.addOrUpdateMessage(message);
//				// mGroupChattingView.setSelection(chattingAdapter.getCount());
//			}
//			break;
//		case FILE_DOWNLOAD:
//			if (resultCode == Activity.RESULT_OK) {
//				chattingAdapter.getSuspendedMap().put(chattingAdapter.getmMessages().get(chattingAdapter.getPosition()).getAttachment().getAttchmentId(), true);
//			//	chattingAdapter.getmMessages().get(chattingAdapter.getPosition()).getAttachment().setSuspended(true);
//				chattingAdapter.updateView(chattingAdapter.getPosition());
//				chattingAdapter.notifyDataSetChanged();
//			}
//			break;
		}

	}
	
	/**
	 * 单聊\群聊底部面板操作（发送图片、文件、位置等）
	 * 
	 * method name: panelButOnclick 
	 * function @Description: TODO
	 * Parameters and return values description：
	 * @param whichButton    field_name
	 * void    return type
	 * @History memory：
	 * @Date：2014-11-13 下午1:09:51	@Modified by：zhangyq
	 * @Description：
	 */
	public void panelButOnclick(int whichButton) {
		attachmentPanel.setVisibility(View.GONE);
		switch (whichButton) {
		case R.id.layoutView_chatting_panel_bottom_photograph:
			try {
				try {
					// mPhotoFile = new
					// File(WeMail.getInstance().getImageChaceDirectory(AccountLogic.getInstance().getDefaultAccount()),
					// FileUtil.getCameraFileName());
					String photoPath = GlobalConstants.DEF_DIR + "DCIM/image/";
					File file = new File(photoPath);
					if (!file.exists()) {
						file.mkdirs();
					}
					mPhotoFile = new File(photoPath, FileUtil.getCameraFileName());
					Intent intentTakePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
					intentTakePicture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
					startActivityForResult(intentTakePicture, CAMERA_WITH_DATA);
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(MailChat.getInstance(), getString(R.string.message_compose_equipment_not_start), Toast.LENGTH_LONG).show();
				}
				break;
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(
								MailChat.getInstance(),
								getString(R.string.message_compose_equipment_not_start),
								Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.layoutView_chatting_panel_bottom_album:
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
			intent.setType("image/*");
			startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);

			break;
		case R.id.layoutView_chatting_panel_bottom_file:
			// 选择文件
			Intent i = new Intent(this, FilePickerActivity.class);
			i.putExtra(FilePickerActivity.SINGLE_CHOICE, false);
			startActivityForResult(i, ACTIVITY_REQUEST_PICK_ATTACHMENT);
			break;
//		case R.id.layoutView_chatting_bottom_panel_location:
//			// startActivity(new Intent(this, SelectLocationActivity.class));
//
//			Intent intentLocation = new Intent(this,
//					SelectLocationActivity.class);
//			startActivityForResult(intentLocation, LOCATION_WITH_DATA);
//			break;
		default:
			break;
		}
	}

	/**
	 * 改变输入模式
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2014-3-18
	 */
	private void changeMode() { 
		chattingTitleTxt.requestFocus();
		attachmentPanel.setVisibility(View.GONE);
		if (mode == 2) {
			// 语音输入
			voiceRecord.setVisibility(View.VISIBLE);
			chattingContentLayout.setVisibility(View.GONE);
			showAttachmentPanel.setVisibility(View.VISIBLE);
			sendTxt.setVisibility(View.GONE);
			changeChattingState
					.setImageResource(R.drawable.selector_btn_chatting_setmode_text);
			closeInputMethod();
		} else {
			// 文字输入
			voiceRecord.setVisibility(View.GONE);
			chattingContentLayout.setVisibility(View.VISIBLE);
			changeChattingState
					.setImageResource(R.drawable.selector_btn_chatting_setmode_voice);
			if (!StringUtil.isEmpty(chattingTitleTxt.getText().toString()
					.trim())) {
				showAttachmentPanel.setVisibility(View.GONE);
				sendTxt.setVisibility(View.VISIBLE);
			} else {
				showAttachmentPanel.setVisibility(View.VISIBLE);
				sendTxt.setVisibility(View.GONE);
			}
		}
	}

	private void closeInputMethod() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(chattingTitleTxt.getWindowToken(), 0);
	}

	public void setTranscriptModeScroll() {
		new Thread(new Runnable() {

			public void run() {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mSingleChattingView
						.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
			}
		}).start();
	}

	public void setTranscriptModeDisabled() {
		mSingleChattingView
				.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
	}

	private MessagingListener dCallback = new MessagingListener() {
		 public void chatMessageArrived(Account mAccount,final DChatMessage dChatMessage){
			//判断是否是本地用户和对方发来的消息
			if (dChatMessage.getDchatUid().endsWith(dChatUid)&&account.getUuid().equals(mAccount.getUuid())) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// 列表不在最底端，提示框显示
						int lastVisiblePosition = mSingleChattingView.getLastVisiblePosition();
						if (lastVisiblePosition != dChatAdapter.getmDChatMessages().size() - 1) {
							setTranscriptModeDisabled();
							dChatAdapter.addOrUpdateMessage(dChatMessage);
							pushMesssage.add(dChatMessage);
							pushMessageLayout.setVisibility(View.VISIBLE);
							String content = dChatMessage.getMessageContent();
							switch (dChatMessage.getMessageType()) {
							case IMAGE:
								content = MailChat.getInstance().getString(R.string.msg_img);
								break;
							case ATTACHMENT:
								content = MailChat.app.getString(R.string.msg_att);
								break;
							case VOICE:
								content = MailChat.getInstance().getString(R.string.msg_voice);
								break;
							case LOCATION:
								content = MailChat.app.getString(R.string.msg_location);
								break;
							default:
								break;
							}
							pushhMessageText.setText(nickname + "：" + content);
							int count = pushMesssage.size();
							if (count > 1) {
								pushhMessageCount.setText(count + "");
							} else {
								pushhMessageCount.setText("");
							}
							setTranscriptModeScroll();
						} else {
							dChatAdapter.addOrUpdateMessage(dChatMessage);
							mSingleChattingView.setSelection(dChatAdapter.getCount());
						}	
						controller.updateDChatReadState(account, dChatUid,0, null);
					}
				});
			}

		}
		/**
		 * 
		 * method name: sendDChatMessageSuccess 
		 * @see cn.mailchat.logic.MessageCallback#sendDChatMessageSuccess(cn.mailchat.Account, java.lang.String)
		 * function@Description: TODO
		 *  @History memory：
		 *     @Date：2014-8-22 下午3:28:07	@Modified by：zhangjx
		 *     @Description：发送单聊消息成功
		 */
		public void sendDMessagesSuccess(Account mAccount, final String uid) {
			if (account.getUuid().equals(mAccount.getUuid())) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if(!uid.endsWith(GlobalConstants.LOG_MESSAGE_UID_SUFFIX)){
							dChatAdapter.updateMessage(uid,true);
						}else{
							panellogBt.setClickable(true);
							panellogBt.setBackgroundResource(R.drawable.btn_chatting_footer_log_icon_n);
							Toast.makeText(ChattingSingleActivity.this, getString(R.string.send_log_success), Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		}
		/**
		 * 
		 * method name: sendDChatMessageFailed 
		 * @see cn.mailchat.logic.MessageCallback#sendDChatMessageFailed(cn.mailchat.Account, java.lang.String)
		 * function@Description: TODO
		 *  @History memory：
		 *     @Date：2014-8-22 下午3:28:19	@Modified by：zhangjx
		 *     @Description：发送单聊消息失败
		 */
		public void sendDMessagesFail(Account mAccount, final String dMessageUid) {
			if (account.getUuid().equals(mAccount.getUuid())) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if(!dMessageUid.endsWith(GlobalConstants.LOG_MESSAGE_UID_SUFFIX)){
							dChatAdapter.updateMessage(dMessageUid, false);
						}else{
							panellogBt.setClickable(true);
							panellogBt.setBackgroundResource(R.drawable.btn_chatting_footer_log_icon_n);
							Toast.makeText(ChattingSingleActivity.this, getString(R.string.send_log_fail), Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		}

		public void listDMessagesFinished(String uuid, String dchatUid,final List<DChatMessage> dChatMessages) {
			if (account.getUuid().equals(uuid) && dchatUid.equals(dChatUid)) {	
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(dChatMessages!=null){
							dChatAdapter.updateMessage(dChatMessages);
							if (isHaveForwardMessage) {
								forwardMessage();
							} else if(isMailInfoToDChatMessage){
								mailInfoToDChatMessage();
							} else {
								if (isLoadMore) {
									mSingleChattingView.setSelection(dChatAdapter.getCount() - showedCount);
									isLoadMore = false;
									if (dChatAdapter.getCount() < showedCount + loadLimit) {
										hasMore = false;
									}
								}else{
									mSingleChattingView.setSelection(dChatAdapter.getCount());
								}
							}
						}
					}
				});
				
			}
		}
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
		public void deleteDChatSuccess(final Account acc, String dChatUid) {
			if (account.getUuid().equals(acc.getUuid())) {	
				mHandler.post(new Runnable() {
	
					@Override
					public void run() {
						finish();
					}
				});
			}
		}
		
		public void againDownDChatThumbnailImageSuccess(Account acc) {
			if (acc.getUuid().equals(account.getUuid())) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						dChatAdapter.notifyDataSetChanged();
					}
				});
			}
		}

		public void againDownDChatThumbnailImageFailed(Account acc) {
			if (acc.getUuid().equals(account.getUuid())) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						dChatAdapter.notifyDataSetChanged();
						Toast.makeText(ChattingSingleActivity.this,  R.string.again_down_thumbnail_image_fail,Toast.LENGTH_SHORT).show();
					}
				});
			}
		}
		
		public void createGroupSuccess(String uuid,CGroup cGroup) {
			if (uuid.equals(account.getUuid())) {
				finish();
			}
		}
		public void sendDMessageTimeSuccess(Account acc, final String dMessageUid,final long time) {
			if (acc.getUuid().equals(account.getUuid())) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						dChatAdapter.updateMessageTime(dMessageUid, time);
					}
				});
			}
		}
		public void uploadStart(Account acc,final String id){
			if(acc.getUuid().equals(account.getUuid())){
				//更新上传条目进度
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						setAttachmentUploadState(id,-1);
					}
				});
			}
		}
		public void uploadProgress(Account account,final String id, final int progress){
			if (account.getUuid().equals(account.getUuid())) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						setAttachmentUploadState(id, progress);
					}
				});
			}
		}
		public void uploadInterrupt(Account acc,final String id){
			if(acc.getUuid().equals(account.getUuid())){
				//更新上传条目进度
				dChatAdapter.deleteMessageForAtt(id);
			}
		}
		public void fileDownloadStart(Account acc,final String id){
			if(acc.getUuid().equals(account.getUuid())){
				//更新上传条目进度
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						setAttachmentDownloadState(id, false, true, -1,false);
					}
				});
			}
		};
		
		public void fileDownloadProgress(Account acc,final String id,final int progress){
			if(acc.getUuid().equals(account.getUuid())){
				//更新上传条目进度
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						setAttachmentDownloadState(id, false, true, progress,false);
					}
				});
			}
		};
		
		public void fileDownloadFinished(Account acc,final String id){
			if(acc.getUuid().equals(account.getUuid())){
				//更新上传条目进度
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						setAttachmentDownloadState(id, false, true, 100,true);
					}
				});
			}
		};
		
		public void fileDownloadFailed(Account acc,final String id){
			if(acc.getUuid().equals(account.getUuid())){
				//更新上传条目进度
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						setAttachmentDownloadState(id, true, false, 0,true);
					}
				});
			}
		};
		
		public void fileDownloadInterrupt(Account acc,final String id){
			if(acc.getUuid().equals(account.getUuid())){
				//更新上传条目进度
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						setAttachmentDownloadState(id, true, false, 0,true);
					}
				});
			}
		};

		public void updateChattingImgHead(Account acc,final ContactAttribute newContactAttribute){
			if(acc.getUuid().equals(account.getUuid())&&email.equals(newContactAttribute.getEmail())){
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						dChatAdapter.setToEmailImgHeadHash(newContactAttribute.getImgHeadHash());
						dChatAdapter.setNickName(newContactAttribute.getNickName());
						dChatAdapter.notifyDataSetChanged();
						String nickName = newContactAttribute.getNickName();
						if (nickName!=null&&nickName.length()>0) {
							if(isHelpAccount){
								tvTitle.setText(nickName);
							}else{
								mActionBarTitleName.setText(nickName);
							}
						}
					}
				});
			}
		}

		public void deleteAllDMessageFinished(Account acc,String dchatUid){
			if(acc.getUuid().equals(account.getUuid())&&dchatUid.equals(dChatUid)){
				controller.listDMessages(account, dChatUid, showedCount + loadLimit,null);
				dChat.setLastMessage("");
				dChat.setLastMessageType(Type.TEXT);
				controller.updateDchat(account, dChat,false);
			}
		}

		public void get35EisContactsInfoForDChatSuccess(Account acc,ContactAttribute contact){
			if(acc.getUuid().equals(account.getUuid())){
				ContactInfoActivity.actionView(ChattingSingleActivity.this, account, contact);
			}
		}

		public void get35EisContactsInfoForDChatFailed(Account acc,ContactAttribute contact){
			if(acc.getUuid().equals(account.getUuid())){
				ContactInfoActivity.actionView(ChattingSingleActivity.this, account, contact);
			}
		}
	};
	
	private void setAttachmentDownloadState(String id,boolean isPause,boolean isSetProgress,int progress,boolean isUpdateDB){
		 List<DChatMessage>  dMessages =dChatAdapter.getmDChatMessages();
		 for(int i=0;i<dMessages.size();i++){
			 DChatMessage dMessage = dMessages.get(i);
			 if(dMessage.getMessageType()==Type.ATTACHMENT){
				 DAttachment dAttachment =dMessage.getAttachments().get(0);
				 if(dAttachment.getAttchmentId().equals(id)){
					 dAttachment.setDownloadPause(isPause);
					 if(isSetProgress){
						 dAttachment.setDownloadProgress(progress);
					 }
					 if(isUpdateDB){
						 controller.updateDChatDownFileState(account, dAttachment); 
					 }
					 dChatAdapter.updateView(i);
					 break;
				 }
			 }
		 }
	}
	private void setAttachmentUploadState(String id,int progress){
		 List<DChatMessage>  dMessages =dChatAdapter.getmDChatMessages();
		 for(int i=0;i<dMessages.size();i++){
			 DChatMessage dMessage = dMessages.get(i);
			 if(dMessage.getMessageType()==Type.ATTACHMENT){
				 DAttachment dAttachment =dMessage.getAttachments().get(0);
				 if(dAttachment.getAttchmentId().equals(id)){
					 dAttachment.setUploadProgress(progress);
					 dChatAdapter.updateView(i);
					 break;
				 }
			 }
		 }
	}
	
	/**
	 * 加载更多
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2014-3-19
	 */
	private void loadingMore() {
		isLoadMore = true;
		showedCount = dChatAdapter.getCount();
		loading_layout.setVisibility(View.VISIBLE);
		mSingleChattingView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
		controller.listDMessages(account, dChatUid, showedCount + loadLimit,null);

		new Thread(new Runnable() {

			public void run() {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						loading_layout.setVisibility(View.GONE);
					}
				});

			}
		}).start();

	}
	
	/**
	 * 对比本地最后一条消息时间
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-1-19
	 */
	public void contrastLastTime(DChatMessage message){	
		List<DChatMessage> dChatMessages =dChatAdapter.getmDChatMessages();
		if(dChatMessages.size()!=0){
			if(dChatMessages.get(dChatMessages.size()-1).getTime()>message.getTime()){
				message.setTime(dChatMessages.get(dChatMessages.size()-1).getTime()+1);
			}	
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_right_btns, menu);
		return true;
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		configureMenu(menu);
		return true;
	}

	private void configureMenu(Menu menu) {
		if (menu == null) {
			return;
		}
		MenuItem dChatSettingMenu = menu.findItem(R.id.action_DChat_setting);
		dChatSettingMenu.setVisible(!isHelpAccount);
		if(isOAdChat){
			dChatSettingMenu.setIcon(R.drawable.icon_tab_send_message_choice);
		}
			menu.findItem(R.id.action_QChat_setting).setVisible(false);
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if (getIntent().getIntExtra(TAG_NOTIFY_SINGLE_CHATTING_FLAG, 0) == 1&&!isHelpAccount) {
			MailChat.isChat=true;
			changeAccount();
			ActivityManager.popAll();
			jumpToMain();
		}
		updateDraft();
		dChatAdapter.stopPlay();
		finish();
	}
//	protected boolean isActivityRunning(Class activityClass) {
//		ActivityManager activityManager = (ActivityManager) getBaseContext()
//				.getSystemService(Context.ACTIVITY_SERVICE);
//		List<ActivityManager.RunningTaskInfo> tasks = activityManager
//				.getRunningTasks(Integer.MAX_VALUE);
//
//		for (ActivityManager.RunningTaskInfo task : tasks) {
//			if (activityClass.getCanonicalName().equalsIgnoreCase(
//					task.baseActivity.getClassName()))
//				return true;
//		}
//
//		return false;
//	}
	private void changeAccount() {
		if (account.getIsHaveUnreadMsg()) {
			account.setmIsHaveUnreadMsg(false);
		}
//		account.setName(account.getEmail().substring(0, account.getEmail().indexOf("@")));
		account.save(Preferences.getPreferences(this));
		Preferences.getPreferences(this).setDefaultAccount(account);

	}
	private void jumpToMain() {
		LocalSearch search = new LocalSearch();
		search.addAllowedFolder(Account.INBOX);
		search.addAccountUuid(mAccountUuid);
		Main4TabActivity.actionDisplaySearch(this, search, false, true);
	}

	private void updateDraft(){
		String txtContent = chattingTitleTxt.getText().toString().trim();
		if (txtContent.length() != 0) {
			controller.updatedChatDraft(account, dChatUid, chattingTitleTxt.getText().toString().trim(), true);	
		}else{
			controller.updatedChatDraft(account, dChatUid, "", false);	
		}
	}
	private BroadcastReceiver netWorkReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			if(NetUtil.isActive()){
				noNetLinearLayoutView.setVisibility(View.GONE);
			}else{
				noNetLinearLayoutView.setVisibility(View.VISIBLE);
			}
		}
	};
	
	private  void  registerNetWorkReceiver(){
		 IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");   
		 registerReceiver(netWorkReceiver, filter);   
	}
	private  void  unRegisterNetWorkReceiver(){
		 unregisterReceiver(netWorkReceiver);  
	}

	private void forwardMessage(){
		isHaveForwardMessage = false;
		DChatMessage newMessage = new DChatMessage(message.getMessageType());
		newMessage.setUuid(UUID.randomUUID().toString());
		newMessage.setTime(System.currentTimeMillis());
		newMessage.setMessageContent(message.getMessageContent());
		newMessage.setSenderEmail(account.getEmail());
		newMessage.setReceiverEmail(email);
		newMessage.setDchatUid(dChatUid);
		newMessage.setMessageState(2);
		contrastLastTime(message);
		List<DAttachment> dAttachments =message.getAttachments();
		if (dAttachments!=null&&dAttachments.size()>0) {
			DAttachment att = dAttachments.get(0);
			att.setMessageUid(newMessage.getUuid());
			att.setAttchmentId(UUID.randomUUID().toString());
			if(!StringUtil.isEmpty(att.getFilePath())){
				att.setLocalPathFlag(1);
			}
			att.setForwardFlag(1);
			newMessage.setAttachments(dAttachments);
		}
		dChatAdapter.addOrUpdateMessage(newMessage);
		mSingleChattingView.setSelection(dChatAdapter.getCount());
		controller.publishDMessage(toTopic, newMessage, account, null);
	}
	/**
	 * 邮件透传，单聊保存mailInfo消息，显示使用
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2016-3-9
	 */
	private void mailInfoToDChatMessage(){
		isMailInfoToDChatMessage = false;
		DChatMessage newMessage = new DChatMessage(message.getMessageType());
		newMessage.setUuid(UUID.randomUUID().toString());
		newMessage.setTime(System.currentTimeMillis());
		newMessage.setMessageContent(message.getMessageContent());
		newMessage.setSenderEmail(account.getEmail());
		newMessage.setReceiverEmail(email);
		newMessage.setDchatUid(dChatUid);
		newMessage.setMailFrom(email);
		newMessage.setMailSubject(message.getMailSubject());
		newMessage.setMailPreview(message.getMailPreview());
		newMessage.setMessageState(2);
		contrastLastTime(message);
		dChatAdapter.addOrUpdateMessage(newMessage);
		mSingleChattingView.setSelection(dChatAdapter.getCount());
		controller.saveAndShowDMessage(newMessage, account, null);
	}
	/**
	 * 邮件透传，判断是否发送该透传消息
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2016-3-9
	 */
	private void sendMailInfoToDChatMessage(){
		if(isMailInfoAction){
			List<DChatMessage> dMessages = dChatAdapter.getmDChatMessages();
			DChatMessage dMessage =dMessages.get(dMessages.size()-2);
			switch (dMessage.getMessageType()) {
			case FROM_MAIL_INFO:
				controller.pulishOnlySaveDMessage(toTopic, dMessage, account);
				break;
			default:
				return;
			}
			
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	    new Handler().postDelayed(new Runnable() {
	           @Override
	           public void run() {
					mSingleChattingView.setSelection(dChatAdapter.getCount());
	           }
	       }, 100);

	}
}
