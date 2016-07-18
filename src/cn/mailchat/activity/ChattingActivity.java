package cn.mailchat.activity;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
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
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.adapter.ChattingAdapter;
import cn.mailchat.beans.PickedFileInfo;
import cn.mailchat.chatting.beans.CAttachment;
import cn.mailchat.chatting.beans.CGroup;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.chatting.beans.CMessage;
import cn.mailchat.chatting.beans.CMessage.State;
import cn.mailchat.chatting.beans.CMessage.Type;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.controller.NotificationCenter;
import cn.mailchat.search.LocalSearch;
import cn.mailchat.utils.ActivityManager;
import cn.mailchat.utils.CommonUtils;
import cn.mailchat.utils.DateUtil;
import cn.mailchat.utils.EncryptUtil;
import cn.mailchat.utils.FileUtil;
import cn.mailchat.utils.ImageUtils;
import cn.mailchat.utils.NetUtil;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.view.FaceRelativeLayout;
import cn.mailchat.view.MessageTitleView;

public class ChattingActivity extends BaseActionbarFragmentActivity implements OnClickListener{
	public static final String TAG_NOTIFY_CHATTING_FLAG = "notify_into_Chatting";

	public static final String CGOUP = "group";
	public static final String CMESSAGE = "cmessage";
	public static final String BACKLAST = "backLast";
	public static final String ACCOUNTUUID = "accountUuid";
	public static final String MAIL_INFO = "mail_info";

	private static final int CLOSE_LAYOUT_VIEW_TOOSHORT = 8;// 关闭录音时间太短的提示
	private static final int SHOW_MIC = 9; // 显示录音提示
	private static final int CLOSE_MIC = 10; // 关闭录音提示
	private static final int VOICE1 = 11;
	private static final int VOICE2 = 12;
	private static final int VOICE3 = 13;
	private static final int VOICE4 = 14;
	private static final int VOICE5 = 15;
	private static final int VOICE6 = 16;

	private static final int ACTIVITY_REQUEST_PICK_ATTACHMENT = 1;// 文件的requestcode
	private static final int CAMERA_WITH_DATA = 2; // 照相的requestCode;
	private static final int PHOTO_PICKED_WITH_DATA = 3; // 相册的requestCode;
	private static final int LOCATION_WITH_DATA = 4; // 定位的requestCode;
	public static final int FILE_DOWNLOAD = 5;

	private LinearLayout voiceRecord;
	// 能动的声音提示
	private ImageView canlVoice;
	// 话筒
	private View ivMic;
	// 取消录音
	private LinearLayout cancleLayout;
	// back
	private LinearLayout layoutBack;
	// 录的时间太短
	private LinearLayout tooshortLayout;
	// 录音机
	private MediaRecorder recorder;
	// 录音时波纹图片刷新
	// private ComposeHandler mHandler;
	// 录音文件
	private File recordFile;
	// // 选择附件alert
	// private MMAlert upLoadAlert;
	// 群聊listview
	private ListView mGroupChattingView;
	// 其他工具
	private ImageButton showAttachmentPanel;
	// 其他工具layout
	private FrameLayout attachmentPanel;
	private LinearLayout chatting_bottom_util;
	// 表情
	private ImageButton panelEmojiLayout;
	// 拍照
	private RelativeLayout panelphotographLayout;
	// 相册
	private RelativeLayout panelalbumLayout;
	// 文件
	private RelativeLayout panelfileLayout;
	// 位置
	private RelativeLayout panellocationLayout;
	// 照片路径
	private File mPhotoFile;
	// 文字内容layout
	private RelativeLayout chattingContentLayout;
	// 语音文字转换
	private ImageButton changeChattingState;
	// 发送文字
	private Button sendTxt;
	// 设置
	// private ImageView chattingSet;
	// 顶部 title
	private EditText chattingTitleTxt;
	private TextView title;

	private RelativeLayout pushMessageLayout;
	private TextView pushhMessageText;
	private TextView pushhMessageCount;

	private FaceRelativeLayout faceLayout;

	private SensorManager sensorManager;
	private AudioManager audioManager;
	private Sensor mProximiny;
	private MediaPlayer mediaPlayer;

	private CGroupMember member_me;

	private CGroup mGroup;
	
	private static final int INT_ONFLING_LEN_LEFT_RIGHT = 30;// 手指向右滑动距离
	private static final int INT_ONFLIING_START_LEFT = 20;// 判断左侧边缘起始的临界值

	private static final String ACTION_FORWARD = "cn.mailchat.intent.action.FORWARD";
	private static final String ACTION_POSITION = "cn.mailchat.intent.action.POSITION";
	private static final String ACTION_MAIL_INFO = "cn.mailchat.intent.action.MAIL_INFO";

	private boolean isHaveForwardMessage = false;

	private int mode = 1;// 输入框状态 1 文字,2 语音

	private RelativeLayout loading_layout;

	private int showedCount = 0;
	// 每次加载条数
	private int loadLimit = 10;
	//是否聊天列表点击昵称查看聊天记录
	private boolean isOnlyRead = false;

	private boolean hasMore = true;
	private boolean isLoadMore = false;

	private boolean isBackLast = false;

	// 图片缓存
	private LruCache<String, Bitmap> mMemoryCache;
	private List<CMessage> pushMesssage;
	private ChattingAdapter chattingAdapter;
	private MessagingController controller;
	private Account  mAccount ;
	private ComposeHandler mHandler =new ComposeHandler(this);
	private View mCustomActionbarView;

	private View mActionBarMessageView;
	private MessageTitleView mActionBarSubject, mActionBarSubheading;
	private NotificationCenter notificationCenter;
	private CMessage mMessage;
	
	private LinearLayout noNetLinearLayoutView;
	private TextView setNetTextView;

	private String mAccountUuid;
	private boolean isFirstJoin;

	private boolean isMailInfoToGroupChatMessage;
	private String [] mailInfoArray;

	public static void actionCreateGroupSuccess(Context mContext, CGroup cGroup,Account mAccount) {
		Intent tIntent = new Intent(mContext, ChattingActivity.class);
		tIntent.putExtra(ACCOUNTUUID, mAccount.getUuid());
		tIntent.putExtra(CGOUP, cGroup);
		mContext.startActivity(tIntent);
	}

	/**
	 * 读信界面透传创建群成功
	 * 
	 * @Description:
	 * @param mContext
	 * @param cGroup
	 * @param mAccount
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-16
	 */
	public static void actionGroupChatFromMailInfo(Context mContext, CGroup cGroup,Account mAccount,String [] mailInfoArray) {
		Intent tIntent = new Intent(mContext, ChattingActivity.class);
		tIntent.putExtra(ACCOUNTUUID, mAccount.getUuid());
		tIntent.putExtra(CGOUP, cGroup);
		tIntent.putExtra(MAIL_INFO, mailInfoArray);
		tIntent.setAction(ACTION_MAIL_INFO);
		mContext.startActivity(tIntent);
	}

	public static void actionForwardMessage(Context context,CMessage cMessage, CGroup cGroup, Account mAccount) {
		Intent tIntent = new Intent(context, ChattingActivity.class);
		tIntent.putExtra(ACCOUNTUUID, mAccount.getUuid());
		tIntent.putExtra(CGOUP, cGroup);
		tIntent.putExtra(CMESSAGE, cMessage);
		tIntent.setAction(ACTION_FORWARD);
		context.startActivity(tIntent);
	}

	/**
	 * 
	 * method name: actionPositionMessage 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param mContext
	 *      @param message
	 *      @param cGroup
	 *      @param mAccount    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-11-5 下午3:28:41	@Modified by：zhangjx
	 *     @Description：跳转查看聊天记录
	 */
	public static void actionPositionMessage(Context mContext, CMessage message, CGroup cGroup,Account mAccount) {
		Intent tIntent = new Intent(mContext, ChattingActivity.class);
		tIntent.putExtra(ACCOUNTUUID, mAccount.getUuid());
		tIntent.putExtra(CMESSAGE, message);
		tIntent.putExtra(CGOUP, cGroup);
		tIntent.setAction(ACTION_POSITION);
		mContext.startActivity(tIntent);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityManager.push(this);
		setContentView(R.layout.activity_chatting);
		initializeActionBar();
		initWidget();
		initEvent();
		initData();
		initActionbarView();
		registerNetWorkReceiver();
		notificationCenter = NotificationCenter.getInstance();
		MobclickAgent.onEvent(getApplicationContext(),"into_group_chat_act");
	}
	
	@Override
	protected void onResume() {
		super.onResume();	
		notificationCenter.notifyClean(mAccount);
		MobclickAgent.onPageStart("ChattingActivity"); //统计页面
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
		MobclickAgent.onPageEnd("ChattingActivity"); //统计页面
		MobclickAgent.onPause(this); 
	}
	private void initWidget() {
		title = (TextView) findViewById(R.id.txtView_title);
		// chattingSet = (ImageView) findViewById(R.id.imgView_operation);
		chattingTitleTxt = (EditText) findViewById(R.id.edit_view_chatting_content_et);
		changeChattingState = (ImageButton) findViewById(R.id.ibtn_view_chatting_mode_btn);
		chattingContentLayout = (RelativeLayout) findViewById(R.id.layout_view_text_panel_ll);
		panelEmojiLayout = (ImageButton) findViewById(R.id.ibtn_view_chatting_mm);
		panelphotographLayout = (RelativeLayout) findViewById(R.id.layoutView_chatting_panel_bottom_photograph);
		panelalbumLayout = (RelativeLayout) findViewById(R.id.layoutView_chatting_panel_bottom_album);
		panelfileLayout = (RelativeLayout) findViewById(R.id.layoutView_chatting_panel_bottom_file);
		faceLayout = (FaceRelativeLayout) findViewById(R.id.facechoose);
		faceLayout.setEt_sendmessage(chattingTitleTxt);

		panellocationLayout = (RelativeLayout) findViewById(R.id.layoutView_chatting_bottom_panel_location);
		attachmentPanel = (FrameLayout) findViewById(R.id.frameView_chatting_bottom_panel);
		showAttachmentPanel = (ImageButton) findViewById(R.id.ibtn_view_chatting_attach_btn);
		// mHandler = new ComposeHandler(ChattingActivity.this);
		tooshortLayout = (LinearLayout) findViewById(R.id.layout_view_tooshort);
		cancleLayout = (LinearLayout) findViewById(R.id.layout_view_cancle_mic);
		canlVoice = (ImageView) findViewById(R.id.img_view_canl_voice);
		ivMic = (View) findViewById(R.id.layout_view_mic);
		mGroupChattingView = (ListView) findViewById(R.id.lv_view_chatting_content);
		layoutBack = (LinearLayout) findViewById(R.id.layout_back);
		voiceRecord = (LinearLayout) findViewById(R.id.btn_view_voice_record);
		sendTxt = (Button) findViewById(R.id.btn_view_chatting_send);

		chatting_bottom_util = (LinearLayout) findViewById(R.id.chatting_bottom_util);


		loading_layout = (RelativeLayout) findViewById(R.id.loading_layout);
		pushMessageLayout = (RelativeLayout) findViewById(R.id.layout_push_message);
		pushhMessageText = (TextView) findViewById(R.id.push_message_txt);
		pushhMessageCount = (TextView) findViewById(R.id.push_message_count);
		pushMessageLayout.setVisibility(View.GONE);
		// chattingSet.setVisibility(View.VISIBLE);
		noNetLinearLayoutView =	(LinearLayout) findViewById(R.id.item_net);
		noNetLinearLayoutView.setVisibility(View.GONE);
		setNetTextView = (TextView) findViewById(R.id.net_set);
		setNetTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
//				startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
			    NetUtil.showSystemSettingView(ChattingActivity.this);
			}
		});
	}
	
	private void initData() {
		// TODO Auto-generated method stub
		pushMesssage =new ArrayList<CMessage>();
		controller = MessagingController.getInstance(getApplication());
		controller.addListener(listener);
		mGroup = (CGroup) getIntent().getSerializableExtra(CGOUP);
		if(mGroup.isDraft()){
			chattingTitleTxt.setText(mGroup.getDraftContent());
			chattingTitleTxt.setSelection(mGroup.getDraftContent().length());
		}else{
			chattingTitleTxt.setText("");
		}
		
		String action = getIntent().getAction();
		if (ACTION_POSITION.equals(action)) {
			mMessage = (CMessage) getIntent().getSerializableExtra(CMESSAGE);

			// 去掉右上角设置和底部操作栏，且发件人昵称不可点击
//			chattingSet.setVisibility(View.GONE);
			findViewById(R.id.operation_footer).setVisibility(View.GONE);
			isOnlyRead = true;
			return;
		}else if (ACTION_FORWARD.equals(action)) {
			mMessage = (CMessage) getIntent().getSerializableExtra(CMESSAGE);
			isHaveForwardMessage = true;
		}else if(ACTION_MAIL_INFO.equals(action)){
			mailInfoArray = getIntent().getStringArrayExtra(MAIL_INFO);
			isMailInfoToGroupChatMessage =true;
		}
		mAccountUuid=getIntent().getStringExtra(ACCOUNTUUID);
		mAccount = Preferences.getPreferences(this).getAccount(mAccountUuid);
		//将自己生产为群成员
		member_me =new CGroupMember(mAccount.getEmail(), mAccount.getEmail());
		member_me.setUid(EncryptUtil.getMd5(mAccount.getEmail()));
		member_me.setAdmin(mGroup.getIsAdmin());
		member_me.setNickName(mAccount.getName());
		String avatar = mAccount.getAccountBigHeadImg();
		String avatarHash=null;
		if(StringUtil.isEmpty(avatar)){
			avatarHash=controller.getCMemberAvatarHash(mAccount, mAccount.getEmail());
		}else{
			avatarHash =avatar.replace(GlobalConstants.HOST_IMG, "");
		}
		member_me.setAvatarHash(avatarHash);
		mediaPlayer = new MediaPlayer();
		chattingAdapter = new ChattingAdapter(this, mGroupChattingView, mAccount, mGroup, null, mediaPlayer);
		mGroupChattingView.setAdapter(chattingAdapter);
		mode=mGroup.getInputType();
		changeMode();
		if (getIntent().getIntExtra(TAG_NOTIFY_CHATTING_FLAG, 0) == 1
				&& !Preferences.getPreferences(this).getDefaultAccount()
						.getEmail().equals(mAccount.getEmail())) {
			showCoustomToast(mAccount.getName(), mAccount.getEmail());
		}
		if (isOnlyRead) {
			controller.listCMessage(mAccount, mGroup.getUid(), mMessage.getUid(), 10000);
		} else {
			controller.listCMessage(mAccount, mGroup.getUid(), "0", 30, null,false);
			isFirstJoin=true;
		}
		controller.updateCGroupUntreatedCount(mAccount, mGroup,0,null);
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
	private void initializeActionBar() {
		// add by zhangjx start hide icon
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(true);
		// add by zhangjx end
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setCustomView(R.layout.actionbar_custom);
		// mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayUseLogoEnabled(true);
		mCustomActionbarView = mActionBar.getCustomView();
		// 返回按钮
		mActionBar.setDisplayHomeAsUpEnabled(true);
	}

	private void initActionbarView() {
		mActionBarMessageView = mCustomActionbarView
				.findViewById(R.id.actionbar_message_view);
		mActionBarSubject = (MessageTitleView) mCustomActionbarView
				.findViewById(R.id.message_title_view);
		mActionBarSubheading = (MessageTitleView) mCustomActionbarView
				.findViewById(R.id.message_title_view_sub);
		mActionBarMessageView.setVisibility(View.VISIBLE);
		mActionBarSubheading.setVisibility(View.GONE);
		mActionBarSubject.setText(mGroup.getGroupName());
	}
	private void initEvent() {
		// TODO Auto-generated method stub
		changeChattingState.setOnClickListener(this);
		showAttachmentPanel.setOnClickListener(this);
		panelEmojiLayout.setOnClickListener(this);
		// 拍照
		panelphotographLayout.setOnClickListener(this);
		// 相册
		panelalbumLayout.setOnClickListener(this);
		// 文件
		panelfileLayout.setOnClickListener(this);
		// 位置
		panellocationLayout.setOnClickListener(this);
		pushMessageLayout.setOnClickListener(this);
		sendTxt.setOnClickListener(this);
		voiceRecord.setOnClickListener(this);
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
//				chattingTitleTxt
//						.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
//								| InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
//								| InputType.TYPE_TEXT_FLAG_MULTI_LINE
//								| InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(chattingTitleTxt, 0);
			}
		});

		mGroupChattingView.setOnScrollListener(new OnScrollListener() {

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
					}  else if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
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
					MobclickAgent.onEvent(ChattingActivity.this,
							"Use_Voice_Record");
				}
			};

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					chattingAdapter.stopPlay();
					voiceRecord.setBackgroundResource(R.drawable.bg_btn_voice_rcd_p);
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
						String path = MailChat.getInstance().getChatVoiceDirectory(mAccount)+ 
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
											CMessage message = new CMessage(Type.VOICE);
											message.setUid(UUID.randomUUID().toString());
											message.setMember(member_me);
											message.setSendTime(getSendTime());
											message.setMessageState(State.sending);
											message.setGroupUid(mGroup.getUid());
											CAttachment attachment = new CAttachment();
											attachment.setMessageUid(message.getUid());
											attachment.setAttchmentId(UUID.randomUUID().toString());
											attachment.setVoiceLength((int) ((endTime - startTime) / 1000));
											String newPath = MailChat.getInstance().getChatVoiceDirectory(mAccount)+ EncryptUtil.getMd5(attachment.getAttchmentId()) + ".amr";
											recordFile.renameTo(new File(newPath));
											attachment.setFilePath(newPath);
											attachment.setDownloadState(CAttachment.DOWNLOAD_STATE_DOWNLOADED);
											message.setAttachment(attachment);
											contrastLastTime(message);
											controller.publishCMessage(mGroup.getUid(), message, mAccount, listener);
											chattingAdapter.addOrUpdateMessage(message);
											mGroupChattingView.setSelection(chattingAdapter.getCount());
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
				if(event.getAction()==MotionEvent.ACTION_UP){
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
								CMessage message = new CMessage(Type.VOICE);
								message.setUid(UUID.randomUUID().toString());
								message.setMember(member_me);
								message.setSendTime(getSendTime());
								message.setMessageState(State.sending);
								message.setGroupUid(mGroup.getUid());
								CAttachment attachment = new CAttachment();
								attachment.setMessageUid(message.getUid());
								attachment.setAttchmentId(UUID.randomUUID().toString());
								attachment.setVoiceLength((int) ((endTime - startTime) / 1000));
								String newPath = MailChat.getInstance().getChatVoiceDirectory(mAccount) + EncryptUtil.getMd5(attachment.getAttchmentId())+ ".amr";
								recordFile.renameTo(new File(newPath));
								attachment.setFilePath(newPath);		
								message.setAttachment(attachment);
								contrastLastTime(message);
								controller.publishCMessage(mGroup.getUid(), message, mAccount, listener);
								chattingAdapter.addOrUpdateMessage(message);
								mGroupChattingView.setSelection(chattingAdapter.getCount());
							}
						}
					} catch (IllegalStateException e) {
						e.printStackTrace();
					}
				}
				if(event.getAction()==MotionEvent.ACTION_MOVE){
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
	
	class ComposeHandler extends Handler {

		WeakReference<ChattingActivity> mReference;

		public ComposeHandler(ChattingActivity activity) {
			mReference = new WeakReference<ChattingActivity>(activity);
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
		showedCount = chattingAdapter.getCount();
		loading_layout.setVisibility(View.VISIBLE);
		mGroupChattingView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
		controller.listCMessage(mAccount, mGroup.getUid(), "0", showedCount + loadLimit, null,false);

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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.ibtn_view_chatting_mode_btn:
			// 输入模式切换
			if (mode == 1) {
				mode = 2;
			} else {
				mode = 1;
			}
			changeMode();
			controller.updateCGroupInputMode(mAccount, mGroup.getUid(), mode);
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
//			chattingTitleTxt.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
//					| InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
//					| InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
			closeInputMethod();
			break;
		case R.id.ibtn_view_chatting_mm:
			// 表情按钮点击
			if (faceLayout.getVisibility() == View.VISIBLE) {
				faceLayout.setVisibility(View.GONE);
				chatting_bottom_util.setVisibility(View.VISIBLE);
				attachmentPanel.setVisibility(View.GONE);
				panelEmojiLayout.setImageResource(R.drawable.icon_emoji);
				// 弹出软键盘
				chattingTitleTxt.requestFocus();
//				chattingTitleTxt.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
//						| InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
//						| InputType.TYPE_TEXT_FLAG_MULTI_LINE
//						| InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
				InputMethodManager imm = (InputMethodManager) chattingTitleTxt.getContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(chattingTitleTxt, 0);
			} else {
				faceLayout.setVisibility(View.VISIBLE);
				attachmentPanel.setVisibility(View.VISIBLE);
				chatting_bottom_util.setVisibility(View.GONE);

				panelEmojiLayout.setImageResource(R.drawable.icon_emoji_p);
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
		case R.id.btn_view_chatting_send:
			String txtContent = chattingTitleTxt.getText().toString().trim();
			if (txtContent.length() == 0) {
				Toast.makeText(ChattingActivity.this,
						getString(R.string.chatting_no_content), 0)
						.show();
				return;
			}
			sendTextMsg(chattingTitleTxt.getText().toString().trim());	
			break;
		case R.id.layout_push_message:
			pushMesssage.clear();
			pushMessageLayout.setVisibility(View.GONE);
			mGroupChattingView.setSelection(chattingAdapter.getCount());
			mGroupChattingView.post(new Runnable() {
				 @Override
				 public void run() {
					 mGroupChattingView.smoothScrollToPosition(chattingAdapter.getCount()-1);
				 }
			 });
			mGroupChattingView.setSelection(chattingAdapter.getCount() - 1);
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case CAMERA_WITH_DATA:
			// 拍照获得附件
			if (resultCode == Activity.RESULT_OK) {
				if (mPhotoFile != null) {
					String gotPhotoPath = mPhotoFile.getPath();
					String picPath = gotPhotoPath;
					CMessage message = new CMessage(Type.IMAGE);
					message.setUid(UUID.randomUUID().toString());
					message.setSendTime(getSendTime());
					message.setMessageState(State.sending);
					message.setMember(member_me);
					message.setGroupUid(mGroup.getUid());
					CAttachment attachment = new CAttachment();
					attachment.setAttchmentId(UUID.randomUUID().toString());
					attachment.setMessageUid(message.getUid());
					attachment.setFilePath(picPath);
					attachment.setLocalPathFlag(1);
					message.setAttachment(attachment);	
					contrastLastTime(message);

					//TODO:这里需要将特殊手机（如浩哥的手机）图片角度旋转正确，再设置宽高，建议再写一个子线程处理图片旋转设置宽高，设置好了chattingAdapter.addOrUpdateMessage(message)去更新界面，再调用发送

					//由于先要在界面上显示，所以不能等压缩后，再设置上传压缩图片宽高，直接设置本地原图宽高（基本不会有什么影响）。
					int[] size = ImageUtils.getNativeImageSize(attachment.getFilePath());
					if(size!=null&&size.length==2){
						attachment.setImageWidth(size[0]);
						attachment.setImageHeight(size[1]);
					}
					chattingAdapter.addOrUpdateMessage(message);
					mGroupChattingView.setSelection(chattingAdapter.getCount());
					controller.publishCMessage(mGroup.getUid(), message, mAccount, null);
				}
			}
			break;
		case PHOTO_PICKED_WITH_DATA:

			// 选择相册获得附件
			if (resultCode == Activity.RESULT_OK) {
//				String picPath = FileUtil.getMediaFilePathByUri(ChattingActivity.this, data.getData());
				//Rom 4.4获取图片无法获取到路径
				String picPath= FileUtil.getPath(ChattingActivity.this, data.getData());
//				String gotPhotoPath = mPhotoFile.getPath();
//				String picPath = gotPhotoPath;
				CMessage message = new CMessage(Type.IMAGE);
				message.setUid(UUID.randomUUID().toString());
				message.setSendTime(getSendTime());
				message.setMessageState(State.sending);
				message.setMember(member_me);
				message.setGroupUid(mGroup.getUid());
				CAttachment attachment = new CAttachment();
				attachment.setAttchmentId(UUID.randomUUID().toString());
				attachment.setMessageUid(message.getUid());
				attachment.setFilePath(picPath);
				attachment.setLocalPathFlag(1);
				message.setAttachment(attachment);
				contrastLastTime(message);
				//由于先要在界面上显示，所以不能等压缩后，再设置上传压缩图片宽高，直接设置本地原图宽高（基本不会有什么影响）。
				int[] size = ImageUtils.getNativeImageSize(attachment.getFilePath());
				if(size!=null&&size.length==2){
					attachment.setImageWidth(size[0]);
					attachment.setImageHeight(size[1]);
				}
				chattingAdapter.addOrUpdateMessage(message);
				mGroupChattingView.setSelection(chattingAdapter.getCount());
				controller.publishCMessage(mGroup.getUid(), message, mAccount, null);
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
				ArrayList<CAttachment> attachments = new ArrayList<CAttachment>();
				CMessage message = new CMessage(Type.ATTACHMENT);
				message.setUid(UUID.randomUUID().toString());
				message.setSendTime(getSendTime());
				message.setMessageState(State.sending);
				message.setMember(member_me);
				message.setGroupUid(mGroup.getUid());
				PickedFileInfo file = Entry.getValue();
				CAttachment attachment = new CAttachment();
				attachment.setAttchmentId(UUID.randomUUID().toString());
				attachment.setMessageUid(message.getUid());
				attachment.setFilePath(file.mContentUri.replace("file://", ""));
				attachment.setName(file.mName);
				attachment.setContentType(file.mContentType);
				attachment.setSize(file.mSize);
				attachment.setLocalPathFlag(1);
				attachments.add(attachment);
				message.setAttachment(attachments.get(0));
				chattingAdapter.addOrUpdateMessage(message);
				mGroupChattingView.setSelection(chattingAdapter.getCount());
				controller.publishCMessage(mGroup.getUid(), message, mAccount, null);
			}
			break;
//		case LOCATION_WITH_DATA:
//			if (resultCode == Activity.RESULT_OK) {
//				message = new CMessage(Type.LOCATION);
//				message.setUid(UUID.randomUUID().toString());
//				message.setMember(member_me);
//				message.setSendTime(getSendTime());
//				message.setLocationName(data.getStringExtra("name"));
//				message.setAddress(data.getStringExtra("addr"));
//				message.setLongitude(data.getDoubleExtra("x", 0));
//				message.setLatitude(data.getDoubleExtra("y", 0));
//
//				controller.sendGroupMessage(mAccount, mGroup.getUid(), message, null);
//
//				chattingAdapter.addOrUpdateMessage(message);
//				// mGroupChattingView.setSelection(chattingAdapter.getCount());
//
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
	
	// 复制文件
	public void copyFile(final String sourceFile, final String targetFile, final boolean repleaseIfexists) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// copy文件
				try {
					// 复制的原因，附件名不是真名
					// 每次都复制的原因，附件重名问题
					FileUtil.copyFile(Uri.parse(sourceFile).getPath(), targetFile, repleaseIfexists);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}).start();
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
	 * @Date：2014-11-13 下午1:06:32	@Modified by：zhangyq
	 * @Description：
	 */
	public void panelButOnclick(int whichButton) {
		attachmentPanel.setVisibility(View.GONE);
		switch (whichButton) {
		case R.id.layoutView_chatting_panel_bottom_photograph:
			try {
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
//			Intent intentLocation = new Intent(this, SelectLocationActivity.class);
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
			changeChattingState.setImageResource(R.drawable.selector_btn_chatting_setmode_text);
			closeInputMethod();
		} else {
			// 文字输入
			voiceRecord.setVisibility(View.GONE);
			chattingContentLayout.setVisibility(View.VISIBLE);
			changeChattingState.setImageResource(R.drawable.selector_btn_chatting_setmode_voice);
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
	
	private void sendTextMsg(String txtContent) {
		CMessage cMessage =new CMessage(Type.TEXT);
		cMessage.setContent(txtContent);
		cMessage.setUid(UUID.randomUUID().toString());
		cMessage.setSendTime(System.currentTimeMillis());
		cMessage.setMember(member_me);
		cMessage.setGroupUid(mGroup.getUid());
		cMessage.setMessageState(State.sending);
		chattingTitleTxt.setText("");
		contrastLastTime(cMessage);
		chattingAdapter.addOrUpdateMessage(cMessage);
		mGroupChattingView.setSelection(chattingAdapter.getCount());
		controller.publishCMessage(mGroup.getUid(), cMessage, mAccount, null);
		MobclickAgent.onEvent(ChattingActivity.this, "send_g_message");
	}
	
	private MessagingListener listener =new MessagingListener(){
		
		public void listCMessagesFinished(Account acc,final List<CMessage> cMessages){
			if(acc.getUuid().equals(mAccount.getUuid()) && !isOnlyRead){
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub		
						
						if (cMessages != null) {
							chattingAdapter.updateMessages(cMessages);
							if(!isFirstJoin){
								if (isLoadMore) {
									mGroupChattingView.setSelection(chattingAdapter.getCount() - showedCount);
									isLoadMore = false;
									if (chattingAdapter.getCount() < showedCount + loadLimit) {
										hasMore = false;
									}
									setTranscriptModeScroll();
								} else {
									if (isOnlyRead) {
										mGroupChattingView.setSelection(0);
									} else {
										mGroupChattingView.setSelection(chattingAdapter.getCount());
									}
								}
							}else{
								if (isHaveForwardMessage) {
									forwardMessage();
								} else if(isMailInfoToGroupChatMessage){
									mailInfoToGroupChatMessage();
								}
								isFirstJoin=false;
								showedCount = chattingAdapter.getCount();
								mGroupChattingView.setSelection(showedCount);
								controller.getGroupWithMembers(mAccount, mGroup.getUid(), listener);
							}
						}
						
					}
				});		
			}
			
		}
		
		public void cMessageArrived(Account acc,final CMessage cMessage){
			 
			if(acc.getUuid().equals(mAccount.getUuid()) && !isOnlyRead&&mGroup.getUid().equals(cMessage.getGroupUid())){
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {	
						int lastVisiblePosition = mGroupChattingView.getLastVisiblePosition();
						if (lastVisiblePosition != chattingAdapter.getcMessages().size() - 1) {
							setTranscriptModeDisabled();
							chattingAdapter.addOrUpdateMessage(cMessage);
							pushMesssage.add(cMessage);
							pushMessageLayout.setVisibility(View.VISIBLE);
							String content = cMessage.getContent();
							switch (cMessage.getMessageType()) {
							case TEXT:
								content = cMessage.getMember().getNickName() + " : " +content;
								break;
							case IMAGE:
								content = cMessage.getMember().getNickName() + " : " +MailChat.getInstance().getString(R.string.msg_img);
								break;
							case ATTACHMENT:
								content = cMessage.getMember().getNickName() + " : " +MailChat.app.getString(R.string.msg_att);
								break;
							case VOICE:
								content = cMessage.getMember().getNickName() + " : " +MailChat.getInstance().getString(R.string.msg_voice);
								break;
							case LOCATION:
								content = cMessage.getMember().getNickName() + " : " +MailChat.app.getString(R.string.msg_location);
								break;
							case NOTIFICATION:
								break;
							default:
								break;
							}
							pushhMessageText.setText(content);
							int count = pushMesssage.size();
							if (count > 1) {
								pushhMessageCount.setText(count + "");
							} else {
								pushhMessageCount.setText("");
							}
							setTranscriptModeScroll();
						}else{
							chattingAdapter.addOrUpdateMessage(cMessage);
							mGroupChattingView.setSelection(chattingAdapter.getCount());
						}
						controller.updateCGroupUntreatedCount(mAccount, mGroup,0,null);
					}
				});
				
			}
		 
		}
		public void sendCMessagesSuccess(Account acc, final String cMessageUid) {
			if (acc.getUuid().equals(mAccount.getUuid()) && !isOnlyRead) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						chattingAdapter.updateMessageSendState(cMessageUid,true);
					}
				});

			}
		}

		public void sendCMessagesFail(Account acc, final String cMessageUid)  {
			if (acc.getUuid().equals(mAccount.getUuid()) && !isOnlyRead) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						chattingAdapter.updateMessageSendState(cMessageUid,false);
					}
				});
			}
		}

		public void leaveCGroupSuccess(Account acc) {
			if (acc.getUuid().equals(mAccount.getUuid())) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						finish();
					}
				});
			}
		}

		public void deleteCGroupSuccess(Account acc) {
			if (acc.getUuid().equals(mAccount.getUuid())) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						finish();
					}
				});
			}
		}
		public void getLocalGroupInfoSuccess(Account acc,CGroup cGroup,String membersName) {
			if (acc.getUuid().equals(mAccount.getUuid())) {
				ChattingGroupSettingActivity.actionGroupSetting(ChattingActivity.this, cGroup, mAccount,membersName);
			}	
		}
		public void getLocalGroupInfoFail(Account acc,CGroup cGroup) {
			if (acc.getUuid().equals(mAccount.getUuid())) {
				ChattingGroupSettingActivity.actionGroupSetting(ChattingActivity.this, cGroup, mAccount,null);
			}
		}
		
		public void againDownCGroupThumbnailImageSuccess(Account acc) {
			if (acc.getUuid().equals(mAccount.getUuid())) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						chattingAdapter.notifyDataSetChanged();
					}
				});
			}
		}

		public void againDownCGroupThumbnailImageFailed(Account acc) {
			if (acc.getUuid().equals(mAccount.getUuid())) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						chattingAdapter.notifyDataSetChanged();
						Toast.makeText(ChattingActivity.this,  R.string.again_down_thumbnail_image_fail,Toast.LENGTH_SHORT).show();
					}
				});
			}
		}
		
		public void changeGroupSuccess(Account acc,final CGroup group) {
			if (acc.getUuid().equals(mAccount.getUuid())&&group.getUid().equals(mGroup.getUid())) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mActionBarSubject.setText(group.getGroupName());
					}
				});
			}
		}
		
		public void getGroupInfoSuccess(Account acc,final CGroup cGroup,final List<CGroupMember> members) {
			if(mAccount.getUuid().equals(acc.getUuid())){
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mActionBarSubject.setText(cGroup.getGroupName());
					}
				});
			}
		}
		public void sendCMessageTimeSuccess(Account account, final String cMessageUid,final long time) {
			if (account.getUuid().equals(mAccount.getUuid())) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						chattingAdapter.updateMessageTime(cMessageUid, time);
					}
				});
			}
		}
		public void delteGroupInfoByMemberSuccess(Account account,CGroup cGroup) {
			if (account.getUuid().equals(mAccount.getUuid())&&mGroup.getUid().equals(cGroup.getUid())) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						finish();
					}
				});
			}
		}
		
		public void kickedOutGroupByMemberSuccess(Account account,CGroup cGroup) {
			if (account.getUuid().equals(mAccount.getUuid())&&mGroup.getUid().equals(cGroup.getUid())) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						finish();
					}
				});
			}
		}
		public void uploadStart(Account account,final String id){
			if(account.getUuid().equals(mAccount.getUuid())){
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
			if(account.getUuid().equals(mAccount.getUuid())){
				//更新上传条目进度
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						setAttachmentUploadState(id,progress);
					}
				});
			}
		}

		public void uploadInterrupt(Account account,final String id){
			if(account.getUuid().equals(mAccount.getUuid())){
				//更新上传条目进度
				chattingAdapter.deleteMessageForAtt(id);
			}
		}

		public void uploadFinished(Account account,final String id){
			if(account.getUuid().equals(mAccount.getUuid())){
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						setAttachmentUploadState(id,100);
					}
				});	
			}
		}
		
		public void fileDownloadStart(Account acc,final String id){
			if(acc.getUuid().equals(mAccount.getUuid())){
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
			if(acc.getUuid().equals(mAccount.getUuid())){
				//更新上传条目进度
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						setAttachmentDownloadState(id, false, true, progress,false);
					}
				});
			}
		}
		
		public void fileDownloadFinished(Account acc,final String id){
			if(acc.getUuid().equals(mAccount.getUuid())){
				//更新上传条目进度
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						setAttachmentDownloadState(id, false, true, 100,true);
					}
				});
			}
		}
		
		public void fileDownloadFailed(Account acc,final String id){
			if(acc.getUuid().equals(mAccount.getUuid())){
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
			if(acc.getUuid().equals(mAccount.getUuid())){
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

		public void getGroupInfoSuccess(Account acc, final CGroup cGroup) {
			if(acc.getUuid().equals(mAccount.getUuid())){
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mGroup=cGroup;
						controller.listCMessage(mAccount, mGroup.getUid(), "0", showedCount, null,true);
					}
				});
			}
		}

		public void listNikeNameAndAvatarCMessagesFinished(Account acc,final List<CMessage> cMessages){
			if(acc.getUuid().equals(mAccount.getUuid())){
				//更新上传条目进度
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						chattingAdapter.updateMessages(cMessages);
					}
				});
			}
		}

		public void deleteAllCMessageFinished(Account account,String cGroupUid){
			if(account.getUuid().equals(mAccount.getUuid())&&cGroupUid.equals(mGroup.getUid())){
				controller.listCMessage(mAccount, mGroup.getUid(), "0", showedCount + loadLimit, null,false);
				controller.updateCgroupLastCmessage(mAccount, mGroup.getUid(), null);
			}
		}

		public void get35EisContactsInfoForGroupSuccess(Account account,ContactAttribute contact){
			if(account.getUuid().equals(mAccount.getUuid())){
				ContactInfoActivity.actionView(ChattingActivity.this, mAccount, contact);
			}
		}

		public void get35EisContactsInfoForGroupFailed(Account account,ContactAttribute contact){
			if(account.getUuid().equals(mAccount.getUuid())){
				ContactInfoActivity.actionView(ChattingActivity.this, mAccount, contact);
			}
		}
	};
	
	private void setAttachmentDownloadState(String id,boolean isPause,boolean isSetProgress,int progress,boolean isUpdateDB){
		 List<CMessage>  cMessages =chattingAdapter.getcMessages();
		 for(int i=0;i<cMessages.size();i++){
			 CMessage cMessage = cMessages.get(i);
			 if(cMessage.getMessageType()==Type.ATTACHMENT){
				 CAttachment cAttachment =cMessage.getAttachment();
				 if(cAttachment.getAttchmentId().equals(id)){
					 cAttachment.setDownloadPause(isPause);
					 if(isSetProgress){
						 cAttachment.setDownloadProgress(progress);
					 }
					 if(isUpdateDB){
						 controller.updateCGroupDownFileState(mAccount, cAttachment); 
					 }
					 chattingAdapter.updateView(i);
					 break;
				 }
			 }
		 }
	}
	
	private void setAttachmentUploadState(String id,int progress){
		 List<CMessage>  cMessages =chattingAdapter.getcMessages();
		 for(int i=0;i<cMessages.size();i++){
			 CMessage cMessage = cMessages.get(i);
			 if(cMessage.getMessageType()==Type.ATTACHMENT){
				 CAttachment cAttachment =cMessage.getAttachment();
				 if(cAttachment.getAttchmentId().equals(id)){
					 cAttachment.setUploadProgress(progress);
					 chattingAdapter.updateView(i);
					 break;
				 }
			 }
		 }
	}
	public void setupUI(View view) {

	    if(!(view instanceof SearchView)) {

	        view.setOnTouchListener(new OnTouchListener() {

	            public boolean onTouch(View v, MotionEvent event) {
//	                searchMenuItem.collapseActionView();
	                return false;
	            }

	        });
	    }

	    //If a layout container, iterate over children and seed recursion.
	    if (view instanceof ViewGroup) {

	        for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

	            View innerView = ((ViewGroup) view).getChildAt(i);

	            setupUI(innerView);
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
			menu.findItem(R.id.action_DChat_setting).setVisible(false);
			menu.findItem(R.id.action_QChat_setting).setVisible(true);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			if (getIntent().getIntExtra(TAG_NOTIFY_CHATTING_FLAG, 0) == 1) {
				MailChat.isChat=true;
				changeAccount();
				ActivityManager.popAll();
				jumpToMain();
			}
			updateDraft();
			chattingAdapter.stopPlay();
			finish();
			CommonUtils.hideSoftInput(this);
			return true;
		case R.id.action_QChat_setting:
			controller.getLocalCGroupInfo(mAccount, mGroup, listener);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void setTranscriptModeScroll() {
		new Thread(new Runnable() {

			public void run() {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mGroupChattingView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
			}
		}).start();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		controller.removeListener(listener);
		unRegisterNetWorkReceiver();
		//ActivityManager.pop(this);
	}
	/** 
	 * method name: setTranscriptModeDisabled 
	 * function @Description: TODO
	 * Parameters and return values description：    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-11-5 下午1:43:09	@Modified by：zhangjx
	 *     @Description：
	 */
		public void setTranscriptModeDisabled() {
		mGroupChattingView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);
	}
	public boolean isOnlyRead() {
		return isOnlyRead;
	}
	public void setOnlyRead(boolean isOnlyRead) {
		this.isOnlyRead = isOnlyRead;
	}
	
	/**
	 * 获取一个发消息的时间
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: sunzhongquan
	 * @date:2014-5-14
	 */
	private long getSendTime() {
		long currentTime = System.currentTimeMillis();
		if (currentTime > chattingAdapter.lastMessageSendTime) {//系统当前时间大于最后一条消息的时间
			return currentTime;
		} else {
			return chattingAdapter.lastMessageSendTime + 1;
		}
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if (getIntent().getIntExtra(TAG_NOTIFY_CHATTING_FLAG, 0) == 1) {
			MailChat.isChat=true;
			changeAccount();
			ActivityManager.popAll();
			jumpToMain();
		}
		updateDraft();
		chattingAdapter.stopPlay();
		finish();
	}
//	protected  boolean isActivityRunning(Class activityClass) {
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
		if (mAccount.getIsHaveUnreadMsg()) {
			mAccount.setmIsHaveUnreadMsg(false);
		}
//		mAccount.setName(mAccount.getEmail().substring(0, mAccount.getEmail().indexOf("@")));
		mAccount.save(Preferences.getPreferences(this));
		Preferences.getPreferences(this).setDefaultAccount(mAccount);

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
			controller.updateCGroupDraft(mAccount, mGroup.getUid(), chattingTitleTxt.getText().toString().trim(), true);	
		}else{
			controller.updateCGroupDraft(mAccount, mGroup.getUid(), "", false);	
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
	
	/**
	 * 对比本地最后一条消息时间
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-1-19
	 */
	public void contrastLastTime(CMessage message){	
		List<CMessage> cMessages =chattingAdapter.getcMessages();
		if(cMessages.size()!=0){
			if(cMessages.get(cMessages.size()-1).getSendTime()>message.getSendTime()){
				message.setSendTime(cMessages.get(cMessages.size()-1).getSendTime()+1);
			}	
		}
	}
	
	private void forwardMessage() {
		isHaveForwardMessage = false;
		CMessage newMessage = new CMessage(mMessage.getMessageType());
		newMessage.setUid(UUID.randomUUID().toString());
		newMessage.setGroupUid(mGroup.getUid());
		newMessage.setMember(member_me);
		newMessage.setContent(mMessage.getContent());
		newMessage.setSendTime(getSendTime());
		newMessage.setMessageState(State.sending);
		CAttachment mAttachment = mMessage.getAttachment();
		if (mAttachment != null) {
			mAttachment.setMessageUid(newMessage.getUid());
			mAttachment.setAttchmentId(UUID.randomUUID().toString());
			if(!StringUtil.isEmpty(mAttachment.getFilePath())){
				mAttachment.setLocalPathFlag(1);
			}
			mAttachment.setForwardFlag(1);
			newMessage.setAttachment(mAttachment);
		}
		chattingAdapter.addOrUpdateMessage(newMessage);
		mGroupChattingView.setSelection(chattingAdapter.getCount());
		controller.publishCMessage(mGroup.getUid(), newMessage, mAccount, null);
	}

	private void mailInfoToGroupChatMessage() {
		isMailInfoToGroupChatMessage = false;
		CMessage newMessage = new CMessage(Type.FROM_MAIL_INFO);
		newMessage.setUid(UUID.randomUUID().toString());
		newMessage.setGroupUid(mGroup.getUid());
		newMessage.setMember(member_me);
		newMessage.setContent("");
		newMessage.setSendTime(getSendTime());
		newMessage.setMessageState(State.sending);
		//邮件信息透传
		newMessage.setMailFrom(mailInfoArray[0]);
		newMessage.setMailFromNickName(mailInfoArray[1]);
		newMessage.setMailSubject(mailInfoArray[2]);
		newMessage.setMailPreview(mailInfoArray[3]);
		chattingAdapter.addOrUpdateMessage(newMessage);
		mGroupChattingView.setSelection(chattingAdapter.getCount());
		controller.publishCMessage(mGroup.getUid(), newMessage, mAccount, null);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mGroupChattingView.setSelection(chattingAdapter.getCount());
			}
		}, 100);
	}
}
