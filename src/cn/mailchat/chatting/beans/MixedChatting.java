package cn.mailchat.chatting.beans;

/**
 * 用于列表展示 群聊和单聊的混合封装类
 * 
 * @Description:
 * @author:shengli
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2014-08-22
 */
public class MixedChatting {

	private int id;
	private String uid;
	private long sendDate;
	private DChat dchat;
	private CGroup group;
	private boolean isGroup;
	private boolean IsSticked;
	
	/**
	 * 通过邮件生成混合聊天列表
	 * 
	 * @Description:
	 * @param DChat
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-08-22
	 */
	public static MixedChatting build(DChat dchat) {
		MixedChatting mixedMessage = new MixedChatting();
		mixedMessage.setDchat(dchat);
		mixedMessage.setSendTime(dchat.getLastTime());
		mixedMessage.setUid(dchat.getUid());
		mixedMessage.isGroup = false;
		return mixedMessage;
	}
	

	/**
	 * 通过群组生成混合消息
	 * 
	 * @Description:
	 * @param group
	 * @return
	 * @see:
	 * @since:
	 * @author: xulei
	 * @date:Jan 13, 2014
	 */
	public static MixedChatting build(CGroup group) {
		MixedChatting mixedMessage = new MixedChatting();
		mixedMessage.setGroup(group);
		mixedMessage.setUid(group.getUid());
		mixedMessage.isGroup = true;
		return mixedMessage;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public CGroup getGroup() {
		return group;
	}
	
	public DChat getDchat() {
		return dchat;
	}

	public void setDchat(DChat dchat) {
		this.dchat = dchat;
	}
	public void setGroup(CGroup group) {
		this.group = group;
	}

	public boolean isGroup() {
		return isGroup;
	}

	public void setGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}

	public long getSendTime() {
		return sendDate;
	}

	public void setSendTime(long sendTime) {
		this.sendDate = sendTime;
	}
	
	public boolean isIsSticked() {
		return IsSticked;
	}

	public void setIsSticked(boolean isSticked) {
		IsSticked = isSticked;
	}
}
