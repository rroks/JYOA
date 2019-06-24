package com.pointlion.sys.mvc.admin.oa.customWorkflow;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.OaCustomflowModelnodeUser;
import com.pointlion.sys.plugin.shiro.ShiroKit;

import java.util.List;

public class OaCustomflowModelnodeUserService {
    public static final OaCustomflowModelnodeUserService me = new OaCustomflowModelnodeUserService();
    public static final String TABLE_NAME = OaCustomflowModelnodeUser.tableName;

    /***
     * 根据主键查询
     */
    public OaCustomflowModelnodeUser getById(String id){
        return OaCustomflowModelnodeUser.dao.findById(id);
    }

    /***
     * 获取分页
     */
    public Page<Record> getPage(int pnum,int psize){
        String sql  = " from "+TABLE_NAME+" o LEFT JOIN act_hi_procinst p ON o.proc_ins_id=p.ID_  where o.userid='"+ShiroKit.getUserId()+"'  order by o.create_time desc";
        return Db.paginate(pnum, psize, " select * ",   sql);
    }

    /***
     * 根据模板节点id获取相关审批人
     */
    public List<OaCustomflowModelnodeUser> getBymodelnodeid(String modelnodeid){
        String sql  = "select o.* from "+TABLE_NAME+" o  where o.modelnodeid='"+modelnodeid+"'  order by o.create_time asc";
        return OaCustomflowModelnodeUser.dao.find( sql);
    }

    /***
     * 删除
     * @param ids
     */
    @Before(Tx.class)
    public void deleteByIds(String ids){
        String idarr[] = ids.split(",");
        for(String id : idarr){
            OaCustomflowModelnodeUser o = me.getById(id);
            o.delete();
        }
    }

}
