package cn.mailchat.chatting.beans;

import java.io.Serializable;
import java.util.Arrays;

import cn.mailchat.Account;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.utils.EncryptUtil;
import cn.mailchat.utils.StringUtil;


public class DChat implements Serializable{
	/** 
	* @Fields serialVersionUID : TODO
	*/ 
	private static final long serialVersionUID = 1L;
	
	/**
	 * 列表UID
	 */
	
	private String uid;
	
	/**
	 * 与谁的通讯
	 */
	private String email;
	/**
	 * 与谁的通讯昵称
	 */
	private String nickName="";
	/**
	 * 最后一条文本消息
	 */
	private String lastMessage;
	/**
	 * 最后一条消息类型
	 */
	private cn.mailchat.chatting.beans.DChatMessage.Type lastMessageType;
	
	/**
	 * 最后一条消息谁发的
	 */
	private String lastMessageEmail;
	/**
	 * 最后一条消息的时间
	 */
	private long lastTime;
	/**
	 * 是否置顶帖
	 */
	private boolean isSticked;
	/**
	 * 置顶时间
	 */
	private long stickedDate;
	
	private boolean isDChatAlert;
	/**
	 * 未读数
	 */
	private int unReadCount;
	/**
	 * 头像
	 */
	private String imgHead;
	/**
	 * 头像hash
	 */
	private String imgHeadHash;
	private boolean isVisibility =true;
	//草稿
	private String  draftContent ;
	//是否存在草稿
	private boolean isDraft;
	//发送中状态
	private int messageState;
	// 输入的状态,1 文字,2 语音
	private int inputType;
	// 单聊类型
	private Type dChatType =Type.NORMAL;//默认普通消息
	// 未处理
	private boolean isUnTreated;

	public boolean isUnTreated() {
		return isUnTreated;
	}

	public void setUnTreated(boolean isUnTreated) {
		this.isUnTreated = isUnTreated;
	}

	public Type getdChatType() {
		return dChatType;
	}

	public void setdChatType(Type dChatType) {
		this.dChatType = dChatType;
	}

	public int getInputType() {
		return inputType;
	}

	public void setInputType(int inputType) {
		this.inputType = inputType;
	}

	public int getMessageState() {
		return messageState;
	}

	public void setMessageState(int messageState) {
		this.messageState = messageState;
	}

	public boolean isDraft() {
		return isDraft;
	}

	public void setDraft(boolean isDraft) {
		this.isDraft = isDraft;
	}

	public String getDraftContent() {
		return draftContent;
	}

	public void setDraftContent(String draftContent) {
		this.draftContent = draftContent;
	}

	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getLastMessage() {
		return lastMessage;
	}
	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}
	public String getLastMessageEmail() {
		return lastMessageEmail;
	}
	public void setLastMessageEmail(String lastMessageEmail) {
		this.lastMessageEmail = lastMessageEmail;
	}
	public long getLastTime() {
		return lastTime;
	}
	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}
	public boolean isSticked() {
		return isSticked;
	}
	public void setSticked(boolean isSticked) {
		this.isSticked = isSticked;
	}
	public long getStickedDate() {
		return stickedDate;
	}
	public void setStickedDate(long stickedDate) {
		this.stickedDate = stickedDate;
	}
	public boolean isDChatAlert() {
		return isDChatAlert;
	}
	public void setDChatAlert(boolean isDChatAlert) {
		this.isDChatAlert = isDChatAlert;
	}
	public cn.mailchat.chatting.beans.DChatMessage.Type getLastMessageType() {
		return lastMessageType;
	}
	public void setLastMessageType(cn.mailchat.chatting.beans.DChatMessage.Type lastMessageType) {
		this.lastMessageType = lastMessageType;
	}
	
	public int getUnReadCount() {
		return unReadCount;
	}
	public void setUnReadCount(int unReadCount) {
		this.unReadCount = unReadCount;
	}
	public String getImgHead() {
		return imgHead;
	}
	public void setImgHead(String imgHead) {
		this.imgHead = imgHead;
	}
	public String getImgHeadHash() {
		return imgHeadHash;
	}
	public void setImgHeadHash(String imgHeadHash) {
		this.imgHeadHash = imgHeadHash;
	}
	public boolean isVisibility() {
		return isVisibility;
	}
	public void setVisibility(boolean isVisibility) {
		this.isVisibility = isVisibility;
	}
	/**
	 * 根据单聊消息来构造单聊列表项
	 * 
	 * @Description:
	 * @param DMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-9-5
	 */
	public static DChat structureDchat(DChatMessage message,Account account){
		DChat dchat =new DChat();
		dchat.setUid(message.getDchatUid());
		String name =null;
		if(account.getEmail().equals(message.getReceiverEmail())){
			name=message.getSenderEmail();
		}else {
			name=message.getReceiverEmail();
		}
		dchat.setEmail(name);
		DChatMessage.Type type = message.getMessageType();
		if(type==DChatMessage.Type.FROM_MAIL_INFO){
			dchat.setLastMessage(String.format(MailChat.getInstance().getString(R.string.mail_info_to_chat_subject), message.getMailFromNickName(),message.getMailSubject()));
		}else{
			dchat.setLastMessage(message.getMessageContent());
		}
		dchat.setLastMessageType(type);
		dchat.setLastMessageEmail(message.getSenderEmail());
		dchat.setLastTime(message.getTime());
		dchat.setDChatAlert(true);
		dchat.setSticked(false);
		dchat.setMessageState(message.getMessageState());
		return dchat;
	}
	
	/**
	 * 生成单聊列表UID
	 * 
	 * @Description:
	 * @param twoEmail 注意："toEmail,fromEmail"的格式
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-8-22
	 */
	public static String getDchatUid(String twoEmail){	
		String[] arrayTowEmail =twoEmail.split(",");
		Arrays.sort(arrayTowEmail); 
		return EncryptUtil.getMd5(arrayTowEmail[0]+","+arrayTowEmail[1]);
	}
	/**
	 * @return the toNickName
	 */
	public String getNickName() {
		String name=null;
		if (nickName==null) {
			name=StringUtil.getPrdfixStr(email);
		}else {
			name=nickName;
		}
		return name;
	}
	/**
	 * @param toNickName the toNickName to set
	 */
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	/**
	 *
	 * @Description: 单聊类型
	 * @author: shengli
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date: 2015-08-11
	 */
	public enum Type {
		/**
		 * 正常单聊
		 */
		NORMAL,
		/**
		 * OA
		 */
		OA,
		/**
		 * 可点击跳转的聊天条目，后期需求(没办法，只能定义在这里)
		 */
		JUMP
	}
}
