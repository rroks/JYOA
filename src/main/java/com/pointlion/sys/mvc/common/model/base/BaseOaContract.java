package com.pointlion.sys.mvc.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseOaContract<M extends BaseOaContract<M>> extends Model<M> implements IBean {

	public void setId(java.lang.String id) {
		set("id", id);
	}
	
	public java.lang.String getId() {
		return getStr("id");
	}

	public void setTitle(java.lang.String title) {
		set("title", title);
	}
	
	public java.lang.String getTitle() {
		return getStr("title");
	}

	public void setName(java.lang.String name) {
		set("name", name);
	}
	
	public java.lang.String getName() {
		return getStr("name");
	}

	public void setCustomName(java.lang.String customName) {
		set("custom_name", customName);
	}
	
	public java.lang.String getCustomName() {
		return getStr("custom_name");
	}

	public void setCustomContactPerson(java.lang.String customContactPerson) {
		set("custom_contact_person", customContactPerson);
	}
	
	public java.lang.String getCustomContactPerson() {
		return getStr("custom_contact_person");
	}

	public void setCustomContactMobile(java.lang.String customContactMobile) {
		set("custom_contact_mobile", customContactMobile);
	}
	
	public java.lang.String getCustomContactMobile() {
		return getStr("custom_contact_mobile");
	}

	public void setType(java.lang.String type) {
		set("type", type);
	}
	
	public java.lang.String getType() {
		return getStr("type");
	}

	public void setType2(java.lang.String type2) {
		set("type2", type2);
	}
	
	public java.lang.String getType2() {
		return getStr("type2");
	}
	
	public void setAmountOfMoney(java.lang.String amountOfMoney) {
		set("amount_of_money", amountOfMoney);
	}
	
	public java.lang.String getAmountOfMoney() {
		return getStr("amount_of_money");
	}

	public void setDetail(java.lang.String detail) {
		set("detail", detail);
	}
	
	public java.lang.String getDetail() {
		return getStr("detail");
	}

	public void setBackground(java.lang.String background) {
		set("background", background);
	}
	
	public java.lang.String getBackground() {
		return getStr("background");
	}

	public void setNeed(java.lang.String need) {
		set("need", need);
	}
	
	public java.lang.String getNeed() {
		return getStr("need");
	}

	public void setWorkTogether(java.lang.String workTogether) {
		set("work_together", workTogether);
	}
	
	public java.lang.String getWorkTogether() {
		return getStr("work_together");
	}

	public void setAddExt(java.lang.String addExt) {
		set("add_ext", addExt);
	}
	
	public java.lang.String getAddExt() {
		return getStr("add_ext");
	}

	public void setDangerNeed(java.lang.String dangerNeed) {
		set("danger_need", dangerNeed);
	}
	
	public java.lang.String getDangerNeed() {
		return getStr("danger_need");
	}

	public void setDangerNeedNoOk(java.lang.String dangerNeedNoOk) {
		set("danger_need_no_ok", dangerNeedNoOk);
	}
	
	public java.lang.String getDangerNeedNoOk() {
		return getStr("danger_need_no_ok");
	}

	public void setDangerPayHard(java.lang.String dangerPayHard) {
		set("danger_pay_hard", dangerPayHard);
	}
	
	public java.lang.String getDangerPayHard() {
		return getStr("danger_pay_hard");
	}

	public void setDangerLaw(java.lang.String dangerLaw) {
		set("danger_law", dangerLaw);
	}
	
	public java.lang.String getDangerLaw() {
		return getStr("danger_law");
	}

	public void setDangerExt(java.lang.String dangerExt) {
		set("danger_ext", dangerExt);
	}
	
	public java.lang.String getDangerExt() {
		return getStr("danger_ext");
	}

	public void setCreateUserid(java.lang.String createUserid) {
		set("create_userid", createUserid);
	}
	
	public java.lang.String getCreateUserid() {
		return getStr("create_userid");
	}

	public void setCreateTime(java.lang.String createTime) {
		set("create_time", createTime);
	}
	
	public java.lang.String getCreateTime() {
		return getStr("create_time");
	}

	public void setState(java.lang.String state) {
		set("state", state);
	}
	
	public java.lang.String getState() {
		return getStr("state");
	}

}