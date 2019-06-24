package com.pointlion.sys.mvc.admin.sys.mobileversion;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.SysMobileVersion;
import com.pointlion.sys.plugin.shiro.ShiroKit;

public class SysMobileVersionService{
	public static final SysMobileVersionService me = new SysMobileVersionService();
	public static final String TABLE_NAME = SysMobileVersion.tableName;
	
	/***
	 * 根据主键查询
	 */
	public SysMobileVersion getById(String id){
		return SysMobileVersion.dao.findById(id);
	}
	
	/***
	 * 获取分页
	 */
	public Page<Record> getPage(int pnum,int psize){
		String sql  = " from "+TABLE_NAME+" o order by o.updatetime desc";
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
    		SysMobileVersion o = me.getById(id);
    		o.delete();
    	}
	}
	public List<SysMobileVersion> getAllExSelf(String ifPublish, String id){
		return SysMobileVersion.dao.find("select * from sys_mobile_version where id != '"+id+"' and if_publish = '"+ifPublish+"' order by updatetime desc");
	}
	
	public void setOtherNotPublish(String ifPublish, String id){
		if("1".equals(ifPublish)){//系统只能有一个版本是已发布的，其余的要设置成未发布
			List<SysMobileVersion> list = getAllExSelf(ifPublish, id);
			if(StrKit.notNull(list)){
				for (SysMobileVersion v : list) {
					v.setIfPublish("0");
					v.update();
				}
			}
		}
	}
}