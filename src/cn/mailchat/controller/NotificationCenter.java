package cn.mailchat.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.umeng.analytics.MobclickAgent;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import cn.mailchat.Account;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.ChattingActivity;
import cn.mailchat.activity.ChattingSingleActivity;
import cn.mailchat.activity.MailDetialActivity;
import cn.mailchat.activity.MailNotifyPendingActivity;
import cn.mailchat.activity.OALoginActivity;
import cn.mailchat.activity.WebViewWithErrorViewActivity;
import cn.mailchat.beans.ContentOfPushMessage;
import cn.mailchat.chatting.beans.CGroup;
import cn.mailchat.chatting.beans.CMessage;
import cn.mailchat.chatting.beans.DChat;
import cn.mailchat.chatting.beans.DChatMessage;
import cn.mailchat.chatting.beans.DChatMessage.Type;
import cn.mailchat.mail.Address;
import cn.mailchat.mail.Message;
import cn.mailchat.mail.store.LocalStore.LocalFolder;
import cn.mailchat.mail.store.LocalStore.LocalMessage;
import cn.mailchat.search.LocalSearch;
import cn.mailchat.service.NotificationActionService;
import cn.mailchat.service.PendingService;
import cn.mailchat.utils.Utility;

public class NotificationCenter {
	private static final int CHAT_NOTIFICATION_ID = 9000;
	private static final int OA_NEW_TRANS_NOTIFICATION_ID = 9001;//oa代办
	private static final int OA_ANNOUNCE_NOTIFICATION_ID = 9002;//oa公告
	
	private boolean isNotify = true;
	private MailChat mApplication;
	private SharedPreferences preferences;
	private Editor editor;
	public NotificationManager manager;
	private static Map<Integer,List<String>> chatOANotificationData;
	private static Map<Integer,List<String>> chatNotificationData; 
	private static Map<Integer,List<Message>> mailNotificationData; 
	private static Map<Integer,List<ContentOfPushMessage>> mailNotificationDataFor2; //三五2.0邮箱使用

	public static NotificationCenter getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder {
		public static final NotificationCenter INSTANCE = new NotificationCenter();
	}

	private NotificationCenter() {
		mApplication=MailChat.getInstance();
		manager = (NotificationManager) mApplication
				.getSystemService(Context.NOTIFICATION_SERVICE);
		preferences = Preferences.getPreferences(mApplication).getPreferences();
		editor = preferences.edit();
		chatOANotificationData = new HashMap<Integer,List<String>>();
		chatNotificationData=new HashMap<Integer,List<String>>();
		mailNotificationData=new HashMap<Integer,List<Message>>();
		
		mailNotificationDataFor2=new HashMap<Integer,List<ContentOfPushMessage>>();
	}

	private boolean canChatNotify(Account account) {
		boolean notify = false;
		if (!MailChat.getInstance().isForground()
				|| PendingService.screenOn == false) {
			if (MailChat.isTopMsgNotify()) {
				notify = true;
				if (account.isNewMsgNotifation()) {
					notify = true;
				}else {
					notify = false;
				}
				if (MailChat.isQuietTime()) {
					notify = false;
				}
			}
		}
		return notify;
	}

	private boolean canMailNotify(Account account) {
		boolean notify = false;
		if (MailChat.isTopNotifyOn()) {
			notify = true;
			if (account.isNewMailNotifation()) {
				notify = true;
			}else {
				notify = false;
			}
			if (MailChat.isQuietTime()) {
				notify = false;
			}
		}
		return notify;
	}
	
	public synchronized void onNewChattingMessage(String uuid, CGroup group,CMessage message) {
		Account account=Preferences.getPreferences(mApplication).getAccount(uuid);
		// check condition
		if (canChatNotify(account)&& group.getIsMessageAlert()&&account.ismIsAllowAllGroupChatNotify()) {
			// 判断时间间隔
			if (isBetween(MailChat.getFirstTime(), System.currentTimeMillis())) {
				isNotify = false;
			} else {
				MailChat.setFirstTime(System.currentTimeMillis());
				MailChat.save(editor);
				editor.commit();
				isNotify = true;
			}
			int notifyId = account.getAccountNumber()+CHAT_NOTIFICATION_ID;
			List<String> chatList=null;
			if(chatNotificationData.get(notifyId)!=null){
				chatList=chatNotificationData.get(notifyId);
			}else{
				chatList=new ArrayList<String>();
			}
			String groupName =group.getGroupName();
			if(groupName.length()>10){
				groupName=groupName.substring(0, 5)+"..."+groupName.substring(groupName.length()-2, groupName.length());
			}
			chatList.add(0,"["+groupName+"]"+message.getMember().getNickName()+":"+getChattingContent(message));
			chatNotificationData.put(notifyId,chatList);
			notifyChatting(notifyId, account, group, message,chatNotificationData.get(notifyId).size(),chatNotificationData.get(notifyId));
		}
	}

	public synchronized void onNewSingleChattingMessage(String uuid,
			DChatMessage message, DChat dChat,boolean isContactAttributeExist) {
		Account account=Preferences.getPreferences(mApplication).getAccount(uuid);
		if (canChatNotify(account) && dChat.isDChatAlert()&&account.ismIsAllowAllSigleChatNotify()) {
			if (isBetween(MailChat.getFirstTime(), System.currentTimeMillis())) {
				isNotify = false;
			} else {
				MailChat.setFirstTime(System.currentTimeMillis());
				MailChat.save(editor);
				editor.commit();
				isNotify = true;
			}
			int notifyId = account.getAccountNumber()+CHAT_NOTIFICATION_ID;
			List<String> chatList=null;
			if(chatNotificationData.get(notifyId)!=null){
				chatList=chatNotificationData.get(notifyId);
			}else{
				chatList=new ArrayList<String>();
			}
			chatList.add(0,dChat.getNickName()+":"+getSingleChattingContent(message));
			chatNotificationData.put(notifyId,chatList);
			notifySingleChatting(notifyId, account, message, dChat,chatNotificationData.get(notifyId).size(),chatNotificationData.get(notifyId),isContactAttributeExist);
		}
	}

	public synchronized void onNew35Mial(String uuid,LocalFolder folder ,LocalMessage localMessage) {
		Account account = Preferences.getPreferences(mApplication).getAccount(
				uuid);
		if (canMailNotify(account)&&folder.isAllowPush()) {
			if (isBetween(MailChat.getFirstTime(), System.currentTimeMillis())) {
				isNotify = false;
			} else {
				MailChat.setFirstTime(System.currentTimeMillis());
				MailChat.save(editor);
				editor.commit();
				isNotify = true;
			}
			int notifyId = account.getAccountNumber();
			List<Message> mailList=null;
			if(mailNotificationData.get(notifyId)!=null){
				mailList=mailNotificationData.get(notifyId);
			}else{
				mailList=new ArrayList<Message>();
			}
			mailList.add(0,localMessage);
			mailNotificationData.put(notifyId,mailList);
			notifyMail(notifyId, account,mailNotificationData.get(notifyId).size(),mailNotificationData.get(notifyId));
		}
	}

	public synchronized void onNew35OA(String uuid,Type oaType,DChat dChat,DChatMessage message){
		Account account = Preferences.getPreferences(mApplication).getAccount(uuid);
		if (canChatNotify(account)&&account.isNewOANotifation()) {
			if (isBetween(MailChat.getFirstTime(), System.currentTimeMillis())) {
				isNotify = false;
			} else {
				MailChat.setFirstTime(System.currentTimeMillis());
				MailChat.save(editor);
				editor.commit();
				isNotify = true;
			}
			int notifyId;
			switch (oaType) {
			case OA_ANNOUNCE:
				notifyId= account.getAccountNumber()+OA_ANNOUNCE_NOTIFICATION_ID;
				break;
			case OA_NEW_TRANS:
				notifyId= account.getAccountNumber()+OA_NEW_TRANS_NOTIFICATION_ID;
				break;
			default:
				return;
			}
			List<String> oaList=null;
			if(chatOANotificationData.get(notifyId)!=null){
				oaList=chatOANotificationData.get(notifyId);
			}else{
				oaList=new ArrayList<String>();
			}
			oaList.add(0,message.getMessageContent());
			chatOANotificationData.put(notifyId,oaList);
			notifyOA(notifyId,account,message, dChat, chatOANotificationData.get(notifyId).size(),oaList);
		}
	}

	private void notifyChatting(int notifyId, Account account, CGroup group,CMessage message,int count,List<String> messages) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				mApplication);
		builder.setSmallIcon(R.drawable.icon_notification_mail_small);
		builder.setLargeIcon(BitmapFactory.decodeResource(
				mApplication.getResources(), R.drawable.icon_notification_msg));
		builder.setWhen(System.currentTimeMillis());
		String content =  getChattingContent(message);
		String groupName =group.getGroupName();
		if(groupName.length()>10){
			groupName=groupName.substring(0, 5)+"..."+groupName.substring(groupName.length()-2, groupName.length());
		}
		builder.setTicker(" [ " + groupName + " ] "
				+ message.getMember().getNickName() + " : "
				+content);
		//TaskStackBuilder stack;
		Intent i;
		if (count == 1) {
			builder.setContentTitle(groupName);
			builder.setContentText(buildMessageSummary(mApplication,message.getMember().getNickName()+" : ",content));
			builder.setSubText(account.getEmail());
			i = new Intent(mApplication, ChattingActivity.class);
			i.setAction(Intent.ACTION_MAIN);
			i.addCategory(Intent.CATEGORY_LAUNCHER);
			i.putExtra(ChattingActivity.CGOUP, group);
			i.putExtra(ChattingActivity.ACCOUNTUUID, account.getUuid());
			i.putExtra(ChattingActivity.TAG_NOTIFY_CHATTING_FLAG, 1);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			//stack=buildNotifyPendingStack(mApplication, i);
		} else {
			NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(builder);
            for (String m :messages) {
            	String[]  c= m.split(":");
                style.addLine(buildMessageSummary(mApplication,c[0],c[1]));
            }
            style.setSummaryText(account.getEmail());
			String title = formteString(R.string.notify_chat_count, count);
			builder.setContentTitle(title);
			String m=messages.get(0);
			String[]  c= m.split(":");
			builder.setContentText(buildMessageSummary(mApplication,c[0]+":",c[1]));
			builder.setSubText(account.getEmail());
			style.setBigContentTitle(title);
			builder.setStyle(style);
			LocalSearch search = new LocalSearch();
			search.addAllowedFolder(Account.INBOX);
			search.addAccountUuid(account.getUuid());
			i = MailNotifyPendingActivity.actionMailNotify(mApplication, search,true);
			//stack=buildNotifyPendingStack(mApplication, i);
		}
		PendingIntent pendingIntent =PendingIntent.getActivity(mApplication, notifyId, i, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
		builder.setContentIntent(pendingIntent);
		builder.setDeleteIntent(NotificationActionService.getAcknowledgeIntent(mApplication, account));
		//builder.setContentIntent(stack.getPendingIntent(notifyId, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT));
		globalConfigureNotification(builder, isNotify,
				MailChat.isNotifyRingtone(), MailChat.isNotifyVibrateOn());
		manager.notify(notifyId, builder.build());
	}

	private void notifySingleChatting(int notifyId,Account account,DChatMessage message, DChat dChat, int count,List<String> messages,boolean isContactAttributeExist) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				mApplication);
		builder.setSmallIcon(R.drawable.icon_notification_mail_small);
		builder.setLargeIcon(BitmapFactory.decodeResource(
				mApplication.getResources(), R.drawable.icon_notification_msg));
		builder.setWhen(System.currentTimeMillis());
		
		String content =getSingleChattingContent(message);
		
		builder.setTicker(dChat.getNickName() + " : "
				+content);
		//TaskStackBuilder stack;
		Intent i;
		if (count == 1) {
			builder.setContentText(content);
			builder.setSubText(account.getEmail());
			builder.setContentTitle(dChat.getNickName());
			i = ChattingSingleActivity.actionNotifyDMessage(
					MailChat.getInstance(), message, dChat, account, 1,isContactAttributeExist);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			//stack=buildNotifyPendingStack(mApplication, i);
		} else {
			NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(builder);
            for (String m :messages) {
            	String[]  c= m.split(":");
                style.addLine(buildMessageSummary(mApplication,c[0],c[1]));
            }
            style.setSummaryText(account.getEmail());
			String title = formteString(R.string.notify_chat_count, count);
			builder.setContentTitle(title);
			String m=messages.get(0);
			String[]  c= m.split(":");
			builder.setContentText(buildMessageSummary(mApplication,c[0]+":",c[1]));
			builder.setSubText(account.getEmail());
			style.setBigContentTitle(title);
			builder.setStyle(style);
			LocalSearch search = new LocalSearch();
			search.addAllowedFolder(Account.INBOX);
			search.addAccountUuid(account.getUuid());
			i = MailNotifyPendingActivity.actionMailNotify(mApplication, search,true);
			//stack=buildNotifyPendingStack(mApplication, i);
		}
		PendingIntent pendingIntent =PendingIntent.getActivity(mApplication, notifyId, i, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
		builder.setContentIntent(pendingIntent);
		builder.setDeleteIntent(NotificationActionService.getAcknowledgeIntent(mApplication, account));
		//builder.setContentIntent(stack.getPendingIntent(notifyId, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT));
		globalConfigureNotification(builder, isNotify,
				MailChat.isNotifyRingtone(), MailChat.isNotifyVibrateOn());
		manager.notify(notifyId, builder.build());
	}

	private void notifyMail(int notifyId, Account  account,int count,List<Message> mails) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				mApplication);
		builder.setSmallIcon(R.drawable.icon_notification_mail_small);
		builder.setLargeIcon(BitmapFactory.decodeResource(
				mApplication.getResources(), R.drawable.icon_notification));
		builder.setWhen(System.currentTimeMillis());
		TaskStackBuilder stack;
		if (count == 1) {
			NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle(builder);
			Message localMessage = mails.get(0);
			// Modified by LL
			// BEGIN
			if (localMessage == null) {
				return;
			}
			// END
			style.bigText(getMessagePreview(mApplication, localMessage));
			builder.setContentTitle(getMailFrom(localMessage));
			builder.setContentText(localMessage.getSubject());
			builder.setSubText(account.getEmail());
			builder.setStyle(style);
			Intent i = MailDetialActivity.actionDisplayMessageIntentByNotify(mApplication, localMessage.makeMessageReference());
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			stack=buildNotifyPendingStack(mApplication, i);
		} else {
			NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(builder);
            for (Message m :mails) {
    			// Modified by LL
    			// BEGIN
    			if (m != null) {
                    style.addLine(buildMessageSummary(mApplication,splitOnEMailPushFrom(getMailFrom(m)),m.getSubject()));
    			}
    			// END
            }
            style.setSummaryText(account.getEmail());
			String title = formteString(R.string.notify_mail_count, count);
			style.setBigContentTitle(title);
			builder.setContentTitle(title);
			Message m=mails.get(0);
			if (m == null) {
				return;
			}
			builder.setContentText(buildMessageSummary(mApplication,splitOnEMailPushFrom(getMailFrom(m))+":",m.getSubject()));
			builder.setSubText(account.getEmail());
			builder.setStyle(style);
			LocalSearch search = new LocalSearch();
			search.addAllowedFolder(Account.INBOX);
			search.addAccountUuid(account.getUuid());
			Intent i = MailNotifyPendingActivity.actionMailNotify(mApplication,
					search,false);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			stack=buildNotifyPendingStack(mApplication, i);
		}
		builder.setContentIntent(stack.getPendingIntent(notifyId, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT));
		builder.setDeleteIntent(NotificationActionService.getAcknowledgeIntent(mApplication, account));
		globalConfigureNotification(builder, isNotify,
				MailChat.isNotifyRingtone(), MailChat.isNotifyVibrateOn());
		manager.notify(notifyId, builder.build());
	}

	private void notifyOA(int notifyId,Account account,DChatMessage message, DChat dChat, int count,List<String> messages) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				mApplication);
		builder.setSmallIcon(R.drawable.icon_notification_mail_small);
		builder.setLargeIcon(BitmapFactory.decodeResource(
				mApplication.getResources(), R.drawable.icon_notification_oa));
		builder.setWhen(System.currentTimeMillis());
		String content =message.getMessageContent();
		builder.setTicker(content);
		//TaskStackBuilder stack;
		Intent i;
		if (count == 1) {
			String[] c =content.split(":");
			builder.setContentText(c[1]);
			builder.setSubText(account.getEmail());
			builder.setContentTitle(c[0]);
			if (account.isBindOA() && account.getoAHost() != null
					&& !account.isOAUser()) {
				i = OALoginActivity.actionLoginOAForIntent(mApplication,
						account);
				MobclickAgent.onEvent(mApplication, "open_login_oa");
			} else {
				i = WebViewWithErrorViewActivity
						.forwardOpenUrlActivity(
								mApplication,
								message.getURL() + "&p="
										+ Utility.getOAUserParam(account)
										+ "&type=" + account.getoALoginType(),
								null, account.getUuid(), 1,true);
			}
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			//stack=buildNotifyPendingStack(mApplication, i);
		} else {
			NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(builder);
            for (String m :messages) {
                String[]  c= m.split(":");
                style.addLine(buildMessageSummary(mApplication,c[0],c[1]));
            }
            style.setSummaryText(account.getEmail());
			String title = formteString(R.string.notify_chat_count, count);
			builder.setContentTitle(title);
			String m=messages.get(0);
			String[]  c= m.split(":");
			builder.setContentText(buildMessageSummary(mApplication,c[0]+":",c[1]));
			builder.setSubText(account.getEmail());
			style.setBigContentTitle(title);
			builder.setStyle(style);
			String URL = message.getURL();
			URL =URL.replace("eventId", "r");
			if (account.isBindOA() && account.getoAHost() != null
					&& !account.isOAUser()) {
				i = OALoginActivity.actionLoginOAForIntent(mApplication,
						account);
				MobclickAgent.onEvent(mApplication, "open_login_oa");
			}else{
				i=WebViewWithErrorViewActivity.forwardOpenUrlActivity(mApplication,URL +"&p="+Utility.getOAUserParam(account), null,account.getUuid(),1,true);
			}
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			//stack=buildNotifyPendingStack(mApplication, i);
		}
		PendingIntent pendingIntent =PendingIntent.getActivity(mApplication, notifyId, i, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
		builder.setContentIntent(pendingIntent);
		builder.setDeleteIntent(NotificationActionService.getAcknowledgeIntent(mApplication, account));
		//builder.setContentIntent(stack.getPendingIntent(notifyId, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT));
		globalConfigureNotification(builder, isNotify,
				MailChat.isNotifyRingtone(), MailChat.isNotifyVibrateOn());
		manager.notify(notifyId, builder.build());
	}

	/**
	 * 清除账户通知栏提醒
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-7-24
	 */
	public void notifyClean(Account account) {
		if(account!=null){
			chatNotificationData.remove(account.getAccountNumber()+CHAT_NOTIFICATION_ID);
			mailNotificationData.remove(account.getAccountNumber());
			mailNotificationDataFor2.remove(account.getAccountNumber());
			manager.cancel(account.getAccountNumber());
			manager.cancel(account.getAccountNumber()+CHAT_NOTIFICATION_ID);
			//OA相关
			chatOANotificationData.remove(account.getAccountNumber()+OA_ANNOUNCE_NOTIFICATION_ID);
			chatOANotificationData.remove(account.getAccountNumber()+OA_NEW_TRANS_NOTIFICATION_ID);
			manager.cancel(account.getAccountNumber()+OA_ANNOUNCE_NOTIFICATION_ID);
			manager.cancel(account.getAccountNumber()+OA_NEW_TRANS_NOTIFICATION_ID);
		}
	}

	/**
	 * 是否在这个时间段
	 * 
	 * @Description:
	 * @param firsttime
	 *            第一次提醒时间
	 * @param time
	 *            当前时间
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-7-24
	 */
	public boolean isBetween(long firstTime, long time) {
		return time - firstTime < 30000;
	}

	private String formteString(int resId, int value) {
		return String.format(mApplication.getString(resId), value);
	}

	/**
	 * 全局通知设置
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-2-9
	 */
	private void globalConfigureNotification(
			NotificationCompat.Builder builder, boolean isNotify,
			boolean isSound, boolean isVibrate) {
		int ledOnMS = MailChat.NOTIFICATION_LED_ON_TIME;
		int ledOffMS = MailChat.NOTIFICATION_LED_OFF_TIME;
		builder.setLights(Color.BLUE, ledOnMS, ledOffMS);
		if (isNotify) {
			if (isSound) {
				Uri uri = null;
				int select = MailChat.getSelectNotifyRingtone();
				if (select == 0) {
					uri = RingtoneManager.getActualDefaultRingtoneUri(
							mApplication, RingtoneManager.TYPE_NOTIFICATION);
				} else {
					RingtoneManager rm = new RingtoneManager(mApplication);
					rm.setType(RingtoneManager.TYPE_NOTIFICATION);
					rm.getCursor();
					uri = rm.getRingtoneUri(select - 1);
				}
				builder.setSound(uri);
			}
			if (isVibrate) {
				builder.setVibrate(new long[] { 300, 300, 300, 300 });
			}
		}
	}

	private CharSequence buildMessageSummary(Context context,
			CharSequence sender, CharSequence subject) {
		if (sender == null) {
			return subject;
		}

		SpannableStringBuilder summary = new SpannableStringBuilder();
		summary.append(sender);
		summary.append(" ");
		summary.append(subject);

		summary.setSpan(getEmphasizedSpan(context), 0, sender.length(), 0);

		return summary;
	}

	private TextAppearanceSpan getEmphasizedSpan(Context context) {
		TextAppearanceSpan sEmphasizedSpan = null;
		if (sEmphasizedSpan == null) {
			sEmphasizedSpan = new TextAppearanceSpan(context,
					R.style.TextAppearance_StatusBar_EventContent_Emphasized);
		}
		return sEmphasizedSpan;
	}
	
    private CharSequence getMessagePreview(Context context, Message message) {
        CharSequence subject = getMessageSubject(context, message);
        String snippet = message.getPreview();

        if (TextUtils.isEmpty(subject)) {
            return snippet;
        } else if (TextUtils.isEmpty(snippet)) {
            return subject;
        }

        SpannableStringBuilder preview = new SpannableStringBuilder();
        preview.append(subject);
        preview.append('\n');
        preview.append(snippet);

        preview.setSpan(getEmphasizedSpan(context), 0, subject.length(), 0);

        return preview;
    }
    private CharSequence getMessageSubject(Context context, Message message) {
        String subject = message.getSubject();
        if (!TextUtils.isEmpty(subject)) {
            return subject;
        }

        return context.getString(R.string.general_no_subject);
    }
    private String getMailFrom(Message localMessage){
		String fromEmail="";
		Address[] address =localMessage.getFrom();
		if(address.length!=0){
			if(address[0].getPersonal()!=null){
				fromEmail=address[0].getPersonal();
			}else{
				fromEmail=address[0].getAddress().split("@")[0].length()>0 ? address[0].getAddress().split("@")[0] : fromEmail;
			}
		}
		return fromEmail;
    }
	public static Map<Integer, List<Message>> getMailNotificationData() {
		return mailNotificationData;
	}
	
	private TaskStackBuilder buildNotifyPendingStack(Context context,Intent intent) {
		TaskStackBuilder stack = TaskStackBuilder.create(context);
		stack.addNextIntent(intent);
		return stack;
	}
	
	private String getChattingContent(CMessage message){
		String content =message.getContent();
		switch (message.getMessageType()) {
		case IMAGE:
			content = mApplication.getString(R.string.msg_img);
			break;
		case ATTACHMENT:
			content = mApplication.getString(R.string.msg_att);
			break;
		case VOICE:
			content = mApplication.getString(R.string.msg_voice);
			break;
		case LOCATION:
			content = mApplication.getString(R.string.msg_location);
			break;
		default:
			break;
		}
		return content;
	}
	
	private String getSingleChattingContent(DChatMessage message){
		String content=message.getMessageContent();;
		switch (message.getMessageType()) {
		case IMAGE:
			content = mApplication.getString(R.string.msg_img);
			break;
		case ATTACHMENT:
			content = mApplication.getString(R.string.msg_att);
			break;
		case VOICE:
			content = mApplication.getString(R.string.msg_voice);
			break;
		case LOCATION:
			content = mApplication.getString(R.string.msg_location);
			break;
		default:
			break;
		}
		return content;
	}
	
	//三五2.0邮箱使用
		public synchronized void onNew35MialFor2(String uuid,ContentOfPushMessage pushMessage) {
			Account account = Preferences.getPreferences(mApplication).getAccount(
					uuid);
			if (canMailNotify(account)) {
				if (isBetween(MailChat.getFirstTime(), System.currentTimeMillis())) {
					isNotify = false;
				} else {
					MailChat.setFirstTime(System.currentTimeMillis());
					MailChat.save(editor);
					editor.commit();
					isNotify = true;
				}
				int notifyId = account.getAccountNumber();
				List<ContentOfPushMessage> mailList=null;
				if(mailNotificationDataFor2.get(notifyId)!=null){
					mailList=mailNotificationDataFor2.get(notifyId);
				}else{
					mailList=new ArrayList<ContentOfPushMessage>();
				}
				mailList.add(0,pushMessage);
				mailNotificationDataFor2.put(notifyId,mailList);
				notifyMailFor2(notifyId, account,mailNotificationDataFor2.get(notifyId).size(),mailNotificationDataFor2.get(notifyId));
			}
		}
		//三五2.0邮箱使用
		private void notifyMailFor2(int notifyId, Account  account,int count,List<ContentOfPushMessage> mails) {
			NotificationCompat.Builder builder = new NotificationCompat.Builder(
					mApplication);
			builder.setSmallIcon(R.drawable.icon_notification_mail_small);
			builder.setLargeIcon(BitmapFactory.decodeResource(
					mApplication.getResources(), R.drawable.icon_notification));
			builder.setWhen(System.currentTimeMillis());
			TaskStackBuilder stack;
			if (count == 1) {
				//NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle(builder);
				ContentOfPushMessage pushMessage =mails.get(0);
				//style.bigText(getMessagePreview(mApplication, pushMessage));
				builder.setContentTitle(pushMessage.getFromer());
				builder.setContentText(pushMessage.getSubject());
				builder.setSubText(account.getEmail());
				//builder.setStyle(style);
			} else {
				NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(builder);
	            for (ContentOfPushMessage m :mails) {
                    style.addLine(buildMessageSummary(mApplication,splitOnEMailPushFrom(m.getFromer()),m.getSubject()));
	            }
	            style.setSummaryText(account.getEmail());
				String title = formteString(R.string.notify_mail_count, count);
				style.setBigContentTitle(title);
				ContentOfPushMessage m=mails.get(0);
				builder.setContentText(buildMessageSummary(mApplication,splitOnEMailPushFrom(m.getFromer())+":",m.getSubject()));
				builder.setSubText(account.getEmail());
				builder.setContentTitle(title);
				builder.setStyle(style);
			}
			
			LocalSearch search = new LocalSearch();
			search.addAllowedFolder(Account.INBOX);
			search.addAccountUuid(account.getUuid());
			Intent i = MailNotifyPendingActivity.actionMailFor2Notify(mApplication,
					search,false,true);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			stack=buildNotifyPendingStack(mApplication, i);
			
			builder.setContentIntent(stack.getPendingIntent(notifyId, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT));
			builder.setDeleteIntent(NotificationActionService.getAcknowledgeIntent(mApplication, account));
			globalConfigureNotification(builder, isNotify,
					MailChat.isNotifyRingtone(), MailChat.isNotifyVibrateOn());
			manager.notify(notifyId, builder.build());
		}
		/**
		 * 截取邮件from名称
		 * 注：可能的情况 ：1.邱尚振<qiushzh@35.cn> 2.<qiushzh@35.cn> 3.qiushzh@35.cn等
		 * @Description:
		 * @see:
		 * @since:
		 * @author: shengli
		 * @date:2015-8-31
		 */
		private String splitOnEMailPushFrom(String from){
			int index = from.indexOf("<");
			if(index!=-1){
				if(index==0){
					int endIndex = from.indexOf(">");
					if(endIndex!=-1){
						from=from.substring(index+1,endIndex);
					}
				}else{
					from=from.substring(0, index);
				}
			}
			return from;
		}
}
