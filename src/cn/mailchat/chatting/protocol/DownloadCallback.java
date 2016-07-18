package cn.mailchat.chatting.protocol;

public interface DownloadCallback {

	public void downloadProgress(String id,int progress,long downloaded);

	public void downloadFinished(String id);

	public void downloadFailed(String id, Exception exception);
	
	public void downloadInterrupt(String id);
}
