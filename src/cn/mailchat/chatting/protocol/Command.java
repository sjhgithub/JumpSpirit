package cn.mailchat.chatting.protocol;

public enum Command {
	/**
	 * 创建群组
	 */
	CREATE_GROUP,
	/**
	 * 更新群组
	 */
	UPDATE_GROUP,
	/**
	 * 群组置顶
	 */
	STICK_GROUP,
	/**
	 * 删除群组
	 */
	DELETE_GROUP,
	/**
	 * 获取群组列表
	 */
	LIST_GROUPS,
	/**
	 * 获取单个群组信息
	 */
	GET_GROUP,
	/**
	 * 退出群组
	 */
	QUIT_GROUP,
	/**
	 * 获取群成员列表
	 */
	LIST_MEMBERS,
	/**
	 * 增加群成员
	 */
	ADD_MEMBER,
	/**
	 * 移出群成员
	 */
	REMOVE_MEMBER,
	/**
	 * 获取群消息列表
	 */
	LIST_MESSAGES,
	/**
	 * 删除群消息
	 */
	DELETE_MESSAGE,
	/**
	 * 发送群消息
	 */
	SEND_MESSAGE,
	/**
	 * 加入群组
	 */
	JOIN_GROUP,
	/**
	 * 获取群组邀请信息,设置邮箱IMAP
	 */
	GET_GROUP_INVITATION,
	/**
	 * 修改群名称
	 */
	CHANGE_GROUP_NAME,
	/**
	 * 上传文件
	 */
	UPLOAD_FILE,
	/**
	 * 下载文件
	 */
	DOWN_FILE,
	/**
	 * 是否用过邮恰
	 */
	IS_USER,
	/**
	 * 获取单聊邀请邮件
	 */
	INVITATION_EMAIL,
	/**
	 * 获取邮箱设置
	 */
	GET_EMAIL_SETTING,
	/**
	 * 判断是否为三五邮箱及版本号
	 */
	GET_35EMAIL_VERSION,
	/**
	 * 上传用户头像
	 */
	UPLOAD_USER_HEAD_FILE,
	/**
	 * 获取用户信息
	 */
	GET_USER_INFO,
	/**
	 * 同步联系人昵称和头像
	 */
	SYNC_USER_INFO,
	/**
	 * 注册push服务
	 */
	REGISTER_PUSH,
	/**
	 * 注销push服务
	 */
	UNREGISTER_PUSH,
	/**
	 * 多邮箱发送邀请邮件
	 */
	INVITATION_EMAILS,
	/**
	 * 获取35eis列表
	*/
	SYNC_35EIS_LIST,
	/**
	 * 开启服务单聊离线频道
	 */
	DCHAT_OFF_LINE,
	/**
	 * 用户认证
	 */
	USER_CHECK,
	/**
	 * 用户认证
	 */
	USER_ADD_EMAIL,
	/**
	 * 获取邀请码
	 */
	GET_INVITATION_CODE,
	/**
	 * 登记新设备
	 */
	REGISTER_NEW_CLIENT
}
