package com.pointlion.sys.mvc.admin.oa.apply.cost;

import java.util.HashMap;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.pointlion.sys.interceptor.MainPageTitleInterceptor;
import com.pointlion.sys.mvc.admin.oa.common.CommonFlowService;
import com.pointlion.sys.mvc.admin.oa.common.OAConstants;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.common.base.BaseController;
import com.pointlion.sys.mvc.common.model.OaApplyCost;
import com.pointlion.sys.mvc.common.model.SysOrg;
import com.pointlion.sys.mvc.common.model.SysUser;
import com.pointlion.sys.mvc.common.utils.Constants;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.mvc.common.utils.StringUtil;
import com.pointlion.sys.mvc.common.utils.UuidUtil;
import com.pointlion.sys.plugin.shiro.ShiroKit;

@Before(MainPageTitleInterceptor.class)
public class OaApplyCostController extends BaseController {
	public static final OaApplyCostService service = OaApplyCostService.me;
	public static WorkFlowService wfservice = WorkFlowService.me;
	public static CommonFlowService commonFlowService = CommonFlowService.me;
	/***
	 * 列表页面
	 */
	public void getApplyListPage(){
		String type = getPara("type");
		String name = "";
		if("1".equals(type)){
			name = "费用报销申请";
		}else if("2".equals(type)){
			name = "出差费用申请";
		}else if("3".equals(type)){
			name = "业务招待费申请";
		}
		setAttr("type",type);
		setBread(name,this.getRequest().getServletPath()+"?type="+type,"管理");
    	render("list.html");
    }
	/***
     * 获取分页数据
     **/
    public void listData(){
    	String curr = getPara("pageNumber");
    	String pageSize = getPara("pageSize");
    	String type = getPara("type");
    	Page<Record> page = service.getPage(Integer.valueOf(curr),Integer.valueOf(pageSize),type);
    	renderPage(page.getList(),"",page.getTotalRow());
    }
    /***
     * 保存
     */
    public void save(){
    	OaApplyCost o = getModel(OaApplyCost.class);
    	if(StrKit.notBlank(o.getId())){
    		o.update();
    	}else{
    		o.setId(UuidUtil.getUUID());
    		o.setCreateTime(DateUtil.getTime());
    		o.save();
    	}
    	renderSuccess();
    }
    /***
     * 获取编辑页面
     */
    public void getEditPage(){
    	String type = getPara("type");
		String name = "";
		if("1".equals(type)){
			name = "费用报销申请";
		}else if("2".equals(type)){
			name = "出差费用申请";
		}else if("3".equals(type)){
			name = "业务招待费申请";
		}
    	setBread("费用申请",this.getRequest().getServletPath().substring(0,this.getRequest().getServletPath().lastIndexOf("/")+1)+"getApplyListPage?type="+type,name);
    	//添加和修改
    	String id = getPara("id");
    	if(StrKit.notBlank(id)){//修改
    		OaApplyCost o = service.getById(id);
    		setAttr("o", o);
    		setAttr("type",o.getType());
    		//是否是查看详情页面
    		String view = getPara("view");//查看
    		if("detail".equals(view)){
    			setAttr("view", view);
    			if(StrKit.notBlank(o.getProcInsId())){
    				setAttr("procInsId", o.getProcInsId());
    				setAttr("defId", wfservice.getDefIdByInsId(o.getProcInsId()));
    			}
    		}
    	}else{
    		SysUser user = SysUser.dao.findById(ShiroKit.getUserId());
    		SysOrg org = SysOrg.dao.findById(user.getOrgid());
    		setAttr("user", user);
    		setAttr("org", org);
    		setAttr("type",type);
    	}
    	setAttr("formModelName",StringUtil.toLowerCaseFirstOne(OaApplyCost.class.getSimpleName()));//模型名称
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
    	OaApplyCost o = OaApplyCost.dao.getById(id);
    	o.setIfSubmit(Constants.IF_SUBMIT_YES);
    	Map<String, Object> var = new HashMap<String,Object>();
//		var = wfservice.getVar(var, ShiroKit.getUsername());
    	String insId = wfservice.startProcess(id, OAConstants.DEFKEY_APPLY_COST,o.getTitle(),var);
    	o.setProcInsId(insId);
    	o.update();
    	renderSuccess("提交成功");
    }
    /***
     * 撤回
     */
    public void callBack(){
    	String id = getPara("id");
    	try{
    		OaApplyCost o = OaApplyCost.dao.getById(id);
        	wfservice.callBack(o.getProcInsId());
        	o.setIfSubmit(Constants.IF_SUBMIT_NO);
        	o.setProcInsId("");
        	o.update();
    		renderSuccess("撤回成功");
    	}catch(Exception e){
    		e.printStackTrace();
    		renderError("撤回失败");
    	}
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