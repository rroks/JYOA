package com.pointlion.sys.mvc.mobile.custom.assessment;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.admin.oa.common.OAConstants;
import com.pointlion.sys.mvc.admin.oa.common.WorkFlowUtil;
import com.pointlion.sys.mvc.admin.oa.customWorkflow.OaCustomflowCaseService;
import com.pointlion.sys.mvc.admin.oa.customWorkflow.OaCustomflowCasenodeService;
import com.pointlion.sys.mvc.admin.oa.customWorkflow.OaCustomflowJoinltyService;
import com.pointlion.sys.mvc.admin.oa.customWorkflow.OaCustomflowModelnodeService;
import com.pointlion.sys.mvc.common.base.BaseController;
import com.pointlion.sys.mvc.common.model.*;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.mvc.common.utils.UuidUtil;
import com.pointlion.sys.plugin.shiro.ShiroKit;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author fen
 */
public class ProcessInstanceController extends BaseController {
    static final OaCustomflowCaseService flowCaseService = OaCustomflowCaseService.me;
    static final OaCustomflowCasenodeService flowCaseNodeService = OaCustomflowCasenodeService.me;
    static final OaCustomflowModelnodeService flowNodeService = OaCustomflowModelnodeService.me;
    public static final OaCustomflowJoinltyService assessmentNodeAssistService = OaCustomflowJoinltyService.me;

    /**
     * 审批监控界面
     */
    public void getAssessmentHistory() {
        String processInstanceId = getPara("processInstanceId");
        List<Record> assessmenHistory = flowCaseNodeService.getHisList(processInstanceId);
        OaCustomflowCase processInstance = flowCaseService.getById(processInstanceId);
        List<Record> processNodes = flowNodeService.getBymodelId(processInstance.getModelid());

        JSONArray jsonArray = new JSONArray();
        // 审批历史
        jsonArray.add(assessmenHistory);
        // 审批节点
        jsonArray.add(processNodes);
        // 当前节点ID
        jsonArray.add(processInstance.getCurrentmodelnodeid());
        renderJson(jsonArray);
    }

    /**
     * 载入退回页面所需数据
     * 获得退回节点
     */
    public void getReturnPageData() throws UnsupportedEncodingException {
        String processInstanceNodeId = getPara("processInstanceNodeId");
        OaCustomflowCasenode instanceNode = flowCaseNodeService.getById(processInstanceNodeId);
        OaCustomflowCase processInstance = flowCaseService.getById(instanceNode.getCaseid());
        List<Record> nodes = flowNodeService.getBymodelId(processInstance.getModelid());
        renderJson(nodes);
    }

    /**
     * 执行退回
     */
    @Before(Tx.class)
    public void doReturn() throws UnsupportedEncodingException {
        String transactionId = getPara("transactionId");
        String instanceNodeId = getPara("instanceNodeId");
        String processNodeId = getPara("processNodeId");
//        String sign = getPara("sign");

        OaCustomflowCasenode instanceNode = flowCaseNodeService.getById(instanceNodeId);
        OaCustomflowCase flowInstance = flowCaseService.getById(instanceNode.getCaseid());
        if (1 != instanceNode.getStatus()) {
            renderError("不可重复审批！");
        }

        OaCustomflowComment comment = getModel(OaCustomflowComment.class);
        String assessmentComment = URLDecoder.decode(getPara("oaCustomflowComment.handle_opinion", ""), "utf-8");
        if (StrKit.notBlank(assessmentComment)) {
            comment.setHandleOpinion(assessmentComment);
        }
        comment.setId(UuidUtil.getUUID());
        comment.setApprovalOpinion("[退回]");
        comment.setCasenodeid(instanceNodeId);
        comment.setApprovalTime(DateUtil.getTime());
        comment.save();

        // why node id instead of node sequence
        if ("0".equals(processNodeId)) {
            flowInstance.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASE_STATE_BACK));
            flowInstance.update();
            flowCaseService.recallBack(flowInstance.getId(), flowInstance.getDefkey(), transactionId);
        } else {
            String[] returnTartgetUserIds = getParas("returnTargetUserIds");
            for (String userId : returnTartgetUserIds) {
                OaCustomflowCasenode nextInstanceNode = new OaCustomflowCasenode();
                nextInstanceNode.setId(UuidUtil.getUUID());
                nextInstanceNode.setCaseid(flowInstance.getId());
                nextInstanceNode.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_RUN));
                nextInstanceNode.setModelnodeid(processNodeId);
                // 是否是提交按钮
                nextInstanceNode.setIsCommitCasenode(0);
                nextInstanceNode.setApprovaluserid(userId);
                nextInstanceNode.setCreatetime(DateUtil.getTime());
                nextInstanceNode.save();
            }
        }
        instanceNode.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_BACK));
        instanceNode.update();
        flowCaseNodeService.updateCaseNodeByModelNodeId(instanceNode.getModelnodeid(), Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_BACK));
        renderSuccess();
    }

    /**
     * 办理界面
     * 事实上只需要获取NEXT PROCESS NODE
     */
    public void handleAssessment() {
        String instanceNodeId = getPara("instanceNodeId");
        OaCustomflowCasenode instanceNode = flowCaseNodeService.getById(instanceNodeId);
        if (Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_RUN).equals(instanceNode.getStatus())) {
            renderError("当前节点已审批！");
        }
        OaCustomflowCase flowInstance = flowCaseService.getById(instanceNode.getCaseid());
        OaCustomflowModelnode flowNode = flowNodeService.getById(flowInstance.getCurrentmodelnodeid());
        JSONObject jsonObject = new JSONObject();
        if (flowCaseNodeService.getBotherNodeById(instanceNode.getId(), instanceNode.getCaseid()).size() >= 1
                && StrKit.notBlank(flowNode.getNextmodelnodeid())) {
            OaCustomflowModelnode nextFlowNode = flowNodeService.getShowmodelnodeById(flowNode.getNextmodelnodeid());
            jsonObject.put("nextProcessNode", nextFlowNode);
        }
        renderJson(jsonObject);
    }

    /**
     * 获取下一个流程模型节点
     */
    public OaCustomflowModelnode getNextProcessNode(String instanceNodeId) {
        OaCustomflowCasenode instanceNode = flowCaseNodeService.getById(instanceNodeId);
        OaCustomflowCase processInstance = flowCaseService.getById(instanceNode.getCaseid());
        OaCustomflowModelnode processNode = flowNodeService.getById(processInstance.getCurrentmodelnodeid());
        if (flowCaseNodeService.getBotherNodeById(instanceNode.getId(), instanceNode.getCaseid()).size() < 1
                && StrKit.notBlank(processNode.getNextmodelnodeid())) {
            return flowNodeService.getShowmodelnodeById(processNode.getNextmodelnodeid());
        }
        return null;
    }

    /**
     * 提交办理
     */
    @Before(Tx.class)
    public void submitAssessment() {
        String assessmentResult = getPara("assessmentResult");
        String transactionId = getPara("transactionId");
        String instanceNodeId = getPara("instanceNodeId");
        String assessorId = getPara("assessorId");
        OaCustomflowComment comment = getModel(OaCustomflowComment.class);

        if (StrKit.notBlank(instanceNodeId)) {
            OaCustomflowCasenode instanceNode = flowCaseNodeService.getById(instanceNodeId);
            if (1 != instanceNode.getStatus()) {
                renderError("不可重复审批！");
            }

            if (StrKit.notBlank(comment.getId())) {
                comment.update();
            } else {
                comment.setId(UuidUtil.getUUID());
                comment.setApprovalOpinion("agree".equals(assessmentResult) ? "[同意]" : "[不同意]");
                comment.setCasenodeid(instanceNode.getId());
                comment.setApprovalTime(DateUtil.getTime());
                comment.save();
            }

            if ("agree".equals(assessmentResult)) {
                instanceNode.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_CASENODE_STATE_AGREE));
                instanceNode.update();
                // 下一个审批节点
                if (flowCaseNodeService.getBotherNodeById(instanceNode.getId(), instanceNode.getCaseid()).size() < 1) {
                    flowCaseService.NextProcess(instanceNode.getId(), assessorId, transactionId);
                }
            } else if ("disagree".equals(assessmentResult)) {
                flowCaseService.DisAgree(instanceNode.getId(), transactionId);
            }

            renderSuccess();
        } else {
            renderError("空的案例节点！");
        }
    }

    /**
     * 获取合同实例分页数据
     */
    public void getContractList() {
        Integer currentPage = Integer.valueOf(getPara("pageNum"));
        Integer pageSize = Integer.valueOf(getPara("pageSize"));
        Page<Record> page = flowCaseService.getPage(currentPage, pageSize);
        renderJson(page);
    }

    /**
     * 新建/保存合同实例
     * 可以调用 OaCustomflowCaseController.save()
     */
    public void saveCtractInstanceCustomFlow() {
        OaCustomflowCase contractInstanceCustomFlow = getModel(OaCustomflowCase.class);
        if (StrKit.notBlank(contractInstanceCustomFlow.getId())) {
            contractInstanceCustomFlow.update();
        } else {
            contractInstanceCustomFlow.setId(UuidUtil.getUUID());
            contractInstanceCustomFlow.setCreatetime(DateUtil.getTime());
            contractInstanceCustomFlow.setCreateuser(ShiroKit.getUserId());
            contractInstanceCustomFlow.save();
        }
        renderSuccess();
    }

    /**
     * 删除合同实例自定义审批（批量）
     */
    public void deleteContractInstanceCustomFlow() {
        String[] contractInstanceCustomFlowIds = getParas("contractInstanceCustomFlowId");
        ArrayList<String> failedDeletes = new ArrayList<>();
        for (String id : contractInstanceCustomFlowIds) {
            if (flowCaseService.deleteById(id)) {
                failedDeletes.add(id);
            }
        }
        if (failedDeletes.size() > 0) {
            renderError(failedDeletes, "无法删除审批");
        } else {
            renderSuccess("删除成功");
        }
    }

    /**
     * 获得发起协办页面所需数据
     * 只需要获取协办人名单（/admin/sys/user/openSelectOneUserPage
     * UserController
     */
    public void loadCreateAssistData() {
        // TODO
    }

    /**
     * 获取协办人名单
     */
    public void getAssistant() {
        // TODO
    }

    /**
     * 创建协办记录/进行协办
     */
    public void createAssist() {
        OaCustomflowJoinlty customAssist = getModel(OaCustomflowJoinlty.class);
        String assistType = getPara("assistType");
        if (StrKit.notBlank(customAssist.getId())) {
            if ("2".equals(assistType)) {
                customAssist.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_JOIN_STATE_RESPONSE));
                customAssist.setResponseTime(DateUtil.getTime());
            }
            customAssist.update();
        } else {
            customAssist.setId(UuidUtil.getUUID());
            customAssist.setCreateTime(DateUtil.getTime());
            customAssist.setCreateUser(ShiroKit.getUserId());
            customAssist.setRequestId(UuidUtil.getUUID());
            customAssist.setCaseId(getPara("contractInstanceId"));
            customAssist.setResponseId(UuidUtil.getUUID());
            customAssist.setStatus(Integer.valueOf(OAConstants.OA_CUSTOM_JOIN_STATE_LAUNCH));
            customAssist.save();
        }
        renderSuccess(customAssist, messageSuccess);
    }

    /**
     * 获取协办记录列表
     */
    public void getAssisstList() {
        Integer currentPage = Integer.valueOf(getPara("pageNumber"));
        Integer pageSize = Integer.valueOf(getPara("pageSize"));
        String caseid= getPara("contractInstanceId");
        Page<Record> page = assessmentNodeAssistService.getByParam(currentPage, pageSize, caseid,"",-1);
        renderPage(page.getList(),"",page.getTotalRow());
    }

    /**
     * 获取协办反馈
     */
    public void getAssisstFeedback() {
        String assistId = getPara("assistId");
        OaCustomflowJoinlty assist = assessmentNodeAssistService.getById(assistId);
        SysUser assistInitiator = SysUser.dao.getById(assist.getCreateUser());
        assist.put("setCreateUsername", assistInitiator.getName());
        renderJson(assist);
    }

    /**
     * 获取归档
     */
    public void getArchive() throws UnsupportedEncodingException {
        Integer pageNumber = Integer.valueOf(getPara("pageNumber"));
        Integer pageSize = Integer.valueOf(getPara("pageSize"));
        String createTime = getPara("startTime");
        String endTime = getPara("endTime");
        String name = URLDecoder.decode(getPara("name"), "utf-8");
        String number = URLDecoder.decode(getPara("number"), "utf-8");
        Map<String, String> map = ImmutableMap.of(
                "startTIme", createTime,
                "endTime", endTime,
                "name", name
        );
        Page<Record> page = flowCaseService.getCaseFile(pageNumber, pageSize, map);
        Iterator<Record> iterator = page.getList().iterator();
        List<Record> list = new ArrayList<>();
        while (iterator.hasNext()) {
            Record record = iterator.next();
            String defKey = String.valueOf(record.get("defkey"));
            if (StrKit.notBlank(defKey)) {
                String dbTable = WorkFlowUtil.getTablenameByDefkey(defKey);
                String sql = "select ";
                if (defKey.contains("Finance")) {
                    sql += "finance_num number, ";
                } else if (defKey.contains("Contract")) {
                    sql += "contract_num number, ";
                } else if (defKey.contains("AccountBank")) {
                    sql += "bankaccount_num number, ";
                }
                sql += "id businessid, create_time from " + dbTable + " where proc_ins_id = '" + String.valueOf(record.get("id")) + "'";
                Record contract = Db.findFirst(sql);
                if (null != contract) {
                    String contractNumber = contract.getStr("number");
                    if (StrKit.notBlank(number) && StrKit.notBlank(contractNumber)) {
                        if (contractNumber.contains(number)) {
                            continue;
                        }
                    }

                    record.set("number", contractNumber);
                    record.set("businessid", contract.getStr("businessid"));
                    record.set("create_time", contract.getStr("create_time"));
                    list.add(record);
                }
            }
        }
        renderJson(list);
    }

}
