package cn.mailchat.adapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.support.v4.util.LruCache;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewGroup;
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
import cn.mailchat.activity.ChattingSingleActivity;
import cn.mailchat.activity.ContactInfoActivity;
import cn.mailchat.activity.ImageFullActivity;
import cn.mailchat.activity.WebViewWithErrorViewActivity;
import cn.mailchat.chatting.beans.CAttachment;
import cn.mailchat.chatting.beans.DAttachment;
import cn.mailchat.chatting.beans.DChat;
import cn.mailchat.chatting.beans.DChatMessage;
import cn.mailchat.chatting.protocol.Protocol;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.helper.StringUtils;
import cn.mailchat.surprise.BoardInfoProvider;
import cn.mailchat.utils.AttachmentUtil;
import cn.mailchat.utils.DateUtil;
import cn.mailchat.utils.EncryptUtil;
import cn.mailchat.utils.FaceConversionUtil;
import cn.mailchat.utils.FileUtil;
import cn.mailchat.utils.GlobalTools;
import cn.mailchat.utils.ImageUtils;
import cn.mailchat.utils.NetUtil;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.utils.TimeUtils;
import cn.mailchat.utils.Utility;
import cn.mailchat.view.ChattingShowDialog;
import cn.mailchat.view.MailDialog;
import cn.mailchat.view.RoundImageView;
import cn.mailchat.view.TextViewFixTouchConsume;

public class DChattingAdapter extends BaseAdapter implements OnClickListener, BoardInfoProvider{

	private Context mContext;
	private Account mAccount;
	private List<DChatMessage> mDChatMessages;
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
	private static final int TYPE_FROM_OA =11;
	private static final int TYPE_MAIL_INFO =12;
	private static final int TYPE_COUNT = 13;

	private MediaPlayer mediaPlayer;
	private String playingUri = "";

	public AnimationDrawable animationDrawable;
	private ImageView voiceView;
	private int viewType;

	// 图片缓存
	private LruCache<String, Bitmap> mMemoryCache;
	// 地理位置logo
	private Drawable locationDrawable;

	private ListView mSingleChattingView;

	// 声音提示
	private Ringtone ringtone;
	// 界面中最新一条数据的发送时间
	private int position;
	private	MessagingController messageController;
	private String nickName;

	private String toEmailImgHeadHash;

	private String currentItemVoiceUid;

	private DChat dChat;

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public DChattingAdapter(Context context, ListView mSingleChattingView,
			Account account, List<DChatMessage> messages,
			final MediaPlayer mediaPlayer,DChat dChat) {
		messageController=MessagingController.getInstance(MailChat.app);
		this.mContext = context;
		this.mAccount = account;
		this.dChat=dChat;
		this.nickName= dChat.getNickName();
		this.toEmailImgHeadHash=dChat.getImgHeadHash();
		this.mSingleChattingView = mSingleChattingView;
		if (messages == null) {
			mDChatMessages = new ArrayList<DChatMessage>();
		} else {
			mDChatMessages = messages;
		}
		this.mediaPlayer = mediaPlayer;

		mInflater = LayoutInflater.from(mContext);

		locationDrawable = mContext.getResources().getDrawable(
				R.drawable.icon_item_location);
		locationDrawable.setBounds(0, 0, locationDrawable.getIntrinsicWidth(),
				locationDrawable.getIntrinsicHeight());

		ringtone = RingtoneManager.getRingtone(mContext, RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		mMemoryCache = ((MailChat) MailChat.app).getmMemoryCache();
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer arg0) {
				// mediaPlayer.release();
				mediaPlayer.reset();
				playingUri = "";
				stopAnim();
			}
		});

		mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
				// playingPosition = -1;
				playingUri = "";
				stopAnim();

				try {
					mediaPlayer.reset();
					// mediaPlayer.release();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}
		});
	}

	public void addOrUpdateMessages(List<DChatMessage> messages) {
		if (messages != null) {
			mDChatMessages.addAll(messages);
			for(int i=0;i<mDChatMessages.size()-1;i++){
				for (int j = i + 1; j < mDChatMessages.size(); j++) {
					if (mDChatMessages.get(i).getTime() > mDChatMessages.get(j).getTime()) {
						Collections.swap(mDChatMessages, i, j);
					}
				}
			}
			notifyDataSetChanged();
		}
	}

	public void removeMessage(DChatMessage message) {
		if (mDChatMessages != null && mDChatMessages.contains(message)) {
			mDChatMessages.remove(message);
		}
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
	public void updateMessage(String originalMessageUid,boolean isSendSuccess) {
		for(DChatMessage msg : mDChatMessages){
			if (msg.getUuid().equals(originalMessageUid)) {
				if(isSendSuccess){
					msg.setMessageState(0);
				}else{
					msg.setMessageState(1);
				}
				break;
			}
		}
		notifyDataSetChanged();
	}
	
	public void updateMessageTime(String originalMessageUid,long time) {
		for(DChatMessage msg : mDChatMessages){
			if (msg.getUuid().equals(originalMessageUid)) {
				msg.setTime(time);
				break;
			}
		}
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
	public void addOrUpdateMessage(DChatMessage message) {
		for (DChatMessage msg : mDChatMessages) {
			if (msg.getUuid().equals(message.getUuid())) {
				return;
			}
		}
		mDChatMessages.add(message);
		for(int i=0;i<mDChatMessages.size()-1;i++){
			for (int j = i + 1; j < mDChatMessages.size(); j++) {
				if (mDChatMessages.get(i).getTime() > mDChatMessages.get(j).getTime()) {
					Collections.swap(mDChatMessages, i, j);
				}
			}
		}
		notifyDataSetChanged();
		// 播放提示音
		// if (mDChat.getIsMessageVoiceReminder() != null) {
		// if (mDChat.getIsMessageVoiceReminder()
		// && ringtone != null
		// && !message.getMember().getEmail()
		// .equals(mAccount.getEmail())
		// && GlobalSettingsConfig.getInstance()
		// .parse(new GlobalSettings()).isNotifyRingtone()) {
		// ringtone.play();
		// }
		// }
	}

	/**
	 * 
	 * method name: updateMessage function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param messages
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-8-22 下午3:17:34 @Modified by：zhangjx
	 * @Description：有消息来的时候更新
	 */
	public void updateMessage(List<DChatMessage> messages) {
		mDChatMessages = messages;
		notifyDataSetChanged();
	}
	@Override
	public int getCount() {
		return mDChatMessages.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public int getViewTypeCount() {
		return TYPE_COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		int type = 0;
		DChatMessage message = mDChatMessages.get(position);
		boolean isTo = mAccount.getEmail().equals(message.getSenderEmail());
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
		case OA_ANNOUNCE:
		case OA_NEW_TRANS:
			type = TYPE_FROM_OA;
			break;
		case FROM_MAIL_INFO:
			type = TYPE_MAIL_INFO;
		default:
			break;
		}
		return type;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
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
	
	public Map<Integer, View> indexViewMap = new ConcurrentHashMap<>();
	
	private void removeOldViewBindIndex (View view){
		if (indexViewMap.containsValue(view)){
			Set<Integer> keySet = indexViewMap.keySet();
			for ( Integer key : keySet ){
				if (indexViewMap.get(key) == view){
					indexViewMap.remove(key);
				}
			}
		}
	}
	
	private void bindView(final ChattingViewHolder holder, final View convertView,
			final int position, final int viewType) {
		
		removeOldViewBindIndex(convertView);
		indexViewMap.put(position, convertView);
		
		DChatMessage message = mDChatMessages.get(position);
		if (holder.nameView != null) {
			holder.headImageView.setTag(position);
			holder.headImageView.setOnClickListener(this);
			String userHeadUrl="";
			String userNickName="";
			switch (message.getMessageType()) {
			case OA_ANNOUNCE:

			case OA_NEW_TRANS:
				userNickName = message.getNickName();
				if(TextUtils.isEmpty(userNickName)){
					userNickName = message.getOAFrom();
					userNickName = StringUtil.getInfoNickName(userNickName);
				}
				String userHeadHash = message.getImgHeadHash();
				if(!TextUtils.isEmpty(userHeadHash)){
					userHeadUrl = GlobalConstants.HOST_IMG+userHeadHash
							+ GlobalConstants.USER_SMALL_HEAD_END;
				}
				break;
			default:
				userNickName= getNickName(message);
				if (message.getSenderEmail().equals(mAccount.getEmail())) {
					String accountBigHeadImg = mAccount.getAccountBigHeadImg();
					if (!TextUtils.isEmpty(accountBigHeadImg)) {
						userHeadUrl = accountBigHeadImg+GlobalConstants.USER_SMALL_HEAD_END;
					}
				} else {
					if (!TextUtils.isEmpty(toEmailImgHeadHash)) {
						userHeadUrl =GlobalConstants.HOST_IMG+toEmailImgHeadHash
								+ GlobalConstants.USER_SMALL_HEAD_END;
					}
				}
				break;
			}
			final String nickName = userNickName;
			ImageLoader.getInstance().displayImage(userHeadUrl,
					holder.headImageView,
					MailChat.getInstance().initImageLoaderOptions(),
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
						public void onLoadingComplete(String arg0, View arg1,
								Bitmap arg2) {
							// TODO Auto-generated method stub
							if (arg2 == null) {
								holder.headImageView.setImageBitmap(ImageUtils
										.getUserFirstTextBitmap(mContext,
												nickName));
							}
						}

						@Override
						public void onLoadingCancelled(String arg0, View arg1) {
							// TODO Auto-generated method stub

						}
					});
			String msgTime="";
			if(message.getMessageState()==0){
				msgTime=TimeUtils.DateFormatHM.format(new Date(message.getTime()));
			}
			holder.nameView.setText(nickName);
			//holder.msgTimeView.setText(msgTime);
		}
		switch (viewType) {
		case TYPE_FROM_TEXT:
			if (!StringUtil.isEmpty(message.getMessageContent())) {
				String msgContent = message.getMessageContent();
				SpannableString spannableString = FaceConversionUtil
						.getInstace().getExpressionString(mContext, msgContent);
				holder.textContentLeftView.setText(spannableString);
				// holder.textContentLeftView.setText(getClickableSpan(getNickName(message,
				// viewType),
				// position));
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
			if (!StringUtil.isEmpty(message.getMessageContent())) {
				SpannableString spannableString2 = FaceConversionUtil
						.getInstace().getExpressionString(mContext,
								message.getMessageContent());
				holder.textContentRightView.setText(spannableString2);
				// holder.textContentRightView.setText(getClickableSpan(getNickName(message,
				// viewType),
				// position));	
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
				//WebViewActivity.extractUrl2Link(holder.textContentRightView);
			} else {
				holder.textContentRightView.setText("");
			}

			// 发送状态
			viewSendFailed(holder.textSendFailed,holder.textSending, message);

			// 时间显示逻辑
			setShowTime(holder.senderTimeContentRightView, position);
			break;
		case TYPE_FROM_IMAGE:
			DAttachment dAttachment = message.getAttachments().get(0);	
			String imageUrl = Protocol.getInstance().getFileURL(dAttachment.getFileid(), dAttachment.getName(), true);
			setImageUI(holder.imageContentLeftView, dAttachment.getImageWidth(), dAttachment.getImageHeight());
			if(dAttachment.isImageLoad()){
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
			String filePaht = message.getAttachments().get(0).getFilePath();
			if(StringUtil.isEmpty(filePaht)){//转发别人发的
				//imageLoader识别的url
				imageUrl2 = Protocol.getInstance().getFileURL(message.getAttachments().get(0).getFileid(), message.getAttachments().get(0).getName(), true);
			}else{
				//本地路径
				imageUrl2 = "file://" +filePaht;
			}
			setImageUI(holder.imageContentRightView, message.getAttachments().get(0).getImageWidth(), message.getAttachments().get(0).getImageHeight());
			//清理缓存是否下载判断，或者本地图片
			if(message.getAttachments().get(0).isImageLoad()||imageUrl2.contains("file://")){

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
			viewSendFailed(holder.imageSendFailed, holder.imageSending,message);
			// 时间显示逻辑
			setShowTime(holder.senderTimeImageRightView, position);

			break;
			
		case TYPE_FROM_VOICE:
			holder.voiceLengthLeftView.setText(message.getAttachments().get(0)
					.getVoiceLength() + "''");
			if (message.getAttachments().get(0).getReadFlag() == 0) {
				holder.voiceUnreadLeftView.setVisibility(View.VISIBLE);
			} else {
				holder.voiceUnreadLeftView.setVisibility(View.GONE);
			}
			if (mediaPlayer != null && mediaPlayer.isPlaying()) {
				if (playingUri.equals(MailChat.getInstance().getChatVoiceDirectory(mAccount) + EncryptUtil.getMd5(message.getAttachments().get(0).getAttchmentId())+ ".amr")) {
					if (animationDrawable != null&& currentItemVoiceUid!=null) {
						startAnim(holder,TYPE_FROM_VOICE);
					}
				} else {
					stopAnim(holder,TYPE_FROM_VOICE);
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
			holder.voiceLengthRightView.setText(message.getAttachments().get(0)
					.getVoiceLength() + "''");
			if (mediaPlayer != null && mediaPlayer.isPlaying()) {
				if (playingUri.equals(MailChat.getInstance().getChatVoiceDirectory(mAccount) + EncryptUtil.getMd5(message.getAttachments().get(0).getAttchmentId())+ ".amr")) {
					if (animationDrawable != null && currentItemVoiceUid!=null) {
						startAnim(holder,TYPE_TO_VOICE);
					}
				} else {
					stopAnim(holder,TYPE_TO_VOICE);
				}
			}
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
			DAttachment dfAttachment =message.getAttachments().get(0);
			holder.attIconLeftView.setImageBitmap(AttachmentUtil.getInstance(
					mContext).getAttachmentIcon(
					message.getAttachments().get(0).getName(), false));
			holder.attNameLeftView.setText(message.getAttachments().get(0).getName());
			setAttachmentItemView(holder, dfAttachment, TYPE_FROM_ATTACHMENT,false);
			holder.attSizeLeftView.setText(FileUtil.sizeLongToString(message.getAttachments().get(0).getSize()));
			// 时间显示逻辑 
			setShowTime(holder.senderTimeAttLeftView, position);
			
			holder.attCancelLeftView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					MessagingController.getInstance(MailChat.getInstance()).cancelDownFile(mAccount,mDChatMessages.get(position).getAttachments().get(0).getAttchmentId(),true);
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
			DAttachment dtAttachment =message.getAttachments().get(0);
			holder.attIconRightView.setImageBitmap(AttachmentUtil.getInstance(
					mContext).getAttachmentIcon(
							message.getAttachments().get(0).getName(), false));
			holder.attNameRightView.setText(message.getAttachments().get(0).getName());
			setAttachmentItemView(holder, dtAttachment, TYPE_TO_ATTACHMENT, message.getMessageState() == 1);
			holder.attSizeRightView.setText(FileUtil.sizeLongToString(message.getAttachments().get(0).getSize()));
			// 发送状态 
			viewSendFailed(holder.attSendFailed,null,message); // 时间显示逻辑
			setShowTime(holder.senderTimeAttRightView, position);
			holder.attCancelRightView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(StringUtil.isEmpty(mDChatMessages.get(position).getAttachments().get(0).getFilePath())){
						MessagingController.getInstance(MailChat.getInstance()).cancelDownFile(mAccount,mDChatMessages.get(position).getAttachments().get(0).getAttchmentId(),true);
					}else{
						MessagingController.getInstance(MailChat.getInstance()).cancelUpFile(mAccount,mDChatMessages.get(position).getAttachments().get(0).getAttchmentId());
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
		case TYPE_FROM_OA:
			String type ="";
			switch (message.getMessageType()) {
			case OA_ANNOUNCE:
				type = mContext.getString(R.string.oa_announce);
				break;
			case OA_NEW_TRANS:
				type = mContext.getString(R.string.oa_trans);
				break;
			default:
				break;
			}
			type="["+type+"]";
			holder.textOALeftView.setText(type+message.getOASubject());
//			if(message.getReadFlag()==0){
				holder.textOALeftView.getPaint().setFakeBoldText(false);
				holder.OAUnreadLeftView.setVisibility(View.GONE);
//			}else{
//				holder.textOALeftView.getPaint().setFakeBoldText(true);
//				holder.OAUnreadLeftView.setVisibility(View.VISIBLE);
//			}
			// 时间显示逻辑
			setShowTime(holder.senderTimeOALeftView, position);
			//holder.OAUnreadLeftView.setVisibility(visibility)
			break;
//		case TYPE_FROM_NOTIFICATION:
//			/*
//			 * TextView notification = (TextView) convertView
//			 * .findViewById(R.id.chatting_item_notification); LinearLayout
//			 * layout = (LinearLayout) convertView
//			 * .findViewById(R.id.notificationLayout); //
//			 * layout.getBackground().setAlpha(100);
//			 * notification.setText(message.getContent());
//			 */
//			break;
//		case TYPE_FROM_LOCATION:
//			
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
//			
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
			String subject =message.getMailSubject();
			String mailInfoNickName = getMailInfoNickName(message,email);
			holder.mailInfoText.setText(String.format(mContext.getString(R.string.mail_info_to_chat_subject), mailInfoNickName,subject));
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

	private void onItemClick(int position, ChattingViewHolder holder,
			int viewType) {
		DChatMessage message = mDChatMessages.get(position);
		this.position = position;
		String filePath = null;
		switch (message.getMessageType()) {
		case TEXT:
			// 查看原邮件
			break;
		case IMAGE:
			// 下载成功查看大图，否则重新下载预览图
			DAttachment dAttachment = message.getAttachments().get(0);
			String imageUrl =dAttachment.getFilePath();
			if(StringUtil.isEmpty(imageUrl)){
				imageUrl =Protocol.getInstance().getFileURL(dAttachment.getFileid(), dAttachment.getName(), true);
			}else{
				//本地路径
				imageUrl = "file://" +imageUrl;
			}
			File f = ImageLoader.getInstance().getDiskCache().get(imageUrl);
			dAttachment.setImageLoad(true);
			ImageView iv;
			if(viewType == TYPE_FROM_IMAGE){
				iv=holder.imageContentLeftView;
			}else{
				iv =holder.imageContentRightView;
			}
			messageController.updateDAttImageLoadStata(mAccount, dAttachment.getAttchmentId(), true, false);
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
				List<DChatMessage> imageMessages=getImageMessages(mDChatMessages);
				int currentItem = getCurrentItem(dAttachment.getAttchmentId(),imageMessages);
				ImageFullActivity.actionDChatImageFullActivity(mContext, mAccount.getUuid(), imageMessages,currentItem);
			}
			break;
		case ATTACHMENT:
			DAttachment attach = message.getAttachments().get(0);
			filePath = attach.getFilePath();
			if(StringUtil.isEmpty(filePath)){
				filePath =MailChat.getInstance().getAttFilePath(MailChat.getInstance().getChatAttachmentDirectory(mAccount), attach.getAttchmentId(), attach.getName());
			}
			if (!new File(filePath).exists()) {
				/***** 检查是否有网络，如果没有网络，则提示用户 **/
				if (!NetUtil.isActive()) {
					NetUtil.showNoConnectedAlertDlg(mContext);
					return;
				}
				MessagingController.getInstance(MailChat.getInstance()).dChatDownFile(mAccount, message);
			} else {
				// 打开
				FileUtil.viewFile(mContext, filePath, null,attach.getName());
			}
			break;
		case OA_ANNOUNCE:
		case OA_NEW_TRANS:
			messageController.updateDChatMessageReadFlag(mAccount, message.getUuid(), 0);
			message.setReadFlag(0);
			notifyDataSetChanged();
			WebViewWithErrorViewActivity.forwardOpenUrlActivity(mContext,
			        message.getURL() + "&p=" + Utility.getOAUserParam(mAccount)+"&type="+mAccount.getoALoginType(),
			        null, mAccount.getUuid(),-1, true);
			break;
//		case LOCATION:
//			// 地理位置
//			Intent intent = new Intent(mContext, MapActivity.class);
//			intent.putExtra("x", message.getLongitude());
//			intent.putExtra("y", message.getLatitude());
//			intent.putExtra("name", message.getLocationName());
//			intent.putExtra("addr", message.getAddress());
//			mContext.startActivity(intent);
//
//			break;
		case FROM_MAIL_INFO:
			String email = message.getMailFrom();
			String subject =message.getMailSubject();
			String mailInfoNickName = getMailInfoNickName(message,email);
			ChatFromMailInfoActivity.actionChatFromMailInfo(mContext, email,mailInfoNickName, subject,message.getMailPreview());
			break;
		default:
			break;
		}
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
		case TYPE_FROM_OA:
			view = mInflater.inflate(R.layout.chatting_item_from_oa,
					parent, false);
			holder.nameView = (TextView) view
					.findViewById(R.id.chatting_item_name);
			holder.headImageView =(RoundImageView)view
					.findViewById(R.id.chatting_item_head);
			holder.senderTimeOALeftView = (TextView) view
					.findViewById(R.id.chatting_item_time);
			holder.textOALeftView=(TextViewFixTouchConsume) view
					.findViewById(R.id.txt_view_chatting_oa);
			holder.OAUnreadLeftView =  (ImageView) view
					.findViewById(R.id.img_oa_unread);
			break;
//		case TYPE_FROM_NOTIFICATION:
//			view = mInflater.inflate(R.layout.chatting_item_from_notification,
//					parent, false);
//			break;
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

	@Override
	public void onClick(View v) {
		int index = (Integer) v.getTag();

		DChatMessage message = mDChatMessages.get(index);

		switch (v.getId()) {

		case R.id.chatting_item_head:
			String email = "";
			String nickName = "";
			String imgHeadPath ="";
			switch (message.getMessageType()) {
			case OA_ANNOUNCE:

			case OA_NEW_TRANS:
				String fromInfo = message.getOAFrom();
				email = StringUtil.getInfoEMail(fromInfo);
				nickName = message.getNickName();
				if(TextUtils.isEmpty(nickName)){
					nickName = StringUtil.getInfoNickName(fromInfo);
				}
				String userHeadHash = message.getImgHeadHash();
				if(!TextUtils.isEmpty(userHeadHash)){
					imgHeadPath = GlobalConstants.HOST_IMG+userHeadHash;
				}
				break;
			default:
				if(message.getSenderEmail().equals(mAccount.getEmail())){
					String accountBigHeadImg = mAccount.getAccountBigHeadImg();
					if (!TextUtils.isEmpty(accountBigHeadImg)) {
						imgHeadPath=accountBigHeadImg;
					}
				}else{
					if (!TextUtils.isEmpty(toEmailImgHeadHash)) {
						imgHeadPath =GlobalConstants.HOST_IMG + toEmailImgHeadHash;
					}
				}
				email = message.getSenderEmail();
				nickName = getNickName(message);
				break;
			}
			ContactAttribute contact = new ContactAttribute();
			contact.setEmail(email);
			contact.setNickName(nickName);
			contact.setImgHeadPath(imgHeadPath);
			messageController.get35EisContactsInfoForJump(mAccount, contact,false);
			break;

		default:
			break;
		}
	}


	/**
	 * 判断存储卡
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-9-27
	 */
//	private boolean checkSdCard(Attachment mAttachment) {
//		// 判断存储卡可用
//		if (!android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment
//				.getExternalStorageState())) {
//			MailToast
//					.makeText(
//							MailChat.getInstance(),
//							mContext.getString(R.string.no_sdcard_or_insufficient_storage),
//							Toast.LENGTH_SHORT).show();
//			return false;
//		}
//		// 剩余空间是否足够
//		long availableSpare = 0;
//		try {
//			// 取得sdcard文件路径
//			StatFs statFs = new StatFs(GlobalConstants.DEF_DIR);
//			// 获取block的SIZE
//			long blocSize = statFs.getBlockSize();
//			// 可使用的Block的数量
//			long availaBlock = statFs.getAvailableBlocks();
//			availableSpare = availaBlock * blocSize;
//		} catch (Exception e) {// IllegalArgumentException
//			e.printStackTrace();
//			MailToast
//					.makeText(
//							MailChat.getInstance(),
//							mContext.getString(R.string.no_sdcard_or_insufficient_storage),
//							Toast.LENGTH_SHORT).show();
//			return false;
//		}
//		if (availableSpare <= mAttachment.getSize()) {
//			MailToast
//					.makeText(
//							MailChat.getInstance(),
//							mContext.getString(R.string.no_sdcard_or_insufficient_storage),
//							Toast.LENGTH_SHORT).show();
//			return false;
//		}
//		return true;
//	}

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

	public void stopPlay(String voiceUuid) {
		if (mediaPlayer != null && mediaPlayer.isPlaying()&&
				!StringUtil.isEmpty(currentItemVoiceUid)&&currentItemVoiceUid.equals(voiceUuid)) {
			mediaPlayer.stop();
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
				voiceView.setImageResource(R.drawable.bg_chatting_from_voice_playing003);
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

	private void onItemLongClick(int position) {
		DChatMessage mMessage = mDChatMessages.get(position);
		String filePath;
		List<Integer> items = new ArrayList<Integer>();
		switch (mMessage.getMessageType()) {
		case TEXT:
			if (mMessage.getMessageState()== 1) {
				items.add(ChattingShowDialog.SENDER);
			}
			items.add(ChattingShowDialog.FORWARD);
			items.add(ChattingShowDialog.COPY);
			items.add(ChattingShowDialog.DEL);
			break;
		case IMAGE:
			if (mMessage.getMessageState()== 1) {
				items.add(ChattingShowDialog.SENDER);
			}
			if(mMessage.getSenderEmail().equals(mAccount.getEmail())){
				if(mMessage.getMessageState()== 0){
					items.add(ChattingShowDialog.FORWARD);
				}
			}else{
				items.add(ChattingShowDialog.FORWARD);
			}
			items.add(ChattingShowDialog.DEL);
			break;
		case VOICE:
			if (mMessage.getMessageState()== 1) {
				items.add(ChattingShowDialog.SENDER);
			}
			items.add(ChattingShowDialog.DEL);
			break;
		case ATTACHMENT:
			DAttachment dAttachment = mMessage.getAttachments().get(0);
			filePath=dAttachment.getFilePath();
			if(StringUtil.isEmpty(filePath)){
				filePath =MailChat.getInstance().getAttFilePath(MailChat.getInstance().getChatAttachmentDirectory(mAccount), dAttachment.getAttchmentId(), dAttachment.getName());
			}
			if (mMessage.getMessageState() == 1) {
				items.add(ChattingShowDialog.SENDER);
			}
			if(mMessage.getSenderEmail().equals(mAccount.getEmail())){
				if(mMessage.getMessageState()== 0){
					items.add(ChattingShowDialog.FORWARD);
				}
			}else{
				items.add(ChattingShowDialog.FORWARD);
			}
			if (new File(filePath).exists()) {
				items.add(ChattingShowDialog.OPEN);
			}else{
				if(dAttachment.isDownloadPause()){
					items.add(ChattingShowDialog.DOWNLOAD);
				}else{
					items.add(ChattingShowDialog.CANCEL_DOWNLOAD);
				}
			}
			items.add(ChattingShowDialog.DEL);
			break;
		case LOCATION:		
			items.add(ChattingShowDialog.DEL);		
			break;
		case NOTIFICATION:
			return;
		case OA_ANNOUNCE:
		case OA_NEW_TRANS:
			items.add(ChattingShowDialog.DEL);
			break;
		default:
			break;
		}
		alertOperation(items, mMessage);
	}

	private void alertOperation(List<Integer> items, DChatMessage mMessage) {
		if (items.size() > 0) {
			ChattingShowDialog dialog = new ChattingShowDialog(mContext, this,
					R.style.dialog, items, mMessage, mAccount);
			dialog.show();
		}
	}
	public void deleteMessage(String uid) {
		
		messageController.deleteDMessage(mAccount, uid);
		((ChattingSingleActivity) mContext).setTranscriptModeDisabled();
		DChatMessage removeMessage=new DChatMessage();
		for (DChatMessage message : mDChatMessages) {
			if (message.getUuid().equals(uid)) {
				removeMessage=message;	
				break;
			}
		}
		mDChatMessages.remove(removeMessage);
		//更新单聊列表最后一条消息。
		if(mDChatMessages.size()>0){
			messageController.updateDchat(mAccount, DChat.structureDchat(mDChatMessages.get(mDChatMessages.size()-1),mAccount),false);
		}else{
			messageController.updateDchat(mAccount, dChat,true);
		}
		notifyDataSetChanged();
		((ChattingSingleActivity) mContext).setTranscriptModeScroll();
	}
	public void deleteMessageForAtt(String attUid) {
		String msgUid =null;
		((ChattingSingleActivity) mContext).setTranscriptModeDisabled();
		DChatMessage removeMessage=new DChatMessage();
		for (DChatMessage message : mDChatMessages) {
			List<DAttachment> dAttachments =message.getAttachments();
			if (dAttachments!=null&&dAttachments.size()>0&&message.getAttachments().get(0).getAttchmentId().equals(attUid)) {
				msgUid=message.getUuid();
				removeMessage=message;
				break;
			}
		}
		mDChatMessages.remove(removeMessage);
		//更新单聊列表最后一条消息。
		if(mDChatMessages.size()>0){
			messageController.updateDchat(mAccount, DChat.structureDchat(mDChatMessages.get(mDChatMessages.size()-1),mAccount),false);
		}else{
			messageController.updateDchat(mAccount, dChat,true);
		}
		notifyDataSetChanged();
		((ChattingSingleActivity) mContext).setTranscriptModeScroll();
		if(msgUid!=null){
			messageController.deleteDMessage(mAccount, msgUid);
		}
	}
	static class ChattingViewHolder {
		// 名称
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

		//左边OA布局
		TextView senderTimeOALeftView;
		TextViewFixTouchConsume textOALeftView;
		ImageView OAUnreadLeftView;

		//邮件透传
		TextView mailInfoText;
	}

	public List<DChatMessage> getmDChatMessages() {
		return mDChatMessages;
	}

	private void setShowTime(TextView textTimeView, int position) {
		//当前消息时间
		long time = mDChatMessages.get(position).getTime();
		//上一条消息时间
		long lastTime =-1;
		if (mDChatMessages.size() > 1 && position > 0) {
			lastTime=mDChatMessages.get(position - 1).getTime();
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

	private void viewSendFailed(ImageView imageView, ProgressBar progressBar,final DChatMessage message) {
		int state = message.getMessageState();
		switch (state) {
		case 0:
			imageView.setVisibility(View.GONE);
			if(progressBar!=null){
				progressBar.setVisibility(View.GONE);
			}
			break;
		case 1:
			imageView.setVisibility(View.VISIBLE);
			if(progressBar!=null){
				progressBar.setVisibility(View.GONE);
			}
			break;
		case 2:
			imageView.setVisibility(View.GONE);
			if(progressBar!=null){
				progressBar.setVisibility(View.VISIBLE);
			}
		}
		imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showSenderMessageAgainDlg(message);
			}
		});
	}

	private String getNickName(DChatMessage message) {
		String name = null;
		if (message.getSenderEmail().equals(mAccount.getEmail())) {
			name =mContext.getString(R.string.to_me);
		} else {
			name = nickName;
		}
		return name;
	}

	private SpannableString getLocationImage() {
		SpannableString spannable = new SpannableString("[location]");
		// 要让图片替代指定的文字就要用ImageSpan
		ImageSpan span = new ImageSpan(locationDrawable,
				ImageSpan.ALIGN_BASELINE);
		spannable.setSpan(span, 0, spannable.length(),
				Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		return spannable;
	}

	/**
	 * 设置发件人可点击
	 * 
	 * @Description:
	 * @param name
	 * @return
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2014-1-20
	 */
	private SpannableString getClickableSpan(String name, final int position) {
		SpannableString spannableString = new SpannableString(name);
		spannableString.setSpan(new Clickable(position), 0, name.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannableString;
	}

	class Clickable extends ClickableSpan {

		private int position;

		public Clickable(int position) {
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			// 进入发件人详情页面
			DChatMessage message = mDChatMessages.get(position);
			// GroupMemberInfoActivity.actionView(mContext, mAccount, mDChat,
			// message.getMember());
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			ds.setColor(mContext.getResources().getColor(
					R.color.bg_chatting_name));
			ds.setUnderlineText(false);
		}
	}

	/**
	 * 更新某项item
	 * 
	 * @Description:
	 * @param itemIndex
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-5-20
	 */
	public void updateView(int itemIndex) {
		// 得到第一个可显示控件的位置，
		int firstVisiblePosition = mSingleChattingView.getFirstVisiblePosition();
		int lastVisiblePosition = mSingleChattingView.getLastVisiblePosition();
		// 只有当要更新的view在可见的位置时才更新，不可见时，跳过不更新
		if (itemIndex >= firstVisiblePosition
				&& itemIndex <= lastVisiblePosition) {
			// 得到要更新的item的view
			View view = mSingleChattingView.getChildAt(itemIndex- firstVisiblePosition);
			ChattingViewHolder holder = (ChattingViewHolder) view.getTag();
			DChatMessage message = mDChatMessages.get(itemIndex);
			boolean isTo = mAccount.getEmail().equals(message.getSenderEmail());
			int type = 0;
			DAttachment dAttachment=null;
			switch (message.getMessageType()) {
			case ATTACHMENT:
				type = isTo ? TYPE_TO_ATTACHMENT : TYPE_FROM_ATTACHMENT;
				dAttachment = message.getAttachments().get(0);
				setAttachmentItemView(holder, dAttachment, type , message.getMessageState() == 1);
				break;
			default:
				break;
			}
		}
	}

	private void showSenderMessageAgainDlg(final DChatMessage message) {
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

	public void senderMessageAgain(DChatMessage message) {
		// TODO Auto-generated method stub
		message.setMessageState(2);
		message.setTime(System.currentTimeMillis());
		notifyDataSetChanged();
		MessagingController.getInstance(MailChat.app).publishDMessage(message.getReceiverEmail()+"/1", message, mAccount, null);
	}
	
	private List<DChatMessage> getImageMessages(List<DChatMessage> mMessages){
		List<DChatMessage> imageMessages =new ArrayList<DChatMessage>();
		for(DChatMessage dMessage : mMessages){
			if(dMessage.getMessageType().equals(DChatMessage.Type.IMAGE)){
				imageMessages.add(dMessage);
			}
		}
		return imageMessages;
	}
	private int getCurrentItem(String imageUid,List<DChatMessage> imageMessages){
		int currentItem=0;
		for (int i = 0; i < imageMessages.size(); i++) {
			if(imageMessages.get(i).getAttachments().get(0).getAttchmentId().equals(imageUid)){
				currentItem=i;
			}
		}
		return currentItem;
	}
	
	private void setAttachmentItemView(ChattingViewHolder holder,DAttachment dAttachment,int type,boolean isSendFailed){
		if (type == TYPE_FROM_ATTACHMENT) {
			int progress = dAttachment.getDownloadProgress();
			String filePath =MailChat.getInstance().getAttFilePath(MailChat.getInstance().getChatAttachmentDirectory(mAccount), dAttachment.getAttchmentId(), dAttachment.getName());
			if (new File(filePath).exists()) {
				holder.attLayoutprogressLeftView.setVisibility(View.GONE);
				holder.attStatusLeftView.setText(R.string.chatting_att_download_finished_open);
				holder.attCancelLeftView.setVisibility(View.GONE);
				holder.attMenuLeftView.setVisibility(View.VISIBLE);
			} else {
				if (!dAttachment.isDownloadPause()) {
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
			String filePath=dAttachment.getFilePath();
			String downFilePath = MailChat.getInstance().getAttFilePath(MailChat.getInstance().getChatAttachmentDirectory(mAccount), dAttachment.getAttchmentId(), dAttachment.getName());
			if(StringUtil.isEmpty(filePath)){
				filePath =downFilePath;
				progress = dAttachment.getDownloadProgress();
				if (new File(filePath).exists()) {
					holder.attLayoutprogressRightView.setVisibility(View.GONE);
					holder.attStatusRightView.setText(R.string.chatting_att_open);
					holder.attCancelRightView.setVisibility(View.GONE);
					holder.attMenuRightView.setVisibility(View.VISIBLE);
				} else {
					if (!dAttachment.isDownloadPause()) {
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
				progress =dAttachment.getUploadProgress();
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

	public String getToEmailImgHeadHash() {
		return toEmailImgHeadHash;
	}

	public void setToEmailImgHeadHash(String toEmailImgHeadHash) {
		this.toEmailImgHeadHash = toEmailImgHeadHash;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	private void onClickForVoice(int position,ChattingViewHolder holder,int viewType){
		DChatMessage mMessage = mDChatMessages.get(position);
		DAttachment attachment = mMessage.getAttachments().get(0);
		// 播放
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
				MessagingController.getInstance(MailChat.getInstance()).updateDAttachmentReadState(
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
	
	private String getMailInfoNickName(DChatMessage message,String mailInfoEmail){
		String mailInfoNickName="";
		//判断显示
		if(message.getSenderEmail().equals(mAccount.getEmail())){
			mailInfoNickName =nickName;
			if(StringUtil.isEmpty(mailInfoNickName)){
				mailInfoNickName =StringUtil.getPrdfixStr(mailInfoEmail);
			}
		}else{
			mailInfoNickName =mAccount.getName();
			if(StringUtil.isEmpty(mailInfoNickName)){
				mailInfoNickName =StringUtil.getPrdfixStr(mAccount.getEmail());
			}
		}
		return mailInfoNickName;
	}
	
	@Override
	public  float[] getBoardInfo(int index) {
		if (!indexViewMap.containsKey(index)){
			return null;
		}
		
		View itemView = indexViewMap.get(index);
		
		Rect r = new Rect();
		itemView.getGlobalVisibleRect(r);
		Log.d("xxxx", "GlobalVisibleRect: left="+r.left+" top="+r.top+" right="+r.right+" down="+r.bottom);
		
		int[] lefttop = new int[2];
		itemView.getLocationOnScreen(lefttop);
		int width = itemView.getWidth();
		float[] locations = new float[4];
		locations[0] = lefttop[0];
		locations[1] = lefttop[1];
		
		Log.d("xxx", "isShown="+itemView.isShown());
		
		Log.d("xxxx", "LocationOnScreen: left="+locations[0]+" top="+locations[1]);
		
		locations[2] = lefttop[0]+width;
		locations[3] = lefttop[1];
		
		if (!itemView.isShown()){
			removeOldViewBindIndex(itemView);
			return null;
		}
		
		View targetView = null;
		int viewType = getItemViewType(index);
		switch (viewType){
			case TYPE_TO_TEXT:
			targetView = itemView.findViewById(R.id.chatting_item_content);	
			break;
			
			case TYPE_FROM_TEXT:
			targetView = itemView.findViewById(R.id.from_text_container);	
			break;
		}
		if (targetView == null){
			Log.d("xxx", "target view is null");
			removeOldViewBindIndex(itemView);
			return null;
		}
		
		Rect targetr = new Rect();
		targetView.getGlobalVisibleRect(targetr);
		Log.d("xxxx", "target GlobalVisibleRect: left="+targetr.left+" top="+targetr.top+" right="+targetr.right+" down="+targetr.bottom);
		
		int[] targetLefttop = new int[2];
		targetView.getLocationOnScreen(targetLefttop);
		int targetWidth = targetView.getWidth();
		float[] targetLocations = new float[4];
		targetLocations[0] = targetLefttop[0];
		targetLocations[1] = targetLefttop[1];
		
		Log.d("xxx", "target isShown="+targetView.isShown());
		
		Log.d("xxxx", "target LocationOnScreen: left="+targetLocations[0]+" top="+targetLocations[1]);
		
		targetLocations[2] = targetLefttop[0]+targetWidth;
		targetLocations[3] = targetLefttop[1];
		
		return targetLocations;
	}

	@Override
	public int boardCount() {
		return mDChatMessages.size();
	}

	@Override
	public Activity getActivity() {
		return (Activity)mContext;
	}
}
