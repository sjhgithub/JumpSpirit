package cn.mailchat.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.GlobalConstants;
import com.c35.mtd.pushmail.beans.Account;
import com.c35.mtd.pushmail.beans.C35Attachment;
import com.c35.mtd.pushmail.beans.C35Folder;
import com.c35.mtd.pushmail.beans.C35Message;
import com.c35.mtd.pushmail.command.request.GetIdsByIdRequest;
import com.c35.mtd.pushmail.command.response.GetFolderListResponse;
import com.c35.mtd.pushmail.exception.MessagingException;
import com.c35.mtd.pushmail.logic.AccountUtil;
import com.c35.mtd.pushmail.logic.C35AccountManager;
import com.c35.mtd.pushmail.util.MailUtil;
import com.c35.mtd.pushmail.store.C35Store;
import com.c35.mtd.pushmail.store.Folder;
import com.c35.mtd.pushmail.store.Store;

public class WeemailUtil {

    public static final String TAG = "WeemailUtil";

    public static Account createC35Account(String email, String password) throws MessagingException {
        if (email == null || password == null) {
            throw new MessagingException("非法参数");
        }

        Account c35Account = new Account();
        c35Account.setEmail(MailUtil.convert35CNToChinaChannel(email));
        c35Account.setPassword(password);
        c35Account.setAutomaticCheckIntervalMinutes(-1);
        c35Account.setNotifyNewMail(false);
        c35Account.setAutoSync(false);
        c35Account.setShakeUnRead(false);
        return c35Account;
    }

    public static void validateC35Account(Account c35Account) throws MessagingException {
        if (c35Account == null) {
            throw new MessagingException("非法参数");
        }

        String email = c35Account.getEmail();
        String password = c35Account.getPassword();

        AccountUtil c35AccountUtil = new AccountUtil(null,
                email,
                password,
                c35Account,
                false,
                TAG,
                false);
        try {
            AccountUtil.getProxyDomainToAccountFromDP(email, password, c35Account);
            c35AccountUtil.save2SharedPreferences(c35Account);
        } catch (Throwable e) {
            throw new MessagingException(TAG, e);
        }
    }

    public static Account getC35Account(cn.mailchat.Account account) throws MessagingException {
        if (account == null) {
            throw new MessagingException("非法参数");
        }

        int version = account.getVersion_35Mail();
        if (version == 1 || version == 2) {
            String email = account.getEmail();

            C35AccountManager c35AccountManager = C35AccountManager.getInstance();
            List<Account> c35Accounts = c35AccountManager.getAccountsFromSP();
            if (c35Accounts.size() > 0) {
                for (Account c35Account : c35Accounts) {
                    if (c35Account.getEmail().equals(MailUtil.convert35CNToChinaChannel(email))) {
                        return c35Account;
                    }
                }
            }

            String storeUri = account.getStoreUri();
            cn.mailchat.mail.ServerSettings settings = cn.mailchat.mail.Store.decodeStoreUri(storeUri);
            String password = settings.password;

            Account c35Account = WeemailUtil.createC35Account(email, password);
            WeemailUtil.validateC35Account(c35Account);
            return c35Account;
        } else {
            throw new MessagingException("非三五企业邮箱账号");
        }
    }

    public static void deleteC35Account(cn.mailchat.Account account) throws MessagingException {
        if (account == null) {
            throw new MessagingException("非法参数");
        }

        int version = account.getVersion_35Mail();
        if (version == 1 || version == 2) {
            String email = account.getEmail();

            C35AccountManager c35AccountManager = C35AccountManager.getInstance();
            List<Account> c35Accounts = c35AccountManager.getAccountsFromSP();
            if (c35Accounts.size() > 0) {
                for (Account c35Account : c35Accounts) {
                    if (c35Account.getEmail().equals(MailUtil.convert35CNToChinaChannel(email))) {
                        c35Account.delete(c35AccountManager);
                        return;
                    }
                }
            }
        } else {
            throw new MessagingException("非三五企业邮箱账号");
        }
    }

    public static void clearC35Accounts() {
        C35AccountManager c35AccountManager = C35AccountManager.getInstance();
        List<Account> c35Accounts = c35AccountManager.getAccountsFromSP();
        if (c35Accounts.size() > 0) {
            for (Account c35Account : c35Accounts) {
                c35Account.delete(c35AccountManager);
            }
        }
    }

    public static C35Store getC35Store(Account c35Account) throws MessagingException {
        if (c35Account == null) {
            throw new MessagingException("非法参数");
        }

        C35Store c35Store = (C35Store) Store.getInstance(c35Account.getStoreUri());
        c35Store.openAndGetTicket();
        return c35Store;
    }

    public static String getC35FolderId(cn.mailchat.Account account,
            String folder,
            Account c35Account,
            C35Store c35Store) throws MessagingException {

        if (account == null || folder == null || c35Account == null || c35Store == null) {
            throw new MessagingException("非法参数");
        }

        if (folder.equals(account.getInboxFolderName())) {
            return EmailApplication.MAILBOX_INBOX;
        } else if (folder.equals(account.getSentFolderName())) {
            return EmailApplication.MAILBOX_SENTBOX;
        } else if (folder.equals(account.getDraftsFolderName())) {
            return EmailApplication.MAILBOX_DRAFTSBOX;
        } else if (folder.equals(account.getTrashFolderName())) {
            return EmailApplication.MAILBOX_TRASHBOX;
        } else if (folder.equals(account.getSpamFolderName())) {
            return ".Spam";
        }

        List<C35Folder> c35Folders = c35Store.getFolderList(c35Store.getTiket(), GetFolderListResponse.GetFolderType.SELFDEFINED);
        for (C35Folder c35Folder : c35Folders) {
            if (folder.equals(c35Folder.getFolderName())) {
                return c35Folder.getFolderId();
            }
        }

        throw new MessagingException("找不到对应文件夹");
    }

    public static String getC35MailId(cn.mailchat.Account account,
            cn.mailchat.mail.Message message,
            Account c35Account,
            C35Store c35Store) throws MessagingException {

        if (account == null || message == null || c35Account == null || c35Store == null) {
            throw new MessagingException("非法参数");
        }

        String mailId = message.getMailId();
        if (mailId != null && mailId.length() > 0) {
            return mailId;
        }

        int version = account.getVersion_35Mail();
        if (version == 2) {
            String c35FolderId = WeemailUtil.getC35FolderId(account, message.getFolder().getName(), c35Account, c35Store);
            if (c35FolderId != null && c35FolderId.startsWith(".")) {
                c35FolderId = "INBOX" + c35FolderId;
            }
            return message.getUid() + "_" + c35FolderId;
        } else if (version == 1) {
            String c35FolderId = WeemailUtil.getC35FolderId(account, message.getFolder().getName(), c35Account, c35Store);
            String messageSentDate = TimeUtils.DateFormatYMDHM.format(message.getSentDate());
            List<C35Message> c35Messages = c35Store.advanceSearchMails(c35Account.getEmail(),
                    c35FolderId,
                    message.getSubject(),
                    messageSentDate + ":00",
                    messageSentDate + ":59");
            if (c35Messages.size() != 1) {
                throw new MessagingException("通过搜索无法确定唯一邮件");
            } else {
                return c35Messages.get(0).getMailId();
            }
        } else {
            throw new MessagingException("非三五企业邮箱账号");
        }
    }

    public static C35Message getC35Message(C35Store c35Store, String c35MailId) throws MessagingException {
        return c35Store.getMailById(c35MailId, 0, 0, GlobalConstants.GZIP_BASE64, 4);
    }

    public static String getC35PreviewUrl(cn.mailchat.Account account,
            cn.mailchat.mail.Message message,
            String attachmentName) throws MessagingException {
        if (account == null || message == null || attachmentName == null) {
            throw new MessagingException("非法参数");
        }

        Account c35Account = getC35Account(account);
        C35Store c35Store = getC35Store(c35Account);
        String c35MailId = getC35MailId(account, message, c35Account, c35Store);
        C35Message c35Message = getC35Message(c35Store, c35MailId);

        for (C35Attachment c35Attachment : c35Message.getAttachs()) {
            if (attachmentName.equals(c35Attachment.getFileName())) {
                return c35Store.fileViewByHtml(c35Attachment.getMailId(),
                        c35Attachment.getId(),
                        c35Attachment.getCid(),
                        null);
            }
        }

        throw new MessagingException("无法找到对应附件");
    }

    public static C35Message createC35Message(cn.mailchat.mail.Message message) throws MessagingException {
        if (message == null) {
            throw new MessagingException("非法参数");
        }

        try {
            C35Message c35Message = new C35Message();

            List<String> from = convertToC35Addresses(message.getFrom());
            c35Message.setFrom(from.get(0));
            List<String> to = convertToC35Addresses(message.getRecipients(cn.mailchat.mail.Message.RecipientType.TO));
            c35Message.setTo(to);
            List<String> cc = convertToC35Addresses(message.getRecipients(cn.mailchat.mail.Message.RecipientType.CC));
            c35Message.setCc(cc);
            List<String> bcc = convertToC35Addresses(message.getRecipients(cn.mailchat.mail.Message.RecipientType.BCC));
            c35Message.setBcc(bcc);

            c35Message.setSubject(message.getSubject());
            Set<cn.mailchat.mail.Part> parts = cn.mailchat.mail.internet.MimeUtility.collectTextParts(message);
            for (cn.mailchat.mail.Part part : parts) {
                if ("text/plain".equalsIgnoreCase(part.getMimeType())) {
                    cn.mailchat.mail.internet.TextBody body = (cn.mailchat.mail.internet.TextBody)part.getBody();
                    c35Message.setPlainText(body.getText());
                    c35Message.setPlainTextCharset(body.getCharset());
                } else if ("text/html".equalsIgnoreCase(part.getMimeType())) {
                    cn.mailchat.mail.internet.TextBody body = (cn.mailchat.mail.internet.TextBody)part.getBody();
                    c35Message.setHyperText(body.getText());
                    c35Message.setHyperTextCharset(body.getCharset());
                }
            }

            return c35Message;
        } catch (MessagingException e) {
            throw e;
        } catch (cn.mailchat.mail.MessagingException e) {
            throw new MessagingException("创建C35Message失败", e);
        }
    }

    public static List<String> convertToC35Addresses(cn.mailchat.mail.Address[] addresses) throws MessagingException {
        if (addresses == null) {
            throw new MessagingException("非法参数");
        }

        List<String> c35Addresses = new ArrayList<String>();
        for (cn.mailchat.mail.Address address : addresses) {
            c35Addresses.add(address.toString());
        }
        return c35Addresses;
    }

    public static void test() {
        try {
            C35AccountManager accountManager = C35AccountManager.getInstance();
            List<Account> accounts = accountManager.getAccountsFromSP();

            if (accounts.size() > 0) {
                cn.mailchat.MailChat.toast("Total " + accounts.size() + " accounts");

                Account acc = accountManager.getDefaultAccount();
                C35Store store = (C35Store) Store.getInstance(acc.getStoreUri());
                store.openAndGetTicket();

                List<C35Folder> folders = store.getFolderList(store.getTiket(), GetFolderListResponse.GetFolderType.SELFDEFINED);
                String toast = "\n";
                for (C35Folder folder : folders) {
                    toast += folder.getFolderName() + "\n";
                }
                cn.mailchat.MailChat.toast(toast);

                List<String> mailIds = store.getIdsById(EmailApplication.MAILBOX_INBOX, 10, null, GetIdsByIdRequest.GetIdsType.NEW);
                toast = "\n";
                for (String mailId : mailIds) {
                    toast += mailId + "\n";
                }
                cn.mailchat.MailChat.toast(toast);

                List<C35Message> messages = store.getMailListByMailIds(mailIds, 100);
                toast = "";
                for (C35Message message : messages) {
                    toast += "\n" + message.getSendTime() + "\n【" + message.getFrom() + "】\n" + message.getSubject() + "\n";
                }
                cn.mailchat.MailChat.toast(toast);

//                C35Message msg = store.getMailById("565268bfad34d3121a3f66a3", 0, 0, 0, 4);
//                MailChat.toast(msg.getPlainText());

                List<C35Message> msgs = store.advanceSearchMails(acc.getEmail(),
                        EmailApplication.MAILBOX_INBOX,
                        "test #1",
                        "2015-11-23 09:10:00",
                        "2015-11-23 09:10:59");
                toast = "";
                for (C35Message msg : msgs) {
                    toast += "\n" + msg.getSendTime() + "\n【" + msg.getFrom() + "】\n" + msg.getSubject() + "\n";
                }
                cn.mailchat.MailChat.toast(toast);
            } else {
                Account account = WeemailUtil.createC35Account("email", "password");
                WeemailUtil.validateC35Account(account);
                cn.mailchat.MailChat.toast("DONE");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            cn.mailchat.MailChat.toast("Failed: " + e.toString());
        }
    }
}