package cn.mailchat.contacts.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.text.TextUtils;

import cn.mailchat.utils.ContactComparator;
import cn.mailchat.utils.HanziToPinyin;
import cn.mailchat.utils.StringUtil;


/**
 * 联系人基本信息
 * 
 * @copyright © 35.com
 * @file name ：ContactAttribute.java
 * @author ：zhangyq
 * @create Data ：2014-9-28下午6:19:26 
 * @Current Version：v1.0 
 * @History memory :
 * @Date : 2014-9-28下午6:19:26 
 * @Modified by：zhangyq
 * @Description :
 */
public class ContactAttribute extends ContactBase implements Cloneable,Serializable {
	/** 
	* @Fields serialVersionUID : TODO
	*/ 
	private static final long serialVersionUID = 1L;
	private String id;
	private String parentId;//父id
	private String eisName;//eis返回的名字
	private String sort;// 排序
	private String email;
	private String mailChatName;//使用邮洽设置的昵称
	private String imgHeadUrl;//头像地址
	private boolean isLeader;//是否领导
	private boolean isParent;// 是否父级
	private boolean isUsedMailchat;// 是否使用过邮洽
	private int totalCount;// 部门内人数
	private String department;// 部门
	private String position;// 职位
	private String phones;// 电话
	private String addr;// 地址
	private String otherRemarks;// 其他备注
	private String invitationCode;// 邀请码
	private boolean isExpand = false;//是否展开
	private int childDepCount;//部门拥有子部门数

	private int type;
	private String spellName;
	private String count;// 如果是常用联系人，次数排序
	private String company;
	private int sendCount;
	private int receiveCount;
	private String imgHeadName;
	private String imgHeadPath;
	private String imgBigHeadPath;
	private String imgHeadHash;
	private String uploadState;
	private String date;
	private boolean isEisContact;//是否是eis上的联系人
	
	private String pId = "0";//根节点pId为0
	private String name;
	private int level;//当前的级别
	private int icon;
	private List<ContactAttribute> childList;//下一级的子Eis35TreeBeen
	private ContactAttribute parent;// 父Eis35TreeBeen
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


	public ContactAttribute(String rEmail, String rName, String rNickName,
			String rImgHeadHash, String rCompany, String rBirthday,
			String rDepartment, String rPosition, String rPhones, String rAddr,
			String rOtherRemarks, String rData) {
		this.rEmail = rEmail;
		this.rName = rName;
		this.rNickName = rNickName;
		this.rImgHeadHash = rImgHeadHash;
		this.rCompany = rCompany;
		this.rBirthday = rBirthday;
		this.rDepartment = rDepartment;
		this.rPosition = rPosition;
		this.rPhones = rPhones;
		this.rAddr = rAddr;
		this.rOtherRemarks = rOtherRemarks;
		this.rData = rData;
	}

	public ContactAttribute(String id, String pId, String name, String sort,
			String email, String mailChatName, String imgHeadUrl,
			int totalCount, boolean isLeader, boolean isParent,
			boolean isUsedMailchat, boolean isExpand, String department,
			String position, String phones, String addr, String otherRemarks,String rEmail, String rName, String rNickName,
			String rImgHeadHash, String rCompany, String rBirthday,
			String rDepartment, String rPosition, String rPhones, String rAddr,
			String rOtherRemarks, int childDepCount,String rData) {
		this.id = id;
		this.pId = pId;
		this.name = name;
		this.sort = sort;
		this.email = email;
		this.mailChatName = mailChatName;
		this.imgHeadUrl = imgHeadUrl;
		this.totalCount = totalCount;
		this.isLeader = isLeader;
		this.isParent = isParent;
		this.isUsedMailchat = isUsedMailchat;
		this.isExpand = isExpand;
		this.department = department;
		this.position = position;
		this.phones = phones;
		this.addr = addr;
		this.otherRemarks = otherRemarks;
		this.isEisContact=true;
		this.rEmail = rEmail;
		this.rName = rName;
		this.rNickName = rNickName;
		this.rImgHeadHash = rImgHeadHash;
		this.rCompany = rCompany;
		this.rBirthday = rBirthday;
		this.rDepartment = rDepartment;
		this.rPosition = rPosition;
		this.rPhones = rPhones;
		this.rAddr = rAddr;
		this.rOtherRemarks = rOtherRemarks;
		this.childDepCount=childDepCount;
		this.rData = rData;
		initChildList();
	}


	public void initChildList() {
		if (childList == null) {
			childList = new ArrayList<ContactAttribute>();
		}
	}

	public String getEisName() {
		return eisName;
	}


	public void setEisName(String eisName) {
		this.eisName = eisName;
	}


	public String getDate() {
		return date;
	}

	
	public void setDate(String date) {
		this.date = date;
	}

	public String getCount() {
		if(StringUtil.isEmpty(count)){
			return "1";
		}
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public ContactAttribute() {
	}

	public ContactAttribute(String email, int type) {
		this.email = email;
		this.type = type;
	}


	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setSpellName(String spellName) {
		this.spellName = spellName;
	}

	public String getSpellName() {
		if (TextUtils.isEmpty(spellName)) {
			setSpellName(HanziToPinyin.toPinyin(getNickName()));
		}

		return spellName;
	}

	@Override
	public Character getFirstChar() {
		if (firstChar == null) {
			if (email==null) {
				return '#';
			}
			firstChar = email.toUpperCase(Locale.ENGLISH).charAt(0);
			if (!ContactComparator.isAlpha(firstChar)) {
				firstChar = '#';
			}
		}
		return firstChar;
	}


	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public ContactAttribute cloneSelf() {
		ContactAttribute ca = null;
		try {
			ca = (ContactAttribute) clone();
		} catch (CloneNotSupportedException e) {
			ca = new ContactAttribute();
		}
		return ca;
	}


	public String getImgHeadHash() {
		return imgHeadHash;
	}


	public void setImgHeadHash(String imgHeadHash) {
		this.imgHeadHash = imgHeadHash;
	}




	public String getUploadState() {
		return uploadState;
	}


	public void setUploadState(String uploadState) {
		this.uploadState = uploadState;
	}


	public String getImgHeadName() {
		return imgHeadName;
	}


	public void setImgHeadName(String imgHeadName) {
		this.imgHeadName = imgHeadName;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public int getSendCount() {
		return sendCount;
	}


	public void setSendCount(int sendCount) {
		this.sendCount = sendCount;
	}


	public int getReceiveCount() {
		return receiveCount;
	}


	public void setReceiveCount(int receiveCount) {
		this.receiveCount = receiveCount;
	}


	public String getImgHeadPath() {
		return imgHeadPath;
	}


	public void setImgHeadPath(String imgHeadPath) {
		this.imgHeadPath = imgHeadPath;
	}


	public String getImgBigHeadPath() {
		return imgBigHeadPath;
	}


	public void setImgBigHeadPath(String imgBigHeadPath) {
		this.imgBigHeadPath = imgBigHeadPath;
	}


	public boolean isUsedMailchat() {
		return isUsedMailchat;
	}


	public void setUsedMailchat(boolean isUsedMailchat) {
		this.isUsedMailchat = isUsedMailchat;
	}

//	@Override
//	public boolean equals(Object obj) {
//		ContactAttribute s = (ContactAttribute) obj;
//		return email.equals(s.email) ;
//	}


	public String getCompany() {
		return company;
	}


	public void setCompany(String company) {
		this.company = company;
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


	public boolean isEisContact() {
		return isEisContact;
	}


	public void setEisContact(boolean isEisContact) {
		this.isEisContact = isEisContact;
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


	public String getSort() {
		return sort;
	}


	public void setSort(String sort) {
		this.sort = sort;
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


	public int getTotalCount() {
		return totalCount;
	}


	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}


	public boolean isExpand() {
		return isExpand;
	}


	public String getpId() {
		return pId;
	}

	public void setpId(String pId) {
		this.pId = pId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public ContactAttribute getParent() {
		return parent;
	}

	public void setParent(ContactAttribute parent) {
		this.parent = parent;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setChildList(List<ContactAttribute> childList) {
		this.childList = childList;
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

	public String getrCompany() {
		return rCompany;
	}

	public void setrCompany(String rCompany) {
		this.rCompany = rCompany;
	}

	public String getInvitationCode() {
		return invitationCode;
	}

	public void setInvitationCode(String invitationCode) {
		this.invitationCode = invitationCode;
	}

	public int getChildDepCount() {
		return childDepCount;
	}

	public void setChildDepCount(int childDepCount) {
		this.childDepCount = childDepCount;
	}

	/**
	 * 是否为跟节点
	 * 
	 * @return
	 */
	public boolean isRoot() {
		return parent == null;
	}

	/**
	 * 判断父节点是否展开
	 * 
	 * @return
	 */
	public boolean isParentExpand() {
		if (parent == null)
			return false;
		return parent.isExpand();
	}

	/**
	 * 是否是子节点
	 * 
	 * @return
	 */
	public boolean isLeaf() {
		if (childList == null && !isParent) {
			return true;
		} else {
			if (childList.isEmpty() && !isParent) {
				return true;
			} else {
				return false;
			}
		}
	}

	/* 插入一个child节点到当前节点中 */
	public void addChildNode(ContactAttribute been) {
		initChildList();
		childList.add(been);
	}

	/**
	 * 获取level
	 */
	public int getLevel() {
		return parent == null ? 0 : parent.getLevel() + 1;
	}

	/**
	 * 设置展开
	 * 
	 * @param isExpand
	 */
	public void setExpand(boolean isExpand) {
		this.isExpand = isExpand;
		if (!isExpand) {
			if (childList!=null&&childList.size()>0) {
				for (ContactAttribute eis35TreeBeen : childList) {
					eis35TreeBeen.setExpand(isExpand);
				}
			}
		}
	}

	/* 返回当前节点的父辈节点集合 */
	public List<ContactAttribute> getElders() {
		List<ContactAttribute> elderList = new ArrayList<ContactAttribute>();
		ContactAttribute parentNode = this.getParent();
		if (parentNode == null) {
			return elderList;
		} else {
			elderList.add(parentNode);
			elderList.addAll(parentNode.getElders());
			return elderList;
		}
	}

	/* 返回当前节点的晚辈集合 */
	public List<ContactAttribute> getJuniors() {
		List<ContactAttribute> juniorList = new ArrayList<ContactAttribute>();
		List<ContactAttribute> childList = this.getChildList();
		if (childList == null) {
			return juniorList;
		} else {
			int childNumber = childList.size();
			for (int i = 0; i < childNumber; i++) {
				ContactAttribute junior = childList.get(i);
				juniorList.add(junior);
				juniorList.addAll(junior.getJuniors());
			}
			return juniorList;
		}
	}

	/* 返回当前节点的孩子集合 */
	public List<ContactAttribute> getChildList() {
		return childList;
	}

	/* 删除节点和它下面的晚辈 */
	public void deleteNode() {
		ContactAttribute parentNode = this.getParent();
		String id = this.getId();

		if (parentNode != null) {
			parentNode.deleteChildNode(id);
		}
	}

	/* 删除当前节点的某个子节点 */
	public void deleteChildNode(String childId) {
		List<ContactAttribute> childList = this.getChildList();
		int childNumber = childList.size();
		for (int i = 0; i < childNumber; i++) {
			ContactAttribute child = childList.get(i);
			if (child.getId() == childId || child.getId().equals(childId)) {
				childList.remove(i);
				return;
			}
		}
	}

	/* 动态的插入一个新的节点到当前树中 */
	public boolean insertJuniorNode(ContactAttribute treeBeen) {
		String juniorParentId = treeBeen.getpId();
		if (this.pId == juniorParentId || this.pId.equals(juniorParentId)) {
			addChildNode(treeBeen);
			return true;
		} else {
			List<ContactAttribute> childList = this.getChildList();
			int childNumber = childList.size();
			boolean insertFlag;

			for (int i = 0; i < childNumber; i++) {
				ContactAttribute childNode = childList.get(i);
				insertFlag = childNode.insertJuniorNode(treeBeen);
				if (insertFlag == true)
					return true;
			}
			return false;
		}
	}

	/* 找到一颗树中某个节点 */
	public ContactAttribute findTreeBeenById(String id) {
		if (this.id == id || this.id.equals(id))
			return this;
		if (childList.isEmpty() || childList == null) {
			return null;
		} else {
			int childNumber = childList.size();
			for (int i = 0; i < childNumber; i++) {
				ContactAttribute child = childList.get(i);
				ContactAttribute resultNode = child.findTreeBeenById(child.getId());
				if (resultNode != null) {
					return resultNode;
				}
			}
			return null;
		}
	}
}
