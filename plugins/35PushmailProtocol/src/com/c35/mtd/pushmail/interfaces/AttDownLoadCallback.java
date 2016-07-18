package com.c35.mtd.pushmail.interfaces;

import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.beans.C35Attachment;
import com.c35.mtd.pushmail.exception.MessagingException;

/**
 * 描述 下载某一附件时的回调接口
 * @Description:
 * @author: zhuanggy
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public interface AttDownLoadCallback {
	public void downloadStarted(C35Attachment att);
	public void updateProgress(C35Attachment att ,int progress);
	public void downloadFinished(C35Attachment att, Account account);
	public void downloadStoped(C35Attachment att);
	public void downloadFailed(C35Attachment att,MessagingException e);
}
