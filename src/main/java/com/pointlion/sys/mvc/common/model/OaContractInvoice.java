package com.pointlion.sys.mvc.common.model;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseOaContractInvoice;
@SuppressWarnings("serial")
public class OaContractInvoice extends BaseOaContractInvoice<OaContractInvoice> {
	public static final OaContractInvoice dao = new OaContractInvoice();
	public static final String tableName = "oa_contract_invoice";
	
	/***
	 * 根据主键查询
	 */
	public OaContractInvoice getById(String id){
		return OaContractInvoice.dao.findById(id);
	}
	
	/***
	 * 获取分页
	 */
	public Page<Record> getPage(int pnum,int psize){
		String sql  = " from "+tableName+" o ";
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
    		OaContractInvoice o = OaContractInvoice.dao.getById(id);
    		o.delete();
    	}
	}
	
}