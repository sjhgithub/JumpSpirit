package com.c35.mtd.pushmail.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @Description:描述MAIL的bean
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class C35Message extends C35MessageInfo implements Serializable, Comparable<C35Message> {

	private static final long serialVersionUID = 1L;

	/**
	 * 会议状态(0=初始 1=接受 2=拒绝 3:可能)
	 */
	public static final int CALENDAR_STATE_DEFAULT = 0;
	public static final int CALENDAR_STATE_CONFIRMED = 1;
	public static final int CALENDAR_STATE_CANCELLED = 2;
	public static final int CALENDAR_STATE_TENTATIVE = 3;

	/**
	 * 主送我，重要发件人，紧急邮件
	 * 
	 */
	public static final int IS_IMPORTANT_FROM = 1; // 重要发件人
	public static final int IS_PRIORITY = 5;// 紧急邮件
	public static final int IS_SEND_ME = 1;// 主送我

	/**
	 * 邮件类型.0=普通邮件 1=会议邮件 2=自摧毁邮 3=定时邮件
	 */
	public static final int MAIL_TYPE_DEFAULT = 0;
	public static final int MAIL_TYPE_CALENDAR = 1;
	public static final int MAIL_TYPE_SUICIDE = 2;
	public static final int MAIL_TYPE_FIXED_TIME = 3;

	/* download flags */
	public static final int DOWNLOAD_FLAG_ENVELOPE = 0;
	public static final int DOWNLOAD_FLAG_PARTIAL = 1;
	public static final int DOWNLOAD_FLAG_FULL = 2;
	/* status flags */
	// 已读
	public static final int READ_FLAG_UNREAD = 0;
	public static final int READ_FLAG_SEEN = 1;

	// 主送我
	public static final int FLAG_TO_ME = 1;
	public static final int FLAG_NOT_TO_ME = 0;
	// 重要发件人
	public static final int FLAG_IMPORTENT_FROM = 1;
	public static final int FLAG_NOT_IMPORTENT_FROM = 0;
	// 紧急邮件
	public static final int FLAG_URGENCY = 1;
	// 已转发
	public static final int FORWARDED = 2;
	// 已回复
	public static final int REPLAIED = 1;
	/* delete flag */
	// 正常邮件
	public static final int DELETE_FLAG_NORMOR = 0;
	// 邮件被删除
	public static final int DELETE_FLAG_DELETED = 1;
	// /彻底删除
	public static final int DELETE_FLAG_DESTORYED = 2;
	/* sending flags */
	public static final int SEND_IN_PROGRESS = 0;
	public static final int SEND_FAILED = 1;
	public static final int SEND_SUCCESS=2;
	/* favorite flag */
	public static final int FAVORITE_FLAG_NORMOR = 0;//不标星
	public static final int FAVORITE_FLAG_FAV = 1;//标星
	private String reMailId;
	//
	private int sendType = 0;
	private int sendStats = -1;
	private String from; // 发件地址
	private List<String> to; // 收件人
	private List<String> cc; // 抄送人
	private List<String> bcc; // 暗送人
	private String subject; // 邮件主题
	private String plainText; // 纯文本正文
	private String hyperText; // 超文本正文
	private String calendarText; // 会议正文
	private String plainTextCharset; // 纯文本正文字符集
	private String hyperTextCharset; // 富文本正文字符集
	private String calendarTextCharset; // 会议正文字符集
	private Integer calendarState; // 会议状态(0=初始 1=接受 2=拒绝 3:可能)
	private String calendarStartTime; // 会议开始时间
	private String calendarEndTime; // 会议结束时间
	private String calendarLocation; // 会议地点
	private String calendarRemindTime; // 会议提醒时间
	private Integer calendarCloseRemind; // 是否关闭提醒0=否 1=关闭
	private Integer attachSize; // 附件个数
	private Integer priority; // 发送优先级;1:高紧急3：普通5：低
	private Integer read; // 是否已读 0=未读 1=已读
	private String reply; // 回复日期如(2010-05-13 12:00:00)，空表示未回复
	private String relay; // 转发日期如(2010-05-13 12:00:00)，空表示未转么
	private Integer acknowledgme; // 是否回执 0=不 1=要
	private long size; // 邮件体大小
	private long compressedToSize;// 预估的传输量大小,用于客户端显示压缩比
	private int deliveredReadCount;// 已读数
	private List<String> deliveredReadUsers;// 已读人员

	public long getCompressedToSize() {
		return compressedToSize;
	}

	public void setCompressedToSize(long compressedToSize) {
		this.compressedToSize = compressedToSize;
	}

	private Integer saveSent; // 是否保存收件箱 0=保存 1=不保存
	private Integer oversea; // 0=不海外转发 1=海外转发
	private String signId; // 签名id
	private String timingSendTime; // 定时发送时间
	private String replyTo; // 回复地址
	private Integer byOneSelf; // 独立发送 =0不 1=独立
	private List<Label> labels; // 已贴标签
	private String folderId; // 邮件目录
	private String folderName;// 箱子名字 // TODO 目前当做folderId用
	private Integer reportMessage; // 是否是退信/回条 0=不 1=是
	private String senderEmail; // 发送者的邮件地址
	private String recallFlag; // 召回邮件用的标识
	private Integer mailType; // 邮件类型.0=普通邮件 1=会议邮件 2=自摧毁邮 3=定时邮件
	private Integer importantFlag; // 是否重要
	private Integer deliverStatus; /*
									 * 投递状态 0:不显示 1:表示投递中 2:投递成功 3：延迟投递（会再次尝试投递） 4：投递部分失败（存在收件人产生退信，不再尝试投递邮件）
									 * 5：进入邮件审核 （当为已发送目录中邮件时用）
									 */
	private List<C35Attachment> attachs; // 附件信息对象
	private List<C35CompressItem> compressItems; // 压缩条目对象

	// //////////////////////
	private String toText;
	private String fromDisplayName;// 来自"wennan"
	private boolean needShowPicturesSectionFlag = false;// 需要显示“显示图片”按钮
	private boolean hasBody = false;//body 已经下载过
	private boolean needRetry = false;// 需要显示重试
	private boolean loadFailed = false;//是否加载失败
	private int moveTop = 0;// 默认，1，向上，2，向下
	private List<String> nameOfdeliveredReadUsers;// 已读人员名字
	private boolean reseiversHasGroup = false;// 收件人中是否包含群组

	
	
	public boolean isReseiversHasGroup() {
		return reseiversHasGroup;
	}

	
	public void setReseiversHasGroup(boolean reseiversHasGroup) {
		this.reseiversHasGroup = reseiversHasGroup;
	}

	public List<String> getNameOfdeliveredReadUsers() {
		return nameOfdeliveredReadUsers;
	}

	
	public void setNameOfdeliveredReadUsers(List<String> nameOfdeliveredReadUsers) {
		this.nameOfdeliveredReadUsers = nameOfdeliveredReadUsers;
	}

	private boolean isLoadedInView;

	public boolean isLoadedInView() {
		return isLoadedInView;
	}

	public void setLoadedInView(boolean isLoadedInView) {
		this.isLoadedInView = isLoadedInView;
	}

	public int getMoveTop() {
		return moveTop;
	}

	public void setMoveTop(int moveTop) {
		this.moveTop = moveTop;
	}

	public boolean isNeedRetry() {
		return needRetry;
	}

	public void setNeedRetry(boolean needRetry) {
		this.needRetry = needRetry;
	}

	public boolean isHasBody() {
		return hasBody;
	}

	public void setHasBody(boolean hasBody) {
		this.hasBody = hasBody;
	}


	public boolean isNeedShowPicturesSectionFlag() {
		return needShowPicturesSectionFlag;
	}

	public void setNeedShowPicturesSectionFlag(boolean showPicturesSectionFlag) {
		this.needShowPicturesSectionFlag = showPicturesSectionFlag;
	}

	public String getFromDisplayName() {
		return fromDisplayName;
	}

	public void setFromDisplayName(String fromDisplayName) {
		this.fromDisplayName = fromDisplayName;
	}

	public String getFromMailAdderss() {
		return fromMailAdderss;
	}

	public void setFromMailAdderss(String fromMailAdderss) {
		this.fromMailAdderss = fromMailAdderss;
	}

	private String fromMailAdderss;

	public String getToText() {
		return toText;
	}

	public void setToText(String toText) {
		this.toText = toText;
	}

	public String getCcText() {
		return ccText;
	}

	public void setCcText(String ccText) {
		this.ccText = ccText;
	}

	private String ccText;
	private String bccText;

	
	public String getBccText() {
		return bccText;
	}

	
	public void setBccText(String bccText) {
		this.bccText = bccText;
	}

	public ArrayList<String> getToNameList() {
		return toNameList;
	}

	public void setToNameList(ArrayList<String> toNameList) {
		this.toNameList = toNameList;
	}

	public ArrayList<String> getToAddressList() {
		return toAddressList;
	}

	public void setToAddressList(ArrayList<String> toAddressList) {
		this.toAddressList = toAddressList;
	}

	public ArrayList<String> getCcNameList() {
		return ccNameList;
	}

	public void setCcNameList(ArrayList<String> ccNameList) {
		this.ccNameList = ccNameList;
	}

	public ArrayList<String> getCcAddressList() {
		return ccAddressList;
	}

	public void setCcAddressList(ArrayList<String> ccAddressList) {
		this.ccAddressList = ccAddressList;
	}

	private ArrayList<String> toNameList;
	private ArrayList<String> toAddressList;
	private ArrayList<String> ccNameList;
	private ArrayList<String> ccAddressList;
	private ArrayList<String> bccNameList;
	
	public ArrayList<String> getBccNameList() {
		return bccNameList;
	}

	
	public void setBccNameList(ArrayList<String> bccNameList) {
		this.bccNameList = bccNameList;
	}

	
	public ArrayList<String> getBccAddressList() {
		return bccAddressList;
	}

	
	public void setBccAddressList(ArrayList<String> bccAddressList) {
		this.bccAddressList = bccAddressList;
	}

	private ArrayList<String> bccAddressList;
	// /////////////////////////////

	private Integer sourceProcessState;
	private String sourceSystemData;
	private String sourceSystem;
	private int hyperTextSize; // 富文本大小
	// 下载状态 0 未下载 1未全部下载 2已全部下载
	private Integer downFalg;
	// 是否转发 0未转发 1已转发
	private Integer forwardFalg;
	// 是否回复 0未回复 1已回复
	private Integer replayFalg;
	// 是否删除 0未删除 1垃圾箱2彻底删除
	private Integer deleteFalg;
	private String preview;

	// 新增2012-3-22 luxf
	private Integer forward_replay_Falg;

	// 是否是重要联系人
	private int isImportantFrom;// 由于数据库里是int型，因此从服务器获得String结果后，Jason解析时转为int型存储
	// private int urgencyPriority;//紧急程度;
	//
	//
	// public int getUrgencyPriority() {
	// return urgencyPriority;
	// }
	//
	// public void setUrgencyPriority(int urgencyPriority) {
	// this.urgencyPriority = urgencyPriority;
	// }
	//
	private String txt;
	private boolean deleted;

	public int getImportantFrom() {
		return isImportantFrom;
	}

	public void setImportantFrom(int isImportantFrom) {
		this.isImportantFrom = isImportantFrom;
	}

	public Integer getForward_replay_Falg() {
		return forward_replay_Falg;
	}

	public void setForward_replay_Falg(Integer forwardReplayFalg) {
		forward_replay_Falg = forwardReplayFalg;
	}

	public C35Message() {
		super();
		// TODO Auto-generated constructor stub
		from = "";
		to = new ArrayList<String>(); // 收件人
		cc = new ArrayList<String>();// 抄送人
		bcc = new ArrayList<String>();// 暗送人
		subject = ""; // 邮件主题
		plainText = ""; // 纯文本正文
		hyperText = ""; // 超文本正文
		calendarText = ""; // 会议正文
		plainTextCharset = ""; // 纯文本正文字符集
		hyperTextCharset = ""; // 富文本正文字符集
		calendarTextCharset = ""; // 会议正文字符集
		calendarState = 0; // 会议状态(0=初始 1=接受 2=拒绝 3:可能)
		calendarStartTime = ""; // 会议开始时间
		calendarEndTime = ""; // 会议结束时间
		calendarLocation = ""; // 会议地点
		calendarRemindTime = ""; // 会议提醒时间
		calendarCloseRemind = 0; // 是否关闭提醒0=否 1=关闭
		attachSize = 0; // 附件个数
		// priority = 0; // 发送优先级
		read = 0; // 是否已读 0=未读 1=已读
		reply = ""; // 回复日期如(2010-05-13 12:00:00)，空表示未回复
		relay = ""; // 转发日期如(2010-05-13 12:00:00)，空表示未转么
		acknowledgme = 0; // 是否回执 0=不 1=要
		size = 0; // 邮件体大小
		saveSent = 0; // 是否保存收件箱 0=保存 1=不保存
		oversea = 0; // 0=不海外转发 1=海外转发
		signId = ""; // 签名id
		timingSendTime = ""; // 定时发送时间
		replyTo = ""; // 回复地址
		byOneSelf = 0; // 独立发送 =0不 1=独立
		labels = new ArrayList<Label>(); // 已贴标签
		folderId = ""; // 邮件目录
		reportMessage = 0; // 是否是退信/回条 0=不 1=是
		senderEmail = ""; // 发送者的邮件地址
		recallFlag = ""; // 召回邮件用的标识
		mailType = 0; // 邮件类型.0=普通邮件 1=会议邮件 2=自摧毁邮 3=定时邮件
		importantFlag = 0; // 是否重要
		deliverStatus = 0; /*
							 * 投递状态 0:不显示 1:表示投递中 2:投递成功 3：延迟投递（会再次尝试投递） 4：投递部分失败（存在收件人产生退信，不再尝试投递邮件） 5：进入邮件审核
							 * （当为已发送目录中邮件时用）
							 */
		attachs = new ArrayList<C35Attachment>(); // 附件信息对象
		sourceProcessState = 0;
		sourceSystemData = "";
		sourceSystem = "";
		hyperTextSize = 0; // 富文本大小
		reMailId = "";
		preview = "";
		priority = 3;// 默认紧急度；1:高紧急3：普通5：低
	}

	public String getPreview() {
		return preview;
	}

	public void setPreview(String preview) {
		this.preview = preview;
	}

	public String getReMailId() {
		return reMailId;
	}

	public void setReMailId(String reMailId) {
		this.reMailId = reMailId;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public int getSendStats() {
		return sendStats;
	}

	public void setSendStats(int sendStats) {
		this.sendStats = sendStats;
	}

	public int getSendType() {
		return sendType;
	}

	public void setSendType(int sendType) {
		this.sendType = sendType;
	}

	public Integer getDownFalg() {
		return downFalg;
	}

	public void setDownFalg(Integer downFalg) {
		this.downFalg = downFalg;
	}

	public Integer getForwardFalg() {
		return forwardFalg;
	}

	public void setForwardFalg(Integer forwardFalg) {
		this.forwardFalg = forwardFalg;
	}

	public Integer getReplayFalg() {
		return replayFalg;
	}

	public void setReplayFalg(Integer replayFalg) {
		this.replayFalg = replayFalg;
	}

	public Integer getDeleteFalg() {
		return deleteFalg;
	}

	public void setDeleteFalg(Integer deleteFalg) {
		this.deleteFalg = deleteFalg;
	}

	public void setSourceProcessState(Integer sourceProcessState) {
		this.sourceProcessState = sourceProcessState;
	}

	public String getSourceSystem() {
		return sourceSystem;
	}

	public void setSourceSystem(String sourceSystem) {
		this.sourceSystem = sourceSystem;
	}

	public String getSourceSystemData() {
		return sourceSystemData;
	}

	public void setSourceSystemData(String sourceSystemData) {
		this.sourceSystemData = sourceSystemData;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public List<String> getTo() {
		return to;
	}

	public void setTo(List<String> to) {
		this.to = to;
	}

	public List<String> getCc() {
		return cc;
	}

	public void setCc(List<String> cc) {
		this.cc = cc;
	}

	public List<String> getBcc() {
		return bcc;
	}

	public void setBcc(List<String> bcc) {
		this.bcc = bcc;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getPlainText() {
		return plainText;
	}

	public void setPlainText(String plainText) {
		this.plainText = plainText;
	}

	public String getHyperText() {
		return hyperText;
	}

	public void setHyperText(String hyperText) {
		this.hyperText = hyperText;
	}

	public String getCalendarText() {
		return calendarText;
	}

	public void setCalendarText(String calendarText) {
		this.calendarText = calendarText;
	}

	public String getPlainTextCharset() {
		return plainTextCharset;
	}

	public void setPlainTextCharset(String plainTextCharset) {
		this.plainTextCharset = plainTextCharset;
	}

	public String getHyperTextCharset() {
		return hyperTextCharset;
	}

	public void setHyperTextCharset(String hyperTextCharset) {
		this.hyperTextCharset = hyperTextCharset;
	}

	public String getCalendarTextCharset() {
		return calendarTextCharset;
	}

	public void setCalendarTextCharset(String calendarTextCharset) {
		this.calendarTextCharset = calendarTextCharset;
	}

	public String getCalendarStartTime() {
		return calendarStartTime;
	}

	public void setCalendarStartTime(String calendarStartTime) {
		this.calendarStartTime = calendarStartTime;
	}

	public String getCalendarEndTime() {
		return calendarEndTime;
	}

	public void setCalendarEndTime(String calendarEndTime) {
		this.calendarEndTime = calendarEndTime;
	}

	public String getCalendarLocation() {
		return calendarLocation;
	}

	public void setCalendarLocation(String calendarLocation) {
		this.calendarLocation = calendarLocation;
	}

	public String getCalendarRemindTime() {
		return calendarRemindTime;
	}

	public void setCalendarRemindTime(String calendarRemindTime) {
		this.calendarRemindTime = calendarRemindTime;
	}

	public Integer getRead() {
		return read;
	}

	public void setRead(Integer read) {
		this.read = read;
	}

	public String getReply() {
		return reply;
	}

	public void setReply(String reply) {
		this.reply = reply;
	}

	public String getRelay() {
		return relay;
	}

	public void setRelay(String relay) {
		this.relay = relay;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getSignId() {
		return signId;
	}

	public void setSignId(String signId) {
		this.signId = signId;
	}

	public String getTimingSendTime() {
		return timingSendTime;
	}

	public void setTimingSendTime(String timingSendTime) {
		this.timingSendTime = timingSendTime;
	}

	public String getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}

	public String getSenderEmail() {
		return senderEmail;
	}

	public void setSenderEmail(String senderEmail) {
		this.senderEmail = senderEmail;
	}

	public String getRecallFlag() {
		return recallFlag;
	}

	public void setRecallFlag(String recallFlag) {
		this.recallFlag = recallFlag;
	}

	public Integer getDeliverStatus() {
		return deliverStatus;
	}

	public void setDeliverStatus(Integer deliverStatus) {
		this.deliverStatus = deliverStatus;
	}

	public List<Label> getLabels() {
		return labels;
	}

	public void setLabels(List<Label> labels) {
		this.labels = labels;
	}

	public List<C35Attachment> getAttachs() {
		return attachs;
	}

	public void setAttachs(List<C35Attachment> attachs) {
		this.attachs = attachs;
	}

	public int getHyperTextSize() {
		return hyperTextSize;
	}

	public void setHyperTextSize(int hyperTextSize) {
		this.hyperTextSize = hyperTextSize;
	}

	public Integer getCalendarCloseRemind() {
		return calendarCloseRemind;
	}

	public void setCalendarCloseRemind(Integer calendarCloseRemind) {
		this.calendarCloseRemind = calendarCloseRemind;
	}

	public Integer getAttachSize() {
		return attachSize;
	}

	public void setAttachSize(Integer attachSize) {
		this.attachSize = attachSize;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public void setImportantFlag(Integer importantFlag) {
		this.importantFlag = importantFlag;
	}

	public Integer getCalendarState() {
		return calendarState;
	}

	public void setCalendarState(Integer calendarState) {
		this.calendarState = calendarState;
	}

	public Integer getAcknowledgme() {
		return acknowledgme;
	}

	public void setAcknowledgme(Integer acknowledgme) {
		this.acknowledgme = acknowledgme;
	}

	public Integer getSaveSent() {
		return saveSent;
	}

	public void setSaveSent(Integer saveSent) {
		this.saveSent = saveSent;
	}

	public Integer getOversea() {
		return oversea;
	}

	public void setOversea(Integer oversea) {
		this.oversea = oversea;
	}

	public Integer getMailType() {
		return mailType;
	}

	public void setMailType(Integer mailType) {
		this.mailType = mailType;
	}

	public Integer getByOneSelf() {
		return byOneSelf;
	}

	public void setByOneSelf(Integer byOneSelf) {
		this.byOneSelf = byOneSelf;
	}

	public Integer getReportMessage() {
		return reportMessage;
	}

	public void setReportMessage(Integer reportMessage) {
		this.reportMessage = reportMessage;
	}

	public Integer getImportantFlag() {
		return importantFlag;
	}

	public Integer getSourceProcessState() {
		return sourceProcessState;
	}

	public List<C35CompressItem> getCompressItems() {
		return compressItems;
	}

	public void setCompressItems(List<C35CompressItem> compressItems) {
		this.compressItems = compressItems;
	}

	@Override
	public String toString() {
		// return "C35Message [from=" + from + ", to=" + to + ", cc=" + cc + ", bcc=" + bcc + ", subject=" +
		// subject + ", plainText=" + plainText + ", hyperText=" + hyperText + ", calendarText=" +
		// calendarText + ", plainTextCharset=" + plainTextCharset + ", hyperTextCharset=" + hyperTextCharset
		// + ", calendarTextCharset=" + calendarTextCharset + ", calendarState=" + calendarState +
		// ", calendarStartTime=" + calendarStartTime + ", calendarEndTime=" + calendarEndTime +
		// ", calendarLocation=" + calendarLocation + ", calendarRemindTime=" + calendarRemindTime +
		// ", calendarCloseRemind=" + calendarCloseRemind + ", attachSize=" + attachSize + ", priority=" +
		// priority + ", read=" + read + ", reply=" + reply + ", relay=" + relay + ", acknowledgme=" +
		// acknowledgme + ", size=" + size + ", saveSent=" + saveSent + ", oversea=" + oversea + ", signId=" +
		// signId + ", timingSendTime=" + timingSendTime + ", replyTo=" + replyTo + ", byOneSelf=" + byOneSelf
		// + ", labels=" + labels + ", folderId=" + folderId + ", reportMessage=" + reportMessage +
		// ", senderEmail=" + senderEmail + ", recallFlag=" + recallFlag + ", mailType=" + mailType +
		// ", importantFlag=" + importantFlag + ", deliverStatus=" + deliverStatus + ", attachs=" + attachs +
		// ", sourceProcessState=" + sourceProcessState + ", sourceSystemData=" + sourceSystemData +
		// ", sourceSystem=" + sourceSystem + ", hyperTextSize=" + hyperTextSize + ", downFalg=" + downFalg +
		// ", forwardFalg=" + forwardFalg + ", replayFalg=" + replayFalg + ", deleteFalg=" + deleteFalg +
		// ",urgencyPriority=" + urgencyPriority + "]";
		return "C35Message [from=" + from + ", to=" + to + ", cc=" + cc + ", bcc=" + bcc + ", subject=" + subject + ", plainText=" + plainText + ", hyperText=" + hyperText + ", calendarText=" + calendarText + ", plainTextCharset=" + plainTextCharset + ", hyperTextCharset=" + hyperTextCharset + ", calendarTextCharset=" + calendarTextCharset + ", calendarState=" + calendarState + ", calendarStartTime=" + calendarStartTime + ", calendarEndTime=" + calendarEndTime + ", calendarLocation=" + calendarLocation + ", calendarRemindTime=" + calendarRemindTime + ", calendarCloseRemind=" + calendarCloseRemind + ", attachSize=" + attachSize + ", priority=" + priority + ", read=" + read + ", reply=" + reply + ", relay=" + relay + ", acknowledgme=" + acknowledgme + ", size=" + size + ", saveSent=" + saveSent + ", oversea=" + oversea + ", signId=" + signId + ", timingSendTime=" + timingSendTime + ", replyTo=" + replyTo + ", byOneSelf=" + byOneSelf + ", labels=" + labels + ", folderId=" + folderId + ", reportMessage=" + reportMessage + ", senderEmail=" + senderEmail + ", recallFlag=" + recallFlag + ", mailType=" + mailType + ", importantFlag=" + importantFlag + ", deliverStatus=" + deliverStatus + ", attachs=" + attachs + ", sourceProcessState=" + sourceProcessState + ", sourceSystemData=" + sourceSystemData + ", sourceSystem=" + sourceSystem + ", hyperTextSize=" + hyperTextSize + ", downFalg=" + downFalg + ", forwardFalg=" + forwardFalg + ", replayFalg=" + replayFalg + ", deleteFalg=" + deleteFalg + "]";
	}

	@Override
	public int compareTo(C35Message o) {
		int j = 0;
		if (o instanceof C35Message) {
			C35Message b = (C35Message) o;
			int i = getSendTime().compareTo(b.getSendTime());
			if (i > 0)
				j = 1;
			else if (i < 0)
				j = -1;
			else
				j = 0;
		}
		return j;
	}

	public String getTxt() {
		return txt;
	}

	public void setTxt(String txt) {
		this.txt = txt;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isLoadFailed() {
		return loadFailed;
	}

	public void setLoadFailed(boolean loadFailed) {
		this.loadFailed = loadFailed;
	}

	public int getDeliveredReadCount() {
		return deliveredReadCount;
	}

	public void setDeliveredReadCount(int deliveredReadCount) {
		this.deliveredReadCount = deliveredReadCount;
	}

	public List<String> getDeliveredReadUsers() {
		return deliveredReadUsers;
	}

	public void setDeliveredReadUsers(List<String> deliveredReadUsers) {
		this.deliveredReadUsers = deliveredReadUsers;
	}
}
