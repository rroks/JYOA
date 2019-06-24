package com.pointlion.sys.mvc.admin.sys.template;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.SysTemplateparam;
import com.pointlion.sys.plugin.shiro.ShiroKit;

public class SysTemplateparamService{
    public static final SysTemplateparamService me = new SysTemplateparamService();
    public static final String TABLE_NAME = SysTemplateparam.tableName;

    /***
     * 根据主键查询
     */
    public SysTemplateparam getById(String id){
        return SysTemplateparam.dao.findById(id);
    }

    /***
     * 获取分页
     */
    public Page<Record> getPage(int pnum,int psize,String name,String module){
        String sql  = " from "+TABLE_NAME+" o where 1=1 ";
        if(StrKit.notBlank(module)){
            sql = sql + " and o.module like '%"+module+"%' ";
        }
        if(StrKit.notBlank(name)){
            sql = sql + " and o.name like '%"+name+"%' ";
        }
        sql = sql + "order by o.id+0 asc";
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
            SysTemplateparam o = me.getById(id);
            o.delete();
        }
    }

}