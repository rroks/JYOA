package com.pointlion.sys.mvc.common.model;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseOaCustomflowType;
@SuppressWarnings("serial")
public class OaCustomflowType extends BaseOaCustomflowType<OaCustomflowType> {
    public static final OaCustomflowType dao = new OaCustomflowType();
    public static final String tableName = "oa_customflow_type";

    /***
     * 根据主键查询
     */
    public OaCustomflowType getById(String id){
        return OaCustomflowType.dao.findById(id);
    }

    /***
     * 删除
     * @param ids
     */
    @Before(Tx.class)
    public void deleteByIds(String ids){
        String idarr[] = ids.split(",");
        for(String id : idarr){
            OaCustomflowType o = OaCustomflowType.dao.getById(id);
            o.delete();
        }
    }

}