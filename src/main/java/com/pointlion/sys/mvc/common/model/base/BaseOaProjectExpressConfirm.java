package com.pointlion.sys.mvc.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseOaProjectExpressConfirm<M extends BaseOaProjectExpressConfirm<M>> extends Model<M> implements IBean {

	public void setId(java.lang.String id) {
		set("id", id);
	}
	
	public java.lang.String getId() {
		return getStr("id");
	}

	public void setUserid(java.lang.String userid) {
		set("userid", userid);
	}
	
	public java.lang.String getUserid() {
		return getStr("userid");
	}

	public void setApplyerName(java.lang.String applyerName) {
		set("applyer_name", applyerName);
	}
	
	public java.lang.String getApplyerName() {
		return getStr("applyer_name");
	}

	public void setOrgId(java.lang.String orgId) {
		set("org_id", orgId);
	}
	
	public java.lang.String getOrgId() {
		return getStr("org_id");
	}

	public void setOrgName(java.lang.String orgName) {
		set("org_name", orgName);
	}
	
	public java.lang.String getOrgName() {
		return getStr("org_name");
	}

	public void setLeaderMessage(java.lang.String leaderMessage) {
		set("leader_message", leaderMessage);
	}
	
	public java.lang.String getLeaderMessage() {
		return getStr("leader_message");
	}

	public void setLeader2Message(java.lang.String leader2Message) {
		set("leader2_message", leader2Message);
	}
	
	public java.lang.String getLeader2Message() {
		return getStr("leader2_message");
	}

	public void setIfSubmit(java.lang.String ifSubmit) {
		set("if_submit", ifSubmit);
	}
	
	public java.lang.String getIfSubmit() {
		return getStr("if_submit");
	}

	public void setIfComplete(java.lang.String ifComplete) {
		set("if_complete", ifComplete);
	}
	
	public java.lang.String getIfComplete() {
		return getStr("if_complete");
	}

	public void setIfAgree(java.lang.String ifAgree) {
		set("if_agree", ifAgree);
	}
	
	public java.lang.String getIfAgree() {
		return getStr("if_agree");
	}

	public void setProcInsId(java.lang.String procInsId) {
		set("proc_ins_id", procInsId);
	}
	
	public java.lang.String getProcInsId() {
		return getStr("proc_ins_id");
	}

	public void setCreateTime(java.lang.String createTime) {
		set("create_time", createTime);
	}
	
	public java.lang.String getCreateTime() {
		return getStr("create_time");
	}

	public void setDes(java.lang.String des) {
		set("des", des);
	}
	
	public java.lang.String getDes() {
		return getStr("des");
	}

	public void setProjectId(java.lang.String projectId) {
		set("project_id", projectId);
	}
	
	public java.lang.String getProjectId() {
		return getStr("project_id");
	}

	public void setExpressContent(java.lang.String expressContent) {
		set("express_content", expressContent);
	}
	
	public java.lang.String getExpressContent() {
		return getStr("express_content");
	}

	public void setExpressNum(java.lang.String expressNum) {
		set("express_num", expressNum);
	}
	
	public java.lang.String getExpressNum() {
		return getStr("express_num");
	}

	public void setSendAddress(java.lang.String sendAddress) {
		set("send_address", sendAddress);
	}
	
	public java.lang.String getSendAddress() {
		return getStr("send_address");
	}

	public void setSendMobile(java.lang.String sendMobile) {
		set("send_mobile", sendMobile);
	}
	
	public java.lang.String getSendMobile() {
		return getStr("send_mobile");
	}

	public void setReceiveAddress(java.lang.String receiveAddress) {
		set("receive_address", receiveAddress);
	}
	
	public java.lang.String getReceiveAddress() {
		return getStr("receive_address");
	}

	public void setReceiveMobile(java.lang.String receiveMobile) {
		set("receive_mobile", receiveMobile);
	}
	
	public java.lang.String getReceiveMobile() {
		return getStr("receive_mobile");
	}

	public void setSendName(java.lang.String send_name) {
		set("send_name", send_name);
	}
	
	public java.lang.String getSendName() {
		return getStr("send_name");
	}
	
	public void setReceiveName(java.lang.String receive_name) {
		set("receive_name", receive_name);
	}
	
	public java.lang.String getReceiveName() {
		return getStr("receive_name");
	} 
	public void setTitle(java.lang.String title) {
		set("title", title);
	}

	public java.lang.String getTitle() {
		return get("title");
	}
}
