package com.pointlion.sys.mvc.admin.oa.customWorkflow;

import java.io.UnsupportedEncodingException;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.pointlion.sys.interceptor.MainPageTitleInterceptor;
import com.pointlion.sys.mvc.admin.sys.user.SysUserService;
import com.pointlion.sys.mvc.common.base.BaseController;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.common.model.*;
import com.pointlion.sys.mvc.common.utils.StringUtil;
import com.pointlion.sys.mvc.common.utils.UuidUtil;
import com.pointlion.sys.mvc.common.utils.Constants;
import com.pointlion.sys.mvc.admin.oa.common.OAConstants;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.plugin.shiro.ShiroKit;

@Before(MainPageTitleInterceptor.class)
public class OaCustomFlowController extends BaseController {
    public static final OaCustomFlowService service = OaCustomFlowService.me;
    public static final OaCustomflowModelnodeService nodeservice = OaCustomflowModelnodeService.me;
    public static final WorkFlowService wfservice = WorkFlowService.me;
    public static final OaCustomflowTypeService typeService = OaCustomflowTypeService.me;
    /***
     * 列表页面
     */
    public void getListPage(){
        setBread("功能名称",this.getRequest().getServletPath(),"管理");
        List<OaCustomflowType> type1tableList  = typeService.getList(1,null);
        setAttr("type1tableList",type1tableList);
        render("list.html");
    }
    /***
     * 添加模板界面
     */
    public void Add(){
        setBread("功能名称",this.getRequest().getServletPath(),"模板新增");
        SysUser user = SysUser.dao.findById(ShiroKit.getUserId());
        SysOrg org = SysOrg.dao.findById(user.getOrgid());
        setAttr("user", user);
        setAttr("org", org);
        //setAttr("formModelName",StringUtil.toLowerCaseFirstOne(OaCustomFlowmodel.class.getSimpleName()));//模型名称
        List<OaCustomflowType> type1tableList  = typeService.getList(1,null);
        setAttr("type1tableList",type1tableList);
        render("addCustomModel.html");
    }
    /***
     * 获取选择模板页面
     */
    public void getSelectListPage(){
        Map<String,String> map = new HashMap<>();
        map.put("orgid",ShiroKit.getUserOrgId());
        List<OaCustomFlowmodel> list = service.getlistbyparam(map);
        String id = getPara("businessid").toString();
        setAttr("businessid", id);
        setAttr("type", getPara("type"));
        setAttr("data",JSON.toJSONString(list));
        render("select.html");
    }
    /***
     * 选择模板页面ajax更新模板信息
     */
    public void getSelectByAjax() throws UnsupportedEncodingException {
        String modelname = java.net.URLDecoder.decode(getPara("modelname",""),"UTF-8");
        String selecttype = getPara("selecttype");
        Map<String,String> map = new HashMap<>();
        map.put("orgid",ShiroKit.getUserOrgId());
        map.put("modelname",modelname);
        map.put("selecttype",selecttype);
        List<OaCustomFlowmodel> list = service.getlistbyparam(map);
        renderJson(list);
    }

    /***
     * 获取分页数据
     **/
    public void listData() throws UnsupportedEncodingException {
        String curr = getPara("pageNumber");
        String pageSize = getPara("pageSize");
        Map<String,String> param = new HashMap<String,String>();
        param.put("type1",getPara("type1"));
        param.put("type2",getPara("type2"));
        param.put("type3",getPara("type3"));
        param.put("state",getPara("state"));
        param.put("startTime",getPara("startTime"));
        param.put("endTime",getPara("endTime"));
        param.put("name",java.net.URLDecoder.decode(getPara("name",""),"UTF-8"));
        Page<Record> page = service.getPage(Integer.valueOf(curr),Integer.valueOf(pageSize),param);
        renderPage(page.getList(),"",page.getTotalRow());
    }
    /***
     * 保存
     */
    public void save() {
        OaCustomFlowmodel o = getModel(OaCustomFlowmodel.class);
        List<OaCustomflowModelnode> nodes = getModels(OaCustomflowModelnode.class);
        if (StrKit.notBlank(o.getId())) {
            OaCustomFlowmodel data = service.getById(o.getId());
            o.update();
            if (StrKit.notBlank(o.getVisibleOrg()) && !data.getVisibleOrg().equals(o.getVisibleOrg()))// 判断是否修改了可见部门
                service.savemodelorg(o.getId(), o.getVisibleOrg());
        } else {
            o.setId(UuidUtil.getUUID());
            String state = getPara("type");
            if (StrKit.notBlank(state) && "1".equals(state))
                o.setState(1);
            else if ("2".equals(state))
                o.setState(2);
            o.setNodeSum(nodes.size());
            o.setCreateTime(DateUtil.getTime());
            o.setCreateUser(ShiroKit.getUserId());
            o.save();
            service.savemodelorg(o.getId(), o.getVisibleOrg());
        }
        ListIterator<OaCustomflowModelnode> iterator = nodes.listIterator(nodes.size());
        int i=nodes.size();
        String nextid ="";//下一节点id
        //节点新增处理
        while (iterator.hasPrevious()){//反向循环
            OaCustomflowModelnode node = iterator.previous();
            String  Approvaluserid = node.getApprovaluserid();
            if(StrKit.notBlank(node.getId())){
                node.setSequence(i);
                node.setNextmodelnodeid(nextid);
                node.update();
                nextid = node.getId();
                i--;
            }else{
                node.setId(UuidUtil.getUUID());
                node.setModelid(o.getId());
                node.setSequence(i);
                node.setNextmodelnodeid(nextid);
                node.save();
                nextid = node.getId();
                i--;
            }
            nodeservice.savenodeApprovaluser(node.getId(),Approvaluserid);//直接重新更新吧
        }

        /*for (int i = nodes.size(); i >0 ; i--)
        {
            OaCustomflowModelnode node = nodes.get(i);
            String  Approvaluserid = node.getApprovaluserid();
            if(StrKit.notBlank(node.getId())){
                node.setSequence(i);
                node.update();
            }else{
                node.setId(UuidUtil.getUUID());
                nodesetModelid(o.getId());
                node.setSequence(i);
                node.save();
            }
            nodeservice.savenodeApprovaluser(node.getId(),Approvaluserid);//直接重新更新吧
        }*/

        renderSuccess();
    }

    /**
     * 停启用自定义审批流程
     * @author Fen
     */
    public void enableCustomProcess() {
        String processId = getPara("customProcessId");
        OaCustomFlowmodel customProcess = service.getById(processId);
        int processStatus = Integer.valueOf(getPara("status"));
        customProcess.setState(processStatus);
        customProcess.update();
        renderSuccess(customProcess.getState().toString());
    }

    /***
     * 获取编辑页面
     */
    public void getEditPage(){
        setBread("功能名称",this.getRequest().getServletPath().substring(0,this.getRequest().getServletPath().lastIndexOf("/")+1)+"getListPage","功能名称");
        String id = getPara("id");
        String view = getPara("view");
        setAttr("view", view);
        if(StrKit.notBlank(id)){//修改
            OaCustomFlowmodel o = service.getById(id);
            setAttr("o", o);
        }else{
            SysUser user = SysUser.dao.findById(ShiroKit.getUserId());
            SysOrg org = SysOrg.dao.findById(user.getOrgid());
            setAttr("user", user);
            setAttr("org", org);
        }
        setAttr("formModelName",StringUtil.toLowerCaseFirstOne(OaCustomFlowmodel.class.getSimpleName()));//模型名称
        List<Record> nodes = nodeservice.getBymodelId(id);
        setAttr("nodesdata", JSON.toJSONString(nodes)); //模板节点数据
        List<OaCustomflowType> type1tableList  = typeService.getList(1,null);
        setAttr("type1tableList",type1tableList);


        OaCustomFlowmodel o = service.getById(id);
        //加载类型选择值
        if(StrKit.notBlank(o.getType1())){
            o.put("type1name",typeService.getById(o.getType1()).getName());
        }
        if(StrKit.notBlank(o.getType2())){
            o.put("type2name",typeService.getById(o.getType2()).getName());
        }
        if(StrKit.notBlank(o.getType3())){
            o.put("type3name",typeService.getById(o.getType3()).getName());
        }
        if("detail".equals(view)){

            String  CreateUserid = o.getCreateUser();
            o.setCreateUser(SysUser.dao.getById(CreateUserid).getName());
            int state = o.getState();
            //流程模板状态（0.停用，1.启用，2.草稿）
            if(0==state)
                o.put("statename","停用");
            else if(0==state)
                o.put("statename","启用");
            else
                o.put("statename","草稿");

        }
        setAttr("o", o);
        render("edit.html");
    }
    /***
     * 选择模板页面ajax更新节点信息
     */
    public void getModelByAjax(){
        String id = getPara("modelId");
        Map<String,Object> map = new HashMap<>();
        if(StrKit.notBlank(id)){//修改
            OaCustomFlowmodel o = service.getById(id);
            List<Record> nodes = nodeservice.getBymodelId(id);
            map.put("nodesdata", JSON.toJSONString(nodes));
            String  CreateUserid = o.getCreateUser();
            if(StrKit.notBlank(CreateUserid)){
                o.setCreateUser(SysUser.dao.getById(CreateUserid).getName());
            }
            int state = o.getState();
            //流程模板状态（0.停用，1.启用，2.草稿）
            if(0==state)
                o.put("statename","停用");
            else if(1==state)
                o.put("statename","启用");
            else
                o.put("statename","草稿");
            if(StrKit.notBlank(o.getType1())){
                o.put("type1name",typeService.getById(o.getType1()).getName());
            }
            if(StrKit.notBlank(o.getType2())){
                o.put("type2name",typeService.getById(o.getType2()).getName());
            }
            if(StrKit.notBlank(o.getType3())){
                o.put("type3name",typeService.getById(o.getType3()).getName());
            }


            map.put("o", o);
            renderJson(map);
        }
        else{
            renderError("不存在该模板！");
        }
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
     * 获取类别维护列表页面
     */
    public void gettypeListPage(){
        setBread("类别维护",this.getRequest().getServletPath(),"类别列表");
        List<OaCustomflowType> type1tableList  = typeService.getList(1,null);
        setAttr("type1tableList",type1tableList);
        render("typelist.html");
    }
    /***
     * 获取类别维护分页数据
     **/
    public void typelistData() throws UnsupportedEncodingException {
        String curr = getPara("pageNumber");
        String pageSize = getPara("pageSize");
        Map<String,String> param = new HashMap<String,String>();
        param.put("type1",getPara("type1")==null?"":getPara("type1"));
        param.put("type2",getPara("type2")==null?"":getPara("type2"));
        param.put("type3",getPara("type3")==null?"":getPara("type3"));
        param.put("name",java.net.URLDecoder.decode(getPara("name",""),"UTF-8"));
        Page<Record> page = typeService.getPage(Integer.valueOf(curr),Integer.valueOf(pageSize),param);
        renderPage(page.getList(),"",page.getTotalRow());
    }

    /***
     * 获取类别维护分页数据
     **/
    public void typelistbyAjax(){
        String parentid = getPara("parentId");
        List<OaCustomflowType> typetableList = typeService.getList(0,parentid);
        renderJson(typetableList);
    }
    /***
     * 获取类别维护编辑页面
     */
    public void gettypeEditPage(){
        String id = getPara("id");
        if(StrKit.notBlank(id)){//修改
            OaCustomflowType o = typeService.getById(id);
           if(o.getLevel()==2){
                OaCustomflowType parent =   typeService.getById(o.getParent());
               setAttr("frist",parent.getId());
            }
            else if(o.getLevel()==3){
                OaCustomflowType parent =   typeService.getById(o.getParent());
                OaCustomflowType grandpa =   typeService.getById(parent.getParent());
               setAttr("second",parent.getId());
               setAttr("frist",grandpa.getId());
               List<OaCustomflowType> type2tableList  = typeService.getList(0,grandpa.getId());
               setAttr("type2tableList",type2tableList);
            }
            setAttr("o", o);
        }else{
            SysUser user = SysUser.dao.findById(ShiroKit.getUserId());
            SysOrg org = SysOrg.dao.findById(user.getOrgid());
            setAttr("user", user);
            setAttr("org", org);
        }
        setAttr("formModelName",StringUtil.toLowerCaseFirstOne(OaCustomflowType.class.getSimpleName()));//模型名称
        List<OaCustomflowType> type1tableList  = typeService.getList(1,null);
        setAttr("type1tableList",type1tableList);
        render("typeEdit.html");
    }
    /***
     * 类别维护保存操作
     */
        public void typesave(){
            OaCustomflowType o = getModel(OaCustomflowType.class);
            String type1 = "none".equals(getPara("oaCustomflowTypetype1"))?"":getPara("oaCustomflowTypetype1");
            String type2 = "none".equals(getPara("oaCustomflowTypetype2"))?"":getPara("oaCustomflowTypetype2");
            if(StrKit.notBlank(o.getId())){
                OaCustomFlowmodel data =service.getById(o.getId());
                o.setLevel(1);
                if(StrKit.notBlank(type1)){
                    if(StrKit.notBlank(type2)){
                        o.setParent(type2);
                        o.setLevel(3);
                    }else{
                        o.setParent(type1);
                        o.setLevel(2);
                    }
                }
                o.setCreateUser(ShiroKit.getUserId());
                o.setCreateTime(DateUtil.getNowtime());
                o.update();
            }else{
                o.setId(UuidUtil.getUUID());
                o.setLevel(1);
                if(StrKit.notBlank(type1)){
                    if(StrKit.notBlank(type2)){
                        o.setParent(type2);
                        o.setLevel(3);
                    }else{
                        o.setParent(type1);
                        o.setLevel(2);
                    }
                }
                o.setCreateTime(DateUtil.getTime());
                o.setState(0);
                o.setCreateUser(ShiroKit.getUserId());
                o.save();
            }
            renderSuccess();
        }
    /***
     * 停启用类型
     */
    public void startstop(){
        String id = getPara("id");
        OaCustomflowType o = typeService.getById(id);

        if(o.getState()==1){

            Map<String,String> map = new HashMap<>();
            map.put("type3",id);
            if(service.getlistbyparam(map).size()>0){
                renderError("当前类别下有审批流程关联时，不可停用");
            }else{
                o.setState(0);
                o.update();
                renderSuccess();
            }
        }else{
            o.setState(1);
            o.update();
            renderSuccess();
        }

    }


    /***
     * 提交
     */
    public void startProcess(){
        String id = getPara("id");
        OaCustomFlowmodel o = OaCustomFlowmodel.dao.getById(id);
        o.update();
        renderSuccess("提交成功");
    }
    /***
     * 撤回
     */
    public void callBack(){
        String id = getPara("id");
        try{
            OaCustomFlowmodel o = OaCustomFlowmodel.dao.getById(id);
           /* wfservice.callBack(o.getProcInsId());
            o.setIfSubmit(Constants.IF_SUBMIT_NO);
            o.setProcInsId("");*/
            o.update();
            renderSuccess("撤回成功");
        }catch(Exception e){
            e.printStackTrace();
            renderError("撤回失败");
        }
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
