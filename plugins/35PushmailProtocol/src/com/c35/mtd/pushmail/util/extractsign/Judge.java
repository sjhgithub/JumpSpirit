package com.c35.mtd.pushmail.util.extractsign;



import java.io.IOException;

/**
 * 
 * 分类器的抽象函数
 * @author yangxf1
 *
 */
public abstract class Judge {

	/**
	 * 判断分数来决定超过签名区的判断阀值
	 * @param score 某文本的判断分数，正常情况下是classify的返回值 
	 * @return 分数来决定超过签名区的判断阀值
	 */
	abstract public boolean isScoreAboveThreshold(int score);
	
	/**
	 * 判断某行文本在签名区的置信度
	 * @param features 所有行文本的特征数组
	 * @param num 当前行序号
	 *  
	 * @return 某行文本在签名区的置信度
	 */	
	abstract public int classify(LineFeature.Feature[] features, int num);
	
	/**
	 * 判断文本是否在签名区
	 * @param features 所有行文本的特征数组
	 * @param num 当前行序号
	 * 
	 * @return 该行是否在签名区
	 */
	abstract public boolean isSingleLineSign(LineFeature.Feature[] features, int num);
	
	protected LineFeature.Feature featureOr(LineFeature.Feature feature1, LineFeature.Feature feature2){
		LineFeature.Feature f = new LineFeature.Feature();

		if (feature1 == null && feature2 == null){
			return f;
		}else if (feature1 == null)
			return feature2;
		else if (feature2 == null)
			return feature1;
		
		
		f.datePattern = feature1.datePattern || feature2.datePattern;
		f.emailPattern = feature1.emailPattern || feature2.emailPattern;
		f.likeAddress = feature1.likeAddress || feature2.likeAddress;
		f.likeAName = feature1.likeAName || feature2.likeAName;
		f.lineMark = feature1.lineMark || feature2.lineMark;
		f.lotsSpecialSymbols = feature1.lotsSpecialSymbols || feature2.lotsSpecialSymbols;
		f.phoneNumberPattern = feature1.phoneNumberPattern || feature2.phoneNumberPattern;
		f.senderEmail = feature1.senderEmail || feature2.senderEmail;
		f.senderName = feature1.senderName || feature2.senderName;
		f.startWithReplyMark = feature1.startWithReplyMark || feature2.startWithReplyMark;
		f.typicalReplyWords = feature1.typicalReplyWords || feature2.typicalReplyWords;
		f.typicalSignWords = feature1.typicalSignWords || feature2.typicalSignWords;
		f.urlPattern =  feature1.urlPattern || feature2.urlPattern;
		
		return f;
	}
}
