package com.pointlion.sys.mvc.common.model;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseOaCustomflowJoinlty;
@SuppressWarnings("serial")
public class OaCustomflowJoinlty extends BaseOaCustomflowJoinlty<OaCustomflowJoinlty> {
    public static final OaCustomflowJoinlty dao = new OaCustomflowJoinlty();
    public static final String tableName = "oa_customflow_joinlty";

    /***
     * 根据主键查询
     */
    public OaCustomflowJoinlty getById(String id){
        return OaCustomflowJoinlty.dao.findById(id);
    }

    /***
     * 删除
     * @param ids
     */
    @Before(Tx.class)
    public void deleteByIds(String ids){
        String idarr[] = ids.split(",");
        for(String id : idarr){
            OaCustomflowJoinlty o = OaCustomflowJoinlty.dao.getById(id);
            o.delete();
        }
    }

}