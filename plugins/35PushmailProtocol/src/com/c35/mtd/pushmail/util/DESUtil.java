/* 
 * Copyright (C), 2004-2010, 三五互联科技股份有限公司
 * Encoding UTF-8 
 * Version: 1.0 
 * Date: 2011-9-5
 * History:
 * 1. Date: 2011-9-5
 *    Author: guojl
 *    Modification: 新建
 * 2. ...
 */
package com.c35.mtd.pushmail.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import com.c35.mtd.pushmail.Debug;

/**
 * DES加密解密相关工具类
 * 
 * @author guojl
 * @version Revision: 0.01 Date: 2011-5-5 下午04:23:02
 */
public class DESUtil {

	/**
	 * 
	 * @description 加密函数(Cipher对象使用默认随机向量，ECB模式)
	 * @param data
	 *            待加密数据
	 * @param key
	 *            密钥
	 * @return
	 * @author guojl
	 */
	public static byte[] encrypt(byte[] data, byte[] key) {
		try {
			SecureRandom sr = new SecureRandom(); // 可信任的随机数源
			DESKeySpec dks = new DESKeySpec(key); // 从原始密钥数据创建DESKeySpec对象

			// 创建密匙工厂，把DESKeySpec转换成一个SecretKey对象
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey secretKey = keyFactory.generateSecret(dks);
			// 使用ECB模式
			Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
			// 用密匙初始化Cipher对象
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, sr);
			// 执行加密操作
			byte encryptedData[] = cipher.doFinal(data);
			return encryptedData;
		} catch (Exception e) {
			// ("DES算法，加密数据出错!");
			e.printStackTrace();
			Debug.e("failfast", "failfast_AA", e);
		}
		return null;
	}

}
