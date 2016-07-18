package com.c35.mtd.pushmail.command.request;

public class GetProxyServerDomainRequest extends BaseRequest {

	private String emailOrDomain;

	public String getEmailOrDomain() {
		return emailOrDomain;
	}

	public void setEmailOrDomain(String emailOrDomain) {
		this.emailOrDomain = emailOrDomain;
	}

}
