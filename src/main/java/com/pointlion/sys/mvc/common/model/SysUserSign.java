package com.pointlion.sys.mvc.common.model;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseSysUserSign;
@SuppressWarnings("serial")
public class SysUserSign extends BaseSysUserSign<SysUserSign> {
	public static final SysUserSign dao = new SysUserSign();
	public static final String tableName = "sys_user_sign";
	
	/***
	 * 根据主键查询
	 */
	public SysUserSign getById(String id){
		return SysUserSign.dao.findById(id);
	}
	
	public SysUserSign getByUserid(String userid){
		return SysUserSign.dao.findFirst("select * from sys_user_sign where userid = '"+userid+"'");
	}

	public SysUserSign getByUserTaskid(String userid,String taskid){
		return SysUserSign.dao.findFirst("select * from sys_user_sign where userid = '"+userid+"' and taskid ='"+taskid +"'");
	}

	
	/***
	 * 删除
	 * @param ids
	 */
	@Before(Tx.class)
	public void deleteByIds(String ids){
    	String idarr[] = ids.split(",");
    	for(String id : idarr){
    		SysUserSign o = SysUserSign.dao.getById(id);
    		o.delete();
    	}
	}
	
}