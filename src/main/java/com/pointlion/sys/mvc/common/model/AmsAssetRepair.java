package com.pointlion.sys.mvc.common.model;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseAmsAssetRepair;
@SuppressWarnings("serial")
public class AmsAssetRepair extends BaseAmsAssetRepair<AmsAssetRepair> {
	public static final AmsAssetRepair dao = new AmsAssetRepair();
	public static final String tableName = "ams_asset_repair";
	/***
	 * 根据主键查询
	 */
	public AmsAssetRepair getById(String id){
		return AmsAssetRepair.dao.findById(id);
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
    		AmsAssetRepair o = AmsAssetRepair.dao.getById(id);
    		o.delete();
    	}
	}
	
}