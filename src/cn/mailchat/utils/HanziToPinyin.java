package cn.mailchat.utils;

import java.text.Collator;
import java.util.Locale;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;


/**
 * 汉字转换成拼音<br/>
 * 在pinyin4j-2.5.0.jar的pinyindb文件夹下有个unicode_to_hanyu_pinyin.txt文件，
 * 如以后有拼音不正确可以修改该文件。
 * @Description:
 * @author: huangyongxing
 * @see:   
 * @since:      
 * @copyright © 35.com
 * @Date:2012-11-6
 */
public class HanziToPinyin {

    // 已知的第一个汉字拼音
    private static final String FIRST_PINYIN_UNIHAN = "\u963F";
    // 已知的最后一个汉字拼音
    private static final String LAST_PINYIN_UNIHAN = "\u84D9";
    // 第一个汉字Unicode
    private static final char FIRST_UNIHAN = '\u3400';
    private static final Collator COLLATOR = Collator.getInstance(Locale.CHINA);
    
	private static HanyuPinyinOutputFormat format = null;

    static{
        format = new HanyuPinyinOutputFormat();
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        // 转换成大写
  		format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
  		// 'ü' -> "v"
  		format.setVCharType(HanyuPinyinVCharType.WITH_V);
    }

    

    /**
     * 单个字节转换成拼音
     * @Description: 非汉字和未知字符（比如中文标点）会输出null，比如A转换后输了null
     * @param c
     * @return
     * @throws BadHanyuPinyinOutputFormatCombination
     * @see: 
     * @since: 
     * @author: huangyongxing
     * @date:2012-11-6
     */
    public static String getCharacterPinYin(char c) throws BadHanyuPinyinOutputFormatCombination {
    	if (c < 256) {
    		return null;
    	}
    	boolean unknown = false;
    	if (c < FIRST_UNIHAN) {
    		unknown = true;
        } else {
            int cmp = COLLATOR.compare(Character.toString(c), FIRST_PINYIN_UNIHAN);
            if (cmp < 0) {
                unknown = true;
            } else if(cmp > 0) {
                cmp = COLLATOR.compare(Character.toString(c), LAST_PINYIN_UNIHAN);
                if (cmp > 0) {
                    unknown = true;
                }
            }
        }
    	if(unknown){
    		return Character.toString(c);
    	}
    	String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(c, format);
    	// 如果c不是汉字，toHanyuPinyinStringArray会返回null
    	if(pinyin == null) return null;
    	// 只取一个发音，如果是多音字，仅取第一个发音
    	return pinyin[0];   
    }

    

    /**
     * 字符串转换成拼音
     * @Description:
     * @param str
     * @return
     * @see: 
     * @since: 
     * @author: huangyongxing
     * @date:2012-11-6
     */
    public static String toPinyin(String str) {
    	StringBuilder sb = new StringBuilder();
        String tempPinyin = null;
    	try {
	        for(int i = 0; i < str.length(); ++i) {
				tempPinyin = getCharacterPinYin(str.charAt(i));
	        	if(tempPinyin == null){
	        		// 如果str.charAt(i)非汉字，则保持原样
	        		sb.append(Character.toString(str.charAt(i)).toUpperCase());
	        	}else{
	        		int len = sb.length();
	        		if(len > 0 && sb.charAt(len - 1) != ' '){
	        			sb.append(" ");
	        		}
	        		sb.append(tempPinyin).append(" ");
	        	}
	        }
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		}
		int lastLen = sb.length();
		if(lastLen > 0 && sb.charAt(lastLen - 1) == ' '){
			sb.deleteCharAt(lastLen - 1);
		}
        return sb.toString();
    }

}
