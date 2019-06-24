package com.pointlion.sys.mvc.admin.oa.contract.stop;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.runtime.ProcessInstance;

import com.pointlion.sys.mvc.admin.oa.common.OAConstants;
import com.pointlion.sys.mvc.admin.oa.common.WorkFlowUtil;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.common.model.OaContract;
import com.pointlion.sys.mvc.common.model.OaContractStop;
import com.pointlion.sys.mvc.common.utils.Constants;
import com.pointlion.sys.plugin.activiti.ActivitiPlugin;

public class FinishContractStop implements JavaDelegate{
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		ProcessInstance instance = ActivitiPlugin.buildProcessEngine().getRuntimeService().createProcessInstanceQuery().processInstanceId(execution.getProcessInstanceId()).singleResult();
		String id = instance.getBusinessKey();//业务主键
		OaContractStop a = OaContractStop.dao.getById(id);
		String defKey = instance.getProcessDefinitionKey();//流程key
		String pass = execution.getVariable("pass").toString();//是否同意
		if(Constants.SUBMIT_PASS_YES.equals(pass)){//同意
			OaContract c = OaContract.dao.getById(a.getContractId());
			if(c!=null){
				c.setState(OAConstants.OA_CONTRACT_STATE_STOP);
				c.update();
			}
		}
		String tableName = WorkFlowUtil.getTablenameByDefkey(defKey);
		WorkFlowService.me.updateIfCompleteAndIfAgree(tableName, pass, id);//更新是否同意，是否完成
	
	}
}
