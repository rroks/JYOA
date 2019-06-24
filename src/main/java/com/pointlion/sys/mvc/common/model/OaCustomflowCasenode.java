package com.pointlion.sys.mvc.common.model;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseOaCustomflowCasenode;
@SuppressWarnings("serial")
public class OaCustomflowCasenode extends BaseOaCustomflowCasenode<OaCustomflowCasenode> {
    public static final OaCustomflowCasenode dao = new OaCustomflowCasenode();
    public static final String tableName = "oa_customflow_casenode";

    /***
     * 根据主键查询
     */
    public OaCustomflowCasenode getById(String id){
        return OaCustomflowCasenode.dao.findById(id);
    }

    /***
     * 删除
     * @param ids
     */
    @Before(Tx.class)
    public void deleteByIds(String ids){
        String idarr[] = ids.split(",");
        for(String id : idarr){
            OaCustomflowCasenode o = OaCustomflowCasenode.dao.getById(id);
            o.delete();
        }
    }

}