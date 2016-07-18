package cn.mailchat.controller;

import java.util.HashMap;
import java.util.List;

import android.content.Context;

import cn.mailchat.Account;
import cn.mailchat.AccountStats;
import cn.mailchat.BaseAccount;
import cn.mailchat.beans.ImapAndSmtpSetting;
import cn.mailchat.beans.SearchVo;
import cn.mailchat.chatting.beans.CGroup;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.chatting.beans.CMessage;
import cn.mailchat.chatting.beans.DChat;
import cn.mailchat.chatting.beans.DChatMessage;
import cn.mailchat.chatting.beans.MixedChatting;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.mail.Folder;
import cn.mailchat.mail.Message;
import cn.mailchat.mail.Part;
import cn.mailchat.mail.internet.MimeMessage;

/**
 * Defines the interface that {@link MessagingController} will use to callback to requesters.
 *
 * <p>
 * This class is defined as non-abstract so that someone who wants to receive only a few messages
 * can do so without implementing the entire interface. It is highly recommended that users of this
 * interface use the {@code @Override} annotation in their implementations to avoid being caught by
 * changes in this class.
 * </p>
 */
public class MessagingListener {
    public void searchStats(AccountStats stats) {}


    public void accountStatusChanged(BaseAccount account, AccountStats stats) {}

    public void accountSizeChanged(Account account, long oldSize, long newSize) {}


    public void listFoldersStarted(Account account) {}

    public void listFolders(Account account, Folder[] folders) {}

    public void listFoldersFinished(Account account) {}

    public void listFoldersFailed(Account account, String message) {}


    public void listLocalMessagesStarted(Account account, String folder) {}

    public void listLocalMessages(Account account, String folder, Message[] messages) {}

    public void listLocalMessagesAddMessages(Account account, String folder,
            List<Message> messages) {}

    public void listLocalMessagesUpdateMessage(Account account, String folder, Message message) {}

    public void listLocalMessagesRemoveMessage(Account account, String folder, Message message) {}

    public void listLocalMessagesFinished(Account account, String folder) {}

    public void listLocalMessagesFailed(Account account, String folder, String message) {}


    public void synchronizeMailboxStarted(Account account, String folder) {}

    public void synchronizeMailboxHeadersStarted(Account account, String folder) {}

    public void synchronizeMailboxHeadersProgress(Account account, String folder,
            int completed, int total) {}

    public void synchronizeMailboxHeadersFinished(Account account, String folder,
            int totalMessagesInMailbox, int numNewMessages) {}

    public void synchronizeMailboxProgress(Account account, String folder, int completed,
            int total) {}

    public void synchronizeMailboxNewMessage(Account account, String folder, Message message) {}

    public void synchronizeMailboxAddOrUpdateMessage(Account account, String folder,
            Message message) {}

    public void synchronizeMailboxRemovedMessage(Account account, String folder,
            Message message) {}

    public void synchronizeMailboxFinished(Account account, String folder,
            int totalMessagesInMailbox, int numNewMessages) {}

    public void synchronizeMailboxFailed(Account account, String folder, String message) {}


    public void loadMessageForViewStarted(Account account, String folder, String uid) {}

    public void loadMessageForViewHeadersAvailable(Account account, String folder, String uid,
            Message message) {}

    public void loadMessageForViewBodyAvailable(Account account, String folder, String uid,
            Message message) {}

    public void loadMessageForViewFinished(Account account, String folder, String uid,
            Message message) {}

    public void loadMessageForViewFailed(Account account, String folder, String uid,
            Throwable t) {}

    /**
     * Called when a message for view has been fully displayed on the screen.
     */
    public void messageViewFinished() {}


    public void checkMailStarted(Context context, Account account) {}

    public void checkMailFinished(Context context, Account account) {}

    public void checkMailFailed(Context context, Account account, String reason) {}


    public void sendPendingMessagesStarted(Account account) {}

    public void sendPendingMessagesCompleted(Account account) {}

    public void sendPendingMessagesFailed(Account account) {}


    public void emptyTrashCompleted(Account account) {}


    public void folderStatusChanged(Account account, String folderName, int unreadMessageCount) {}


    public void systemStatusChanged() {}


    public void messageDeleted(Account account, String folder, Message message) {}

    public void messageUidChanged(Account account, String folder, String oldUid, String newUid) {}


    public void setPushActive(Account account, String folderName, boolean enabled) {}


    public void loadAttachmentStarted(Account account, Message message, Part part, Object tag,
            boolean requiresDownload) {}

    public void loadAttachmentFinished(Account account, Message message, Part part, Object tag) {}

    public void loadAttachmentFailed(Account account, Message message, Part part, Object tag,
            Throwable t) {}



    public void pendingCommandStarted(Account account, String commandTitle) {}

    public void pendingCommandsProcessing(Account account) {}

    public void pendingCommandCompleted(Account account, String commandTitle) {}

    public void pendingCommandsFinished(Account account) {}


    /**
     * Called when a remote search is started
     *
     * @param acct
     * @param folder
     */
    public void remoteSearchStarted(Account acct, String folder) {}


    /**
     * Called when server has responded to our query.  Messages have not yet been downloaded.
     *
     * @param numResults
     */
    public void remoteSearchServerQueryComplete(Account account, String folderName, int numResults) { }


    /**
     * Called when a new result message is available for a remote search
     * Can assume headers have been downloaded, but potentially not body.
     * @param account
     * @param folder
     * @param message
     */
    public void remoteSearchAddMessage(Account account, String folder, Message message, int numDone, int numTotal) { }

    /**
     * Called when Remote Search is fully complete
     *
     * @param acct
     * @param folder
     * @param numResults
     */
    public void remoteSearchFinished(Account acct, String folder, int numResults, List<Message> extraResults) {}

    /**
     * Called when there was a problem with a remote search operation.
     *
     * @param acct
     * @param folder
     * @param err
     */
    public void remoteSearchFailed(Account acct, String folder, String err) { }

    /**
     * General notification messages subclasses can override to be notified that the controller
     * has completed a command. This is useful for turning off progress indicators that may have
     * been left over from previous commands.
     *
     * @param moreCommandsToRun
     *         {@code true} if the controller will continue on to another command immediately.
     *         {@code false} otherwise.
     */
    public void controllerCommandCompleted(boolean moreCommandsToRun) {}

    public void enableProgressIndicator(boolean enable) { }
    /**
	 * 接收消息
	 * 
	 * @Description:
	 * @param account
	 * @param DMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-9-26
	 */
    public void chatMessageArrived(Account account,DChatMessage dChatMessage){}
    /**
	 * 获取单聊列表完成
	 * 
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-9-26
	 */
    public void listDchatsFinished(Account account,List<DChat> DChats){}
    /**
   	 * 获取单聊消息列表完成
   	 * 
   	 * @Description:
   	 * @param account
   	 * @see:
   	 * @since:
   	 * @author: shengli
   	 * @date:2014-10-10
   	 */
    public void listDMessagesFinished(String uuid, String dchatUid,final List<DChatMessage> dChatMessages) {}
    /**
   	 * 创建群成功
   	 * 
   	 * @Description:
   	 * @param accountUuid
   	 * @see:
   	 * @since:
   	 * @author: shengli
   	 * @date:2014-10-16
   	 */
    public void createGroupSuccess(String uuid,CGroup cGroup) {}
    /**
   	 * 创建群失败
   	 * 
   	 * @Description:
   	 * @param accountUuid
   	 * @see:
   	 * @since:
   	 * @author: shengli
   	 * @date:2014-10-16
   	 */
    public void createGroupFail(String uuid) {}
    /**
   	 * 加入群成功
   	 * 
   	 * @Description:
   	 * @param accountUuid
   	 * @see:
   	 * @since:
   	 * @author: shengli
   	 * @date:2014-10-16
   	 */
    public void joinGroupSuccess(String uuid) {}
    /**
   	 * 加入群失败
   	 * 
   	 * @Description:
   	 * @param accountUuid
   	 * @see:
   	 * @since:
   	 * @author: shengli
   	 * @date:2014-10-16
   	 */
    public void joinGroupFail(String uuid) {}
    
    /**
	 * 获取单聊天混合列表完成
	 * 
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-17
	 */
    public void listMixedChattingsFinished(Account account,List<MixedChatting> mixedChattings){}
    
    /**
 	 * 获取群聊天消息列表完成
 	 * 
 	 * @Description:
 	 * @param account
 	 * @see:
 	 * @since:
 	 * @author: shengli
 	 * @date:2014-10-17
 	 */
    public void listCMessagesFinished(Account account,List<CMessage> cMessages){}
    
    /**
 	 * 发送单聊消息成功
 	 * 
 	 * @Description:
 	 * @param account
 	 * @see:
 	 * @since:
 	 * @author: shengli
 	 * @date:2014-10-20
 	 */
    public void sendDMessagesSuccess(Account account,String dMessageUid){}
    
    /**
 	 * 发送单聊消息失败
 	 * 
 	 * @Description:
 	 * @param account
 	 * @see:
 	 * @since:
 	 * @author: shengli
 	 * @date:2014-10-20
 	 */
    public void sendDMessagesFail(Account account,String dMessageUid){}
    /**
	 * 接收群消息
	 * 
	 * @Description:
	 * @param account
	 * @param CMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-20
	 */
    public void cMessageArrived(Account account,CMessage cMessage){}

	/**
	 * 群聊消息发送成功
	 *
	 * @Description:
	 * @param account
	 * @param CMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-22
	 */
	public void sendCMessagesSuccess(Account account, String cMessageUid) {}

	/**
	 * 发送群聊消息失败
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-20
	 */
	public void sendCMessagesFail(Account account, String cMessageUid) {}

	/**
	 * 
	 * method name: setDChatNewMsgAlertSuccess 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account
	 *      @param cMessageUid    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-10-23 下午8:32:54	@Modified by：zhangjx
	 *     @Description：设置新消息提醒成功
	 */
	public void setDChatNewMsgAlertSuccess(Account account, String cMessageUid) {}
	/**
	 * 
	 * method name: setDChatNewMsgAlertFail 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account
	 *      @param cMessageUid    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-10-23 下午8:34:01	@Modified by：zhangjx
	 *     @Description：设置新消息提醒失败
	 */
	public void setDChatNewMsgAlertFail(Account account, String cMessageUid) {}
	/**
	 * 
	 * method name: setDChatStickMsgTopSuccess 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account
	 *      @param dChatDMessageUid
	 *      @param isSticked    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-10-23 下午8:44:24	@Modified by：zhangjx
	 *     @Description：设置单聊置顶成功
	 */
	public void setDChatStickMsgTopSuccess(Account account,String dChatDMessageUid,  boolean isSticked) {}
	/**
	 * 
	 * method name: setDChatStickMsgTopFail 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account
	 *      @param dChatDMessageUid
	 *      @param isSticked    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-10-23 下午8:44:32	@Modified by：zhangjx
	 *     @Description：设置单聊置顶失败
	 */
	public void setDChatStickMsgTopFail(Account account,String dChatDMessageUid,  boolean isSticked) {}
	/**
	 * 
	 * method name: deleteDChatSuccess 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account
	 *      @param dChatDMessageUid    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-10-23 下午8:44:40	@Modified by：zhangjx
	 *     @Description：删除单聊成功
	 */
	public void deleteDChatSuccess(Account account, String dChatUid) {}
	/**
	 * 
	 * method name: deleteDChatFail 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account
	 *      @param dChatDMessageUid    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-10-23 下午8:44:47	@Modified by：zhangjx
	 *     @Description：删除单聊失败
	 */
	public void deleteDChatFail(Account account, String dChatUid) {}
	/**
	 * 
	 * method name: getDChatSuccess 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account
	 *      @param dChatDMessageUid    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-10-23 下午9:33:09	@Modified by：zhangjx
	 *     @Description：获取dchat对象成功
	 */
	public void getDChatSuccess(Account account, DChat dchat) {}
	/**
	 * 
	 * method name: getDChatFail 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account
	 *      @param dChatDMessageUid    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-10-23 下午9:33:22	@Modified by：zhangjx
	 *     @Description：获取dchat对象失败
	 */
	public void getDChatFail(Account account, DChat dchat) {}


	/** 
	 * method name: updateDChatReadStateSuccess 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-10-24 下午1:34:25	@Modified by：zhangjx
	 *     @Description：更新未读数为0
	 */
	public void updateDChatReadStateSuccess(Account account) {}
	


	/**
	 * 异步获取群列表成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-24
	 */
	public void listSynCGroupsSuccess(Account account) {}

	/**
	 * 异步获取群列表失败
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-24
	 */
	public void listSynCGroupsFail(Account account) {}
	/** 
	 * method name: getTotalMsgUnreadCountSuccess 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-10-24 下午6:00:36	@Modified by：zhangjx
	 *     @Description：获取混合消息列表所有未读消息数
	 */
	public void getTotalMsgUnreadCountSuccess(Account account,int totalCount) {}
	
	/**
	 * 群成员退出群组成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-30
	 */
	public void leaveCGroupSuccess(Account account) {}
	/**
	 * 群成员退出群组成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-30
	 */
	public void leaveCGroupFail(Account account) {}
	/**
	 * 管理员删除群组成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-30
	 */
	public void deleteCGroupSuccess(Account account) {}
	/**
	 * 管理员删除群组失败
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-30
	 */
	public void deleteCGroupFail(Account account) {}
	
	/**
	 * 更新群组未读数成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-31
	 */
	public void updateCGroupUntreatedCountSuccess(Account account) {}
	/**
	 * 设置群组置顶成功
	 * 
	 * method name: stickGroupSuccess 
	 * function @Description: TODO
	 * Parameters and return values description：
	 * @param account
	 * @param groupUid
	 * @param isSticked    field_name
	 * void    return type
	 * @History memory：
	 * @Date：2014-10-31 上午10:40:33	@Modified by：zhangyq
	 * @Description：
	 */
	public void stickGroupSuccess(Account account,String groupUid,  boolean isSticked) {}
	
	/**
	 * 设置群组置顶失败
	 * 
	 * method name: stickGroupFail 
	 * function @Description: TODO
	 * Parameters and return values description：
	 * @param account
	 * @param groupUid
	 * @param isSticked    field_name
	 * void    return type
	 * @History memory：
	 * @Date：2014-10-30 下午5:22:18	@Modified by：zhangyq
	 * @Description：
	 */
	public void stickGroupFail(Account account,String groupUid,  boolean isSticked) {}
	public void globalChatSearchSuccess(Account account, 	List<MixedChatting>  searchVo) {}
	public void globalContantSearchSuccess(Account account, List<ContactAttribute> searchVo) {}

	/**
	 * 从本地加载群组信息
	 * 
	 * method name: getGroupInfoSuccess 
	 * function @Description: TODO
	 * Parameters and return values description：
	 * @param group    field_name
	 * void    return type
	 * @History memory：
	 * @Date：2014-11-5 下午7:33:02	@Modified by：zhangyq
	 * @Description：
	 */
	public void getGroupInfoSuccess(Account acc,CGroup group) {
	}

	public void listMembersSuccess(Account acc,List<CGroupMember> members) {
	}

	/**
	 * 删除群成员失败
	 * 
	 * method name: delGroupMemberFailed 
	 * function @Description: TODO
	 * Parameters and return values description：
	 * @param member    field_name
	 * void    return type
	 * @History memory：
	 * @Date：2014-11-5 下午5:20:31	@Modified by：zhangyq
	 * @Description：
	 */
	public void delGroupMemberFailed(Account acc,CGroupMember member) {
	}

	/**
	 * 删除群成员成功
	 * 
	 * method name: delGroupMemberSuccess 
	 * function @Description: TODO
	 * Parameters and return values description：
	 * @param member    field_name
	 * void    return type
	 * @History memory：
	 * @Date：2014-11-5 下午5:20:18	@Modified by：zhangyq
	 * @Description：
	 */
	public void delGroupMemberSuccess(Account acc,CGroupMember member) {}

	/**
	 * 增加群成员成功
	 * 
	 * method name: addGroupMemberSuccess 
	 * function @Description: TODO
	 * Parameters and return values description：
	 * @param member    field_name
	 * void    return type
	 * @History memory：
	 * @Date：2014-11-10 下午6:17:50	@Modified by：zhangyq
	 * @Description：
	 */
	public void addGroupMemberSuccess(Account acc,String cGroupUid,List<CGroupMember> member) {}
	
	/**
	 * 增加群成员失败
	 * 
	 * method name: addGroupMemberFailed 
	 * function @Description: TODO
	 * Parameters and return values description：
	 * @param member    field_name
	 * void    return type
	 * @History memory：
	 * @Date：2014-11-10 下午6:19:23	@Modified by：zhangyq
	 * @Description：
	 */
	public void addGroupMemberFailed(Account acc,List<CGroupMember> member) {}


	/** 
	 * method name: searchMessagesFinished 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param uuid
	 *      @param groupUid
	 *      @param keyWord
	 *      @param messages    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-11-5 下午6:27:20	@Modified by：zhangjx
	 *     @Description：搜索群聊天消息成功
	 */
	public void searchGroupMessagesFinished(String uuid, String groupUid,
			String keyWord, List<CMessage> messages) {}


	/** 
	 * method name: searchDChatMessagesFinished 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param uuid
	 *      @param dChatUid
	 *      @param currentKeyWord
	 *      @param localMessages    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-11-5 下午6:31:36	@Modified by：zhangjx
	 *     @Description：搜索单聊消息
	 */
	public void searchDChatMessagesFinished(String uuid, String dChatUid,
			String currentKeyWord, List<DChatMessage> localMessages,List<String> nickNameList) {}
	
	/**
	 * 获取群信息成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-31
	 */
	public void getGroupInfoSuccess(Account account,CGroup cGroup,List<CGroupMember> members) {}
	
	/**
	 * 获取群信息成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-31
	 */
	public void getGroupInfoFail(Account account,String groupUid) {}
	
	/**
	 * 获取本地群信息成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-31
	 */
	public void getLocalGroupInfoSuccess(Account account,CGroup cGroup,String membersName) {}

	/**
	 * 获取本地群信息成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-11-14
	 */
	public void getLocalGroupInfoFail(Account account,CGroup cGroup) {}
	/** 
	 * method name: changeGroupSuccess 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param group    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-11-6 下午8:07:20	@Modified by：zhangjx
	 *     @Description：修改群名称成功
	 */
	public void changeGroupSuccess(Account account,CGroup group) {
		// TODO Auto-generated method stub
		
	}


	/** 
	 * method name: changeGroupFailed 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param group
	 *      @param string    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-11-6 下午8:07:25	@Modified by：zhangjx
	 *     @Description：修改群名称失败
	 */
	public void changeGroupFailed(Account account,CGroup group, String string) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * 收到删除群组消息后，退订成功，删除本地群组成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-11-10
	 */
	public void delteGroupInfoByMemberSuccess(Account account,CGroup cGroup) {}
	/**
	 * 收到被踢出群组消息后，退订成功，删除本地群组成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-4-16
	 */
	public void kickedOutGroupByMemberSuccess(Account account,CGroup cGroup) {}
	
	public void sendMailSuccess(Account account) {}
	public void sendMailFailed(Account account, String uid, String reason,
			MessageException exception) {}
	/**
	 * 重新下载群聊预览图成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-11-17
	 */
	public void againDownCGroupThumbnailImageSuccess(Account account) {}
	/**
	 * 重新下载群聊预览图失败
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-11-17
	 */
	public void againDownCGroupThumbnailImageFailed(Account account) {}
	/**
	 * 重新下载单聊预览图成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-11-17
	 */
	public void againDownDChatThumbnailImageSuccess(Account account) {}
	/**
	 * 重新下载单聊预览图失败
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-11-17
	 */
	public void againDownDChatThumbnailImageFailed(Account account) {}
	/**
	 * 获取更新后的群成员名称
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-11-19
	 */
	public void listLocalGroupMemberSuccess(Account acc,String cGroupUid,final String membersName,List<CGroupMember> cGroupMembers) {}
	
	/**
	 * 发送邀请邮件成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-11-19
	 */
	public void sendInvitationSuccess(Account acc, String invitationEmail) {}
	/**
	 * 发送邀请邮件失败
	 *
	 * @Description:
	 * @param account
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-11-19
	 */
	public void sendInvitationFailed(Account acc, String invitationEmail) {}
	/**
	 * 添加联系人完成
	 *
	 * @Description:
	 * @param account
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-04
	 */
	public void addContactFinish(Account acc,List<ContactAttribute> contactList){}
	/**
	 * 显示/隐藏群组成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-04
	 */
	public void hiddenGroupSuccess(Account acc) {}
	/**
	 * 更新群聊草稿成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-12
	 */
	public void updateCGroupDraftSuccess(Account acc) {}
	/**
	 * 更新单聊草稿成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-12
	 */
	public void updateDchatDraftSuccess(Account acc) {}
	
	/**
	 * 附件进度
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-5-18
	 */
	public void fileDownloadStart(Account acc,String id){};
	/**
	 * 附件进度
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-18
	 */
	public void fileDownloadProgress(Account acc,String id,int progress){};
	
	/**
	 * 附件下载完成
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-12
	 */
	public void fileDownloadFinished(Account acc,String id){};
	
	/**
	 * 附件下载失败
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-12
	 */
	public void fileDownloadFailed(Account acc,String id){};
	/**
	 * 附件下载暂停
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-5-18
	 */
	public void fileDownloadInterrupt(Account acc,String id){};
	/**
	 * 
	 * method name: copyOrMoveMailSuccess 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account
	 *      @param isCopy    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-12-19 下午1:42:29	@Modified by：zhangjx
	 *     @Description： 复制或者保存成功或失败
	 */
	public void copyOrMoveMailSuccess(Account account,boolean isCopy) {}
	public void copyOrMoveFailed(Account account,boolean isCopy) {}
	
	/**
	 * 根据email获取联系人成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-12
	 */
	public void getContactForDChatSuccess(Account acc,String email,ContactAttribute  contact ){};
	

	/**
	 * 根据email获取联系人成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-26
	 */
	public void getContactForMessageSuccess(Account acc,String email,ContactAttribute  contact ){};
	
	/**
	 * 发送单聊消息跳转销毁界面回调
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-12
	 */
	public void isSingleSetFinished(Account acc){
		
	}
	/**
	 * 
	 * method name: receiveMessage 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param acc
	 *      @param folder    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2015-3-20 下午3:37:56	@Modified by：zhangjx
	 *     @Description：收到非默认账户
	 */
	public void receiveMessage(Account acc,Folder folder){}
	/**
	 * 登录失败提示
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-26
	 */
	public void loginDialogShow(String  email,boolean isShowmanualSettingImp,int errorCode,boolean is35Mail){}
	/**
	 * 获取高级登录设置失败
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-26
	 */
	public void getEmailSetFail(String  email,int errorCode,boolean isGoImapSetting){}
	/**
	 * 获取高级登录设置成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-26
	 */
	public void getEmailSetSuccess(String  email,ImapAndSmtpSetting imapAndSmtpSetting,boolean isGoImapSetting){}
	/**
	 * 高级登录成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-26
	 */
	public void advancedLoggingSuccess(String  email){}
	/**
	 * 发送群聊消息到服务器成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-26
	 */
	public void sendCMessageTimeSuccess(Account account, String cMessageUid,long time) {}
	/**
	 * 发送单聊消息到服务器成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-26
	 */
	public void sendDMessageTimeSuccess(Account account, String dMessageUid,long time) {}
	/**
	 * 获取35邮箱版本回调方法
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-5-6
	 */
	public void get35MailVersionForLogin(String email,int version) {}
	/**
	 * 保存聊天大图成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-7-15
	 */
	public void chattingSaveImageSuccess(Account account){}
	/**
	 * 保存聊天大图失败
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-7-15
	 */
	public void chattingSaveImageFail(Account account){}
	/**
	 * 
	 * method name: uploadUserInfoStart 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2015-4-17 下午2:57:19	@Modified by：zhangjx
	 *     @Description：上传用户头像开始
	 */
	public void uploadUserInfoStart(Account account) {}
	/**
	 * 
	 * method name: uploadUserInfoSuccess 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2015-4-17 下午2:57:46	@Modified by：zhangjx
	 *     @Description：上传头像成功
	 */
	public void uploadUserInfoSuccess(Account account,ContactAttribute newContactAttribute) {}
	/**
	 * 
	 * method name: uploadUserInfoFailed 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2015-4-17 下午2:57:53	@Modified by：zhangjx
	 *     @Description：上传头像失败
	 */
	public void uploadUserInfoFailed(Account account) {}
	/**
	 * 
	 * method name: loadUserInfoSuccess 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account
	 *      @param newContactAttribute    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2015-4-17 下午5:30:17	@Modified by：zhangjx
	 *     @Description：获取用户信息开始
	 */
	public void loadUserInfoStart(Account account) {}
	/**
	 * 
	 * method name: loadUserInfoSuccess 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account
	 *      @param newContactAttribute    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2015-4-17 下午5:30:17	@Modified by：zhangjx
	 *     @Description：获取用户信息成功
	 */
	public void loadUserInfoSuccess(Account account,ContactAttribute newContactAttribute) {}
	/**
	 * 
	 * method name: loadUserInfoFailed 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account
	 *      @param err    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2015-4-17 下午5:31:22	@Modified by：zhangjx
	 *     @Description：获取用户信息失败
	 */
	public void loadUserInfoFailed(Account account) {}
	/**
	 * 上传文件进度开始
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-7-2
	 */
	public void uploadStart(Account account,String id){}
	/**
	 * 上传文件进度
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-5-8
	 */
	public void uploadProgress(Account account,String id, int progress){}
	/**
	 *上传文件终断
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-7-2
	 */
	public void uploadInterrupt(Account account,String id){}
	/**
	 * 上传文件完成
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-5-8
	 */
	public void uploadFinished(Account account,String id){}

	/**
	 * 更新聊天界面头像
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-5-22
	 */
	public void updateChattingImgHead(Account account,ContactAttribute newContactAttribute){}
	/**
	 * 同步带昵称头像的群组消息完成
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-5-22
	 */
	public void listNikeNameAndAvatarCMessagesFinished(Account account,List<CMessage> cMessages){}
	/**
	 * 搜索聊天混合消息完成(转发页面搜索条件)
	 *
	 * @Description:
	 * @param account
	 * @param List<MixedChatting>
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-6-17
	 */
	public void searchMixedChattingByForwardFinished(Account account,List<MixedChatting> mixedChattings){}
	/**
	 * 搜索联系人完成(转发页面搜索条件)
	 *
	 * @Description:
	 * @param account
	 * @param List<MixedChatting>
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-6-17
	 */
	public void searchContactsByForwardFinished(Account account,List<ContactAttribute> contacts){}
	/**
	 *
	 * method name: getCoustomFoldersCountFinished
	 * function @Description: TODO
	 * Parameters and return values description:
	 *      @param account
	 *      @param count   field_name
	 *      void   return type
	 *  @History memory：
	 *     @Date：2015-6-29 下午6:03:12	@Modified by：zhangjx
	 *     @Description:获取自定义邮件夹个数
	 */
	public void getCoustomFoldersCountFinished(Account account,int count){}
	/**
	 * 更新消息列表消息发送状态开始
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-7-7
	 */
	public void updateChatListMessageSendStateStart(Account account){}
	/**
	 * 更新消息列表消息发送状态完成
	 *
	 * @Description:
	 * @param account
	 * @param sendState 成功 失败状态
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-7-7
	 */
	public void updateChatListMessageSendStateFinished(Account account,int sendState){}
	/**
	 * 获取已经使用邮洽的联系人完成
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-7-27
	 */
	public void getContactsByUsedMailChatFinished(Account account,List<ContactAttribute> contacts){}
	/**
	 * 群发邀请邮件成功
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-7-28
	 */
	public void inviteActionUsersSuccess(Account account){}
	/**
	 * 群发邀请邮件失败
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-7-28
	 */
	public void inviteActionUsersFailed(Account account){}

	/**
	 * 
	 * method name: list35EisStart function @Description: TODO Parameters and
	 * return values description:
	 * 
	 * @param account
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-18 下午4:08:17 @Modified by：zhangjx
	 * @Description:获取eis列表数据开始
	 */
	public void list35EisStart(Account account) {
	}

	/**
	 * 
	 * method name: list35EisSuccess function @Description: TODO Parameters and
	 * return values description:
	 * 
	 * @param account
	 * @param result
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-18 下午4:09:19 @Modified by：zhangjx
	 * @Description:获取eis列表数据成功
	 */
	public void list35EisSuccess(Account account, List<ContactAttribute> result) {
	}

	/**
	 * 
	 * method name: list35EisFailed function @Description: TODO Parameters and
	 * return values description:
	 * 
	 * @param account
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-18 下午4:09:35 @Modified by：zhangjx
	 * @Description:获取eis列表数据失败
	 */
	public void list35EisFailed(Account account) {
	}

	/**
	 * 
	 * method name: loadPersonalContactForViewStarted function @Description:
	 * TODO Parameters and return values description:
	 * 
	 * @param account
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-18 下午4:09:50 @Modified by：zhangjx
	 * @Description:获取本地个人通讯录开始
	 */
	public void loadPersonalContactForViewStarted(Account account) {
	}

	/**
	 * 
	 * method name: loadPersonalContactForViewFinished function @Description:
	 * TODO Parameters and return values description:
	 * 
	 * @param account
	 * @param allContacts
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-18 下午4:10:17 @Modified by：zhangjx
	 * @Description:获取本地个人通讯录成功
	 */
	public void loadPersonalContactForViewFinished(Account account,
			List<ContactAttribute> allContacts) {
	}

	/**
	 * 
	 * method name: loadPersonalContactForViewFailed function @Description: TODO
	 * Parameters and return values description:
	 * 
	 * @param account
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-18 下午4:10:37 @Modified by：zhangjx
	 * @Description:获取本地个人通讯录失败
	 */
	public void loadPersonalContactForViewFailed(Account account) {
	}

	/**
	 * 
	 * method name: loadSameDomainContactForViewStarted function @Description:
	 * TODO Parameters and return values description:
	 * 
	 * @param account
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-18 下午4:10:54 @Modified by：zhangjx
	 * @Description:获取相同域联系人开始
	 */
	public void loadSameDomainContactForViewStarted(Account account) {
	}

	/**
	 * 
	 * method name: loadSameDomainContactForViewFinished function @Description:
	 * TODO Parameters and return values description:
	 * 
	 * @param account
	 * @param allContacts
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-18 下午4:11:23 @Modified by：zhangjx
	 * @Description:获取本地相同域联系人成功
	 */
	public void loadSameDomainContactForViewFinished(Account account,
			List<ContactAttribute> allContacts) {
	}

	/**
	 * 
	 * method name: loadSameDomainContactForViewFailed function @Description:
	 * TODO Parameters and return values description:
	 * 
	 * @param account
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-18 下午4:11:50 @Modified by：zhangjx
	 * @Description:获取本地相同域联系人失败
	 */
	public void loadSameDomainContactForViewFailed(Account account) {
	}
	public void loadPersonalContactWithoutUsedFinished(Account account,
			List<ContactAttribute> allContacts) {
	}
	public void loadSameDomainContactWithoutUsedFinished(Account account,
			List<ContactAttribute> allContacts) {
	}
	/**
	 * 
	 * method name: addContactSuccess function @Description: TODO Parameters and
	 * return values description:
	 * 
	 * @param account
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-1 下午8:32:50 @Modified by：zhangjx
	 * @Description:新增联系人成功
	 */
	public void addContactSuccess(Account account,ContactAttribute contact,boolean needRefreshView) {
	}

	/**
	 * 
	 * method name: addContactFailed function @Description: TODO Parameters and
	 * return values description:
	 * 
	 * @param account
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-1 下午8:33:12 @Modified by：zhangjx
	 * @Description:新增联系人失败
	 */
	public void addContactFailed(Account account) {
	}

	/**
	 * 
	 * method name: loadCommonContactForViewStarted function @Description: TODO
	 * Parameters and return values description:
	 * 
	 * @param account
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-18 下午4:12:53 @Modified by：zhangjx
	 * @Description:获取本地常用联系人开始
	 */
	public void loadCommonContactForViewStarted(Account account) {
	}

	/**
	 * 
	 * method name: loadCommonContactForViewFinished function @Description: TODO
	 * Parameters and return values description:
	 * 
	 * @param account
	 * @param CommonContacts
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-18 下午4:13:13 @Modified by：zhangjx
	 * @Description:获取本地常用联系人成功
	 */
	public void loadCommonContactForViewFinished(Account account,
			List<ContactAttribute> commonContacts) {
	}

	/**
	 * 清空单聊消息完成
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-9-18
	 */
	public void deleteAllDMessageFinished(Account account,String dChatUid){}
	public void deleteAllDMessageFailed(Account account,String dChatUid){}
	/**
	 * 清空群聊消息完成
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-9-18
	 */
	public void deleteAllCMessageFinished(Account account,String cGroupUid){}
	public void deleteAllCMessageFailed(Account account,String cGroupUid){}
	public void loadCommonContactForViewFailed(Account account) {
	}

	public void searchContactStart(Account account) {
	}

	public void searchContactSuccess(Account account,String currFragmentTag,
			List<ContactAttribute> eisContacts, List<ContactAttribute> commonContacts,
			List<ContactAttribute> personalContacts,
			List<ContactAttribute> sameDomainContacts) {
	}

	public void searchContactFailed(Account account) {
	}
	public void searchAllContactStart(Account account) {
	}

	public void searchAllContactSuccess(Account account,List<ContactAttribute> eisContacts) {
	}

	public void searchAllContactFailed(Account account) {
	}
	/**
	 * 刷新主界面actionBar
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-10-15
	 */
	public void refreshMainActionBar(Account account){}
	/**
	 * 群聊界面获取联系人在EIS中的信息(部门职位等)
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-11-30
	 */
	public void get35EisContactsInfoForGroupSuccess(Account account,ContactAttribute contact){}
	/**
	 * 群聊界面获取联系人在EIS中的信息(部门职位等)
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-11-30
	 */
	public void get35EisContactsInfoForGroupFailed(Account account,ContactAttribute contact){}

	/**
	 * 单聊界面获取联系人在EIS中的信息(部门职位等)
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-11-30
	 */
	public void get35EisContactsInfoForDChatSuccess(Account account,ContactAttribute contact){}
	/**
	 * 单聊界面获取联系人在EIS中的信息(部门职位等)
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-11-30
	 */
	public void get35EisContactsInfoForDChatFailed(Account account,ContactAttribute contact){}
	/**
	 * 
	 * method name: getInvitationCodeStart
	 * function @Description: TODO
	 * Parameters and return values description:
	 *      @param account   field_name
	 *      void   return type
	 *  @History memory：
	 *     @Date：2015-12-11 上午11:10:14	@Modified by：zhangjx
	 *     @Description:获取唯一邀请码
	 */
	public void getInvitationCodeStart(Account account) {
	}

	public void getInvitationCodeSuccess(Account account,String iCode) {
	}

	public void getInvitationCodeFailed(Account account) {
	}
	/**
	 * 更新认证账户错误提示
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-12-22
	 */
	public void UserIfCertificateProblem(Account account) {
	}
	/**
	 * 新增联系人已存在回调
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-12-24
	 */
	public void AddContactHasExist(Account account) {}
	/**
	 * 获取账户缓存大小
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2016-1-22
	 */
	public void getAccountCacheSize(Account account,long size) {}
	/**
	 * 新增联系人已存在回调
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2016-1-22
	 */
	public void cleanAccountCache(Account account,boolean isCleanSuccess) {}
	/**
	 * 微信分享
	 */
    public void   weiXinShareStart(){}
    public void   weiXinShareSucceed(String openid_info) {}
    public void   weiXinShareFailed(){}
	/**
	 * 登录oa
	 */
    public void   loginInOAStart(){}
    public void   loginInOASucceed(Account account) {}
    public void   loginInOAFailed(){}

	/**
	 * 已经绑定了oa
	 * 
	 * @param account
	 */
	public void alreadyBindOA(Account account) {

	}
	/**
	 * 已经解绑定了oa
	 * 
	 * @param account
	 */
	public void unBindOA(Account account) {

	}

	/**
	 * 接收到新的oa事务，主界面tab红点显示
	 * 
	 * @param account
	 */
	public void arrivedNewOaMessageArrived(Account account) {
	}
}
