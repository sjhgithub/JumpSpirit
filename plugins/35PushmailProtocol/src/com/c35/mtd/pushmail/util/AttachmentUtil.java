package com.c35.mtd.pushmail.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.c35.mtd.pushmail.EmailApplication;
import com.c35.mtd.pushmail.R;

/**
 * 
 * @Description:附件图标工具类
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-9-19
 */
public class AttachmentUtil {

	private static Bitmap attach_apk_icon = null;
	private static Bitmap attach_pdf_icon = null;
	private static Bitmap attach_txt_icon = null;
	private static Bitmap attach_word_icon = null;
	private static Bitmap attach_excel_icon = null;
	private static Bitmap attach_ppt_icon = null;
	private static Bitmap attach_access_icon = null;
	private static Bitmap attach_compress_icon_up = null;
	private static Bitmap attach_compress_icon = null;
	private static Bitmap attach_pic_icon = null;
	private static Bitmap attach_audio_icon = null;
	private static Bitmap attach_video_icon = null;
	private static Bitmap attach_mail_icon = null;
	private static Bitmap attach_flash_icon = null;
	private static Bitmap attach_html_icon = null;
	private static Bitmap attach_file_icon = null;
	private static AttachmentUtil instance = null;
	private String[] mimeArray = null;

	private AttachmentUtil() {
		attach_apk_icon = BitmapFactory.decodeResource(EmailApplication.getInstance().getResources(), R.drawable.attach_apk_icon);
		attach_pdf_icon = BitmapFactory.decodeResource(EmailApplication.getInstance().getResources(), R.drawable.attach_pdf_icon);
		attach_txt_icon = BitmapFactory.decodeResource(EmailApplication.getInstance().getResources(), R.drawable.attach_txt_icon);
		attach_word_icon = BitmapFactory.decodeResource(EmailApplication.getInstance().getResources(), R.drawable.attach_word_icon);
		attach_excel_icon = BitmapFactory.decodeResource(EmailApplication.getInstance().getResources(), R.drawable.attach_excel_icon);
		attach_ppt_icon = BitmapFactory.decodeResource(EmailApplication.getInstance().getResources(), R.drawable.attach_ppt_icon);
		attach_access_icon = BitmapFactory.decodeResource(EmailApplication.getInstance().getResources(), R.drawable.attach_access_icon);
		attach_compress_icon_up = BitmapFactory.decodeResource(EmailApplication.getInstance().getResources(), R.drawable.rar_up);
		attach_compress_icon = BitmapFactory.decodeResource(EmailApplication.getInstance().getResources(), R.drawable.attach_compress_icon);
		attach_pic_icon = BitmapFactory.decodeResource(EmailApplication.getInstance().getResources(), R.drawable.attach_pic_icon);
		attach_audio_icon = BitmapFactory.decodeResource(EmailApplication.getInstance().getResources(), R.drawable.attach_audio_icon);
		attach_video_icon = BitmapFactory.decodeResource(EmailApplication.getInstance().getResources(), R.drawable.attach_video_icon);
		attach_mail_icon = BitmapFactory.decodeResource(EmailApplication.getInstance().getResources(), R.drawable.attach_mail_icon);
		attach_flash_icon = BitmapFactory.decodeResource(EmailApplication.getInstance().getResources(), R.drawable.attach_flash_icon);
		attach_html_icon = BitmapFactory.decodeResource(EmailApplication.getInstance().getResources(), R.drawable.attach_html_icon);
		attach_file_icon = BitmapFactory.decodeResource(EmailApplication.getInstance().getResources(), R.drawable.attach_file_icon);
		// mimeArray = new String[] { ".txt",".doc", ".docx", ".ppt", ".pptx", ".xls", ".xlsx", ".pdf",
		// ".java", ".jsp", ".js", ".c", ".cpp", ".h", ".hpp", ".py", ".cs", ".sh", ".css", ".rar", ".zip"
		// ,".tar",".gz",".html",".htm",".jpg",".png",".bmp",".gif",".jpeg" };
		mimeArray = new String[] { ".txt", ".doc", ".docx", ".ppt", ".pptx", ".xls", ".xlsx", ".pdf", ".java", ".jsp", ".js", ".c", ".cpp", ".h", ".hpp", ".py", ".cs", ".sh", ".css", ".rar", ".zip", ".tar", ".gz", ".html", ".htm" };
	}

	public static AttachmentUtil getInstance() {
		if (instance == null) {
			return new AttachmentUtil();
		}
		return instance;
	}

	/**
	 * 根据后缀获取相应类型图片
	 * @param fileName
	 *            文件名
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-9-5
	 */
	public Bitmap selectFileIcon(String fileName, boolean fromAttachmentList) {
		fileName = fileName.toLowerCase();
		if (fileName.endsWith(".apk")) {
			return attach_apk_icon;
		} else if (fileName.endsWith(".pdf")) {
			return attach_pdf_icon;
		} else if (fileName.endsWith(".txt") || fileName.endsWith(".xml")) {
			return attach_txt_icon;
		} else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
			return attach_word_icon;
		} else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
			return attach_excel_icon;
		} else if (fileName.endsWith(".pps") || fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
			return attach_ppt_icon;
		} else if (fileName.endsWith(".mdb") || fileName.endsWith(".accdb")) {
			return attach_access_icon;
		} else if (fileName.endsWith(".zip") || fileName.endsWith(".rar")) {
			//若是从附件夹列表使用的，则返回不带展开合起的icon
			if (fromAttachmentList) {
				return attach_compress_icon;
			}
			return attach_compress_icon_up;
		} else if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".bmp") || fileName.endsWith(".jpeg") || fileName.endsWith(".gif")) {
			return attach_pic_icon;
		} else if (fileName.endsWith(".mp3") || fileName.endsWith(".wma") || fileName.endsWith(".wav") || fileName.endsWith(".midi") || fileName.endsWith(".mid") || fileName.endsWith(".amr") || fileName.endsWith(".aif") || fileName.endsWith(".m4a") || fileName.endsWith(".xmf") || fileName.endsWith(".ogg")) {
			return attach_audio_icon;
		} else if (fileName.endsWith(".avi") || fileName.endsWith(".rm") || fileName.endsWith(".mpeg") || fileName.endsWith(".mpg") || fileName.endsWith(".dat") || fileName.endsWith(".ra") || fileName.endsWith(".rmvb") || fileName.endsWith(".mov") || fileName.endsWith(".qt") || fileName.endsWith(".mp4") || fileName.endsWith(".3gp")) {
			return attach_video_icon;
		} else if (fileName.endsWith(".eml")) {
			return attach_mail_icon;
		} else if (fileName.endsWith(".swf")) {
			return attach_flash_icon;
		} else if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return attach_html_icon;
		} else {
			return attach_file_icon;
		}
	}

	/**
	 * 
	 * @Description:文件是否支持预览
	 * @param fileName
	 *            文件名
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-11-8
	 */
	public boolean canPreview(String fileName) {
		fileName = fileName.toLowerCase();
		for (int i = 0; i < mimeArray.length; i++) {
			if (fileName.endsWith(mimeArray[i])) {
				return true;
			}

		}
		return false;
	}

	/**
	 * 判断是否是图片类型
	 * 
	 * @Description:
	 * @param fileName
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-1-9
	 */
	public static boolean isPicture(String fileName) {
		if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".bmp") || fileName.endsWith(".jpeg") || fileName.endsWith(".gif")) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 
	 * @Description:是否音频文件
	 * @param fileName
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2013-4-19
	 */
	public static boolean isAudio(String fileName) {
		if (fileName.endsWith(".amr")) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 判断是否是文档类型
	 * 
	 * @Description:
	 * @param fileName
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-1-9
	 */
	public static boolean isDocument(String fileName) {
		if (fileName.endsWith(".pdf") || fileName.endsWith(".txt") || fileName.endsWith(".xml") || fileName.endsWith(".doc") || fileName.endsWith(".docx") || fileName.endsWith(".xls") || fileName.endsWith(".xlsx") || fileName.endsWith(".pps") || fileName.endsWith(".ppt") || fileName.endsWith(".pptx") || fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return true;
		} else {
			return false;
		}

	}

}
