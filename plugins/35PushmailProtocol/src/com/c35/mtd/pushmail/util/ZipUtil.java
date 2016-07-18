package com.c35.mtd.pushmail.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 
 * @Description:将一个字符串按照zip方式压缩和解压缩
 * @author:gongfacun
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class ZipUtil {

	/**
	 * 压缩
	 * @Description:
	 * @param src
	 * @return
	 * @throws IOException
	 * @see: 
	 * @since: 
	 * @author: cuiwei
	 * @date:2013-5-15
	 */
	public static byte[] compress(byte[] src) throws IOException {
		if (src == null || src.length == 0) {
			return src;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(src);
		gzip.close();
		return out.toByteArray();
		// return Base64.encodeBase64(src);
	}

	/**
	 * 解压缩
	 * @Description:
	 * @param src
	 * @return
	 * @throws IOException
	 * @see: 
	 * @since: 
	 * @author: cuiwei
	 * @date:2013-5-15
	 */
	public static byte[] uncompress(byte[] src) throws IOException {
		
		if (src == null || src.length == 0) {
			return src;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		ByteArrayInputStream in = new ByteArrayInputStream(src);

		GZIPInputStream gunzip = new GZIPInputStream(in);

		byte[] buffer = new byte[256];
		int n;
		while ((n = gunzip.read(buffer)) != -1) {
			out.write(buffer, 0, n);
		}
		byte[] aa = null;
		
		try {

			gunzip.close();
			src = null;
			
			in.close();
			aa = out.toByteArray();
			
			out.close();
		} catch (Exception e) {
			System.out.println("gggggggggg" + e.getMessage());
		}catch (Error e) {
			System.out.println(" uncompress Error " + e.toString());
		}
		
//		int nnumber;
//		MultiMemberGZIPInputStream MmGz = new MultiMemberGZIPInputStream(in);
//		byte[] buf = new byte[256];
//		nnumber = MmGz.read(buf, 0, buf.length);
//		while (nnumber != -1) {
//			out.write(buf, 0, nnumber);
//			nnumber = MmGz.read(buf, 0, buf.length);
//		}
//		MmGz.close();
		
		return aa;
		// return Base64.decodeBase64(src);
	}
}