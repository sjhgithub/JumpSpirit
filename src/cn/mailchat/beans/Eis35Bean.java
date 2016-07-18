package cn.mailchat.beans;

/**
 * 
 * @copyright © 35.com
 * @file name ：Eis35Bean.java
 * @author ：zhangjx
 * @create Data ：2015-8-24上午11:29:23
 * @Current Version：v1.0
 * @History memory :
 * @Date : 2015-8-24上午11:29:23
 * @Modified by：zhangjx
 * @Description :
 */
public class Eis35Bean {
	private String id;
	private String parentId;//父id
	private String name;//名称或eis姓名
	private String sort;// 排序
	private String email;
	private String mailChatName;//使用邮洽设置的昵称
	private String imgHeadUrl;//头像地址
	private boolean isLeader;//是否领导
	private boolean isParent;//是否父级
	private boolean isUsedMailchat;//是否使用过邮洽
	private int totalCount;//部门内人数
	private String department;
	private String position;
	private String phones;
	private String addr;//地址
	private String otherRemarks;//其他备注
	private int childDepCount;//部门拥有子部门数

	//将保存到备注表
	private String rEmail;
	private String rName;
	private String rNickName;
	private String rImgHeadHash;
	private String rCompany;
	private String rBirthday;
	private String rDepartment;// 部门
	private String rPosition;// 职位
	private String rPhones;// 电话
	private String rAddr;// 地址
	private String rOtherRemarks;// 其他备注
	private String rData;
	/**
	 * 是否展开
	 */
	private boolean isExpand = false;
	public Eis35Bean() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMailChatName() {
		return mailChatName;
	}

	public void setMailChatName(String mailChatName) {
		this.mailChatName = mailChatName;
	}

	public String getImgHeadUrl() {
		return imgHeadUrl;
	}

	public void setImgHeadUrl(String imgHeadUrl) {
		this.imgHeadUrl = imgHeadUrl;
	}

	public boolean isLeader() {
		return isLeader;
	}

	public void setLeader(boolean isLeader) {
		this.isLeader = isLeader;
	}

	public boolean isParent() {
		return isParent;
	}

	public void setParent(boolean isParent) {
		this.isParent = isParent;
	}

	public boolean isUsedMailchat() {
		return isUsedMailchat;
	}

	public void setUsedMailchat(boolean isUsedMailchat) {
		this.isUsedMailchat = isUsedMailchat;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public boolean isExpand() {
		return isExpand;
	}

	public void setExpand(boolean isExpand) {
		this.isExpand = isExpand;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getPhones() {
		return phones;
	}

	public void setPhones(String phones) {
		this.phones = phones;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public String getOtherRemarks() {
		return otherRemarks;
	}

	public void setOtherRemarks(String otherRemarks) {
		this.otherRemarks = otherRemarks;
	}

	public String getrEmail() {
		return rEmail;
	}

	public void setrEmail(String rEmail) {
		this.rEmail = rEmail;
	}

	public String getrName() {
		return rName;
	}

	public void setrName(String rName) {
		this.rName = rName;
	}

	public String getrNickName() {
		return rNickName;
	}

	public void setrNickName(String rNickName) {
		this.rNickName = rNickName;
	}

	public String getrImgHeadHash() {
		return rImgHeadHash;
	}

	public void setrImgHeadHash(String rImgHeadHash) {
		this.rImgHeadHash = rImgHeadHash;
	}

	public String getrCompany() {
		return rCompany;
	}

	public void setrCompany(String rCompany) {
		this.rCompany = rCompany;
	}

	public String getrBirthday() {
		return rBirthday;
	}

	public void setrBirthday(String rBirthday) {
		this.rBirthday = rBirthday;
	}

	public String getrDepartment() {
		return rDepartment;
	}

	public void setrDepartment(String rDepartment) {
		this.rDepartment = rDepartment;
	}

	public String getrPosition() {
		return rPosition;
	}

	public void setrPosition(String rPosition) {
		this.rPosition = rPosition;
	}

	public String getrPhones() {
		return rPhones;
	}

	public void setrPhones(String rPhones) {
		this.rPhones = rPhones;
	}

	public String getrAddr() {
		return rAddr;
	}

	public void setrAddr(String rAddr) {
		this.rAddr = rAddr;
	}

	public String getrOtherRemarks() {
		return rOtherRemarks;
	}

	public void setrOtherRemarks(String rOtherRemarks) {
		this.rOtherRemarks = rOtherRemarks;
	}

	public String getrData() {
		return rData;
	}

	public void setrData(String rData) {
		this.rData = rData;
	}

	public int getChildDepCount() {
		return childDepCount;
	}

	public void setChildDepCount(int childDepCount) {
		this.childDepCount = childDepCount;
	}

}
