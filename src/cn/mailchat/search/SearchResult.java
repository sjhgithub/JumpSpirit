package cn.mailchat.search;

import cn.mailchat.mail.Message;

public class SearchResult {
    
    public static enum Type {LOCAL, REMOTE}
    
    private String mUid;
    private Type mType;
    
    public SearchResult(String uid, Type type) {
        mUid = uid;
        mType = type;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public Type getType() {
        return mType;
    }

    public void setType(Type type) {
        mType = type;
    }
}
