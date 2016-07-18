package com.c35.mtd.pushmail.beans;

/**
 * 
 * @Description:描述 附件的BEAN
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class C35Attachment implements Cloneable {

	private String id; // 附件id
	private String fileName; // 附件名
	private String cid; // 当为内嵌资源时，有cid
	private int isIcon; // 0：否；1是 是否是处理过后的图片数据，当为内嵌资源时，有效
	private long fileSize; // 附件大小

	private String mailId; // 原邮件id(EML转发时用)
	private int type; // 附件类型:1=附件 2=内嵌资源 3=整封EML邮件发送 4=原邮件的附件
	private String contentType; // 是图片还是文件
	private String path; // 本地存储的完整路径
	private String content_uri; // provider的Uri
	private int downState; // 下载状态.0未下载，1下载完成
	public static final int NOT_DOWNLOAD = 0;
	public static final int DOWNLOADED = 1;
	private int sID; // 数据库第一列的序列id
	private long time;//语音时长
	private boolean compress = false;// 是否是压缩包
	private int compressItemNum; // 附件压缩包内文件个数（用于终端提示用户）
									// ,-1：非压缩类型附件,n：文件个数(大于20时，服务器不返回CompressItem对象内该附件id的项目)

	public int getCompressItemNum() {
		return compressItemNum;
	}

	public void setCompressItemNum(int compressItemNum) {
		this.compressItemNum = compressItemNum;
	}

	private String sourceAttachmentId;
	private String sourceMessageUid;

	public String getSourceMessageUid() {
		return sourceMessageUid;
	}

	public void setSourceMessageUid(String sourceMessageUid) {
		this.sourceMessageUid = sourceMessageUid;
	}

	public String getSourceAttachmentId() {
		return sourceAttachmentId;
	}

	public void setSourceAttachmentId(String sourceAttachmentId) {
		this.sourceAttachmentId = sourceAttachmentId;
	}

	public boolean isCompress() {
		return compress;
	}

	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	public int getsID() {
		return sID;
	}

	public void setsID(int sID) {
		this.sID = sID;
	}

	public int getDownState() {
		return downState;
	}

	public void setDownState(int downState) {
		this.downState = downState;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getContent_uri() {
		return content_uri;
	}

	public void setContent_uri(String content_uri) {
		this.content_uri = content_uri;
	}

	public int getIsIcon() {
		return isIcon;
	}

	public void setIsIcon(int isIcon) {
		this.isIcon = isIcon;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getMailId() {
		return mailId;
	}

	public void setMailId(String mailId) {
		this.mailId = mailId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/*** 加入附件列表后新增 ***added by zhuanggy*/

	private String mailSubject;// 包含此附件的邮件标题 /****需要加入数据库字段***/
	private String folderId;// 所在邮件夹id
	private String fromAddr;// 发件人地址
	private long sendTime;// 发送时间
	private int accountId;// 所对应的账户id

	//以下供列表中显示用
	private boolean isDownloading = false;// 是否正在被下载
	private int downloadProcess = -1;// 下载进度
	
	public boolean isDownloading() {
		return isDownloading;
	}

	
	public void setDownloading(boolean isDownloading) {
		this.isDownloading = isDownloading;
	}

	
	public int getDownloadProcess() {
		return downloadProcess;
	}

	
	public void setDownloadProcess(int downloadProcess) {
		this.downloadProcess = downloadProcess;
	}


	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public long getSendTime() {
		return sendTime;
	}

	public void setSendTime(long sendTime) {
		this.sendTime = sendTime;
	}

	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}

	public String getFromAddr() {
		return fromAddr;
	}

	public void setFromAddr(String fromAddr) {
		this.fromAddr = fromAddr;
	}

	public String getMailSubject() {
		return mailSubject;
	}

	public void setMailSubject(String mailSubject) {
		this.mailSubject = mailSubject;
	}

	
	public long getTime() {
		return time;
	}

	
	public void setTime(long time) {
		this.time = time;
	}

	// public int getFileIconResource() {
	// return fileIconResource;
	// }
	//
	// public void setFileIconResource(int fileIconResource) {
	// this.fileIconResource = fileIconResource;
	// }

}
