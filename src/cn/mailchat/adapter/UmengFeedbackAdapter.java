package cn.mailchat.adapter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.fb.model.Conversation;
import com.umeng.fb.model.Reply;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.activity.ChattingSingleActivity;
import cn.mailchat.activity.ContactInfoActivity;
import cn.mailchat.activity.ImageFullActivity;
import cn.mailchat.chatting.beans.CAttachment;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.chatting.beans.CMessage;
import cn.mailchat.chatting.beans.DAttachment;
import cn.mailchat.chatting.beans.DChat;
import cn.mailchat.chatting.beans.DChatMessage;
import cn.mailchat.chatting.beans.CMessage.State;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.helper.StringUtils;
import cn.mailchat.utils.DateUtil;
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

public class UmengFeedbackAdapter extends BaseAdapter implements OnClickListener{

	private Context mContext;
	private Account mAccount;
	private LayoutInflater mInflater;

	private static final int TYPE_COUNT = 2;
		private final int VIEW_TYPE_USER = 0;
	private final int VIEW_TYPE_DEV = 1;

	private String playingUri = "";

	public AnimationDrawable animationDrawable;
	private ImageView voiceView;
	private int viewType;

	// 图片缓存
	private LruCache<String, Bitmap> mMemoryCache;
	// 地理位置logo
	private Drawable locationDrawable;

	private ListView mSingleChattingView;

	private Map<Integer, Integer> progressMap;
	private Map<String, Boolean> suspendedMap;// 附件暂停

	public Map<String, Boolean> getSuspendedMap() {
		return suspendedMap;
	}

	public void setSuspendedMap(Map<String, Boolean> suspendedMap) {
		this.suspendedMap = suspendedMap;
	}

	// 声音提示
	private Ringtone ringtone;
	// 界面中最新一条数据的发送时间
	public long lastMessageSendTime = 0;
	private int position;
	private String nickName;
	private Conversation mComversation;
	private List<Reply> mFeekbackMessages;
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public UmengFeedbackAdapter(Context context, ListView mSingleChattingView,
			Account account, Conversation comversation,String nickName) {
		this.mContext = context;
		this.mAccount = account;
		this. nickName= nickName;
		this.mSingleChattingView = mSingleChattingView;
		this.mComversation=comversation;
			mFeekbackMessages =mComversation.getReplyList();

		mInflater = LayoutInflater.from(mContext);

		locationDrawable = mContext.getResources().getDrawable(
				R.drawable.icon_item_location);
		locationDrawable.setBounds(0, 0, locationDrawable.getIntrinsicWidth(),
				locationDrawable.getIntrinsicHeight());

		ringtone = RingtoneManager.getRingtone(mContext, RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		mMemoryCache = ((MailChat) MailChat.app).getmMemoryCache();
		suspendedMap = new HashMap<String, Boolean>();

	}



	@Override
	public int getCount() {
		return mFeekbackMessages.size();
	}

	@Override
	public Object getItem(int position) {
		return mFeekbackMessages.get(position);
	}

	@Override
	public int getViewTypeCount() {
		return TYPE_COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		// 获取单条回复
		Reply reply = mComversation.getReplyList().get(position);
		if (Reply.TYPE_DEV_REPLY.equals(reply.type)) {
			// 开发者回复Item布局
			return VIEW_TYPE_DEV;
		} else {
			// 用户反馈、回复Item布局
			return VIEW_TYPE_USER;
		}
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int viewType = getItemViewType(position);
		FeekbackViewHolder holder = null;
		if (convertView == null) {
			holder = new FeekbackViewHolder();
			convertView = initItemView(parent, viewType, holder);
			convertView.setTag(holder);
		} else {
			holder = (FeekbackViewHolder) convertView.getTag();
		}
		bindView(holder, convertView, position, viewType);
		return convertView;
	}
	private String getNickName(Reply message) {
		String name = null;
		if (!Reply.TYPE_DEV_REPLY.equals(message.type)) {
			name = mContext.getString(R.string.to_me);
		} else {
			name = nickName;
		}
		return name;
	}
	private void bindView(final FeekbackViewHolder holder, View convertView,
			final int position, final int viewType) {
		Reply message = mFeekbackMessages.get(position);
		if (holder.nameView != null) {
			holder.nameView.setTag(position);
			holder.nameView.setOnClickListener(this);
			String font = "";
			if (!Reply.TYPE_DEV_REPLY.equals(message.type)) {
				font = TimeUtils.DateFormatHM.format(new Date(message.created_at))
						+ "<font   color=\"#0190d9\">  " +mContext.getString(R.string.to_me)+ "</font>";
				//我头像设置
				String accountBigHeadImg = mAccount.getAccountBigHeadImg();
				if (!TextUtils.isEmpty(accountBigHeadImg)) {
					String headUrlString="";
					if (!accountBigHeadImg.endsWith("_s")) {
						headUrlString=accountBigHeadImg+GlobalConstants.USER_SMALL_HEAD_END;
					}else{
						headUrlString=accountBigHeadImg;
					}
					ImageLoader.getInstance().displayImage(headUrlString,
								holder.headImageView, MailChat.getInstance().initImageLoaderOptions());
				} else {
					holder.headImageView.setImageBitmap(ImageUtils.getUserFirstTextBitmap(mContext,mContext.getString(R.string.to_me)));
				}
			} else {
				font = "<font   color=\"#0190d9\">"
						+ nickName
						+ "  </font>"
						+ TimeUtils.DateFormatHM.format(new Date(message.created_at));
				//头像设置
				holder.headImageView.setBackgroundResource(R.drawable.icon);
//				holder.headImageView.setImageBitmap(ImageUtils.getUserFirstTextBitmap(mContext, getNickName(message)));
			}

			holder.nameView.setText(Html.fromHtml(font));
		}

		switch (viewType) {
		case VIEW_TYPE_DEV:
			if (!StringUtil.isEmpty(message.content)) {
				String msgContent = message.content;
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
			setShowTime(holder.senderTimeContentLeftView,message, position);
			break;
		case VIEW_TYPE_USER:
			if (!StringUtil.isEmpty(message.content)) {
				SpannableString spannableString2 = FaceConversionUtil
						.getInstace().getExpressionString(mContext,
								message.content);
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
			setShowTime(holder.senderTimeContentRightView,message, position);
			break;
//		case TYPE_FROM_IMAGE:
//			String imageUrl = message.getAttachments().get(0).getFilePath();	
//			Bitmap bitmap = null;
//			if (!StringUtil.isEmpty(imageUrl) && new File(imageUrl).exists()) {
//				bitmap = mMemoryCache.get(imageUrl);
//				if (bitmap == null) {
//					// bitmap = getNativeImage(imageUrl);
//					bitmap = ImageUtils.getLocalBitmap(imageUrl);
//					if (bitmap != null) {
//						bitmap = ImageUtils.createFramedPhoto(bitmap.getWidth(),
//								bitmap.getHeight(), bitmap,
//								GlobalTools.dip2px(mContext, 4));
//						mMemoryCache.put(imageUrl, bitmap);
//					}
//
//				}
//			}
//
//			if (bitmap != null) {
//				holder.imageContentLeftView.setImageBitmap(bitmap);
//			} else {
//				holder.imageContentLeftView
//						.setImageResource(R.drawable.icon_chatting_undown);
//			}
//
//			// 时间显示逻辑
//			setShowTime2(holder.senderTimeImageLeftView, position);
//			break;
//		case TYPE_TO_IMAGE:	
//			String imageUrl2 = message.getAttachments().get(0).getFilePath();
//			Bitmap bitmap2 = null;
//			if (!StringUtil.isEmpty(imageUrl2)) {
//				bitmap2 = mMemoryCache.get(imageUrl2);
//				if (bitmap2 == null) {
//					bitmap2 = getNativeImage(imageUrl2);
//					if (bitmap2 != null) {
//						bitmap2 = ImageUtils.createFramedPhoto(
//								bitmap2.getWidth(), bitmap2.getHeight(),
//								bitmap2, GlobalTools.dip2px(mContext, 4));
//						mMemoryCache.put(imageUrl2, bitmap2);
//					}
//				}
//			}
//
//			if (bitmap2 != null) {
//				holder.imageContentRightView.setImageBitmap(bitmap2);
//			} else {
//				holder.imageContentRightView
//						.setImageResource(R.drawable.icon_chatting_undown);
//			}
//
//			// 发送状态
//			viewSendFailed(holder.imageSendFailed, holder.imageSending,message);
//			// 时间显示逻辑
//			setShowTime2(holder.senderTimeImageRightView, position);
//
//			break;
			
//		case TYPE_FROM_VOICE:
//			holder.voiceLengthLeftView.setText(message.getAttachments().get(0)
//					.getVoiceLength() + "''");
//			if (message.getAttachments().get(0).getReadFlag() == 0) {
//				holder.voiceUnreadLeftView.setVisibility(View.VISIBLE);
//			} else {
//				holder.voiceUnreadLeftView.setVisibility(View.GONE);
//			}
//			if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//				if (playingUri.equals(message.getAttachments().get(0).getFilePath())) {
//					if (animationDrawable != null
//							&& !animationDrawable.isRunning()) {
//						startAnim(holder);
//					}
//				} else {
//					stopAnim();
//				}
//			}
//			 // 时间显示逻辑
//			setShowTime2(holder.senderTimeVoiceLeftView, position);
//			 
//			break;
//		case TYPE_TO_VOICE:
//
//			holder.voiceLengthRightView.setText(message.getAttachments().get(0)
//					.getVoiceLength() + "''");
//
//			if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//				if (playingUri.equals(message.getAttachments().get(0).getFilePath())) {
//					if (animationDrawable != null
//							&& !animationDrawable.isRunning()) {
//						startAnim(holder);
//					}
//				} else {
//					stopAnim();
//				}
//			}
//			// 发送状态
//			viewSendFailed(holder.voiceSendFailed, message);
//			//时间显示逻辑 
//			setShowTime2(holder.senderTimeVoiceRightView, position);
//			 
//			break;
//		case TYPE_FROM_ATTACHMENT:
//			
//			holder.attIconLeftView.setImageBitmap(AttachmentUtil.getInstance(
//					mContext).getAttachmentIcon(
//					message.getAttachments().get(0).getName(), false));
//			holder.attNameLeftView.setText(message.getAttachments().get(0).getName());
//			String filePath = Uri.parse(message.getAttachments().get(0).getFilePath())
//					.getPath(); //
//			Attachment attach = message.getAttachments().get(0);
//			if (new File(filePath).exists()) {
//				holder.attLayoutprogressLeftView.setVisibility(View.GONE);
//				holder.attStatusLeftView.setText("已下载");
//			} else {
//				holder.attLayoutprogressLeftView.setVisibility(View.GONE);
//				holder.attStatusLeftView.setText("未下载");
//			}
//
//			holder.attSizeLeftView.setText(FileUtil.sizeLongToString(message.getAttachments().get(0).getSize()));
//
//			if (progressMap != null && progressMap.containsKey(position)
//					&& !new File(filePath).exists()) {
//				Integer progress = progressMap.get(position);
//				if (progress > 0) {
//					if (suspendedMap != null
//							&& suspendedMap.get(message.getAttachments().get(0).getAttachmentId())) {
//						holder.attLayoutprogressLeftView
//								.setVisibility(View.VISIBLE);
//						holder.attProgressLeftView.setProgress(progress);
//						holder.attStatusLeftView.setText("暂停");
//					} else {
//						holder.attLayoutprogressLeftView
//								.setVisibility(View.VISIBLE);
//						holder.attProgressLeftView.setProgress(progress);
//						holder.attStatusLeftView.setText("下载中");
//					}
//				}
//			} else {
//				holder.attLayoutprogressLeftView.setVisibility(View.GONE);
//			}
//
//			// 时间显示逻辑 
//			setShowTime2(holder.senderTimeAttLeftView, position);
//
//			break;
//		case TYPE_TO_ATTACHMENT:
//
//			holder.attIconRightView.setImageBitmap(AttachmentUtil.getInstance(
//					mContext).getAttachmentIcon(
//							message.getAttachments().get(0).getName(), false));
//			holder.attNameRightView.setText(message.getAttachments().get(0).getName());
//			String tofilePath = Uri
//					.parse(message.getAttachments().get(0).getFilePath()).getPath();
//			if (new File(tofilePath).exists()) {
//				holder.attLayoutprogressRightView.setVisibility(View.GONE);
//				holder.attStatusRightView.setText("");
//			} else {
//				holder.attLayoutprogressRightView.setVisibility(View.GONE);
//				holder.attStatusRightView.setText("未下载");
//			}
//
//			holder.attSizeRightView.setText(FileUtil.sizeLongToString(message.getAttachments().get(0).getSize()));
//
//			if (progressMap != null && progressMap.containsKey(position)
//					&& !new File(tofilePath).exists()) {
//				Integer progress = progressMap.get(position);
//				if (progress > 0) {
//					if (suspendedMap != null
//							&& suspendedMap.get(message.getAttachments().get(0).getAttachmentId())) {
//						holder.attLayoutprogressRightView
//								.setVisibility(View.VISIBLE);
//						holder.attProgressRightView.setProgress(progress);
//						holder.attStatusRightView.setText("暂停");
//					} else {
//						holder.attLayoutprogressRightView
//								.setVisibility(View.VISIBLE);
//						holder.attProgressRightView.setProgress(progress);
//						holder.attStatusRightView.setText("下载中");
//					}
//				}
//			} else {
//				holder.attLayoutprogressRightView.setVisibility(View.GONE);
//			}
//
//			// 发送状态 
//			viewSendFailed(holder.attSendFailed, message); // 时间显示逻辑
//			setShowTime2(holder.senderTimeAttRightView, position);
//			 
//			break;
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
		}

		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
//				onItemClick(position, holder, viewType);
//				Toast.makeText(mContext, "点击"+position, Toast.LENGTH_SHORT).show();
			}
		});

	}

//	private void onItemClick(int position, FeekbackViewHolder holder,
//			int viewType) {
//		Reply message = mFeekbackMessages.get(position);
//		this.position = position;
//		String filePath = null;
//		switch (message.getMessageType()) {
//		case TEXT:
//			// 查看原邮件
//			break;
//		case IMAGE:
//			DAttachment dAttachment = message.getAttachments().get(0);
//			String imageUrl =dAttachment.getFilePath();
//			if(!StringUtils.isNullOrEmpty(imageUrl)&&!new File(imageUrl).exists()&&dAttachment.getLocalPathFlag()!=1){
//				messageController.againDownThumbnailImage(mAccount, dAttachment.getAttchmentId(), dAttachment.getName(),message.getTime(),dAttachment.getFileid(), false);
//				Toast.makeText(mContext, R.string.again_down_thumbnail_image,Toast.LENGTH_SHORT).show();
//			}else if(!StringUtils.isNullOrEmpty(imageUrl)&&new File(imageUrl).exists()){
//				//进入大图查看
//				List<DChatMessage> imageMessages=getImageMessages(mDChatMessages);
//				int currentItem = getCurrentItem(dAttachment.getAttchmentId(),imageMessages);
//				ImageFullActivity.actionDChatImageFullActivity(mContext, mAccount.getUuid(), imageMessages,currentItem);
//			}
//			break;
//		case VOICE:
//			
//			 // 播放 
//			Attachment attachment = message.getAttachments().get(0); 
//			//这个path后期修改 
//			// String path = 
//			//Uri.parse(MailChat.getInstance().getChattingAttachmentDirectory(mAccount.getUuid()) 
//			// +attachment.getAttchmentId()).getPath();
//			String path = attachment.getFilePath();
//
//			// 正在播放，停止 if (playingUri.equals(path)) { stopPlay(); return; }
//
//			stopAnim();
//
//			// 开始播放
//			if (!StringUtil.isEmpty(path) && new File(path).exists()) {
//				this.viewType = viewType;
//				startAnim(holder);
//				startPlay(path);
//				playingUri = path;
//				if (attachment.getReadFlag() == 0
//						&& viewType == TYPE_FROM_VOICE) {
//					MessageController.getInstance().updateDAttachmentReadState(
//							mAccount, String.valueOf(attachment.getAttachmentId()), 1, null);		
//					attachment.setReadFlag(1);
//					holder.voiceUnreadLeftView.setVisibility(View.GONE);
//				}
//			} else {
//				Toast.makeText(mContext,
//						mContext.getString(R.string.msg_loading),
//						Toast.LENGTH_SHORT).show();
//				MessageController.getInstance().downloadAttachment(mAccount, attachment);
//			}
//
//			break;
//		case ATTACHMENT:
//			Attachment attach = message.getAttachments().get(0);
//			// attach.setSuspended(false);//点击设置为非暂停状态
//			suspendedMap.put(String.valueOf(attach.getAttachmentId()), false);
//
//			filePath = attach.getFilePath();
//			if (checkSdCard(attach)) {
//				if (!new File(filePath).exists()) {
//					/***** 检查是否有网络，如果没有网络，则提示用户 **/
//					if (!NetUtil.isActive()) {
//						NetUtil.showNoConnectedAlertDlg(mContext);
//						return;
//					}
////					if (attach.getName().endsWith("doc")
////							|| attach.getName().endsWith("docx")
////							|| attach.getName().endsWith("xls")
////							|| attach.getName().endsWith("xlsx")) {
////						// 预览
////						Intent i = new Intent(mContext,
////								AttachmentPreviewActivity.class);
////						i.putExtra("message", message);
////						i.putExtra(ChattingActivity.ACCOUNTUUID,
////								mAccount.getUuid());
////						mContext.startActivity(i);
////
////					} else {
//						// 下载
//						Intent downloadIntent = new Intent(mContext,
//								MessaggeAttachmentDownloadActivity.class);
//						downloadIntent.putExtra("attachment", attach);
//						downloadIntent.putExtra("mAccountUid",
//								mAccount.getUuid());
//						// mContext.startActivity(downloadIntent);
//						((Activity) mContext).startActivityForResult(
//								downloadIntent, ChattingSingleActivity.FILE_DOWNLOAD);
////					}
//				} else {
//					// 打开
//					FileUtil.viewFile(mContext, attach.getFilePath(), null,
//							attach.getName());
//				}
//			}
//			break;
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
//		default:
//			break;
//		}
//	}

	
	private View initItemView(ViewGroup parent, int viewType,
			FeekbackViewHolder holder) {
		View view = null;
		// holder = new ChattingViewHolder();
		switch (viewType) {
		case VIEW_TYPE_DEV:
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
			break;
		case VIEW_TYPE_USER:
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
			break;
		}
		return view;
	}

	public Bitmap getNativeImage(String imagePath) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高
		Bitmap myBitmap = BitmapFactory.decodeFile(imagePath, options); // 此时返回myBitmap为空

		// 计算缩放比
		int be = (int) (options.outHeight / (float) 300);
		int ys = options.outHeight % 300;// 求余数
		float fe = ys / (float) 300;
		if (fe >= 0.5)
			be = be + 1;
		if (be <= 0)
			be = 1;
		
		options.inSampleSize = be;

		// 重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false
		options.inJustDecodeBounds = false;

		myBitmap = BitmapFactory.decodeFile(imagePath, options);
		return myBitmap;
	}


//	private void onItemClick(int position, ChattingViewHolder holder,
//			int viewType) {
//		DChatMessage message = mDChatMessages.get(position);
//		this.position = position;
//		String filePath = null;
//		switch (message.getMessageType()) {
//
//		case TEXT:
//
//			// 查看原邮件
//			break;
//		case IMAGE:
//			// 查看大图
//			// filePath =
//			// Uri.parse(MailChat.getInstance().getChattingAttachmentDirectory(mAccount.getUuid())
//			// + message.getAttachment().getAttchmentId()).getPath();
////			String imageUrl = message.getAttachments().get(0).getFilePath();
////
////			if (!StringUtil.isEmpty(imageUrl)) {
////				ArrayList<DChatMessage> imageMessages = new ArrayList<DChatMessage>();
////				int p = 0;
////				for (int i = 0; i < mDChatMessages.size(); i++) {
////					DChatMessage msg = mDChatMessages.get(i);
////					if (msg.getMessageType() == Type.IMAGE) {
////						imageMessages.add(msg);
////						if (i == position) {
////							p = imageMessages.size() - 1;
////						}
////					}
////				}
////				Intent mIntent = new Intent(mContext, ImageFullSingleChatActivity.class);
////				mIntent.putExtra("imageMessages", imageMessages);
////				mIntent.putExtra("position", p);
////				mIntent.putExtra(ChattingSingleActivity.ACCOUNTUUID,
////						mAccount.getUuid());
////				mContext.startActivity(mIntent);
////			} else {
////				Toast.makeText(mContext,
////						mContext.getString(R.string.msg_loading),
////						Toast.LENGTH_SHORT).show();
////			}
//			break;
//		case VOICE:
//			
//			 // 播放 
//			Attachment attachment = message.getAttachments().get(0); 
//			//这个path后期修改 
//			// String path = 
//			//Uri.parse(MailChat.getInstance().getChattingAttachmentDirectory(mAccount.getUuid()) 
//			// +attachment.getAttchmentId()).getPath();
//			String path = attachment.getFilePath();
//
//			// 正在播放，停止 if (playingUri.equals(path)) { stopPlay(); return; }
//
//			stopAnim();
//
//			// 开始播放
//			if (!StringUtil.isEmpty(path) && new File(path).exists()) {
//				this.viewType = viewType;
//				startAnim(holder);
//				startPlay(path);
//				playingUri = path;
//				if (attachment.getReadFlag() == 0
//						&& viewType == TYPE_FROM_VOICE) {
//					MessageController.getInstance().updateDAttachmentReadState(
//							mAccount, String.valueOf(attachment.getAttachmentId()), 1, null);		
//					attachment.setReadFlag(1);
//					holder.voiceUnreadLeftView.setVisibility(View.GONE);
//				}
//			} else {
//				Toast.makeText(mContext,
//						mContext.getString(R.string.msg_loading),
//						Toast.LENGTH_SHORT).show();
//				MessageController.getInstance().downloadAttachment(mAccount, attachment);
//			}
//
//			break;
//		case ATTACHMENT:
//			Attachment attach = message.getAttachments().get(0);
//			// attach.setSuspended(false);//点击设置为非暂停状态
//			suspendedMap.put(String.valueOf(attach.getAttachmentId()), false);
//
//			filePath = attach.getFilePath();
//			if (checkSdCard(attach)) {
//				if (!new File(filePath).exists()) {
//					/***** 检查是否有网络，如果没有网络，则提示用户 **/
//					if (!NetUtil.isActive()) {
//						NetUtil.showNoConnectedAlertDlg(mContext);
//						return;
//					}
////					if (attach.getName().endsWith("doc")
////							|| attach.getName().endsWith("docx")
////							|| attach.getName().endsWith("xls")
////							|| attach.getName().endsWith("xlsx")) {
////						// 预览
////						Intent i = new Intent(mContext,
////								AttachmentPreviewActivity.class);
////						i.putExtra("message", message);
////						i.putExtra(ChattingActivity.ACCOUNTUUID,
////								mAccount.getUuid());
////						mContext.startActivity(i);
////
////					} else {
//						// 下载
//						Intent downloadIntent = new Intent(mContext,
//								MessaggeAttachmentDownloadActivity.class);
//						downloadIntent.putExtra("attachment", attach);
//						downloadIntent.putExtra("mAccountUid",
//								mAccount.getUuid());
//						// mContext.startActivity(downloadIntent);
//						((Activity) mContext).startActivityForResult(
//								downloadIntent, ChattingSingleActivity.FILE_DOWNLOAD);
////					}
//				} else {
//					// 打开
//					FileUtil.viewFile(mContext, attach.getFilePath(), null,
//							attach.getName());
//				}
//			}
//			break;
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
//		default:
//			break;
//		}
//	}

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
//	private void startPlay(String path) {
//		try {
//			mediaPlayer.reset();
//			mediaPlayer.setDataSource(path);
//			mediaPlayer.prepare();
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (IllegalStateException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		mediaPlayer.start();
//	}

//	public void stopPlay() {
//		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//			mediaPlayer.stop();
//			// mediaPlayer.release();
//			// playingPosition = -1;
//			playingUri = "";
//			stopAnim();
//		}
//	}

//	private void startAnim(FeekbackViewHolder holder) {
//		if (viewType == TYPE_FROM_VOICE) {
//			voiceView = holder.voiceContentLeftView;
//			voiceView.setImageResource(R.drawable.anim_left_voice);
//		} else {
//			voiceView = holder.voiceContentRightView;
//			voiceView.setImageResource(R.drawable.anim_right_voice);
//		}
//		animationDrawable = (AnimationDrawable) voiceView.getDrawable();
//		animationDrawable.start();
//	}

//	private void stopAnim() {
//		if (animationDrawable != null && animationDrawable.isRunning()) {
//			animationDrawable.stop();
//			if (viewType == TYPE_FROM_VOICE) {
//				voiceView
//						.setImageResource(R.drawable.bg_chatting_from_voice_playing003);
//			} else {
//				voiceView.setImageResource(R.drawable.icon_chatting_to_voice);
//			}
//
//		}
//	}

//	private void alertOperation(List<Integer> items, DChatMessage mMessage) {
//		if (items.size() > 0) {
////			ChattingShowDialog dialog = new ChattingShowDialog(mContext, this,
////					R.style.dialog, items, mMessage, mAccount);
////			dialog.show();
//		}
//	}
//	private void alertOperation(List<Integer> items, DChatMessage mMessage) {
//		if (items.size() > 0) {
//			 ChattingShowDialog dialog = new ChattingShowDialog(mContext,this, R.style.dialog, items, mMessage, mAccount);
//			 dialog.show();
//		}
//	}

//	public void deleteMessage(String uid) {
//		messageController.deleteDMessage(mAccount, uid);
//		((ChattingSingleActivity) mContext).setTranscriptModeDisabled();
//		DChatMessage removeMessage=new DChatMessage();
//		for (DChatMessage message : mDChatMessages) {
//			if (message.getUuid().equals(uid)) {
//				removeMessage=message;	
//				break;
//			}
//		}
//		mDChatMessages.remove(removeMessage);
//		//更新单聊列表最后一条消息。
//		if(mDChatMessages.size()>0){
//			messageController.updateDchat(mAccount, DChat.structureDchat(mDChatMessages.get(mDChatMessages.size()-1)));
//		}else{
//			//TODO:去数据库查询是否该列表还有无消息
//			//messageController.updateDchat(mAccount, dChat);
//		}
//		notifyDataSetChanged();
//		((ChattingSingleActivity) mContext).setTranscriptModeScroll();
//	}

	static class FeekbackViewHolder {
		// 名称
		TextView nameView;
		//头像
		RoundImageView headImageView;
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
		TextView senderTimeVoiceLeftView;
		ImageView voiceContentLeftView;
		ImageView voiceUnreadLeftView;
		TextView voiceLengthLeftView;
		// 右边音频布局
		TextView senderTimeVoiceRightView;
		ImageView voiceContentRightView;
		TextView voiceLengthRightView;
		ImageView voiceSendFailed;
		// 左边附件布局
		TextView senderTimeAttLeftView;
		ImageView attIconLeftView;
		TextView attNameLeftView;
		TextView attSizeLeftView;
		TextView attStatusLeftView;
		ProgressBar attProgressLeftView;
		RelativeLayout attLayoutprogressLeftView;
		// 右边附件布局
		TextView senderTimeAttRightView;
		ImageView attIconRightView;
		TextView attNameRightView;
		TextView attSizeRightView;
		TextView attStatusRightView;
		ImageView attSendFailed;
		ProgressBar attProgressRightView;
		RelativeLayout attLayoutprogressRightView;
		// 左边地理位置布局
		TextView senderTimeLocationLeftView;
		TextViewFixTouchConsume locationNameLeftView;

		// 右边地理位置布局
		TextView senderTimeLocationRightView;
		TextViewFixTouchConsume locationNameRightView;
		ImageView locatioSendFailed;

	}


	private void setShowTime(TextView textTimeView,Reply reply, int position) {
		// 时间提示语
		long td = System.currentTimeMillis();
		String today = TimeUtils.DataFormatENYYMMDD.format(new Date(td));

		// 本条信息时间
		long time1 = reply.created_at;
		long time2 = 0;
		if (mFeekbackMessages.size() > 1 && position > 0) {
			// 上条信息时间
			time2 = mFeekbackMessages.get(position - 1).created_at;
		}

		// 时间间隔小于2分钟不显示
		if (time1 - time2 >= 2 * 60 * 1000) {
			textTimeView.setVisibility(View.VISIBLE);

			Date date = new Date(time1);

			String time = null;

			// 当天的邮件显示 上午 时：分，下午 时：分
			// 昨天的邮件显示 昨天 时：分
			// 之前的邮件全部显示 X月X日 时：分
			if (today.equals(TimeUtils.DataFormatENYYMMDD.format(date))) {
				time = TimeUtils.DateFormatHHMM.format(date);
			} else if (DateUtil.befoDay("yyyy-MM-dd").equals(
					TimeUtils.DataFormatENYYMMDD.format(date))) {
				time = mContext.getString(R.string.yesterday)
						+ TimeUtils.DateFormatHM.format(date);
			} else {
				time = TimeUtils.DateFormatMDHM.format(date);
			}

			textTimeView.setText(time);

		} else {
			textTimeView.setVisibility(View.GONE);
		}
	}

//	private void setShowTime2(TextView textTimeView, int position) {
//		// 一天只显示一条
//		Date date = new Date(mDChatMessages.get(position).getTime());
//		String dy2 = "";
//		if (mDChatMessages.size() > 1 && position > 0) {
//			// 上条信息时间
//			Date date2 = new Date(mDChatMessages.get(position - 1).getTime());
//			dy2 = TimeUtils.DataFormatENYYMMDD.format(date2);
//		}
//
//		String dy1 = TimeUtils.DataFormatENYYMMDD.format(date);
//		if (!dy1.equals(dy2)) {
//			textTimeView.setVisibility(View.VISIBLE);
//			if(MailChat.application.isZh()){
//				textTimeView.setText(TimeUtils.DateFormatMMDDF.format(date));
//			}else{
//				textTimeView.setText(TimeUtils.DateFormatENMMDDF.format(date));
//			}
//		} else {
//			textTimeView.setVisibility(View.GONE);
//		}
//
//	}

	// private void clickSenderView(TextView senderTextView, final int position)
	// {
	// senderTextView.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View arg0) {
	// // 进入发件人详情页面
	// DChatMessage message = mMessages.get(position);
	// GroupMemberInfoActivity.actionView(mContext, mAccount, mGroup.getUuid(),
	// message.getMember());
	// }
	// });
	// }

	private void viewSendFailed(ImageView imageView, ProgressBar progressBar,final Reply reply) {
		
		// 根据Reply的状态来设置replyStateFailed的状态
		if (Reply.STATUS_NOT_SENT.equals(reply.status)) {
			imageView.setVisibility(View.VISIBLE);
		} else {
			imageView.setVisibility(View.GONE);
		}
		// 根据Reply的状态来设置replyProgressBar的状态
		if (Reply.STATUS_SENDING.equals(reply.status)) {
			progressBar.setVisibility(View.VISIBLE);
		} else {
			progressBar.setVisibility(View.GONE);
		}
		imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showSenderMessageAgainDlg(reply);
			}
		});
	}

	/**
	 * 重发对话框
	 * 
	 * @Description:
	 * @param message
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2014-1-22
	 */
//	private void showSenderMessageAgainDlg(final DChatMessage message) {
//		MailDialog.Builder builder = new MailDialog.Builder(mContext);
//		builder.setTitle(mContext.getString(R.string.operate_notice));
//		builder.setMessage(mContext.getString(R.string.sender_message_again));
//		builder.setPositiveButton(mContext.getString(R.string.okay_action),
//				new DialogInterface.OnClickListener() {
//
//					public void onClick(DialogInterface dialog, int id) {
//						dialog.dismiss();
//						// 重新发送
//						senderMessageAgain(message);
//
//					}
//				}).setNeutralButton(mContext.getString(R.string.cancel_action),
//				new DialogInterface.OnClickListener() {
//
//					public void onClick(DialogInterface dialog, int id) {
//						dialog.dismiss();
//					}
//				});
//		builder.create().show();
//	}

	/**
	 * 重发
	 * 
	 * @Description:
	 * @param message
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-9-15
	 */
//	public void senderMessageAgain(DChatMessage message) {
//		//message.setTime(System.currentTimeMillis());
//		MessageController.getInstance().sendDMessage(mAccount, message);	
//	}

	private String getNickName(DChatMessage message, int viewType) {
		String name = null;
		if (message.getSenderEmail().equals(mAccount.getEmail())) {
			name = mContext.getString(R.string.to_me);
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
	 * @author: xuqq
	 * @date:2014-4-1
	 */
//	public void updateView(int itemIndex) {
//		// LogX.d("updateView。。。。。。。。。。。"+itemIndex);
//		// 得到第一个可显示控件的位置，
//		int firstVisiblePosition = mSingleChattingView
//				.getFirstVisiblePosition();
//		int lastVisiblePosition = mSingleChattingView.getLastVisiblePosition();
//		// 只有当要更新的view在可见的位置时才更新，不可见时，跳过不更新
//		if (itemIndex >= firstVisiblePosition
//				&& itemIndex <= lastVisiblePosition) {
//			// 得到要更新的item的view
//			View view = mSingleChattingView.getChildAt(itemIndex
//					- firstVisiblePosition);
//
//			// getView(itemIndex, view, mSingleChattingView);
//			// 更新进度条
//			// 从view中取得holder
//			ChattingViewHolder holder = (ChattingViewHolder) view.getTag();
//
//			DChatMessage message = mDChatMessages.get(itemIndex);
//			boolean isTo = mAccount.getEmail().equals(message.getToEmail());
//			int type = 0;
//			switch (message.getMessageType()) {
//			case ATTACHMENT:
//				type = isTo ? TYPE_TO_ATTACHMENT : TYPE_FROM_ATTACHMENT;
//				break;
//			default:
//				break;
//			}
//			if (type == TYPE_FROM_ATTACHMENT) {
//				if (progressMap != null && progressMap.containsKey(itemIndex)) {
//					Integer progress = progressMap.get(itemIndex);
//					if (suspendedMap != null
//							&& suspendedMap.get(message.getAttachments()
//									.get(position).getId())) {
//						holder.attLayoutprogressLeftView
//								.setVisibility(View.VISIBLE);
//						holder.attStatusLeftView.setText("暂停");
//						holder.attProgressLeftView.setProgress(progress);
//					} else {
//						holder.attStatusLeftView.setText("下载中");
//						holder.attLayoutprogressLeftView
//								.setVisibility(View.VISIBLE);
//						holder.attProgressLeftView.setProgress(progress);
//					}
//
//				} else {
//					holder.attLayoutprogressLeftView.setVisibility(View.GONE);
//					String path = Uri.parse(
//							message.getAttachments().get(position)
//									.getFilePath()).getPath();
//					if (new File(path).exists()) {
//						holder.attStatusLeftView.setText("已下载");
//					} else {
//						holder.attStatusLeftView.setText("未下载");
//					}
//				}
//			} else if (type == TYPE_TO_ATTACHMENT) {
//				if (progressMap != null && progressMap.containsKey(itemIndex)) {
//					Integer progress = progressMap.get(itemIndex);
//					if (suspendedMap != null
//							&& suspendedMap.get(message.getAttachments()
//									.get(position).getId())) {
//						holder.attLayoutprogressRightView
//								.setVisibility(View.VISIBLE);
//						holder.attStatusRightView.setText("暂停");
//						holder.attProgressRightView.setProgress(progress);
//					} else {
//						holder.attStatusRightView.setText("下载中");
//						holder.attLayoutprogressRightView
//								.setVisibility(View.VISIBLE);
//						holder.attProgressRightView.setProgress(progress);
//					}
//
//				} else {
//					holder.attLayoutprogressRightView.setVisibility(View.GONE);
//					String path = Uri.parse(
//							message.getAttachments().get(position)
//									.getFilePath()).getPath();
//					if (new File(path).exists()) {
//						holder.attStatusRightView.setText("");
//					} else {
//						holder.attStatusRightView.setText("未下载");
//					}
//				}
//			}
//		}
//	}

	public void setProgressMap(Map<Integer, Integer> progressMap) {
		this.progressMap = progressMap;
	}
	private void showSenderMessageAgainDlg(final Reply message) {
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

	public void senderMessageAgain(Reply message) {
		// TODO Auto-generated method stub
		
		
		
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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
