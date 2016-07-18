/*
 * Copyright (c) 2010 35.com, Inc. All rights reserved.
 * File Name:@(#)ContactInfoModel.java
 * Encoding: UTF-8 
 * Date:Aug 20, 2010
 */
package com.c35.mtd.pushmail.util.extractsign;

import java.io.Serializable;


/**
 * @author chenshch
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ContactInfoModel implements Serializable, Comparable<ContactInfoModel> {

    private String id;

    private String name; // 姓名

    private String email; // email

    private String companyName; // 公司

    private String branchName; // 部门名

    private String postName; // 职位
    
    private String companyTel; //公司电话
    
    private String mobilePhone; //移动电话
    
    private String homeAddress; //家庭地址
    
    private String groupId;

    private boolean isMe = false;

    public boolean getIsMe() {
        return isMe;
    }

    public void setIsMe(boolean isMe) {
        this.isMe = isMe;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the companyName
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * @param companyName the companyName to set
     */
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /**
     * @return the branchName
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * @param branchName the branchName to set
     */
    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    /**
     * @return the postName
     */
    public String getPostName() {
        return postName;
    }

    /**
     * @param postName the postName to set
     */
    public void setPostName(String postName) {
        this.postName = postName;
    }

    /**
     * 
     * @return company telephone number
     */
    public String getCompanyTel() {
        return companyTel;
    }

    /**
     * 
     * @param companyTel to set
     */
    public void setCompanyTel(String companyTel) {
        this.companyTel = companyTel;
    }

    /**
     * 
     * @return mobile phone number
     */
    public String getMobilePhone() {
        return mobilePhone;
    }

    /**
     * 
     * @param mobilePhone to set
     */
    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    /**
     * 
     * @return home address
     */
    public String getHomeAddress() {
        return homeAddress;
    }

    /**
     * 
     * @param homeAddress to set
     */
    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }
    
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public int compareTo(ContactInfoModel o) {
        return this.email.compareToIgnoreCase(o.getEmail());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ContactInfoModel other = (ContactInfoModel) obj;
        if (email == null) {
            if (other.email != null) {
                return false;
            }
        } else if (!email.equals(other.email)) {
            return false;
        }
        return true;
    }

}
