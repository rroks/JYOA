package com.pointlion.sys.mvc.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseSysUser<M extends BaseSysUser<M>> extends Model<M> implements IBean {

	public void setId(java.lang.String id) {
		set("id", id);
	}
	
	public java.lang.String getId() {
		return getStr("id");
	}

	public void setUsername(java.lang.String username) {
		set("username", username);
	}
	
	public java.lang.String getUsername() {
		return getStr("username");
	}

	public void setPassword(java.lang.String password) {
		set("password", password);
	}
	
	public java.lang.String getPassword() {
		return getStr("password");
	}

	public void setName(java.lang.String name) {
		set("name", name);
	}
	
	public java.lang.String getName() {
		return getStr("name");
	}

	public void setSex(java.lang.String sex) {
		set("sex", sex);
	}
	
	public java.lang.String getSex() {
		return getStr("sex");
	}

	public void setStatus(java.lang.String status) {
		set("status", status);
	}
	
	public java.lang.String getStatus() {
		return getStr("status");
	}

	public void setOrgid(java.lang.String orgid) {
		set("orgid", orgid);
	}
	
	public java.lang.String getOrgid() {
		return getStr("orgid");
	}

	public void setStationid(java.lang.String stationid) {
		set("stationid", stationid);
	}
	
	public java.lang.String getStationid() {
		return getStr("stationid");
	}

	public void setEmail(java.lang.String email) {
		set("email", email);
	}
	
	public java.lang.String getEmail() {
		return getStr("email");
	}

	public void setIdcard(java.lang.String idcard) {
		set("idcard", idcard);
	}
	
	public java.lang.String getIdcard() {
		return getStr("idcard");
	}

	public void setIsAdmin(java.lang.String isAdmin) {
		set("is_admin", isAdmin);
	}
	
	public java.lang.String getIsAdmin() {
		return getStr("is_admin");
	}

	public void setSort(java.lang.Long sort) {
		set("sort", sort);
	}
	
	public java.lang.Long getSort() {
		return getLong("sort");
	}

	public void setMobile(java.lang.String mobile) {
		set("mobile", mobile);
	}
	
	public java.lang.String getMobile() {
		return getStr("mobile");
	}

	public void setImg(java.lang.String img) {
		set("img", img);
	}
	
	public java.lang.String getImg() {
		return getStr("img");
	}

	public void setType(java.lang.String type) {
		set("type", type);
	}
	
	public java.lang.String getType() {
		return getStr("type");
	}

	public void setEntryDate(java.lang.String entryDate) {
		set("entry_date", entryDate);
	}
	
	public java.lang.String getEntryDate() {
		return getStr("entry_date");
	}

	public void setBirthDate(java.lang.String birthDate) {
		set("birth_date", birthDate);
	}
	
	public java.lang.String getBirthDate() {
		return getStr("birth_date");
	}

	public void setYearHoliday(java.lang.String yearHoliday) {
		set("year_holiday", yearHoliday);
	}
	
	public java.lang.String getYearHoliday() {
		return getStr("year_holiday");
	}

	public void setSignNamePic(java.lang.String signNamePic) {
		set("sign_name_pic", signNamePic);
	}
	
	public java.lang.String getSignNamePic() {
		return getStr("sign_name_pic");
	}

	public void setInCompanyDate(java.lang.String inCompanyDate) {
		set("in_company_date", inCompanyDate);
	}
	
	public java.lang.String getInCompanyDate() {
		return getStr("in_company_date");
	}

	public void setDimissionDate(java.lang.String dimissionDate) {
		set("dimission_date", dimissionDate);
	}
	
	public java.lang.String getDimissionDate() {
		return getStr("dimission_date");
	}

	public void setExperience(java.lang.String experience) {
		set("experience", experience);
	}
	
	public java.lang.String getExperience() {
		return getStr("experience");
	}

	public void setEduExperience(java.lang.String eduExperience) {
		set("edu_experience", eduExperience);
	}
	
	public java.lang.String getEduExperience() {
		return getStr("edu_experience");
	}

	public void setWorkNum(java.lang.String workNum) {
		set("work_num", workNum);
	}
	
	public java.lang.String getWorkNum() {
		return getStr("work_num");
	}

	public void setPosition(java.lang.String position) {
		set("position", position);
	}
	
	public java.lang.String getPosition() {
		return getStr("position");
	}

	public void setSignImg(java.lang.String signImg) {
		set("sign_img", signImg);
	}
	
	public java.lang.String getSignImg() {
		return getStr("sign_img");
	}

}