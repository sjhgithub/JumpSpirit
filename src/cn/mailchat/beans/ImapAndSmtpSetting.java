package cn.mailchat.beans;

public class ImapAndSmtpSetting {
	private String imapHost;
	private int imapPost;
	private int imapSafety;//0-无;1-STARTTLS;2-SSL/TLS
	
	private String smtpHost;
	private int smtpPost;
	private int smtpSafety;//0-无;1-STARTTLS;2-SSL/TLS
	
	public String getImapHost() {
		return imapHost;
	}
	public void setImapHost(String imapHost) {
		this.imapHost = imapHost;
	}
	public int getImapPost() {
		return imapPost;
	}
	public void setImapPost(int imapPost) {
		this.imapPost = imapPost;
	}
	public int getImapSafety() {
		return imapSafety;
	}
	public void setImapSafety(int imapSafety) {
		this.imapSafety = imapSafety;
	}
	public String getSmtpHost() {
		return smtpHost;
	}
	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}
	public int getSmtpPost() {
		return smtpPost;
	}
	public void setSmtpPost(int smtpPost) {
		this.smtpPost = smtpPost;
	}
	public int getSmtpSafety() {
		return smtpSafety;
	}
	public void setSmtpSafety(int smtpSafety) {
		this.smtpSafety = smtpSafety;
	}
}
