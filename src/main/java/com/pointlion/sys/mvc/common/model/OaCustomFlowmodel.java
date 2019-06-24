package com.pointlion.sys.mvc.common.model;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseOaCustomflowFlowmodel;
public class OaCustomFlowmodel extends BaseOaCustomflowFlowmodel<OaCustomFlowmodel>{
    public static final OaCustomFlowmodel dao = new OaCustomFlowmodel();
    public static final String tableName = "oa_customflow_flowmodel";

    /***
     * 根据主键查询
     */
    public OaCustomFlowmodel getById(String id){
        return OaCustomFlowmodel.dao.findById(id);
    }

    /***
     * 删除
     * @param ids
     */
    @Before(Tx.class)
    public void deleteByIds(String ids){
        String idarr[] = ids.split(",");
        for(String id : idarr){
            OaCustomFlowmodel o = OaCustomFlowmodel.dao.getById(id);
            o.delete();
        }
    }
}
