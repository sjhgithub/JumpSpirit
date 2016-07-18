package cn.mailchat.utils;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.mailchat.helper.StringUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;



/**
 * BASE64编码解码工具包
 * </p>
 * <p>
 * 依赖javabase64-1.3.1.jar
 * </p>
 * @Description:
 * @author:Zhonggaoyong
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2014-1-2
 */
public class Base64Utils {

    /**
     * 文件读取缓冲区大小
     */
    private static final int CACHE_SIZE = 1024;
    
    /**
     * <p>
     * BASE64字符串解码为二进制数据
     * </p>
     * 
     * @param base64
     * @return
     * @throws Exception
     */
    public static byte[] decode(String base64) throws Exception {
        return Base64.decode(base64.getBytes(),Base64.DEFAULT);
    }
    
    /**
     * <p>
     * 二进制数据编码为BASE64字符串
     * </p>
     * 
     * @param bytes
     * @return
     * @throws Exception
     */
    public static String encode(byte[] bytes) throws Exception {
        return new String(Base64.encode(bytes,Base64.DEFAULT));
    }
    
    /**
     * <p>
     * 将文件编码为BASE64字符串
     * </p>
     * <p>
     * 大文件慎用，可能会导致内存溢出
     * </p>
     * 
     * @param filePath 文件绝对路径
     * @return
     * @throws Exception
     */
    public static String encodeFile(String filePath) throws Exception {
        byte[] bytes = fileToByte(filePath);
        return encode(bytes);
    }
    
    /**
     * <p>
     * BASE64字符串转回文件
     * </p>
     * 
     * @param filePath 文件绝对路径
     * @param base64 编码字符串
     * @throws Exception
     */
    public static void decodeToFile(String filePath, String base64) throws Exception {
        byte[] bytes = decode(base64);
        byteArrayToFile(bytes, filePath);
    }
    
    /**
     * <p>
     * 文件转换为二进制数组
     * </p>
     * 
     * @param filePath 文件路径
     * @return
     * @throws Exception
     */
    public static byte[] fileToByte(String filePath) throws Exception {
        byte[] data = new byte[0];
        File file = new File(filePath);
        if (file.exists()) {
            FileInputStream in = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
            byte[] cache = new byte[CACHE_SIZE];
            int nRead = 0;
            while ((nRead = in.read(cache)) != -1) {
                out.write(cache, 0, nRead);
                out.flush();
            }
            out.close();
            in.close();
            data = out.toByteArray();
         }
        return data;
    }
    
    /**
     * <p>
     * 二进制数据写文件
     * </p>
     * 
     * @param bytes 二进制数据
     * @param filePath 文件生成目录
     */
    public static void byteArrayToFile(byte[] bytes, String filePath) throws Exception {
        InputStream in = new ByteArrayInputStream(bytes);   
        File destFile = new File(filePath);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        destFile.createNewFile();
        OutputStream out = new FileOutputStream(destFile);
        byte[] cache = new byte[CACHE_SIZE];
        int nRead = 0;
        while ((nRead = in.read(cache)) != -1) {   
            out.write(cache, 0, nRead);
            out.flush();
        }
        out.close();
        in.close();
    }

	/**
	 * 根据路径获得图片并压缩，返回bitmap用于显示
	 * 
	 * @author liwent
	 * @date 2014年8月4日
	 * @param filePath
	 * @return Bitmap图片
	 */
	private static Bitmap getSmallBitmap(String filePath) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, 480, 800);
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filePath, options);
	}

	/**
	 * 计算图片大小
	 * 
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return 图片大小
	 */
	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

	/**
	 * 将图片转换为Base64字节码
	 * 
	 * @author liwent
	 * @date 2014年8月4日
	 * @param filePath
	 * @return 图片Base64字节码
	 */
	public static String imageToBase64(String filePath) {
		Bitmap bitmap = getSmallBitmap(filePath);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
		byte[] b = baos.toByteArray();
		return Base64.encodeToString(b, Base64.DEFAULT);

	}

	/**
	 * 将Base64字节码转换为bitmap图片
	 * 
	 * @author liwent
	 * @date 2014年8月4日
	 * @param imgBase64
	 * @return Bitmap
	 */
	public static Bitmap base64ToImage(String imgBase64) {
		try {
			byte[] bitmapArray;
			bitmapArray = Base64.decode(imgBase64, Base64.DEFAULT);
			return BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	 /**
     * 
     * @param base64Data
     * @param imgName
     * @param imgFormat 图片格式
     */
    public static void base64ToBitmap(String base64Data,String imgName,String imgPath) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
 
        File myCaptureFile = new File(imgPath, imgName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myCaptureFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        boolean isTu = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        if (isTu) {
            // fos.notifyAll();
            try {
                fos.flush();
                fos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                fos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
	// 将 s 进行 BASE64 编码
	public static String getBASE64(String s) {
		return new String(Base64.encode(s.getBytes(), Base64.DEFAULT));
	}

	// 将 BASE64 编码的字符串 s 进行解码
	public static String getFromBASE64(String s) {
		byte[] result = Base64.decode(s, Base64.DEFAULT);
		return new String(result);
	}
}