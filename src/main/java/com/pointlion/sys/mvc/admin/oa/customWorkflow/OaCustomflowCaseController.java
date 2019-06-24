package com.pointlion.sys.mvc.admin.oa.customWorkflow;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.json.FastJson;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.interceptor.MainPageTitleInterceptor;
import com.pointlion.sys.mvc.admin.oa.common.WorkFlowUtil;
import com.pointlion.sys.mvc.common.base.BaseController;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.common.model.*;
import com.pointlion.sys.mvc.common.utils.StringUtil;
import com.pointlion.sys.mvc.common.utils.UuidUtil;
import com.pointlion.sys.mvc.common.utils.Constants;
import com.pointlion.sys.mvc.admin.oa.common.OAConstants;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.plugin.shiro.ShiroKit;
import org.apache.commons.lang.StringUtils;

@Before(MainPageTitleInterceptor.class)
public class OaCustomflowCaseController extends BaseController{

    public static final OaCustomflowCaseService service = OaCustomflowCaseService.me;
    public static final OaCustomflowCasenodeService caseNodeService = OaCustomflowCasenodeService.me;
    public static final OaCustomflowModelnodeService modelNodeService = OaCustomflowModelnodeService.me;
    public static final OaCustomflowModelnodeUserService ModelnodeUserService = OaCustomflowModelnodeUserService.me;
    public static final OaCustomflowJoinltyService joinltyService = OaCustomflowJoinltyService.me;
    public static WorkFlowService wfservice = WorkFlowService.me;
    /***
     * 列表页面
     */
    public void getListPage(){
        setBread("流程实例",this.getRequest().getServletPath(),"管理");
        render("list.html");
    }
    /***
     * 审批监控界面
     */
    public void openTaskHisListPage(){
        String caseid = getPara("caseid");
        List<Record> tasklist = caseNodeService.getHisList(caseid);
        OaCustomflowCase o = service.getById(caseid);
        List<Record> nodes = modelNodeService.getBymodelId(o.getModelid());
        setAttr("nodesdata", JSON.toJSONString(nodes)); //模板节点数据
        setAttr("hislist",tasklist);
        setAttr("Currentmodelnodeid",o.getCurrentmodelnodeid());
        render("customTaskView.html");
    }


    /***
     * 退回界面
     */
    public void backPage() throws UnsupportedEncodingException {
        String casenodeid = getPara("casenodeid");
        String buinessid = getPara("buinessid");
        OaCustomflowComment o = getModel(OaCustomflowComment.class);
        OaCustomflowCasenode casenode = caseNodeService.getById(casenodeid);
        OaCustomflowCase Case = service.getById(casenode.getCaseid());
        SysUser user = SysUser.dao.getById(Case.getCreateuser());
        setAttr("commituser", user.getName()); //模板节点数据
        List<Record> nodes = modelNodeService.getBymodelId(Case.getModelid());
        if(StrKit.notBlank(o.getHandleOpinion()))
            o.setHandleOpinion(URLDecoder.decode(o.getHandleOpinion(),"UTF-8"));
        String handle_opinion = getPara("oaCustomflowComment.handle_opinion");
        String sign_address = getPara("oaCustomflowComment.sign_address");
        setAttr("handle_opinion", handle_opinion); //模板节点数据
        setAttr("sign_address", sign_address); //模板节点数据
        setAttr("nodesdata", JSON.toJSONString(nodes)); //模板节点数据
        setAttr("casenodeid", casenodeid); //模板节点数据
        setAttr("buinessid", buinessid); //模板节点数据
        render("back.html");
    }
    /***
     * 退回操作
     */
    @Before(Tx.class)
    public void back(){
        String casenodeid = getPara("casenodeid");
        String approvaltype = getPara("approvaltype");
        String modelnodeid = getPara("modelnodeid");
        String buinessid = getPara("buinessid");
        String handle_opinion = getPara("handle_opinion");
        String sign_address = getPara("sign_address");
        OaCustomflowCasenode casenode = caseNodeService.getById(casenodeid);
        OaCustomflowCase Case = service.getById(casenode.getCaseid());
        if(1!=casenode.getStatus()){
            renderError("已审批不可重复审批！");
            return;
        }
        OaCustomflowComment comment = new OaCustomflowComment();
        comment.setId(UuidUtil.getUUID());
        comment.setApprovalOpinion("[退回]");
        comment.setHandleOpinion(URLDecoder.decode(handle_opinion));
        comment.setCasenodeid(casenodeid);
        comment.setApprovalTime(DateUtil.getTime());
        comment.save();

        if("0".equals(modelnodeid)){//退回提交节点
            Case.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASE_STATE_BACK));
            Case.update();
            service.recallBack(Case.getId(),Case.getDefkey(),buinessid);
        }else{
            if("1".equals(approvaltype)){//退回单人审批节点
                Case.setCurrentmodelnodeid(modelnodeid);
                Case.update();
                //获取节点相关审批人
                List<OaCustomflowModelnodeUser> ModelnodeUserlist = ModelnodeUserService.getBymodelnodeid(modelnodeid);
                Iterator it = ModelnodeUserlist.iterator();
                while(it.hasNext()){
                    OaCustomflowModelnodeUser modelnodeluser = (OaCustomflowModelnodeUser) it.next();
                    //生成下一审批节点
                    OaCustomflowCasenode Casenextnode = new OaCustomflowCasenode();
                    Casenextnode.setId(UuidUtil.getUUID());
                    Casenextnode.setCaseid(Case.getId());
                    Casenextnode.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_RUN));
                    Casenextnode.setModelnodeid(modelnodeid);
                    Casenextnode.setIsCommitCasenode(0);//是否是提交按钮
                    Casenextnode.setApprovaluserid(modelnodeluser.getUserId());
                    Casenextnode.setCreatetime(DateUtil.getTime());
                    Casenextnode.save();
                }
            }
            else if("2".equals(approvaltype)){//退回多人审批节点
                String userlist = getPara("userlist");
                List<String> parseArray = JSONObject.parseArray(userlist,String.class);
                for (String userid:parseArray) {
                    //生成下一审批节点
                    OaCustomflowCasenode Casenextnode = new OaCustomflowCasenode();
                    Casenextnode.setId(UuidUtil.getUUID());
                    Casenextnode.setCaseid(Case.getId());
                    Casenextnode.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_RUN));
                    Casenextnode.setModelnodeid(modelnodeid);
                    Casenextnode.setIsCommitCasenode(0);//是否是提交按钮
                    Casenextnode.setApprovaluserid(userid);
                    Casenextnode.setCreatetime(DateUtil.getTime());
                    Casenextnode.save();
                }
            }
            else if("3".equals(approvaltype)){//退回自定义审批节点
                String customuserid = getPara("customuserid");
                String useridarr[]  = customuserid.split(",");
                for(String userid:useridarr){
                    //生成下一审批节点
                    OaCustomflowCasenode Casenextnode = new OaCustomflowCasenode();
                    Casenextnode.setId(UuidUtil.getUUID());
                    Casenextnode.setCaseid(Case.getId());
                    Casenextnode.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_RUN));
                    Casenextnode.setModelnodeid(modelnodeid);
                    Casenextnode.setIsCommitCasenode(0);//是否是提交按钮
                    Casenextnode.setApprovaluserid(userid);
                    Casenextnode.setCreatetime(DateUtil.getTime());
                    Casenextnode.save();
                }
            }
        }
        casenode.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_BACK));
        casenode.update();
        caseNodeService.updateCaseNodeByModelNodeId(casenode.getModelnodeid(),Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_BACK));
        renderSuccess();


    }
    /***
     * 办理界面
     */

    public void handle(){
        String buinessid = getPara("buinessid");
        String casenodeid = getPara("casenodeid");
        OaCustomflowCasenode casenode = caseNodeService.getById(casenodeid);
        if(casenode.getStatus()!=Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_RUN)){
            renderError("当前流程已审批不可重复审批！");
        }
        OaCustomflowCase Case = service.getById(casenode.getCaseid());
        OaCustomflowModelnode modelnode =  modelNodeService.getById(Case.getCurrentmodelnodeid());
        if(caseNodeService.getBotherNodeById(casenode.getId(),casenode.getCaseid()).size()<1){
            if(StrKit.notBlank(modelnode.getNextmodelnodeid())){
                OaCustomflowModelnode nextModelnode = modelNodeService.getShowmodelnodeById(modelnode.getNextmodelnodeid());
                setAttr("o",nextModelnode);
            }
        }
        setAttr("caseid",Case.getId());
        setAttr("casenodeid",casenodeid);
        setAttr("buinessid",buinessid);
        render("handle.html");
    }
    /***
     * 办理提交
     */
    @Before(Tx.class)
    public void saveComment(){
        String approvalOption = getPara("approval_opinion");
        String buinessid = getPara("buinessid");
        OaCustomflowComment o = getModel(OaCustomflowComment.class);
        String casenodeid = getPara("casenodeid");
        String approvaluserid = getPara("approvaluserid");
        if(StrKit.notBlank(casenodeid)){
            OaCustomflowCasenode casenode =  caseNodeService.getById(casenodeid);
            if(1!=casenode.getStatus()){
                renderError("已审批不可重复审批！");
                return;
            }
            if("agree".equals(approvalOption)){
                //更新审批信息
                if(StrKit.notBlank(o.getId())){
                    o.update();
                }else{
                    o.setId(UuidUtil.getUUID());
                    o.setApprovalOpinion("[同意]");
                    o.setCasenodeid(casenode.getId());
                    o.setApprovalTime(DateUtil.getTime());
                    o.save();
                }
                casenode.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_AGREE));//原案例节点设置已完成
                casenode.update();
                //生成下一级审批
                if(caseNodeService.getBotherNodeById(casenode.getId(),casenode.getCaseid()).size()<1){//没有同级审批时生成下一个流程
                    service.NextProcess(casenode.getId(),approvaluserid,buinessid);
                }

            }else if("disagree".equals(approvalOption)){
                if(StrKit.notBlank(o.getId())){
                    o.update();
                }else{
                    o.setId(UuidUtil.getUUID());
                    o.setApprovalOpinion("[不同意]");
                    o.setCasenodeid(casenode.getId());
                    o.setApprovalTime(DateUtil.getTime());
                    o.save();
                }
                service.DisAgree(casenode.getId(),buinessid);
            }

            renderSuccess();
        }else{
            renderError("案例节点为空！");
        }


    }
    /***
     * 获取分页数据
     **/
    public void listData(){
        String curr = getPara("pageNumber");
        String pageSize = getPara("pageSize");
        Page<Record> page = service.getPage(Integer.valueOf(curr),Integer.valueOf(pageSize));
        renderPage(page.getList(),"",page.getTotalRow());
    }
    /***
     * 保存
     */
    public void save(){
        OaCustomflowCase o = getModel(OaCustomflowCase.class);
        if(StrKit.notBlank(o.getId())){
            o.update();
        }else{
            o.setId(UuidUtil.getUUID());
            o.setCreatetime(DateUtil.getTime());
            o.setCreateuser(ShiroKit.getUserId());
            o.save();
        }
        renderSuccess();
    }

    /***
     * 删除
     * @throws Exception
     */
    public void delete() throws Exception{
        String ids = getPara("ids");
        //执行删除
        service.deleteByIds(ids);
        renderSuccess("删除成功!");
    }

    /***
     * 打开发起协办界面
     */

    public void beginJoinlty(){
        String caseid = getPara("caseid");
        setAttr("caseid",caseid);
        render("launchJointly.html");
    }
    /***
     * 创建协办记录
     */

    public void createJoinlty(){
        OaCustomflowJoinlty o = getModel(OaCustomflowJoinlty.class);
        String type = getPara("type");//请求类型1.协办发起方请求2.协办反馈方请求
        if(StrKit.notBlank(o.getId())&&"2".equals(type)){
            o.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_JOIN_STATE_RESPONSE));
            o.setResponseTime(DateUtil.getTime());
            o.update();
        }else if(StrKit.notBlank(o.getId())&&"1".equals(type)){
            o.update();
        }else{
            o.setId(UuidUtil.getUUID());
            o.setCreateTime(DateUtil.getTime());
            o.setCreateUser(ShiroKit.getUserId());
            o.setRequestId(UuidUtil.getUUID());
            o.setCaseId(getPara("caseid"));
            o.setResponseId(UuidUtil.getUUID());

            o.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_JOIN_STATE_LAUNCH));
            o.save();
        }
        renderSuccess(o,messageSuccess);
    }
    /***
     * 获取协办记录界面
     */

    public void getJoinlty(){
        String caseid= getPara("caseid");
        setAttr("caseid",caseid);
        render("showJoinlty.html");
    }
    /***
     * 获取协办记录
     */

    public void getJoinltyList(){
        String curr = getPara("pageNumber");
        String pageSize = getPara("pageSize");
        String caseid= getPara("caseid");
        Page<Record> page = joinltyService.getByParam(Integer.valueOf(curr),Integer.valueOf(pageSize),caseid,"",-1);
        renderPage(page.getList(),"",page.getTotalRow());
    }

    /***
     * 获取协办反馈界面
     */
    public void getDojoinPage(){
        String id= getPara("id");
        OaCustomflowJoinlty o =joinltyService.getById(id);
        SysUser createuser = SysUser.dao.getById(o.getCreateUser());
        o.put("setCreateUsername",createuser.getName());
        setAttr("o",o);
        setAttr("view",getPara("view"));
        render("responseJointly.html");
    }
    /***
     * 获取归档界面
     */
    public void getCaseFilePage(){
        render("casefile.html");
    }
    /***
     * 获取归档界面
     */
    public void ListCaseFile() throws UnsupportedEncodingException {
        String pageNumber = getPara("pageNumber");
        String pageSize = getPara("pageSize");
        String startTime = getPara("startTime");
        String endTime = getPara("endTime");
        String name = java.net.URLDecoder.decode(getPara("name",""),"UTF-8");
        String number = java.net.URLDecoder.decode(getPara("number",""),"UTF-8");
        Map<String,String> map = new HashMap<>();
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        map.put("name",name);
        Page<Record> page = service.getCaseFile(Integer.valueOf(pageNumber),Integer.valueOf(pageSize),map);
        Iterator<Record> it= page.getList().iterator();
        List<Record> list = new ArrayList<Record>();
        int i=0;
        while(it.hasNext()){
            Record r =  it.next();
            String defkey = String.valueOf(r.get("defkey"));
            if(StrKit.notBlank(defkey)){
                String tablename = WorkFlowUtil.getTablenameByDefkey(defkey);
                String tablesql = "select";
                if(String.valueOf(r.get("defkey")).indexOf("Finance")>=0){
                    tablesql+=" o.finance_num number";
                }
                else if(String.valueOf(r.get("defkey")).indexOf("Contract")>=0){
                    tablesql+=" o.contract_num number";
                }
                //银行卡类型的财务流程
                else if(String.valueOf(r.get("defkey")).indexOf("AccountBank")>=0){
                    tablesql+=" o.bankaccount_num number";
                }
                tablesql+=",o.id businessid,o.create_time from "+tablename+" o where o.proc_ins_id='"+String.valueOf(r.get("id"))+"'";
                Record business = Db.findFirst(tablesql);
                if(business!=null){
                    if(StrKit.notBlank(number)&&StrKit.notBlank(business.getStr("number"))){
                        if(business.getStr("number").indexOf(number)==-1){
                            continue;
                        }
                    }
                    r.set("number",business.getStr("number"));
                    r.set("businessid",business.getStr("businessid"));
                    r.set("create_time",business.getStr("create_time"));
                    i++;
                    list.add(r);
                }

            }
        }
        renderPage(list,"",i);
    }



    /**************************************************************************/
    public void setBread(String name,String url,String nowBread){
        Map<String,String> pageTitleBread = new HashMap<String,String>();
        pageTitleBread.put("pageTitle", name);
        pageTitleBread.put("url", url);
        pageTitleBread.put("nowBread", nowBread);
        this.setAttr("pageTitleBread", pageTitleBread);
    }
}
