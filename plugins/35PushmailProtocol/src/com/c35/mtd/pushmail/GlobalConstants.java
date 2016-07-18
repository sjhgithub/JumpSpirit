package com.c35.mtd.pushmail;

import java.io.File;

import android.os.Environment;

/**
 * 
 * @Description:存放全局应用的常量类。
 * @author:
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class GlobalConstants {
	
//	public static String currentUid;		// 当前的uid, 刷新正文时临时使用
	
	public static final float RATE = 0.65f;// 登录时屏幕缩小比率，用于内嵌图片大小适配

	public static final String KEY_IS_SHOWN_MESSAGELIST_BOTTOMPANNEL = "navi_messagelist_bottompannel_is_shown"; // 底部面板引导是否显示过
	public static final String KEY_IS_SHOWN_MESSAGELIST_SLIDEDELETE = "navi_messagelist_slidedelete_is_shown"; // 滑动删除引导是否显示过
	public static final String KEY_IS_SHOWN_MESSAGELIST_PUSHBOX = "navi_messagelist_pushbox_is_shown"; // 消息盒子亮起时的引导是否显示过
	public static final String KEY_IS_SHOWN_MESSAGELIST_BOX = "navi_messagelist_box_is_shown"; // 消息盒子引导是否显示过
	public static final String KEY_IS_SHOWN_MESSAGEVIEW_ONSCROLL = "navi_messageview_onscroll_is_shown"; // 详情页引导是否显示过
	public static final String KEY_IS_SHOWN_USERGUIDE = "user_guide_is_shown"; // 新手引导是否显示过
	public static final String KEY_CURRENT_VERSION_CODE_FOR_USERGUIDE = "current_version_code_for_user_guide"; // 版本，带日期
	public static final String PUSHMAIL_PREF_FILE = "push_mail_pref_file"; // 配置文件
	public static final String KEY_IS_SHOWN_PIC = "pic_is_shown"; // 图片显示
	public static final String NEW_VERSION = "new_version";// 是否有新版本
	public static final String CURRENT_VERSION_CODE = "current_version_code"; // 当前版本号
	public static final String THE_TIME_ENTER = "the_time_enter";// 第几次进入应用，最多记到第二次

//	public static final File FILE_SAVE_ADDED_ACCOUNT_ADDR = new File("/data/data/com.c35.mtd.pushmail/addedaccount.cfg");// 存放已经绑定成功过的账号
	// Modified by LL
	public static final File FILE_SAVE_ADDED_ACCOUNT_ADDR = new File(EmailApplication.getInstance().getFilesDir(), "addedaccount.cfg");

	public static final String KEY_IS_SHOWN_SHORTCUT = "shortcut_is_shown"; // 是否创建过快捷方式
	// ///////////////////////////////同步接口新增
	public static final int READ_FLAG = 10; // 是否已读（ 0=未读 1=已读）
	public static final int IMPORTANT_FLAG = 11; // 是否收藏（0=非收藏1=收藏）
	public static final int DELETE_FALG = 12; // 删除操作（0=移到已删除（如果是已删除文件夹，就彻底删除）；1=彻底删除）
	public static final int MOVE_TO = 13; // 移动操作（目标文件夹id）

	public static final String NO_READ = "0";// 未读
	public static final String YES_READ = "1";// 已读
	public static final String NO_FAV = "0";// 取消收藏
	public static final String YES_FAV = "1";// 收藏
	public static final String NOT_DEL_COMPLETELY = "0";// 删除
	public static final String DEL_COMPLETELY = "1";// 彻底删除

	public static final int COMMIT_STATUS_DEFAULT = 0; // 未提交
	public static final int COMMIT_STATUS_COMMITING = 1; // 正在提交
	public static final int COMMIT_STATUS_FAILED = -1; // 提交失败

	// 同步操作错误码
	public static int ERROR_SYNC_OPERATE_FOLDER_NOTEXIST = 501;// 文件夹不存在
	public static int ERROR_SYNC_OPERATE_MAIL_NOTEXIST = 502;// 邮件不存在

	public static int ERROR_SYNC_OPERATE_STATE_READ = 510;// 已读未读失败
	public static int ERROR_SYNC_OPERATE_STATE_IMPORTANTFLAG = 511;// 收藏失败
	public static int ERROR_SYNC_OPERATE_DELETE = 512;// 删除失败
	public static int ERROR_SYNC_OPERATE_MOVETO = 513;// 移动失败

	// LoginActivity,MainActivity,MessageList中常用的常量进行提取。
	public static final String SUCCESS = "1";// 从web服务器获取的邮箱信息 校验成功！
	public static final String FAILED = "0";// 校验失败
	public static final String IS_35_EMAIL = "1"; // 是35即时邮
	public static final String IS_PUSH_EMAIL = "1"; // 是push版
	public static final String PUSH_OPEN = "1";
	public static final String PUSH_CLOSE = "0";
	public static final String NOT_35_EMAIL = "0"; // 从web服务器获取的邮箱信息1、不是35即时邮
	public static final int VALIDATE_SUCCESS = 0x108; // 校验通过
	public static final int ERROR_EMAIL = 0; // 找不到账号
	public static final int ACCOUNT_UNUSERED = 2; // 账号被禁用
	public static final int ACCOUNT_CLOSED = 3; // 账号被关闭
	public static final int VALIDATE_NOT_35_MAIL = 0x110;// 不是三五即时邮账户
	public static final int SHOW_EXTRA_ACCOUNT_DIALOG = 0x135;// 外部帐号登录
	public static final int VALIDATE_ERROR_PASSWORD = 0x111;// 密码错误
	public static final int SERVER_ERROR = 0x112; // 服务器出错
	public static final int VALIDATE_IS_PUSH = 0x113; // 校验是否为push账户
	public static final int VALIDATE_NOT_PUSH = 0x118; // 校验不是为push账户
	public static final int LOGINSERVER = 0x114; // 登陆邮箱标记
	public static final int TRY_LOGIN_OA = 0x117; // 登陆邮箱标记
	public static final int OPEN_LOCAL_PUSH = 0x115; // 本地push开关开启
	public static final int CLOSE_LOCAL_PUSH = 0x116; // 本地push开关关闭
	public static final int CHECK_INTERNET_ERROR = 0x120;// 网络联接错误
	public static final int CHECK_VERSION_ON = 0x121;// 版本错误
	public static final int LOGIN_CHECKPUSH_ERROR = 0x122;//LOGIN_CHECKPUSH_ERROR
	public static final int LOGIN_SAVEACCOUNT_REGISTEPUSH_ERROR = 0x123;//LOGIN_SAVEACCOUNT_REGISTEPUSH_ERROR
	public static final int LOGIN_LINK_TIMEOUT= 0x125;
	public static final int LOGIN_OTHERS_ERROR = 0x124;//LOGIN_SAVEACCOUNT_REGISTEPUSH_ERROR
//	public static final int USER_REPEAT = 0x546;// 帐号重复
	public static final String HIPPO_SERVICE_IDENTIFIER = "HIPPO_ON_SERVICE_001";

	public static final int DEL_ACCOUNT_ALL = 0x441;
	public static final int DEL_ACCOUNT_SHOW = 0x444;
	public static final int DEL_DEFAULT_ACCOUNT_SHOW_SET = 0x445;
	public static final int DEL_NOT_DEFAULT_ACCOUNT_SHOW_SET = 0x446;
	public static final int SELF_FOLDER_TYPE = 1;// 自定义文件夹类型
	public static final int MAIlBOX_SELF_NAME_ID = 9999;// 自定义文件夹在列表中的id
	public static final int MAIlBOX_DEFAULT_NAME_ID = 8888; // 缺省文件夹在列表中的id
	public static final int MAIlBOX_FILLING_NUMBER = 0; // 用于文件夹列表填充数据
	public static final String MAILBOX_SELF_LIST_NAME = "selffolder";// 自定义文件夹列表中的name
	public static final String MAILBOX_DEFAULT_LIST_NAME = "defaultfolder";// 缺省文件夹列表中的name
	public static final int FOLDER_LABEL_TYPE = -1; // 邮件夹归类类型的类型
	// 原DirectoryPicker常用常量
	private static final String PACKAGE_NAME = "com.c35.mtd.pushmail.activity";
	private static final String CLASS = PACKAGE_NAME + ".DirectoryPicker";
	public static final String DEF_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
	public static final String FILE_NAME = CLASS + ".filename";
	public static final String BACK_DATA = CLASS + ".backdata";
	public static final String PREF_INFO = CLASS + ".pref_info";
	public static final String TAG_NAME = CLASS + ".dirname";
	public static final String FILE_NAME_NEW = "filename";
	public static final String FILE_PATH = "filepath";

	// MessageView用常量
	public static final String PREFIX = "com.c35.mtd.pushmail";
	public static final String EXTRA_FOLDER = PREFIX + ".MessageView_folder";
	public static final String EXTRA_MESSAGE = PREFIX + ".MessageView_message";
	public static final String EXTRA_FOLDER_UIDS = PREFIX + ".MessageView_folderUids";
	public static final String EXTRA_NEXT = PREFIX + ".MessageView_next";

	public static final String EXTRA_FOLDER_ID = "extraSelfFolderId";
	public static final String SEARCH_FOLDER = "search";

	public static final String OLD_MAIL_DIRECTORY = DEF_DIR + "/com.c35.mtd.pushmail";// 老的mail 目录
	public static final String MAIL_DIRECTORY = "/35.com/35mail";// mail 目录
	public static final String APPCATION_SCARD_DIRECTORY = DEF_DIR + MAIL_DIRECTORY; // SDCard 上应用存储目录
	public static final String FILE_PATH_FOR_OPEN = APPCATION_SCARD_DIRECTORY + "/files2open";// 文件打开路径
	public static final String RECORD_FOLDER = "/c35MailRecorder";// 录音文件存放路径
	public static final String RECORD_PATH = APPCATION_SCARD_DIRECTORY + RECORD_FOLDER;// 录音文件存放路径

	// 数据库文件定义
	public static final String DATABASE_DIRECTORY_NAME = "database";
	public static final String DATABASE_DIRECTORY = "/" + DATABASE_DIRECTORY_NAME + "/";
	public static final String DATABASE_NAME = "35PushMail.db";
	public static final String DATABASE_NAME_ACCOUNTS = "account_info.db";

	// 附件临时存储路径
	public static final String ATTACHMENT_PATH = APPCATION_SCARD_DIRECTORY + "/temp/";
	// 彩蛋路径
	public static final String EGG_SHELL_NAME = "SubscribeIppusInformation.html";
	public static final String EGG_SHELL_PATH = APPCATION_SCARD_DIRECTORY + "/" + EGG_SHELL_NAME;

	// menu菜单
	public static final int MULTY = 0; // 多选
	public static final int MULTY_CANCAL = 1; // 取消多选
	public static final int SEARCH = 2; // 搜索
	public static final int EXIT = 3; // 退出
	public static final int SET = 4; // 设置
	public static final int APP_LIST = 5; // 35云办公
	public static final int VIDEO = 6; // 视频演示
	public static final int RESENDALL = 7; // 全部重发
	public static final int UNFOLD_CCBCC = 8; // 添加密送
	public static final int FOLD_CCBCC = 9;// 删除密送
	public static final int READ_MARK = 10; // 标记已读
	public static final int UNREAD_MARK = 11; // 标记未读
	public static final int FAV_MARK = 12; // 收藏
	public static final int UNFAV_MARK = 13; // 取消收藏
	public static final int SAVE_AS_DRAF = 14;// 另存为草稿
	public static final int SEND = 15;// 发送
	public static final int DELETE = 16; // 删除
	public static final int MOVE = 19; // 移动
	public static final int RESTORE = 20; // 还原
	public static final int RECALL = 21; // 召回
	public static final long AUTODOWN_LOAD_ATT_SIZE = 2 * 1024 * 1024;// 完整模式下自动下载附件的大小
	// 记录更新提示时间
	public static final String SHOW_UPDATE_TIME_CANCEL = "showupdatetimecancel";
	// 几天后提醒
	public static final int SHOW_UPDATE_DAY_SIZE = 7;
	// 设置login页面语言
	public static final String LANGUAGE_FOR_LONGIN = "auto";
	// 选择联系人的数据传输key
	public static final String DATA_TRANSFER_FETCH_CONTACTS = "DATA_TRANSFER_FETCH_CONTACTS";
	// 压缩选项常量
	public static final int NO_ZIP_ONLY_JSON = 0;// 不压缩
	public static final int GZIP_BASE64 = 1;// 压缩+base64
	public static final int GZIP_ENCRYPT_ENTER = 2;// 加密压缩（换行+加密+压缩）
	public static final int GZIP_ENTER = 3;// 压缩（换行+压缩）
	
	public static final String DATE_FORMAT_YYYYMMDD = "yyyy-MM-dd";
	
	// 服务器背景图
	public static final String LAST_GET_IMAGE_TIME = "lastgetimagetime";// 获取背景图片时间
	public static final String BACKGROUND_IMAGE_VERSION = "backgroundimageversion";// 图片版本号
	public static final long INTERVAL_TIME = 12 * 60 * 60 * 1000;// 间隔时间6h
//	public static final String BACKGROUND_IMAGE_URL = "http://images.mobile.35.com/whynot/pushmailimg/query.do";// 获取图片服务器地址_test
	public static final String BACKGROUND_IMAGE_URL = "http://y.35.com/pushmailimg/query.do";// 获取图片服务器地址
	public static final String BACKGROUND_SPLASH_IMAGE = "/splash_image.png";// 存储名称
	public static final String BACKGROUND_START_TIME = "bstarttime";
	public static final String BACKGROUND_END_TIME = "bendtime";

}
