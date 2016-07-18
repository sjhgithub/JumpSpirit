package cn.mailchat.beans;

public class ChooseFolderBean {
	private String folderName;
	private boolean isAllowPush;
	private boolean isCustomFolder;
	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public boolean isAllowPush() {
		return isAllowPush;
	}

	public void setAllowPush(boolean isAllowPush) {
		this.isAllowPush = isAllowPush;
	}

	public boolean isCustomFolder() {
		return isCustomFolder;
	}

	public void setCustomFolder(boolean isCustomFolder) {
		this.isCustomFolder = isCustomFolder;
	}

}
