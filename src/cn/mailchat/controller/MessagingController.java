package cn.mailchat.controller;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.paho.android.service.MqttService;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Process;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;
import cn.mailchat.Account;
import cn.mailchat.AccountStats;
import cn.mailchat.GlobalConstants;
import cn.mailchat.MailChat;
import cn.mailchat.MailChat.Intents;
import cn.mailchat.MailChat.NotificationHideSubject;
import cn.mailchat.MailChat.NotificationQuickDelete;
import cn.mailchat.NotificationSetting;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.activity.Accounts;
import cn.mailchat.activity.ChattingSingleActivity;
import cn.mailchat.activity.MailDetialActivity;
import cn.mailchat.activity.MailNotifyPendingActivity;
import cn.mailchat.activity.Main4TabActivity;
import cn.mailchat.activity.MessageReference;
import cn.mailchat.activity.NotificationDeleteConfirmation;
import cn.mailchat.activity.setup.AccountSetupCheckSettings.CheckDirection;
import cn.mailchat.beans.ContentOfPushMessage;
import cn.mailchat.beans.Eis35Bean;
import cn.mailchat.beans.ImapAndSmtpSetting;
import cn.mailchat.cache.EmailProviderCache;
import cn.mailchat.chatting.beans.CAttachment;
import cn.mailchat.chatting.beans.CGroup;
import cn.mailchat.chatting.beans.CGroupMember;
import cn.mailchat.chatting.beans.CMessage;
import cn.mailchat.chatting.beans.CMessage.State;
import cn.mailchat.chatting.beans.CMessage.Type;
import cn.mailchat.chatting.beans.DAttachment;
import cn.mailchat.chatting.beans.DChat;
import cn.mailchat.chatting.beans.DChatMessage;
import cn.mailchat.chatting.beans.MixedChatting;
import cn.mailchat.chatting.beans.PendingHTTPSCommand;
import cn.mailchat.chatting.beans.PendingMQTTConmmand;
import cn.mailchat.chatting.protocol.ActionListener;
import cn.mailchat.chatting.protocol.ActionListener.Action;
import cn.mailchat.chatting.protocol.Connection;
import cn.mailchat.chatting.protocol.CreateJson;
import cn.mailchat.chatting.protocol.DownloadCallback;
import cn.mailchat.chatting.protocol.Downloader;
import cn.mailchat.chatting.protocol.MQTTCommand;
import cn.mailchat.chatting.protocol.MQTTMessageType;
import cn.mailchat.chatting.protocol.MQTTPushMessage;
import cn.mailchat.chatting.protocol.MQTTPushType;
import cn.mailchat.chatting.protocol.ParseJson;
import cn.mailchat.chatting.protocol.Protocol;
import cn.mailchat.chatting.protocol.Response;
import cn.mailchat.chatting.protocol.SuccessfulFailureStateCallBack;
import cn.mailchat.chatting.protocol.UploadCallback;
import cn.mailchat.chatting.protocol.Uploader;
import cn.mailchat.contacts.beans.ContactAttribute;
import cn.mailchat.helper.Contacts;
import cn.mailchat.helper.StringUtils;
import cn.mailchat.helper.TreeHelper;
import cn.mailchat.helper.power.TracingPowerManager;
import cn.mailchat.helper.power.TracingPowerManager.TracingWakeLock;
import cn.mailchat.mail.Address;
import cn.mailchat.mail.AuthenticationFailedException;
import cn.mailchat.mail.Body;
import cn.mailchat.mail.CertificateValidationException;
import cn.mailchat.mail.FetchProfile;
import cn.mailchat.mail.Flag;
import cn.mailchat.mail.Folder;
import cn.mailchat.mail.Folder.FolderType;
import cn.mailchat.mail.MailNotExistException;
import cn.mailchat.mail.Message;
import cn.mailchat.mail.Message.RecipientType;
import cn.mailchat.mail.MessagingException;
import cn.mailchat.mail.Part;
import cn.mailchat.mail.PushReceiver;
import cn.mailchat.mail.Pusher;
import cn.mailchat.mail.Store;
import cn.mailchat.mail.Transport;
import cn.mailchat.mail.internet.MimeHeader;
import cn.mailchat.mail.internet.MimeMessage;
import cn.mailchat.mail.internet.MimeUtility;
import cn.mailchat.mail.internet.TextBody;
import cn.mailchat.mail.store.ImapStore;
import cn.mailchat.mail.store.LocalStore;
import cn.mailchat.mail.store.Columns.TbCAttachments;
import cn.mailchat.mail.store.Columns.TbDAttachments;
import cn.mailchat.mail.store.LocalStore.LocalAttachmentBody;
import cn.mailchat.mail.store.LocalStore.LocalAttachmentBodyPart;
import cn.mailchat.mail.store.LocalStore.LocalFolder;
import cn.mailchat.mail.store.LocalStore.LocalMessage;
import cn.mailchat.mail.store.LocalStore.PendingCommand;
import cn.mailchat.mail.store.LockableDatabase.DbCallback;
import cn.mailchat.mail.store.LockableDatabase.WrappedException;
import cn.mailchat.mail.store.PendingChatCommandLocalStore;
import cn.mailchat.mail.store.Pop3Store;
import cn.mailchat.mail.store.UnavailableAccountException;
import cn.mailchat.mail.store.UnavailableStorageException;
import cn.mailchat.provider.EmailProvider;
import cn.mailchat.provider.EmailProvider.StatsColumns;
import cn.mailchat.search.ConditionsTreeNode;
import cn.mailchat.search.LocalSearch;
import cn.mailchat.search.SearchAccount;
import cn.mailchat.search.SearchResult;
import cn.mailchat.search.SearchSpecification;
import cn.mailchat.search.SqlQueryBuilder;
import cn.mailchat.service.NotificationActionService;
import cn.mailchat.service.PendingService;
import cn.mailchat.utils.ContactComparator;
import cn.mailchat.utils.EncryptUtil;
import cn.mailchat.utils.FileUtil;
import cn.mailchat.utils.ImageUtils;
import cn.mailchat.utils.NetUtil;
import cn.mailchat.utils.RSAUtils;
import cn.mailchat.utils.StringUtil;
import cn.mailchat.utils.SystemUtil;
import cn.mailchat.utils.TimeUtils;
import cn.mailchat.utils.Utility;
import cn.mailchat.utils.WeemailUtil;
import cn.mailchat.view.AttachmentView;

import com.c35.mtd.pushmail.beans.C35Attachment;
import com.c35.mtd.pushmail.beans.C35Message;
import com.c35.mtd.pushmail.store.C35Store;
import com.google.gson.JsonObject;
import com.umeng.analytics.MobclickAgent;



/**
 * Starts a long running (application) Thread that will run through commands
 * that require remote mailbox access. This class is used to serialize and
 * prioritize these commands. Each method that will submit a command requires a
 * MessagingListener instance to be provided. It is expected that that listener
 * has also been added as a registered listener using addListener(). When a
 * command is to be executed, if the listener that was provided with the command
 * is no longer registered the command is skipped. The design idea for the above
 * is that when an Activity starts it registers as a listener. When it is paused
 * it removes itself. Thus, any commands that that activity submitted are
 * removed from the queue once the activity is no longer active.
 */
public class MessagingController implements Runnable, MqttCallback, SuccessfulFailureStateCallBack {
    public static final long INVALID_MESSAGE_ID = -1;

    /**
     * Immutable empty {@link String} array
     */
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Immutable empty {@link Message} array
     */
    private static final Message[] EMPTY_MESSAGE_ARRAY = new Message[0];

    /**
     * Immutable empty {@link Folder} array
     */
    private static final Folder[] EMPTY_FOLDER_ARRAY = new Folder[0];

    /**
     * The maximum message size that we'll consider to be "small". A small message is downloaded
     * in full immediately instead of in pieces. Anything over this size will be downloaded in
     * pieces with attachments being left off completely and downloaded on demand.
     *
     *
     * 25k for a "small" message was picked by educated trial and error.
     * http://answers.google.com/answers/threadview?id=312463 claims that the
     * average size of an email is 59k, which I feel is too large for our
     * blind download. The following tests were performed on a download of
     * 25 random messages.
     * <pre>
     * 5k - 61 seconds,
     * 25k - 51 seconds,
     * 55k - 53 seconds,
     * </pre>
     * So 25k gives good performance and a reasonable data footprint. Sounds good to me.
     */

    private static final String PENDING_COMMAND_MOVE_OR_COPY = "cn.mailchat.MessagingController.moveOrCopy";
    private static final String PENDING_COMMAND_MOVE_OR_COPY_BULK = "cn.mailchat.MessagingController.moveOrCopyBulk";
    private static final String PENDING_COMMAND_MOVE_OR_COPY_BULK_NEW = "cn.mailchat.MessagingController.moveOrCopyBulkNew";
    private static final String PENDING_COMMAND_EMPTY_TRASH = "cn.mailchat.MessagingController.emptyTrash";
    private static final String PENDING_COMMAND_SET_FLAG_BULK = "cn.mailchat.MessagingController.setFlagBulk";
    private static final String PENDING_COMMAND_SET_FLAG = "cn.mailchat.MessagingController.setFlag";
    private static final String PENDING_COMMAND_APPEND = "cn.mailchat.MessagingController.append";
    private static final String PENDING_COMMAND_MARK_ALL_AS_READ = "cn.mailchat.MessagingController.markAllAsRead";
    private static final String PENDING_COMMAND_EXPUNGE = "cn.mailchat.MessagingController.expunge";
    public static class UidReverseComparator implements Comparator<Message> {
        @Override
        public int compare(Message o1, Message o2) {
            if (o1 == null || o2 == null || o1.getUid() == null || o2.getUid() == null) {
                return 0;
            }
            int id1, id2;
            try {
                id1 = Integer.parseInt(o1.getUid());
                id2 = Integer.parseInt(o2.getUid());
            } catch (NumberFormatException e) {
                return 0;
            }
            //reversed intentionally.
            if (id1 < id2)
                return 1;
            if (id1 > id2)
                return -1;
            return 0;
        }
    }

    /**
     * Maximum number of unsynced messages to store at once
     */
    private static final int UNSYNC_CHUNK_SIZE = 5;

    private static MessagingController inst = null;
    private BlockingQueue<Command> mCommands = new PriorityBlockingQueue<Command>();

    private Thread mThread;
    private Set<MessagingListener> mListeners = new CopyOnWriteArraySet<MessagingListener>();

    private final ConcurrentHashMap<String, AtomicInteger> sendCount = new ConcurrentHashMap<String, AtomicInteger>();

    ConcurrentHashMap<Account, Pusher> pushers = new ConcurrentHashMap<Account, Pusher>();

    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    private MessagingListener checkMailListener = null;

    private MemorizingListener memorizingListener = new MemorizingListener();

    private boolean mBusy;

    /**
     *  {@link MailChat}
     */
    private static Application mApplication;
    public Connection connection;
    public static final String NET_CHANGE_ACTION="android.net.conn.CONNECTIVITY_CHANGE";
    public static final String RE_CONNECT="cn.mailchat.ACTION.RECONNECT";
    public static final String CHAT_PENDING ="cn.mailchat.ACTION.PENDING";
    public static final String CHAT_UNSUBSCRIBE_PENDING ="cn.mailchat.ACTION.UNSUBSCRIBE_PENDING";
    public static final String NULL_MQTT_SERVICE ="cn.mailchat.ACTION.NULLMQTT";
    public static final String CHECK_PUSH_STATA="cn.mailchat.ACTION.CHECK_PUSH";
    private static final int MQTT_QOS=2;
    
    private ScheduledExecutorService scheduledExecutorService =Executors.newSingleThreadScheduledExecutor();
    private BlockingQueue<MQTTPushMessage> penddingPushMessage;
    private Thread mqttThread;
    private ConcurrentHashMap<String,Downloader> downloaders=new ConcurrentHashMap<String,Downloader>();
    private ConcurrentHashMap<String,Uploader> uploaders=new ConcurrentHashMap<String,Uploader>();
    /**
     * A holder class for pending notification data
     *
     * This class holds all pieces of information for constructing
     * a notification with message preview.
     */
    private static class NotificationData {
        /** Number of unread messages before constructing the notification */
        int unreadBeforeNotification;
        /**
         * List of messages that should be used for the inbox-style overview.
         * It's sorted from newest to oldest message.
         * Don't modify this list directly, but use {@link addMessage} and
         * {@link removeMatchingMessage} instead.
         */
        LinkedList<Message> messages;
        /**
         * List of references for messages that the user is still to be notified of,
         * but which don't fit into the inbox style anymore. It's sorted from newest
         * to oldest message.
         */
        LinkedList<MessageReference> droppedMessages;

        /**
         * Maximum number of messages to keep for the inbox-style overview.
         * As of Jellybean, phone notifications show a maximum of 5 lines, while tablet
         * notifications show 7 lines. To make sure no lines are silently dropped,
         * we default to 5 lines.
         */
        private final static int MAX_MESSAGES = 5;

        /**
         * Constructs a new data instance.
         *
         * @param unread Number of unread messages prior to instance construction
         */
        public NotificationData(int unread) {
            unreadBeforeNotification = unread;
            droppedMessages = new LinkedList<MessageReference>();
            messages = new LinkedList<Message>();
        }

        /**
         * Adds a new message to the list of pending messages for this notification.
         *
         * The implementation will take care of keeping a meaningful amount of
         * messages in {@link #messages}.
         *
         * @param m The new message to add.
         */
        public void addMessage(Message m) {
            while (messages.size() >= MAX_MESSAGES) {
                Message dropped = messages.removeLast();
                droppedMessages.addFirst(dropped.makeMessageReference());
            }
            messages.addFirst(m);
        }

        /**
         * Remove a certain message from the message list.
         *
         * @param context A context.
         * @param ref Reference of the message to remove
         * @return true if message was found and removed, false otherwise
         */
        public boolean removeMatchingMessage(Context context, MessageReference ref) {
            for (MessageReference dropped : droppedMessages) {
                if (dropped.equals(ref)) {
                    droppedMessages.remove(dropped);
                    return true;
                }
            }

            for (Message message : messages) {
                if (message.makeMessageReference().equals(ref)) {
                    if (messages.remove(message) && !droppedMessages.isEmpty()) {
                        Message restoredMessage = droppedMessages.getFirst().restoreToLocalMessage(context);
                        if (restoredMessage != null) {
                            messages.addLast(restoredMessage);
                            droppedMessages.removeFirst();
                        }
                    }
                    return true;
                }
            }

            return false;
        }

        /**
         * Gets a list of references for all pending messages for the notification.
         *
         * @return Message reference list
         */
        public ArrayList<MessageReference> getAllMessageRefs() {
            ArrayList<MessageReference> refs = new ArrayList<MessageReference>();
            for (Message m : messages) {
                refs.add(m.makeMessageReference());
            }
            refs.addAll(droppedMessages);
            return refs;
        }

        /**
         * Gets the total number of messages the user is to be notified of.
         *
         * @return Amount of new messages the notification notifies for
         */
        public int getNewMessageCount() {
            return messages.size() + droppedMessages.size();
        }
    };
    
   

    // Key is accountNumber
    private ConcurrentHashMap<Integer, NotificationData> notificationData = new ConcurrentHashMap<Integer, NotificationData>();
    //新增联系人缓存
    private List<ContactAttribute> tempContactList =new ArrayList<ContactAttribute>();
    //eis联系人缓存
	private Map<String, List<ContactAttribute>> tempEisContactsMap  = new HashMap<String, List<ContactAttribute>>();
    //个人通讯录联系人缓存
	private Map<String, List<ContactAttribute>> tempPersionContactsMap  = new HashMap<String, List<ContactAttribute>>();
    //企业通讯录联系人缓存
	private Map<String, List<ContactAttribute>> tempSameContactsMap  = new HashMap<String, List<ContactAttribute>>();
	//邀请联系人缓存
	private Map<String, List<ContactAttribute>> tempSearchEisContactsMap  = new HashMap<String, List<ContactAttribute>>();
	private Map<String, List<ContactAttribute>> tempSearchPerssionContactsMap  = new HashMap<String, List<ContactAttribute>>();
	private Map<String, List<ContactAttribute>> tempSearchSameContactsMap  = new HashMap<String, List<ContactAttribute>>();
    private static final Flag[] SYNC_FLAGS = new Flag[] { Flag.SEEN, Flag.FLAGGED, Flag.ANSWERED, Flag.FORWARDED };


    private void suppressMessages(Account account, List<Message> messages) {
        EmailProviderCache cache = EmailProviderCache.getCache(account.getUuid(),
                mApplication.getApplicationContext());
        cache.hideMessages(messages);
    }

    private void unsuppressMessages(Account account, Message[] messages) {
        EmailProviderCache cache = EmailProviderCache.getCache(account.getUuid(),
                mApplication.getApplicationContext());
        cache.unhideMessages(messages);
    }

    private boolean isMessageSuppressed(Account account, Message message) {
        LocalMessage localMessage = (LocalMessage) message;
        String accountUuid = account.getUuid();
        long messageId = localMessage.getId();
        long folderId = ((LocalFolder) localMessage.getFolder()).getId();

        EmailProviderCache cache = EmailProviderCache.getCache(accountUuid,
                mApplication.getApplicationContext());
        return cache.isMessageHidden(messageId, folderId);
    }

    private void setFlagInCache(final Account account, final List<Long> messageIds,
            final Flag flag, final boolean newState) {

        EmailProviderCache cache = EmailProviderCache.getCache(account.getUuid(),
                mApplication.getApplicationContext());
        String columnName = LocalStore.getColumnNameForFlag(flag);
        String value = Integer.toString((newState) ? 1 : 0);
        cache.setValueForMessages(messageIds, columnName, value);
    }

    private void removeFlagFromCache(final Account account, final List<Long> messageIds,
            final Flag flag) {

        EmailProviderCache cache = EmailProviderCache.getCache(account.getUuid(),
                mApplication.getApplicationContext());
        String columnName = LocalStore.getColumnNameForFlag(flag);
        cache.removeValueForMessages(messageIds, columnName);
    }

    private void setFlagForThreadsInCache(final Account account, final List<Long> threadRootIds,
            final Flag flag, final boolean newState) {

        EmailProviderCache cache = EmailProviderCache.getCache(account.getUuid(),
                mApplication.getApplicationContext());
        String columnName = LocalStore.getColumnNameForFlag(flag);
        String value = Integer.toString((newState) ? 1 : 0);
        cache.setValueForThreads(threadRootIds, columnName, value);
    }

    private void removeFlagForThreadsFromCache(final Account account, final List<Long> messageIds,
            final Flag flag) {

        EmailProviderCache cache = EmailProviderCache.getCache(account.getUuid(),
                mApplication.getApplicationContext());
        String columnName = LocalStore.getColumnNameForFlag(flag);
        cache.removeValueForThreads(messageIds, columnName);
    }


    /**
     * @param application  {@link MailChat}
     */
    private MessagingController(Application application) {
        mApplication = application;
        mThread = new Thread(this);
        mThread.setName("MessagingController");
        mThread.start();
        if (memorizingListener != null) {
            addListener(memorizingListener);
        }
        MQTTConnect(false);
        penddingPushMessage=new LinkedBlockingDeque<MQTTPushMessage>();
        new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					try {
						MQTTPushMessage pushMessage= penddingPushMessage.take();
						saveAndParseMQTTMessage(pushMessage.topic,pushMessage.message);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
        checkAllRegisterPush();
    }
	/**
	 * MQTT连接
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-11-12
	 */
    public void  MQTTConnect(boolean isCleanSession){
    	connection=Connection.getInstance(mApplication);
        connection.getClient().setCallback(this);
        try {
        	connection.addConnectionOptions(readSSL(isCleanSession));
   			connection.getClient().connect(readSSL(isCleanSession), null, new ActionListener(ActionListener.Action.CONNECT,this,null,MQTTCommand.FIRST_CONNECT,null));//TODO：后期修改
   		} catch (MqttException | NoSuchAlgorithmException | KeyStoreException | CertificateException | NotFoundException | IOException | KeyManagementException e) {
   			// TODO Auto-generated catch block
   			e.printStackTrace();
   		}	
    }
    /**
	 * 移除断开当前MQTT连接
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2016-3-11
	 */
    public void disMQTTConnect(){
   		try {
			connection.getClient().disconnect();
		} catch (MqttException | IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    /**
     * Gets or creates the singleton instance of MessagingController. Application is used to
     * provide a Context to classes that need it.
     * @param application {@link MailChat}
     * @return
     */
    public synchronized static MessagingController getInstance(Application application) {
        if (inst == null) {
            inst = new MessagingController(application);
        }
        return inst;
    }

    public boolean isBusy() {
        return mBusy;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (true) {
            String commandDescription = null;
            try {
                final Command command = mCommands.take();

                if (command != null) {
                    commandDescription = command.description;

                    if (MailChat.DEBUG)
                        Log.i(MailChat.LOG_TAG, "Running " + (command.isForeground ? "Foreground" : "Background") + " command '" + command.description + "', seq = " + command.sequence);

                    mBusy = true;
                    try {
                        command.runnable.run();
                    } catch (UnavailableAccountException e) {
                        // retry later
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    sleep(30 * 1000);
                                    mCommands.put(command);
                                } catch (InterruptedException e) {
                                    Log.e(MailChat.LOG_TAG, "interrupted while putting a pending command for"
                                          + " an unavailable account back into the queue."
                                          + " THIS SHOULD NEVER HAPPEN.");
                                }
                            }
                        } .start();
                    }

                    if (MailChat.DEBUG)
                        Log.i(MailChat.LOG_TAG, (command.isForeground ? "Foreground" : "Background") +
                              " Command '" + command.description + "' completed");

                    for (MessagingListener l : getListeners(command.listener)) {
                        l.controllerCommandCompleted(!mCommands.isEmpty());
                    }
                }
            } catch (Exception e) {
                Log.e(MailChat.LOG_TAG, "Error running command '" + commandDescription + "'", e);
            }
            mBusy = false;
        }
    }

    private void put(String description, MessagingListener listener, Runnable runnable) {
        putCommand(mCommands, description, listener, runnable, true);
    }

    private void putBackground(String description, MessagingListener listener, Runnable runnable) {
        putCommand(mCommands, description, listener, runnable, false);
    }

    private void putCommand(BlockingQueue<Command> queue, String description, MessagingListener listener, Runnable runnable, boolean isForeground) {
        int retries = 10;
        Exception e = null;
        while (retries-- > 0) {
            try {
                Command command = new Command();
                command.listener = listener;
                command.runnable = runnable;
                command.description = description;
                command.isForeground = isForeground;
                queue.put(command);
                return;
            } catch (InterruptedException ie) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ne) {
                }
                e = ie;
            }
        }
        throw new Error(e);
    }


    public void addListener(MessagingListener listener) {
        mListeners.add(listener);
        refreshListener(listener);
    }

    public void refreshListener(MessagingListener listener) {
        if (memorizingListener != null && listener != null) {
            memorizingListener.refreshOther(listener);
        }
    }

    public void removeListener(MessagingListener listener) {
        mListeners.remove(listener);
    }

    public Set<MessagingListener> getListeners() {
        return mListeners;
    }


    public Set<MessagingListener> getListeners(MessagingListener listener) {
        if (listener == null) {
            return mListeners;
        }

        Set<MessagingListener> listeners = new HashSet<MessagingListener>(mListeners);
        listeners.add(listener);
        return listeners;

    }


    /**
     * Lists folders that are available locally and remotely. This method calls
     * listFoldersCallback for local folders before it returns, and then for
     * remote folders at some later point. If there are no local folders
     * includeRemote is forced by this method. This method should be called from
     * a Thread as it may take several seconds to list the local folders.
     * TODO this needs to cache the remote folder list
     *
     * @param account
     * @param listener
     * @throws MessagingException
     */
    public void listFolders(final Account account, final boolean refreshRemote, final MessagingListener listener) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                listFoldersSynchronous(account, refreshRemote, listener);
            }
        });
    }

    /**
     * Lists folders that are available locally and remotely. This method calls
     * listFoldersCallback for local folders before it returns, and then for
     * remote folders at some later point. If there are no local folders
     * includeRemote is forced by this method. This method is called in the
     * foreground.
     * TODO this needs to cache the remote folder list
     *
     * @param account
     * @param listener
     * @throws MessagingException
     */
    public void listFoldersSynchronous(final Account account, final boolean refreshRemote, final MessagingListener listener) {
        for (MessagingListener l : getListeners(listener)) {
            l.listFoldersStarted(account);
        }
        List <? extends Folder > localFolders = null;
        if (!account.isAvailable(mApplication)) {
            Log.i(MailChat.LOG_TAG, "not listing folders of unavailable account");
        } else {
            try {
                Store localStore = account.getLocalStore();
                localFolders = localStore.getPersonalNamespaces(false);
                Folder[] folderArray = localFolders.toArray(EMPTY_FOLDER_ARRAY);

                if (refreshRemote || localFolders.isEmpty()) {
                    doRefreshRemote(account, listener);
                    return;
                }

                for (MessagingListener l : getListeners(listener)) {
                    l.listFolders(account, folderArray);
                }
            } catch (Exception e) {
                for (MessagingListener l : getListeners(listener)) {
                    l.listFoldersFailed(account, e.getMessage());
                }

                addErrorMessage(account, null, e);
                return;
            } finally {
                if (localFolders != null) {
                    for (Folder localFolder : localFolders) {
                        closeFolder(localFolder);
                    }
                }
            }
        }

        for (MessagingListener l : getListeners(listener)) {
            l.listFoldersFinished(account);
        }
    }
    private void doRefreshRemote(final Account account, final MessagingListener listener) {
        put("doRefreshRemote", listener, new Runnable() {
            @Override
            public void run() {
                List <? extends Folder > localFolders = null;
                try {
					// #############验证下是否是35域#################
					int mailVersion = get35MailVersionForFolder(account, true);
					Store store = account.getRemoteStore();
					List<? extends Folder> remoteFolders = store
							.getPersonalNamespaces(false);

                    LocalStore localStore = account.getLocalStore();
                    Set<String> remoteFolderNames = new HashSet<String>();
                    List<LocalFolder> foldersToCreate = new LinkedList<LocalFolder>();

                    localFolders = localStore.getPersonalNamespaces(false);
                    Set<String> localFolderNames = new HashSet<String>();
                    for (Folder localFolder : localFolders) {
                        localFolderNames.add(localFolder.getName());
                    }
					for (Folder remoteFolder : remoteFolders) {
						String rFolderName = remoteFolder.getName();
						if (!TextUtils.isEmpty(rFolderName)) {
							if (rFolderName.contains("INBOX.")) {
								rFolderName = rFolderName.substring(rFolderName
										.lastIndexOf(".") + 1).trim();
							}
						}
						// 如果不是系统邮件夹
						if (!rFolderName.equals("Drafts")
								&& !rFolderName.equals("Draft")
								&& !rFolderName.equals("Sent")
								&& !rFolderName.equals("Spam")
								&& !rFolderName.equals("Trash")
								&& !rFolderName.equals("Sent Messages")
								&& !rFolderName.equals("Junk")
								&& !rFolderName.equals("Deleted Messages")) {
							// 并且服务端邮件夹没有在本地邮件夹
							if (localFolderNames.contains(rFolderName) == false) {
								LocalFolder localFolder = localStore
										.getFolder(rFolderName);
								foldersToCreate.add(localFolder);
							}
							remoteFolderNames.add(rFolderName);
						} else {
							if (rFolderName.equals("Drafts")
									|| rFolderName.equals("Draft")) {
								account.setDraftsFolderName(rFolderName);
							} else if (rFolderName.equals("Sent")
									|| rFolderName.equals("Sent Messages")) {
								account.setSentFolderName(rFolderName);
							} else if (rFolderName.equals("Spam")
									|| rFolderName.equals("Junk")) {
								account.setSpamFolderName(rFolderName);
							} else if (rFolderName.equals("Trash")
									|| rFolderName.equals("Deleted Messages")) {
								account.setTrashFolderName(rFolderName);
							}
						}
					}
                    localStore.createFolders(foldersToCreate, account.getDisplayCount(),mailVersion);
                    localFolders = localStore.getPersonalNamespaces(false);
                    /*
                     * Clear out any folders that are no longer on the remote store.
                     */
                    for (Folder localFolder : localFolders) {
                        String localFolderName = localFolder.getName();
                        // FIXME: This is a hack used to clean up when we accidentally created the
                        //        special placeholder folder "-NONE-".
                        if (MailChat.FOLDER_NONE.equals(localFolderName)) {
                            localFolder.delete(false);
                        }

                        if (!account.isSpecialFolder(localFolderName) &&
                                !remoteFolderNames.contains(localFolderName)) {
                            localFolder.delete(false);
                        }
                    }

                    localFolders = localStore.getPersonalNamespaces(false);
                                
                    Folder[] folderArray = localFolders.toArray(EMPTY_FOLDER_ARRAY);

                    for (MessagingListener l : getListeners(listener)) {
                        l.listFolders(account, folderArray);
                    }
                    for (MessagingListener l : getListeners(listener)) {
                        l.listFoldersFinished(account);
                    }
                } catch (Exception e) {
                    for (MessagingListener l : getListeners(listener)) {
                        l.listFoldersFailed(account, "");
                    }
                    addErrorMessage(account, null, e);
                } finally {
                    if (localFolders != null) {
                        for (Folder localFolder : localFolders) {
                            closeFolder(localFolder);
                        }
                    }
                }
            }
        });
    }

    /**
     * Find all messages in any local account which match the query 'query'
     * @throws MessagingException
     */
    public void searchLocalMessages(final LocalSearch search, final MessagingListener listener) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                searchLocalMessagesSynchronous(search, listener);
            }
        });
    }

    public void searchLocalMessagesSynchronous(final LocalSearch search, final MessagingListener listener) {
        final AccountStats stats = new AccountStats();
        final Set<String> uuidSet = new HashSet<String>(Arrays.asList(search.getAccountUuids()));
        List<Account> accounts = Preferences.getPreferences(mApplication.getApplicationContext()).getAccounts();
        boolean allAccounts = uuidSet.contains(SearchSpecification.ALL_ACCOUNTS);

        // for every account we want to search do the query in the localstore
        for (final Account account : accounts) {

            if (!allAccounts && !uuidSet.contains(account.getUuid())) {
                continue;
            }

            // Collecting statistics of the search result
            MessageRetrievalListener retrievalListener = new MessageRetrievalListener() {
                @Override
                public void messageStarted(String message, int number, int ofTotal) {}
                @Override
                public void messagesFinished(int number) {}
                @Override
                public void messageFinished(Message message, int number, int ofTotal) {
                    if (!isMessageSuppressed(message.getFolder().getAccount(), message)) {
                        List<Message> messages = new ArrayList<Message>();

                        messages.add(message);
                        stats.unreadMessageCount += (!message.isSet(Flag.SEEN)) ? 1 : 0;
                        stats.flaggedMessageCount += (message.isSet(Flag.FLAGGED)) ? 1 : 0;
                        if (listener != null) {
                            listener.listLocalMessagesAddMessages(account, null, messages);
                        }
                    }
                }
            };

            // alert everyone the search has started
            if (listener != null) {
                listener.listLocalMessagesStarted(account, null);
            }

            // build and do the query in the localstore
            try {
                LocalStore localStore = account.getLocalStore();
                localStore.searchForMessages(retrievalListener, search);
            } catch (Exception e) {
                if (listener != null) {
                    listener.listLocalMessagesFailed(account, null, e.getMessage());
                }
                addErrorMessage(account, null, e);
            } finally {
                if (listener != null) {
                    listener.listLocalMessagesFinished(account, null);
                }
            }
        }

        // publish the total search statistics
        if (listener != null) {
            listener.searchStats(stats);
        }
    }



    public Future<?> searchRemoteMessages(final String acctUuid, final String folderName, final String query,
            final Flag[] requiredFlags, final Flag[] forbiddenFlags, final MessagingListener listener) {
        if (MailChat.DEBUG) {
            String msg = "searchRemoteMessages ("
                         + "acct=" + acctUuid
                         + ", folderName = " + folderName
                         + ", query = " + query
                         + ")";
            Log.i(MailChat.LOG_TAG, msg);
        }

        return threadPool.submit(new Runnable() {
            @Override
            public void run() {
                searchRemoteMessagesSynchronous(acctUuid, folderName, query, requiredFlags, forbiddenFlags, listener);
            }
        });
    }
    public void searchRemoteMessagesSynchronous(final String acctUuid, final String folderName, final String query,
            final Flag[] requiredFlags, final Flag[] forbiddenFlags, final MessagingListener listener) {
        final Account acct = Preferences.getPreferences(mApplication.getApplicationContext()).getAccount(acctUuid);

        if (listener != null) {
            listener.remoteSearchStarted(acct, folderName);
        }

        List<Message> extraResults = new ArrayList<Message>();
        try {
            Store remoteStore = acct.getRemoteStore();
            LocalStore localStore = acct.getLocalStore();

            if (remoteStore == null || localStore == null) {
                throw new MessagingException("Could not get store");
            }

            Folder remoteFolder = remoteStore.getFolder(folderName);
            LocalFolder localFolder = localStore.getFolder(folderName);
            if (remoteFolder == null || localFolder == null) {
                throw new MessagingException("Folder not found");
            }

            List<Message> messages = remoteFolder.search(query, requiredFlags, forbiddenFlags);

            if (MailChat.DEBUG) {
                Log.i("Remote Search", "Remote search got " + messages.size() + " results");
            }
            if (MailChat.remoteSearchPaging) {
                if (messages.size() < MailChat.remoteSearchSize) {
                    MailChat.remoteSearchPaging = false;
                } else {
                    MailChat.remoteSearchStart += MailChat.remoteSearchSize;
                }
            }

            // There's no need to fetch messages already completely downloaded
            List<Message> remoteMessages = localFolder.extractNewMessages(messages);
            messages.clear();

            if (listener != null) {
                listener.remoteSearchServerQueryComplete(acct, folderName, remoteMessages.size());
            }

            Collections.sort(remoteMessages, new UidReverseComparator());

            int resultLimit = acct.getRemoteSearchNumResults();
            if (resultLimit > 0 && remoteMessages.size() > resultLimit) {
                extraResults = remoteMessages.subList(resultLimit, remoteMessages.size());
                remoteMessages = remoteMessages.subList(0, resultLimit);
            }
            
            List<SearchResult> results = new ArrayList<SearchResult>();
            for (Message msg : remoteMessages) {
                results.add(new SearchResult(msg.getUid(), SearchResult.Type.REMOTE));
            }

            loadSearchResultsSynchronous(results, localFolder, remoteFolder, listener);


        } catch (Exception e) {
            if (Thread.currentThread().isInterrupted()) {
                Log.i(MailChat.LOG_TAG, "Caught exception on aborted remote search; safe to ignore.", e);
            } else {
                Log.e(MailChat.LOG_TAG, "Could not complete remote search", e);
                if (listener != null) {
                    listener.remoteSearchFailed(acct, null, e.getMessage());
                }
                addErrorMessage(acct, null, e);
            }
        } finally {
            if (listener != null) {
                listener.remoteSearchFinished(acct, folderName, 0, extraResults);
            }
        }

    }

    public void loadSearchResults(final Account account, final String folderName, final List<SearchResult> messages, final MessagingListener listener) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.enableProgressIndicator(true);
                }
                try {
                    Store remoteStore = account.getRemoteStore();
                    LocalStore localStore = account.getLocalStore();

                    if (remoteStore == null || localStore == null) {
                        throw new MessagingException("Could not get store");
                    }

                    Folder remoteFolder = remoteStore.getFolder(folderName);
                    LocalFolder localFolder = localStore.getFolder(folderName);
                    if (remoteFolder == null || localFolder == null) {
                        throw new MessagingException("Folder not found");
                    }
                    
                    remoteFolder.open(Folder.OPEN_MODE_RO);

                    loadSearchResultsSynchronous(messages, localFolder, remoteFolder, listener);
                } catch (MessagingException e) {
                    Log.e(MailChat.LOG_TAG, "Exception in loadSearchResults: " + e);
                    addErrorMessage(account, null, e);
                } finally {
                    if (listener != null) {
                        listener.enableProgressIndicator(false);
                    }
                }
            }
        });
    }

    public void loadSearchResultsSynchronous(List<SearchResult> messages, final LocalFolder localFolder, final Folder remoteFolder, final MessagingListener listener) throws MessagingException {
        final FetchProfile header = new FetchProfile();
        header.add(FetchProfile.Item.FLAGS);
        header.add(FetchProfile.Item.ENVELOPE);
        final FetchProfile structure = new FetchProfile();
        structure.add(FetchProfile.Item.STRUCTURE);
        
        
        
        final Message[] messageArray = new Message[messages.size()];
        for (int j = 0; j < messages.size(); j++) {
            messageArray[j] = remoteFolder.getMessage(messages.get(j).getUid());
        }
        
        remoteFolder.fetch(messageArray, header, new MessageRetrievalListener() {

            @Override
            public void messageStarted(String uid, int number, int ofTotal) {
                
            }

            @Override
            public void messageFinished(Message message, int number, int ofTotal) {
                try {
                    if (number == ofTotal) {
                        
                        localFolder.appendMessages(messageArray);
                        
                        for (int i = 0; i < messageArray.length; i++) {
                            Message msg = messageArray[i];
                            LocalMessage localMsg = localFolder.getMessage(msg.getUid());
                            if (listener != null) {
                                listener.remoteSearchAddMessage(remoteFolder.getAccount(), remoteFolder.getName(), localMsg, i, messageArray.length);
                            }
                        }
                        
                    }
                } catch (MessagingException e) {
                    // DO NOTHING
                }
            }

            @Override
            public void messagesFinished(int total) {
                
            }
            
        });



        int i = 0;
        for (SearchResult result : messages) {
            i++;
            Message message = remoteFolder.getMessage(result.getUid());
            LocalMessage localMsg = localFolder.getMessage(message.getUid());

            remoteFolder.fetch(new Message [] {message}, header, null);
            //fun fact: ImapFolder.fetch can't handle getting STRUCTURE at same time as headers
            remoteFolder.fetch(new Message [] {message}, structure, null);

            Set<Part> viewables = MimeUtility.collectTextParts(message);
            for (Part part : viewables) {
                remoteFolder.fetchPart(message, part, null);
            }

            message.setFlag(Flag.X_DOWNLOADED_PARTIAL, true);

            localFolder.appendMessages(new Message [] {message});
            localMsg = localFolder.getMessage(message.getUid());

            if (listener != null) {
                listener.remoteSearchAddMessage(remoteFolder.getAccount(), remoteFolder.getName(), localMsg, i, messages.size());
            }
        }

        
        
        // 加载完搜索结果关闭文件夹
        // Modified by LL
        // BEGIN
        if (remoteFolder != null) {
            remoteFolder.close();
        }
        // END
    }


    public void loadMoreMessages(Account account, String folder, MessagingListener listener) {
        try {
            LocalStore localStore = account.getLocalStore();
            LocalFolder localFolder = localStore.getFolder(folder);
            if (localFolder.getVisibleLimit() > 0) {
                localFolder.setVisibleLimit(localFolder.getVisibleLimit() + account.getDisplayCount());
            }
            synchronizeMailbox(account, folder, listener, null);
        } catch (MessagingException me) {
            addErrorMessage(account, null, me);

            throw new RuntimeException("Unable to set visible limit on folder", me);
        }
    }

    public void resetVisibleLimits(Collection<Account> accounts) {
        for (Account account : accounts) {
            account.resetVisibleLimits();
        }
    }

    /**
     * Start background synchronization of the specified folder.
     * @param account
     * @param folder
     * @param listener
     * @param providedRemoteFolder TODO
     */
    public void synchronizeMailbox(final Account account, final String folder, final MessagingListener listener, final Folder providedRemoteFolder) {
        putBackground("synchronizeMailbox", listener, new Runnable() {
            @Override
            public void run() {
                synchronizeMailboxSynchronous(account, folder, listener, providedRemoteFolder);
            }
        });
    }

    /**
     * Start foreground synchronization of the specified folder. This is generally only called
     * by synchronizeMailbox.
     * @param account
     * @param folder
     *
     * TODO Break this method up into smaller chunks.
     * @param providedRemoteFolder TODO
     */
    private void synchronizeMailboxSynchronous(final Account account, final String folder, final MessagingListener listener, Folder providedRemoteFolder) {
        Folder remoteFolder = null;
        LocalFolder tLocalFolder = null;
        if (MailChat.DEBUG)
            Log.i(MailChat.LOG_TAG, "Synchronizing folder " + account.getDescription() + ":" + folder);

        for (MessagingListener l : getListeners(listener)) {
            l.synchronizeMailboxStarted(account, folder);
        }
        /*
         * We don't ever sync the Outbox or errors folder
         */
        if (folder.equals(account.getOutboxFolderName()) || folder.equals(account.getErrorFolderName())) {
            for (MessagingListener l : getListeners(listener)) {
                l.synchronizeMailboxFinished(account, folder, 0, 0);
            }

            return;
        }

        Exception commandException = null;
        try {
            if (MailChat.DEBUG)
                Log.d(MailChat.LOG_TAG, "SYNC: About to process pending commands for account " + account.getDescription());
            
            /*
            try {
                processPendingCommandsSynchronous(account);
            } catch (Exception e) {
                addErrorMessage(account, null, e);

                Log.e(MailChat.LOG_TAG, "Failure processing command, but allow message sync attempt", e);
                commandException = e;
            }
            */
            // 遗留命令未处理完毕则放弃文件夹同步操作
            // Modified by LL
            processPendingCommandsSynchronous(account);

            /*
             * Get the message list from the local store and create an index of
             * the uids within the list.
             */
            if (MailChat.DEBUG)
                Log.v(MailChat.LOG_TAG, "SYNC: About to get local folder " + folder);

            final LocalStore localStore = account.getLocalStore();
            tLocalFolder = localStore.getFolder(folder);
            final LocalFolder localFolder = tLocalFolder;
            localFolder.open(Folder.OPEN_MODE_RW);
            localFolder.updateLastUid();
            Message[] localMessages = localFolder.getMessages(null);
            
            // Create an index of Message-IDs and X-MailChat-Message-IDs
            // for messages with local uid.
            // Modified by LL
            // BEGIN
            HashMap<String, Message> localMessageIDMap = new HashMap<String, Message>();
            HashMap<String, Message> localMailChatMessageIDMap = new HashMap<String, Message>();
            
            boolean isLocalFolder = prepareSyncMaps(account,
            		localFolder,
            		localMessages,
            		localMessageIDMap,
            		localMailChatMessageIDMap);
            // END
            
            HashMap<String, Message> localUidMap = new HashMap<String, Message>();
            for (Message message : localMessages) {
                localUidMap.put(message.getUid(), message);
            }

            if (providedRemoteFolder != null) {
                if (MailChat.DEBUG)
                    Log.v(MailChat.LOG_TAG, "SYNC: using providedRemoteFolder " + folder);
                remoteFolder = providedRemoteFolder;
            } else {
                Store remoteStore = account.getRemoteStore();

                if (MailChat.DEBUG)
                    Log.v(MailChat.LOG_TAG, "SYNC: About to get remote folder " + folder);
                remoteFolder = remoteStore.getFolder(folder);

                if (! verifyOrCreateRemoteSpecialFolder(account, folder, remoteFolder, listener)) {
                    return;
                }


                /*
                 * Synchronization process:
                 *
                Open the folder
                Upload any local messages that are marked as PENDING_UPLOAD (Drafts, Sent, Trash)
                Get the message count
                Get the list of the newest MailChat.DEFAULT_VISIBLE_LIMIT messages
                getMessages(messageCount - MailChat.DEFAULT_VISIBLE_LIMIT, messageCount)
                See if we have each message locally, if not fetch it's flags and envelope
                Get and update the unread count for the folder
                Update the remote flags of any messages we have locally with an internal date newer than the remote message.
                Get the current flags for any messages we have locally but did not just download
                Update local flags
                For any message we have locally but not remotely, delete the local message to keep cache clean.
                Download larger parts of any new messages.
                (Optional) Download small attachments in the background.
                 */

                /*
                 * Open the remote folder. This pre-loads certain metadata like message count.
                 */
                if (MailChat.DEBUG)
                    Log.v(MailChat.LOG_TAG, "SYNC: About to open remote folder " + folder);

                remoteFolder.open(Folder.OPEN_MODE_RW);
                if (Account.EXPUNGE_ON_POLL.equals(account.getExpungePolicy())) {
                    if (MailChat.DEBUG)
                        Log.d(MailChat.LOG_TAG, "SYNC: Expunging folder " + account.getDescription() + ":" + folder);
                    remoteFolder.expunge();
                }

            }

            /*
             * Get the remote message count.
             */
            int remoteMessageCount = remoteFolder.getMessageCount();

            int visibleLimit = localFolder.getVisibleLimit();

            if (visibleLimit < 0) {
                visibleLimit = MailChat.DEFAULT_VISIBLE_LIMIT;
            }

            Message[] remoteMessageArray = EMPTY_MESSAGE_ARRAY;
            final ArrayList<Message> remoteMessages = new ArrayList<Message>();
            HashMap<String, Message> remoteUidMap = new HashMap<String, Message>();

            if (MailChat.DEBUG)
                Log.v(MailChat.LOG_TAG, "SYNC: Remote message count for folder " + folder + " is " + remoteMessageCount);
            final Date earliestDate = account.getEarliestPollDate();


            if (remoteMessageCount > 0) {
                /* Message numbers start at 1.  */
                int remoteStart;
                if (visibleLimit > 0) {
                    remoteStart = Math.max(0, remoteMessageCount - visibleLimit) + 1;
                } else {
                    remoteStart = 1;
                }
                int remoteEnd = remoteMessageCount;

                if (MailChat.DEBUG)
                    Log.v(MailChat.LOG_TAG, "SYNC: About to get messages " + remoteStart + " through " + remoteEnd + " for folder " + folder);

                final AtomicInteger headerProgress = new AtomicInteger(0);
                for (MessagingListener l : getListeners(listener)) {
                    l.synchronizeMailboxHeadersStarted(account, folder);
                }


                remoteMessageArray = remoteFolder.getMessages(remoteStart, remoteEnd, earliestDate, null);

                int messageCount = remoteMessageArray.length;

                for (Message thisMess : remoteMessageArray) {
                    headerProgress.incrementAndGet();
                    for (MessagingListener l : getListeners(listener)) {
                        l.synchronizeMailboxHeadersProgress(account, folder, headerProgress.get(), messageCount);
                    }
                    Message localMessage = localUidMap.get(thisMess.getUid());
                    if (localMessage == null || !localMessage.olderThan(earliestDate)) {
                        remoteMessages.add(thisMess);
                        remoteUidMap.put(thisMess.getUid(), thisMess);
                    }
                }
                if (MailChat.DEBUG)
                    Log.v(MailChat.LOG_TAG, "SYNC: Got " + remoteUidMap.size() + " messages for folder " + folder);

                remoteMessageArray = null;
                for (MessagingListener l : getListeners(listener)) {
                    l.synchronizeMailboxHeadersFinished(account, folder, headerProgress.get(), remoteUidMap.size());
                }

            } else if (remoteMessageCount < 0) {
                throw new Exception("Message count " + remoteMessageCount + " for folder " + folder);
            }
            
            /*
            // Remove any messages that are in the local store but no longer on the remote store or are too old
            if (account.syncRemoteDeletions()) {
                ArrayList<Message> destroyMessages = new ArrayList<Message>();
                for (Message localMessage : localMessages) {
                    if (remoteUidMap.get(localMessage.getUid()) == null) {
                        destroyMessages.add(localMessage);
                    }
                }


                localFolder.destroyMessages(destroyMessages.toArray(EMPTY_MESSAGE_ARRAY));

                for (Message destroyMessage : destroyMessages) {
                    for (MessagingListener l : getListeners(listener)) {
                        l.synchronizeMailboxRemovedMessage(account, folder, destroyMessage);
                    }
                }
            }
            localMessages = null;

            // Now we download the actual content of messages.
            int newMessages = downloadMessages(account, remoteFolder, localFolder, remoteMessages, false);
            */
            // Delete repeated messages in sent/drafts/trash folder
            // Modified by LL
            // BEGIN
            
            // Now we download the actual content of messages.
            int newMessages = downloadMessages(account,
            		remoteFolder,
            		localFolder,
            		remoteMessages,
            		false,
            		localMessageIDMap,
            		localMailChatMessageIDMap);
            
            // Remove any messages that are in the local store but no longer on the remote store or are too old
            if (account.syncRemoteDeletions()) {
                ArrayList<Message> destroyMessages = new ArrayList<Message>();
                
                // 删除本地/远程重复邮件改到下载过程中，以避免短暂出现重复邮件
                // Modified by LL
                /*
                if (isLocalFolder) {
	                Message[] newLocalMessages = localFolder.getMessages(null);
	                for (Message message : newLocalMessages) {
	                	String messageID = getMessageID(message);
	                	if ((messageID != null) && (!isLocalUid(message.getUid()))) {
	                		Message msg = localMessageIDMap.get(messageID);
	                		if (msg != null) {
	                			//message.setBody(msg.getBody());
	                			destroyMessages.add(msg);
	                			localMessageIDMap.remove(messageID);
	                			continue;
	                		}
	                	}
	                	
	                	String mailChatMessageId = MimeUtility.getFirstHeader(message,
	                			MailChat.MAILCHAT_MESSAGE_ID_HEADER);
	                	if ((mailChatMessageId != null) && (!isLocalUid(message.getUid()))) {
	                		Message msg = localMailChatMessageIdMap.get(mailChatMessageId);
	                		if (msg != null) {
	                			//message.setBody(msg.getBody());
	                			destroyMessages.add(msg);
	                			localMailChatMessageIdMap.remove(mailChatMessageId);
	                		}
	                	}
	                }
	                newLocalMessages = null;
                }
                */
                
                for (Message localMessage : localMessages) {
                	String uid = localMessage.getUid();
                    if (remoteUidMap.get(uid) == null) {
                    	if (!(isLocalFolder && MimeUtility.isLocalUid(uid))) {
                    		destroyMessages.add(localMessage);
                    	}
                    }
                }

                localFolder.destroyMessages(destroyMessages.toArray(EMPTY_MESSAGE_ARRAY));

                for (Message destroyMessage : destroyMessages) {
                    for (MessagingListener l : getListeners(listener)) {
                        l.synchronizeMailboxRemovedMessage(account, folder, destroyMessage);
                    }
                }
            }
            localMessages = null;
            
            if (isLocalFolder) {
            	processPendingCommandsSynchronous(account);
            }
            // END

            int unreadMessageCount = localFolder.getUnreadMessageCount();
            for (MessagingListener l : getListeners()) {
                l.folderStatusChanged(account, folder, unreadMessageCount);
            }

            /* Notify listeners that we're finally done. */

            remoteFolder.setLastChecked(System.currentTimeMillis());
            localFolder.setLastChecked(System.currentTimeMillis());
            localFolder.setStatus(null);
            
            if (MailChat.DEBUG)
                Log.d(MailChat.LOG_TAG, "Done synchronizing folder " + account.getDescription() + ":" + folder +
                      " @ " + new Date() + " with " + newMessages + " new messages");
         
            for (MessagingListener l : getListeners(listener)) {
                l.synchronizeMailboxFinished(account, folder, remoteMessageCount, newMessages);
            }

            /*
            if (commandException != null) {
                String rootMessage = getRootCauseMessage(commandException);
                Log.e(MailChat.LOG_TAG, "Root cause failure in " + account.getDescription() + ":" +
                      tLocalFolder.getName() + " was '" + rootMessage + "'");
                localFolder.setStatus(rootMessage);
                for (MessagingListener l : getListeners(listener)) {
                    l.synchronizeMailboxFailed(account, folder, rootMessage);
                }
            }else {
				// 加载更多完成后更新联系人
        		
			}
			*/
            // 加载更多完成后更新联系人
            reflashContactList(account,null);

            if (MailChat.DEBUG)
                Log.i(MailChat.LOG_TAG, "Done synchronizing folder " + account.getDescription() + ":" + folder);

        } catch (Exception e) {
            Log.e(MailChat.LOG_TAG, "synchronizeMailbox", e);
            // If we don't set the last checked, it can try too often during
            // failure conditions
            String rootMessage = MailChat.getRootCauseMessage(e);
            if (tLocalFolder != null) {
                try {
                    tLocalFolder.setStatus(rootMessage);
                    tLocalFolder.setLastChecked(System.currentTimeMillis());
                } catch (MessagingException me) {
                    Log.e(MailChat.LOG_TAG, "Could not set last checked on folder " + account.getDescription() + ":" +
                          tLocalFolder.getName(), e);
                }
            }

            for (MessagingListener l : getListeners(listener)) {
                l.synchronizeMailboxFailed(account, folder, rootMessage);
            }
            notifyUserIfCertificateProblem(mApplication, e, account, true);
            addErrorMessage(account, null, e);
            Log.e(MailChat.LOG_TAG, "Failed synchronizing folder " + account.getDescription() + ":" + folder + " @ " + new Date());
        } finally {
            if (providedRemoteFolder == null) {
                closeFolder(remoteFolder);
            }

            closeFolder(tLocalFolder);
        }

    }

    private boolean prepareSyncMaps(Account account,
    		Folder localFolder,
    		Message[] localMessages,
    		HashMap<String, Message> localMessageIDMap,
    		HashMap<String, Message> localMailChatMessageIDMap) {
    	boolean isSentFolder = localFolder.getName().equals(account.getSentFolderName());
        boolean isDraftsFolder = localFolder.getName().equals(account.getDraftsFolderName());
        boolean isTrashFolder = localFolder.getName().equals(account.getTrashFolderName());
        
        // Whether this folder may contain messages with local uid
        boolean isLocalFolder = isSentFolder || isDraftsFolder || isTrashFolder;
        
        for (Message message : localMessages) {
        	String uid = message.getUid();
        	if (MimeUtility.isLocalUid(uid)) {
            	String messageID = getMessageID(message);
            	if (messageID != null) {
            		localMessageIDMap.put(messageID, message);
            	}
            	
            	String mailChatMessageID = MimeUtility.getFirstHeader(
            			message,
            			MailChat.MAILCHAT_MESSAGE_ID_HEADER);
            	if (mailChatMessageID != null) {
            		localMailChatMessageIDMap.put(mailChatMessageID, message);
            	}
            }
        }
        
        return isLocalFolder;
    }

    private void closeFolder(Folder f) {
        if (f != null) {
            f.close();
        }
    }


    /*
     * If the folder is a "special" folder we need to see if it exists
     * on the remote server. It if does not exist we'll try to create it. If we
     * can't create we'll abort. This will happen on every single Pop3 folder as
     * designed and on Imap folders during error conditions. This allows us
     * to treat Pop3 and Imap the same in this code.
     */
    private boolean verifyOrCreateRemoteSpecialFolder(final Account account, final String folder, final Folder remoteFolder, final MessagingListener listener) throws MessagingException {
        if (folder.equals(account.getTrashFolderName()) ||
                folder.equals(account.getSentFolderName()) ||
                folder.equals(account.getDraftsFolderName())) {
            if (!remoteFolder.exists()) {
                if (!remoteFolder.create(FolderType.HOLDS_MESSAGES)) {
                    for (MessagingListener l : getListeners(listener)) {
                        l.synchronizeMailboxFinished(account, folder, 0, 0);
                    }
                    if (MailChat.DEBUG)
                        Log.i(MailChat.LOG_TAG, "Done synchronizing folder " + folder);

                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Fetches the messages described by inputMessages from the remote store and writes them to
     * local storage.
     *
     * @param account
     *            The account the remote store belongs to.
     * @param remoteFolder
     *            The remote folder to download messages from.
     * @param localFolder
     *            The {@link LocalFolder} instance corresponding to the remote folder.
     * @param inputMessages
     *            A list of messages objects that store the UIDs of which messages to download.
     * @param flagSyncOnly
     *            Only flags will be fetched from the remote store if this is {@code true}.
     *
     * @return The number of downloaded messages that are not flagged as {@link Flag#SEEN}.
     *
     * @throws MessagingException
     */
    private int downloadMessages(final Account account, final Folder remoteFolder,
                                 final LocalFolder localFolder, List<Message> inputMessages,
                                 boolean flagSyncOnly) throws MessagingException {

        final Date earliestDate = account.getEarliestPollDate();
        Date downloadStarted = new Date(); // now

        if (earliestDate != null) {
            if (MailChat.DEBUG) {
                Log.d(MailChat.LOG_TAG, "Only syncing messages after " + earliestDate);
            }
        }
        final String folder = remoteFolder.getName();

        int unreadBeforeStart = 0;
        try {
            AccountStats stats = account.getStats(mApplication);
            unreadBeforeStart = stats.unreadMessageCount;

        } catch (MessagingException e) {
            Log.e(MailChat.LOG_TAG, "Unable to getUnreadMessageCount for account: " + account, e);
        }

        ArrayList<Message> syncFlagMessages = new ArrayList<Message>();
        List<Message> unsyncedMessages = new ArrayList<Message>();
        final AtomicInteger newMessages = new AtomicInteger(0);

        List<Message> messages = new ArrayList<Message>(inputMessages);

        for (Message message : messages) {
            evaluateMessageForDownload(message, folder, localFolder, remoteFolder, account, unsyncedMessages, syncFlagMessages , flagSyncOnly);
        }

        final AtomicInteger progress = new AtomicInteger(0);
        final int todo = unsyncedMessages.size() + syncFlagMessages.size();
        for (MessagingListener l : getListeners()) {
            l.synchronizeMailboxProgress(account, folder, progress.get(), todo);
        }

        if (MailChat.DEBUG)
            Log.d(MailChat.LOG_TAG, "SYNC: Have " + unsyncedMessages.size() + " unsynced messages");

        messages.clear();
        final ArrayList<Message> largeMessages = new ArrayList<Message>();
        final ArrayList<Message> smallMessages = new ArrayList<Message>();
        if (!unsyncedMessages.isEmpty()) {

            /*
             * Reverse the order of the messages. Depending on the server this may get us
             * fetch results for newest to oldest. If not, no harm done.
             */
            Collections.sort(unsyncedMessages, new UidReverseComparator());
            
            int visibleLimit = localFolder.getVisibleLimit();
            int listSize = unsyncedMessages.size();

            if ((visibleLimit > 0) && (listSize > visibleLimit)) {
                unsyncedMessages = unsyncedMessages.subList(0, visibleLimit);
            }

            FetchProfile fp = new FetchProfile();
            if (remoteFolder.supportsFetchingFlags()) {
                fp.add(FetchProfile.Item.FLAGS);
            }
            fp.add(FetchProfile.Item.ENVELOPE);

            if (MailChat.DEBUG)
                Log.d(MailChat.LOG_TAG, "SYNC: About to fetch " + unsyncedMessages.size() + " unsynced messages for folder " + folder);


            fetchUnsyncedMessages(account, remoteFolder, localFolder, unsyncedMessages, smallMessages, largeMessages, progress, todo, fp);

            // If a message didn't exist, messageFinished won't be called, but we shouldn't try again
            // If we got here, nothing failed
            for (Message message : unsyncedMessages) {
                String newPushState = remoteFolder.getNewPushState(localFolder.getPushState(), message);
                if (newPushState != null) {
                    localFolder.setPushState(newPushState);
                }
            }
            if (MailChat.DEBUG) {
                Log.d(MailChat.LOG_TAG, "SYNC: Synced unsynced messages for folder " + folder);
            }


        }

        if (MailChat.DEBUG)
            Log.d(MailChat.LOG_TAG, "SYNC: Have "
                  + largeMessages.size() + " large messages and "
                  + smallMessages.size() + " small messages out of "
                  + unsyncedMessages.size() + " unsynced messages");

        unsyncedMessages.clear();

        /*
         * Grab the content of the small messages first. This is going to
         * be very fast and at very worst will be a single up of a few bytes and a single
         * download of 625k.
         */
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.BODY);
        //        fp.add(FetchProfile.Item.FLAGS);
        //        fp.add(FetchProfile.Item.ENVELOPE);
        if (smallMessages.size() > 0) {
        	downloadSmallMessages(account, remoteFolder, localFolder, smallMessages, progress, unreadBeforeStart, newMessages, todo, fp);
        }
        smallMessages.clear();

        /*
         * Now do the large messages that require more round trips.
         */
        fp.clear();
        fp.add(FetchProfile.Item.STRUCTURE);
        if (largeMessages.size() > 0) {
        	downloadLargeMessages(account, remoteFolder, localFolder, largeMessages, progress, unreadBeforeStart,  newMessages, todo, fp);
        }
        largeMessages.clear();

        /*
         * Refresh the flags for any messages in the local store that we didn't just
         * download.
         */

        refreshLocalMessageFlags(account, remoteFolder, localFolder, syncFlagMessages, progress, todo);

        if (MailChat.DEBUG)
            Log.d(MailChat.LOG_TAG, "SYNC: Synced remote messages for folder " + folder + ", " + newMessages.get() + " new messages");

        localFolder.purgeToVisibleLimit(new MessageRemovalListener() {
            @Override
            public void messageRemoved(Message message) {
                for (MessagingListener l : getListeners()) {
                    l.synchronizeMailboxRemovedMessage(account, folder, message);
                }
            }

        });

        // If the oldest message seen on this sync is newer than
        // the oldest message seen on the previous sync, then
        // we want to move our high-water mark forward
        // this is all here just for pop which only syncs inbox
        // this would be a little wrong for IMAP (we'd want a folder-level pref, not an account level pref.)
        // fortunately, we just don't care.
        Long oldestMessageTime = localFolder.getOldestMessageDate();

        if (oldestMessageTime != null) {
            Date oldestExtantMessage = new Date(oldestMessageTime);
            if (oldestExtantMessage.before(downloadStarted) &&
                    oldestExtantMessage.after(new Date(account.getLatestOldMessageSeenTime()))) {
                account.setLatestOldMessageSeenTime(oldestExtantMessage.getTime());
                account.save(Preferences.getPreferences(mApplication.getApplicationContext()));
            }

        }
        return newMessages.get();
    }
    
	// 删除本地/远程重复邮件改到下载过程中，以避免短暂出现重复邮件
    // Modified by LL
    // BEGIN
    private int downloadMessages(final Account account,
    		final Folder remoteFolder,
            final LocalFolder localFolder,
            List<Message> inputMessages,
            boolean flagSyncOnly,
            HashMap<String, Message> localMessageIDMap,
            HashMap<String, Message> localMailChatMessageIDMap) throws MessagingException {

        final Date earliestDate = account.getEarliestPollDate();
        Date downloadStarted = new Date(); // now

        if (earliestDate != null) {
            if (MailChat.DEBUG) {
                Log.d(MailChat.LOG_TAG, "Only syncing messages after " + earliestDate);
            }
        }
        final String folder = remoteFolder.getName();

        int unreadBeforeStart = 0;
        try {
            AccountStats stats = account.getStats(mApplication);
            unreadBeforeStart = stats.unreadMessageCount;

        } catch (MessagingException e) {
            Log.e(MailChat.LOG_TAG, "Unable to getUnreadMessageCount for account: " + account, e);
        }

        ArrayList<Message> syncFlagMessages = new ArrayList<Message>();
        List<Message> unsyncedMessages = new ArrayList<Message>();
        final AtomicInteger newMessages = new AtomicInteger(0);

        List<Message> messages = new ArrayList<Message>(inputMessages);

        for (Message message : messages) {
            evaluateMessageForDownload(message, folder, localFolder, remoteFolder, account, unsyncedMessages, syncFlagMessages , flagSyncOnly);
        }

        final AtomicInteger progress = new AtomicInteger(0);
        final int todo = unsyncedMessages.size() + syncFlagMessages.size();
        for (MessagingListener l : getListeners()) {
            l.synchronizeMailboxProgress(account, folder, progress.get(), todo);
        }

        if (MailChat.DEBUG)
            Log.d(MailChat.LOG_TAG, "SYNC: Have " + unsyncedMessages.size() + " unsynced messages");

        messages.clear();
        final ArrayList<Message> largeMessages = new ArrayList<Message>();
        final ArrayList<Message> smallMessages = new ArrayList<Message>();
        if (true) {//!unsyncedMessages.isEmpty()) {

            /*
             * Reverse the order of the messages. Depending on the server this may get us
             * fetch results for newest to oldest. If not, no harm done.
             */
            Collections.sort(unsyncedMessages, new UidReverseComparator());
            
            int visibleLimit = localFolder.getVisibleLimit();
            int listSize = unsyncedMessages.size();

            if ((visibleLimit > 0) && (listSize > visibleLimit)) {
                unsyncedMessages = unsyncedMessages.subList(0, visibleLimit);
            }

            FetchProfile fp = new FetchProfile();
            if (remoteFolder.supportsFetchingFlags()) {
                fp.add(FetchProfile.Item.FLAGS);
            }
            fp.add(FetchProfile.Item.ENVELOPE);

            if (MailChat.DEBUG)
                Log.d(MailChat.LOG_TAG, "SYNC: About to fetch " + unsyncedMessages.size() + " unsynced messages for folder " + folder);


            fetchUnsyncedMessages(account,
            		remoteFolder,
            		localFolder,
            		unsyncedMessages,
            		smallMessages,
            		largeMessages,
            		progress,
            		todo,
            		fp,
            		localMessageIDMap,
            		localMailChatMessageIDMap);

            // If a message didn't exist, messageFinished won't be called, but we shouldn't try again
            // If we got here, nothing failed
            for (Message message : unsyncedMessages) {
                String newPushState = remoteFolder.getNewPushState(localFolder.getPushState(), message);
                if (newPushState != null) {
                    localFolder.setPushState(newPushState);
                }
            }
            if (MailChat.DEBUG) {
                Log.d(MailChat.LOG_TAG, "SYNC: Synced unsynced messages for folder " + folder);
            }


        }

        if (MailChat.DEBUG)
            Log.d(MailChat.LOG_TAG, "SYNC: Have "
                  + largeMessages.size() + " large messages and "
                  + smallMessages.size() + " small messages out of "
                  + unsyncedMessages.size() + " unsynced messages");

        unsyncedMessages.clear();

        /*
         * Grab the content of the small messages first. This is going to
         * be very fast and at very worst will be a single up of a few bytes and a single
         * download of 625k.
         */
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.BODY);
        //        fp.add(FetchProfile.Item.FLAGS);
        //        fp.add(FetchProfile.Item.ENVELOPE);
        if (smallMessages.size() > 0) {
        	downloadSmallMessages(account, remoteFolder, localFolder, smallMessages, progress, unreadBeforeStart, newMessages, todo, fp);
        }
        smallMessages.clear();

        /*
         * Now do the large messages that require more round trips.
         */
        fp.clear();
        fp.add(FetchProfile.Item.STRUCTURE);
        if (largeMessages.size() > 0) {
        	downloadLargeMessages(account, remoteFolder, localFolder, largeMessages, progress, unreadBeforeStart,  newMessages, todo, fp);
        }
        largeMessages.clear();

        /*
         * Refresh the flags for any messages in the local store that we didn't just
         * download.
         */

        refreshLocalMessageFlags(account, remoteFolder, localFolder, syncFlagMessages, progress, todo);

        if (MailChat.DEBUG)
            Log.d(MailChat.LOG_TAG, "SYNC: Synced remote messages for folder " + folder + ", " + newMessages.get() + " new messages");

        localFolder.purgeToVisibleLimit(new MessageRemovalListener() {
            @Override
            public void messageRemoved(Message message) {
                for (MessagingListener l : getListeners()) {
                    l.synchronizeMailboxRemovedMessage(account, folder, message);
                }
            }

        });

        // If the oldest message seen on this sync is newer than
        // the oldest message seen on the previous sync, then
        // we want to move our high-water mark forward
        // this is all here just for pop which only syncs inbox
        // this would be a little wrong for IMAP (we'd want a folder-level pref, not an account level pref.)
        // fortunately, we just don't care.
        Long oldestMessageTime = localFolder.getOldestMessageDate();

        if (oldestMessageTime != null) {
            Date oldestExtantMessage = new Date(oldestMessageTime);
            if (oldestExtantMessage.before(downloadStarted) &&
                    oldestExtantMessage.after(new Date(account.getLatestOldMessageSeenTime()))) {
                account.setLatestOldMessageSeenTime(oldestExtantMessage.getTime());
                account.save(Preferences.getPreferences(mApplication.getApplicationContext()));
            }

        }
        return newMessages.get();
    }
    // END
    
    private void evaluateMessageForDownload(final Message message, final String folder,
                                            final LocalFolder localFolder,
                                            final Folder remoteFolder,
                                            final Account account,
                                            final List<Message> unsyncedMessages,
                                            final ArrayList<Message> syncFlagMessages,
                                            boolean flagSyncOnly) throws MessagingException {
        if (message.isSet(Flag.DELETED)) {
            syncFlagMessages.add(message);
            return;
        }
        
        if (folder.equals(account.getDraftsFolderName())) {
            unsyncedMessages.add(message);
            return;
        }

        Message localMessage = localFolder.getMessage(message.getUid());

        if (localMessage == null) {
            if (!flagSyncOnly) {
                if (!(message.isSet(Flag.X_DOWNLOADED_FULL)
                        || message.isSet(Flag.X_DOWNLOADED_PARTIAL)
                        || message.isSet(Flag.X_PUSH_MAIL))) {
                    if (MailChat.DEBUG)
                        Log.v(MailChat.LOG_TAG, "Message with uid " + message.getUid() + " has not yet been downloaded");

                    unsyncedMessages.add(message);
                } else {
                    if (MailChat.DEBUG)
                        Log.v(MailChat.LOG_TAG, "Message with uid " + message.getUid() + " is partially or fully downloaded");

                    // Store the updated message locally
                    localFolder.appendMessages(new Message[] { message });

                    localMessage = localFolder.getMessage(message.getUid());

                    localMessage.setFlag(Flag.X_DOWNLOADED_FULL, message.isSet(Flag.X_DOWNLOADED_FULL));
                    localMessage.setFlag(Flag.X_DOWNLOADED_PARTIAL, message.isSet(Flag.X_DOWNLOADED_PARTIAL));

                    for (MessagingListener l : getListeners()) {
                        l.synchronizeMailboxAddOrUpdateMessage(account, folder, localMessage);
                        if (!localMessage.isSet(Flag.SEEN)) {
                            l.synchronizeMailboxNewMessage(account, folder, localMessage);
                        }
                    }
                }
            }
        } else if (!localMessage.isSet(Flag.DELETED)) {
            if (MailChat.DEBUG)
                Log.v(MailChat.LOG_TAG, "Message with uid " + message.getUid() + " is present in the local store");

            if (!(localMessage.isSet(Flag.X_DOWNLOADED_FULL) 
                    || localMessage.isSet(Flag.X_DOWNLOADED_PARTIAL)
                    || localMessage.isSet(Flag.X_PUSH_MAIL))) {
                if (MailChat.DEBUG)
                    Log.v(MailChat.LOG_TAG, "Message with uid " + message.getUid()
                          + " is not downloaded, even partially; trying again");

                unsyncedMessages.add(message);
            } else {
                String newPushState = remoteFolder.getNewPushState(localFolder.getPushState(), message);
                if (newPushState != null) {
                    localFolder.setPushState(newPushState);
                }
                syncFlagMessages.add(message);
            }
        }
    }

    private void fetchUnsyncedMessages(final Account account, final Folder remoteFolder,
                                       final LocalFolder localFolder,
                                       List<Message> unsyncedMessages,
                                       final ArrayList<Message> smallMessages,
                                       final ArrayList<Message> largeMessages,
                                       final AtomicInteger progress,
                                       final int todo,
                                       FetchProfile fp) throws MessagingException {
        final String folder = remoteFolder.getName();

        final Date earliestDate = account.getEarliestPollDate();

        /*
         * Messages to be batch written
         */
        final List<Message> chunk = new ArrayList<Message>(UNSYNC_CHUNK_SIZE);

        remoteFolder.fetch(unsyncedMessages.toArray(EMPTY_MESSAGE_ARRAY), fp,
        new MessageRetrievalListener() {
            @Override
            public void messageFinished(Message message, int number, int ofTotal) {
                try {
                    String newPushState = remoteFolder.getNewPushState(localFolder.getPushState(), message);
                    if (newPushState != null) {
                        localFolder.setPushState(newPushState);
                    }
                    if (message.isSet(Flag.DELETED) || message.olderThan(earliestDate)) {

                        if (MailChat.DEBUG) {
                            if (message.isSet(Flag.DELETED)) {
                                Log.v(MailChat.LOG_TAG, "Newly downloaded message " + account + ":" + folder + ":" + message.getUid()
                                      + " was marked deleted on server, skipping");
                            } else {
                                Log.d(MailChat.LOG_TAG, "Newly downloaded message " + message.getUid() + " is older than "
                                      + earliestDate + ", skipping");
                            }
                        }
                        progress.incrementAndGet();
                        for (MessagingListener l : getListeners()) {
                            l.synchronizeMailboxProgress(account, folder, progress.get(), todo);
                        }
                        return;
                    }
                    
                    /*
                    if (account.getMaximumAutoDownloadMessageSize() > 0 &&
                    message.getSize() > account.getMaximumAutoDownloadMessageSize()) {
                        largeMessages.add(message);
                    } else {
                        smallMessages.add(message);
                    }
                    */
                    // 不再区分大小邮件，刷新列表时邮件一律只取结构和文本，
                    // 不预载图片和附件
                    // Modified by LL
                    largeMessages.add(message);

                    // And include it in the view
                    if (message.getSubject() != null && message.getFrom() != null) {
                        /*
                         * We check to make sure that we got something worth
                         * showing (subject and from) because some protocols
                         * (POP) may not be able to give us headers for
                         * ENVELOPE, only size.
                         */

                        // keep message for delayed storing
                        chunk.add(message);

                        if (chunk.size() >= UNSYNC_CHUNK_SIZE) {
                            writeUnsyncedMessages(chunk, localFolder, account, folder);
                            chunk.clear();
                        }
                    }
                } catch (Exception e) {
                    Log.e(MailChat.LOG_TAG, "Error while storing downloaded message.", e);
                    addErrorMessage(account, null, e);
                }
            }

            @Override
            public void messageStarted(String uid, int number, int ofTotal) {}

            @Override
            public void messagesFinished(int total) {
                // FIXME this method is almost never invoked by various Stores! Don't rely on it unless fixed!!
            }

        });
        if (!chunk.isEmpty()) {
            writeUnsyncedMessages(chunk, localFolder, account, folder);
            chunk.clear();
        }
    }
    
    // 删除本地/远程重复邮件改到下载过程中，以避免短暂出现重复邮件
    // Modified by LL
    // BEGIN
	private void fetchUnsyncedMessages(final Account account,
			final Folder remoteFolder,
			final LocalFolder localFolder,
			final List<Message> unsyncedMessages,
			final ArrayList<Message> smallMessages,
			final ArrayList<Message> largeMessages,
			final AtomicInteger progress,
			final int todo,
			FetchProfile fp,
			final HashMap<String, Message> localMessageIDMap,
            final HashMap<String, Message> localMailChatMessageIDMap) throws MessagingException {
		
		final boolean isSentFolder = localFolder.getName().equals(account.getSentFolderName());
        boolean isDraftsFolder = localFolder.getName().equals(account.getDraftsFolderName());
        boolean isTrashFolder = localFolder.getName().equals(account.getTrashFolderName());
        final boolean isLocalFolder = isSentFolder || isDraftsFolder || isTrashFolder;
		
		final String folder = remoteFolder.getName();

		final Date earliestDate = account.getEarliestPollDate();
		
		final List<Message> appendMessages = new ArrayList<Message>();
		if (isLocalFolder) {
			for (Message message : localMailChatMessageIDMap.values()) {
				appendMessages.add(message);
			}
		}

		/*
		 * Messages to be batch written
		 */
		final List<Message> chunk = new ArrayList<Message>(UNSYNC_CHUNK_SIZE);

		remoteFolder.fetch(unsyncedMessages.toArray(EMPTY_MESSAGE_ARRAY), fp,
				new MessageRetrievalListener() {
					@Override
					public void messageFinished(Message message, int number, int ofTotal) {
						try {
							String newPushState = remoteFolder.getNewPushState(
									localFolder.getPushState(), message);
							if (newPushState != null) {
								localFolder.setPushState(newPushState);
							}
							if (message.isSet(Flag.DELETED)
									|| message.olderThan(earliestDate)) {

								if (MailChat.DEBUG) {
									if (message.isSet(Flag.DELETED)) {
										Log.v(MailChat.LOG_TAG,
												"Newly downloaded message "
														+ account
														+ ":"
														+ folder
														+ ":"
														+ message.getUid()
														+ " was marked deleted on server, skipping");
									} else {
										Log.d(MailChat.LOG_TAG,
												"Newly downloaded message "
														+ message.getUid()
														+ " is older than "
														+ earliestDate
														+ ", skipping");
									}
								}
								progress.incrementAndGet();
								for (MessagingListener l : getListeners()) {
									l.synchronizeMailboxProgress(account,
											folder, progress.get(), todo);
								}
								return;
							}

							/*
							 * if (account.getMaximumAutoDownloadMessageSize() >
							 * 0 && message.getSize() >
							 * account.getMaximumAutoDownloadMessageSize()) {
							 * largeMessages.add(message); } else {
							 * smallMessages.add(message); }
							 */
							// 不再区分大小邮件，刷新列表时邮件一律只取结构和文本，
							// 不预载图片和附件
							// Modified by LL
							boolean isUpdateLocalMessage = false;
							
							{
							    LocalMessage msg = localFolder.getMessage(message.getUid());
                                if (msg != null) {
                                    int compare = MimeUtility.compareInternalDate(msg, message);
                                    if (compare == 0 || isSentFolder) {
                                        isUpdateLocalMessage = true;
                                    } else if (compare < 0) {
                                        msg.destroy();
                                    } else if (compare > 0) {
                                        unsyncedMessages.remove(message);
                                        appendMessages.add(msg);
                                        isUpdateLocalMessage = true;
                                    }
                                }
                            }
							
							if (!isUpdateLocalMessage) {
    							String mailChatMessageId = MimeUtility.getFirstHeader(message,
    		                			MailChat.MAILCHAT_MESSAGE_ID_HEADER);
    		                	if (mailChatMessageId != null) {
    		                		LocalMessage msg = (LocalMessage)localMailChatMessageIDMap.get(mailChatMessageId);
    		                		if (msg != null) {
    		                		    int compare = MimeUtility.compareInternalDate(msg, message);
    		                		    if (compare == 0 || isSentFolder) {
    		                		        String oldUid = msg.getUid();
    		                		        String newUid = message.getUid();
    		                		        msg.setUid(newUid);
    		                		        localFolder.changeUid(msg);
    		                		        for (MessagingListener l : getListeners()) {
    		                		            l.messageUidChanged(account, localFolder.getName(), oldUid, newUid);
    		                		        }
    		                		        localMailChatMessageIDMap.remove(mailChatMessageId);
    		                		        unsyncedMessages.remove(message);
    		                		        appendMessages.remove(msg);
    		                		        isUpdateLocalMessage = true;
    		                		    } else if (compare < 0) {
    		                		        localMailChatMessageIDMap.remove(mailChatMessageId);
                                            appendMessages.remove(msg);
    		                		        msg.destroy();
    		                		    } else if (compare > 0) {
                                            String oldUid = msg.getUid();
                                            String newUid = message.getUid();
                                            msg.setUid(newUid);
                                            localFolder.changeUid(msg);
                                            for (MessagingListener l : getListeners()) {
                                                l.messageUidChanged(account, localFolder.getName(), oldUid, newUid);
                                            }
                                            localMailChatMessageIDMap.remove(mailChatMessageId);
                                            unsyncedMessages.remove(message);
                                            isUpdateLocalMessage = true;
    		                		    }
    		                		}
    		                	}
							}
		                	
		                	if (!isUpdateLocalMessage) {
								String messageID = getMessageID(message);
			                	if (messageID != null) {
			                		LocalMessage msg = (LocalMessage)localMessageIDMap.get(messageID);
			                		if (msg != null) {
			                		    int compare = MimeUtility.compareInternalDate(msg, message);
			                		    if (compare == 0 || isSentFolder) {
			                		        String oldUid = msg.getUid();
			                		        String newUid = message.getUid();
			                		        msg.setUid(newUid);
			                		        localFolder.changeUid(msg);
			                		        for (MessagingListener l : getListeners()) {
			                		            l.messageUidChanged(account, localFolder.getName(), oldUid, newUid);
			                		        }
			                		        localMessageIDMap.remove(messageID);
			                		        unsyncedMessages.remove(message);
			                		        appendMessages.remove(msg);
			                		        isUpdateLocalMessage = true;
			                		    } else if (compare < 0) {
			                		        localMessageIDMap.remove(messageID);
			                		        appendMessages.remove(msg);
			                		        msg.destroy();
	                                    } else if (compare > 0) {
	                                        String oldUid = msg.getUid();
	                                        String newUid = message.getUid();
	                                        msg.setUid(newUid);
	                                        localFolder.changeUid(msg);
	                                        for (MessagingListener l : getListeners()) {
	                                            l.messageUidChanged(account, localFolder.getName(), oldUid, newUid);
	                                        }
	                                        localMessageIDMap.remove(messageID);
	                                        unsyncedMessages.remove(message);
	                                        isUpdateLocalMessage = true;
	                                    }
			                		}
			                	}
		                	}
							
							if (!isUpdateLocalMessage) {
								/*
			                    if (account.getMaximumAutoDownloadMessageSize() > 0 &&
			                    message.getSize() > account.getMaximumAutoDownloadMessageSize()) {
			                        largeMessages.add(message);
			                    } else {
			                        smallMessages.add(message);
			                    }
			                    */
			                    // 不再区分大小邮件，刷新列表时邮件一律只取结构和文本，
			                    // 不预载图片和附件
			                    // Modified by LL
								largeMessages.add(message);
								
								// And include it in the view
								if (message.getSubject() != null
										&& message.getFrom() != null) {
									/*
									 * We check to make sure that we got something
									 * worth showing (subject and from) because some
									 * protocols (POP) may not be able to give us
									 * headers for ENVELOPE, only size.
									 */

									// keep message for delayed storing
									chunk.add(message);

									if (chunk.size() >= UNSYNC_CHUNK_SIZE) {
										writeUnsyncedMessages(chunk, localFolder,
												account, folder);
										chunk.clear();
									}
								}
							}
						} catch (Exception e) {
							Log.e(MailChat.LOG_TAG,
									"Error while storing downloaded message.",
									e);
							addErrorMessage(account, null, e);
						}
					}

					@Override
					public void messageStarted(String uid, int number,
							int ofTotal) {
					}

					@Override
					public void messagesFinished(int total) {
						// FIXME this method is almost never invoked by various
						// Stores! Don't rely on it unless fixed!!
					}

				});
		if (!chunk.isEmpty()) {
			writeUnsyncedMessages(chunk, localFolder, account, folder);
			chunk.clear();
		}
		
		if (isLocalFolder) {
			for (Message message : appendMessages) {
	            PendingCommand command = new PendingCommand();
	            command.command = PENDING_COMMAND_APPEND;
	            command.arguments =
	                new String[] {
	                localFolder.getName(),
	                message.getUid()
	            };
	            queuePendingCommand(account, command);
	        }
		}
	}
    // END

    /**
     * Actual storing of messages
     *
     * <br>
     * FIXME: <strong>This method should really be moved in the above MessageRetrievalListener once {@link MessageRetrievalListener#messagesFinished(int)} is properly invoked by various stores</strong>
     *
     * @param messages Never <code>null</code>.
     * @param localFolder
     * @param account
     * @param folder
     */
    private void writeUnsyncedMessages(final List<Message> messages, final LocalFolder localFolder, final Account account, final String folder) {
        if (MailChat.DEBUG) {
            Log.v(MailChat.LOG_TAG, "Batch writing " + Integer.toString(messages.size()) + " messages");
        }
        try {
            // Store the new message locally
            localFolder.appendMessages(messages.toArray(new Message[messages.size()]));

            for (final Message message : messages) {
                final Message localMessage = localFolder.getMessage(message.getUid());
                syncFlags(localMessage, message);
                if (MailChat.DEBUG)
                    Log.v(MailChat.LOG_TAG, "About to notify listeners that we got a new unsynced message "
                          + account + ":" + folder + ":" + message.getUid());
                for (final MessagingListener l : getListeners()) {
                    l.synchronizeMailboxAddOrUpdateMessage(account, folder, localMessage);
                }
            }
        } catch (final Exception e) {
            Log.e(MailChat.LOG_TAG, "Error while storing downloaded message.", e);
            addErrorMessage(account, null, e);
        }
    }


    private boolean shouldImportMessage(final Account account, final String folder, final Message message, final AtomicInteger progress, final Date earliestDate) {

        if (account.isSearchByDateCapable() && message.olderThan(earliestDate)) {
            if (MailChat.DEBUG) {
                Log.d(MailChat.LOG_TAG, "Message " + message.getUid() + " is older than "
                      + earliestDate + ", hence not saving");
            }
            return false;
        }
        return true;
    }

    private void downloadSmallMessages(final Account account, final Folder remoteFolder,
                                       final LocalFolder localFolder,
                                       ArrayList<Message> smallMessages,
                                       final AtomicInteger progress,
                                       final int unreadBeforeStart,
                                       final AtomicInteger newMessages,
                                       final int todo,
                                       FetchProfile fp) throws MessagingException {
        final String folder = remoteFolder.getName();

        final Date earliestDate = account.getEarliestPollDate();

        if (MailChat.DEBUG)
            Log.d(MailChat.LOG_TAG, "SYNC: Fetching small messages for folder " + folder);

        remoteFolder.fetch(smallMessages.toArray(new Message[smallMessages.size()]),
        fp, new MessageRetrievalListener() {
            @Override
            public void messageFinished(final Message message, int number, int ofTotal) {
                try {

                    if (!shouldImportMessage(account, folder, message, progress, earliestDate)) {
                        progress.incrementAndGet();

                        return;
                    }

                    // Store the updated message locally
                    final Message localMessage = localFolder.storeSmallMessage(message, new Runnable() {
                        @Override
                        public void run() {
                            progress.incrementAndGet();
                        }
                    });

                    // Increment the number of "new messages" if the newly downloaded message is
                    // not marked as read.
                    if (!localMessage.isSet(Flag.SEEN)) {
                        newMessages.incrementAndGet();
                    }

                    if (MailChat.DEBUG)
                        Log.v(MailChat.LOG_TAG, "About to notify listeners that we got a new small message "
                              + account + ":" + folder + ":" + message.getUid());

                    // Update the listener with what we've found
                    for (MessagingListener l : getListeners()) {
                        l.synchronizeMailboxAddOrUpdateMessage(account, folder, localMessage);
                        l.synchronizeMailboxProgress(account, folder, progress.get(), todo);
                        if (!localMessage.isSet(Flag.SEEN)) {
                            l.synchronizeMailboxNewMessage(account, folder, localMessage);
                        }
                    }
                    // Send a notification of this message

                    if (shouldNotifyForMessage(account, localFolder, message)) {
                        // Notify with the localMessage so that we don't have to recalculate the content preview.
                        notifyAccount(mApplication, account, localMessage, unreadBeforeStart);
                    }

                } catch (MessagingException me) {
                    addErrorMessage(account, null, me);
                    Log.e(MailChat.LOG_TAG, "SYNC: fetch small messages", me);
                }
            }

            @Override
            public void messageStarted(String uid, int number, int ofTotal) {}

            @Override
            public void messagesFinished(int total) {}
        });

        if (MailChat.DEBUG)
            Log.d(MailChat.LOG_TAG, "SYNC: Done fetching small messages for folder " + folder);
    }



    private void downloadLargeMessages(final Account account, final Folder remoteFolder,
                                       final LocalFolder localFolder,
                                       ArrayList<Message> largeMessages,
                                       final AtomicInteger progress,
                                       final int unreadBeforeStart,
                                       final AtomicInteger newMessages,
                                       final int todo,
                                       FetchProfile fp) throws MessagingException {
    	// 根据UID对邮件进行排序，优先下载UID数值更大（即日期更新）的邮件
    	// Modified by LL
    	Collections.sort(largeMessages, new UidReverseComparator());
    	
        final String folder = remoteFolder.getName();

        final Date earliestDate = account.getEarliestPollDate();

        if (MailChat.DEBUG)
            Log.d(MailChat.LOG_TAG, "SYNC: Fetching large messages for folder " + folder);
        remoteFolder.fetch(largeMessages.toArray(new Message[largeMessages.size()]), fp, null);
        for (Message message : largeMessages) {

            if (!shouldImportMessage(account, folder, message, progress, earliestDate)) {
                progress.incrementAndGet();
                continue;
            }

            if (message.getBody() == null) {
                String contentType = MimeUtility.getFirstHeader(message, MimeHeader.HEADER_CONTENT_TYPE);
                if (contentType != null && contentType.toLowerCase(Locale.US).startsWith("text")) {
                    /*
                     * The provider was unable to get the structure of the message, so
                     * we'll download a reasonable portion of the messge and mark it as
                     * incomplete so the entire thing can be downloaded later if the user
                     * wishes to download it.
                     */
                    fp.clear();
                    fp.add(FetchProfile.Item.BODY_SANE);
                    /*
                     *  TODO a good optimization here would be to make sure that all Stores set
                     *  the proper size after this fetch and compare the before and after size. If
                     *  they equal we can mark this SYNCHRONIZED instead of PARTIALLY_SYNCHRONIZED
                     */

                    remoteFolder.fetch(new Message[] { message }, fp, null);

                    // Store the updated message locally
                    localFolder.appendMessages(new Message[] { message });

                    Message localMessage = localFolder.getMessage(message.getUid());


                    // Certain (POP3) servers give you the whole message even when you ask for only the first x Kb
                    if (!message.isSet(Flag.X_DOWNLOADED_FULL)) {
                        /*
                         * Mark the message as fully downloaded if the message size is smaller than
                         * the account's autodownload size limit, otherwise mark as only a partial
                         * download.  This will prevent the system from downloading the same message
                         * twice.
                         *
                         * If there is no limit on autodownload size, that's the same as the message
                         * being smaller than the max size
                         */
//                        if (account.getMaximumAutoDownloadMessageSize() == 0 || message.getSize() < account.getMaximumAutoDownloadMessageSize()) {
                            localMessage.setFlag(Flag.X_DOWNLOADED_FULL, true);
//                        } else {
//                            // Set a flag indicating that the message has been partially downloaded and
//                            // is ready for view.
//                            localMessage.setFlag(Flag.X_DOWNLOADED_PARTIAL, true);
//                        }
                    }
                }
            } else {
                /*
                 * We have a structure to deal with, from which
                 * we can pull down the parts we want to actually store.
                 * Build a list of parts we are interested in. Text parts will be downloaded
                 * right now, attachments will be left for later.
                 */

                Set<Part> viewables = MimeUtility.collectTextParts(message);

                /*
                 * Now download the parts we're interested in storing.
                 */
                for (Part part : viewables) {
                    remoteFolder.fetchPart(message, part, null);
                }
                // Store the updated message locally
                localFolder.appendMessages(new Message[] { message });

                Message localMessage = localFolder.getMessage(message.getUid());

                // Set a flag indicating this message has been fully downloaded and can be
                // viewed.
                localMessage.setFlag(Flag.X_DOWNLOADED_PARTIAL, true);
            }
            if (MailChat.DEBUG)
                Log.v(MailChat.LOG_TAG, "About to notify listeners that we got a new large message "
                      + account + ":" + folder + ":" + message.getUid());

            // Update the listener with what we've found
            progress.incrementAndGet();
            // TODO do we need to re-fetch this here?
            Message localMessage = localFolder.getMessage(message.getUid());

            // Increment the number of "new messages" if the newly downloaded message is
            // not marked as read.
            if (!localMessage.isSet(Flag.SEEN)) {
                newMessages.incrementAndGet();
            }

            for (MessagingListener l : getListeners()) {
                l.synchronizeMailboxAddOrUpdateMessage(account, folder, localMessage);
                l.synchronizeMailboxProgress(account, folder, progress.get(), todo);
                if (!localMessage.isSet(Flag.SEEN)) {
                    l.synchronizeMailboxNewMessage(account, folder, localMessage);
                }
            }

            // Send a notification of this message
            if (shouldNotifyForMessage(account, localFolder, message)) {
                // Notify with the localMessage so that we don't have to recalculate the content preview.
                notifyAccount(mApplication, account, localMessage, unreadBeforeStart);
            }

        }//for large messages
        if (MailChat.DEBUG)
            Log.d(MailChat.LOG_TAG, "SYNC: Done fetching large messages for folder " + folder);

    }

    private void refreshLocalMessageFlags(final Account account, final Folder remoteFolder,
                                          final LocalFolder localFolder,
                                          ArrayList<Message> syncFlagMessages,
                                          final AtomicInteger progress,
                                          final int todo
                                         ) throws MessagingException {

        final String folder = remoteFolder.getName();
        if (remoteFolder.supportsFetchingFlags()) {
            if (MailChat.DEBUG)
                Log.d(MailChat.LOG_TAG, "SYNC: About to sync flags for "
                      + syncFlagMessages.size() + " remote messages for folder " + folder);

            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.FLAGS);

            List<Message> undeletedMessages = new LinkedList<Message>();
            for (Message message : syncFlagMessages) {
                if (!message.isSet(Flag.DELETED)) {
                    undeletedMessages.add(message);
                }
            }

            remoteFolder.fetch(undeletedMessages.toArray(EMPTY_MESSAGE_ARRAY), fp, null);
            for (Message remoteMessage : syncFlagMessages) {
                Message localMessage = localFolder.getMessage(remoteMessage.getUid());
                boolean messageChanged = syncFlags(localMessage, remoteMessage);
                if (messageChanged) {
                    boolean shouldBeNotifiedOf = false;
                    if (localMessage.isSet(Flag.DELETED) || isMessageSuppressed(account, localMessage)) {
                        for (MessagingListener l : getListeners()) {
                            l.synchronizeMailboxRemovedMessage(account, folder, localMessage);
                        }
                    } else {
                        for (MessagingListener l : getListeners()) {
                            l.synchronizeMailboxAddOrUpdateMessage(account, folder, localMessage);
                        }
                        if (shouldNotifyForMessage(account, localFolder, localMessage)) {
                            shouldBeNotifiedOf = true;
                        }
                    }

                    // we're only interested in messages that need removing
                    if (!shouldBeNotifiedOf) {
                        NotificationData data = getNotificationData(account, null);
                        if (data != null) {
                            synchronized (data) {
                                MessageReference ref = localMessage.makeMessageReference();
                                if (data.removeMatchingMessage(mApplication, ref)) {
                                    notifyAccountWithDataLocked(mApplication, account, null, data);
                                }
                            }
                        }
                    }
                }
                progress.incrementAndGet();
                for (MessagingListener l : getListeners()) {
                    l.synchronizeMailboxProgress(account, folder, progress.get(), todo);
                }
            }
        }
    }

    private boolean syncFlags(Message localMessage, Message remoteMessage) throws MessagingException {
        boolean messageChanged = false;
        if (localMessage == null || localMessage.isSet(Flag.DELETED)) {
            return false;
        }
        if (remoteMessage.isSet(Flag.DELETED)) {
            if (localMessage.getFolder().getAccount().syncRemoteDeletions()) {
                localMessage.setFlag(Flag.DELETED, true);
                messageChanged = true;
            }
        } else {
            for (Flag flag : MessagingController.SYNC_FLAGS) {
                /*
                if (remoteMessage.isSet(flag) != localMessage.isSet(flag)) {
                    localMessage.setFlag(flag, remoteMessage.isSet(flag));
                    messageChanged = true;
                }
                */
            	// 修复服务器端不支持标签同步后消失问题
				// Modified by LL
				// BEGIN
				if ((remoteMessage.isSet(flag) != localMessage.isSet(flag))
						&& remoteMessage.getFolder().isFlagSupported(flag)) {
					localMessage.setFlag(flag, remoteMessage.isSet(flag));
					messageChanged = true;
				}
				// END
            }
        }
        return messageChanged;
    }

    private void queuePendingCommand(Account account, PendingCommand command) {
        try {
            LocalStore localStore = account.getLocalStore();
            localStore.addPendingCommand(command);
        } catch (Exception e) {
            addErrorMessage(account, null, e);

            throw new RuntimeException("Unable to enqueue pending command", e);
        }
    }

    private void processPendingCommands(final Account account) {
        putBackground("processPendingCommands", null, new Runnable() {
            @Override
            public void run() {
                try {
                    processPendingCommandsSynchronous(account);
                } catch (UnavailableStorageException e) {
                    Log.i(MailChat.LOG_TAG, "Failed to process pending command because storage is not available - trying again later.");
                    throw new UnavailableAccountException(e);
                } catch (MessagingException me) {
                    Log.e(MailChat.LOG_TAG, "processPendingCommands", me);

                    addErrorMessage(account, null, me);

                    /*
                     * Ignore any exceptions from the commands. Commands will be processed
                     * on the next round.
                     */
                }
            }
        });
    }

    private void processPendingCommandsSynchronous(Account account) throws MessagingException {
        LocalStore localStore = account.getLocalStore();
        ArrayList<PendingCommand> commands = localStore.getPendingCommands();

        int progress = 0;
        int todo = commands.size();
        if (todo == 0) {
            return;
        }

        for (MessagingListener l : getListeners()) {
            l.pendingCommandsProcessing(account);
            l.synchronizeMailboxProgress(account, null, progress, todo);
        }

        PendingCommand processingCommand = null;
        try {
            for (PendingCommand command : commands) {
                processingCommand = command;
                if (MailChat.DEBUG)
                    Log.d(MailChat.LOG_TAG, "Processing pending command '" + command + "'");

                String[] components = command.command.split("\\.");
                String commandTitle = components[components.length - 1];
                for (MessagingListener l : getListeners()) {
                    l.pendingCommandStarted(account, commandTitle);
                }
                /*
                 * We specifically do not catch any exceptions here. If a command fails it is
                 * most likely due to a server or IO error and it must be retried before any
                 * other command processes. This maintains the order of the commands.
                 */
                try {
                    if (PENDING_COMMAND_APPEND.equals(command.command)) {
                        processPendingAppend(command, account);
                    } else if (PENDING_COMMAND_SET_FLAG_BULK.equals(command.command)) {
                        processPendingSetFlag(command, account);
                    } else if (PENDING_COMMAND_SET_FLAG.equals(command.command)) {
                        processPendingSetFlagOld(command, account);
                    } else if (PENDING_COMMAND_MARK_ALL_AS_READ.equals(command.command)) {
                        processPendingMarkAllAsRead(command, account);
                    } else if (PENDING_COMMAND_MOVE_OR_COPY_BULK.equals(command.command)) {
                        processPendingMoveOrCopyOld2(command, account);
                    } else if (PENDING_COMMAND_MOVE_OR_COPY_BULK_NEW.equals(command.command)) {
                        processPendingMoveOrCopy(command, account);
                    } else if (PENDING_COMMAND_MOVE_OR_COPY.equals(command.command)) {
                        processPendingMoveOrCopyOld(command, account);
                    } else if (PENDING_COMMAND_EMPTY_TRASH.equals(command.command)) {
                        processPendingEmptyTrash(command, account);
                    } else if (PENDING_COMMAND_EXPUNGE.equals(command.command)) {
                        processPendingExpunge(command, account);
                    }
                    localStore.removePendingCommand(command);
                    if (MailChat.DEBUG)
                        Log.d(MailChat.LOG_TAG, "Done processing pending command '" + command + "'");
                } catch (MessagingException me) {
                    if (me.isPermanentFailure()) {
                        addErrorMessage(account, null, me);
                        Log.e(MailChat.LOG_TAG, "Failure of command '" + command + "' was permanent, removing command from queue");
                        localStore.removePendingCommand(processingCommand);
                    } else {
                        throw me;
                    }
                } finally {
                    progress++;
                    for (MessagingListener l : getListeners()) {
                        l.synchronizeMailboxProgress(account, null, progress, todo);
                        l.pendingCommandCompleted(account, commandTitle);
                    }
                }
            }
        } catch (MessagingException me) {
            notifyUserIfCertificateProblem(mApplication, me, account, true);
            addErrorMessage(account, null, me);
            Log.e(MailChat.LOG_TAG, "Could not process command '" + processingCommand + "'", me);
            throw me;
        } finally {
            for (MessagingListener l : getListeners()) {
                l.pendingCommandsFinished(account);
            }
        }
    }

    /**
     * Process a pending append message command. This command uploads a local message to the
     * server, first checking to be sure that the server message is not newer than
     * the local message. Once the local message is successfully processed it is deleted so
     * that the server message will be synchronized down without an additional copy being
     * created.
     * TODO update the local message UID instead of deleteing it
     *
     * @param command arguments = (String folder, String uid)
     * @param account
     * @throws MessagingException
     */
    private void processPendingAppend(PendingCommand command, Account account)
    throws MessagingException {
        Folder remoteFolder = null;
        LocalFolder localFolder = null;
        try {

            String folder = command.arguments[0];
            String uid = command.arguments[1];

            if (account.getErrorFolderName().equals(folder)) {
                return;
            }

            LocalStore localStore = account.getLocalStore();
            localFolder = localStore.getFolder(folder);
            LocalMessage localMessage = localFolder.getMessage(uid);

            if (localMessage == null) {
                return;
            }

            Store remoteStore = account.getRemoteStore();
            remoteFolder = remoteStore.getFolder(folder);
            if (!remoteFolder.exists()) {
                if (!remoteFolder.create(FolderType.HOLDS_MESSAGES)) {
                    return;
                }
            }
            remoteFolder.open(Folder.OPEN_MODE_RW);
            if (remoteFolder.getMode() != Folder.OPEN_MODE_RW) {
                return;
            }

            Message remoteMessage = null;
            if (!localMessage.getUid().startsWith(MailChat.LOCAL_UID_PREFIX)) {
                remoteMessage = remoteFolder.getMessage(localMessage.getUid());
            }

            if (remoteMessage == null) {
                // TODO： mail.35.cn 无法正确处理getUidFromMessageId()
                // Modified by LL for debug
                // BEGIN
                /*
                if (localMessage.isSet(Flag.X_REMOTE_COPY_STARTED)) {
                    Log.w(MailChat.LOG_TAG, "Local message with uid " + localMessage.getUid() +
                          " has flag " + Flag.X_REMOTE_COPY_STARTED + " already set, checking for remote message with " +
                          " same message id");
                    String rUid = remoteFolder.getUidFromMessageId(localMessage);
                    if (rUid != null) {
                        Log.w(MailChat.LOG_TAG, "Local message has flag " + Flag.X_REMOTE_COPY_STARTED + " already set, and there is a remote message with " +
                              " uid " + rUid + ", assuming message was already copied and aborting this copy");

                        String oldUid = localMessage.getUid();
                        localMessage.setUid(rUid);
                        localFolder.changeUid(localMessage);
                        for (MessagingListener l : getListeners()) {
                            l.messageUidChanged(account, folder, oldUid, localMessage.getUid());
                        }
                        return;
                    } else {
                        Log.w(MailChat.LOG_TAG, "No remote message with message-id found, proceeding with append");
                    }
                }
                */
                // END

                /*
                 * If the message does not exist remotely we just upload it and then
                 * update our local copy with the new uid.
                 */
                FetchProfile fp = new FetchProfile();
                fp.add(FetchProfile.Item.BODY);
                localFolder.fetch(new Message[] { localMessage } , fp, null);
                String oldUid = localMessage.getUid();
                
                // Modified by LL for debug
                //localMessage.setFlag(Flag.X_REMOTE_COPY_STARTED, true);
                
                remoteFolder.appendMessages(new Message[] { localMessage });

                localFolder.changeUid(localMessage);
                for (MessagingListener l : getListeners()) {
                    l.messageUidChanged(account, folder, oldUid, localMessage.getUid());
                }
            } else {
                /*
                // If the remote message exists we need to determine which copy to keep.
                // See if the remote message is newer than ours
                FetchProfile fp = new FetchProfile();
                fp.add(FetchProfile.Item.ENVELOPE);
                remoteFolder.fetch(new Message[] { remoteMessage }, fp, null);
                Date localDate = localMessage.getInternalDate();
                Date remoteDate = remoteMessage.getInternalDate();
                if (remoteDate != null && remoteDate.compareTo(localDate) > 0) {
                    // If the remote message is newer than ours we'll just
                    // delete ours and move on. A sync will get the server message
                    // if we need to be able to see it.
                    localMessage.destroy();
                } else {
                    // Otherwise we'll upload our message and then delete the remote message.
                    fp.clear();
                    fp = new FetchProfile();
                    fp.add(FetchProfile.Item.BODY);
                    localFolder.fetch(new Message[] { localMessage }, fp, null);
                    String oldUid = localMessage.getUid();

                    // Modified by LL for debug
                    //localMessage.setFlag(Flag.X_REMOTE_COPY_STARTED, true);

                    remoteFolder.appendMessages(new Message[] { localMessage });
                    localFolder.changeUid(localMessage);
                    for (MessagingListener l : getListeners()) {
                        l.messageUidChanged(account, folder, oldUid, localMessage.getUid());
                    }
                    if (remoteDate != null) {
                        remoteMessage.setFlag(Flag.DELETED, true);
                        if (Account.EXPUNGE_IMMEDIATELY.equals(account.getExpungePolicy())) {
                            remoteFolder.expunge();
                        }
                    }
                }
                */
                // Modified by LL for debug
                // BEGIN
                FetchProfile fp = new FetchProfile();
                fp.add(FetchProfile.Item.BODY);
                localFolder.fetch(new Message[] { localMessage }, fp, null);
                String oldUid = localMessage.getUid();

                // Modified by LL for debug
                //localMessage.setFlag(Flag.X_REMOTE_COPY_STARTED, true);

                remoteFolder.appendMessages(new Message[] { localMessage });
                localFolder.changeUid(localMessage);
                for (MessagingListener l : getListeners()) {
                    l.messageUidChanged(account, folder, oldUid, localMessage.getUid());
                }
                
                remoteMessage.setFlag(Flag.DELETED, true);
                if (Account.EXPUNGE_IMMEDIATELY.equals(account.getExpungePolicy())) {
                    remoteFolder.expunge();
                }
                // END
            }
        } finally {
            closeFolder(remoteFolder);
            closeFolder(localFolder);
        }
    }
    private void queueMoveOrCopy(Account account, String srcFolder, String destFolder, boolean isCopy, String uids[]) {
        if (account.getErrorFolderName().equals(srcFolder)) {
            return;
        }
        PendingCommand command = new PendingCommand();
        command.command = PENDING_COMMAND_MOVE_OR_COPY_BULK_NEW;

        int length = 4 + uids.length;
        command.arguments = new String[length];
        command.arguments[0] = srcFolder;
        command.arguments[1] = destFolder;
        command.arguments[2] = Boolean.toString(isCopy);
        command.arguments[3] = Boolean.toString(false);
        System.arraycopy(uids, 0, command.arguments, 4, uids.length);
        queuePendingCommand(account, command);
    }

    private void queueMoveOrCopy(Account account, String srcFolder, String destFolder, boolean isCopy, String uids[], Map<String, String> uidMap) {
        if (uidMap == null || uidMap.isEmpty()) {
            queueMoveOrCopy(account, srcFolder, destFolder, isCopy, uids);
        } else {
            if (account.getErrorFolderName().equals(srcFolder)) {
                return;
            }
            PendingCommand command = new PendingCommand();
            command.command = PENDING_COMMAND_MOVE_OR_COPY_BULK_NEW;

            int length = 4 + uidMap.keySet().size() + uidMap.values().size();
            command.arguments = new String[length];
            command.arguments[0] = srcFolder;
            command.arguments[1] = destFolder;
            command.arguments[2] = Boolean.toString(isCopy);
            command.arguments[3] = Boolean.toString(true);
            System.arraycopy(uidMap.keySet().toArray(), 0, command.arguments, 4, uidMap.keySet().size());
            System.arraycopy(uidMap.values().toArray(), 0, command.arguments, 4 + uidMap.keySet().size(), uidMap.values().size());
            queuePendingCommand(account, command);
        }
    }

    /**
     * Convert pending command to new format and call
     * {@link #processPendingMoveOrCopy(PendingCommand, Account)}.
     *
     * <p>
     * TODO: This method is obsolete and is only for transition from K-9 4.0 to K-9 4.2
     * Eventually, it should be removed.
     * </p>
     *
     * @param command
     *         Pending move/copy command in old format.
     * @param account
     *         The account the pending command belongs to.
     *
     * @throws MessagingException
     *         In case of an error.
     */
    private void processPendingMoveOrCopyOld2(PendingCommand command, Account account)
            throws MessagingException {
        PendingCommand newCommand = new PendingCommand();
        int len = command.arguments.length;
        newCommand.command = PENDING_COMMAND_MOVE_OR_COPY_BULK_NEW;
        newCommand.arguments = new String[len + 1];
        newCommand.arguments[0] = command.arguments[0];
        newCommand.arguments[1] = command.arguments[1];
        newCommand.arguments[2] = command.arguments[2];
        newCommand.arguments[3] = Boolean.toString(false);
        System.arraycopy(command.arguments, 3, newCommand.arguments, 4, len - 3);

        processPendingMoveOrCopy(newCommand, account);
    }

    /**
     * Process a pending trash message command.
     *
     * @param command arguments = (String folder, String uid)
     * @param account
     * @throws MessagingException
     */
    private void processPendingMoveOrCopy(PendingCommand command, Account account)
    throws MessagingException {
        Folder remoteSrcFolder = null;
        Folder remoteDestFolder = null;
        LocalFolder localDestFolder = null;
        try {
            String srcFolder = command.arguments[0];
            if (account.getErrorFolderName().equals(srcFolder)) {
                return;
            }
            String destFolder = command.arguments[1];
            String isCopyS = command.arguments[2];
            String hasNewUidsS = command.arguments[3];

            boolean hasNewUids = false;
            if (hasNewUidsS != null) {
                hasNewUids = Boolean.parseBoolean(hasNewUidsS);
            }

            Store remoteStore = account.getRemoteStore();
            remoteSrcFolder = remoteStore.getFolder(srcFolder);

            Store localStore = account.getLocalStore();
            localDestFolder = (LocalFolder) localStore.getFolder(destFolder);
            List<Message> messages = new ArrayList<Message>();

            /*
             * We split up the localUidMap into two parts while sending the command, here we assemble it back.
             */
            Map<String, String> localUidMap = new HashMap<String, String>();
            if (hasNewUids) {
                int offset = (command.arguments.length - 4) / 2;

                for (int i = 4; i < 4 + offset; i++) {
                    localUidMap.put(command.arguments[i], command.arguments[i + offset]);

                    String uid = command.arguments[i];
                    if (!uid.startsWith(MailChat.LOCAL_UID_PREFIX)) {
                        messages.add(remoteSrcFolder.getMessage(uid));
                    }
                }

            } else {
                for (int i = 4; i < command.arguments.length; i++) {
                    String uid = command.arguments[i];
                    if (!uid.startsWith(MailChat.LOCAL_UID_PREFIX)) {
                        messages.add(remoteSrcFolder.getMessage(uid));
                    }
                }
            }

            boolean isCopy = false;
            if (isCopyS != null) {
                isCopy = Boolean.parseBoolean(isCopyS);
            }

            if (!remoteSrcFolder.exists()) {
                throw new MessagingException("processingPendingMoveOrCopy: remoteFolder " + srcFolder + " does not exist", true);
            }
            remoteSrcFolder.open(Folder.OPEN_MODE_RW);
            if (remoteSrcFolder.getMode() != Folder.OPEN_MODE_RW) {
                throw new MessagingException("processingPendingMoveOrCopy: could not open remoteSrcFolder " + srcFolder + " read/write", true);
            }

            if (MailChat.DEBUG)
                Log.d(MailChat.LOG_TAG, "processingPendingMoveOrCopy: source folder = " + srcFolder
                      + ", " + messages.size() + " messages, destination folder = " + destFolder + ", isCopy = " + isCopy);

            Map <String, String> remoteUidMap = null;

            if (!isCopy && destFolder.equals(account.getTrashFolderName())) {
                if (MailChat.DEBUG)
                    Log.d(MailChat.LOG_TAG, "processingPendingMoveOrCopy doing special case for deleting message");

                String destFolderName = destFolder;
                if (MailChat.FOLDER_NONE.equals(destFolderName)) {
                    destFolderName = null;
                }
                remoteSrcFolder.delete(messages.toArray(EMPTY_MESSAGE_ARRAY), destFolderName);
            } else {
                remoteDestFolder = remoteStore.getFolder(destFolder);

                if (isCopy) {
                    remoteUidMap = remoteSrcFolder.copyMessages(messages.toArray(EMPTY_MESSAGE_ARRAY), remoteDestFolder);
                } else {
                    remoteUidMap = remoteSrcFolder.moveMessages(messages.toArray(EMPTY_MESSAGE_ARRAY), remoteDestFolder);
                }
            }
            if (!isCopy && Account.EXPUNGE_IMMEDIATELY.equals(account.getExpungePolicy())) {
                if (MailChat.DEBUG)
                    Log.i(MailChat.LOG_TAG, "processingPendingMoveOrCopy expunging folder " + account.getDescription() + ":" + srcFolder);

                remoteSrcFolder.expunge();
            }

            /*
             * This next part is used to bring the local UIDs of the local destination folder
             * upto speed with the remote UIDs of remote destination folder.
             */
            if (!localUidMap.isEmpty() && remoteUidMap != null && !remoteUidMap.isEmpty()) {
                for (Map.Entry<String, String> entry : remoteUidMap.entrySet()) {
                    String remoteSrcUid = entry.getKey();
                    String localDestUid = localUidMap.get(remoteSrcUid);
                    String newUid = entry.getValue();

                    Message localDestMessage = localDestFolder.getMessage(localDestUid);
                    if (localDestMessage != null) {
                        localDestMessage.setUid(newUid);
                        localDestFolder.changeUid((LocalMessage)localDestMessage);
                        for (MessagingListener l : getListeners()) {
                            l.messageUidChanged(account, destFolder, localDestUid, newUid);
                        }
                    }
                }
            }
        } finally {
            closeFolder(remoteSrcFolder);
            closeFolder(remoteDestFolder);
        }
    }

    private void queueSetFlag(final Account account, final String folderName, final String newState, final String flag, final String[] uids) {
        putBackground("queueSetFlag " + account.getDescription() + ":" + folderName, null, new Runnable() {
            @Override
            public void run() {
                PendingCommand command = new PendingCommand();
                command.command = PENDING_COMMAND_SET_FLAG_BULK;
                int length = 3 + uids.length;
                command.arguments = new String[length];
                command.arguments[0] = folderName;
                command.arguments[1] = newState;
                command.arguments[2] = flag;
                System.arraycopy(uids, 0, command.arguments, 3, uids.length);
                queuePendingCommand(account, command);
                processPendingCommands(account);
            }
        });
    }
    /**
     * Processes a pending mark read or unread command.
     *
     * @param command arguments = (String folder, String uid, boolean read)
     * @param account
     */
    private void processPendingSetFlag(PendingCommand command, Account account)
    throws MessagingException {
        String folder = command.arguments[0];

        if (account.getErrorFolderName().equals(folder)) {
            return;
        }

        boolean newState = Boolean.parseBoolean(command.arguments[1]);

        Flag flag = Flag.valueOf(command.arguments[2]);

        Store remoteStore = account.getRemoteStore();
        Folder remoteFolder = remoteStore.getFolder(folder);
        if (!remoteFolder.exists() || !remoteFolder.isFlagSupported(flag)) {
            return;
        }

        try {
            remoteFolder.open(Folder.OPEN_MODE_RW);
            if (remoteFolder.getMode() != Folder.OPEN_MODE_RW) {
                return;
            }
            List<Message> messages = new ArrayList<Message>();
            for (int i = 3; i < command.arguments.length; i++) {
                String uid = command.arguments[i];
                if (!uid.startsWith(MailChat.LOCAL_UID_PREFIX)) {
                    messages.add(remoteFolder.getMessage(uid));
                }
            }

            if (messages.isEmpty()) {
                return;
            }
            remoteFolder.setFlags(messages.toArray(EMPTY_MESSAGE_ARRAY), new Flag[] { flag }, newState);
        } finally {
            closeFolder(remoteFolder);
        }
    }

    // TODO: This method is obsolete and is only for transition from K-9 2.0 to K-9 2.1
    // Eventually, it should be removed
    private void processPendingSetFlagOld(PendingCommand command, Account account)
    throws MessagingException {
        String folder = command.arguments[0];
        String uid = command.arguments[1];

        if (account.getErrorFolderName().equals(folder)) {
            return;
        }
        if (MailChat.DEBUG)
            Log.d(MailChat.LOG_TAG, "processPendingSetFlagOld: folder = " + folder + ", uid = " + uid);

        boolean newState = Boolean.parseBoolean(command.arguments[2]);

        Flag flag = Flag.valueOf(command.arguments[3]);
        Folder remoteFolder = null;
        try {
            Store remoteStore = account.getRemoteStore();
            remoteFolder = remoteStore.getFolder(folder);
            if (!remoteFolder.exists()) {
                return;
            }
            remoteFolder.open(Folder.OPEN_MODE_RW);
            if (remoteFolder.getMode() != Folder.OPEN_MODE_RW) {
                return;
            }
            Message remoteMessage = null;
            if (!uid.startsWith(MailChat.LOCAL_UID_PREFIX)) {
                remoteMessage = remoteFolder.getMessage(uid);
            }
            if (remoteMessage == null) {
                return;
            }
            remoteMessage.setFlag(flag, newState);
        } finally {
            closeFolder(remoteFolder);
        }
    }
    private void queueExpunge(final Account account, final String folderName) {
        putBackground("queueExpunge " + account.getDescription() + ":" + folderName, null, new Runnable() {
            @Override
            public void run() {
                PendingCommand command = new PendingCommand();
                command.command = PENDING_COMMAND_EXPUNGE;

                command.arguments = new String[1];

                command.arguments[0] = folderName;
                queuePendingCommand(account, command);
                processPendingCommands(account);
            }
        });
    }
    private void processPendingExpunge(PendingCommand command, Account account)
    throws MessagingException {
        String folder = command.arguments[0];

        if (account.getErrorFolderName().equals(folder)) {
            return;
        }
        if (MailChat.DEBUG)
            Log.d(MailChat.LOG_TAG, "processPendingExpunge: folder = " + folder);

        Store remoteStore = account.getRemoteStore();
        Folder remoteFolder = remoteStore.getFolder(folder);
        try {
            if (!remoteFolder.exists()) {
                return;
            }
            remoteFolder.open(Folder.OPEN_MODE_RW);
            if (remoteFolder.getMode() != Folder.OPEN_MODE_RW) {
                return;
            }
            remoteFolder.expunge();
            if (MailChat.DEBUG)
                Log.d(MailChat.LOG_TAG, "processPendingExpunge: complete for folder = " + folder);
        } finally {
            closeFolder(remoteFolder);
        }
    }


    // TODO: This method is obsolete and is only for transition from K-9 2.0 to K-9 2.1
    // Eventually, it should be removed
    private void processPendingMoveOrCopyOld(PendingCommand command, Account account)
    throws MessagingException {
        String srcFolder = command.arguments[0];
        String uid = command.arguments[1];
        String destFolder = command.arguments[2];
        String isCopyS = command.arguments[3];

        boolean isCopy = false;
        if (isCopyS != null) {
            isCopy = Boolean.parseBoolean(isCopyS);
        }

        if (account.getErrorFolderName().equals(srcFolder)) {
            return;
        }

        Store remoteStore = account.getRemoteStore();
        Folder remoteSrcFolder = remoteStore.getFolder(srcFolder);
        Folder remoteDestFolder = remoteStore.getFolder(destFolder);

        if (!remoteSrcFolder.exists()) {
            throw new MessagingException("processPendingMoveOrCopyOld: remoteFolder " + srcFolder + " does not exist", true);
        }
        remoteSrcFolder.open(Folder.OPEN_MODE_RW);
        if (remoteSrcFolder.getMode() != Folder.OPEN_MODE_RW) {
            throw new MessagingException("processPendingMoveOrCopyOld: could not open remoteSrcFolder " + srcFolder + " read/write", true);
        }

        Message remoteMessage = null;
        if (!uid.startsWith(MailChat.LOCAL_UID_PREFIX)) {
            remoteMessage = remoteSrcFolder.getMessage(uid);
        }
        if (remoteMessage == null) {
            throw new MessagingException("processPendingMoveOrCopyOld: remoteMessage " + uid + " does not exist", true);
        }

        if (MailChat.DEBUG)
            Log.d(MailChat.LOG_TAG, "processPendingMoveOrCopyOld: source folder = " + srcFolder
                  + ", uid = " + uid + ", destination folder = " + destFolder + ", isCopy = " + isCopy);

        if (!isCopy && destFolder.equals(account.getTrashFolderName())) {
            if (MailChat.DEBUG)
                Log.d(MailChat.LOG_TAG, "processPendingMoveOrCopyOld doing special case for deleting message");

            remoteMessage.delete(account.getTrashFolderName());
            remoteSrcFolder.close();
            return;
        }

        remoteDestFolder.open(Folder.OPEN_MODE_RW);
        if (remoteDestFolder.getMode() != Folder.OPEN_MODE_RW) {
            throw new MessagingException("processPendingMoveOrCopyOld: could not open remoteDestFolder " + srcFolder + " read/write", true);
        }

        if (isCopy) {
            remoteSrcFolder.copyMessages(new Message[] { remoteMessage }, remoteDestFolder);
        } else {
            remoteSrcFolder.moveMessages(new Message[] { remoteMessage }, remoteDestFolder);
        }
        remoteSrcFolder.close();
        remoteDestFolder.close();
    }

    private void processPendingMarkAllAsRead(PendingCommand command, Account account) throws MessagingException {
        String folder = command.arguments[0];
        Folder remoteFolder = null;
        LocalFolder localFolder = null;
        try {
            Store localStore = account.getLocalStore();
            localFolder = (LocalFolder) localStore.getFolder(folder);
            localFolder.open(Folder.OPEN_MODE_RW);
            Message[] messages = localFolder.getMessages(null, false);
            ArrayList<Message> remoteMessages = new ArrayList<Message>();
            for (Message message : messages) {
                if (!message.isSet(Flag.SEEN)) {
                    message.setFlag(Flag.SEEN, true);
                    for (MessagingListener l : getListeners()) {
                        l.listLocalMessagesUpdateMessage(account, folder, message);
                    }
                    String uid = message.getUid();
                    if (uid != null && !uid.startsWith(MailChat.LOCAL_UID_PREFIX)) {
                        remoteMessages.add(message);
                    }
                }
            }

            for (MessagingListener l : getListeners()) {
                l.folderStatusChanged(account, folder, 0);
            }


            if (account.getErrorFolderName().equals(folder)) {
                return;
            }

            if (remoteMessages.size() > 0) {
                Store remoteStore = account.getRemoteStore();
                remoteFolder = remoteStore.getFolder(folder);

                if (!remoteFolder.exists() || !remoteFolder.isFlagSupported(Flag.SEEN)) {
                    return;
                }
                remoteFolder.open(Folder.OPEN_MODE_RW);
                if (remoteFolder.getMode() != Folder.OPEN_MODE_RW) {
                    return;
                }

                remoteFolder.setFlags(remoteMessages.toArray(EMPTY_MESSAGE_ARRAY), new Flag[] {Flag.SEEN}, true);
                remoteFolder.close();
            }
        } catch (UnsupportedOperationException uoe) {
            Log.w(MailChat.LOG_TAG, "Could not mark all server-side as read because store doesn't support operation", uoe);
        } finally {
            closeFolder(localFolder);
            closeFolder(remoteFolder);
        }
    }

    void notifyUserIfCertificateProblem(Context context, Exception e,
            Account account, boolean incoming) {
		if (account.isHideAccount()) {
			return;
		}
        /*
    	if (!(e instanceof CertificateValidationException)) {
        	return;
    	}
    	
    	CertificateValidationException cve = (CertificateValidationException) e;
        if (!cve.needsUserAttention()) {
            return;
        }
    	*/
    	// 增加对AuthenticationFailedException的处理
    	if (e instanceof CertificateValidationException) {
    		CertificateValidationException cve = (CertificateValidationException) e;
            if (!cve.needsUserAttention()) {
                return;
            }
    	} else if (!(e instanceof AuthenticationFailedException)) {
    		return;
    	}

		account.setAuthenticated(false);
		account.save(Preferences.getPreferences(context));
	    int version = account.getVersion_35Mail();
	    if (version == 1 || version == 2) {
	        try {
	            WeemailUtil.deleteC35Account(account);
	        } catch (Exception ex) {
	            Log.e(MailChat.LOG_COLLECTOR_TAG, "删除微妹账号失败", ex);
	        }
	    }

		for (MessagingListener l : getListeners(null)) {
			l.UserIfCertificateProblem(account);
		}
//        final int id = incoming
//                ? MailChat.CERTIFICATE_EXCEPTION_NOTIFICATION_INCOMING + account.getAccountNumber()
//                : MailChat.CERTIFICATE_EXCEPTION_NOTIFICATION_OUTGOING + account.getAccountNumber();
//        final Intent i = incoming
//                ? AccountSetupIncoming.intentActionEditIncomingSettings(context, account)
//                : AccountSetupOutgoing.intentActionEditOutgoingSettings(context, account);
//
//        // 标记因认证错误需要对已有账号配置进行更新
//        i.putExtra(MailChat.EXTRA_ACCOUNT_UPDATE, true);
//        final PendingIntent pi = PendingIntent.getActivity(context,
//                account.getAccountNumber(), i, PendingIntent.FLAG_UPDATE_CURRENT);
//        final String title = context.getString(
//                R.string.notification_certificate_error_title, account.getDescription());
//
//        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
//        builder.setSmallIcon(R.drawable.icon_notification_mail_small);
//		builder.setLargeIcon(BitmapFactory.decodeResource(
//				mApplication.getResources(), R.drawable.icon_notification));
//        builder.setWhen(System.currentTimeMillis());
//        builder.setAutoCancel(true);
//        builder.setTicker(title);
//        builder.setContentTitle(title);
//        builder.setContentText(context.getString(R.string.notification_certificate_error_text));
//        builder.setContentIntent(pi);
//
//        configureNotification(builder, null, null,
//                MailChat.NOTIFICATION_LED_FAILURE_COLOR,
//                MailChat.NOTIFICATION_LED_BLINK_FAST, true);
//
//        final NotificationManager nm = (NotificationManager)
//                context.getSystemService(Context.NOTIFICATION_SERVICE);
//        nm.notify(null, id, builder.build());
    }

    public void clearCertificateErrorNotifications(Context context,
            final Account account, CheckDirection direction) {
        final NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (direction == CheckDirection.INCOMING) {
            nm.cancel(null, MailChat.CERTIFICATE_EXCEPTION_NOTIFICATION_INCOMING + account.getAccountNumber());
        } else {
            nm.cancel(null, MailChat.CERTIFICATE_EXCEPTION_NOTIFICATION_OUTGOING + account.getAccountNumber());
        }
    }


    static long uidfill = 0;
    static AtomicBoolean loopCatch = new AtomicBoolean();
    /**
     * 
     * method name: addErrorMessage 
     * function @Description: TODO
     * Parameters and return values description：
     *      @param account
     *      @param subject
     *      @param t    field_name
     *      void    return type
     *  @History memory：
     *     @Date：2014-10-30 上午11:06:39	@Modified by：zhangjx
     *     @Description：发送错误日志给yqfeedback@35.cn
     */
    public void addErrorMessage(Account account, String subject, Throwable t) {
    	// 不再通过邮件发送错误日志
    	// Modified by LL
    	/*
        try {
            if (t == null) {
                return;
            }

            CharArrayWriter baos = new CharArrayWriter(t.getStackTrace().length * 10);
            PrintWriter ps = new PrintWriter(baos);
            try {
                PackageInfo packageInfo = mApplication.getPackageManager().getPackageInfo(
                        mApplication.getPackageName(), 0);
                ps.format("MailChat version: %s\r\n", packageInfo.versionName);
            } catch (Exception e) {
                // ignore
            }
            ps.format("Device make: %s\r\n", Build.MANUFACTURER);
            ps.format("Device model: %s\r\n", Build.MODEL);
            ps.format("Android version: %s\r\n\r\n", Build.VERSION.RELEASE);
            t.printStackTrace(ps);
            ps.close();

            if (subject == null) {
                subject = getRootCauseMessage(t);
            }

            addErrorMessage(account, subject, baos.toString());
        } catch (Throwable it) {
            Log.e(MailChat.LOG_TAG, "Could not save error message to " + account.getErrorFolderName(), it);
        }
        */
    }

    public void addErrorMessage(Account account, String subject, String body) {
        if (!MailChat.DEBUG) {
            return;
        }
        if (!loopCatch.compareAndSet(false, true)) {
            return;
        }
        try {
            if (body == null || body.length() < 1) {
                return;
            }
            Store localStore = account.getLocalStore();
            LocalFolder localFolder = (LocalFolder)localStore.getFolder(account.getErrorFolderName());
            Message[] messages = new Message[1];
            MimeMessage message = new MimeMessage();


            message.setBody(new TextBody(body));
            message.setFlag(Flag.X_DOWNLOADED_FULL, true);
            message.setSubject(subject);
            long nowTime = System.currentTimeMillis();
            Date nowDate = new Date(nowTime);
            message.setInternalDate(nowDate);
            message.addSentDate(nowDate);
            message.setFrom(new Address(account.getEmail(), account.getName()));
//            message.setRecipients(RecipientType.TO,Address.parseUnencoded(MailChat.BUG_EMAIL));
            messages[0] = message;
//            localFolder.open(Folder.OPEN_MODE_RW);
//            localFolder.appendMessages(new Message[] { message });
            localFolder.appendMessages(messages);
            localFolder.clearMessagesOlderThan(nowTime - (15 * 60 * 1000));
//            localFolder.close();
//            sendPendingMessages(account, null);
        } catch (Throwable it) {
            Log.e(MailChat.LOG_TAG, "Could not save error message to " + account.getErrorFolderName(), it);
        } finally {
            loopCatch.set(false);
        }
    }



    public void markAllMessagesRead(final Account account, final String folder) {

        if (MailChat.DEBUG)
            Log.i(MailChat.LOG_TAG, "Marking all messages in " + account.getDescription() + ":" + folder + " as read");
        List<String> args = new ArrayList<String>();
        args.add(folder);
        PendingCommand command = new PendingCommand();
        command.command = PENDING_COMMAND_MARK_ALL_AS_READ;
        command.arguments = args.toArray(EMPTY_STRING_ARRAY);
        queuePendingCommand(account, command);
        processPendingCommands(account);
    }

    public void setFlag(final Account account, final List<Long> messageIds, final Flag flag,
            final boolean newState) {

        setFlagInCache(account, messageIds, flag, newState);

        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                setFlagSynchronous(account, messageIds, flag, newState, false);
            }
        });
    }

    public void setFlagForThreads(final Account account, final List<Long> threadRootIds,
            final Flag flag, final boolean newState) {

        setFlagForThreadsInCache(account, threadRootIds, flag, newState);

        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                setFlagSynchronous(account, threadRootIds, flag, newState, true);
            }
        });
    }

    private void setFlagSynchronous(final Account account, final List<Long> ids,
            final Flag flag, final boolean newState, final boolean threadedList) {

        LocalStore localStore;
        try {
            localStore = account.getLocalStore();
        } catch (MessagingException e) {
            Log.e(MailChat.LOG_TAG, "Couldn't get LocalStore instance", e);
            return;
        }

        // Update affected messages in the database. This should be as fast as possible so the UI
        // can be updated with the new state.
        try {
            if (threadedList) {
                localStore.setFlagForThreads(ids, flag, newState);
                removeFlagForThreadsFromCache(account, ids, flag);
            } else {
                localStore.setFlag(ids, flag, newState);
                removeFlagFromCache(account, ids, flag);
            }
        } catch (MessagingException e) {
            Log.e(MailChat.LOG_TAG, "Couldn't set flags in local database", e);
        }

        // Read folder name and UID of messages from the database
        Map<String, List<String>> folderMap;
        try {
            folderMap = localStore.getFoldersAndUids(ids, threadedList);
        } catch (MessagingException e) {
            Log.e(MailChat.LOG_TAG, "Couldn't get folder name and UID of messages", e);
            return;
        }

        // Loop over all folders
        for (Entry<String, List<String>> entry : folderMap.entrySet()) {
            String folderName = entry.getKey();

            // Notify listeners of changed folder status
            LocalFolder localFolder = localStore.getFolder(folderName);
            try {
                int unreadMessageCount = localFolder.getUnreadMessageCount();
                for (MessagingListener l : getListeners()) {
                    l.folderStatusChanged(account, folderName, unreadMessageCount);
                }
            } catch (MessagingException e) {
                Log.w(MailChat.LOG_TAG, "Couldn't get unread count for folder: " + folderName, e);
            }

            // The error folder is always a local folder
            // TODO: Skip the remote part for all local-only folders
            if (account.getErrorFolderName().equals(folderName)) {
                continue;
            }

            // Send flag change to server
            String[] uids = entry.getValue().toArray(EMPTY_STRING_ARRAY);
            queueSetFlag(account, folderName, Boolean.toString(newState), flag.toString(), uids);
            processPendingCommands(account);
        }
    }

    /**
     * Set or remove a flag for a set of messages in a specific folder.
     *
     * <p>
     * The {@link Message} objects passed in are updated to reflect the new flag state.
     * </p>
     *
     * @param account
     *         The account the folder containing the messages belongs to.
     * @param folderName
     *         The name of the folder.
     * @param messages
     *         The messages to change the flag for.
     * @param flag
     *         The flag to change.
     * @param newState
     *         {@code true}, if the flag should be set. {@code false} if it should be removed.
     */
    public void setFlag(Account account, String folderName, Message[] messages, Flag flag,
            boolean newState) {
        // TODO: Put this into the background, but right now some callers depend on the message
        //       objects being modified right after this method returns.
        Folder localFolder = null;
        try {
            Store localStore = account.getLocalStore();
            localFolder = localStore.getFolder(folderName);
            localFolder.open(Folder.OPEN_MODE_RW);

            // Allows for re-allowing sending of messages that could not be sent
            if (flag == Flag.FLAGGED && !newState &&
                    account.getOutboxFolderName().equals(folderName)) {
                for (Message message : messages) {
                    String uid = message.getUid();
                    if (uid != null) {
                        sendCount.remove(uid);
                    }
                }
            }

            // Update the messages in the local store
            localFolder.setFlags(messages, new Flag[] {flag}, newState);

            int unreadMessageCount = localFolder.getUnreadMessageCount();
            for (MessagingListener l : getListeners()) {
                l.folderStatusChanged(account, folderName, unreadMessageCount);
            }


            /*
             * Handle the remote side
             */

            // The error folder is always a local folder
            // TODO: Skip the remote part for all local-only folders
            if (account.getErrorFolderName().equals(folderName)) {
                return;
            }

            String[] uids = new String[messages.length];
            for (int i = 0, end = uids.length; i < end; i++) {
                uids[i] = messages[i].getUid();
            }

            queueSetFlag(account, folderName, Boolean.toString(newState), flag.toString(), uids);
            processPendingCommands(account);
        } catch (MessagingException me) {
            addErrorMessage(account, null, me);
            throw new RuntimeException(me);
        } finally {
            closeFolder(localFolder);
        }
    }

    /**
     * Set or remove a flag for a message referenced by message UID.
     *
     * @param account
     *         The account the folder containing the message belongs to.
     * @param folderName
     *         The name of the folder.
     * @param uid
     *         The UID of the message to change the flag for.
     * @param flag
     *         The flag to change.
     * @param newState
     *         {@code true}, if the flag should be set. {@code false} if it should be removed.
     */
    public void setFlag(Account account, String folderName, String uid, Flag flag,
            boolean newState) {
        Folder localFolder = null;
        try {
            LocalStore localStore = account.getLocalStore();
            localFolder = localStore.getFolder(folderName);
            localFolder.open(Folder.OPEN_MODE_RW);

            Message message = localFolder.getMessage(uid);
            if (message != null) {
                setFlag(account, folderName, new Message[] { message }, flag, newState);
            }
        } catch (MessagingException me) {
            addErrorMessage(account, null, me);
            throw new RuntimeException(me);
        } finally {
            closeFolder(localFolder);
        }
    }

    public void clearAllPending(final Account account) {
        try {
            Log.w(MailChat.LOG_TAG, "Clearing pending commands!");
            LocalStore localStore = account.getLocalStore();
            localStore.removePendingCommands();
        } catch (MessagingException me) {
            Log.e(MailChat.LOG_TAG, "Unable to clear pending command", me);
            addErrorMessage(account, null, me);
        }
    }

    public void loadMessageForViewRemote(final Account account, final String folder,
                                         final String uid, final MessagingListener listener) {
        put("loadMessageForViewRemote", listener, new Runnable() {
            @Override
            public void run() {
                loadMessageForViewRemoteSynchronous(account, folder, uid, listener, false, false);
            }
        });
    }

    public boolean loadMessageForViewRemoteSynchronous(final Account account, final String folder,
            final String uid, final MessagingListener listener, final boolean force,
            final boolean loadPartialFromSearch) {
        Folder remoteFolder = null;
        LocalFolder localFolder = null;
        try {
            LocalStore localStore = account.getLocalStore();
            localFolder = localStore.getFolder(folder);
            localFolder.open(Folder.OPEN_MODE_RW);

            Message message = localFolder.getMessage(uid);

            if (uid.startsWith(MailChat.LOCAL_UID_PREFIX)) {
                Log.w(MailChat.LOG_TAG, "Message has local UID so cannot download fully.");

                /*
                // ASH move toast
                android.widget.Toast.makeText(mApplication,
                        "Message has local UID so cannot download fully",
                        android.widget.Toast.LENGTH_LONG).show();
                */
                MailChat.toast("!");

                // TODO: Using X_DOWNLOADED_FULL is wrong because it's only a partial message. But
                // one we can't download completely. Maybe add a new flag; X_PARTIAL_MESSAGE ?
                message.setFlag(Flag.X_DOWNLOADED_FULL, true);
                message.setFlag(Flag.X_DOWNLOADED_PARTIAL, false);
            }
            /* commented out because this was pulled from another unmerged branch:
            } else if (localFolder.isLocalOnly() && !force) {
                Log.w(MailChat.LOG_TAG, "Message in local-only folder so cannot download fully.");
                // ASH move toast
                android.widget.Toast.makeText(mApplication,
                        "Message in local-only folder so cannot download fully",
                        android.widget.Toast.LENGTH_LONG).show();
                message.setFlag(Flag.X_DOWNLOADED_FULL, true);
                message.setFlag(Flag.X_DOWNLOADED_PARTIAL, false);
            }*/

            if (message.isSet(Flag.X_DOWNLOADED_FULL)) {
                /*
                 * If the message has been synchronized since we were called we'll
                 * just hand it back cause it's ready to go.
                 */
                FetchProfile fp = new FetchProfile();
                fp.add(FetchProfile.Item.ENVELOPE);
                fp.add(FetchProfile.Item.BODY);
                localFolder.fetch(new Message[] { message }, fp, null);
            } else {
            	// 未完成加载的邮件，尽量先显示已加载的内容，
            	// 以免在加载完成前始终显示空白页面。
            	// Modified by LL
            	// BEGIN
            	FetchProfile fp = new FetchProfile();
                fp.add(FetchProfile.Item.ENVELOPE);
                fp.add(FetchProfile.Item.BODY);
            	localFolder.fetch(new Message[] { message }, fp, null);
                
                for (MessagingListener l : getListeners(listener)) {
                    l.loadMessageForViewHeadersAvailable(account, folder, uid, message);
                }
	            for (MessagingListener l : getListeners(listener)) {
	                l.loadMessageForViewBodyAvailable(account, folder, uid, message);
	            }
            	// END
            	
                /*
                 * At this point the message is not available, so we need to download it
                 * fully if possible.
                 */
                Store remoteStore = account.getRemoteStore();
                remoteFolder = remoteStore.getFolder(folder);
                remoteFolder.open(Folder.OPEN_MODE_RW);

                // Get the remote message and fully download it
                Message remoteMessage = remoteFolder.getMessage(uid);
                
                //FetchProfile fp = new FetchProfile();
                // Modified by LL
                fp.clear();
                
                /*
                fp.add(FetchProfile.Item.BODY);

                remoteFolder.fetch(new Message[] { remoteMessage }, fp, null);

                // Store the message locally and load the stored message into memory
                localFolder.appendMessages(new Message[] { remoteMessage });
                */
                // 装载邮件时分段加载
                // Modified by LL
                loadMessageText(account, remoteFolder, localFolder, remoteMessage);
                
                if (loadPartialFromSearch) {
                    fp.add(FetchProfile.Item.BODY);
                }
                fp.add(FetchProfile.Item.ENVELOPE);
                message = localFolder.getMessage(uid);
                localFolder.fetch(new Message[] { message }, fp, null);
                
                /*
                // Mark that this message is now fully synched
                if (account.isMarkMessageAsReadOnView()) {
                    message.setFlag(Flag.SEEN, true);
                }
                */
                
                message.setFlag(Flag.X_DOWNLOADED_FULL, true);
            }
            
            markMessageAsReadOnView(account, message);

            // now that we have the full message, refresh the headers
            for (MessagingListener l : getListeners(listener)) {
                l.loadMessageForViewHeadersAvailable(account, folder, uid, message);
            }

            for (MessagingListener l : getListeners(listener)) {
                l.loadMessageForViewBodyAvailable(account, folder, uid, message);
            }
            for (MessagingListener l : getListeners(listener)) {
                l.loadMessageForViewFinished(account, folder, uid, message);
            }
            return true;
        } catch (Exception e) {
            for (MessagingListener l : getListeners(listener)) {
                l.loadMessageForViewFailed(account, folder, uid, e);
            }
            notifyUserIfCertificateProblem(mApplication, e, account, true);
            addErrorMessage(account, null, e);
            return false;
        } finally {
            closeFolder(remoteFolder);
            closeFolder(localFolder);
        }
    }

    public void loadMessageForView(final Account account, final String folder, final String uid,
                                   final MessagingListener listener) {
        for (MessagingListener l : getListeners(listener)) {
            l.loadMessageForViewStarted(account, folder, uid);
        }
        threadPool.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    LocalStore localStore = account.getLocalStore();
                    LocalFolder localFolder = localStore.getFolder(folder);
                    localFolder.open(Folder.OPEN_MODE_RW);

                    LocalMessage message = localFolder.getMessage(uid);
                    if (message == null || message.getId() == 0) {
                        throw new IllegalArgumentException("Message not found: folder=" + folder + ", uid=" + uid);
                    }
                    // IMAP search results will usually need to be downloaded before viewing.
                    // TODO: limit by account.getMaximumAutoDownloadMessageSize().
                    if (!message.isSet(Flag.X_DOWNLOADED_FULL) &&
                            !message.isSet(Flag.X_DOWNLOADED_PARTIAL)) {
                        if (loadMessageForViewRemoteSynchronous(account, folder, uid, listener,
                                false, true)) {

                            markMessageAsReadOnView(account, message);
                        }
                        return;
                    }


                    for (MessagingListener l : getListeners(listener)) {
                        l.loadMessageForViewHeadersAvailable(account, folder, uid, message);
                    }

                    FetchProfile fp = new FetchProfile();
                    fp.add(FetchProfile.Item.ENVELOPE);
                    fp.add(FetchProfile.Item.BODY);
                    localFolder.fetch(new Message[] {
                                          message
                                      }, fp, null);
                    localFolder.close();

                    for (MessagingListener l : getListeners(listener)) {
                        l.loadMessageForViewBodyAvailable(account, folder, uid, message);
                    }

                    for (MessagingListener l : getListeners(listener)) {
                        l.loadMessageForViewFinished(account, folder, uid, message);
                    }
                    markMessageAsReadOnView(account, message);

                } catch (Exception e) {
                    for (MessagingListener l : getListeners(listener)) {
                        l.loadMessageForViewFailed(account, folder, uid, e);
                    }
                    addErrorMessage(account, null, e);

                }
            }
        });
    }

    /**
     * Mark the provided message as read if not disabled by the account setting.
     *
     * @param account
     *         The account the message belongs to.
     * @param message
     *         The message to mark as read. This {@link Message} instance will be modify by calling
     *         {@link Message#setFlag(Flag, boolean)} on it.
     *
     * @throws MessagingException
     *
     * @see Account#isMarkMessageAsReadOnView()
     */
    private void markMessageAsReadOnView(Account account, Message message)
            throws MessagingException {

        if (account.isMarkMessageAsReadOnView() && !message.isSet(Flag.SEEN)) {
            List<Long> messageIds = Collections.singletonList(message.getId());
            setFlag(account, messageIds, Flag.SEEN, true);

            ((LocalMessage) message).setFlagInternal(Flag.SEEN, true);
        }
    }

    /**
     * Attempts to load the attachment specified by part from the given account and message.
     * @param account
     * @param message
     * @param part
     * @param listener
     */
    public void loadAttachment(
        final Account account,
        final Message message,
        final Part part,
        final Object tag,
        final MessagingListener listener) {
        /*
         * Check if the attachment has already been downloaded. If it has there's no reason to
         * download it, so we just tell the listener that it's ready to go.
         */

        if (part.getBody() != null) {
            for (MessagingListener l : getListeners(listener)) {
                l.loadAttachmentStarted(account, message, part, tag, false);
            }

            for (MessagingListener l : getListeners(listener)) {
                l.loadAttachmentFinished(account, message, part, tag);
            }
            return;
        }



        for (MessagingListener l : getListeners(listener)) {
            l.loadAttachmentStarted(account, message, part, tag, true);
        }

        put("loadAttachment", listener, new Runnable() {
            @Override
            public void run() {
                Folder remoteFolder = null;
                LocalFolder localFolder = null;
                try {
                    LocalStore localStore = account.getLocalStore();
                    
                    // Avoid attachments download repeatedly.
                    /*
                    List<Part> attachments = MimeUtility.collectAttachments(message);
                    for (Part attachment : attachments) {
                        attachment.setBody(null);
                    }
                    */
                    
                    Store remoteStore = account.getRemoteStore();
                    localFolder = localStore.getFolder(message.getFolder().getName());
                    remoteFolder = remoteStore.getFolder(message.getFolder().getName());
                    remoteFolder.open(Folder.OPEN_MODE_RW);

                    //FIXME: This is an ugly hack that won't be needed once the Message objects have been united.
                    Message remoteMessage = remoteFolder.getMessage(message.getUid());
                    remoteMessage.setBody(message.getBody());
                    remoteFolder.fetchPart(remoteMessage, part, null);
                    
                    // Message为本地副本情况，不判断会误判为下载成功
                    if (part.getBody() == null) {
                    	throw new MessagingException("Fetch part failed");
                    }
                    
                    localFolder.updateMessage((LocalMessage)message);
                                        
                    for (MessagingListener l : getListeners(listener)) {
                        l.loadAttachmentFinished(account, message, part, tag);
                    }
                } catch (MessagingException me) {
                    if (MailChat.DEBUG)
                        Log.v(MailChat.LOG_TAG, "Exception loading attachment", me);

                    for (MessagingListener l : getListeners(listener)) {
                        l.loadAttachmentFailed(account, message, part, tag, me);
                    }
                    notifyUserIfCertificateProblem(mApplication, me, account, true);
                    addErrorMessage(account, null, me);

                } finally {
                    closeFolder(localFolder);
                    closeFolder(remoteFolder);
                }
            }
        });
    }

    /**
     * Stores the given message in the Outbox and starts a sendPendingMessages command to
     * attempt to send the message.
     * @param account
     * @param message
     * @param listener
     */
    public boolean sendMessage(final Account account,
                            final Message message,
                            MessagingListener listener) {
        try {
        	LocalStore localStore = account.getLocalStore();
            LocalFolder localFolder = localStore.getFolder(account.getOutboxFolderName());
            localFolder.open(Folder.OPEN_MODE_RW);
            localFolder.appendMessages(new Message[] { message });
            Message localMessage = localFolder.getMessage(message.getUid());
            localMessage.setFlag(Flag.X_DOWNLOADED_FULL, true);
            localFolder.close();
            sendPendingMessages(account, listener);
            return true;
        } catch (Exception e) {
            Log.e(MailChat.LOG_TAG, e.toString());
            addErrorMessage(account, null, e);
        }
        return false;
    }


    public void sendPendingMessages(MessagingListener listener) {
        final Preferences prefs = Preferences.getPreferences(mApplication.getApplicationContext());
        for (Account account : prefs.getAvailableAccounts()) {
            sendPendingMessages(account, listener);
        }
    }


    /**
     * Attempt to send any messages that are sitting in the Outbox.
     * @param account
     * @param listener
     */
    public void sendPendingMessages(final Account account,
                                    MessagingListener listener) {
        putBackground("sendPendingMessages", listener, new Runnable() {
            @Override
            public void run() {
                if (!account.isAvailable(mApplication)) {
                    throw new UnavailableAccountException();
                }
                if (messagesPendingSend(account)) {
                    // 屏蔽原无进度发送邮件提示
                    //notifyWhileSending(account);

                    try {
                        sendPendingMessagesSynchronous(account);
                    } finally {
                        // 屏蔽原无进度发送邮件提示
                        //notifyWhileSendingDone(account);
                    }
                }
            }
        });
    }

    private void cancelNotification(int id) {
        NotificationManager notifMgr =
            (NotificationManager) mApplication.getSystemService(Context.NOTIFICATION_SERVICE);

        notifMgr.cancel(id);
    }

    private void notifyWhileSendingDone(Account account) {
        if (account.isShowOngoing()) {
            cancelNotification(MailChat.SEND_MESSAGE_NOTIFICATION - account.getAccountNumber());
        }
    }

    /**
     * Display an ongoing notification while a message is being sent.
     *
     * @param account
     *         The account the message is sent from. Never {@code null}.
     *         发送邮件中
     */
    private void notifyWhileSending(Account account) {
        if (!account.isShowOngoing()) {
            return;
        }
        
        MailChat.toast(mApplication.getString(R.string.notification_bg_send_title));

        NotificationManager notifMgr =
            (NotificationManager) mApplication.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mApplication);
        builder.setSmallIcon(R.drawable.ic_notify_check_mail);
        builder.setWhen(System.currentTimeMillis());
        builder.setOngoing(true);

        String accountDescription = account.getDescription();
        String accountName = (TextUtils.isEmpty(accountDescription)) ?
                account.getEmail() : accountDescription;

        builder.setTicker(mApplication.getString(R.string.notification_bg_send_ticker,
                accountName));

        builder.setContentTitle(mApplication.getString(R.string.notification_bg_send_title));
        builder.setContentText(account.getDescription());

        TaskStackBuilder stack = buildMessageListBackStack(mApplication, account,
                account.getInboxFolderName());
        builder.setContentIntent(stack.getPendingIntent(0, 0));

        if (MailChat.NOTIFICATION_LED_WHILE_SYNCING) {
            configureNotification(builder, null, null,
                    account.getNotificationSetting().getLedColor(),
                    MailChat.NOTIFICATION_LED_BLINK_FAST, true);
        }

        notifMgr.notify(MailChat.SEND_MESSAGE_NOTIFICATION - account.getAccountNumber(),
                builder.build());
    }
    /**
     * 
     * method name: notifyWhileSendingMailSuccess 
     * function @Description: TODO
     * Parameters and return values description：
     *      @param account    field_name
     *      void    return type
     *  @History memory：
     *     @Date：2014-12-9 下午3:43:04	@Modified by：zhangjx
     *     @Description：发送邮件成功提醒
     */
    private void notifyWhileSendingMailSuccess(Account account) {
        
        MailChat.toast(mApplication.getString(R.string.message_send_success));
        
        final NotificationManager notifMgr =
            (NotificationManager) mApplication.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mApplication);
        builder.setSmallIcon(R.drawable.icon_send_mail_success);
        builder.setWhen(System.currentTimeMillis());
        
        // 修复邮件发送成功通知无法清除问题
        // Modified by LL
        //builder.setOngoing(true);

        String accountDescription = account.getDescription();
        String accountName = (TextUtils.isEmpty(accountDescription)) ?
                account.getEmail() : accountDescription;

        builder.setTicker(mApplication.getString(R.string.message_send_success));

        builder.setContentTitle(mApplication.getString(R.string.message_send_success));
        builder.setContentText(account.getDescription());

        TaskStackBuilder stack = buildMessageListBackStack(mApplication, account,
                account.getInboxFolderName());
        builder.setContentIntent(stack.getPendingIntent(0, 0));

        if (MailChat.NOTIFICATION_LED_WHILE_SYNCING) {
            configureNotification(builder, null, null,
                    account.getNotificationSetting().getLedColor(),
                    MailChat.NOTIFICATION_LED_BLINK_FAST, true);
        }
        
        /*
        notifMgr.notify(MailChat.FETCHING_EMAIL_NOTIFICATION - account.getAccountNumber(),
                builder.build());
        */
        // 修复邮件发送成功通知无法清除问题
        // Modified by LL
        // BEGIN
        final int id = MailChat.FETCHING_EMAIL_NOTIFICATION - account.getAccountNumber();
        notifMgr.notify(id, builder.build());
        
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(5000);
                } catch (Exception e) {
                }
                notifMgr.cancel(id);
            }
        }.start();
        // END
    }
    private void notifySendTempFailed(Account account, Exception lastFailure) {
        notifySendFailed(account, lastFailure, account.getOutboxFolderName());
    }

    private void notifySendPermFailed(Account account, Exception lastFailure) {
        //notifySendFailed(account, lastFailure, account.getDraftsFolderName());
    	// 5XX错误导致的发件失败也统一存放在待发送文件夹
    	// Modified by LL
    	notifySendFailed(account, lastFailure, account.getOutboxFolderName());
    }

    /**
     * Display a notification when sending a message has failed.
     *
     * @param account
     *         The account that was used to sent the message.
     * @param lastFailure
     *         The {@link Exception} instance that indicated sending the message has failed.
     * @param openFolder
     *         The name of the folder to open when the notification is clicked.
     */
    private void notifySendFailed(Account account, Exception lastFailure, String openFolder) {
        
        MailChat.toast(mApplication.getString(R.string.message_send_failure));
        
    	Log.d(MailChat.LOG_TAG, "notifySendFailed==>openFolder=="+openFolder);
        NotificationManager notifMgr =
                (NotificationManager) mApplication.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mApplication);
        builder.setSmallIcon(R.drawable.icon_notification_mail_small);
        builder.setLargeIcon(BitmapFactory.decodeResource(
				mApplication.getResources(), R.drawable.icon_notification));
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);
        builder.setTicker(mApplication.getString(R.string.send_failure_subject));
        builder.setContentTitle(mApplication.getString(R.string.send_failure_subject));
        builder.setContentText(MailChat.getRootCauseMessage(lastFailure));
        
        /*
        TaskStackBuilder stack = buildFolderListBackStack(mApplication, account,openFolder);
        builder.setContentIntent(stack.getPendingIntent(0, 0));
        */
        // 修复点击发送失败通知未跳转到待发送文件夹问题
        // Modified by LL
        // BEGIN
		LocalSearch search = new LocalSearch(account.getOutboxFolderName());
		search.addAccountUuid(account.getUuid());
		search.addAllowedFolder(account.getOutboxFolderName());

		Intent intent = MailNotifyPendingActivity.actionMailNotify(
				MailChat.getInstance(),
				search,
				false);

		PendingIntent pendingIntent = PendingIntent.getActivity(
				MailChat.getInstance(),
				MailChat.SEND_FAILED_NOTIFICATION - account.getAccountNumber(),
				intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		builder.setContentIntent(pendingIntent);
        // END

        configureNotification(builder,  null, null, MailChat.NOTIFICATION_LED_FAILURE_COLOR,
                MailChat.NOTIFICATION_LED_BLINK_FAST, true);

        notifMgr.notify(MailChat.SEND_FAILED_NOTIFICATION - account.getAccountNumber(),
                builder.build());
    }

    /**
     * Display an ongoing notification while checking for new messages on the server.
     *
     * @param account
     *         The account that is checked for new messages. Never {@code null}.
     * @param folder
     *         The folder that is being checked for new messages. Never {@code null}.
     */
    private void notifyFetchingMail(final Account account, final Folder folder) {
        if (!account.isShowOngoing()) {
            return;
        }

        final NotificationManager notifMgr =
                (NotificationManager) mApplication.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mApplication);
        builder.setSmallIcon(R.drawable.ic_notify_check_mail);
        builder.setWhen(System.currentTimeMillis());
        builder.setOngoing(true);
        builder.setTicker(mApplication.getString(
                R.string.notification_bg_sync_ticker, account.getDescription(), folder.getName()));
        builder.setContentTitle(mApplication.getString(R.string.notification_bg_sync_title));
        builder.setContentText(account.getDescription() +
                mApplication.getString(R.string.notification_bg_title_separator) +
                folder.getName());

        TaskStackBuilder stack = buildMessageListBackStack(mApplication, account,
                account.getInboxFolderName());
        builder.setContentIntent(stack.getPendingIntent(0, 0));

        if (MailChat.NOTIFICATION_LED_WHILE_SYNCING) {
            configureNotification(builder, null, null,
                    account.getNotificationSetting().getLedColor(),
                    MailChat.NOTIFICATION_LED_BLINK_FAST, true);
        }

        notifMgr.notify(MailChat.FETCHING_EMAIL_NOTIFICATION - account.getAccountNumber(),
                builder.build());
    }

    private void notifyFetchingMailCancel(final Account account) {
        if (account.isShowOngoing()) {
            cancelNotification(MailChat.FETCHING_EMAIL_NOTIFICATION - account.getAccountNumber());
        }
    }

    public boolean messagesPendingSend(final Account account) {
        Folder localFolder = null;
        try {
            localFolder = account.getLocalStore().getFolder(
                              account.getOutboxFolderName());
            if (!localFolder.exists()) {
                return false;
            }

            localFolder.open(Folder.OPEN_MODE_RW);

            if (localFolder.getMessageCount() > 0) {
                return true;
            }
        } catch (Exception e) {
            Log.e(MailChat.LOG_TAG, "Exception while checking for unsent messages", e);
        } finally {
            closeFolder(localFolder);
        }
        return false;
    }

    /**
     * Attempt to send any messages that are sitting in the Outbox.
     * 发邮件
     * @param account
     */
    public void sendPendingMessagesSynchronous(final Account account) {
        LocalFolder localFolder = null;
        Exception lastFailure = null;
        try {
            LocalStore localStore = account.getLocalStore();
            localFolder = localStore.getFolder(account.getOutboxFolderName());
            if (!localFolder.exists()) {
                return;
            }
            //开始
            for (MessagingListener l : getListeners()) {
                l.sendPendingMessagesStarted(account);
            }
            localFolder.open(Folder.OPEN_MODE_RW);

            Message[] localMessages = localFolder.getMessages(null);
            int progress = 0;
            int todo = localMessages.length;
            for (MessagingListener l : getListeners()) {
                l.synchronizeMailboxProgress(account, account.getSentFolderName(), progress, todo);
            }
            /*
             * The profile we will use to pull all of the content
             * for a given local message into memory for sending.
             */
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.BODY);
            // 本次提取为发送邮件准备数据
            fp.add(FetchProfile.Item.SEND);
            
            FetchProfile fp2 = new FetchProfile();
            fp2.add(FetchProfile.Item.ENVELOPE);
            fp2.add(FetchProfile.Item.BODY);

            if (MailChat.DEBUG)
                Log.i(MailChat.LOG_TAG, "Scanning folder '" + account.getOutboxFolderName() + "' (" + ((LocalFolder)localFolder).getId() + ") for messages to send");

            Transport transport = Transport.getInstance(account);
            for (Message message : localMessages) {
                if (message.isSet(Flag.DELETED)) {
                    message.destroy();
                    continue;
                } else if (message.isSet(Flag.X_SEND_FAILED)) {
                    continue;
                }
                
//                AtomicInteger count = new AtomicInteger(0);
                
                try {
                    /*
                    AtomicInteger oldCount = sendCount.putIfAbsent(message.getUid(), count);
                    if (oldCount != null) {
                        count = oldCount;
                    }

                    if (MailChat.DEBUG)
                        Log.i(MailChat.LOG_TAG, "Send count for message " + message.getUid() + " is " + count.get());

                    if (count.incrementAndGet() > MailChat.MAX_SEND_ATTEMPTS) {
                    	// 确保超出最大发送尝试次数后，显示未发送成功提示
                    	lastFailure = new MessagingException("Exceed max send attampts: " + message.getSubject());
                    	
                        Log.e(MailChat.LOG_TAG, "Send count for message " + message.getUid() + " can't be delivered after " + MailChat.MAX_SEND_ATTEMPTS + " attempts.  Giving up until the user restarts the device");
                        //notifySendTempFailed(account, new MessagingException(message.getSubject()));
                        continue;
                    }
                    */

                    notifyWhileSending(account);

                    localFolder.fetch(new Message[] { message }, fp, null);
                    
                    String sourceIdentity = null;
                    
                    Part atPart = null;
					Message atMessage = null;
					Account atAccount = null;
					AttachmentView atView = null;
                    
                    try {
                    	// 待发送邮件删除源邮件的引用信息
                    	String[] sourceIdentityHeaders = message.getHeader(MailChat.SOURCE_IDENTITY_HEADER);
                        if (sourceIdentityHeaders != null && sourceIdentityHeaders.length > 0) {
                        	sourceIdentity = sourceIdentityHeaders[0];
                            //message.removeHeader(MailChat.SOURCE_IDENTITY_HEADER);
                        }

                        if (message.getHeader(MailChat.IDENTITY_HEADER) != null) {
                        	// 跳过格式不正确邮件时，显示未发送成功提示
//                        	count.set(MailChat.MAX_SEND_ATTEMPTS);
                            lastFailure = new MessagingException(
                                    MailChat.app.getString(R.string.send_mail_draft_error)
                                    + " : " + message.getSubject());
                            message.setFlag(Flag.X_SEND_FAILED, true);
                        	
                        	Log.v(MailChat.LOG_TAG, "The user has set the Outbox and Drafts folder to the same thing. " +
                                  "This message appears to be a draft, so K-9 will not send it");
                            continue;
                        }

                        // 处理未下载附件
                        boolean failed = false;
                        MessageReference sourceMessageReference = null;
                        LocalFolder sourceLocalFolder = null;
                        LocalMessage sourceMessage = null;
                        List<Part> sourceAtts = null;
                        
                        List<Part> atts = MimeUtility.collectAttachments(message);

                        // 通过三五企业邮箱云服务发信
                        // BEGIN
                        if (MailChat.is35CloudServices()) {
                            MobclickAgent.onEvent(MailChat.app, "cloud_service_send_mail");

                            int version = account.getVersion_35Mail();
                            if (version == 1 || version == 2) {
                                try {
                                    com.c35.mtd.pushmail.beans.Account c35Account = WeemailUtil.getC35Account(account);
                                    C35Store c35Store = WeemailUtil.getC35Store(c35Account);
                                    C35Message c35Message = WeemailUtil.createC35Message(message);

                                    if (atts != null && atts.size() > 0) {
                                        if (sourceIdentity == null) {
                                            throw new MessagingException("源邮件SourceIdentity为空无法免流量转发附件");
                                        }

                                        if (sourceMessageReference == null) {
                                            sourceMessageReference = new MessageReference(sourceIdentity);
                                        }

                                        sourceLocalFolder = account.getLocalStore().getFolder(sourceMessageReference.folderName);
                                        if (sourceLocalFolder != null) {
                                            sourceMessage = (LocalMessage)sourceLocalFolder.getMessage(sourceMessageReference.uid);
                                            if (sourceMessage != null) {
                                                FetchProfile fetchProfile = new FetchProfile();
                                                fetchProfile.add(FetchProfile.Item.BODY);
                                                sourceLocalFolder.fetch(new Message[]{sourceMessage}, fetchProfile, null);
                                            } else {
                                                throw new MessagingException("源邮件SourceMessage为空无法免流量转发附件");
                                            }
                                        } else {
                                            throw new MessagingException("源邮件SourceFolder为空无法免流量转发附件");
                                        }
                                        
                                        String sourceMailId = sourceMessage.getMailId();
                                        if (sourceMailId == null) {
                                            sourceMailId = WeemailUtil.getC35MailId(account, sourceMessage, c35Account, c35Store);
                                            sourceMessage.setMailId(sourceMailId);
                                            sourceLocalFolder.updateMessage(sourceMessage);
                                            for (final MessagingListener l : getListeners()) {
                                                l.synchronizeMailboxAddOrUpdateMessage(account, sourceLocalFolder.getName(), sourceMessage);
                                            }
                                        }

                                        C35Message c35SourceMessage = c35Store.getMailById(sourceMailId, 0, 0, 0, 4);

                                        List<C35Attachment> c35SourceAtts = c35SourceMessage.getAttachs();

                                        List<C35Attachment> c35Atts = new ArrayList<C35Attachment>();
                                        for (Part att : atts) {
                                            String contentType = MimeUtility.unfoldAndDecode(att.getContentType());
                                            String attName = MimeUtility.getHeaderParameter(contentType, "name");
                                            if (attName == null) {
                                                String contentDisposition = MimeUtility.unfoldAndDecode(att.getDisposition());
                                                attName = MimeUtility.getHeaderParameter(contentDisposition, "filename");
                                            }

                                            C35Attachment c35SourceAtt = null;
                                            for (C35Attachment c35TempSourceAtt : c35SourceAtts) {
                                                if (c35TempSourceAtt.getFileName().equals(attName)) {
                                                    c35SourceAtt = c35TempSourceAtt;
                                                    break;
                                                    //sourceAtt.setCid(att.getContentId());
//                                                    String cid = att.getContentId();
//                                                    if (cid != null && cid.length() > 0) {
//                                                        throw new MessagingException("内嵌资源暂时无法免流量转发");
//                                                    }
                                                }
                                            }

                                            if (c35SourceAtt == null) {
                                                Body body = att.getBody();
                                                if (body != null) {
                                                    C35Attachment c35Att = new C35Attachment();
                                                    c35Att.setFileName(attName);

                                                    File file = FileUtil.saveInputStreamToTempFile(
                                                            body.getInputStream(),
                                                            attName);
                                                    c35Att.setPath(file.getAbsolutePath());

                                                    c35Att.setType(1);
                                                    c35Att.setDownState(C35Attachment.DOWNLOADED);
                                                    c35Atts.add(c35Att);
                                                } else {
                                                    throw new MessagingException("无法在源邮件中找到匹配附件");
                                                }
                                            } else if (c35SourceAtt.getCid() != null && c35SourceAtt.getCid().length() > 0) {
                                                if (sourceAtts == null) {
                                                    sourceAtts = MimeUtility.collectAttachments(sourceMessage);
                                                }

                                                String[] attStoreData = att.getHeader(MimeHeader.HEADER_ANDROID_ATTACHMENT_STORE_DATA);
                                                Part sourceAtt = null;
                                                for (Part tempSourceAtt : sourceAtts) {
                                                    if (MimeHeader.isEqual(attStoreData, tempSourceAtt.getHeader(MimeHeader.HEADER_ANDROID_ATTACHMENT_STORE_DATA))) {
                                                        sourceAtt = tempSourceAtt;
                                                        break;
                                                    }
                                                }

                                                if (sourceAtt != null && sourceAtt.getBody() != null) {
                                                    Body body = sourceAtt.getBody();
                                                    C35Attachment c35Att = new C35Attachment();
                                                    c35Att.setFileName(attName);

                                                    File file = FileUtil.saveInputStreamToTempFile(
                                                            body.getInputStream(),
                                                            attName);
                                                    c35Att.setPath(file.getAbsolutePath());

                                                    c35Att.setType(2);
                                                    c35Att.setDownState(C35Attachment.DOWNLOADED);
                                                    c35Att.setCid(c35SourceAtt.getCid());
                                                    c35Atts.add(c35Att);
                                                } else {
                                                    throw new MessagingException("无法找到匹配内嵌图片");
                                                }
                                            } else {
                                                c35SourceAtt.setType(4);
                                                c35SourceAtt.setSourceMessageUid(c35SourceAtt.getMailId());
                                                c35SourceAtt.setSourceAttachmentId(c35SourceAtt.getId());
                                                c35Atts.add(c35SourceAtt);
                                            }
                                        }

                                        c35Message.setAttachs(c35Atts);
                                        c35Message.setAttachSize(c35Atts.size());
                                    }

                                    c35Store.sendMail(c35Message, c35Account);
                                    MailChat.toast(MailChat.app.getString(R.string.message_send_success_via_35_cloud_services));
                                    MobclickAgent.onEvent(MailChat.app, "cloud_service_send_mail_success");

                                    message.setFlag(Flag.SEEN, true);
                                    progress++;
                                    for (MessagingListener l : getListeners()) {
                                        l.synchronizeMailboxProgress(account, account.getSentFolderName(), progress, todo);
                                    }

                                    /*
                                    if (!account.hasSentFolder()) {
                                        message.setFlag(Flag.DELETED, true);
                                    } else {
                                        LocalFolder localSentFolder = (LocalFolder) localStore.getFolder(account.getSentFolderName());
                                        localFolder.moveMessages(new Message[] { message }, localSentFolder);
                                    }
                                    */
                                    //message.setFlag(Flag.DELETED, true);
                                    message.destroy();

                                    // 发送成功后设置源邮件Flag
                                    if (sourceIdentity != null) {
                                        if (sourceMessageReference == null) {
                                            sourceMessageReference = new MessageReference(sourceIdentity);
                                        }
                                        if (sourceMessageReference != null && sourceMessageReference.flag != null) {
                                            final String sourceFolderName = sourceMessageReference.folderName;
                                            final String sourceMessageUid = sourceMessageReference.uid;
                                            setFlag(account, sourceFolderName, sourceMessageUid, sourceMessageReference.flag, true);
                                        }
                                    }

                                    continue;
                                } catch (Exception e) {
                                    Log.e(MailChat.LOG_TAG,
                                            MailChat.app.getString(R.string.message_send_failure_via_35_cloud_services),
                                            e);
                                }
                            }
                        }
                        // END

                        for (Part att : atts) {
                            long attId = ((LocalAttachmentBodyPart) att).getAttachmentId();
                            List<Part> attParts = MimeUtility.collectAttachments(message);
                            for (Part attPart : attParts) {
                                if (((LocalAttachmentBodyPart) attPart).getAttachmentId() == attId) {
                                    att = attPart;
                                    break;
                                }
                            }
                            
                        	if (att.getBody() == null) {
                        		if (sourceIdentity == null) {
                        			// 确保未下载附件源消息SourceIdentity不存在时，显示未发送成功提示
//                        			count.set(MailChat.MAX_SEND_ATTEMPTS);
                        		    lastFailure = new MessagingException(
                                            MailChat.app.getString(R.string.send_mail_source_identity_error)
                                            + " : " + message.getSubject());
                        		    message.setFlag(Flag.X_SEND_FAILED, true);
                                	
                        			Log.e(MailChat.LOG_TAG, "SourceIdentity doesn't exist, abort downloading attachment.");
                        			failed = true;
                        			break;
                        		}
                        		
                        		if (sourceMessage == null) {
                        		    if (sourceMessageReference == null) {
                        		        sourceMessageReference = new MessageReference(sourceIdentity);
                        		    }
                                    sourceLocalFolder = account.getLocalStore().getFolder(sourceMessageReference.folderName);
                                    if (sourceLocalFolder != null) {
                                        sourceMessage = (LocalMessage)sourceLocalFolder.getMessage(sourceMessageReference.uid);
                                        if (sourceMessage != null) {
                                        	FetchProfile fetchProfile = new FetchProfile();
                                        	fetchProfile.add(FetchProfile.Item.BODY);
                                        	sourceLocalFolder.fetch(new Message[]{sourceMessage}, fetchProfile, null);
                                        } else {
                                        	// 确保未下载附件源消息不存在时，显示未发送成功提示
//                                			count.set(MailChat.MAX_SEND_ATTEMPTS);
                                            lastFailure = new MessagingException(
                                                    MailChat.app.getString(R.string.send_mail_source_message_error)
                                                    + " : " + message.getSubject());
                                            message.setFlag(Flag.X_SEND_FAILED, true);
                                        	
                                        	Log.e(MailChat.LOG_TAG, "Source message doesn't exist, abort downloading attachment.");
                                        	failed = true;
                                			break;
                                        }
                                    } else {
                                    	// 确保未下载附件源消息文件夹不存在时，显示未发送成功提示
//                            			count.set(MailChat.MAX_SEND_ATTEMPTS);
                                        lastFailure = new MessagingException(
                                                MailChat.app.getString(R.string.send_mail_source_message_folder_error)
                                                + " : " + message.getSubject());
                                        message.setFlag(Flag.X_SEND_FAILED, true);
                                    	
                                    	Log.e(MailChat.LOG_TAG, "Source message's folder doesn't exist, abort downloading attachment.");
                                    	failed = true;
                            			break;
                                    }
                        		}

                                if (sourceAtts == null) {
                                    sourceAtts = MimeUtility.collectAttachments(sourceMessage);
                                }

                        		String[] attStoreData = att.getHeader(MimeHeader.HEADER_ANDROID_ATTACHMENT_STORE_DATA);
                        		Part sourceAtt = null;
                        		for (Part srcPart : sourceAtts) {
                        			if (MimeHeader.isEqual(attStoreData, srcPart.getHeader(MimeHeader.HEADER_ANDROID_ATTACHMENT_STORE_DATA))) {
                        				sourceAtt = srcPart;
                        				break;
                        			}
                        		}
                        		
                        		if (sourceAtt == null) {
                        			// 确保未下载附件源附件不存在时，显示未发送成功提示
//                        			count.set(MailChat.MAX_SEND_ATTEMPTS);
                        		    lastFailure = new MessagingException(
                                            MailChat.app.getString(R.string.send_mail_source_message_part_error)
                                            + " : " + message.getSubject());
                        		    message.setFlag(Flag.X_SEND_FAILED, true);
                        			
                        			Log.e(MailChat.LOG_TAG, "Source message's attachment part doesn't exist, abort downloading attachment.");
                                	failed = true;
                        			break;
                        		}

                        		if (sourceAtt.getBody() != null) {
                                    att.setBody(sourceAtt.getBody());
                                    att.getBody().setEncoding("base64");

                                    ((LocalAttachmentBodyPart) att).setMessageId(((LocalAttachmentBodyPart) sourceAtt).getMessageId());

                                    localFolder.updateMessage((LocalMessage) message);
                                    localFolder.fetch(new Message[] { message }, fp2, null);
                        		    continue;
                        		}

								String downloadingId = ((LocalAttachmentBodyPart) sourceAtt)
										.getAttachmentId()
										+ sourceMessage.getUid()
										+ account.getUuid();
								
								AttachmentView attView = null;
								synchronized (MailChat.attachmentList) {
									attView = MailChat.attachmentList.get(downloadingId);
								}
								
								if (attView == null) {
									LayoutInflater inflater = (LayoutInflater) MailChat.getInstance()
											.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
									attView = (AttachmentView) inflater.inflate(R.layout.message_view_attachment, null);
									attView.populateFromPart(sourceAtt, sourceMessage, account, this, null);
									attView.mIsTemp = true;
								}
								
								atPart = sourceAtt;
								atMessage = sourceMessage;
								atAccount = account;
								atView = attView;
								
								final AttachmentView finalView = atView;
								
								attView.mDownloadingNotification = attView.new DownloadAttachmentNotification(
										attView,
										MailChat.SEND_MESSAGE_NOTIFICATION - account.getAccountNumber());
								attView.mStatus = AttachmentView.Status.DOWNLOADING;
								synchronized (MailChat.downloadingList) {
									MailChat.downloadingList.put(downloadingId, attView);
								}

								for (MessagingListener l : getListeners(atView.getListener())) {
					                l.loadAttachmentStarted(atAccount, atMessage, atPart, new Object[]{false, atView}, false);
					            }
                        		
                        		Folder remoteSourceFolder = account.getRemoteStore().getFolder(sourceMessageReference.folderName);
                        		remoteSourceFolder.open(Folder.OPEN_MODE_RW);

                                //FIXME: This is an ugly hack that won't be needed once the Message objects have been united.
                                Message remoteSourceMessage = remoteSourceFolder.getMessage(sourceMessage.getUid());
                                remoteSourceMessage.setBody(sourceMessage.getBody());
                                remoteSourceFolder.fetchPart(remoteSourceMessage, sourceAtt, null);
                                sourceLocalFolder.updateMessage((LocalMessage)sourceMessage);
                                closeFolder(remoteSourceFolder);
                                closeFolder(sourceLocalFolder);
                                
                                finalView.mPart = (LocalAttachmentBodyPart)atPart;
                                synchronized (MailChat.downloadingList) {
									MailChat.downloadingList.remove(downloadingId);
								}
					            for (MessagingListener l : getListeners(atView.getListener())) {
					                l.loadAttachmentFinished(atAccount, atMessage, atPart, new Object[]{false, atView});
					            }
					            
					            atPart = null;
								atMessage = null;
								atAccount = null;
								atView = null;
                        		
                                att.setBody(sourceAtt.getBody());
                                att.getBody().setEncoding("base64");

                                ((LocalAttachmentBodyPart) att).setMessageId(((LocalAttachmentBodyPart) sourceAtt).getMessageId());

                                localFolder.updateMessage((LocalMessage) message);
                                localFolder.fetch(new Message[] { message }, fp2, null);
                        	}
                        }
                        
                        if (failed) {
                        	continue;
                        }

                        ImapStore remoteStore = (ImapStore)account.getRemoteStore();
                        remoteStore.testConnection();

                        localFolder.fetch(new Message[] { message }, fp, null);
                        message.setFlag(Flag.X_SEND_IN_PROGRESS, true);
                        if (MailChat.DEBUG)
                            Log.i(MailChat.LOG_TAG, "Sending message with UID " + message.getUid());

                        message.removeHeader(MailChat.SOURCE_IDENTITY_HEADER);
                        transport.sendMessage(message);

                        message.setFlag(Flag.X_SEND_IN_PROGRESS, false);
                        message.setFlag(Flag.SEEN, true);
                        progress++;
                        for (MessagingListener l : getListeners()) {
                            l.synchronizeMailboxProgress(account, account.getSentFolderName(), progress, todo);
                        }
                        if (!account.hasSentFolder()) {
                            if (MailChat.DEBUG)
                                Log.i(MailChat.LOG_TAG, "Account does not have a sent mail folder; deleting sent message");
                            message.setFlag(Flag.DELETED, true);
                        } else {
                            LocalFolder localSentFolder = (LocalFolder) localStore.getFolder(account.getSentFolderName());
                            if (MailChat.DEBUG)
                                Log.i(MailChat.LOG_TAG, "Moving sent message to folder '" + account.getSentFolderName() + "' (" + localSentFolder.getId() + ") ");

                            localFolder.moveMessages(new Message[] { message }, localSentFolder);

                            if (MailChat.DEBUG)
                                Log.i(MailChat.LOG_TAG, "Moved sent message to folder '" + account.getSentFolderName() + "' (" + localSentFolder.getId() + ") ");
                        }
                        
                        // 发送成功后设置源邮件Flag
                        if (sourceIdentity != null) {
                            if (sourceMessageReference == null) {
                                sourceMessageReference = new MessageReference(sourceIdentity);
                            }
                        	if (sourceMessageReference != null && sourceMessageReference.flag != null) {
                    			final String sourceFolderName = sourceMessageReference.folderName;
                    			final String sourceMessageUid = sourceMessageReference.uid;
                    			setFlag(account, sourceFolderName, sourceMessageUid, sourceMessageReference.flag, true);
                    		}
                        }

                        notifyWhileSendingMailSuccess(account);
                    } catch (Exception e) {
                    	// 发送不成功写回源邮件的引用信息
                    	if (sourceIdentity != null) {
                            message.removeHeader(MailChat.SOURCE_IDENTITY_HEADER);
                    		message.setHeader(MailChat.SOURCE_IDENTITY_HEADER, sourceIdentity);
                    	}

                        // 5.x.x errors from the SMTP server are "PERMFAIL"
                        // move the message over to drafts rather than leaving it in the outbox
                        // This is a complete hack, but is worlds better than the previous
                        // "don't even bother" functionality
                        if (MailChat.getRootCauseMessage(e).startsWith("5")) {
                            //localFolder.moveMessages(new Message[] { message }, (LocalFolder) localStore.getFolder(account.getDraftsFolderName()));
                        	// 5XX错误导致的发件失败也统一存放在待发送文件夹，通过计数阻止再次尝试发送
//                			count.set(MailChat.MAX_SEND_ATTEMPTS);
                            Log.e(MailChat.LOG_COLLECTOR_TAG, "Encounter PERMFAIL");
                        }
                        //通知栏通知发消息失败
                        notifyUserIfCertificateProblem(mApplication, e, account, false);
                        addErrorMessage(account, "Failed to send message", e);
                        message.setFlag(Flag.X_SEND_FAILED, true);
                        Log.e(MailChat.LOG_TAG, "Failed to send message", e);
                        for (MessagingListener l : getListeners()) {
                            l.synchronizeMailboxFailed(account, localFolder.getName(), MailChat.getRootCauseMessage(e));
                        }
                        for (MessagingListener l : getListeners()) { // TODO general failed
                        	l.sendMailFailed(account, message.getUid(), MailChat.getRootCauseMessage(e), null);
                        }
                        lastFailure = e;
                        
                        if (atAccount != null && atMessage != null && atPart != null && atView != null) {
				            for (MessagingListener l : getListeners(atView.getListener())) {
				                l.loadAttachmentFailed(atAccount, atMessage, atPart, new Object[]{false, atView}, null);
				            }
                        }
                    } finally {
                        notifyWhileSendingDone(account);
                    }
                } catch (Exception e) {
                    Log.e(MailChat.LOG_TAG, "Failed to fetch message for sending", e);
                    for (MessagingListener l : getListeners()) {
                        l.synchronizeMailboxFailed(account, localFolder.getName(), MailChat.getRootCauseMessage(e));
                    }
                    addErrorMessage(account, "Failed to fetch message for sending", e);
                    lastFailure = e;
                }
            }
            //成功
            for (MessagingListener l : getListeners()) {
                l.sendPendingMessagesCompleted(account);
            }
			if (lastFailure != null) {
				if (MailChat.getRootCauseMessage(lastFailure).startsWith("5")) {
					notifySendPermFailed(account, lastFailure);
				} else {
					notifySendTempFailed(account, lastFailure);
				}
			} else {
				for (MessagingListener l : getListeners()) {
					l.sendMailSuccess(account);
				}
			}
        } catch (UnavailableStorageException e) {
            Log.i(MailChat.LOG_TAG, "Failed to send pending messages because storage is not available - trying again later.");
            throw new UnavailableAccountException(e);
        } catch (Exception e) {
        	//失败
            for (MessagingListener l : getListeners()) {
                l.sendPendingMessagesFailed(account);
            }
            addErrorMessage(account, null, e);
        } finally {
//            if (lastFailure == null) {
//                cancelNotification(MailChat.SEND_FAILED_NOTIFICATION - account.getAccountNumber());
//            }
            closeFolder(localFolder);
        }
    }

    public void getAccountStats(final Context context, final Account account,
            final MessagingListener listener) {

        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    AccountStats stats = account.getStats(context);
                    listener.accountStatusChanged(account, stats);
                } catch (MessagingException me) {
                    Log.e(MailChat.LOG_TAG, "Count not get unread count for account " +
                            account.getDescription(), me);
                }

            }
        });
    }

    public void getSearchAccountStats(final SearchAccount searchAccount,
            final MessagingListener listener) {

        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                getSearchAccountStatsSynchronous(searchAccount, listener);
            }
        });
    }

    public AccountStats getSearchAccountStatsSynchronous(final SearchAccount searchAccount,
            final MessagingListener listener) {

        Preferences preferences = Preferences.getPreferences(mApplication);
        LocalSearch search = searchAccount.getRelatedSearch();

        // Collect accounts that belong to the search
        String[] accountUuids = search.getAccountUuids();
        List<Account> accounts;
        if (search.searchAllAccounts()) {
            accounts = preferences.getAccounts();
        } else {
        	   accounts = new ArrayList<Account>(accountUuids.length);
            for (int i = 0, len = accountUuids.length; i < len; i++) {
                String accountUuid = accountUuids[i];
                accounts.set(i, preferences.getAccount(accountUuid));
            }
        }

        ContentResolver cr = mApplication.getContentResolver();

        int unreadMessageCount = 0;
        int flaggedMessageCount = 0;

        String[] projection = {
                StatsColumns.UNREAD_COUNT,
                StatsColumns.FLAGGED_COUNT
        };

        for (Account account : accounts) {
            StringBuilder query = new StringBuilder();
            List<String> queryArgs = new ArrayList<String>();
            ConditionsTreeNode conditions = search.getConditions();
            SqlQueryBuilder.buildWhereClause(account, conditions, query, queryArgs);

            String selection = query.toString();
            String[] selectionArgs = queryArgs.toArray(EMPTY_STRING_ARRAY);

            Uri uri = Uri.withAppendedPath(EmailProvider.CONTENT_URI,
                    "account/" + account.getUuid() + "/stats");

            // Query content provider to get the account stats
            Cursor cursor = cr.query(uri, projection, selection, selectionArgs, null);
            try {
                if (cursor.moveToFirst()) {
                    unreadMessageCount += cursor.getInt(0);
                    flaggedMessageCount += cursor.getInt(1);
                }
            } finally {
                cursor.close();
            }
        }

        // Create AccountStats instance...
        AccountStats stats = new AccountStats();
        stats.unreadMessageCount = unreadMessageCount;
        stats.flaggedMessageCount = flaggedMessageCount;

        // ...and notify the listener
        if (listener != null) {
            listener.accountStatusChanged(searchAccount, stats);
        }

        return stats;
    }

    public void getFolderUnreadMessageCount(final Account account, final String folderName,
                                            final MessagingListener l) {
        Runnable unreadRunnable = new Runnable() {
            @Override
            public void run() {

                int unreadMessageCount = 0;
                try {
                    Folder localFolder = account.getLocalStore().getFolder(folderName);
                    unreadMessageCount = localFolder.getUnreadMessageCount();
                } catch (MessagingException me) {
                    Log.e(MailChat.LOG_TAG, "Count not get unread count for account " + account.getDescription(), me);
                }
                l.folderStatusChanged(account, folderName, unreadMessageCount);
            }
        };


        put("getFolderUnread:" + account.getDescription() + ":" + folderName, l, unreadRunnable);
    }



    public boolean isMoveCapable(Message message) {
        return !message.getUid().startsWith(MailChat.LOCAL_UID_PREFIX);
    }
    public boolean isCopyCapable(Message message) {
        return isMoveCapable(message);
    }

    public boolean isMoveCapable(final Account account) {
        try {
            Store localStore = account.getLocalStore();
            Store remoteStore = account.getRemoteStore();
            return localStore.isMoveCapable() && remoteStore.isMoveCapable();
        } catch (MessagingException me) {

            Log.e(MailChat.LOG_TAG, "Exception while ascertaining move capability", me);
            return false;
        }
    }
    public boolean isCopyCapable(final Account account) {
        try {
            Store localStore = account.getLocalStore();
            Store remoteStore = account.getRemoteStore();
            return localStore.isCopyCapable() && remoteStore.isCopyCapable();
        } catch (MessagingException me) {
            Log.e(MailChat.LOG_TAG, "Exception while ascertaining copy capability", me);
            return false;
        }
    }
    public void moveMessages(final Account account, final String srcFolder,
            final List<Message> messages, final String destFolder,
            final MessagingListener listener) {

        suppressMessages(account, messages);

        putBackground("moveMessages", null, new Runnable() {
            @Override
            public void run() {
                moveOrCopyMessageSynchronous(account, srcFolder, messages, destFolder, false,
                        listener);
            }
        });
    }

    public void moveMessagesInThread(final Account account, final String srcFolder,
            final List<Message> messages, final String destFolder) {

        suppressMessages(account, messages);

        putBackground("moveMessagesInThread", null, new Runnable() {
            @Override
            public void run() {
                try {
                    List<Message> messagesInThreads = collectMessagesInThreads(account, messages);
                    moveOrCopyMessageSynchronous(account, srcFolder, messagesInThreads, destFolder,
                            false, null);
                } catch (MessagingException e) {
                    addErrorMessage(account, "Exception while moving messages", e);
                }
            }
        });
    }

    public void moveMessage(final Account account, final String srcFolder, final Message message,
            final String destFolder, final MessagingListener listener) {

        moveMessages(account, srcFolder, Collections.singletonList(message), destFolder, listener);
    }

    public void copyMessages(final Account account, final String srcFolder,
            final List<Message> messages, final String destFolder,
            final MessagingListener listener) {

        putBackground("copyMessages", null, new Runnable() {
            @Override
            public void run() {
                moveOrCopyMessageSynchronous(account, srcFolder, messages, destFolder, true,
                        listener);
            }
        });
    }

    public void copyMessagesInThread(final Account account, final String srcFolder,
            final List<Message> messages, final String destFolder) {

        putBackground("copyMessagesInThread", null, new Runnable() {
            @Override
            public void run() {
                try {
                    List<Message> messagesInThreads = collectMessagesInThreads(account, messages);
                    moveOrCopyMessageSynchronous(account, srcFolder, messagesInThreads, destFolder,
                            true, null);
                } catch (MessagingException e) {
                    addErrorMessage(account, "Exception while copying messages", e);
                }
            }
        });
    }

    public void copyMessage(final Account account, final String srcFolder, final Message message,
            final String destFolder, final MessagingListener listener) {

        copyMessages(account, srcFolder, Collections.singletonList(message), destFolder, listener);
    }

    private void moveOrCopyMessageSynchronous(final Account account, final String srcFolder,
            final List<Message> inMessages, final String destFolder, final boolean isCopy,
            MessagingListener listener) {

        try {
            Map<String, String> uidMap = new HashMap<String, String>();
            Store localStore = account.getLocalStore();
            Store remoteStore = account.getRemoteStore();
            if (!isCopy && (!remoteStore.isMoveCapable() || !localStore.isMoveCapable())) {
                return;
            }
            if (isCopy && (!remoteStore.isCopyCapable() || !localStore.isCopyCapable())) {
                return;
            }

            Folder localSrcFolder = localStore.getFolder(srcFolder);
            Folder localDestFolder = localStore.getFolder(destFolder);

            boolean unreadCountAffected = false;
            List<String> uids = new LinkedList<String>();
            for (Message message : inMessages) {
                String uid = message.getUid();
                if (!uid.startsWith(MailChat.LOCAL_UID_PREFIX)) {
                    uids.add(uid);
                }

                if (!unreadCountAffected && !message.isSet(Flag.SEEN)) {
                    unreadCountAffected = true;
                }
            }

            Message[] messages = localSrcFolder.getMessages(uids.toArray(EMPTY_STRING_ARRAY), null);
            if (messages.length > 0) {
                Map<String, Message> origUidMap = new HashMap<String, Message>();

                for (Message message : messages) {
                    origUidMap.put(message.getUid(), message);
                }

                if (MailChat.DEBUG)
                    Log.i(MailChat.LOG_TAG, "moveOrCopyMessageSynchronous: source folder = " + srcFolder
                          + ", " + messages.length + " messages, " + ", destination folder = " + destFolder + ", isCopy = " + isCopy);

                if (isCopy) {
                    FetchProfile fp = new FetchProfile();
                    fp.add(FetchProfile.Item.ENVELOPE);
                    fp.add(FetchProfile.Item.BODY);
                    localSrcFolder.fetch(messages, fp, null);
                    uidMap = localSrcFolder.copyMessages(messages, localDestFolder);

                    if (unreadCountAffected) {
                        // If this copy operation changes the unread count in the destination
                        // folder, notify the listeners.
                        int unreadMessageCount = localDestFolder.getUnreadMessageCount();
                        for (MessagingListener l : getListeners()) {
                            l.folderStatusChanged(account, destFolder, unreadMessageCount);
                        }
                    }
                } else {
                    uidMap = localSrcFolder.moveMessages(messages, localDestFolder);
                    for (Map.Entry<String, Message> entry : origUidMap.entrySet()) {
                        String origUid = entry.getKey();
                        Message message = entry.getValue();
                        for (MessagingListener l : getListeners()) {
                            l.messageUidChanged(account, srcFolder, origUid, message.getUid());
                        }
                    }
                    unsuppressMessages(account, messages);

                    if (unreadCountAffected) {
                        // If this move operation changes the unread count, notify the listeners
                        // that the unread count changed in both the source and destination folder.
                        int unreadMessageCountSrc = localSrcFolder.getUnreadMessageCount();
                        int unreadMessageCountDest = localDestFolder.getUnreadMessageCount();
                        for (MessagingListener l : getListeners()) {
                            l.folderStatusChanged(account, srcFolder, unreadMessageCountSrc);
                            l.folderStatusChanged(account, destFolder, unreadMessageCountDest);
                        }
                    }
                }

                queueMoveOrCopy(account, srcFolder, destFolder, isCopy, origUidMap.keySet().toArray(EMPTY_STRING_ARRAY), uidMap);
            }

			processPendingCommands(account);
for (MessagingListener l : getListeners()) {
				l.copyOrMoveMailSuccess(account, isCopy);
			}
		} catch (UnavailableStorageException e) {
			Log.i(MailChat.LOG_TAG,
					"Failed to move/copy message because storage is not available - trying again later.");
			for (MessagingListener l : getListeners()) {
				l.copyOrMoveFailed(account, isCopy);
			}
			throw new UnavailableAccountException(e);
		} catch (MessagingException me) {
			addErrorMessage(account, null, me);
			for (MessagingListener l : getListeners()) {
				l.copyOrMoveFailed(account, isCopy);
			}
			throw new RuntimeException("Error moving message", me);
		}
    }

    public void expunge(final Account account, final String folder, final MessagingListener listener) {
        putBackground("expunge", null, new Runnable() {
            @Override
            public void run() {
                queueExpunge(account, folder);
            }
        });
    }
    
    /*
    public void deleteDraft(final Account account, long id,boolean isShowToast) {
        LocalFolder localFolder = null;
        try {
            LocalStore localStore = account.getLocalStore();
            localFolder = localStore.getFolder(account.getDraftsFolderName());
            localFolder.open(Folder.OPEN_MODE_RW);
            String uid = localFolder.getMessageUidById(id);
            if (uid != null) {
                Message message = localFolder.getMessage(uid);
                if (message != null) {
                    deleteMessages(Collections.singletonList(message),isShowToast, null);
                }
            }
        } catch (MessagingException me) {
            addErrorMessage(account, null, me);
        } finally {
            closeFolder(localFolder);
        }
    }
    */
    // Sent draft should not be move to Trash folder, so we set a header to mark it.
    // Modified by LL
    // BEGIN
    public void deleteDraft(final Account account, long id,boolean isShowToast, boolean isSentDraft) {
        LocalFolder localFolder = null;
        try {
            LocalStore localStore = account.getLocalStore();
            localFolder = localStore.getFolder(account.getDraftsFolderName());
            localFolder.open(Folder.OPEN_MODE_RW);
            String uid = localFolder.getMessageUidById(id);
            if (uid != null) {
                Message message = localFolder.getMessage(uid);
                if (message != null) {
                	if (isSentDraft) {
                		message.setHeader(MailChat.SENT_DRAFT_HEADER, MailChat.SENT_DRAFT_HEADER_TRUE);
                	} else {
                		message.setHeader(MailChat.SENT_DRAFT_HEADER, MailChat.SENT_DRAFT_HEADER_FALSE);
                	}
                    deleteMessages(Collections.singletonList(message),isShowToast, null);
                }
            }
        } catch (MessagingException me) {
            addErrorMessage(account, null, me);
        } finally {
            closeFolder(localFolder);
        }
    }
    // END
    /**
     * 
     * method name: deleteOutboxTempMail
     * function @Description: TODO
     * Parameters and return values description:
     *      @param account
     *      @param id   field_name
     *      void   return type
     *  @History memory：
     *     @Date：2015-6-8 下午3:05:49	@Modified by：zhangjx
     *     @Description:点击发件箱进入重新发邮件前，删除旧邮件
     */
    public void deleteOutboxTempMail(final Account account, String id) {
        LocalFolder localFolder = null;
        try {
			LocalStore localStore = account.getLocalStore();
			localFolder = localStore.getFolder(account.getOutboxFolderName());
			localFolder.open(Folder.OPEN_MODE_RW);
			Message message = localFolder.getMessage(id);
			if (message != null) {
				message.setHeader(MailChat.SENT_DRAFT_HEADER,
						MailChat.SENT_DRAFT_HEADER_TRUE);
				deleteMessages(Collections.singletonList(message), false, null);
			}
        } catch (MessagingException me) {
            addErrorMessage(account, null, me);
        } finally {
            closeFolder(localFolder);
        }
    }
    
    public void deleteThreads(final List<Message> messages) {
        actOnMessages(messages, new MessageActor() {

            @Override
            public void act(final Account account, final Folder folder,
                    final List<Message> accountMessages) {

                suppressMessages(account, messages);

                putBackground("deleteThreads", null, new Runnable() {
                    @Override
                    public void run() {
                        deleteThreadsSynchronous(account, folder.getName(), accountMessages);
                    }
                });
            }
        });
    }

    public void deleteThreadsSynchronous(Account account, String folderName,
            List<Message> messages) {

        try {
            List<Message> messagesToDelete = collectMessagesInThreads(account, messages);

            deleteMessagesSynchronous(account, folderName,
                    messagesToDelete.toArray(EMPTY_MESSAGE_ARRAY),true, null);
        } catch (MessagingException e) {
            Log.e(MailChat.LOG_TAG, "Something went wrong while deleting threads", e);
        }
    }

    public List<Message> collectMessagesInThreads(Account account, List<Message> messages)
            throws MessagingException {

        LocalStore localStore = account.getLocalStore();

        List<Message> messagesInThreads = new ArrayList<Message>();
        for (Message message : messages) {
            LocalMessage localMessage = (LocalMessage) message;
            long rootId = localMessage.getRootId();
            long threadId = (rootId == -1) ? localMessage.getThreadId() : rootId;

            Message[] messagesInThread = localStore.getMessagesInThread(threadId);
            Collections.addAll(messagesInThreads, messagesInThread);
        }

        return messagesInThreads;
    }

    public void deleteMessages(final List<Message> messages,final boolean isShowToast, final MessagingListener listener) {
        actOnMessages(messages, new MessageActor() {

            @Override
            public void act(final Account account, final Folder folder,
            final List<Message> accountMessages) {
                suppressMessages(account, messages);

                putBackground("deleteMessages", null, new Runnable() {
                    @Override
                    public void run() {
                        deleteMessagesSynchronous(account, folder.getName(),
                                accountMessages.toArray(EMPTY_MESSAGE_ARRAY), isShowToast,listener);
                    }
                });
            }

        });

    }

    private void deleteMessagesSynchronous(final Account account, final String folder, final Message[] messages,final boolean isShowToast,
                                           MessagingListener listener) {
        Folder localFolder = null;
        Folder localTrashFolder = null;
        String[] uids = getUidsFromMessages(messages);
        try {
            //We need to make these callbacks before moving the messages to the trash
            //as messages get a new UID after being moved
			if (isShowToast) {
				for (Message message : messages) {
					for (MessagingListener l : getListeners(listener)) {
						l.messageDeleted(account, folder, message);
					}
				}
			}
            Store localStore = account.getLocalStore();
            localFolder = localStore.getFolder(folder);
            Map<String, String> uidMap = null;
            
            //if (folder.equals(account.getTrashFolderName()) || !account.hasTrashFolder()) {
            // Sent draft should not be move to Trash folder, so we check the header mark
            // and deal with this case.
            // Modified by LL
            // BEGIN
            boolean isSentDraft = false;
            if (folder.equals(account.getDraftsFolderName()) && (messages.length == 1)) {
            	String[] headers = messages[0].getHeader(MailChat.SENT_DRAFT_HEADER);
            	if (headers != null && headers.length > 0 
            			&& MailChat.SENT_DRAFT_HEADER_TRUE.equals(headers[0])) {
            		isSentDraft = true;
            	}
            }
            if (folder.equals(account.getTrashFolderName()) || !account.hasTrashFolder() || isSentDraft) {
            // END
                if (MailChat.DEBUG)
                    Log.d(MailChat.LOG_TAG, "Deleting messages in trash folder or trash set to -None-, not copying");

                localFolder.setFlags(messages, new Flag[] { Flag.DELETED }, true);
            } else {
                localTrashFolder = localStore.getFolder(account.getTrashFolderName());
                if (!localTrashFolder.exists()) {
                    localTrashFolder.create(Folder.FolderType.HOLDS_MESSAGES);
                }
                if (localTrashFolder.exists()) {
                    if (MailChat.DEBUG)
                        Log.d(MailChat.LOG_TAG, "Deleting messages in normal folder, moving");

                    uidMap = localFolder.moveMessages(messages, localTrashFolder);

                }
            }

            for (MessagingListener l : getListeners()) {
                l.folderStatusChanged(account, folder, localFolder.getUnreadMessageCount());
                if (localTrashFolder != null) {
                    l.folderStatusChanged(account, account.getTrashFolderName(), localTrashFolder.getUnreadMessageCount());
                }
            }

            if (MailChat.DEBUG)
                Log.d(MailChat.LOG_TAG, "Delete policy for account " + account.getDescription() + " is " + account.getDeletePolicy());
            /*
            if (folder.equals(account.getOutboxFolderName())) {
                for (Message message : messages) {
                    // If the message was in the Outbox, then it has been copied to local Trash, and has
                    // to be copied to remote trash
                    PendingCommand command = new PendingCommand();
                    command.command = PENDING_COMMAND_APPEND;
                    command.arguments =
                        new String[] {
                        account.getTrashFolderName(),
                        message.getUid()
                    };
                    queuePendingCommand(account, command);
                }
                processPendingCommands(account);
            } else
            */
            if (account.getDeletePolicy() == Account.DELETE_POLICY_ON_DELETE) {
                if (folder.equals(account.getTrashFolderName())) {
                    queueSetFlag(account, folder, Boolean.toString(true), Flag.DELETED.toString(), uids);
                } else {
                    queueMoveOrCopy(account, folder, account.getTrashFolderName(), false, uids, uidMap);
                }
                processPendingCommands(account);
            } else if (account.getDeletePolicy() == Account.DELETE_POLICY_MARK_AS_READ) {
                queueSetFlag(account, folder, Boolean.toString(true), Flag.SEEN.toString(), uids);
                processPendingCommands(account);
            } else {
                if (MailChat.DEBUG)
                    Log.d(MailChat.LOG_TAG, "Delete policy " + account.getDeletePolicy() + " prevents delete from server");
            }

            unsuppressMessages(account, messages);
        } catch (UnavailableStorageException e) {
            Log.i(MailChat.LOG_TAG, "Failed to delete message because storage is not available - trying again later.");
            throw new UnavailableAccountException(e);
        } catch (MessagingException me) {
            addErrorMessage(account, null, me);

            throw new RuntimeException("Error deleting message from local store.", me);
        } finally {
            closeFolder(localFolder);
            closeFolder(localTrashFolder);
        }
    }

    private String[] getUidsFromMessages(Message[] messages) {
        String[] uids = new String[messages.length];
        for (int i = 0; i < messages.length; i++) {
            uids[i] = messages[i].getUid();
        }
        return uids;
    }

    private void processPendingEmptyTrash(PendingCommand command, Account account) throws MessagingException {
        Store remoteStore = account.getRemoteStore();

        Folder remoteFolder = remoteStore.getFolder(account.getTrashFolderName());
        try {
            if (remoteFolder.exists()) {
                remoteFolder.open(Folder.OPEN_MODE_RW);
                remoteFolder.setFlags(new Flag [] { Flag.DELETED }, true);
                if (Account.EXPUNGE_IMMEDIATELY.equals(account.getExpungePolicy())) {
                    remoteFolder.expunge();
                }

                // When we empty trash, we need to actually synchronize the folder
                // or local deletes will never get cleaned up
                synchronizeFolder(account, remoteFolder, true, 0, null);
                compact(account, null);


            }
        } finally {
            closeFolder(remoteFolder);
        }
    }

    public void emptyTrash(final Account account, MessagingListener listener) {
        putBackground("emptyTrash", listener, new Runnable() {
            @Override
            public void run() {
                LocalFolder localFolder = null;
                try {
                    Store localStore = account.getLocalStore();
                    localFolder = (LocalFolder) localStore.getFolder(account.getTrashFolderName());
                    localFolder.open(Folder.OPEN_MODE_RW);

                    boolean isTrashLocalOnly = isTrashLocalOnly(account);
                    if (isTrashLocalOnly) {
                        localFolder.clearAllMessages();
                    } else {
                        localFolder.setFlags(new Flag[] { Flag.DELETED }, true);
                    }

                    for (MessagingListener l : getListeners()) {
                        l.emptyTrashCompleted(account);
                    }

                    if (!isTrashLocalOnly) {
                        List<String> args = new ArrayList<String>();
                        PendingCommand command = new PendingCommand();
                        command.command = PENDING_COMMAND_EMPTY_TRASH;
                        command.arguments = args.toArray(EMPTY_STRING_ARRAY);
                        queuePendingCommand(account, command);
                        processPendingCommands(account);
                    }
                } catch (UnavailableStorageException e) {
                    Log.i(MailChat.LOG_TAG, "Failed to empty trash because storage is not available - trying again later.");
                    throw new UnavailableAccountException(e);
                } catch (Exception e) {
                    Log.e(MailChat.LOG_TAG, "emptyTrash failed", e);
                    addErrorMessage(account, null, e);
                } finally {
                    closeFolder(localFolder);
                }
            }
        });
    }

    /**
     * Find out whether the account type only supports a local Trash folder.
     *
     * <p>Note: Currently this is only the case for POP3 accounts.</p>
     *
     * @param account
     *         The account to check.
     *
     * @return {@code true} if the account only has a local Trash folder that is not synchronized
     *         with a folder on the server. {@code false} otherwise.
     *
     * @throws MessagingException
     *         In case of an error.
     */
    private boolean isTrashLocalOnly(Account account) throws MessagingException {
        // TODO: Get rid of the tight coupling once we properly support local folders
        return (account.getRemoteStore() instanceof Pop3Store);
    }

    public void sendAlternate(final Context context, Account account, Message message) {
        if (MailChat.DEBUG)
            Log.d(MailChat.LOG_TAG, "About to load message " + account.getDescription() + ":" + message.getFolder().getName()
                  + ":" + message.getUid() + " for sendAlternate");

        loadMessageForView(account, message.getFolder().getName(),
        message.getUid(), new MessagingListener() {
            @Override
            public void loadMessageForViewBodyAvailable(Account account, String folder, String uid,
            Message message) {
                if (MailChat.DEBUG)
                    Log.d(MailChat.LOG_TAG, "Got message " + account.getDescription() + ":" + folder
                          + ":" + message.getUid() + " for sendAlternate");

                try {
                    Intent msg = new Intent(Intent.ACTION_SEND);
                    String quotedText = null;
                    Part part = MimeUtility.findFirstPartByMimeType(message,
                                "text/plain");
                    if (part == null) {
                        part = MimeUtility.findFirstPartByMimeType(message, "text/html");
                    }
                    if (part != null) {
                        quotedText = MimeUtility.getTextFromPart(part);
                    }
                    if (quotedText != null) {
                        msg.putExtra(Intent.EXTRA_TEXT, quotedText);
                    }
                    msg.putExtra(Intent.EXTRA_SUBJECT, message.getSubject());

                    Address[] from = message.getFrom();
                    String[] senders = new String[from.length];
                    for (int i = 0; i < from.length; i++) {
                        senders[i] = from[i].toString();
                    }
                    msg.putExtra(Intents.Share.EXTRA_FROM, senders);

                    Address[] to = message.getRecipients(RecipientType.TO);
                    String[] recipientsTo = new String[to.length];
                    for (int i = 0; i < to.length; i++) {
                        recipientsTo[i] = to[i].toString();
                    }
                    msg.putExtra(Intent.EXTRA_EMAIL, recipientsTo);

                    Address[] cc = message.getRecipients(RecipientType.CC);
                    String[] recipientsCc = new String[cc.length];
                    for (int i = 0; i < cc.length; i++) {
                        recipientsCc[i] = cc[i].toString();
                    }
                    msg.putExtra(Intent.EXTRA_CC, recipientsCc);

                    msg.setType("text/plain");
                    context.startActivity(Intent.createChooser(msg, context.getString(R.string.send_alternate_chooser_title)));
                } catch (MessagingException me) {
                    Log.e(MailChat.LOG_TAG, "Unable to send email through alternate program", me);
                }
            }
        });

    }

    /**
     * Checks mail for one or multiple accounts. If account is null all accounts
     * are checked.
     *
     * @param context
     * @param account
     * @param listener
     */
    public void checkMail(final Context context, final Account account,
                          final boolean ignoreLastCheckedTime,
                          final boolean useManualWakeLock,
                          final MessagingListener listener) {

        TracingWakeLock twakeLock = null;
        if (useManualWakeLock) {
            TracingPowerManager pm = TracingPowerManager.getPowerManager(context);

            twakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MailChat MessagingController.checkMail");
            twakeLock.setReferenceCounted(false);
            twakeLock.acquire(MailChat.MANUAL_WAKE_LOCK_TIMEOUT);
        }
        final TracingWakeLock wakeLock = twakeLock;

        for (MessagingListener l : getListeners()) {
            l.checkMailStarted(context, account);
        }
        putBackground("checkMail", listener, new Runnable() {
            @Override
            public void run() {

                try {
                    if (MailChat.DEBUG)
                        Log.i(MailChat.LOG_TAG, "Starting mail check");
                    Preferences prefs = Preferences.getPreferences(context);

                    Collection<Account> accounts;
                    if (account != null) {
                        accounts = new ArrayList<Account>(1);
                        accounts.add(account);
                    } else {
                        accounts = prefs.getAvailableAccounts();
                    }

                    for (final Account account : accounts) {
                        /*
                        // 三五 3.0/2.0 邮箱和隐藏账号不需要Poll
                        if (!(account.getVersion_35Mail() == 1
                                || account.getVersion_35Mail() == 2
                                || account.isHideAccount())) {
                        */
                        // 隐藏账号和认证错误账号不能定时刷新文件夹
                        if (!account.isHideAccount() && account.isAuthenticated()) {
							checkMailForAccount(context, account,ignoreLastCheckedTime, prefs, listener);
						}
                    }

                } catch (Exception e) {
                    Log.e(MailChat.LOG_TAG, "Unable to synchronize mail", e);
                    addErrorMessage(account, null, e);
                }
                putBackground("finalize sync", null, new Runnable() {
                    @Override
                    public void run() {

                        if (MailChat.DEBUG)
                            Log.i(MailChat.LOG_TAG, "Finished mail sync");

                        if (wakeLock != null) {
                            wakeLock.release();
                        }
                        for (MessagingListener l : getListeners()) {
                            l.checkMailFinished(context, account);
                        }
						// 更新联系人列表
//						for (MessagingListener l : getListeners(null)) {
//							l.addContactFinish(account);
//						}
                    }
                }
                             );
            }
        });
    }



    private void checkMailForAccount(final Context context, final Account account,
                                     final boolean ignoreLastCheckedTime,
                                     final Preferences prefs,
                                     final MessagingListener listener) {
        if (!account.isAvailable(context)) {
            if (MailChat.DEBUG) {
                Log.i(MailChat.LOG_TAG, "Skipping synchronizing unavailable account " + account.getDescription());
            }
            return;
        }
        
        //时间转换为毫秒
        final long accountInterval = account.getAutomaticCheckIntervalMinutes() * 60 * 1000;
        Log.i(MailChat.LOG_COLLECTOR_TAG,">>>>>  "+account.getEmail()+"  aoto refresh mail in  "+ accountInterval+" /ms");
        if (!ignoreLastCheckedTime && accountInterval <= 0) {
            if (MailChat.DEBUG)
                Log.i(MailChat.LOG_TAG, "Skipping synchronizing account " + account.getDescription());
            return;
        }

        if (MailChat.DEBUG)
            Log.i(MailChat.LOG_TAG, "Synchronizing account " + account.getDescription());

        account.setRingNotified(false);

//        sendPendingMessages(account, listener);

        try {
            Account.FolderMode aDisplayMode = account.getFolderDisplayMode();
            Account.FolderMode aSyncMode = account.getFolderSyncMode();

            Store localStore = account.getLocalStore();
            for (final Folder folder : localStore.getPersonalNamespaces(false)) {
                folder.open(Folder.OPEN_MODE_RW);
                folder.refresh(prefs);

                Folder.FolderClass fDisplayClass = folder.getDisplayClass();
                Folder.FolderClass fSyncClass = folder.getSyncClass();

//                if (modeMismatch(aDisplayMode, fDisplayClass)) {
//                    // Never sync a folder that isn't displayed
//                    /*
//                    if (MailChat.DEBUG)
//                        Log.v(MailChat.LOG_TAG, "Not syncing folder " + folder.getName() +
//                              " which is in display mode " + fDisplayClass + " while account is in display mode " + aDisplayMode);
//                    */
//
//                    continue;
//                }

//                if (modeMismatch(aSyncMode, fSyncClass)) {
//                    // Do not sync folders in the wrong class
//                    /*
//                    if (MailChat.DEBUG)
//                        Log.v(MailChat.LOG_TAG, "Not syncing folder " + folder.getName() +
//                              " which is in sync mode " + fSyncClass + " while account is in sync mode " + aSyncMode);
//                    */
//
//                    continue;
//                }
                //需要定时刷新邮件的邮件夹
                if (folder.isAllowPush()) {
                    synchronizeFolder(account, folder, ignoreLastCheckedTime, accountInterval, listener);
				}
            }
        } catch (MessagingException e) {
            Log.e(MailChat.LOG_TAG, "Unable to synchronize account " + account.getName(), e);
            addErrorMessage(account, null, e);
        } finally {
            putBackground("clear notification flag for " + account.getDescription(), null, new Runnable() {
                @Override
                public void run() {
                    if (MailChat.DEBUG)
                        Log.v(MailChat.LOG_TAG, "Clearing notification flag for " + account.getDescription());
                    account.setRingNotified(false);
                    try {
                        AccountStats stats = account.getStats(context);
                        if (stats == null || stats.unreadMessageCount == 0) {
                            notifyAccountCancel(context, account);
                        }
                    } catch (MessagingException e) {
                        Log.e(MailChat.LOG_TAG, "Unable to getUnreadMessageCount for account: " + account, e);
                    }
                }
            }
                         );
        }


    }


    private void synchronizeFolder(
        final Account account,
        final Folder folder,
        final boolean ignoreLastCheckedTime,
        final long accountInterval,
        final MessagingListener listener) {


        if (MailChat.DEBUG)
            Log.v(MailChat.LOG_TAG, "Folder " + folder.getName() + " was last synced @ " +
                  new Date(folder.getLastChecked()));

        if (!ignoreLastCheckedTime && folder.getLastChecked() >
                (System.currentTimeMillis() - accountInterval)) {
            if (MailChat.DEBUG)
                Log.v(MailChat.LOG_TAG, "Not syncing folder " + folder.getName()
                      + ", previously synced @ " + new Date(folder.getLastChecked())
                      + " which would be too recent for the account period");

            return;
        }
        putBackground("sync" + folder.getName(), null, new Runnable() {
            @Override
            public void run() {
                LocalFolder tLocalFolder = null;
                try {
                    // In case multiple Commands get enqueued, don't run more than
                    // once
                    final LocalStore localStore = account.getLocalStore();
                    tLocalFolder = localStore.getFolder(folder.getName());
                    tLocalFolder.open(Folder.OPEN_MODE_RW);

                    if (!ignoreLastCheckedTime && tLocalFolder.getLastChecked() >
                    (System.currentTimeMillis() - accountInterval)) {
                        if (MailChat.DEBUG)
                            Log.v(MailChat.LOG_TAG, "Not running Command for folder " + folder.getName()
                                  + ", previously synced @ " + new Date(folder.getLastChecked())
                                  + " which would be too recent for the account period");
                        return;
                    }
                    //notifyFetchingMail(account, folder);
                    try {
                        synchronizeMailboxSynchronous(account, folder.getName(), listener, null);
                    } finally {
                        //notifyFetchingMailCancel(account);
                    }
                } catch (Exception e) {

                    Log.e(MailChat.LOG_TAG, "Exception while processing folder " +
                          account.getDescription() + ":" + folder.getName(), e);
                    addErrorMessage(account, null, e);
                } finally {
                    closeFolder(tLocalFolder);
                }
            }
        }
                     );


    }



    public void compact(final Account account, final MessagingListener ml) {
        putBackground("compact:" + account.getDescription(), ml, new Runnable() {
            @Override
            public void run() {
                try {
                    LocalStore localStore = account.getLocalStore();
                    long oldSize = localStore.getSize();
                    localStore.compact();
                    long newSize = localStore.getSize();
                    for (MessagingListener l : getListeners(ml)) {
                        l.accountSizeChanged(account, oldSize, newSize);
                    }
                } catch (UnavailableStorageException e) {
                    Log.i(MailChat.LOG_TAG, "Failed to compact account because storage is not available - trying again later.");
                    throw new UnavailableAccountException(e);
                } catch (Exception e) {
                    Log.e(MailChat.LOG_TAG, "Failed to compact account " + account.getDescription(), e);
                }
            }
        });
    }

    public void clear(final Account account, final MessagingListener ml) {
        putBackground("clear:" + account.getDescription(), ml, new Runnable() {
            @Override
            public void run() {
                try {
                    LocalStore localStore = account.getLocalStore();
                    long oldSize = localStore.getSize();
                    localStore.clear();
                    localStore.resetVisibleLimits(account.getDisplayCount());
                    long newSize = localStore.getSize();
                    AccountStats stats = new AccountStats();
                    stats.size = newSize;
                    stats.unreadMessageCount = 0;
                    stats.flaggedMessageCount = 0;
                    for (MessagingListener l : getListeners(ml)) {
                        l.accountSizeChanged(account, oldSize, newSize);
                        l.accountStatusChanged(account, stats);
                    }
                } catch (UnavailableStorageException e) {
                    Log.i(MailChat.LOG_TAG, "Failed to clear account because storage is not available - trying again later.");
                    throw new UnavailableAccountException(e);
                } catch (Exception e) {
                    Log.e(MailChat.LOG_TAG, "Failed to clear account " + account.getDescription(), e);
                }
            }
        });
    }

    public void recreate(final Account account, final MessagingListener ml) {
        putBackground("recreate:" + account.getDescription(), ml, new Runnable() {
            @Override
            public void run() {
                try {
                    LocalStore localStore = account.getLocalStore();
                    long oldSize = localStore.getSize();
                    localStore.recreate();
                    localStore.resetVisibleLimits(account.getDisplayCount());
                    long newSize = localStore.getSize();
                    AccountStats stats = new AccountStats();
                    stats.size = newSize;
                    stats.unreadMessageCount = 0;
                    stats.flaggedMessageCount = 0;
                    for (MessagingListener l : getListeners(ml)) {
                        l.accountSizeChanged(account, oldSize, newSize);
                        l.accountStatusChanged(account, stats);
                    }
                } catch (UnavailableStorageException e) {
                    Log.i(MailChat.LOG_TAG, "Failed to recreate an account because storage is not available - trying again later.");
                    throw new UnavailableAccountException(e);
                } catch (Exception e) {
                    Log.e(MailChat.LOG_TAG, "Failed to recreate account " + account.getDescription(), e);
                }
            }
        });
    }


    private boolean shouldNotifyForMessage(Account account, LocalFolder localFolder, Message message) {
        // If we don't even have an account name, don't show the notification.
        // (This happens during initial account setup)
        if (account.getName() == null) {
            return false;
        }

        // Do not notify if the user does not have notifications enabled or if the message has
        // been read.
        if (!account.isNotifyNewMail() || message.isSet(Flag.SEEN)) {
            return false;
        }

        Account.FolderMode aDisplayMode = account.getFolderDisplayMode();
        Account.FolderMode aNotifyMode = account.getFolderNotifyNewMailMode();
        Folder.FolderClass fDisplayClass = localFolder.getDisplayClass();
        Folder.FolderClass fNotifyClass = localFolder.getNotifyClass();

        if (modeMismatch(aDisplayMode, fDisplayClass)) {
            // Never notify a folder that isn't displayed
            return false;
        }

        if (modeMismatch(aNotifyMode, fNotifyClass)) {
            // Do not notify folders in the wrong class
            return false;
        }

        // If the account is a POP3 account and the message is older than the oldest message we've
        // previously seen, then don't notify about it.
        if (account.getStoreUri().startsWith("pop3") &&
                message.olderThan(new Date(account.getLatestOldMessageSeenTime()))) {
            return false;
        }

        // No notification for new messages in Trash, Drafts, Spam or Sent folder.
        // But do notify if it's the INBOX (see issue 1817).
        Folder folder = message.getFolder();
        if (folder != null) {
            String folderName = folder.getName();
            if (!account.getInboxFolderName().equals(folderName) &&
                    (account.getTrashFolderName().equals(folderName)
                     || account.getDraftsFolderName().equals(folderName)
                     || account.getSpamFolderName().equals(folderName)
                     || account.getSentFolderName().equals(folderName))) {
                return false;
            }
        }

        if (message.getUid() != null && localFolder.getLastUid() != null) {
            try {
                Integer messageUid = Integer.parseInt(message.getUid());
                if (messageUid <= localFolder.getLastUid()) {
                    if (MailChat.DEBUG)
                        Log.d(MailChat.LOG_TAG, "Message uid is " + messageUid + ", max message uid is " +
                              localFolder.getLastUid() + ".  Skipping notification.");
                    return false;
                }
            } catch (NumberFormatException e) {
                // Nothing to be done here.
            }
        }

        // Don't notify if the sender address matches one of our identities and the user chose not
        // to be notified for such messages.
        if (account.isAnIdentity(message.getFrom()) && !account.isNotifySelfNewMail()) {
            return false;
        }

        return true;
    }

    /**
     * Get the pending notification data for an account.
     * See {@link NotificationData}.
     *
     * @param account The account to retrieve the pending data for
     * @param previousUnreadMessageCount The number of currently pending messages, which will be used
     *                                    if there's no pending data yet. If passed as null, a new instance
     *                                    won't be created if currently not existent.
     * @return A pending data instance, or null if one doesn't exist and
     *          previousUnreadMessageCount was passed as null.
     */
    private NotificationData getNotificationData(Account account, Integer previousUnreadMessageCount) {
        NotificationData data;

        synchronized (notificationData) {
            data = notificationData.get(account.getAccountNumber());
            if (data == null && previousUnreadMessageCount != null) {
                data = new NotificationData(previousUnreadMessageCount);
                notificationData.put(account.getAccountNumber(), data);
            }
        }

        return data;
    }

    private CharSequence getMessageSender(Context context, Account account, Message message) {
        try {
            boolean isSelf = false;
            final Contacts contacts = MailChat.showContactName() ? Contacts.getInstance(context) : null;
            final Address[] fromAddrs = message.getFrom();

            if (fromAddrs != null) {
                isSelf = account.isAnIdentity(fromAddrs);
                if (!isSelf && fromAddrs.length > 0) {
                    return fromAddrs[0].toFriendly(contacts).toString();
                }
            }

            if (isSelf) {
                // show To: if the message was sent from me
                Address[] rcpts = message.getRecipients(Message.RecipientType.TO);

                if (rcpts != null && rcpts.length > 0) {
                    return context.getString(R.string.message_to_fmt,
                            rcpts[0].toFriendly(contacts).toString());
                }

                return context.getString(R.string.general_no_sender);
            }
        } catch (MessagingException e) {
            Log.e(MailChat.LOG_TAG, "Unable to get sender information for notification.", e);
        }

        return null;
    }

    private CharSequence getMessageSubject(Context context, Message message) {
        String subject = message.getSubject();
        if (!TextUtils.isEmpty(subject)) {
            return subject;
        }

        return context.getString(R.string.general_no_subject);
    }

    private static TextAppearanceSpan sEmphasizedSpan;
    private TextAppearanceSpan getEmphasizedSpan(Context context) {
        if (sEmphasizedSpan == null) {
            sEmphasizedSpan = new TextAppearanceSpan(context,
                    R.style.TextAppearance_StatusBar_EventContent_Emphasized);
        }
        return sEmphasizedSpan;
    }

    private CharSequence getMessagePreview(Context context, Message message) {
        CharSequence subject = getMessageSubject(context, message);
        String snippet = message.getPreview();

        if (TextUtils.isEmpty(subject)) {
            return snippet;
        } else if (TextUtils.isEmpty(snippet)) {
            return subject;
        }

        SpannableStringBuilder preview = new SpannableStringBuilder();
        preview.append(subject);
        preview.append('\n');
        preview.append(snippet);

        preview.setSpan(getEmphasizedSpan(context), 0, subject.length(), 0);

        return preview;
    }

    private CharSequence buildMessageSummary(Context context, CharSequence sender, CharSequence subject) {
        if (sender == null) {
            return subject;
        }

        SpannableStringBuilder summary = new SpannableStringBuilder();
        summary.append(sender);
        summary.append(" ");
        summary.append(subject);

        summary.setSpan(getEmphasizedSpan(context), 0, sender.length(), 0);

        return summary;
    }

    public static final boolean platformSupportsExtendedNotifications() {
        // supported in Jellybean
        // TODO: use constant once target SDK is set to >= 16
        return Build.VERSION.SDK_INT >= 16;
    }

    private Message findNewestMessageForNotificationLocked(Context context,
            Account account, NotificationData data) {
        if (!data.messages.isEmpty()) {
            return data.messages.getFirst();
        }

        if (!data.droppedMessages.isEmpty()) {
            return data.droppedMessages.getFirst().restoreToLocalMessage(context);
        }

        return null;
    }

    /**
     * Creates a notification of a newly received message.
     */
    private void notifyAccount(Context context, Account account,
            Message message, int previousUnreadMessageCount) {
        final NotificationData data = getNotificationData(account, previousUnreadMessageCount);
        synchronized (data) {
			if (account.getVersion_35Mail() == 0
					&& account.isNewMailNotifation()) {
				notifyAccountWithDataLocked(context, account, message, data);
			}
        }
    }

    private void notifyAccountWithDataLocked(Context context, Account account,
            Message message, NotificationData data) {
        boolean updateSilently = false;
        if (message == null) {
            /* this can happen if a message we previously notified for is read or deleted remotely */
            message = findNewestMessageForNotificationLocked(context, account, data);
            updateSilently = true;
            if (message == null) {
                // seemingly both the message list as well as the overflow list is empty;
                // it probably is a good idea to cancel the notification in that case
                notifyAccountCancel(context, account);
                return;
            }
        } else {
            data.addMessage(message);
        }

        final KeyguardManager keyguardService = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        final CharSequence sender = getMessageSender(context, account, message);
        final CharSequence subject = getMessageSubject(context, message);
        CharSequence summary = buildMessageSummary(context, sender, subject);

        boolean privacyModeEnabled =
                (MailChat.getNotificationHideSubject() == NotificationHideSubject.ALWAYS) ||
                (MailChat.getNotificationHideSubject() == NotificationHideSubject.WHEN_LOCKED &&
                keyguardService.inKeyguardRestrictedInputMode());

        if (privacyModeEnabled || summary.length() == 0) {
            summary = context.getString(R.string.notification_new_title);
        }

        NotificationManager notifMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.icon_notification_mail_small);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_notification));
        builder.setWhen(System.currentTimeMillis());
        if (!updateSilently) {
            builder.setTicker(summary);
        }

        final int newMessages = data.getNewMessageCount();
        final int unreadCount = data.unreadBeforeNotification + newMessages;

        builder.setNumber(unreadCount);

        String accountDescr = (account.getDescription() != null) ?
                account.getDescription() : account.getEmail();
        final ArrayList<MessageReference> allRefs = data.getAllMessageRefs();

        if (platformSupportsExtendedNotifications() && !privacyModeEnabled) {
            if (newMessages > 1) {
                // multiple messages pending, show inbox style
                NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(builder);
                for (Message m : data.messages) {
                    style.addLine(buildMessageSummary(context,
                            getMessageSender(context, account, m),
                            getMessageSubject(context, m)));
                }
//                if (!data.droppedMessages.isEmpty()) {
//                    style.setSummaryText(context.getString(R.string.notification_additional_messages,
//                            data.droppedMessages.size(), accountDescr));
//                }
                style.setSummaryText(accountDescr);
                String title = context.getString(R.string.notification_new_messages_title, newMessages);
                style.setBigContentTitle(title);
                builder.setContentTitle(title);
                Message m = data.messages.get(data.messages.size()-1);
                builder.setContentText(buildMessageSummary(mApplication,getMessageSender(context, account, m)+":",
                        getMessageSubject(context, m)));
                builder.setSubText(accountDescr);
                builder.setStyle(style);
            } else {
                // single message pending, show big text
                NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle(builder);
                CharSequence preview = getMessagePreview(context, message);
                if (preview != null) {
                    style.bigText(preview);
                }
                builder.setContentText(subject);
                builder.setSubText(accountDescr);
                builder.setContentTitle(sender);
                builder.setStyle(style);

                builder.addAction(R.drawable.ic_action_single_message_options_dark,
                        context.getString(R.string.notification_action_reply),
                        NotificationActionService.getReplyIntent(context, account, message.makeMessageReference()));
            }

            builder.addAction(R.drawable.ic_action_mark_as_read_dark,
                    context.getString(R.string.notification_action_mark_as_read),
                    NotificationActionService.getReadAllMessagesIntent(context, account, allRefs));

            NotificationQuickDelete deleteOption = MailChat.getNotificationQuickDeleteBehaviour();
            boolean showDeleteAction = deleteOption == NotificationQuickDelete.ALWAYS ||
                    (deleteOption == NotificationQuickDelete.FOR_SINGLE_MSG && newMessages == 1);

            if (showDeleteAction) {
                // we need to pass the action directly to the activity, otherwise the
                // status bar won't be pulled up and we won't see the confirmation (if used)
                builder.addAction(R.drawable.ic_action_delete_dark,
                        context.getString(R.string.notification_action_delete),
                        NotificationDeleteConfirmation.getIntent(context, account, allRefs));
            }
        } else {
            String accountNotice = context.getString(R.string.notification_new_one_account_fmt,
                    unreadCount, accountDescr);
            builder.setContentTitle(accountNotice);
            builder.setContentText(summary);
        }

        for (Message m : data.messages) {
            if (m.isSet(Flag.FLAGGED)) {
                builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                break;
            }
        }

        TaskStackBuilder stack;
        boolean treatAsSingleMessageNotification;

        if (platformSupportsExtendedNotifications()) {
            // in the new-style notifications, we focus on the new messages, not the unread ones
            treatAsSingleMessageNotification = newMessages == 1;
        } else {
            // in the old-style notifications, we focus on unread messages, as we don't have a
            // good way to express the new message count
            treatAsSingleMessageNotification = unreadCount == 1;
        }

        if (treatAsSingleMessageNotification) {
            stack = buildMessageViewBackStack(context, message.makeMessageReference());
        } else if (account.goToUnreadMessageSearch()) {
            stack = buildUnreadBackStack(context, account);//这个build应该没有用了
        } else {
            String initialFolder = message.getFolder().getName();
            /* only go to folder if all messages are in the same folder, else go to folder list */
            for (MessageReference ref : allRefs) {
                if (!TextUtils.equals(initialFolder, ref.folderName)) {
                    initialFolder = null;
                    break;
                }
            }

            stack = buildMessageListBackStack(context, account, initialFolder);
        }

        builder.setContentIntent(stack.getPendingIntent(
                account.getAccountNumber(),
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT));
        builder.setDeleteIntent(NotificationActionService.getAcknowledgeIntent(context, account));

        // Only ring or vibrate if we have not done so already on this account and fetch
        boolean ringAndVibrate = false;
        if (!updateSilently && !account.isRingNotified()) {
            account.setRingNotified(true);
            ringAndVibrate = true;
        }

        NotificationSetting n = account.getNotificationSetting();
        //帐号提醒设置
//        configureNotification(
//                builder,
//                (n.shouldRing()) ?  n.getRingtone() : null,
//                (n.shouldVibrate()) ? n.getVibration() : null,
//                (n.isLed()) ? Integer.valueOf(n.getLedColor()) : null,
//                MailChat.NOTIFICATION_LED_BLINK_SLOW,
//                ringAndVibrate);
        //全局提醒设置
    	globalConfigureNotification(builder,MailChat.isNotifyRingtone(), MailChat.isNotifyVibrateOn());
        notifMgr.notify(account.getAccountNumber(), builder.build());
    }

    private TaskStackBuilder buildAccountsBackStack(Context context) {
        TaskStackBuilder stack = TaskStackBuilder.create(context);
        if (!skipAccountsInBackStack(context)) {
            stack.addNextIntent(new Intent(context, Accounts.class).putExtra(Accounts.EXTRA_STARTUP, false));
        }
        return stack;
    }

    private TaskStackBuilder buildFolderListBackStack(Context context, Account account,String openFolder) {
        TaskStackBuilder stack = buildAccountsBackStack(context);
        LocalSearch search = new LocalSearch(openFolder);
        search.addAllowedFolder(openFolder);
        search.addAccountUuid(account.getUuid());
        stack.addNextIntent(MailNotifyPendingActivity.actionMailNotify(context, search,false));
//        stack.addNextIntent(Main4TabActivity.actionHandleAccountIntent(context, account,false));
//		 stack.addNextIntent(Main4TabActivity.intentDisplaySearch(context, search, true, false, false));
        return stack;
    }

    private TaskStackBuilder buildUnreadBackStack(Context context, final Account account) {
        TaskStackBuilder stack = buildAccountsBackStack(context);
        LocalSearch search = Accounts.createUnreadSearch(context, account);
        stack.addNextIntent(Main4TabActivity.intentDisplaySearch(context, search, true, false, false));
        return stack;
    }

    private TaskStackBuilder buildMessageListBackStack(Context context, Account account, String folder) {
        TaskStackBuilder stack = skipFolderListInBackStack(context, account, folder)
                ? buildAccountsBackStack(context)
                : buildFolderListBackStack(context, account,folder);

        if (folder != null) {
            LocalSearch search = new LocalSearch(folder);
            search.addAllowedFolder(folder);
            search.addAccountUuid(account.getUuid());
            stack.addNextIntent(MailNotifyPendingActivity.actionMailNotify(context, search,false));
        }
        return stack;
    }

    private TaskStackBuilder buildMessageViewBackStack(Context context, MessageReference message) {
        TaskStackBuilder stack = TaskStackBuilder.create(context);
        stack.addNextIntent(MailDetialActivity.actionDisplayMessageIntentByNotify(context, message));
        return stack;
    }

    private boolean skipFolderListInBackStack(Context context, Account account, String folder) {
        return folder != null && folder.equals(account.getAutoExpandFolderName());
    }

    private boolean skipAccountsInBackStack(Context context) {
        return Preferences.getPreferences(context).getAccounts().size() == 1;
    }

    /**
     * Configure the notification sound and LED
     *
     * @param builder
     *         {@link NotificationCompat.Builder} instance used to configure the notification.
     *         Never {@code null}.
     * @param ringtone
     *          String name of ringtone. {@code null}, if no ringtone should be played.
     * @param vibrationPattern
     *         {@code long[]} vibration pattern. {@code null}, if no vibration should be played.
     * @param ledColor
     *         Color to flash LED. {@code null}, if no LED flash should happen.
     * @param ledSpeed
     *         Either {@link MailChat#NOTIFICATION_LED_BLINK_SLOW} or
     *         {@link MailChat#NOTIFICATION_LED_BLINK_FAST}.
     * @param ringAndVibrate
     *          {@code true}, if ringtone/vibration are allowed. {@code false}, otherwise.
     */
    private void configureNotification(NotificationCompat.Builder builder, String ringtone,
            long[] vibrationPattern, Integer ledColor, int ledSpeed, boolean ringAndVibrate) {

        // if it's quiet time, then we shouldn't be ringing, buzzing or flashing
        if (MailChat.isQuietTime()) {
            return;
        }

        if (ringAndVibrate) {
            if (ringtone != null && !TextUtils.isEmpty(ringtone)) {
                builder.setSound(Uri.parse(ringtone));
            }

            if (vibrationPattern != null) {
                builder.setVibrate(vibrationPattern);
            }
        }

        if (ledColor != null) {
            int ledOnMS;
            int ledOffMS;
            if (ledSpeed == MailChat.NOTIFICATION_LED_BLINK_SLOW) {
                ledOnMS = MailChat.NOTIFICATION_LED_ON_TIME;
                ledOffMS = MailChat.NOTIFICATION_LED_OFF_TIME;
            } else {
                ledOnMS = MailChat.NOTIFICATION_LED_FAST_ON_TIME;
                ledOffMS = MailChat.NOTIFICATION_LED_FAST_OFF_TIME;
            }

            builder.setLights(ledColor, ledOnMS, ledOffMS);
        }
    }
    /**
	 * 全局通知设置
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-2-9
	 */
 	private void globalConfigureNotification(NotificationCompat.Builder builder,boolean isSound,boolean isVibrate){
 	    if (MailChat.isQuietTime()) {
             return;
        }
		int ledOnMS = MailChat.NOTIFICATION_LED_ON_TIME;
        int ledOffMS = MailChat.NOTIFICATION_LED_OFF_TIME;
		builder.setLights(Color.BLUE, ledOnMS, ledOffMS);
		boolean isNotify=true;
		if (System.currentTimeMillis()-MailChat.getFirstTime()<30000) {
			isNotify = false;
		} else {
			MailChat.setFirstTime(System.currentTimeMillis());
			Editor editor = Preferences.getPreferences(mApplication).getPreferences().edit();
			MailChat.save(editor);
			editor.commit();
			isNotify = true;
		}
		 if (isNotify) {
            if (isSound) {
				Uri uri = null;
				int select = MailChat.getSelectNotifyRingtone();
				if (select == 0) {
					uri = RingtoneManager.getActualDefaultRingtoneUri(
							mApplication, RingtoneManager.TYPE_NOTIFICATION);
				} else {
					RingtoneManager rm = new RingtoneManager(mApplication);
					rm.setType(RingtoneManager.TYPE_NOTIFICATION);
					rm.getCursor();
					uri = rm.getRingtoneUri(select - 1);
				}
				builder.setSound(uri);
            }
            if (isVibrate) {
                builder.setVibrate(new long[]{300,300,300,300});
            }
        }
	}
 	
    /** Cancel a notification of new email messages */
    public void notifyAccountCancel(Context context, Account account) {
        NotificationManager notifMgr =
            (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifMgr.cancel(account.getAccountNumber());
        notifMgr.cancel(-1000 - account.getAccountNumber());
        notificationData.remove(account.getAccountNumber());
    }

	/**
	 * Save a draft message.
	 * 
	 * @param account
	 *            Account we are saving for.
	 * @param message
	 *            Message to save.
	 * @return Message representing the entry in the local store.
	 */
	public Message saveDraft(final Account account, final Message message,
			long existingDraftId, final boolean isOutBox)
			throws MessagingException {

		Message localMessage = null;
		LocalFolder localFolder = null;
		// try {
		LocalStore localStore = account.getLocalStore();
		if (!isOutBox) {
			localFolder = localStore.getFolder(account.getDraftsFolderName());
		} else {
			localFolder = localStore.getFolder(account.getOutboxFolderName());
		}
		localFolder.open(Folder.OPEN_MODE_RW);
		if (existingDraftId != INVALID_MESSAGE_ID) {
			/*
			 * String uid = localFolder.getMessageUidById(existingDraftId);
			 * message.setUid(uid);
			 */
			// Keep uid, X-MailChat-Message-ID and Message-ID Header for sync
			// Modified by LL
			// BEGIN
			String uid = localFolder.getMessageUidById(existingDraftId);
			Message msg = localFolder.getMessage(uid);

			message.setUid(uid);

			String[] headers = msg.getHeader(MailChat.MESSAGE_ID_HEADER);
			if ((headers != null) && (headers.length > 0)) {
				message.setHeader(MailChat.MESSAGE_ID_HEADER, headers[0]);
			}

			headers = msg.getHeader(MailChat.MAILCHAT_MESSAGE_ID_HEADER);
			if ((headers != null) && (headers.length > 0)) {
				message.setHeader(MailChat.MAILCHAT_MESSAGE_ID_HEADER,
						headers[0]);
			}
			// END
		}
		// Save the message to the store.
		localFolder.appendMessages(new Message[] { message });
		// Fetch the message back from the store. This is the Message that's
		// returned to the caller.
		localMessage = localFolder.getMessage(message.getUid());
		localMessage.setFlag(Flag.X_DOWNLOADED_FULL, true);

		// DO NOT append drafts message to remote drafts folder.
		// Modified by LL
		// BEGIN
		/*
		 * PendingCommand command = new PendingCommand(); command.command =
		 * PENDING_COMMAND_APPEND; command.arguments = new String[] {
		 * localFolder.getName(), localMessage.getUid() };
		 * queuePendingCommand(account, command);
		 * processPendingCommands(account);
		 */
		// END

		// } catch (MessagingException e) {
		// Log.e(MailChat.LOG_TAG, "Unable to save message as draft.", e);
		// addErrorMessage(account, null, e);
		// }
		return localMessage;
	}

    public long getId(Message message) {
        long id;
        if (message instanceof LocalMessage) {
            id = ((LocalMessage) message).getId();
        } else {
            Log.w(MailChat.LOG_TAG, "MessagingController.getId() called without a LocalMessage");
            id = INVALID_MESSAGE_ID;
        }

        return id;
    }

    public boolean modeMismatch(Account.FolderMode aMode, Folder.FolderClass fMode) {
        if (aMode == Account.FolderMode.NONE
                || (aMode == Account.FolderMode.FIRST_CLASS &&
                    fMode != Folder.FolderClass.FIRST_CLASS)
                || (aMode == Account.FolderMode.FIRST_AND_SECOND_CLASS &&
                    fMode != Folder.FolderClass.FIRST_CLASS &&
                    fMode != Folder.FolderClass.SECOND_CLASS)
                || (aMode == Account.FolderMode.NOT_SECOND_CLASS &&
                    fMode == Folder.FolderClass.SECOND_CLASS)) {
            return true;
        } else {
            return false;
        }
    }

    static AtomicInteger sequencing = new AtomicInteger(0);
    static class Command implements Comparable<Command> {
        public Runnable runnable;

        public MessagingListener listener;

        public String description;

        boolean isForeground;

        int sequence = sequencing.getAndIncrement();

        @Override
        public int compareTo(Command other) {
            if (other.isForeground && !isForeground) {
                return 1;
            } else if (!other.isForeground && isForeground) {
                return -1;
            } else {
                return (sequence - other.sequence);
            }
        }
    }

    public MessagingListener getCheckMailListener() {
        return checkMailListener;
    }

    public void setCheckMailListener(MessagingListener checkMailListener) {
        if (this.checkMailListener != null) {
            removeListener(this.checkMailListener);
        }
        this.checkMailListener = checkMailListener;
        if (this.checkMailListener != null) {
            addListener(this.checkMailListener);
        }
    }

    public Collection<Pusher> getPushers() {
        return pushers.values();
    }

    public boolean setupPushing(final Account account) {
        try {
            Pusher previousPusher = pushers.remove(account);
            if (previousPusher != null) {
                previousPusher.stop();
            }
            Preferences prefs = Preferences.getPreferences(mApplication);

            Account.FolderMode aDisplayMode = account.getFolderDisplayMode();
            Account.FolderMode aPushMode = account.getFolderPushMode();

            List<String> names = new ArrayList<String>();

            Store localStore = account.getLocalStore();
            for (final Folder folder : localStore.getPersonalNamespaces(false)) {
                if (folder.getName().equals(account.getErrorFolderName())
                        || folder.getName().equals(account.getOutboxFolderName())) {
                    /*
                    if (MailChat.DEBUG)
                        Log.v(MailChat.LOG_TAG, "Not pushing folder " + folder.getName() +
                              " which should never be pushed");
                    */

                    continue;
                }
                folder.open(Folder.OPEN_MODE_RW);
                folder.refresh(prefs);

//                Folder.FolderClass fDisplayClass = folder.getDisplayClass();
//                Folder.FolderClass fPushClass = folder.getPushClass();
//
//                if (modeMismatch(aDisplayMode, fDisplayClass)) {
//                    // Never push a folder that isn't displayed
//                    /*
//                    if (MailChat.DEBUG)
//                        Log.v(MailChat.LOG_TAG, "Not pushing folder " + folder.getName() +
//                              " which is in display class " + fDisplayClass + " while account is in display mode " + aDisplayMode);
//                    */
//
//                    continue;
//                }
//
//                if (modeMismatch(aPushMode, fPushClass)) {
//                    // Do not push folders in the wrong class
//                    /*
//                    if (MailChat.DEBUG)
//                        Log.v(MailChat.LOG_TAG, "Not pushing folder " + folder.getName() +
//                              " which is in push mode " + fPushClass + " while account is in push mode " + aPushMode);
//                    */
//
//                    continue;
//                }
//                if (MailChat.DEBUG)
//                    Log.i(MailChat.LOG_TAG, "Starting pusher for " + account.getDescription() + ":" + folder.getName());
                //需要定时刷新邮件的邮件夹
				if (folder.isAllowPush()) {
					names.add(folder.getName());
				}
            }

            if (!names.isEmpty()) {
                PushReceiver receiver = new MessagingControllerPushReceiver(mApplication, account, this);
                int maxPushFolders = account.getMaxPushFolders();

                if (names.size() > maxPushFolders) {
                    if (MailChat.DEBUG)
                        Log.i(MailChat.LOG_TAG, "Count of folders to push for account " + account.getDescription() + " is " + names.size()
                              + ", greater than limit of " + maxPushFolders + ", truncating");

                    names = names.subList(0, maxPushFolders);
                }

                try {
                    // 三五 3.0/2.0 邮箱和隐藏账号不需要Pusher
                    if (account.getVersion_35Mail() == 1
                            || account.getVersion_35Mail() == 2
                            || account.isHideAccount()) {
                        return false;
                    }
                    
                    Store store = account.getRemoteStore();
                    if (!store.isPushCapable()) {
                        if (MailChat.DEBUG)
                            Log.i(MailChat.LOG_TAG, "Account " + account.getDescription() + " is not push capable, skipping");

                        return false;
                    }
                    Pusher pusher = store.getPusher(receiver);
                    if (pusher != null) {
                        Pusher oldPusher  = pushers.putIfAbsent(account, pusher);
                        if (oldPusher == null) {
                            pusher.start(names);
                        }
                    }
                } catch (Exception e) {
                    Log.e(MailChat.LOG_TAG, "Could not get remote store", e);
                    return false;
                }

                return true;
            } else {
                if (MailChat.DEBUG)
                    Log.i(MailChat.LOG_TAG, "No folders are configured for pushing in account " + account.getDescription());
                return false;
            }

        } catch (Exception e) {
            Log.e(MailChat.LOG_TAG, "Got exception while setting up pushing", e);
        }
        return false;
    }

    public void stopAllPushing() {
        if (MailChat.DEBUG)
            Log.i(MailChat.LOG_TAG, "Stopping all pushers");

        Iterator<Pusher> iter = pushers.values().iterator();
        while (iter.hasNext()) {
            Pusher pusher = iter.next();
            iter.remove();
            pusher.stop();
        }
    }

    public void messagesArrived(final Account account, final Folder remoteFolder, final List<Message> messages, final boolean flagSyncOnly) {
        if (MailChat.DEBUG)
            Log.i(MailChat.LOG_TAG, "Got new pushed email messages for account " + account.getDescription()
                  + ", folder " + remoteFolder.getName());

        final CountDownLatch latch = new CountDownLatch(1);
        putBackground("Push messageArrived of account " + account.getDescription()
        + ", folder " + remoteFolder.getName(), null, new Runnable() {
            @Override
            public void run() {
                LocalFolder localFolder = null;
                try {
                    LocalStore localStore = account.getLocalStore();
                    localFolder = localStore.getFolder(remoteFolder.getName());
                    localFolder.open(Folder.OPEN_MODE_RW);

                    account.setRingNotified(false);
                    
                    //int newCount = downloadMessages(account, remoteFolder, localFolder, messages, flagSyncOnly);
                    // 统一使用新下载邮件方法
                    // Modified by LL
                    // BEGIN
                    Message[] localMessages = localFolder.getMessages(null);
                    
                    HashMap<String, Message> localMessageIDMap = new HashMap<String, Message>();
                    HashMap<String, Message> localMailChatMessageIDMap = new HashMap<String, Message>();
                    
                    prepareSyncMaps(account,
                    		localFolder,
                    		localMessages,
                    		localMessageIDMap,
                    		localMailChatMessageIDMap);
                    
                    int newCount = downloadMessages(account,
                    		remoteFolder,
                    		localFolder,
                    		messages,
                    		flagSyncOnly,
                    		localMessageIDMap,
                    		localMailChatMessageIDMap);
                    // END

                    int unreadMessageCount = localFolder.getUnreadMessageCount();

                    localFolder.setLastPush(System.currentTimeMillis());
                    localFolder.setStatus(null);

                    if (MailChat.DEBUG)
                        Log.i(MailChat.LOG_TAG, "messagesArrived newCount = " + newCount + ", unread count = " + unreadMessageCount);

                    if (unreadMessageCount == 0) {
                        notifyAccountCancel(mApplication, account);
                    }

                    for (MessagingListener l : getListeners()) {
                        l.folderStatusChanged(account, remoteFolder.getName(), unreadMessageCount);
                    }

                } catch (Exception e) {
                    String rootMessage = MailChat.getRootCauseMessage(e);
                    String errorMessage = "Push failed: " + rootMessage;
                    try {
                        // Oddly enough, using a local variable gets rid of a
                        // potential null pointer access warning with Eclipse.
                        LocalFolder folder = localFolder;
                        folder.setStatus(errorMessage);
                    } catch (Exception se) {
                        Log.e(MailChat.LOG_TAG, "Unable to set failed status on localFolder", se);
                    }
                    for (MessagingListener l : getListeners()) {
                        l.synchronizeMailboxFailed(account, remoteFolder.getName(), errorMessage);
                    }
                    addErrorMessage(account, null, e);
                } finally {
                    closeFolder(localFolder);
                    latch.countDown();
                }

            }
        });
        try {
            latch.await();
        } catch (Exception e) {
            Log.e(MailChat.LOG_TAG, "Interrupted while awaiting latch release", e);
        }
        if (MailChat.DEBUG)
            Log.i(MailChat.LOG_TAG, "MessagingController.messagesArrivedLatch released");
    }

    public void systemStatusChanged() {
        for (MessagingListener l : getListeners()) {
            l.systemStatusChanged();
        }
    }

    enum MemorizingState { STARTED, FINISHED, FAILED }

    static class Memory {
        Account account;
        String folderName;
        MemorizingState syncingState = null;
        MemorizingState sendingState = null;
        MemorizingState pushingState = null;
        MemorizingState processingState = null;
        String failureMessage = null;

        int syncingTotalMessagesInMailbox;
        int syncingNumNewMessages;

        int folderCompleted = 0;
        int folderTotal = 0;
        String processingCommandTitle = null;

        Memory(Account nAccount, String nFolderName) {
            account = nAccount;
            folderName = nFolderName;
        }

        String getKey() {
            return getMemoryKey(account, folderName);
        }


    }
    static String getMemoryKey(Account taccount, String tfolderName) {
        return taccount.getDescription() + ":" + tfolderName;
    }
    static class MemorizingListener extends MessagingListener {
        HashMap<String, Memory> memories = new HashMap<String, Memory>(31);

        Memory getMemory(Account account, String folderName) {
            Memory memory = memories.get(getMemoryKey(account, folderName));
            if (memory == null) {
                memory = new Memory(account, folderName);
                memories.put(memory.getKey(), memory);
            }
            return memory;
        }

        @Override
        public synchronized void synchronizeMailboxStarted(Account account, String folder) {
            Memory memory = getMemory(account, folder);
            memory.syncingState = MemorizingState.STARTED;
            memory.folderCompleted = 0;
            memory.folderTotal = 0;
        }

        @Override
        public synchronized void synchronizeMailboxFinished(Account account, String folder,
                int totalMessagesInMailbox, int numNewMessages) {
            Memory memory = getMemory(account, folder);
            memory.syncingState = MemorizingState.FINISHED;
            memory.syncingTotalMessagesInMailbox = totalMessagesInMailbox;
            memory.syncingNumNewMessages = numNewMessages;
        }

        @Override
        public synchronized void synchronizeMailboxFailed(Account account, String folder,
                String message) {

            Memory memory = getMemory(account, folder);
            memory.syncingState = MemorizingState.FAILED;
            memory.failureMessage = message;
        }
        synchronized void refreshOther(MessagingListener other) {
            if (other != null) {

                Memory syncStarted = null;
                Memory sendStarted = null;
                Memory processingStarted = null;

                for (Memory memory : memories.values()) {

                    if (memory.syncingState != null) {
                        switch (memory.syncingState) {
                        case STARTED:
                            syncStarted = memory;
                            break;
                        case FINISHED:
                            other.synchronizeMailboxFinished(memory.account, memory.folderName,
                                                             memory.syncingTotalMessagesInMailbox, memory.syncingNumNewMessages);
                            break;
                        case FAILED:
                            other.synchronizeMailboxFailed(memory.account, memory.folderName,
                                                           memory.failureMessage);
                            break;
                        }
                    }

                    if (memory.sendingState != null) {
                        switch (memory.sendingState) {
                        case STARTED:
                            sendStarted = memory;
                            break;
                        case FINISHED:
                            other.sendPendingMessagesCompleted(memory.account);
                            break;
                        case FAILED:
                            other.sendPendingMessagesFailed(memory.account);
                            break;
                        }
                    }
                    if (memory.pushingState != null) {
                        switch (memory.pushingState) {
                        case STARTED:
                            other.setPushActive(memory.account, memory.folderName, true);
                            break;
                        case FINISHED:
                            other.setPushActive(memory.account, memory.folderName, false);
                            break;
                        case FAILED:
                            break;
                        }
                    }
                    if (memory.processingState != null) {
                        switch (memory.processingState) {
                        case STARTED:
                            processingStarted = memory;
                            break;
                        case FINISHED:
                        case FAILED:
                            other.pendingCommandsFinished(memory.account);
                            break;
                        }
                    }
                }
                Memory somethingStarted = null;
                if (syncStarted != null) {
                    other.synchronizeMailboxStarted(syncStarted.account, syncStarted.folderName);
                    somethingStarted = syncStarted;
                }
                if (sendStarted != null) {
                    other.sendPendingMessagesStarted(sendStarted.account);
                    somethingStarted = sendStarted;
                }
                if (processingStarted != null) {
                    other.pendingCommandsProcessing(processingStarted.account);
                    if (processingStarted.processingCommandTitle != null) {
                        other.pendingCommandStarted(processingStarted.account, processingStarted.processingCommandTitle);

                    } else {
                        other.pendingCommandCompleted(processingStarted.account, processingStarted.processingCommandTitle);
                    }
                    somethingStarted = processingStarted;
                }
                if (somethingStarted != null && somethingStarted.folderTotal > 0) {
                    other.synchronizeMailboxProgress(somethingStarted.account, somethingStarted.folderName, somethingStarted.folderCompleted, somethingStarted.folderTotal);
                }

            }
        }
        @Override
        public synchronized void setPushActive(Account account, String folderName, boolean active) {
            Memory memory = getMemory(account, folderName);
            memory.pushingState = (active ? MemorizingState.STARTED : MemorizingState.FINISHED);
        }

        @Override
        public synchronized void sendPendingMessagesStarted(Account account) {
            Memory memory = getMemory(account, null);
            memory.sendingState = MemorizingState.STARTED;
            memory.folderCompleted = 0;
            memory.folderTotal = 0;
        }

        @Override
        public synchronized void sendPendingMessagesCompleted(Account account) {
            Memory memory = getMemory(account, null);
            memory.sendingState = MemorizingState.FINISHED;
        }

        @Override
        public synchronized void sendPendingMessagesFailed(Account account) {
            Memory memory = getMemory(account, null);
            memory.sendingState = MemorizingState.FAILED;
        }


        @Override
        public synchronized void synchronizeMailboxProgress(Account account, String folderName, int completed, int total) {
            Memory memory = getMemory(account, folderName);
            memory.folderCompleted = completed;
            memory.folderTotal = total;
        }


        @Override
        public synchronized void pendingCommandsProcessing(Account account) {
            Memory memory = getMemory(account, null);
            memory.processingState = MemorizingState.STARTED;
            memory.folderCompleted = 0;
            memory.folderTotal = 0;
        }
        @Override
        public synchronized void pendingCommandsFinished(Account account) {
            Memory memory = getMemory(account, null);
            memory.processingState = MemorizingState.FINISHED;
        }
        @Override
        public synchronized void pendingCommandStarted(Account account, String commandTitle) {
            Memory memory = getMemory(account, null);
            memory.processingCommandTitle = commandTitle;
        }

        @Override
        public synchronized void pendingCommandCompleted(Account account, String commandTitle) {
            Memory memory = getMemory(account, null);
            memory.processingCommandTitle = null;
        }

    }

    private void actOnMessages(List<Message> messages, MessageActor actor) {
        Map<Account, Map<Folder, List<Message>>> accountMap = new HashMap<Account, Map<Folder, List<Message>>>();

        for (Message message : messages) {
            if ( message == null) {
               continue;
            }
            Folder folder = message.getFolder();
            Account account = folder.getAccount();

            Map<Folder, List<Message>> folderMap = accountMap.get(account);
            if (folderMap == null) {
                folderMap = new HashMap<Folder, List<Message>>();
                accountMap.put(account, folderMap);
            }
            List<Message> messageList = folderMap.get(folder);
            if (messageList == null) {
                messageList = new LinkedList<Message>();
                folderMap.put(folder, messageList);
            }

            messageList.add(message);
        }
        for (Map.Entry<Account, Map<Folder, List<Message>>> entry : accountMap.entrySet()) {
            Account account = entry.getKey();

            //account.refresh(Preferences.getPreferences(MailChat.app));
            Map<Folder, List<Message>> folderMap = entry.getValue();
            for (Map.Entry<Folder, List<Message>> folderEntry : folderMap.entrySet()) {
                Folder folder = folderEntry.getKey();
                List<Message> messageList = folderEntry.getValue();
                actor.act(account, folder, messageList);
            }
        }
    }

    interface MessageActor {
        public void act(final Account account, final Folder folder, final List<Message> messages);
    }

    /**
	 * 加载SSL安全加密
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
     * @throws KeyManagementException 
     * @throws KeyStoreException 
     * @throws NoSuchAlgorithmException 
     * @throws IOException 
     * @throws NotFoundException 
     * @throws CertificateException 
	 * @date:2014-10-29
	 */
    public MqttConnectOptions readSSL(boolean isCleanSession) throws KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, NotFoundException, IOException {
		// TODO Auto-generated method stub
    	MqttConnectOptions conOpt = new MqttConnectOptions();
		conOpt.setCleanSession(isCleanSession);
		conOpt.setKeepAliveInterval(300);
		SSLContext context = null;
		KeyStore ts = KeyStore.getInstance("bks");
		ts.load(mApplication.getResources().openRawResource(R.raw.mailchat),
				"com35xm2014ptc".toCharArray());
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
		tmf.init(ts);
		TrustManager[] tm = tmf.getTrustManagers();
		context = SSLContext.getInstance("TLSv1");
		context.init(null, tm, null);
		SocketFactory factory = context.getSocketFactory();
		conOpt.setSocketFactory(factory);
		return conOpt;
	}
	/**
	 * 闹钟定时任务,使用系统闹钟，是防止CPU休眠
	 *
	 * @Description:
	 * @param action 所属广播动作
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-28
	 */
	public void alarmTask(String action, long second,Class<? extends Service> serviceClass, int requestCode) {
		AlarmManager alarmMgr = (AlarmManager) mApplication.getSystemService(mApplication.getApplicationContext().ALARM_SERVICE);
		Intent intent = new Intent(mApplication, serviceClass);
		intent.setAction(action);
		PendingIntent pendIntent = PendingIntent.getService(mApplication,
				requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		long triggerAtTime=  SystemClock.elapsedRealtime() + 1000 * second;
//		long triggerAtTime;
//		if (time < 0) {
//			triggerAtTime = SystemClock.elapsedRealtime() + 1000 * 3;
//		} else if (time == 0) {
//			triggerAtTime = SystemClock.elapsedRealtime() + 1000 * 10;
//		} else {
//			triggerAtTime = SystemClock.elapsedRealtime() + 3 * 1000 * 60;
//		}
		alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime,pendIntent);
	}
	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		Log.i(Connection.TAG,  "connectionLost");
		if (arg0 != null&&NetUtil.isActive()&&connection.getClient().getClientHandle()!=null&&!connection.getClient().isConnected()) {
			// format string to use a notification text
			alarmTask(RE_CONNECT,10,MqttService.class,GlobalConstants.MQTT_CONNECT_ALARM_REQUESTCODE);	
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		Log.i(Connection.TAG,  "deliveryComplete");
	}

	@Override
	public void messageArrived(String topic, MqttMessage message)
			throws Exception {
		penddingPushMessage.add(new MQTTPushMessage(topic, message));
	}
	private void saveAndParseMQTTMessage(String topic,MqttMessage message) throws Exception{
		Preferences prefs = Preferences.getPreferences(mApplication);
		List<Account> accounts = prefs.getAccounts();
		Account account = null;
		byte[]  timeMessageByte = message.getPayload();
		byte[] timeByte=Arrays.copyOfRange(timeMessageByte, 0, 8);
		byte[] messageByte =Arrays.copyOfRange(timeMessageByte, 8, timeMessageByte.length);
		long timeStamp=0;
		for (int i = 0; i < timeByte.length; i++){
             int temp = ((int)timeByte[i]) & 0xff;
             temp <<= i * 8;
             timeStamp = temp + timeStamp;
        }
		timeStamp=timeStamp*1000;
		JSONObject json = new JSONObject(new String(messageByte));
		if (topic.endsWith("/s")) {//系统消息
			String toEmail = topic.substring(0, topic.indexOf("/"));
			for (Account localdaccount : accounts) {
				if (localdaccount.getEmail().equals(toEmail)) {
					account = localdaccount;
				}
			}
			if (account != null) {
				if(!json.isNull("f")){
					JSONObject messageJSONObject  = json.getJSONObject("m");
					int MQTTmessageType =messageJSONObject.getInt("t");
					if (MQTTmessageType==MQTTMessageType.SYSTEM_JOINGROUP_MESSAGE) {//优化为switch
						/**
						 * 加入群系统消息
						 */
						String subscribeGroupId = messageJSONObject.getJSONArray("c").getString(0);
						String fromEmai = json.getString("f");
						if (!account.getEmail().equals(fromEmai)) {
							PendingMQTTConmmand pendingMQTTConmmand =new PendingMQTTConmmand(-1,ActionListener.Action.SUBSCRIBE,MQTTCommand.JOIN_GROUP,subscribeGroupId,fromEmai);
							if(connection.getClient().getClientHandle()!=null){
								connection.getClient().subscribe(subscribeGroupId,MQTT_QOS,null,
										new ActionListener(ActionListener.Action.SUBSCRIBE,
												MessagingController.this, account, MQTTCommand.JOIN_GROUP,
												pendingMQTTConmmand));
							//Pending中最后一个参数注意.
							}else{
								LocalStore localStore =account.getLocalStore();
								localStore.saveMQTTPending(pendingMQTTConmmand);
								localStore.saveHTTPSPending(new PendingHTTPSCommand(-1,cn.mailchat.chatting.protocol.Command.JOIN_GROUP,
										fromEmai+","+pendingMQTTConmmand.getTopic()));
							}
						}
					} else if (MQTTmessageType==MQTTMessageType.SYSTEM_DELETEGROUP_MESSAGE) {
						/**
						 * 群主解散群系统消息
						 */
						String unSubscribeGroupId =  messageJSONObject.getJSONArray("c").getString(0);
						String fromEmai = json.getString("f");
						if (!account.getEmail().equals(fromEmai)) {
							int count =0;
							for(Account localdaccount:accounts){
								//查看所有账户中,加入到该群的账户数,如果等于1,直接将该连接中订阅的群频道退订
								if(localdaccount.getLocalStore().getCGroup(unSubscribeGroupId)!=null){
									count++;
								}
							}
							if(count>1){
								leaveGroupByMember(account, unSubscribeGroupId,true);
							}else if(count==1){
								leaveAndUnsubscribeGroupByMember(account, unSubscribeGroupId,true);
							}else{
							}
						}
					} else if (MQTTmessageType==MQTTMessageType.SYSTEM_LEAVEGROUP_MESSAGE) {
						/**
						 * 群成员退出群系统消息
						 */
						String unSubscribeGroupId =  messageJSONObject.getJSONArray("c").getString(0);
						String fromEmai = json.getString("f");
						// 生成消息
						LocalStore localStore = account.getLocalStore();
						// 创建群成员
						String adminNickName = localStore
								.getNickNameByEmail(fromEmai);
						CGroupMember member = new CGroupMember();
						member.setUid(EncryptUtil.getMd5(fromEmai));
						member.setEmail(fromEmai);
						member.setNickName(adminNickName);
						member.setAdmin(false);
						// 创建消息
						CMessage cMessage = new CMessage(Type.NOTIFICATION);
						cMessage.setUid(UUID.randomUUID().toString());
						cMessage.setGroupUid(unSubscribeGroupId);
						cMessage.setMember(member);
						cMessage.setSendTime(System.currentTimeMillis());
						cMessage.setContent(String.format(mApplication
								.getString(R.string.change_group_leave_message),
								adminNickName));
						// 由于不确定当前账户是否保存该群成员
						localStore.saveOrUpdateGroupMember(unSubscribeGroupId,
								member);//主要是为了保存群成员。 这个方法中会创建关系，以前的需求，后面改掉
						localStore.deleteCGroupCmember(unSubscribeGroupId,new String[]{ member.getUid()});//删除关系
						localStore.saveOrUpdateCMessageAndCAttach(
								unSubscribeGroupId, cMessage);
						localStore.updateCgroupLastCmessage(unSubscribeGroupId,
								cMessage);
						// 其他设备退群，直接删除
						if (fromEmai.equals(account.getEmail())) {
							localStore.deleteCGroup(unSubscribeGroupId);
							int count = 0;
							for (Account localdaccount : accounts) {
								// 查看所有账户中,加入到该群的账户数,如果等于1,直接将该连接中订阅的群频道退订
								if (localdaccount.getLocalStore().getCGroup(
										unSubscribeGroupId) != null) {
									count++;
								}
							}
							if (count == 1&& connection.getClient().getClientHandle() != null) {
								PendingMQTTConmmand pendingMQTTConmmand =new PendingMQTTConmmand(-1,ActionListener.Action.UNSUBSCRIBE,MQTTCommand.MEMBER_LEAVE_GROUP,unSubscribeGroupId,null);
								connection.getClient().unsubscribe(unSubscribeGroupId,null,
										new ActionListener(ActionListener.Action.UNSUBSCRIBE,MessagingController.this,account,MQTTCommand.MEMBER_LEAVE_GROUP,pendingMQTTConmmand));
							}
							return;
						}
						for (MessagingListener l : getListeners(null)) {
							l.cMessageArrived(account, cMessage);
						}
					}else if(MQTTmessageType==MQTTMessageType.SYSTEM_GROUPRENAME_MESSAGE){
						/**
						 * 群名称修改系统消息
						 */
						String subscribeGroupId =  messageJSONObject.getJSONArray("c").getString(0);
						String reName =messageJSONObject.getJSONArray("c").getString(1);
						String fromEmai = json.getString("f");
						if(!account.getEmail().equals(fromEmai)){
							updateLocalGroupName(account, subscribeGroupId, reName);
						}
					}else if(MQTTmessageType==MQTTMessageType.SYSTEM_KICKED_OUT_GROUPR_MESSAGE){
						/**
						 * 你被踢系统消息
						 */
						String unSubscribeGroupId =  messageJSONObject.getJSONArray("c").getString(0);
						int count =0;
						for(Account localdaccount:accounts){
							//查看所有账户中,加入到该群的账户数,如果等于1,直接将该连接中订阅的群频道退订
							if(localdaccount.getLocalStore().getCGroup(unSubscribeGroupId)!=null){
								count++;
							}
						}
						if(count>1){
							leaveGroupByMember(account, unSubscribeGroupId,false);
						}else if(count==1){
							leaveAndUnsubscribeGroupByMember(account, unSubscribeGroupId,false);
						}else{
						}
					}else if(MQTTmessageType==MQTTMessageType.SYSTEM_KICKED_OUT_MEMBER_MESSAGE){
						/**
						 * 收到群成员被踢系统消息
						 */
						JSONArray array =messageJSONObject.getJSONArray("c");
						account.getLocalStore().deleteCGroupCmember(array.getString(0), new String[]{EncryptUtil.getMd5(array.getString(1))});
					}
				}else{//服务器推送类型
					int MQTTmessageType =json.getInt("p");
					if(MQTTmessageType==MQTTPushType.SERVER_MAIL_PUSH){
						Account defaultAccoun = prefs.getDefaultAccount();
						JSONObject  pushMessage= json.getJSONObject("m");
						String  senderEmail= pushMessage.optString("f");
						//请求更新发件人头像等个人信息
						ContactAttribute contact=addTempContact(senderEmail,null);
						tempContactList.add(contact);
						reflashContactList(account, tempContactList);
						// Modified by LL
						// BEGIN
						String directory = insertPushMessage(account, pushMessage);
						LocalFolder folder = null;
						if (directory != null) {
						    if (!".".equals(directory)) {
    							LocalStore store = account.getLocalStore();
    							folder = store.getFolder(directory);
    							LocalMessage localMessage = folder
    									.getMessage(pushMessage.getString("i"));
    							long mailReceivedTime = pushMessage.getLong("t");
    							if ((mailReceivedTime - System.currentTimeMillis()) < 30 * 1000 * 60) {
    								NotificationCenter.getInstance().onNew35Mial(account.getUuid(), folder, localMessage);
    							}
						    }
						}else{
							long mailReceivedTime = pushMessage.getLong("t");
							if ((mailReceivedTime - System.currentTimeMillis()) < 30 * 1000 * 60) {
								NotificationCenter.getInstance().onNew35MialFor2(account.getUuid(), new ContentOfPushMessage("1", pushMessage.getString("u"), pushMessage.getString("f"), pushMessage.getString("s")));
							}
						}
						if(folder != null){
							for (MessagingListener l : getListeners(null)) {
								l.receiveMessage(account,folder);
							}
						}
						// END
					}
				}
			}else{
				if(connection.getClient().getClientHandle()!=null){
					connection.getClient().unsubscribe(topic,null,
							new ActionListener(ActionListener.Action.UNSUBSCRIBE,
									MessagingController.this, null,null,null));
				}
			}
		} else if (topic.endsWith("/1") || topic.endsWith(GlobalConstants.HIDE_ACCOUNT_SUFFIX)){//单聊消息//匿名消息(邮洽小助手)IOS端定义
			//topic是自己的email地址
			String receiverEmail = null;
			if(topic.endsWith("/1")){
				receiverEmail = topic.substring(0, topic.indexOf("/"));
			}else{
				receiverEmail=topic;
			}
			for (Account localdaccount : accounts) {
				if (localdaccount.getEmail().equals(receiverEmail)) {
					account = localdaccount;
				}
			}
			//小助手同步逻辑判断
			//Start
			boolean isSendMyselfForSyn =false;
			if(!json.isNull("t")){
				receiverEmail = json.getString("t");
				isSendMyselfForSyn=true;
			}
			//end
			PushDMeesage(account,topic,receiverEmail,prefs, timeStamp, json,isSendMyselfForSyn);
		} else if(topic.endsWith("/a")){//新推送频道
			String accountEmail = topic.substring(0, topic.indexOf("/"));
			for (Account localdaccount : accounts) {
				if (localdaccount.getEmail().equals(accountEmail)) {
					account = localdaccount;
				}
			}
			if (account != null) {
				JSONObject  comtentJson = json.getJSONObject("m");
				String appType = comtentJson.getString("a");//应用类型
				LocalStore localStore = account.getLocalStore();
				switch (appType) {
				case "oa":
					DChatMessage dChatMessage = ParseJson.parseOA2Dmessage(comtentJson,accountEmail);
					dChatMessage.setReadFlag(1);
					dChatMessage.setTime(timeStamp);
					dChatMessage.setDchatUid(DChat.getDchatUid(account.getEmail()+ "," + GlobalConstants.DCHAT_OA));
					DChat dChat = DChat.structureDchat(dChatMessage, account);// 注意理解这里的构造
					dChat.setdChatType(DChat.Type.OA);
					DChat LocalDChat = localStore.getDChat(dChatMessage.getDchatUid());
					DChat notifyDChat = null;
					if (LocalDChat != null) {
						dChat.setUnReadCount(LocalDChat.getUnReadCount() + 1);
						localStore.updateDchat(dChat, 1,false);
						notifyDChat = LocalDChat;
					} else {
						dChat.setUnReadCount(dChat.getUnReadCount() + 1);
						localStore.persistDChatList(dChat);
						notifyDChat = dChat;
					}
					String fromInfo = dChatMessage.getOAFrom();
					updateAndSaveContactAttribute(account,StringUtil.getInfoEMail(fromInfo), StringUtil.getInfoNickName(fromInfo),true);// 保存联系人
					localStore.persistDChatMessage(dChatMessage);
					for (MessagingListener l : getListeners(null)) {
						l.chatMessageArrived(account, dChatMessage);
					}
					for (MessagingListener l : getListeners(null)) {
						l.arrivedNewOaMessageArrived(account);
					}
					if (notifyDChat != null) {
						NotificationCenter.getInstance().onNew35OA(account.getUuid(), dChatMessage.getMessageType(), notifyDChat, dChatMessage);
					}
					break;
				default:
					break;
				}
				//刷新总数
				getTotalMsgUnreadCount(account, null);
			}
		} 
//		else if(topic.equals("Notification")){
//			for (Account localdaccount : accounts) {
//				PushDMeesage(localdaccount, localdaccount.getEmail()+"/1",localdaccount.getEmail(), prefs, timeStamp, json,false);
//			}
//		}
		else {//群聊消息
			Account defaultAccoun = prefs.getDefaultAccount();
			for (Account localdaccount : accounts) {
				LocalStore localStore = localdaccount.getLocalStore();
				String fromEmail = json.getString("f");
				if (localStore.getCGroup(topic) != null) {
					if(!defaultAccoun.getEmail().equals(localdaccount.getEmail())){
						for (MessagingListener l : getListeners(null)) {
							l.receiveMessage(localdaccount,null);
						}
					}
					CMessage cMessage = ParseJson.parseCMessage(json, topic);
					//过滤还未支持类型消息，界面不做处理。
					if(cMessage ==null){
						return;
					}
					if(localStore.isCMessageExists(cMessage.getUid())){
						//过滤重复消息
					}else{
						//if(!localdaccount.getEmail().equals(fromEmail)) {
							//确保聊天列表发送状态正确，收到消息时标记为成功状态。
							cMessage.setMessageState(CMessage.State.sendSuccess);
						//}
						cMessage.setSendTime(timeStamp);
						CGroupMember cGroupMember = new CGroupMember();
						cGroupMember.setUid(EncryptUtil.getMd5(fromEmail));
						cGroupMember.setEmail(fromEmail);
						cGroupMember.setInviteMember(false);
						// 从联系人表获取对应的昵称
						cGroupMember.setNickName(localStore
								.getNickNameByEmail(fromEmail));
						String imgHeadHash=localStore.getImgHeadHash(fromEmail);
						if(imgHeadHash==null){
							imgHeadHash = localStore.getCMemberAvatarHash(fromEmail);
						}
						cGroupMember.setAvatarHash(imgHeadHash);
						cMessage.setMember(cGroupMember);
						updateAndSaveContactAttribute(localdaccount, fromEmail,
								cGroupMember.getNickName(), true);// 保存联系人
						CGroup cGroup = localStore.getCGroup(topic);
						switch (cMessage.getMessageType()) {
						case TEXT:
							localStore.updateCgroupLastCmessage(topic, cMessage);
							localStore.saveOrUpdateGroupMember(topic,
									cMessage.getMember());
							localStore.saveOrUpdateCMessageAndCAttach(topic,
									cMessage);
							if (cMessage.getMessageType()
									.equals(CMessage.Type.TEXT)) {
								localStore.updateCGroupUntreatedCount(topic,
										cGroup.getUnreadCount() + 1);
							}
							localStore.hiddenGroup(topic, false);
							for (MessagingListener l : getListeners(null)) {
								l.cMessageArrived(localdaccount, cMessage);
							}
							NotificationCenter.getInstance()
									.onNewChattingMessage(localdaccount.getUuid(),
											cGroup, cMessage);
							break;
						case IMAGE:
							//Imageloder加载图片，服务端图片全部不保存本地路径
							saveImageCChat(topic, localdaccount,cMessage,cGroup,false);
							break;
						case ATTACHMENT:
							localStore.updateCgroupLastCmessage(topic, cMessage);
							localStore.saveOrUpdateGroupMember(topic,
									cMessage.getMember());
							localStore.saveOrUpdateCMessageAndCAttach(topic,
									cMessage);
							localStore.updateCGroupUntreatedCount(topic,
									cGroup.getUnreadCount() + 1);
							for (MessagingListener l : getListeners(null)) {
								l.cMessageArrived(localdaccount, cMessage);
							}
							NotificationCenter.getInstance()
									.onNewChattingMessage(localdaccount.getUuid(),
											cGroup, cMessage);
							break;
						case VOICE:
							FileUtil.byteArray2File(cMessage.getAttachment().getFileByte(), MailChat.getInstance().getChatVoiceDirectory(localdaccount), EncryptUtil.getMd5(cMessage.getAttachment().getAttchmentId())+ ".amr");
							localStore.updateCgroupLastCmessage(topic, cMessage);
							localStore.saveOrUpdateGroupMember(topic,
									cMessage.getMember());
							localStore.saveOrUpdateCMessageAndCAttach(topic,
									cMessage);
							localStore.updateCGroupUntreatedCount(topic,
									cGroup.getUnreadCount() + 1);
							for (MessagingListener l : getListeners(null)) {
								l.cMessageArrived(localdaccount, cMessage);
							}
							NotificationCenter.getInstance()
									.onNewChattingMessage(localdaccount.getUuid(),
											cGroup, cMessage);
							break;
						case LOCATION:

							break;
						case NOTIFICATION:
							localStore.updateCgroupLastCmessage(topic, cMessage);
							localStore.saveOrUpdateGroupMember(topic,
									cMessage.getMember());
							localStore.saveOrUpdateCMessageAndCAttach(topic,
									cMessage);
							localStore.hiddenGroup(topic, false);
							for (MessagingListener l : getListeners(null)) {
								l.cMessageArrived(localdaccount, cMessage);
							}
							NotificationCenter.getInstance()
									.onNewChattingMessage(localdaccount.getUuid(),
											cGroup, cMessage);
							break;
						case FROM_MAIL_INFO:
							cMessage.setMailFromNickName(localStore
									.getNickNameByEmail(fromEmail));
							localStore
									.updateCgroupLastCmessage(topic, cMessage);
							localStore.saveOrUpdateGroupMember(topic,
									cMessage.getMember());
							localStore.saveOrUpdateCMessageAndCAttach(topic,
									cMessage);
							localStore.updateCGroupUntreatedCount(topic,
									cGroup.getUnreadCount() + 1);
							for (MessagingListener l : getListeners(null)) {
								l.cMessageArrived(localdaccount, cMessage);
							}
							break;
						default:
							return;
						}
						//刷新总数
						getTotalMsgUnreadCount(localdaccount, null);
					}
				}
			}
		}
	}

	private void PushDMeesage(Account account,String topic,String receiverEmail,Preferences prefs,long timeStamp,JSONObject json,boolean isSendMyselfForSyn) throws MessagingException, MqttException{
		if (account != null) {
			Account defaultAccoun = prefs.getDefaultAccount();
			if(!defaultAccoun.getEmail().equals(account.getEmail())){
				for (MessagingListener l : getListeners(null)) {
					l.receiveMessage(account,null);
				}
			}
			LocalStore localStore = account.getLocalStore();
			DChatMessage dChatMessage = ParseJson.parseDmessage(json,
					receiverEmail);
			//过滤还未支持类型消息，界面不做处理。
			if(dChatMessage ==null){
				return;
			}
			if(localStore.isDMessageExists(dChatMessage.getUuid())){
				//过滤重复消息
				return;
			}
			dChatMessage.setTime(timeStamp);
			String email = dChatMessage.getSenderEmail();
			if(isSendMyselfForSyn){
				email = receiverEmail;
			}
			dChatMessage.setDchatUid(DChat.getDchatUid(account.getEmail()
					+ "," + email));
			//获取邮件透传所属email的昵称
			String nickName=localStore.getNickNameByEmail(dChatMessage.getReceiverEmail());
			dChatMessage.setMailFromNickName(nickName);
			DChat dChat = DChat.structureDchat(dChatMessage, account);// 注意理解这里的构造
			DChat localDChat = localStore.getDChat(dChatMessage
					.getDchatUid());
			DChat notifyDChat = null;
			if (localDChat != null) {
				localDChat.setLastMessage(dChat.getLastMessage());
				dChat.setUnReadCount(localDChat.getUnReadCount() + 1);
				localStore.updateDchat(dChat, 1,false);
				notifyDChat = localDChat;
			} else {
				dChat.setUnReadCount(dChat.getUnReadCount() + 1);
				localStore.persistDChatList(dChat);
				notifyDChat = dChat;
			}
			notifyDChat.setNickName(localStore.getNickNameByEmail(email));
			boolean isExist =updateAndSaveContactAttribute(account,
					notifyDChat.getEmail(), notifyDChat.getNickName(),
					true);// 保存联系人
			if(!isExist){
				syncRemoteDChatUserInfo(account, notifyDChat.getEmail());
			}
			switch (dChatMessage.getMessageType()) {
			case TEXT:
				localStore.persistDChatMessage(dChatMessage);
				for (MessagingListener l : getListeners(null)) {
					l.chatMessageArrived(account, dChatMessage);
				}
				if (notifyDChat != null) {
					NotificationCenter.getInstance()
							.onNewSingleChattingMessage(account.getUuid(),
									dChatMessage, notifyDChat,isExist);
				}
				break;
			case IMAGE:
				saveImageDChat(account,dChatMessage, notifyDChat,isExist);
				break;
			case ATTACHMENT:
				localStore.persistDChatMessage(dChatMessage);
				localStore.persistDChatAttachment(dChatMessage.getAttachments().get(0));
				for (MessagingListener l : getListeners(null)) {
					l.chatMessageArrived(account, dChatMessage);
				}
				if (notifyDChat != null) {
					NotificationCenter.getInstance()
							.onNewSingleChattingMessage(account.getUuid(),
									dChatMessage, notifyDChat,isExist);
				}
				break;
			case VOICE:
				FileUtil.byteArray2File(dChatMessage.getAttachments().get(0).getFileByte(), MailChat.getInstance().getChatVoiceDirectory(account), EncryptUtil.getMd5(dChatMessage.getAttachments().get(0).getAttchmentId())+ ".amr");
				localStore.persistDChatMessage(dChatMessage);
				localStore.persistDChatAttachment(dChatMessage.getAttachments().get(0));
				for (MessagingListener l : getListeners(null)) {
					l.chatMessageArrived(account, dChatMessage);
				}
				if (notifyDChat != null) {
					NotificationCenter.getInstance()
							.onNewSingleChattingMessage(account.getUuid(),
									dChatMessage, notifyDChat,isExist);
				}
				break;
			case LOCATION:
				break;
			case FROM_MAIL_INFO:
				localStore.persistDChatMessage(dChatMessage);
				for (MessagingListener l : getListeners(null)) {
					l.chatMessageArrived(account, dChatMessage);
				}
				break;
			default:
				return;
			}
			//刷新总数
			getTotalMsgUnreadCount(account, null);
		}else{
			if(connection.getClient().getClientHandle()!=null){
				connection.getClient().unsubscribe(topic,null,
						new ActionListener(ActionListener.Action.UNSUBSCRIBE,
								MessagingController.this, null,null,null));
			}
		}
	}
	@Override
	public void ConnectSuccess(MQTTCommand command) {
		// TODO Auto-generated method stub
		Log.i(Connection.TAG, "CONNECT:onSuccess");
//		if (command != null) {
//			switch (command) {
//				case FIRST_CONNECT:
					subscribeAllTopic();
//					connection.getClient().getConnectToken().setActionCallback(new ActionListener(ActionListener.Action.CONNECT,this,null,null,null));
//					break;
//				default:
//					break;
//			}
//		}
		//更新ping成功时间，防止勿提示.
		if(!MailChat.getInstance().isForground()){
			Editor editor =mApplication.getSharedPreferences("mqtt_ping",Context.MODE_PRIVATE).edit();
			editor.putLong("pingSuccessTime", System.currentTimeMillis());
			editor.commit();
		}
	}

	@Override
	public void ConnectFail(MQTTCommand command) {
		// TODO Auto-generated method stub
		Log.i(Connection.TAG, "CONNECT:onFailure");
		if (NetUtil.isActive()&&connection.getClient().getClientHandle()!=null&&!connection.getClient().isConnected()) {
			// format string to use a notification text
			alarmTask(RE_CONNECT,10,MqttService.class,GlobalConstants.MQTT_CONNECT_ALARM_REQUESTCODE);	
		}
		if(NetUtil.isActive()&&connection.getClient().getClientHandle()==null){
			alarmTask(NULL_MQTT_SERVICE,10,PendingService.class,GlobalConstants.NULL_MQTT_SERVICE_ALARM_REQUESTCODE);
		}
	}

	@Override
	public void DisConnectSuccess() {
		// TODO Auto-generated method stub
		Log.i(Connection.TAG, "DISCONNECT:onSuccess");
	}

	@Override
	public void DisConnectFail() {
		// TODO Auto-generated method stub
		Log.i(Connection.TAG, "DISCONNECT:onFailure");
	}

	@Override
	public void SubscribeSuccess(Account account, MQTTCommand command,Object data) {
		// TODO Auto-generated method stub
		Log.i(Connection.TAG, "SUBSCRIBE:onSuccess");
		try {
			if (command != null&&account!=null) {
				LocalStore localStore= account.getLocalStore();
				PendingMQTTConmmand pendingMQTTConmmand=null;
				switch (command) {
				case SUBSCRIBE_ACCOUNT:
					pendingMQTTConmmand = (PendingMQTTConmmand)data;
					if(pendingMQTTConmmand.getId()!=-1){
						localStore.deleteMQTTPending(pendingMQTTConmmand.getId()+"");
					}
					break;
				case SUBSCRIBE_CGROUP:
					pendingMQTTConmmand = (PendingMQTTConmmand)data;
					if(pendingMQTTConmmand.getId()!=-1){
						localStore.deleteMQTTPending(pendingMQTTConmmand.getId()+"");
					}
					break;
				case SUBSCRIBE_ALL:
					pendingMQTTConmmand = (PendingMQTTConmmand)data;
					if(pendingMQTTConmmand.getId()!=-1){
						localStore.deleteMQTTPending(pendingMQTTConmmand.getId()+"");
					}
					break;	
				case CREATE_GROUP:
					createGroupByHttps(account,(CGroup)data,null);
					break;
				case JOIN_GROUP:
					pendingMQTTConmmand = (PendingMQTTConmmand)data;
					joinGroup(account, pendingMQTTConmmand.getTopic(),pendingMQTTConmmand.getContent(),-1);
					if(pendingMQTTConmmand.getId()!=-1){
						localStore.deleteMQTTPending(pendingMQTTConmmand.getId()+"");
					}
					break;
				default:
					break;
				}
			}
			
		} catch (MessagingException e) {
			// TODO: handle exception
		}
	}

	@Override
	public void SubscribeFail(Account account, MQTTCommand command,Object data) {
		// TODO Auto-generated method stub
		Log.i(Connection.TAG, "SUBSCRIBE:onFailure");
		try {
			if (command != null&&account!=null) {
				LocalStore localStore= account.getLocalStore();
				PendingMQTTConmmand pendingMQTTConmmand=null;
				switch (command) {
				case SUBSCRIBE_ACCOUNT:
					pendingMQTTConmmand = (PendingMQTTConmmand)data;
					if(pendingMQTTConmmand.getId()==-1){
						localStore.saveMQTTPending(pendingMQTTConmmand);
					}
					alarmTask(CHAT_PENDING, 5, PendingService.class, GlobalConstants.FAIL_PENDDING_ALARM_REQUESTCODE);
					break;
				case SUBSCRIBE_CGROUP:
					pendingMQTTConmmand = (PendingMQTTConmmand)data;
					if(pendingMQTTConmmand.getId()==-1){
						localStore.saveMQTTPending(pendingMQTTConmmand);
					}
					alarmTask(CHAT_PENDING, 5, PendingService.class, GlobalConstants.FAIL_PENDDING_ALARM_REQUESTCODE);
					break;
				case SUBSCRIBE_ALL:
					pendingMQTTConmmand = (PendingMQTTConmmand)data;
					if(pendingMQTTConmmand.getId()==-1){
						localStore.saveMQTTPending(pendingMQTTConmmand);
					}
					alarmTask(CHAT_PENDING, 5, PendingService.class, GlobalConstants.FAIL_PENDDING_ALARM_REQUESTCODE);
					break;
				case CREATE_GROUP:
					for (MessagingListener l : getListeners(null)) {
						l.createGroupFail(account.getUuid());
				    }
					break;
				case JOIN_GROUP:
					pendingMQTTConmmand = (PendingMQTTConmmand)data;
					if(pendingMQTTConmmand.getId()==-1){
						localStore.saveMQTTPending(pendingMQTTConmmand);
						localStore.saveHTTPSPending(new PendingHTTPSCommand(-1,cn.mailchat.chatting.protocol.Command.JOIN_GROUP,
								pendingMQTTConmmand.getContent()+","+pendingMQTTConmmand.getTopic()));
					}
					alarmTask(CHAT_PENDING, 5, PendingService.class, GlobalConstants.FAIL_PENDDING_ALARM_REQUESTCODE);
					break;
				default:
					break;
				}
			}
		} catch (MessagingException e) {
			// TODO: handle exception
		}
	}

	@Override
	public void PublishSuccess(Account account, MQTTCommand command,Object data) {
		// TODO Auto-generated method stub
		Log.i(Connection.TAG, "PUBLISH:onSuccess");
		try {
			if (command != null) {
				LocalStore localStore= account.getLocalStore();
				PendingMQTTConmmand pendingMQTTConmmand=null;
				switch (command) {
				case SEND_CMESSAGE:
					List<String> cMessageParams = (ArrayList<String>)data;
					if(localStore.isCMessageVisibility(cMessageParams.get(1))){
						localStore.updateCGroupSendState(cMessageParams.get(0), 0);
					}
					localStore.updateCMessageState(cMessageParams.get(1), State.sendSuccess);
					for (MessagingListener l : getListeners(null)) {
						l.sendCMessagesSuccess(account,cMessageParams.get(1));
			        }
					break;
				case SEND_DMESSAGE:
					List<String> dMessageParams = (ArrayList<String>)data;
					if(!dMessageParams.get(1).endsWith(GlobalConstants.LOG_MESSAGE_UID_SUFFIX)){
						if(localStore.isDChatMessageVisibility(dMessageParams.get(1))){
							localStore.updateDChatSendState(dMessageParams.get(0), 0);
						}
						localStore.updateDchatMessageState(dMessageParams.get(1), 0, -1);
					}
					for (MessagingListener l : getListeners(null)) {
						l.sendDMessagesSuccess(account, dMessageParams.get(1));
			        }
					break;
				case SEND_INVITATION:
					pendingMQTTConmmand=(PendingMQTTConmmand)data;
					if(pendingMQTTConmmand.getId()!=-1){
						localStore.deleteMQTTPending(pendingMQTTConmmand.getId()+"");
					}
					break;
				case ADMIN_DELETE_GROUP:
					pendingMQTTConmmand=(PendingMQTTConmmand)data;
					if(pendingMQTTConmmand.getId()!=-1){
						localStore.deleteMQTTPending(pendingMQTTConmmand.getId()+"");
					}
					break;
				case RE_GROUPNAME:
					pendingMQTTConmmand=(PendingMQTTConmmand)data;
					if(pendingMQTTConmmand.getId()!=-1){
						localStore.deleteMQTTPending(pendingMQTTConmmand.getId()+"");
					}
					break;
				case ADMIN_KICKED_OUT_MEMBER:
					pendingMQTTConmmand=(PendingMQTTConmmand)data;
					if(pendingMQTTConmmand.getId()!=-1){
						localStore.deleteMQTTPending(pendingMQTTConmmand.getId()+"");
					}
					break;
				default:
					break;
				}
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void PublishSuccessByTime(Account account, MQTTCommand command,
			Object data, long time) {
		// TODO Auto-generated method stub
		Log.i(Connection.TAG, "PUBLISH:onSuccessByTime:"+time);
		try {
			if (command != null) {
				LocalStore localStore = account.getLocalStore();
				switch (command) {
				case SEND_CMESSAGE:
					List<String> params = (ArrayList<String>)data;
					localStore.updateCMessageTime(params.get(1), time);
					for (MessagingListener l : getListeners(null)) {
						l.sendCMessageTimeSuccess(account, params.get(1), time);
			        }
					break;
				case SEND_DMESSAGE:
					List<String> dMessageParams = (ArrayList<String>)data;
					if(!dMessageParams.get(1).endsWith(GlobalConstants.LOG_MESSAGE_UID_SUFFIX)){
						localStore.updateDchatMessageTime(dMessageParams.get(1), time);
					}
					for (MessagingListener l : getListeners(null)) {
						l.sendDMessageTimeSuccess(account, dMessageParams.get(1), time);
					}
					break;
				default:
					break;
				}
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void PublishFail(Account account, MQTTCommand command,Object data) {
		// TODO Auto-generated method stub
		Log.i(Connection.TAG, "PUBLISH:onFailure");
		if (NetUtil.isActive()&&connection.getClient().getClientHandle()!=null&&!connection.getClient().isConnected()) {
			// format string to use a notification text
			Intent intent = new Intent(mApplication, MqttService.class);
			intent.setAction(RE_CONNECT);
			mApplication.startService(intent);
		}
		try {
			if (command != null) {
				LocalStore localStore = account.getLocalStore();
				PendingMQTTConmmand pendingMQTTConmmand=null;
				switch (command) {
				case SEND_CMESSAGE:
					List<String> params = (ArrayList<String>)data;
					if(localStore.isCMessageVisibility(params.get(1))){
						localStore.updateCGroupSendState(params.get(0), 1);
					}
					localStore.updateCMessageState(params.get(1), State.sendFail);
					for (MessagingListener l : getListeners(null)) {
						l.sendCMessagesFail(account,params.get(1));
			        }
					break;
				case SEND_DMESSAGE:
					List<String> dMessageParams = (ArrayList<String>)data;
					if(localStore.isDChatMessageVisibility(dMessageParams.get(1))){
						localStore.updateDChatSendState(dMessageParams.get(0), 1);
					}
					localStore.updateDchatMessageState(dMessageParams.get(1), 1, -1);
					for (MessagingListener l : getListeners(null)) {
						l.sendDMessagesFail(account, dMessageParams.get(1));
					}
					break;
				case SEND_INVITATION:
					pendingMQTTConmmand=(PendingMQTTConmmand)data;
					if(pendingMQTTConmmand.getId()==-1){
						localStore.saveMQTTPending(pendingMQTTConmmand);
					}
					alarmTask(CHAT_PENDING, 5, PendingService.class, GlobalConstants.FAIL_PENDDING_ALARM_REQUESTCODE);
					break;
				case ADMIN_DELETE_GROUP:
					pendingMQTTConmmand=(PendingMQTTConmmand)data;
					if(pendingMQTTConmmand.getId()==-1){
						localStore.saveMQTTPending(pendingMQTTConmmand);
					}
					alarmTask(CHAT_PENDING, 5, PendingService.class, GlobalConstants.FAIL_PENDDING_ALARM_REQUESTCODE);
					break;
				case RE_GROUPNAME:
					pendingMQTTConmmand=(PendingMQTTConmmand)data;
					if(pendingMQTTConmmand.getId()==-1){
						localStore.saveMQTTPending(pendingMQTTConmmand);
					}
					alarmTask(CHAT_PENDING, 5, PendingService.class, GlobalConstants.FAIL_PENDDING_ALARM_REQUESTCODE);
					break;
				case ADMIN_KICKED_OUT_MEMBER:
					pendingMQTTConmmand=(PendingMQTTConmmand)data;
					if(pendingMQTTConmmand.getId()==-1){
						localStore.saveMQTTPending(pendingMQTTConmmand);
					}
					alarmTask(CHAT_PENDING, 5, PendingService.class, GlobalConstants.FAIL_PENDDING_ALARM_REQUESTCODE);
					break;
				default:
					break;
				}
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void unSubscribeSuccess(final Account account, MQTTCommand command,final Object data) {
		Log.i(Connection.TAG, "UNSUBSCRIBE:onSuccess");
		try {
			if (command != null) {
				PendingMQTTConmmand pendingMQTTConmmand =null;
				if(account!=null){
					LocalStore localStore= account.getLocalStore();
					switch (command) {
					case MEMBER_LEAVE_GROUP:
						pendingMQTTConmmand=(PendingMQTTConmmand)data;
						if(pendingMQTTConmmand.getId()!=-1){
							localStore.deleteMQTTPending(pendingMQTTConmmand.getId()+"");
						}
						break;
					case ADMIN_DELETE_GROUP:
						pendingMQTTConmmand=(PendingMQTTConmmand)data;
						if(pendingMQTTConmmand.getId()!=-1){
							localStore.deleteMQTTPending(pendingMQTTConmmand.getId()+"");
						}
						break;
					case LEAVE_GROUP:
						pendingMQTTConmmand=(PendingMQTTConmmand)data;
						if(pendingMQTTConmmand.getId()!=-1){
							localStore.deleteMQTTPending(pendingMQTTConmmand.getId()+"");
						}
						break;
					case CREATE_GROUP_FAIL:
						pendingMQTTConmmand=(PendingMQTTConmmand)data;
						if(pendingMQTTConmmand.getId()!=-1){
							localStore.deleteMQTTPending(pendingMQTTConmmand.getId()+"");
						}
						break;
					default:
						break;
					}
				}else{
					pendingMQTTConmmand=(PendingMQTTConmmand)data;
					if(pendingMQTTConmmand.getId()!=-1){
						PendingChatCommandLocalStore.getInstance(mApplication).deleteMQTTPending(pendingMQTTConmmand.getId()+"");
					}
				}
			}
		} catch (MessagingException e) {
			// TODO: handle exception
		}
	}
	
	@Override
	public void unSubscribeFail(Account account, MQTTCommand command,Object data) {
		Log.i(Connection.TAG, "UNSUBSCRIBE:onFailure");
		try {
			if (command != null) {
				PendingMQTTConmmand pendingMQTTConmmand =null;
				if(account!=null){
					LocalStore localStore  =account.getLocalStore();
					switch (command) {
					case MEMBER_LEAVE_GROUP:
						pendingMQTTConmmand=(PendingMQTTConmmand)data;
						if(pendingMQTTConmmand.getId()==-1){
							localStore.saveMQTTPending(pendingMQTTConmmand);
						}
						alarmTask(CHAT_PENDING, 5, PendingService.class, GlobalConstants.FAIL_PENDDING_ALARM_REQUESTCODE);
						break;
					case ADMIN_DELETE_GROUP:
						pendingMQTTConmmand=(PendingMQTTConmmand)data;
						if(pendingMQTTConmmand.getId()==-1){
							localStore.saveMQTTPending(pendingMQTTConmmand);
						}
						alarmTask(CHAT_PENDING, 5, PendingService.class, GlobalConstants.FAIL_PENDDING_ALARM_REQUESTCODE);
						break;
					case LEAVE_GROUP:
						pendingMQTTConmmand=(PendingMQTTConmmand)data;
						if(pendingMQTTConmmand.getId()==-1){
							localStore.saveMQTTPending(pendingMQTTConmmand);
						}
						alarmTask(CHAT_PENDING, 5, PendingService.class, GlobalConstants.FAIL_PENDDING_ALARM_REQUESTCODE);
						break;
					case CREATE_GROUP_FAIL:
						pendingMQTTConmmand=(PendingMQTTConmmand)data;
						if(pendingMQTTConmmand.getId()==-1){
							localStore.saveMQTTPending(pendingMQTTConmmand);
						}
						alarmTask(CHAT_PENDING, 5, PendingService.class, GlobalConstants.FAIL_PENDDING_ALARM_REQUESTCODE);
						break;
					default:
						break;
					}
				}else{
					if(command.equals(MQTTCommand.UNSUBSCRIBE_ACCOUNT)){
						pendingMQTTConmmand=(PendingMQTTConmmand)data;
						if(pendingMQTTConmmand.getId()==-1){
							PendingChatCommandLocalStore.getInstance(mApplication).saveMQTTPending(pendingMQTTConmmand);
						}
						alarmTask(CHAT_UNSUBSCRIBE_PENDING, 5, PendingService.class, GlobalConstants.FAIL_PENDDING_ALARM_REQUESTCODE_ACCOUNT);
					}
				}
			}
		} catch (MessagingException e) {
			// TODO: handle exception
		}
	}
	/**
	 * 订阅群组主题
	 * 
	 * @Description:
	 * @param DMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-9-26
	 */
	public void subscribeCGroup(Account account,List<CGroup> cGroups) {
//		threadPool.execute(new Runnable() {
//			@Override
//			public void run() {
				try {
					ArrayList<String> cGroupsUUID =new ArrayList<String>();
//					LocalStore localStore =account.getLocalStore();
//					List<CGroup> cGroups= localStore.getCGroups();
					String cGroupSubscribe = null;
					for(int i=0;i<cGroups.size();i++){
						String uid =cGroups.get(i).getUid();
						cGroupsUUID.add(uid);
						if(cGroupSubscribe==null){
							cGroupSubscribe=uid;
						}else{
							cGroupSubscribe+=","+uid;
						}
					}
					int [] qos =new int[cGroupsUUID.size()];
					for(int i=0;i<cGroupsUUID.size();i++){
						qos[i]=MQTT_QOS;
					}
					if(connection.getClient().getClientHandle()!=null&&cGroupsUUID.size()>0){
						PendingMQTTConmmand pendingMQTTConmmand =new PendingMQTTConmmand(-1,ActionListener.Action.SUBSCRIBE,MQTTCommand.SUBSCRIBE_CGROUP,cGroupSubscribe,null);
						connection.getClient().subscribe(cGroupsUUID.toArray(new String[cGroupsUUID.size()]), qos, null, new ActionListener(ActionListener.Action.SUBSCRIBE,MessagingController.this,account,MQTTCommand.SUBSCRIBE_CGROUP,pendingMQTTConmmand));
					}
				} catch (MqttException  e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				catch (MessagingException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//   });
	}
	/**
	 * 订阅该连接的数据库所有主题
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-15
	 */
	public void subscribeAllTopic() {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Preferences prefs = Preferences.getPreferences(mApplication);
					List<Account>  accounts = prefs.getAccounts();
					List<String> allTopic =new ArrayList<String>();
					for(Account localdaccount:accounts){
						LocalStore localStore =localdaccount.getLocalStore();
						if(localdaccount.isHideAccount()){
							allTopic.add(SystemUtil.getCliendId(mApplication)+GlobalConstants.HIDE_ACCOUNT_SUFFIX);
						}else{
							allTopic.add(localdaccount.getEmail()+"/1");//单聊消息频道
							allTopic.add(localdaccount.getEmail()+"/s");//系统频道
							allTopic.add(localdaccount.getEmail()+"/a");//服务端推送
						}
						List<CGroup> cGroups= localStore.getCGroups();
						for(CGroup cGroup:cGroups){
							allTopic.add(cGroup.getUid());//账户群频道
						}
					}
					allTopic.add("Notification");
					//拼装所有频道
					String allTopicSubscribe =null;
					for(int i=0;i<allTopic.size();i++){
						if(allTopicSubscribe==null){
							allTopicSubscribe=allTopic.get(i);
						}else{
							allTopicSubscribe+=","+allTopic.get(i);
						}
					}
					
					int [] qos =new int[allTopic.size()];
					for(int i=0;i<allTopic.size();i++){
						qos[i]=MQTT_QOS;
					}
					if(connection.getClient().getClientHandle()!=null&&allTopic.size()>0){
						PendingMQTTConmmand pendingMQTTConmmand =new PendingMQTTConmmand(-1,ActionListener.Action.SUBSCRIBE,MQTTCommand.SUBSCRIBE_ALL,allTopicSubscribe,null);
						connection.getClient().subscribe(allTopic.toArray(new String[allTopic.size()]), qos, null, new ActionListener(ActionListener.Action.SUBSCRIBE,MessagingController.this,prefs.getDefaultAccount(),MQTTCommand.SUBSCRIBE_ALL,pendingMQTTConmmand));
					}
				} catch (MqttException  e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 订阅账户主题
	 * 
	 * @Description:
	 * @param DMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-20
	 */
	public void subscribeAccount(final Account account ) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					String [] topic =new String []{account.getEmail()+"/1",account.getEmail()+"/s",account.getEmail()+"/a"};
					int [] qos =new int[]{MQTT_QOS,MQTT_QOS};
				if(connection.getClient().getClientHandle()!=null){
					String accountSubscribe =account.getEmail()+"/1"+","+account.getEmail()+"/s"+","+account.getEmail()+"/a";
					PendingMQTTConmmand pendingMQTTConmmand =new PendingMQTTConmmand(-1,ActionListener.Action.SUBSCRIBE,MQTTCommand.SUBSCRIBE_ACCOUNT,accountSubscribe,null);
					connection.getClient().subscribe(topic, qos, null, new ActionListener(ActionListener.Action.SUBSCRIBE,MessagingController.this,account,MQTTCommand.SUBSCRIBE_ACCOUNT,pendingMQTTConmmand));
					}
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 保存到本地消息，用于不急着发送的消息，只做本地显示使用，当需要发送是判断发送
	 *
	 * @Description:
	 * @param DMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2016-3-9
	 */
	public void saveAndShowDMessage(DChatMessage dMessage,
			Account account, MessagingListener listener) {
		try {
			LocalStore localStore = account.getLocalStore();
			if (!dMessage.getUuid().endsWith(
					GlobalConstants.LOG_MESSAGE_UID_SUFFIX)) {
				localStore.persistDChatMessage(dMessage);
			}
			if (dMessage.getMessageType() == DChatMessage.Type.FROM_MAIL_INFO) {
				dMessage.setMailFromNickName(localStore
						.getNickNameByEmail(dMessage.getMailFrom()));
			}
			DChat dChat = DChat.structureDchat(dMessage, account);
			DChat localDChat = localStore.getDChat(dMessage.getDchatUid());
			if (localDChat != null) {
				localDChat.setLastMessage(dChat.getLastMessage());
				if (!dMessage.getUuid().endsWith(
						GlobalConstants.LOG_MESSAGE_UID_SUFFIX)) {
					localStore.updateDchat(dChat, 1, false);
				}
			} else {
				localStore.persistDChatList(dChat);
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 发送本地已保存显示的消息，主要更具特定条件判断是否发送。
	 *
	 * @Description:
	 * @param DMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2016-3-9
	 */
	public void pulishOnlySaveDMessage(final String topic, final DChatMessage dMessage,final Account account) {
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(connection.getClient().getClientHandle()!=null){
					JSONObject dMessageJson=null;
					switch (dMessage.getMessageType()) {
					case FROM_MAIL_INFO:
						dMessageJson = CreateJson.createDmessage(dMessage);
						break;
					default:
						return;
					}
					List<String> params =new ArrayList<String>();
					params.add(dMessage.getDchatUid());
					params.add(dMessage.getUuid());
					try {
						connection.getClient().publish(topic, dMessageJson.toString().getBytes(),MQTT_QOS, false, null, new ActionListener(ActionListener.Action.PUBLISH,MessagingController.this,account,MQTTCommand.SEND_DMESSAGE,params));
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					MQTTConnect(false);
				}
			}
		});
	}
	/**
	 *
	 * method name: publishUserMsgToHelpAccount
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account
	 *      @param topic
	 *      @param email
	 *      @param uuid
	 *      @param clientVersion    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2016-3-21 下午2:10:09	@Modified by：zhangjx
	 *     @Description：将登录框相关信息发送给小助手，服务端截取并自动回复
	 */
	public void publishUserMsgToHelpAccount(final Account account,final String topic,
			final String email, final String uuid, final String clientVersion) {
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(connection.getClient().getClientHandle()!=null){
					String	message = CreateJson.sendUserMessageToHelpAccount(account.getEmail(),email,uuid,clientVersion).toString();
					try {
						connection.getClient().publish(topic, message.getBytes(),MQTT_QOS, false, null, null);
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					MQTTConnect(false);
				}
			}
		});
	}
	/**
	 * 发送单聊消息
	 * 
	 * @Description:
	 * @param DMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-9-26
	 */
	public void publishDMessage(final String topic,final DChatMessage dMessage,final Account account,final MessagingListener listener) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				LocalStore localStore =null;
				try {	
					localStore= account.getLocalStore();
					if(!dMessage.getUuid().endsWith(GlobalConstants.LOG_MESSAGE_UID_SUFFIX)){
						localStore.persistDChatMessage(dMessage);
					}
					if(dMessage.getMessageType()==DChatMessage.Type.FROM_MAIL_INFO){
						dMessage.setMailFromNickName(localStore.getNickNameByEmail(dMessage.getMailFrom()));
					}
					DChat dChat = DChat.structureDchat(dMessage,account);
					DChat localDChat = localStore.getDChat(dMessage.getDchatUid());
					if (localDChat != null) {
						localDChat.setLastMessage(dChat.getLastMessage());
						if(!dMessage.getUuid().endsWith(GlobalConstants.LOG_MESSAGE_UID_SUFFIX)){
							localStore.updateDchat(dChat, 1,false);
						}
					} else {
						localStore.persistDChatList(dChat);
					}
					if(connection.getClient().getClientHandle()!=null){
						JSONObject dMessageJson=null;
						DAttachment  dAttachment=null;
						String attchmentId=null;
						List<DAttachment> dAttachments=null;
						File file=null;
						if(!dMessage.getUuid().endsWith(GlobalConstants.LOG_MESSAGE_UID_SUFFIX)){
							localStore.updateDChatSendState(dMessage.getDchatUid(), 2);
						}
						switch (dMessage.getMessageType()) {
						case TEXT:
							dMessageJson = CreateJson.createDmessage(dMessage);
							break;
						case IMAGE:
							dAttachments=  dMessage.getAttachments();
							dAttachment=dAttachments.get(0);
							localStore.persistDChatAttachment(dAttachment);
							if(dAttachment.getFileid()==null){
								file=new File(dAttachment.getFilePath());
								if(file.exists()){
									if(file.length() / 1000 >1024){
										file=new File(MailChat.application.getChatLocalThumbnailImageCacheDirectory(account)+file.getName().substring(0, file.getName().indexOf("."))+".jpeg");
										//发送时有处理图片的旋转，可以参考
										ImageUtils.compressBmpToFile(ImageUtils.getNativeImage(dAttachment.getFilePath(),true), file);
									}
									JSONObject imageJson = Protocol.getInstance().uploadFile(file,dAttachment.getAttchmentId(),new UploadCallback() {

										@Override
										public void uploadProgress(String id, int progress) {
											// TODO Auto-generated method stub
										}

										@Override
										public void uploadInterrupt(String id) {
											// TODO Auto-generated method stub

										}
									});
									dAttachment.setName(imageJson.getString("filename"));
									dAttachment.setFileid(imageJson.getString("checksum"));
									dAttachment.setSize(imageJson.getLong("size"));
									localStore.updateUploadDAttServerAttribute(dAttachment);
								}else{
									return;
								}
							}
							dMessageJson = CreateJson.createDmessage(dMessage);
							break;
						case ATTACHMENT:
							dAttachments=  dMessage.getAttachments();
							dAttachment=dAttachments.get(0);
							attchmentId=dAttachment.getAttchmentId();
							if(!dMessage.getUuid().endsWith(GlobalConstants.LOG_MESSAGE_UID_SUFFIX)){
								localStore.persistDChatAttachment(dAttachment);
							}
							if(dAttachment.getFileid()==null){
								file=new File(dAttachment.getFilePath());
								if(file.exists()){
									Uploader uploader =null;
									if(uploaders.get(attchmentId)!=null){
										return;
									}else{
										uploader=new Uploader();
										uploaders.put(attchmentId, uploader);
									}
									for (MessagingListener l : getListeners(null)) {
										l.uploadStart(account, attchmentId);
								    }
									JSONObject fileJson=uploader.uploadFile(Protocol.getInstance().getUrl(cn.mailchat.chatting.protocol.Command.UPLOAD_FILE), file, null, "file", attchmentId, new UploadCallback(){

										@Override
										public void uploadProgress(String id,
												int progress) {
											// TODO Auto-generated method stub
											//TODO:优化上传卡主问题，导致消息列表状态异常。
											/*	1、查询库里checksum是否存在，如果存在不管
												2.如果不存在，将对应progress置为0
												3.界面处理，显示进度和取消按键*/
											for (MessagingListener l : getListeners(null)) {
												l.uploadProgress(account, id, progress);
										    }
										}

										@Override
										public void uploadInterrupt(String id) {
											// TODO Auto-generated method stub
											uploaders.remove(id);
										}

									});
									uploaders.remove(attchmentId);
									if(fileJson!=null){
										dAttachment.setName(fileJson.getString("filename"));
										dAttachment.setFileid(fileJson.getString("checksum"));
										dAttachment.setSize(fileJson.getLong("size"));
										localStore.updateUploadDAttServerAttribute(dAttachment);
									}else{
										throw new MessageException(10001);
									}
								}else {
									return;
								}
							}
							dMessageJson = CreateJson.createDmessage(dMessage);
							break;
						case VOICE:
							DAttachment vAttachment= dMessage.getAttachments().get(0);
							localStore.persistDChatAttachment(vAttachment);
							File vfile =new File(vAttachment.getFilePath());
							if(vfile.exists()){
								vAttachment.setFileByte(FileUtil.file2ByteArray(vfile));
								dMessageJson = CreateJson.createDmessage(dMessage);
								break;
							}else{
								return;
							}
						case LOCATION:
							break;
						case NOTIFICATION:
							break;
						case FROM_MAIL_INFO:
							dMessageJson = CreateJson.createDmessage(dMessage);
							break;
						default:
							return;
						}
						List<String> params =new ArrayList<String>();
						params.add(dMessage.getDchatUid());
						params.add(dMessage.getUuid());
						connection.getClient().publish(topic, dMessageJson.toString().getBytes(),MQTT_QOS, false, null, new ActionListener(ActionListener.Action.PUBLISH,MessagingController.this,account,MQTTCommand.SEND_DMESSAGE,params));	
						//小助手同步
						if(account.getEmail().equals(GlobalConstants.HELP_ACCOUNT_EMAIL)||account.getEmail().equals(GlobalConstants.IOS_HELP_ACCOUNT_EMAIL)){
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							String helpDMessageJson = dMessageJson.put("t", dMessage.getReceiverEmail()).toString();
							connection.getClient().publish(account.getEmail()+"/1", helpDMessageJson.getBytes(),MQTT_QOS, false, null, new ActionListener(ActionListener.Action.PUBLISH,MessagingController.this,account,MQTTCommand.SEND_DMESSAGE,params));	
						}
					}else{
						MQTTConnect(false);
						//TODO：做连接成功判断
						localStore.updateDchatMessageState(dMessage.getUuid(), 1, -1);
						localStore.updateDChatSendState(dMessage.getDchatUid(), 1);
						for (MessagingListener l : getListeners(null)) {
							l.sendDMessagesFail(account, dMessage.getUuid());
						}
					}
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MqttPersistenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MessageException |JSONException e) {
					// TODO Auto-generated catch block
					if(localStore!=null&&!dMessage.getUuid().endsWith(GlobalConstants.LOG_MESSAGE_UID_SUFFIX)){
						try {
							localStore.updateDchatMessageState(dMessage.getUuid(), 1, -1);
							if(localStore.isDChatMessageVisibility(dMessage.getUuid())){
								localStore.updateDChatSendState(dMessage.getDchatUid(), 1);
							}
						} catch (UnavailableStorageException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}			
					for (MessagingListener l : getListeners(null)) {
						l.sendDMessagesFail(account, dMessage.getUuid());
					}
					e.printStackTrace();
				}
			}
		});
	}
	
	
	/**
	 * 发送群聊消息
	 * 
	 * @Description:
	 * @param cMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-17
	 */
	public void publishCMessage(final String topic,final CMessage cMessage,final Account account,final MessagingListener listener) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				LocalStore localStore = null;
				try {
					localStore = account.getLocalStore();
					cMessage.getMember().setNickName(localStore.getNickNameByEmail(account.getEmail()));
					localStore.saveOrUpdateCMessageAndCAttach(cMessage.getGroupUid(), cMessage);
					localStore.updateCgroupLastCmessage(cMessage.getGroupUid(), cMessage);
					if(connection.getClient().getClientHandle()!=null){
						JSONObject cMessageJson=null;
						CAttachment cAttachment=null;
						String attchmentId =null;
						File file=null;
						JSONObject fileJson=null;
						localStore.updateCGroupSendState(cMessage.getGroupUid(),2);
						switch (cMessage.getMessageType()) {
						case TEXT:
							cMessageJson = CreateJson.createCmessage(cMessage);
							break;
						case IMAGE:
							cAttachment=  cMessage.getAttachment();
							if(cAttachment.getFileid()==null){
								file =new File(cAttachment.getFilePath());
								if(file.exists()){
									int [] size;
									if(file.length() / 1000 >1024){
										//压缩路径
										file=new File(MailChat.application.getChatLocalThumbnailImageCacheDirectory(account)+file.getName().substring(0, file.getName().indexOf("."))+".jpeg");
										//发送时有处理图片的旋转，可以参考
										ImageUtils.compressBmpToFile(ImageUtils.getNativeImage(cAttachment.getFilePath(),true), file);
									}
									fileJson= Protocol.getInstance().uploadFile(file,cAttachment.getAttchmentId(),new UploadCallback() {

										@Override
										public void uploadProgress(String id, int progress) {
											// TODO Auto-generated method stub

										}

										@Override
										public void uploadInterrupt(String id) {
											// TODO Auto-generated method stub

										}
									});
									cAttachment.setName(fileJson.getString("filename"));
									cAttachment.setFileid(fileJson.getString("checksum"));
									cAttachment.setSize(fileJson.getLong("size"));
									localStore.updateUploadCAttServerAttribute(cAttachment);
								}else{
									return;
								}
							}
							cMessageJson = CreateJson.createCmessage(cMessage);
							break;
						case ATTACHMENT:
							cAttachment=  cMessage.getAttachment();
							attchmentId = cAttachment.getAttchmentId();
							if(cAttachment.getFileid()==null){
								file =new File(cAttachment.getFilePath());
								if(file.exists()){
									Uploader uploader =null;
									if(uploaders.get(attchmentId)!=null){
										return;
									}else{
										uploader=new Uploader();
										uploaders.put(attchmentId, uploader);
									}
									for (MessagingListener l : getListeners(null)) {
										l.uploadStart(account, attchmentId);
								    }
									fileJson=uploader.uploadFile(Protocol.getInstance().getUrl(cn.mailchat.chatting.protocol.Command.UPLOAD_FILE), file, null, "file", attchmentId, new UploadCallback(){

										@Override
										public void uploadProgress(String id,
												int progress) {
											// TODO Auto-generated method stub
											for (MessagingListener l : getListeners(null)) {
												l.uploadProgress(account, id, progress);
										    }
										}

										@Override
										public void uploadInterrupt(String id) {
											// TODO Auto-generated method stub
											uploaders.remove(id);
											//如果子线程中加回调，更新UI，下面失败的时候更新消息列表发送状态需要注意去加条件不让其更新发送状态。
										}

									});
									uploaders.remove(attchmentId);
									if(fileJson!=null){
										cAttachment.setName(fileJson.getString("filename"));
										cAttachment.setFileid(fileJson.getString("checksum"));
										cAttachment.setSize(fileJson.getLong("size"));
										localStore.updateUploadCAttServerAttribute(cAttachment);
									}else{
										throw new MessageException(10001);
									}
								}else{
									return;
								}
							}
							cMessageJson = CreateJson.createCmessage(cMessage);
							break;
						case VOICE:
							cAttachment=  cMessage.getAttachment();
							file =new File(cAttachment.getFilePath());
							if(file.exists()){
								cAttachment.setFileByte(FileUtil.file2ByteArray(file));
								cMessageJson = CreateJson.createCmessage(cMessage);
								break;
							}else{
								return;
							}
						case LOCATION:
							break;
						case FROM_MAIL_INFO:
							cMessageJson = CreateJson.createCmessage(cMessage);
							break;
						default:
							return;
						}
						List<String> params =new ArrayList<String>();
						params.add(cMessage.getGroupUid());
						params.add(cMessage.getUid());
						connection.getClient().publish(topic, cMessageJson.toString().getBytes(), MQTT_QOS, false, null, new ActionListener(ActionListener.Action.PUBLISH,MessagingController.this,account,MQTTCommand.SEND_CMESSAGE,params));
					}else{
						MQTTConnect(false);
						//TODO：做连接成功判断
						localStore.updateCMessageState(cMessage.getUid(), State.sendFail);
						localStore.updateCGroupSendState(cMessage.getGroupUid(), 1);
						for (MessagingListener l : getListeners(null)) {
							l.sendCMessagesFail(account, cMessage.getUid());
						}
					}
				}catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch (MessageException |JSONException e) {
					// TODO Auto-generated catch block
					if(localStore!=null){
						try {
							localStore.updateCMessageState(cMessage.getUid(), State.sendFail);
							if(localStore.isCMessageVisibility(cMessage.getUid())){
								localStore.updateCGroupSendState(cMessage.getGroupUid(), 1);
							}
						} catch (UnavailableStorageException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					for (MessagingListener l : getListeners(null)) {
						l.sendCMessagesFail(account, cMessage.getUid());
					}
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 取消上传
	 *
	 * @Description:
	 * @param Account
	 * @param attchmentId
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-7-2
	 */
	public void cancelUpFile(Account account ,String attchmentId){
		Uploader uploader =uploaders.get(attchmentId);
		if(uploader!=null){
			//先更新UI（如果想用户看到取消的进度，可以放到回调中。如果放到回调中，注意该条消息在聊天列表中发送状态更新顺序。（主线程，子线程顺序，需要做标记防止更新出错。））
			for (MessagingListener l : getListeners(null)) {
				l.uploadInterrupt(account, attchmentId);
		    }
			//再更新后台
			uploader.cancel(attchmentId);
		}
	}

	/**
	 * 获取单聊列表
	 * 
	 * @Description:
	 * @param DMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-10
	 */
	public void listDChats(final Account account,final MessagingListener listener) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					LocalStore localStore = account.getLocalStore();
					List<DChat> DChats=localStore.listDchats();
					for (MessagingListener l : getListeners(null)) {
			    		l.listDchatsFinished(account,DChats);
			        }
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
	}
	
	/**
	 * 获取单聊消息
	 * 
	 * @Description:
	 * @param DMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-10
	 */
	public void listDMessages(final Account account,final String dChatUid,final int count ,final MessagingListener listener) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					LocalStore localStore = account.getLocalStore();
					List<DChatMessage> dChatMessages=localStore.listDchatMessages(dChatUid, count);	
					for (MessagingListener l : getListeners(null)) {
						 l.listDMessagesFinished(account.getUuid(), dChatUid, dChatMessages);
			        }
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	/**
	 * 创建群
	 * 
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-16
	 */
	public void createGroup(final Account account,final CGroup group,final MessagingListener listener) {
		threadPool.execute(new Runnable() {
			@SuppressWarnings("null")
			@Override
			public void run() {
				try {
					LocalStore localStore = account.getLocalStore();
					if(connection.getClient().getClientHandle()!=null){
						String groupName =null;	
						//TODO:群成员昵称设置，后期优化
						for(CGroupMember cGroupMember :group.getMembers()){
							String nickName =localStore.getNickNameByEmail(cGroupMember.getEmail());
							cGroupMember.setNickName(nickName);
							ContactAttribute contactAttribute =localStore.getContactAttribute(cGroupMember.getEmail(),false);
							if(contactAttribute==null){
								//存储联系人
								localStore.savaContact(cGroupMember.getEmail(), nickName,false);
								ContactAttribute contact=addTempContact(cGroupMember.getEmail(),null);
								tempContactList.add(contact);
							}else{
								//更新联系人使用次数
								localStore.updateContactAttributeReceiveCount(cGroupMember.getEmail(), contactAttribute.getReceiveCount()+1);
							}
							if(groupName==null){								
								groupName=nickName;
							}else{
								groupName+=","+nickName;
							}
						}
						reflashContactList(account,tempContactList);
						if(groupName.length()>99){
							groupName=groupName.substring(0, 98);
						}
						group.setGroupName(groupName);
						connection.getClient().subscribe(
								group.getUid(),
								MQTT_QOS,
								null,
								new ActionListener(
										ActionListener.Action.SUBSCRIBE,
										MessagingController.this, account,
										MQTTCommand.CREATE_GROUP, group));
					}else{
						for (MessagingListener l : getListeners(null)) {
							l.createGroupFail(account.getUuid());
				        }
					}	
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					for (MessagingListener l : getListeners(null)) {
						l.createGroupFail(account.getUuid());
			        }
					e.printStackTrace();		
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
		});
	}
	/**
	 * 创建群(订阅成功后)
	 * 
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-1-11
	 */
	public void createGroupByHttps(final Account account,final CGroup group,final MessagingListener listener) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				LocalStore localStore=null;
				Map<String,String> invitationMessages =new HashMap<String,String>();
				try {
					localStore = account.getLocalStore();
					CGroup cGroup = Protocol.getInstance().createGroup(
							account.getEmail(), group,((MailChat) mApplication).getLanguage());
					cGroup.setLastSendDate(System.currentTimeMillis());
					cGroup.setIsAdmin(true);// 设置当前账户为该群管理员
					cGroup.setIsMember(false);
					cGroup.setIsMessageAlert(true);
					cGroup.setIsSticked(false);
					cGroup.setMembers(group.getMembers());
					cGroup.setGroupName(group.getGroupName());
					
					// 将当前账户转换为成员保持到成员表
					CGroupMember member = new CGroupMember();
					member.setUid(EncryptUtil.getMd5(account.getEmail()));
					member.setEmail(account.getEmail());
					member.setNickName(localStore.getNickNameByEmail(account.getEmail()));
					member.setAdmin(true);
					member.setInviteMember(false);
					CMessage cJoinMessage =new CMessage(Type.NOTIFICATION);
					cJoinMessage.setUid(UUID.randomUUID().toString());
					cJoinMessage.setMember(member);
					cJoinMessage.setSendTime(System.currentTimeMillis());
					cJoinMessage.setGroupUid(cGroup.getUid());
					String font = TimeUtils.DateFormatMDHM.format(new Date(cJoinMessage
							.getSendTime()));
					String cGroupName=null;
					List<CGroupMember> cGroupMembers = cGroup.getMembers();		
					for (int j = 0; j < cGroupMembers.size(); j++) {
						CGroupMember cGroupMember =	cGroupMembers.get(j);
						if(cGroupName==null){
							cGroupName =cGroupMember.getNickName();
						}else{
							cGroupName+=" , "+cGroupMember.getNickName();
						}
						if(j>3){
							cGroupName	=cGroupName+" .... ";
							break;
						}
					}					
					cJoinMessage.setContent(String.format(mApplication.getString(R.string.chat_notify_invitation_item),font,cGroupName));	
				
					CMessage cMessage =new CMessage(Type.NOTIFICATION);
					cMessage.setUid(UUID.randomUUID().toString());
					cMessage.setMember(member);
					cMessage.setSendTime(System.currentTimeMillis()+1);
					cMessage.setGroupUid(cGroup.getUid());
					cMessage.setContent(mApplication.getString(R.string.chat_notify_item));
					
					cGroup.setLastMessageType(cJoinMessage.getMessageType());
					cGroup.setLastMessageUid(cJoinMessage.getUid());
					cGroup.setLastMemberNickName(member.getEmail());
					cGroup.setLastSendDate(cJoinMessage.getSendTime());
					cGroup.setLastMessageContent(cJoinMessage.getContent());		
				
					localStore.saveOrUpdateCGroup(cGroup);
					localStore.saveOrUpdateGroupMember(cGroup.getUid(),member);
					localStore.saveOrUpdateCMessageAndCAttach(cGroup.getUid(), cJoinMessage);
					localStore.saveOrUpdateCMessageAndCAttach(cGroup.getUid(), cMessage);
					
					List<CGroupMember> CGroupMembers = cGroup.getMembers();
					//创建群成功，发送系统消息，通知群成员调用/JG接口加入群中
					if(connection.getClient().getClientHandle()!=null){
						String messageUid =UUID.randomUUID().toString();
						for(CGroupMember cGroupMember : CGroupMembers){
							String topic=cGroupMember.getEmail()+"/s";
							String invitationMessage = CreateJson.invitationMessage(account.getEmail(),cGroupMember.getEmail(),cGroup.getUid(),messageUid).toString();
							connection.getClient().publish(topic,invitationMessage.getBytes(), MQTT_QOS, false, null, new ActionListener(ActionListener.Action.PUBLISH,MessagingController.this,account,MQTTCommand.SEND_INVITATION,new PendingMQTTConmmand(-1,ActionListener.Action.PUBLISH,MQTTCommand.SEND_INVITATION,topic,invitationMessage)));
							invitationMessages.put(topic, invitationMessage);
							try {
								Thread.sleep(50);//休眠0.05秒，防止过快引起发送失败问题
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					for (MessagingListener l : getListeners(null)) {
						l.createGroupSuccess(account.getUuid(), cGroup);
					}
				} catch (UnavailableStorageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					//保存邀请消息,执行重发
					if (localStore != null && invitationMessages.size() > 0) {
						for (Map.Entry<String, String> entry : invitationMessages.entrySet()) {
							localStore.saveMQTTPending(new PendingMQTTConmmand(-1,ActionListener.Action.PUBLISH,MQTTCommand.SEND_INVITATION,entry.getKey(),entry.getValue()));
						}
					}
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch (MessageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					for (MessagingListener l : getListeners(null)) {
						l.createGroupFail(account.getUuid());
					}
					//存储起来，退订该频道
					if (localStore != null) {
						localStore.saveMQTTPending(new PendingMQTTConmmand(-1,ActionListener.Action.UNSUBSCRIBE,MQTTCommand.CREATE_GROUP_FAIL,group.getUid(), null));
					}
					alarmTask(CHAT_PENDING, 5, PendingService.class, GlobalConstants.FAIL_PENDDING_ALARM_REQUESTCODE);
				} 
			}
		});
	}
	/**
	 * 加入群
	 * 
	 * @Description:
	 * @param account
	 * @param subscribeGroupId 需要订阅的主题，及代理服务器的群组ID
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-16
	 */
	public void joinGroup(final Account account,final String subscribeGroupId,final String inviteEmail,final int pendingId) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				LocalStore localStore=null;
				try {
					localStore = account.getLocalStore();
					CGroup cGroup = Protocol.getInstance().joinGroup(
							account.getEmail(), subscribeGroupId);
					cGroup.setIsAdmin(false);
					cGroup.setIsMember(true);

					CGroupMember member = new CGroupMember();
					member.setUid(EncryptUtil.getMd5(account.getEmail()));
					member.setEmail(account.getEmail());
					member.setNickName(account.getEmail());
					member.setAdmin(false);// 获取到邀请消息，加入群组,为群成员。
					List<CGroupMember> members = cGroup.getMembers();
					// members.add(member);
					// TODO:群成员昵称设置，后期优化
					for (CGroupMember cGroupMember : members) {
						String nickName = localStore
								.getNickNameByEmail(cGroupMember.getEmail());
						cGroupMember.setNickName(nickName);
					}
					// 加入邀请消息提示
					CMessage cMessage = new CMessage(Type.NOTIFICATION);
					cMessage.setUid(UUID.randomUUID().toString());
					cMessage.setGroupUid(cGroup.getUid());
					cMessage.setMember(member);
					cMessage.setSendTime(System.currentTimeMillis());
					String inviteNickName = localStore
							.getNickNameByEmail(inviteEmail);
					cMessage.setContent(String.format(mApplication
							.getString(R.string.change_group_join_message),
							inviteNickName));
					// 添加到最后一条消息
					cGroup.setLastMemberNickName(inviteNickName);
					cGroup.setLastMessageContent(cMessage.getContent());
					cGroup.setLastMessageType(Type.NOTIFICATION);
					cGroup.setLastMessageUid(cMessage.getUid());
					cGroup.setLastSendDate(cMessage.getSendTime());
					localStore.saveOrUpdateCMessageAndCAttach(subscribeGroupId,
							cMessage);
					localStore.saveOrUpdateCGroup(cGroup);
					localStore.saveOrUpdateGroupMembers(cGroup.getUid(),
							members);
					for (MessagingListener l : getListeners(null)) {
						l.joinGroupSuccess(account.getUuid());
					}
					if(pendingId!=-1){
						localStore.deleteHTTPSPending(pendingId+"");
					}
				} catch ( MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MessageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if(pendingId==-1){
						localStore.saveHTTPSPending(new PendingHTTPSCommand(-1,cn.mailchat.chatting.protocol.Command.JOIN_GROUP,
								inviteEmail+","+subscribeGroupId));
					}
					alarmTask(CHAT_PENDING, 5, PendingService.class, GlobalConstants.FAIL_PENDDING_ALARM_REQUESTCODE);
					if(e.getExceptionType()==Response.ERROR){
						localStore.deleteHTTPSPending(pendingId+"");
					}
				}		
			}
		});
	}
	
	
	/**
	 * 获取混合列表
	 * 
	 * @Description:
	 * @param DMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-10
	 */
	public void listMixedChattings(final Account account,final MessagingListener listener) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					LocalStore localStore = account.getLocalStore();
					int totalCount=localStore.getMixedUnreadCount();
					List<MixedChatting> mixedChattings=localStore.listMixedChatting();
					//判断邮洽小助手是否添加到列表
					if(mixedChattings.size()>0){
						DChat helpDChat = getHelpDChat(account);
						if(!helpDChat.isVisibility()){
							localStore.updateDchatDeleteFlag(DChat.getDchatUid(account.getEmail()+","+GlobalConstants.HELP_ACCOUNT_EMAIL),true);
							int index =-1;
							for(int i=0;i<mixedChattings.size();i++){
								 MixedChatting mixedChatting = mixedChattings.get(i);
								 long time = 0;
								 if(mixedChatting.isGroup()){
									 time = mixedChatting.getGroup().getLastSendDate();
								 }else{
									 time = mixedChatting.getDchat().getLastTime();
								 }
								 if(time<=helpDChat.getLastTime()){
									 index= i;
									 break;
								 }
							}
							if(index!=-1){
								mixedChattings.add(index, MixedChatting.build(helpDChat));
							}
						}
					}
					for (MessagingListener l : getListeners(null)) {
			    		l.listMixedChattingsFinished(account, mixedChattings);
			    		 l.getTotalMsgUnreadCountSuccess(account,totalCount);
			        }
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
	}
	/**
	 * 同步群组列表
	 *
	 * @Description:
	 * @param account
	 * @param listener
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date: 2014-10-24
	 */
	public void syncGroups(final Account account,
			final MessagingListener listener) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					LocalStore localStore = account.getLocalStore();
					List<CGroup> cGroups = Protocol.getInstance().getCGroups(
							account.getEmail());
					if (cGroups != null && cGroups.size() >= 0) {
						// 做列表和本地同步处理
						List<CGroup> localGroups = localStore.getAllCGroups();
						HashSet<String> remoteCGroupUids = new HashSet<>();
						HashSet<String> localUids = new HashSet<>();
						if (localGroups != null && localGroups.size() > 0) {
							for (CGroup localUid : localGroups) {
								localUids.add(localUid.getUid());
							}
						}
						for (CGroup cGroup : cGroups) {
							// 本地没有，服务器有,存起来
							if (!localUids.contains(cGroup.getUid())) {
								CGroupMember cGroupMember = cGroup.getMembers()
										.get(0);
								cGroupMember.setNickName(localStore
										.getNickNameByEmail(cGroupMember
												.getEmail()));
								localStore.saveOrUpdateGroupMember(
										cGroup.getUid(), cGroupMember);
								// 如果不是创建者,防止获取群消息时没有自己
								if (!cGroupMember.getEmail().equals(
										account.getEmail())) {
									CGroupMember member = new CGroupMember();
									member.setUid(EncryptUtil.getMd5(account
											.getEmail()));
									member.setEmail(account.getEmail());
									member.setNickName(localStore
											.getNickNameByEmail(account
													.getEmail()));
									member.setAdmin(false);
									member.setInviteMember(false);
									localStore.saveOrUpdateGroupMember(
											cGroup.getUid(), member);
								}
								localStore.saveOrUpdateCGroup(cGroup);
							}
							remoteCGroupUids.add(cGroup.getUid());
						}
						subscribeCGroup(account, cGroups);
						// 服务器上不存在，本地存在的，本地删除该localCGroupUid
						for (CGroup localUid : localGroups) {
							if (remoteCGroupUids.size() >=0
									&& !remoteCGroupUids.contains(localUid
											.getUid())) {
								localStore.deleteCGroup(localUid.getUid());
							}
						}
					} else {
						for (CGroup cGroup : cGroups) {
							CGroupMember cGroupMember = cGroup.getMembers()
									.get(0);
							cGroupMember.setNickName(localStore
									.getNickNameByEmail(cGroupMember.getEmail()));
							localStore.saveOrUpdateGroupMember(cGroup.getUid(),
									cGroupMember);
							// 如果不是创建者,防止获取群消息时没有自己
							if (!cGroupMember.getEmail().equals(
									account.getEmail())) {
								CGroupMember member = new CGroupMember();
								member.setUid(EncryptUtil.getMd5(account
										.getEmail()));
								member.setEmail(account.getEmail());
								member.setNickName(localStore
										.getNickNameByEmail(account.getEmail()));
								member.setAdmin(false);
								member.setInviteMember(false);
								localStore.saveOrUpdateGroupMember(
										cGroup.getUid(), member);
							}
							localStore.saveOrUpdateCGroup(cGroup);
						}
						subscribeCGroup(account, cGroups);
					}
				} catch (MessageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	/**
	 * 获取群消息
	 * 
	 * @Description:
	 * @param CMessage
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-17
	 */
	public void listCMessage(final Account account,final String groupUid,final String lastId ,final int count ,final MessagingListener listener,final boolean isSyncLocal)  {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					LocalStore localStore = account.getLocalStore();
					List<CMessage> cMessages =  localStore.getCMessages(groupUid, lastId, count);
					if (isSyncLocal) {
						// 用于获取群组完成更新所有群成员头像，昵称后，重新更新有到头像，昵称的消息
						for (MessagingListener l : getListeners(null)) {
							l.listNikeNameAndAvatarCMessagesFinished(account,
									cMessages);
						}
					} else {
						for (MessagingListener l : getListeners(null)) {
							l.listCMessagesFinished(account, cMessages);
						}
					}
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 获取群当前登陆账户群邀请消息，并订阅该群
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-24
	 */
	public void getGroupInvitation(final Account account,final MessagingListener listener,final boolean isSeniorSet)  {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					if(account.isGetGroupInvitationSuccess()){
						Log.i(MailChat.LOG_COLLECTOR_TAG, "-------------邮箱使用判断调用成功-------------");
						return;
					}
					Log.i(MailChat.LOG_COLLECTOR_TAG, "-------------邮箱使用判断调用了-------------");
					List<PendingMQTTConmmand> pendingMQTTConmmands=null;
					if(isSeniorSet){
						for (MessagingListener l : getListeners(null)) {
							 l.advancedLoggingSuccess(account.getEmail());
				        }
						URI imapUri =new URI(account.getStoreUri());
						URI smtpUri =new URI(account.getTransportUri());
						String imapScheme = imapUri.getScheme();
						int imapSafety =-1;
						if (imapScheme.equals("imap")) {
							imapSafety =0;
						} else if (imapScheme.startsWith("imap+tls")) {
							imapSafety =1;
						} else if (imapScheme.startsWith("imap+ssl")) {
							imapSafety =2;
						} else {
//							throw new IllegalArgumentException(
//									"Unsupported protocol (" + imapScheme + ")");
						}
						String smtpScheme = smtpUri.getScheme();
						int smtpSafety =-1;
						if (smtpScheme.equals("smtp")) {
							smtpSafety =0;
						} else if (smtpScheme.startsWith("smtp+tls")) {
							smtpSafety =1;
						} else if (smtpScheme.startsWith("smtp+ssl")) {
							smtpSafety =2;
						} else {
//							throw new IllegalArgumentException(
//									"Unsupported protocol (" + smtpScheme + ")");
						}
						String imapSet =imapUri.getHost()+"|"+imapUri.getPort()+"|"+imapSafety;
						String smtpSet =smtpUri.getHost()+"|"+smtpUri.getPort()+"|"+smtpSafety;
						pendingMQTTConmmands= Protocol.getInstance().getGroupInvitation(account.getEmail(),imapSet,smtpSet);
					}else{
						pendingMQTTConmmands= Protocol.getInstance().getGroupInvitation(account.getEmail());
					}

					//更新标记
					account.setGetGroupInvitationSuccess(true);
					account.save(Preferences.getPreferences(mApplication));

					if(pendingMQTTConmmands.size()>0&&pendingMQTTConmmands!=null){
						for(PendingMQTTConmmand pendingMQTTConmmand :pendingMQTTConmmands){
							if(connection.getClient().getClientHandle()!=null){
								connection.getClient().subscribe(pendingMQTTConmmand.getTopic(), MQTT_QOS, null, 
										new ActionListener(ActionListener.Action.SUBSCRIBE,MessagingController.this,account,MQTTCommand.JOIN_GROUP,pendingMQTTConmmand));
								try {
									Thread.sleep(50);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}						
						}
					}
				} catch (MessageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});	
	}
	/**
	 * 
	 * method name: setDChatNewMsgAlertSuccess 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account
	 *      @param dChatUid
	 *      @param isCheck
	 *      @param listener    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-10-23 下午8:52:00	@Modified by：zhangjx
	 *     @Description：设置单聊新消息提醒
	 */
	public void setDChatNewMsgAlert(final Account account, String dChatUid,
			boolean isSticked, MessagingListener listener) {
		try {
			LocalStore localStore = account.getLocalStore();
			localStore.updateDchatAlert(dChatUid, isSticked);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * method name: listDMessages 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account
	 *      @param dChatUid
	 *      @param count
	 *      @param listener    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-10-23 下午8:47:36	@Modified by：zhangjx
	 *     @Description： 设置单聊置顶
	 */
	public void setDChatStickMsgTop(Account account,
			String dChatUid, boolean isSticked,
			MessagingListener listener) {
		try {
			LocalStore localStore = account.getLocalStore();
			localStore.updateDchatSticked(dChatUid, isSticked);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * method name: deleteDChat 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account
	 *      @param dChatUid
	 *      @param isCheck
	 *      @param listener    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-10-23 下午8:53:35	@Modified by：zhangjx
	 *     @Description：删除单聊
	 */
	public void deleteDChat(Account account, String dChatUid,
			MessagingListener listener) {
		try {
			LocalStore localStore = account.getLocalStore();
			localStore.deleteDchatFlag(dChatUid);
			for (MessagingListener l : getListeners(null)) {
				l.deleteDChatSuccess(account, dChatUid);
	        }
		} catch (MessagingException e) {
			e.printStackTrace();
			for (MessagingListener l : getListeners(null)) {
				l.deleteDChatFail(account, dChatUid);
	        }
		}
	}
	public void getDChat(final Account account,final String dChatUid ,final MessagingListener listener) {
		threadPool.execute(new Runnable() {
			
			@Override
			public void run() {
				DChat dchat=null;
				try {
					LocalStore localStore = account.getLocalStore();
					 dchat=localStore.getDChat(dChatUid);
						for (MessagingListener l : getListeners(null)) {
							 l.getDChatSuccess(account, dchat);
				        }
				
				} catch (MessagingException e) {
					e.printStackTrace();
					for (MessagingListener l : getListeners(null)) {
						 l.getDChatFail(account,dchat);
			        }
				}
			}
		});
	}

	/** 
	 * method name: updateDChatReadState 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account
	 *      @param dChatUid
	 *      @param object    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-10-24 下午1:30:23	@Modified by：zhangjx
	 *     @Description：更新未读数
	 */
	public void updateDChatReadState(Account account, String dChatUid,int untreatedCount,
			MessagingListener listener) {
		try {
			LocalStore localStore = account.getLocalStore();
			localStore.updateDchatUntreatedCount(dChatUid,untreatedCount);
			getTotalMsgUnreadCount(account, null);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 标记所有单聊会话无未读消息
	 */
	public void markAllSingleConversationNoUnread ( Account account ){
		try {
			LocalStore localStore = account.getLocalStore();
			localStore.updateDchatUntreatedCount(null,0);
			getTotalMsgUnreadCount(account, null);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 标记所有群聊会话无未读消息
	 */
	public void markAllGroupConersationNoUnread ( Account account ){
		try {
			LocalStore localStore =account.getLocalStore();
			localStore.updateCGroupUntreatedCount(null, 0);
			getTotalMsgUnreadCount(account, null);
		} catch ( MessagingException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 标记所有的会话无未读消息
	 */
	public void markAllConversationNoUnread (Account account){
		markAllSingleConversationNoUnread(account);
		markAllGroupConersationNoUnread(account);
	}
	
	/**
	 * 
	 * method name: getTotalMsgUnreadCount 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account
	 *      @param listener    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-10-27 下午1:15:16	@Modified by：zhangjx
	 *     @Description：获取混合消息列表所有未读消息数
	 */
	public void getTotalMsgUnreadCount(Account account,MessagingListener listener) {
		try {
			LocalStore localStore = account.getLocalStore();
			int totalCount = localStore.getMixedUnreadCount();
			for (MessagingListener l : getListeners(null)) {
				l.getTotalMsgUnreadCountSuccess(account, totalCount);
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 群成员退出群组
	 *备注:当所有账户中,该群加入数为1时,退订连接MQTT频道并且删除当前账户本地及HTTP服务端数据；
	 *		    当所有账户中,该群加入数大于1时,删除当前账户本地及HTTP服务端数据；
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-30
	 */
	public void leaveCGroup(final Account account, final CGroup cGroup,
			final MessagingListener listener) {
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				try {
					Protocol.getInstance().leaveCGroup(account.getEmail(),
							cGroup.getUid());
					account.getLocalStore().deleteCGroup(cGroup.getUid());
					for (MessagingListener l : getListeners(null)) {
						l.leaveCGroupSuccess(account);
					}
					// 通知其他群成员，自己退出了群
					List<CGroupMember> cGroupMembers = cGroup.getMembers();
					if (connection.getClient().getClientHandle() != null) {
						String messageUid =UUID.randomUUID().toString();
						for (CGroupMember cGroupMember : cGroupMembers) {
							if (!cGroupMember.getEmail().equals(account.getEmail())) {
								String topic = cGroupMember.getEmail()+"/s";
								String leaveCGroupMessage =CreateJson.leaveCGroupMessage(account.getEmail(),cGroup.getUid(),messageUid).toString();
								PendingMQTTConmmand pendingMQTTConmmand =new PendingMQTTConmmand(-1,ActionListener.Action.PUBLISH,MQTTCommand.MEMBER_LEAVE_GROUP,topic,leaveCGroupMessage);
								connection.getClient().publish(topic,leaveCGroupMessage.getBytes(),MQTT_QOS, false, null, 
										new ActionListener(ActionListener.Action.PUBLISH,MessagingController.this,account,MQTTCommand.MEMBER_LEAVE_GROUP,pendingMQTTConmmand));
								try {
									Thread.sleep(50);//休眠0.05秒，防止过快引起发送失败问题
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
					Preferences prefs = Preferences.getPreferences(mApplication);
					List<Account> accounts = prefs.getAccounts();
					int count = 0;
					for (Account localdaccount : accounts) {
						// 查看所有账户中,加入到该群的账户数,如果等于1,直接将该连接中订阅的群频道退订
						if (localdaccount.getLocalStore().getCGroup(
								cGroup.getUid()) != null) {
							count++;
						}
					}
					if (count == 1&& connection.getClient().getClientHandle() != null) {
						PendingMQTTConmmand pendingMQTTConmmand =new PendingMQTTConmmand(-1,ActionListener.Action.UNSUBSCRIBE,MQTTCommand.MEMBER_LEAVE_GROUP,cGroup.getUid(),null);
						connection.getClient().unsubscribe(cGroup.getUid(),null,
								new ActionListener(ActionListener.Action.UNSUBSCRIBE,MessagingController.this,account,MQTTCommand.MEMBER_LEAVE_GROUP,pendingMQTTConmmand));
					}
				} catch (MessageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					for (MessagingListener l : getListeners(null)) {
						l.leaveCGroupFail(account);
					}
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 群管理员删除群组
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-30
	 */
	public void deleteCGroup(final Account account,final CGroup cGroup,final MessagingListener listener){
		
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Protocol.getInstance().deleteCGroup(account.getEmail(),cGroup.getUid());
					account.getLocalStore().deleteCGroup(cGroup.getUid());
					for (MessagingListener l : getListeners(null)) {
						l.deleteCGroupSuccess(account);
			        }
					List<CGroupMember> cGroupMembers =cGroup.getMembers();
					if(	connection.getClient().getClientHandle()!=null){
						String messageUid =UUID.randomUUID().toString();
						for(CGroupMember cGroupMember :cGroupMembers){
							//TODO:给群管理员自己自己也发送，删除群的系统消息，该判定去掉。解决多客户端，群没退出问题。
							if(!cGroupMember.getEmail().equals(account.getEmail())){
								String topic = cGroupMember.getEmail()+"/s";
								String deleteCGroupMessage =CreateJson.deleteCGroupMessage(account.getEmail(),cGroup.getUid(),messageUid).toString();
								PendingMQTTConmmand pendingMQTTConmmand =new PendingMQTTConmmand(-1,ActionListener.Action.PUBLISH,MQTTCommand.ADMIN_DELETE_GROUP,topic,deleteCGroupMessage);
								connection.getClient().publish(topic,deleteCGroupMessage.getBytes(), MQTT_QOS, false, null,
										new ActionListener(ActionListener.Action.PUBLISH,MessagingController.this, account, MQTTCommand.ADMIN_DELETE_GROUP, pendingMQTTConmmand));
								try {
									Thread.sleep(50);//休眠0.05秒，防止过快引起发送失败问题
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
					if(connection.getClient().getClientHandle()!=null&&cGroup.getMembers()!=null&&cGroup.getMembers().size()>0){
						PendingMQTTConmmand pendingMQTTConmmand =new PendingMQTTConmmand(-1,ActionListener.Action.UNSUBSCRIBE,MQTTCommand.ADMIN_DELETE_GROUP,cGroup.getUid(),null);
						connection.getClient().unsubscribe(
								cGroup.getUid(),
								null,
								new ActionListener(
										ActionListener.Action.UNSUBSCRIBE,
										MessagingController.this, account, MQTTCommand.ADMIN_DELETE_GROUP, pendingMQTTConmmand));
					}
				} catch (MessageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					for (MessagingListener l : getListeners(null)) {
						l.deleteCGroupFail(account);
			        }
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnavailableStorageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
			}
		});
	}
	
	/**
	 * 群组置顶\取消置顶操作
	 * 
	 * method name: stickGroup function @Description: TODO Parameters and return
	 * values description：
	 * 
	 * @param account
	 * @param cChatUid
	 * @param isSticked
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-10-30 下午5:01:18 @Modified by：zhangyq
	 * @Description：
	 */
	public void stickGroup(Account account, String groupUid, boolean isSticked,
			MessagingListener listener) {
		try {
			LocalStore localStore = account.getLocalStore();
			localStore.stickGroup(groupUid, isSticked);
		} catch (MessagingException e) {
			Log.e(MailChat.LOG_TAG, "MessagingController stickGroup fail.");
		}
	}

	/**
	 * 
	 * method name: getTotalMsgUnreadCount function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param account
	 * @param listener
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-10-30 下午5:30:10 @Modified by：zhangjx
	 * @Description：全局搜索
	 */
	public void searchGlobalData(final Account account, final String keyWord,
			final String searchFlag) {
		threadPool.execute(new Runnable() {
			private ArrayList<ContactAttribute> tempContactAttributes;

			@Override
			public void run() {
				try {
					LocalStore localStore = account.getLocalStore();

					if (searchFlag.equals("chat")) {
						List<MixedChatting> mixedChatting = localStore
								.searchMixedChatting(keyWord);
						for (MessagingListener l : getListeners(null)) {
							l.globalChatSearchSuccess(account, mixedChatting);
						}
					} else if (searchFlag.equals("contant")) {
						List<ContactAttribute> searchContants = localStore
								.searchContacts(keyWord,false);
						tempContactAttributes = new ArrayList<ContactAttribute>();
						if (searchContants.size() > 0) {
							tempContactAttributes.addAll(searchContants);
							Collections.sort(tempContactAttributes,
									new ContactComparator());
						}
						for (MessagingListener l : getListeners(null)) {
							l.globalContantSearchSuccess(account,
									tempContactAttributes);
						}
					} else {

					}
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 群消息提醒
	 * 
	 * method name: setDChatNewMsgAlert 
	 * function @Description: TODO
	 * Parameters and return values description：
	 * @param account
	 * @param dChatUid
	 * @param isSticked
	 * @param listener    field_name
	 * void    return type
	 * @History memory：
	 * @Date：2014-10-30 下午5:44:36	@Modified by：zhangyq
	 * @Description：
	 */
	public void msgAlertGroup(Account account, String groupUid, boolean isAlert) {
		try {
			LocalStore localStore = account.getLocalStore();
			localStore.msgAlertGroup(groupUid, isAlert);
		} catch (MessagingException e) {
			Log.e(MailChat.LOG_TAG, "MessagingController msgAlertGroup fail.");
			e.printStackTrace();
		}
	}

	/**
	 * 更新未读数
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-30
	 */
	public void updateCGroupUntreatedCount(Account account,CGroup cGroup,int untreatedCount,MessagingListener listener){
		try {
			LocalStore localStore =account.getLocalStore();
			localStore.updateCGroupUntreatedCount(cGroup.getUid(), untreatedCount);
			getTotalMsgUnreadCount(account, null);
		} catch ( MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取成员列表
	 * 
	 * method name: listMembers 
	 * function @Description: TODO
	 * Parameters and return values description：
	 * @param account
	 * @param group
	 * @param limit
	 * @param listener    field_name
	 * void    return type
	 * @History memory：
	 * @Date：2014-11-5 下午7:46:26	@Modified by：zhangyq
	 * @Description：
	 */
	public void listMembers(Account account, CGroup group, MessagingListener listener) {
		try {
			LocalStore localStore =account.getLocalStore();
			List<CGroupMember> members = localStore.getCMembers(group.getUid());
			for (MessagingListener l : getListeners(listener)) {
				l.listMembersSuccess(account,members);
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 移除群成员
	 * 
	 * method name: deleteGroupMember 
	 * function @Description: TODO
	 * Parameters and return values description：
	 * @param account
	 * @param groupUid
	 * @param member
	 * @param listener    field_name
	 * void    return type
	 * @History memory：
	 * @Date：2014-11-5 下午5:22:55	@Modified by：zhangyq
	 * @Description：
	 */
	public void deleteGroupMember(final Account account, final String groupUid,
			final List<CGroupMember> mGroupMembers,final int position,final MessagingListener listener) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				CGroupMember member = mGroupMembers.get(position);
				try {
					Protocol.getInstance().deleteMembers(account.getEmail(), groupUid, member.getEmail());
					LocalStore localStore = account.getLocalStore();
					localStore.deleteCGroupCmember(groupUid,new String[]{ member.getUid()});	
					for (MessagingListener l : getListeners(listener)) {
						l.delGroupMemberSuccess(account,member);
					}
					if(connection.getClient().getClientHandle()!=null){
						//构造移除群成员,提示消息
						CMessage cMessage =new CMessage(Type.NOTIFICATION);
						cMessage.setUid(UUID.randomUUID().toString());
						cMessage.setSendTime(System.currentTimeMillis());
						String font = TimeUtils.DateFormatMDHM.format(new Date(cMessage.getSendTime()));
						cMessage.setContent(String.format(mApplication.getString(R.string.chat_notify_delete_invitation_item),localStore.getNickNameByEmail(account.getEmail()),font,member.getNickName()));
						cMessage.setGroupUid(groupUid);
						CGroupMember cmember =new CGroupMember();
						cmember.setUid(EncryptUtil.getMd5(account.getEmail()));
						cmember.setEmail(account.getEmail());
						cmember.setAdmin(localStore.getCGroup(groupUid).getIsAdmin());
						cMessage.setMember(cmember);
						String cNotifyMessage=CreateJson.createCmessage(cMessage).toString();
						connection.getClient().publish(groupUid,  cNotifyMessage.getBytes(), MQTT_QOS, false, null, 
								new ActionListener(ActionListener.Action.PUBLISH,MessagingController.this,
								account,MQTTCommand.ADMIN_KICKED_OUT_MEMBER,
								new PendingMQTTConmmand(-1,ActionListener.Action.PUBLISH,MQTTCommand.ADMIN_KICKED_OUT_MEMBER,groupUid,cNotifyMessage)));
						localStore.saveOrUpdateCMessageAndCAttach(groupUid, cMessage);
						localStore.updateCgroupLastCmessage(groupUid, cMessage);
						for (MessagingListener l : getListeners(null)) {
			 	    		l.cMessageArrived(account, cMessage);
			 	        }
						//发送系统消息让该群成员离开群组
						PendingMQTTConmmand pendingMQTTConmmand=null;
						String topic = member.getEmail()+"/s";
						String deleteCGroupMessage =CreateJson.kickedOutMemberMessage(account.getEmail(),groupUid,UUID.randomUUID().toString()).toString();
						pendingMQTTConmmand = new PendingMQTTConmmand(-1,ActionListener.Action.PUBLISH,MQTTCommand.ADMIN_KICKED_OUT_MEMBER,topic,deleteCGroupMessage);
						connection.getClient().publish(topic,deleteCGroupMessage.getBytes(), MQTT_QOS, false, null,
								new ActionListener(ActionListener.Action.PUBLISH,MessagingController.this, account, MQTTCommand.ADMIN_KICKED_OUT_MEMBER, pendingMQTTConmmand));
						//通知其他群成员删除该群成员(也可以在获服务端取群成员信息时，做同步处理，等群成员做了人数限制后，这样就做到双重保障)
						String deleteMemberMessageUid = UUID.randomUUID().toString();
						for(CGroupMember cGroupMember :mGroupMembers){
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							String deleteMemberMessage =CreateJson.kickedOutByAntherMemberMessage(account.getEmail(), groupUid, member.getEmail(), deleteMemberMessageUid).toString();
							pendingMQTTConmmand =new PendingMQTTConmmand(-1,ActionListener.Action.PUBLISH,MQTTCommand.ADMIN_KICKED_OUT_MEMBER,topic,deleteMemberMessage);
							connection.getClient().publish(cGroupMember.getEmail()+"/s",deleteMemberMessage.getBytes(), MQTT_QOS, false, null,
									new ActionListener(ActionListener.Action.PUBLISH,MessagingController.this, account, MQTTCommand.ADMIN_KICKED_OUT_MEMBER, pendingMQTTConmmand));
						}
					}
				} catch (MessageException e) {
					e.printStackTrace();
					for (MessagingListener l : getListeners(listener)) {
						l.delGroupMemberFailed(account,member);
					}
				} catch (MessagingException e) {
					e.printStackTrace();
					for (MessagingListener l : getListeners(listener)) {
						l.delGroupMemberFailed(account,member);
					}
				} catch (MqttPersistenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 增加群成员
	 * 
	 * method name: deleteGroupMember 
	 * function @Description: TODO
	 * Parameters and return values description：
	 * @param account
	 * @param groupUid
	 * @param member
	 * @param listener    field_name
	 * void    return type
	 * @History memory：
	 * @Date：2014-11-5 下午5:22:55	@Modified by：zhangyq
	 * @Description：
	 */
	public void addGroupMember(final Account account, final String groupUid,
			final List<CGroupMember> memberList, final MessagingListener listener) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				LocalStore localStore;
				try {
					localStore = account.getLocalStore();
					String membersStr = null;
					for (CGroupMember cGroupMember : memberList) {
						if (membersStr == null) {
							membersStr = cGroupMember.getEmail();
						} else {
							membersStr += "," + cGroupMember.getEmail();
						}
						//添加联系人
						ContactAttribute contactAttribute =localStore.getContactAttribute(cGroupMember.getEmail(),false);
						if(contactAttribute==null){
							//存储联系人
							localStore.savaContact(cGroupMember.getEmail(), cGroupMember.getEmail().substring(0, cGroupMember.getEmail().indexOf("@")),false);
							ContactAttribute contact=addTempContact(cGroupMember.getEmail(),null);
							tempContactList.add(contact);
						}else{
							//更新联系人使用次数
							localStore.updateContactAttributeReceiveCount(cGroupMember.getEmail(), contactAttribute.getReceiveCount()+1);
						}
					}
					reflashContactList(account,tempContactList);
					Protocol.getInstance().addMembers(account.getEmail(), groupUid, membersStr,((MailChat) mApplication).getLanguage());		
					//获取当前群组的所有群成员,发送系统邀请提示消息
					if(connection.getClient().getClientHandle()!=null){
						String cGroupName=null;
						for (int j = 0; j < memberList.size(); j++) {
							CGroupMember cGroupMember =	memberList.get(j);
							if(cGroupName==null){
								cGroupName =cGroupMember.getNickName();
							}else{
								cGroupName+=" , "+cGroupMember.getNickName();
							}
							if(j>3){
								cGroupName	=cGroupName+" .... ";
								break;
							}
						}
						//构造添加群成员,提示消息
						CMessage cMessage =new CMessage(Type.NOTIFICATION);
						cMessage.setUid(UUID.randomUUID().toString());
						cMessage.setSendTime(System.currentTimeMillis());
						String font = TimeUtils.DateFormatMDHM.format(new Date(cMessage.getSendTime()));
						cMessage.setContent(String.format(mApplication.getString(R.string.chat_notify_add_invitation_item),localStore.getNickNameByEmail(account.getEmail()),font,cGroupName));
						cMessage.setGroupUid(groupUid);
						CGroupMember member =new CGroupMember();
						member.setUid(EncryptUtil.getMd5(account.getEmail()));
						member.setEmail(account.getEmail());
						member.setAdmin(localStore.getCGroup(groupUid).getIsAdmin());
						cMessage.setMember(member);
						String createCmessage= CreateJson.createCmessage(cMessage).toString();
						connection.getClient().publish(groupUid, createCmessage.getBytes(), MQTT_QOS, false, null, new ActionListener(ActionListener.Action.PUBLISH,MessagingController.this,account,
								null,new PendingMQTTConmmand(-1,ActionListener.Action.PUBLISH,MQTTCommand.SEND_INVITATION,groupUid,createCmessage)));
						localStore.saveOrUpdateCMessageAndCAttach(groupUid, cMessage);
						localStore.updateCgroupLastCmessage(groupUid, cMessage);
						for (MessagingListener l : getListeners(null)) {
			 	    		l.cMessageArrived(account, cMessage);
			 	        }
					}
					for (CGroupMember cGroupMember : memberList) {
						// 构造群成员所需信息
						cGroupMember.setUid(EncryptUtil.getMd5(cGroupMember.getEmail()));
						cGroupMember.setAdmin(false);
						cGroupMember.setInviteMember(true);
						cGroupMember.setFirstChar(cGroupMember.getEmail().charAt(0));
						cGroupMember.setNickName(cGroupMember.getEmail().substring(0, cGroupMember.getEmail().indexOf("@")));
					}
					localStore.saveOrUpdateGroupMembers(groupUid, memberList);
				
					if(connection.getClient().getClientHandle()!=null){
						String messageUid=UUID.randomUUID().toString();
						for(CGroupMember cGroupMember : memberList){
							String topic =cGroupMember.getEmail()+"/s";
							String invitationMessage =CreateJson.invitationMessage(account.getEmail(),cGroupMember.getEmail(),groupUid,messageUid).toString();
							PendingMQTTConmmand pendingMQTTConmmand =new PendingMQTTConmmand(-1,ActionListener.Action.PUBLISH,MQTTCommand.SEND_INVITATION,topic,invitationMessage);
							connection.getClient().publish(topic,invitationMessage.getBytes(), MQTT_QOS, false, null, new ActionListener(ActionListener.Action.PUBLISH,MessagingController.this,account,MQTTCommand.SEND_INVITATION,pendingMQTTConmmand));
							try {
								Thread.sleep(50);//休眠0.05秒，防止过快引起发送失败问题
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
			
					for (MessagingListener l : getListeners(listener)) {
						l.addGroupMemberSuccess(account,groupUid,memberList);
					}
				} catch (MessageException e) {
					e.printStackTrace();
					for (MessagingListener l : getListeners(listener)) {
						l.addGroupMemberFailed(account,memberList);
					}
				} catch (MessagingException e) {
					e.printStackTrace();
					for (MessagingListener l : getListeners(listener)) {
						l.addGroupMemberFailed(account,memberList);
					}
				} catch (MqttPersistenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 异步获取服务端群组信息
	 * 
	 * method name: getGroupWithMembers 
	 * function @Description: TODO
	 * Parameters and return values description：
	 * @param account
	 * @param groupUid
	 * @param callback
	 * @param sync    field_name
	 * void    return type
	 * @History memory：
	 * @Date：2014-11-5 下午7:49:39	@Modified by：zhangyq
	 * @Description：
	 */
	public void getGroupWithMembers(final Account account,
			final String groupUid, final MessagingListener listener) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					LocalStore localStore =account.getLocalStore();
					CGroup group = Protocol.getInstance().getGroup(account.getEmail(), groupUid);
					List<CGroupMember>  members = group.getMembers();
					localStore.saveOrUpdateGroupMembers(groupUid, members);
					for (MessagingListener l: getListeners(listener)) {
						l.getGroupInfoSuccess(account,group);
					}
				} catch (MessagingException e) {
					e.printStackTrace();
				} catch (MessageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}



	/**
	 * 
	 * method name: deleteDMessage function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param account
	 * @param messageUid
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-11-5 上午11:44:47 @Modified by：zhangjx
	 * @Description：删除指定单聊消息
	 */
	public void deleteDMessage(final Account account, final String messageUid) {
		try {
			account.getLocalStore().deleteDchatMessageFlag(messageUid);
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * method name: updateDchat function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param account
	 * @param dChat
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-11-5 上午11:46:39 @Modified by：zhangjx
	 * @Description：更新单聊列表
	 */
	public void updateDchat(final Account account, final DChat dChat,
			boolean isLastContentToEmpty) {
		try {
			account.getLocalStore().updateDchat(dChat, -1, isLastContentToEmpty);
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * method name: deleteGroupMessage function @Description: TODO Parameters
	 * and return values description：
	 * 
	 * @param mAccount
	 * @param uid
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-11-5 下午1:42:25 @Modified by：zhangjx
	 * @Description：删除群聊信息
	 */
	public void deleteGroupMessage(final Account account,
			final String messageUid) {
		try {
			account.getLocalStore().updateCMessageDeleteFlag(messageUid, 1);
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * method name: updateCgroupLastCmessage function @Description: TODO
	 * Parameters and return values description：
	 * 
	 * @param mAccount
	 * @param uid
	 * @param cMessage
	 *            field_name void return type
	 * @History memory：
	 * @Date：2014-11-5 下午1:43:28 @Modified by：zhangjx
	 * @Description：更新群聊列表
	 */
	public void updateCgroupLastCmessage(Account account,
			String groupUid, CMessage message) {
		try {
			account.getLocalStore().updateCgroupLastCmessage(groupUid, message);
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * method name: listCMessage 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account
	 *      @param groupUid
	 *      @param lastMessageUid
	 *      @param expectCount    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-11-5 下午5:08:23	@Modified by：zhangjx
	 *     @Description：获取指定群的消息列表
	 */
	public void listCMessage(final Account account, final String groupUid,
			final String lastMessageUid, final int expectCount) {
		threadPool.execute(new Runnable() {

			public void run() {
				try {
				LocalStore localStore = account.getLocalStore();
					List<CMessage> localMessages = localStore.getCMessages(groupUid, lastMessageUid, expectCount);
					List<CMessage> temp = new ArrayList<CMessage>();
					if (localMessages != null) {
						for (CMessage message : localMessages) {
							Type messageType = message.getMessageType();
							if (messageType == Type.VOICE
									|| messageType == Type.IMAGE
									|| messageType == Type.ATTACHMENT) {
								if (message.getAttachment() == null) {
									temp.add(message);
								}
							}
						}
						localMessages.removeAll(temp);
					}
					for (MessagingListener l : getListeners(null)) {
						l.listCMessagesFinished(account, localMessages);
					}
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	/** 
	 * method name: searchDChatMessages 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param mAccount
	 *      @param dChatUid
	 *      @param currentKeyWord
	 *      @param listener    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-11-5 下午5:51:45	@Modified by：zhangjx
	 *     @Description：搜索单聊消息
	 */
	public void searchDChatMessages(final Account account, final String dChatUid,
			final String currentKeyWord, MessagingListener listener) {
		threadPool.execute(new Runnable() {

			public void run() {
				List<DChatMessage> localMessages;
				try {
					LocalStore localStore = account.getLocalStore();
					localMessages = localStore.searchDChatMessages(dChatUid, currentKeyWord);
					List<String> nickNameList =new ArrayList<String>();
					for(DChatMessage dChatMessage :localMessages){
						nickNameList.add(localStore.getNickNameByEmail(dChatMessage.getSenderEmail()));
					}
					for (MessagingListener l : getListeners(null)) {
						l.searchDChatMessagesFinished(account.getUuid(),dChatUid,currentKeyWord, localMessages,nickNameList);
					}
				} catch (UnavailableStorageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
		});
	}

	/** 
	 * method name: searchGroupMessages 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param mAccount
	 *      @param mGroupId
	 *      @param currentKeyWord
	 *      @param object    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-11-5 下午5:51:54	@Modified by：zhangjx
	 *     @Description：搜索群聊消息
	 */
	public void searchGroupMessages(final Account account,final String groupUid,
			final String currentKeyWord,final MessagingListener listener) {
		threadPool.execute(new Runnable() {

			public void run() {
				LocalStore localStore;
				try {
					localStore = account.getLocalStore();
					List<CMessage> localMessages=localStore.searchGroupMessages(groupUid, currentKeyWord);
					// 目前屏蔽掉系统通知消息的展示
					if (localMessages != null) {
						List<CMessage> temp = new ArrayList<CMessage>();
						for (CMessage message : localMessages) {
							if (message.getMessageType() == Type.NOTIFICATION) {
								temp.add(message);
							}
						}
						localMessages.removeAll(temp);
					}
					for (MessagingListener l : getListeners(null)) {
						l.searchGroupMessagesFinished(account.getUuid(),
								groupUid, currentKeyWord, localMessages);
					}
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
	}
	/**
	 * 获取群组信息
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-10-30
	 */
	public void getCGroupInfo(final Account account,final CGroup cGroup,final MessagingListener listener,final boolean isSyncMembers){
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				CGroup  group =null;	
				try {
					LocalStore localStore=account.getLocalStore();
					group = Protocol.getInstance().getGroup(account.getEmail(), cGroup.getUid());
					List<CGroupMember>  members = group.getMembers();
					localStore.UpdateCGroupNameAndAdmin(group);
					localStore.saveOrUpdateGroupMembers(cGroup.getUid(), members);
					List<CGroupMember> localMembers=null;
					if(isSyncMembers){
						//如果群成员被删除的系统消息，未收到（可能发送的客户端出现问题，再重发删除群成员的系统消息），做同步操作，保证数据不会出现问题
						localMembers=localStore.getCMembers(cGroup.getUid());
						List<CGroupMember> removeMembers =new ArrayList<CGroupMember>();
						if(localMembers.size()>members.size()){
							removeMembers.addAll(localMembers);
							removeMembers.removeAll(members);
							localMembers.removeAll(removeMembers);
							String [] memberUids =new String [removeMembers.size()];
							for (int i = 0; i < removeMembers.size(); i++) {
								memberUids[i]=removeMembers.get(i).getUid();
							}
							localStore.deleteCGroupCmember(group.getUid(),memberUids);
						}
					}
					for (MessagingListener l : getListeners(listener)) {
						 l.getGroupInfoSuccess(account, group,localMembers);
			        }
				} catch (MessageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					for (MessagingListener l : getListeners(listener)) {
						 l.getGroupInfoFail(account, cGroup.getUid());
			        }
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	/**
	 * 获取本地群组信息 
	 * 备注：由于创建群或邀请别人加入群时，还不知道他是否已经加入。但是收到成员消息时，会自动本地保存群成员。
	 * 所以需要去数据库获取群成员。供设置界面显示。
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-11-14
	 */
	public void getLocalCGroupInfo(final Account account,final CGroup cGroup,final MessagingListener listener){
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					LocalStore localStore = account.getLocalStore();
					List<CGroupMember> cGroupMembers=localStore.getCMembers(cGroup.getUid());
					CGroup updateCGroup = localStore.getCGroup(cGroup.getUid());//重新获取，防止数据库有数据更新，而导致群设置数据有问题
					String cGroupMemberName=null;
					for(CGroupMember cGroupMember :cGroupMembers){
						if(cGroupMemberName==null){
							cGroupMemberName =cGroupMember.getNickName();
						}else{
							cGroupMemberName+=" , "+cGroupMember.getNickName();
						}
					}
					updateCGroup.setMembers(cGroupMembers);
					if(cGroupMembers.size()>0){
						//获取本地
						for (MessagingListener l : getListeners(listener)) {
							 l.getLocalGroupInfoSuccess(account, updateCGroup,cGroupMemberName);
				        }
					}else{
						for (MessagingListener l : getListeners(listener)) {
							 l.getLocalGroupInfoFail(account, updateCGroup);
				        }
					}
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					for (MessagingListener l : getListeners(listener)) {
						 l.getLocalGroupInfoFail(account, cGroup);
			        }
				}
			
			}
			
		});
	}

	/**
	 * 获取本地群组信息 
	 * 备注：由于创建群或邀请别人加入群时，还不知道他是否已经加入。但是收到成员消息时，会自动本地保存群成员。
	 * 所以需要去数据库获取群成员。供设置界面显示。
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-11-14
	 */
	public void listLocalCGroupMember(final Account account,final String  cGroupUid,final MessagingListener listener){
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					LocalStore localStore = account.getLocalStore();
					List<CGroupMember> cGroupMembers=localStore.getCMembers(cGroupUid);
					//TODO:群成员昵称设置，后期优化
					String cGroupMemberName=null;
					for(CGroupMember cGroupMember :cGroupMembers){
						String nickName =localStore.getNickNameByEmail(cGroupMember.getEmail());
						cGroupMember.setNickName(nickName);
						if(cGroupMemberName==null){
							cGroupMemberName =cGroupMember.getNickName();
						}else{
							cGroupMemberName+=" , "+cGroupMember.getNickName();
						}
					}
					if(cGroupMembers.size()>0){
						//获取本地
						for (MessagingListener l : getListeners(null)) {
							l.listLocalGroupMemberSuccess(account, cGroupUid,cGroupMemberName,cGroupMembers);
				        }
					}else{
						for (MessagingListener l : getListeners(listener)) {
							 
				        }
					}
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					for (MessagingListener l : getListeners(listener)) {
						
			        }
				}
			
			}
			
		});
	}

	/** 
	 * method name: updateGroupName 
	 * function @Description: TODO
	 * Parameters and return values description：
	 *      @param account
	 *      @param mGroup
	 *      @param object    field_name
	 *      void    return type
	 *  @History memory：
	 *     @Date：2014-11-6 下午7:58:36	@Modified by：zhangjx
	 *     @Description：修改群名称
	 */
	public void updateGroupName(final Account account,final CGroup group,
			final MessagingListener listener) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					// 1.update to server
					Protocol.getInstance().updateGroupName(account.getEmail(),group);
					List<CGroupMember>  cGroupMembers = group.getMembers();
					if(cGroupMembers.size()>0&&connection.getClient().getClientHandle()!=null){
						String messageUid=UUID.randomUUID().toString();
						for(CGroupMember cGroupMember : cGroupMembers){
							String topic =cGroupMember.getEmail()+"/s";
							String cGroupReNameMessage =CreateJson.cGroupReNameMessage(account.getEmail(),group.getUid(),group.getGroupName(),messageUid).toString();
							connection.getClient().publish(topic, cGroupReNameMessage.getBytes(), MQTT_QOS, false, null, 
									new ActionListener(ActionListener.Action.PUBLISH,MessagingController.this,account,MQTTCommand.RE_GROUPNAME,
											new PendingMQTTConmmand(-1, ActionListener.Action.PUBLISH, MQTTCommand.RE_GROUPNAME, topic, cGroupReNameMessage)));
						}
					}
					// 2. update local TODO:
					group.setReName(true);
					account.getLocalStore().UpdateCGroupName(group);	
					for (MessagingListener l : getListeners(listener)) {
						l.changeGroupSuccess(account,group);
					}
				} catch (Exception e) {
					for (MessagingListener l : getListeners(listener)) {
						l.changeGroupFailed(account,group, e.toString());
					}
				}
			}
		});
	}
	/**
	 * 收到群名称修改的系统消息,修改本地群名称
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-24
	 */
	public void updateLocalGroupName(final Account account,final String groupId,final String reName) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				CGroup group=null;
				try {
					LocalStore localStore =account.getLocalStore();
					group =localStore.getCGroup(groupId);
					if(group!=null){
						group.setReName(true);
						group.setGroupName(reName);
						account.getLocalStore().UpdateCGroupName(group);	
						for (MessagingListener l : getListeners(null)) {
							l.changeGroupSuccess(account,group);
						}
					}
				} catch (Exception e) {
					for (MessagingListener l : getListeners(null)) {
						l.changeGroupFailed(account,group, e.toString());
					}
				}
			}
		});
	}
	/**
	 * 收到群主删除群或者被移除群，收到消息,退订该连接群频道
	 *备注:当所有账户中,只有1个账户在该群时,才退订该订阅.(退订成功然后删除当前账户本地群)
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-11-10
	 */
	public void leaveAndUnsubscribeGroupByMember(final Account account,final String subscribeGroupId,boolean isDeleteCGroup) {
		leaveGroupByMember(account,subscribeGroupId,isDeleteCGroup);
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					if (connection.getClient().getClientHandle() != null) {
							connection.getClient().unsubscribe(subscribeGroupId, null, new ActionListener(
									ActionListener.Action.UNSUBSCRIBE,
									MessagingController.this, account,
									MQTTCommand.LEAVE_GROUP, new PendingMQTTConmmand(-1,ActionListener.Action.UNSUBSCRIBE,MQTTCommand.LEAVE_GROUP,subscribeGroupId,null)));
					}
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
	}
	
	/**
	 * 收到群主删除群或者被移除群,删除本地该账户数据库记录,并且删除服务器端记录(HTTPS)
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-25
	 */
	public void leaveGroupByMember(final Account account,final String unSubscribeGroupId,final boolean isDeleteCGroup) {
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					LocalStore localStore =account.getLocalStore();
					CGroup cGroup  = localStore.getCGroup(unSubscribeGroupId);
					localStore.deleteCGroup(unSubscribeGroupId);
					if(cGroup==null){
						//这个情况很奇怪，需要具体查看聊天加群删除群成员逻辑。
						return;
					}
					if(isDeleteCGroup){
						for (MessagingListener l : getListeners(null)) {
							l.delteGroupInfoByMemberSuccess(account,cGroup);
				        }
					}else{
						for (MessagingListener l : getListeners(null)) {
							l.kickedOutGroupByMemberSuccess(account,cGroup);
				        }
					}
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	/**
	 * 下载单聊小图片
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-11-13
	 */
	public void downThumbnailImageDChat(final Account account,final DChatMessage dChatMessage,final DChat notifyDChat,final boolean isContactAttributeExist) {
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					LocalStore localStore = account.getLocalStore();
					DChatMessage.Type type =dChatMessage.getMessageType();
					DAttachment dAttachment = dChatMessage.getAttachments().get(0);
					dAttachment.setAttchmentId(UUID.randomUUID().toString());
					//dAttachment.setFilePath(thumbnailImageDirectory+savaName);
					localStore.persistDChatMessage(dChatMessage);
					localStore.persistDChatAttachment(dAttachment);
					Protocol.getInstance().downFile(dAttachment.getAttchmentId(),((MailChat) mApplication).getAttFilePath(((MailChat) mApplication).getChatThumbnailImageCacheDirectory(account), dAttachment.getAttchmentId(), dAttachment.getName()),dAttachment.getFileid(), dAttachment.getName(),true,1,new DownloadCallback() {
						@Override
						public void downloadProgress(String id,int progress,long downloaded) {
							// TODO Auto-generated method stub
						}
						@Override
						public void downloadFinished(String id) {
							// TODO Auto-generated method stub
						}
						@Override
						public void downloadFailed(String id, Exception exception) {
							// TODO Auto-generated method stub
						}
						@Override
						public void downloadInterrupt(String id) {
							// TODO Auto-generated method stub
							
						}
					});		
					for (MessagingListener l : getListeners(null)) {
						l.chatMessageArrived(account, dChatMessage);
					}
					if(notifyDChat!=null){
						NotificationCenter.getInstance().onNewSingleChattingMessage(account.getUuid(), dChatMessage, notifyDChat,isContactAttributeExist);
		 	    	} 
				} catch (MessageException e) {
					// TODO Auto-generated catch block
					for (MessagingListener l : getListeners(null)) {
						l.chatMessageArrived(account, dChatMessage);
					}
					e.printStackTrace();
				} catch (UnavailableStorageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 下载群聊小图片
	 * 
	 * method name: downFileInCChat 
	 * function @Description: TODO
	 * Parameters and return values description：
	 * @param account
	 * @param fileid
	 * @param fileName
	 * @param isthumbnail
	 * @param cMessage
	 * @param notifyCGroup    field_name
	 * void    return type
	 * @History memory：
	 * @Date：2014-11-17 上午11:36:08	@Modified by：zhangyq
	 * @Description：
	 */
	public void downThumbnailImageCChat(final String topic, final Account account,final CMessage cMessage, final CGroup notifyCGroup,final boolean isSavePath) {
		threadPool.execute(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					LocalStore localStore = account.getLocalStore();
					CAttachment cAttachment = cMessage.getAttachment();
					cAttachment.setAttchmentId(UUID.randomUUID().toString());
					String cAttFilePath =((MailChat) mApplication).getAttFilePath(((MailChat) mApplication).getChatThumbnailImageCacheDirectory(account), cAttachment.getAttchmentId(), cAttachment.getName());
					if(isSavePath){
						cAttachment.setFilePath(cAttFilePath);
					}
					localStore.updateCgroupLastCmessage(topic, cMessage);
					localStore.saveOrUpdateGroupMember(topic, cMessage.getMember());
					localStore.saveOrUpdateCMessageAndCAttach(topic, cMessage);
					localStore.updateCGroupUntreatedCount(topic, notifyCGroup.getUnreadCount()+1);
					Protocol.getInstance().downFile(cAttachment.getAttchmentId(),cAttFilePath,cAttachment.getFileid(), cAttachment.getName(),true,1,new DownloadCallback(){
						@Override
						public void downloadProgress(String id,int progress,long downloaded) {
							// TODO Auto-generated method stub
						}
						@Override
						public void downloadFinished(String id) {
							// TODO Auto-generated method stub
						}
						@Override
						public void downloadFailed(String id,
								Exception exception) {
							// TODO Auto-generated method stub
						}
						@Override
						public void downloadInterrupt(String id) {
							// TODO Auto-generated method stub
							
						}
					});
					for (MessagingListener l : getListeners(null)) {
		 	    		l.cMessageArrived(account, cMessage);
		 	        }
					if(notifyCGroup != null){
						NotificationCenter.getInstance().onNewChattingMessage(account.getUuid(), notifyCGroup, cMessage);
					} 
				} catch (MessageException e) {
					// TODO Auto-generated catch block
					for (MessagingListener l : getListeners(null)) {
				 	    l.cMessageArrived(account, cMessage);
				    }
					e.printStackTrace();
				} catch (UnavailableStorageException e) {
					// TODO Auto-generated catch block		
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	/**
	 * 下载预览图失败,重新下载群聊或单聊预览图
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-11-18
	 */
	public void againDownThumbnailImage(final Account account,final String attchmentId ,final String fileName,final long time,final String fileid,final boolean isCGrouMessage){
		threadPool.execute(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						Protocol.getInstance().downFile(attchmentId ,((MailChat) mApplication).getAttFilePath(((MailChat) mApplication).getChatThumbnailImageCacheDirectory(account), attchmentId, fileName),fileid, fileName,true,1,new DownloadCallback() {
							@Override
							public void downloadProgress(String id,int progress,long downloaded) {
								// TODO Auto-generated method stub
							}
							@Override
							public void downloadFinished(String id) {
								// TODO Auto-generated method stub
								//account.getLocalStore();
							}
							@Override
							public void downloadFailed(String id, Exception exception) {
								// TODO Auto-generated method stub
							}
							@Override
							public void downloadInterrupt(String id) {
								// TODO Auto-generated method stub
								
							}
						});
						if(isCGrouMessage){
							for (MessagingListener l : getListeners(null)) {
				 	    		l.againDownCGroupThumbnailImageSuccess(account);
				 	        }
						}else{
							for (MessagingListener l : getListeners(null)) {
				 	    		l.againDownDChatThumbnailImageSuccess(account);
				 	        }
						}
					} catch (MessageException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						if (isCGrouMessage) {
							for (MessagingListener l : getListeners(null)) {
								l.againDownCGroupThumbnailImageFailed(account);
							}
						} else {
							for (MessagingListener l : getListeners(null)) {
								l.againDownDChatThumbnailImageFailed(account);
							}
						}
					} 
				}
		});
	}
	
	/**
	 * 发送邀请邮件
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-11-20
	 */
	public void sendInvitationEmail(final Account acount ,final String invitationEmail,final MessagingListener listener){
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Protocol.getInstance().invitationUser(acount.getEmail(), invitationEmail,((MailChat) mApplication).getLanguage());
					for (MessagingListener l : getListeners(listener)) {
						l.sendInvitationSuccess(acount, invitationEmail);
		 	        }
				} catch (MessageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					for (MessagingListener l : getListeners(listener)) {
						l.sendInvitationFailed(acount, invitationEmail);
		 	        }
				}
			}
		});
	}

	/**
	 * 判断该用户是否用过邮恰
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-11-20
	 */
	public void actionDChatOrInvitation(final Account account ,final Context context,final Handler mHandler,final boolean isfinish,final ProgressDialog dialog,final Object... params){
		try {
			if(!account.isAuthenticated()){
				Toast.makeText(context,context.getString(R.string.authenticate_error_no_chat),Toast.LENGTH_SHORT).show();
				return;
			}
			if(params.length <= 0){
				return;
			}
			final List<Object> paramsList = Arrays.asList(params);
			final int size = paramsList.size();
			final String toEmail = (String) paramsList.get(0);
			final String toNikeName = (String) paramsList.get(1);

			final LocalStore localStore = account.getLocalStore();
			String dchatUid =DChat.getDchatUid(account.getEmail()+","+toEmail);
			DChat dChat = localStore.getDChat(dchatUid);
			Log.i(MailChat.LOG_COLLECTOR_TAG,"获取dChat成功");
				if(dChat!=null){
						if(size == 2){
							//普通单聊跳转
							ChattingSingleActivity.actionChatList(context,dChat,account);
						}else if(size == 4){
							//邮件透传单聊跳转
							DChatMessage dMessage = new DChatMessage(cn.mailchat.chatting.beans.DChatMessage.Type.FROM_MAIL_INFO);
							dMessage.setMailFrom(toEmail);
							dMessage.setMailSubject((String) paramsList.get(2));
							dMessage.setMailPreview((String) paramsList.get(3));
							ChattingSingleActivity.actionDChatFromMailInfo(context, dMessage, dChat, account,true);
						}else{
							return;
						}
						if(isfinish){
							((Activity) context).finish();
						}
				}else{
					if (NetUtil.isActive()) {
						dialog.show();
						threadPool.execute(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								try {
									Protocol.getInstance().openDChatOffLine(account.getEmail(), toEmail, 1);
									try {
										final DChat dchat =new DChat();
										dchat.setUid(DChat.getDchatUid(account.getEmail()+","+toEmail));
										dchat.setEmail(toEmail);
										dchat.setNickName(toNikeName);
										dchat.setVisibility(false);
										dchat.setDChatAlert(true);
										dchat.setSticked(false);
										final ContactAttribute contactAttribute = localStore.getContactAttribute(toEmail,false);
										final boolean isExist = contactAttribute!=null;
										if(isExist){
											dchat.setNickName(contactAttribute.getNickName());
											dchat.setImgHeadHash(contactAttribute.getImgHeadHash());
											localStore.updateContactAttributeReceiveCount(toEmail, contactAttribute.getReceiveCount()+1);
										}else{
											int index = toEmail.indexOf("@");
											String nikeName = null ;
											if(index!=-1){
												nikeName = toEmail.substring(0, index);
											}
											localStore.savaContact(toEmail, nikeName,false);//存储联系人
										}
										localStore.persistDChatList(dchat);

										mHandler.post(new Runnable() {
											@Override
											public void run() {
												dialog.hide();
												boolean isImgHeadHashExist;
												if(isExist && !StringUtil.isEmpty(contactAttribute.getImgHeadHash())){
													isImgHeadHashExist = true;
												}else{
													isImgHeadHashExist = false;
												}
												if(size == 2){
													ChattingSingleActivity.actionChatList(context,dchat,account,isImgHeadHashExist);
												}else if(size == 4){
													DChatMessage dMessage = new DChatMessage(cn.mailchat.chatting.beans.DChatMessage.Type.FROM_MAIL_INFO);
													dMessage.setMailFrom(toEmail);
													dMessage.setMailSubject((String) paramsList.get(2));
													dMessage.setMailPreview((String) paramsList.get(3));
													ChattingSingleActivity.actionDChatFromMailInfo(context, dMessage, dchat, account,isImgHeadHashExist);
												}else{
													return;
												}
												if (isfinish) {
													((Activity) context).finish();
												}
											}
										});
									} catch (UnavailableStorageException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										mHandler.post(new Runnable() {
											@Override
											public void run() {
												dialog.hide();
												Toast.makeText(context,R.string.create_dchat_fail,Toast.LENGTH_SHORT).show();
											}
										});
									}
								} catch (MessageException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									mHandler.post(new Runnable() {
										@Override
										public void run() {
											dialog.hide();
											Toast.makeText(context,R.string.create_dchat_fail,Toast.LENGTH_SHORT).show();
										}
									});
								}
							}
						});
					} else {
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(context,R.string.send_dchat_invitation_no_net,Toast.LENGTH_SHORT).show();
							}
						});
					}
			   }
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			Log.e(MailChat.LOG_COLLECTOR_TAG, "action dchat exception", e);
			e.printStackTrace();
		}
	}

	public void feedBack(final Account account, final String content) {
		threadPool.execute(new Runnable() {
			public void run() {
		        if (!loopCatch.compareAndSet(false, true)) {
		            return;
		        }
		        try {
		            if (content == null || content.length() < 1) {
		                return;
		            }
		            Store localStore = account.getLocalStore();
		            LocalFolder localFolder = (LocalFolder)localStore.getFolder(account.getOutboxFolderName());
		            Message[] messages = new Message[1];
		            MimeMessage message = new MimeMessage();


		            message.setBody(new TextBody(content));
		            message.setFlag(Flag.X_DOWNLOADED_FULL, true);
		            String subject = mApplication.getString(R.string.feedback_start) 
		            		+ account.getEmail() + mApplication.getString(R.string.feedback_end);
		            message.setSubject(subject);
		            long nowTime = System.currentTimeMillis();
		            Date nowDate = new Date(nowTime);
		            message.setInternalDate(nowDate);
		            message.addSentDate(nowDate);
		            message.setFrom(new Address(account.getEmail(), account.getName()));
		            message.setRecipients(RecipientType.TO,Address.parseUnencoded(MailChat.BUG_EMAIL));
		            messages[0] = message;
		            localFolder.open(Folder.OPEN_MODE_RW);
		            localFolder.appendMessages(new Message[] { message });
		            //localFolder.appendMessages(messages);
		            //localFolder.clearMessagesOlderThan(nowTime - (15 * 60 * 1000));
		            localFolder.close();
		            sendPendingMessages(account, null);

		        } catch (Throwable it) {
		            Log.e(MailChat.LOG_TAG, "Could not save error message to " + account.getErrorFolderName(), it);
		        } finally {
		            loopCatch.set(false);
		        }
			}
		});

	}
	
	/**
	 * 群聊,单聊保存或更新联系人并回调界面更新
	 *
	 * @Description:
	 * @param email
	 * @param nikeName
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-04
	 */
	private boolean updateAndSaveContactAttribute(Account  account,String email ,String nikeName,boolean isPush){
		LocalStore localStore;
		boolean isExist=true;
		try {
			localStore = account.getLocalStore();
			ContactAttribute contactAttribute =localStore.getContactAttribute(email,false);
			if(isPush){
				if(contactAttribute==null){
					//存储联系人
					localStore.savaContact(email, nikeName,false);//存储联系人
					ContactAttribute contact=addTempContact(email,null);
					tempContactList.add(contact);
					if (tempContactList.size()>0) {
						reflashContactList(account,tempContactList);
					}
					isExist=false;
				}
			}else{
				if(contactAttribute==null){
					//存储联系人
					localStore.savaContact(email, nikeName,false);//存储联系人
					ContactAttribute contact=addTempContact(email,null);
					tempContactList.add(contact);
					if (tempContactList.size()>0) {
						reflashContactList(account,tempContactList);
					}
					isExist=false;
				}else{
					//更新联系人使用次数
					localStore.updateContactAttributeReceiveCount(email, contactAttribute.getReceiveCount()+1);
				}
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isExist;
	}
	
	/**
	 * 群聊隐藏/显示
	 *
	 * @Description:
	 * @param hidden 0-因此 1-显示
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-04
	 */
	public void hiddenGroup(Account mAccount, String cGroupUid,
			boolean hidden, MessagingListener listener) {
		try {
			LocalStore localStore = mAccount.getLocalStore();
			localStore.hiddenGroup(cGroupUid, hidden);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 群聊存储输入框草稿
	 *
	 * @Description:
	 * @param 
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-12
	 */
	public void updateCGroupDraft(final Account mAccount,  final String cGroupUid,final String draftContent,final boolean isSave){
		threadPool.execute(new Runnable() {
			
			public void run() {
				try {
					LocalStore localStore = mAccount.getLocalStore();
					localStore.updateCGroupDraft(cGroupUid, draftContent, isSave);
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
	}
	
	/**
	 * 单聊存储输入框草稿
	 *
	 * @Description:
	 * @param 
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-12
	 */
	public void updatedChatDraft(final Account mAccount,  final String dChatUid,final String draftContent,final boolean isSave){
		threadPool.execute(new Runnable() {
			
			public void run() {
				try {
					LocalStore localStore = mAccount.getLocalStore();
					localStore.updateDChatDraft(dChatUid, draftContent, isSave);
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 群聊下载(大图,附件进度显示)
	 *
	 * @Description:
	 * @param 
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-18
	 */
	public void cGroupDownFile(final Account account,final CMessage cMessage){
		final CAttachment cAttachment = cMessage.getAttachment();
		final String cAttchmentId = cAttachment.getAttchmentId();
		if(downloaders.containsKey(cAttchmentId)){
			// 避免重复请求
			return;
		}
		threadPool.execute(new Runnable() {
			public void run() {
				CAttachment cAttachment = cMessage.getAttachment();
				String fileDirectory = null;
				switch (cMessage.getMessageType()) {
				case IMAGE:
					fileDirectory = ((MailChat) mApplication).getChatImageCacheDirectory(account);
					break;
				case ATTACHMENT:
					fileDirectory = ((MailChat) mApplication).getChatAttachmentDirectory(account);
					break;
				default:
					return;
				}
				final Downloader downloder = new Downloader();
				downloaders.put(cAttchmentId, downloder);
				for (MessagingListener l : getListeners(null)) {
					l.fileDownloadStart(account, cAttchmentId);
				}
				downloder.downloadFile(Protocol.getInstance().getUrl(cn.mailchat.chatting.protocol.Command.DOWN_FILE),fileDirectory, cAttchmentId, cAttachment.getFileid(), cAttachment.getName(),cAttachment.getSize(), new DownloadCallback() {

					@Override
					public void downloadProgress(String id,int progress, long downloaded) {
						// TODO Auto-generated method stub
						if(downloaders.containsKey(id)){
							for (MessagingListener l : getListeners(null)) {
								l.fileDownloadProgress(account, id,progress);
							}
						}
					}

					@Override
					public void downloadFinished(String id) {
						// TODO Auto-generated method stub
						downloaders.remove(id);
						for (MessagingListener l : getListeners(null)) {
							l.fileDownloadFinished(account, id);
						}
					}

					@Override
					public void downloadFailed(String id,Exception exception) {
						// TODO Auto-generated method stub
						if(downloaders.containsKey(id)){
							downloaders.remove(id);
							for (MessagingListener l : getListeners(null)) {
								l.fileDownloadFailed(account, id);
							}
						}
					}

					@Override
					public void downloadInterrupt(String id) {
						// TODO Auto-generated method stub
						if(downloaders.containsKey(id)){
							downloaders.remove(id);
							for (MessagingListener l : getListeners(null)) {
								l.fileDownloadInterrupt(account, id);
							}
						}
					}
				});
			}
		});
	}
	
	/**
	 * 单聊下载(大图,附件进度显示)
	 *
	 * @Description:
	 * @param 
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-18
	 */
	public void dChatDownFile(final Account account,final DChatMessage dChatMessage){
		final DAttachment	dAttachment =dChatMessage.getAttachments().get(0);
		final String dAttchmentId =dAttachment.getAttchmentId();
		if (downloaders.containsKey(dAttchmentId)) {
			// 避免重复请求
			return;
		}
		threadPool.execute(new Runnable() {
			
			public void run() {
				String fileDirectory = null;
				switch (dChatMessage.getMessageType()) {
				case IMAGE:
					fileDirectory = ((MailChat) mApplication).getChatImageCacheDirectory(account);
					break;
				case ATTACHMENT:
					fileDirectory = ((MailChat) mApplication).getChatAttachmentDirectory(account);
					break;
				default:
					return;
				}
				Downloader downloder = new Downloader();
				downloaders.put(dAttchmentId, downloder);
				for (MessagingListener l : getListeners(null)) {
					l.fileDownloadStart(account, dAttchmentId);
				}
				downloder.downloadFile(Protocol.getInstance().getUrl(cn.mailchat.chatting.protocol.Command.DOWN_FILE),fileDirectory, dAttchmentId, dAttachment.getFileid(), dAttachment.getName(),dAttachment.getSize(), new DownloadCallback() {

					@Override
					public void downloadProgress(String id,int progress, long downloaded) {
						// TODO Auto-generated method stub
						for (MessagingListener l : getListeners(null)) {
							l.fileDownloadProgress(account, id,progress);
						}
					}

					@Override
					public void downloadFinished(String id) {
						// TODO Auto-generated method stub
						downloaders.remove(id);
						for (MessagingListener l : getListeners(null)) {
							l.fileDownloadFinished(account, id);
						}
					}

					@Override
					public void downloadFailed(String id,Exception exception) {
						// TODO Auto-generated method stub
						downloaders.remove(id);
						for (MessagingListener l : getListeners(null)) {
							l.fileDownloadFailed(account, id);
						}
					}

					@Override
					public void downloadInterrupt(String id) {
						// TODO Auto-generated method stub
						downloaders.remove(id);
						for (MessagingListener l : getListeners(null)) {
							l.fileDownloadInterrupt(account, id);
						}
					}
				});
			}
		});
	}
	
	public void cancelDownFile(Account account ,String attchmentId,boolean isInterrupt){
		Downloader downloder =downloaders.remove(attchmentId);
		if(downloder!=null){
			downloder.cancel(attchmentId);
			if(isInterrupt){
				for (MessagingListener l : getListeners(null)) {
					l.fileDownloadInterrupt(account, attchmentId);
				}
			}
		}
	}
	
	public void updateCGroupDownFileState(Account account, CAttachment cAttachment) {
		try {
			LocalStore localStore = account.getLocalStore();
			localStore.updateCAttachmentDownloadPauseFlag(cAttachment.getAttchmentId(),cAttachment.isDownloadPause());
			localStore.updateCAttachmentDownloadProgress(cAttachment.getAttchmentId(),cAttachment.getDownloadProgress());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateDChatDownFileState(Account account, DAttachment dAttachment) {
		try {
			LocalStore localStore = account.getLocalStore();
			localStore.updateDAttachmentDownloadPauseFlag(dAttachment.getAttchmentId(),dAttachment.isDownloadPause());
			localStore.updateDAttachmentDownloadProgress(dAttachment.getAttchmentId(),dAttachment.getDownloadProgress());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取单聊对应联系人
	 *
	 * @Description:
	 * @param email 当前单聊的email
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-19
	 */
	public void getContactForJump(final Account acount ,final String email,final boolean isDChat){
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					LocalStore  localStore = acount.getLocalStore();
					ContactAttribute  contact = localStore.getContactAttribute(email,false);
					if(contact!=null){//应该不会出现NULL的情况,因为联系人在创建单聊的时候已经添加
						contact = localStore.getContactsInfoForChat(contact,acount.getVersion_35Mail()==1);
						if(isDChat){
							for (MessagingListener l : getListeners(null)) {
								 l.getContactForDChatSuccess(acount,email,contact);
					        }
						}else{
							for (MessagingListener l : getListeners(null)) {
								 l.getContactForMessageSuccess(acount, email, contact);
					        }
						}
					}
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	public void finishForSingleSet(final Account acount){
		
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				for (MessagingListener l : getListeners(null)) {
					 l.isSingleSetFinished(acount);
		        }
			}
		});
	}
	
	/**
	 * 登陆失败提示显示
	 *
	 * @Description:
	 * @param email
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-19
	 */
	public void loginDialogShow(final String email,final boolean isShowmanualSettingImp,final int errorCode,final  boolean is35Mail){
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				for (MessagingListener l : getListeners(null)) {
					 l.loginDialogShow(email,isShowmanualSettingImp,errorCode,is35Mail);
		        }
			}
		});
	}
	
	// Function for handling local and remote messages' match in sent/drafts/trash folder
	private String getMessageID(Message message) {
		if (message == null) {
			return null;
		}
		try {
			String[] headers = message.getHeader(MailChat.MESSAGE_ID_HEADER);
			if ((headers != null) && (headers.length > 0)) {
				return headers[0];
			} else {
				return null;
			}
		} catch (Exception e) {
			Log.e(MailChat.LOG_TAG, "Fetch Message-ID Failed!", e);
			return null;
		}
	}

    // 将通过MQTT推送的邮件摘要插入本地邮件列表
    // 返回值表示插入的本地文件夹名，未插入则返回NULL
    private String insertPushMessage(Account account, JSONObject pushMessage) {
        if (!account.isAuthenticated()) {
            return null;
        }

        String directory = null;
        String returnValue = null;
        try {
            directory = pushMessage.getString("d");
            if (!pushMessage.isNull("n")) {
                JSONArray dirs = pushMessage.getJSONArray("n");
                directory = "";
                for (int i = 0; i < dirs.length(); i++) {
                    directory = directory + dirs.getString(i);
                    if (i != dirs.length() - 1) {
                        directory = directory + ".";
                    }
                }
            }
            
            if (".Applicationeventbox".equalsIgnoreCase(directory)) {
                //TODO 准确判断不同语言环境下消息盒子的名称
                directory = "消息盒子";
            } else if (account.getInboxFolderName().equalsIgnoreCase(directory)) {
                directory = account.getInboxFolderName();
            }
            
            if (account == null
                    || pushMessage == null
                    || directory == null
                    || directory.startsWith(".")) {
                returnValue = null;
                return returnValue;
            }
            
            returnValue = directory;
            
        	String uid = pushMessage.getString("i");
        	String recipients = pushMessage.getString("u");
        	String sender = pushMessage.getString("f");
        	String subject = pushMessage.getString("s");
        	long time = pushMessage.getLong("t");
        	String content = pushMessage.getString("m");

        	// 从推送消息中获取非内联附件数目
            int attCount = 0;
            if (pushMessage.has("a")) {
                attCount = pushMessage.getInt("a");
            }

            String mailId = null;
            if (pushMessage.has("id")) {
                mailId = pushMessage.getString("id");
            }

        	LocalStore store = account.getLocalStore();
        	LocalFolder folder = store.getFolder(directory);
        	LocalMessage message = folder.getMessage(uid);

        	if (message == null) {
        		MimeMessage msg = new MimeMessage();
        		msg.setUid(uid);
        		msg.setMailId(mailId);
        		msg.setSubject(subject);
        		msg.setBody(new TextBody(content));
        		msg.setPushMessage(true);
        		// 存储非内联附件数目
        		msg.setAttachmentCount(attCount);
        		
        		Address[] addresses = Address.parseUnencoded(sender);
        		if (addresses.length > 0) {
        		    msg.setFrom(addresses[0]);
        		}
        		msg.setRecipients(RecipientType.TO, Address.parseUnencoded(recipients));
        		
        		Date date = new Date(time);
        		msg.setInternalDate(date);
        		msg.addSentDate(date);
                
                // 标记通过MQTT推送的邮件
        		msg.setFlag(Flag.X_PUSH_MAIL, true);
        		
                folder.open(Folder.OPEN_MODE_RW);
        		folder.appendMessages(new Message[]{msg});
        		folder.close();
        		
                for (final MessagingListener l : getListeners()) {
                    l.synchronizeMailboxAddOrUpdateMessage(account, folder.getName(), msg);
                    l.folderStatusChanged(account, folder.getName(), folder.getUnreadMessageCount());
                }
        	} else {
        	    returnValue = ".";
        	    if (mailId != null && mailId.length() > 0) {
        	        FetchProfile fp = new FetchProfile();
        	        fp.add(FetchProfile.Item.BODY);

                    folder.open(Folder.OPEN_MODE_RW);
                    folder.fetch(new Message[] {message}, fp, null);
                    message.setMailId(mailId);
                    folder.updateMessage(message);
                    folder.close();
                    
                    for (final MessagingListener l : getListeners()) {
                        l.synchronizeMailboxAddOrUpdateMessage(account, folder.getName(), message);
                    }
        	    }
        	}
        	return returnValue;
        } catch (Exception e) {
            Log.e(MailChat.LOG_TAG, "Error while inserting push message.", e);
            addErrorMessage(account, null, e);
            returnValue = null;
            return returnValue;
        } finally {
            if (directory != null) {
                long time = returnValue == null ? 0 : System.currentTimeMillis();
                try {
                    account.getRemoteStore().getFolder(directory).setLastChecked(time);
                } catch (Exception e) {
                    // DO NOTHING
                }
            }
        }
    }
    
    // 装载邮件的文本部分
	private void loadMessageText(final Account account,
			final Folder remoteFolder, 
			final LocalFolder localFolder,
			Message message) throws MessagingException {
		
		FetchProfile fp = new FetchProfile();
		fp.add(FetchProfile.Item.FLAGS);
		fp.add(FetchProfile.Item.ENVELOPE);
		remoteFolder.fetch(new Message[] { message }, fp, null);
		
		fp.clear();
		fp.add(FetchProfile.Item.STRUCTURE);
		remoteFolder.fetch(new Message[] { message }, fp, null);
		
		if (message.getBody() == null) {
		    String contentType = MimeUtility.getFirstHeader(message, MimeHeader.HEADER_CONTENT_TYPE);
		    if (contentType != null && contentType.toLowerCase(Locale.US).startsWith("text")) {
    			fp.clear();
    			fp.add(FetchProfile.Item.BODY_SANE);
    			remoteFolder.fetch(new Message[] { message }, fp, null);

    			localFolder.appendMessages(new Message[] { message });
    			Message localMessage = localFolder.getMessage(message.getUid());

    			if (!message.isSet(Flag.X_DOWNLOADED_FULL)) {
//    				if (account.getMaximumAutoDownloadMessageSize() == 0
//    						|| message.getSize() < account
//    								.getMaximumAutoDownloadMessageSize()) {
    					localMessage.setFlag(Flag.X_DOWNLOADED_FULL, true);
//    				} else {
//    					localMessage.setFlag(Flag.X_DOWNLOADED_PARTIAL, true);
//    				}
    			}
            } else {
    		    Set<String> names = message.getHeaderNames();
    		    if (names == null || names.size() == 0) {
    		        throw new MailNotExistException();
    		    }
            }
		} else {
			Set<Part> viewables = MimeUtility.collectTextParts(message);
			for (Part part : viewables) {
				remoteFolder.fetchPart(message, part, null);
			}

			localFolder.appendMessages(new Message[] { message });
			Message localMessage = localFolder.getMessage(message.getUid());
			localMessage.setFlag(Flag.X_DOWNLOADED_PARTIAL, true);
		}
	}
	
	public void saveMessageToEml(Account account, String folder, String uid, MessageRetrievalListener listener)
	        throws MessagingException {
        if (uid.startsWith(MailChat.LOCAL_UID_PREFIX)) {
            throw new MessagingException("Message with local UID cannot save as EML");
        }
	    
	    Folder remoteFolder = null;
        try {
            Store remoteStore = account.getRemoteStore();
            remoteFolder = remoteStore.getFolder(folder);
            remoteFolder.open(Folder.OPEN_MODE_RW);

            Message remoteMessage = remoteFolder.getMessage(uid);
            
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.EML);
            remoteFolder.fetch(new Message[] { remoteMessage }, fp, listener);
        } catch (Exception e) {
            throw new MessagingException("Save message as EML failed", e);
        } finally {
            closeFolder(remoteFolder);
        }
    }
	
    /**
	 * 执行MQTT，HTTPS请求失败的命令
	 *
	 * @Description:
	 * @param 
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-1-12
	 */
    public void executePending(){
    	threadPool.execute(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					if(NetUtil.isActive()){
						Preferences prefs = Preferences.getPreferences(mApplication);
						List<Account> accounts = prefs.getAccounts();
						for(Account account :accounts){
							LocalStore localStore =	account.getLocalStore();
							ArrayList<PendingMQTTConmmand> pendingMQTTConmmands =localStore.getMQTTPending();
							if(pendingMQTTConmmands!=null&&pendingMQTTConmmands.size()>0&&connection.getClient().getClientHandle()!=null){
								for(PendingMQTTConmmand pendingMQTTConmmand:pendingMQTTConmmands){
									String topic = pendingMQTTConmmand.getTopic();
									String content =pendingMQTTConmmand.getContent();
									Action  action =pendingMQTTConmmand.getAction();
									MQTTCommand mqttCommand=pendingMQTTConmmand.getCommand();
									switch (action) {
									case PUBLISH:
										connection.getClient().publish(topic, content.getBytes(), MQTT_QOS, false, null, 
												new ActionListener(action, MessagingController.this, account, mqttCommand, pendingMQTTConmmand));
										break;
									case SUBSCRIBE:
										if(topic.contains(",")){
											String[] topics=topic.split("\\,"); 
											int [] qos =new int[topics.length];
											for(int i=0;i<topics.length;i++){
												qos[i]=MQTT_QOS;
											}
											connection.getClient().subscribe(topics, qos, null,
													new ActionListener(action, MessagingController.this, account, mqttCommand, pendingMQTTConmmand));
										}else{
											connection.getClient().subscribe(topic, MQTT_QOS, null,
													new ActionListener(action, MessagingController.this, account, mqttCommand, pendingMQTTConmmand));
										}
										break;
									case UNSUBSCRIBE:
										if(topic.contains(",")){
											String[] topics=topic.split("\\,");
											connection.getClient().unsubscribe(topics, null, 
													new ActionListener(action, MessagingController.this, account, mqttCommand, pendingMQTTConmmand));
										}else{
											connection.getClient().unsubscribe(topic, null, 
													new ActionListener(action, MessagingController.this, account, mqttCommand, pendingMQTTConmmand));
										}
										break;
									default:
										break;
									}
								}
							}
							ArrayList<PendingHTTPSCommand> pendingHTTPSConmmands = localStore.getHTTPSPending();
							if(pendingHTTPSConmmands!=null&&pendingHTTPSConmmands.size()>0){
								for(PendingHTTPSCommand pendingHTTPSCommand:pendingHTTPSConmmands){
									String[] parameter = pendingHTTPSCommand.getParameters().split("\\,");
									switch (pendingHTTPSCommand.getCommand()) {
									case JOIN_GROUP:
										joinGroup(account, parameter[1], parameter[0], pendingHTTPSCommand.getId());
										break;
									default:
										break;
									}
								}
							}
						}
					}
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MqttPersistenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
    }
    /**
	 * 获取服务端配置信息
	 *
	 * @Description:
	 * @param email
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-1-12
	 */
    public void getEmailSet(final String email,final boolean isGoImapSetting){
    	threadPool.execute(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					List<Object> paramList = Protocol.getInstance().getEmailSet(email);
					if(paramList.size()==1){
						int result=(int)paramList.get(0);
						if(result==-1||result==0){
							for (MessagingListener l : getListeners(null)) {
								 l.getEmailSetFail(email, -1,isGoImapSetting);
					        }
						}else if(result==1){
							for (MessagingListener l : getListeners(null)) {
								 l.getEmailSetFail(email, result,isGoImapSetting);
					        }
						}
					}else if(paramList.size()==2){
						for (MessagingListener l : getListeners(null)) {
							 l.getEmailSetSuccess(email,(ImapAndSmtpSetting)paramList.get(1),isGoImapSetting);
				        }
					}
				} catch (MessageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					for (MessagingListener l : getListeners(null)) {
						 l.getEmailSetFail(email, -1,isGoImapSetting);
			        }
				}
			}
		});
    }
    
    /**
   	 * 判断是否为35域邮箱和版本
   	 *
   	 * @Description:
   	 * @param domain后缀
   	 * @see:
   	 * @since:
   	 * @author: shengli
   	 * @date:2015-3-25
   	 */
     public void get35MailVersion(final Account account,final boolean isAgain){
       	threadPool.execute(new Runnable() {
   			
   			@Override
   			public void run() {
   				// TODO Auto-generated method stub
				String email = account.getEmail();
				if (email.indexOf("@") == -1) {
					return;
				}
				String[] splitEmail = email.split("@");
   				String domain= splitEmail[1];
   				if (domain.equalsIgnoreCase("35.cn")) {
   					domain ="china-channel.com";
   				}
				int version35 = Protocol.getInstance().get35MailVersion(domain);
				account.setVersion_35Mail(version35);
				account.save(Preferences.getPreferences(mApplication));
				Log.i(MailChat.LOG_COLLECTOR_TAG, "get 35mail version::"
						+ version35);
   				if (account.getVersion_35Mail() == -1&&isAgain) {
   					scheduledExecutorService.schedule(new Runnable() {
   						@Override
   						public void run() {
   							// TODO Auto-generated method stub
   							get35MailVersion(account,false);
   						}
   					}, 30, TimeUnit.SECONDS);
   				}
   			}
   		});
     }

	/**
	 * 
	 * method name: get35MailVersionForFolder function @Description: TODO
	 * Parameters and return values description:
	 * 
	 * @param account
	 * @param isAgain
	 * @return field_name int return type
	 * @History memory：
	 * @Date：2015-6-11 下午2:10:10 @Modified by：zhangjx
	 * @Description:创建邮件夹时，如果是35域，标识是否接收push
	 */
	public int get35MailVersionForFolder(final Account account,
			final boolean isAgain) {
		String[] splitEmail = account.getEmail().split("@");
		String domain = splitEmail[1];
		if (domain.equalsIgnoreCase("35.cn")) {
			domain = "china-channel.com";
		}
		int version = Protocol.getInstance().get35MailVersionForLogin(domain);
		if (account.getVersion_35Mail() == -1 && isAgain) {
			scheduledExecutorService.schedule(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					get35MailVersion(account, false);
				}
			}, 30, TimeUnit.SECONDS);
		}
		return version;
	}

	/**
	 * 删除邮件时,检测通知栏有无该删除的邮件,如果有清除通知栏
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-2-11
	 */
	public void cancelDeleteMessageNotify(final Account account ,final List<Message> messages){
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<Integer, List<Message>> accountMailNotifyData  = NotificationCenter.getMailNotificationData();
				List<Message> messages = accountMailNotifyData.get(account.getAccountNumber());
				if(messages!=null){
					if(messages.size()==1){
						for(Message message :messages){
							if(messages.get(0).getUid().equals(message.getUid())){
								NotificationCenter.getInstance().notifyClean(account);
							}
						}
					}
				}
			}
		});
	}
	/**
	 * 退订账户
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-4-15
	 */
	public void unsubscribeAccount(final Account account){
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String accountUnsubscribe =account.getEmail()+"/1"+","+account.getEmail()+"/s";
				PendingMQTTConmmand pendingMQTTConmmand =new PendingMQTTConmmand(-1,ActionListener.Action.UNSUBSCRIBE,MQTTCommand.UNSUBSCRIBE_ACCOUNT,accountUnsubscribe,null);
				try {
					if (connection.getClient().getClientHandle() != null) {
						connection.getClient().unsubscribe(
								accountUnsubscribe,
								null,
								new ActionListener(
										ActionListener.Action.UNSUBSCRIBE,
										MessagingController.this, null,
										MQTTCommand.UNSUBSCRIBE_ACCOUNT,
										pendingMQTTConmmand));
					}
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
	}
	
	public void executeUnsubscribeAccountPending() {
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				ArrayList<PendingMQTTConmmand> pendingMQTTConmmands =PendingChatCommandLocalStore.getInstance(mApplication).getMQTTPending();
				if(pendingMQTTConmmands!=null&&pendingMQTTConmmands.size()>0&&connection.getClient().getClientHandle()!=null){
					for(PendingMQTTConmmand pendingMQTTConmmand:pendingMQTTConmmands){
						String topic = pendingMQTTConmmand.getTopic();
						Action action =pendingMQTTConmmand.getAction();
						MQTTCommand mqttCommand=pendingMQTTConmmand.getCommand();
						switch (action) {
						case UNSUBSCRIBE:
							if(topic.contains(",")){
								String[] topics=topic.split("\\,");
								try {
									connection.getClient().unsubscribe(topics, null, 
											new ActionListener(action, MessagingController.this, null, mqttCommand, pendingMQTTConmmand));
								} catch (MqttException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							break;
						default:
							break;
						}
					}
				}
			}
		});
	}
	 
    /**
   	 * 判断是否为35域邮箱和版本
   	 *
   	 * @Description:
   	 * @param domain后缀
   	 * @see:
   	 * @since:
   	 * @author: shengli
   	 * @date:2015-5-6
   	 */
     public void get35MailVersionForLogin(final Account account,final String email){
       	threadPool.execute(new Runnable() {

   			@Override
			public void run() {
   				String[] splitEmail =email.split("@");
   				String domain= splitEmail[1];
   				if (domain.equalsIgnoreCase("35.cn")) {
   					domain ="china-channel.com";
   				}
				int version = Protocol.getInstance().get35MailVersion(
						domain);
				if (account!=null) {
					account.setIs35Mail(version==1||version==2?true:false);
					account.save(Preferences.getPreferences(mApplication));
				}
				for (MessagingListener l : getListeners(null)) {
					l.get35MailVersionForLogin(email, version);
				}
   			}
       	});
     }

	/**
	 * 注册push服务
	 * 
	 * @Description:
	 * @param domain后缀
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-5-12
	 */
     public void registerPush(final Account account){
    	 threadPool.execute(new Runnable() {
    		@Override
    		public void run() {
    			if(account.isOnPushSuccess()){
    				return;
    			}
				String[] splitEmail =account.getEmail().split("@");
   				String domain= splitEmail[1];
   				if (domain.equalsIgnoreCase("35.cn")) {
   					domain ="china-channel.com";
   				}
   				account.setOnPushSuccess(Protocol.getInstance().registerPush(account));
   				account.save(Preferences.getPreferences(mApplication));
   				if (!account.isOnPushSuccess()) {
   					//如果未注册成功，30秒后检测所有账户push状态
   					alarmTask(CHECK_PUSH_STATA, 30, PendingService.class, GlobalConstants.PUSH_CHECK_ALARM_REQUESTCODE);
   				}
    		}
    	 });
     }
     
     /**
    	 * 注销35邮箱在线状态(下线)
    	 *
    	 * @Description:
    	 * @see:
    	 * @since:
    	 * @author: shengli
    	 * @date:2015-2-4
    	 */
    public void unRegisterPush(final Account deleteAccount){
     	threadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				try {
 					Protocol.getInstance().unRegisterPush(deleteAccount.getEmail(),SystemUtil.getCliendId(mApplication));
 				} catch (MessageException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
    }
	public void saveImageToPhoto(final Account account, final String filePath,
			final String fileName) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					// 把文件插入到系统图库
					MediaStore.Images.Media.insertImage(mApplication
							.getContentResolver(), filePath, FileUtil
							.generateUniqueFileName(
									GlobalConstants.PHOTO_DEF_DIR, fileName),
							null);
					// 通知图库更新
					mApplication.sendBroadcast(new Intent(
							Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri
									.parse("file://" + filePath)));
					for (MessagingListener l : getListeners(null)) {
						l.chattingSaveImageSuccess(account);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e(MailChat.LOG_TAG,
							"ChatImage---insertImage : " + e.toString());
					for (MessagingListener l : getListeners(null)) {
						l.chattingSaveImageFail(account);
					}
				}
				// if(FileUtil.copyFile(filePath,
				// FileUtil.generateUniqueFileName(GlobalConstants.PHOTO_DEF_DIR,
				// fileName), false)){
				// Toast.makeText(getApplication(), R.string.save_image_succeed,
				// Toast.LENGTH_SHORT).show();
				// }else{
				// Toast.makeText(getApplication(), R.string.save_image_failed,
				// Toast.LENGTH_SHORT).show();
				// }
			}
		});
	}

	public void updateCAttachmentReadState(Account mAccount,String attchmentId, int readFlag) {
		// TODO Auto-generated method stub
		try {
			mAccount.getLocalStore().updateCAttachmentReadState(attchmentId, readFlag);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void updateDAttachmentReadState(Account mAccount,String attchmentId, int readFlag) {
		// TODO Auto-generated method stub
		try {
			mAccount.getLocalStore().updateDAttachmentReadFlag(attchmentId, readFlag);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * method name: uploadUserInfo function @Description: TODO Parameters and
	 * return values description：
	 * 
	 * @param mAccount
	 * @param contact
	 * @param listener
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-4-17 下午1:17:27 @Modified by：zhangjx
	 * @Description：上传用户头像和昵称
	 */
	public void uploadUserInfo(final Account mAccount,
			final ContactAttribute contact, final String cliendId,final MessagingListener listener) {
		threadPool.execute(new Runnable() {
			@SuppressWarnings("null")
			@Override
			public void run() {
				for (MessagingListener l : getListeners(null)) {
					l.uploadUserInfoStart(mAccount);
				}
				File smallImgFile = null;
				if (contact.getImgHeadPath() != null) {
					smallImgFile = new File(contact.getImgHeadPath());
					if (smallImgFile.length() / 1000 > 1024) {
						smallImgFile = new File(MailChat.application
								.getUserSmallImageCacheDirectory(mAccount)
								+ smallImgFile.getName().substring(0,
										smallImgFile.getName().indexOf("."))
								+ ".png");
						ImageUtils.compressBmpToFile(ImageUtils
								.getNativeImage(contact.getImgBigHeadPath(),false),
								smallImgFile);
					}
				}
				JSONObject responseJson = null;
				ContactAttribute newContactAttribute = null;
				try {
					responseJson = Protocol.getInstance().uploadUserHeadFile(contact,smallImgFile,cliendId);
					LocalStore localStore=mAccount.getLocalStore();
					if (responseJson.optInt("result") == 1) {
						newContactAttribute = new ContactAttribute();
						//由于35 3.0邮箱eis在服务端有缓存，所以本地修改自己账号的头像等信息后需要更新数据库信息，不然下拉刷新还是旧的
						if (mAccount.getVersion_35Mail() != 1) {
							newContactAttribute.setImgHeadPath(responseJson.optString("checksum"));
							newContactAttribute.setImgHeadHash(responseJson
									.optString("checksum"));
							newContactAttribute.setNickName(responseJson
									.optString("name"));
							newContactAttribute.setCompany(responseJson
									.optString("com"));
							newContactAttribute.setDepartment(responseJson
									.optString("dep"));
							newContactAttribute.setPosition(responseJson
									.optString("title"));
							localStore.updateContactInfoByEmail(
									mAccount.getEmail(), newContactAttribute,false);
						}else {
							newContactAttribute.setImgHeadPath(responseJson.optString("checksum"));
							newContactAttribute.setImgHeadHash(responseJson
									.optString("checksum"));
							newContactAttribute.setNickName(responseJson
									.optString("name"));
							newContactAttribute.setCompany(responseJson
									.optString("com"));
							newContactAttribute.setDepartment(responseJson
									.optString("dep"));
							newContactAttribute.setPosition(responseJson
									.optString("title"));
							newContactAttribute.setEmail(mAccount.getEmail());
							newContactAttribute.setrEmail(mAccount.getEmail());
							newContactAttribute.setrImgHeadHash(responseJson
									.optString("checksum"));
							newContactAttribute.setrImgHeadHash(responseJson
									.optString("checksum"));
							newContactAttribute.setrNickName(responseJson
									.optString("name"));
							newContactAttribute.setrCompany(responseJson
									.optString("com"));
							newContactAttribute.setrDepartment(responseJson
									.optString("dep"));
							newContactAttribute.setrPosition(responseJson
									.optString("title"));
							localStore.updateContactInfoByEmail(
									mAccount.getEmail(), newContactAttribute,false);
							localStore.insertContactRemark( newContactAttribute);
						}
						//如果用户成功提交了邀请码，由于服务端异步处理无法返回邀请码给客户端，故取到提交时的邀请码保存
//						if (!TextUtils.isEmpty(contact.getInvitationCode())) {
//							mAccount.setAlreadyInvationCode(contact.getInvitationCode());
//							mAccount.save(Preferences.getPreferences(mApplication));
//						}
						for (MessagingListener l : getListeners(null)) {
							l.uploadUserInfoSuccess(mAccount, newContactAttribute);
						}
					}else {
						Log.i(MailChat.LOG_COLLECTOR_TAG,">>uploadUserInfo error"+responseJson.toString());
					}
//					ContactAttribute contactAttribute=localStore.getContactAttribute(mAccount.getEmail());
					//更新联系人列表头像
					tempContactList.add(addTempContact(mAccount.getEmail(),null));
					if (tempContactList.size()>0) {
						reflashContactList(mAccount,tempContactList);
					}
				} catch (MessageException e) {
					for (MessagingListener l : getListeners(null)) {
						l.uploadUserInfoFailed(mAccount);
					}
					e.printStackTrace();
				} catch (UnavailableStorageException e) {
					for (MessagingListener l : getListeners(null)) {
						l.uploadUserInfoFailed(mAccount);
					}
					e.printStackTrace();
				} catch (MessagingException e) {
					for (MessagingListener l : getListeners(null)) {
						l.uploadUserInfoFailed(mAccount);
					}
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 
	 * method name: loadRemoteUserInfo function @Description: TODO Parameters
	 * and return values description：
	 * 
	 * @param mAccount
	 * @param listener
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-4-17 下午5:35:19 @Modified by：zhangjx
	 * @Description：获取用户头像和姓名信息
	 */
	public void loadRemoteUserInfo(final Context context,final Account mAccount,
			final MessagingListener listener) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				for (MessagingListener l : getListeners(null)) {
					l.loadUserInfoStart(mAccount);
				}
				JSONObject responseJson = null;
				ContactAttribute newContactAttribute = null;
				try {
					responseJson = Protocol.getInstance().loadRemoteUserInfo(
							mAccount.getEmail());
					if (responseJson.optInt("result") == 1) {
						newContactAttribute = new ContactAttribute();
						String imgHash=responseJson.optString("checksum");
						String nickName=responseJson.optString("name");
						String company=responseJson.optString("com");
						String department=responseJson.optString("dep");
						String position=responseJson.optString("title");
						String c1=responseJson.optString("c1");
						String c3=responseJson.optString("c3");
						String c2=responseJson.optString("c2");
						String c4=responseJson.optString("c4");
						String invitecode=responseJson.optString("invitecode");
						String msg = "";
						if(!StringUtils.isNullStrOrEmpty(invitecode)){
							if (!StringUtils.isNullStrOrEmpty(c1)){
								//此邮箱已经注册。。。。
								msg = context.getString(R.string.invitat_email_already);
							}else if(!StringUtils.isNullStrOrEmpty(c2)){
								//此邮箱已被邀请过
								msg = context.getString(R.string.invitat_email_already);
							}else if(!StringUtils.isNullStrOrEmpty(c3)){
								//此设备已经注册。。。。
								msg = context.getString(R.string.invitat_device_already);
							}else if(!StringUtils.isNullStrOrEmpty(c4)){
								//此设备已被邀请过
								msg = context.getString(R.string.invitat_device_already);
							}
							mAccount.setAlreadyInvationCode(invitecode+msg);
						}else {
							mAccount.setAlreadyInvationCode(null);
						}
						mAccount.save(Preferences.getPreferences(mApplication));
						//保存第一次获取到的邀请码，避免新设备先登录旧账号（已被邀请）后再登录新账号（未被邀请），可以输入邀请码的问题
						//没数据就保存，如果有保存过就不保存了
						if (TextUtils.isEmpty(MailChat
								.getPhoneAlreadyInvationed())) {
							MailChat.setPhoneAlreadyInvationed(invitecode + msg);
							Editor editor = Preferences
									.getPreferences(mApplication)
									.getPreferences().edit();
							MailChat.save(editor);
							editor.commit();
						}
						newContactAttribute.setImgHeadPath(imgHash);
						newContactAttribute.setImgHeadHash(imgHash);
						newContactAttribute.setNickName(nickName);
						newContactAttribute.setCompany(company);
						newContactAttribute.setDepartment(department);
						newContactAttribute.setPosition(position);
						newContactAttribute.setInvitationCode(invitecode);
						if (!TextUtils.isEmpty(imgHash)||!TextUtils.isEmpty(imgHash)) {
							mAccount.getLocalStore().updateContactInfoByEmail(
									mAccount.getEmail(), newContactAttribute,false);
						}
					}else {
						Log.i(MailChat.LOG_COLLECTOR_TAG,"==loadRemoteUserInfo error==>"+responseJson.toString());
					}
					//解析群组邀请数据
					List<PendingMQTTConmmand> pendingMQTTConmmands = ParseJson.parseGroupInvitation2(responseJson);
					if(pendingMQTTConmmands!=null&&pendingMQTTConmmands.size()>0){
						for(PendingMQTTConmmand pendingMQTTConmmand :pendingMQTTConmmands){
							if(connection.getClient().getClientHandle()!=null){
								connection.getClient().subscribe(pendingMQTTConmmand.getTopic(), MQTT_QOS, null, 
										new ActionListener(ActionListener.Action.SUBSCRIBE,MessagingController.this,mAccount,MQTTCommand.JOIN_GROUP,pendingMQTTConmmand));
								try {
									Thread.sleep(1);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
					for (MessagingListener l : getListeners(null)) {
						l.loadUserInfoSuccess(mAccount, newContactAttribute);
					}
				} catch (MessageException e) {
					for (MessagingListener l : getListeners(null)) {
						l.loadUserInfoFailed(mAccount);
					}
					e.printStackTrace();
				} catch (UnavailableStorageException e) {
					for (MessagingListener l : getListeners(null)) {
						l.loadUserInfoFailed(mAccount);
					}
					e.printStackTrace();
				} catch (MessagingException e) {
					for (MessagingListener l : getListeners(null)) {
						l.loadUserInfoFailed(mAccount);
					}
					e.printStackTrace();
				} catch (MqttException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
	}

	/**
	 * 
	 * method name: syncRemoteUserInfo function @Description: TODO Parameters
	 * and return values description：
	 * 
	 * @param account
	 * @param allContacts
	 * @param listener
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-4-21 下午5:38:40 @Modified by：zhangjx
	 * @Description：同步联系人信息
	 */
	public synchronized void syncRemoteUserInfo(final Account account,
			final List<ContactAttribute> allContacts, MessagingListener listener) {
		final ArrayList copyedContactList = new ArrayList<>(Arrays.asList(new Object[allContacts.size()]));
		Collections.copy(copyedContactList, allContacts);
		threadPool.execute(new Runnable() {
			private ContactAttribute newContactAttribute;

			@Override
			public void run() {
				if (copyedContactList.size() > 0) {
					try {
						JSONObject responseJson = Protocol
								.getInstance()
								.syncRemoteUserInfo(
										account.getEmail(),
										CreateJson
												.createSyncUserInfo(copyedContactList));
						if (responseJson != null) {
							if (responseJson.optInt("result") == 1) {
								if (responseJson.optString("data") != null) {
									JSONArray array = new JSONArray(
											responseJson.optString("data"));
									if (array != null) {
										LocalStore localStore = account
												.getLocalStore();
										for (int i = 0; i < array.length(); i++) {
											JSONObject jbObject = (JSONObject) array.get(i);
											newContactAttribute = new ContactAttribute();
											newContactAttribute.setImgHeadPath(jbObject
													.optString("checksum"));
											newContactAttribute.setImgHeadHash(jbObject
													.optString("checksum"));
											newContactAttribute
													.setNickName(jbObject
															.optString("name"));
											newContactAttribute.setEmail(jbObject
													.optString("email"));
											newContactAttribute.setCompany(jbObject.optString("com"));
											newContactAttribute.setDepartment(jbObject.optString("dep"));
											newContactAttribute.setPosition(jbObject.optString("title"));
											newContactAttribute.setUsedMailchat(true);
											localStore.updateContactInfoByEmail(
													jbObject.optString("email"),
													newContactAttribute, false);
										}
									}
								}
								reflashContactList(account, null);
							}
						}
					} catch (MessageException e) {
						e.printStackTrace();
					} catch (UnavailableStorageException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MessagingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		});
	}
	private ContactAttribute addTempContact(String email,String imgHash){
		tempContactList.clear();
		ContactAttribute contact=new ContactAttribute();
		contact.setEmail(email);
		if (imgHash!=null) {
			contact.setImgHeadHash(imgHash);
		}else {
		}
		return contact;
	}
	private void reflashContactList(Account account,List<ContactAttribute> contactList){
		for (MessagingListener l : getListeners(null)) {
			 l.addContactFinish(account,contactList);
       }
	}

	/**
	 * 当联系人未存到本地时,去服务器获取头像及昵称
	 * 
	 * @Description:
	 * @param account
	 * @param email
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-5-25
	 */
	public void syncRemoteDChatUserInfo(final Account account,final String email){
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					JSONObject jsonObject = new JSONObject();
					JSONArray jsonArray = new JSONArray();
					JSONObject json = new JSONObject();
					json.put("e", email);
					json.put("a", "");
					jsonArray.put(json);
					jsonObject.put("data", jsonArray);
					JSONObject	responseJson = Protocol.getInstance().syncRemoteUserInfo(email,jsonObject);
					JSONArray array=new JSONArray(responseJson.optString("data"));
					if(array!=null&&array.length()>0){
						JSONObject jbObject=array.getJSONObject(0);
						ContactAttribute newContactAttribute = new ContactAttribute();
						newContactAttribute.setImgHeadPath(jbObject.optString("checksum"));
						newContactAttribute.setImgHeadHash(jbObject
								.optString("checksum"));
						newContactAttribute.setNickName(jbObject
								.optString("name"));
						newContactAttribute.setEmail(jbObject
								.optString("email"));
						newContactAttribute.setUsedMailchat(true);
						account .getLocalStore().updateContactInfoByEmail(
								jbObject.optString("email"), newContactAttribute,true);
						for (MessagingListener l : getListeners(null)) {
							l.updateChattingImgHead(account,newContactAttribute);
						}
					}
				} catch (MessageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnavailableStorageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	public String getCMemberAvatarHash(Account account,String email){
		String avatarHash =null;
		try {
			avatarHash = account.getLocalStore().getCMemberAvatarHash(email);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			avatarHash=null;
		}
		return avatarHash;
	}
/**
 * 
 * method name: updateFolderPushState
 * function @Description: TODO
 * Parameters and return values description:
 *      @param account
 *      @param folderName
 *      @param isAllowFolderPush
 *      @param listener   field_name
 *      void   return type
 *  @History memory：
 *     @Date：2015-6-11 下午3:54:08	@Modified by：zhangjx
 *     @Description:
 */
	public void updateFolderPushState(final Account account,final String folderName,final boolean isAllowFolderPush,
				final MessagingListener listener) {
			threadPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
					     LocalFolder localFolder = (LocalFolder)account.getLocalStore().getFolder(folderName);
					     ((LocalFolder) localFolder).setFolderIsAllowPush(isAllowFolderPush);
					} catch (Exception e) {
					}
				}
			});
	}
	/**
	 * 搜索聊天混合消息（转发）
	 *
	 * @Description:
	 * @param account
	 * @param List<MixedChatting>
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-6-17
	 */
	public void searchMixedChattingByForward(final Account account,final String tempKeyWord){
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					LocalStore localStore = account.getLocalStore();
					List<MixedChatting> mixedChattings = localStore.searchMixedChattingByForward(tempKeyWord);
					for (MessagingListener l : getListeners(null)) {
						l.searchMixedChattingByForwardFinished(account, mixedChattings);
					}
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	/**
	 * 搜索联系人（转发）
	 *
	 * @Description:
	 * @param account
	 * @param List<MixedChatting>
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-6-17
	 */
	public void searchContactsByForward(final Account account,final String tempKeyWord){
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					LocalStore localStore = account.getLocalStore();
					List<ContactAttribute> contacts = localStore.searchContacts(tempKeyWord,false);
					for (MessagingListener l : getListeners(null)) {
						l.searchContactsByForwardFinished(account, contacts);
					}
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 
	 * method name: getCoustomFoldersCount function @Description: TODO
	 * Parameters and return values description:
	 * 
	 * @param mAccount
	 * @param mListener
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-6-30 下午5:54:55 @Modified by：zhangjx
	 * @Description:获取自定义邮件夹个数
	 */
	public void getCoustomFoldersCount(final Account mAccount,
			final MessagingListener mListener) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					LocalStore localStore = mAccount.getLocalStore();
					int count = localStore.getCoustomFoldersCount();
					for (MessagingListener l : getListeners(mListener)) {
						l.getCoustomFoldersCountFinished(mAccount, count);
					}
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}
		});
	}
	/**
	 * 更新群输入模式
	 *
	 * @Description:
	 * @param account
	 * @param cGroupUid
	 * @param inputMode
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-7-8
	 */
	public void updateCGroupInputMode(Account account,String cGroupUid,int inputMode){
		try {
			account.getLocalStore().updateCGroupInputMode(cGroupUid, inputMode);
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 更新单聊输入模式
	 *
	 * @Description:
	 * @param account
	 * @param cGroupUid
	 * @param inputMode
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-7-8
	 */
	public void updateDChatInputMode(Account account,String dChatUid,int inputMode){
		try {
			account.getLocalStore().updateDChatInputMode(dChatUid, inputMode);
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 保存单聊
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-7-27
	 */
	public void saveDChat(Account account,DChat dchat){
		try {
			account.getLocalStore().persistDChatList(dchat);
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 获取邮洽小助手单聊条目
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-8-4
	 */
	public DChat getHelpDChat(Account account){
		DChat dchat =null;
		try {
			LocalStore localStore = account.getLocalStore();
			//生成邮洽小助手
			String dChatUuid = DChat.getDchatUid(account.getEmail()+","+GlobalConstants.HELP_ACCOUNT_EMAIL);
			DChat localDChat = localStore.getDChat(dChatUuid);
			if(localDChat==null){
				dchat = new DChat();
				dchat.setUid(DChat.getDchatUid(account.getEmail()+","+GlobalConstants.HELP_ACCOUNT_EMAIL));
				dchat.setEmail(GlobalConstants.HELP_ACCOUNT_EMAIL);
				dchat.setNickName(mApplication.getString(R.string.mailchat_help));
				dchat.setVisibility(false);
				dchat.setDChatAlert(true);
				dchat.setSticked(false);
				dchat.setLastMessage(mApplication.getString(R.string.help_mailchat_message));
				dchat.setLastTime(System.currentTimeMillis());
				localStore.persistDChatList(dchat);
				DChatMessage dmessage = new DChatMessage(DChatMessage.Type.TEXT);
				dmessage.setUuid(UUID.randomUUID().toString());
				dmessage.setDchatUid(dChatUuid);
				dmessage.setSenderEmail(GlobalConstants.HELP_ACCOUNT_EMAIL);
				dmessage.setReceiverEmail(account.getEmail());
				dmessage.setTime(dchat.getLastTime());
				dmessage.setMessageContent(mApplication.getString(R.string.help_mailchat_message));
				localStore.persistDChatMessage(dmessage);
			}else{
				dchat=localDChat;
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dchat;
	}

	/**
	 * 保存洽小助手为联系人，如果联系人未存储请求更新联系人头像，昵称信息
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-12-29
	 */
	public void synAndGetHelpContactAttribute(Account account){
		try {
			LocalStore localStore = account.getLocalStore();
			ContactAttribute helpContact = localStore.getContactAttribute(GlobalConstants.HELP_ACCOUNT_EMAIL,false);
			if(helpContact==null){
				helpContact = createHelpContact(account);
				localStore.savaContactAttribute(helpContact,false,false);
				syncRemoteDChatUserInfo(account, helpContact.getEmail());
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 获使用邮洽的联系人，如果联系人未存储请求更新联系人头像，昵称信息
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-8-4
	 */
	public void synAndGetContactAttributesByUsedMailChat(Account account){
		synAndGetHelpContactAttribute(account);
		getContactAttributesByUsedMailChat(account);
	}
	/**
	 * 获使用邮洽的联系人
	 *
	 * @Description:
	 * @param account
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-8-4
	 */
	public void getContactAttributesByUsedMailChat(final Account account){
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					List<ContactAttribute> localContacts = account.getLocalStore().getContactsByUsedMailChat();
					int helpIndex =-1;
					for(ContactAttribute contactAttribute:localContacts){
						helpIndex++;
						if(contactAttribute.getEmail().equals(GlobalConstants.HELP_ACCOUNT_EMAIL)){
							Collections.swap(localContacts, 0, helpIndex);
							break;
						}
					}
					ArrayList<ContactAttribute> contacts = new ArrayList<ContactAttribute>();
					int i=0;
					for(ContactAttribute contact:localContacts){
						if(!contact.getEmail().equals(account.getEmail())){
							i++;
							contacts.add(contact);
							if(i>3){
								break;
							}
						}
					}
					for (MessagingListener l : getListeners(null)) {
						l.getContactsByUsedMailChatFinished(account,contacts);
					}
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	public void inviteActionUsers(final Account account,final String invitAtionEmails){
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Protocol.getInstance().inviteAtionUsers(account.getEmail(), invitAtionEmails, ((MailChat) mApplication).getLanguage());
					for (MessagingListener l : getListeners(null)) {
						l.inviteActionUsersSuccess(account);
					}
				} catch (MessageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					for (MessagingListener l : getListeners(null)) {
						l.inviteActionUsersFailed(account);
					}
				}
			}
		});
	}

	public void setDefaultFoldersPushValue(Account account) {
		try {
			account.getLocalStore().setDefaultFoldersPushValue(account);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 创建邮洽小助手联系人
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-8-3
	 */
	private ContactAttribute createHelpContact(Account account){
		ContactAttribute contact=new ContactAttribute();
		contact.setEmail(GlobalConstants.HELP_ACCOUNT_EMAIL);
		contact.setNickName(mApplication.getString(R.string.mailchat_help));
		contact.setUsedMailchat(true);
		return contact;
	}

	/**
	 * 
	 * method name: list35Eis function @Description: TODO Parameters and return
	 * values description:
	 * 
	 * @param account
	 * @param syncRightNow
	 *            是否马上立刻请求，用于下拉或者获取数据失败的情况下
	 * @param mListener
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-29 下午2:03:01 @Modified by：zhangjx
	 * @Description:
	 */
	public void list35Eis(final Account account,final boolean syncRightNow,final boolean  isInviteChat,
			final MessagingListener mListener) {
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				for (MessagingListener l : getListeners(null)) {
					l.list35EisStart(account);
				}
				List<ContactAttribute> resultList = null;
				try {
					LocalStore localStore = account.getLocalStore();
					long executTime = System.currentTimeMillis();
					if(executTime-account.getbContactUpdateTime()>12*60*60*1000||syncRightNow){//超过12小时
						JSONObject json = Protocol.getInstance().sync35EisList(account,false);
						if (json!=null) {
							//传入我们的普通bean，转化为我们排序后的tree
//						resultList =TreeHelper.getSortedEis(ParseJson.parse35EisList(json), 0);
							//保存到本地
							localStore.cleanAllAndSaveAllBContact(json);
							//保存更新时间
							account.setbContactUpdateTime(executTime);
							account.setCompanyName(json.optString("name"));
							account.save(Preferences.getPreferences(mApplication));
						}
						resultList = TreeHelper.getSortedEis(localStore.listEis35Beans(false), 0);
						//###############eis緩存不為空的時候，清空####################
						if (tempEisContactsMap!=null&&tempEisContactsMap.get(account.getEmail())!=null) {
							tempEisContactsMap.get(account.getEmail()).clear();
							//eis搜索緩存集合不為空，清空
							if (tempSearchEisContactsMap!=null&&tempSearchEisContactsMap.get(account.getEmail())!=null) {
								tempSearchEisContactsMap.get(account.getEmail()).clear();
							}
						}
						tempEisContactsMap.put(account.getEmail(),  resultList);
						for (MessagingListener l : getListeners(null)) {
							l.list35EisSuccess(account, resultList);
						}
					}else{
						if (tempEisContactsMap!=null&&tempEisContactsMap.get(account.getEmail())!=null) {
							resultList=tempEisContactsMap.get(account.getEmail());
						}else {
							//获取本地企业联系人数据
							List<Eis35Bean> datas=localStore.listEis35Beans(false);
							resultList = TreeHelper.getSortedEis(datas, 0);
							tempEisContactsMap.put(account.getEmail(),  resultList);
						}
						for (MessagingListener l : getListeners(null)) {
							l.list35EisSuccess(account, resultList);
						}
						reflashContactList(account, null);
					}
				} catch (MessageException e) {
					e.printStackTrace();
				} catch (MessagingException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		});
	}
	private void cleanTempSearchContact(Account account){
		if (tempSearchEisContactsMap!=null&&tempSearchEisContactsMap.get(account.getEmail())!=null) 
		tempSearchEisContactsMap.get(account.getEmail()).clear();
		if (tempSearchPerssionContactsMap!=null&&tempSearchPerssionContactsMap.get(account.getEmail())!=null) 
	    tempSearchPerssionContactsMap.get(account.getEmail()).clear();
		if (tempSearchSameContactsMap!=null&&tempSearchSameContactsMap.get(account.getEmail())!=null) 
	    tempSearchSameContactsMap.get(account.getEmail()).clear();
	}
	/**
	 * 更新单聊消息未读标记
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-8-27
	 */
	public void updateDChatMessageReadFlag(Account account,String dChatMessageUid,int readFlag){
		try {
			account.getLocalStore().updateDChatMessageReadFlag(dChatMessageUid, readFlag);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * method name: listLocalPersonalContactForView function @Description: TODO
	 * Parameters and return values description:
	 * 
	 * @param account
	 * @param listener
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-24 下午6:58:34 @Modified by：zhangjx
	 * @Description:获取个人联系人
	 */
	public void listLocalPersonalContactForView(final Account account,final boolean syncRightNow,final boolean  isInviteChat,
			MessagingListener listener) {
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				for (MessagingListener l : getListeners(null)) {
					l.loadPersonalContactForViewStarted(account);
				}
				if (account != null) {
					List<ContactAttribute> personalContact=null;
					LocalStore localStore;
					try {
						localStore = account.getLocalStore();
						if (syncRightNow) {
							 personalContact = localStore
									.searchLocalPersonalContacts(
											"@"
													+ Utility
															.getEmailDomain(account
																	.getEmail()),
											"",isInviteChat,account.getVersion_35Mail()==1);
							Collections.sort(personalContact,new ContactComparator());
							//###############PersionContacts緩存不為空的時候，清空####################
							if (tempPersionContactsMap!=null&&tempPersionContactsMap.get(account.getEmail())!=null) {
								tempPersionContactsMap.get(account.getEmail()).clear();
								if (tempSearchPerssionContactsMap!=null&&tempSearchPerssionContactsMap.get(account.getEmail())!=null) {
									tempSearchPerssionContactsMap.get(account.getEmail()).clear();
								}
							}
							tempPersionContactsMap.put(account.getEmail(), personalContact);
						}else {
							//如果緩存有數據
							if (tempPersionContactsMap!=null&&tempPersionContactsMap.get(account.getEmail())!=null) {
								personalContact=tempPersionContactsMap.get(account.getEmail());
							}else{
								 personalContact = localStore
											.searchLocalPersonalContacts(
													"@"
															+ Utility
																	.getEmailDomain(account
																			.getEmail()),
													"",isInviteChat,account.getVersion_35Mail()==1);
									Collections.sort(personalContact,new ContactComparator());
									tempPersionContactsMap.put(account.getEmail(),  personalContact);
							}
						}
						if (isInviteChat) {
							//过滤掉已使用过邮洽的contact
							for (MessagingListener l : getListeners(null)) {
								l.loadPersonalContactWithoutUsedFinished(
										account, personalContact);
							}
						} else {
							for (MessagingListener l : getListeners(null)) {
								l.loadPersonalContactForViewFinished(account,personalContact);
							}
						}
					} catch (MessagingException e) {
						for (MessagingListener l : getListeners(null)) {
							l.loadPersonalContactForViewFailed(account);
						}
						e.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * 
	 * method name: listLocalSameDomainContactForView function @Description:
	 * TODO Parameters and return values description:
	 * 
	 * @param account
	 * @param listener
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-24 下午6:58:57 @Modified by：zhangjx
	 * @Description:获取同域联系人
	 */
	public void listLocalSameDomainContactForView(final Account account,final boolean syncRightNow,final boolean  isInviteChat,
			MessagingListener listener) {
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				for (MessagingListener l : getListeners(null)) {
					l.loadSameDomainContactForViewStarted(account);
				}
				if (account != null) {
					List<ContactAttribute> sameDomainContact=null;
					LocalStore localStore;
					try {
						localStore = account.getLocalStore();
						if (syncRightNow) {
						sameDomainContact = localStore
									.searchLocalSameDomainContacts(
											"@"
													+ Utility
															.getEmailDomain(account
																	.getEmail()),
											"",isInviteChat);
							Collections.sort(sameDomainContact,new ContactComparator());
							//###############SameContacts緩存不為空的時候，清空####################						
							if (tempSameContactsMap!=null&&tempSameContactsMap.get(account.getEmail())!=null) {
								tempSameContactsMap.get(account.getEmail()).clear();
								if (tempSearchSameContactsMap!=null&&tempSearchSameContactsMap.get(account.getEmail())!=null) {
									tempSearchSameContactsMap.get(account.getEmail()).clear();
								}
							}
							tempSameContactsMap.put(account.getEmail(), sameDomainContact);
						}else {
							if (tempSameContactsMap!=null&&tempSameContactsMap.get(account.getEmail())!=null) {
								sameDomainContact=tempSameContactsMap.get(account.getEmail());
							}else{
								sameDomainContact = localStore
										.searchLocalSameDomainContacts(
												"@"
														+ Utility
																.getEmailDomain(account
																		.getEmail()),
												"",isInviteChat);
								Collections.sort(sameDomainContact,new ContactComparator());
								tempSameContactsMap.put(account.getEmail(),  sameDomainContact);
							}
						}
						if (isInviteChat) {
							//过滤掉已使用过邮洽的contact
							for (MessagingListener l : getListeners(null)) {
								l.loadSameDomainContactWithoutUsedFinished(account,
										sameDomainContact);
							}
						} else {
							for (MessagingListener l : getListeners(null)) {
								l.loadSameDomainContactForViewFinished(account,
										sameDomainContact);
							}
						}

					} catch (MessagingException e) {
						for (MessagingListener l : getListeners(null)) {
							l.loadSameDomainContactForViewFailed(account);
						}
						e.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * 
	 * method name: updateEisListExpandState function @Description: TODO
	 * Parameters and return values description:
	 * 
	 * @param account
	 * @param id
	 * @param isExpand
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-24 下午6:59:24 @Modified by：zhangjx
	 * @Description:更新eis被展开标识
	 */
	public void updateEisListExpandState(Account account, String id,
			boolean isExpand) {
		try {
			account.getLocalStore().updateEisListExpandState(id, isExpand);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * method name: updateContactVisibilityFlag function @Description: TODO
	 * Parameters and return values description:
	 * 
	 * @param account
	 * @param email
	 * @param isVisibility
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-24 下午6:59:56 @Modified by：zhangjx
	 * @Description:删除联系人，即标识不显示
	 */
	public void updateContactVisibilityFlag(Account account, String email,
			boolean isVisibility) {
		try {
			account.getLocalStore().updateContactVisibilityFlag(email,
					isVisibility);
			reflashContactList(account, null);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * method name: saveContactLocal function @Description: TODO Parameters and
	 * return values description:
	 * 
	 * @param account
	 * @param contact
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-24 下午7:00:24 @Modified by：zhangjx
	 * @Description:新增联系人
	 */
	public void saveContactLocal(Account account, ContactAttribute contact,boolean isEditModel) {
		try {
			LocalStore localStore=account.getLocalStore();
			if (isEditModel) {
				if(!contact.isEisContact()){
					//编辑下，并且非eis联系人更新
					localStore.updateContactAttribute(contact);
				}else{
					//编辑下，并且eis联系人
					localStore.savaContactAttribute(contact, true,false);
				}
			}else {
				//新增模式
				//不管是eis还是个人通讯录联系人，都将其保持到普通联系人表以及本地remark表中
				if(localStore.isContactAttributeExist(contact.getEmail(), account.getVersion_35Mail()==1)){
					for (MessagingListener l : getListeners(null)) {
						l.AddContactHasExist(account);
					}
					return;
				}else{
					localStore.savaContactAttribute(contact, true,true);
				}
			}
			//查询刚插入的联系人
			ContactAttribute returnNewContact=localStore.getContactAttribute(contact.getEmail(),contact.isEisContact());
			for (MessagingListener l : getListeners(null)) {
				l.addContactSuccess(account,returnNewContact,isEditModel);
			}
			if (contact.isEisContact()) {
				// 更新缓存中的联系人信息
				List<ContactAttribute> contactAttributes = tempEisContactsMap
						.get(account.getEmail());
				if (contactAttributes != null) {
					cleanTempSearchContact(account);
					// List<ContactAttribute> resultList =
					// TreeHelper.getSortedEis(localStore.listEis35Beans(false),
					// 0);
					// tempEisContactsMap.put(account.getEmail(), resultList);

					for (int i = 0; i < contactAttributes.size(); i++) {
						if (contact.getEmail()
								.equals(contactAttributes.get(i).getEmail())) {
							ContactAttribute newContact = contactAttributes
									.get(i);
							newContact.setId(contact.getId());
							newContact.setEmail(contact.getEmail());
							newContact.setEisContact(true);
							String imgHash=contact.getImgHeadHash();
//							newContact.setImgHeadUrl(imgHash);
							if (!StringUtils.isNullOrEmpty(imgHash)&&!"null".equals(imgHash)) {
								newContact.setImgHeadHash(imgHash);
								if (imgHash.startsWith("http")) {
									newContact.setImgHeadPath(imgHash);
								} else if (!TextUtils.isEmpty(imgHash)) {
									newContact.setImgHeadPath(GlobalConstants.HOST_IMG
											+ imgHash);
								}
							}
							newContact.setEisContact(contact.isEisContact());
							newContact.setrNickName(contact.getrNickName());
							newContact.setrCompany(contact.getrCompany());
							newContact.setrDepartment(contact.getrDepartment());
							newContact.setrPosition(contact.getrPosition());
							newContact.setrPhones(contact.getrPhones());
							newContact.setrAddr(contact.getrAddr());
							newContact.setrOtherRemarks(contact
									.getrOtherRemarks());
							newContact.setrEmail(contact.getrEmail());
						}
					}
					for (MessagingListener l : getListeners(null)) {
						l.list35EisSuccess(account, contactAttributes);
					}
				}
			}
			ContactAttribute tempContact = addTempContact(contact.getEmail(),null);
			tempContactList.add(tempContact);
			// 触发请求服务端的用户头像
			reflashContactList(account, tempContactList);
		} catch (MessagingException e) {
			for (MessagingListener l : getListeners(null)) {
				l.addContactFailed(account);
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	/**
	 * 添加小助手，并让其显示
	 * 
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-9-14
	 */
	public void addHelpDChatForChatList(Account account) {
		try {
			DChat helpDChat = getHelpDChat(account);
			if (!helpDChat.isVisibility()) {
				LocalStore localStore = account.getLocalStore();
				localStore.updateDchatDeleteFlag(
						DChat.getDchatUid(account.getEmail() + ","
								+ GlobalConstants.HELP_ACCOUNT_EMAIL), true);
				//如果显示就去获取小助手相关信息
				synAndGetHelpContactAttribute(account);
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 清空单聊消息
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-9-18
	 */
	public void deleteAllDMessage(final Account account,final String dChatUid){
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					LocalStore localStore = account.getLocalStore();
					localStore.updateAllDchatMessageFlag(dChatUid, 1);
					for (MessagingListener l : getListeners(null)) {
						l.deleteAllDMessageFinished(account,dChatUid);
					}
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					for (MessagingListener l : getListeners(null)) {
						l.deleteAllDMessageFailed(account,dChatUid);
					}
				}
			}
		});
	}
	/**
	 * 清空群聊消息
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-9-18
	 */
	public void deleteAllCMessage(final Account account, final String cGroupUid) {
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					LocalStore localStore = account.getLocalStore();
					localStore.updateAllCMessageDeleteFlag(cGroupUid, 1);
					for (MessagingListener l : getListeners(null)) {
						l.deleteAllCMessageFinished(account, cGroupUid);
					}
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					for (MessagingListener l : getListeners(null)) {
						l.deleteAllCMessageFailed(account, cGroupUid);
					}
				}
			}
		});
	}
	/**
	 * 
	 * method name: loadCommonContactForView function @Description: TODO
	 * Parameters and return values description:
	 * 
	 * @param account
	 * @param listener
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-9-24 下午7:00:40 @Modified by：zhangjx
	 * @Description:获取常用联系人
	 */
	public void loadCommonContactForView(final Account account,
			MessagingListener listener) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				for (MessagingListener l : getListeners(null)) {
					l.loadCommonContactForViewStarted(account);
				}
				if (account != null) {
					LocalStore localStore;
					try {
						localStore = account.getLocalStore();
						List<ContactAttribute> commonContacts = null;
						if (account.getVersion_35Mail() == 1) {
							commonContacts = localStore
									.getImportantContactsWith35Eis(account);
						} else {
							commonContacts = localStore
									.getImportantContacts("");
						}
						//去除重复
						//极端情况一个人在N多部门，会去掉N-1个重复联系人，导致常用联系人会不足。可以从数据库查询优化
						if (commonContacts != null) {
							for (int i = 0; i < commonContacts.size() - 1; i++) {
								for (int j = commonContacts.size() - 1; j > i; j--) {
									if (commonContacts
											.get(j)
											.getEmail()
											.equals(commonContacts.get(i)
													.getEmail())) {
										commonContacts.remove(j);
									}
								}
							}
						}
						for (MessagingListener l : getListeners(null)) {
							l.loadCommonContactForViewFinished(account,
									commonContacts);
						}
					} catch (MessagingException e) {
						for (MessagingListener l : getListeners(null)) {
							l.loadCommonContactForViewFailed(account);
						}
						e.printStackTrace();
					}
				}
			}
		});
	}
	/**
	 * 根据域名判断，是否为OA用户,并添加OA条目到聊天列表
	 *
	 * @Description:
	 * @param domain
	 *            需要验证的域名
	 * @param domain
	 *            邮箱地址
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-09-21
	 */
	public void addOAItem(final Account account){
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//原始逻辑
//				if(account.isOAChecked()){
//					if(account.isOAUser()){
//						createOA(account);
//					}
//				}else{
//					boolean isOAUser = Protocol.getInstance().isOAUser(Utility.getEmailDomain(account.getEmail()), account.getEmail());
//					if(isOAUser){
//						createOA(account);
//					}
//					account.setCheckedOA(true);
//					account.setOAUser(isOAUser);
//					account.save(Preferences.getPreferences(mApplication));
//					if(isOAUser){
//						for (MessagingListener l : getListeners(null)) {
//							l.refreshMainActionBar(account);
//						}
//					}
//				}

				//由于OA服务器端更新不及时，先使用多次调用方法，来更新
				boolean isOAUser =false;
				if(!account.isOAUser()){
					isOAUser = Protocol.getInstance().isOAUser(Utility.getEmailDomain(account.getEmail()), account.getEmail());
					account.setCheckedOA(true);
					account.setOAUser(isOAUser);
					account.save(Preferences.getPreferences(mApplication));
				}
//				if(isOAUser){
					createOA(account);
					for (MessagingListener l : getListeners(null)) {
						l.refreshMainActionBar(account);
					}
//				}
			}
		});
	}
	/**
	 * 
	 * method name: searchContact
	 * function @Description: TODO
	 * Parameters and return values description:
	 *      @param account
	 *      @param keyWord 搜索关键词
	 *      @param isInviteContact 是否邀请联系人，将过滤掉已使用邮洽的用户
	 *      @param listener   field_name
	 *      void   return type
	 *  @History memory：
	 *     @Date：2015-11-16 下午3:15:32	@Modified by：zhangjx
	 *     @Description:搜索联系人
	 */
	public void searchContact(final Account account, final String keyWord,final boolean isInviteContact,final boolean isFirstLoadAllData,final String currFragmentTag,
			final MessagingListener listener) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				for (MessagingListener l : getListeners(listener)) {
					l.searchContactStart(account);
				}
				if (account != null) {
					LocalStore localStore;
					try {
						localStore = account.getLocalStore();
						List<ContactAttribute> commonContacts = null;
						List<ContactAttribute> personalContacts = null;
						List<ContactAttribute> eisContacts = null;
						List<ContactAttribute> sameDomainContacts = null;
						// 35-3.0账号，查eis和个人通讯录
						if (account.getVersion_35Mail() == 1) {
							if (isInviteContact) {
							//邀请联系人的界面，将过滤掉已使用过邮洽的联系人
//								if (keyWord.equals("")&&isFirstLoadAllData) {
//									tempSearchEisContactsMap.remove(account.getEmail());
//								}
								if (tempSearchEisContactsMap.get(account.getEmail())!= null) {
									eisContacts = getTempContact(account.getEmail(),
											keyWord, tempSearchEisContactsMap,true);
								}else {
									try {
										eisContacts = TreeHelper.getSortedEis(
												localStore
														.listEis35Beans(isInviteContact),
												0);
									} catch (IllegalArgumentException e) {
										e.printStackTrace();
									} catch (IllegalAccessException e) {
										e.printStackTrace();
									}
								}
							}else {
								//非邀请联系人的界面
								//取缓存中的联系人
								if (tempEisContactsMap.get(account.getEmail())!= null) {
									eisContacts = getTempContact(account.getEmail(),
											keyWord, tempEisContactsMap,true);
								}else {
									// 获取本地企业联系人数据
									try {
										eisContacts = TreeHelper.getSortedEis(
												localStore.listEis35Beans(false), 0);
									} catch (IllegalArgumentException e) {
										e.printStackTrace();
									} catch (IllegalAccessException e) {
										e.printStackTrace();
									}
								}
							}
						} else {
							// 获取本地相同域的联系人
							if (isInviteContact) {
								//邀请联系人的界面，将过滤掉已使用过邮洽的联系人
//								if (keyWord.equals("")&&isFirstLoadAllData) {
//									tempSearchSameContactsMap.remove(account.getEmail());
//								}
								if (tempSearchSameContactsMap.get(account.getEmail())!= null) {
								sameDomainContacts = getTempContact(account.getEmail(), keyWord,
										tempSearchSameContactsMap,false);
								}else {
									sameDomainContacts = localStore.searchLocalSameDomainContacts(
											"@"
													+ Utility
													.getEmailDomain(account
															.getEmail()),
															keyWord.trim().toLowerCase(
																	Locale.ENGLISH), isInviteContact);
								}
							}else {
								//非邀请联系人的界面
								if (tempSameContactsMap.get(account.getEmail())!= null) {
									sameDomainContacts = getTempContact(
											account.getEmail(), keyWord,
											tempSameContactsMap,false);
								}else {
									sameDomainContacts = localStore.searchLocalSameDomainContacts(
											"@"
													+ Utility
													.getEmailDomain(account
															.getEmail()),
															keyWord.trim().toLowerCase(
																	Locale.ENGLISH), false);
								}
							}
						}
						//取缓存中的联系人
						if (isInviteContact) {
							//邀请联系人的界面，将过滤掉已使用过邮洽的联系人
//							if (keyWord.equals("")&&isFirstLoadAllData) {
//								tempSearchPerssionContactsMap.remove(account.getEmail());
//							}
							if (tempSearchPerssionContactsMap.get(account.getEmail())!= null) {
								personalContacts = getTempContact(account.getEmail(),
										keyWord, tempSearchPerssionContactsMap,false);
							}else{
								personalContacts = localStore.searchLocalPersonalContacts(
										"@"
												+ Utility.getEmailDomain(account
														.getEmail()),
														keyWord.trim().toLowerCase(Locale.ENGLISH),
														isInviteContact,account.getVersion_35Mail()==1);
							}
						}else {
							if (tempPersionContactsMap.get(account.getEmail())!= null) {
								personalContacts = getTempContact(account.getEmail(),
										keyWord, tempPersionContactsMap,false);
							}else{
								// 搜索个人通讯录
								personalContacts = localStore.searchLocalPersonalContacts(
										"@"
												+ Utility.getEmailDomain(account
														.getEmail()),
										keyWord.trim().toLowerCase(Locale.ENGLISH),
										false,account.getVersion_35Mail()==1);
							}
						}
						if (!isInviteContact) {
							// 搜索常用联系人
							commonContacts = localStore
									.searchImportantContactsWith35Eis(keyWord
											.trim().toLowerCase(Locale.ENGLISH));
						}
						if (StringUtil.isEmpty(keyWord)) {
							if (personalContacts != null) {
								Collections.sort(personalContacts,
										new ContactComparator());
							}
							if (sameDomainContacts != null) {
								Collections.sort(sameDomainContacts,
										new ContactComparator());
							}
						}
						for (MessagingListener l : getListeners(listener)) {
							l.searchContactSuccess(account,currFragmentTag, eisContacts,
									commonContacts, personalContacts,
									sameDomainContacts);
						}
					} catch (MessagingException e) {
						for (MessagingListener l : getListeners(listener)) {
							l.loadCommonContactForViewFailed(account);
						}
						e.printStackTrace();
					}
				}
			}
		});
	}
	public List<ContactAttribute> getTempContact(String email, String keyWord,
			Map<String, List<ContactAttribute>> tempContactAttribute,boolean isEis) {
		List<ContactAttribute> results = new ArrayList<ContactAttribute>();
		List<ContactAttribute> resultContacts = new ArrayList<ContactAttribute>();
		if (tempContactAttribute.size()>0) {
			results.clear();
			resultContacts.clear();
		// 无搜索关键词
		if (StringUtil.isEmpty(keyWord) && email != null) {
			// 取得缓存的联系人
			if (isEis) {
				//eis的不需要排序
				resultContacts =tempContactAttribute.get(email);
			}else{
				resultContacts = restructureContacts(email, tempContactAttribute);
			}
			results=resultContacts;
		} else {
			// 有关键词
			// 取得缓存的联系人
			if (isEis) {
				//eis的联系人不需要部门
				resultContacts =getAllchildList(email, tempContactAttribute);
			}else{
				resultContacts = restructureContacts(email, tempContactAttribute);
			}
			// 进行email、name、nickName匹配
			keyWord = keyWord.trim().toLowerCase(Locale.ENGLISH);
			for (ContactAttribute contact : resultContacts) {
				if (contact.getEmail()!=null&&contact.getEmail().toLowerCase(Locale.ENGLISH)
						.contains(keyWord)||(contact.getNickName()!=null&&contact.getNickName().toLowerCase(Locale.ENGLISH)
								.contains(keyWord))||(contact.getName()!=null&&contact.getName().toLowerCase(Locale.ENGLISH)
								.contains(keyWord))){
							results.add(contact);
						}
			}
			// 回收
			resultContacts.clear();
		}
		}
		return results;
	}

	synchronized public List<ContactAttribute> getAllchildList(String email,
			Map<String, List<ContactAttribute>> tempContactAttribute) {
		List<ContactAttribute> childContactAttributes = new ArrayList<ContactAttribute>();
		List<ContactAttribute> allContactAttributes = tempContactAttribute
				.get(email);
		for (ContactAttribute contact : allContactAttributes) {
			if (contact.isLeaf()) {
				childContactAttributes.add(contact);
			}
		}
		return childContactAttributes;
	}
	/**
	 * 重组联系人，且排序
	 */
	synchronized public List<ContactAttribute> restructureContacts(String email,
			Map<String, List<ContactAttribute>> tempContactAttribute) {
		List<ContactAttribute> tempContactAttributes = new ArrayList<ContactAttribute>();
		if (tempContactAttribute!=null&&tempContactAttribute.size()>0) {
			tempContactAttributes.clear();
			if (email != null&&tempContactAttribute.get(email)!=null) {
				tempContactAttributes.addAll(tempContactAttribute.get(email));
				Collections.sort(tempContactAttributes, new ContactComparator());
			}
		}
		return tempContactAttributes;
	}

	/**
	 * 
	 * method name: serchAllContacts function @Description: TODO Parameters and
	 * return values description:
	 * 
	 * @param account
	 * @param keyWord
	 * @param isSearchUnUsedMailchat
	 * @param listener
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-12-30 下午2:28:09 @Modified by：zhangjx
	 * @Description:联系人搜索
	 */
	public void serchAllContacts(final Account account, final String keyWord,
			final boolean isSearchUnUsedMailchat,
			final MessagingListener listener) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				if (account != null) {
					try {
						for (MessagingListener l : getListeners(listener)) {
							l.searchAllContactStart(account);
						}
						List<ContactAttribute> results = new ArrayList<ContactAttribute>();
						List<ContactAttribute> contactAttribute = new ArrayList<ContactAttribute>();
						LocalStore localStore = account.getLocalStore();
						contactAttribute.clear();
						results.clear();
						// 如果是35用户，搜索eis联系人和普通往来邮件截取的邮件
						if (account.getVersion_35Mail() == 1) {
							// //eis如果有緩存
							if (tempEisContactsMap != null
									&& tempEisContactsMap.get(account
											.getEmail()) != null) {
								List<ContactAttribute> tempEisContacts = tempEisContactsMap
										.get(account.getEmail());
								for (ContactAttribute contact : tempEisContacts) {
									if (!contact.isParent()) {
										contactAttribute.add(contact);
									}
								}
							} else {
								// eis沒有緩存
								try {
									contactAttribute = TreeHelper.getSortedEis(
											localStore.searchEis35Beans(
													keyWord.trim().toLowerCase(
															Locale.ENGLISH),
													isSearchUnUsedMailchat), 0);
								} catch (IllegalArgumentException
										| IllegalAccessException e) {
									e.printStackTrace();
								}
							}
						}
						//查出同域联系人 add 2016.01.15,
						//修复写信匹配联系人无法匹配到ares和apollo账号的问题
						if (tempSameContactsMap != null
								&& tempSameContactsMap.size() > 0&&tempSameContactsMap
								.get(account.getEmail())!=null&&tempSameContactsMap
								.get(account.getEmail()).size()>0) {
								contactAttribute.addAll(tempSameContactsMap
										.get(account.getEmail()));
						} else {
							// SameDomainContacts没有缓存
							contactAttribute.addAll(localStore.searchLocalSameDomainContacts(
									"@"
											+ Utility.getEmailDomain(account
													.getEmail()),
									keyWord.trim().toLowerCase(Locale.ENGLISH),
									isSearchUnUsedMailchat));
						}
						// PersionContacts如果有缓存
						if (tempPersionContactsMap != null
								&& tempPersionContactsMap.get(account
										.getEmail()) != null) {
							contactAttribute.addAll(tempPersionContactsMap
									.get(account.getEmail()));
						} else {
							// PersionContacts没有缓存
							contactAttribute.addAll(localStore.searchLocalPersonalContacts(
									"@"
											+ Utility.getEmailDomain(account
													.getEmail()),
									keyWord.trim().toLowerCase(Locale.ENGLISH),
									isSearchUnUsedMailchat, account
											.getVersion_35Mail() == 1));
						}
						for (ContactAttribute contact : contactAttribute) {
							if (contact.getEmail() != null
									&& contact
											.getEmail()
											.toLowerCase(Locale.ENGLISH)
											.contains(
													keyWord.trim().toLowerCase(
															Locale.ENGLISH))
									|| (contact.getNickName() != null && contact
											.getNickName()
											.toLowerCase(Locale.ENGLISH)
											.contains(
													keyWord.trim().toLowerCase(
															Locale.ENGLISH)))
									|| (contact.getName() != null && contact
											.getName()
											.toLowerCase(Locale.ENGLISH)
											.contains(
													keyWord.trim().toLowerCase(
															Locale.ENGLISH)))) {
								results.add(contact);
							}
						}
						Collections.sort(results, new ContactComparator());
						for (MessagingListener l : getListeners(listener)) {
							l.searchAllContactSuccess(account, results);
						}
					} catch (MessagingException e) {
						for (MessagingListener l : getListeners(listener)) {
							l.searchAllContactFailed(account);
						}
						e.printStackTrace();
					}
				}
			}
		});
	}
	public void createOA(Account account){
		try {
			LocalStore  localStore = account.getLocalStore();
			DChat localDChat = localStore.getDChat(DChat.getDchatUid(account.getEmail()+ "," + GlobalConstants.DCHAT_OA));
			if(localDChat!=null){
				if(localDChat.getLastTime()==0){
					localDChat.setLastTime(System.currentTimeMillis());
				}
				localStore.updateDchat(localDChat, 1,false);
			}else{
				DChat dChat = new DChat();
				dChat.setdChatType(DChat.Type.OA);
				dChat.setUid(DChat.getDchatUid(account.getEmail()+ "," + GlobalConstants.DCHAT_OA));
				dChat.setLastTime(System.currentTimeMillis());
				dChat.setEmail(GlobalConstants.DCHAT_OA);
				dChat.setVisibility(true);
				dChat.setDChatAlert(true);
				dChat.setSticked(false);
				localStore.persistDChatList(dChat);
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 用户认证
	 *
	 * @Description:
	 * @param isRegist
	 *            是否为注册用户
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-10-28
	 */
	public void checkUser(final boolean isRegist,final String userName,final String password){
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				String token ="";
				String userParams="";
				String cliendId = SystemUtil.getCliendId(mApplication);
				if(isRegist){
					userParams="u="+userName+"&p="+password+"&d="+cliendId;
				}else{
					userParams="d="+cliendId+"&p="+cliendId;
				}
				try {
					token= Base64.encodeToString(RSAUtils.encryptByPublicKey(userParams.getBytes(), GlobalConstants.PUBLIC_KEY),Base64.DEFAULT);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					Protocol.getInstance().userCheck(token);
				} catch (MessageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 将邮箱绑定到账户上
	 *
	 * @Description:
	 * @param isRegist
	 *            是否为注册用户
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-10-28
	 */
	public void bingEmailToUser(final Account account,final boolean isRegist,final String userName,final String password){
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				String token ="";
				String userParams="";
				String cliendId = SystemUtil.getCliendId(mApplication);
				try {
					if(isRegist){
						userParams="u="+userName+"&p"+password+"&ae="+account.getEmail()+"&ap="+Utility.getUserPassword(new URI(account.getStoreUri()));
					}else{
						userParams="d="+cliendId+"&p="+cliendId+"&ae="+account.getEmail()+"&ap="+Utility.getUserPassword(new URI(account.getStoreUri()));
					}
					token= Base64.encodeToString(RSAUtils.encryptByPublicKey(userParams.getBytes(), GlobalConstants.PUBLIC_KEY),Base64.DEFAULT);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					Protocol.getInstance().bingEmailToUser(token);
				} catch (MessageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 添加聊天列表可以跳转的条目
	 *
	 * @Description:
	 * @param account
	 *            账户
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-11-06
	 */
	public void createJumpItem(Account account,String jumpMark,String lastMessageComtent){
		try {
			LocalStore  localStore = account.getLocalStore();
			DChat localDChat = localStore.getDChat(DChat.getDchatUid(account.getEmail()+ "," + jumpMark));
			if(localDChat==null){
				DChat dChat = new DChat();
				dChat.setdChatType(DChat.Type.JUMP);
				dChat.setUid(DChat.getDchatUid(account.getEmail()+ "," + jumpMark));
				dChat.setLastTime(0);
				dChat.setEmail(jumpMark);
				dChat.setVisibility(true);
				dChat.setDChatAlert(true);
				dChat.setSticked(true);
				dChat.setStickedDate(Long.MAX_VALUE);
				dChat.setLastMessage(lastMessageComtent);
				dChat.setUnTreated(true);
				localStore.persistDChatList(dChat);
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 更新单聊条目未处理标记
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @throws UnavailableStorageException
	 * @date:2015-11-10
	 */
	public void updateDChatUnTreatedFlag(Account account,DChat dChat){
		try {
			account.getLocalStore().updateDChatUnTreatedFlag(dChat);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 获取联系人在EIS中的信息(部门职位等)
	 *
	 * @Description:
	 * @see:
	 * @since:
	 * @author: shengli
	 * @throws UnavailableStorageException
	 * @date:2015-11-10
	 */
	public void get35EisContactsInfoForJump(Account account,ContactAttribute contact,boolean isFormGroup){
		try {
			contact = account.getLocalStore().getContactsInfoForChat(contact,account.getVersion_35Mail()==1);
			if(isFormGroup){
				for (MessagingListener l:getListeners(null)) {
					l.get35EisContactsInfoForGroupSuccess(account,contact);
				}
			}else{
				for (MessagingListener l:getListeners(null)) {
					l.get35EisContactsInfoForDChatSuccess(account,contact);
				}
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(isFormGroup){
				for (MessagingListener l:getListeners(null)) {
					l.get35EisContactsInfoForGroupFailed(account, contact);
				}
			}else{
				for (MessagingListener l:getListeners(null)) {
					l.get35EisContactsInfoForDChatFailed(account, contact);
				}
			}
		}
	}

	/**
	 * 
	 * method name: getInvitationCode function @Description: TODO Parameters and
	 * return values description:
	 * 
	 * @param account
	 * @param cliendId
	 * @param listener
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-12-11 上午11:13:43 @Modified by：zhangjx
	 * @Description:获取设备唯一邀请码
	 */
	public void getInvitationCode(final Account account, final String cliendId,
			MessagingListener listener) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				for (MessagingListener l : getListeners(null)) {
					l.getInvitationCodeStart(account);
				}
				String invitationCode=account.getInvationCode();
				try {
					if (StringUtils.isNullOrEmpty(invitationCode)) {
						JSONObject json = Protocol.getInstance().getInvitationCode(account,
								cliendId);
						int result = json.getInt("result");
						if (result == 1) {
							invitationCode = json.getString("code");
							account.setInvationCode(invitationCode);
							account.save(Preferences.getPreferences(mApplication));
						}else{
							for (MessagingListener l : getListeners(null)) {
								l.getInvitationCodeFailed(account);
							}
							return;
						}
					}
					for (MessagingListener l : getListeners(null)) {
						l.getInvitationCodeSuccess(account, invitationCode);
					}
				} catch (MessageException | JSONException e) {
					for (MessagingListener l : getListeners(null)) {
						l.getInvitationCodeFailed(account);
					}
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 
	 * method name: registerNewClient function @Description: TODO Parameters and
	 * return values description:
	 * 
	 * @param cliendId
	 *            field_name void return type
	 * @History memory：
	 * @Date：2015-12-17 上午11:22:01 @Modified by：zhangjx
	 * @Description:
	 */
	public void registerNewClient(final String cliendId) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				boolean isNewClient=MailChat.isNewClient();
				try {
					if (!isNewClient) {
						Log.i(MailChat.LOG_COLLECTOR_TAG, "Register new client : " +cliendId);
						JSONObject json = Protocol.getInstance().registerNewClient(cliendId);
						int result = json.getInt("result");
						if (result == 1) {
							Log.i(MailChat.LOG_COLLECTOR_TAG, "Register new client success");
							MailChat.setNewClient(true);
						}else{
							Log.i(MailChat.LOG_COLLECTOR_TAG, "Register new client failed");
							MailChat.setNewClient(false);
						}
						Editor editor = Preferences.getPreferences(mApplication).getPreferences().edit();
						MailChat.save(editor);
						editor.commit();
					}
				} catch (MessageException | JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 检测所有用户注册push服务状态。
	 * 
	 * @Description:
	 * @param 
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2016-1-14
	 */
    private static final String REGISTER_PUSH_SYNC = "REGISTER_PUSH_SYNC";

    public void checkAllRegisterPush(){
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                synchronized(REGISTER_PUSH_SYNC) {
                    if(NetUtil.isActive()){
                        //cpu唤醒
                        TracingPowerManager pm = TracingPowerManager.getPowerManager(mApplication);
                        TracingWakeLock twakeLock = pm.newWakeLock(
                                PowerManager.PARTIAL_WAKE_LOCK,
                                "MailChat MessagingController.checkAllRegisterPush");
                        twakeLock.setReferenceCounted(false);
                        twakeLock.acquire(MailChat.PUSH_WAKE_LOCK_TIMEOUT);

                        boolean isAgain =false;
                        Preferences prefs = Preferences.getPreferences(mApplication);
                        List<Account> accounts = prefs.getAccounts();
                        for(Account account :accounts){
                            if(!account.isHideAccount()){
                                if(!account.isOnPushSuccess()){
                                    account.setOnPushSuccess(Protocol.getInstance().registerPush(account));
                                }
                                if(!account.isOnPushSuccess()){
                                    isAgain=true;
                                }
                            }
                        }
                        //如果其中账户注册失败的，30秒后再次执行
                        if(isAgain){
                            alarmTask(CHECK_PUSH_STATA, 60, PendingService.class, GlobalConstants.PUSH_CHECK_ALARM_REQUESTCODE);
                        }

                        //cpu释放
                        if (twakeLock != null) {
                            twakeLock.release();
                        }
                    }
                }
            }
        });
    }

	/**
	 * 获取账户缓存大小
	 *
	 * @Description:
	 * @param 
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2016-1-21
	 */
    public void getAccountCacheSize(final Account account){
        threadPool.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				long size =-1;
				try {
					size = account.getLocalStore().getCacheSize();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for (MessagingListener l : getListeners(null)) {
					l.getAccountCacheSize(account, size);
				}
			}
		});
	}

	/**
	 * 清除账户缓存
	 *
	 * @Description:
	 * @param
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2016-1-22
	 */
	public void cleanAccountCache(final Account account){
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				boolean isCleanSuccess = false;
				try {
					account.getLocalStore().clearCache();
					isCleanSuccess=true;
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for (MessagingListener l : getListeners(null)) {
					l.cleanAccountCache(account, isCleanSuccess);
				}
			}
		});
	}

	public void shareToWeixin(final String code, final MessagingListener callback) {
		threadPool.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    for (MessagingListener mCallback : getListeners(callback)) {
                        mCallback.weiXinShareStart();
                    }
                    String openid_info = Protocol.getInstance().getWeixinShareOpenId(code);
                    for (MessagingListener mCallback : getListeners(callback)) {
                        mCallback.weiXinShareSucceed(openid_info);
                    }
                } catch (Exception e) {
                    for (MessagingListener mCallback : getListeners(callback)) {
                        mCallback.weiXinShareFailed();
                    }
                }
            }
        });
	}

	/**
	 * 保存群聊图片信息
	 *
	 * @Description:
	 * @param
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2016-1-25
	 */
	public void saveImageCChat(String topic,Account account,CMessage cMessage, CGroup notifyCGroup,boolean isSavePath) {
		try {
			LocalStore localStore = account.getLocalStore();
			CAttachment cAttachment = cMessage.getAttachment();
			cAttachment.setAttchmentId(UUID.randomUUID().toString());
			String cAttFilePath =((MailChat) mApplication).getAttFilePath(((MailChat) mApplication).getChatThumbnailImageCacheDirectory(account), cAttachment.getAttchmentId(), cAttachment.getName());
			if(isSavePath){
				cAttachment.setFilePath(cAttFilePath);
			}
			localStore.updateCgroupLastCmessage(topic, cMessage);
			localStore.saveOrUpdateGroupMember(topic, cMessage.getMember());
			localStore.saveOrUpdateCMessageAndCAttach(topic, cMessage);
			localStore.updateCGroupUntreatedCount(topic, notifyCGroup.getUnreadCount()+1);
			for (MessagingListener l : getListeners(null)) {
				l.cMessageArrived(account, cMessage);
			}
			if(notifyCGroup != null){
				NotificationCenter.getInstance().onNewChattingMessage(account.getUuid(), notifyCGroup, cMessage);
			}
		} catch (MessageException e) {
			// TODO Auto-generated catch block
			for (MessagingListener l : getListeners(null)) {
				l.cMessageArrived(account, cMessage);
			}
			e.printStackTrace();
		} catch (UnavailableStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 保存群聊图片信息
	 *
	 * @Description:
	 * @param
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2016-2-25
	 */
	public void saveImageDChat(final Account account,final DChatMessage dChatMessage,final DChat notifyDChat,final boolean isContactAttributeExist) {
		threadPool.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					LocalStore localStore = account.getLocalStore();
					DChatMessage.Type type =dChatMessage.getMessageType();
					DAttachment dAttachment = dChatMessage.getAttachments().get(0);
					dAttachment.setAttchmentId(UUID.randomUUID().toString());
					//dAttachment.setFilePath(thumbnailImageDirectory+savaName);
					localStore.persistDChatMessage(dChatMessage);
					localStore.persistDChatAttachment(dAttachment);
					for (MessagingListener l : getListeners(null)) {
						l.chatMessageArrived(account, dChatMessage);
					}
					if(notifyDChat!=null){
						NotificationCenter.getInstance().onNewSingleChattingMessage(account.getUuid(), dChatMessage, notifyDChat,isContactAttributeExist);
		 	    	} 
				} catch (UnavailableStorageException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 更新群附件(图片等)是否使用自动下载
	 *
	 * @Description:
	 * @param: CAttachment
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return
	 * @date:2016-2-25
	 */
	public void updateCAttImageLoadStata(Account account,String uid,boolean isImageLoad,boolean isAllUpdate){
		try {
			account.getLocalStore().updateCAttImageLoadStata(uid, isImageLoad, isAllUpdate);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 更新单聊附件(图片等)宽高
	 *
	 * @Description:
	 * @param: CAttachment
	 * @see:
	 * @since:
	 * @author: shengli
	 * @return
	 * @date:2016-2-25
	 */
	public void updateDAttImageLoadStata(Account account ,String uid,boolean isImageLoad,boolean isAllUpdate){
		try {
			account.getLocalStore().updateDAttImageLoadStata(uid, isImageLoad, isAllUpdate);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 判断是否绑定了oa
	 * @param account
	 * @param isCheckAlreadyBind 显示广告页的时候判断是否绑定了oa
	 */
	public void checkIsBindOA(final Account account,final boolean isCheckAlreadyBind,final MessagingListener listener) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				//如果已经确定是35oa return
				if(account.isOAUser()){
					return;
				}
				boolean isBindOA = false;
//				if (!account.isBindOAChecked()) {
					String accessDomain=null;
					String oaEmail=null;
					JSONObject json= Protocol.getInstance().checkIsBindOA(account.getEmail());
					if (json==null) {
						return;
					}
					if (json.optInt("result")==0) {
						try {
							isBindOA=true;
							JSONObject jsonPageData=json.getJSONObject("pagedata");
							JSONArray jsonArray=jsonPageData.optJSONArray("rows");
							if (jsonArray!=null&&jsonArray.length()>0) {
								for (int i = 0; i < jsonArray.length(); i++) {
									JSONObject jsonChild = (JSONObject) jsonArray
											.get(i);
									accessDomain = jsonChild
											.optString("accessDomain");
									JSONObject jsonTorgusers=jsonChild.getJSONObject("torgusers");
									oaEmail=jsonTorgusers.optString("username")+"@"+jsonTorgusers.optString("domain");
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						account.setoAHost(accessDomain);
						account.setoAEmail(oaEmail);
//						account.setBindOAChecked(true);
						account.setBindOA(isBindOA);
						account.save(Preferences.getPreferences(mApplication));
					if (isCheckAlreadyBind&&listener!=null) {
						listener.alreadyBindOA(account);
					}
					}else{
						account.setoAHost(null);
						account.setoAEmail(null);
//						account.setBindOAChecked(false);
						account.setOAUser(false);
						account.setBindOA(false);
						account.setBindOAUser(false);
						account.save(Preferences.getPreferences(mApplication));
						if (isCheckAlreadyBind&&listener!=null) {
							listener.unBindOA(account);
						}
					}
//				}
			}
		});
	}
	/**
	 * 判断是否绑定了oa
	 * @param account
	 */
	public void loginInOA(final Account account) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
//				int type = 0;
				boolean isSuccess=false;
				if (account.getoAHost() != null) {
					for (MessagingListener l : getListeners(null)) {
						l.loginInOAStart();
					}
					try {
						isSuccess = Protocol.getInstance().loginInOA(account);
//						account.setoALoginType(isSuccess?1:0);
						account.setBindOAUser(isSuccess);
						account.setOAUser(isSuccess);
						account.save(Preferences.getPreferences(mApplication));
						for (MessagingListener l : getListeners(null)) {
							l.loginInOASucceed(account);
						}
					} catch (MessageException e) {
						for (MessagingListener l : getListeners(null)) {
							l.loginInOAFailed();
						}
						e.printStackTrace();
					}
				}
			}
		});
	}
}
