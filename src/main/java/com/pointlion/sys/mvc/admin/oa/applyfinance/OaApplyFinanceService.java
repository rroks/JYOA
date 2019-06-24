package com.pointlion.sys.mvc.admin.oa.applyfinance;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.OaApplyFinance;
import com.pointlion.sys.plugin.shiro.ShiroKit;

public class OaApplyFinanceService{
	public static final OaApplyFinanceService me = new OaApplyFinanceService();
	public static final String TABLE_NAME = OaApplyFinance.tableName;
	
	/***
	 * 根据主键查询
	 */
	public OaApplyFinance getById(String id){
		return OaApplyFinance.dao.findById(id);
	}
	
	/***
	 * 获取分页
	 */
	public Page<Record> getPage(int pnum,int psize){
		String sql  = " from "+TABLE_NAME+" o LEFT JOIN act_hi_procinst p ON o.proc_ins_id=p.ID_  where o.userid='"+ShiroKit.getUserId()+"'  order by o.create_time desc";
		return Db.paginate(pnum, psize, " select * ", sql);
	}
	
	/***
	 * 删除
	 * @param ids
	 */
	@Before(Tx.class)
	public void deleteByIds(String ids){
    	String idarr[] = ids.split(",");
    	for(String id : idarr){
    		OaApplyFinance o = me.getById(id);
    		o.delete();
    	}
	}
	
}