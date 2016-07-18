package cn.mailchat.view.pager.fragment;

import cn.mailchat.R;




public enum SetImapInfoTab {
    MAIL_QQ(0, SetImapInfoTab.CATALOG_QQ, R.string.frame_title_setm_qq, SetImapInfoFragment.class),
    MAIL_163(1, SetImapInfoTab.CATALOG_163,R.string.frame_title_setm_163, SetImapInfoFragment.class),
    MAIL_SINA(2, SetImapInfoTab.CATALOG_SINA, R.string.frame_title_setm_sina, SetImapInfoFragment.class);

    public final static int CATALOG_QQ = 1;
    public final static int CATALOG_163 = 2;
    public final static int CATALOG_SINA = 3;

    private Class<?> clz;
    private int idx;
    private int title;
    private int catalog;
    private SetImapInfoTab(int idx, int catalog, int title, Class<?> clz) {
        this.idx = idx;
        this.clz = clz;
        this.setCatalog(catalog);
        this.setTitle(title);
    }

    public static SetImapInfoTab getTabByIdx(int idx) {
        for (SetImapInfoTab t : values()) {
            if (t.getIdx() == idx)
                return t;
        }
        return MAIL_QQ;
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
