package cn.mailchat.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import cn.mailchat.R;


public class AttachmentUtil {

	private static Bitmap attach_apk_icon = null;
	private static Bitmap attach_pdf_icon = null;
	private static Bitmap attach_txt_icon = null;
	private static Bitmap attach_word_icon = null;
	private static Bitmap attach_excel_icon = null;
	private static Bitmap attach_ppt_icon = null;
	private static Bitmap attach_access_icon = null;
	private static Bitmap attach_compress_icon_open = null;
	private static Bitmap attach_compress_icon_close = null;
	private static Bitmap attach_compress_icon = null;
	private static Bitmap attach_compress_icon_up = null;
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
	}

	private AttachmentUtil(Context context) {
		attach_apk_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_attach_apk);
		attach_pdf_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_attach_pdf);
		attach_txt_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_attach_txt);
		attach_word_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_attach_word);
		attach_excel_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_attach_excel);
		attach_ppt_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_attach_ppt);
		attach_access_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_attach_access);
		attach_compress_icon_open = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_attach_rar_open);
		attach_compress_icon_close = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_attach_rar_close);
		attach_compress_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_attach_rar);
		attach_pic_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_attach_pic);
		attach_audio_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_attach_audio);
		attach_video_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_attach_video);
		attach_mail_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_attach_mail);
		attach_flash_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_attach_flash);
		attach_html_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_attach_html);
		attach_file_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_attach_file);
		mimeArray = new String[] { ".txt", ".doc", ".docx", ".ppt", ".pptx",
				".xls", ".xlsx", ".pdf", ".java", ".jsp", ".js", ".c", ".cpp",
				".h", ".hpp", ".py", ".cs", ".sh", ".css", ".rar", ".zip",
				".tar", ".gz", ".html", ".htm", ".jpg", ".png", ".bmp", ".gif",
				".jpeg" };
		//mimeArray = new String[] { ".txt", ".doc", ".docx", ".ppt", ".pptx", ".xls", ".xlsx", ".pdf", ".java", ".jsp", ".js", ".c", ".cpp", ".h", ".hpp", ".py", ".cs", ".sh", ".css", ".rar", ".zip", ".tar", ".gz", ".html", ".htm" };

	}

	/**
	 * 获取文件后缀
	 * 
	 * @Description:
	 * @param fileName
	 * @return
	 * @see:
	 * @since:
	 * @author: Zhonggaoyong
	 * @date:2013-10-31
	 */
	public String fileSuffix(String fileName) {
		for (int i = 0; i < mimeArray.length; i++) {
			if (fileName.contains(mimeArray[i])) {
				return mimeArray[i];
			}
		}
		return "";
	}

	public static AttachmentUtil getInstance(Context context) {
		if (instance == null) {
			return new AttachmentUtil(context);
		}
		return instance;
	}

	/**
	 * 
	 * @Description:根据后缀获取相应类型图片
	 * @param context
	 * @param fileName
	 *            文件名
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-9-5
	 */
	public Bitmap getAttachmentIcon(String fileName, boolean ifCompressCanOpen) {
		if (StringUtil.isEmpty(fileName)) {
			return attach_file_icon;
		}
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
		} else if (fileName.endsWith(".zip") || fileName.endsWith(".rar")) {
			// 若能展开，则返回展开的icon
			if (ifCompressCanOpen) {
				return attach_compress_icon_open;
			}
			return attach_compress_icon;
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
	 * 写信页根据附件名称获得icon
	 * 
	 * @Description:
	 * @param context
	 * @param fileName
	 * @param uri
	 * @return
	 * @see:
	 * @since:
	 * @author: zhuanggy
	 * @date:2013-9-23
	 */
	public static Bitmap getAttachmentIconForMessageCompose(Context context, String fileName, String uri) {
		if (StringUtil.isEmpty(fileName)) {
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_file_file);
		}
		if (fileName.endsWith(".apk")) {
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_file_apk);
		} else if (fileName.endsWith(".pdf")) {
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_file_pdf);
		} else if (fileName.endsWith(".txt") || fileName.endsWith(".xml")) {
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_file_txt);
		} else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_file_word);
		} else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_file_excel);
		} else if (fileName.endsWith(".pps") || fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_file_ppt);
		} else if (fileName.endsWith(".zip") || fileName.endsWith(".rar")) {
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_file_zip);
		} else if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".bmp") || fileName.endsWith(".jpeg") || fileName.endsWith(".gif")) {
			// 防止内存溢出
			Options opts = new Options();
			opts.inSampleSize = 4;
			opts.inJustDecodeBounds = false;
			opts.inPreferredConfig = Bitmap.Config.RGB_565;
			return BitmapFactory.decodeFile(Uri.parse(uri).getPath(), opts);
		} else if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_file_html);
		} else {
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_file_file);
		}
	}

	/**
	 * 
	 * @Description:根据后缀获取相应类型图片
	 * @param context
	 * @param fileName
	 *            文件名
	 * @return
	 * @see:
	 * @since:
	 * @author: 温楠
	 * @date:2012-9-5
	 */
	public Bitmap selectFileIcon(String fileName, boolean fromAttachmentList) {

		fileName = (fileName == null ? "" : fileName).toLowerCase();

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
		} else if (fileName.endsWith(".zip") || fileName.endsWith(".rar")) {
			// 若是从附件夹列表使用的，则返回不带展开合起的icon
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
	
}
