package cn.mailchat.fragment.contact;

import cn.mailchat.R;

public enum Contact35Tab {
    CONTACT_EIS(0, Contact35Tab.TYPE_CONTACT_EIS, R.string.title_contact_tab_eis, Contact35EisFragment.class),
    CONTACT_PERSONAL(1, Contact35Tab.TYPE_CONTACT_PERSONAL, R.string.title_contact_tab_personal, ContactPersonalFragment.class);

    public final static int TYPE_CONTACT_EIS = 1;
    public final static int TYPE_CONTACT_PERSONAL = 2;
    
    
    private Class<?> clz;
    private int idx;
    private int title;
    private int tabType;
    private Contact35Tab(int idx, int tabType, int title, Class<?> clz) {
        this.idx = idx;
        this.clz = clz;
        this.setTabType(tabType);
        this.setTitle(title);
    }
    public static Contact35Tab getTabByIdx(int idx) {
        for (Contact35Tab t : values()) {
            if (t.getIdx() == idx)
                return t;
        }
        return CONTACT_EIS;
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
