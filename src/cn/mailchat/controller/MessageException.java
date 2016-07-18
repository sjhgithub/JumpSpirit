package cn.mailchat.controller;

/**
 * 异常处理模块
 * 
 * @author: cuiwei
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2014-3-21
 */
public class MessageException extends Exception {

	public static final long serialVersionUID = -1;
	// server
	public static final int UNKNOW_SERVER_EXCEPTION = 1;
	public static final int NEED_LOGIN = 2;
	public static final int INVALID_COMMAND = 3;
	public static final int CLIENT_VERSION_TOO_LOW = 4;
	public static final int INVALID_PARMATERS = 5;
	public static final int INVALID_LOGIN = 6;
	public static final int INVALID_EMAIL = 7;
	public static final int SMTP_CONNECT_ERROR = 8;
	public static final int SMTP_AUTH_ERROR = 9;
	public static final int INVALID_ORIGIN_ID = 10;
	public static final int SEND_MAIL_ERROR = 11;
	public static final int MAIL_DEL_ERROR = 12;
	public static final int ONLY_FILE_OP = 13;
	public static final int INVALID_OFFSET = 14;
	public static final int IMAP_CONNECT_ERROR = 15;
	public static final int IMAP_AUTH_ERROR = 16;

	// local
	public static final int UNKNOW_LOCAL_EXCEPTION = 10000;
	// local-net
	public static final int UNKNOW_NET_EXCEPTION = 11000;
	public static final int NET_UNAVIABLE = 11001;
	public static final int CONNECT_FAILED = 11002;
	// local-IO
	public static final int UNKNOW_IO_EXCEPTION = 12000;
	public static final int SYS_STORAGE_EXCEPTION = 901;
	// local-database
	public static final int UNKNOW_DATABASE_EXCEPTION = 13000;

	private int exceptionType;

	/**
	 * 已知的异常
	 * 
	 * @param type
	 * @param message
	 */
	public MessageException(int type, String message) {
		super(message);
		setExceptionType(type);
	}

	public MessageException(int type) {
		this(type, "");
	}

	/**
	 * 未知或未定义的异常类型
	 * 
	 * @param message
	 */
	public MessageException(String message) {
		this(UNKNOW_LOCAL_EXCEPTION, message);
	}

	/**
	 * 未知或未定义的异常类型，带Throwable
	 * 
	 * @param message
	 */
	public MessageException(String message, Throwable throwable) {
		super(message, throwable);
		setExceptionType(UNKNOW_LOCAL_EXCEPTION);
	}

	/**
	 * @return the exceptionType
	 */
	public int getExceptionType() {
		return exceptionType;
	}

	/**
	 * @param exceptionType
	 *            the exceptionType to set
	 */
	public void setExceptionType(int exceptionType) {
		this.exceptionType = exceptionType;
	}

}
