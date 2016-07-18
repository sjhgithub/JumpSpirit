package cn.mailchat.activity;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.LruCache;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
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
import cn.mailchat.adapter.DChattingAdapter;
import cn.mailchat.adapter.UmengFeedbackAdapter;
import cn.mailchat.chatting.beans.DAttachment;
import cn.mailchat.chatting.beans.DChatMessage;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.mail.store.LocalStore;
import cn.mailchat.utils.ActivityManager;
import cn.mailchat.utils.FileUtil;
import cn.mailchat.utils.NetUtil;
import cn.mailchat.utils.SystemUtil;
import cn.mailchat.view.FaceRelativeLayout;

import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.SyncListener;
import com.umeng.fb.model.Conversation;
import com.umeng.fb.model.Reply;
import com.umeng.fb.model.UserInfo;

/**
 * 
 * @copyright © 35.com
 * @file name ：UmengFeekbackActivity.java
 * @author ：zhangjx
 * @create Data ：2014-8-19下午2:03:47
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2014-8-19下午2:03:47
 * @Modified by：zhangjx
 * @Description :单聊界面
 */
public class UmengFeekbackActivity extends BaseActionbarFragmentActivity {
	public static final String TAG_NOTIFY_SINGLE_CHATTING_FLAG = "notify_into_SingleChatting";
	public static final String DMESSAGE = "dmessage";
	public static final String TOEMAIL = "toEmail";
	public static final String TO_NICKNAME = "toNickname";
	public static final String BACKLAST = "backLast";
	public static final String ACCOUNTUUID = "accountUuid";
	private static final String ACTION_CHATLIST = "cn.mailchat.intent.action.CHAT_LIST";
	public static final String ACTION_DNOTIFY = "dnotify";
	private static final String ACTION_FORWARD = "singleForward";
	private static final String EXTRA_EMAIL = "email";
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
	// private TextView title;
	/**
	 * 返回按钮
	 */
	// private LinearLayout layoutBack;
	/**
	 * 标题右侧按钮
	 */
	// private ImageView chattingSet;
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

	private int mode = 2;// 输入框状态 1：语音 2：文字
	// 照片路径
	private File mPhotoFile;

	// 图片缓存
	private LruCache<String, Bitmap> mMemoryCache;
	private Map<Integer, Integer> progressMap;
	private Map<String, Boolean> suspendedMap;// 附件暂停
	// 消息
	private List<DChatMessage> pushMesssage;
	private DChattingAdapter dChatAdapter;
	private Account account;
	private MediaPlayer mMediaPlayer;
	private boolean isOnlyRead = false;
	private boolean hasMore = true;
	private boolean isLoadMore = false;
	private int showedCount = 0;
	// 每次加载条数
	private int loadLimit = 10;
	private String dChatUid;
	private String toEmail;
	private boolean isHaveForwardMessage;
	private static final SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyyMMddHHmmssSSS", Locale.CHINA);
	private View mCustomActionbarView;

	private LinearLayout noNetLinearLayoutView;
	private TextView setNetTextView;
	private FeedbackAgent mAgent;
	private Conversation mComversation;
	private UmengFeedbackAdapter adapter;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private TextView mTitle;
	private String loginEmail;

	public static void actionChatList(Context mContext,String loginEmail) {
		Intent tIntent = new Intent(mContext, UmengFeekbackActivity.class);
		Bundle bundle = new Bundle();// 创建Bundle对象
		bundle.putString(EXTRA_EMAIL, loginEmail);
		tIntent.putExtras(bundle);
		tIntent.setAction(ACTION_CHATLIST);
		mContext.startActivity(tIntent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivityManager.push(this);
		setContentView(R.layout.activity_umeng_feedback);
		initializeActionBar();
		initView();
		initData();
		initListener();
		registerNetWorkReceiver();
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
				.getString(R.string.setting_feedback));
	}


	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("UmengFeekbackActivity"); // 统计页面
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
		MobclickAgent.onPageEnd("UmengFeekbackActivity"); // 统计页面
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unRegisterNetWorkReceiver();
	}
	private void initListener() {
		// chattingSet.setOnClickListener(myClickListener());
		// layoutBack.setOnClickListener(myClickListener());
		changeChattingState.setOnClickListener(myClickListener());
		sendTxt.setOnClickListener(myClickListener());
		changeChattingState.setOnClickListener(myClickListener());
		voiceRecord.setOnClickListener(myClickListener());
		panelEmojiLayout.setOnClickListener(myClickListener());
		panelalbumLayout.setOnClickListener(myClickListener());
		panelfileLayout.setOnClickListener(myClickListener());
		panellocationLayout.setOnClickListener(myClickListener());
		showAttachmentPanel.setOnClickListener(myClickListener());
		// layoutBack.setOnClickListener(myClickListener());
		// chattingSet.setOnClickListener(myClickListener());
		pushMessageLayout.setOnClickListener(myClickListener());
		panelphotographLayout.setOnClickListener(myClickListener());
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
				// if (!StringUtil.isEmpty(chattingTitleTxt.getText().toString()
				// .trim())) {
				// showAttachmentPanel.setVisibility(View.GONE);
				// sendTxt.setVisibility(View.VISIBLE);
				// } else {
				// showAttachmentPanel.setVisibility(View.VISIBLE);
				// sendTxt.setVisibility(View.GONE);
				// }
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
				// chattingTitleTxt
				// .setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
				// | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
				// | InputType.TYPE_TEXT_FLAG_MULTI_LINE
				// | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(chattingTitleTxt, 0);
			}
		});

		// 下拉刷新
		mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				sync();
			}
		});
	}

	private OnClickListener myClickListener() {
		// TODO Auto-generated method stub
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.layout_back:
					finish();
					overridePendingTransition(R.anim.left_in, R.anim.right_out);
					break;
				case R.id.ibtn_view_chatting_attach_btn:
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
					// chattingTitleTxt
					// .setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
					// | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
					// | InputType.TYPE_TEXT_FLAG_MULTI_LINE
					// | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
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
						// chattingTitleTxt
						// .setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
						// | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
						// | InputType.TYPE_TEXT_FLAG_MULTI_LINE
						// | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
						InputMethodManager imm = (InputMethodManager) chattingTitleTxt
								.getContext().getSystemService(
										Context.INPUT_METHOD_SERVICE);
						imm.showSoftInput(chattingTitleTxt, 0);
						// imm.toggleSoftInput(0,
						// InputMethodManager.SHOW_FORCED);
					} else {
						faceLayout.setVisibility(View.VISIBLE);
						attachmentPanel.setVisibility(View.VISIBLE);
						chatting_bottom_util.setVisibility(View.GONE);

						panelEmojiLayout
								.setImageResource(R.drawable.icon_emoji_p);
						// 获取焦点
						chattingTitleTxt.requestFocus();
						// chattingTitleTxt.setInputType(InputType.TYPE_NULL);
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
					if (mode == 1) {
						mode = 2;
					} else {
						mode = 1;
					}
					changeMode();
					break;
				case R.id.btn_view_chatting_send:
					String txtContent = chattingTitleTxt.getText().toString()
							.trim();
					if (txtContent.length() == 0) {
						Toast.makeText(UmengFeekbackActivity.this,
								getString(R.string.chatting_no_content), 0)
								.show();
						return;
					}
					sendTextMsg(txtContent);
					setUmengUserInfo(mAgent);
					break;
				case R.id.layout_push_message:
					pushMesssage.clear();
					pushMessageLayout.setVisibility(View.GONE);
					mSingleChattingView.setSelection(dChatAdapter.getCount());
					mSingleChattingView.post(new Runnable() {
						@Override
						public void run() {
							mSingleChattingView
									.smoothScrollToPosition(dChatAdapter
											.getCount() - 1);
						}
					});
					mSingleChattingView
							.setSelection(dChatAdapter.getCount() - 1);
					break;
				default:
					break;
				}
			}
		};
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
		chattingTitleTxt.getEditableText().clear();
		if (!TextUtils.isEmpty(txtContent)) {
			// 将内容添加到会话列表
			mComversation.addUserReply(txtContent);
			// 刷新ListView
			adapter.notifyDataSetChanged();
			scrollToBottom();
			// 数据同步
			sync();
		}
	}

	private void initData() {
		Intent intent = this.getIntent(); // 获取已有的intent对象
		Bundle bundle = intent.getExtras(); // 获取intent里面的bundle对象
		loginEmail= bundle.getString(EXTRA_EMAIL); // 获取Bundle里面的字符串
		mSwipeRefreshLayout.setColorSchemeResources(R.color.gray, R.color.green, R.color.blue); 
		pushMesssage = new ArrayList<DChatMessage>();
		account = Preferences.getPreferences(this).getDefaultAccount();
		mAgent = new FeedbackAgent(this);
		mComversation = new FeedbackAgent(this).getDefaultConversation();
		adapter = new UmengFeedbackAdapter(this, mSingleChattingView, account,
				mComversation, getString(R.string.mail_chat_team));
		mSingleChattingView.setAdapter(adapter);
		sync();
		chattingTitleTxt.setText("");
		changeMode();
	}

	@SuppressWarnings("null")
	private void setUmengUserInfo(final FeedbackAgent agent) {
		UserInfo info = agent.getUserInfo();
		String email="";
		Account mAccount= Preferences.getPreferences(this).getDefaultAccount();
		if (mAccount!=null&&loginEmail==null) {
			email=mAccount.getEmail();
		} else {
			email=loginEmail;
		}
		if (info == null) {
			info = new UserInfo();
			updateUmengUserInfo(agent, info, email);
		} else {
			updateUmengUserInfo(agent, info, email);
		}
	}

	private void updateUmengUserInfo(final FeedbackAgent agent,UserInfo info, String email) {
		Map<String, String> contact = info.getContact();
		if (contact == null)
			contact = new HashMap<String, String>();
		contact.put("email", email);
//		 contact.put("qq", "*******");
//		 contact.put("phone",);
//		 contact.put("plain", SystemUtil.getModel()+"-->"+SystemUtil.getRelease()+"-->"+ getString(R.string.version_name));
		info.setContact(contact);
		// optional, setting user gender information.
		info.setAgeGroup(1);
		contact.put("plain",MailChat.lastAccountSetupCheckException==null?"":MailChat.lastAccountSetupCheckException);
		info.setGender("male");
		// info.setGender("female");
		agent.setUserInfo(info);

		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean result = agent.updateUserInfo();
			}
		}).start();
	}
	// 数据同步
	private void sync() {

		mComversation.sync(new SyncListener() {

			@Override
			public void onReceiveDevReply(List<Reply> arg0) {
				// TODO Auto-generated method stub
				// SwipeRefreshLayout停止刷新
				mSwipeRefreshLayout.setRefreshing(false);
				// 刷新ListView
				adapter.notifyDataSetChanged();
				scrollToBottom();
			}

			@Override
			public void onSendUserReply(List<Reply> arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void scrollToBottom() {
		if (adapter.getCount() > 0) {
			mSingleChattingView.smoothScrollToPosition(adapter.getCount());
		}
	}

	private void initView() {
		chattingTitleTxt = (EditText) findViewById(R.id.edit_view_chatting_content_et);
		changeChattingState = (ImageButton) findViewById(R.id.ibtn_view_chatting_mode_btn);
		changeChattingState.setVisibility(View.GONE);
		chattingContentLayout = (RelativeLayout) findViewById(R.id.layout_view_text_panel_ll);
		mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.feedback_reply_refresh);
		panelEmojiLayout = (ImageButton) findViewById(R.id.ibtn_view_chatting_mm);
		// 附件布局
		attachmentPanel = (FrameLayout) findViewById(R.id.frameView_chatting_bottom_panel);
		// 拍照
		panelphotographLayout = (RelativeLayout) findViewById(R.id.layoutView_chatting_panel_bottom_photograph);
		// 相册
		panelalbumLayout = (RelativeLayout) findViewById(R.id.layoutView_chatting_panel_bottom_album);
		// 文件
		panelfileLayout = (RelativeLayout) findViewById(R.id.layoutView_chatting_panel_bottom_file);
		// 表情
		faceLayout = (FaceRelativeLayout) findViewById(R.id.facechoose);
		faceLayout.setEt_sendmessage(chattingTitleTxt);
		// 定位
		panellocationLayout = (RelativeLayout) findViewById(R.id.layoutView_chatting_bottom_panel_location);
		// 加号
		showAttachmentPanel = (ImageButton) findViewById(R.id.ibtn_view_chatting_attach_btn);
		showAttachmentPanel.setVisibility(View.GONE);
		mHandler = new ComposeHandler(UmengFeekbackActivity.this);
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
		// chattingSet.setVisibility(View.VISIBLE);
		noNetLinearLayoutView = (LinearLayout) findViewById(R.id.item_net);
		noNetLinearLayoutView.setVisibility(View.GONE);
		setNetTextView = (TextView) findViewById(R.id.net_set);
		setNetTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
//				startActivity(new Intent(
//						android.provider.Settings.ACTION_WIRELESS_SETTINGS));
				NetUtil.showSystemSettingView(UmengFeekbackActivity.this);
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

		WeakReference<UmengFeekbackActivity> mReference;

		public ComposeHandler(UmengFeekbackActivity umengFeekbackActivity) {
			mReference = new WeakReference<UmengFeekbackActivity>(
					umengFeekbackActivity);
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
					mReference.get().canlVoice
							.setImageResource(R.drawable.voice1);
					break;
				case VOICE2:
					mReference.get().canlVoice
							.setImageResource(R.drawable.voice2);
					break;
				case VOICE3:
					mReference.get().canlVoice
							.setImageResource(R.drawable.voice3);
					break;
				case VOICE4:
					mReference.get().canlVoice
							.setImageResource(R.drawable.voice4);
					break;
				case VOICE5:
					mReference.get().canlVoice
							.setImageResource(R.drawable.voice5);
					break;
				case VOICE6:
					mReference.get().canlVoice
							.setImageResource(R.drawable.voice6);
					break;
				}
			}
		}

	}

	/**
	 * 
	 * method name: onActivityResult
	 * 
	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int,
	 *      android.content.Intent) function@Description: TODO
	 * @History memory：
	 * @Date：2014-11-13 下午1:29:11 @Modified by：zhangyq
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
					DChatMessage message = new DChatMessage(
							DChatMessage.Type.IMAGE);
					message.setUuid(UUID.randomUUID().toString());
					message.setTime(getSendTime());
					message.setSenderEmail(account.getEmail());
					message.setReceiverEmail(toEmail);
					message.setDchatUid(dChatUid);
					message.setMessageState(2);
					DAttachment attachment = new DAttachment();
					attachment.setAttchmentId(UUID.randomUUID().toString());
					attachment.setMessageUid(message.getUuid());
					String[] filePathDebris = gotPhotoPath.split("/");
					attachment
							.setName(filePathDebris[filePathDebris.length - 1]);
					attachment.setFilePath(picPath);
					attachment.setLocalPathFlag(1);
					ArrayList<DAttachment> dAttachments = new ArrayList<DAttachment>();
					dAttachments.add(attachment);
					message.setAttachments(dAttachments);
					if (!NetUtil.isActive()) {
						message.setMessageState(1);
					}
					contrastLastTime(message);
					dChatAdapter.addOrUpdateMessage(message);
					mSingleChattingView.setSelection(dChatAdapter.getCount());
				}
			}
			break;
		case PHOTO_PICKED_WITH_DATA:

			// 选择相册获得附件
			if (resultCode == Activity.RESULT_OK) {
				// String picPath =
				// FileUtil.getMediaFilePathByUri(ChattingActivity.this,
				// data.getData());
				// Rom 4.4获取图片无法获取到路径
				String picPath = FileUtil.getPath(UmengFeekbackActivity.this,
						data.getData());
				DChatMessage message = new DChatMessage(DChatMessage.Type.IMAGE);
				message.setUuid(UUID.randomUUID().toString());
				message.setTime(getSendTime());
				message.setSenderEmail(account.getEmail());
				message.setReceiverEmail(toEmail);
				message.setDchatUid(dChatUid);
				message.setMessageState(2);
				DAttachment attachment = new DAttachment();
				attachment.setAttchmentId(UUID.randomUUID().toString());
				attachment.setMessageUid(message.getUuid());
				attachment.setFilePath(picPath);
				String[] filePathDebris = picPath.split("/");
				attachment.setName(filePathDebris[filePathDebris.length - 1]);
				attachment.setLocalPathFlag(1);
				ArrayList<DAttachment> dAttachments = new ArrayList<DAttachment>();
				dAttachments.add(attachment);
				message.setAttachments(dAttachments);
				if (!NetUtil.isActive()) {
					message.setMessageState(1);
				}
				contrastLastTime(message);
				dChatAdapter.addOrUpdateMessage(message);
				mSingleChattingView.setSelection(dChatAdapter.getCount());
			}
			break;
		// case ACTIVITY_REQUEST_PICK_ATTACHMENT:
		//
		// // 选择文件返回结果
		// if (data == null) {
		// return;
		// }
		// /**
		// * 解析附件信息 [从选择文件页面回来]
		// *
		// * @Description:
		// * @param data
		// * @see:
		// * @since:
		// * @author: zhuanggy
		// * @date:2013-9-23
		// */
		// ArrayList<Attachment> dAttachments = new ArrayList<Attachment>();
		// Map<String, PickedFileInfo> checkedFileMap = (Map<String,
		// PickedFileInfo>) data.getSerializableExtra("checkedFileMap");
		// DChatMessage message = new
		// DChatMessage(DChatMessage.Type.ATTACHMENT);
		// message.setUuid(UUID.randomUUID().toString());
		// message.setTime(getSendTime());
		// message.setFromEmail(account.getEmail());
		// message.setToEmail(toEmail);
		// message.setDchatUid(dChatUid);
		// for (Entry<String, PickedFileInfo> Entry : checkedFileMap.entrySet())
		// {
		// PickedFileInfo file = Entry.getValue();
		// Attachment attachment = new Attachment();
		// //attachment.setAttchmentId(UUID.randomUUID().toString());
		// attachment.setFilePath(file.mContentUri.replace("file://", ""));
		// attachment.setMessageSid(message.getUuid());
		// attachment.setName(file.mName);
		// attachment.setContentType(file.mContentType);
		// attachment.setDAttachment(true);
		// attachment.setSize(file.mSize);
		// attachment.setLocalPathFlag(1);
		// dAttachments.add(attachment);
		// break;
		// }
		// message.setAttachments(dAttachments);
		// if (!NetUtil.isActive()) {
		// message.setMessageState("1");
		// }
		// contrastLastTime(message);
		// controller.sendDMessage(account, message);
		// dChatAdapter.addOrUpdateMessage(message);
		// break;
		//
		// case LOCATION_WITH_DATA:
		// if (resultCode == Activity.RESULT_OK) {
		// message = new DChatMessage(DChatMessage.Type.LOCATION);
		// message.setUuid(UUID.randomUUID().toString());
		// message.setTime(getSendTime());
		// message.setFromEmail(account.getEmail());
		// message.setToEmail(toEmail);
		// message.setDchatUid(dChatUid);
		// message.setLocationName(data.getStringExtra("name"));
		// message.setAddress(data.getStringExtra("addr"));
		// message.setLongitude(data.getDoubleExtra("x", 0));
		// message.setLatitude(data.getDoubleExtra("y", 0));
		// if(data.getBooleanExtra("type", true)){
		// message.setLocationType("baidu");
		// }else{
		// message.setLocationType("google");
		// }
		// if (!NetUtil.isActive()) {
		// message.setMessageState("1");
		// }
		// contrastLastTime(message);
		// controller.sendDMessage(account, message);
		// dChatAdapter.addOrUpdateMessage(message);
		// // mGroupChattingView.setSelection(chattingAdapter.getCount());
		// }
		// break;
		// case FILE_DOWNLOAD:
		// if (resultCode == Activity.RESULT_OK) {
		// chattingAdapter.getSuspendedMap().put(chattingAdapter.getmMessages().get(chattingAdapter.getPosition()).getAttachment().getAttchmentId(),
		// true);
		// //
		// chattingAdapter.getmMessages().get(chattingAdapter.getPosition()).getAttachment().setSuspended(true);
		// chattingAdapter.updateView(chattingAdapter.getPosition());
		// chattingAdapter.notifyDataSetChanged();
		// }
		// break;
		}

	}

	/**
	 * 单聊\群聊底部面板操作（发送图片、文件、位置等）
	 * 
	 * method name: panelButOnclick function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param whichButton
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-11-13 下午1:09:51 @Modified by：zhangyq
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
					mPhotoFile = new File(photoPath,
							FileUtil.getCameraFileName());
					Intent intentTakePicture = new Intent(
							MediaStore.ACTION_IMAGE_CAPTURE, null);
					intentTakePicture.putExtra(MediaStore.EXTRA_OUTPUT,
							Uri.fromFile(mPhotoFile));
					startActivityForResult(intentTakePicture, CAMERA_WITH_DATA);
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(
							MailChat.getInstance(),
							getString(R.string.message_compose_equipment_not_start),
							Toast.LENGTH_LONG).show();
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
		// case R.id.layoutView_chatting_panel_bottom_file:
		// // 选择文件
		// Intent i = new Intent(this, FilePickerActivity.class);
		// i.putExtra(FilePickerActivity.SINGLE_CHOICE, true);
		// startActivityForResult(i, ACTIVITY_REQUEST_PICK_ATTACHMENT);
		//
		// break;
		// case R.id.layoutView_chatting_bottom_panel_location:
		// // startActivity(new Intent(this, SelectLocationActivity.class));
		//
		// Intent intentLocation = new Intent(this,
		// SelectLocationActivity.class);
		// startActivityForResult(intentLocation, LOCATION_WITH_DATA);
		// break;
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
		attachmentPanel.setVisibility(View.GONE);
		if (mode == 1) {
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
			sendTxt.setVisibility(View.VISIBLE);
		}
	}

	private void closeInputMethod() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(chattingTitleTxt.getWindowToken(), 0);
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
		return currentTime;
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

	// private MessagingListener dCallback = new MessagingListener() {
	// public void chatMessageArrived(Account mAccount,final DChatMessage
	// dChatMessage){
	// //判断是否是本地用户和对方发来的消息
	// if
	// (dChatMessage.getDchatUid().endsWith(dChatUid)&&account.getUuid().equals(mAccount.getUuid()))
	// {
	// runOnUiThread(new Runnable() {
	//
	// @Override
	// public void run() {
	// // 列表不在最底端，提示框显示
	// int lastVisiblePosition = mSingleChattingView.getLastVisiblePosition();
	// if (lastVisiblePosition != dChatAdapter.getmDChatMessages().size() - 1) {
	// setTranscriptModeDisabled();
	// dChatAdapter.addOrUpdateMessage(dChatMessage);
	// pushMesssage.add(message);
	// pushMessageLayout.setVisibility(View.VISIBLE);
	// String content = dChatMessage.getMessageContent();
	// switch (dChatMessage.getMessageType()) {
	// case IMAGE:
	// content = MailChat.getInstance().getString(R.string.msg_img);
	// break;
	// case ATTACHMENT:
	// content = MailChat.app.getString(R.string.msg_att);
	// break;
	// case VOICE:
	// content = MailChat.getInstance().getString(R.string.msg_voice);
	// break;
	// case LOCATION:
	// content = MailChat.app.getString(R.string.msg_location);
	// break;
	// default:
	// break;
	// }
	// pushhMessageText.setText(toNickname + "：" + content);
	// int count = pushMesssage.size();
	// if (count > 1) {
	// pushhMessageCount.setText(count + "");
	// } else {
	// pushhMessageCount.setText("");
	// }
	// setTranscriptModeScroll();
	// } else {
	// dChatAdapter.addOrUpdateMessage(dChatMessage);
	// mSingleChattingView.setSelection(dChatAdapter.getCount());
	// }
	// controller.updateDChatReadState(account, dChatUid, null);
	// }
	// });
	// }
	//
	// }
	// /**
	// *
	// * method name: sendDChatMessageSuccess
	// * @see
	// cn.mailchat.logic.MessageCallback#sendDChatMessageSuccess(cn.mailchat.Account,
	// java.lang.String)
	// * function@Description: TODO
	// * @History memory：
	// * @Date：2014-8-22 下午3:28:07 @Modified by：zhangjx
	// * @Description：发送单聊消息成功
	// */
	// public void sendDMessagesSuccess(Account mAccount, final String uid) {
	// if (account.getUuid().equals(mAccount.getUuid())) {
	// runOnUiThread(new Runnable() {
	//
	// @Override
	// public void run() {
	// dChatAdapter.updateMessage(uid,true);
	// }
	// });
	// }
	// }
	// /**
	// *
	// * method name: sendDChatMessageFailed
	// * @see
	// cn.mailchat.logic.MessageCallback#sendDChatMessageFailed(cn.mailchat.Account,
	// java.lang.String)
	// * function@Description: TODO
	// * @History memory：
	// * @Date：2014-8-22 下午3:28:19 @Modified by：zhangjx
	// * @Description：发送单聊消息失败
	// */
	// public void sendDMessagesFail(Account mAccount, final String dMessageUid)
	// {
	// if (account.getUuid().equals(mAccount.getUuid())) {
	// runOnUiThread(new Runnable() {
	//
	// @Override
	// public void run() {
	// dChatAdapter.updateMessage(dMessageUid, false);
	// }
	// });
	// }
	// }
	//
	// public void listDMessagesFinished(String uuid, String dchatUid,final
	// List<DChatMessage> dChatMessages) {
	// if (account.getUuid().equals(uuid) && dchatUid.equals(dChatUid)) {
	// runOnUiThread(new Runnable() {
	//
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// if(dChatMessages!=null){
	// dChatAdapter.updateMessage(dChatMessages);
	// if (isHaveForwardMessage) {
	// //forwardMessage();
	// } else {
	// if (isLoadMore) {
	// mSingleChattingView.setSelection(dChatAdapter.getCount() - showedCount);
	// isLoadMore = false;
	// if (dChatAdapter.getCount() < showedCount + loadLimit) {
	// hasMore = false;
	// }
	// }else{
	// mSingleChattingView.setSelection(dChatAdapter.getCount());
	// }
	// }
	//
	// }
	// }
	// });
	//
	// }
	// }
	// /**
	// *
	// * method name: deleteDChatSuccess function @Description: TODO
	// * Parameters and return values description：
	// *
	// * @param account
	// * @param dChatDMessageUid
	// * field_name void return type
	// * @History memory：
	// * @Date：2014-10-23 下午8:44:40 @Modified by：zhangjx
	// * @Description：删除单聊成功
	// */
	// public void deleteDChatSuccess(final Account acc, String
	// dChatDMessageUid) {
	// if (account.getUuid().equals(acc.getUuid())) {
	// mHandler.post(new Runnable() {
	//
	// @Override
	// public void run() {
	// finish();
	// }
	// });
	// }
	// }
	//
	// public void againDownDChatThumbnailImageSuccess(Account acc) {
	// if (acc.getUuid().equals(account.getUuid())) {
	// mHandler.post(new Runnable() {
	//
	// @Override
	// public void run() {
	// dChatAdapter.notifyDataSetChanged();
	// }
	// });
	// }
	// }
	//
	// public void againDownDChatThumbnailImageFailed(Account acc) {
	// if (acc.getUuid().equals(account.getUuid())) {
	// mHandler.post(new Runnable() {
	//
	// @Override
	// public void run() {
	// dChatAdapter.notifyDataSetChanged();
	// Toast.makeText(UmengFeekbackActivity.this,
	// R.string.again_down_thumbnail_image_fail,Toast.LENGTH_SHORT).show();
	// }
	// });
	// }
	// }
	//
	// public void createGroupSuccess(String uuid,CGroup cGroup) {
	// if (uuid.equals(account.getUuid())) {
	// finish();
	// }
	// }
	// };


	/**
	 * 对比本地最后一条消息时间
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2014-9-9
	 */
	public void contrastLastTime(DChatMessage message) {
		// TODO:服务器和客户端都去同步一个时区的时间。后期如果可以的话。方法二：复写安卓同步时间模块
		// 现在客户端处理，对比最后一条消息时间，如果本地时间小于最后一条消息时间（服务器时间或者发送失败的本地时间--这个本地时间是上一条消息+1毫秒），大于的话就直接取本地时间。
		try {
			LocalStore localStore = account.getLocalStore();
			List<DChatMessage> dChatMessages = localStore.listDchatMessages(
					message.getDchatUid(), 1);
			if (dChatMessages.size() != 0) {
				if (dChatMessages.get(0).getTime() > message.getTime()) {
					message.setTime(dChatMessages.get(0).getTime() + 1);
				}
			}
		} catch (MessagingException e) {
			e.printStackTrace();
			Toast.makeText(this,
					getString(R.string.compare_message_time_failed),
					Toast.LENGTH_LONG).show();
		}
	}



	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		finish();
	}

	private BroadcastReceiver netWorkReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			if (NetUtil.isActive()) {
				noNetLinearLayoutView.setVisibility(View.GONE);
			} else {
				noNetLinearLayoutView.setVisibility(View.VISIBLE);
			}
		}
	};

	private void registerNetWorkReceiver() {
		IntentFilter filter = new IntentFilter(
				"android.net.conn.CONNECTIVITY_CHANGE");
		registerReceiver(netWorkReceiver, filter);
	}

	private void unRegisterNetWorkReceiver() {
		unregisterReceiver(netWorkReceiver);
	}
	
}
