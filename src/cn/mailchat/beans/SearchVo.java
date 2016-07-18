package cn.mailchat.beans;

import java.util.List;

import cn.mailchat.chatting.beans.MixedChatting;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.mail.Message;
import cn.mailchat.mail.store.LocalStore.LocalMessage;

/**
 * 将搜索结果封装成视图对象
 * 
 * @copyright © 35.com
 * @file name ：SearchVo.java
 * @author ：zhangyq
 * @create Data ：2014-10-30下午1:51:53 
 * @Current Version：v1.0 
 * @History memory :
 * @Date : 2014-10-30下午1:51:53 
 * @Modified by：zhangyq
 * @Description :
 */
public class SearchVo {
	// 联系人结果集
	private List<ContactAttribute> contactList;
	// 混合消息结果集
	private List<MixedChatting> mixChatList;
	// 邮件结果集
	private List<LocalMessage>  emailList;

	public List<ContactAttribute> getContactList() {
		return contactList;
	}
	public void setContactList(List<ContactAttribute> contactList) {
		this.contactList = contactList;
	}
	public List<MixedChatting> getMixChatList() {
		return mixChatList;
	}
	public void setMixChatList(List<MixedChatting> mixChatList) {
		this.mixChatList = mixChatList;
	}
	public List<LocalMessage> getEmailList() {
		return emailList;
	}
	public void setEmailList(List<LocalMessage> emailList) {
		this.emailList = emailList;
	}
	
}
