package com.pointlion.sys.mvc.admin.oa.permissiontable;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.helper.DataUtil;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.interceptor.MainPageTitleInterceptor;
import com.pointlion.sys.mvc.common.base.BaseController;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.common.utils.StringUtil;
import com.pointlion.sys.mvc.common.model.OaPermissionTable;
import com.pointlion.sys.mvc.common.model.SysDct;
import com.pointlion.sys.mvc.common.model.SysUser;
import com.pointlion.sys.mvc.common.model.SysUserRole;
import com.pointlion.sys.mvc.common.model.SysOrg;
import com.pointlion.sys.mvc.common.model.SysRole;
import com.pointlion.sys.mvc.common.utils.UuidUtil;
import com.pointlion.sys.mvc.common.utils.Constants;
import com.pointlion.sys.mvc.admin.oa.common.OAConstants;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.plugin.shiro.ShiroKit;



@Before(MainPageTitleInterceptor.class)
public class OaPermissionTableController extends BaseController {
	public static final OaPermissionTableService service = OaPermissionTableService.me;
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
    	OaPermissionTable o = getModel(OaPermissionTable.class);
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
    		OaPermissionTable o = service.getById(id);
    		setAttr("o", o);
    		//是否是查看详情页面
    		if("detail".equals(view)){
    			setAttr("view", view);
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
    	setAttr("formModelName",StringUtil.toLowerCaseFirstOne(OaPermissionTable.class.getSimpleName()));//模型名称
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
    	OaPermissionTable o = OaPermissionTable.dao.getById(id);
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
    		OaPermissionTable o = OaPermissionTable.dao.getById(id);
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
    

    
	/**赋表
	 * @param userid
	 * @param groupid
	 * @param tabledata
	 */
	@Before(Tx.class)
	public void giveUserTable(String userid, String groupid, String tabledata){
		OaPermissionTable.dao.deleteByGroupid(groupid);//删除原有的table
		String dataArr[] = tabledata.split(",");
		if (StrKit.notBlank(tabledata)) {
			for (String data : dataArr) {
				SysDct dct = SysDct.dao.getById(data);
				OaPermissionTable table = new OaPermissionTable();
				table.setId(UuidUtil.getUUID());
				table.setTablename(dct.getName());
				table.setTablevalue(dct.getValue());
				table.setTableid(data);
				table.setGroupid(groupid);
				table.setCreatetime(DateUtil.getTime());
				table.setDefkey(dct.getKey());
				table.setUserid(userid);
				table.save();
			}
		}
	}
    
    public void deleteByGroupid(){
    	
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