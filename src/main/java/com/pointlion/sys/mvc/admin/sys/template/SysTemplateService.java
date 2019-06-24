package com.pointlion.sys.mvc.admin.sys.template;
import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.SysTemplate;
import com.pointlion.sys.plugin.shiro.ShiroKit;

public class SysTemplateService {
    public static final SysTemplateService me = new SysTemplateService();
    public static final String TABLE_NAME = SysTemplate.tableName;

    /***
     * 根据主键查询
     */
    public SysTemplate getById(String id){
        return SysTemplate.dao.findById(id);
    }

    /***
     * 获取分页
     */
    public Page<Record> getPage(int pnum,int psize,String name){
        String sql  = " from "+TABLE_NAME+" o LEFT JOIN sys_user p ON o.uploaduser=p.id where 1=1 ";
        if(StrKit.notBlank(name)){
            sql = sql + " and o.name like '%"+name+"%' ";
        }
        sql = sql + "order by o.id+0";
        return Db.paginate(pnum, psize, " select o.*,p.name uploaduser ", sql);
    }

    /***
     * 删除
     * @param ids
     */
    @Before(Tx.class)
    public void deleteByIds(String ids){
        String idarr[] = ids.split(",");
        for(String id : idarr){
            SysTemplate o = me.getById(id);
            o.delete();
        }
    }
}
