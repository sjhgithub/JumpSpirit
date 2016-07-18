package cn.mailchat.controller;

import java.util.List;

import cn.mailchat.chatting.beans.CGroup;
import cn.mailchat.chatting.beans.CGroupMember;

/**
 * 群组成员操作的监听回调
 * 
 * @copyright © 35.com
 * @file name ：GroupMembersCallback.java
 * @author ：zhangyq
 * @create Data ：2014-11-5上午11:31:04 
 * @Current Version：v1.0 
 * @History memory :
 * @Date : 2014-11-5上午11:31:04 
 * @Modified by：zhangyq
 * @Description :
 */
public class GroupMembersCallback {
	/**
	 * 本地加载群组信息
	 * 
	 * method name: getGroupInfoSuccess 
	 * function @Description: TODO
	 * Parameters and return values description：
	 * @param group    field_name
	 * void    return type
	 * @History memory：
	 * @Date：2014-11-5 上午11:32:18	@Modified by：zhangyq
	 * @Description：
	 */
	public void getGroupInfoSuccess(CGroup group) {
	}
	
	public void listMembersSuccess(List<CGroupMember> members) {
	}
	
	public void sysnMembersSuccess(List<CGroupMember> members) {
	}

	public void sysnMembersFailed(String error) {
	}
	
	/**
	 * 检索群成员
	 * 
	 * method name: onSearchlistCGroupMember 
	 * function @Description: TODO
	 * Parameters and return values description：
	 * @param members    field_name
	 * void    return type
	 * @History memory：
	 * @Date：2014-11-5 下午1:17:37	@Modified by：zhangyq
	 * @Description：
	 */
	public void onSearchlistCGroupMember(List<CGroupMember> members) {
	}
	
	public void delGroupMemberSuccess(CGroupMember mGroupMember) {
	}

	public void delGroupMemberFailed(CGroupMember mGroupMember,
			MessageException exception) {
	}

}
