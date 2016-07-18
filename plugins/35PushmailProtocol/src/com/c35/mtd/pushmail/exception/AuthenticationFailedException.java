package com.c35.mtd.pushmail.exception;

/**
 * 用户名或密码不正确异常类
 * @Description:
 * @author: zhuanggy
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class AuthenticationFailedException extends MessagingException {
    public static final long serialVersionUID = -1;

    public AuthenticationFailedException(String message) {
        super(message);
    }

    public AuthenticationFailedException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
