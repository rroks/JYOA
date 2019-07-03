package com.pointlion.sys.mvc.admin.oa.customWorkflow;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.admin.oa.common.WorkFlowUtil;
import com.pointlion.sys.mvc.common.model.*;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.mvc.common.utils.UuidUtil;
import com.pointlion.sys.plugin.shiro.ShiroKit;

import java.util.*;

public class OaCustomflowCasenodeService {

    public static final OaCustomflowCasenodeService me = new OaCustomflowCasenodeService();
    public static final String TABLE_NAME = OaCustomflowCasenode.tableName;

    /***
     * 根据主键查询
     */
    public OaCustomflowCasenode getById(String id){
        return OaCustomflowCasenode.dao.findById(id);
    }

    /***
     * 获取分页
     */
    public Page<Record> getPage(int pnum,int psize){
        String sql  = " from "+TABLE_NAME+" o LEFT JOIN act_hi_procinst p ON o.proc_ins_id=p.ID_  where o.userid='"+ShiroKit.getUserId()+"'  order by o.create_time desc";
        return Db.paginate(pnum, psize, " select * ", sql);
    }

    /***
     * 删除
     * @param ids
     */
    @Before(Tx.class)
    public void deleteByIds(String ids){
        String idarr[] = ids.split(",");
        for(String id : idarr){
            OaCustomflowCasenode o = me.getById(id);
            o.delete();
        }
    }
    /***
     * 获取分页
     */
    public List<Record> getNextCaseNode(String casenodeid){
        String sql  = " from oa_customflow_casenode_mapping o   where o.casenodeid='"+casenodeid+"'order by o.create_time desc";
        return Db.find( " select * ", sql);
    }

    /***
     * 获取待办
     */
    public Map<String,List<Record>> getcurrenttask(String userid){
        String sqltable  = "select b.defkey from "+TABLE_NAME+" a left join oa_customflow_case b on a.caseid=b.id  where a.`status`=1 and a.approvaluserid='"+userid+"'order by a.createtime desc";
        Map<String,List<Record>> All = new HashMap<String,List<Record>>();
        List<Record> casenodes =  Db.find(sqltable);
        Iterator IT = casenodes.iterator();
        while(IT.hasNext()){
            Record rd = (Record) IT.next();
            String tablename = WorkFlowUtil.getTablenameByDefkey(rd.get("defkey").toString());
            All.put(rd.get("defkey").toString(),getcurrentbydefkey(tablename,userid));
        }
        return All;
    }

    public List<Record> getcurrentbydefkey(String TABLENAME,String userid){
        String sql  = "select a.id TASKID,b.applyer_name applyer_name,b.org_name org_name,c.name TASKNAME,b.title title,b.create_time create_time from "+TABLE_NAME+" a ,"+TABLENAME+" b,oa_customflow_modelnode c where a.caseid=b.proc_ins_id and a.modelnodeid=c.id and a.`status`=1 and a.approvaluserid='"+userid+"'order by a.createtime desc";
        List<Record> casenodes =  Db.find(sql);
        Iterator IT = casenodes.iterator();
        List<Record> list  = new ArrayList<Record>();
        while(IT.hasNext()){
            Record rd = (Record) IT.next();
            Record map= new Record();
            map.set("applyer_name",String.valueOf(rd.getStr("applyer_name")));
            map.set("org_name",String.valueOf(rd.getStr("org_name")));
            map.set("TASKNAME",String.valueOf(rd.getStr("TASKNAME")));
            map.set("title",String.valueOf(rd.getStr("title")));
            map.set("create_time",String.valueOf(rd.getStr("create_time")));
            map.set("TASKID",String.valueOf(rd.getStr("TASKID")));
            map.set("custom",String.valueOf(1));
            list.add(map);
        }
        return list;
    }
    /***
     * 判断是否还有同级审批节点
     */
    public List<OaCustomflowCasenode> getBotherNodeById(String CaseNodeid,String Caseid){
        OaCustomflowCasenode caseNode = getById(CaseNodeid);
        String sql  = "select * from "+TABLE_NAME+" o   where  o.id<>'"+CaseNodeid+"' and  o.is_commit_casenode =0 and o.status=1 and o.modelnodeid='"+caseNode.getModelnodeid() +"' and o.caseid='"+Caseid +"' order by o.createtime desc";
        return OaCustomflowCasenode.dao.find(sql);
    }

    public List<OaCustomflowCasenode> getSameLevelNodeById(String CaseNodeid,String Caseid){
        OaCustomflowCasenode caseNode = getById(CaseNodeid);
        String sql  = "select * from "+TABLE_NAME+" o   where  o.id <> '"+CaseNodeid+"' and  o.is_commit_casenode = 0 and (o.status = 1 or o.status = 4) and o.modelnodeid = '"+caseNode.getModelnodeid() +"' and o.caseid = '"+Caseid +"' order by o.createtime desc";
        return OaCustomflowCasenode.dao.find(sql);
    }

    public List<OaCustomflowCasenode> getSameLevelNodeByInstanceIdAndModelId(String instanceId, String modelNodeId) {
        String sql = String.format("select * from oa_customflow_casenode where caseid = '%s' and modelnodeid = '%s';", instanceId, modelNodeId);
        return OaCustomflowCasenode.dao.find(sql);
    }

    /***
     * //更新模板节点下所有案例节点状态
     */
    public int updateCaseNodeByModelNodeId(String ModelNodeId,int i){
        String sql  = " update " +TABLE_NAME+" o  set o.status = "+i+" where o.status = 1 and o.modelnodeid ='"+ModelNodeId+"'";
        return Db.update( sql);
    }
    /***
     * //更新案例下所有案例节点状态
     */
    public int updateCaseNodeByCaseId(String CaseId,int i){
        String sql  = " update " + TABLE_NAME+" o  set o.status = "+i+" where  o.caseid ='"+CaseId+"' and o.status = 1";
        return Db.update(sql);
    }
    /***
     * //更新案例所有节点为已完成
     */
    public List<Record> getHisList(String caseId){
        String sql  ="select b.`name` taskName,c.`name` assignee,d.approval_opinion,d.handle_opinion message,a.`status`,d.approval_time endTime from " +TABLE_NAME+" a";
        sql+=" LEFT JOIN oa_customflow_modelnode b on a.modelnodeid=b.id ";
        sql+=" LEFT JOIN sys_user c on a.approvaluserid=c.id ";
        sql+=" LEFT JOIN oa_customflow_comment d on d.casenodeid =a.id ";
        sql+=" where a.is_commit_casenode = 0 and  a.caseid= '" +caseId+ "' order by a.createtime desc ";

        return Db.find(sql);
    }

    /***
     * 更新提交节点为已完成
     */
    public int updateCommitNode(String CaseId){
        String sql  = " update " + TABLE_NAME+" o  set o.status = 0  where  o.is_commit_casenode = 1 and  o.caseid ='"+CaseId+"'";
        return Db.update(sql);
    }
    /***
     * 获取已经办理流程DefKey集合
     */
    public List<String> getHavedoneDefkeyList(String userid){
        List<String> havedoneKeyList = new ArrayList<String>();
        List<Record> list = Db.find("select d.defkey DEFKEY from " + TABLE_NAME+" t ,oa_customflow_case d where d.id=t.caseid and t.status in (2,3.4) and t.approvaluserid='"+userid+"' GROUP BY d.defkey");//所有已办数据类型
        for(Record r:list){
            havedoneKeyList.add(r.getStr("DEFKEY"));
        }
        return havedoneKeyList;
    }
    /***
     * 根据模板节点id获取案例节点
     */
    public List<OaCustomflowCasenode> getcaseNodeBymodelId(String ModelNodeid){
        String sql  = "select * from "+TABLE_NAME+" o   where  o.is_commit_casenode =0 and o.modelnodeid='"+ModelNodeid +"'  order by o.createtime desc";
        return OaCustomflowCasenode.dao.find(sql);
    }
}
