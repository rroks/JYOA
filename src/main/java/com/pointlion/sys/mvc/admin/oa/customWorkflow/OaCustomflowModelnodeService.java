package com.pointlion.sys.mvc.admin.oa.customWorkflow;


import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.model.OaCustomflowModelnode;
import com.pointlion.sys.mvc.common.model.OaCustomflowModelnodeUser;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.mvc.common.utils.UuidUtil;
import com.pointlion.sys.plugin.shiro.ShiroKit;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OaCustomflowModelnodeService{
    public static final OaCustomflowModelnodeService me = new OaCustomflowModelnodeService();
    public static final String TABLE_NAME = OaCustomflowModelnode.tableName;

    /***
     * 根据主键查询
     */
    public OaCustomflowModelnode getById(String id){
        return OaCustomflowModelnode.dao.findById(id);
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
            OaCustomflowModelnode o = me.getById(id);
            o.delete();
        }
    }
    /***
     * 根据主键查询
     */
    public List<Record> getBymodelId(String modelId){
        String sql  = "select o.* from "+TABLE_NAME+" o  where o.modelid='"+modelId+"'  order by o.sequence asc";
        String approvalusersql  = "select o.user_id userid,u.name username from oa_customflow_modelnode_user o left join sys_user u on o.user_id=u.id where o.modelnodeid='";
        List<Record> nodes = Db.find(sql);
        for(int i=0;i<nodes.size();i++) {
            Record node = nodes.get(i);
            String nodeid = node.get("id");
            List<String> nameList = new ArrayList<String>();
            List<String> userList = new ArrayList<String>();
            //获取节点所有审批人的中文字符串
            if("3".equals(node.get("approvaltype"))){
                node.set("approvalname", "");
            }
            if(StrKit.notBlank(nodeid)&&!"3".equals(node.get("approvaltype"))){
                List<Record> userArr = Db.find(approvalusersql+nodeid+"'");
                for(Record user:userArr){
                    nameList.add(user.getStr("username"));
                    StringBuffer map = new StringBuffer();
                    map.append(user.getStr("userid"));
                    map.append(",");
                    map.append(user.getStr("username"));
                    userList.add(map.toString());
                }
                node.set("approvalname", StringUtils.join(nameList,","));
                node.set("userList", userList);
            }
            //转换审批类型为字符串供前台展示 节点审批类型(1.单人审批，2.多人审批，3.自定义审批)
            int approvaltype = node.get("approvaltype");
            if(1==approvaltype){
                node.set("approvaltype", "单人审批");
            }else if(2==approvaltype){
                node.set("approvaltype", "多人审批");
            }else{
                node.set("approvaltype", "自定义审批");
            }
        }
        return nodes;
    }

    /***
     * 根据主键查询
     */
    public void savenodeApprovaluser(String modelnodeid,String userids){
        Db.update("delete from oa_customflow_modelnode_user where modelnodeid = '"+modelnodeid+"' ");
        if(!StrKit.notBlank(userids))
            return;
        String useridarr[]  = userids.split(",");
        for(String userid:useridarr){
            OaCustomflowModelnodeUser cc = new OaCustomflowModelnodeUser();
            cc.setId(UuidUtil.getUUID());
            cc.setModelnodeid(modelnodeid);
            cc.setCreateTime(DateUtil.getTime());
            cc.setUserId(userid);
            cc.save();
        }
    }
    public List<OaCustomflowModelnode> getBymodelId2(String modelId){
        String sql  = "select o.* from "+TABLE_NAME+" o  where o.modelid='"+modelId+"'  order by o.sequence asc";
        String approvalusersql  = "select o.user_id,u.name username from oa_customflow_modelnode_user o left join sys_user u on o.user_id=u.id where o.modelnodeid='";
        List<OaCustomflowModelnode> nodes = OaCustomflowModelnode.dao.find(sql);
        return nodes;
    }
    /***
     * 根据主键查询
     */
    public OaCustomflowModelnode getShowmodelnodeById(String id){
        OaCustomflowModelnode o = OaCustomflowModelnode.dao.findById(id);
        String approvalusersql  = "select o.user_id,u.name username from oa_customflow_modelnode_user o left join sys_user u on o.user_id=u.id where o.modelnodeid='";
        String nodeid = o.getId();
        int approvaltype =  o.getApprovaltype();
        List<String> nameList = new ArrayList<String>();
        //获取节点所有审批人的中文字符串
        if("3".equals(approvaltype)){
            o.put("approvalname", "");
        }
        if(StrKit.notBlank(nodeid)&&!"3".equals(approvaltype)){
            List<Record> userArr = Db.find(approvalusersql+nodeid+"'");
            for(Record user:userArr){
                nameList.add(user.getStr("username"));
            }
            o.put("approvalname", StringUtils.join(nameList,","));
        }
        //转换审批类型为字符串供前台展示 节点审批类型(1.单人审批，2.多人审批，3.自定义审批)
        if(1==approvaltype){
            o.put("approvaltypename", "单人审批");
        }else if(2==approvaltype){
            o.put("approvaltypename", "多人审批");
        }else{
            o.put("approvaltypename", "自定义审批");
        }
        return o;
    }
}
