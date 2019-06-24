package com.pointlion.sys.config.routes;

import com.jfinal.config.Routes;
import com.pointlion.sys.mvc.mobile.bumph.MobileBumphController;
import com.pointlion.sys.mvc.mobile.common.MobileController;
import com.pointlion.sys.mvc.mobile.custom.assessment.ProcessController;
import com.pointlion.sys.mvc.mobile.custom.assessment.ProcessInstanceController;
import com.pointlion.sys.mvc.mobile.help.CommonHelpController;
import com.pointlion.sys.mvc.mobile.login.MobileLoginController;
import com.pointlion.sys.mvc.mobile.notice.MobileNoticeController;
import com.pointlion.sys.mvc.mobile.workflow.MobileWorkflowController;

public class MobileRoutes extends Routes{

	@Override
	public void config() {
		//手机端接口
		add("/mobile/common",MobileController.class);
		add("/mobile/notice",MobileNoticeController.class);
		add("/mobile/login",MobileLoginController.class);
		add("/mobile/bumph",MobileBumphController.class);
		add("/mobile/help",CommonHelpController.class);
		add("/mobile/assessment",MobileWorkflowController.class);
		add("/mobile/custom/assessment", ProcessController.class);
		add("/mobile/custom/assessment/instance", ProcessInstanceController.class);
	}

}
