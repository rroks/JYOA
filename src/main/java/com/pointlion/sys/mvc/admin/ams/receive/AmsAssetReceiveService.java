package com.pointlion.sys.mvc.admin.ams.receive;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.admin.sys.dataauth.SysDataAuthTranslator;
import com.pointlion.sys.mvc.common.model.AmsAssetReceive;

public class AmsAssetReceiveService{
	public static final AmsAssetReceiveService me = new AmsAssetReceiveService();
	public static final String TABLE_NAME = AmsAssetReceive.tableName;
	
	/***
	 * 根据主键查询
	 */
	public AmsAssetReceive getById(String id){
		return AmsAssetReceive.dao.findById(id);
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
    		AmsAssetReceive o = me.getById(id);
    		o.delete();
    	}
	}
	
}