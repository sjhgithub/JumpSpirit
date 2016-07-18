package cn.mailchat.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.util.LruCache;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.mailchat.Account;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.activity.ChattingActivity;
import cn.mailchat.activity.ChattingSingleActivity;
import cn.mailchat.activity.Main4TabActivity;
import cn.mailchat.activity.OALoginActivity;
import cn.mailchat.activity.WebViewWithErrorViewActivity;
import cn.mailchat.chatting.beans.CGroup;
import cn.mailchat.chatting.beans.CMessage.Type;
import cn.mailchat.chatting.beans.DChat;
import cn.mailchat.chatting.beans.DChatMessage;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.drag.ViewDragHelper;
import cn.mailchat.drag.ViewDragWare;
import cn.mailchat.utils.ImageUtils;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.utils.TimeUtils;
import cn.mailchat.view.BadgeView;
import cn.mailchat.view.ChatListDialog;
import cn.mailchat.view.RoundImageView;
import cn.mailchat.view.ShareDialog;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.umeng.analytics.MobclickAgent;

public class ChatListAdapter extends CursorAdapter {
	private Activity mContext;
	private String today;
	private Account account;
	
	private MessagingController mMessagingController;
	
	// 图片缓存Map
	private LruCache<String, Bitmap> mMemoryCache;

	public ChatListAdapter(Context context, Cursor c, Account account) {
		super(context, null, 0);
		// TODO Auto-generated constructor stub
		this.mContext = (Activity)context;
		today = TimeUtils.DataFormatENYYMMDD.format(new Date(System.currentTimeMillis()));
		mMemoryCache = ((MailChat) MailChat.app).getmMemoryCache();
		this.account = account;
		mMessagingController = MessagingController.getInstance(mContext.getApplication());
	}
	
	@Override
	public View newView(final Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = new ViewHolder();
		View convertView = LayoutInflater.from(context).inflate(R.layout.item_chat_tab_new, null);
		holder.contentLayout = (LinearLayout) convertView.findViewById(R.id.layout_chat);
		holder.layout_logo = (RelativeLayout) convertView.findViewById(R.id.layout_logo);
		holder.logoImg = (RoundImageView) convertView.findViewById(R.id.logo_img);
		holder.chatName = (TextView) convertView.findViewById(R.id.txt_chat_name);
		holder.PreviewView = (TextView) convertView.findViewById(R.id.txt_chat_preview);
		holder.time = (TextView) convertView.findViewById(R.id.txt_chat_time);
		// holder.chatUnreadCount=(TextView)
		// convertView.findViewById(R.id.txt_chat_unread);
		holder.badgeView = new BadgeView(context, holder.layout_logo);
		holder.badgeView.setBackgroundResource(R.drawable.badge_count_bg);
		holder.badgeView.setBadgeMargin(1, 1);
		holder.badgeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
		holder.badgeView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
		holder.badgeView.setIncludeFontPadding(true);
		// holder.badgeView.setTextSize(10f);
		holder.badgeView.setGravity(Gravity.CENTER);
		holder.badgeView.setTextColor(Color.WHITE);

		// holder.logoPrompt = (ImageView)
		// convertView.findViewById(R.id.logo_chat_prompt);

		holder.logoPrompt = new BadgeView(context, holder.layout_logo);
		holder.logoPrompt.setBackgroundResource(R.drawable.icon_unread_bg);
		holder.logoPrompt.setHeight(10);
		// holder.sendingView =(ImageView)
		// convertView.findViewById(R.id.img_chat_sending);
		convertView.setTag(holder);
		return convertView;
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		final ViewHolder holder = (ViewHolder) view.getTag();
		CGroup group = null;
		DChat dChat = null;
		if (cursor.getString(4) != null) {
			group = parseCursorCGroup(cursor);
			// 群名称
			String groupName = group.getGroupName();

			holder.chatName.setText(groupName);

			// 最后一条信息时间
			long lastSendDate = group.getLastSendDate();
			String time = "";
			if (lastSendDate != 0) {
				Date date = new Date(lastSendDate);
				// 当天的邮件显示 上午 时：分，下午 时：分
				// 之前的邮件全部显示日期，X月X日
				if (today.equals(TimeUtils.DataFormatENYYMMDD.format(date))) {
					time = DateFormat.is24HourFormat(context) ? TimeUtils.DateFormatHM.format(date)
							: TimeUtils.DateFormatHHMM.format(date);
				} else {
					String language = MailChat.application.getLanguage();
					if (language.equals(Locale.SIMPLIFIED_CHINESE.toString())
							|| language.equals(Locale.TAIWAN.toString())) {
						time = TimeUtils.DataFormatCHINESMMDD.format(date);
					} else {
						time = TimeUtils.DataFormatENMMDD.format(date);
					}
				}

			}
			holder.time.setText(time);
			String cGroupLastMessageContent = getCGroupLastMessageContent(group);
			SpannableString content = null;
			if (group.getMessageState() == 2) {
				// holder.sendingView.setVisibility(View.VISIBLE);
				String sendingMessageContent = context.getString(R.string.chatlist_sending) + cGroupLastMessageContent;
				content = colorTextContent(context, sendingMessageContent, sendingMessageContent.indexOf("["),
						sendingMessageContent.indexOf("]") + 1, R.color.chat_list_sending);
				holder.PreviewView.setText(content);
			} else if (group.getMessageState() == 1) {
				String sendFailMessageContent = context.getString(R.string.chatlist_send_failed)
						+ cGroupLastMessageContent;
				content = colorTextContent(context, sendFailMessageContent, sendFailMessageContent.indexOf("["),
						sendFailMessageContent.indexOf("]") + 1, R.color.chat_list_send_failed);
				holder.PreviewView.setText(content);
			} else {
				// holder.sendingView.setVisibility(View.GONE);
				if (group.isDraft()) {
					String draftContent = context.getString(R.string.chatlist_draft) + " : " + group.getDraftContent();
					content = colorTextContent(context, draftContent, draftContent.indexOf("["),
							draftContent.indexOf("]") + 1, R.color.group_blue);
					holder.PreviewView.setText(content);
				} else {
					holder.PreviewView.setText(cGroupLastMessageContent);
				}
			}
			holder.logoPrompt.hide();
			// 未读消息数
			int unreadCount = group.getUnreadCount();
			// unreadCount = 99;
			if (unreadCount > 0) {
				String unReadCount = unreadCount > 99 ? context.getString(R.string.large_size) : unreadCount + "";
				holder.badgeView.setText(unReadCount);
				holder.badgeView.show();
				holder.chatName.setTextColor(context.getResources().getColor(R.color.chat_up));
				holder.time.setTextColor(context.getResources().getColor(R.color.time_bbb));
				// holder.chatName.getPaint().setFakeBoldText(true);// 加粗

			} else {
				// holder.contentLayout.setBackgroundResource(R.drawable.selector_bg_messagelist_read);
				holder.chatName.setTextColor(context.getResources().getColor(R.color.chat_up));
				holder.time.setTextColor(context.getResources().getColor(R.color.time_bbb));
				holder.PreviewView.setTextColor(context.getResources().getColor(R.color.chat_down));
				// holder.chatUnreadCount.setVisibility(View.GONE);
				holder.badgeView.setText("");
				holder.badgeView.hide();
				// viewHolder.fromView.setCompoundDrawables(groupImgRead, null,
				// null, null);
				// holder.chatName.getPaint().setFakeBoldText(false);
			}
			holder.PreviewView.setTextColor(context.getResources().getColor(R.color.chat_down));
			// 是否置顶
			if (group.getIsSticked()) {
				holder.contentLayout.setBackgroundResource(R.drawable.selector_bg_messagelist_stick);
			} else {
				holder.contentLayout.setBackgroundResource(R.drawable.selector_bg_messagelist);
			}

			// 群图片
			holder.logoImg.setVisibility(View.VISIBLE);
			String cacheUid = "1@!#!";
			Bitmap groupIcon = mMemoryCache.get(cacheUid);
			if (groupIcon == null) {

				if (groupIcon == null) {
					// groupIcon =ImageUtils.getUserFirstTextBitmap(context,
					// groupName);
					groupIcon = ImageUtils.getBitmapFromResources(context, R.drawable.icon_group_chat_head);
				}

				if (groupIcon != null) {
					mMemoryCache.put(cacheUid, groupIcon);
				}

			}
			holder.logoImg.setImageBitmap(groupIcon);
		} else {
			dChat = parseCursorDChat(cursor);
			// 最后一条信息时间
			long lastSendDate = dChat.getLastTime();
			String time = "";
			if (lastSendDate != 0) {
				Date date = new Date(lastSendDate);
				// 当天的邮件显示 上午 时：分，下午 时：分
				// 之前的邮件全部显示日期，X月X日
				if (today.equals(TimeUtils.DataFormatENYYMMDD.format(date))) {
					time = DateFormat.is24HourFormat(context) ? TimeUtils.DateFormatHM.format(date)
							: TimeUtils.DateFormatHHMM.format(date);
				} else {
					String language = MailChat.application.getLanguage();
					if (language.equals(Locale.SIMPLIFIED_CHINESE.toString())
							|| language.equals(Locale.TAIWAN.toString())) {
						time = TimeUtils.DataFormatCHINESMMDD.format(date);
					} else {
						time = TimeUtils.DataFormatENMMDD.format(date);
					}
				}
			}
			holder.time.setText(time);
			String dChatLastMessageContent = getDChatLastMessageContent(dChat);
			SpannableString content = null;
			if (dChat.getMessageState() == 2) {
				// holder.sendingView.setVisibility(View.VISIBLE);
				String sendingMessageContent = context.getString(R.string.chatlist_sending) + dChatLastMessageContent;
				content = colorTextContent(context, sendingMessageContent, sendingMessageContent.indexOf("["),
						sendingMessageContent.indexOf("]") + 1, R.color.chat_list_sending);
				holder.PreviewView.setText(content);
			} else if (dChat.getMessageState() == 1) {
				String sendFailMessageContent = context.getString(R.string.chatlist_send_failed)
						+ dChatLastMessageContent;
				content = colorTextContent(context, sendFailMessageContent, sendFailMessageContent.indexOf("["),
						sendFailMessageContent.indexOf("]") + 1, R.color.chat_list_send_failed);
				holder.PreviewView.setText(content);
			} else {
				// holder.sendingView.setVisibility(View.GONE);
				if (dChat.isDraft()) {
					String draftContent = context.getString(R.string.chatlist_draft) + " : " + dChat.getDraftContent();
					content = colorTextContent(context, draftContent, draftContent.indexOf("["),
							draftContent.indexOf("]") + 1, R.color.group_blue);
					holder.PreviewView.setText(content);
				} else {
					holder.PreviewView.setText(dChatLastMessageContent);
				}
			}
			if (dChat.isUnTreated()) {
				holder.logoPrompt.show();
			} else {
				holder.logoPrompt.hide();
			}
			// 未读消息数
			int unreadCount = dChat.getUnReadCount();
			// unreadCount = 99;
			if (unreadCount > 0) {
				String unReadCount = unreadCount > 99 ? context.getString(R.string.large_size) : unreadCount + "";
				holder.badgeView.setText(unReadCount);
				holder.badgeView.show();
				holder.chatName.setTextColor(context.getResources().getColor(R.color.chat_up));
				holder.time.setTextColor(context.getResources().getColor(R.color.time_bbb));
				// holder.chatName.getPaint().setFakeBoldText(true);// 加粗

			} else {
				// viewHolder.contentLayout.setBackgroundResource(R.drawable.selector_bg_messagelist_read);
				holder.chatName.setTextColor(context.getResources().getColor(R.color.chat_up));
				holder.time.setTextColor(context.getResources().getColor(R.color.time_bbb));
				// viewHolder.PreviewView.setTextColor(context.getResources().getColor(R.color.text_read));
				// holder.chatUnreadCount.setVisibility(View.GONE);
				holder.badgeView.setText("");
				holder.badgeView.hide();
				// viewHolder.fromView.setCompoundDrawables(groupImgRead, null,
				// null, null);
				// holder.chatName.getPaint().setFakeBoldText(false);
			}
			holder.PreviewView.setTextColor(context.getResources().getColor(R.color.chat_down));
			if (dChat.getdChatType() == DChat.Type.JUMP) {
				holder.contentLayout.setBackgroundResource(R.drawable.selector_bg_messagelist);
			} else {
				// 是否置顶
				if (dChat.isSticked()) {
					holder.contentLayout.setBackgroundResource(R.drawable.selector_bg_messagelist_stick);
				} else {
					holder.contentLayout.setBackgroundResource(R.drawable.selector_bg_messagelist);
				}
			}
			// 用户头像
			holder.logoImg.setVisibility(View.VISIBLE);
			switch (dChat.getdChatType()) {
			case NORMAL:
				String toUserName =dChat.getNickName();
				if(dChat.getEmail().endsWith(GlobalConstants.HIDE_ACCOUNT_SUFFIX)){
					toUserName=context.getString(R.string.chat_anonymous);
				}
				holder.chatName.setText(toUserName);
				final String toNickName = toUserName;
				String userHeadUrl = !StringUtil.isEmpty(dChat.getImgHead())
						? dChat.getImgHead() + GlobalConstants.USER_SMALL_HEAD_END : "";
				ImageLoader.getInstance().displayImage(userHeadUrl, holder.logoImg,
						MailChat.getInstance().initImageLoaderOptions(), new ImageLoadingListener() {

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
								if (arg2 == null) {
									holder.logoImg
											.setImageBitmap(ImageUtils.getUserFirstTextBitmap(context, toNickName));
								}
							}

							@Override
							public void onLoadingCancelled(String arg0, View arg1) {
								// TODO Auto-generated method stub
							}
						});
				break;
			case OA:
				holder.chatName.setText(context.getString(R.string.dchat_oa_nike_name));
				holder.logoImg.setImageBitmap(ImageUtils.getBitmapFromResources(context, R.drawable.icon_oa));
				break;
			case JUMP:
				if (dChat.getUid().equals(
						DChat.getDchatUid(account.getEmail() + "," + GlobalConstants.DCHAT_JUMP_INVITE_COLLEAGUES))) {
					holder.chatName.setText(context.getString(R.string.chat_list_invite_colleagues));
					holder.logoImg.setImageBitmap(
							ImageUtils.getBitmapFromResources(context, R.drawable.icon_chat_invite_colleagues));
				}
				break;
			default:
				break;
			}
		}
		final CGroup cGroup = group;
		final DChat dchat = dChat;
		holder.contentLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (cGroup != null) {
					cGroup.setUnreadCount(0);
					ChattingActivity.actionCreateGroupSuccess(context, cGroup, account);
				} else {
					switch (dchat.getdChatType()) {
					case JUMP:
						if (dchat.getUid().equals(DChat.getDchatUid(
								account.getEmail() + "," + GlobalConstants.DCHAT_JUMP_INVITE_COLLEAGUES))) {
							// InviteContactActivity.actionContactInvite(context,
							// account,true);
							// ChooseContactsActivity.startActivityToInviteChat(context,true,
							// true);
							// MobclickAgent.onEvent(context,
							// "chat_list_data_jump_to_invite_contact_act");
							handleShare();
							MobclickAgent.onEvent(context, "chat_list_show_invite_dialog");
							if (dchat.isUnTreated()) {
								dchat.setUnTreated(false);
								MessagingController.getInstance(MailChat.app).updateDChatUnTreatedFlag(account, dchat);
							}
						}
						break;
					default:
						if (dchat.getdChatType() == DChat.Type.OA) {
							// 如果是oa用户
							if (account.isOAUser()) {
								if (account.isBindOA()
										&& account.getoAHost() != null
										&& account.getoAEmail() != null
										&& account.isBindOAUser()) {
									OALoginActivity.actionLoginOA(mContext,
											account);
									MobclickAgent.onEvent(context,
											"open_login_oa");
								}else{
									MobclickAgent.onEvent(context, "open_oa_view");
									dchat.setUnReadCount(0);
									ChattingSingleActivity.actionChatList(context,
											dchat, account);
								}
							} else {
								WebViewWithErrorViewActivity
										.forwardOpenUrlActivity(context,
												GlobalConstants.OA_NO_OPEN_URL,
												null, account.getUuid(),-1, true);
								MobclickAgent.onEvent(context,
										"open_no_oa_view");
							}
						}else{
							dchat.setUnReadCount(0);
							ChattingSingleActivity.actionChatList(context,
									dchat, account);
						}
						break;
					}
				}
			}
		});
		holder.contentLayout.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				if (cGroup != null) {
					onItemLongClick(cGroup);
				} else {
					onItemLongClick(dchat);
				}
				return false;
			}
		});
		holder.badgeView.invalidate();
		ViewDragHelper.makeViewDragable(holder.badgeView, new ViewDragWare() {
			
			@Override
			public void reShowOrgView() {
				holder.badgeView.show();
			}
			
			@Override
			public void hideOrgView() {
				holder.badgeView.hide();
			}
			
			@Override
			public View getDragView() {
				return holder.badgeView;
			}
			
			@Override
			public Activity getActivity() {
				return mContext;
			}
			
			@Override
			public void doOnActionSuccess() {
				if (cGroup != null){//群聊会话
					cGroup.setUnreadCount(0);
					mMessagingController.updateCGroupUntreatedCount(account, cGroup, 0, null);
				}else{//单聊会话
					dchat.setUnReadCount(0);
					mMessagingController.updateDChatReadState(account, dchat.getUid(), 0, null);
				}
			}
		});
	}

	// 分享
	public void handleShare() {
		final ShareDialog dialog = new ShareDialog(mContext);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);
		dialog.setTitle(R.string.share_to);
		dialog.setShareInfo(mContext.getString(R.string.share_title), mContext.getString(R.string.share_sub_title),
				GlobalConstants.SHARE_URL);
		dialog.show();
	}

	static class ViewHolder {
		LinearLayout contentLayout;
		RelativeLayout layout_logo;
		RoundImageView logoImg;// logo
		TextView chatName;// 名称
		// TextView chatUnreadCount;// 未读数
		TextView time;// 时间
		TextView PreviewView;// 预览
		BadgeView badgeView;
		// ImageView sendingView;//发送中
		BadgeView logoPrompt;
	}

	private String getCGroupLastMessageContent(CGroup group) {
		// 最后一条信息类型
		cn.mailchat.chatting.beans.CMessage.Type type = group.getLastMessageType();
		String sendName = group.getLastMemberNickName();
		String cGroupLastMessageContent = null;
		if (StringUtil.isEmpty(sendName)) {
			sendName = mContext.getString(R.string.to_me);
		} else {
			sendName = sendName + "：";
		}
		if (type != null && group.getLastMessageUid() != null) {
			switch (type) {
			case TEXT:
				// 当群组表中LastMessageUid为""时，消息为空
				if (group.getLastMessageUid().equals("")) {
					cGroupLastMessageContent = "";
				} else {
					cGroupLastMessageContent = sendName + group.getLastMessageContent();
				}
				break;
			case IMAGE:
				if (group.getLastMessageUid().equals("")) {
					cGroupLastMessageContent = group.getLastMessageContent();
				} else {
					cGroupLastMessageContent = sendName + mContext.getString(R.string.msg_img);
				}
				break;
			case ATTACHMENT:
				if (group.getLastMessageUid().equals("")) {
					cGroupLastMessageContent = group.getLastMessageContent();
				} else {
					cGroupLastMessageContent = sendName + mContext.getString(R.string.msg_att);
				}
				break;
			case VOICE:
				if (group.getLastMessageUid().equals("")) {
					cGroupLastMessageContent = group.getLastMessageContent();
				} else {
					cGroupLastMessageContent = sendName + mContext.getString(R.string.msg_voice);
				}
				break;
			case LOCATION:
				if (group.getLastMessageUid().equals("")) {
					cGroupLastMessageContent = group.getLastMessageContent();
				} else {
					cGroupLastMessageContent = sendName + mContext.getString(R.string.msg_location);
				}
				break;
			case NOTIFICATION:
				cGroupLastMessageContent = group.getLastMessageContent();
				break;
			case FROM_MAIL_INFO:
				// TODO:如果需要保证成功，再说
				// 设置为发送成功
				group.setMessageState(0);
				cGroupLastMessageContent = group.getLastMessageContent();
				break;
			}
		} else {
			cGroupLastMessageContent = "";
		}
		return cGroupLastMessageContent;
	}

	private String getDChatLastMessageContent(DChat dChat) {
		// 最后一条信息类型
		DChatMessage.Type type = dChat.getLastMessageType();
		String dChatLastContent = null;
		if (type != null) {
			switch (type) {
			case TEXT:
				// 当群组表中LastMessageUid为""时，消息为空
				if (dChat.getLastMessage() == null) {
					dChatLastContent = "";
				} else {
					dChatLastContent = dChat.getLastMessage();
				}
				break;
			case IMAGE:
				dChatLastContent = mContext.getString(R.string.msg_img);
				break;
			case ATTACHMENT:
				dChatLastContent = mContext.getString(R.string.msg_att);
				break;
			case VOICE:
				dChatLastContent = mContext.getString(R.string.msg_voice);
				break;
			case LOCATION:
				dChatLastContent = mContext.getString(R.string.msg_location);
				break;
			case OA_ANNOUNCE:
				if (dChat.getLastMessage() == null) {
					dChatLastContent = "";
				} else {
					dChatLastContent = dChat.getLastMessage();
				}
				break;
			case OA_NEW_TRANS:
				if (dChat.getLastMessage() == null) {
					dChatLastContent = "";
				} else {
					dChatLastContent = dChat.getLastMessage();
				}
				break;
			case FROM_MAIL_INFO:
				// TODO:如果需要保证成功，再说
				// 设置为发送成功
				dChat.setMessageState(0);
				dChatLastContent = dChat.getLastMessage();
				break;
			default:
				break;
			}
		} else {
			dChatLastContent = "";
		}
		return dChatLastContent;
	}

	public SpannableString colorTextContent(Context mContext, String content, int Start, int end, int color) {
		SpannableString span = new SpannableString(content);
		span.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(color)), Start, end,
				Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		return span;
	}

	private CGroup parseCursorCGroup(Cursor cursor) {
		CGroup mGroup = new CGroup();
		mGroup.setUid(cursor.getString(4));
		mGroup.setGroupName(cursor.getString(5));
		mGroup.setDesc(cursor.getString(7));
		mGroup.setAvatar(cursor.getString(8));
		mGroup.setIsPriv(cursor.getInt(9) == 0 ? false : true);
		mGroup.setIsMember(cursor.getInt(10) == 0 ? false : true);
		mGroup.setIsAdmin(cursor.getInt(11) == 0 ? false : true);
		mGroup.setcUser(cursor.getInt(12));
		mGroup.setcPosts(cursor.getInt(13));
		mGroup.setGroupType(cursor.getInt(14));
		mGroup.setIsUntreated(cursor.getInt(28) == 0 ? false : true);
		mGroup.setLastMemberNickName(cursor.getString(24));
		mGroup.setLastMessageType(Type.values()[cursor.getInt(26)]);
		mGroup.setLastMessageContent(cursor.getString(25));
		mGroup.setLastMessageUid(cursor.getString(22));
		mGroup.setLastSendDate(cursor.getLong(27));
		mGroup.setIsSticked(cursor.getInt(16) == 0 ? false : true);
		mGroup.setStickedDate(cursor.getLong(15));
		mGroup.setIsMessageAlert(cursor.getInt(17) == 1 ? false : true);
		mGroup.setIsMessageVoiceReminder(cursor.getInt(18) == 1 ? false : true);
		mGroup.setUnreadCount(cursor.getInt(23));
		mGroup.setInputType(cursor.getInt(29));
		mGroup.setIsVisibility(cursor.getInt(19));
		mGroup.setReName(cursor.getInt(6) == 1 ? true : false);
		mGroup.setDraftContent(cursor.getString(20));
		mGroup.setDraft(cursor.getInt(21) == 1 ? true : false);
		mGroup.setMessageState(cursor.getInt(30));
		return mGroup;
	}

	private DChat parseCursorDChat(Cursor cursor) {
		DChat dChat = new DChat();
		dChat.setUid(cursor.getString(31));
		dChat.setLastMessage(cursor.getString(39));
		dChat.setLastMessageEmail(cursor.getString(43));
		dChat.setLastTime(cursor.getLong(42));
		dChat.setEmail(cursor.getString(36));
		dChat.setStickedDate(cursor.getLong(34));
		dChat.setSticked(cursor.getInt(35) == 1 ? true : false);
		dChat.setDChatAlert(cursor.getInt(33) == 1 ? true : false);
		DChatMessage.Type type = DChatMessage.Type.values()[cursor.getInt((40))];
		dChat.setLastMessageType(type);
		dChat.setUnReadCount(cursor.getInt(41));
		dChat.setNickName(cursor.getString(48));
		dChat.setImgHead(cursor.getString(49));
		dChat.setImgHeadHash(cursor.getString(50));
		dChat.setDraftContent(cursor.getString(37));
		dChat.setDraft(cursor.getInt(38) == 1 ? true : false);
		dChat.setMessageState(cursor.getInt(44));
		dChat.setInputType(cursor.getInt(45));
		dChat.setVisibility(cursor.getInt(32) == 1);
		dChat.setdChatType(DChat.Type.values()[cursor.getInt((46))]);
		dChat.setUnTreated(cursor.getInt(47) == 1 ? true : false);
		return dChat;
	}

	private void onItemLongClick(Object chat) {
		// TODO Auto-generated method stub
		List<Integer> items = new ArrayList<Integer>();
		if (chat instanceof CGroup) {
			CGroup cGroup = (CGroup) chat;
			if (cGroup.getIsSticked()) {
				items.add(ChatListDialog.CANCELTOP);
			} else {
				items.add(ChatListDialog.TOP);
			}
			items.add(ChatListDialog.DEL);
			ChatListDialog dialog = new ChatListDialog(mContext, R.style.dialog, items, account, cGroup);
			dialog.show();
		} else {
			DChat dChat = (DChat) chat;
			switch (dChat.getdChatType()) {
			case JUMP:
				break;
			default:
				if (dChat.isSticked()) {
					items.add(ChatListDialog.CANCELTOP);
				} else {
					items.add(ChatListDialog.TOP);
				}
				break;
			}
			if(!dChat.getEmail().equals(GlobalConstants.HELP_ACCOUNT_EMAIL)&&dChat.getdChatType()!=cn.mailchat.chatting.beans.DChat.Type.OA){
				items.add(ChatListDialog.DEL);
			}
			ChatListDialog dialog = new ChatListDialog(mContext, R.style.dialog, items, account, dChat);
			dialog.show();
		}
	}
}
