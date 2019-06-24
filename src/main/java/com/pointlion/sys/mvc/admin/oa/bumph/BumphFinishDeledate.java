package com.pointlion.sys.mvc.admin.oa.bumph;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.runtime.ProcessInstance;

import com.pointlion.sys.mvc.common.model.OaBumph;
import com.pointlion.sys.mvc.common.utils.Constants;
import com.pointlion.sys.plugin.activiti.ActivitiPlugin;

/***
 * 流程结束服务类
 * @author Administrator
 *
 */
public class BumphFinishDeledate implements JavaDelegate{



	@Override
	public void execute(DelegateExecution execution) throws Exception {
		ProcessInstance instance = ActivitiPlugin.buildProcessEngine().getRuntimeService().createProcessInstanceQuery().processInstanceId(execution.getProcessInstanceId()).singleResult();
		String id = instance.getBusinessKey();
		OaBumph bumph = OaBumph.dao.findById(id);
		String pass = execution.getVariable("pass").toString();//是否同意
		bumph.setIfComplete(BumphConstants.IF_COMPLETE_YES);
		String ifAgree = Constants.IF_AGREE_NO;//不同意
		if(Constants.SUBMIT_PASS_YES.equals(pass)){//同意
			ifAgree = Constants.IF_AGREE_YES;
		}
		bumph.setIfAgree(ifAgree);
		bumph.update();
	}


}
