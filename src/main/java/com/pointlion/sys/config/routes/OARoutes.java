package com.pointlion.sys.config.routes;

import com.jfinal.config.Routes;
import com.pointlion.sys.mvc.admin.oa.apply.bankaccount.OaApplyBankAccountController;
import com.pointlion.sys.mvc.admin.oa.apply.businesscard.OaApplyBusinessCardController;
import com.pointlion.sys.mvc.admin.oa.apply.buy.OaApplyBuyController;
import com.pointlion.sys.mvc.admin.oa.apply.cost.OaApplyCostController;
import com.pointlion.sys.mvc.admin.oa.apply.custom.OaApplyCustomController;
import com.pointlion.sys.mvc.admin.oa.apply.finance.OaApplyFinanceController;
import com.pointlion.sys.mvc.admin.oa.apply.gift.OaApplyGiftController;
import com.pointlion.sys.mvc.admin.oa.apply.hotel.OaApplyHotelController;
import com.pointlion.sys.mvc.admin.oa.apply.leave.OaApplyLeaveController;
import com.pointlion.sys.mvc.admin.oa.apply.meetroom.OaApplyMeetroomController;
import com.pointlion.sys.mvc.admin.oa.apply.officeobject.OaApplyOfficeObjectController;
import com.pointlion.sys.mvc.admin.oa.apply.resget.OaResGetController;
import com.pointlion.sys.mvc.admin.oa.apply.seal.OaApplySealController;
import com.pointlion.sys.mvc.admin.oa.apply.trainticket.OaApplyTrainTicketController;
import com.pointlion.sys.mvc.admin.oa.apply.usecar.OaApplyUseCarController;
import com.pointlion.sys.mvc.admin.oa.apply.usercarwork.OaApplyUsercarWorkController;
import com.pointlion.sys.mvc.admin.oa.apply.userchangestation.OaApplyUserChangeStationController;
import com.pointlion.sys.mvc.admin.oa.apply.userdimission.OaApplyUserDimissionController;
import com.pointlion.sys.mvc.admin.oa.apply.userregular.OaApplyUserRegularController;
import com.pointlion.sys.mvc.admin.oa.bumph.BumphController;
import com.pointlion.sys.mvc.admin.oa.common.CommonBusinessController;
import com.pointlion.sys.mvc.admin.oa.common.CommonFlowController;
import com.pointlion.sys.mvc.admin.oa.contract.OaContractController;
import com.pointlion.sys.mvc.admin.oa.contract.apply.OaContractApplyController;
import com.pointlion.sys.mvc.admin.oa.contract.change.OaContractChangeController;
import com.pointlion.sys.mvc.admin.oa.contract.invoice.OaContractInvoiceController;
import com.pointlion.sys.mvc.admin.oa.contract.pay.OaContractPayController;
import com.pointlion.sys.mvc.admin.oa.contract.stop.OaContractStopController;
import com.pointlion.sys.mvc.admin.oa.customWorkflow.OaCustomFlowController;
import com.pointlion.sys.mvc.admin.oa.customWorkflow.OaCustomflowCaseController;
import com.pointlion.sys.mvc.admin.oa.dct.resourcecar.DctResourceCarController;
import com.pointlion.sys.mvc.admin.oa.dct.resourcemeetroom.DctResourceMeetroomController;
import com.pointlion.sys.mvc.admin.oa.hrorg.HrOrgController;
import com.pointlion.sys.mvc.admin.oa.notice.NoticeController;
import com.pointlion.sys.mvc.admin.oa.permissiongroup.OaPermissionGroupController;
import com.pointlion.sys.mvc.admin.oa.project.OaProjectController;
import com.pointlion.sys.mvc.admin.oa.project.build.OaProjectBuildController;
import com.pointlion.sys.mvc.admin.oa.project.changemember.OaProjectChangeMemberController;
import com.pointlion.sys.mvc.admin.oa.project.expressconfirm.OaProjectExpressConfirmController;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowController;
import com.pointlion.sys.mvc.admin.oa.workflow.main.StencilsetRestResource;
import com.pointlion.sys.mvc.admin.oa.workflow.model.ModelController;
import com.pointlion.sys.mvc.admin.oa.workflow.model.ModelEditorJsonRestResource;
import com.pointlion.sys.mvc.admin.oa.workflow.model.ModelSaveRestResource;
import com.pointlion.sys.mvc.admin.oa.workflow.rest.ProcessDefinitionDiagramLayoutResource;
import com.pointlion.sys.mvc.admin.oa.workflow.rest.ProcessInstanceDiagramLayoutResource;
import com.pointlion.sys.mvc.admin.oa.workflow.rest.ProcessInstanceHighlightsResource;

public class OARoutes extends Routes{

	@Override
	public void config() {
		setBaseViewPath("/WEB-INF/admin/oa");
		//通用业务控制器
		add("/admin/oa/common/business",CommonBusinessController.class);
		//流程图
		add("/admin/oa/model",ModelController.class,"/workflow/model");//工作流-模型
		add("/admin/oa/workflow",WorkFlowController.class,"/workflow");//工作流
		/***在线办公****/
		add("/admin/oa/notice",NoticeController.class,"/notice");//通知公告
		//公文
		add("/admin/oa/bumph",BumphController.class,"/bumph");//公文管理---内部发文，收文转发
		//项目管理
		add("/admin/oa/project/build",OaProjectBuildController.class,"/project/build");//项目立项
		add("/admin/oa/project/changemember",OaProjectChangeMemberController.class,"/project/changemember");//项目成员变更
		add("/admin/oa/project/expressconfirm",OaProjectExpressConfirmController.class,"/project/expressconfirm");
		add("/admin/oa/project",OaProjectController.class,"/project");//项目管理
		//合同管理
		add("/admin/oa/contract",OaContractController.class,"/contract");//合同管理
		add("/admin/oa/contract/apply",OaContractApplyController.class,"/contract/apply");//合同申请
		add("/admin/oa/contract/change",OaContractChangeController.class,"/contract/change");//合同变更
		add("/admin/oa/contract/invoice",OaContractInvoiceController.class,"/contract/invoice");//合同开票
		add("/admin/oa/contract/pay",OaContractPayController.class,"/contract/pay");//合同付款
		add("/admin/oa/contract/stop",OaContractStopController.class,"/contract/stop");//合同终止
		//在线办公---申请
		add("/admin/oa/commonFlow",CommonFlowController.class,"/common");//处理通用任务
		add("/admin/oa/apply/businesscard",OaApplyBusinessCardController.class,"/apply/businesscard");//名片印刷申请
		add("/admin/oa/apply/buy",OaApplyBuyController.class,"/apply/buy");//采购申请
		add("/admin/oa/apply/custom",OaApplyCustomController.class,"/apply/custom");//自定义流程
		add("/admin/oa/apply/leave",OaApplyLeaveController.class,"/apply/leave");//请销假
		add("/admin/oa/apply/usecar",OaApplyUseCarController.class,"/apply/usecar");//用车申请
		add("/admin/oa/apply/resget",OaResGetController.class,"/apply/resget");//物品领用
		add("/admin/oa/apply/hotel",OaApplyHotelController.class,"/apply/hotel");//宾馆预定申请
		add("/admin/oa/apply/officeobject",OaApplyOfficeObjectController.class,"/apply/officeobject");//办公用品申请
		add("/admin/oa/apply/seal",OaApplySealController.class,"/apply/seal");//用章申请
		add("/admin/oa/apply/trainticket",OaApplyTrainTicketController.class,"/apply/trainticket");//车船票申请
		add("/admin/oa/apply/usercarwork",OaApplyUsercarWorkController.class,"/apply/usercarwork");//私车公用补助申请
		add("/admin/oa/apply/gift",OaApplyGiftController.class,"/apply/gift");//礼品申请
		add("/admin/oa/apply/meetroom",OaApplyMeetroomController.class,"/apply/meetroom");//会议室申请
		add("/admin/oa/apply/userchangestation",OaApplyUserChangeStationController.class,"/apply/userchangestation");//调岗申请
		add("/admin/oa/apply/userdimission",OaApplyUserDimissionController.class,"/apply/userdimission");//离职申请
		add("/admin/oa/apply/userregular",OaApplyUserRegularController.class,"/apply/userregular");//转正申请
		add("/admin/oa/apply/cost",OaApplyCostController.class,"/apply/cost");//转正申请
		add("/admin/oa/apply/finance",OaApplyFinanceController.class,"/apply/finance");//财务类申请
		add("/admin/oa/apply/bankaccount",OaApplyBankAccountController.class,"/apply/bankaccount");//银行卡开销户申请
		//主数据管理
		add("/admin/oa/dct/resourcecar",DctResourceCarController.class,"/dct/resourcecar");//车辆信息管理
		add("/admin/oa/dct/resourcemeetroom",DctResourceMeetroomController.class,"/dct/resourcemeetroom");//会议室管理
		//流程在线编辑器和流程跟踪所用路由
		add("/admin/oa/process-instance/highlights",ProcessInstanceHighlightsResource.class);//modeler
		add("/admin/oa/process-instance/diagram-layout",ProcessInstanceDiagramLayoutResource.class);//modeler
		add("/admin/oa/process-definition/diagram-layout",ProcessDefinitionDiagramLayoutResource.class);//modeler
		add("/admin/oa/modelEditor/save",ModelSaveRestResource.class);
		add("/admin/oa/modelEditor/json",ModelEditorJsonRestResource.class);
		add("/admin/oa/editor/stencilset",StencilsetRestResource.class);
		//oa数据权限
		add("/admin/oa/dataPermission",OaPermissionGroupController.class,"/dataPermission");
		
		//人事权限
		add("/admin/oa/hrorg",HrOrgController.class,"/hrorg");

		//自定义审批
		add("/admin/oa/CustomFlow", OaCustomFlowController.class,"/customWorkflow");
		add("/admin/oa/CustomFlow/Case", OaCustomflowCaseController.class,"/customWorkflow");
	}

}
