package com.c35.mtd.pushmail.exception;

/**
 * 与服务器交互时出现的异常捕获。
 * 
 * @Description:
 * @author: zhuanggy
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class MessagingException extends Exception {

	public static final long serialVersionUID = -1;

	// Any exception that does not specify a specific issue
	public static final int UNSPECIFIED_EXCEPTION = 0;// 未指定类型
	// Connection or IO errors
	// public static final int IOERROR = 1;
	// The configuration requested TLS but the server did not support it.
	// public static final int TLS_REQUIRED = 2;
	// Authentication is required but the server did not support it.
	// public static final int AUTH_REQUIRED = 3;
	// General security failures
	// public static final int GENERAL_SECURITY = 4;
	// public static final int UNKNOWN_MESSAGE = 6;

	public static final int OUT_OF_MEMORY = 5;

	// 异常处理：
	// 1、网络中断或超时，导致服务器连接失败；
	// 2、服务器响应错误：a、响应格式错误；b，格式正确，返回具体错误代号
	// 3、磁盘读写失败
	// CODE_OK 200 成功
	// CODE_COMMAND_NO 404 命令没找到
	// CODE_VERSION_NO 901 版本号不正确，不支持
	// CODE_LOGIN_NO 902 登录不正确
	// CODE_AUTH_NO 903 没有通过认证(ticket失效时)
	// CODE_COMMAND_ERROR 904 命令格式错误
	// CODE_DATA_ERROR 908 入参格式不正确

	// 1、网络中断或超时，导致服务器连接失败；
	public static final int CONNECT_ERROR = 35;
	// 2、服务器响应错误：a、响应格式错误；
	public static final int RESPONSE_ERROR_FORMAT = 36;
	// b，格式正确，返回具体错误代号
	public static final int CODE_COMMAND_SUCCESS = 200;// 成功
	public static final int CODE_COMMAND_NO = 404;// 命令没找到
	public static final int CODE_VERSION_NO = 901;// 版本号不正确，不支持
	public static final int CODE_LOGIN_NO = 902;// 登录不正确
	public static final int CODE_AUTH_NO = 903;// 没有通过认证
	public static final int CODE_COMMAND_ERROR = 904;// 命令格式错误
	public static final int CODE_SEARCH_TIME_OUT_FOR_IMAP_ERROR = 907;// IMAP 搜索邮件超时未响应
	public static final int CODE_PROGRAM_ERROR = 909;// 程序错误
	public static final int CODE_CONNECT_ERROR = 303;// 已经和服务器断开连接
	public static final int UNFIND_MESSAGE = 313;// 找不到该条记录
	public static final int STOP_LOAD_ATT = 1205;// 附件下载失败 error_downl_att
	public static final int REQUEST_DATA_ERROE = 100002;// 请求参数错误todo 不是908吗？

	// 3、磁盘读写失败
	public static final int DISK_IO_ERROR = 37;// 
	public static final int LOGIN_CHECKPUSH_ERROR = 1004;// 
	public static final int LOGIN_SAVEACCOUNT_REGISTEPUSH_ERROR = 1005;// 
	
	public static final int SERVER_IO_ERROR = 38;// 
	public static final int SERVER_IO_ERROR_NO_TOAST = 3801;// 
	// 4、请求格式错误
	public static final int REQUEST_ERROR_FORMAT = 39;// 请求格式错误
	// store初始化错误
	public static final int STORE_GETINSTANCE_ERROR = 40;
	// 运行时异常错误
	public static final int PROGRAM_RUNING_ERROR = 41;
	public static final int FOLDER_ID_ERROR = 42;// FOLDER_ID错误
	private final int mExceptionType;

	public static final int COMMAND_ERROR_SEARCH_ATTACH_LIST = 43;// 执行searchAttachList命令错误
	public static final int COMMAND_ERROR_GET_MAILSTATUS = 44;// 执行getMailsStatus命令错误
	public static final int COMMAND_ERROR_COMMIT_MAILSTATUS = 45;// 执行commitMailsStatus命令错误

	public static final int RETURN_COMMAND_ERROR_ADVANCESEARCHMAILS = 2000;// 高级搜索返回值错误
	public static final int RETURN_COMMAND_ERROR_COMMITMAILSTATUS = 2001;// 提交邮件状态返回值错误
	public static final int RETURN_COMMAND_ERROR_FILEVIEWBYHTML = 2002;// fileviewByHtml返回值错误
	public static final int RETURN_COMMAND_ERROR_GETATTACHMENTLISTIDS = 2003;// 从服务器获取附件列表ID的响应错误
	public static final int RETURN_COMMAND_ERROR_GETATTACHMENTLIST = 2004;// 从服务器获取附件列表响应错误
	public static final int RETURN_COMMAND_ERROR_GETFOLDERLIST = 2005;// 获取邮件夹列表响应错误
	public static final int RETURN_COMMAND_ERROR_GETIDSBYID = 2006;// 通过id获取邮件列表错误
	public static final int RETURN_COMMAND_ERROR_GETMAILBYID = 2007;// 通过邮件id获取邮件错误
	public static final int RETURN_COMMAND_ERROR_GETMAILIDSBYFOLDER = 2008;// GetMailIdsByFolder命令响应错误
	public static final int RETURN_COMMAND_ERROR_GETMAILLISTBYMAILIDS = 2009;// 通过一批mailId得到邮件列表响应错误
	public static final int RETURN_COMMAND_ERROR_GETMAILSSTATUS = 2010;// GetMailsStatus命令响应错误
	public static final int RETURN_COMMAND_ERROR_LOGIN = 2011;// Login命令响应错误
	public static final int RETURN_COMMAND_ERROR_SEARCHIDSBYTYPE = 2012;// SearchIdsByType命令响应错误
	public static final int RETURN_COMMAND_ERROR_VERSION = 2013;// Version命令响应错误
	public static final int RETURN_COMMAND_ERROR_LINK_TIMEOUT = 2014;// 与服务器连接超时
	public static final int RETURN_COMMAND_ERROR_GET_CONTACTS = 2015;// 获取联系人错误
	public static final int RETURN_COMMAND_ERROR_GET_RESULTSTRING = 2016;// resultStringSize<1

	public static final int DOWNLOAD_PHOTO_ERROR_MESSAGE = 46; // 图片加载失败
	public static final int DOWNLOAD_ATTACHMENT_ERROR_MESSAGE = 47;// 附件加载失败
	public static final int SYN_SELF_FOLDER_ERROR = 48;
	
	public static final int DOWNLOAD_CID_PHOTO_ERROR_MESSAGE = 49;//内嵌图片加载失败
	
	/**
	 * 抛出异常，未指定异常类型
	 * 
	 * @param message
	 */
	public MessagingException(String message) {
		super(message);
		mExceptionType = UNSPECIFIED_EXCEPTION;
	}

	/**
	 * 抛出异常，未指定异常类型
	 * 
	 * @param message
	 * @param throwable
	 */
	public MessagingException(String message, Throwable throwable) {
		super(message, throwable);
		mExceptionType = UNSPECIFIED_EXCEPTION;
	}

	/**
	 * 抛出空异常，指定异常类型
	 * 
	 * @param exceptionType
	 */
	public MessagingException(int exceptionType) {
		super(""+exceptionType);
		mExceptionType = exceptionType;
	}

	/**
	 * 抛出异常，包含异常内容和类型
	 * 
	 * @param exceptionType
	 * @param message
	 */
	public MessagingException(int exceptionType, String message) {
		super(message);
		mExceptionType = exceptionType;
	}

	/**
	 * 得到异常类型
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2012-11-2
	 */
	public int getExceptionType() {
		return mExceptionType;
	}
}
