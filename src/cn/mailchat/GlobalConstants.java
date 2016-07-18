package cn.mailchat;

import java.io.File;
import android.os.Environment;

import cn.mailchat.Account;
import cn.mailchat.MailChat;

/**
 * 存放全局应用的常量类
 * 
 * @Description:
 * @author:shengli
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2014-12-14
 */
public class GlobalConstants {
	// 选择联系人的数据传输key
	public static final String DATA_TRANSFER_FETCH_CONTACTS = "DATA_TRANSFER_FETCH_CONTACTS";
	public static final int MAX_ATTACHMENT_UPLOAD_SIZE = (20 * 1024 * 1024);// 详情页附件总大小的限制
	// sd卡目录
	public static final String DEF_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
	public static final String MAILCHAT_DIRECTORY = "mailchat/";	// SDCard 上应用存储目录
	public static final String APPCATION_SCARD_DIRECTORY = DEF_DIR + MAILCHAT_DIRECTORY;
	//邮件,聊天文件夹命名
	public static final String MAIL_DIRECTORY = "mail/";
	public static final String CHAT_DIRECTORY = "chat/";
	//用户头像文件夹命名
	public static final String USER_DIRECTORY = "users/";
	//图片附件文件夹命名
	public static final String IMAGE_DIRECTORY="image/";// 大图片缓存
	public static final String THUMBNAIL_IMAGE_DIRECTORY = "thumbnailImage/";// 小图片缓存
	public static final String VOICE_DIRECTORY = "voice/";// 录音缓存
	public static final String ATTACHMENT_DIRECTORY = "att/";// 附件目录
	public static final String VIEWTEMP_DIRECTORY = "viewTemp/";// 打开附件时的缓存目录
	public static final String LOCAL_THUMBNAIL_IMAGE_DIRECTORY = "localthumbnailImage/";// 上传文件时,压缩图片路径
	// mail xml目录
	public static final String MAIL_SHARE_PREFERENCE_DIRECTORY = Environment.getDataDirectory() + "/data/" + MailChat.getInstance().getPackageName() + "/shared_prefs/";
	// 应用的语言设置
	public static final String APP_LANGUAGE_AUTO = "auto";
	public static final String APP_LANGUAGE_CN = "zh_CN";
	public static final String APP_LANGUAGE_TW = "zh_TW";
	public static final String APP_LANGUAGE_US = "en_US";
	public static final String DEF_RING_URI = "content://settings/system/notification_sound";// 默认通知铃声
	//public static final String OA_BASE_URL = "http://oa.35.cn/outerPostAction.do?actionType=9&toPage=webapp";
	public static final String OA_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDCH6nli51I48axnSczbd8fWq06EiHeSDznBaygAHS8twslRynrWxFMtPwxVk3Jp3oHwzi1/dDO5JKkeRB7MNj2DHsTcSZ2lC95EnodJqmrcNRUfd3UcMAo4c7AeVj3ZYNry05jbydZS7DIDrJJTZmgz0fxXH99A1SnZjMSuiyveQIDAQAB";
	public static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0KCw6uzLYyz0weBX/ialAodGL14QGH/vIXpJazlaNJM/vbXBL2M7NTyjWw1Xapbd8h0V8JNHbtlDah1296T8Kly7v2U8k9+LnozwH3OMCDCdcQuDLGbXj0edwjHD0E5b9lZCYrYoTpLkEXzUIJlOFc8dYI0J/u/KNC32326JnQW2Xzo4C8wPeEc61V549WuMvmdPPQO8mD3/7BFHMW9CbZ3YOCc1jdWRF0IHU1Ru5c+xe4ec3y7hpGwuIZbUDUpdYy3F9EomNfsl+A26pkAU2Y9FgLO0wWkWyQ43/XD9T0lTDqrxj3T1VlXRRIlKiTpyhb6ZEzdV+Eg6YHKwDWpA2QIDAQAB";

	public static final String HOST_IMG = "https://api.mailchat.cn:80/avatar/";
	public static final String USER_SMALL_HEAD_END = "_s";
	public static int DEFAULT_POLL_INTERVAL = 15;
	public static int DEFAULT_PUSH_CAPABLE_POLL_INTERVAL = 60;
	//系统相册路径
	public static final String PHOTO_DEF_DIR =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/";
	//匿名账户后缀
	public static final String HIDE_ACCOUNT_SUFFIX="_$@";
	//hlep_email
	public static final String HELP_ACCOUNT_EMAIL="fb@mailchat.cn";
	public static final String IOS_HELP_ACCOUNT_EMAIL="help@mailchat.cn";
	/**
	 * 中心用户变动通知
	 * */
	public static final String USER_CHANGE = "USER_CHANGE";
	public static final String CONTENT_URI = "content://com.c35.ptc";
	/**
	 * 单聊特殊聊天条目唯一标示定义相关
	 * */
	//单聊OA定义
	public static final String DCHAT_OA = "OA";
	//单聊邀请同事协作办公定义
	public static final String DCHAT_JUMP_INVITE_COLLEAGUES = "cn.mailchat.invite.colleagues";
	/**
	 * oa相关URL
	 * 规则：OA_BASE_URL_START+域名+OA_BASE_URL_END +p=加密信息；
	 * */
	public static final String OA_BASE_URL_START="http://";
	public static final String OA_BASE_MAIN_URL_END="/outerPostAction.do?actionType=4000&msgType=";//主界面
	public static final String OA_BASE_NEW_TRANS_URL_END="/outerPostAction.do?actionType=4000&msgType=NEW_TRANS";//待办事务列表
	public static final String OA_BASE_MYPASS_URL_END="/outerPostAction.do?actionType=4000&msgType=MYPASS";//我办过的事务列表
	public static final String OA_BASE_MYCREATE_URL_END="/outerPostAction.do?actionType=4000&msgType=MYCREATE";//我发起的事务列表
	public static final String OA_BASE_ANNOUNCE_URL_END="/outerPostAction.do?actionType=4000&msgType=ANNOUNCE";//公告列表
	public static final String OA_BASE_NEW_URL_END="/outerPostAction.do?actionType=4000&msgType=NEW";//发起事务
	public static final String OA_SING_URL_END="/outerPostAction.do?actionType=4000&msgType=WQREG";//oa签到
	//日志消息uid后缀
	public static final String LOG_MESSAGE_UID_SUFFIX="_log";
	//调查问卷URL
	public static final String SURVEY_URL="http://www.wenjuan.com/s/EnQzI3/";
	//Intent传递常亮定义
	public static final String ACCOUNTUUID = "accountUuid";

	public static final String CANCELED_BY_USER = "CANCELED BY USER";

	/**
	 * 
	 * 闹钟requestCode定义，一定不要重复。
	 * */
    //下面是定义【Service】具体的闹钟requestCode,使用闹钟是注意不要重复使用这些值。
	public static final int MQTT_CONNECT_ALARM_REQUESTCODE=0;
	public static final int FAIL_PENDDING_ALARM_REQUESTCODE=1;
	public static final int NULL_MQTT_SERVICE_ALARM_REQUESTCODE=2;
	public static final int PUSH_CHECK_ALARM_REQUESTCODE=3;
	public static final int FAIL_PENDDING_ALARM_REQUESTCODE_ACCOUNT=4;
	//下面是定义【BroadcastReceiver】具体的闹钟requestCode,使用闹钟是注意不要重复使用这些值。
	public static final int MQTT_PING_REQUESTCODE=0;//MQTT库AlarmPingSender中定义为0.
	public static final int MAIL_BOOT_REQUESTCODE=1;//邮件后台轮询.
	/**
	 *
	 * HTTP服务器url
	 * */
	public static final String BASE_URL ="https://api.mailchat.cn:80/";//外网
	public static final String BASE_URL_TEST ="https://bbs.mailchat.cn/";//测试服务器
	public static final String BASE_URL_LAN_TEST ="https://test1.mailchat.cn/";//内网测试服务器
	/**
	 *
	 * MQTT服务器url
	 * */
	public static final String MQTT_HOST ="im.mailchat.cn";//MQTT host
	public static final int MQTT_PORT =80;//MQTT port
	public static final String MQTT_HOST_TEST ="bbs.mailchat.cn";//MQTT测试服务器 host
	public static final int MQTT_PORT_TEST =8088;//MQTT测试服务器 port
	public static final String MQTT_HOST_LAN_TEST ="test1.mailchat.cn";//MQTT内网测试服务器 host
	public static final int MQTT_PORT_LAN_TEST =8080;//MQTT内网测试服务器 port

	/**
	 *
	 * 引导页版本号
	 * 如果有新引导页，版本号+1
	 * */
	public static final int guideVersionCode = 2;
	public static final String weixin_share_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&grant_type=authorization_code&code=%s";
	public static final String SHARE_URL ="http://a.app.qq.com/o/simple.jsp?pkgname=cn.mailchat&g_f=991653";//分享url
    public static final String QQ = "qq";
    public static final String WEIBO = "weibo";
    public static final String WECHAT = "wechat";

    public static final String WEICHAT_APPID = "wx5ac6e38b16430115";
    public static final String WEICHAT_SECRET = "a031ddeeb476c42089f45dda8579de28";

    public static final String QQ_APPID = "1104867012";
    public static final String QQ_APPKEY = "yPabkfynEMLcHa7l";
    public static final String OA_NO_OPEN_URL = "http://vip01.oa.35.com/OAFlyer/flyer.html";
    public static final String OA_CHECK_BIND_URL = "http://vip01.oa.35.com/openportal/queryemailuser.35?email=";
    public static final String OA_LOGIN_IN = "/outerPostAction.do?actionType=4003&p=";
}

