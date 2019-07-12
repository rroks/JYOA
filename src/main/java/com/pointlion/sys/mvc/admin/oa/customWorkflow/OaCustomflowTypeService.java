package com.pointlion.sys.mvc.admin.oa.customWorkflow;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.OaCustomflowType;
import com.pointlion.sys.plugin.shiro.ShiroKit;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

public class OaCustomflowTypeService {
    public static final OaCustomflowTypeService me = new OaCustomflowTypeService();
    public static final String TABLE_NAME = OaCustomflowType.tableName;

    /***
     * 根据主键查询
     */
    public OaCustomflowType getById(String id){
        return OaCustomflowType.dao.findById(id);
    }

    /***
     * 获取分页
     */
    public Page<Record> getPage(int pnum, int psize, Map param){
        String sql  = " from "+TABLE_NAME+" o LEFT JOIN sys_user p ON o.create_user=p.id  where 1=1 ";
        String name =String.valueOf(param.get("name"));
        String type1 ="All".equals(param.get("type1"))?"":String.valueOf(param.get("type1"));
        String type2 ="All".equals(param.get("type2"))?"":String.valueOf(param.get("type2"));
        String type3 ="All".equals(param.get("type3"))?"":String.valueOf(param.get("type3"));
        if(StrKit.notBlank(name)){
            sql+=" and o.name like '%"+name+"%'";
        }
        if(StrKit.notBlank(type1)){
            if(StrKit.notBlank(type2)){
                if(StrKit.notBlank(type3)){
                    sql+=" and o.id = '"+type3+"'";
                }else{
                    sql+=" and o.id = '"+type2+"'";
                }
            }else{
                sql+=" and o.id = '"+type1+"'";
            }

        }


        sql+=" order by o.create_time desc";
        return Db.paginate(pnum, psize, " select o.*,p.name create_user ", sql);
    }

    /***
     * 获取分页
     */
    public List<OaCustomflowType> getList(int level,String parentid){
        String sql  = "select o.* from "+TABLE_NAME+" o where 1=1";
        if(StrKit.notBlank(parentid)){
            sql +=" and o.parent ='"+parentid +"'";
        }
        if(level!=0){
            sql +=" and o.level ="+level ;
        }

        sql +="  order by o.create_time desc";
        return OaCustomflowType.dao.find(sql);
    }

    /***
     * 删除
     * @param ids
     */
    @Before(Tx.class)
    public void deleteByIds(String ids){
        String idarr[] = ids.split(",");
        for(String id : idarr){
            OaCustomflowType o = me.getById(id);
            o.delete();
        }
    }

    /***
     * 删除
     * @param typeObj
     */
    @Before(Tx.class)
    public boolean deleteById(OaCustomflowType typeObj){
        typeObj.delete();
        String sql = "select * from " + TABLE_NAME + " where id = '%s'";
        return OaCustomflowType.dao.find(String.format(sql, typeObj)).size() == 0;
    }

    public boolean hasChild(String typeId) {
        String sql = "select * from %s where parent = '%s'";
        return OaCustomflowType.dao.find(String.format(sql, TABLE_NAME, sql)).size() != 0;
    }

}
