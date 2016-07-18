package cn.mailchat.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import cn.mailchat.R;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

public class ImageUtils {
	private static Map<String, Integer> colorMap = new HashMap<String, Integer>();
	public final static String SDCARD_MNT = "/mnt/sdcard";
	public final static String SDCARD = "/sdcard";

	public static boolean isEnglish(String charaString) {
		return charaString.matches("^[a-zA-Z]*");
	}

	/**
	 * 直角转圆角矩形
	 * 
	 * @Description:
	 * @param x
	 *            图像的宽度
	 * @param y
	 *            图像的高度
	 * @param image
	 * @param outerRadiusRat
	 *            圆角的大小
	 * @return
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2014-2-17
	 */
	public static Bitmap createFramedPhoto(int width, int height, Bitmap image,
			float outerRadiusRat) {
		// 根据源文件新建一个darwable对象
		Drawable imageDrawable = new BitmapDrawable(image);

		// 新建一个新的输出图片
		Bitmap output = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		// 新建一个矩形
		RectF outerRect = new RectF(0, 0, width, height);

		// 产生一个圆角矩形
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		canvas.drawRoundRect(outerRect, outerRadiusRat, outerRadiusRat, paint);

		// 将源图片绘制到这个圆角矩形上
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		imageDrawable.setBounds(0, 0, width, height);
		canvas.saveLayer(outerRect, paint, Canvas.ALL_SAVE_FLAG);
		imageDrawable.draw(canvas);
		canvas.restore();

		return output;
	}

	/**
	 * 绘制有文字的圆角矩形
	 * 
	 * @Description:
	 * @param text
	 * @return
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2014-2-17
	 */
	public static Bitmap createTextBitmap(Context context, int x, int y,
			String text, float textSize) {
		Bitmap bitmap = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		if (isEnglish(text)) {
			text = text.toUpperCase();
		}
		// 新建一个矩形
		RectF outerRect = new RectF(0, 0, x, y);
		// 产生一个圆角矩形
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		int color = 0;
		if (colorMap.containsKey(text)) {
			color = colorMap.get(text);
		} else {
			// 随机颜色
			Random random = new Random();
			color = random.nextInt(6);
			colorMap.put(text, color);
		}

		switch (color) {
		case 0:
			paint.setColor(context.getResources().getColor(
					R.color.group_icon_color_01));
			break;
		case 1:
			paint.setColor(context.getResources().getColor(
					R.color.group_icon_color_02));
			break;
		case 2:
			paint.setColor(context.getResources().getColor(
					R.color.group_icon_color_03));
			break;
		case 3:
			paint.setColor(context.getResources().getColor(
					R.color.group_icon_color_04));
			break;
		case 4:
			paint.setColor(context.getResources().getColor(
					R.color.group_icon_color_05));
			break;
		case 5:
			paint.setColor(context.getResources().getColor(
					R.color.group_icon_color_06));
			break;
		}

		canvas.drawRoundRect(outerRect, 0, 0, paint);

		// 写字
		paint.setTextSize(textSize);
		paint.setColor(Color.WHITE);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setTypeface(Typeface.DEFAULT);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

		FontMetricsInt fontMetrics = paint.getFontMetricsInt();
		float baseline = outerRect.top
				+ (outerRect.bottom - outerRect.top - fontMetrics.bottom + fontMetrics.top)
				/ 2 - fontMetrics.top;
		paint.setTextAlign(Paint.Align.CENTER);

		canvas.drawText(text, outerRect.centerX(), baseline, paint);

		canvas.saveLayer(outerRect, paint, Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return bitmap;
	}

	/**
	 * 
	 * method name: getImgHead function @Description: TODO Parameters and return
	 * values description：
	 * 
	 * @param userName
	 * @return field_name Bitmap return type
	 * @History memory：
	 * @Date：2014-8-11 上午10:51:04 @Modified by：zhangjx
	 * @Description：获取网络头像，如果没有的话以用户名首字母生成图片
	 */
	public static Bitmap getUserFirstTextBitmap(Context context, String userName) {
		Bitmap imgHead = null;
		if (imgHead == null&&userName!=null) {
			if (imgHead == null) {
				imgHead = createTextBitmap(context,
						GlobalTools.dip2px(context, 36),
						GlobalTools.dip2px(context, 36),
						userName.substring(0, 1),
						GlobalTools.sp2px(context, 24f));
			}

		}

		return imgHead;
	}
	/**
	 * 读取本地图片
	 * 
	 * @Description:
	 * @param pathString
	 * @return
	 * @see:
	 * @since:
	 * @author: xuqq
	 * @date:2014-2-26
	 */
	public static Bitmap getLocalBitmap(String pathString) {
		Bitmap bitmap = null;
		try {
			File file = new File(pathString);
			if (file.exists()) {
				bitmap = BitmapFactory.decodeFile(pathString);
			}
		} catch (Exception e) {
		}

		return bitmap;
	}
	public static Bitmap getBitmapFromResources(Context context, int resId) {
		Resources res = context.getResources();
		return BitmapFactory.decodeResource(res, resId);
	}
	/**
	 * 读取本地图片并将超过尺寸的大图片压缩
	 * 
	 * @Description:
	 * @param pathString
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-19
	 */
	public static Bitmap getNativeImage(String imagePath,boolean isCheckRotate) {
		Bitmap myBitmap=null;
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			options.inPurgeable = true;
			options.inInputShareable = true;
			options.inSampleSize = 1;
			options.inJustDecodeBounds = true;
			// 获取这个图片的宽和高
			myBitmap = BitmapFactory.decodeFile(imagePath, options); // 此时返回myBitmap为空

			int w = options.outWidth;
			int h = options.outHeight;
			// 现在主流手机比较多是960*540分辨率，所以高和宽我们设置为
			float hh = 960f;// 这里设置高度为960f
			float ww = 540f;// 这里设置宽度为540f
			// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
			int be = 1;// be=1表示不缩放
			if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
				be = (int) (w / ww);
			} else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
				be = (int) (h / hh);
			}
			if (be <= 0)
				be = 1;
			options.inSampleSize = be;
			// 重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false
			options.inJustDecodeBounds = false;
			myBitmap = BitmapFactory.decodeFile(imagePath, options);
			if (isCheckRotate) {
				ExifInterface exifInterface = new ExifInterface(imagePath);
				int result = exifInterface.getAttributeInt(
						ExifInterface.TAG_ORIENTATION,
						ExifInterface.ORIENTATION_UNDEFINED);
				int rotate = 0;
				switch (result) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					rotate = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					rotate = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					rotate = 270;
					break;
				default:
					break;
				}
				if (rotate > 0) {
					Matrix matrix = new Matrix();
					matrix.setRotate(rotate);
					Bitmap rotateBitmap = Bitmap.createBitmap(myBitmap, 0, 0,
							options.outWidth, options.outHeight, matrix, true);
					if (rotateBitmap != null) {
						myBitmap.recycle();
						myBitmap = rotateBitmap;
					}
				}
			}
		} catch (OutOfMemoryError e) {
			// TODO: handle exception
			myBitmap=null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return myBitmap;
	}
	/**
	 * 大图片压缩
	 * 
	 * @Description:
	 * @param Bitmap
	 * @param File 压缩后存储路径
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2014-12-29
	 */
	public static void compressBmpToFile(Bitmap bmp,File file){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int options = 100;
		bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
		while (baos.toByteArray().length / 1000 >1024&&!(options<=10)) { 
			baos.reset();
			options -= 10;
			bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
		}
//		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
//		BitmapFactory.Options newOpts = new BitmapFactory.Options();
//		//开始读入图片，此时把options.inJustDecodeBounds 设回true了
//		newOpts.inJustDecodeBounds = true;
//		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
//		newOpts.inJustDecodeBounds = false;
//		int w = newOpts.outWidth;
//		int h = newOpts.outHeight;
//		float hh = 960f;
//		float ww = 540f;
//		int be = 1;//be=1表示不缩放
//		if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
//			be = (int) (newOpts.outWidth / ww);
//		} else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
//			be = (int) (newOpts.outHeight / hh);
//		}
//		if (be <= 0)
//			be = 1;
//		newOpts.inSampleSize = be;//设置缩放比例
//		//重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
//	    isBm = new ByteArrayInputStream(baos.toByteArray());  
//	    bitmap = BitmapFactory.decodeStream(isBm, null, newOpts); 
//	    ByteArrayOutputStream mybaos = new ByteArrayOutputStream();
//		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, mybaos);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baos.toByteArray());
			fos.flush();
			fos.close();
			baos.close();
//			mybaos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 判断当前Url是否标准的content://样式，如果不是，则返回绝对路径
	 * 
	 * @param uri
	 * @return
	 */
	public static String getAbsolutePathFromNoStandardUri(Uri mUri) {
		String filePath = null;

		String mUriString = mUri.toString();
		mUriString = Uri.decode(mUriString);

		String pre1 = "file://" + SDCARD + File.separator;
		String pre2 = "file://" + SDCARD_MNT + File.separator;

		if (mUriString.startsWith(pre1)) {
			filePath = Environment.getExternalStorageDirectory().getPath()
					+ File.separator + mUriString.substring(pre1.length());
		} else if (mUriString.startsWith(pre2)) {
			filePath = Environment.getExternalStorageDirectory().getPath()
					+ File.separator + mUriString.substring(pre2.length());
		}
		return filePath;
	}

	/**
	 * 通过uri获取文件的绝对路径
	 * 
	 * @param uri
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String getAbsoluteImagePath(Activity context, Uri uri) {
		String imagePath = "";
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = context.managedQuery(uri, proj, // Which columns to
														// return
				null, // WHERE clause; which rows to return (all rows)
				null, // WHERE clause selection arguments (none)
				null); // Order-by clause (ascending by name)

		if (cursor != null) {
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			if (cursor.getCount() > 0 && cursor.moveToFirst()) {
				imagePath = cursor.getString(column_index);
			}
		}

		return imagePath;
	}
	
	/**
	 * 读取本地图片的尺寸
	 * 
	 * @Description:
	 * @param pathString
	 * @return
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2016-2-25
	 */
	public static int[] getNativeImageSize(String imagePath) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inSampleSize = 1;
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高
		BitmapFactory.decodeFile(imagePath, options); // 此时返回myBitmap为空
		return new int []{options.outWidth,options.outHeight};
	}
}
