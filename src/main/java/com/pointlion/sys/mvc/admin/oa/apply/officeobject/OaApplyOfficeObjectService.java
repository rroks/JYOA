package com.pointlion.sys.mvc.admin.oa.apply.officeobject;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.admin.sys.dataauth.SysDataAuthTranslator;
import com.pointlion.sys.mvc.common.model.OaApplyOfficeObject;

public class OaApplyOfficeObjectService{
	public static final OaApplyOfficeObjectService me = new OaApplyOfficeObjectService();
	private static final String TABLE_NAME = OaApplyOfficeObject.tableName;
	
	/***
	 * 根据主键查询
	 */
	public OaApplyOfficeObject getById(String id){
		return OaApplyOfficeObject.dao.findById(id);
	}
	
	/***
	 * 获取分页
	 */
	public Page<Record> getPage(int pnum,int psize){
		String dataAuth = SysDataAuthTranslator.me.translator(TABLE_NAME);
		String sql  = " from "+dataAuth+" o LEFT JOIN act_hi_procinst p ON o.proc_ins_id=p.ID_ where 1=1 order by o.create_time desc";
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
    		OaApplyOfficeObject o = me.getById(id);
    		o.delete();
    	}
	}
	
}