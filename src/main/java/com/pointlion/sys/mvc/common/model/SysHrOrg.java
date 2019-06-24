package com.pointlion.sys.mvc.common.model;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseSysHrOrg;
@SuppressWarnings("serial")
public class SysHrOrg extends BaseSysHrOrg<SysHrOrg> {
	public static final SysHrOrg dao = new SysHrOrg();
	public static final String tableName = "sys_hr_org";
	
	/***
	 * 根据主键查询
	 */
	public SysHrOrg getById(String id){
		return SysHrOrg.dao.findById(id);
	}
	
	/***
	 * 删除
	 * @param ids
	 */
	@Before(Tx.class)
	public void deleteByIds(String ids){
    	String idarr[] = ids.split(",");
    	for(String id : idarr){
    		SysHrOrg o = SysHrOrg.dao.getById(id);
    		o.delete();
    	}
	}
	
	
	
	/**获取选中的组织
	 * @param userid
	 * @return
	 */
	public List<SysHrOrg> getCheckOrgByUserid(String userid){
		return SysHrOrg.dao.find("select * from "+tableName+" where userid = '"+userid+"'");
	}
	
	public void deleteByUserid(String userid){
		List<SysHrOrg> list = getCheckOrgByUserid(userid);
		if(StrKit.notNull(list)){
			for (SysHrOrg org : list) {
				org.delete();
			}
		}
	}
	
	
}