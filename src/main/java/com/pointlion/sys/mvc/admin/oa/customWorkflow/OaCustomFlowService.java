package com.pointlion.sys.mvc.admin.oa.customWorkflow;


import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.*;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.mvc.common.utils.UuidUtil;
import com.pointlion.sys.plugin.shiro.ShiroKit;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class OaCustomFlowService {
    public static final OaCustomFlowService me = new OaCustomFlowService();
    public static final String TABLE_NAME = OaCustomFlowmodel.tableName;

    /***
     * 根据主键查询
     */
    public OaCustomFlowmodel getById(String id){
        return OaCustomFlowmodel.dao.findById(id);
    }

    /***
     * 获取分页
     */
    public Page<Record> getPage(int pnum, int psize, Map<String,String> param){
        String sql  = " from "+TABLE_NAME+" o LEFT JOIN sys_user p ON o.create_user=p.id LEFT JOIN oa_customflow_type type ON o.type_3=type.id  where 1=1 ";
        if(StrKit.notBlank(param.get("name"))){
            sql = sql + " and o.name like '%"+param.get("name")+"%' ";
        }if(StrKit.notBlank(param.get("type1"))){
            sql = sql + " and o.type_1 ='"+param.get("type1")+"' ";
        }if(StrKit.notBlank(param.get("type2"))){
            sql = sql + " and o.type_2 ='"+param.get("type2")+"' ";
        }if(StrKit.notBlank(param.get("type3"))){
            sql = sql + " and o.type_3 ='"+param.get("type3")+"' ";
        }if(StrKit.notBlank(param.get("state"))){
            sql = sql + " and o.state ='"+param.get("state")+"' ";
        }if(StrKit.notBlank(param.get("startTime"))){
            sql = sql + "  and o.create_time >= '"+param.get("startTime")+" 00:00:00'";
        }if(StrKit.notBlank(param.get("endTime"))){
            sql = sql + "  and o.create_time <= '"+param.get("endTime")+" 23:59:59'";
        }

        sql += " and (o.id in (select distinct map.modelid from oa_customflow_model_org map where map.orgid in (" + getAuthorizedOrg(param.get("userId")) + ")) ";
        sql += " or o.create_user = '" + param.get("userId") + "') ";

//        if(StrKit.notBlank(param.get("orgid"))){
//            sql+=" and o.id in (select map.modelid from oa_customflow_model_org map where map.orgid ='"+param.get("orgid")+"') ";
//        }
//        if (StrKit.notBlank((param.get("userId")))) {
//            sql += " or o.create_user = '" + param.get("userId") + "' ";
//        }

        sql = sql + "order by o.create_time desc";
        return Db.paginate(pnum, psize, " select o.id,o.name,o.type_1,o.type_2,o.create_time,o.state,o.node_sum,o.execute_sum,o.visible_org,p.name create_user ,type.name type_3", sql);
    }

    /**
     * 判断是否是超级管理员
     */
    public boolean isSuperAdministrator(String userId) {
        List<SysRole> roles = SysRole.dao.getAllRoleByUserid(userId);
        for (SysRole role : roles) {
            if (role.getId().equals("6")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 非管理员用户获取流程列表
     */
    public Page<Record> getCustomProcess(Map<String, String> params) {
        return null;
    }

    /***
     * 删除
     * @param ids
     */
    @Before(Tx.class)
    public void deleteByIds(String ids){
        String idarr[] = ids.split(",");
        for(String id : idarr){
            OaCustomFlowmodel o = me.getById(id);
            o.delete();
        }
    }

    /***
     * 删除
     * @param ids
     */
    @Before(Tx.class)
    public void deleteById(String[] ids){
        for(String id : ids){
            OaCustomFlowmodel o = me.getById(id);
            o.delete();
        }
    }

    /***
     * 删除
     * @param id
     */
    @Before(Tx.class)
    public void deleteById(String id){
        OaCustomFlowmodel o = me.getById(id);
        o.delete();
    }

    /***
     * 根据主键查询
     */
    public void savemodelorg(String modelid,String orgids){
        Db.update("delete from oa_customflow_model_org where modelid = '"+modelid+"' ");
        if(!StrKit.notBlank(orgids)){
            return;
        }
        String orgidarr[]  = orgids.split(",");
        for(String orgid:orgidarr){
            OaCustomflowModelOrg oo = new OaCustomflowModelOrg();
            oo.setId(UuidUtil.getUUID());
            oo.setOrgid(orgid);
            oo.setModelid(modelid);
            oo.setUpdatetime(DateUtil.getTime());
            oo.setUpdateuser(ShiroKit.getUserId());
            oo.save();
        }
    }
    /***
     * 根据主键查询
     */
    public List<OaCustomFlowmodel> getlistbyparam(Map<String,String> param){
        String sql  = "select o.*,p.name create_user from "+TABLE_NAME+" o LEFT JOIN sys_user p ON o.create_user=p.id  where o.state=1 ";
        String type3 = param.get("type3");
        if(StrKit.notBlank(type3)){
            sql+=" and o.type_3 = '"+type3+"'";
        }
        if(StrKit.notBlank(param.get("modelname"))){
            sql+=" and o.name like '%"+param.get("modelname")+"%'";
        }
        if(StrKit.notBlank(param.get("selecttype"))){
            if("myself".equals(param.get("selecttype"))){
                sql += " and o.create_user = '" + ShiroKit.getUserId() + "'";
            }
        }

        sql += " and (o.id in (select distinct map.modelid from oa_customflow_model_org map where map.orgid in (" + getAuthorizedOrg(param.get("userId")) + ")) ";
        sql += " or o.create_user = '" + param.get("userId") + "') ";

//        if(StrKit.notBlank(param.get("orgid"))){
//            sql+=" and (o.id in (select map.modelid from oa_customflow_model_org map where map.orgid ='" + param.get("orgid") + "') ";
//        }
//        if (StrKit.notBlank((param.get("userId")))) {
//            sql += " or o.create_user = '" + param.get("userId") + "') ";
//        }

//        if (!isSuperAdministrator(ShiroKit.getUserId())) {
//            sql+=" and o.id in (select map.modelid from oa_customflow_model_org map where map.orgid ='" + ShiroKit.getUserOrgId() + "') ";
//            sql += " or o.create_user = '" + ShiroKit.getUserId() + "' ";
//        }

        return OaCustomFlowmodel.dao.find(sql);
    }

    private String getAuthorizedOrg(String userId) {
        List<SysHrOrg> tList = SysHrOrg.dao.getCheckOrgByUserid(userId);
        StringJoiner sj = new StringJoiner(",");
        for (SysHrOrg org: tList) {
            sj.add("'" + org.getOrgid() + "'");
        }
        return sj.toString();
    }

    public boolean isInUse(String modelId) {
        String sql = "select * from oa_customflow_case where modelid = '%s' and status in (3,4)";
        return Db.find(String.format(sql, modelId)).isEmpty();
    }
}
