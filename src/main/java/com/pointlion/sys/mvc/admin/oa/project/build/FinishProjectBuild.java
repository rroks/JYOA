package com.pointlion.sys.mvc.admin.oa.project.build;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.runtime.ProcessInstance;

import com.pointlion.sys.mvc.admin.oa.common.WorkFlowUtil;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.common.model.OaProject;
import com.pointlion.sys.mvc.common.model.OaProjectBuild;
import com.pointlion.sys.mvc.common.utils.Constants;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.plugin.activiti.ActivitiPlugin;

public class FinishProjectBuild implements JavaDelegate{
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		ProcessInstance instance = ActivitiPlugin.buildProcessEngine().getRuntimeService().createProcessInstanceQuery().processInstanceId(execution.getProcessInstanceId()).singleResult();
		String id = instance.getBusinessKey();//业务主键
		String defKey = instance.getProcessDefinitionKey();//流程key
		String pass = execution.getVariable("pass").toString();//是否同意
		if(Constants.SUBMIT_PASS_YES.equals(pass)){//同意
			OaProjectBuild b = OaProjectBuild.dao.getById(id);
			OaProject p = new OaProject();
			p.setId(b.getId());
			p.setActAllStrategySuggest(b.getActAllStrategySuggest());
			p.setActSuggest(b.getActSuggest());
			p.setBuildId(b.getId());
			p.setContactMail(b.getContactMail());
			p.setContactMobile(b.getContactMobile());
			p.setContactName(b.getContactName());
			p.setCreateTime(DateUtil.getTime());
			p.setCreateUserId(b.getUserid());
			p.setCustomerContactMobile(b.getCustomerContactMobile());
			p.setCustomerContactName(b.getCustomerContactName());
			p.setCustomerName(b.getCustomerName());
			p.setCustomHope(b.getCustomHope());
			p.setLeader(b.getLeader());
			p.setMember(b.getMember());
			p.setProjectMoney(b.getProjectMoney());
			p.setProjectName(b.getProjectName());
			p.setProjectStartTime(b.getProjectStartTime());
			p.setRiskAndMeasure(b.getRiskAndMeasure());
			p.save();
		}
		String tableName = WorkFlowUtil.getTablenameByDefkey(defKey);
		WorkFlowService.me.updateIfCompleteAndIfAgree(tableName, pass, id);//更新是否同意，是否完成
	
	}
}
