package com.c35.mtd.pushmail.exception;

/**
 * 无法安全地连接到服务器类
 * @Description:
 * @author: zhuanggy
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class CertificateValidationException extends MessagingException {
    public static final long serialVersionUID = -1;

    public CertificateValidationException(String message) {
        super(message);
    }

    public CertificateValidationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
