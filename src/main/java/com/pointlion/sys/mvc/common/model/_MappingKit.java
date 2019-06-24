
package com.pointlion.sys.mvc.common.model;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

public class _MappingKit {

	public static void mapping(ActiveRecordPlugin arp) {
		arp.addMapping("sys_user", "id", SysUser.class);//用户
		arp.addMapping("sys_user_sign", "id", SysUserSign.class);//用户签名
		arp.addMapping("sys_user_role", "id", SysUserRole.class);//用户角色
		arp.addMapping("sys_menu", "id", SysMenu.class);//菜单
		arp.addMapping("sys_role", "id", SysRole.class);//角色
		arp.addMapping("sys_role_auth", "id", SysRoleAuth.class);//角色对应功能权限
		arp.addMapping("sys_org", "id", SysOrg.class);//组织结构
		arp.addMapping("sys_friend", "id", SysFriend.class);//用户好友
		arp.addMapping("sys_custom_setting", "id", SysCustomSetting.class);//自定义设置
		arp.addMapping("sys_point", "id", SysPoint.class);//积分
		arp.addMapping("sys_point_user", "id", SysPointUser.class);//用户积分
		arp.addMapping("sys_attachment", "id", SysAttachment.class);//系统附件
		arp.addMapping("sys_mobile_message", "id", SysMobileMessage.class);//系统短信模块
		arp.addMapping("sys_data_auth_rule", "id", SysDataAuth.class);//数据权限配置信息表
		arp.addMapping("sys_log", "id", SysLog.class);//系统日志表
		arp.addMapping("sys_dct", "id", SysDct.class);//系统字典表
		arp.addMapping("sys_dct_group", "id", SysDctGroup.class);//系统字典分组表
		arp.addMapping("cms_content", "id", CmsContent.class);//内容
		arp.addMapping("cms_type", "id", CmsType.class);//内容类型
		arp.addMapping("chat_history", "id", ChatHistory.class);//群聊
		arp.addMapping("act_re_model", "ID_", ActReModel.class);//流程模型
		arp.addMapping("act_re_procdef", "ID_", ActReProcdef.class);
		arp.addMapping("v_tasklist", "TASKID", VTasklist.class);//任务--视图
		arp.addMapping("oa_notice", "id", OaNotice.class);//通知公告
		arp.addMapping("oa_notice_user", "id", OaNoticeUser.class);//通知公告收
		arp.addMapping("oa_bumph", "id", OaBumph.class);//公文
		arp.addMapping("oa_bumph_org", "id", OaBumphOrg.class);//公文主送抄送单位
		arp.addMapping("oa_bumph_org_user", "id", OaBumphOrgUser.class);//公文主送抄送人员表
		arp.addMapping("oa_apply_buy", "id", OaApplyBuy.class);//采购申请
		arp.addMapping("oa_apply_custom", "id", OaApplyCustom.class);//自定义流程
		arp.addMapping("oa_apply_buy_item", "id", OaApplyBuyItem.class);//采购申请明细
		arp.addMapping("oa_apply_leave", "id", OaApplyLeave.class);//请假
		arp.addMapping("oa_apply_res_get", "id", OaApplyResGet.class);//资源借用
		arp.addMapping("oa_apply_use_car", "id", OaApplyUseCar.class);//车辆借用
		arp.addMapping("oa_apply_business_card", "id", OaApplyBusinessCard.class);//名片印刷申请
		arp.addMapping("oa_apply_hotel", "id", OaApplyHotel.class);//宾馆预定申请
		arp.addMapping("oa_apply_office_object", "id", OaApplyOfficeObject.class);//办公用品申请
		arp.addMapping("oa_apply_seal", "id", OaApplySeal.class);//用章申请
		arp.addMapping("oa_apply_train_ticket", "id", OaApplyTrainTicket.class);//车船票申请
		arp.addMapping("oa_apply_usercar_work", "id", OaApplyUsercarWork.class);//私车公用补贴申请
		arp.addMapping("oa_apply_gift", "id", OaApplyGift.class);//礼品申请
		arp.addMapping("oa_apply_meetroom", "id", OaApplyMeetroom.class);//会议室申请
		arp.addMapping("dct_resource_car", "id", DctResourceCar.class);//汽车字典管理
		arp.addMapping("dct_resource_meetroom", "id", DctResourceMeetroom.class);//会议室管理
		arp.addMapping("oa_apply_user_change_station", "id", OaApplyUserChangeStation.class);//调岗
		arp.addMapping("oa_apply_user_dimission", "id", OaApplyUserDimission.class);//离职
		arp.addMapping("oa_apply_user_regular", "id", OaApplyUserRegular.class);//转正
		arp.addMapping("oa_apply_cost", "id", OaApplyCost.class);//费用申请
		arp.addMapping("oa_project", "id", OaProject.class);//项目立项申请
		arp.addMapping("oa_project_build", "id", OaProjectBuild.class);//项目立项申请
		arp.addMapping("oa_project_change_member", "id", OaProjectChangeMember.class);//项目成员变更
		arp.addMapping("oa_project_express_confirm", "id", OaProjectExpressConfirm.class);//项目单据快递确认
		arp.addMapping("ams_asset", "id", AmsAsset.class);//资产管理
		arp.addMapping("ams_asset_allot", "id", AmsAssetAllot.class);//资产调配
		arp.addMapping("ams_asset_borrow", "id", AmsAssetBorrow.class);//资产借用
		arp.addMapping("ams_asset_dispose", "id", AmsAssetDispose.class);//资产处置
		arp.addMapping("ams_asset_need", "id", AmsAssetNeed.class);//资产需求
		arp.addMapping("ams_asset_receive", "id", AmsAssetReceive.class);//资产领用
		arp.addMapping("ams_asset_repair", "id", AmsAssetRepair.class);//资产报修
		arp.addMapping("ams_asset_sign_in", "id", AmsAssetSignIn.class);//资产录入
		arp.addMapping("oa_contract", "id", OaContract.class);//合同字典
		arp.addMapping("oa_contract_apply", "id", OaContractApply.class);//合同申请
		arp.addMapping("oa_contract_pay", "id", OaContractPay.class);//合同付款
		arp.addMapping("oa_contract_invoice", "id", OaContractInvoice.class);//合同开票
		arp.addMapping("oa_contract_change", "id", OaContractChange.class);//合同更改
		arp.addMapping("oa_contract_stop", "id", OaContractStop.class);//合同终止
		arp.addMapping("oa_apply_finance", "id", OaApplyFinance.class);//财务申请
		arp.addMapping("oa_apply_bank_account", "id", OaApplyBankAccount.class);//银行卡开销户申请
		arp.addMapping("oa_flow_carbon_c", "id", OaFlowCarbonC.class);//流程抄送
		arp.addMapping("oa_picture", "id", OaPicture.class);//存储表单图片信息
		arp.addMapping("sys_mobile_version", "id", SysMobileVersion.class);//手机端版本更新
		//
		//****************用户数据权限start******************
		arp.addMapping("oa_permission_group", "id", OaPermissionGroup.class);
		arp.addMapping("oa_permission_org", "id", OaPermissionOrg.class);
		arp.addMapping("oa_permission_table", "id", OaPermissionTable.class);
		//****************用户数据权限end******************
		//****************用户人事权限start******************
		arp.addMapping("sys_hr_org", "id", SysHrOrg.class);
		//****************模板自定义******************
		arp.addMapping("sys_template", "id", SysTemplate.class);
		arp.addMapping("sys_templateparam", "id", SysTemplateparam.class);
		//****************自定义审批流程******************
		arp.addMapping("oa_customflow_flowmodel", "id", OaCustomFlowmodel.class);
		arp.addMapping("oa_customflow_modelnode", "id", OaCustomflowModelnode.class);
		arp.addMapping("oa_customflow_modelnode_user", "id", OaCustomflowModelnodeUser.class);
		arp.addMapping("oa_customflow_type", "id", OaCustomflowType.class);
		arp.addMapping("oa_customflow_model_org", "id", OaCustomflowModelOrg.class);
		arp.addMapping("oa_customflow_case", "id", OaCustomflowCase.class);
		arp.addMapping("oa_customflow_casenode", "id", OaCustomflowCasenode.class);
		arp.addMapping("oa_customflow_casenode_mapping", "id", OaCustomflowCasenodeMapping.class);
		arp.addMapping("oa_customflow_comment", "id", OaCustomflowComment.class);
		arp.addMapping("oa_customflow_joinlty", "id", OaCustomflowJoinlty.class);
	}
}

