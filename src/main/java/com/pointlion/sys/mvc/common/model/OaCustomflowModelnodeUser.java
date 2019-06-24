package com.pointlion.sys.mvc.common.model;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseOaCustomflowModelnodeUser;
@SuppressWarnings("serial")
public class OaCustomflowModelnodeUser extends BaseOaCustomflowModelnodeUser<OaCustomflowModelnodeUser> {
    public static final OaCustomflowModelnodeUser dao = new OaCustomflowModelnodeUser();
    public static final String tableName = "oa_customflow_modelnode_user";

    /***
     * 根据主键查询
     */
    public OaCustomflowModelnodeUser getById(String id){
        return OaCustomflowModelnodeUser.dao.findById(id);
    }

    /***
     * 删除
     * @param ids
     */
    @Before(Tx.class)
    public void deleteByIds(String ids){
        String idarr[] = ids.split(",");
        for(String id : idarr){
            OaCustomflowModelnodeUser o = OaCustomflowModelnodeUser.dao.getById(id);
            o.delete();
        }
    }

}