package com.pointlion.sys.mvc.mobile.custom.assessment;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.admin.oa.common.OAConstants;
import com.pointlion.sys.mvc.admin.oa.contract.apply.OaContractApplyService;
import com.pointlion.sys.mvc.admin.oa.customWorkflow.OaCustomFlowService;
import com.pointlion.sys.mvc.admin.oa.customWorkflow.OaCustomflowModelnodeService;
import com.pointlion.sys.mvc.admin.oa.customWorkflow.OaCustomflowTypeService;
import com.pointlion.sys.mvc.admin.oa.hrorg.HrOrgService;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.admin.sys.login.LoginValidator;
import com.pointlion.sys.mvc.common.base.BaseController;
import com.pointlion.sys.mvc.common.model.*;
import com.pointlion.sys.mvc.common.utils.Constants;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.mvc.common.utils.UuidUtil;
import com.pointlion.sys.plugin.shiro.ShiroKit;
import com.pointlion.sys.plugin.shiro.ext.CaptchaFormAuthenticationInterceptor;
import com.pointlion.sys.plugin.shiro.ext.CaptchaUsernamePasswordToken;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.util.ThreadContext;
import com.google.common.collect.ImmutableMap;
import java.net.URLDecoder;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.apache.shiro.subject.Subject;

/**
 * @author fen
 */
public class ProcessController extends BaseController {
    private static final OaCustomflowTypeService processTypeService = OaCustomflowTypeService.me;
    private static final OaCustomFlowService customProcessService = OaCustomFlowService.me;
    private static final OaCustomflowModelnodeService customProcessNodeService = OaCustomflowModelnodeService.me;
    private static final OaContractApplyService contractApplicationService = OaContractApplyService.me;
    private static final WorkFlowService workflowService = WorkFlowService.me;

    /**
     * 登陆 OA 系统，用于测试，发行时删除
     */
    @Before({LoginValidator.class, CaptchaFormAuthenticationInterceptor.class})
    public void index() {
        CaptchaUsernamePasswordToken token = this.getAttr("shiroToken");
        Subject subject = SecurityUtils.getSubject();
        ThreadContext.bind(subject);
        if (subject.isAuthenticated()) {
            subject.logout();
        } else {
            subject.login(token);
        }

        SysUser user = SysUser.dao.getByUserNameOrMobile(getPara("username"));
        PasswordService pwdService = new DefaultPasswordService();
        pwdService.passwordsMatch(getPara("password"), user.getPassword());
        renderJson(user);
    }

    /**
     * 获取单类类型表
     */
    public void getTypeByLevel() {
        String typeLevel = getPara("typeLevel");
        String parentTypeId = getPara("parentTypeId");
        List<OaCustomflowType> typeList = processTypeService.getList(Integer.parseInt(typeLevel), parentTypeId);
        renderJson(typeList);
    }

    /**
     * 获取流程模板列表
     */
    public void getAllCustomProcessList() {
        List<OaCustomFlowmodel> list = customProcessService.getlistbyparam(ImmutableMap.of("orgid", ShiroKit.getUserOrgId(), "userId", ShiroKit.getUserId()));
        renderJson(list);
    }

    /**
     * 获取所选审批流程模板信息
     */
    public void getCustomProcessDetail() throws UnsupportedEncodingException {
        String customProcessName = URLDecoder.decode(getPara("customProcessName", ""), "utf-8");

        List<OaCustomFlowmodel> list = customProcessService.getlistbyparam(ImmutableMap.of(
                "modelname", customProcessName,
                "userId", ShiroKit.getUserId()
        ));
        renderJson(list);
    }

    /**
     * 获取审批流程列表
     */
    public void getCustomProcessList() throws UnsupportedEncodingException {
        String currentPage = getPara("pageNum");
        String pageSize = getPara("pageSize");
        Map<String, String> params = new HashMap<>(16);
        params.put("type1", getPara("typeOne"));
        params.put("type2", getPara("typeTwo"));
        params.put("type3", getPara("typeThree"));
        params.put("state", getPara("processUsageStatus"));
        params.put("startTime", getPara("startTime"));
        params.put("endTime", getPara("endTime"));
        params.put("name", URLDecoder.decode(getPara("customProcessName", ""), "UTF-8"));

        params.put("userId", ShiroKit.getUserId());

        Page<Record> page = customProcessService.getPage(Integer.valueOf(currentPage), Integer.valueOf(pageSize), params);
        renderJson(page);
    }

    /**
     * 保存
     */
    public void saveFlow() {
        // 可以直接调用 OaCustomFlowController.save()

    }

    /**
     * 获取编辑页面可变更信息
     */
    public void getEditableCustomProcessDetail() {
        // flow Id
        String flowId = getPara("id");
        OaCustomFlowmodel customFlow = customProcessService.getById(flowId);

        if (StrKit.notBlank(customFlow.getType1())) {
            customFlow.put("typeOneName", processTypeService.getById(customFlow.getType1()).getName());
            if (StrKit.notBlank(customFlow.getType2())) {
                customFlow.put("typeTwoName", processTypeService.getById(customFlow.getType2()).getName());
                if (StrKit.notBlank(customFlow.getType3())) {
                    customFlow.put("typeThreeName", processTypeService.getById(customFlow.getType3()).getName());
                }
            }
        }
        renderJson(customFlow);
    }

    /**
     * 获取审批流程的节点信息
     */
    public void getCustomProcessNodesDetail() {
        // flow Id
        String flowId = getPara("customProcessId");
        List<Record> nodes = customProcessNodeService.getBymodelId(flowId);
        renderJson(nodes);
    }

    /**
     * 删除自定义模板（待确定
     * （应该是一个重复的方法
     */
    public void deleteCustomProcess() {
        customProcessService.deleteById(getParas("customProcessId"));
        renderSuccess("删除成功！");
    }

    /**
     * 提交合同审批
     */
    public void startContractAssessment() {
        String id = getPara("id");
        String defKey = contractApplicationService.getDefKeyByType(getPara("type"));

        OaContractApply contractApplication = OaContractApply.dao.getById(id);
        List<SysUser> list = SysUser.dao.getUserByOrgidAndRoleKey2(contractApplication.getOrgId(), OAConstants.WORKFLOW_ROLE_KEY_OrgLeader);
        contractApplication.setIfSubmit(Constants.IF_SUBMIT_YES);
        String insId = workflowService.startProcess(id, defKey, contractApplication.getTitle(), null);
        contractApplication.setProcInsId(insId);
        contractApplication.update();
        renderSuccess("提交成功");
    }

    /**
     * 获取类别维护列表
     */
    public void getType() {
        Integer currentPage = Integer.valueOf(getPara("pageNum"));
        Integer pageSize = Integer.valueOf(getPara("pageSize"));
        Map<String, String> params = ImmutableMap.of(
                "type1", getPara("typeOne"),
                "type2", getPara("typeTwo"),
                "type3", getPara("typeThree"),
                "name", getPara("typeName")
        );
        Page<Record> page = processTypeService.getPage(currentPage, pageSize, params);
        renderPage(page.getList(), "", page.getTotalRow());
    }

    /**
     * 新增与保存类型
     */
    public void saveType() {
        OaCustomflowType flowType = getModel(OaCustomflowType.class);
        String typeName = getPara("oaCustomflowTypeType.name");
        String typeOne = getPara("oaCustomflowTypeTypeOne");
        String typeTwo = getPara("oaCustomflowTypeTypeTwo");
        flowType.setName(typeName);

        flowType.setCreateUser(ShiroKit.getUserId());
        flowType.setCreateTime(DateUtil.getNowtime());

        flowType.setLevel(1);
        if (StrKit.notBlank(typeOne)) {
            if (!StrKit.notBlank(typeTwo)) {
                flowType.setParent(typeOne);
                flowType.setLevel(2);
            } else {
                flowType.setParent(typeTwo);
                flowType.setLevel(3);
            }
        }

        if (StrKit.notBlank(flowType.getId())) {
            flowType.update();
        } else {
            flowType.setId(UuidUtil.getUUID());
            flowType.setState(0);
            flowType.save();
        }
        renderSuccess(flowType, "OK");
    }

    /**
     * 删除类型
     */
    public void deleteType() {
        String id = getPara("typeId");
        OaCustomflowType o = processTypeService.getById(id);
        if (null == o) {
            renderError("类型不存在！");
        } else {
            boolean flag = processTypeService.deleteById(o);
            if (flag) {
                renderSuccess("删除成功！");
            } else {
                renderError("删除失败！");
            }
        }
    }

    /**
     * 停启用类型
     */
    public void enableType() {
        String typeId = getPara("typeId");
        OaCustomflowType proceeType = processTypeService.getById(typeId);

        if (proceeType.getState() == 1) {
            Map<String, String> params = ImmutableMap.of(
                    "type3", typeId
            );
            if (customProcessService.getlistbyparam(params).size() > 0) {
                renderError("当前类别下游审批流程关联时，不可停用");
            } else {
                proceeType.setState(0);
            }
        } else {
            proceeType.setState(1);
        }
        proceeType.update();
        renderSuccess();
    }

    /**
     * 撤回
     */
    public void withdrawSubmision() {
        String assessmentProcessId = getPara("id");
        OaCustomFlowmodel flowModel = OaCustomFlowmodel.dao.getById(assessmentProcessId);
        flowModel.update();
        renderSuccess("撤回成功");
        // 可能需要 catch 异常
        // renderError("撤回失败");
    }
}
