package cn.mailchat.mail.store;

/**
 * 所有的表结构定义
 * 
 * @author: cuiwei
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2014-3-24
 */
public class Columns {

	/**
	 * 附件表
	 */
	public static class TbAttachments {

		public static final String TB_NAME = "tb_attachments";
		public static final String F_ID = "_id";
		// 附件id
		public static final String F_ATTACHMENT_ID = "f_attachment_id";
		// 邮件sid
		public static final String F_MESSAGE_ID = "f_message_id";
		// 名称
		public static final String F_NAME = "f_name";
		public static final String F_FILE_PATH = "f_file_path";
		// 大小
		public static final String F_SIZE = "f_size";
		// 类型
		public static final String F_CONTENT_TYPE = "f_mime_type";
		// 下载状态(自有协议）
		public static final String F_DOWNLOAD_STATE = "f_download_state";
		// 原附件ID(自有协议，转发、回复等）
		public static final String F_SOURCE_ATTACHMENT_ID = "f_source_attachment_id";
		// 原附件ID(自有协议，转发、回复等）
		public static final String F_SOURCE_MESSAGE_UID = "f_source_message_uid";
		// 语音附件时长(自有协议）
		public static final String F_VOICE_DURATION = "f_voice_duration";
		// 是否是内嵌图片
		public static final String F_IS_INLINE = "f_is_inline";
		// 若是子文件，则存储父压缩包的id
		public static final String F_PARENT_ATTACHMENT_ID = "f_parent_attachment_id";
		// 内嵌图片的cid
		public static final String F_CONTENT_ID = "f_content_id";
		// 附件编码
		public static final String F_ENCODING = "f_encoding";

	}

	/**
	 * 邮件体
	 */
	public static class TbBody {

		public static final String TB_NAME = "tb_body";
		public static final String F_ID = "_id";
		// 邮件sid
		public static final String F_MESSAGE_ID = "f_message_id";
		// 纯文本
		public static final String F_TEXT_CONTENT = "f_text_content";
		// 富文本
		public static final String F_HTML_CONTENT = "f_html_content";
		// 原始邮件富文本
		public static final String F_ORIGINAL_HTML = "f_original_html";
		// 是否引用原文
		public static final String F_MODIFIED = "f_modified";
	}

	// /**
	// * 会议邮件
	// */
	// public static class TbCalendars {
	//
	// public static final String TB_NAME = "tb_calendars";
	// public static final String F_ID = "_id";
	// // 邮件uid
	// public static final String F_MESSAGE_UID = "f_message_uid";
	// // 会议状态
	// public static final String F_CALENDAR_STATE = "f_calendar_state";
	// // 会议开始时间
	// public static final String F_CALENDAR_START_TIME =
	// "f_calendar_start_time";
	// // 会议结束时间
	// public static final String F_CALENDAR_END_TIME = "f_calendar_end_time";
	// // 会议地点
	// public static final String F_CALENDAR_LOCATION = "f_calendar_location";
	// }

	/**
	 * 离线 操作历史
	 */
	public static class TbCommands {

		public static final String TB_NAME = "tb_commands";
		public static final String F_ID = "_id";
		public static final String F_EMAIL = "f_email";
		// 操作类型
		public static final String F_COMMAND = "f_command";
		// 参数（取决于操作类型）
		public static final String F_ARGUMENTS = "f_arguments";
	}

	/**
	 * 文件夹
	 */
	public static class TbFolders {

		public static final String TB_NAME = "tb_folders";
		public static final String F_ID = "_id";
		// 最新更新时间
		public static final String F_UPDATE_TIME = "f_update_time";
		/**
		 * 文件夹名称字段
		 */
		public static final String F_NAME = "f_name";
		public static final String F_SYNC = "f_sync";// 按名称判断文件夹类型，是否同步，是不是服务器上有的文件夹
		// 文件夹类型（兼容自有协议）notuse
		public static final String F_TYPE = "f_type";
		// // 文件夹顺序 todo
		// public static final String F_ORDER = "f_order";
		// 父文件夹ID（兼容自有协议），默认0 notuse
		public static final String F_PARENT_ID = "f_parent_id";
		// 未读数 notuse
		public static final String F_UNREAD = "f_unread";
		// 总数 notuse
		public static final String F_COUNT = "f_count";
		// 服务器端邮件夹id
		public static final String F_SERVER_ID = "f_serverId";
		// 服务器端邮件夹名称
		public static final String F_DISPLAY_NAME = "f_displayName";
		// 邮件夹层级，1一级，2二级，3三级
		public static final String F_LEVEL = "f_level";
		// 分隔符
		public static final String F_SEPARATOR = "f_separator";
	}

	/**
	 * 邮件摘要
	 */
	public static class TbMessages {

		public static final String TB_NAME = "tb_messages";
		public static final String F_ID = "_id";
		// 邮件sid
		public static final String F_SID = "f_sid";
		// 文件夹id
		public static final String F_FOLDER_ID = "f_folder_id";
		// public static final String F_ORIGINAL_FOLDER_ID =
		// "f_original_folder_id";
		// 主题
		public static final String F_SUBJECT = "f_subject";
		// 发件人
		public static final String F_SENDER = "f_sender";
		// 收件人列表
		public static final String F_TO_LIST = "f_to_list";
		// 抄送列表
		public static final String F_CC_LIST = "f_cc_list";
		// 密送列表
		public static final String F_BCC_LIST = "f_bcc_list";
		// 发件时间
		public static final String F_SEND_DATE = "f_send_date";
		// 收件时间
		public static final String F_INTERNAL_DATE = "f_internal_date";
		// 下载邮件状态 1，2
		public static final String F_DOWNLOAD_STATE = "f_download_state";
		// 附件数
		public static final String F_ATTACHMENT_COUNT = "f_attachment_count";
		// 已读标识
		public static final String F_READ_FLAG = "f_read_flag";
		// 收藏标识
		public static final String F_FAVORITE_FLAG = "f_favorite_flag";
		// 优先级 紧急 优先级
		public static final String F_PRIORITY = "f_priority";
		// 邮件大小 包括附件
		public static final String F_MAIL_SIZE = "f_mail_size";
		// 预览内容
		public static final String F_PERVIEW = "f_perview";
		// 邮件类型（兼容自有协议0=普通邮件 1=会议邮件 2=自摧毁邮 3=定时邮件）
		public static final String F_MAIL_TYPE = "f_mail_type";
		// 发送方式（兼容自有协议发送类型0=普通发送 1=回复 2=转发）
		public static final String F_SEND_TYPE = "f_send_type";
		// 是否重要发件人（兼容自有协议）
		public static final String F_IMPORTANT_SENDER = "f_important_sender";
		// 是否重要邮件（兼容自有协议）
		public static final String F_IMPORTANT_MAIL = "f_important_mail";
		// 是否要已读回执（兼容自有协议）
		public static final String F_RECEIPT_FLAG = "f_receipt_flag";
		// 邮件压缩后大小（兼容自有协议）
		public static final String F_COMPRESSED_SIZE = "f_compressed_size";
		// 原始邮件id
		public static final String F_ORIGINAL_MESSAGE_SID = "f_original_message_sid";
		// 删除标识
		public static final String F_DELETE_FLAG = "f_delete_flag";
	}

	/**
	 * 账户表
	 */
	public static class TbAccounts {

		public static final String TB_NAME = "tb_accounts";
		public static final String F_ID = "_id";
		public static final String F_UUID = "f_uuid";
		// email
		public static final String F_EMAIL = "f_email";
		public static final String F_PASSWORD = "f_password";
		public static final String F_ISDEFAULT = "f_isdefault";
	}

	/**
	 * 流量统计表
	 */
	public static class TbTrafficData {

		public static final String TB_NAME = "tb_traffic_data";
		public static final String F_ID = "_id";
		public static final String F_FLOW_DATE = "f_flow_date";
		public static final String F_RXBYTES_START = "f_RxBytes_start";
		public static final String F_TXBYTES_START = "f_TxBytes_start";
		public static final String F_APP_TYPE = "f_app_type";
		public static final String F_RXBYTES_DAYSUM_WIFI_ORALL = "f_RxBytes_daySum_wifi_orall";
		public static final String F_TXBYTES_DAYSUM_WIFI_ORALL = "f_TxBytes_daySum_wifi_orall";
		public static final String F_RXBYTES_DAYSUM_MOBILE = "f_RxBytes_daySum_Mobile";
		public static final String F_TXBYTES_DAYSUM_MOBILE = "f_TxBytes_daySum_Mobile";
	}

	/**
	 * 搜索历史表
	 * 
	 * @Description:
	 * @author:xuqq
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:2013-12-2
	 */
	public static class TbSearchHistory {

		public static final String TB_NAME = "tb_search_history";
		public static final String F_ID = "_id";
		public static final String F_ACCOUNT_UUID = "f_account_uuid";
		public static final String F_KEYWORD = "f_keyword";
	}

	/**
	 * 创建群组
	 */
	public static class TbCGroup {

		public static final String TB_NAME = "tb_c_group";
		public static final String F_ID = "_id";
		public static final String F_UID = "f_group_uid"; // 群组id
		// 群组名称
		public static final String F_GROUP_NAME = "f_group_name";
		// 群描述
		public static final String F_GROUP_DESC = "f_group_desc";
		// 群头像 服务器地址
		public static final String F_AVATAR = "f_group_avatar";
		// 是否私密
		public static final String F_IS_PRIV = "f_is_priv";
		// 是否成员
		public static final String F_IS_MEMBER = "f_is_member";
		// 是否管理员
		public static final String F_IS_ADMIN = "f_is_admin";
		// 成员数
		public static final String F_C_USER = "f_c_user";
		// 帖子数
		public static final String F_C_POSTS = "f_c_posts";
		// 群类型
		public static final String F_GROUP_TYPE = "f_group_type";
		// 是否有未处理的请求
		public static final String F_IS_UNTREATED = "f_is_untreated";
		// 置顶贴时间
		public static final String F_STICKED_DATE = "f_sticked_date";
		// 是否是置顶帖 0非置顶帖1是置顶帖
		public static final String F_IS_STICKED = "f_is_sticked";
		// 未处理数量
		public static final String F_UNTREATED_COUNT = "f_untreated_count";
		// 最后一条消息发送时间
		public static final String F_LAST_SENDDATE = "f_last_senddate";
		// 最后一条消息发送者
		public static final String F_LAST_SENDNICKNAME = "f_last_send_nickname";
		// 最后一条消息内容、
		public static final String F_LAST_SEND_CONTENT = "f_last_send_content";
		// 最后一条消息发送的内容类型
		public static final String F_LAST_SEND_TYPE = "f_last_send_type";
		// 最后一条消息uid
		public static final String F_LAST_SEND_UID = "f_last_send_uid";
		// 是否可见 点击list左滑删除时
		public static final String F_IS_VISIBILITY = "f_is_visibility";
		// 群消息通知栏提醒 0:开启 1：关闭
		public static final String F_IS_MESSAGE_ALERT = "f_is_message_alert";
		// 群消息声音提醒 0:开启 1：关闭
		public static final String F_IS_MESSAGE_VOICEREMINDER = "f_is_message_VoiceReminder";
		// 输入的状态，1 文字,2 语音
		public static final String F_INPUT_TYPE = "f_input_type";
		//是否第一次改名
		public static final String F_RENAME="f_re_name";
		//草稿存储  Draft
		public static final String F_DRAFT="f_draft_content";
		//草稿标记
		public static final String F_IS_DRAFT="f_is_draft";
		//发送状态标记
		public static final String F_SEND_STATE="f_send_state";
	}

	/**
	 * 群成员
	 * 
	 * @Description:
	 * @author:Zhonggaoyong
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:2014-1-9
	 */
	public static class TbCMember {

		public static final String TB_NAME = "tb_c_member";
		// public static final String F_ID = "_id";
		// 成员uid，邮件 服务器生成
		public static final String F_UID = "f_cmember_uid";
		// 名称
		public static final String F_NICK_NAME = "f_nick_name";
		// email
		public static final String F_EMAIL = "f_email";
		// 头像
		public static final String F_AVATAR = "f_avatar";
		/* 高清头像 */
		public static final String F_BIG_AVATAR = "f_big_avatar";
		/* 低分辨率头像HASH值 */
		public static final String F_AVATAR_HASH = "f_avatar_hash";
		// 是否管理员
		public static final String F_IS_ADMIN = "f_is_admin";
		
	}
	/**
	 * 群组和成员的关系
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-5-9
	 */
	public static class TbCGroup_CMember {
		public static final String TB_NAME = "tb_c_member_group";
		// public static final String F_ID = "_id";
		// 成员uid，邮件 服务器生成
		public static final String F_CMEMBER_UID = "f_cmember_uid";	
	
		public static final String F_CGROUP_UID = "f_group_uid";	
		// 是否是管理员
		public static final String F_IS_ADMIN = "f_is_admin";
		// 是否是已经邀请的
		public static final String F_IS_INVITE  ="f_is_invite_member";
	}
	/**
	 * 群消息
	 * 
	 * @Description:
	 * @author:Zhonggaoyong
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:2014-1-9
	 */
	public static class TbCMessages {

		public static final String TB_NAME = "tb_c_message";
		public static final String F_ID = "_id";
		// 唯一消息id
		public static final String F_UID = "f_uid";
		// 群组外键 服务器生成
		public static final String F_CGROUP_UID = "f_group_uid";
		// 成员外键 服务器生成
		public static final String F_CMEMBER_UID = "f_cmember_uid";
		// 发送时间
		public static final String F_SENDTIME = "f_sendtime";
		// 消息类型
		// 0、 普通文本消息 1、 IMAGE图片消息 2、附件消息 3、音频消息 4、位置消息5 系统消息
		public static final String F_MESSAGETYPE = "f_messagetype";// ddd
		public static final String F_LAT = "f_lat";
		public static final String F_LON = "f_lon";
		// 地理位置
		public static final String F_ADDRESS = "f_address";
		// 地理名称
		public static final String F_LOCATION_NAME = "f_location_name";

		// 内容
		public static final String F_CONTENT = "f_content";

		// 0 发送中 本地尚未同步 1发送失败 2服务器数据 发送成功的
		public static final String F_MESSAGE_STATE = "f_message_state";
		// 是否已被删除0 未删除 1 已被删除
		public static final String F_DELETE_FLAG = "f_delete_flag";
		//未读标记
		public static final String F_READ_FLAG = "f_read_flag";
		//服务端推送类型
		public static final String F_SERVER_MESSAGE_TYPE = "f_s_message_type";
		//邮件到消息透传来自谁
		public static final String F_MAIL_FROM_EMAIL="f_mail_from_email";
		//邮件到消息透传来自谁
		public static final String F_MAIL_FROM_NICKNAME="f_mail_from_nickname";
		//邮件到消息透传主题
		public static final String F_MAIL_SUBJECT="f_mail_subject";
		//邮件到消息透传预览
		public static final String F_MAIL_PREVIEW="f_mail_preview";
	}

	/**
	 * 附件
	 * 
	 * @Description:
	 * @author:Zhonggaoyong
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:2014-1-9
	 */
	public static class TbCAttachments {

		public static final String TB_NAME = "tb_c_attachments";
		public static final String F_ID = "_id";
		// 当前消息的附件id
		public static final String F_UID = "f_uid";
		// 消息外键
		public static final String F_CMESSAGES_UID = "f_cmessages_uid";
		// 群组外键
		public static final String F_CGROUP_UID = "f_group_uid";
		// 附件名称
		public static final String F_NAME = "f_name";
		// 大小
		public static final String F_SIZE = "f_size";
		// 下载状态
		public static final String F_DOWNLOAD_STATE = "f_download_state";
		// 类型
		public static final String F_MIME_TYPE = "f_mime_type";
		// 本地url
		public static final String F_FILE_PATH = "f_file_path";
		// 音频长度
		public static final String F_VOICE_LENGTH = "f_voice_length";
		// 已听读标识
		public static final String F_READ_FLAG = "f_read_flag";
		// 本地图片标识
		public static final String F_FORWARD_FLAG = "f_forward_flag";
		// 图片转发标识
		public static final String F_LOCALPATH_FLAG= "f_localPath_flag";
		//服务端下载路径ID
		public static final String F_FILE_ID="f_file_id";
		//附件下载进度 
		public static final String F_DOWNLOAD_PROGRESS="f_download_progress";
		//附件下载暂停标记
		public static final String F_DOWNLOAD_PAUSE_FLAG="f_download_pause_flag";
		//附件宽（图片等）
		public static final String F_WIDTH="f_width";
		//附件高（图片等）
		public static final String F_HEIGHT="f_height";
		//是否自动下载（当清除缓存后，判断是否执行imagerLoder, 默认为  0: 自动下载   1: 不自动下载）
		public static final String F_IS_IMAGE_LOAD="f_is_imageload";
	}

	/**
	 * 用户联系人表 <li>现在用户联系人表在客户端检索，那么联系人便是往来邮件的email所对应的联系人 <li>
	 * 常用联系人、重要联系人等。按照一定的规则检索即可
	 * 
	 * @Description:
	 * @author:Zhonggaoyong
	 * @see:
	 * @since:
	 * @copyright © 35.com
	 * @Date:2013-10-11
	 */
	public static class TbUserContacts {

		public static final String TB_NAME = "tb_user_contacts";
		public static final String F_ID = "_id";
		// email
		public static final String F_EMAIL = "f_email";
		// 昵称
		public static final String F_NICK_NAME = "f_nick_name";
		// 拼音
		public static final String F_SPELL_NAME = "f_spell_name";
		// 发送次数
		public static final String F_SEND_COUNT = "f_send_count";
		// 接收次数
		public static final String F_RECEIVE_COUNT = "f_receive_count";
		//联系人头像路径
		public static final String F_IMG_HEAD_NAME = "f_img_head";
		//联系人头像hash值，用作比较
		public static final String F_IMG_HEAD_HASH = "f_img_head_hash";
		/* 头像上传状态 */
		public static final String F_UPLOAD_STATE = "f_upload_state";
		// 时间
		public static final String F_DATE = "f_date";
		// 是否使用过邮洽
		public static final String F_IS_USED_MAILCHAT = "f_is_used_mailchat";
		// 删除标记 （显示/隐藏） 1显示 0 不显示
		public static final String F_IS_VISIBILITY = "f_is_visibility";
		//公司
		public static final String F_COMPANY = "f_company";
		//部门
		public static final String F_DEPARTMENT = "f_department";
		//职位
		public static final String F_POSITION = "f_position";
		//电话
		public static final String F_PHONE = "f_phone";
		//地址
		public static final String F_ADDR = "f_addr";
		//其他备注
		public static final String F_REMARKS = "f_remarks";
		//是否为自己添加的联系人
		public static final String F_IS_ADD = "f_is_add";
	}

	// ................................................................................

	/**
	 * 邮件列表id集合表，用来描述数据库中邮件列表的连续与断层
	 * 
	 * @author Administrator
	 * 
	 */
	public static class TbMessageStamp {

		public static final String TB_NAME = "tb_message_stamp";

		public static final String F_ID = "_id";
		// 用户账户
		public static final String F_EMAIL = "f_email";
		// 文件夹id
		public static final String F_FOLDER_ID = "f_folder_id";
		// 邮件id集合
		public static final String F_MESSAGE_IDS = "f_message_ids";
		// 时间节点
		public static final String F_TIME = "f_time";
	}

	
	/**
	 * 用户个人信息表
	 * @author liwent
	 * @see Documented
	 * @date 2014年8月19日
	 */
	
	public static class TbUserInfo1 {
		/* 表名 */
		public static final String TB_NAME = "tb_user_info";
		/* 数据库索引 */
		public static final String F_ID = "_id";
		/* 用户账户 */
		public static final String F_EMAIL = "f_email";
		/* 头像名称 */
		public static final String F_PHOTO_NAME = "f_photo_name";
		/* 个人昵称 */
		public static final String F_NICK_NAME = "f_nick_name";
		/* 头像上传状态 */
		public static final String F_UPLOAD_STATE = "f_upload_state";
		/* 小图hash对照码 */
		public static final String F_PHOTO_HASH = "f_photo_hash";
		// // 来往次数（不做表列名，只是为以后虚拟查询操作使用）
		public static final String F_COUNT = "f_count";
	}

	/**
	 * 单聊列表
	 * 
	 * @author shengli
	 * 
	 */
	public static class TbDchat {
		public static final String TB_NAME = "tb_d_chatlist";
		public static final String F_UID ="f_message_dchat_uid";
		//与谁通讯
		public static final String F_TO_EMAIL = "f_to_email";
		public static final String F_LAST_MESSAGE ="f_last_message";
		public static final String F_LAST_MESSAGE_TYPE ="f_last_message_type";
		public static final String F_LAST_TIME ="f_last_time";
		public static final String F_LAST_MESSAGE_EMAIL="f_last_message_email";
		// 置顶贴时间
		public static final String F_STICKED_DATE = "f_sticked_date";
		public static final String F_IS_STICKED = "f_is_sticked";
		public static final String F_IS_VISIBILITY = "f_is_visibility";
		public static final String F_IS_DCHAT_ALERT = "f_is_dchat_alert";
		public static final String F_UNREADCOUNT ="f_unread_count";
		//草稿存储  Draft
		public static final String F_DRAFT="f_draft_content";
		//草稿标记
		public static final String F_IS_DRAFT="f_is_draft";
		//发送状态标记
		public static final String F_SEND_STATE="f_send_state";
		// 输入的状态，1 文字,2 语音
		public static final String F_INPUT_TYPE = "f_input_type";
		// 单聊类型 0 普通单聊  1 oa
		public static final String F_DCHAT_TYPE = "f_dchat_type";
		// 是否有未处理
		public static final String F_IS_UNTREATED = "f_is_untreated";
	}
	/**
	 * 单聊消息
	 * 
	 * @author shengli
	 * 
	 */
	public static class TbDchatMessage {	
		public static final String TB_NAME = "tb_d_chat_message";
		
		public static final String F_UID ="f_message_uid";//本地生成，用于关联附件表
		public static final String F_DCHAT_UID ="f_message_dchat_uid";//本地生成，用于关联列表;
		/**
		 * 该消息发给谁，即谁收到的。可以理解为receiverEmail
		 */
		public static final String F_TO_EMAIL = "f_to_email";
		/**
		 * 该消息来自谁，即谁发的。可以理解为senderEmail
		 */
		public static final String F_FROM_EMAIL = "f_from_email";
		public static final String F_MESSAGE_CONTENT ="f_message_content";
		public static final String F_TIME ="f_time";
		// 0、 普通文本消息 1、 IMAGE图片消息 2、附件消息 3、音频消息 4、位置消息 5 系统消息
	    public static final String F_MESSAGE_TYPE = "f_messagetype";
		public static final String F_DELETE_FLAG="f_delete_flag";
		public static final String F_MESSAGE_STATE="f_message_state";
		//地图类型
		public static final String F_LOCATION_TYPE="f_location_type";
		//坐标点
		public static final String F_LAT = "f_lat";
		public static final String F_LON = "f_lon";
		// 地理位置
		public static final String F_ADDRESS = "f_address";
		// 地理名称
		public static final String F_LOCATION_NAME = "f_location_name";	
		//点击URL
		public static final String F_URL="f_url";
		//OA事件ID
		public static final String F_OA_ID="f_oa_id";
		//OA公告或发起人
		public static final String F_OA_FROM="f_oa_from";
		//OA标题
		public static final String F_OA_SUBJECT="f_oa_subject";
		//OA时间
		public static final String F_OA_TIME="f_oa_time";
		//未读标记
		public static final String F_READ_FLAG = "f_read_flag";
		//服务端推送类型
		public static final String F_SERVER_MESSAGE_TYPE = "f_s_message_type";
		// 邮件到消息透传来自谁
		public static final String F_MAIL_FROM_EMAIL="f_mail_from_email";
		// 邮件到消息透传来自谁
		public static final String F_MAIL_FROM_NICKNAME="f_mail_from_nickname";
		// 邮件到消息透传主题
		public static final String F_MAIL_SUBJECT = "f_mail_subject";
		// 邮件到消息透传预览
		public static final String F_MAIL_PREVIEW = "f_mail_preview";
	}
	/**
	 * 单聊消息
	 * 
	 * @author 单聊附件表
	 * 
	 */
	public static class TbDAttachments {	
		public static final String TB_NAME = "tb_d_chat_attachment";
		
	    public static final String F_UID = "f_uid";
	    
	    public static final String F_MESSAGE_UID ="f_message_uid";//本地生成，用于关联附件表
	    // 附件名称
		public static final String F_NAME = "f_name";
		// 大小
		public static final String F_SIZE = "f_size";
		// 类型
		public static final String F_MIME_TYPE = "f_mime_type";
		// 本地url
		public static final String F_FILE_PATH = "f_file_path";
		// 音频长度
		public static final String F_VOICE_LENGTH = "f_voice_length";
		// 已听读标识
		public static final String F_READ_FLAG = "f_read_flag";
		// 图片转发标识
		public static final String F_FORWARD_FLAG = "f_forward_flag";
		// 本地图片标识
		public static final String F_LOCALPATH_FLAG = "f_localPath_flag";
		//服务端下载路径ID
		public static final String F_FILE_ID="f_file_id";
		//附件下载进度 
		public static final String F_DOWNLOAD_PROGRESS="f_download_progress";
		//附件下载暂停标记
		public static final String F_DOWNLOAD_PAUSE_FLAG="f_download_pause_flag";
		// 附件宽（图片等）
		public static final String F_WIDTH = "f_width";
		// 附件高（图片等）
		public static final String F_HEIGHT = "f_height";
		//是否自动下载（当清除缓存后，判断是否执行imagerLoder, 默认为  0: 自动下载   1: 不自动下载）
		public static final String F_IS_IMAGE_LOAD="f_is_imageload";
	}
	
	/**
	 * MQTT失败命令缓存表
	 * 
	 * @author shengli
	 * 
	 */
	public static class MQTTPendingAction {	
		public static final String TB_NAME = "tb_mqtt_pending";
		public static final String ID ="id";
		//SUBSCRIBE 2;PUBLISH 3;UNSUBSCRIBE 4
		public static final String ACTION="action";
		public static final String COMMAND ="commmand";
		public static final String TOPIC="topic";
		public static final String CONTENT="content";
	}
	

	/**
	 * HTTPS失败命令缓存表
	 * 
	 * @author shengli
	 * 
	 */
	public static class HTTPSPendingAction {	
		public static final String TB_NAME = "tb_https_pending";
		public static final String ID ="id";
		public static final String COMMAND ="commmand";
		public static final String PARAMETERS ="parameters";
	}

	/**
     * 企业联系人部门表
     *
     * @author shengli
     *
     */
    public static class TbBusinessContactDepartment{
        public static final String TB_NAME = "tb_b_contact_department";
        //部门id
        public static final String F_ID = "f_id";
        //名称
        public static final String F_NAME ="f_name";
        //上级部门id
        public static final String F_PARENT_ID ="f_parent_id";
        //排序id
        public static final String F_SORT_ID = "f_sort_id";
        //是否为展开状态
        public static final String F_IS_OPEN="f_is_open";
        //该部门联系人数量
        public static final String F_USER_TOTAL_COUNT="f_user_totalCount";
        //部门拥有子部门数
        public static final String F_CHILD_DEP_COUNT = "f_child_dep_count";
    }
    /**
     * 企业联系人员工表
     *
     * @author shengli
     *
     */
    public static class TbBusinessContactUser{
        public static final String TB_NAME = "tb_b_contact_user";
        //员工Email
        public static final String F_EMAIL = "f_email";
        //员工姓名
        public static final String F_NAME = "f_name";
        //昵称
        public static final String F_NICK_NAME = "f_nick_name";
        //拼音
        public static final String F_SPELL_NAME = "f_spell_name";
        //员工头像HASH
        public static final String F_IMG_HEAD_HASH = "f_img_head_hash";
        //员工生日
        public static final String F_BIRTHDAY = "f_birthday";
        //员工电话
        public static final String F_PHONE = "f_phone";
        //发送次数
        public static final String F_SEND_COUNT = "f_send_count";
        //接收次数
        public static final String F_RECEIVE_COUNT = "f_receive_count";
        //头像上传状态
        public static final String F_UPLOAD_STATE = "f_upload_state";
        //时间
        public static final String F_DATE = "f_date";
        //是否使用过邮洽
        public static final String F_IS_USED_MAILCHAT = "f_is_used_mailchat";
        //备注
        public static final String F_REMARK ="f_remark";
        //地址
        public static final String F_ADDR ="f_address";
        //公司
        public static final String F_COMPANY = "f_company";
    }
    /**
     * 企业联系人关系表
     *
     * @author shengli
     *
     */
    public static class TbBusinessContactDepartment_User{
        public static final String TB_NAME = "tb_b_contact_department_user";
        //部门id
        public static final String F_DEP_ID = "f_dep_id";
        //员工Email
        public static final String F_USER_EMAIL = "f_user_email";
        //员工是否为领导
        public static final String F_IS_LEADER = "f_is_leader";
        //员工职位
        public static final String F_POSITION = "f_position";
    }
    /**
     * 联系人本地修改保存及与服务端同步
     *
     * @author zhangjx
     *
     */
    public static class TbContactRemark{
        public static final String TB_NAME = "tb_user_contact_remark";
        //Email
        public static final String F_EMAIL = "f_r_email";
        //姓名
        public static final String F_NAME = "f_r_name";
        //昵称
        public static final String F_NICK_NAME = "f_r_nick_name";
		//公司
		public static final String F_COMPANY = "f_r_company";
		//部门
		public static final String F_DEPARTMENT = "f_r_department";
		//职位
		public static final String F_POSITION = "f_r_position";
		//电话
		public static final String F_PHONE = "f_r_phone";
		//地址
		public static final String F_ADDR = "f_r_addr";
	     //备注
        public static final String F_REMARK ="f_r_remark";
        //头像HASH
        public static final String F_IMG_HEAD_HASH = "f_r_img_head_hash";
        //生日
        public static final String F_BIRTHDAY = "f_r_birthday";
        //时间
        public static final String F_DATE = "f_r_date";
    }
}
