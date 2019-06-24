package com.pointlion.sys.mvc.admin.oa.permissiongroup;

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
import com.pointlion.sys.mvc.common.model.OaPermissionGroup;
import com.pointlion.sys.mvc.common.model.SysUser;
import com.pointlion.sys.mvc.common.model.SysOrg;
import com.pointlion.sys.mvc.common.model.SysRole;
import com.pointlion.sys.mvc.common.utils.UuidUtil;
import com.pointlion.sys.mvc.common.utils.Constants;
import com.pointlion.sys.mvc.admin.oa.common.OAConstants;
import com.pointlion.sys.mvc.admin.oa.permissionorg.OaPermissionOrgController;
import com.pointlion.sys.mvc.admin.oa.permissiontable.OaPermissionTableController;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.plugin.shiro.ShiroKit;



@Before(MainPageTitleInterceptor.class)
public class OaPermissionGroupController extends BaseController {
	public static final OaPermissionGroupService service = OaPermissionGroupService.me;
	public static WorkFlowService wfservice = WorkFlowService.me;
	/***
	 * 列表页面
	 */
	public void getListPage(){
		String userUrl = "/admin/sys/user/getListPage";
		String userid = getPara("userid");
		SysUser user = SysUser.dao.getById(userid);
		setBread("权限管理",userUrl,user.getUsername()+"权限");
		System.out.println("request:"+this.getRequest().getServletPath());
		setAttr("userid", userid);
    	render("list.html");
    }
	/***
     * 获取分页数据
     **/
    public void listData(){
    	String userid = getPara("userid");
    	String curr = getPara("pageNumber");
    	String pageSize = getPara("pageSize");
    	Page<Record> page = service.getPage(Integer.valueOf(curr),Integer.valueOf(pageSize),userid);
    	renderPage(page.getList(),"",page.getTotalRow());
    }
    /***
     * 保存
     */
    public void save(){
    	System.out.println("进入保存界面");
    	OaPermissionGroup o = getModel(OaPermissionGroup.class);
    	System.out.println(o);
    	String userid = o.getUserid();
    	String authname = o.getAuthname();
    	if(StrKit.isBlank(authname)){
    		authname = "数据权限"+DateUtil.getDate();
    	}
    	SysUser user = SysUser.dao.getById(userid);
    	if(StrKit.notBlank(o.getId())){
    		o.update();
    	}else{
    		o.setId(UuidUtil.getUUID());
    		o.setAuthname(authname);
    		o.setCreatetime(DateUtil.getTime());
    		o.setUpdatetime(DateUtil.getTime());
    		o.setUserid(userid);
    		o.setUsername(user.getUsername());
    		o.setControllerid(ShiroKit.getUserId());
    		o.setControllername(ShiroKit.getUsername());
    		o.save();
    	}
    	System.out.println("+++++++++++++++++++++++end+++++++++++++++++++++++++");
    	renderSuccess();
    }
    /***
     * 获取编辑页面
     */
    public void getEditPage(){
    	String userid = getPara("userid");
    	setBread("数据权限",this.getRequest().getServletPath().substring(0,this.getRequest().getServletPath().lastIndexOf("/")+1)+"getListPage?userid="+userid,"功能名称");
    	String id = getPara("id");
    	String view = getPara("view");
		setAttr("view", view);
    	if(StrKit.notBlank(id)){//修改
    		OaPermissionGroup o = service.getById(id);
    		setAttr("o", o);
    		setAttr("userid", o.getUserid());
    		//是否是查看详情页面
    		if("detail".equals(view)){
    			setAttr("view", view);
//    			if(StrKit.notBlank(o.getProcInsId())){
//    				setAttr("procInsId", o.getProcInsId());
//    				setAttr("defId", wfservice.getDefIdByInsId(o.getProcInsId()));
//    			}
    		}
    	}else{
    		setAttr("userid", userid);
    	}
    	setAttr("formModelName",StringUtil.toLowerCaseFirstOne(OaPermissionGroup.class.getSimpleName()));//模型名称
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
    	OaPermissionGroup o = OaPermissionGroup.dao.getById(id);
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
    		OaPermissionGroup o = OaPermissionGroup.dao.getById(id);
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
    
    public void getAllTable(){
    	
    }
    
    
    /***
     * 获取所有可见的表单
     */
    public void getGiveTable(){
    	String userid = getPara("userid");
    	String groupid = getPara("groupid");
    	setAttr("userid", userid);
    	setAttr("groupid", groupid);
    	render("giveTablename.html");
    }
    
    /**赋予表单
     * 
     */
    public void giveUserTable(){
    	String userid = getPara("userid");
    	String groupid = getPara("groupid");
    	String data = getPara("data");
    	new OaPermissionTableController().giveUserTable(userid, groupid, data);
    	renderSuccess();
    }
    
    /**赋予组织
     * 
     */
    public void giveUserOrg(){
    	String userid = getPara("userid");
    	String groupid = getPara("groupid");
    	String data = getPara("data");
    	new OaPermissionOrgController().giveUserOrg(userid, groupid, data);
    	renderSuccess();
    }
    
    
    
    
    /***
     * 获取所有的表格名称
     * 给用户赋予表格时候用
     */
    public void getAllTableTreeNode(){
    	String userid = getPara("userid");
    	String groupid = getPara("groupid");
    	renderJson(new OaPermissionGroupService().getAllTableTreeNode(userid,groupid));
    }
    /***
     * 获取用户下所有表格
     */
    public void getAllTableByUserid(){
    	String userid = getPara("userid");
    	String groupid = getPara("groupid");
    	renderJson(new OaPermissionGroupService().getAllTableTreeNodeByuserid(userid,groupid));
    }
    
    
    /**
     * 获取已选中的组织
     */
    public void getAllOrgByUserid(){
    	String userid = getPara("userid");
    	String groupid = getPara("groupid");
    	renderJson(new OaPermissionGroupService().getAllOrgTreeNodeByuserid(userid,groupid));
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