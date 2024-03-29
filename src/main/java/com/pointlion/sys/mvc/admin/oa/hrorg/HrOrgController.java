package com.pointlion.sys.mvc.admin.oa.hrorg;

import java.util.HashMap;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.pointlion.sys.interceptor.MainPageTitleInterceptor;
import com.pointlion.sys.mvc.common.base.BaseController;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.common.utils.StringUtil;
import com.pointlion.sys.mvc.common.model.SysHrOrg;
import com.pointlion.sys.mvc.common.model.SysUser;
import com.pointlion.sys.mvc.common.model.SysOrg;
import com.pointlion.sys.mvc.common.utils.UuidUtil;
import com.pointlion.sys.mvc.common.utils.Constants;
import com.pointlion.sys.mvc.admin.oa.common.OAConstants;
import com.pointlion.sys.mvc.admin.oa.permissiongroup.OaPermissionGroupService;
import com.pointlion.sys.mvc.admin.oa.permissionorg.OaPermissionOrgController;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.plugin.shiro.ShiroKit;



@Before(MainPageTitleInterceptor.class)
public class HrOrgController extends BaseController {
	public static final HrOrgService service = HrOrgService.me;
	public static WorkFlowService wfservice = WorkFlowService.me;
	/***
	 * 列表页面
	 */
	public void getListPage(){
		setBread("功能名称",this.getRequest().getServletPath(),"管理");
    	render("list.html");
    }
	/***
     * 获取分页数据
     **/
    public void listData(){
    	String curr = getPara("pageNumber");
    	String pageSize = getPara("pageSize");
    	Page<Record> page = service.getPage(Integer.valueOf(curr),Integer.valueOf(pageSize));
    	renderPage(page.getList(),"",page.getTotalRow());
    }
    /***
     * 保存
     */
    public void save(){
    	SysHrOrg o = getModel(SysHrOrg.class);
    	if(StrKit.notBlank(o.getId())){
    		o.update();
    	}else{
    		o.setId(UuidUtil.getUUID());
//    		o.setCreateTime(DateUtil.getTime());
    		o.save();
    	}
    	renderSuccess();
    }
    /***
     * 获取编辑页面
     */
    public void getEditPage(){
    	setBread("功能名称",this.getRequest().getServletPath().substring(0,this.getRequest().getServletPath().lastIndexOf("/")+1)+"getListPage","功能名称");
    	String id = getPara("id");
    	String view = getPara("view");
		setAttr("view", view);
    	if(StrKit.notBlank(id)){//修改
    		SysHrOrg o = service.getById(id);
    		setAttr("o", o);
    		//是否是查看详情页面
    		if("detail".equals(view)){
//    			setAttr("view", view);
//    			if(StrKit.notBlank(o.getProcInsId())){
//    				setAttr("procInsId", o.getProcInsId());
//    				setAttr("defId", wfservice.getDefIdByInsId(o.getProcInsId()));
//    			}
    		}
    	}else{
    		SysUser user = SysUser.dao.findById(ShiroKit.getUserId());
    		SysOrg org = SysOrg.dao.findById(user.getOrgid());
    		setAttr("user", user);
    		setAttr("org", org);
    	}
    	setAttr("formModelName",StringUtil.toLowerCaseFirstOne(SysHrOrg.class.getSimpleName()));//模型名称
    	render("edit.html");
    }
    /***
     * 删除
     * @throws Exception
     */
    public void delete() throws Exception{
		String ids = getPara("ids");
    	//执行删除
		service.deleteByIds(ids);
    	renderSuccess("删除成功!");
    }
    /***
     * 提交
     */
    public void startProcess(){
    	String id = getPara("id");
    	SysHrOrg o = SysHrOrg.dao.getById(id);
//    	o.setIfSubmit(Constants.IF_SUBMIT_YES);
//    	String insId = wfservice.startProcess(id, OAConstants.DEFKEY_PROJECT_CHANGE_MEMBER,o.getTitle(),null);
//    	o.setProcInsId(insId);
    	o.update();
    	renderSuccess("提交成功");
    }
    /***
     * 撤回
     */
    public void callBack(){
    	String id = getPara("id");
    	try{
    		SysHrOrg o = SysHrOrg.dao.getById(id);
//        	wfservice.callBack(o.getProcInsId());
//        	o.setIfSubmit(Constants.IF_SUBMIT_NO);
//        	o.setProcInsId("");
        	o.update();
    		renderSuccess("撤回成功");
    	}catch(Exception e){
    		e.printStackTrace();
    		renderError("撤回失败");
    	}
    }
    
    /**
     * 获取已选中的组织
     */
    public void getAllCheckedOrgByUserid(){
    	String userid = getPara("userid");
    	String groupid = getPara("groupid");
    	renderJson(new HrOrgService().getAllOrgTreeNodeByuserid(userid));
    }
    
    /**
     * 获取已选中的组织和父节点之外的其余节点
     */
    public void getAllCheckedOrgAndParentByUserid(){
    	String userid = getPara("userid");
    	String groupid = getPara("groupid");
    	renderJson(new HrOrgService().getAllOrgTreeNodeAndParentByuserid(userid));
    }
    
    /**赋予组织
     * 
     */
    public void giveUserOrg(){
    	String userid = getPara("userid");
    	String data = getPara("data");
    	new HrOrgService().giveUserOrg(userid, data);
    	renderSuccess();
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