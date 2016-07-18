package cn.mailchat.fragment;

import cn.mailchat.R;

public enum ForwardTabInfo {
	CHAT(0, ForwardTabInfo.CATALOG_CHAT, R.string.forwarding_chat,ForwardViewPagerItemFragment.class), 
	MAIL(1, ForwardTabInfo.CATALOG_MAIL,R.string.forwarding_mail, ForwardViewPagerItemFragment.class);

	public final static int CATALOG_CHAT = 1;
	public final static int CATALOG_MAIL = 2;
	private Class<?> clz;
	private int idx;
	private int title;
	private int catalog;

	private ForwardTabInfo(int idx, int catalog, int title, Class<?> clz) {
		this.idx = idx;
		this.clz = clz;
		this.setCatalog(catalog);
		this.setTitle(title);
	}

	public static ForwardTabInfo getTabByIdx(int idx) {
		for (ForwardTabInfo t : values()) {
			if (t.getIdx() == idx)
				return t;
		}
		return CHAT;
	}

	public Class<?> getClz() {
		return clz;
	}

	public void setClz(Class<?> clz) {
		this.clz = clz;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public int getTitle() {
		return title;
	}

	public void setTitle(int title) {
		this.title = title;
	}

	public int getCatalog() {
		return catalog;
	}

	public void setCatalog(int catalog) {
		this.catalog = catalog;
	}

}
