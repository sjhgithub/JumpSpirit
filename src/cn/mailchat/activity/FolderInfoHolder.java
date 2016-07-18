package cn.mailchat.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;

import cn.mailchat.Account;
import cn.mailchat.MailChat;
import cn.mailchat.R;
import cn.mailchat.mail.Folder;

public class FolderInfoHolder implements Comparable<FolderInfoHolder> {
    public String name;
    public String displayName;
    public long lastChecked;
    public int unreadMessageCount = -1;
    public int flaggedMessageCount = -1;
    public boolean loading;
    public String status;
    public boolean lastCheckFailed;
    public Folder folder;
    public boolean pushActive;
    @Override
    public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
        return this.name.equals(((FolderInfoHolder)o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public int compareTo(FolderInfoHolder o) {
        String s1 = this.name;
        String s2 = o.name;

        int ret = s1.compareToIgnoreCase(s2);
        if (ret != 0) {
            return ret;
        } else {
            return s1.compareTo(s2);
        }

    }

    private String truncateStatus(String mess) {
        if (mess != null && mess.length() > 27) {
            mess = mess.substring(0, 27);
        }
        return mess;
    }

    // constructor for an empty object for comparisons
    public FolderInfoHolder() {
    }

    public FolderInfoHolder(Context context, Folder folder, Account account) {
        if (context == null) {
            throw new IllegalArgumentException("null context given");
        }
        populate(context, folder, account);
    }

    public FolderInfoHolder(Context context, Folder folder, Account account, int unreadCount) {
        populate(context, folder, account, unreadCount);
    }

    public void populate(Context context, Folder folder, Account account, int unreadCount) {
        populate(context, folder, account);
        this.unreadMessageCount = unreadCount;
        folder.close();

    }


    public void populate(Context context, Folder folder, Account account) {
        this.folder = folder;
        this.name = folder.getName();
        this.lastChecked = folder.getLastUpdate();
        this.status = truncateStatus(folder.getStatus());
        this.displayName = getDisplayName(context, account, name);
    }
    public static  List <? extends Folder > orderFolder(List <? extends Folder >  folderArray){
//		String[] newOrderFolders = { "INBOX", "K9MAIL_INTERNAL_OUTBOX", "Sent",
//				"Drafts", "Trash", "Spam" };
		String[] newOrderFolders = { "INBOX","收件箱", "K9MAIL_INTERNAL_OUTBOX","待发送", "Sent","已发送",
				"Drafts","草稿箱", "Trash","已删除", "Spam","垃圾邮件" };
		List<Folder> otherLocalFolders=new ArrayList<Folder>();
		for (int i = 0; i < newOrderFolders.length; i++) {
			String folderName=newOrderFolders[i];
			for (Folder folder : folderArray) {
				if (folderName.equals(folder.getName())) {
					otherLocalFolders.add(folder);
				}
			}
		}
		folderArray.removeAll(otherLocalFolders);
		folderArray.addAll(0,(ArrayList)otherLocalFolders);
		for (int i = 0; i < folderArray.size(); i++) {
			if (MailChat.ERROR_FOLDER_NAME.equals(folderArray.get(
					i).getName())) {
				folderArray.remove(i);
			}
		}
		return folderArray;
    }
    /**
     * Returns the display name for a folder.
     *
     * <p>
     * This will return localized strings for special folders like the Inbox or the Trash folder.
     * </p>
     *
     * @param context
     *         A {@link Context} instance that is used to get the string resources.
     * @param account
     *         The {@link Account} the folder belongs to.
     * @param name
     *         The name of the folder for which to return the display name.
     *
     * @return The localized name for the provided folder if it's a special folder or the original
     *         folder name if it's a non-special folder.
     */
	public static String getDisplayName(Context context, Account account,
			String name) {
		final String displayName;
		// 垃圾邮件
		if (name.equals(account.getSpamFolderName())
				|| name.equals(context.getResources().getString(
						R.string.special_mailbox_name_spam))) {
			displayName = String.format(
					context.getString(R.string.special_mailbox_name_spam_fmt),
					name);
		} else if (name.equals(account.getArchiveFolderName())
				|| name.equals(context.getResources().getString(
						R.string.special_mailbox_name_archive))) {
			// 归档
			displayName = String
					.format(context
							.getString(R.string.special_mailbox_name_archive_fmt),
							name);
		} else if (name.equals(account.getSentFolderName())
				|| name.equals(context.getResources().getString(
						R.string.special_mailbox_name_sent))) {
			// 已发送
			displayName = String.format(
					context.getString(R.string.special_mailbox_name_sent_fmt),
					name);
		} else if (name.equals(account.getTrashFolderName())
				|| name.equals(context.getResources().getString(
						R.string.special_mailbox_name_trash))) {
			// 已删除
			displayName = String.format(
					context.getString(R.string.special_mailbox_name_trash_fmt),
					name);
		} else if (name.equals(account.getDraftsFolderName())
				|| name.equals(context.getResources().getString(
						R.string.special_mailbox_name_drafts))) {
			// 草稿
			displayName = String
					.format(context
							.getString(R.string.special_mailbox_name_drafts_fmt),
							name);
		} else if (name.equals(account.getOutboxFolderName())
				|| name.equals(context.getResources().getString(
						R.string.special_mailbox_name_outbox))) {
			// 待发送
			displayName = context
					.getString(R.string.special_mailbox_name_outbox);
			// FIXME: We really shouldn't do a case-insensitive comparison here
		} else if (name.equalsIgnoreCase(account.getInboxFolderName())
				|| name.equals(context.getResources().getString(
						R.string.special_mailbox_name_inbox))) {
			// 发件箱
			displayName = context
					.getString(R.string.special_mailbox_name_inbox);
		} else {
			displayName = name;
		}

		return displayName;
	}

	public static Drawable getFolderIcon(Context context, Account account,
			String name) {
		final Drawable folderIcon;
		name=name.trim();
		if (name.equals(account.getSpamFolderName())
				|| name.equals(context.getResources().getString(
						R.string.special_mailbox_name_spam))) {
			folderIcon = context.getResources().getDrawable(
					R.drawable.inon_folder_trash);
		} else if (name.equals(account.getSentFolderName())
				|| name.equals(context.getResources().getString(
						R.string.special_mailbox_name_sent))) {
			folderIcon = context.getResources().getDrawable(
					R.drawable.icon_folder_already_send);
		} else if (name.equals(account.getTrashFolderName())
				|| name.equals(context.getResources().getString(
						R.string.special_mailbox_name_trash))) {
			folderIcon = context.getResources().getDrawable(
					R.drawable.icon_folder_already_delete);
		} else if (name.equals(account.getDraftsFolderName())
				|| name.equals(context.getResources().getString(
						R.string.special_mailbox_name_drafts))) {
			folderIcon = context.getResources().getDrawable(
					R.drawable.icon_folder_drafts);
		} else if (name.equals(account.getOutboxFolderName())
				|| name.equals(context.getResources().getString(
						R.string.special_mailbox_name_outbox))) {
			folderIcon = context.getResources().getDrawable(
					R.drawable.icon_folder_wait_send);
		} else if (name.equalsIgnoreCase(account.getInboxFolderName())
				|| name.equals(context.getResources().getString(
						R.string.special_mailbox_name_inbox))) {
			folderIcon = context.getResources().getDrawable(
					R.drawable.icon_folder_inbox);
		} else {
			folderIcon = context.getResources().getDrawable(
					R.drawable.inon_folder_other);
		}
		return folderIcon;
	}
}
