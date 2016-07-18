package cn.mailchat.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.ParsingDetector;
import info.monitorenter.cpdetector.io.UnicodeDetector;


public class ParseEncodeUtil {
	/**
	 * 根据字节获取其对应字符的编码
	 * 注：不是百分百准确,根据统计学获取的.能判断出大部分编码
	 * @Description:
	 * @param email
	 * @see:
	 * @since:
	 * @author: shengli
	 * @date:2015-3-12
	 */
	public static String getbyteEncode(byte[] b) {
		CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();

		Charset charset = null;
		synchronized(detector) {
		    detector.add(new ParsingDetector(false));
		    /*
		     * JChardetFacade封装了由Mozilla组织提供的JChardet，它可以完成大多数文件的编码
		     * 测定。所以，一般有了这个探测器就可满足大多数项目的要求，如果你还不放心，可以
		     * 再多加几个探测器，比如下面的ASCIIDetector、UnicodeDetector等。
		     */
		    detector.add(JChardetFacade.getInstance());// 用到antlr.jar、chardet.jar
		    // ASCIIDetector用于ASCII编码测定
		    detector.add(ASCIIDetector.getInstance());
		    // UnicodeDetector用于Unicode家族编码的测定
		    detector.add(UnicodeDetector.getInstance());

		    try {
		        charset = detector.detectCodepage(new ByteArrayInputStream(b),
		                b.length);
		    } catch (IllegalArgumentException | IOException e) {
		        e.printStackTrace();
		    }
		}
		if (charset != null) {
			return charset.name();
		} else {
			return null;
		}
	}
}
