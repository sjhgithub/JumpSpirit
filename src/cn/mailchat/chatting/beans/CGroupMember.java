package cn.mailchat.chatting.beans;

import java.io.Serializable;
import java.util.Locale;

import cn.mailchat.utils.ContactComparator;


public class CGroupMember implements Serializable {

	private String nickName; // 成员名称
	private String email; // 邮箱地址
	private String avatarHash; // 成员头像Hash值，用于拼装请求参数
	protected Character firstChar;// 获取首字母
	private boolean isAdmin;//是否管理员
	private boolean isInviteMember;//是否是要求了，但未加入群组的

	public boolean isInviteMember() {
		return isInviteMember;
	}

	public void setInviteMember(boolean isInviteMember) {
		this.isInviteMember = isInviteMember;
	}

	public boolean isAdmin() {
		return isAdmin;
	}
	
	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public CGroupMember(String nickname, String email) {
		this.nickName = nickname;
		this.email = email;
	}

	public CGroupMember() {
	}

	private String uid; // 成员id

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAvatarHash() {
		return avatarHash;
	}

	public void setAvatarHash(String avatarHash) {
		this.avatarHash = avatarHash;
	}

	public Character getFirstChar() {
		if (firstChar == null) {
			firstChar = email.toUpperCase(Locale.ENGLISH).charAt(0);
			if (!ContactComparator.isAlpha(firstChar)) {
				firstChar = '#';
			}
		}
		return firstChar;
	}

	public void setFirstChar(Character firstChar) {
		this.firstChar = firstChar;
	}

	@Override
	public boolean equals(Object obj) {
		CGroupMember s = (CGroupMember) obj;
		return email.equals(s.email) ;
	}

	@Override
	public int hashCode() {
		return email.hashCode();
	}
}
