package com.pointlion.sys.mvc.common.model;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseOaPermissionOrg;
@SuppressWarnings("serial")
public class OaPermissionOrg extends BaseOaPermissionOrg<OaPermissionOrg> {
	public static final OaPermissionOrg dao = new OaPermissionOrg();
	public static final String tableName = "oa_permission_org";
	
	/***
	 * 根据主键查询
	 */
	public OaPermissionOrg getById(String id){
		return OaPermissionOrg.dao.findById(id);
	}
	
	public List<OaPermissionOrg> getByUserid(String groupid){
		return OaPermissionOrg.dao.find("select * from oa_permission_org where groupid = '"+groupid+"' order by createtime desc");
	}
	
	
	public void deleteByGroupid(String groupid){
		List<OaPermissionOrg> list = getByUserid(groupid);
		if(StrKit.notNull(list)){
			for (OaPermissionOrg org : list) {
				org.delete();
			}
		}
	}
	
	public List<OaPermissionOrg> getByUseridAndGroupid(String userid, String groupid){
		return OaPermissionOrg.dao.find("select * from oa_permission_org where userid = '"+userid+"' and groupid = '"+groupid+"' order by createtime desc");
	}
	
	
	/***
	 * 删除
	 * @param ids
	 */
	@Before(Tx.class)
	public void deleteByIds(String ids){
    	String idarr[] = ids.split(",");
    	for(String id : idarr){
    		OaPermissionOrg o = OaPermissionOrg.dao.getById(id);
    		o.delete();
    	}
	}
	
}