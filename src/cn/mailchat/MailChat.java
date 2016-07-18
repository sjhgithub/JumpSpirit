package cn.mailchat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import android.app.ActivityManager;
import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.StrictMode;
import android.os.Vibrator;
import android.support.v4.util.LruCache;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;
import cn.mailchat.Account.SortType;
import cn.mailchat.activity.MailComposeActivity;
import cn.mailchat.activity.MailSearchActivity.SearchRange;
import cn.mailchat.activity.UpgradeDatabases;
import cn.mailchat.activity.setup.WelcomeActivity;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.controller.MessagingListener;
import cn.mailchat.mail.Address;
import cn.mailchat.mail.Message;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.mail.internet.BinaryTempFileBody;
import cn.mailchat.mail.store.LocalStore;
import cn.mailchat.preferences.Storage;
import cn.mailchat.provider.UnreadWidgetProvider;
import cn.mailchat.search.SearchResult;
import cn.mailchat.security.LocalKeyStore;
import cn.mailchat.service.BootReceiver;
import cn.mailchat.service.LocationService;
import cn.mailchat.service.MailService;
import cn.mailchat.service.ShutdownReceiver;
import cn.mailchat.service.StorageGoneReceiver;
import cn.mailchat.utils.AttachmentUtil;
import cn.mailchat.utils.AuthImageDownloader;
import cn.mailchat.utils.EncryptUtil;
import cn.mailchat.view.AttachmentView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.umeng.analytics.MobclickAgent;

public class MailChat extends com.c35.mtd.pushmail.EmailApplication {
    /**
     * Components that are interested in knowing when the MailChat instance is
     * available and ready (Android invokes Application.onCreate() after other
     * components') should implement this interface and register using
     * {@link MailChat#registerApplicationAware(ApplicationAware)}.
     */
    public static interface ApplicationAware {
        /**
         * Called when the Application instance is available and ready.
         *
         * @param application
         *            The application instance. Never <code>null</code>.
         * @throws Exception
         */
        void initializeComponent(Application application);
    }

    public static Application app = null;
    public static MailChat application;
    public static File tempDirectory;
    public static final String LOG_TAG = "MailChat";
    public static final String BUG_EMAIL = "fb@mailchat.cn";
	public static Vibrator mVibrator;
	public static LocationService locationService;
    /**
     * Name of the {@link SharedPreferences} file used to store the last known version of the
     * accounts' databases.
     *
     * <p>
     * See {@link UpgradeDatabases} for a detailed explanation of the database upgrade process.
     * </p>
     */
    private static final String DATABASE_VERSION_CACHE = "database_version_cache";

    /**
     * Key used to store the last known database version of the accounts' databases.
     *
     * @see #DATABASE_VERSION_CACHE
     */
    private static final String KEY_LAST_ACCOUNT_DATABASE_VERSION = "last_account_database_version";

    /**
     * Components that are interested in knowing when the MailChat instance is
     * available and ready.
     *
     * @see ApplicationAware
     */
    private static List<ApplicationAware> observers = new ArrayList<ApplicationAware>();

    /**
     * This will be {@code true} once the initialization is complete and {@link #notifyObservers()}
     * was called.
     * Afterwards calls to {@link #registerApplicationAware(cn.mailchat.MailChat.ApplicationAware)} will
     * immediately call {@link cn.mailchat.MailChat.ApplicationAware#initializeComponent(MailChat)} for the
     * supplied argument.
     */
    private static boolean sInitialized = false;

    public enum BACKGROUND_OPS {
        ALWAYS, NEVER, WHEN_CHECKED_AUTO_SYNC
    }

    private static String language = "";
    private static Theme theme = Theme.LIGHT;
    private static Theme messageViewTheme = Theme.USE_GLOBAL;
    private static Theme composerTheme = Theme.USE_GLOBAL;
    private static boolean useFixedMessageTheme = true;

    private static final FontSizes fontSizes = new FontSizes();

    private static BACKGROUND_OPS backgroundOps = BACKGROUND_OPS.WHEN_CHECKED_AUTO_SYNC;
    /**
     * Some log messages can be sent to a file, so that the logs
     * can be read using unprivileged access (eg. Terminal Emulator)
     * on the phone, without adb.  Set to null to disable
     */
    public static final String logFile = null;
    //public static final String logFile = Environment.getExternalStorageDirectory() + "/mailChat/debug.log";

    /**
     * If this is enabled, various development settings will be enabled
     * It should NEVER be on for Market builds
     * Right now, it just governs strictmode
     **/
    public static boolean DEVELOPER_MODE = false;


    /**
     * If this is enabled there will be additional logging information sent to
     * Log.d, including protocol dumps.
     * Controlled by Preferences at run-time
     */
    public static boolean DEBUG = false;

    /**
     * Should K-9 log the conversation it has over the wire with
     * SMTP servers?
     */

    public static boolean DEBUG_PROTOCOL_SMTP = true;

    /**
     * Should K-9 log the conversation it has over the wire with
     * IMAP servers?
     */

    public static boolean DEBUG_PROTOCOL_IMAP = true;


    /**
     * Should K-9 log the conversation it has over the wire with
     * POP3 servers?
     */

    public static boolean DEBUG_PROTOCOL_POP3 = true;

    /**
     * Should K-9 log the conversation it has over the wire with
     * WebDAV servers?
     */

    public static boolean DEBUG_PROTOCOL_WEBDAV = true;



    /**
     * If this is enabled than logging that normally hides sensitive information
     * like passwords will show that information.
     */
    public static boolean DEBUG_SENSITIVE = false;

    /**
     * Can create messages containing stack traces that can be forwarded
     * to the development team.
     *
     * Feature is enabled when DEBUG == true
     */
    public static String ERROR_FOLDER_NAME = "MailChat-errors";
    //public static String ERROR_FOLDER_NAME = "错误日志";

    /**
     * A reference to the {@link SharedPreferences} used for caching the last known database
     * version.
     *
     * @see #checkCachedDatabaseVersion()
     * @see #setDatabasesUpToDate(boolean)
     */
    private static SharedPreferences sDatabaseVersionCache;

    /**
     * {@code true} if this is a debuggable build.
     */
    private static boolean sIsDebuggable;

    private static boolean mAnimations = true;

    private static boolean mConfirmDelete = false;
    private static boolean mConfirmDeleteStarred = false;
    private static boolean mConfirmSpam = false;
    private static boolean mConfirmDeleteFromNotification = true;

    private static NotificationHideSubject sNotificationHideSubject = NotificationHideSubject.NEVER;

    /**
     * Controls when to hide the subject in the notification area.
     */
    public enum NotificationHideSubject {
        ALWAYS,
        WHEN_LOCKED,
        NEVER
    }

    private static NotificationQuickDelete sNotificationQuickDelete = NotificationQuickDelete.NEVER;

    /**
     * Controls behaviour of delete button in notifications.
     */
    public enum NotificationQuickDelete {
        ALWAYS,
        FOR_SINGLE_MSG,
        NEVER
    }

    /**
     * Controls when to use the message list split view.
     */
    public enum SplitViewMode {
        ALWAYS,
        NEVER,
        WHEN_IN_LANDSCAPE
    }

    private static boolean mMessageListCheckboxes = true;
    private static boolean mMessageListStars = true;
    private static int mMessageListPreviewLines = 2;

    private static boolean mShowCorrespondentNames = true;
    private static boolean mMessageListSenderAboveSubject = false;
    private static boolean mShowContactName = false;
    private static boolean mChangeContactNameColor = false;
    private static int mContactNameColor = 0xff00008f;
    private static boolean sShowContactPicture = true;
    private static boolean mMessageViewFixedWidthFont = false;
    private static boolean mMessageViewReturnToList = false;
    private static boolean mMessageViewShowNext = false;

    private static boolean mGesturesEnabled = true;
    private static boolean mUseVolumeKeysForNavigation = false;
    private static boolean mUseVolumeKeysForListNavigation = false;
    private static boolean mStartIntegratedInbox = false;
    private static boolean mMeasureAccounts = true;
    private static boolean mCountSearchMessages = true;
    private static boolean mHideSpecialAccounts = false;
    private static boolean mAutofitWidth;
    private static boolean mQuietTimeEnabled = false;
    private static String mQuietTimeStarts = null;
    private static String mQuietTimeEnds = null;
    private static String mAttachmentDefaultPath = "";
    private static boolean mWrapFolderNames = false;
    private static boolean mHideUserAgent = false;
    private static boolean mHideTimeZone = false;
    
    private static boolean topNotifyOn = true;//邮件顶部状态栏通知 
    private static boolean topMsgNotify  = true;//聊天消息顶部状态栏通知 
    private static boolean notifyRingtone = true;// 通知铃声 
    private static int selectNotifyRingtone =0;//选择通知铃声位置。
    private static String selectNotifyRingtoneName =null;//选择通知铃声位置的名称。
	private static boolean notifyVibrateOn = true;// 通知时振动
	private static long firstTime;// 通知的安静时段结束时间
	private static boolean isGesture = false;// 手势密码
	private static boolean isGestureUnclock =false;
	private static int guideVersionCode;//引导界面版本号
	private static boolean isShowAccountSettingPopo =true;//是否显示账号设置引导popo
	private static boolean mMessageListAttachmentShortcuts = true; //列表中显示邮件的附件
	private static boolean m35CloudServices = false;
	private static String splashUrl="";//闪屏URL
    private  static String phoneAlreadyInvationed;//设备被邀请过了
	private static boolean notifySyncError = false;
	//是否参加过活动
    private static boolean isJoinCampaign;
	//第一个活动魔窗位的活动的key
    private static String firstWMActivtyKey;
    //是否显示popup提示
    private static boolean isShowPopup =false;

	private static SortType mSortType;

    private static HashMap<SortType, Boolean> mSortAscending = new HashMap<SortType, Boolean>();

    private static boolean sUseBackgroundAsUnreadIndicator = true;
    private static boolean sThreadedViewEnabled = true;
    private static SplitViewMode sSplitViewMode = SplitViewMode.NEVER;
    private static boolean sColorizeMissingContactPictures = true;

    private static boolean sMessageViewArchiveActionVisible = false;
    private static boolean sMessageViewDeleteActionVisible = true;
    private static boolean sMessageViewMoveActionVisible = false;
    private static boolean sMessageViewCopyActionVisible = false;
    private static boolean sMessageViewSpamActionVisible = false;
    private static boolean isNewClient;//是否需要发送客户端登记至服务端
    private static boolean isHideSettingBadageView;//是否隐藏首页设置按钮红点
    private static boolean isHideOABadageView;//是否隐藏首页工作台按钮红点
    /**
     * @see #areDatabasesUpToDate()
     */
    private static boolean sDatabasesUpToDate = false;
    private static int bottomTabPosition =0;//标记主页面底部栏位置。

    /**
     * The MIME type(s) of attachments we're willing to view.
     */
    public static final String[] ACCEPTABLE_ATTACHMENT_VIEW_TYPES = new String[] {
        "*/*",
    };

    /**
     * The MIME type(s) of attachments we're not willing to view.
     */
    public static final String[] UNACCEPTABLE_ATTACHMENT_VIEW_TYPES = new String[] {
    };

    /**
     * The MIME type(s) of attachments we're willing to download to SD.
     */
    public static final String[] ACCEPTABLE_ATTACHMENT_DOWNLOAD_TYPES = new String[] {
        "*/*",
    };

    /**
     * The MIME type(s) of attachments we're not willing to download to SD.
     */
    public static final String[] UNACCEPTABLE_ATTACHMENT_DOWNLOAD_TYPES = new String[] {
    };

    /**
     * For use when displaying that no folder is selected
     */
    public static final String FOLDER_NONE = "-NONE-";

    public static final String LOCAL_UID_PREFIX = "MAILCHATLOCAL:";

    public static final String REMOTE_UID_PREFIX = "MAILCHATREMOTE:";

    public static final String IDENTITY_HEADER = "X-MailChat-Identity";
    
    // Modified by LL
    // BEGIN
    private static final String IS_SHORTCUT_CREATED = "is_shortcut_created";
    
    public static final String MESSAGE_ID_HEADER = "Message-ID";
    public static final String MAILCHAT_MESSAGE_ID_HEADER = "X-MailChat-Message-ID";
    
    // 记录是否因认证错误需要对已有账号配置进行更新
    public static final String EXTRA_ACCOUNT_UPDATE = "cn.mailchat.intent.extra.account_update";
    
    // Header for distinguish sent draft.
    public static final String SENT_DRAFT_HEADER = "X-MailChat-Sent-Draft";
    public static final String SENT_DRAFT_HEADER_TRUE = "true";
    public static final String SENT_DRAFT_HEADER_FALSE = "false";
    
    // 记录源邮件的引用信息
    public static final String SOURCE_IDENTITY_HEADER = "X-MailChat-Source-Identity";
    
    public static final String LOG_COLLECTOR_TAG = "LogCollector";
    // END

    /**
     * Specifies how many messages will be shown in a folder by default. This number is set
     * on each new folder and can be incremented with "Load more messages..." by the
     * VISIBLE_LIMIT_INCREMENT
     */
    //public static int DEFAULT_VISIBLE_LIMIT = 25;
    // Change default visible limit to 10
    // Modified by LL
    // BEGIN
    public static int DEFAULT_VISIBLE_LIMIT = 10;
    // END

    /**
     * The maximum size of an attachment we're willing to download (either View or Save)
     * Attachments that are base64 encoded (most) will be about 1.375x their actual size
     * so we should probably factor that in. A 5MB attachment will generally be around
     * 6.8MB downloaded but only 5MB saved.
     */
    public static final int MAX_ATTACHMENT_DOWNLOAD_SIZE = (128 * 1024 * 1024);


    /* How many times should K-9 try to deliver a message before giving up
     * until the app is killed and restarted
     */

    public static int MAX_SEND_ATTEMPTS = 5;

    /**
     * Max time (in millis) the wake lock will be held for when background sync is happening
     */
    public static final int WAKE_LOCK_TIMEOUT = 600000;

    public static final int MANUAL_WAKE_LOCK_TIMEOUT = 120000;

    public static final int PUSH_WAKE_LOCK_TIMEOUT = 60000;

    public static final int MAIL_SERVICE_WAKE_LOCK_TIMEOUT = 60000;

    public static final int BOOT_RECEIVER_WAKE_LOCK_TIMEOUT = 60000;

    /**
     * Time the LED is on/off when blinking on new email notification
     */
    public static final int NOTIFICATION_LED_ON_TIME = 500;
    public static final int NOTIFICATION_LED_OFF_TIME = 2000;

    public static final boolean NOTIFICATION_LED_WHILE_SYNCING = false;
    public static final int NOTIFICATION_LED_FAST_ON_TIME = 100;
    public static final int NOTIFICATION_LED_FAST_OFF_TIME = 100;


    public static final int NOTIFICATION_LED_BLINK_SLOW = 0;
    public static final int NOTIFICATION_LED_BLINK_FAST = 1;



    public static final int NOTIFICATION_LED_FAILURE_COLOR = 0xffff0000;

    // Must not conflict with an account number
    public static final int FETCHING_EMAIL_NOTIFICATION = -5000000;
    public static final int SEND_FAILED_NOTIFICATION = -1500000;
    public static final int CERTIFICATE_EXCEPTION_NOTIFICATION_INCOMING = -2000000;
    public static final int CERTIFICATE_EXCEPTION_NOTIFICATION_OUTGOING = -2500000;
    public static final int CONNECTIVITY_ID = -3;

    public static final int DOWNLOAD_ATTACHMENT_NOTIFICATION = -7000000;
    public static final int SEND_MESSAGE_NOTIFICATION = -9000000;

    public static final int AUTO_REFRESH_INTERVAL = 15 * 60 * 1000;
    public static boolean forceRefresh = false;

    public static final HashMap<String, AttachmentView> downloadingList
        = new HashMap<String, AttachmentView>();
    public static final HashMap<String, AttachmentView> attachmentList
    	= new HashMap<String, AttachmentView>();

    public static String lastAccountSetupCheckException;

	public static LogCollector logCollector = null;

	public static SearchRange remoteSearchRange = SearchRange.TEXT;
	public static final Set<SearchResult> remoteSearchList = new HashSet<SearchResult>();
	public static boolean remoteSearchPaging;
	public static int remoteSearchStart;
	public static int remoteSearchSize;
	public static String remoteSearchQuery;

    private LruCache<String, Bitmap> mMemoryCache;
    public static boolean isChat =false;
    public static boolean isMail =false;
    public static class Intents {

        public static class EmailReceived {
            public static final String ACTION_EMAIL_RECEIVED    = "cn.mailchat.intent.action.EMAIL_RECEIVED";
            public static final String ACTION_EMAIL_DELETED     = "cn.mailchat.intent.action.EMAIL_DELETED";
            public static final String ACTION_REFRESH_OBSERVER  = "cn.mailchat.intent.action.REFRESH_OBSERVER";
            public static final String EXTRA_ACCOUNT            = "cn.mailchat.intent.extra.ACCOUNT";
            public static final String EXTRA_FOLDER             = "cn.mailchat.intent.extra.FOLDER";
            public static final String EXTRA_SENT_DATE          = "cn.mailchat.intent.extra.SENT_DATE";
            public static final String EXTRA_FROM               = "cn.mailchat.intent.extra.FROM";
            public static final String EXTRA_TO                 = "cn.mailchat.intent.extra.TO";
            public static final String EXTRA_CC                 = "cn.mailchat.intent.extra.CC";
            public static final String EXTRA_BCC                = "cn.mailchat.intent.extra.BCC";
            public static final String EXTRA_SUBJECT            = "cn.mailchat.intent.extra.SUBJECT";
            public static final String EXTRA_FROM_SELF          = "cn.mailchat.intent.extra.FROM_SELF";
        }

        public static class Share {
            /*
             * We don't want to use EmailReceived.EXTRA_FROM ("cn.mailchat.intent.extra.FROM")
             * because of different semantics (String array vs. string with comma separated
             * email addresses)
             */
            public static final String EXTRA_FROM               = "cn.mailchat.intent.extra.SENDER";
        }
    }

    /**
     * Called throughout the application when the number of accounts has changed. This method
     * enables or disables the Compose activity, the boot receiver and the service based on
     * whether any accounts are configured.
     */
    public static void setServicesEnabled(Context context) {
        int acctLength = Preferences.getPreferences(context).getAvailableAccounts().size();

        setServicesEnabled(context, acctLength > 0, null);

    }

    private static void setServicesEnabled(Context context, boolean enabled, Integer wakeLockId) {

        PackageManager pm = context.getPackageManager();

        if (!enabled && pm.getComponentEnabledSetting(new ComponentName(context, MailService.class)) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            /*
             * If no accounts now exist but the service is still enabled we're about to disable it
             * so we'll reschedule to kill off any existing alarms.
             */
            MailService.actionReset(context, wakeLockId);
        }
        Class<?>[] classes = { MailComposeActivity.class, BootReceiver.class, MailService.class };

        for (Class<?> clazz : classes) {

            boolean alreadyEnabled = pm.getComponentEnabledSetting(new ComponentName(context, clazz)) ==
                                     PackageManager.COMPONENT_ENABLED_STATE_ENABLED;

            if (enabled != alreadyEnabled) {
                pm.setComponentEnabledSetting(
                    new ComponentName(context, clazz),
                    enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            }
        }

        if (enabled && pm.getComponentEnabledSetting(new ComponentName(context, MailService.class)) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            /*
             * And now if accounts do exist then we've just enabled the service and we want to
             * schedule alarms for the new accounts.
             */
            MailService.actionReset(context, wakeLockId);
        }

    }

    /**
     * Register BroadcastReceivers programmaticaly because doing it from manifest
     * would make K-9 auto-start. We don't want auto-start because the initialization
     * sequence isn't safe while some events occur (SD card unmount).
     */
    protected void registerReceivers() {
        final StorageGoneReceiver receiver = new StorageGoneReceiver();
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");

        final BlockingQueue<Handler> queue = new SynchronousQueue<Handler>();

        // starting a new thread to handle unmount events
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    queue.put(new Handler());
                } catch (InterruptedException e) {
                    Log.e(MailChat.LOG_TAG, "", e);
                }
                Looper.loop();
            }

        }, "Unmount-thread").start();

        try {
            final Handler storageGoneHandler = queue.take();
            registerReceiver(receiver, filter, null, storageGoneHandler);
            Log.i(MailChat.LOG_TAG, "Registered: unmount receiver");
        } catch (InterruptedException e) {
            Log.e(MailChat.LOG_TAG, "Unable to register unmount receiver", e);
        }

        registerReceiver(new ShutdownReceiver(), new IntentFilter(Intent.ACTION_SHUTDOWN));
        Log.i(MailChat.LOG_TAG, "Registered: shutdown receiver");
    }

    public static void save(SharedPreferences.Editor editor) {
        editor.putBoolean("enableDebugLogging", MailChat.DEBUG);
        editor.putBoolean("enableSensitiveLogging", MailChat.DEBUG_SENSITIVE);
        editor.putString("backgroundOperations", MailChat.backgroundOps.name());
        editor.putBoolean("animations", mAnimations);
        editor.putBoolean("gesturesEnabled", mGesturesEnabled);
        editor.putBoolean("useVolumeKeysForNavigation", mUseVolumeKeysForNavigation);
        editor.putBoolean("useVolumeKeysForListNavigation", mUseVolumeKeysForListNavigation);
        editor.putBoolean("autofitWidth", mAutofitWidth);
        editor.putBoolean("quietTimeEnabled", mQuietTimeEnabled);
        editor.putString("quietTimeStarts", mQuietTimeStarts);
        editor.putString("quietTimeEnds", mQuietTimeEnds);
        editor.putBoolean("topMsgNotify", topMsgNotify);
        
        editor.putBoolean("startIntegratedInbox", mStartIntegratedInbox);
        editor.putBoolean("measureAccounts", mMeasureAccounts);
        editor.putBoolean("countSearchMessages", mCountSearchMessages);
        editor.putBoolean("messageListSenderAboveSubject", mMessageListSenderAboveSubject);
        editor.putBoolean("hideSpecialAccounts", mHideSpecialAccounts);
        editor.putBoolean("messageListStars", mMessageListStars);
        editor.putInt("messageListPreviewLines", mMessageListPreviewLines);
        editor.putBoolean("messageListCheckboxes", mMessageListCheckboxes);
        editor.putBoolean("showCorrespondentNames", mShowCorrespondentNames);
        editor.putBoolean("showContactName", mShowContactName);
        editor.putBoolean("showContactPicture", sShowContactPicture);
        editor.putBoolean("changeRegisteredNameColor", mChangeContactNameColor);
        editor.putInt("registeredNameColor", mContactNameColor);
        editor.putBoolean("messageViewFixedWidthFont", mMessageViewFixedWidthFont);
        editor.putBoolean("messageViewReturnToList", mMessageViewReturnToList);
        editor.putBoolean("messageViewShowNext", mMessageViewShowNext);
        editor.putBoolean("wrapFolderNames", mWrapFolderNames);
        editor.putBoolean("hideUserAgent", mHideUserAgent);
        editor.putBoolean("hideTimeZone", mHideTimeZone);

        editor.putString("language", language);
        editor.putInt("theme", theme.ordinal());
        editor.putInt("messageViewTheme", messageViewTheme.ordinal());
        editor.putInt("messageComposeTheme", composerTheme.ordinal());
        editor.putBoolean("fixedMessageViewTheme", useFixedMessageTheme);

        editor.putBoolean("confirmDelete", mConfirmDelete);
        editor.putBoolean("confirmDeleteStarred", mConfirmDeleteStarred);
        editor.putBoolean("confirmSpam", mConfirmSpam);
        editor.putBoolean("confirmDeleteFromNotification", mConfirmDeleteFromNotification);

        editor.putString("sortTypeEnum", mSortType.name());
        editor.putBoolean("sortAscending", mSortAscending.get(mSortType));

        editor.putString("notificationHideSubject", sNotificationHideSubject.toString());
        editor.putString("notificationQuickDelete", sNotificationQuickDelete.toString());

        editor.putString("attachmentdefaultpath", mAttachmentDefaultPath);
        editor.putBoolean("useBackgroundAsUnreadIndicator", sUseBackgroundAsUnreadIndicator);
        editor.putBoolean("threadedView", sThreadedViewEnabled);
        editor.putString("splitViewMode", sSplitViewMode.name());
        editor.putBoolean("colorizeMissingContactPictures", sColorizeMissingContactPictures);

        editor.putBoolean("messageViewArchiveActionVisible", sMessageViewArchiveActionVisible);
        editor.putBoolean("messageViewDeleteActionVisible", sMessageViewDeleteActionVisible);
        editor.putBoolean("messageViewMoveActionVisible", sMessageViewMoveActionVisible);
        editor.putBoolean("messageViewCopyActionVisible", sMessageViewCopyActionVisible);
        editor.putBoolean("messageViewSpamActionVisible", sMessageViewSpamActionVisible);
        editor.putBoolean("isNewClient", isNewClient);
        editor.putBoolean("isHideSettingBadageView", isHideSettingBadageView);
        editor.putBoolean("isHideOABadageView", isHideOABadageView);        
        
        editor.putBoolean("notifySwitch", topNotifyOn);//通知
        editor.putBoolean("notifyRingSwitch",notifyRingtone);//铃声
        editor.putInt("selectNotifyRingtone",selectNotifyRingtone);//选择铃声位置
        editor.putString("selectNotifyRingtoneName", selectNotifyRingtoneName);//选择铃声名称
        editor.putBoolean("notifyVibrateSwitch",notifyVibrateOn );//震动
        editor.putLong("firstTime", firstTime);
        editor.putBoolean("gesture",isGesture );//手势
        editor.putBoolean("gestureUnclock",isGestureUnclock );//手势解锁状态
        editor.putInt("guideVersionCode", guideVersionCode);
        editor.putBoolean("isShowAccountSettingPopo", isShowAccountSettingPopo);
        editor.putBoolean("messageListAttachmentShortcuts", mMessageListAttachmentShortcuts);
        editor.putBoolean("35CloudServices", m35CloudServices);
        editor.putString("splashUrl", splashUrl);
        editor.putString("phoneAlreadyInvationed", phoneAlreadyInvationed);
        editor.putBoolean("notifySyncError", notifySyncError);
        editor.putBoolean("isJoinCampaign", isJoinCampaign);
        editor.putString("firstWMActivtyKey", firstWMActivtyKey);
        editor.putBoolean("isShowPopup", isShowPopup);
        editor.putInt("bottomTabPosition",bottomTabPosition);
        fontSizes.save(editor);
    }

    @Override
    public void onCreate() {
        if (MailChat.DEVELOPER_MODE) {
            StrictMode.enableDefaults();
        }

        PRNGFixes.apply();

        super.onCreate();
        app = this;
        application = this;

        sIsDebuggable = ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);

        checkCachedDatabaseVersion();
        initImageLoader(this);
        Preferences prefs = Preferences.getPreferences(this);
        loadPrefs(prefs);
        
        // 查询是否已创建过桌面快捷图标
        // Modified by LL
        // BEGIN
        Storage storage = Storage.getStorage(this);
        boolean isShortcutCreated = storage.getBoolean(IS_SHORTCUT_CREATED, false);
        
        if (!isShortcutCreated) {
        	createShortcut();
        	
        	Editor edit = storage.edit();
        	edit.putBoolean(IS_SHORTCUT_CREATED, true);
        	edit.commit();
        }
        // END

        /*
         * We have to give MimeMessage a temp directory because File.createTempFile(String, String)
         * doesn't work in Android and MimeMessage does not have access to a Context.
         */
        BinaryTempFileBody.setTempDirectory(getCacheDir());

        LocalKeyStore.setKeyStoreLocation(getDir("KeyStore", MODE_PRIVATE).toString());

        /*
         * Enable background sync of messages
         */

        setServicesEnabled(this);
        registerReceivers();

        MessagingController.getInstance(this).addListener(new MessagingListener() {
            private void broadcastIntent(String action, Account account, String folder, Message message) {
                try {
                    Uri uri = Uri.parse("email://messages/" + account.getAccountNumber() + "/" + Uri.encode(folder) + "/" + Uri.encode(message.getUid()));
                    Intent intent = new Intent(action, uri);
                    intent.putExtra(MailChat.Intents.EmailReceived.EXTRA_ACCOUNT, account.getDescription());
                    intent.putExtra(MailChat.Intents.EmailReceived.EXTRA_FOLDER, folder);
                    intent.putExtra(MailChat.Intents.EmailReceived.EXTRA_SENT_DATE, message.getSentDate());
                    intent.putExtra(MailChat.Intents.EmailReceived.EXTRA_FROM, Address.toString(message.getFrom()));
                    intent.putExtra(MailChat.Intents.EmailReceived.EXTRA_TO, Address.toString(message.getRecipients(Message.RecipientType.TO)));
                    intent.putExtra(MailChat.Intents.EmailReceived.EXTRA_CC, Address.toString(message.getRecipients(Message.RecipientType.CC)));
                    intent.putExtra(MailChat.Intents.EmailReceived.EXTRA_BCC, Address.toString(message.getRecipients(Message.RecipientType.BCC)));
                    intent.putExtra(MailChat.Intents.EmailReceived.EXTRA_SUBJECT, message.getSubject());
                    intent.putExtra(MailChat.Intents.EmailReceived.EXTRA_FROM_SELF, account.isAnIdentity(message.getFrom()));
                    MailChat.this.sendBroadcast(intent);
                    if (MailChat.DEBUG)
                        Log.d(MailChat.LOG_TAG, "Broadcasted: action=" + action
                              + " account=" + account.getDescription()
                              + " folder=" + folder
                              + " message uid=" + message.getUid()
                             );

                } catch (MessagingException e) {
                    Log.w(MailChat.LOG_TAG, "Error: action=" + action
                          + " account=" + account.getDescription()
                          + " folder=" + folder
                          + " message uid=" + message.getUid()
                         );
                }
            }

            private void updateUnreadWidget() {
                try {
                    UnreadWidgetProvider.updateUnreadCount(MailChat.this);
                } catch (Exception e) {
                    if (MailChat.DEBUG) {
                        Log.e(LOG_TAG, "Error while updating unread widget(s)", e);
                    }
                }
            }

            @Override
            public void synchronizeMailboxRemovedMessage(Account account, String folder, Message message) {
                broadcastIntent(MailChat.Intents.EmailReceived.ACTION_EMAIL_DELETED, account, folder, message);
                updateUnreadWidget();
            }

            @Override
            public void messageDeleted(Account account, String folder, Message message) {
                broadcastIntent(MailChat.Intents.EmailReceived.ACTION_EMAIL_DELETED, account, folder, message);
                updateUnreadWidget();
            }

            @Override
            public void synchronizeMailboxNewMessage(Account account, String folder, Message message) {
                broadcastIntent(MailChat.Intents.EmailReceived.ACTION_EMAIL_RECEIVED, account, folder, message);
                updateUnreadWidget();
            }

            @Override
            public void folderStatusChanged(Account account, String folderName,
                    int unreadMessageCount) {

                updateUnreadWidget();

                // let observers know a change occurred
                Intent intent = new Intent(MailChat.Intents.EmailReceived.ACTION_REFRESH_OBSERVER, null);
                intent.putExtra(MailChat.Intents.EmailReceived.EXTRA_ACCOUNT, account.getDescription());
                intent.putExtra(MailChat.Intents.EmailReceived.EXTRA_FOLDER, folderName);
                MailChat.this.sendBroadcast(intent);

            }

        });
    	int memClass = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE))
				.getMemoryClass();
		int cacheSize = 1024 * 1024 * memClass / 8;
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes();
			}
		};
		//Intent intent =new Intent(this, GuardianService.class);
		//startService(intent);
		MobclickAgent.openActivityDurationTrack(false);
        notifyObservers();

        //初始化百度定位
        initBaiduMap();
    }


	/**
     * 
     * method name: initBaiduMap
     * function @Description: TODO
     * Parameters and return values description:   field_name
     *      void   return type
     *  @History memory：
     *     @Date：2016-1-11 下午4:18:28	@Modified by：zhangjx
     *     @Description:初始化定位sdk
     */
	private void initBaiduMap() {
        locationService = new LocationService(getApplicationContext());
		mVibrator = (Vibrator) getApplicationContext().getSystemService(
				Service.VIBRATOR_SERVICE);
//		SDKInitializer.initialize(getApplicationContext());
	}
	public LruCache<String, Bitmap> getmMemoryCache() {
		return mMemoryCache;
	}
	
    /**
     * Loads the last known database version of the accounts' databases from a
     * {@link SharedPreference}.
     *
     * <p>
     * If the stored version matches {@link LocalStore#DB_VERSION} we know that the databases are
     * up to date.<br>
     * Using {@code SharedPreferences} should be a lot faster than opening all SQLite databases to
     * get the current database version.
     * </p><p>
     * See {@link UpgradeDatabases} for a detailed explanation of the database upgrade process.
     * </p>
     *
     * @see #areDatabasesUpToDate()
     */
    public void checkCachedDatabaseVersion() {
        sDatabaseVersionCache = getSharedPreferences(DATABASE_VERSION_CACHE, MODE_PRIVATE);

        int cachedVersion = sDatabaseVersionCache.getInt(KEY_LAST_ACCOUNT_DATABASE_VERSION, 0);

        if (cachedVersion >= LocalStore.DB_VERSION) {
            MailChat.setDatabasesUpToDate(false);
        }
    }

    public static void loadPrefs(Preferences prefs) {
        SharedPreferences sprefs = prefs.getPreferences();
        DEBUG = sprefs.getBoolean("enableDebugLogging", false);
        if (!DEBUG && sIsDebuggable && Debug.isDebuggerConnected()) {
            // If the debugger is attached, we're probably (surprise surprise) debugging something.
            DEBUG = true;
            Log.i(MailChat.LOG_TAG, "Debugger attached; enabling debug logging.");
        }
        DEBUG_SENSITIVE = sprefs.getBoolean("enableSensitiveLogging", false);
        mAnimations = sprefs.getBoolean("animations", true);
        mGesturesEnabled = sprefs.getBoolean("gesturesEnabled", false);
        mUseVolumeKeysForNavigation = sprefs.getBoolean("useVolumeKeysForNavigation", false);
        mUseVolumeKeysForListNavigation = sprefs.getBoolean("useVolumeKeysForListNavigation", false);
        mStartIntegratedInbox = sprefs.getBoolean("startIntegratedInbox", false);
        mMeasureAccounts = sprefs.getBoolean("measureAccounts", true);
        mCountSearchMessages = sprefs.getBoolean("countSearchMessages", true);
        mHideSpecialAccounts = sprefs.getBoolean("hideSpecialAccounts", false);
        mMessageListSenderAboveSubject = sprefs.getBoolean("messageListSenderAboveSubject", false);
        mMessageListCheckboxes = sprefs.getBoolean("messageListCheckboxes", false);
        mMessageListStars = sprefs.getBoolean("messageListStars", true);
        mMessageListPreviewLines = sprefs.getInt("messageListPreviewLines", 2);

        mAutofitWidth = sprefs.getBoolean("autofitWidth", true);

        mQuietTimeEnabled = sprefs.getBoolean("quietTimeEnabled", false);
        mQuietTimeStarts = sprefs.getString("quietTimeStarts", "23:00");
        mQuietTimeEnds = sprefs.getString("quietTimeEnds", "8:00");

        topMsgNotify = sprefs.getBoolean("topMsgNotify", true);  
        topNotifyOn = sprefs.getBoolean("notifySwitch", true);
        notifyRingtone = sprefs.getBoolean("notifyRingSwitch", true);
        selectNotifyRingtone = sprefs.getInt("selectNotifyRingtone", 0);
        selectNotifyRingtoneName=sprefs.getString("selectNotifyRingtoneName", "");
        notifyVibrateOn = sprefs.getBoolean("notifyVibrateSwitch", true);
        firstTime = sprefs.getLong("firstTime", 0);
        isGesture = sprefs.getBoolean("gesture", false);
        isGestureUnclock=sprefs.getBoolean("gestureUnclock", false);
        guideVersionCode=sprefs.getInt("guideVersionCode", 0);
        isShowAccountSettingPopo=sprefs.getBoolean("isShowAccountSettingPopo", true);
        mMessageListAttachmentShortcuts = sprefs.getBoolean("messageListAttachmentShortcuts", true);
        m35CloudServices = sprefs.getBoolean("35CloudServices", false);
        splashUrl = sprefs.getString("splashUrl", "");
        phoneAlreadyInvationed = sprefs.getString("phoneAlreadyInvationed", "");
        notifySyncError = sprefs.getBoolean("notifySyncError", false);
        isJoinCampaign = sprefs.getBoolean("isJoinCampaign", true);
        firstWMActivtyKey = sprefs.getString("firstWMActivtyKey", "");
        isShowPopup = sprefs.getBoolean("isShowPopup", true);

        mShowCorrespondentNames = sprefs.getBoolean("showCorrespondentNames", true);
        mShowContactName = sprefs.getBoolean("showContactName", false);
        sShowContactPicture = sprefs.getBoolean("showContactPicture", true);
        mChangeContactNameColor = sprefs.getBoolean("changeRegisteredNameColor", false);
        mContactNameColor = sprefs.getInt("registeredNameColor", 0xff00008f);
        mMessageViewFixedWidthFont = sprefs.getBoolean("messageViewFixedWidthFont", false);
        mMessageViewReturnToList = sprefs.getBoolean("messageViewReturnToList", false);
        mMessageViewShowNext = sprefs.getBoolean("messageViewShowNext", false);
        mWrapFolderNames = sprefs.getBoolean("wrapFolderNames", false);
        mHideUserAgent = sprefs.getBoolean("hideUserAgent", false);
        mHideTimeZone = sprefs.getBoolean("hideTimeZone", false);

        mConfirmDelete = sprefs.getBoolean("confirmDelete", false);
        mConfirmDeleteStarred = sprefs.getBoolean("confirmDeleteStarred", false);
        mConfirmSpam = sprefs.getBoolean("confirmSpam", false);
        mConfirmDeleteFromNotification = sprefs.getBoolean("confirmDeleteFromNotification", true);
        bottomTabPosition = sprefs.getInt("bottomTabPosition", 0);
        try {
            String value = sprefs.getString("sortTypeEnum", Account.DEFAULT_SORT_TYPE.name());
            mSortType = SortType.valueOf(value);
        } catch (Exception e) {
            mSortType = Account.DEFAULT_SORT_TYPE;
        }

        boolean sortAscending = sprefs.getBoolean("sortAscending", Account.DEFAULT_SORT_ASCENDING);
        mSortAscending.put(mSortType, sortAscending);

        String notificationHideSubject = sprefs.getString("notificationHideSubject", null);
        if (notificationHideSubject == null) {
            // If the "notificationHideSubject" setting couldn't be found, the app was probably
            // updated. Look for the old "keyguardPrivacy" setting and map it to the new enum.
            sNotificationHideSubject = (sprefs.getBoolean("keyguardPrivacy", false)) ?
                    NotificationHideSubject.WHEN_LOCKED : NotificationHideSubject.NEVER;
        } else {
            sNotificationHideSubject = NotificationHideSubject.valueOf(notificationHideSubject);
        }

        String notificationQuickDelete = sprefs.getString("notificationQuickDelete", null);
        if (notificationQuickDelete != null) {
            sNotificationQuickDelete = NotificationQuickDelete.valueOf(notificationQuickDelete);
        }

        String splitViewMode = sprefs.getString("splitViewMode", null);
        if (splitViewMode != null) {
            sSplitViewMode = SplitViewMode.valueOf(splitViewMode);
        }

        mAttachmentDefaultPath = sprefs.getString("attachmentdefaultpath",  Environment.getExternalStorageDirectory().toString());
        sUseBackgroundAsUnreadIndicator = sprefs.getBoolean("useBackgroundAsUnreadIndicator", true);
        sThreadedViewEnabled = sprefs.getBoolean("threadedView", true);
        fontSizes.load(sprefs);

        try {
            setBackgroundOps(BACKGROUND_OPS.valueOf(sprefs.getString(
                    "backgroundOperations",
                    BACKGROUND_OPS.WHEN_CHECKED_AUTO_SYNC.name())));
        } catch (Exception e) {
            setBackgroundOps(BACKGROUND_OPS.WHEN_CHECKED_AUTO_SYNC);
        }

        sColorizeMissingContactPictures = sprefs.getBoolean("colorizeMissingContactPictures", true);

        sMessageViewArchiveActionVisible = sprefs.getBoolean("messageViewArchiveActionVisible", false);
        sMessageViewDeleteActionVisible = sprefs.getBoolean("messageViewDeleteActionVisible", true);
        sMessageViewMoveActionVisible = sprefs.getBoolean("messageViewMoveActionVisible", false);
        sMessageViewCopyActionVisible = sprefs.getBoolean("messageViewCopyActionVisible", false);
        sMessageViewSpamActionVisible = sprefs.getBoolean("messageViewSpamActionVisible", false);
        isNewClient = sprefs.getBoolean("isNewClient", false);
        isHideSettingBadageView=  sprefs.getBoolean("isHideSettingBadageView", false);
        isHideOABadageView=  sprefs.getBoolean("isHideOABadageView", false);

        MailChat.setK9Language(sprefs.getString("language", ""));

        int themeValue = sprefs.getInt("theme", Theme.LIGHT.ordinal());
        // We used to save the resource ID of the theme. So convert that to the new format if
        // necessary.
        if (themeValue == Theme.DARK.ordinal() || themeValue == android.R.style.Theme) {
            MailChat.setK9Theme(Theme.DARK);
        } else {
            MailChat.setK9Theme(Theme.LIGHT);
        }

        themeValue = sprefs.getInt("messageViewTheme", Theme.USE_GLOBAL.ordinal());
        MailChat.setK9MessageViewThemeSetting(Theme.values()[themeValue]);
        themeValue = sprefs.getInt("messageComposeTheme", Theme.USE_GLOBAL.ordinal());
        MailChat.setK9ComposerThemeSetting(Theme.values()[themeValue]);
        MailChat.setUseFixedMessageViewTheme(sprefs.getBoolean("fixedMessageViewTheme", true));
    }

    /**
     * since Android invokes Application.onCreate() only after invoking all
     * other components' onCreate(), here is a way to notify interested
     * component that the application is available and ready
     */
    protected void notifyObservers() {
        synchronized (observers) {
            for (final ApplicationAware aware : observers) {
                if (MailChat.DEBUG) {
                    Log.v(MailChat.LOG_TAG, "Initializing observer: " + aware);
                }
                try {
                    aware.initializeComponent(this);
                } catch (Exception e) {
                    Log.w(MailChat.LOG_TAG, "Failure when notifying " + aware, e);
                }
            }

            sInitialized = true;
            observers.clear();
        }
    }

    /**
     * Register a component to be notified when the {@link MailChat} instance is ready.
     *
     * @param component
     *            Never <code>null</code>.
     */
    public static void registerApplicationAware(final ApplicationAware component) {
        synchronized (observers) {
            if (sInitialized) {
                component.initializeComponent(MailChat.app);
            } else if (!observers.contains(component)) {
                observers.add(component);
            }
        }
    }

    public static String getK9Language() {
        return language;
    }

    public static void setK9Language(String nlanguage) {
        language = nlanguage;
    }

    /**
     * Possible values for the different theme settings.
     *
     * <p><strong>Important:</strong>
     * Do not change the order of the items! The ordinal value (position) is used when saving the
     * settings.</p>
     */
    public enum Theme {
        LIGHT,
        DARK,
        USE_GLOBAL
    }

    public static int getK9ThemeResourceId(Theme themeId) {
        return (themeId == Theme.LIGHT) ? R.style.Theme_MailChat_Light : R.style.Theme_MailChat_Dark;
    }

    public static int getK9ThemeResourceId() {
        return getK9ThemeResourceId(theme);
    }

    public static Theme getK9MessageViewTheme() {
        return messageViewTheme == Theme.USE_GLOBAL ? theme : messageViewTheme;
    }

    public static Theme getK9MessageViewThemeSetting() {
        return messageViewTheme;
    }

    public static Theme getK9ComposerTheme() {
        return composerTheme == Theme.USE_GLOBAL ? theme : composerTheme;
    }

    public static Theme getK9ComposerThemeSetting() {
        return composerTheme;
    }

    public static Theme getK9Theme() {
        return theme;
    }

    public static void setK9Theme(Theme ntheme) {
        if (ntheme != Theme.USE_GLOBAL) {
            theme = ntheme;
        }
    }

    public static void setK9MessageViewThemeSetting(Theme nMessageViewTheme) {
        messageViewTheme = nMessageViewTheme;
    }

    public static boolean useFixedMessageViewTheme() {
        return useFixedMessageTheme;
    }

    public static void setK9ComposerThemeSetting(Theme compTheme) {
        composerTheme = compTheme;
    }

    public static void setUseFixedMessageViewTheme(boolean useFixed) {
        useFixedMessageTheme = useFixed;
        if (!useFixedMessageTheme && messageViewTheme == Theme.USE_GLOBAL) {
            messageViewTheme = theme;
        }
    }

    public static BACKGROUND_OPS getBackgroundOps() {
        return backgroundOps;
    }

    public static boolean setBackgroundOps(BACKGROUND_OPS backgroundOps) {
        BACKGROUND_OPS oldBackgroundOps = MailChat.backgroundOps;
        MailChat.backgroundOps = backgroundOps;
        return backgroundOps != oldBackgroundOps;
    }

    public static boolean setBackgroundOps(String nbackgroundOps) {
        return setBackgroundOps(BACKGROUND_OPS.valueOf(nbackgroundOps));
    }

    public static boolean gesturesEnabled() {
        return mGesturesEnabled;
    }

    public static void setGesturesEnabled(boolean gestures) {
        mGesturesEnabled = gestures;
    }

    public static boolean useVolumeKeysForNavigationEnabled() {
        return mUseVolumeKeysForNavigation;
    }

    public static void setUseVolumeKeysForNavigation(boolean volume) {
        mUseVolumeKeysForNavigation = volume;
    }

    public static boolean useVolumeKeysForListNavigationEnabled() {
        return mUseVolumeKeysForListNavigation;
    }

    public static void setUseVolumeKeysForListNavigation(boolean enabled) {
        mUseVolumeKeysForListNavigation = enabled;
    }

    public static boolean autofitWidth() {
        return mAutofitWidth;
    }

    public static void setAutofitWidth(boolean autofitWidth) {
        mAutofitWidth = autofitWidth;
    }

    public static boolean getQuietTimeEnabled() {
        return mQuietTimeEnabled;
    }

    public static void setQuietTimeEnabled(boolean quietTimeEnabled) {
        mQuietTimeEnabled = quietTimeEnabled;
    }

    public static String getQuietTimeStarts() {
        return mQuietTimeStarts;
    }

    public static void setQuietTimeStarts(String quietTimeStarts) {
        mQuietTimeStarts = quietTimeStarts;
    }

    public static String getQuietTimeEnds() {
        return mQuietTimeEnds;
    }

    public static void setQuietTimeEnds(String quietTimeEnds) {
        mQuietTimeEnds = quietTimeEnds;
    }
    public static boolean isTopNotifyOn() {
		return topNotifyOn;
	}

	public static void setTopNotifyOn(boolean topNotifyOn) {
		MailChat.topNotifyOn = topNotifyOn;
	}


	public static boolean isNotifyVibrateOn() {
		return notifyVibrateOn;
	}

	public static void setNotifyVibrateOn(boolean notifyVibrateOn) {
		MailChat.notifyVibrateOn = notifyVibrateOn;
	}

	public static boolean isNotifyRingtone() {
		return notifyRingtone;
	}

	public static void setNotifyRingtone(boolean notifyRingtone) {
		MailChat.notifyRingtone = notifyRingtone;
	}

	public static int getSelectNotifyRingtone() {
		return selectNotifyRingtone;
	}

	public static void setSelectNotifyRingtone(int selectNotifyRingtone) {
		MailChat.selectNotifyRingtone = selectNotifyRingtone;
	}

	public static String getSelectNotifyRingtoneName() {
		return selectNotifyRingtoneName;
	}

	public static void setSelectNotifyRingtoneName(String selectNotifyRingtoneName) {
		MailChat.selectNotifyRingtoneName = selectNotifyRingtoneName;
	}

    public static boolean isQuietTime() {
        if (!mQuietTimeEnabled) {
            return false;
        }

        Time time = new Time();
        time.setToNow();
        Integer startHour = Integer.parseInt(mQuietTimeStarts.split(":")[0]);
        Integer startMinute = Integer.parseInt(mQuietTimeStarts.split(":")[1]);
        Integer endHour = Integer.parseInt(mQuietTimeEnds.split(":")[0]);
        Integer endMinute = Integer.parseInt(mQuietTimeEnds.split(":")[1]);

        Integer now = (time.hour * 60) + time.minute;
        Integer quietStarts = startHour * 60 + startMinute;
        Integer quietEnds =  endHour * 60 + endMinute;

        // If start and end times are the same, we're never quiet
        if (quietStarts.equals(quietEnds)) {
            return false;
        }


        // 21:00 - 05:00 means we want to be quiet if it's after 9 or before 5
        if (quietStarts > quietEnds) {
            // if it's 22:00 or 03:00 but not 8:00
            if (now >= quietStarts || now <= quietEnds) {
                return true;
            }
        }

        // 01:00 - 05:00
        else {

            // if it' 2:00 or 4:00 but not 8:00 or 0:00
            if (now >= quietStarts && now <= quietEnds) {
                return true;
            }
        }

        return false;
    }



    public static boolean startIntegratedInbox() {
        return mStartIntegratedInbox;
    }

    public static void setStartIntegratedInbox(boolean startIntegratedInbox) {
        mStartIntegratedInbox = startIntegratedInbox;
    }

    public static boolean showAnimations() {
        return mAnimations;
    }

    public static void setAnimations(boolean animations) {
        mAnimations = animations;
    }

    public static int messageListPreviewLines() {
        return mMessageListPreviewLines;
    }

    public static void setMessageListPreviewLines(int lines) {
        mMessageListPreviewLines = lines;
    }

    public static boolean messageListCheckboxes() {
        return mMessageListCheckboxes;
    }

    public static void setMessageListCheckboxes(boolean checkboxes) {
        mMessageListCheckboxes = checkboxes;
    }

    public static boolean messageListStars() {
        return mMessageListStars;
    }

    public static void setMessageListStars(boolean stars) {
        mMessageListStars = stars;
    }

    public static boolean showCorrespondentNames() {
        return mShowCorrespondentNames;
    }

     public static boolean messageListSenderAboveSubject() {
         return mMessageListSenderAboveSubject;
     }

    public static void setMessageListSenderAboveSubject(boolean sender) {
         mMessageListSenderAboveSubject = sender;
    }
    public static void setShowCorrespondentNames(boolean showCorrespondentNames) {
        mShowCorrespondentNames = showCorrespondentNames;
    }

    public static boolean showContactName() {
        return mShowContactName;
    }

    public static void setShowContactName(boolean showContactName) {
        mShowContactName = showContactName;
    }

    public static boolean changeContactNameColor() {
        return mChangeContactNameColor;
    }

    public static void setChangeContactNameColor(boolean changeContactNameColor) {
        mChangeContactNameColor = changeContactNameColor;
    }

    public static int getContactNameColor() {
        return mContactNameColor;
    }

    public static void setContactNameColor(int contactNameColor) {
        mContactNameColor = contactNameColor;
    }

    public static boolean messageViewFixedWidthFont() {
        return mMessageViewFixedWidthFont;
    }

    public static void setMessageViewFixedWidthFont(boolean fixed) {
        mMessageViewFixedWidthFont = fixed;
    }

    public static boolean messageViewReturnToList() {
        return mMessageViewReturnToList;
    }

    public static void setMessageViewReturnToList(boolean messageViewReturnToList) {
        mMessageViewReturnToList = messageViewReturnToList;
    }

    public static boolean messageViewShowNext() {
        return mMessageViewShowNext;
    }

    public static void setMessageViewShowNext(boolean messageViewShowNext) {
        mMessageViewShowNext = messageViewShowNext;
    }

    public static FontSizes getFontSizes() {
        return fontSizes;
    }

    public static boolean measureAccounts() {
        return mMeasureAccounts;
    }

    public static void setMeasureAccounts(boolean measureAccounts) {
        mMeasureAccounts = measureAccounts;
    }

    public static boolean countSearchMessages() {
        return mCountSearchMessages;
    }

    public static void setCountSearchMessages(boolean countSearchMessages) {
        mCountSearchMessages = countSearchMessages;
    }

    public static boolean isHideSpecialAccounts() {
        return mHideSpecialAccounts;
    }

    public static void setHideSpecialAccounts(boolean hideSpecialAccounts) {
        mHideSpecialAccounts = hideSpecialAccounts;
    }

    public static boolean confirmDelete() {
        return mConfirmDelete;
    }

    public static void setConfirmDelete(final boolean confirm) {
        mConfirmDelete = confirm;
    }

    public static boolean confirmDeleteStarred() {
        return mConfirmDeleteStarred;
    }

    public static void setConfirmDeleteStarred(final boolean confirm) {
        mConfirmDeleteStarred = confirm;
    }

    public static boolean confirmSpam() {
        return mConfirmSpam;
    }

    public static void setConfirmSpam(final boolean confirm) {
        mConfirmSpam = confirm;
    }

    public static boolean confirmDeleteFromNotification() {
        return mConfirmDeleteFromNotification;
    }

    public static void setConfirmDeleteFromNotification(final boolean confirm) {
        mConfirmDeleteFromNotification = confirm;
    }

    public static NotificationHideSubject getNotificationHideSubject() {
        return sNotificationHideSubject;
    }

    public static void setNotificationHideSubject(final NotificationHideSubject mode) {
        sNotificationHideSubject = mode;
    }

    public static NotificationQuickDelete getNotificationQuickDeleteBehaviour() {
        return sNotificationQuickDelete;
    }

    public static void setNotificationQuickDeleteBehaviour(final NotificationQuickDelete mode) {
        sNotificationQuickDelete = mode;
    }

    public static boolean wrapFolderNames() {
        return mWrapFolderNames;
    }
    public static void setWrapFolderNames(final boolean state) {
        mWrapFolderNames = state;
    }

    public static boolean hideUserAgent() {
        return mHideUserAgent;
    }
    public static void setHideUserAgent(final boolean state) {
        mHideUserAgent = state;
    }

    public static boolean hideTimeZone() {
        return mHideTimeZone;
    }
    public static void setHideTimeZone(final boolean state) {
        mHideTimeZone = state;
    }

    public static String getAttachmentDefaultPath() {
        return mAttachmentDefaultPath;
    }

    public static void setAttachmentDefaultPath(String attachmentDefaultPath) {
        MailChat.mAttachmentDefaultPath = attachmentDefaultPath;
    }

    public static synchronized SortType getSortType() {
        return mSortType;
    }

    public static synchronized void setSortType(SortType sortType) {
        mSortType = sortType;
    }

    public static synchronized boolean isSortAscending(SortType sortType) {
        if (mSortAscending.get(sortType) == null) {
            mSortAscending.put(sortType, sortType.isDefaultAscending());
        }
        return mSortAscending.get(sortType);
    }

    public static synchronized void setSortAscending(SortType sortType, boolean sortAscending) {
        mSortAscending.put(sortType, sortAscending);
    }

    public static synchronized boolean useBackgroundAsUnreadIndicator() {
        return sUseBackgroundAsUnreadIndicator;
    }

    public static synchronized void setUseBackgroundAsUnreadIndicator(boolean enabled) {
        sUseBackgroundAsUnreadIndicator = enabled;
    }

    public static synchronized boolean isThreadedViewEnabled() {
        return sThreadedViewEnabled;
    }

    public static synchronized void setThreadedViewEnabled(boolean enable) {
        sThreadedViewEnabled = enable;
    }

    public static synchronized SplitViewMode getSplitViewMode() {
        return sSplitViewMode;
    }

    public static synchronized void setSplitViewMode(SplitViewMode mode) {
        sSplitViewMode = mode;
    }

    public static boolean showContactPicture() {
        return sShowContactPicture;
    }

    public static void setShowContactPicture(boolean show) {
        sShowContactPicture = show;
    }

    public static boolean isColorizeMissingContactPictures() {
        return sColorizeMissingContactPictures;
    }

    public static void setColorizeMissingContactPictures(boolean enabled) {
        sColorizeMissingContactPictures = enabled;
    }

    public static boolean isMessageViewArchiveActionVisible() {
        return sMessageViewArchiveActionVisible;
    }

    public static void setMessageViewArchiveActionVisible(boolean visible) {
        sMessageViewArchiveActionVisible = visible;
    }

    public static boolean isMessageViewDeleteActionVisible() {
        return sMessageViewDeleteActionVisible;
    }

    public static void setMessageViewDeleteActionVisible(boolean visible) {
        sMessageViewDeleteActionVisible = visible;
    }

    public static boolean isMessageViewMoveActionVisible() {
        return sMessageViewMoveActionVisible;
    }

    public static void setMessageViewMoveActionVisible(boolean visible) {
        sMessageViewMoveActionVisible = visible;
    }

    public static boolean isMessageViewCopyActionVisible() {
        return sMessageViewCopyActionVisible;
    }

    public static void setMessageViewCopyActionVisible(boolean visible) {
        sMessageViewCopyActionVisible = visible;
    }

    public static boolean isMessageViewSpamActionVisible() {
        return sMessageViewSpamActionVisible;
    }

    public static void setMessageViewSpamActionVisible(boolean visible) {
        sMessageViewSpamActionVisible = visible;
    }

    public static boolean isNewClient() {
		return isNewClient;
	}

	public static void setNewClient(boolean isNewClient) {
		MailChat.isNewClient = isNewClient;
	}

	public static boolean isHideSettingBadageView() {
		return isHideSettingBadageView;
	}

	public static void setHideSettingBadageView(boolean isHideSettingBadageView) {
		MailChat.isHideSettingBadageView = isHideSettingBadageView;
	}

	/**
     * Check if we already know whether all databases are using the current database schema.
     *
     * <p>
     * This method is only used for optimizations. If it returns {@code true} we can be certain that
     * getting a {@link LocalStore} instance won't trigger a schema upgrade.
     * </p>
     *
     * @return {@code true}, if we know that all databases are using the current database schema.
     *         {@code false}, otherwise.
     */
    public static synchronized boolean areDatabasesUpToDate() {
        return sDatabasesUpToDate;
    }

    /**
     * Remember that all account databases are using the most recent database schema.
     *
     * @param save
     *         Whether or not to write the current database version to the
     *         {@code SharedPreferences} {@link #DATABASE_VERSION_CACHE}.
     *
     * @see #areDatabasesUpToDate()
     */
    public static synchronized void setDatabasesUpToDate(boolean save) {
        sDatabasesUpToDate = true;

        if (save) {
            Editor editor = sDatabaseVersionCache.edit();
            editor.putInt(KEY_LAST_ACCOUNT_DATABASE_VERSION, LocalStore.DB_VERSION);
            editor.commit();
        }
    }
    
    public static MailChat getInstance() {
		return application;
	}
    
    public boolean isForground() {
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		return cn.getPackageName().equals(app.getPackageName());
	}
    
	public static boolean isTopMsgNotify() {
		return topMsgNotify;
	}

	public static void setTopMsgNotify(boolean topMsgNotify) {
		MailChat.topMsgNotify = topMsgNotify;
	}
	
	public static long getFirstTime() {
		return firstTime;
	}

	public static void setFirstTime(long firstTime) {
		MailChat.firstTime = firstTime;
	}
	
	public static boolean isGesture() {
		return isGesture;
	}

	public static void setGesture(boolean isGesture) {
		MailChat.isGesture = isGesture;
	}
	
	public static boolean isGestureUnclock() {
		return isGestureUnclock;
	}

	public static void setGestureUnclock(boolean isGestureUnclock) {
		MailChat.isGestureUnclock = isGestureUnclock;
	}
	
	public static int getGuideVersionCode() {
		return guideVersionCode;
	}

	public static void setGuideVersionCode(int guideVersionCode) {
		MailChat.guideVersionCode = guideVersionCode;
	}

	public static boolean isShowAccountSettingPopo() {
		return isShowAccountSettingPopo;
	}

	public static void setShowAccountSettingPopo(boolean isShowAccountSettingPopo) {
		MailChat.isShowAccountSettingPopo = isShowAccountSettingPopo;
	}

	public static boolean isMessageListAttachmentShortcuts() {
        return mMessageListAttachmentShortcuts;
    }

    public static void setMessageListAttachmentShortcuts(
            boolean messageListAttachmentShortcuts) {
        MailChat.mMessageListAttachmentShortcuts = messageListAttachmentShortcuts;
    }

    public static boolean is35CloudServices() {
        return m35CloudServices;
    }

    public static void set35CloudServices(
            boolean is35CloudServices) {
        MailChat.m35CloudServices = is35CloudServices;
    }

	public static String getSplashUrl() {
		return splashUrl;
	}

	public static void setSplashUrl(String splashUrl) {
		MailChat.splashUrl = splashUrl;
	}
	
    public static String getPhoneAlreadyInvationed() {
		return phoneAlreadyInvationed;
	}

	public static void setPhoneAlreadyInvationed(String phoneAlreadyInvationed) {
		MailChat.phoneAlreadyInvationed = phoneAlreadyInvationed;
	}

	public static boolean isNotifySyncError() {
        return notifySyncError;
    }

    public static void setNotifySyncError(boolean notifySyncError) {
        MailChat.notifySyncError = notifySyncError;
    }

	public static boolean isJoinCampaign() {
		return isJoinCampaign;
	}

	public static void setJoinCampaign(boolean isJoinCampaign) {
		MailChat.isJoinCampaign = isJoinCampaign;
	}

	public static String getFirstWMActivtyKey() {
		return firstWMActivtyKey;
	}

	public static void setFirstWMActivtyKey(String firstWMActivtyKey) {
		MailChat.firstWMActivtyKey = firstWMActivtyKey;
	}

	public static boolean isShowPopup() {
		return isShowPopup;
	}

	public static void setShowPopup(boolean isShowPopup) {
		MailChat.isShowPopup = isShowPopup;
	}

	public static int getBottomTabPosition() {
		return bottomTabPosition;
	}

	public static void setBottomTabPosition(int bottomTabPosition) {
		MailChat.bottomTabPosition = bottomTabPosition;
	}

	public static boolean isHideOABadageView() {
		return isHideOABadageView;
	}

	public static void setHideOABadageView(boolean isHideOABadageView) {
		MailChat.isHideOABadageView = isHideOABadageView;
	}

	public int getVersionCode() {
		try {
			PackageManager pm = getPackageManager();
			PackageInfo packageInfo = pm.getPackageInfo(getPackageName(),
					PackageManager.GET_ACTIVITIES);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return -1;
	}
	/**
	 * 获得mailchat根路径
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-14
	 */
	public String getMailchatDirectory() {
		String path = GlobalConstants.APPCATION_SCARD_DIRECTORY;
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		return path;
	}
	/**
	 * 获得邮件图片路径
	 * 
	 * @Description:
	 * @param account
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-4-14
	 */
	public String getMailImageCacheDirectory(Account account) {
		String path = GlobalConstants.APPCATION_SCARD_DIRECTORY
		+ account.getUuid() + "/"+GlobalConstants.MAIL_DIRECTORY
		+ GlobalConstants.IMAGE_DIRECTORY;
		File file = new File(path);
		if (!file.exists()) {
		file.mkdirs();
		}
		return path;
	}
	/**
	 * 获得邮件附件路径
	 * 
	 * @Description:
	 * @param account
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-4-14
	 */
	public String getMailAttachmentDirectory(Account account) {
		String path = GlobalConstants.APPCATION_SCARD_DIRECTORY
				+ account.getUuid() + "/"+GlobalConstants.MAIL_DIRECTORY
				+ GlobalConstants.ATTACHMENT_DIRECTORY;
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		return path;
	}
	/**
	 * 获得聊天大图片缓存路径
	 * 
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-14
	 */
	public String getChatImageCacheDirectory(Account account) {
		String path = GlobalConstants.APPCATION_SCARD_DIRECTORY
				+ account.getUuid() + "/"+GlobalConstants.CHAT_DIRECTORY
				+ GlobalConstants.IMAGE_DIRECTORY;
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		return path;
	}
	/**
	 * 获得聊天缩略图缓存路径
	 * 
	 * @Description:
	 * @param account
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-14
	 */
	public String getChatThumbnailImageCacheDirectory(Account account) {
		String path = GlobalConstants.APPCATION_SCARD_DIRECTORY
				+ account.getUuid() + "/"+GlobalConstants.CHAT_DIRECTORY
				+ GlobalConstants.THUMBNAIL_IMAGE_DIRECTORY;
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		return path;
	}
	/**
	 * 
	 * method name: getUserImageChaceDirectory function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param account
	 * @return field_name String return type
	 * @History memory：
	 * @Date：2015-4-16 下午6:59:52 @Modified by：zhangjx
	 * @Description：获得头像大图缓存路径
	 */
	public String getUserBigImageCacheDirectory(Account account) {
		String path ="";
		String storageState = Environment.getExternalStorageState();
		if (storageState.equals(Environment.MEDIA_MOUNTED)) {
			path = GlobalConstants.APPCATION_SCARD_DIRECTORY
					+ account.getUuid() + "/" + GlobalConstants.USER_DIRECTORY
					+ GlobalConstants.IMAGE_DIRECTORY;
			File savedir = new File(path);
			if (!savedir.exists()) {
				savedir.mkdirs();
			}
		} else {
			Log.d(MailChat.LOG_COLLECTOR_TAG, "无法保存上传的头像，请检查SD卡是否挂载");
			return null;
		}
		return path;
	}

	/**
	 * 
	 * method name: getUserSmallImageChaceDirectory function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param account
	 * @return field_name String return type
	 * @History memory：
	 * @Date：2015-4-16 下午7:01:11 @Modified by：zhangjx
	 * @Description：获得头像小图缓存路径
	 */
	public String getUserSmallImageCacheDirectory(Account account) {
		String path = GlobalConstants.APPCATION_SCARD_DIRECTORY
				+ account.getUuid() + "/" + GlobalConstants.USER_DIRECTORY
				+ GlobalConstants.THUMBNAIL_IMAGE_DIRECTORY;
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		return path;
	}
	/**
	 * 获得聊天本地压缩图缓存路径
	 * 
	 * @Description:
	 * @param account
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-29
	 */
	public String getChatLocalThumbnailImageCacheDirectory(Account account) {
		String path = GlobalConstants.APPCATION_SCARD_DIRECTORY
				+ account.getUuid() + "/"+GlobalConstants.CHAT_DIRECTORY
				+ GlobalConstants.LOCAL_THUMBNAIL_IMAGE_DIRECTORY;
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		return path;
	}
	/**
	 * 获得聊天语音缓存路径
	 * 
	 * @Description:
	 * @param account
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-29
	 */
	public String getChatVoiceDirectory(Account account) {
		String path = GlobalConstants.APPCATION_SCARD_DIRECTORY
				+ account.getUuid() + "/"+GlobalConstants.CHAT_DIRECTORY
				+ GlobalConstants.VOICE_DIRECTORY;
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		return path;
	}

	/**
	 * 获得聊天文件缓存路径
	 * 
	 * @Description:
	 * @param account
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-5-14
	 */
	public String getChatAttachmentDirectory(Account account) {
		String path = GlobalConstants.APPCATION_SCARD_DIRECTORY
				+ account.getUuid() + "/"+GlobalConstants.CHAT_DIRECTORY
				+ GlobalConstants.ATTACHMENT_DIRECTORY;
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		return path;
	}
	/**
	 * 获取语言环境
	 * 
	 * @Description:
	 * @param account
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-29
	 */
	public String getLanguage() {
        Locale locale = getResources().getConfiguration().locale;
        return locale.toString();
    }
	
	// 创建桌面快捷图标
	// Modified by LL
	// BEGIN
	private void createShortcut() {
        //创建快捷方式的Intent
        Intent shortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        //不允许重复创建
        shortcutIntent.putExtra("duplicate", false);
        //快捷方式名称
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        //快捷图片
        Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(),
        		R.drawable.icon);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        //点击快捷图片，运行的程序主入口
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, 
        		new Intent(getApplicationContext(), WelcomeActivity.class));
        //发送广播
        sendBroadcast(shortcutIntent);
    }
	// END

	public void initImageLoader(Context context) {
		DisplayImageOptions displayOptions = initImageLoaderOptions();
		// This configuration tuning is custom. You can tune every option, you
		// may tune some of them,
		// or you can create default configuration by
		// ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCacheSize(50 * 1024 * 1024)
				// 50 Mb
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				// .writeDebugLogs() // Remove for release app
				 .imageDownloader(new AuthImageDownloader(context,5 * 1000,5 * 1000))
				.defaultDisplayImageOptions(displayOptions).build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
    }

	public DisplayImageOptions initImageLoaderOptions() {
		DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisk(true).showImageOnLoading(R.drawable.bg_img_loading).bitmapConfig(Bitmap.Config.RGB_565).build();
		return options;
    }

	public static void runOnUiThread(final Runnable runnable) {
		new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                runnable.run();
            }

        }.execute((Void[])null);
	}

    public static void toast(final String text) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                Toast.makeText(MailChat.getInstance(), text, Toast.LENGTH_SHORT).show();
            }

        }.execute((Void[])null);
    }
    
    public static String getRootCauseMessage(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        
        String message;
        if (t instanceof MessagingException) {
            message = t.getMessage();
        } else {
            message = t.getLocalizedMessage();
        }
        return (message != null && message.length() > 0)
                ? message : t.getClass().getSimpleName();
    }

    // 是否已登录三五企业邮箱账号
    public static boolean has35Account() {
        for (Account account : Preferences.getPreferences(MailChat.app).getAccounts()) {
            if (account.isHideAccount()) {
                continue;
            }
            int version = account.getVersion_35Mail();
            if (version == 1 || version == 2) {
                return true;
            }
        }
        return false;
    }

	/**
	 * 获取根据附件ID保存的路径.
	 *
	 * @Description:
	 * @param directoryPath 本地保存的文件夹
	 * @param attachmentId 附件ID
	 * @param fileName 文件名称
	 * @return attFilePath 附件保存路径
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-6-19
	 */
	public String getAttFilePath(String directoryPath,String attachmentId,String fileName){
		String attFilePath=null;
		String fileSuffix =AttachmentUtil.getInstance(this).fileSuffix(fileName);
		String directoryAttIdPath = directoryPath+ EncryptUtil.getMd5(attachmentId);
		if(fileSuffix.equals("")){
			int lastIndex = fileName.lastIndexOf(".");
			if(lastIndex!=-1){
				attFilePath= directoryAttIdPath + fileName.substring(lastIndex);
			}else{
				attFilePath= directoryAttIdPath;
			}
		}else{
			attFilePath = directoryAttIdPath+fileSuffix;
		}
		return attFilePath;
	}

	/**
	 * 获取ImageLoader存储路径
	 *
	 * @Description:
	 * @return imageLoaderPath 保存路径
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2016-2-25
	 */
	public String getImageLoaderPath(){
		return ImageLoader.getInstance().getDiskCache().getDirectory().getPath();
	}

	/**
	 * 清空ImageLoader缓存
	 *
	 * @Description:
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2016-2-26
	 */
	public void cleanImageLoaderCache(){
		ImageLoader.getInstance().clearMemoryCache();
		ImageLoader.getInstance().clearDiskCache();
	}
}
