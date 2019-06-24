package com.pointlion.sys.mvc.common.model;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseOaCustomflowComment;
@SuppressWarnings("serial")
public class OaCustomflowComment extends BaseOaCustomflowComment<OaCustomflowComment> {
    public static final OaCustomflowComment dao = new OaCustomflowComment();
    public static final String tableName = "oa_customflow_comment";

    /***
     * 根据主键查询
     */
    public OaCustomflowComment getById(String id){
        return OaCustomflowComment.dao.findById(id);
    }

    /***
     * 删除
     * @param ids
     */
    @Before(Tx.class)
    public void deleteByIds(String ids){
        String idarr[] = ids.split(",");
        for(String id : idarr){
            OaCustomflowComment o = OaCustomflowComment.dao.getById(id);
            o.delete();
        }
    }

}