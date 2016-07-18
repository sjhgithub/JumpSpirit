package com.c35.mtd.pushmail.command.request;

/**
 * GetIdsById命令请求
 * @author:温楠
 * @see:
 * @since:
 * @copyright © 35.com
 * @Date:2012-11-2
 */
public class GetIdsByIdRequest extends BaseRequest {

	private String folderId;
	private int limit;// 最多返回的个数（必填项）

	private String id;// （非必填项）作为参照物的某封邮件的uid。（如果不传此字段，则表示收取当前箱子中最新的limit个uids）
	// 收取方式：（必填项）
	// Type = 1 ，为 请求服务器上时间比参照物uid新的uid们，不包括作为参照物的uid。
	// Type = 2 ，为 请求服务器上时间比参照物uid旧的uid们，不包括作为参照物的uid。
	private int type;

	public enum GetIdsType {
		// **为 请求服务器上时间比参照物uid新的uid们，不包括作为参照物的uid*//*
		NEW,
		// ** 为 请求服务器上时间比参照物uid旧的uid们，不包括作为参照物的uid *//*
		OLD,
	}

	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
