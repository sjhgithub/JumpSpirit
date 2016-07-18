package cn.mailchat.chatting.beans;

import java.io.Serializable;
import java.util.List;

public class DChatMessage implements Serializable{
	/** 
	* @Fields serialVersionUID : TODO
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
		 * 系统的通知信息
		 */
		NOTIFICATION,
		/**
		 * 读信页面发起消息透传
		 */
		FROM_MAIL_INFO,

		//现在没用，占个位置
		//start
		/**
		 * 图片+文字
		 */
		TEXT_IMAGE,
		/**
		 * 图片+文字+附件
		 */
		TEXT_IMAGE_ATTACHMENT,
		/**
		 * 图片+附件
		 */
		IMAGE_ATTACHMENT,
		//end

		/**
		 * OA公告消息
		 */
		OA_ANNOUNCE,
		/**
		 * OA代办消息
		 */
		OA_NEW_TRANS
	}

	private String uuid;
	//注意理解这两个
	/**
	 * 该消息发给谁，即谁收到的。可以理解为receiverEmail，原toEmail
	 */
	private String receiverEmail;
	/**
	 * 该消息来自谁，即谁发的。可以理解为senderEmail，原fromEmail
	 */
	private String senderEmail;

	/**
	 * 文本消息内容
	 */
	private String messageContent;
	/**
	 * 时间
	 */
	private long time;
	/**
	 * 删除标记
	 */
	private int deleteflag;
	/**
	 * 消息类型
	 */
	private Type messageType;
	/**
	 * 消息状态 0 发送成功  1发送失败  2.发送中
	 */
	private int messageState;
	
	private List<DAttachment> attachments;

	/**
	 * 地理位置名称（仅 LOCATION 类型消息有该属性）
	 */
	private String locationName;

	/**
	 * 地理属性（仅 LOCATION 类型消息有该属性）
	 */
	private String locationType;

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
	 * 消息所属列表UID
	 */
	private String dchatUid;
	/**
	 * URL OA链接等
	 */
	private String URL;
	/**
	 * OA事件ID
	 */
	private String OAId;
	/**
	 * OA公告或发起人
	 */
	private String OAFrom;
	/**
	 * OA公告或发起人
	 */
	private String OASubject;

	private int readFlag;
	/**
	 * 邮件到消息透传来自谁。(注：单聊的这里的email和属性toEmail是一致的。)
	 */
	private String mailFrom;
	private String mailFromNickName;
	/**
	 * 邮件到消息透传主题
	 */
	private String mailSubject;
	/**
	 * 邮件到消息透传预览
	 */
	private String mailPreview;
	/**
	 * 以下主要是OA等系统消息，里面的发起人不一样，所以需要定义到消息里面来获取头像。
	 */
	//发送该消息的人的头像,可以理解为发送人的头像(fromEmailImgHeadHash==senderEmailImgHeadHash)
	private String imgHeadHash;
	//发送该消息的人的昵称,可以理解为发送人的昵称(fromEmailNickName==senderEmailImgHeadHash)
	private String nickName;
	//服务端推送类型
	private int serverMessageType;

	public int getServerMessageType() {
		return serverMessageType;
	}

	public void setServerMessageType(int serverMessageType) {
		this.serverMessageType = serverMessageType;
	}

	public DChatMessage() {
		
	}

	public DChatMessage(Type dMessageType) {
		this.messageType = dMessageType;
		this.messageContent = "";
	}
	
	public String getDchatUid() {
		return dchatUid;
	}
	public void setDchatUid(String dchatUid) {
		this.dchatUid = dchatUid;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getReceiverEmail() {
		return receiverEmail;
	}

	public void setReceiverEmail(String receiverEmail) {
		this.receiverEmail = receiverEmail;
	}

	public String getSenderEmail() {
		return senderEmail;
	}

	public void setSenderEmail(String senderEmail) {
		this.senderEmail = senderEmail;
	}
	public String getMessageContent() {
		return messageContent;
	}
	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public int getDeleteflag() {
		return deleteflag;
	}
	public void setDeleteflag(int deleteflag) {
		this.deleteflag = deleteflag;
	}
	public Type getMessageType() {
		return messageType;
	}
	public void setMessageType(Type messageType) {
		this.messageType = messageType;
	}
	public int getMessageState() {
		return messageState;
	}
	public void setMessageState(int messageState) {
		this.messageState = messageState;
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
	public List<DAttachment> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<DAttachment> attachments) {
		this.attachments = attachments;
	}
	public String getLocationType() {
		return locationType;
	}
	public void setLocationType(String locationType) {
		this.locationType = locationType;
	}
	public String getURL() {
		return URL;
	}
	public void setURL(String URL) {
		this.URL = URL;
	}
	public String getOAId() {
		return OAId;
	}

	public void setOAId(String oAId) {
		OAId = oAId;
	}

	public String getOAFrom() {
		return OAFrom;
	}

	public void setOAFrom(String oAFrom) {
		OAFrom = oAFrom;
	}

	public String getOASubject() {
		return OASubject;
	}

	public void setOASubject(String oASubject) {
		OASubject = oASubject;
	}

	public int getReadFlag() {
		return readFlag;
	}

	public void setReadFlag(int readFlag) {
		this.readFlag = readFlag;
	}

	public String getImgHeadHash() {
		return imgHeadHash;
	}

	public void setImgHeadHash(String imgHeadHash) {
		this.imgHeadHash = imgHeadHash;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getMailFrom() {
		return mailFrom;
	}

	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
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

	public String getMailFromNickName() {
		return mailFromNickName;
	}

	public void setMailFromNickName(String mailFromNickName) {
		this.mailFromNickName = mailFromNickName;
	}

}
