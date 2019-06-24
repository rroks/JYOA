package com.pointlion.sys.mvc.admin.oa.project.build;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.admin.sys.dataauth.SysDataAuthTranslator;
import com.pointlion.sys.mvc.common.model.OaProjectBuild;

public class OaProjectBuildService{
	public static final OaProjectBuildService me = new OaProjectBuildService();
	public static final String TABLE_NAME = OaProjectBuild.tableName;
	
	/***
	 * 根据主键查询
	 */
	public OaProjectBuild getById(String id){
		return OaProjectBuild.dao.findById(id);
	}
	
	/***
	 * 获取分页
	 */
	public Page<Record> getPage(int pnum,int psize){
		String dataAuth = SysDataAuthTranslator.me.translator(TABLE_NAME);
//		String sql  = " from (select b.*,j.project_name,j.customer_name from "+TABLE_NAME+" b , oa_project j where b.id = j.build_id) o LEFT JOIN act_hi_procinst p ON o.proc_ins_id=p.ID_  where o.userid='"+ShiroKit.getUserId()+"'  order by o.create_time desc";
		String sql  = " from "+dataAuth+" o LEFT JOIN act_hi_procinst p ON o.proc_ins_id=p.ID_  where 1=1 order by o.create_time desc";
		return Db.paginate(pnum, psize, " select o.*,p.PROC_DEF_ID_ defid ", sql);
	}
	
	/***
	 * 删除
	 * @param ids
	 */
	@Before(Tx.class)
	public void deleteByIds(String ids){
    	String idarr[] = ids.split(",");
    	for(String id : idarr){
    		OaProjectBuild o = me.getById(id);
    		o.delete();
    	}
	}
	
}