package com.pointlion.sys.mvc.common.model;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseSysAttachment;
@SuppressWarnings("serial")
public class SysAttachment extends BaseSysAttachment<SysAttachment> {
	public static final SysAttachment dao = new SysAttachment();
	public static final String tableName = "sys_attachment";
	
	/***
	 * 根据主键查询
	 */
	public SysAttachment getById(String id){
		return SysAttachment.dao.findById(id);
	}
	
	public List<SysAttachment> getByBusidAndSuffix(String busId, String suffix){
		return SysAttachment.dao.find("select * from sys_attachment where business_id = '"+busId+"' and suffix = '"+suffix+"'");
	}
	
	/***
	 * 获取分页
	 */
	public Page<Record> getPage(int pnum,int psize){
		String sql  = " from sys_attachment o ";
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
    		SysAttachment o = SysAttachment.dao.getById(id);
    		o.delete();
    	}
	}
	
}