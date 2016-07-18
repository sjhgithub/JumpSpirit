package cn.mailchat.adapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.support.v4.util.LruCache;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.activity.ChatFromMailInfoActivity;
import cn.mailchat.activity.ChattingActivity;
import cn.mailchat.activity.ChattingSingleActivity;
import cn.mailchat.activity.ContactInfoActivity;
import cn.mailchat.activity.ImageFullActivity;
import cn.mailchat.chatting.beans.CAttachment;
import cn.mailchat.chatting.beans.CGroup;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.chatting.beans.CMessage;
import cn.mailchat.chatting.beans.CMessage.State;
import cn.mailchat.chatting.protocol.Protocol;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.utils.AttachmentUtil;
import cn.mailchat.utils.EncryptUtil;
import cn.mailchat.utils.FaceConversionUtil;
import cn.mailchat.utils.FileUtil;
import cn.mailchat.utils.GlobalTools;
import cn.mailchat.utils.ImageUtils;
import cn.mailchat.utils.NetUtil;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.utils.TimeUtils;
import cn.mailchat.view.ChattingShowDialog;
import cn.mailchat.view.MailDialog;
import cn.mailchat.view.RoundImageView;
import cn.mailchat.view.TextViewFixTouchConsume;

public class ChattingAdapter extends BaseAdapter implements OnClickListener{
	private Context mContext;
	private Account mAccount;
	private CGroup mGroup;
	private List<CMessage> mMessages;
	private LayoutInflater mInflater;

	private static final int TYPE_FROM_TEXT = 0;
	private static final int TYPE_FROM_IMAGE = 1;
	private static final int TYPE_FROM_VOICE = 2;
	private static final int TYPE_FROM_ATTACHMENT = 3;
	private static final int TYPE_FROM_LOCATION = 4;
	private static final int TYPE_FROM_NOTIFICATION = 5;
	private static final int TYPE_TO_TEXT = 6;
	private static final int TYPE_TO_IMAGE = 7;
	private static final int TYPE_TO_VOICE = 8;
	private static final int TYPE_TO_ATTACHMENT = 9;
	private static final int TYPE_TO_LOCATION = 10;
	private static final int TYPE_MAIL_INFO =11;
	private static final int TYPE_COUNT = 12;

	private MediaPlayer mediaPlayer;
	private String playingUri = "";

	public AnimationDrawable animationDrawable;
	private ImageView voiceView;
	private int viewType;
	// 界面中最新一条数据的发送时间
	public long lastMessageSendTime = 0;
	// 图片缓存
	private LruCache<String, Bitmap> mMemoryCache;
	// 地理位置logo
	private Drawable locationDrawable;

	private ListView mGroupChattingView;
	private MessagingController messageController;
	private String currentItemVoiceUid;
	public ChattingAdapter(Context context, ListView mGroupChattingView,
			Account account, CGroup group, List<CMessage> messages,
			final MediaPlayer mediaPlayer) {
		messageController=MessagingController.getInstance(MailChat.app);
		this.mContext = context;
		this.mAccount = account;
		this.mGroupChattingView = mGroupChattingView;
		if (messages == null) {
			mMessages = new ArrayList<CMessage>();
		} else {
			mMessages = messages;
		}
		mGroup = group;
		lastMessageSendTime = mGroup.getLastSendDate();
		mInflater = LayoutInflater.from(mContext);
		this.mediaPlayer = mediaPlayer;
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer arg0) {
				// mediaPlayer.release();
				mediaPlayer.reset();
				playingUri = "";
				stopAnim();
			}
		});
		locationDrawable = mContext.getResources().getDrawable(
				R.drawable.icon_item_location);
		locationDrawable.setBounds(0, 0, locationDrawable.getIntrinsicWidth(),
				locationDrawable.getIntrinsicHeight());
		mMemoryCache = ((MailChat) MailChat.app).getmMemoryCache();
	}
	@Override
	public int getItemViewType(int position) {
		int type = 0;
		CMessage message = mMessages.get(position);
		boolean isTo = mAccount.getEmail().equals(
				message.getMember().getEmail());
		switch (message.getMessageType()) {
		case TEXT:
			type = isTo ? TYPE_TO_TEXT : TYPE_FROM_TEXT;
			break;
		case IMAGE:
			type = isTo ? TYPE_TO_IMAGE : TYPE_FROM_IMAGE;
			break;
		case VOICE:
			type = isTo ? TYPE_TO_VOICE : TYPE_FROM_VOICE;
			break;
		case ATTACHMENT:
			type = isTo ? TYPE_TO_ATTACHMENT : TYPE_FROM_ATTACHMENT;
			break;
		case LOCATION:
			type = isTo ? TYPE_TO_LOCATION : TYPE_FROM_LOCATION;
			break;
		case NOTIFICATION:
			type = TYPE_FROM_NOTIFICATION;
			break;
		case FROM_MAIL_INFO:
			type = TYPE_MAIL_INFO;
			break;
		}
		return type;
	}

	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mMessages.size();
	}
	
	@Override
	public int getViewTypeCount() {
		return TYPE_COUNT;
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		int viewType = getItemViewType(position);
		ChattingViewHolder holder = null;
		if (convertView == null) {
			holder = new ChattingViewHolder();
			convertView = initItemView(parent, viewType, holder);
			convertView.setTag(holder);
		} else {
			holder = (ChattingViewHolder) convertView.getTag();
		}
		bindView(holder, convertView, position, viewType);
		return convertView;
	}
	
	private View initItemView(ViewGroup parent, int viewType,
			ChattingViewHolder holder) {
		View view = null;
		switch (viewType) {
		case TYPE_FROM_TEXT:
			view = mInflater.inflate(R.layout.chatting_item_from_content,
					parent, false);
			holder.senderTimeContentLeftView = (TextView) view
					.findViewById(R.id.chatting_item_time);
			holder.textContentLeftView = (TextViewFixTouchConsume) view
					.findViewById(R.id.txt_view_chatting_content);
			holder.nameView = (TextView) view
					.findViewById(R.id.chatting_item_name);
			holder.headImageView =(RoundImageView)view
					.findViewById(R.id.chatting_item_head);
			holder.msgTimeView =(TextView) view.findViewById(R.id.chatting_item_msg_time);
			break;
		case TYPE_FROM_IMAGE:
			view = mInflater.inflate(R.layout.chatting_item_from_picture,
					parent, false);
			holder.nameView = (TextView) view
					.findViewById(R.id.chatting_item_name);
			holder.senderTimeImageLeftView = (TextView) view
					.findViewById(R.id.chatting_item_time);
			holder.imageContentLeftView = (ImageView) view
					.findViewById(R.id.img_view_chatting_picture);
			holder.headImageView =(RoundImageView)view
					.findViewById(R.id.chatting_item_head);
			holder.msgTimeView =(TextView) view.findViewById(R.id.chatting_item_msg_time);
			break;
		case TYPE_FROM_VOICE:
			view = mInflater.inflate(R.layout.chatting_item_from_voice, parent,
					false);
			holder.nameView = (TextView) view
					.findViewById(R.id.chatting_item_name);
			holder.senderTimeVoiceLeftView = (TextView) view
					.findViewById(R.id.chatting_item_time);
			holder.voiceContentLeftView = (ImageView) view
					.findViewById(R.id.img_view_chatting_voice_play);
			holder.voiceUnreadLeftView = (ImageView) view
					.findViewById(R.id.img_voice_unread);
			holder.voiceLengthLeftView = (TextView) view
					.findViewById(R.id.txt_view_chatting_voice_size);
			holder.headImageView =(RoundImageView)view
					.findViewById(R.id.chatting_item_head);
			holder.voiceLeftView =(RelativeLayout) view.findViewById(R.id.chatting_layout_from);
			holder.msgTimeView =(TextView) view.findViewById(R.id.chatting_item_msg_time);
			break;
		case TYPE_FROM_ATTACHMENT:
			view = mInflater.inflate(R.layout.chatting_item_from_attachments,
					parent, false);
			holder.nameView = (TextView) view
					.findViewById(R.id.chatting_item_name);
			holder.senderTimeAttLeftView = (TextView) view
					.findViewById(R.id.chatting_item_time);
			holder.attIconLeftView = (ImageView) view
					.findViewById(R.id.img_view_chatting_attachments_file_logo);
			holder.attNameLeftView = (TextView) view
					.findViewById(R.id.txt_view_chatting_attachments_file_name);
			holder.attSizeLeftView = (TextView) view
					.findViewById(R.id.txt_view_chatting_attachments_file_size);
			holder.attStatusLeftView = (TextView) view
					.findViewById(R.id.txt_view_chatting_attachments_status);
			holder.attProgressLeftView = (ProgressBar) view
					.findViewById(R.id.progress);
			holder.attLayoutprogressLeftView = (RelativeLayout) view
					.findViewById(R.id.layout_progress);
			holder.attCancelLeftView=(ImageView) view
					.findViewById(R.id.img_view_chatting_attachments_file_cancel);
			holder.attMenuLeftView=(ImageView) view
					.findViewById(R.id.img_view_chatting_attachments_file_menu);
			holder.headImageView =(RoundImageView)view
					.findViewById(R.id.chatting_item_head);
			holder.msgTimeView =(TextView) view.findViewById(R.id.chatting_item_msg_time);
			break;
		case TYPE_FROM_LOCATION:
			view = mInflater.inflate(R.layout.chatting_item_from_location,
					parent, false);
			holder.nameView = (TextView) view
					.findViewById(R.id.chatting_item_name);
			holder.senderTimeLocationLeftView = (TextView) view
					.findViewById(R.id.chatting_item_time);
			holder.locationNameLeftView = (TextViewFixTouchConsume) view
					.findViewById(R.id.tv_chatting_item_location_name);
			holder.msgTimeView =(TextView) view.findViewById(R.id.chatting_item_msg_time);
			// holder.locationAddrLeftView = (TextView)
			// view.findViewById(R.id.tv_chatting_item_location_addr);
			holder.headImageView =(RoundImageView)view
					.findViewById(R.id.chatting_item_head);
			break;
		case TYPE_FROM_NOTIFICATION:
			view = mInflater.inflate(R.layout.chatting_item_from_notification,
					parent, false);
			break;
		case TYPE_TO_TEXT:
			view = mInflater.inflate(R.layout.chatting_item_to_text, parent,
					false);
			holder.nameView = (TextView) view
					.findViewById(R.id.chatting_item_name);
			holder.senderTimeContentRightView = (TextView) view
					.findViewById(R.id.chatting_item_time);
			holder.textContentRightView = (TextViewFixTouchConsume) view
					.findViewById(R.id.txt_view_chatting_content);
			holder.textSendFailed = (ImageView) view
					.findViewById(R.id.chatting_item_send_failed);
			holder.textSending= (ProgressBar) view.findViewById(R.id.pb_text_sending);
			holder.headImageView =(RoundImageView)view
					.findViewById(R.id.chatting_item_head);
			holder.msgTimeView =(TextView) view.findViewById(R.id.chatting_item_msg_time);
			break;
		case TYPE_TO_IMAGE:
			view = mInflater.inflate(R.layout.chatting_item_to_picture, parent,
					false);
			holder.nameView = (TextView) view
					.findViewById(R.id.chatting_item_name);
			holder.senderTimeImageRightView = (TextView) view
					.findViewById(R.id.chatting_item_time);
			holder.imageContentRightView = (ImageView) view
					.findViewById(R.id.img_view_chatting_picture);
			holder.imageSendFailed = (ImageView) view
					.findViewById(R.id.chatting_item_send_failed);
			holder.imageSending = (ProgressBar) view.findViewById(R.id.picture_sending);
			holder.headImageView =(RoundImageView)view
					.findViewById(R.id.chatting_item_head);
			holder.msgTimeView =(TextView) view.findViewById(R.id.chatting_item_msg_time);
			break;
		case TYPE_TO_VOICE:
			view = mInflater.inflate(R.layout.chatting_item_to_voice, parent,
					false);
			holder.nameView = (TextView) view
					.findViewById(R.id.chatting_item_name);
			holder.senderTimeVoiceRightView = (TextView) view
					.findViewById(R.id.chatting_item_time);
			holder.voiceContentRightView = (ImageView) view
					.findViewById(R.id.img_view_chatting_voice_play);
			holder.voiceLengthRightView = (TextView) view
					.findViewById(R.id.txt_view_chatting_voice_size);
			holder.voiceSendFailed = (ImageView) view
					.findViewById(R.id.chatting_item_send_failed);
			holder.voiceSending = (ProgressBar) view
					.findViewById(R.id.voice_sending);
			holder.headImageView =(RoundImageView)view
					.findViewById(R.id.chatting_item_head);
			holder.voiceRightView =(RelativeLayout) view.findViewById(R.id.chatting_layout_to);
			holder.msgTimeView =(TextView) view.findViewById(R.id.chatting_item_msg_time);
			break;
		case TYPE_TO_ATTACHMENT:
			view = mInflater.inflate(R.layout.chatting_item_to_attachment,
					parent, false);
			holder.nameView = (TextView) view
					.findViewById(R.id.chatting_item_name);
			holder.senderTimeAttRightView = (TextView) view
					.findViewById(R.id.chatting_item_time);
			holder.attIconRightView = (ImageView) view
					.findViewById(R.id.img_view_chatting_attachments_file_logo);
			holder.attNameRightView = (TextView) view
					.findViewById(R.id.txt_view_chatting_attachments_file_name);
			holder.attSizeRightView = (TextView) view
					.findViewById(R.id.txt_view_chatting_attachments_file_size);
			holder.attStatusRightView = (TextView) view
					.findViewById(R.id.txt_view_chatting_attachments_status);
			holder.attSendFailed = (ImageView) view
					.findViewById(R.id.chatting_item_send_failed);
			holder.attProgressRightView = (ProgressBar) view
					.findViewById(R.id.progress);
			holder.attLayoutprogressRightView = (RelativeLayout) view
					.findViewById(R.id.layout_progress);
			holder.attCancelRightView=(ImageView) view
					.findViewById(R.id.img_view_chatting_attachments_file_cancel);
			holder.attMenuRightView=(ImageView) view
					.findViewById(R.id.img_view_chatting_attachments_file_menu);
			holder.headImageView =(RoundImageView)view
					.findViewById(R.id.chatting_item_head);
			holder.msgTimeView =(TextView) view.findViewById(R.id.chatting_item_msg_time);
			break;
		case TYPE_TO_LOCATION:
			view = mInflater.inflate(R.layout.chatting_item_to_location,
					parent, false);
			holder.nameView = (TextView) view
					.findViewById(R.id.chatting_item_name);
			holder.senderTimeLocationRightView = (TextView) view
					.findViewById(R.id.chatting_item_time);
			holder.locationNameRightView = (TextViewFixTouchConsume) view
					.findViewById(R.id.tv_chatting_item_location_name);
			holder.locatioSendFailed = (ImageView) view
					.findViewById(R.id.chatting_item_send_failed);
			holder.msgTimeView =(TextView) view.findViewById(R.id.chatting_item_msg_time);
			// holder.locationAddrRightView = (TextView)
			// view.findViewById(R.id.tv_chatting_item_location_addr);
			holder.headImageView =(RoundImageView)view
					.findViewById(R.id.chatting_item_head);
			break;
		case TYPE_MAIL_INFO:
			view = mInflater.inflate(R.layout.chatting_item_mail_info,
					parent, false);
			holder.mailInfoText = (TextView) view
					.findViewById(R.id.chatting_item_mail_info);
			break;
		}
		return view;
	}
	private void bindView(final ChattingViewHolder holder, View convertView,
			final int position, final int viewType) {
		CMessage message = mMessages.get(position);
		if (holder.nameView != null) {
			if (!((ChattingActivity) mContext).isOnlyRead()) {
				holder.nameView.setOnClickListener(this);
			}
			holder.nameView.setTag(position);
			//头像设置
			holder.headImageView.setTag(position);
			final String nickName = getNickName(message);
			String avatarHash = message.getMember().getAvatarHash() != null ? GlobalConstants.HOST_IMG +message.getMember().getAvatarHash()
					+ GlobalConstants.USER_SMALL_HEAD_END : "";
			ImageLoader.getInstance().displayImage(avatarHash, holder.headImageView,
					MailChat.getInstance().initImageLoaderOptions(),new ImageLoadingListener() {

						@Override
						public void onLoadingStarted(String arg0, View arg1) {
							// TODO Auto-generated method stub
						}

						@Override
						public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
							// TODO Auto-generated method stub
						}

						@Override
						public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
							// TODO Auto-generated method stub
							if(arg2==null){
								holder.headImageView.setImageBitmap(ImageUtils.getUserFirstTextBitmap(mContext, nickName));
							}
						}

						@Override
						public void onLoadingCancelled(String arg0, View arg1) {
							// TODO Auto-generated method stub
						}
					});

			holder.headImageView.setOnClickListener(this);
		
			//昵称,消息时间设置
			String msgTime=TimeUtils.DateFormatHM.format(new Date(message
					.getSendTime()));
			if(message.getMember().getEmail().equals(mAccount.getEmail())&&!message.getMessageState().equals(CMessage.State.sendSuccess)){
				msgTime="";
			}
			holder.nameView.setText(nickName);
			//holder.msgTimeView.setText(msgTime);
		}

		switch (viewType) {
		case TYPE_FROM_TEXT:

			if (!StringUtil.isEmpty(message.getContent())) {
				String msgContent = message.getContent();
				SpannableString spannableString = FaceConversionUtil
						.getInstace().getExpressionString(mContext, msgContent);
				holder.textContentLeftView.setText(spannableString);

				int lineCount = holder.textContentLeftView.getLineCount();
				if (lineCount == 1) {
					int w = GlobalTools.dip2px(mContext, 4);
					int h = GlobalTools.dip2px(mContext, 5f);
					holder.textContentLeftView.setPadding(w, h, w, h);
				} else {
					int size = GlobalTools.dip2px(mContext, 4);
					holder.textContentLeftView.setPadding(size, size, size,
							size);
				}
				//holder.textContentLeftView.setText(getClickableSpan(getNickName(message,viewType),position));
				holder.textContentLeftView
						.setMovementMethod(TextViewFixTouchConsume.LocalLinkMovementMethod
								.getInstance());
				//WebViewActivity.extractUrl2Link(holder.textContentLeftView);
			} else {
				holder.textContentLeftView.setText("");
			}
			// 时间显示逻辑
			setShowTime(holder.senderTimeContentLeftView, position);
			break;
		case TYPE_TO_TEXT:
			if (!StringUtil.isEmpty(message.getContent())) {
				SpannableString spannableString2 = FaceConversionUtil
						.getInstace().getExpressionString(mContext,
								message.getContent());
				// holder.textContentRightView.setText(spannableString2);
				// holder.textContentRightView.setText(getClickableSpan(getNickName(message,
				// viewType),
				// position));
				holder.textContentRightView.setText(spannableString2);
				int lineCount = holder.textContentRightView.getLineCount();
				if (lineCount == 1) {
					int w = GlobalTools.dip2px(mContext, 4);
					int h = GlobalTools.dip2px(mContext, 5f);
					holder.textContentRightView.setPadding(w, h, w, h);
				} else {
					int size = GlobalTools.dip2px(mContext, 4);
					holder.textContentRightView.setPadding(size, size, size,
							size);
				}
				holder.textContentRightView
						.setMovementMethod(TextViewFixTouchConsume.LocalLinkMovementMethod
								.getInstance());
			//	WebViewActivity.extractUrl2Link(holder.textContentRightView);
			} else {
				holder.textContentRightView.setText("");
			}
			
			holder.textSendFailed.setVisibility(View.GONE);
			// 发送状态
			viewSendFailed(holder.textSendFailed,holder.textSending,message);

			// 时间显示逻辑
			setShowTime(holder.senderTimeContentRightView, position);
			break;
		case TYPE_FROM_IMAGE:
			//别人发送的只会是网络图片
			String imageUrl = Protocol.getInstance().getFileURL(message.getAttachment().getFileid(), message.getAttachment().getName(), true);
			setImageUI(holder.imageContentLeftView, message.getAttachment().getImageWidth(), message.getAttachment().getImageHeight());
			if(message.getAttachment().isImageLoad()){
				ImageLoader.getInstance().displayImage(imageUrl,
						holder.imageContentLeftView, initImageLoaderOptions(),
						new ImageLoadingListener() {

							@Override
							public void onLoadingStarted(String arg0, View arg1) {
								// TODO Auto-generated method stub
							}

							@Override
							public void onLoadingFailed(String arg0, View arg1,
									FailReason arg2) {
								// TODO Auto-generated method stub
							}

							@Override
							public void onLoadingComplete(String arg0,
									View arg1, Bitmap arg2) {
								// TODO Auto-generated method stub
								if (arg2 == null) {
									holder.imageContentLeftView
											.setImageDrawable(null);
								}
							}

							@Override
							public void onLoadingCancelled(String arg0,
									View arg1) {
								// TODO Auto-generated method stub
							}
						});
			}else{
				holder.imageContentLeftView.setImageDrawable(null);
			}
			// 时间显示逻辑
			setShowTime(holder.senderTimeImageLeftView, position);
			break;
		case TYPE_TO_IMAGE:
			String imageUrl2 ="";
			String filePaht = message.getAttachment().getFilePath();
			if(StringUtil.isEmpty(filePaht)){//转发别人发的
				//imageLoader识别的url
				imageUrl2 = Protocol.getInstance().getFileURL(message.getAttachment().getFileid(), message.getAttachment().getName(), true);
			}else{
				//本地路径
				imageUrl2 = "file://" +filePaht;
			}
			setImageUI(holder.imageContentRightView, message.getAttachment().getImageWidth(), message.getAttachment().getImageHeight());
			if(message.getAttachment().isImageLoad()||imageUrl2.contains("file://")){

				//TODO:ImageLoader加载本体图片时，没有处理本地照片旋转问题，会使一些特殊照片，方向不对，需要处理。

				ImageLoader.getInstance().displayImage(imageUrl2,
						holder.imageContentRightView, initImageLoaderOptions(),
						new ImageLoadingListener() {

							@Override
							public void onLoadingStarted(String arg0, View arg1) {
								// TODO Auto-generated method stub
							}

							@Override
							public void onLoadingFailed(String arg0, View arg1,
									FailReason arg2) {
								// TODO Auto-generated method stub
							}

							@Override
							public void onLoadingComplete(String arg0,
									View arg1, Bitmap arg2) {
								// TODO Auto-generated method stub
								if (arg2 == null) {
									holder.imageContentRightView
											.setImageDrawable(null);
								}
							}

							@Override
							public void onLoadingCancelled(String arg0,
									View arg1) {
								// TODO Auto-generated method stub
							}
						});
			}else{
				holder.imageContentRightView.setImageDrawable(null);
			}
			// 发送状态
			viewSendFailed(holder.imageSendFailed,holder.imageSending, message);
			// 时间显示逻辑
			setShowTime(holder.senderTimeImageRightView, position);

			break;
		case TYPE_FROM_VOICE:
			holder.voiceLengthLeftView.setText(message.getAttachment().getVoiceLength() + "''");
			if (message.getAttachment().getReadFlag() == 0) {
				holder.voiceUnreadLeftView.setVisibility(View.VISIBLE);
			} else {
				holder.voiceUnreadLeftView.setVisibility(View.GONE);
			}
			if (mediaPlayer != null && mediaPlayer.isPlaying()) {
				if (playingUri.equals(MailChat.getInstance().getChatVoiceDirectory(mAccount) + EncryptUtil.getMd5(message.getAttachment().getAttchmentId())+ ".amr")) {
					if (animationDrawable != null && currentItemVoiceUid!=null) {
						startAnim(holder,viewType);
					}
				} else {
					stopAnim(holder,viewType);
				}
			}
			// 时间显示逻辑
			setShowTime(holder.senderTimeVoiceLeftView, position);
			holder.voiceLeftView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					onClickForVoice(position,holder,viewType);
				}
			});
			holder.voiceLeftView.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					// TODO Auto-generated method stub
					onItemLongClick(position);
					return false;
				}
			});
			break;
		case TYPE_TO_VOICE:
			holder.voiceLengthRightView.setText(message.getAttachment().getVoiceLength() + "''");
			if (mediaPlayer != null && mediaPlayer.isPlaying()) {
				if (playingUri.equals(MailChat.getInstance().getChatVoiceDirectory(mAccount) + EncryptUtil.getMd5(message.getAttachment().getAttchmentId())+ ".amr")) {
					if (animationDrawable != null && currentItemVoiceUid!=null) {
						startAnim(holder,viewType);
					}
				} else {
					stopAnim(holder,viewType);
				}
			}
			holder.voiceSendFailed.setVisibility(View.GONE);
			// 发送状态
			viewSendFailed(holder.voiceSendFailed,holder.voiceSending,message);
			// 时间显示逻辑
			setShowTime(holder.senderTimeVoiceRightView, position);
			holder.voiceRightView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					onClickForVoice(position,holder,viewType);
				}
			});
			holder.voiceRightView.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					// TODO Auto-generated method stub
					onItemLongClick(position);
					return false;
				}
			});
			break;
		case TYPE_FROM_ATTACHMENT:
			CAttachment cfAttachment  =message.getAttachment();
			holder.attIconLeftView.setImageBitmap(AttachmentUtil.getInstance(
					mContext).getAttachmentIcon(cfAttachment.getName(), false));
			holder.attNameLeftView.setText(cfAttachment.getName());
			setAttachmentItemView(holder, cfAttachment, TYPE_FROM_ATTACHMENT,false);
			holder.attSizeLeftView.setText(FileUtil.sizeLongToString(message
					.getAttachment().getSize()));
			// 时间显示逻辑
			setShowTime(holder.senderTimeAttLeftView, position);
			
			holder.attCancelLeftView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					MessagingController.getInstance(MailChat.getInstance()).cancelDownFile(mAccount,mMessages.get(position).getAttachment().getAttchmentId(),true);
				}
			});
			holder.attMenuLeftView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					onItemLongClick(position);
				}
			});
			break;
		case TYPE_TO_ATTACHMENT:
			CAttachment ctAttachment  =message.getAttachment();
			holder.attIconRightView.setImageBitmap(AttachmentUtil.getInstance(
					mContext).getAttachmentIcon(message.getAttachment().getName(), false));
			holder.attNameRightView.setText(message.getAttachment().getName());
			setAttachmentItemView(holder, ctAttachment, TYPE_TO_ATTACHMENT,message.getMessageState() == CMessage.State.sendFail);
			holder.attSizeRightView.setText(FileUtil.sizeLongToString(message
					.getAttachment().getSize()));
			holder.attSendFailed.setVisibility(View.GONE);
//			// 发送状态
			viewSendFailed(holder.attSendFailed,null,message);
			// 时间显示逻辑
			setShowTime(holder.senderTimeAttRightView, position);
			
			holder.attCancelRightView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(StringUtil.isEmpty(mMessages.get(position).getAttachment().getFilePath())){
						MessagingController.getInstance(MailChat.getInstance()).cancelDownFile(mAccount,mMessages.get(position).getAttachment().getAttchmentId(),true);
					}else{
						MessagingController.getInstance(MailChat.getInstance()).cancelUpFile(mAccount,mMessages.get(position).getAttachment().getAttchmentId());
					}
				}
			});
			
			holder.attMenuRightView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					onItemLongClick(position);
				}
			});
			break;
		case TYPE_FROM_NOTIFICATION:
			TextView notification = (TextView) convertView
					.findViewById(R.id.chatting_item_notification);
			LinearLayout layout = (LinearLayout) convertView
					.findViewById(R.id.notificationLayout);
			// layout.getBackground().setAlpha(100);
			notification.setText(message.getContent());
			break;
//		case TYPE_FROM_LOCATION:
//			String fontLeftLocationName;
//			if (message.getAddress() != null) {
//				fontLeftLocationName = message.getLocationName() + " "
//						+ "<font   color=\"#0190d9\">" + message.getAddress()
//						+ "</font>";
//			} else {
//				fontLeftLocationName = message.getLocationName();
//			}
//			// holder.locationNameLeftView.setText(getClickableSpan(getNickName(message,
//			// viewType),
//			// position));
//			holder.locationNameLeftView.setText(Html
//					.fromHtml(fontLeftLocationName));
//			// holder.locationNameLeftView.append(Html.fromHtml(fontLeftLocationName));
//			// holder.locationNameLeftView.setMovementMethod(TextViewFixTouchConsume.LocalLinkMovementMethod.getInstance());
//
//			setShowTime2(holder.senderTimeLocationLeftView, position);
//			break;
//		case TYPE_TO_LOCATION:
//			String fontRightLocationName;
//			if (message.getAddress() != null) {
//				fontRightLocationName = message.getLocationName() + " "
//						+ "<font   color=\"#0190d9\">" + message.getAddress()
//						+ "</font>";
//			} else {
//				fontRightLocationName = message.getLocationName();
//			}
//
//			// holder.locationNameRightView.setText(getClickableSpan(getNickName(message,
//			// viewType),
//			// position));
//			holder.locationNameRightView.setText(Html
//					.fromHtml(fontRightLocationName));
//			// holder.locationNameRightView.append(Html.fromHtml(fontRightLocationName));
//			// holder.locationNameRightView.setMovementMethod(TextViewFixTouchConsume.LocalLinkMovementMethod.getInstance());
//
//			// 发送状态
//			viewSendFailed(holder.locatioSendFailed, message);
//			setShowTime2(holder.senderTimeLocationRightView, position);
//			break;
		case TYPE_MAIL_INFO:
			String email = message.getMailFrom();
			String nickName =message.getMailFromNickName();
			if(StringUtil.isEmpty(nickName)){
				nickName =StringUtil.getPrdfixStr(email);
			}
			String subject =message.getMailSubject();
			holder.mailInfoText.setText(String.format(mContext.getString(R.string.mail_info_to_chat_subject), nickName,subject));
			break;
		}

		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				onItemClick(position, holder, viewType);
			}
		});

		convertView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				onItemLongClick(position);
				return false;
			}
		});
	}
	private void  onItemClick(int position, ChattingViewHolder holder,
			int viewType) {
		CMessage message = mMessages.get(position);
		String filePath = null;
		switch (message.getMessageType()) {
			case TEXT:
				break;
			case IMAGE:
				// 下载成功查看大图，否则重新下载预览图
				CAttachment cAttachment = message.getAttachment();
				String imageUrl =cAttachment.getFilePath();
				if(StringUtil.isEmpty(imageUrl)){
					imageUrl =Protocol.getInstance().getFileURL(cAttachment.getFileid(), cAttachment.getName(), true);
				}else{
					//本地路径
					imageUrl = "file://" +imageUrl;
				}
				File f = ImageLoader.getInstance().getDiskCache().get(imageUrl);
				cAttachment.setImageLoad(true);
				ImageView iv;
				if(viewType == TYPE_FROM_IMAGE){
					iv=holder.imageContentLeftView;
				}else{
					iv =holder.imageContentRightView;
				}
				messageController.updateCAttImageLoadStata(mAccount, cAttachment.getAttchmentId(), true, false);
				//f==null判断接收的图片是否存在，后面判断是因为转发的图片Fildid是一样的，导致imageUrl相同，所以要判断转发的view是否存在图片
				if(f==null||iv.getDrawable()==null){
					Toast.makeText(mContext, R.string.again_down_thumbnail_image,
							Toast.LENGTH_SHORT).show();
					ImageLoader.getInstance().displayImage(imageUrl,
							iv, initImageLoaderOptions(),
							new ImageLoadingListener() {
	
								@Override
								public void onLoadingStarted(String arg0, View arg1) {
									// TODO Auto-generated method stub
								}
	
								@Override
								public void onLoadingFailed(String arg0, View arg1,
										FailReason arg2) {
									// TODO Auto-generated method stub
									Toast.makeText(
											mContext,
											R.string.again_down_thumbnail_image_fail,
											Toast.LENGTH_SHORT).show();
								}
	
								@Override
								public void onLoadingComplete(String arg0,
										View arg1, Bitmap arg2) {
									// TODO Auto-generated method stub
									if (arg2 == null) {
										// 失败
										Toast.makeText(
												mContext,
												R.string.again_down_thumbnail_image_fail,
												Toast.LENGTH_SHORT).show();
									}
								}
	
								@Override
								public void onLoadingCancelled(String arg0,
										View arg1) {
									// TODO Auto-generated method stub
								}
							});
				}else{
					//进入大图查看
					List<CMessage> imageMessages=getImageMessages(mMessages);
					int currentItem = getCurrentItem(cAttachment.getAttchmentId(),imageMessages);
					ImageFullActivity.actionCGroupImageFullActivity(mContext, mAccount.getUuid(), imageMessages, currentItem);
					imageMessages=null;
				}
				break;
			case ATTACHMENT:
				CAttachment attach = message.getAttachment();
				filePath = attach.getFilePath();
				if(StringUtil.isEmpty(filePath)){
					filePath =MailChat.getInstance().getAttFilePath(MailChat.getInstance().getChatAttachmentDirectory(mAccount), attach.getAttchmentId(), attach.getName());
				}
				//TODO:删除本地文件后，进入聊天页面时遍历取数据库路径，查询本地文件是否存在，如果不存在更新该删除附件本地数据库保存路径为null，防止判断问题，重复下载。
				if (!new File(filePath).exists()) {
					/***** 检查是否有网络，如果没有网络，则提示用户 **/
					if (!NetUtil.isActive()) {
						NetUtil.showNoConnectedAlertDlg(mContext);
						return;
					}
					MessagingController.getInstance(MailChat.getInstance()).cGroupDownFile(mAccount, message);
				} else {
					// 打开
					FileUtil.viewFile(mContext, filePath, null,
							attach.getName());
				}
				break;
	//		case LOCATION:
	//			// 百度地理位置
	//			LogX.d("-message.getAddress()->"+message.getAddress());
	//			LogX.d("-message.getAddress()->"+message.getLocationName());
	//			if (message.getAddress() != null) {
	//				Intent intent = new Intent(mContext, MapActivity.class);
	//				intent.putExtra("x", message.getLongitude());
	//				intent.putExtra("y", message.getLatitude());
	//				intent.putExtra("name", message.getLocationName());
	//				intent.putExtra("addr", message.getAddress());
	//				mContext.startActivity(intent);
	//
	//			} else {
	////				googleMap
	//				Intent intent = new Intent(mContext, GoogleMapActivity.class);
	//				intent.putExtra("x", message.getLongitude());
	//				intent.putExtra("y", message.getLatitude());
	//				intent.putExtra("name", message.getLocationName());
	//				intent.putExtra("addr", message.getAddress());
	//				mContext.startActivity(intent);
	//			}
	//			break;
			case FROM_MAIL_INFO:
				ChatFromMailInfoActivity.actionChatFromMailInfo(mContext, message.getMailFrom(),message.getMailFromNickName(), message.getMailSubject(),message.getMailPreview());
				break;
			default:
				break;
		}
	}
	
	private void onItemLongClick(int position) {
		CMessage mMessage = mMessages.get(position);
		String filePath;
		// Integer[] items = null;
		// 发送失败,有重发,自己发的消息有删除
		List<Integer> items = new ArrayList<Integer>();
		switch (mMessage.getMessageType()) {
		case TEXT:
			if (mMessage.getMessageState() == State.sendFail) {
				items.add(ChattingShowDialog.SENDER);
			}
			items.add(ChattingShowDialog.FORWARD);
			items.add(ChattingShowDialog.COPY);
			items.add(ChattingShowDialog.DEL);
			break;
		case IMAGE:
			if (mMessage.getMessageState() == State.sendFail) {
				items.add(ChattingShowDialog.SENDER);
			}
			if(mMessage.getMember().getEmail().equals(mAccount.getEmail())){
				if(mMessage.getMessageState()== State.sendSuccess){
					items.add(ChattingShowDialog.FORWARD);
				}
			}else{
				items.add(ChattingShowDialog.FORWARD);
			}
			items.add(ChattingShowDialog.DEL);
			break;
		case VOICE:
			if (mMessage.getMessageState() == State.sendFail) {
				items.add(ChattingShowDialog.SENDER);
			}
			items.add(ChattingShowDialog.DEL);
			break;
		case ATTACHMENT:
			CAttachment cAttachment = mMessage.getAttachment();
			filePath=cAttachment.getFilePath();
			if(StringUtil.isEmpty(filePath)){
				filePath = MailChat.getInstance().getAttFilePath(MailChat.getInstance().getChatAttachmentDirectory(mAccount), cAttachment.getAttchmentId(), cAttachment.getName());
			}
			if (mMessage.getMessageState() == State.sendFail) {
				items.add(ChattingShowDialog.SENDER);
			}
			if(mMessage.getMember().getEmail().equals(mAccount.getEmail())){
				if(mMessage.getMessageState()== State.sendSuccess){
					items.add(ChattingShowDialog.FORWARD);
				}
			}else{
				items.add(ChattingShowDialog.FORWARD);
			}
			if (new File(filePath).exists()) {
				items.add(ChattingShowDialog.OPEN);
			}else{
				if(!cAttachment.isDownloadPause()){
					items.add(ChattingShowDialog.CANCEL_DOWNLOAD);
				}else{
					items.add(ChattingShowDialog.DOWNLOAD);
				}
			}
			items.add(ChattingShowDialog.DEL);
			break;
		case LOCATION:
			if (mMessage.getMessageState() == State.sendFail) {
				items.add(ChattingShowDialog.SENDER);
			}
			if (mMessage.getMember().getEmail().equals(mAccount.getEmail())) {
				items.add(ChattingShowDialog.DEL);
			}
			break;
		case NOTIFICATION:
			return;
		}
		alertOperation(items, mMessage);
	}
	private void alertOperation(List<Integer> items, CMessage mMessage) {
		if (items.size() > 0) {
			ChattingShowDialog dialog = new ChattingShowDialog(mContext, this,
					R.style.dialog, items, mMessage, mAccount);
			dialog.show();
		}
	}
	class ChattingViewHolder {

		TextView nameView;
		//头像
		RoundImageView headImageView;
		//消息时间
		TextView msgTimeView;
		// 左边文本框
		TextView senderTimeContentLeftView;
		TextViewFixTouchConsume textContentLeftView;
		// 右边文本框
		TextView senderTimeContentRightView;
		TextViewFixTouchConsume textContentRightView;
		ImageView textSendFailed;
		ProgressBar textSending;
		// 左边图片布局
		TextView senderTimeImageLeftView;
		ImageView imageContentLeftView;
		// 右边图片布局
		TextView senderTimeImageRightView;
		ImageView imageContentRightView;
		ImageView imageSendFailed;
		ProgressBar imageSending;
		// 左边音频布局
		RelativeLayout voiceLeftView;
		TextView senderTimeVoiceLeftView;
		ImageView voiceContentLeftView;
		ImageView voiceUnreadLeftView;
		TextView voiceLengthLeftView;
		// 右边音频布局
		RelativeLayout voiceRightView;
		TextView senderTimeVoiceRightView;
		ImageView voiceContentRightView;
		TextView voiceLengthRightView;
		ImageView voiceSendFailed;
		ProgressBar voiceSending;
		// 左边附件布局
		TextView senderTimeAttLeftView;
		ImageView attIconLeftView;
		TextView attNameLeftView;
		TextView attSizeLeftView;
		TextView attStatusLeftView;
		ProgressBar attProgressLeftView;
		RelativeLayout attLayoutprogressLeftView;
		ImageView attMenuLeftView;
		ImageView attCancelLeftView;
		// 右边附件布局
		TextView senderTimeAttRightView;
		ImageView attIconRightView;
		TextView attNameRightView;
		TextView attSizeRightView;
		TextView attStatusRightView;
		ImageView attSendFailed;
		ProgressBar attProgressRightView;
		RelativeLayout attLayoutprogressRightView;
		ImageView attMenuRightView;
		ImageView attCancelRightView;
		// 左边地理位置布局
		TextView senderTimeLocationLeftView;
		TextViewFixTouchConsume locationNameLeftView;

		// 右边地理位置布局
		TextView senderTimeLocationRightView;
		TextViewFixTouchConsume locationNameRightView;
		ImageView locatioSendFailed;

		//邮件透传
		TextView mailInfoText;

	}
	
	private String getNickName(CMessage message) {
		String name = null;
		String email =message.getMember().getEmail();
		if (email.equals(mAccount.getEmail())) {
			name = mContext.getString(R.string.to_me);
		} else {
			name = message.getMember().getNickName();
			if(name==null)
				name=email.substring(0, email.indexOf("@"));
		}
		return name;
	}
	private void setShowTime(TextView textTimeView, int position) {
		//当前消息时间
		long time = mMessages.get(position).getSendTime();
		//上一条消息时间
		long lastTime =-1;
		if (mMessages.size() > 1 && position > 0) {
			lastTime=mMessages.get(position - 1).getSendTime();
		}
		//时间判断
		if(lastTime==-1){//第一条消息
			textTimeView.setText(getLocaleDateTime(time));
		}else{
			if((time - lastTime)> 3*60*1000){//时间间隔
				textTimeView.setVisibility(View.VISIBLE);
				textTimeView.setText(getLocaleDateTime(time));
			}else{
				textTimeView.setVisibility(View.GONE);
			}
		}
	}
	/**
	 * 获取本地时间显示
	 *
	 * @Description:
	 * @param long time
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-8-7
	 */
	private String getLocaleDateTime(long time){
		Date date = new Date(time);
		//当天0点时间
		long dayStartTime = TimeUtils.getTimesmorning();
		//当天24点时间
		long dayEndTime = TimeUtils.getTimesnight();
		//昨天0点时间
		long yesterdayStartTime = dayStartTime-86400000;
		//昨天24点时间
		long yesterdayEndtTime = dayEndTime-86400000;
		//这周一的时间
		long weekStartTime = TimeUtils.getTimesWeekmorning();
		String timeTxt ="";
		String language = MailChat.application.getLanguage();
		String timeShow = DateFormat.is24HourFormat(mContext) ? TimeUtils.DateFormatHM.format(date) : TimeUtils.DateFormatHHMM.format(date);
		if(language.equals(Locale.SIMPLIFIED_CHINESE.toString())||language.equals(Locale.TAIWAN.toString())){
			if(time >= dayStartTime && time < dayEndTime){//今天
				timeTxt = "今天 " + timeShow;
			}else if(time >= yesterdayStartTime && time < yesterdayEndtTime){//昨天
				timeTxt = "昨天 " + timeShow;
			}else if(time<yesterdayStartTime&&time>=weekStartTime){//一个星期内
				timeTxt = TimeUtils.DateFormatEEE.format(date) +" "+ timeShow;
			}else{//一个星期外
				timeTxt = TimeUtils.DataFormatCHINESYYMMDDHHMM.format(date);
			}
		}else{
			if(time >= dayStartTime && time < dayEndTime){//今天
				timeTxt = timeShow;
			}else if(time >= yesterdayStartTime && time < yesterdayEndtTime){//昨天
				timeTxt = "Yesterday " + timeShow;
			}else if(time<yesterdayStartTime&&time>=weekStartTime){//一个星期内
				timeTxt = TimeUtils.DateFormatEEE.format(date) +" "+ timeShow;
			}else{//一个星期外
				timeTxt = TimeUtils.DateFormatYMDHMS.format(date);
			}
		}
		return timeTxt;
	}
	private void viewSendFailed(ImageView imageView,ProgressBar progressBar, final CMessage message) {
		State state = message.getMessageState();
		if (state != null) {
			switch (state) {
			case sending:
				imageView.setVisibility(View.GONE);
				if(progressBar!=null){
					progressBar.setVisibility(View.VISIBLE);
				}
				break;
			case sendSuccess:
				imageView.setVisibility(View.GONE);
				if(progressBar!=null){
					progressBar.setVisibility(View.GONE);
				}
				break;
			case sendFail:
				imageView.setVisibility(View.VISIBLE);
				if(progressBar!=null){
					progressBar.setVisibility(View.GONE);
				}
				break;
			}
		} else {
			imageView.setVisibility(View.GONE);
		}

		imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showSenderMessageAgainDlg(message);
			}
		});
	}
	
	public void addOrUpdateMessages(List<CMessage> messages) {
		if (messages != null) {
			/**
			 * TODO:过滤重复
			 */
			mMessages.addAll(removeDuplicateWithOrder(messages));
			for(int i=0;i<mMessages.size()-1;i++){
				for (int j = i + 1; j < mMessages.size(); j++) {
					if (mMessages.get(i).getSendTime() > mMessages.get(j).getSendTime()) {
						Collections.swap(mMessages, i, j);
					}
				}
			}
			notifyDataSetChanged();
		}
	}
	
	public List<CMessage> removeDuplicateWithOrder(List<CMessage> cMessageList) {

		Set<CMessage> set = new HashSet<CMessage>();
		List<CMessage> newList = new ArrayList<CMessage>();
		for (Iterator<CMessage> iter = cMessageList.iterator(); iter.hasNext();) {
			CMessage cMessage = iter.next();
			if (set.add(cMessage))
				newList.add(cMessage);
		}
		set = null;
		return newList;
	}
	
	/**
	 * 更新消息
	 * 
	 * @Description:
	 * @param originalMessageUid
	 * @param message
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2014-4-3
	 */
	public void updateMessageSendState(String originalMessageUid,boolean isSendSuccess) {
		for(CMessage msg :mMessages){
			if (msg.getUid().equals(originalMessageUid)) {
				if(isSendSuccess){
					msg.setMessageState(State.sendSuccess);
				}else{
					msg.setMessageState(State.sendFail);
				}
				break;
			}
		}
		notifyDataSetChanged();
	}
	
	public void updateMessageTime(String originalMessageUid,long time) {
		for(CMessage msg :mMessages){
			if (msg.getUid().equals(originalMessageUid)) {
				msg.setSendTime(time);
				break;
			}
		}
		notifyDataSetChanged();
	}
	
	public void updateMessages(List<CMessage> messages) {
		mMessages = messages;
		notifyDataSetChanged();
	}
	/**
	 * 加入新消息
	 * 
	 * @Description:
	 * @param message
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2014-3-26
	 */
	public void addOrUpdateMessage(CMessage message) {
		for (CMessage msg : mMessages) {
			if (msg.getUid().equals(message.getUid())) {
				return;
			}
		}
		mMessages.add(message);
		for(int i=0;i<mMessages.size()-1;i++){
			for (int j = i + 1; j < mMessages.size(); j++) {
				if (mMessages.get(i).getSendTime() > mMessages.get(j).getSendTime()) {
					Collections.swap(mMessages, i, j);
				}
			}
		}
		if (mMessages != null && !mMessages.isEmpty()) {
			lastMessageSendTime = mMessages.get(mMessages.size() - 1)
					.getSendTime();
		}
		notifyDataSetChanged();
		// 播放提示音
//		if (mGroup.getIsMessageVoiceReminder() != null) {
//			if (mGroup.getIsMessageVoiceReminder()
//					&& ringtone != null
//					&& !message.getMember().getEmail()
//							.equals(mAccount.getEmail())
//					&& GlobalSettingsConfig.getInstance()
//							.parse(new GlobalSettings()).isNotifyRingtone()) {
//				ringtone.play();
//			}
//		}
	}

	private void showSenderMessageAgainDlg(final CMessage message) {
		MailDialog.Builder builder = new MailDialog.Builder(mContext);
		builder.setTitle(mContext.getString(R.string.operate_notice));
		builder.setMessage(mContext.getString(R.string.sender_message_again));
		builder.setPositiveButton(mContext.getString(R.string.okay_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						// 重新发送
						senderMessageAgain(message);
					}

				}).setNeutralButton(mContext.getString(R.string.cancel_action),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}
	public void deleteMessage(String uid) {
		messageController.deleteGroupMessage(mAccount, uid);
		((ChattingActivity) mContext).setTranscriptModeDisabled();
		for (CMessage message : mMessages) {
			if (message.getUid().equals(uid)) {
				mMessages.remove(message);
				if (mMessages.size() > 0) {
					messageController.updateCgroupLastCmessage(
							mAccount, mGroup.getUid(),
							mMessages.get(mMessages.size() - 1));
				}
				notifyDataSetChanged();
				break;
			}
		}
		if (mMessages.size() == 0) {
			messageController.updateCgroupLastCmessage(mAccount,
					mGroup.getUid(), null);
			notifyDataSetChanged();
		}
		((ChattingActivity) mContext).setTranscriptModeScroll();
	}
	public void deleteMessageForAtt(String attUid) {
		String msgUid =null;
		((ChattingActivity) mContext).setTranscriptModeDisabled();
		for (CMessage message : mMessages) {
			CAttachment cAttachment =message.getAttachment();
			if(cAttachment!=null&&attUid.equals(cAttachment.getAttchmentId())){
				msgUid=message.getUid();
				mMessages.remove(message);
				if (mMessages.size() > 0) {
					messageController.updateCgroupLastCmessage(mAccount,
							mGroup.getUid(),
							mMessages.get(mMessages.size() - 1));
				}
				notifyDataSetChanged();
				break;
			}
		}
		if (mMessages.size() == 0) {
			messageController.updateCgroupLastCmessage(mAccount,
					mGroup.getUid(), null);
			notifyDataSetChanged();
		}
		((ChattingActivity) mContext).setTranscriptModeScroll();
		if(msgUid!=null){
			messageController.deleteGroupMessage(mAccount, msgUid);
		}
	}
	public void senderMessageAgain(CMessage message) {
		// TODO Auto-generated method stub
		message.setMessageState(State.sending);
		message.setSendTime(System.currentTimeMillis());
		notifyDataSetChanged();
		MessagingController.getInstance(MailChat.app).publishCMessage(
				mGroup.getUid(), message, mAccount, null);
	}
	@Override
	public void onClick(View v) {
		int index = (Integer) v.getTag();
		CMessage message = mMessages.get(index);
		switch (v.getId()) {
		case R.id.chatting_item_head:// //有内部控件进行点击，那么再此进行分支
			CGroupMember member = message.getMember();
			ContactAttribute contact = new ContactAttribute();
			contact.setEmail(member.getEmail());
			contact.setNickName(member.getNickName());
			String avatarHash = member.getAvatarHash();
			contact.setImgHeadHash(avatarHash);
			if(avatarHash!=null){
				contact.setImgHeadPath(GlobalConstants.HOST_IMG+member.getAvatarHash());
			}
			messageController.get35EisContactsInfoForJump(mAccount, contact,true);
			break;
		default:
			break;
		}
	}
	
	public List<CMessage> getcMessages() {
		return mMessages;
	}
	
	private List<CMessage> getImageMessages(List<CMessage> mMessages){
		List<CMessage> imageMessages =new ArrayList<CMessage>();
		for(CMessage cMessage : mMessages){
			if(cMessage.getMessageType().equals(CMessage.Type.IMAGE)){
				imageMessages.add(cMessage);
			}
		}
		return imageMessages;
	}
	private int getCurrentItem(String imageUid,List<CMessage> imageMessages){
		int currentItem=0;
		for (int i = 0; i < imageMessages.size(); i++) {
			if(imageMessages.get(i).getAttachment().getAttchmentId().equals(imageUid)){
				currentItem=i;
			}
		}
		return currentItem;
	}
	
	/**
	 * 开始播放
	 */
	private void startPlay(String path) {
		try {
			mediaPlayer.reset();
			mediaPlayer.setDataSource(path);
			mediaPlayer.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mediaPlayer.start();
	}

	public void stopPlay() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
			// mediaPlayer.release();
			// playingPosition = -1;
			playingUri = "";
			stopAnim();
			currentItemVoiceUid=null;
		}
	}
	
	private void startAnim(ChattingViewHolder holder,int viewType) {
		if (viewType == TYPE_FROM_VOICE) {
			voiceView = holder.voiceContentLeftView;
			voiceView.setImageResource(R.drawable.anim_left_voice);
		} else {
			voiceView = holder.voiceContentRightView;
			voiceView.setImageResource(R.drawable.anim_right_voice);
		}
		animationDrawable = (AnimationDrawable) voiceView.getDrawable();
		animationDrawable.start();
	}

	private void stopAnim() {
		if (animationDrawable != null && animationDrawable.isRunning()) {
			animationDrawable.stop();
			if (viewType == TYPE_FROM_VOICE) {
				voiceView
						.setImageResource(R.drawable.bg_chatting_from_voice_playing003);
			} else {
				voiceView.setImageResource(R.drawable.icon_chatting_to_voice);
			}

		}
	}

	private void stopAnim(ChattingViewHolder holder,int viewType) {
		if (viewType == TYPE_FROM_VOICE) {
			holder.voiceContentLeftView.setImageResource(R.drawable.bg_chatting_from_voice_playing003);
		} else {
			holder.voiceContentRightView.setImageResource(R.drawable.icon_chatting_to_voice);
		}
	}

	public void stopPlay(String voiceUuid) {
		if (mediaPlayer != null && mediaPlayer.isPlaying()&&
				!StringUtil.isEmpty(currentItemVoiceUid)&&currentItemVoiceUid.equals(voiceUuid)) {
			mediaPlayer.stop();
			playingUri = "";
			stopAnim();
			currentItemVoiceUid=null;
		}
	}

	public void updateView(int itemIndex) {
		// 得到第一个可显示控件的位置，
		int firstVisiblePosition = mGroupChattingView.getFirstVisiblePosition();
		int lastVisiblePosition = mGroupChattingView.getLastVisiblePosition();
		// 只有当要更新的view在可见的位置时才更新，不可见时，跳过不更新
		if (itemIndex >= firstVisiblePosition
				&& itemIndex <= lastVisiblePosition) {
			// 得到要更新的item的view
			View view = mGroupChattingView.getChildAt(itemIndex- firstVisiblePosition);
			// 更新进度条
			// 从view中取得holder
			ChattingViewHolder holder = (ChattingViewHolder) view.getTag();
			CMessage message = mMessages.get(itemIndex);
			boolean isTo = mAccount.getEmail().equals(
					message.getMember().getEmail());
			int type = 0;
			CAttachment cAttachment =null;
			switch (message.getMessageType()) {
			case ATTACHMENT:
				type = isTo ? TYPE_TO_ATTACHMENT : TYPE_FROM_ATTACHMENT;
				cAttachment=message.getAttachment();
				setAttachmentItemView(holder, cAttachment, type,message.getMessageState() == CMessage.State.sendFail);
				break;
			default:
				return;
			}
		}
	}
	
	private void setAttachmentItemView(ChattingViewHolder holder,CAttachment cAttachment,int type,boolean isSendFailed){
		if (type == TYPE_FROM_ATTACHMENT) {
			int progress = cAttachment.getDownloadProgress();
			String filePath = MailChat.getInstance().getAttFilePath(MailChat.getInstance().getChatAttachmentDirectory(mAccount), cAttachment.getAttchmentId(), cAttachment.getName());
			if (new File(filePath).exists()) {
				holder.attLayoutprogressLeftView.setVisibility(View.GONE);
				holder.attStatusLeftView.setText(R.string.chatting_att_download_finished_open);
				holder.attCancelLeftView.setVisibility(View.GONE);
				holder.attMenuLeftView.setVisibility(View.VISIBLE);
			} else {
				if (!cAttachment.isDownloadPause()) {
					if(progress>0&&progress<100){
						holder.attLayoutprogressLeftView.setVisibility(View.VISIBLE);
						holder.attProgressLeftView.setProgress(progress);
						holder.attStatusLeftView.setText(R.string.chatting_att_downloading);
						holder.attCancelLeftView.setVisibility(View.VISIBLE);
						holder.attMenuLeftView.setVisibility(View.GONE);
					}else if(progress==0||progress==100){
						holder.attLayoutprogressLeftView.setVisibility(View.GONE);
						holder.attStatusLeftView.setText("");
						holder.attCancelLeftView.setVisibility(View.GONE);
						holder.attMenuLeftView.setVisibility(View.VISIBLE);
					}else{
						holder.attLayoutprogressLeftView.setVisibility(View.VISIBLE);
						holder.attProgressLeftView.setProgress(0);
						holder.attStatusLeftView.setText(R.string.chatting_att_downloading);
						holder.attCancelLeftView.setVisibility(View.VISIBLE);
						holder.attMenuLeftView.setVisibility(View.GONE);
					}
				}else{
					if(progress==0){
						holder.attLayoutprogressLeftView.setVisibility(View.GONE);
						holder.attStatusLeftView.setText("");
						holder.attCancelLeftView.setVisibility(View.GONE);
						holder.attMenuLeftView.setVisibility(View.VISIBLE);
					}else{
						holder.attLayoutprogressLeftView.setVisibility(View.VISIBLE);
						holder.attProgressLeftView.setProgress(progress);
						holder.attStatusLeftView.setText(R.string.chatting_att_download_pause);
						holder.attCancelLeftView.setVisibility(View.GONE);
						holder.attMenuLeftView.setVisibility(View.VISIBLE);
					}
				}
			}
		} else if (type == TYPE_TO_ATTACHMENT) {
			int progress ;
			String filePath=cAttachment.getFilePath();
			String downFilePath = MailChat.getInstance().getAttFilePath(MailChat.getInstance().getChatAttachmentDirectory(mAccount), cAttachment.getAttchmentId(), cAttachment.getName());
			if(StringUtil.isEmpty(filePath)){
				filePath =downFilePath;
				progress = cAttachment.getDownloadProgress();
				if (new File(filePath).exists()) {
					holder.attLayoutprogressRightView.setVisibility(View.GONE);
					holder.attStatusRightView.setText(R.string.chatting_att_open);
					holder.attCancelRightView.setVisibility(View.GONE);
					holder.attMenuRightView.setVisibility(View.VISIBLE);
				} else {
					if (!cAttachment.isDownloadPause()) {
						if(progress>0&&progress<100){
							holder.attLayoutprogressRightView.setVisibility(View.VISIBLE);
							holder.attProgressRightView.setProgress(progress);
							holder.attProgressRightView.setSecondaryProgress(progress);
							holder.attStatusRightView.setText(R.string.chatting_att_downloading);
							holder.attCancelRightView.setVisibility(View.VISIBLE);
							holder.attMenuRightView.setVisibility(View.GONE);
						}else if(progress==0||progress==100){
							holder.attLayoutprogressRightView.setVisibility(View.GONE);
							holder.attStatusRightView.setText("");
							holder.attCancelRightView.setVisibility(View.GONE);
							holder.attMenuRightView.setVisibility(View.VISIBLE);
						}else{
							holder.attLayoutprogressRightView.setVisibility(View.VISIBLE);
							holder.attProgressRightView.setProgress(0);
							holder.attProgressRightView.setSecondaryProgress(0);
							holder.attStatusRightView.setText(R.string.chatting_att_downloading);
							holder.attCancelRightView.setVisibility(View.VISIBLE);
							holder.attMenuRightView.setVisibility(View.GONE);
						}
					}else{
						if(progress==0){
							holder.attLayoutprogressRightView.setVisibility(View.GONE);
							holder.attStatusRightView.setText("");
							holder.attCancelRightView.setVisibility(View.GONE);
							holder.attMenuRightView.setVisibility(View.VISIBLE);
						}else{
							holder.attLayoutprogressRightView.setVisibility(View.VISIBLE);
							holder.attProgressRightView.setProgress(progress);
							holder.attProgressRightView.setSecondaryProgress(progress);
							holder.attStatusRightView.setText(R.string.chatting_att_download_pause);
							holder.attCancelRightView.setVisibility(View.GONE);
							holder.attMenuRightView.setVisibility(View.VISIBLE);
						}
					}
				}
			}else {
				progress =cAttachment.getUploadProgress();
				if(progress>0&&progress<100){
					holder.attStatusRightView.setText(R.string.chatting_att_uploading);
					holder.attLayoutprogressRightView.setVisibility(View.VISIBLE);
					holder.attProgressRightView.setProgress(progress);
					holder.attProgressRightView.setSecondaryProgress(progress);
					holder.attCancelRightView.setVisibility(View.VISIBLE);
					holder.attMenuRightView.setVisibility(View.GONE);
				}else if(progress==0||progress==100){
					holder.attLayoutprogressRightView.setVisibility(View.GONE);
					holder.attProgressRightView.setProgress(0);
					holder.attProgressRightView.setSecondaryProgress(0);
					holder.attStatusRightView.setText(R.string.chatting_att_open);
					holder.attCancelRightView.setVisibility(View.GONE);
					holder.attMenuRightView.setVisibility(View.VISIBLE);
				}else{
					if(isSendFailed){
						holder.attLayoutprogressRightView.setVisibility(View.GONE);
						holder.attProgressRightView.setProgress(0);
						holder.attProgressRightView.setSecondaryProgress(0);
						holder.attStatusRightView.setText(R.string.upload_failed);
						holder.attCancelRightView.setVisibility(View.GONE);
						holder.attMenuRightView.setVisibility(View.VISIBLE);
					}else{
						holder.attStatusRightView.setText(R.string.chatting_att_uploading);
						holder.attLayoutprogressRightView.setVisibility(View.VISIBLE);
						holder.attProgressRightView.setProgress(0);
						holder.attProgressRightView.setSecondaryProgress(0);
						holder.attCancelRightView.setVisibility(View.VISIBLE);
						holder.attMenuRightView.setVisibility(View.GONE);
					}
				}
			}
		}
	}

	private void onClickForVoice(int position,ChattingViewHolder holder,int viewType){
		CMessage message = mMessages.get(position);
		// 播放
		CAttachment attachment = message.getAttachment();
		String path = MailChat.getInstance().getChatVoiceDirectory(mAccount) + EncryptUtil.getMd5(attachment.getAttchmentId())+ ".amr";
		// 正在播放，停止
		if (playingUri.equals(path)) {
			stopPlay();
			return;
		}
		stopAnim();
		// 开始播放
		if (!StringUtil.isEmpty(path) && new File(path).exists()) {
			currentItemVoiceUid=attachment.getAttchmentId();
			this.viewType = viewType;
			startAnim(holder,viewType);
			startPlay(path);
			playingUri = path;

			if (attachment.getReadFlag() == 0
					&& viewType == TYPE_FROM_VOICE) {
				MessagingController.getInstance(MailChat.getInstance()).updateCAttachmentReadState(
						mAccount, attachment.getAttchmentId(), 1);
				attachment.setReadFlag(1);
				holder.voiceUnreadLeftView.setVisibility(View.GONE);
			}
		}
	}
	/**
	 * 根据发送大图尺寸，设置图片尺寸UI宽高
	 * 
	 * @Description:
	 * @param ImageView
	 * @param width
	 * @param height
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2016-2-24
	 */
	private void setImageUI(ImageView iv,int width,int height){
		int maxWidth = 200;
		int maxHeight = 300;
		LayoutParams lp = iv.getLayoutParams();
		//兼容以前版本，当以前版发送不带尺寸时。
		if(width == 0 || height == 0){
			width = maxWidth;
			height = maxHeight;
		}
		if(width>maxWidth || height>maxHeight){
			if(width>height){
				float scaled = (float)height/width;
				width =maxWidth;
				height=(int) (width * scaled);
			}else{
				float scaled = (float)width/height;
				height = maxHeight;
				width = (int) (height * scaled);
			}
		}
		lp.width = width;
		lp.height = height;
		iv.setLayoutParams(lp);
	}

	/**
	 * ImageLoaderOptions
	 * @Description:
	 * @param isCacheInMemory 
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2016-2-26
	 */
	private DisplayImageOptions initImageLoaderOptions() {
		DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisk(true).showImageOnLoading(R.drawable.no_image).bitmapConfig(Bitmap.Config.RGB_565).build();
		return options;
    }
}
