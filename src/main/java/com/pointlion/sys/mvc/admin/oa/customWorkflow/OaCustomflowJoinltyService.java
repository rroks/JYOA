package com.pointlion.sys.mvc.admin.oa.customWorkflow;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.admin.oa.common.WorkFlowUtil;
import com.pointlion.sys.mvc.common.model.OaCustomflowJoinlty;
import com.pointlion.sys.plugin.shiro.ShiroKit;

import java.util.*;

public class OaCustomflowJoinltyService {
    public static final OaCustomflowJoinltyService me = new OaCustomflowJoinltyService();
    public static final String TABLE_NAME = OaCustomflowJoinlty.tableName;

    /***
     * 根据主键查询
     */
    public OaCustomflowJoinlty getById(String id){
        return OaCustomflowJoinlty.dao.findById(id);
    }

    /***
     * 获取分页
     */
    public Page<Record> getPage(int pnum, int psize){
        String sql  = " from "+TABLE_NAME+" o LEFT JOIN act_hi_procinst p ON o.proc_ins_id=p.ID_  where o.userid='"+ ShiroKit.getUserId()+"'  order by o.create_time desc";
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
            OaCustomflowJoinlty o = me.getById(id);
            o.delete();
        }
    }

    /***
     * 根据主键查询
     */
    public Page<Record> getByParam(int pnum,int psize,String caseid, String acceptuserid, int status){
        String sql = " from "+TABLE_NAME+" o left join sys_user au on o.accept_user=au.id left join sys_user cu on o.create_user=cu.id where 1=1 ";
        if(StrKit.notBlank(caseid)){
            sql +=" and o.caseid ='"+caseid +"'";
        }
        if(StrKit.notBlank(acceptuserid)){
            sql +=" and o.accept_user ='"+acceptuserid +"'";
        }
        if(status!=-1){//-1无状态
            sql +=" and o.status ="+status +"";
        }
        sql+=" order by o.create_time desc";
        return Db.paginate(pnum, psize,"select o.id,cu.name create_user,o.create_time,o.request_content,o.response_content,au.name accept_user,o.response_time,o.status,o.caseid ",sql);
    }
    /***
     * 获取待办
     */
    public Map<String,List<Record>> getcurrenttask(String userid){
        String sqltable  = "select b.defkey from "+TABLE_NAME+" a left join oa_customflow_case b on a.caseid=b.id  where a.`status`=1 and a.accept_user='"+userid+"'order by a.create_time desc";
        Map<String,List<Record>> All = new HashMap<String,List<Record>>();
        List<Record> jointlys =  Db.find(sqltable);
        Iterator IT = jointlys.iterator();
        while(IT.hasNext()){
            Record rd = (Record) IT.next();
            String tablename = WorkFlowUtil.getTablenameByDefkey(rd.get("defkey").toString());
            All.put(rd.get("defkey").toString(),getcurrentbydefkey(tablename,userid));
        }
        return All;
    }
    public List<Record> getcurrentbydefkey(String TABLENAME,String userid){
        String sql  = "select a.id TASKID,b.applyer_name applyer_name,b.org_name org_name,b.title title,a.create_time create_time from "+TABLE_NAME+" a ,"+TABLENAME+" b,oa_customflow_case d where a.caseid=d.id and a.caseid=b.proc_ins_id  and a.`status`=1 and a.accept_user='"+userid+"'order by a.create_time desc";
        List<Record> casenodes =  Db.find(sql);
        Iterator IT = casenodes.iterator();
        List<Record> list  = new ArrayList<Record>();
        while(IT.hasNext()){
            Record rd = (Record) IT.next();
            Record map= new Record();
            map.set("applyer_name",String.valueOf(rd.getStr("applyer_name")));
            map.set("org_name",String.valueOf(rd.getStr("org_name")));
            map.set("TASKNAME",String.valueOf("请求协办"));
            map.set("title",String.valueOf(rd.getStr("title")));
            map.set("create_time",String.valueOf(rd.getStr("create_time")));
            map.set("TASKID",String.valueOf(rd.getStr("TASKID")));
            map.set("custom",String.valueOf(1));
            map.set("joinlty",String.valueOf(1));
            list.add(map);
        }
        return list;
    }
}
