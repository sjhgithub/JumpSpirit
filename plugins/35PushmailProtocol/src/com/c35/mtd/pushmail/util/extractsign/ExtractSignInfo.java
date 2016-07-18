package com.c35.mtd.pushmail.util.extractsign;


import java.util.ArrayList;

import com.c35.mtd.pushmail.util.extractsign.ContactInfoModel;

/**
 * 
 * 抽取邮件签名信息
 * 
 * @author yangxf1 
 * 
 *
 */
public class ExtractSignInfo {
	
	private Judge judge;
	
	/**
	 * 构造函数
	 * @param judge 分类器对象，用于进行判断一文本行是否在签名区内 
	 */
	public ExtractSignInfo(Judge judge){
		this.judge = judge;
	}
	
	/**
	 * 构造函数
	 *  
	 */
	public ExtractSignInfo(){
		
	}

	/**
	 * 设置分类器对象
	 * @param judge 分类器对象，用于进行判断一文本行是否在签名区内 
	 */

	public void setJudge(Judge  j){
		judge = j;
	}
	
	protected String postprocess(String str){
		if (str == null)
			return str;
		return str.replaceAll("\\(|\\)|（|）", " ").replaceAll(" +", " ");
	}
	
	protected ContactInfoModel extractFromResult(LineFeature cal){
	    ContactInfoModel s = new ContactInfoModel();
	    s.setName(cal.getAttributeValue(LineFeature.EXTRACTED_NAME));
	    s.setEmail(cal.getAttributeValue(LineFeature.EXTRACTED_EMAIL));
		s.setCompanyName(postprocess(cal.getAttributeValue(LineFeature.EXTRACTED_COMPANY)));
		s.setBranchName(postprocess(cal.getAttributeValue(LineFeature.EXTRACTED_DEPARTMENT)));
		s.setPostName(postprocess(cal.getAttributeValue(LineFeature.EXTRACTED_DESIGNATION)));
		s.setCompanyTel(postprocess(cal.getAttributeValue(LineFeature.EXTRACTED_TEL)));
        s.setHomeAddress(postprocess(cal.getAttributeValue(LineFeature.EXTRACTED_ADDRESS)));
		return s;
		
	}
	
	/**
	 * 从邮件文本中抽取签名信息
	 * @param mailText 邮件正文对象 
	 * @param senderName 邮件发件人姓名
	 * @param senderEmail 邮件发件人Email
	 */
	
	public ContactInfoModel extract(String mailText, String senderName, String senderEmail){
		

		LineFeature cal = new LineFeature(senderName, senderEmail);
		
		String[] lines = mailText.replaceAll("\\s+\n+","\n").split("\n");
		LineFeature.Feature[] features = new LineFeature.Feature[lines.length];  
		ArrayList<Integer> scores = new ArrayList<Integer>(lines.length);
		
		int i = 0;
		for (i = 0; i < lines.length && i < 5; i++){
			//if (cal.validCharNum(lines[i]) < 30)
				features[i] = cal.calLineFeature(lines[i]);
		}


		i = 0;
		while (i  < lines.length){
	
		
			int score = judge.classify(features, i); 
			scores.add(score);
			boolean isCurLineSignRegion = judge.isScoreAboveThreshold(score); 

	
			if (!isCurLineSignRegion && isReplyRegion(features,i))
				break;
		
			if (i + 5 < lines.length)
				//if (cal.validCharNum(lines[i+5]) < 30)
					features[i+5] = cal.calLineFeature(lines[i+5]);
			i++;
			
		}

		int bestIdx = decideBestSignLine(scores);
		
		if (bestIdx >= 0){
			int start = decideSignStartLine(features, bestIdx);
			int end = decideSignEndLine(features, bestIdx, scores.size());
			
			for (i = start ; i < end; i++)
				cal.extractAttributes(lines[i]);
		}
		
		return extractFromResult(cal);
	}
	
	/*
	 * 从邮件对象中抽取签名信息
	 * @param mail 邮件对象 
	 
	/*
	public SignInfo extract(Mail mail){
		
		return extract(mail.body, mail.senderName, mail.senderEmail);
	}
	*/
	
	
	int decideSignStartLine(LineFeature.Feature[] features, int bestLine){
		
		int i = bestLine;
		int notSignLineInRow = 0;
		while (bestLine - i < 10 && i >= 0 && notSignLineInRow < 2){
			if (judge.isSingleLineSign(features, i))
				notSignLineInRow = 0;
			else
				notSignLineInRow++;
			i--;
		}
		return i+1+notSignLineInRow;
	}

	
	int decideSignEndLine(LineFeature.Feature[] features, int bestLine, int maxLine){
		
		int i = bestLine;
		int notSignLineInRow = 0;
		while (i - bestLine < 5 && i < maxLine && notSignLineInRow < 2){
			if (judge.isSingleLineSign(features, i)){
				notSignLineInRow = 0;
			}else
				notSignLineInRow++;
			i++;
		}
		return i-1-notSignLineInRow;
	}

	int decideBestSignLine(ArrayList<Integer> scores){
		int bestScore = 0;
		int size = scores.size();
		int bestIdx = -1;
		
		for (int i = size - 1; i >= 0; i--){
			int score = 0;
			int start = i - 2;
			if (start < 0) 
				start = 0;
			int end = i + 2;
			if (end >= size)
				end = bestIdx - 1;
			for (int j = start; j <= end; j++)
				score += scores.get(j);
			if (score > bestScore){
				bestScore = score;
				bestIdx = i;
			}
		}
		
		return bestIdx;
	}
	
	protected boolean isReplyRegion(LineFeature.Feature[] features, int num){
		int score = 0;
		
		if (features[num] == null)
			return false;
		
		if (num >= 1){
			score += calFeatureScore4Reply(features[num-1]);
		}else
			score -= 5;
		
		score += calFeatureScore4Reply(features[num]);
		if (num < features.length - 1){
			score += calFeatureScore4Reply(features[num+1]);
		}
		
		if (num < features.length - 2){
			score += calFeatureScore4Reply(features[num+2]);
		}
		
		return (score >= 20);
			
	}
	
	private int calFeatureScore4Reply(LineFeature.Feature feature){
		int sum = 0;
		
		if (feature == null)
			return 0;
		
		if (feature.datePattern)
			sum += 5;

		if (feature.typicalReplyWords)
			sum += 10;
		
		if (feature.startWithReplyMark)
			sum += 5;
		
		return sum;
		

	}
	
 	/**
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) throws Exception {
 		//ReadMailText readMail = new ReadMailText();
 	    //readMail.loadMails("mail.txt");
 	    //Judge j = new JudgeByC5(); 
 	    //ExtractSignInfo o = new ExtractSignInfo (j);
 	    //for (int i = 0; i <= 100; i++){
		//	System.out.println(i);
		//	
 	    	//System.out.println(readMail.getMailObjList().get(i).body);
		//	System.out.println(o.extract(readMail.getMailObjList().get(i)).toString());
 	    //}
 		
 	    Judge j = new JudgeByC5(); 
 	    ExtractSignInfo o = new ExtractSignInfo (j);
 	    ContactInfoModel info = o.extract("\n\n\n\n-- \n\n 姓名：奥奥\n 电子邮件：aa@xie.34test.cn\n 员工编号：E001\n 部门：TEST\n 小组：functionTest\n 职位：MidLevel\n 联系地址：厦门软件园二期\n 移动电话：12536547841\n\n\n 固定电话：0592-8754784\n\n\n 传真号码：0592-6938521\n\n\n 即时通讯：123467\n 进入企业时间：2008-1-5\n 生日：1984-01-14\n 性别：女\n 备注：eeeeeeeeeeeeeeee\n \n\n\n\n\n\n\n\n\n\n\n----- 原邮件信息 -----\n\n发件人：a（） a <aa@xiehq.35test.cn>\n\n收件人：a（）a <aa@xiehq.35test.cn>\n\n邮件发送时间：2009-12-08 22:44:10\n\n主题：Re:212121\n\n\n\n\n\n\n-- \n\n\n\n谢谢，辛苦了！\n\n柯秀明\n\n在线客服部·座席专员\n\nMobile:131555555555\n\n电话：2957777   分机：5008\n\n邮箱：kexm@35.cn\n\n\n\n\n\n\n\n\n\n\n\n----- 原邮件信息 -----\n\n发件人：a（） a <aa@xiehq.35test.cn> \n\n收件人：啊啊 <aa@xiehq.35test.cn> \n\n邮件发送时间：2009-12-08 17:25:31 \n\n主题：212121 \n\n\n\n\n郑梅琴（Megan） \n质控部 体系管理专员 \nTel：2958416 (8416) \nMobile：13859929886 \nEmail：zhengmq@35.cn \n\n\n\n-- \n\n\n\n谢谢，辛苦了！\n\n柯秀明\n\n在线客服部·座席专员\n\n电话：2957777   分机：5008\n\n邮箱：kexm@35.cn\n\n\n\n\n\n\n----------------------------------------------------\n\ndd\n", "奥奥", "aa@xiehq.35test.cn");
 	    System.out.println(info.toString());

 	}
 	

}
