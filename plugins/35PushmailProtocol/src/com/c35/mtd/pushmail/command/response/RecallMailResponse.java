package com.c35.mtd.pushmail.command.response;

import java.util.List;

import com.c35.mtd.pushmail.beans.RecallMailResult;

/**
 * @Description:邮件召回命令响应
 * @see:
 * @since:
 * @author: hanlx
 * @date:2013-1-7
 */
public class RecallMailResponse extends BaseResponse{
	private List<RecallMailResult> recallMailResults;

	
	public List<RecallMailResult> getRecallMailResults() {
		return recallMailResults;
	}

	
	public void setRecallMailResults(List<RecallMailResult> recallMailResults) {
		this.recallMailResults = recallMailResults;
	}
	
}
