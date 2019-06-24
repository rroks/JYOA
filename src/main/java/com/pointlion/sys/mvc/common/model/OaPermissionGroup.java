package com.pointlion.sys.mvc.common.model;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseOaPermissionGroup;
@SuppressWarnings("serial")
public class OaPermissionGroup extends BaseOaPermissionGroup<OaPermissionGroup> {
	public static final OaPermissionGroup dao = new OaPermissionGroup();
	public static final String tableName = "oa_permission_group";
	
	/***
	 * 根据主键查询
	 */
	public OaPermissionGroup getById(String id){
		return OaPermissionGroup.dao.findById(id);
	}
	
	/***
	 * 删除
	 * @param ids
	 */
	@Before(Tx.class)
	public void deleteByIds(String ids){
    	String idarr[] = ids.split(",");
    	for(String id : idarr){
    		OaPermissionGroup o = OaPermissionGroup.dao.getById(id);
    		o.delete();
    	}
	}
	
	public List<Record> getOrgidByDefKeyAndU(String defKey, String userid){
		return Db.find("select o.orgid from oa_permission_org o left join oa_permission_table t on o.groupid = t.groupid where t.userid = '"+userid+"' and t.defkey ='"+defKey+"'");
	}
	
}