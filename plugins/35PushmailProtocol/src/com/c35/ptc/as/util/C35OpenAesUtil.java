package com.c35.ptc.as.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.c35.mtd.pushmail.Debug;

import android.util.Base64;

/**
 * 
 * @Description:Aes加密解密工具
 * @author:hanchunxue  
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2012-12-11
 */
public class C35OpenAesUtil {

	// 用于随机生成字符串的密匙
	public static final String LOCALKEYCODE = "%>NCTPmLD)f9a3F$a,>S8QBRgNdx{PtI0J%o_q&+B7v#|6]yr0Q}OD2r4513+hu<Fy733oa_Aq&gdtl)hC,0|v4zY<S^<B1.wV+zqA+>CAZ54O@c*h|7H9pT`3?l,=%_";

	/**
	 * 数据加密
	 * 
	 * @param text
	 *            要加密的数据
	 * @return 加密后文本内容，通过base64编码
	 */
	public static String encrypt(String text) {
		if (text == null || "".equals(text.trim())) {
			return text;
		}
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			//cuiwei added for android4.2 etc. start
	        SecureRandom sr = null;  
	        if (android.os.Build.VERSION.SDK_INT >= 17) {  
	            sr = SecureRandom.getInstance("SHA1PRNG","Crypto");  
	        }  
	        else{  
	            sr = SecureRandom.getInstance("SHA1PRNG");  
	        }  
	        sr.setSeed(LOCALKEYCODE.getBytes());   
	        //kgen.init(128, new SecureRandom(LOCALKEYCODE.getBytes()));
			kgen.init(128, sr);
			//cuiwei added for android4.2 etc.  end
			SecretKey secretKey = kgen.generateKey();
			byte[] key = secretKey.getEncoded();
			SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
			// Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// 创建密码器
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			byte[] byteContent = text.getBytes("utf-8");
			// 创建向量
			// / IvParameterSpec ips = new IvParameterSpec("www.35.cn/ippush".getBytes());
			cipher.init(Cipher.ENCRYPT_MODE, keySpec);// 初始化
			byte[] result = cipher.doFinal(byteContent); // 加密
			return Base64.encodeToString(result, Base64.DEFAULT);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Debug.e("failfast", "failfast_AA", e);
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			Debug.e("failfast", "failfast_AA", e);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Debug.e("failfast", "failfast_AA", e);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			Debug.e("failfast", "failfast_AA", e);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			Debug.e("failfast", "failfast_AA", e);
		} catch (BadPaddingException e) {
			e.printStackTrace();
			Debug.e("failfast", "failfast_AA", e);
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 数据解密
	 * 
	 * @param text
	 *            要解密的数据
	 * @return 解密后文本内容
	 */
	public static String decrypt(String text) {
		if (text == null || "".equals(text.trim())) {
			return text;
		}
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			//cuiwei added for android4.2 etc. start
	        SecureRandom sr = null;  
	        if (android.os.Build.VERSION.SDK_INT >= 17) {  
	            sr = SecureRandom.getInstance("SHA1PRNG","Crypto");  
	        }  
	        else{  
	            sr = SecureRandom.getInstance("SHA1PRNG");  
	        }  
	        sr.setSeed(LOCALKEYCODE.getBytes());   
	        //kgen.init(128, new SecureRandom(LOCALKEYCODE.getBytes()));
			kgen.init(128, sr);
			//cuiwei added for android4.2 etc.  end
			SecretKey secretKey = kgen.generateKey();
			byte[] key = secretKey.getEncoded();
			SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
			// Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// 创建密码器
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			// IvParameterSpec ips = new IvParameterSpec("www.35.cn/ippush".getBytes());
			cipher.init(Cipher.DECRYPT_MODE, keySpec);// 初始化

			byte[] btext = Base64.decode(text, Base64.DEFAULT);
			byte[] result = cipher.doFinal(btext); // 解密
			return new String(result, "utf-8");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Debug.e("failfast", "failfast_AA", e);
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			Debug.e("failfast", "failfast_AA", e);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			Debug.e("failfast", "failfast_AA", e);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			Debug.e("failfast", "failfast_AA", e);
		} catch (BadPaddingException e) {
			e.printStackTrace();
			Debug.e("failfast", "failfast_AA", e);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Debug.e("failfast", "failfast_AA", e);
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
