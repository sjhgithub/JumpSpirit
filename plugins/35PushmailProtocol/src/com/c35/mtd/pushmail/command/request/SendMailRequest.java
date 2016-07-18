package com.c35.mtd.pushmail.command.request;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @Description:SendMail命令请求
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class SendMailRequest extends BaseRequest {

	// 用户
	private String user;
	// 0=不海外转发 1=海外转发
	private int oversea;
	// 独立发送 =0不 1=独立
	private int byOneSelf;
	// 是否保存到服务器的已发送箱 1=保存 0=不保存
	private int saveSent;
	// 旧邮件id,用于回复及转发的操作的邮件id
	private String reMailId;
	// 发送类型0=普通发送 1=回复 2=转发
	private int sendType;
	// 草稿邮件的id
	private String draftMailId;
	// 重要性 1=高 5=低
	private int priority;
	// 会话的id
	private String mailsessionId;
	// 发送时间,如果为空，表示立即发送
	private String timingSendTime;
	// 发送人
	private String from;
	// 收件人
	private List<String> to;
	// 抄送人
	private List<String> cc;
	// 暗送人
	private List<String> bcc;
	// 主题
	private String subject;
	// 回执标识 0=不 1=要
	private int ask;
	// 邮件富文本
	private String hyperText;
	// Webmail默认用UTF-8
	private String hyperTextCharset;
	// 邮件纯文本(如果纯文本为空/需转富文本当纯文本)
	private String plainText;
	// 回复到
	private String replyTo;
	// 签名id
	private String signId;
	// 纯文本编码
	private String plainTextCharset;
	// 来自哪个系统
	private String sourceSystem;
	// 客户数据
	private String sourceSystemData;
	// 外部系统处理状态
	private int sourceProcessState;
	// 会议地点
	private String calendarLocation;
	// 会议开始时间
	private String calendarStartTime;
	// 会议结束时间
	private String calendarEndTime;
	// 会议提醒时间
	private String calendarRemindTime;
	// 邮件提醒
	private String remindTime;
	// 资源文件或附件
	private List<AttachmentRequest> attachments;

	public SendMailRequest() {
		user = "";
		oversea = 0;
		// 独立发送 =0不 1=独立
		byOneSelf = 0;
		// 是否保存收件箱 0=保存 1=不保存
		saveSent = 0;
		// 旧邮件id,用于回复及转发的操作的邮件id
		reMailId = "";
		// 发送类型0=普通发送 1=回复 2=转发
		sendType = 0;
		// 草稿邮件的id
		draftMailId = "";
		// 重要性 1=高 5=低
		priority = 0;
		// 会话的id
		mailsessionId = "";
		// 发送时间,如果为空，表示立即发送
		timingSendTime = "";
		// 发送人
		from = "";
		// 收件人
		to = new ArrayList<String>();
		// 抄送人
		cc = new ArrayList<String>();
		// 暗送人
		bcc = new ArrayList<String>();
		// 主题
		subject = "";
		// 回执标识 0=不 1=要
		ask = 0;
		// 邮件富文本
		hyperText = "";
		// Webmail默认用UTF-8
		hyperTextCharset = "";
		// 邮件纯文本(如果纯文本为空/需转富文本当纯文本)
		plainText = "";
		// 回复到
		replyTo = "";
		// 签名id
		signId = "";
		// 纯文本编码
		plainTextCharset = "";
		// 来自哪个系统
		sourceSystem = "";
		// 客户数据
		sourceSystemData = "";
		// 外部系统处理状态
		sourceProcessState = 0;
		// 会议地点
		calendarLocation = "";
		// 会议开始时间
		calendarStartTime = "";
		// 会议结束时间
		calendarEndTime = "";
		// 会议提醒时间
		calendarRemindTime = "";
		// 邮件提醒
		remindTime = "";
		// 资源文件或附件
		attachments = new ArrayList<AttachmentRequest>();
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public int getOversea() {
		return oversea;
	}

	public void setOversea(int oversea) {
		this.oversea = oversea;
	}

	public int getByOneSelf() {
		return byOneSelf;
	}

	public void setByOneSelf(int byOneSelf) {
		this.byOneSelf = byOneSelf;
	}

	public int getSaveSent() {
		return saveSent;
	}

	public void setSaveSent(int saveSent) {
		this.saveSent = saveSent;
	}

	public String getReMailId() {
		return reMailId;
	}

	public void setReMailId(String reMailId) {
		this.reMailId = reMailId;
	}

	public int getSendType() {
		return sendType;
	}

	public void setSendType(int sendType) {
		this.sendType = sendType;
	}

	public String getDraftMailId() {
		return draftMailId;
	}

	public void setDraftMailId(String draftMailId) {
		this.draftMailId = draftMailId;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getMailsessionId() {
		return mailsessionId;
	}

	public void setMailsessionId(String mailsessionId) {
		this.mailsessionId = mailsessionId;
	}

	public String getTimingSendTime() {
		return timingSendTime;
	}

	public void setTimingSendTime(String timingSendTime) {
		this.timingSendTime = timingSendTime;
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

	public int getAsk() {
		return ask;
	}

	public void setAsk(int ask) {
		this.ask = ask;
	}

	public String getHyperText() {
		return hyperText;
	}

	public void setHyperText(String hyperText) {
		this.hyperText = hyperText;
	}

	public String getHyperTextCharset() {
		return hyperTextCharset;
	}

	public void setHyperTextCharset(String hyperTextCharset) {
		this.hyperTextCharset = hyperTextCharset;
	}

	public String getPlainText() {
		return plainText;
	}

	public void setPlainText(String plainText) {
		this.plainText = plainText;
	}

	public String getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	public String getSignId() {
		return signId;
	}

	public void setSignId(String signId) {
		this.signId = signId;
	}

	public String getPlainTextCharset() {
		return plainTextCharset;
	}

	public void setPlainTextCharset(String plainTextCharset) {
		this.plainTextCharset = plainTextCharset;
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

	public int getSourceProcessState() {
		return sourceProcessState;
	}

	public void setSourceProcessState(int sourceProcessState) {
		this.sourceProcessState = sourceProcessState;
	}

	public String getCalendarLocation() {
		return calendarLocation;
	}

	public void setCalendarLocation(String calendarLocation) {
		this.calendarLocation = calendarLocation;
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

	public String getCalendarRemindTime() {
		return calendarRemindTime;
	}

	public void setCalendarRemindTime(String calendarRemindTime) {
		this.calendarRemindTime = calendarRemindTime;
	}

	public String getRemindTime() {
		return remindTime;
	}

	public void setRemindTime(String remindTime) {
		this.remindTime = remindTime;
	}

	public List<AttachmentRequest> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<AttachmentRequest> attachments) {
		this.attachments = attachments;
	}

	@Override
	public String toString() {
		return "SendMailRequest [user=" + user + ", oversea=" + oversea + ", byOneSelf=" + byOneSelf + ", saveSent=" + saveSent + ", reMailId=" + reMailId + ", sendType=" + sendType + ", draftMailId=" + draftMailId + ", priority=" + priority + ", mailsessionId=" + mailsessionId + ", timingSendTime=" + timingSendTime + ", from=" + from + ", to=" + to + ", cc=" + cc + ", bcc=" + bcc + ", subject=" + subject + ", ask=" + ask + ", hyperText=" + hyperText + ", hyperTextCharset=" + hyperTextCharset + ", plainText=" + plainText + ", replyTo=" + replyTo + ", signId=" + signId + ", plainTextCharset=" + plainTextCharset + ", sourceSystem=" + sourceSystem + ", sourceSystemData=" + sourceSystemData + ", sourceProcessState=" + sourceProcessState + ", calendarLocation=" + calendarLocation + ", calendarStartTime=" + calendarStartTime + ", calendarEndTime=" + calendarEndTime + ", calendarRemindTime=" + calendarRemindTime + ", remindTime=" + remindTime + ", attachments=" + attachments + "]";
	}
}
