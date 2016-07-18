package cn.mailchat.chatting.protocol;

public enum MQTTCommand {
	// 消息相关
	/**
	 * 发送群组消息
	 */
	SEND_CMESSAGE,
	/**
	 * 发送单聊消息
	 */
	SEND_DMESSAGE,
	/**
	 * 群成员退出群
	 */
	MEMBER_LEAVE_GROUP,
	/**
	 * 群管理员删除群
	 */
	ADMIN_DELETE_GROUP,
	/**
	 * 群管理员踢人
	 */
	ADMIN_KICKED_OUT_MEMBER,
	/**
	 * 创建群
	 */
	CREATE_GROUP,
	/**
	 * 收到群删除消息，退订开群
	 */
	LEAVE_GROUP,
	/**
	 * 进入应用第一次连接成功时的标记
	 * 用于连接成功后,订阅MQTT主题
	 */
	FIRST_CONNECT,
	/**
	 * 创建群时,邀请消息
	 */
	SEND_INVITATION,
	/**
	 * 创建群时失败，退订该订阅
	 */
	CREATE_GROUP_FAIL,
	/**
	 * 受到邀请加入群，订阅
	 */
	JOIN_GROUP,
	/**
	 * 群名称修改
	 */
	RE_GROUPNAME,
	/**
	 * 订阅账户频道
	 */
	SUBSCRIBE_ACCOUNT,
	/**
	 * 订阅群频道
	 */
	SUBSCRIBE_CGROUP,
	/**
	 * 订阅所有频道
	 */
	SUBSCRIBE_ALL,
	/**
	 * 退订账户频道
	 */
	UNSUBSCRIBE_ACCOUNT,
}
