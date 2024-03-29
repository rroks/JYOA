package com.pointlion.sys.mvc.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseSysDataAuth<M extends BaseSysDataAuth<M>> extends Model<M> implements IBean {

	public void setId(java.lang.String id) {
		set("id", id);
	}
	
	public java.lang.String getId() {
		return getStr("id");
	}

	public void setTable(java.lang.String table) {
		set("table", table);
	}
	
	public java.lang.String getTable() {
		return getStr("table");
	}

	public void setAuthName(java.lang.String authName) {
		set("auth_name", authName);
	}
	
	public java.lang.String getAuthName() {
		return getStr("auth_name");
	}

	public void setDes(java.lang.String des) {
		set("des", des);
	}
	
	public java.lang.String getDes() {
		return getStr("des");
	}

	public void setCommandText(java.lang.String commandText) {
		set("command_text", commandText);
	}
	
	public java.lang.String getCommandText() {
		return getStr("command_text");
	}
	
	public void setStatus(java.lang.String status) {
		set("status", status);
	}
	
	public java.lang.String getStatus() {
		return getStr("status");
	}

}
