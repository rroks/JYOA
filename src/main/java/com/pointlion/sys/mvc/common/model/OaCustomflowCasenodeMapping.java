package com.pointlion.sys.mvc.common.model;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseOaCustomflowCasenodeMapping;
@SuppressWarnings("serial")
public class OaCustomflowCasenodeMapping extends BaseOaCustomflowCasenodeMapping<OaCustomflowCasenodeMapping> {
    public static final OaCustomflowCasenodeMapping dao = new OaCustomflowCasenodeMapping();
    public static final String tableName = "oa_customflow_casenode_mapping";

    /***
     * 根据主键查询
     */
    public OaCustomflowCasenodeMapping getById(String id){
        return OaCustomflowCasenodeMapping.dao.findById(id);
    }

    /***
     * 删除
     * @param ids
     */
    @Before(Tx.class)
    public void deleteByIds(String ids){
        String idarr[] = ids.split(",");
        for(String id : idarr){
            OaCustomflowCasenodeMapping o = OaCustomflowCasenodeMapping.dao.getById(id);
            o.delete();
        }
    }

}