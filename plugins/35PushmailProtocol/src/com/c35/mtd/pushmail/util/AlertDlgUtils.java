package com.c35.mtd.pushmail.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

import com.c35.mtd.pushmail.R;

/**
 * 为避免代码重复，用于显示简单的提示对话框。 本次添加是为了显示“演示视频”的耗费流量警告提示，以后也可使用；
 * @author:zhuanggy
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-8-21
 */

public class AlertDlgUtils {

	private static final String MAIL_DEMO_VIDEO = "http://www.35.com/mail/pushmail.php";

	/**
	 * 弹出耗费流量的提示
	 * 
	 * @Description:
	 * @param context
	 * @see:
	 * @since:
	 * @author: zgy
	 * @date:2012-8-22
	 */
	public static void showAdVideoAlertDlg(final Context context) {
	    // Modified by LL
	    /*
		MailDialog.Builder builder = new MailDialog.Builder(context);
		builder.setTitle(context.getString(R.string.operate_notice));
		builder.setMessage(context.getString(R.string.alertstr_addisplay));
		builder.setPositiveButton(context.getString(R.string.okay_action), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				// 打开网址，进入视频演示
				// try {
				// URL url = new URL(MAIL_DEMO_VIDEO);
				// URLConnection urlcon = url.openConnection();
				// InputStream is = urlcon.getInputStream();
				// BufferedInputStream bfis = new BufferedInputStream(is);
				// ByteArrayBuffer btab = new ByteArrayBuffer(50);
				// int current = 0;
				// while ((current = bfis.read()) != -1) {
				// btab.append((byte) current);
				// }
				// // 将缓存的内容转化为String,用UTF-8编码
				// String myString = EncodingUtils.getString(btab.toByteArray(), "UTF-8");
				// // Debug.i(TAG, "myString:" + myString);
				// Uri uri = Uri.parse(myString.split(",")[0]);
				// Intent i = new Intent(Intent.ACTION_VIEW, uri);
				// context.startActivity(i);
				//
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
				//
				dialog.dismiss();
				FileUtil.viewHtmlFlie(context, MAIL_DEMO_VIDEO);
			}
		}).setNegativeButton(context.getString(R.string.cancel_action), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		builder.create().show();
		*/
	}

}
