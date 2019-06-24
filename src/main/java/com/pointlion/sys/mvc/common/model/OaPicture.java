package com.pointlion.sys.mvc.common.model;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseOaPicture;
@SuppressWarnings("serial")
public class OaPicture extends BaseOaPicture<OaPicture> {
	public static final OaPicture dao = new OaPicture();
	public static final String tableName = "oa_picture";
	
	/***
	 * 根据主键查询
	 */
	public OaPicture getById(String id){
		return OaPicture.dao.findById(id);
	}
	
	
	public List<OaPicture> getBylinkidAndDefKey(String linkid, String defKey){
		String sql = "select * from oa_picture where linkid= '"+linkid+"'";
		if(StrKit.notBlank(defKey)){
			sql = sql + "and defkey = '"+defKey+"'";
		}
		sql = sql + "order by createdate desc";
		return OaPicture.dao.find(sql);
	}
	
	
	
	/***
	 * 删除
	 * @param ids
	 */
	@Before(Tx.class)
	public void deleteByIds(String ids){
    	String idarr[] = ids.split(",");
    	for(String id : idarr){
    		OaPicture o = OaPicture.dao.getById(id);
    		o.delete();
    	}
	}
	
}