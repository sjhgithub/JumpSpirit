package com.c35.mtd.pushmail.command.response;

import org.json.JSONException;
import org.json.JSONObject;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.beans.C35Message;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.util.JsonUtil;

/**
 * 
 * @Description:GetMailById命令响应
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class GetMailByIdResponse extends BaseResponse {

	C35Message message = null;

	public C35Message getMessage() {
		return message;
	}

	public void setMessage(C35Message message) {
		this.message = message;
	}

	private static final String TAG = "GetMailByIdResponse";

	@Override
	public void initFeild(String response) throws MessagingException {
		super.initFeild(response);
		Debug.i(TAG, "commandMessage =" + commandMessage);
		try {
			setMessage(JsonUtil.parseMessage(new JSONObject(commandMessage)));
		} catch (JSONException e) {
			Debug.w("C35", "JsonExp", e);
			throw new MessagingException(MessagingException.RETURN_COMMAND_ERROR_GETMAILBYID, e.getMessage());
		}

	}

	// {
	// "to":["liujie <liujie@szdep.com>"],
	// "oversea":0,
	// "signId":"",
	// "plainTextCharset":"UTF-8",
	// "plainText":"﻿\r\r\r\r\r    尊敬的用户，您好!\r    本邮件来自Bordeaux@35反垃圾邮件系统，提供垃圾邮件隔离报告功能。\n\r    自上一次隔离报告以来，你有0封新邮件被隔离在垃圾邮件箱中，你可以点击“立即查看”链接查看隔离列表。\n\r    立即查看\r    如果链接不能点击，请粘贴以下网址到浏览器地址栏进行浏览。\n\r    http://mail.szdep.com/servlet/spamReportServlet?U56WbqhJZO4nmwsWx6LSvr/4g9N7sD5xY4XHVFtAg/t5b1ZdXUxDsCyGlLDwH8EifZFaHUfNps33So6lhl9XKw==&TfGhq2wmRfk31zJ5dzjkGB2mplwzm/7ZmRr7STeZb0+GjkZ8/MhFsq8ipr/erD9SWJOVMAcXTMnyTqrKtgvXLg==&qt0ma1T6SBHk4jl9mPWnWhTDeuqEsMWz/59bRpSpmVc26t2+b10HXWFn7b8NLynz6WcQcSCTxcxRUFsi/fkWpg==\n\r    \r        接收邮件：\r        如果您还不确认该邮件对您是否有用，您可以点击“接收邮件”链接来获取该邮件内容（将自动转移到收件箱，使Foxmail，Outlook等客户端可以接收该邮件。\n\r        加为联系人：\r        如果该发件邮箱是您的联系人邮箱，您可以点击“加为联系人”链接来避免以后来自该邮箱的邮件被过滤隔离。\n\r        友情提醒：垃圾邮件只在服务器上保留7天。\n\r    \n\r    \r        Bordeaux@35反垃圾邮件网关\n\r        2011-11-18 10:00:05\n\r        \n\r        Dear User,\r        This is a report from \r            Bordeaux@35 Anti-Spam mail system, which lists all new incoming SPAMs that \r            were quarantined.\n\r        Since last report, 0 mails HAVE been tagged as SPAM and quarantined in your webmail spam box. Click \"Show List\" below for details.\n\r        Show List Now!\r        \r            Alternatively,\r            You can copy & paste URL below in your browser address to view the list.\n\r        http://mail.szdep.com/servlet/spamReportServlet?Wkbo1WNLtAXMCJH6Tc3mbYNKYf5DHisnS5H6s9CDbNN8CmxavGBD9zmqNbAEhmlisF44krDffs4YDW4rnD3zdA==&3Oa0kUtnhIkMdG5Mk1mNgsp5IO1BZpLv3Q3rCIiJzlyrFw6MH9jP/RjSFi1/VipEyGjuC/Z4reCgB8IgS+F4AA==&iZ5TRSZht8gVaedjT5Dqd46mrcnzAeETFMMqLEB4Wg/sFvXnqPBvog4qBESZYebY1TX1MfOFxIRsIABvQw2DKA==\n\r        \r            Receive:\r            If you are not sure whether a mail is a true SPAM.  click \"Receive\". \r              The mail will be moved to INBOX and can be downloaded with any client(Outlook, \r              Forxmail, etc) .\n\r            Add as contact:\r            If the sender is a valid contact you know, just click \"Add as contact\" \r              . Emails from the same sender will never be quarantined into SPAM box.\n\r            Tip:  Emails in your spam box only reserve for 7 days.\n\r        \n\r        Bordeaux@35 Anti-Spam Mail System\n\r        2011-11-18 10:00:05\n\r         \n\r    \n\r\n\r\r"
	// "subject":"垃圾邮件隔离报告",
	// "calendarStartTime":"",
	// "calendarRemindTime":"",
	// "relay":"",
	// "attachs":[],
	// "acknowledgme":0,
	// "calendarText":"",
	// "from":"mail-deamon <mail-deamon@szdep.com>",
	// "attachSize":0,
	// "folderId":"inbox",
	// "mailType":0,
	// "recallFlag":"",
	// "timingSendTime":"",
	// "saveSent":0,
	// "byOneSelf":0,
	// "priority":3,
	// "reply":"",
	// "read":0,
	// "senderEmail":"mail-deamon <mail-deamon@szdep.com>",
	// "calendarState":3,
	// "calendarCloseRemind":1,
	// "reportMessage":0,
	// "hyperText":"PE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"
	// \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\r\n<html
	// xmlns=\"http://www.w3.org/1999/xhtml\">\r\n<head><meta http-equiv=\"Content-Type\" content=\"text/html;
	// charset=utf-8\" />\r\n<title>隔离邮件报告<\/title>\r\n<\/head>\r\n<body st",
	// "labels":[],
	// "sendTime":"2011-11-18 10:00:06",
	// "bcc":[],
	// "size":10311,
	// "deliverStatus":0,
	// "importantFlag":0,
	// "replyTo":"",
	// "calendarEndTime":"",
	// "mailId":"4ec5bc269900482ac77e8ebf_inbox",
	// "calendarLocation":"",
	// "hyperTextCharset":"UTF-8",
	// "calendarTextCharset":"",
	// "cc":[],
	// }

}
