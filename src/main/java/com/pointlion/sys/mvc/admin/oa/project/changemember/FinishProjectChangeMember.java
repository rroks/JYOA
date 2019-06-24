package com.pointlion.sys.mvc.admin.oa.project.changemember;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.runtime.ProcessInstance;

import com.pointlion.sys.mvc.admin.oa.common.WorkFlowUtil;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.common.model.OaProject;
import com.pointlion.sys.mvc.common.model.OaProjectChangeMember;
import com.pointlion.sys.mvc.common.utils.Constants;
import com.pointlion.sys.plugin.activiti.ActivitiPlugin;

public class FinishProjectChangeMember implements JavaDelegate{
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		ProcessInstance instance = ActivitiPlugin.buildProcessEngine().getRuntimeService().createProcessInstanceQuery().processInstanceId(execution.getProcessInstanceId()).singleResult();
		String id = instance.getBusinessKey();//业务主键
		String defKey = instance.getProcessDefinitionKey();//流程key
		String pass = execution.getVariable("pass").toString();//是否同意
		if(Constants.SUBMIT_PASS_YES.equals(pass)){//同意
			OaProjectChangeMember b = OaProjectChangeMember.dao.getById(id);
			OaProject p = OaProject.dao.getById(b.getId());
			if(p!=null){
				p.setLeader(b.getLeader());
				p.setMember(b.getMember());
				p.update();
			}
		}
		String tableName = WorkFlowUtil.getTablenameByDefkey(defKey);
		WorkFlowService.me.updateIfCompleteAndIfAgree(tableName, pass, id);//更新是否同意，是否完成
	}
}
