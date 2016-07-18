package cn.mailchat.chatting.beans;

import java.io.Serializable;


/**
 * 
 * @Description:群聊信息对象
 * @author:xulei
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:Dec 2, 2013
 */
public class CMessage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * 
	 * @Description: 群聊消息类型
	 * @author:xulei
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:Dec 2, 2013
	 */
	public enum Type {
		/**
		 * 纯文本消息
		 */
		TEXT,
		/**
		 * 图片消息
		 */
		IMAGE,
		/**
		 * 附件消息
		 */
		ATTACHMENT,
		/**
		 * 语音消息
		 */
		VOICE,
		/**
		 * 位置消息
		 */
		LOCATION,
		/**
		 * 系统的通知信息 <br>
		 * 比如，人员加入群组，退出群组等
		 */
		NOTIFICATION,
		/**
		 * 读信页面发起消息透传
		 */
		FROM_MAIL_INFO
	}

	/**
	 * 消息状态
	 * 
	 * @Description:
	 * @author:Zhonggaoyong
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:2014-1-11
	 */
	public enum State {
		/**
		 * 发送
		 */
		sending,
		/**
		 * 发送失败
		 */
		sendFail,
		/**
		 * 发送成功
		 */
		sendSuccess
	}

	/**
	 * 群UID
	 */
	private String groupUid;
	/**
	 * 消息的UID
	 */
	private String uid;
	/**
	 * 发送人
	 */
	private CGroupMember member;
	/**
	 * 发送时间
	 */
	private long sendTime;
	/**
	 * 消息类型
	 */
	private Type messageType;
	/**
	 * 消息内容（如果消息为 LOCATION，内容为具体的地址}）
	 */
	private String content;
	/**
	 * 消息附件（ATTACHMENT VOICE IMAGE等3种消息有附件）
	 */
	private CAttachment attachment;
	private String locationName;// 地理位置名称
	/**
	 * 地理位置（仅 LOCATION 类型消息有该属性）
	 */
	private String address;
	/**
	 * 经度（仅 LOCATION 类型消息有该属性）
	 */
	private double longitude;
	/**
	 * 纬度（仅 LOCATION 类型消息有该属性）
	 */
	private double latitude;

	/**
	 * 消息状态 0 未发送 1已发送 2服务器数据
	 */
	private State messageState;
	
	private int delete_flag;
	private int readFlag;
	// 服务端推送类型
	private int serverMessageType;
	/**
	 * 邮件到消息透传来自谁
	 */
	private String mailFrom;
	/**
	 * 邮件到消息透传自谁的昵称
	 */
	private String mailFromNickName;
	/**
	 * 邮件到消息透传主题
	 */
	private String mailSubject;
	/**
	 * 邮件到消息透传预览
	 */
	private String mailPreview;

	public int getServerMessageType() {
		return serverMessageType;
	}

	public void setServerMessageType(int serverMessageType) {
		this.serverMessageType = serverMessageType;
	}

	public CMessage() {
		this.messageType = Type.TEXT;
	}

	public CMessage(Type messageType) {
		this.messageType = messageType;
		this.content = "";
	}

	public String getGroupUid() {
		return groupUid;
	}

	public void setGroupUid(String groupUid) {
		this.groupUid = groupUid;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public long getSendTime() {
		return sendTime;
	}

	public void setSendTime(long sendTime) {
		this.sendTime = sendTime;
	}

	public CAttachment getAttachment() {
		return attachment;
	}

	public void setAttachment(CAttachment attachment) {
		this.attachment = attachment;
	}

	public State getMessageState() {
		return messageState;
	}

	public void setMessageState(State messageState) {
		this.messageState = messageState;
	}

	public Type getMessageType() {
		return messageType;
	}

	public void setMessageType(Type messageType) {
		this.messageType = messageType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the member
	 */
	public CGroupMember getMember() {
		return member;
	}

	/**
	 * @param member
	 *            the member to set
	 */
	public void setMember(CGroupMember member) {
		this.member = member;
	}

	
	public int getDelete_flag() {
		return delete_flag;
	}

	
	public void setDelete_flag(int delete_flag) {
		this.delete_flag = delete_flag;
	}

	public int getReadFlag() {
		return readFlag;
	}

	public void setReadFlag(int readFlag) {
		this.readFlag = readFlag;
	}

	public String getMailFrom() {
		return mailFrom;
	}

	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}

	public String getMailFromNickName() {
		return mailFromNickName;
	}

	public void setMailFromNickName(String mailFromNickName) {
		this.mailFromNickName = mailFromNickName;
	}

	public String getMailSubject() {
		return mailSubject;
	}

	public void setMailSubject(String mailSubject) {
		this.mailSubject = mailSubject;
	}

	public String getMailPreview() {
		return mailPreview;
	}

	public void setMailPreview(String mailPreview) {
		this.mailPreview = mailPreview;
	}

	@Override
	public boolean equals(Object obj) {
		CMessage s = (CMessage) obj;
		return uid.equals(s.uid) ;
	}

	@Override
	public int hashCode() {		
		return uid.hashCode();
	}
}
