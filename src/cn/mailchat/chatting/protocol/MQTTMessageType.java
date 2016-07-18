package cn.mailchat.chatting.protocol;

public class MQTTMessageType {
	//群聊系统消息
	public static final int SYSTEM_JOINGROUP_MESSAGE =001;//群聊邀请消息
	public static final int SYSTEM_DELETEGROUP_MESSAGE =002;//群删除消息
	public static final int SYSTEM_LEAVEGROUP_MESSAGE =003;//离开群消息
	public static final int SYSTEM_GROUPRENAME_MESSAGE =004;//修改群名称消息
	public static final int SYSTEM_KICKED_OUT_GROUPR_MESSAGE =005;//被踢出群消息
	public static final int SYSTEM_KICKED_OUT_MEMBER_MESSAGE =006;//被踢出群消息,让其他群成员删除数据
	//单聊系统消息  
	public static final int SEND_USER_MESSAGE_TO_HELP_ACCOUNT =1002;//用户给小助手自动发消息
}
