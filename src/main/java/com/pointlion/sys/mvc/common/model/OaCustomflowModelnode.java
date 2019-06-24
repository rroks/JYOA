package com.pointlion.sys.mvc.common.model;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseOaCustomflowModelnode;
@SuppressWarnings("serial")
public class OaCustomflowModelnode extends BaseOaCustomflowModelnode<OaCustomflowModelnode> {
    public static final OaCustomflowModelnode dao = new OaCustomflowModelnode();
    public static final String tableName = "oa_customflow_modelnode";

    /***
     * 根据主键查询
     */
    public OaCustomflowModelnode getById(String id){
        return OaCustomflowModelnode.dao.findById(id);
    }

    /***
     * 删除
     * @param ids
     */
    @Before(Tx.class)
    public void deleteByIds(String ids){
        String idarr[] = ids.split(",");
        for(String id : idarr){
            OaCustomflowModelnode o = OaCustomflowModelnode.dao.getById(id);
            o.delete();
        }
    }

}