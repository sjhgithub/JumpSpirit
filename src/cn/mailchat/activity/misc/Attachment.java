package cn.mailchat.activity.misc;

import java.util.List;
import java.util.StringTokenizer;

import cn.mailchat.MailChat;
import cn.mailchat.activity.MessageReference;
import cn.mailchat.helper.Utility;
import cn.mailchat.mail.Flag;
import cn.mailchat.mail.MessagingException;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Container class for information about an attachment.
 *
 * This is used by {@link cn.mailchat.activity.MessageCompose} to fetch and manage attachments.
 */
public class Attachment implements Parcelable {
    /**
     * The URI pointing to the source of the attachment.
     *
     * In most cases this will be a {@code content://}-URI.
     */
    public Uri uri;

    /**
     * The current loading state.
     */
    public LoadingState state;

    /**
     * The ID of the loader that is used to load the metadata or contents.
     */
    public int loaderId;

    /**
     * The content type of the attachment.
     *
     * Only valid when {@link #state} is {@link LoadingState#METADATA} or
     * {@link LoadingState#COMPLETE}.
     */
    public String contentType;

    /**
     * The (file)name of the attachment.
     *
     * Only valid when {@link #state} is {@link LoadingState#METADATA} or
     * {@link LoadingState#COMPLETE}.
     */
    public String name;

    /**
     * The size of the attachment.
     *
     * Only valid when {@link #state} is {@link LoadingState#METADATA} or
     * {@link LoadingState#COMPLETE}.
     */
    public long size;

    /**
     * The name of the temporary file containing the local copy of the attachment.
     *
     * Only valid when {@link #state} is {@link LoadingState#COMPLETE}.
     */
    public String filename;
    
    // 记录内联附件Content-ID, Content-Disposition, Attachment Store Data
    // Modified by LL
    // BEGIN
    public String contentId;
    public String contentDisposition;
    public String storeData;
    // END
    
    public Attachment() {}

    public static enum LoadingState {
        /**
         * The only thing we know about this attachment is {@link #uri}.
         */
        URI_ONLY,

        /**
         * The metadata of this attachment have been loaded.
         *
         * {@link #contentType}, {@link #name}, and {@link #size} should contain usable values.
         */
        METADATA,

        /**
         * The contents of the attachments have been copied to the temporary file {@link #filename}.
         */
        COMPLETE,

        /**
         * Something went wrong while trying to fetch the attachment's contents.
         */
        CANCELLED,
        
        // Modified by LL
        DELETED
    }
    
   
    // === Parcelable ===

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(uri, flags);
        dest.writeSerializable(state);
        dest.writeInt(loaderId);
        dest.writeString(contentType);
        dest.writeString(name);
        dest.writeLong(size);
        dest.writeString(filename);
        
        // 写入内联附件Content-ID, Content-Disposition, Attachment Store Data
        // Modified by LL
        // BEGIN
        dest.writeString(contentId);
        dest.writeString(contentDisposition);
        dest.writeString(storeData);
        // END
    }

    public static final Parcelable.Creator<Attachment> CREATOR =
            new Parcelable.Creator<Attachment>() {
        @Override
        public Attachment createFromParcel(Parcel in) {
            return new Attachment(in);
        }

        @Override
        public Attachment[] newArray(int size) {
            return new Attachment[size];
        }
    };

    public Attachment(Parcel in) {
        uri = in.readParcelable(Uri.class.getClassLoader());
        state = (LoadingState) in.readSerializable();
        loaderId = in.readInt();
        contentType = in.readString();
        name = in.readString();
        size = in.readLong();
        filename = in.readString();
        
        // 读取内联附件Content-ID, Content-Disposition, Attachment Store Data
        // Modified by LL
        // BEGIN
        contentId = in.readString();
        contentDisposition = in.readString();
    	storeData = in.readString();
        // END
    }
    
    /*
    // 使用Identity String的方案
    // Modified by LL
    // BEGIN
    private final String IDENTITY_SEPARATOR = "###";
    private final String NULL_STRING = "NULL";
    
    public String toIdentityString() {
        StringBuilder refString = new StringBuilder();

        refString.append(uri != null ? uri.toString() : NULL_STRING);
        refString.append(IDENTITY_SEPARATOR);
        refString.append(state.name());
        refString.append(IDENTITY_SEPARATOR);
        refString.append(loaderId);
        refString.append(IDENTITY_SEPARATOR);
        refString.append(contentType);
        refString.append(IDENTITY_SEPARATOR);
        refString.append(name);
        refString.append(IDENTITY_SEPARATOR);
        refString.append(size);
        refString.append(IDENTITY_SEPARATOR);
        refString.append(filename);
        refString.append(IDENTITY_SEPARATOR);
        refString.append(contentId);
        refString.append(IDENTITY_SEPARATOR);
        refString.append(messageReference != null ? messageReference.toIdentityString() : NULL_STRING);
        refString.append(IDENTITY_SEPARATOR);
        refString.append(attachmentId);

        return refString.toString();
    }
    
    public Attachment(String identity) throws MessagingException {
        
        if (identity == null || identity.length() < 1) {
            throw new MessagingException("Null or truncated Attachment identity.");
        }

        StringTokenizer tokens = new StringTokenizer(identity, IDENTITY_SEPARATOR, false);
        if (tokens.countTokens() == 10) {
        	String token = tokens.nextToken();
        	if (NULL_STRING.equals(token)) {
        		uri = null;
        	} else {
        		uri = Uri.parse(token);
        	}
        	
            state = LoadingState.valueOf(tokens.nextToken());
            loaderId = Integer.parseInt(tokens.nextToken());
            contentType = tokens.nextToken();
            name = tokens.nextToken();
            size = Long.parseLong(tokens.nextToken());
            filename = tokens.nextToken();
            contentId = tokens.nextToken();
            
            token = tokens.nextToken();
            if (NULL_STRING.equals(token)) {
            	messageReference = null;
            } else {
            	messageReference = new MessageReference(token);
            }
            
            attachmentId = Long.parseLong(tokens.nextToken());
        } else {
        	throw new MessagingException("Parse attachment identity string failed.");
        }
    }
    // END
    */
}
