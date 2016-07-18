package cn.mailchat.chatting.beans;

import java.io.Serializable;

public class CAttachment implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int DOWNLOAD_STATE_UNDOWNLOAD = 0;
	public static final int DOWNLOAD_STATE_DOWNLOADED = 1;
	private int id;
	private String messageUid;
	private String attchmentId;
	private String name;
	private long size;
	private int downloadState;
	private String contentType;
	private String filePath;
	private int voiceLength;
	private int readFlag;
	//图片是否为本地发送，判断路径使用
	private int forwardFlag;
	private int localPathFlag;
	
	private String fileid;//服务的附近需要
	private byte[] fileByte;
	private int downloadProgress;
	private boolean isDownloadPause =true;//默认是暂停（当进度为0时，表示未下载）
	private int uploadProgress;
	
	private int imageWidth;
	private int imageHeight;
	private boolean isImageLoad =true;//默认下载图片

	public byte[] getFileByte() {
		return fileByte;
	}

	public void setFileByte(byte[] fileByte) {
		this.fileByte = fileByte;
	}

	public int getLocalPathFlag() {
		return localPathFlag;
	}

	public void setLocalPathFlag(int localPathFlag) {
		this.localPathFlag = localPathFlag;
	}

	public int getForwardFlag() {
		return forwardFlag;
	}

	public void setForwardFlag(int forwardFlag) {
		this.forwardFlag = forwardFlag;
	}

	public int getReadFlag() {
		return readFlag;
	}

	public void setReadFlag(int readFlag) {
		this.readFlag = readFlag;
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

	public int getDownloadState() {
		return downloadState;
	}

	public void setDownloadState(int downloadState) {
		this.downloadState = downloadState;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getFilePath() {
		return filePath == null ? "" : filePath;
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		CAttachment a = (CAttachment) o;
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
