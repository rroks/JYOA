package com.pointlion.sys.mvc.admin.oa.customWorkflow;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.admin.oa.common.OAConstants;
import com.pointlion.sys.mvc.admin.oa.common.WorkFlowUtil;
import com.pointlion.sys.mvc.common.model.*;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.mvc.common.utils.UuidUtil;
import com.pointlion.sys.plugin.shiro.ShiroKit;
//import org.springframework.security.access.method.P;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OaCustomflowCaseService {
    public static final OaCustomflowCaseService me = new OaCustomflowCaseService();
    public static final OaCustomflowModelnodeService modelnodeService = new OaCustomflowModelnodeService();
    public static final OaCustomflowModelnodeUserService ModelnodeUserService = new OaCustomflowModelnodeUserService();
    public static final OaCustomflowCasenodeService casenodeService = new OaCustomflowCasenodeService();
    public static final String TABLE_NAME = OaCustomflowCase.tableName;

    /***
     * 根据主键查询
     */
    public OaCustomflowCase getById(String id){
        return OaCustomflowCase.dao.findById(id);
    }

    /***
     * 获取分页
     */
    public Page<Record> getPage(int pnum,int psize){
        String sql  = " from "+TABLE_NAME+" o LEFT JOIN act_hi_procinst p ON o.id=p.ID_  where o.createuser='"+ShiroKit.getUserId()+"'  order by o.createtime desc";
        return Db.paginate(pnum, psize, " select * ", sql);
    }

    /***
     * 获取信息
     */
    public List<OaCustomflowCase> getList(Map<String,String> param){
        String sql  = " from "+TABLE_NAME+" o where 1=1 ";
        if(StrKit.notBlank(param.get(""))){
            sql+=" and o. = '"+"'";
        }
        sql+="order by o.create_time desc";
        return OaCustomflowCase.dao.find( " select * ", sql);
    }

    /***
     * 删除
     * @param ids
     */
    @Before(Tx.class)
    public void deleteByIds(String ids){
        String idarr[] = ids.split(",");
        for(String id : idarr){
            OaCustomflowCase o = me.getById(id);
            o.delete();
        }
    }

    /**
     * 删除
     * @author fen
     */
    @Before(Tx.class)
    public boolean deleteById(String id) {
        OaCustomflowCase o = me.getById(id);
        if (null != o) {
            o.delete();
            return null == me.getById(id);
        } else {
            return false;
        }

    }

    /***
     * 启动流程
     * @param modelid
     */
    @Before(Tx.class)
    public String startProcess(String approvaluserid,String ProcInsId,String businessid,String modelid,String tablename,String defkey,String SelectFileid){
        OaCustomflowCase oldCase  = OaCustomflowCase.dao.findById(ProcInsId);
        if(oldCase!=null){
            //获取流程模板
            OaCustomFlowmodel model = OaCustomFlowmodel.dao.getById(oldCase.getModelid());
            //获取流程模板节点
            List<OaCustomflowModelnode> nodes =  modelnodeService.getBymodelId2(oldCase.getModelid());
            //更新提交节点为完成状态
            casenodeService.updateCommitNode(ProcInsId);
            //获取第一个模板节点相关信息
            OaCustomflowModelnode modelnode = nodes.get(0);
            //更新案例实例状态
            oldCase.setCurrentmodelnodeid(modelnode.getId());
            oldCase.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_RUN));//设置开始状态
            oldCase.update();
            //获取节点相关审批人
            if(modelnode.getApprovaltype()==3) {//如果为自定义审批获取原先选择的第一级审批人
                List<OaCustomflowCasenode> caselist =  casenodeService.getcaseNodeBymodelId(modelnode.getId());
                Iterator it = caselist.iterator();
                while (it.hasNext()) {
                    OaCustomflowCasenode Casenode = (OaCustomflowCasenode) it.next();
                    //生成下一审批节点
                    OaCustomflowCasenode Casenextnode = new OaCustomflowCasenode();
                    Casenextnode.setId(UuidUtil.getUUID());
                    Casenextnode.setCaseid(oldCase.getId());
                    Casenextnode.setModelnodeid(modelnode.getId());
                    Casenextnode.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_RUN));
                    Casenextnode.setIsCommitCasenode(0);//是否是提交按钮
                    Casenextnode.setApprovaluserid(Casenode.getApprovaluserid());
                    Casenextnode.setCreatetime(DateUtil.getTime());
                    Casenextnode.save();
                }
            }else {
                List<OaCustomflowModelnodeUser> ModelnodeUserlist = ModelnodeUserService.getBymodelnodeid(modelnode.getId());
                Iterator it = ModelnodeUserlist.iterator();
                while (it.hasNext()) {
                    OaCustomflowModelnodeUser modelnodeluser = (OaCustomflowModelnodeUser) it.next();
                    //生成下一审批节点
                    OaCustomflowCasenode Casenextnode = new OaCustomflowCasenode();
                    Casenextnode.setId(UuidUtil.getUUID());
                    Casenextnode.setCaseid(oldCase.getId());
                    Casenextnode.setModelnodeid(modelnode.getId());
                    Casenextnode.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_RUN));
                    Casenextnode.setIsCommitCasenode(0);//是否是提交按钮
                    Casenextnode.setApprovaluserid(modelnodeluser.getUserId());
                    Casenextnode.setCreatetime(DateUtil.getTime());
                    Casenextnode.save();
                }
            }
            return oldCase.getId();
        }else{
            //生成流程实例
            OaCustomflowCase Case = new OaCustomflowCase();
            Case.setId(UuidUtil.getUUID());
            Case.setModelid(modelid);
            Case.setTablename(tablename);
            Case.setDefkey(defkey);
            Case.setCreatetime(DateUtil.getTime());
            Case.setCreateuser(ShiroKit.getUserId());
            Case.setFileUser(SelectFileid);

            //获取流程模板
            OaCustomFlowmodel model = OaCustomFlowmodel.dao.getById(modelid);
            model.setExecuteSum(model.getExecuteSum()+1);
            model.update();

            //获取流程模板节点
            List<OaCustomflowModelnode> nodes =  modelnodeService.getBymodelId2(modelid);


            //提交后生成第一个提交节点
            OaCustomflowCasenode Casenode = new OaCustomflowCasenode();
            Casenode.setId(UuidUtil.getUUID());
            Casenode.setCaseid(Case.getId());
            Casenode.setIsCommitCasenode(1);//是否是提交按钮
            Casenode.setCreatetime(DateUtil.getTime());
            Casenode.setStatus(0);
            Casenode.save();

            //获取第一个模板节点相关信息
            OaCustomflowModelnode modelnode = nodes.get(0);
            //更新案例实例状态
            Case.setCurrentmodelnodeid(modelnode.getId());
            Case.setStatus(1);//设置开始状态
            Case.save();
            //获取节点相关审批人
            if(modelnode.getApprovaltype()==3){//如果为自定义审批
                if(StrKit.isBlank(approvaluserid)){
                    return "";
                }
                String approvalusers[] = approvaluserid.split(",");
                for(String userid : approvalusers){
                    //生成下一审批节点
                    OaCustomflowCasenode Casenextnode = new OaCustomflowCasenode();
                    Casenextnode.setId(UuidUtil.getUUID());
                    Casenextnode.setCaseid(Case.getId());
                    Casenextnode.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_RUN));
                    Casenextnode.setModelnodeid(modelnode.getId());
                    Casenextnode.setIsCommitCasenode(0);//是否是提交按钮
                    Casenextnode.setApprovaluserid(userid);
                    Casenextnode.setCreatetime(DateUtil.getTime());
                    Casenextnode.save();
                }
            }
            else {
                List<OaCustomflowModelnodeUser> ModelnodeUserlist = ModelnodeUserService.getBymodelnodeid(modelnode.getId());
                Iterator it = ModelnodeUserlist.iterator();
                while (it.hasNext()) {
                    OaCustomflowModelnodeUser modelnodeluser = (OaCustomflowModelnodeUser) it.next();
                    //生成下一审批节点
                    OaCustomflowCasenode Casenextnode = new OaCustomflowCasenode();
                    Casenextnode.setId(UuidUtil.getUUID());
                    Casenextnode.setCaseid(Case.getId());
                    Casenextnode.setModelnodeid(modelnode.getId());
                    Casenextnode.setIsCommitCasenode(0);//是否是提交按钮
                    Casenextnode.setApprovaluserid(modelnodeluser.getUserId());
                    Casenextnode.setCreatetime(DateUtil.getTime());
                    Casenextnode.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_RUN));
                    Casenextnode.save();
                }
            }
            return Case.getId();
        }

    }

    /***
     * 下一流程
     * @param casenodeid
     */
    @Before(Tx.class)
    public void NextProcess(String casenodeid,String approvaluserid,String buinessid){
        OaCustomflowCasenode casenode = casenodeService.getById(casenodeid);
        OaCustomflowCase Case = getById(casenode.getCaseid());
        OaCustomflowModelnode modelnode = modelnodeService.getById(casenode.getModelnodeid());
        if(StrKit.isBlank(modelnode.getNextmodelnodeid())){
            Case.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASE_STATE_AGREE));//设置结束状态
            Case.setCurrentmodelnodeid("");
            Case.setFileTime(DateUtil.getTime());
            Case.update();
            String tablename = WorkFlowUtil.getTablenameByDefkey(Case.getDefkey());//更新完成状态和同意状态
            String sql = "update "+tablename +" t set t.if_complete='1',if_agree ='1' where t.id='"+buinessid+"'";
            Db.update(sql);
            return;
        }
        OaCustomflowModelnode nextModelnode = modelnodeService.getById(modelnode.getNextmodelnodeid());
        //更新案例实例状态
        casenode.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_AGREE));//原案例节点设置已完成
        casenode.update();
        Case.setCurrentmodelnodeid(nextModelnode.getId());
        Case.update();
        //获取节点相关审批人
        if(nextModelnode.getApprovaltype()==3){//如果为自定义审批
            if(StrKit.isBlank(approvaluserid)){
                return;
            }
            String approvalusers[] = approvaluserid.split(",");
            for(String userid : approvalusers){
                //生成下一审批节点
                OaCustomflowCasenode Casenextnode = new OaCustomflowCasenode();
                Casenextnode.setId(UuidUtil.getUUID());
                Casenextnode.setCaseid(Case.getId());
                Casenextnode.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_RUN));
                Casenextnode.setModelnodeid(nextModelnode.getId());
                Casenextnode.setIsCommitCasenode(0);//是否是提交按钮
                Casenextnode.setApprovaluserid(userid);
                Casenextnode.setCreatetime(DateUtil.getTime());
                Casenextnode.save();
            }
        }else{
            List<OaCustomflowModelnodeUser> ModelnodeUserlist = ModelnodeUserService.getBymodelnodeid(nextModelnode.getId());
            Iterator it = ModelnodeUserlist.iterator();
            while(it.hasNext()){
                OaCustomflowModelnodeUser modelnodeluser = (OaCustomflowModelnodeUser) it.next();
                //生成下一审批节点
                OaCustomflowCasenode Casenextnode = new OaCustomflowCasenode();
                Casenextnode.setId(UuidUtil.getUUID());
                Casenextnode.setCaseid(Case.getId());
                Casenextnode.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_RUN));
                Casenextnode.setModelnodeid(nextModelnode.getId());
                Casenextnode.setIsCommitCasenode(0);//是否是提交按钮
                Casenextnode.setApprovaluserid(modelnodeluser.getUserId());
                Casenextnode.setCreatetime(DateUtil.getTime());
                Casenextnode.save();
            }
        }
    }
    /***
     * 不同意
     * @param casenodeid
     */
    @Before(Tx.class)
    public void DisAgree(String casenodeid,String buinessid){
        OaCustomflowCasenode casenode = casenodeService.getById(casenodeid);
        OaCustomflowCase Case = getById(casenode.getCaseid());
        //设置案例流程不同意
        Case.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASE_STATE_DISAGREE));//设置不同意状态
        Case.update();
        String tablename = WorkFlowUtil.getTablenameByDefkey(Case.getDefkey());
        String sql = "update "+tablename +" t set t.if_complete='1',if_agree ='2' where t.id='"+buinessid+"'";
        Db.update(sql);
        casenodeService.updateCaseNodeByModelNodeId(casenode.getModelnodeid(),Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_DISAGREE));
    }

    /***
     * 撤回流程
     */
    @Before(Tx.class)
    public void callBack(String procid){
        OaCustomflowCase Case = getById(procid);
        Case.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASE_STATE_RECALL));
        Case.update();
        casenodeService.updateCaseNodeByCaseId(procid,Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_RECALL));
    }
    /***
     * 退回起点
     */
    @Before(Tx.class)
    public void recallBack(String procid,String defkey,String buinessid){
        OaCustomflowCase CASE = getById(procid);
        CASE.setCurrentmodelnodeid("");//更新案例当前案例节点
        CASE.update();
        //提交后生成第一个提交节点
        OaCustomflowCasenode Casenode = new OaCustomflowCasenode();
        Casenode.setId(UuidUtil.getUUID());
        Casenode.setCaseid(procid);
        Casenode.setIsCommitCasenode(1);//是否是提交按钮
        Casenode.setCreatetime(DateUtil.getTime());
        Casenode.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_RUN));
        Casenode.save();
        OaCustomflowCase Case = getById(Casenode.getCaseid());
        String tablename = WorkFlowUtil.getTablenameByDefkey(Case.getDefkey());
        String sql = "update "+tablename +" t set t.if_submit='0' where t.id='"+buinessid+"'";
        Db.update(sql);
    }
    /***
     * 获取固定流程待办任务对象
     */
    public Record getTaskObject(String taskid,String defKey){
        String tableName = WorkFlowUtil.getTablenameByDefkey(defKey);
        Record o = Db.findFirst("select o.id buinessid,t.id taskid,o.*,t.* from "+tableName+" o , oa_customflow_casenode t where t.caseid = o.proc_ins_id and t.id='"+taskid+"'");
        return o;
    }
    /**单张表查询任务
     * @param tableName
     * @param userId
     * @param ifComplete
     * @return
     */
    public List<Record> getApplyBykeySingle(String tableName, String userId, String ifComplete){
        String totalSql = "";
        String sql = "select b.id,b.type,p.id as defid, b.proc_ins_id, b.create_time as createtime, b.applyer_name ,b.org_name, b.title, b.if_submit, b.if_complete, b.if_agree,b.if_customflow,p.defkey from "+tableName+" b left join "+TABLE_NAME+" p on p.id = b.proc_ins_id  where 1=1";
        if(StrKit.notBlank(userId)){
            sql = sql + " and b.userid='"+userId+"'and b.if_submit='1' and b.if_customflow='1' and  p.status<>4";
        }
        if(StrKit.notBlank(ifComplete)){
            sql = sql + " and b.if_complete='"+ifComplete+"'";
        }
        String end = " order by createtime desc";
        return Db.find(sql+end);
    }
    /***
     * 获取已办分页
     * --首页和管理页面使用
     */
    public Page<Record> getHavedonePage(int pnum,int psize,String defkey,String userid,String sqlEXT){
        String tableName = WorkFlowUtil.getTablenameByDefkey(defkey);
        if(StrKit.isBlank(tableName)){
            tableName = OaApplyCustom.tableName;
        }
        String sql = "FROM "+tableName+" o, ( SELECT DISTINCT p.id FROM  " + TABLE_NAME+" p, oa_customflow_casenode t WHERE t.approvaluserid='"+userid+"' AND t.caseid = p.id AND p.defkey = '"+defkey+"' AND  t.status in (2,3.4) ) tt WHERE tt.id = o.proc_ins_id ";
//		String sql  = " from "+tableName+" o , (select BUSINESS_KEY_,d.ID_ defid from act_hi_identitylink i,act_hi_procinst p,act_re_procdef d where i.TYPE_='participant' and p.ID_=i.PROC_INST_ID_ and p.PROC_DEF_ID_=d.ID_ and d.KEY_='"+defkey+"' and i.USER_ID_='"+username+"') tt where tt.BUSINESS_KEY_=o.id";
        if(StrKit.notBlank(sqlEXT)){
            sql = sql + sqlEXT;
        }
        sql = sql +" order by o.create_time desc";
        return Db.paginate(pnum, psize, " select o.* ", sql);
    }
    /***
     * 查询抄送给我的
     * --管理页面使用
     */
    public Page<Record> getFlowCCPage(int pnum,int psize,String key ,String userid,String sqlEXT){
        String tableName = WorkFlowUtil.getTablenameByDefkey(key);
        if(StrKit.isBlank(tableName)){
            tableName = OaApplyCustom.tableName;
        }
        String sql = " from "+tableName+" o, oa_flow_carbon_c  cc, oa_customflow_case p  where o.proc_ins_id=p.id and cc.business_id = o.id and cc.user_id='"+userid+"' and cc.defkey='"+key+"' ";
        if(StrKit.notBlank(sqlEXT)){
            sql = sql + sqlEXT;
        }
        sql = sql +" order by o.create_time desc";
        return Db.paginate(pnum, psize, "select o.*,p.id defid  ",sql);
    }
    /***
     * 查询某人的待办条目
     * --管理页面使用
     */
    public Page<Record> getToDoPageByKey(int pnum,int psize,String key ,String userid,String sqlEXT){
        String tableName = WorkFlowUtil.getTablenameByDefkey(key);
        String sql = " from " + TABLE_NAME+" p ,"+tableName+" o,oa_customflow_casenode t where o.proc_ins_id=p.id and t.caseid=o.proc_ins_id ";
        if(StrKit.notBlank(key)){
            sql = sql + "and  p.DEFKEY='"+key+"'";
        }
        if(StrKit.notBlank(userid)){
            sql = sql + " and (t.approvaluserid='"+userid+"')";
        }
        if(StrKit.notBlank(sqlEXT)){
            sql = sql + sqlEXT;
        }
        sql = sql +" order by o.create_time desc";
        return Db.paginate(pnum, psize, "select *,p.id defid  ",sql);
    }
    /***
     * 查询某组织的申请
     * --管理页面使用
     */
    public Page<Record> getApplyPageByKeyAndOrgid(int pnum,int psize,String key ,String orgids,String type, String ifSubmit,String sqlEXT){
        String tableName = WorkFlowUtil.getTablenameByDefkey(key);
        String sql = " from "+tableName+" o,oa_customflow_case p where o.proc_ins_id=p.id and o.type='"+type+"' and if_submit = '"+ifSubmit+"'";
        if(StrKit.notBlank(orgids)){
            if(StrKit.isBlank(orgids.substring(1))){
                sql = sql + " and org_id ="+orgids+"";
            }else{
                sql = sql + " and org_id in("+ orgids +")";
            }
        }
        if(StrKit.notBlank(sqlEXT)){
            sql = sql + sqlEXT;
        }

        sql = sql +" order by o.create_time desc";
        return Db.paginate(pnum, psize, "select *,p.id defid  ",sql);
    }

    /**单张表查询任务
     * @param param
     * @return
     */
    public Page<Record> getCaseFile(int pnum,int psize, Map<String,String> param){
        String sql = " from oa_customflow_case o left join sys_user cu on o.createuser=cu.id LEFT JOIN sys_user fu on o.fileuser=fu.id where 1=1 ";
        if(StrKit.notBlank(param.get("name"))){//des\title\name\custom_name\custom_contact_person\custom_contact_moble
            sql += "cu.`name` like '%"+param.get("name")+"%'";
        }
        if(StrKit.notBlank(param.get("startTime"))){
            sql = sql + "  and o.filetime >= '"+param.get("startTime")+" 00:00:00'";
        }
        if(StrKit.notBlank(param.get("endTime"))){
            sql = sql + "  and o.filetime <= '"+param.get("endTime")+" 23:59:59'";
        }
        String end = " order by createtime desc";
        Page<Record> page = Db.paginate(pnum, psize,"select o.id,o.defkey,cu.`name` createuser,o.createtime,fu.`name` fileuser,o.filetime ",sql+end);
        return page;
    }
    /***
     * 查询某归档的查看权限
     * --管理页面使用
     */
    public Page<Record> getfileview(int pnum,int psize,String key ,String userid,String type, String ifSubmit,String sqlEXT){
        String tableName = WorkFlowUtil.getTablenameByDefkey(key);
        String sql = " from "+tableName+" o,oa_customflow_case p where o.proc_ins_id=p.id  and p.fileuser='"+userid+"'";
        if(StrKit.notBlank(sqlEXT)){
            sql = sql + sqlEXT;
        }

        sql = sql +" order by o.create_time desc";
        return Db.paginate(pnum, psize, "select *,p.id defid  ",sql);
    }
}
