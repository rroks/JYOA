package com.pointlion.sys.mvc.common.model;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseOaCustomflowCase;
@SuppressWarnings("serial")
public class OaCustomflowCase extends BaseOaCustomflowCase<OaCustomflowCase> {
    public static final OaCustomflowCase dao = new OaCustomflowCase();
    public static final String tableName = "oa_customflow_case";

    /***
     * 根据主键查询
     */
    public OaCustomflowCase getById(String id){
        return OaCustomflowCase.dao.findById(id);
    }

    /***
     * 删除
     * @param ids
     */
    @Before(Tx.class)
    public void deleteByIds(String ids){
        String idarr[] = ids.split(",");
        for(String id : idarr){
            OaCustomflowCase o = OaCustomflowCase.dao.getById(id);
            o.delete();
        }
    }

}