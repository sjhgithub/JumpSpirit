package com.c35.mtd.pushmail.interfaces;

import java.util.List;

import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.beans.C35Attachment;
import com.c35.mtd.pushmail.beans.C35Message;
import com.c35.mtd.pushmail.command.response.GetMailsStatusResponse;
import com.c35.mtd.pushmail.exception.MessagingException;

/**
 * 对邮件相关操作的监听，当操作开始或结束时会回调用此类中的方法
 * 
 * @Description:
 * @author: zhuanggy
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class MessagingListener {

	/**
	 * 同步邮件头完成后的回调方法
	 * 
	 * @Description:
	 * @param account
	 * @param folder
	 * @param newMessages
	 * @param messages2Sync
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-11-2
	 */
	public void synchronizeMailboxHeaderFinished(Account account, String folder, int newMessages, List<String> messages2Sync) {
	}

	/**
	 * 同步邮件箱完成后的回调方法
	 * 
	 * @Description:
	 * @param account
	 * @param folder
	 * @param newMessages
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-11-2
	 */
	public void synchronizeMailboxFinished(Account account, String folder, int newMessages) {
	}

	/**
	 * 命令失败后的回调方法
	 * 
	 * @Description:
	 * @param account
	 * @param folder
	 * @param e
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-11-2
	 */
	public void processCMDFailed(Account account, String folder, Exception e) {
	}


	/**
	 * 加载邮件头的回调方法，撰写页面
	 * @param account
	 * @param uid
	 * @param message
	 * @see:
	 * @since:
	 * @author: liujie
	 * @date:2012-11-2
	 */
	public void loadMessageForViewHeaders(Account account, String uid, final C35Message message) {
	}


	/**
	 * 加载邮件内容的回调方法（用于显示邮件详情），正文预览页面和撰写页面用
	 * @Description:
	 * @param account
	 * @param uid
	 * @param message
	 * @see:
	 * @since:
	 * @author: liujie
	 * @date:2012-11-2
	 */
	public void loadAndShowMessageBodyForView(Account account, String uid, C35Message message) {
	}

	/**
	 * 加载邮件失败回调(服务器)
	 * @Description:
	 * @param account
	 * @param uid
	 * @param message
	 * @see:
	 * @since:
	 * @author: liujie
	 * @date:2012-11-2
	 */
	public void loadRemoteMessageForViewFailed(Account account, String uid, Exception message,C35Message  msg) {
	}

	/**
	 * 定时刷新邮件开始
	 * @param account
	 * @see:
	 * @since:
	 * @author: liujie
	 * @date:2012-11-2
	 */
	public void checkMailStarted(Account account) {
	}

	/**
	 * 定时刷新邮件结束
	 * @param account
	 * @see:
	 * @since:
	 * @author: liujie
	 * @date:2012-11-2
	 */
	public void checkMailFinished(Account account) {
	}

	
	/**
	 * 发送邮件完成时的回调
	 * @Description:
	 * @param account
	 * @param uid
	 * @param message
	 * @see:
	 * @since:
	 * @author: liujie
	 * @date:2012-11-2
	 */
	public void sendPendingMessagesCompleted(Account account) {
	}


	/**
	 * 为撰写邮件界面结束后返回邮件列表页时，实现刷新。提供的接口
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: Hailin
	 * @date:2012-11-2
	 */
	public void synAutoRefreshMessageListFromMessageCompose() {
	}

	/**
	 * 取得本地一批邮件的最新状态完成时的回调
	 * @Description:
	 * @param response
	 * @see: 
	 * @since: 
	 * @author: wennan
	 * @date:2012-11-2
	 */
	public void getMailsStatusFinish(GetMailsStatusResponse response) {
	}
	
	/**
	 * 用于消息盒子刷新数据，正常结束
	 * @param msgs
	 * @see: 
	 * @since: 
	 * @author: wennan
	 * @date:2013-6-1
	 */
	public void loadHeaderForInfoBoxFinish(List<C35Message> msgs) {
	}
	
	/**
	 * 用于消息盒子刷新数据,数据异常（比如已经被删除）
	 * @see: 
	 * @since: 
	 * @author: 温楠
	 * @date:2013-6-21
	 */
	public void loadHeaderForInfoBoxDataError() {
	}
	
	/**
	 * 用于消息盒子刷新数据,因为没有数据，或者异常要结束掉转圈
	 * @param msgs
	 * @see: 
	 * @since: 
	 * @author: wennan
	 * @date:2013-6-1
	 */
	public void loadHeaderForInfoBoxOver() {
	}
	
	/**
	 * 获得附件列表完成时的回调 [暂时放到MessageingListener中, 需要监听命令执行情况]
	 * 
	 * @Description:
	 * @param response
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-1-6
	 */
	public void getAttachmentListFinish(List<C35Attachment> attachments, int allAttachments){
		
	}

	/**
	 * 获取附件列表失败 [暂时放到MessageingListener中, 需要监听命令执行情况]
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-1-8
	 */
	public void getAttachmentListFailed(){
	}

	/**
	 * 同步附件附件成功
	 * @Description:
	 * @param attachmentsIds
	 * @see: 
	 * @since: 
	 * @author: zhuanggy
	 * @date:2013-1-10
	 */
	public void syncAttachmentListFinished( ) {
	}
	
	/**
	 * 获得所有附件的id失败
	 * @Description:
	 * @see: 
	 * @since: 
	 * @author: zhuanggy
	 * @date:2013-1-10
	 */
	public void syncAttachmentListFailed( ) {
	}
	
}
