package cn.mailchat.beans;

import java.io.Serializable;


/**
 * 详情页选择文件时所需的文件对象
 * 
 * @Description:
 * @author: zhuanggy
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2013-9-23
 */
public class PickedFileInfo implements Serializable {

	public long mSize;
	public String mName;
	public String mContentUri;
	public String mContentType;
	public boolean mIsDir;
	
}
