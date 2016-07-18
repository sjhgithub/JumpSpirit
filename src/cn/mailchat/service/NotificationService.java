package cn.mailchat.service;

import cn.mailchat.MailChat;
import android.app.IntentService;
import android.content.Intent;

public class NotificationService extends IntentService {

	private static final String WORKING_THREAD_NAME = "Notification Service Working Thread";

	public NotificationService() {
		super(WORKING_THREAD_NAME);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String downloadingId = intent.getStringExtra(EXTRA_DOWNLOADING_ID);
		if (downloadingId != null) {
			synchronized (MailChat.downloadingList) {
				MailChat.downloadingList.remove(downloadingId);
			}
		}
	}

	// 生成附件相关通知被清除后触发的Intent
	private static final String EXTRA_DOWNLOADING_ID = "downloading_id";

	public static Intent actionNotificationDeletedIntent(String downloadingId) {
		Intent intent = new Intent(MailChat.getInstance(), NotificationService.class);
		intent.putExtra(EXTRA_DOWNLOADING_ID, downloadingId);
		return intent;
	}

}
