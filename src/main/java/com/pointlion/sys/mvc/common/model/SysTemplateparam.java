package com.pointlion.sys.mvc.common.model;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseSysTemplateparam;
@SuppressWarnings("serial")
public class SysTemplateparam extends BaseSysTemplateparam<SysTemplateparam> {
    public static final SysTemplateparam dao = new SysTemplateparam();
    public static final String tableName = "sys_templateparam";

    /***
     * 根据主键查询
     */
    public SysTemplateparam getById(String id){
        return SysTemplateparam.dao.findById(id);
    }

    /***
     * 删除
     * @param ids
     */
    @Before(Tx.class)
    public void deleteByIds(String ids){
        String idarr[] = ids.split(",");
        for(String id : idarr){
            SysTemplateparam o = SysTemplateparam.dao.getById(id);
            o.delete();
        }
    }

}