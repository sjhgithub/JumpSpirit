package cn.mailchat.activity.setup;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.widget.Toast;

import cn.mailchat.MailChat;
import cn.mailchat.Preferences;
import cn.mailchat.R;
import cn.mailchat.MailChat.NotificationHideSubject;
import cn.mailchat.MailChat.NotificationQuickDelete;
import cn.mailchat.MailChat.SplitViewMode;
import cn.mailchat.activity.ColorPickerDialog;
import cn.mailchat.activity.MailChatPreferenceActivity;
import cn.mailchat.controller.MessagingController;
import cn.mailchat.helper.FileBrowserHelper;
import cn.mailchat.helper.FileBrowserHelper.FileBrowserFailOverCallback;
import cn.mailchat.preferences.CheckBoxListPreference;
import cn.mailchat.preferences.TimePickerPreference;
import cn.mailchat.service.MailService;



public class Prefs extends MailChatPreferenceActivity {

    /**
     * Immutable empty {@link CharSequence} array
     */
    private static final CharSequence[] EMPTY_CHAR_SEQUENCE_ARRAY = new CharSequence[0];

    /*
     * Keys of the preferences defined in res/xml/global_preferences.xml
     */
    private static final String PREFERENCE_LANGUAGE = "language";
    private static final String PREFERENCE_THEME = "theme";
    private static final String PREFERENCE_MESSAGE_VIEW_THEME = "messageViewTheme";
    private static final String PREFERENCE_FIXED_MESSAGE_THEME = "fixedMessageViewTheme";
    private static final String PREFERENCE_COMPOSER_THEME = "messageComposeTheme";
    private static final String PREFERENCE_FONT_SIZE = "font_size";
    private static final String PREFERENCE_ANIMATIONS = "animations";
    private static final String PREFERENCE_GESTURES = "gestures";
    private static final String PREFERENCE_VOLUME_NAVIGATION = "volumeNavigation";
    private static final String PREFERENCE_START_INTEGRATED_INBOX = "start_integrated_inbox";
    private static final String PREFERENCE_CONFIRM_ACTIONS = "confirm_actions";
    private static final String PREFERENCE_NOTIFICATION_HIDE_SUBJECT = "notification_hide_subject";
    private static final String PREFERENCE_MEASURE_ACCOUNTS = "measure_accounts";
    private static final String PREFERENCE_COUNT_SEARCH = "count_search";
    private static final String PREFERENCE_HIDE_SPECIAL_ACCOUNTS = "hide_special_accounts";
    private static final String PREFERENCE_MESSAGELIST_CHECKBOXES = "messagelist_checkboxes";
    private static final String PREFERENCE_MESSAGELIST_PREVIEW_LINES = "messagelist_preview_lines";
    private static final String PREFERENCE_MESSAGELIST_SENDER_ABOVE_SUBJECT = "messagelist_sender_above_subject";
    private static final String PREFERENCE_MESSAGELIST_STARS = "messagelist_stars";
    private static final String PREFERENCE_MESSAGELIST_SHOW_CORRESPONDENT_NAMES = "messagelist_show_correspondent_names";
    private static final String PREFERENCE_MESSAGELIST_SHOW_CONTACT_NAME = "messagelist_show_contact_name";
    private static final String PREFERENCE_MESSAGELIST_CONTACT_NAME_COLOR = "messagelist_contact_name_color";
    private static final String PREFERENCE_MESSAGELIST_SHOW_CONTACT_PICTURE = "messagelist_show_contact_picture";
    private static final String PREFERENCE_MESSAGELIST_COLORIZE_MISSING_CONTACT_PICTURES =
            "messagelist_colorize_missing_contact_pictures";
    private static final String PREFERENCE_MESSAGEVIEW_FIXEDWIDTH = "messageview_fixedwidth_font";
    private static final String PREFERENCE_MESSAGEVIEW_VISIBLE_REFILE_ACTIONS = "messageview_visible_refile_actions";

    private static final String PREFERENCE_MESSAGEVIEW_RETURN_TO_LIST = "messageview_return_to_list";
    private static final String PREFERENCE_MESSAGEVIEW_SHOW_NEXT = "messageview_show_next";
    private static final String PREFERENCE_QUIET_TIME_ENABLED = "quiet_time_enabled";
    private static final String PREFERENCE_QUIET_TIME_STARTS = "quiet_time_starts";
    private static final String PREFERENCE_QUIET_TIME_ENDS = "quiet_time_ends";
    private static final String PREFERENCE_NOTIF_QUICK_DELETE = "notification_quick_delete";
    private static final String PREFERENCE_HIDE_USERAGENT = "privacy_hide_useragent";
    private static final String PREFERENCE_HIDE_TIMEZONE = "privacy_hide_timezone";

    private static final String PREFERENCE_AUTOFIT_WIDTH = "messageview_autofit_width";
    private static final String PREFERENCE_BACKGROUND_OPS = "background_ops";
    private static final String PREFERENCE_DEBUG_LOGGING = "debug_logging";
    private static final String PREFERENCE_SENSITIVE_LOGGING = "sensitive_logging";

    private static final String PREFERENCE_ATTACHMENT_DEF_PATH = "attachment_default_path";
    private static final String PREFERENCE_BACKGROUND_AS_UNREAD_INDICATOR = "messagelist_background_as_unread_indicator";
    private static final String PREFERENCE_THREADED_VIEW = "threaded_view";
    private static final String PREFERENCE_FOLDERLIST_WRAP_NAME = "folderlist_wrap_folder_name";
    private static final String PREFERENCE_SPLITVIEW_MODE = "splitview_mode";

    private static final int ACTIVITY_CHOOSE_FOLDER = 1;

    // Named indices for the mVisibleRefileActions field
    private static final int VISIBLE_REFILE_ACTIONS_DELETE = 0;
    private static final int VISIBLE_REFILE_ACTIONS_ARCHIVE = 1;
    private static final int VISIBLE_REFILE_ACTIONS_MOVE = 2;
    private static final int VISIBLE_REFILE_ACTIONS_COPY = 3;
    private static final int VISIBLE_REFILE_ACTIONS_SPAM = 4;

    private ListPreference mLanguage;
    private ListPreference mTheme;
    private CheckBoxPreference mFixedMessageTheme;
    private ListPreference mMessageTheme;
    private ListPreference mComposerTheme;
    private CheckBoxPreference mAnimations;
    private CheckBoxPreference mGestures;
    private CheckBoxListPreference mVolumeNavigation;
    private CheckBoxPreference mStartIntegratedInbox;
    private CheckBoxListPreference mConfirmActions;
    private ListPreference mNotificationHideSubject;
    private CheckBoxPreference mMeasureAccounts;
    private CheckBoxPreference mCountSearch;
    private CheckBoxPreference mHideSpecialAccounts;
    private ListPreference mPreviewLines;
    private CheckBoxPreference mSenderAboveSubject;
    private CheckBoxPreference mCheckboxes;
    private CheckBoxPreference mStars;
    private CheckBoxPreference mShowCorrespondentNames;
    private CheckBoxPreference mShowContactName;
    private CheckBoxPreference mChangeContactNameColor;
    private CheckBoxPreference mShowContactPicture;
    private CheckBoxPreference mColorizeMissingContactPictures;
    private CheckBoxPreference mFixedWidth;
    private CheckBoxPreference mReturnToList;
    private CheckBoxPreference mShowNext;
    private CheckBoxPreference mAutofitWidth;
    private ListPreference mBackgroundOps;
    private CheckBoxPreference mDebugLogging;
    private CheckBoxPreference mSensitiveLogging;
    private CheckBoxPreference mHideUserAgent;
    private CheckBoxPreference mHideTimeZone;
    private CheckBoxPreference mWrapFolderNames;
    private CheckBoxListPreference mVisibleRefileActions;

    private CheckBoxPreference mQuietTimeEnabled;
    private cn.mailchat.preferences.TimePickerPreference mQuietTimeStarts;
    private cn.mailchat.preferences.TimePickerPreference mQuietTimeEnds;
    private ListPreference mNotificationQuickDelete;
    private Preference mAttachmentPathPreference;

    private CheckBoxPreference mBackgroundAsUnreadIndicator;
    private CheckBoxPreference mThreadedView;
    private ListPreference mSplitViewMode;


    public static void actionPrefs(Context context) {
        Intent i = new Intent(context, Prefs.class);
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.global_preferences);

        mLanguage = (ListPreference) findPreference(PREFERENCE_LANGUAGE);
        List<CharSequence> entryVector = new ArrayList<CharSequence>(Arrays.asList(mLanguage.getEntries()));
        List<CharSequence> entryValueVector = new ArrayList<CharSequence>(Arrays.asList(mLanguage.getEntryValues()));
        String supportedLanguages[] = getResources().getStringArray(R.array.supported_languages);
        Set<String> supportedLanguageSet = new HashSet<String>(Arrays.asList(supportedLanguages));
        for (int i = entryVector.size() - 1; i > -1; --i) {
            if (!supportedLanguageSet.contains(entryValueVector.get(i))) {
                entryVector.remove(i);
                entryValueVector.remove(i);
            }
        }
        initListPreference(mLanguage, MailChat.getK9Language(),
                           entryVector.toArray(EMPTY_CHAR_SEQUENCE_ARRAY),
                           entryValueVector.toArray(EMPTY_CHAR_SEQUENCE_ARRAY));

        mTheme = setupListPreference(PREFERENCE_THEME, themeIdToName(MailChat.getK9Theme()));
        mFixedMessageTheme = (CheckBoxPreference) findPreference(PREFERENCE_FIXED_MESSAGE_THEME);
        mFixedMessageTheme.setChecked(MailChat.useFixedMessageViewTheme());
        mMessageTheme = setupListPreference(PREFERENCE_MESSAGE_VIEW_THEME,
                themeIdToName(MailChat.getK9MessageViewThemeSetting()));
        mComposerTheme = setupListPreference(PREFERENCE_COMPOSER_THEME,
                themeIdToName(MailChat.getK9ComposerThemeSetting()));

        findPreference(PREFERENCE_FONT_SIZE).setOnPreferenceClickListener(
        new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                onFontSizeSettings();
                return true;
            }
        });

        mAnimations = (CheckBoxPreference)findPreference(PREFERENCE_ANIMATIONS);
        mAnimations.setChecked(MailChat.showAnimations());

        mGestures = (CheckBoxPreference)findPreference(PREFERENCE_GESTURES);
        mGestures.setChecked(MailChat.gesturesEnabled());

        mVolumeNavigation = (CheckBoxListPreference)findPreference(PREFERENCE_VOLUME_NAVIGATION);
        mVolumeNavigation.setItems(new CharSequence[] {getString(R.string.volume_navigation_message), getString(R.string.volume_navigation_list)});
        mVolumeNavigation.setCheckedItems(new boolean[] {MailChat.useVolumeKeysForNavigationEnabled(), MailChat.useVolumeKeysForListNavigationEnabled()});

        mStartIntegratedInbox = (CheckBoxPreference)findPreference(PREFERENCE_START_INTEGRATED_INBOX);
        mStartIntegratedInbox.setChecked(MailChat.startIntegratedInbox());

        mConfirmActions = (CheckBoxListPreference) findPreference(PREFERENCE_CONFIRM_ACTIONS);

        boolean canDeleteFromNotification = MessagingController.platformSupportsExtendedNotifications();
        CharSequence[] confirmActionEntries = new CharSequence[canDeleteFromNotification ? 4 : 3];
        boolean[] confirmActionValues = new boolean[canDeleteFromNotification ? 4 : 3];
        int index = 0;

        confirmActionEntries[index] = getString(R.string.global_settings_confirm_action_delete);
        confirmActionValues[index++] = MailChat.confirmDelete();
        confirmActionEntries[index] = getString(R.string.global_settings_confirm_action_delete_starred);
        confirmActionValues[index++] = MailChat.confirmDeleteStarred();
        if (canDeleteFromNotification) {
            confirmActionEntries[index] = getString(R.string.global_settings_confirm_action_delete_notif);
            confirmActionValues[index++] = MailChat.confirmDeleteFromNotification();
        }
        confirmActionEntries[index] = getString(R.string.global_settings_confirm_action_spam);
        confirmActionValues[index++] = MailChat.confirmSpam();

        mConfirmActions.setItems(confirmActionEntries);
        mConfirmActions.setCheckedItems(confirmActionValues);

        mNotificationHideSubject = setupListPreference(PREFERENCE_NOTIFICATION_HIDE_SUBJECT,
                MailChat.getNotificationHideSubject().toString());

        mMeasureAccounts = (CheckBoxPreference)findPreference(PREFERENCE_MEASURE_ACCOUNTS);
        mMeasureAccounts.setChecked(MailChat.measureAccounts());

        mCountSearch = (CheckBoxPreference)findPreference(PREFERENCE_COUNT_SEARCH);
        mCountSearch.setChecked(MailChat.countSearchMessages());

        mHideSpecialAccounts = (CheckBoxPreference)findPreference(PREFERENCE_HIDE_SPECIAL_ACCOUNTS);
        mHideSpecialAccounts.setChecked(MailChat.isHideSpecialAccounts());


        mPreviewLines = setupListPreference(PREFERENCE_MESSAGELIST_PREVIEW_LINES,
                                            Integer.toString(MailChat.messageListPreviewLines()));

        mSenderAboveSubject = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_SENDER_ABOVE_SUBJECT);
        mSenderAboveSubject.setChecked(MailChat.messageListSenderAboveSubject());
        mCheckboxes = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_CHECKBOXES);
        mCheckboxes.setChecked(MailChat.messageListCheckboxes());

        mStars = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_STARS);
        mStars.setChecked(MailChat.messageListStars());

        mShowCorrespondentNames = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_SHOW_CORRESPONDENT_NAMES);
        mShowCorrespondentNames.setChecked(MailChat.showCorrespondentNames());

        mShowContactName = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_SHOW_CONTACT_NAME);
        mShowContactName.setChecked(MailChat.showContactName());

        mShowContactPicture = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_SHOW_CONTACT_PICTURE);
        mShowContactPicture.setChecked(MailChat.showContactPicture());

        mColorizeMissingContactPictures = (CheckBoxPreference)findPreference(
                PREFERENCE_MESSAGELIST_COLORIZE_MISSING_CONTACT_PICTURES);
        mColorizeMissingContactPictures.setChecked(MailChat.isColorizeMissingContactPictures());

        mBackgroundAsUnreadIndicator = (CheckBoxPreference)findPreference(PREFERENCE_BACKGROUND_AS_UNREAD_INDICATOR);
        mBackgroundAsUnreadIndicator.setChecked(MailChat.useBackgroundAsUnreadIndicator());

        mChangeContactNameColor = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_CONTACT_NAME_COLOR);
        mChangeContactNameColor.setChecked(MailChat.changeContactNameColor());

        mThreadedView = (CheckBoxPreference) findPreference(PREFERENCE_THREADED_VIEW);
        mThreadedView.setChecked(MailChat.isThreadedViewEnabled());

        if (MailChat.changeContactNameColor()) {
            mChangeContactNameColor.setSummary(R.string.global_settings_registered_name_color_changed);
        } else {
            mChangeContactNameColor.setSummary(R.string.global_settings_registered_name_color_default);
        }
        mChangeContactNameColor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final Boolean checked = (Boolean) newValue;
                if (checked) {
                    onChooseContactNameColor();
                    mChangeContactNameColor.setSummary(R.string.global_settings_registered_name_color_changed);
                } else {
                    mChangeContactNameColor.setSummary(R.string.global_settings_registered_name_color_default);
                }
                mChangeContactNameColor.setChecked(checked);
                return false;
            }
        });

        mFixedWidth = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGEVIEW_FIXEDWIDTH);
        mFixedWidth.setChecked(MailChat.messageViewFixedWidthFont());

        mReturnToList = (CheckBoxPreference) findPreference(PREFERENCE_MESSAGEVIEW_RETURN_TO_LIST);
        mReturnToList.setChecked(MailChat.messageViewReturnToList());

        mShowNext = (CheckBoxPreference) findPreference(PREFERENCE_MESSAGEVIEW_SHOW_NEXT);
        mShowNext.setChecked(MailChat.messageViewShowNext());

        mAutofitWidth = (CheckBoxPreference) findPreference(PREFERENCE_AUTOFIT_WIDTH);
        mAutofitWidth.setChecked(MailChat.autofitWidth());

        mQuietTimeEnabled = (CheckBoxPreference) findPreference(PREFERENCE_QUIET_TIME_ENABLED);
        mQuietTimeEnabled.setChecked(MailChat.getQuietTimeEnabled());
        //TODO 
        //安静时间段开始
        mQuietTimeStarts = (TimePickerPreference) findPreference(PREFERENCE_QUIET_TIME_STARTS);
        mQuietTimeStarts.setDefaultValue(MailChat.getQuietTimeStarts());
        mQuietTimeStarts.setSummary(MailChat.getQuietTimeStarts());
        mQuietTimeStarts.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
//            	System.out.println("终于找到你了[开始]："+newValue);
                final String time = (String) newValue;
                mQuietTimeStarts.setSummary(time);
                return false;
            }
        });
        //安静时间断结束
        mQuietTimeEnds = (TimePickerPreference) findPreference(PREFERENCE_QUIET_TIME_ENDS);
        mQuietTimeEnds.setSummary(MailChat.getQuietTimeEnds());
        mQuietTimeEnds.setDefaultValue(MailChat.getQuietTimeEnds());
        mQuietTimeEnds.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
//            	System.out.println("终于找到你了[开始]："+newValue);
                final String time = (String) newValue;
                mQuietTimeEnds.setSummary(time);
                return false;
            }
        });

        mNotificationQuickDelete = setupListPreference(PREFERENCE_NOTIF_QUICK_DELETE,
                MailChat.getNotificationQuickDeleteBehaviour().toString());
        if (!MessagingController.platformSupportsExtendedNotifications()) {
            PreferenceScreen prefs = (PreferenceScreen) findPreference("notification_preferences");
            prefs.removePreference(mNotificationQuickDelete);
            mNotificationQuickDelete = null;
        }

        mBackgroundOps = setupListPreference(PREFERENCE_BACKGROUND_OPS, MailChat.getBackgroundOps().name());

        mDebugLogging = (CheckBoxPreference)findPreference(PREFERENCE_DEBUG_LOGGING);
        mSensitiveLogging = (CheckBoxPreference)findPreference(PREFERENCE_SENSITIVE_LOGGING);
        mHideUserAgent = (CheckBoxPreference)findPreference(PREFERENCE_HIDE_USERAGENT);
        mHideTimeZone = (CheckBoxPreference)findPreference(PREFERENCE_HIDE_TIMEZONE);

        mDebugLogging.setChecked(MailChat.DEBUG);
        mSensitiveLogging.setChecked(MailChat.DEBUG_SENSITIVE);
        mHideUserAgent.setChecked(MailChat.hideUserAgent());
        mHideTimeZone.setChecked(MailChat.hideTimeZone());

        mAttachmentPathPreference = findPreference(PREFERENCE_ATTACHMENT_DEF_PATH);
        mAttachmentPathPreference.setSummary(MailChat.getAttachmentDefaultPath());
        mAttachmentPathPreference
        .setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FileBrowserHelper
                .getInstance()
                .showFileBrowserActivity(Prefs.this,
                                         new File(MailChat.getAttachmentDefaultPath()),
                                         ACTIVITY_CHOOSE_FOLDER, callback);

                return true;
            }

            FileBrowserFailOverCallback callback = new FileBrowserFailOverCallback() {

                @Override
                public void onPathEntered(String path) {
                    mAttachmentPathPreference.setSummary(path);
                    MailChat.setAttachmentDefaultPath(path);
                }

                @Override
                public void onCancel() {
                    // canceled, do nothing
                }
            };
        });

        mWrapFolderNames = (CheckBoxPreference)findPreference(PREFERENCE_FOLDERLIST_WRAP_NAME);
        mWrapFolderNames.setChecked(MailChat.wrapFolderNames());

        mVisibleRefileActions = (CheckBoxListPreference) findPreference(PREFERENCE_MESSAGEVIEW_VISIBLE_REFILE_ACTIONS);
        CharSequence[] visibleRefileActionsEntries = new CharSequence[5];
        visibleRefileActionsEntries[VISIBLE_REFILE_ACTIONS_DELETE] = getString(R.string.delete_action);
        visibleRefileActionsEntries[VISIBLE_REFILE_ACTIONS_ARCHIVE] = getString(R.string.archive_action);
        visibleRefileActionsEntries[VISIBLE_REFILE_ACTIONS_MOVE] = getString(R.string.move_action);
        visibleRefileActionsEntries[VISIBLE_REFILE_ACTIONS_COPY] = getString(R.string.copy_action);
        visibleRefileActionsEntries[VISIBLE_REFILE_ACTIONS_SPAM] = getString(R.string.spam_action);

        boolean[] visibleRefileActionsValues = new boolean[5];
        visibleRefileActionsValues[VISIBLE_REFILE_ACTIONS_DELETE] = MailChat.isMessageViewDeleteActionVisible();
        visibleRefileActionsValues[VISIBLE_REFILE_ACTIONS_ARCHIVE] = MailChat.isMessageViewArchiveActionVisible();
        visibleRefileActionsValues[VISIBLE_REFILE_ACTIONS_MOVE] = MailChat.isMessageViewMoveActionVisible();
        visibleRefileActionsValues[VISIBLE_REFILE_ACTIONS_COPY] = MailChat.isMessageViewCopyActionVisible();
        visibleRefileActionsValues[VISIBLE_REFILE_ACTIONS_SPAM] = MailChat.isMessageViewSpamActionVisible();

        mVisibleRefileActions.setItems(visibleRefileActionsEntries);
        mVisibleRefileActions.setCheckedItems(visibleRefileActionsValues);

        mSplitViewMode = (ListPreference) findPreference(PREFERENCE_SPLITVIEW_MODE);
        initListPreference(mSplitViewMode, MailChat.getSplitViewMode().name(),
                mSplitViewMode.getEntries(), mSplitViewMode.getEntryValues());
    }

    private static String themeIdToName(MailChat.Theme theme) {
        switch (theme) {
            case DARK: return "dark";
            case USE_GLOBAL: return "global";
            default: return "light";
        }
    }

    private static MailChat.Theme themeNameToId(String theme) {
        if (TextUtils.equals(theme, "dark")) {
            return MailChat.Theme.DARK;
        } else if (TextUtils.equals(theme, "global")) {
            return MailChat.Theme.USE_GLOBAL;
        } else {
            return MailChat.Theme.LIGHT;
        }
    }

    private void saveSettings() {
        SharedPreferences preferences = Preferences.getPreferences(this).getPreferences();

        MailChat.setK9Language(mLanguage.getValue());

        MailChat.setK9Theme(themeNameToId(mTheme.getValue()));
        MailChat.setUseFixedMessageViewTheme(mFixedMessageTheme.isChecked());
        MailChat.setK9MessageViewThemeSetting(themeNameToId(mMessageTheme.getValue()));
        MailChat.setK9ComposerThemeSetting(themeNameToId(mComposerTheme.getValue()));

        MailChat.setAnimations(mAnimations.isChecked());
        MailChat.setGesturesEnabled(mGestures.isChecked());
        MailChat.setUseVolumeKeysForNavigation(mVolumeNavigation.getCheckedItems()[0]);
        MailChat.setUseVolumeKeysForListNavigation(mVolumeNavigation.getCheckedItems()[1]);
        MailChat.setStartIntegratedInbox(!mHideSpecialAccounts.isChecked() && mStartIntegratedInbox.isChecked());
        MailChat.setNotificationHideSubject(NotificationHideSubject.valueOf(mNotificationHideSubject.getValue()));

        int index = 0;
        MailChat.setConfirmDelete(mConfirmActions.getCheckedItems()[index++]);
        MailChat.setConfirmDeleteStarred(mConfirmActions.getCheckedItems()[index++]);
        if (MessagingController.platformSupportsExtendedNotifications()) {
            MailChat.setConfirmDeleteFromNotification(mConfirmActions.getCheckedItems()[index++]);
        }
        MailChat.setConfirmSpam(mConfirmActions.getCheckedItems()[index++]);

        MailChat.setMeasureAccounts(mMeasureAccounts.isChecked());
        MailChat.setCountSearchMessages(mCountSearch.isChecked());
        MailChat.setHideSpecialAccounts(mHideSpecialAccounts.isChecked());
        MailChat.setMessageListPreviewLines(Integer.parseInt(mPreviewLines.getValue()));
        MailChat.setMessageListCheckboxes(mCheckboxes.isChecked());
        MailChat.setMessageListStars(mStars.isChecked());
        MailChat.setShowCorrespondentNames(mShowCorrespondentNames.isChecked());
        MailChat.setMessageListSenderAboveSubject(mSenderAboveSubject.isChecked());
        MailChat.setShowContactName(mShowContactName.isChecked());
        MailChat.setShowContactPicture(mShowContactPicture.isChecked());
        MailChat.setColorizeMissingContactPictures(mColorizeMissingContactPictures.isChecked());
        MailChat.setUseBackgroundAsUnreadIndicator(mBackgroundAsUnreadIndicator.isChecked());
        MailChat.setThreadedViewEnabled(mThreadedView.isChecked());
        MailChat.setChangeContactNameColor(mChangeContactNameColor.isChecked());
        MailChat.setMessageViewFixedWidthFont(mFixedWidth.isChecked());
        MailChat.setMessageViewReturnToList(mReturnToList.isChecked());
        MailChat.setMessageViewShowNext(mShowNext.isChecked());
        MailChat.setAutofitWidth(mAutofitWidth.isChecked());
        MailChat.setQuietTimeEnabled(mQuietTimeEnabled.isChecked());

        boolean[] enabledRefileActions = mVisibleRefileActions.getCheckedItems();
        MailChat.setMessageViewDeleteActionVisible(enabledRefileActions[VISIBLE_REFILE_ACTIONS_DELETE]);
        MailChat.setMessageViewArchiveActionVisible(enabledRefileActions[VISIBLE_REFILE_ACTIONS_ARCHIVE]);
        MailChat.setMessageViewMoveActionVisible(enabledRefileActions[VISIBLE_REFILE_ACTIONS_MOVE]);
        MailChat.setMessageViewCopyActionVisible(enabledRefileActions[VISIBLE_REFILE_ACTIONS_COPY]);
        MailChat.setMessageViewSpamActionVisible(enabledRefileActions[VISIBLE_REFILE_ACTIONS_SPAM]);

        MailChat.setQuietTimeStarts(mQuietTimeStarts.getTime());
        MailChat.setQuietTimeEnds(mQuietTimeEnds.getTime());
        MailChat.setWrapFolderNames(mWrapFolderNames.isChecked());

        if (mNotificationQuickDelete != null) {
            MailChat.setNotificationQuickDeleteBehaviour(
                    NotificationQuickDelete.valueOf(mNotificationQuickDelete.getValue()));
        }

        MailChat.setSplitViewMode(SplitViewMode.valueOf(mSplitViewMode.getValue()));
        MailChat.setAttachmentDefaultPath(mAttachmentPathPreference.getSummary().toString());
        boolean needsRefresh = MailChat.setBackgroundOps(mBackgroundOps.getValue());

        if (!MailChat.DEBUG && mDebugLogging.isChecked()) {
            Toast.makeText(this, R.string.debug_logging_enabled, Toast.LENGTH_LONG).show();
        }
        MailChat.DEBUG = mDebugLogging.isChecked();
        MailChat.DEBUG_SENSITIVE = mSensitiveLogging.isChecked();
        MailChat.setHideUserAgent(mHideUserAgent.isChecked());
        MailChat.setHideTimeZone(mHideTimeZone.isChecked());

        Editor editor = preferences.edit();
        MailChat.save(editor);
        editor.commit();

        if (needsRefresh) {
            MailService.actionReset(this, null);
        }
    }

    @Override
	public void onPause() {
        saveSettings();
        super.onPause();
    }

    private void onFontSizeSettings() {
        FontSizeSettings.actionEditSettings(this);
    }

    private void onChooseContactNameColor() {
        new ColorPickerDialog(this, new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                MailChat.setContactNameColor(color);
            }
        },
        MailChat.getContactNameColor()).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case ACTIVITY_CHOOSE_FOLDER:
            if (resultCode == RESULT_OK && data != null) {
                // obtain the filename
                Uri fileUri = data.getData();
                if (fileUri != null) {
                    String filePath = fileUri.getPath();
                    if (filePath != null) {
                        mAttachmentPathPreference.setSummary(filePath.toString());
                        MailChat.setAttachmentDefaultPath(filePath.toString());
                    }
                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
