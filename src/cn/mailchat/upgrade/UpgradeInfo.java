package cn.mailchat.upgrade;

import java.io.Serializable;

public class UpgradeInfo implements Serializable {

	private static final long serialVersionUID = 4842983065419046234L;
	private String version;
	private boolean isForceUpgrade;
	private String url;
	private String description;

	public UpgradeInfo(String version, boolean isForceUpgrade, String url,
			String description) {
		super();
		this.version = version;
		this.isForceUpgrade = isForceUpgrade;
		this.url = url;
		this.description = description;
	}

	public String getVersion() {
		return version;
	}

	public boolean isForceUpgrade() {
		return isForceUpgrade;
	}

	public String getUrl() {
		return url;
	}

	public String getDescription() {
		return description;
	}

}
