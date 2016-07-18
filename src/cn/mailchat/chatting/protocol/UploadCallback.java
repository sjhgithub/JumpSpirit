package cn.mailchat.chatting.protocol;

import org.json.JSONObject;

public interface UploadCallback {
	public void uploadProgress(String id, int progress);

//	public void uploadFinished(String id);
//
//	public void uploadFailed(String id, Exception exception);

	public void uploadInterrupt(String id);
}
