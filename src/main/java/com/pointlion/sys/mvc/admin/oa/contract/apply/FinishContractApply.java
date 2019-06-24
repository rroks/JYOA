package com.pointlion.sys.mvc.admin.oa.contract.apply;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.runtime.ProcessInstance;

import com.pointlion.sys.mvc.admin.oa.common.WorkFlowUtil;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.common.model.OaContract;
import com.pointlion.sys.mvc.common.model.OaContractApply;
import com.pointlion.sys.mvc.common.utils.Constants;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.plugin.activiti.ActivitiPlugin;

public class FinishContractApply implements JavaDelegate{
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		ProcessInstance instance = ActivitiPlugin.buildProcessEngine().getRuntimeService().createProcessInstanceQuery().processInstanceId(execution.getProcessInstanceId()).singleResult();
		String id = instance.getBusinessKey();//业务主键
		String defKey = instance.getProcessDefinitionKey();//流程key
		String pass = execution.getVariable("pass").toString();//是否同意
		if(Constants.SUBMIT_PASS_YES.equals(pass)){//同意
			OaContractApply a = OaContractApply.dao.getById(id);
			OaContract c = new OaContract();
			c.setId(a.getId());
			c.setAddExt(a.getAddExt());
			c.setAmountOfMoney(a.getAmountOfMoney());
			c.setBackground(a.getBackground());
			c.setCreateTime(DateUtil.getTime());
			c.setCreateUserid(a.getUserid());
			c.setCustomContactMobile(a.getCustomContactMobile());
			c.setCustomContactPerson(a.getCustomContactPerson());
			c.setCustomName(a.getCustomName());
			c.setDangerExt(a.getDangerExt());
			c.setDangerLaw(a.getDangerLaw());
			c.setDangerNeed(a.getDangerNeed());
			c.setDangerNeedNoOk(a.getDangerNeedNoOk());
			c.setDangerPayHard(a.getDangerPayHard());
			c.setDetail(a.getDetail());
			c.setName(a.getName());
			c.setNeed(a.getNeed());
			c.setTitle(a.getTitle());
			c.setType(a.getType());
			c.setType2(a.getType2());;
			c.setWorkTogether(a.getWorkTogether());
			c.save();
		}
		String tableName = WorkFlowUtil.getTablenameByDefkey(defKey);
		WorkFlowService.me.updateIfCompleteAndIfAgree(tableName, pass, id);//更新是否同意，是否完成
	
	}
}
