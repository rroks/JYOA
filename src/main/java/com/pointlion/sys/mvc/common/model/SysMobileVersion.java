package com.pointlion.sys.mvc.common.model;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseSysMobileVersion;
@SuppressWarnings("serial")
public class SysMobileVersion extends BaseSysMobileVersion<SysMobileVersion> {
	public static final SysMobileVersion dao = new SysMobileVersion();
	public static final String tableName = "sys_mobile_version";
	
	/***
	 * 根据主键查询
	 */
	public SysMobileVersion getById(String id){
		return SysMobileVersion.dao.findById(id);
	}
	
	/***
	 * 删除
	 * @param ids
	 */
	@Before(Tx.class)
	public void deleteByIds(String ids){
    	String idarr[] = ids.split(",");
    	for(String id : idarr){
    		SysMobileVersion o = SysMobileVersion.dao.getById(id);
    		o.delete();
    	}
	}
	
	public SysMobileVersion getByPublish(String ifPublish){
		return SysMobileVersion.dao.findFirst("select * from sys_mobile_version where if_publish = '"+ifPublish+"' order by updatetime desc");
	}
}