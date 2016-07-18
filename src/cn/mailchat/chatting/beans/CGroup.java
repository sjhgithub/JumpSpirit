package cn.mailchat.chatting.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.mailchat.chatting.beans.CMessage.Type;
import cn.mailchat.utils.Address;

/**
 * 群组属性类
 * 
 * @Description:
 * @author:Zhonggaoyong
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2014-1-6
 */
public class CGroup implements Serializable {

	// 群id
	private String uid;
	// 名称
	private String groupName;
	// 是否私密
	private Boolean isPriv;
	// 头像
	private String avatar;
	// 描述
	private String desc;
	// 是否成员
	private Boolean isMember;
	// 是否管理员
	private Boolean isAdmin;
	// 用户数
	private int cUser;
	// 帖子数
	private int cPosts;
	// 成员列表
	private List<CGroupMember> members;
	// 群组类型
	private int groupType;
	// 是否有未处理的请求
	private boolean isUntreated;
	// 置顶帖置顶时间(默认-1)
	private long stickedDate = -1;
	// 是否置顶帖
	private Boolean isSticked;
	// 未处理数量
	private int unreadCount;
	private String lastMemberNickName;
	// 最后一条消息发送文本内容
	private String lastMessageContent;
	// 最后一条消息类型
	private Type lastMessageType;
	// 最后一条消息ID
	private String lastMessageUid;
	// 最后一条发送时间
	private long lastSendDate;
	// 群消息提醒
	private Boolean isMessageAlert;
	// 群消息声音提醒
	private Boolean isMessageVoiceReminder;
	// 输入的状态,1 文字,2 语音
	private int inputType;
	// 是否隐藏 0隐藏，1显示，默认1
	private int isVisibility = 1;
	//是否为第一次修改名称
	private boolean isReName;
	//草稿
	private String  draftContent ;
	//是否存在草稿
	private boolean isDraft;
	//发送状态  0 发送成功  1发送失败  2.发送中
	private int messageState;

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

	public Type getLastMessageType() {
		return lastMessageType;
	}

	public void setLastMessageType(Type lastMessageType) {
		this.lastMessageType = lastMessageType;
	}

	public int getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(int unreadCount) {
		this.unreadCount = unreadCount;
	}

	public long getLastSendDate() {
		return lastSendDate;
	}

	public void setLastSendDate(long lastSendDate) {
		this.lastSendDate = lastSendDate;
	}

	public Boolean getIsMessageAlert() {
		return isMessageAlert;
	}

	public void setIsMessageAlert(Boolean isMessageAlert) {
		this.isMessageAlert = isMessageAlert;
	}

	public String getLastMemberNickName() {
		return lastMemberNickName;
	}

	public void setLastMemberNickName(String lastMemberNickName) {
		this.lastMemberNickName = lastMemberNickName;
	}

	public String getLastMessageContent() {
		return lastMessageContent;
	}

	public void setLastMessageContent(String lastMessageContent) {
		this.lastMessageContent = lastMessageContent;
	}

	public String getLastMessageUid() {
		return lastMessageUid;
	}

	public void setLastMessageUid(String lastMessageUid) {
		this.lastMessageUid = lastMessageUid;
	}

	public long getStickedDate() {
		return stickedDate;
	}

	public void setStickedDate(long stickedDate) {
		this.stickedDate = stickedDate;
	}

	public Boolean getIsSticked() {
		return isSticked;
	}

	public void setIsSticked(Boolean isSticked) {
		this.isSticked = isSticked;
	}

	public void setMembers(List<CGroupMember> members) {
		this.members = members;
	}

	public Boolean getIsUntreated() {
		return isUntreated;
	}

	public void setIsUntreated(Boolean isUntreated) {
		this.isUntreated = isUntreated;
	}

	public int getGroupType() {
		return groupType;
	}

	public void setGroupType(int groupType) {
		this.groupType = groupType;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public Boolean getIsPriv() {
		return isPriv;
	}

	public void setIsPriv(Boolean isPriv) {
		this.isPriv = isPriv;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Boolean getIsMember() {
		return isMember;
	}

	public void setIsMember(Boolean isMember) {
		this.isMember = isMember;
	}

	public Boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public int getcUser() {
		return cUser;
	}

	public void setcUser(int cUser) {
		this.cUser = cUser;
	}

	public List<CGroupMember> getMembers() {
		return members;
	}

	public int getcPosts() {
		return cPosts;
	}

	public void setcPosts(int cPosts) {
		this.cPosts = cPosts;
	}

	public int getInputType() {
		return inputType;
	}

	public void setInputType(int inputType) {
		this.inputType = inputType;
	}

	public Boolean getIsMessageVoiceReminder() {
		return isMessageVoiceReminder;
	}

	public void setIsMessageVoiceReminder(Boolean isMessageVoiceReminder) {
		this.isMessageVoiceReminder = isMessageVoiceReminder;
	}

	public int getIsVisibility() {
		return isVisibility;
	}

	public void setIsVisibility(int isVisibility) {
		this.isVisibility = isVisibility;
	}
	
	public boolean isReName() {
		return isReName;
	}

	public void setReName(boolean isReName) {
		this.isReName = isReName;
	}
	/**
	 * 地址转换为成员
	 * 
	 * @Description:
	 * @param addresses
	 * @return
	 * @see:
	 * @since:
	 * @author: Zhonggaoyong
	 * @date:2014-1-6
	 */
//	public static ArrayList<CGroupMember> getAddressChangeMembers(List<Address> addresses) {
//		if (addresses != null) {
//			ArrayList<CGroupMember> membersTemp = new ArrayList<CGroupMember>();
//			for (Address s : addresses) {
//				CGroupMember member = new CGroupMember(s.getPersonal(), s.getAddress());
//				membersTemp.add(member);
//			}
//			return membersTemp;
//		}
//		return null;
//	}
	
	/**
	 * 地址转换为成员
	 * 
	 * method name: getAddressChangeMembers 
	 * function @Description: TODO
	 * Parameters and return values description：
	 * @param addresses
	 * @return    field_name
	 * ArrayList<CGroupMember>    return type
	 * @History memory：
	 * @Date：2014-11-10 下午5:36:03	@Modified by：zhangyq
	 * @Description：
	 */
	public static ArrayList<CGroupMember> getAddressChangeMembers(List<cn.mailchat.mail.Address> addresses) {
		if (addresses != null) {
			ArrayList<CGroupMember> membersTemp = new ArrayList<CGroupMember>();
			for (cn.mailchat.mail.Address s : addresses) {
				CGroupMember member = new CGroupMember(s.getPersonal(), s.getAddress());
				membersTemp.add(member);
			}
			return membersTemp;
		}
		
		return null;
	}

	public enum GroupListType {
		ALL, PAGEGROUP, SEARCHGROUP, MEGROUP
	}


}
