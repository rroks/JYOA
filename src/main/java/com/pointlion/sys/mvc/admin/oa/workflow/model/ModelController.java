/**
 * 
 */
package com.pointlion.sys.mvc.admin.oa.workflow.model;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.pointlion.sys.interceptor.MainPageTitleInterceptor;
import com.pointlion.sys.mvc.admin.oa.common.WorkFlowUtil;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.common.base.BaseController;
import com.pointlion.sys.mvc.common.model.ActReModel;
import com.pointlion.sys.mvc.common.model.SysDct;
import com.pointlion.sys.plugin.activiti.ActivitiPlugin;

/**
 * @author Lion
 * @date 2017年2月16日 下午4:04:25
 * @qq 439635374
 */
@Before(MainPageTitleInterceptor.class)
public class ModelController extends BaseController{
	/***
	 * 获得列表页
	 */
	public void getCustomListPage(){
		setBread("自定义流程管理",this.getRequest().getServletPath(),"自定义流程管理");
    	render("customList.html");
    }
	/***
	 * 获得列表页
	 */
	public void getAbsoluteListPage(){
		setBread("固定流程管理",this.getRequest().getServletPath(),"固定流程管理");
    	render("AbsoluteList.html");
    }
	
	/***
	 * 获得启动自定义流程列表页，菜单中的
	 */
	public void getStartCustomFlowList(){
		setBread("自定义流程启动",this.getRequest().getServletPath(),"自定义流程启动");
    	render("startCustomFlowList.html");
	}
	/***
	 * 获得新增页面
	 */
	public void getAddPage(){
		setBread("自定义流程管理",this.getRequest().getServletPath().substring(0,this.getRequest().getServletPath().lastIndexOf("/")+1)+"getCustomListPage","自定义流程管理");
		render("add.html");
	}
	/***
	 * 保存模型--新建新模型
	 */
	public void save(){
		String name = getPara("name");
		String key = getPara("key");
		try {
			if(WorkFlowUtil.ifHaveThisFlow(key)){//已经有固定流程了
				renderError("该流程已经存在，请输入其他流程Key值！");
			}else{
				WorkFlowService service = new WorkFlowService();
				service.createModel(ActivitiPlugin.buildProcessEngine(), name, key);
				service.createModel(ActivitiPlugin.buildProcessEngine(), name, key);
				renderSuccess();
			}
		} catch (UnsupportedEncodingException e) {
			renderError();
			e.printStackTrace();
		}
	}
    /***
     * 查询分页数据
     */
    public void listCustomData(){
    	String curr = getPara("pageNumber");
    	String pageSize = getPara("pageSize");
    	Page<ActReModel> page = ActReModel.dao.getCustomModelPage(Integer.valueOf(curr),Integer.valueOf(pageSize));
    	renderPage(page.getList(),"" ,page.getTotalRow());
    }
    
    /***
     * 查询分页数据
     */
    public void listAbsoluteData(){
    	String curr = getPara("pageNumber");
    	String pageSize = getPara("pageSize");
    	Page<ActReModel> page = ActReModel.dao.getAbsoluteModelPage(Integer.valueOf(curr),Integer.valueOf(pageSize));
    	renderPage(page.getList(),"" ,page.getTotalRow());
    }
    
    /***
     * 模型部署
     */
	public void deploy() {
		String id = getPara("id");
		WorkFlowService service = new WorkFlowService();
		String message = service.deploy(id);
		renderSuccess(message);
	}
    /***
     * 删除模型
     */
	public void delete() {
		String id = getPara("id");
		WorkFlowService service = new WorkFlowService();
		service.deleteModel(id);
		renderSuccess();
	}
	
	
	
	public void getSelectCandidateUserPage(){
		List<SysDct> list = SysDct.dao.getByType("WORKFLOW_VAR_CANDIDATE");
		setAttr("var",list);
		render("selectCandidateUserPage.html");
	}
	
	
	
	
	
	
	
    /**************************************************************************/
	public void setBread(String name,String url,String nowBread){
		Map<String,String> pageTitleBread = new HashMap<String,String>();
		pageTitleBread.put("pageTitle", name);
		pageTitleBread.put("url", url);
		pageTitleBread.put("nowBread", nowBread);
		this.setAttr("pageTitleBread", pageTitleBread);
	}
}
