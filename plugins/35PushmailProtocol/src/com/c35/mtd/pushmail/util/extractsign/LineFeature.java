package com.c35.mtd.pushmail.util.extractsign;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用于获得文本行的特征，及抽取签名信息
 * 
 * @author yangxf1
 *
 */
public class LineFeature {
	
	static public class Feature{

		
		boolean emailPattern;
		boolean urlPattern;
		boolean phoneNumberPattern;
		boolean lineMark;
		boolean lotsSpecialSymbols;
		boolean typicalSignWords;
		boolean senderName;
		boolean senderEmail;
		boolean likeAName;
		boolean likeAddress;
		boolean datePattern;
		boolean typicalReplyWords;
		boolean startWithReplyMark;
		int length;
	}

	public HashMap<String, Object[]> attributes;
	
	public static String EXTRACTED_NAME = "EXTRACTED_NAME";
	public static String EXTRACTED_EMAIL = "EXTRACTED_EMAIL";
	public static String EXTRACTED_URL = "EXTRACTED_URL";
	public static String EXTRACTED_TEL = "EXTRACTED_PHONE";
	public static String EXTRACTED_FAX = "EXTRACTED_FAX";
	public static String EXTRACTED_IM = "EXTRACTED_IM";
	public static String EXTRACTED_DESIGNATION = "EXTRACTED_DESIGNATION";
	public static String EXTRACTED_COMPANY = "EXTRACTED_COMPANY";
	public static String EXTRACTED_DEPARTMENT = "EXTRACTED_DEPARTMENT";
	public static String EXTRACTED_ADDRESS = "EXTRACTED_ADDRESS";
	
	private final String chineseChar = "[\\w\\u4E00-\\u9FA5\\uF900-\\uFA2D]";
	private final String nonChineseChar = "[^\\w\\u4E00-\\u9FA5\\uF900-\\uFA2D]";
	private final String namePattern = "[A-Z][a-z]+\\s\\s?[A-Z][\\.]?\\s\\s?[A-Z][a-z]+";	
	private final String emailPattern = "([a-z0-9A-Z]+[-|\\.]?){1,3}[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.){1,3}[a-zA-Z]{2,}";
	private final String urlPattern = "(http://)?(w+(-\\w+)*)(\\.(\\w+(-\\w+)*)){2,5}([\\\\/]?[\\w\\.\\_]*)*";
	private final String phonePattern = "(((\\(\\d{3,4}\\)|\\d{3,4}-|\\s)?\\d{7})[^\\d]|\\d{10,})";
	
	private final String telAttribList = "电话号?码?|手机号?码?|telephone|phone|cell|mobile|分机号?码?|总机号?码?|座机号?码?|hotline|ext|[Ｔt][Ｅe][Ｌl]";
	private final String faxAttribList = "传真号?码?|facsimile|fax|[Ｆf][Ａa][Ｘx]";
	private final String imAttribList = "IM|ICQ|MSN|qq|ＱＱ|ＭＳＮ|ＩＣＱ|ＩＭ";
	private final String addressAttribList = "住址|地址|address|addr";
	private final String emailAttribList = "邮箱|邮件|e\\-?mail";
	private final String webAttribList = "网址|网站|网页|homepage|web|website";
	private final String miscAttribList = "部门|职位|disclaimer";
	private final String iconList = "\\*";
	private final String attribList = "(("+telAttribList+"|"+iconList+"|"+faxAttribList+"|"+imAttribList+"|"+addressAttribList+"|"+emailAttribList+"|"+webAttribList+"|"+miscAttribList+")[ |:|：])";
	
	private final String designationList = "((副|实习|见习|代理|助理)?(首席执行官|董事长|助理|总监|主任|主管|经理|副理|协理|课长|组长|部长|教授|所长|助理|顾问|专员|工程师|总裁)\\s)";
	private final String designationList_en = "(Professor|chairman|assistant|director|President|Consultant|Representative|Administrator|Executive|Coordinator|Engineer|Officer|manager)";
	private final String designationListMoreGeneral = "(官|长|理|监|任|管|员|师)"+nonChineseChar;
	private final String companyList = "(大学|公司|学校|学院|实验室|中心)";
	private final String companyList_en = "(Laboratories|Institutes?|University|Inc\\.|Co ?\\. ?,?\\s{1,3}Ltd|Corp\\.|Corporations|College|Laboratory|Center|Centre)";
	private final String locationList = "(市|区|街|路|\\d+号|镇|乡|村|州|道|大厦)";
	private final String locationList_en = "(Ave\\.|Division|Street|St\\.|Avenue|Road|Block)";
	private final String departmentList = "(部|组|处)";
	private final String departmentList1 = "(小组|部门|department)\\s*[:：]\\s*";
	private final String departmentList_en = "(department|division|Dept\\.)";
	
	private final String replyWordList = "(Time|Date|From|To|Sender|Receiver|subject|日期|时间|发件人|收件人|主题|写道|wrote)[\\s:：]"; 
	private final String datePattern = "((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))";   	

	private String senderName;
	private String senderEmail;
	

	/**
	 * 构造函数
	 * 
	 * @param senderName 邮件发送人姓名
	 * @param senderEmail 邮件发送人email
	 */
	
	public LineFeature(String senderName, String senderEmail){
		
		this.attributes = new HashMap<String, Object[]>();
		if (senderName != null && senderName.length() > 0)
		addAttributes(EXTRACTED_NAME,senderName, 10);
		addAttributes(EXTRACTED_EMAIL,senderEmail, 10);
		
		this.senderName = senderName;
		this.senderEmail = senderEmail.split("@")[0]+"@";

		
	}
	
	/**
	 * 获得签名属性值 
	 * @param attribute 签名属性名
	 * @return 签名属性值
	 */
	
	public String getAttributeValue(String attribute){
		Object[] o = attributes.get(attribute);
		if (o == null)
			return null;
		return (String)o[0];
	}
	
	private void addAttributes(String attribute, String value, int confidence){
		Object[] o = attributes.get(attribute);
		if (o == null )
			attributes.put(attribute, new Object[]{value, confidence});
		else{
			if (((Integer)o[1]) < confidence){
				o[0] = value;
				o[1] = confidence;
			}else if (((Integer)o[1]) == confidence){
				o[0] = (String)o[0]+", "+value;  
			}
		}
		
	}
	
	/**
	 * 计算文本行的特征值
	 * @param line 文本行
	 * 
	 * @return 特征对象
	 */
	public Feature calLineFeature(String line){
		
		//line = " From 0592-5391950  2009/10/25 经理    邱美丽    qiuml@china-channel.com      0592-5391808";
		line = " "+line+" ";
		Feature feature = new Feature();
		
		
		feature.emailPattern = isEmailPattern(line);
		feature.urlPattern = isURLPattern(line);
		feature.phoneNumberPattern = isPhoneNumberPattern(line);
		feature.lineMark = isLineMark(line);
		feature.lotsSpecialSymbols = isLotsSpecialSymbols(line);
		feature.typicalSignWords = isTypicalSignWords(line);
		feature.senderName = isSenderName(line, senderName);
		feature.senderEmail = isSenderEmail(line, senderEmail);
		feature.likeAName = isLikeAName(line);
		feature.likeAddress = isLikeAddress(line);
		feature.datePattern = this.isDate(line);
		feature.startWithReplyMark = isStartWithReplyMark(line);
		feature.typicalReplyWords = isTypicalReplyWords(line); 
		feature.length = validCharNum(line);
		return feature;
	}
	
	/**
	 * 抽取行文本中的签名信息
	 * 
	 * @param line 文本行
	 * 
	 * 
	 */
	public void extractAttributes(String line){
		//line = "实时招聘信息：http://www.35.com/about/jobsearch.php";
		line = " "+line+" ";
		extractName(line);
		extractTitle(line);
		extractWebUrl(line);
		extractCompany(line);
		extractDepartment(line);
		extractAddress(line);
		extractFax(line);
		extractTel(line);
		extractIM(line);
	}
	
	/**
	 * 文本行的有效字符数
	 * 
	 * @param line 文本行
	 * 
	 * @return 文本行的有效字符数
	 */
	public int validCharNum(String line){
		int count = 0;

		Pattern pattern = Pattern.compile("[\\s\\u4E00-\\u9FA5\\uF900-\\uFA2D]",Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		while (matcher.find()){
			count++;
		}
		return count;
	}

	private boolean checkMatch(String line, String match){
		Pattern pattern = Pattern.compile(match,Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		
		return matcher.find();
	}
	
	boolean isEmailPattern(String line){
		if (!line.contains("@"))
			return false;
		return checkMatch(line, emailPattern);
		
	}
	

	boolean isURLPattern(String line){
		return checkMatch(line, urlPattern);
	}


	
	boolean isPhoneNumberPattern(String line){
		return checkMatch(line, phonePattern);
	}
	

	boolean isTypicalReplyWords(String line){
		Pattern pattern = Pattern.compile(replyWordList,Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		return matcher.find();
	}

	
	boolean isStartWithReplyMark(String line){
		Pattern pattern = Pattern.compile("\\s*>",Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		return matcher.find();
	}

	boolean isDate(String line){
		Pattern pattern = Pattern.compile(datePattern,Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		return matcher.find();
	}

	
	boolean isLineMark(String line){
		Pattern pattern = Pattern.compile("^[\\s]*(\\-\\-\\-|___)+[\\s]*$",Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		return matcher.matches();
		
	}
	
	boolean isLotsSpecialSymbols(String line){
		Pattern pattern = Pattern.compile("^[\\s]*([\\*]|#|[\\+]|[\\^]|\\-|[\\~]|[\\&]|[///]|[\\$]|_|[\\!]|[\\/]|[\\%]|[\\:]|[\\=]){10,}[\\s]*$",Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		return matcher.matches();
		
	}

	boolean isTypicalSignWords(String line){
		String match = companyList+"|"+companyList_en+"|"+departmentList_en+"|"+locationList_en+"|"+designationList+"|"+designationList_en+"|"+attribList;
		Pattern pattern = Pattern.compile(match,Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		return matcher.find();
	}
	
	boolean isSenderName(String line, String senderName){
		if (senderName == null)
			return false;
		return line.replaceAll(" ", "").contains(senderName);
	}
	
	boolean isLikeAName(String line){


		Pattern pattern = Pattern.compile("^\\s+[\\u4E00-\\u9FA5\\uF900-\\uFA2D\\s]{2,4}\\s+$",Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		return matcher.matches();
	}
	
	boolean isSenderEmail(String line, String senderEmail){
		if (senderEmail == null)
			return false;
		return line.contains(senderEmail);
	}
	
	
	private void extractTel(String line){
		
		String attribName = "("+telAttribList+")\\W{1,5}";

		Pattern pattern = Pattern.compile(attribName+"([（）\\d\\s\\(\\)\\-——\\.]{1,15})", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		boolean found = false;
		while (matcher.find()){
			found = true;
			String str = matcher.group(2);
			addAttributes(EXTRACTED_TEL, str, 8);
		}
		
		if (!found){
			pattern = Pattern.compile(phonePattern, Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(line);
			found = false;
			while (matcher.find()){
				found = true;
				String str = matcher.group(2);
				addAttributes(EXTRACTED_TEL, str, 4);
			}
		}
		
		
	}
	
	private void extractFax(String line){
		String attribName = "("+faxAttribList+")\\W{1,5}";;

		
		Pattern pattern = Pattern.compile(attribName+"([\\d\\s\\(\\)\\-——]{1,15})", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);

		while (matcher.find()){

			String str = matcher.group(2);
			addAttributes(EXTRACTED_FAX, str, 8);
			
		}
		
	
		
	}
	
	private void extractIM(String line){
		String IM = "[\\W]+(("+imAttribList+") *[:：] *\\w[\\w\\.\\_@\\-]{5,})\\W";;

		
		Pattern pattern = Pattern.compile(IM, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);

		while (matcher.find()){

			String str = matcher.group(1);
			addAttributes(EXTRACTED_IM, str, 8);
			
		}
		
	
		
	}
	
	private boolean isLikeAddress(String line){
		String match = chineseChar+"{1,4}?("+locationList+")";
		Pattern pattern = Pattern.compile(match, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		int count = 0;
		
		HashSet<String> h = new HashSet<String>();
		while (matcher.find()){
			String str1 = matcher.group(1);
			if (h.contains(str1))
				return false;
			h.add(str1);
			count++;
		}
		return count >= 3;
	}

	private void extractAddress(String line){
		String attribName = "("+addressAttribList+")\\W{0,5}";
		Pattern pattern = Pattern.compile(attribName+"[:|：|\\s]+("+chineseChar+"*)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()){
			String str = matcher.group(2);
			addAttributes(EXTRACTED_ADDRESS, str, 10);
		}else{
			
			pattern = Pattern.compile(chineseChar+"{2,}("+locationList+")"+chineseChar+"{2,}", Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(line);
			if (matcher.find()){
				String str = matcher.group();
				addAttributes(EXTRACTED_ADDRESS, str, 3);
			}else{
				
				pattern = Pattern.compile("[\\w ]+"+locationList_en+"[\\w ]+", Pattern.CASE_INSENSITIVE);
				matcher = pattern.matcher(line);
				if (matcher.find()){
					String str = matcher.group();
					pattern = Pattern.compile("\\d", Pattern.CASE_INSENSITIVE);
					matcher = pattern.matcher(str);
					if (matcher.find())
						addAttributes(EXTRACTED_ADDRESS, str, 3);
				}
			}
		}
	}
	
	private void extractCompany(String line){
		
		Pattern pattern = Pattern.compile("[^:|：|\\s]+("+companyList+")\\w*", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()){
			String str = matcher.group();
			addAttributes(EXTRACTED_COMPANY, str, 10);
		}else{
			
			pattern = Pattern.compile("[\\w ]+"+companyList_en, Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(line);
			if (matcher.find()){
				String str = matcher.group();
				addAttributes(EXTRACTED_COMPANY, str, 6);
			}
		}
		
	}

	private void extractTitle(String line){
		Pattern pattern = Pattern.compile(chineseChar+"{0,5}"+designationList, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()){
			String str = matcher.group();
			addAttributes(EXTRACTED_DESIGNATION, str, 10);
		}else{
			pattern = Pattern.compile(chineseChar+"+"+designationListMoreGeneral, Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(line);
			if (matcher.find()){
				String str = matcher.group();
				addAttributes(EXTRACTED_DESIGNATION, str, 10);
			}else{
				pattern = Pattern.compile("[\\w ]+"+designationList_en, Pattern.CASE_INSENSITIVE);
				matcher = pattern.matcher(line);
				if (matcher.find()){
					String str = matcher.group();
					addAttributes(EXTRACTED_DESIGNATION, str, 10);
				}
			}
		}
			

	}

	private void extractDepartment(String line){

		Pattern pattern = Pattern.compile("\\W+"+departmentList1+"("+chineseChar+"{2,}"+")\\W", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()){
			String str = matcher.group(2);
			addAttributes(EXTRACTED_DEPARTMENT, str, 10);
		}else{
			pattern = Pattern.compile("("+chineseChar+"{2,}"+departmentList+")\\W", Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(line);
			if (matcher.find()){
				String str = matcher.group(1);
				addAttributes(EXTRACTED_DEPARTMENT, str, 10);
			}else{
				pattern = Pattern.compile("[\\w ]+"+departmentList_en, Pattern.CASE_INSENSITIVE);
				matcher = pattern.matcher(line);
				if (matcher.find()){
					String str = matcher.group();
					addAttributes(EXTRACTED_DEPARTMENT, str, 10);
				}
			}
		}
	}

	
	private void extractName(String line){
		Pattern pattern = Pattern.compile("^\\s*([\\w\\u4E00-\\u9FA5\\uF900-\\uFA2D]{2,4})[\\(（\\s]", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()){
			String str = matcher.group(1);
			addAttributes(EXTRACTED_NAME, str, 5);
		}else{
			pattern = Pattern.compile(namePattern, Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(line);
			if (matcher.find()){
				String str = matcher.group();
				addAttributes(EXTRACTED_NAME, str, 5);
			}	
		}
			
	}
	
	private void extractWebUrl(String line){
		Pattern pattern = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()){
			String str = matcher.group();
			if (!str.contains("@"))
				addAttributes(EXTRACTED_URL, str, 10);
		}
	}
}