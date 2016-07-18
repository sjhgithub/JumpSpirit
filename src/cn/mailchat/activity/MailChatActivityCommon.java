package cn.mailchat.activity;

import java.util.Locale;

import cn.mailchat.MailChat;
import cn.mailchat.activity.misc.SwipeGestureDetector;
import cn.mailchat.activity.misc.SwipeGestureDetector.OnSwipeGestureListener;
import cn.mailchat.helper.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.GestureDetector;
import android.view.MotionEvent;


/**
 * This class implements functionality common to most activities used in K-9 Mail.
 *
 * @see MailChatActivity
 * @see MailChatListActivity
 * @see MailChatFragmentActivity
 */
public class MailChatActivityCommon {
    /**
     * Creates a new instance of {@link MailChatActivityCommon} bound to the specified activity.
     *
     * @param activity
     *         The {@link Activity} the returned {@code MailChatActivityCommon} instance will be bound to.
     *
     * @return The {@link MailChatActivityCommon} instance that will provide the base functionality of the
     *         "MailChat" activities.
     */
    public static MailChatActivityCommon newInstance(Activity activity) {
        return new MailChatActivityCommon(activity);
    }

    public static void setLanguage(Context context, String language) {
        Locale locale;
        if (StringUtils.isNullOrEmpty(language)) {
            locale = Locale.getDefault();
        } else if (language.length() == 5 && language.charAt(2) == '_') {
            // language is in the form: en_US
            locale = new Locale(language.substring(0, 2), language.substring(3));
        } else {
            locale = new Locale(language);
        }

        Configuration config = new Configuration();
        config.locale = locale;
        Resources resources = context.getResources();
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }


    /**
     * Base activities need to implement this interface.
     *
     * <p>The implementing class simply has to call through to the implementation of these methods
     * in {@link MailChatActivityCommon}.</p>
     */
    public interface K9ActivityMagic {
        void setupGestureDetector(OnSwipeGestureListener listener);
    }


    private Activity mActivity;
    private GestureDetector mGestureDetector;


    private MailChatActivityCommon(Activity activity) {
        mActivity = activity;
        setLanguage(mActivity, MailChat.getK9Language());
        mActivity.setTheme(MailChat.getK9ThemeResourceId());
    }

    /**
     * Call this before calling {@code super.dispatchTouchEvent(MotionEvent)}.
     */
    public void preDispatchTouchEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            mGestureDetector.onTouchEvent(event);
        }
    }

    /**
     * Get the background color of the theme used for this activity.
     *
     * @return The background color of the current theme.
     */
    public int getThemeBackgroundColor() {
        TypedArray array = mActivity.getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.colorBackground });

        int backgroundColor = array.getColor(0, 0xFF00FF);

        array.recycle();

        return backgroundColor;
    }

    /**
     * Call this if you wish to use the swipe gesture detector.
     *
     * @param listener
     *         A listener that will be notified if a left to right or right to left swipe has been
     *         detected.
     */
    public void setupGestureDetector(OnSwipeGestureListener listener) {
        mGestureDetector = new GestureDetector(mActivity,
                new SwipeGestureDetector(mActivity, listener));
    }
}
