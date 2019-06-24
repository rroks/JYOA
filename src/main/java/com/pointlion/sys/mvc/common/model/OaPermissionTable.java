package com.pointlion.sys.mvc.common.model;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseOaPermissionTable;
@SuppressWarnings("serial")
public class OaPermissionTable extends BaseOaPermissionTable<OaPermissionTable> {
	public static final OaPermissionTable dao = new OaPermissionTable();
	public static final String tableName = "oa_permission_table";
	
	/***
	 * 根据主键查询
	 */
	public OaPermissionTable getById(String id){
		return OaPermissionTable.dao.findById(id);
	}
	
	public List<OaPermissionTable> getByGroupid(String groupid){
		return OaPermissionTable.dao.find("select * from oa_permission_table where groupid = '"+groupid+"' order by createtime desc");
	}
	
	public List<OaPermissionTable> getByUAndNotGroupid(String userid, String groupid){
		return OaPermissionTable.dao.find("select * from oa_permission_table where groupid != '"+groupid+"' and userid = '"+userid+"' order by createtime desc");
	}
	
	
	public void deleteByGroupid(String groupid){
		List<OaPermissionTable> list = getByGroupid(groupid);
		if(StrKit.notNull(list)){
			for (OaPermissionTable oa : list) {
				oa.delete();
			}
		}
	}
	
	
	/***
	 * 删除
	 * @param ids
	 */
	@Before(Tx.class)
	public void deleteByIds(String ids){
    	String idarr[] = ids.split(",");
    	for(String id : idarr){
    		OaPermissionTable o = OaPermissionTable.dao.getById(id);
    		o.delete();
    	}
	}
	
	public List<OaPermissionTable> getByUseridAndGruopid(String userid, String groupid){
		return OaPermissionTable.dao.find("select * from oa_permission_table where userid = '"+userid+"' and groupid = '"+groupid+"'");
	}
	
}