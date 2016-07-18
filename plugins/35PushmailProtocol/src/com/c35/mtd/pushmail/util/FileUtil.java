package com.c35.mtd.pushmail.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.c35.mtd.pushmail.Debug;
import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.GlobalConstants;
import com.c35.mtd.pushmail.R;
import com.c35.mtd.pushmail.beans.Account;

/**
 * 文件工具类
 * @author:gongfacun
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class FileUtil {

	private static final String TAG = "FileUtil";

	public static final int PIC_QUALITY_MEDUIM = 2;

	/**
	 * 得到保存图片文件夹的绝对路径
	 * @return
	 */
	public static String filePath() {
		String filePath = Environment.getExternalStorageDirectory().getPath() + GlobalConstants.MAIL_DIRECTORY + "/Picture/";
		File out = new File(filePath);
		if (!out.exists()) {
			out.mkdirs();
		}
		return filePath;
	}

	/**
	 * 给定的文件路径，根据等级转换成新的路径
	 * @param newPath
	 *            新的文件路径
	 * @param oldPath
	 *            修改前的文件路径
	 * @param size
	 *            压缩图片标准，中，低两个级
	 * @return 新文件的路径
	 */
	public static String getNewPath(String newPath, String oldPath, int size) {

		int suffixNum = oldPath.lastIndexOf(".");
		int fileNameSta = oldPath.lastIndexOf("/");
		if (size == 2) {
			newPath = newPath + oldPath.substring(fileNameSta + 1, suffixNum) + "(medi)" + oldPath.substring(suffixNum);

		} else {
			newPath = newPath + oldPath.substring(fileNameSta + 1, suffixNum) + "(low)" + oldPath.substring(suffixNum);
		}
		Debug.d(TAG, "newPath=" + newPath);

		return newPath;
	}

	/**
	 * 根据设置来确定上传的图片质量
	 * @param gotPhotoPath
	 *            原图片的路径
	 * @account 默认账号
	 * @context
	 * @return 返回设置要求的图片文件
	 */
	public static File getPicAttchment(String gotPhotoPath, Account account) {
		File attaFile;
		File oldPicture = new File(gotPhotoPath);
		if (account.isAutoPicture()) {
			if (!NetworkUtil.isWifi()) {
				attaFile = ImageUtil.smallPic(gotPhotoPath, FileUtil.getNewPath(FileUtil.filePath(), gotPhotoPath, PIC_QUALITY_MEDUIM), PIC_QUALITY_MEDUIM);
				if (attaFile == null) {
					Debug.e(TAG, "图片压缩失败");
					attaFile = oldPicture;
				}
			} else {
				attaFile = oldPicture;
			}
		} else {
			int quality = account.getPictureQuality();
			if (quality > 1) {
				attaFile = ImageUtil.smallPic(gotPhotoPath, FileUtil.getNewPath(FileUtil.filePath(), gotPhotoPath, quality), quality);
				if (attaFile == null) {
					Debug.e(TAG, "图片压缩失败");
					attaFile = oldPicture;
				}
			} else {
				attaFile = oldPicture;
			}
		}
		return attaFile;
	}

	/**
	 * 转换文件的大小，将文件的字节数转换为kb、mb、或gb
	 * 
	 * @Description:
	 * @param size单位byte
	 * @return保留1位小数
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-1-11
	 */
	public static String sizeLongToString(long size) {
		if (size <= 0) {
			return "0";
		} else {
			String a = "";
			if (size / 1024 < 1024.0) {
				a = String.format("%.1f", size / 1024.0) + "KB";
			} else if (size / 1048576 < 1024) {
				a = String.format("%.1f", size / 1048576.0) + "MB";
			} else {
				a = String.format("%.1f", size / 1073740824.0) + "GB";
			}
			return a;
		}
	}

	/**
	 * 删除某一目录
	 * @param file
	 * @see:
	 * @since:
	 * @author: hanlx
	 * @date:2013-1-18
	 */
	public static void deleteFile(File file) {
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			file.delete();
			return;
		} else {
			File[] tempFile = file.listFiles();
			if (tempFile.length == 0) {
				file.delete();
				return;
			} else {
				for (int i = 0; i < tempFile.length; i++) {
					deleteFile(tempFile[i]);
				}
				file.delete();
			}
		}

	}

	/**
	 * 删除文件夹及文件
	 * @param delpath
	 * @return
	 * @throws Exception
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2013-7-4
	 */
	public static boolean deleteFile(String delpath) throws Exception {
		try {

			File file = new File(delpath);
			if (!file.isDirectory()) {
				file.delete();
			} else if (file.isDirectory()) {
				String[] filelist = file.list();
				for (int i = 0; i < filelist.length; i++) {
					File delfile = new File(delpath + File.separator + filelist[i]);
					if (!delfile.isDirectory()) {
						delfile.delete();
						Log.d(TAG, delfile.getAbsolutePath() + "删除文件成功");
					} else if (delfile.isDirectory()) {
						deleteFile(delpath + File.separator + filelist[i]);
					}
				}
				Log.d(TAG, file.getAbsolutePath() + "删除文件成功");
				file.delete();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 *  复制文件
	 * @param sourceFile
	 * @param targetFile
	 * @throws IOException
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2013-7-5
	 */
	public static void copyFile(File sourceFile, File targetFile) {
		BufferedInputStream inBuff = null;
		BufferedOutputStream outBuff = null;
		try {
			// 新建文件输入流并对它进行缓冲
			inBuff = new BufferedInputStream(new FileInputStream(sourceFile));

			// 新建文件输出流并对它进行缓冲
			outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

			// 缓冲数组
			byte[] b = new byte[1024 * 5];
			int len;
			while ((len = inBuff.read(b)) != -1) {
				outBuff.write(b, 0, len);
			}
			// 刷新此缓冲的输出流
			outBuff.flush();
		} catch (Exception e) {
			Debug.e("failfast", "failfast_AA", e);
		} finally {
			// 关闭流

			try {
				if (inBuff != null) {
					inBuff.close();
				}
				if (outBuff != null) {
					outBuff.close();
				}
			} catch (Exception e) {
				Debug.e("failfast", "failfast_AA", e);
			}

		}
	}

	/**
	 * 
	 *复制文件
	 * @param sourceFile     //mnt/sdcard/35.com/35mail/database/35PushMail.db_att/52956b1a1170c168639832d0_inbox_0_test\MountainLion 2.0功能点列表.xls
	 * @param targetFilePath  /mnt/sdcard//35.com/35mail/files2open
	 * @param targetFileName  test__MountainLion 2.0功能点列表.xls
	 * @see: 
	 * @since: 
	 * @author: 温楠
	 * @date:2013-7-31
	 */
	public static void copyFile(String sourceFile, String targetFilePath, String targetFileName) {

		File file = new File(targetFilePath);
		if (!file.exists() && !file.isDirectory()) {
			file.mkdirs();

		}
		file = new File((targetFilePath + "/" + targetFileName).replace("//", "/"));
		//todo,和上面的copyFile共用
		FileInputStream input = null;
		FileOutputStream output = null;
		try {
			input = new FileInputStream(sourceFile);
			output = new FileOutputStream(file);
			byte[] b = new byte[1024 * 5];
			int len;
			while ((len = input.read(b)) != -1) {
				output.write(b, 0, len);
			}
			output.flush();
			output.close();
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
			Debug.e("failfast", "failfast_AA", e);
		}
	}

	/**
	 * 获取文件后缀名
	 * 
	 * @param 文件对象
	 * @return 文件后缀名
	 */
	private static String getMIMEType(String fName) {
		String type = "";
		/* 取得扩展名 */
		String end = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();
		/* 按扩展名的类型决定MimeType */
		// 在MIME和文件类型的匹配表中找到对应的MIME类型。
		end = "." + end;
		for (int i = 0; i < MIME_MapTable.length; i++) {
			if (end.equals(MIME_MapTable[i][0]))
				type = MIME_MapTable[i][1];
		}

		/* 如果无法直接打开，就弹出软件列表给用户选择 */
		if (!StringUtil.isNotEmpty(type)) {
			type = "*/*";
		}
		return type;
	}

	/**
	 * 调用系统打开邮件附件
	 * 
	 * @param file
	 *            文件对象
	 */
	public static void fileHandle(String filePath, String fileName, Context context) {
		File file = new File(filePath);
		if (!file.exists()) {
		    Log.e(TAG, "File doesn't exist");
			return;
		}
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Intent.ACTION_VIEW);
		/* 调用getMIMEType()来取得MimeType */
		String type = getMIMEType(fileName);
		/* 设定intent的file与MimeType */
		intent.setDataAndType(Uri.fromFile(file), type);
		// intent.setDataAndType(Uri.fromFile(file),"*/*");
		context.startActivity(Intent.createChooser(intent, context.getString(R.string.file_open_style)));
	}

	private static final String[][] MIME_MapTable = {
			// {后缀名， MIME类型}
	{ ".3gp", "video/3gpp" }, { ".apk", "application/vnd.android.package-archive" }, { ".asf", "video/x-ms-asf" }, { ".avi", "video/x-msvideo" }, { ".bin", "application/octet-stream" }, { ".bmp", "image/bmp" }, { ".c", "text/plain" }, { ".class", "application/octet-stream" }, { ".conf", "text/plain" }, { ".cpp", "text/plain" }, { ".doc", "application/msword" }, { ".docx", "application/msword" }, { ".exe", "application/octet-stream" }, { ".gif", "image/gif" }, { ".gtar", "application/x-gtar" }, { ".gz", "application/x-gzip" }, { ".h", "text/plain" }, { ".htm", "text/html" }, { ".html", "text/html" }, { ".jar", "application/java-archive" }, { ".java", "text/plain" }, { ".jpeg", "image/jpeg" }, { ".jpg", "image/jpeg" }, { ".js", "application/x-javascript" }, { ".log", "text/plain" }, { ".m3u", "audio/x-mpegurl" }, { ".m4a", "audio/mp4a-latm" }, { ".m4b", "audio/mp4a-latm" }, { ".m4p", "audio/mp4a-latm" }, { ".m4u", "video/vnd.mpegurl" }, { ".m4v", "video/x-m4v" }, { ".mov", "video/quicktime" }, { ".mp2", "audio/x-mpeg" }, { ".mp3", "audio/x-mpeg" }, { ".mp4", "video/mp4" }, { ".mpc", "application/vnd.mpohun.certificate" }, { ".mpe", "video/mpeg" }, { ".mpeg", "video/mpeg" }, { ".mpg", "video/mpeg" }, { ".mpg4", "video/mp4" }, { ".mpga", "audio/mpeg" }, { ".msg", "application/vnd.ms-outlook" }, { ".ogg", "audio/ogg" }, { ".pdf", "application/pdf" }, { ".png", "image/png" }, { ".pps", "application/vnd.ms-powerpoint" }, { ".ppt", "application/vnd.ms-powerpoint" }, { ".pptx", "application/vnd.ms-powerpoint" }, { ".prop", "text/plain" }, { ".rar", "application/x-rar-compressed" }, { ".rc", "text/plain" }, { ".rmvb", "audio/x-pn-realaudio" }, { ".rtf", "application/rtf" }, { ".sh", "text/plain" }, { ".tar", "application/x-tar" }, { ".tgz", "application/x-compressed" }, { ".txt", "text/plain" }, { ".wav", "audio/x-wav" }, { ".wma", "audio/x-ms-wma" }, { ".wmv", "audio/x-ms-wmv" }, { ".wps", "application/vnd.ms-works" }, { ".xls", "application/vnd.ms-excel" }, { ".xlsx", "application/vnd.ms-excel" }, { ".xml", "text/plain" }, { ".z", "application/x-compress" }, { ".zip", "application/zip" }, { "", "*/*" } };

	/**
	 * 用浏览器打开html文件
	 * @param context
	 * @param file
	 * @see: 
	 * @since: 
	 * @author: 温楠
	 * @date:2013-8-9
	 */
	public static void viewHtmlFlie(Context context, File file) {
		try {// 默认系统浏览器
			Uri u = Uri.fromFile(file);
			Intent it = new Intent();
			it.setAction(Intent.ACTION_VIEW);
			it.setDataAndType(u, "text/html");
			it.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
			context.startActivity(it);
		} catch (Exception e) {
			try {// 选择列表打开
				Uri u = Uri.fromFile(file);
				Intent it = new Intent();
				it.setAction(Intent.ACTION_VIEW);
				it.setDataAndType(u, "text/html");
				context.startActivity(it);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			
		}
	}
	
	
	/**
	 * 用浏览器打开html文件
	 * @param context
	 * @param file
	 * @see: 
	 * @since: 
	 * @author: 温楠
	 * @date:2013-8-9
	 */
	public static void viewHtmlFlie(Context context, String html) {
		try {// 默认系统浏览器
			Uri u = Uri.parse(html);
			Intent it = new Intent();
			it.setAction(Intent.ACTION_VIEW);
			it.setDataAndType(u, "text/html");
			it.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
			context.startActivity(it);
		} catch (Exception e) {
			try {// 选择列表打开
				Uri u = Uri.parse(html);
				Intent it = new Intent();
				it.setAction(Intent.ACTION_VIEW);
				it.setDataAndType(u, "text/html");
				context.startActivity(it);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			
		}
	}
}
