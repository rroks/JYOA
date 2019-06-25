package com.pointlion.sys.mvc.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseSysTemplate<M extends BaseSysTemplate<M>> extends Model<M> implements IBean {

	public void setId(java.lang.String id) {
		set("id", id);
	}
	
	public java.lang.String getId() {
		return getStr("id");
	}

	public void setName(java.lang.String name) {
		set("name", name);
	}
	
	public java.lang.String getName() {
		return getStr("name");
	}

	public void setBelong(java.lang.String belong) { set("belong", belong); }
	
	public java.lang.String getBelong() {
		return getStr("belong");
	}

	public void setFilepath(java.lang.String filepath) {
		set("filepath", filepath);
	}

	public java.lang.String getFilepath() {
		return getStr("filepath");
	}

	public void setFilename(java.lang.String filename) {
		set("belong", filename);
	}

	public java.lang.String getFilename() {
		return getStr("filename");
	}

	public void setUploaduser(java.lang.String uploaduser) {
		set("uploaduser", uploaduser);
	}
	
	public java.lang.String getUploaduser() {
		return getStr("uploaduser");
	}

	public void setUploadtime(java.lang.String uploadtime) {
		set("uploadtime", uploadtime);
	}
	
	public java.lang.String getUploadtime() {
		return getStr("uploadtime");
	}

}