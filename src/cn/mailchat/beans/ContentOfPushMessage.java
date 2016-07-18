package cn.mailchat.beans;

public class ContentOfPushMessage {

	private String quantity;
	private String receiver;
	private String fromer;
	private String subject;
	private String folderId;

	public ContentOfPushMessage(String quantity, String receiver, String fromer, String subject) {
		super();
		this.quantity = quantity;
		this.receiver = receiver;
		this.fromer = fromer;
		this.subject = subject;
	}

	public ContentOfPushMessage(String quantity, String receiver, String fromer, String subject, String folderId) {
		super();
		this.quantity = quantity;
		this.receiver = receiver;
		this.fromer = fromer;
		this.subject = subject;
		this.folderId = folderId;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getFromer() {
		return fromer;
	}

	public void setFromer(String fromer) {
		this.fromer = fromer;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}

	@Override
	public int hashCode() {
		return 147;
	}

	@Override
	public boolean equals(Object o) {

		ContentOfPushMessage obj = (ContentOfPushMessage) o;

		if (obj != null && obj instanceof ContentOfPushMessage && this.getReceiver() == obj.getReceiver() && this.getFolderId() == obj.getFolderId()) {
			return true;
		}

		return false;
	}

	@Override
	public String toString() {
		return "ContentOfPushMessage [quantity=" + quantity + ", receiver=" + receiver + ", fromer=" + fromer + ", subject=" + subject + ", folderId=" + folderId + "]";
	}


}
