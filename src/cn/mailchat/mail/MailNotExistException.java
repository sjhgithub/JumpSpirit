package cn.mailchat.mail;

public class MailNotExistException extends MessagingException {
	
	public MailNotExistException() {
		super("MailNotExist");
	}
	
}
