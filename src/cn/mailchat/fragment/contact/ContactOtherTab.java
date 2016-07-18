package cn.mailchat.fragment.contact;

import cn.mailchat.R;

public enum ContactOtherTab {
    CONTACT_SAME_DOMAIN(0, ContactOtherTab.TYPE_CONTACT_SAME_DOMAIN,R.string.title_contact_tab_same_domain, ContactSameDomainFragment.class),
    CONTACT_PERSONAL(1, ContactOtherTab.TYPE_CONTACT_PERSONAL, R.string.title_contact_tab_personal, ContactPersonalFragment.class);

    public final static int TYPE_CONTACT_SAME_DOMAIN = 1;
    public final static int TYPE_CONTACT_PERSONAL = 2;
    
    
    private Class<?> clz;
    private int idx;
    private int title;
    private int tabType;
    private ContactOtherTab(int idx, int tabType, int title, Class<?> clz) {
        this.idx = idx;
        this.clz = clz;
        this.setTabType(tabType);
        this.setTitle(title);
    }
    public static ContactOtherTab getTabByIdx(int idx) {
        for (ContactOtherTab t : values()) {
            if (t.getIdx() == idx)
                return t;
        }
        return CONTACT_SAME_DOMAIN;
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
	public int getTabType() {
		return tabType;
	}
	public void setTabType(int tabType) {
		this.tabType = tabType;
	}
    
}
