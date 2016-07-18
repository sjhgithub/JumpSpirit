package cn.mailchat.chatting.beans;

import java.io.Serializable;

public class DAttachment implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int id;
	/**
	 * 所属消息ID
	 */
	private String messageUid;
	/**
	 * 附件ID
	 */
	private String attchmentId;
	/**
	 * 消息名称
	 */
	private String name;
	/**
	 * 消息大小
	 */
	private long size;
	/**
	 * 消息类型
	 */
	private String contentType;
	/**
	 * 附件路径
	 */
	private String filePath;
	
	/**
	 * 语音消息长度
	 */
	private int voiceLength;
	/**
	 * 语音消息已读标记
	 */
	private int readFlag;
	
	//图片是否为本地发送，判断路径使用
	private int forwardFlag;
	//本地附件标记 1为本地
	private int localPathFlag;
	
	private String fileid;//服务的附件需要
	
	private byte[] fileByte;
	
	private int downloadProgress;
	private boolean isDownloadPause = true;//默认是暂停（当进度为0时，表示未下载）;
	private int uploadProgress;
	
	private int imageWidth;
	private int imageHeight;
	private boolean isImageLoad =true;//默认下载图片;

	public byte[] getFileByte() {
		return fileByte;
	}

	public void setFileByte(byte[] fileByte) {
		this.fileByte = fileByte;
	}

	public String getMessageUid() {
		return messageUid;
	}

	public void setMessageUid(String messageUid) {
		this.messageUid = messageUid;
	}

	public String getAttchmentId() {
		return attchmentId;
	}

	public void setAttchmentId(String attchmentId) {
		this.attchmentId = attchmentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public int getVoiceLength() {
		return voiceLength;
	}

	public void setVoiceLength(int voiceLength) {
		this.voiceLength = voiceLength;
	}

	public int getReadFlag() {
		return readFlag;
	}

	public void setReadFlag(int readFlag) {
		this.readFlag = readFlag;
	}

	public int getForwardFlag() {
		return forwardFlag;
	}

	public void setForwardFlag(int forwardFlag) {
		this.forwardFlag = forwardFlag;
	}

	public int getLocalPathFlag() {
		return localPathFlag;
	}

	public void setLocalPathFlag(int localPathFlag) {
		this.localPathFlag = localPathFlag;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	@Override
	public boolean equals(Object o) {
		DAttachment a = (DAttachment) o;
		if (a.getFilePath() == null) {
			if (filePath == null) {
				return (a.getId() == id) ? true : false;
			} else {
				return false;
			}
		} else {
			return (a.getFilePath().equals(filePath) && a.getId() == id) ? true : false;
		}
	}

	@Override
	public int hashCode() {
		return ("" + filePath + id + size).hashCode();
	}

	public String getFileid() {
		return fileid;
	}

	public void setFileid(String fileid) {
		this.fileid = fileid;
	}

	public int getDownloadProgress() {
		return downloadProgress;
	}

	public void setDownloadProgress(int downloadProgress) {
		this.downloadProgress = downloadProgress;
	}

	public boolean isDownloadPause() {
		return isDownloadPause;
	}

	public void setDownloadPause(boolean isDownloadPause) {
		this.isDownloadPause = isDownloadPause;
	}

	public int getUploadProgress() {
		return uploadProgress;
	}

	public void setUploadProgress(int uploadProgress) {
		this.uploadProgress = uploadProgress;
	}

	public int getImageWidth() {
		return imageWidth;
	}

	public void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
	}

	public int getImageHeight() {
		return imageHeight;
	}

	public void setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
	}

	public boolean isImageLoad() {
		return isImageLoad;
	}

	public void setImageLoad(boolean isImageLoad) {
		this.isImageLoad = isImageLoad;
	}
}
