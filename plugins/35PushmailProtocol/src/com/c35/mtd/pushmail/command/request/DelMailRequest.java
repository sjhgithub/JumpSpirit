package com.c35.mtd.pushmail.command.request;

import java.util.List;
/**
 * 
 * @Description:DelMail命令请求
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class DelMailRequest extends BaseRequest {

    private List<String> mailIds;

    public List<String> getMailIds() {
        return mailIds;
    }

    public void setMailIds(List<String> mailIds) {
        this.mailIds = mailIds;
    }
}
