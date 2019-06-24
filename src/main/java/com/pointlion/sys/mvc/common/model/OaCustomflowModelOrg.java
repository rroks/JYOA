package com.pointlion.sys.mvc.common.model;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseOaCustomflowModelOrg;
@SuppressWarnings("serial")
public class OaCustomflowModelOrg extends BaseOaCustomflowModelOrg<OaCustomflowModelOrg> {
    public static final OaCustomflowModelOrg dao = new OaCustomflowModelOrg();
    public static final String tableName = "oa_customflow_model_org";

    /***
     * 根据主键查询
     */
    public OaCustomflowModelOrg getById(String id){
        return OaCustomflowModelOrg.dao.findById(id);
    }

    /***
     * 删除
     * @param ids
     */
    @Before(Tx.class)
    public void deleteByIds(String ids){
        String idarr[] = ids.split(",");
        for(String id : idarr){
            OaCustomflowModelOrg o = OaCustomflowModelOrg.dao.getById(id);
            o.delete();
        }
    }

}
