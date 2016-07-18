package cn.mailchat;

public interface BaseAccount {
    public String getEmail();
    public String getName();
    public void setEmail(String email);
    public String getDescription();
    public void setDescription(String description);
    public String getUuid();
    public boolean getIsHaveUnreadMsg();
	public  String getAccountBigHeadImg();
	public  void setAccountBigHeadImg(String headImg);
}
