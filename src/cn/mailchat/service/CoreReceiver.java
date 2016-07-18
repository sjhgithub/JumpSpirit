
package cn.mailchat.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import cn.mailchat.MailChat;
import cn.mailchat.helper.power.TracingPowerManager;
import cn.mailchat.helper.power.TracingPowerManager.TracingWakeLock;

public class CoreReceiver extends BroadcastReceiver {

    public static String WAKE_LOCK_RELEASE = "cn.mailchat.service.CoreReceiver.wakeLockRelease";

    public static String WAKE_LOCK_ID = "cn.mailchat.service.CoreReceiver.wakeLockId";

    private static ConcurrentHashMap<Integer, TracingWakeLock> wakeLocks = new ConcurrentHashMap<Integer, TracingWakeLock>();
    private static AtomicInteger wakeLockSeq = new AtomicInteger(0);

    private static Integer getWakeLock(Context context) {
        TracingPowerManager pm = TracingPowerManager.getPowerManager(context);
        TracingWakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CoreReceiver getWakeLock");
        wakeLock.setReferenceCounted(false);
        wakeLock.acquire(MailChat.BOOT_RECEIVER_WAKE_LOCK_TIMEOUT);
        Integer tmpWakeLockId = wakeLockSeq.getAndIncrement();
        wakeLocks.put(tmpWakeLockId, wakeLock);
        if (MailChat.DEBUG)
            Log.v(MailChat.LOG_TAG, "CoreReceiver Created wakeLock " + tmpWakeLockId);
        return tmpWakeLockId;
    }

    private static void releaseWakeLock(Integer wakeLockId) {
        if (wakeLockId != null) {
            TracingWakeLock wl = wakeLocks.remove(wakeLockId);
            if (wl != null) {
                if (MailChat.DEBUG)
                    Log.v(MailChat.LOG_TAG, "CoreReceiver Releasing wakeLock " + wakeLockId);
                wl.release();
            } else {
                Log.w(MailChat.LOG_TAG, "BootReceiver WakeLock " + wakeLockId + " doesn't exist");
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Integer tmpWakeLockId = CoreReceiver.getWakeLock(context);
        try {
            if (MailChat.DEBUG)
                Log.i(MailChat.LOG_TAG, "CoreReceiver.onReceive" + intent);
            if (CoreReceiver.WAKE_LOCK_RELEASE.equals(intent.getAction())) {
                Integer wakeLockId = intent.getIntExtra(WAKE_LOCK_ID, -1);
                if (wakeLockId != -1) {
                    if (MailChat.DEBUG)
                        Log.v(MailChat.LOG_TAG, "CoreReceiver Release wakeLock " + wakeLockId);
                    CoreReceiver.releaseWakeLock(wakeLockId);
                }
            } else {
                tmpWakeLockId = receive(context, intent, tmpWakeLockId);
            }
        } finally {
            CoreReceiver.releaseWakeLock(tmpWakeLockId);
        }
    }

    public Integer receive(Context context, Intent intent, Integer wakeLockId) {
        return wakeLockId;
    }

    public static void releaseWakeLock(Context context, int wakeLockId) {
        if (MailChat.DEBUG)
            Log.v(MailChat.LOG_TAG, "CoreReceiver Got request to release wakeLock " + wakeLockId);
        Intent i = new Intent();
        i.setClass(context, CoreReceiver.class);
        i.setAction(WAKE_LOCK_RELEASE);
        i.putExtra(WAKE_LOCK_ID, wakeLockId);
        context.sendBroadcast(i);
    }
}
