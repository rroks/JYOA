package com.pointlion.sys.mvc.common.model;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.base.BaseSysTemplate;
@SuppressWarnings("serial")
public class SysTemplate extends BaseSysTemplate<SysTemplate> {
    public static final SysTemplate dao = new SysTemplate();
    public static final String tableName = "sys_template";

    /***
     * 根据主键查询
     */
    public SysTemplate getById(String id){
        return SysTemplate.dao.findById(id);
    }

    /***
     * 删除
     * @param ids
     */
    @Before(Tx.class)
    public void deleteByIds(String ids){
        String idarr[] = ids.split(",");
        for(String id : idarr){
            SysTemplate o = SysTemplate.dao.getById(id);
            o.delete();
        }
    }

}