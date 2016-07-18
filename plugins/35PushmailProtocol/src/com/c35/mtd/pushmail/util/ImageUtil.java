package com.c35.mtd.pushmail.util;

import java.io.File;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.BitmapFactory.Options;
import android.text.TextUtils;

/**
 * 
 * @Description:图片处理
 * @author:温楠  
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2012-11-16
 */
public class ImageUtil {
	private interface DecodeImage {
		Bitmap decodeImage(Options opts);
	}

	

	/**
	 * @param imagePath
	 * @param perferWidth
	 *            建议的宽
	 * @param preferHeight
	 *            建议的高
	 * @return
	 */
	public static Bitmap loadImage(final String imagePath, int preferWidth, int preferHeight) {
		if (TextUtils.isEmpty(imagePath)) {
			return null;
		}
		File file = new File(imagePath);
		if (!file.exists() || !file.isFile()) {
			return null;
		}

		DecodeImage decoder = new DecodeImage() {

			@Override
			public Bitmap decodeImage(Options opt) {
				return BitmapFactory.decodeFile(imagePath, opt);
			}
		};
		return loadImage(decoder, preferWidth, preferHeight);
	}
	

	private static Bitmap loadImage(DecodeImage decoder, int perferWidth, int preferHeight) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		decoder.decodeImage(opts);
		int realHeight = opts.outHeight;
		int realWidth = opts.outWidth;
		
		opts.inSampleSize = Math.max(realHeight / preferHeight, realWidth / perferWidth);
		if (opts.inSampleSize < 1) {
			opts.inSampleSize = 1;
		}
		opts.inJustDecodeBounds = false;
		return decoder.decodeImage(opts);
	}
	
	
	/**
	 * 图片压缩工具类
	 * 
	 * @param bitmap
	 * @param Width
	 *            新图像尺寸
	 * @param Height
	 * @return
	 */
	public static Bitmap smallImage(Bitmap bitmap, int Width, int Height) {
		int oldWidth = bitmap.getWidth();
		int oldHeight = bitmap.getHeight();

		float scaleWidth = ((float) Width) / oldWidth;
		float scaleHeight = ((float) Height) / oldHeight;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);

		return Bitmap.createBitmap(bitmap, 0, 0, oldWidth, oldHeight, matrix, true);

	}

	/*
	 * @param oldPath 原本图片的路径
	 * 
	 * @param newPath 压缩后图片的路径
	 * 
	 * @param size 压缩比例
	 */
	public static File smallPic(String oldPath, String newPath, int size) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		Bitmap resizeBmp;
		opts.inSampleSize = size;
		opts.inJustDecodeBounds = false;
		resizeBmp = BitmapFactory.decodeFile(oldPath, opts);
		File pictureFile = new File(newPath);
		try {
			if (pictureFile.exists()) {
				pictureFile.delete();
			}
			pictureFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(pictureFile);
			resizeBmp.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
			fOut.flush();
			fOut.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (resizeBmp.isRecycled() == false) {
				resizeBmp.recycle();
				System.gc();
			}
		}
		return pictureFile;
	}

	

}
