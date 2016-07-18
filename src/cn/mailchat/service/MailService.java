
package cn.mailchat.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import cn.mailchat.Account;
import cn.mailchat.Account.FolderMode;
import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.helper.Utility;
import cn.mailchat.mail.Pusher;

public class MailService extends CoreService {
	/**
	 * directly open transport,never check account and net state. do check job
	 * before request this action
	 */
	private static final String ACTION_OPEN = "cn.mailchat.ACTION_OPEN";
    private static final String ACTION_CHECK_MAIL = "cn.mailchat.intent.action.MAIL_SERVICE_WAKEUP";
    private static final String ACTION_RESET = "cn.mailchat.intent.action.MAIL_SERVICE_RESET";
    private static final String ACTION_RESCHEDULE_POLL = "cn.mailchat.intent.action.MAIL_SERVICE_RESCHEDULE_POLL";
    private static final String ACTION_CANCEL = "cn.mailchat.intent.action.MAIL_SERVICE_CANCEL";
    private static final String ACTION_REFRESH_PUSHERS = "cn.mailchat.intent.action.MAIL_SERVICE_REFRESH_PUSHERS";
    private static final String ACTION_RESTART_PUSHERS = "cn.mailchat.intent.action.MAIL_SERVICE_RESTART_PUSHERS";
    private static final String CONNECTIVITY_CHANGE = "cn.mailchat.intent.action.MAIL_SERVICE_CONNECTIVITY_CHANGE";
    private static final String CANCEL_CONNECTIVITY_NOTICE = "cn.mailchat.intent.action.MAIL_SERVICE_CANCEL_CONNECTIVITY_NOTICE";

    private static long nextCheck = -1;
    private static boolean pushingRequested = false;
    private static boolean pollingRequested = false;
    private static boolean syncBlocked = false;
    public static boolean screenOn = true;
    public static boolean lock = false;

    public static void actionReset(Context context, Integer wakeLockId) {
        Intent i = new Intent();
        i.setClass(context, MailService.class);
        i.setAction(MailService.ACTION_RESET);
        addWakeLockId(context, i, wakeLockId, true);
        context.startService(i);
    }

    public static void actionRestartPushers(Context context, Integer wakeLockId) {
        Intent i = new Intent();
        i.setClass(context, MailService.class);
        i.setAction(MailService.ACTION_RESTART_PUSHERS);
        addWakeLockId(context, i, wakeLockId, true);
        context.startService(i);
    }

    public static void actionReschedulePoll(Context context, Integer wakeLockId) {
        Intent i = new Intent();
        i.setClass(context, MailService.class);
        i.setAction(MailService.ACTION_RESCHEDULE_POLL);
        addWakeLockId(context, i, wakeLockId, true);
        context.startService(i);
    }

    public static void actionCancel(Context context, Integer wakeLockId) {
        Intent i = new Intent();
        i.setClass(context, MailService.class);
        i.setAction(MailService.ACTION_CANCEL);
        addWakeLockId(context, i, wakeLockId, false); // CK:Q: why should we not create a wake lock if one is not already existing like for example in actionReschedulePoll?
        context.startService(i);
    }

    public static void connectivityChange(Context context, Integer wakeLockId) {
        Intent i = new Intent();
        i.setClass(context, MailService.class);
        i.setAction(MailService.CONNECTIVITY_CHANGE);
        addWakeLockId(context, i, wakeLockId, false); // CK:Q: why should we not create a wake lock if one is not already existing like for example in actionReschedulePoll?
        context.startService(i);
    }
   
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(MailChat.LOG_COLLECTOR_TAG,">>>>>MailService::onCreate");
        if (MailChat.DEBUG)
            Log.d(MailChat.LOG_TAG, "***** MailService *****: onCreate");
    }
    

    @Override
    public int startService(Intent intent, int startId) {
        long startTime = System.currentTimeMillis();
        boolean oldIsSyncDisabled = isSyncDisabled();
        boolean doBackground = true;

        final boolean hasConnectivity = Utility.hasConnectivity(getApplication());
        boolean autoSync = ContentResolver.getMasterSyncAutomatically();
        MailChat.BACKGROUND_OPS bOps = MailChat.getBackgroundOps();

//        switch (bOps) {
//            case NEVER:
//                doBackground = false;
//                break;
//            case ALWAYS:
//                doBackground = true;
//                break;
//            case WHEN_CHECKED_AUTO_SYNC:
//                doBackground = autoSync;
//                break;
//        }

        syncBlocked = !(doBackground && hasConnectivity);
        Log.i(MailChat.LOG_COLLECTOR_TAG,">>>>>MailService.onStart(" + intent + ", " + startId
                + "), hasConnectivity = " + hasConnectivity + ", doBackground = " + doBackground);
        if (MailChat.DEBUG)
            Log.i(MailChat.LOG_TAG, "MailService.onStart(" + intent + ", " + startId
                  + "), hasConnectivity = " + hasConnectivity + ", doBackground = " + doBackground);

        // MessagingController.getInstance(getApplication()).addListener(mListener);
        if (ACTION_CHECK_MAIL.equals(intent.getAction())) {
            if (MailChat.DEBUG)
                Log.i(MailChat.LOG_TAG, "***** MailService *****: checking mail");
            if (hasConnectivity && doBackground) {
                PollService.startService(this);
            }
            reschedulePollInBackground(hasConnectivity, doBackground, startId, false);
        } else if (ACTION_CANCEL.equals(intent.getAction())) {
            if (MailChat.DEBUG)
                Log.d(MailChat.LOG_TAG, "***** MailService *****: cancel");
            cancel();
        } else if (ACTION_RESET.equals(intent.getAction())) {
            if (MailChat.DEBUG)
                Log.d(MailChat.LOG_TAG, "***** MailService *****: reschedule");
            rescheduleAllInBackground(hasConnectivity, doBackground, startId);
        } else if (ACTION_RESTART_PUSHERS.equals(intent.getAction())) {
            if (MailChat.DEBUG)
                Log.d(MailChat.LOG_TAG, "***** MailService *****: restarting pushers");
            reschedulePushersInBackground(hasConnectivity, doBackground, startId);
        } else if (ACTION_RESCHEDULE_POLL.equals(intent.getAction())) {
            if (MailChat.DEBUG)
                Log.d(MailChat.LOG_TAG, "***** MailService *****: rescheduling poll");
            reschedulePollInBackground(hasConnectivity, doBackground, startId, true);
        } else if (ACTION_REFRESH_PUSHERS.equals(intent.getAction())) {
            refreshPushersInBackground(hasConnectivity, doBackground, startId);
        } else if (CONNECTIVITY_CHANGE.equals(intent.getAction())) {
            rescheduleAllInBackground(hasConnectivity, doBackground, startId);
            if (MailChat.DEBUG)
                Log.i(MailChat.LOG_TAG, "Got connectivity action with hasConnectivity = " + hasConnectivity + ", doBackground = " + doBackground);
        } else if (CANCEL_CONNECTIVITY_NOTICE.equals(intent.getAction())) {
            /* do nothing */
        }

        if (isSyncDisabled() != oldIsSyncDisabled) {
            MessagingController.getInstance(getApplication()).systemStatusChanged();
        }

        if (MailChat.DEBUG)
            Log.i(MailChat.LOG_TAG, "MailService.onStart took " + (System.currentTimeMillis() - startTime) + "ms");

        return START_NOT_STICKY;
    }

	@Override
	public void onDestroy() {
		Log.i(MailChat.LOG_COLLECTOR_TAG, ">>>>>MailService  :: onDestroy()");
		if (MailChat.DEBUG)
			Log.d(MailChat.LOG_TAG, "***** MailService *****: onDestroy()");
		super.onDestroy();
		// MessagingController.getInstance(getApplication()).removeListener(mListener);
	}

    private void cancel() {
        Intent i = new Intent();
        i.setClassName(getApplication().getPackageName(), "cn.mailchat.service.MailService");
        i.setAction(ACTION_CHECK_MAIL);
        BootReceiver.cancelIntent(this, i);
    }

    private final static String PREVIOUS_INTERVAL = "MailService.previousInterval";
    private final static String LAST_CHECK_END = "MailService.lastCheckEnd";

    public static void saveLastCheckEnd(Context context) {
        long lastCheckEnd = System.currentTimeMillis();
        if (MailChat.DEBUG)
            Log.d(MailChat.LOG_TAG, "Saving lastCheckEnd = " + new Date(lastCheckEnd));
        Preferences prefs = Preferences.getPreferences(context);
        SharedPreferences sPrefs = prefs.getPreferences();
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putLong(LAST_CHECK_END, lastCheckEnd);
        editor.commit();
    }

    private void rescheduleAllInBackground(final boolean hasConnectivity,
            final boolean doBackground, Integer startId) {

        execute(getApplication(), new Runnable() {
            @Override
            public void run() {
                reschedulePoll(hasConnectivity, doBackground, true);
                reschedulePushers(hasConnectivity, doBackground);
            }
        }, MailChat.MAIL_SERVICE_WAKE_LOCK_TIMEOUT, startId);
    }

    private void reschedulePollInBackground(final boolean hasConnectivity,
            final boolean doBackground, Integer startId, final boolean considerLastCheckEnd) {

        execute(getApplication(), new Runnable() {
            public void run() {
                reschedulePoll(hasConnectivity, doBackground, considerLastCheckEnd);
            }
        }, MailChat.MAIL_SERVICE_WAKE_LOCK_TIMEOUT, startId);
    }

    private void reschedulePushersInBackground(final boolean hasConnectivity,
            final boolean doBackground, Integer startId) {

        execute(getApplication(), new Runnable() {
            public void run() {
                reschedulePushers(hasConnectivity, doBackground);
            }
        }, MailChat.MAIL_SERVICE_WAKE_LOCK_TIMEOUT, startId);
    }

    private void refreshPushersInBackground(boolean hasConnectivity, boolean doBackground,
            Integer startId) {

        if (hasConnectivity && doBackground) {
            execute(getApplication(), new Runnable() {
                public void run() {
                    refreshPushers();
                    schedulePushers();
                }
            }, MailChat.MAIL_SERVICE_WAKE_LOCK_TIMEOUT, startId);
        }
    }

    private void reschedulePoll(final boolean hasConnectivity, final boolean doBackground,
            boolean considerLastCheckEnd) {

        if (!(hasConnectivity && doBackground)) {
            if (MailChat.DEBUG) {
                Log.d(MailChat.LOG_TAG, "No connectivity, canceling check for " +
                        getApplication().getPackageName());
            }

            nextCheck = -1;
            cancel();

            return;
        }

        Preferences prefs = Preferences.getPreferences(MailService.this);
        SharedPreferences sPrefs = prefs.getPreferences();
        int previousInterval = sPrefs.getInt(PREVIOUS_INTERVAL, -1);
        long lastCheckEnd = sPrefs.getLong(LAST_CHECK_END, -1);

        if (lastCheckEnd > System.currentTimeMillis()) {
            Log.d(MailChat.LOG_TAG, "The database claims that the last time mail was checked was in " +
                    "the future (" + lastCheckEnd + "). To try to get things back to normal, " +
                    "the last check time has been reset to: " + System.currentTimeMillis());
            lastCheckEnd = System.currentTimeMillis();
        }

        int shortestInterval = -1;

        for (Account account : prefs.getAvailableAccounts()) {
            /*
            // 三五 3.0/2.0 邮箱和隐藏账号不需要Poll
            if (account.getVersion_35Mail() == 1
                    || account.getVersion_35Mail() == 2
                    || account.isHideAccount()) {
            */
            if (account.isHideAccount()) {
                continue;
            }
            
            if (account.getAutomaticCheckIntervalMinutes() != -1 &&
                    account.getFolderSyncMode() != FolderMode.NONE &&
                    (account.getAutomaticCheckIntervalMinutes() < shortestInterval ||
                            shortestInterval == -1)) {
                shortestInterval = account.getAutomaticCheckIntervalMinutes();
                if (MailChat.DEBUG)
                   Log.d(MailChat.LOG_TAG, "***** MailService *****:AutomaticCheckIntervalMinutes"+shortestInterval);
            }
        }
        
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putInt(PREVIOUS_INTERVAL, shortestInterval);
        editor.commit();

        if (shortestInterval == -1) {
            if (MailChat.DEBUG) {
                Log.d(MailChat.LOG_TAG, "No next check scheduled for package " +
                        getApplication().getPackageName());
            }

            nextCheck = -1;
            pollingRequested = false;
            cancel();
        } else {
            long delay = (shortestInterval * (60 * 1000));
            long base = (previousInterval == -1 || lastCheckEnd == -1 ||
                    !considerLastCheckEnd ? System.currentTimeMillis() : lastCheckEnd);
            long nextTime = base + delay;

            if (MailChat.DEBUG) {
                Log.d(MailChat.LOG_TAG, "previousInterval = " + previousInterval +
                      ", shortestInterval = " + shortestInterval +
                      ", lastCheckEnd = " + new Date(lastCheckEnd) +
                      ", considerLastCheckEnd = " + considerLastCheckEnd);
            }

            nextCheck = nextTime;
            pollingRequested = true;

            try {
                if (MailChat.DEBUG) {
                    Log.d(MailChat.LOG_TAG, "Next check for package " +
                            getApplication().getPackageName() + " scheduled for " +
                            new Date(nextTime));
                }
            } catch (Exception e) {
                // I once got a NullPointerException deep in new Date();
                Log.e(MailChat.LOG_TAG, "Exception while logging", e);
            }

            Intent i = new Intent();
            i.setClassName(getApplication().getPackageName(), "cn.mailchat.service.MailService");
            i.setAction(ACTION_CHECK_MAIL);
            BootReceiver.scheduleIntent(MailService.this, nextTime, i);
        }
    }

    public static boolean isSyncDisabled() {
        return  syncBlocked || (!pollingRequested && !pushingRequested);
    }

    private void stopPushers() {
        MessagingController.getInstance(getApplication()).stopAllPushing();
        PushService.stopService(MailService.this);
    }

    private void reschedulePushers(boolean hasConnectivity, boolean doBackground) {
        if (MailChat.DEBUG) {
            Log.d(MailChat.LOG_TAG, "Rescheduling pushers");
        }

        stopPushers();

        if (!(hasConnectivity && doBackground)) {
            if (MailChat.DEBUG) {
                Log.d(MailChat.LOG_TAG, "Not scheduling pushers:  connectivity? " + hasConnectivity +
                        " -- doBackground? " + doBackground);
            }
            return;
        }

        setupPushers();
        schedulePushers();
    }


    private synchronized void setupPushers() {
        boolean pushing = false;
        List<Account> accounts = Preferences.getPreferences(MailService.this).getAccounts();
        for (Account account : accounts) {
            if (MailChat.DEBUG)
                Log.d(MailChat.LOG_TAG, "Setting up pushers for account " + account.getDescription());
            if (account.isEnabled() && account.isAvailable(getApplicationContext())&& !account.isHideAccount()) {
                pushing |= MessagingController.getInstance(getApplication()).setupPushing(account);
            } else {
                //TODO: setupPushing of unavailable accounts when they become available (sd-card inserted)
            }
        }
        if (pushing) {
            PushService.startService(MailService.this);
        }
        pushingRequested = pushing;
    }

    private void refreshPushers() {
        try {
            long nowTime = System.currentTimeMillis();
            if (MailChat.DEBUG)
                Log.i(MailChat.LOG_TAG, "Refreshing pushers");
            Collection<Pusher> pushers = MessagingController.getInstance(getApplication()).getPushers();
            for (Pusher pusher : pushers) {
                long lastRefresh = pusher.getLastRefresh();
                int refreshInterval = pusher.getRefreshInterval();
                long sinceLast = nowTime - lastRefresh;
                if (sinceLast + 10000 > refreshInterval) { // Add 10 seconds to keep pushers in sync, avoid drift
                    if (MailChat.DEBUG) {
                        Log.d(MailChat.LOG_TAG, "PUSHREFRESH: refreshing lastRefresh = " + lastRefresh + ", interval = " + refreshInterval
                              + ", nowTime = " + nowTime + ", sinceLast = " + sinceLast);
                    }
                    pusher.refresh();
                    pusher.setLastRefresh(nowTime);
                } else {
                    if (MailChat.DEBUG) {
                        Log.d(MailChat.LOG_TAG, "PUSHREFRESH: NOT refreshing lastRefresh = " + lastRefresh + ", interval = " + refreshInterval
                              + ", nowTime = " + nowTime + ", sinceLast = " + sinceLast);
                    }
                }
            }

//            // Whenever we refresh our pushers, send any unsent messages
//            if (MailChat.DEBUG) {
//                Log.d(MailChat.LOG_TAG, "PUSHREFRESH:  trying to send mail in all folders!");
//            }
//
//            MessagingController.getInstance(getApplication()).sendPendingMessages(null);

        } catch (Exception e) {
            Log.e(MailChat.LOG_TAG, "Exception while refreshing pushers", e);
        }
    }

    private void schedulePushers() {
        int minInterval = -1;

        Collection<Pusher> pushers = MessagingController.getInstance(getApplication()).getPushers();
        for (Pusher pusher : pushers) {
            int interval = pusher.getRefreshInterval();
            if (interval > 0 && (interval < minInterval || minInterval == -1)) {
                minInterval = interval;
            }
        }
        if (MailChat.DEBUG) {
            Log.d(MailChat.LOG_TAG, "Pusher refresh interval = " + minInterval);
        }
        if (minInterval > 0) {
            long nextTime = System.currentTimeMillis() + minInterval;
            if (MailChat.DEBUG)
                Log.d(MailChat.LOG_TAG, "Next pusher refresh scheduled for " + new Date(nextTime));
            Intent i = new Intent();
            i.setClassName(getApplication().getPackageName(), "cn.mailchat.service.MailService");
            i.setAction(ACTION_REFRESH_PUSHERS);
            BootReceiver.scheduleIntent(MailService.this, nextTime, i);
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // Unused
        return null;
    }

    public static long getNextPollTime() {
        return nextCheck;
    }
}
