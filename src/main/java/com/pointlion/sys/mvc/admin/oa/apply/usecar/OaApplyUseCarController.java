package com.pointlion.sys.mvc.admin.oa.apply.usecar;

import java.util.HashMap;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.pointlion.sys.interceptor.MainPageTitleInterceptor;
import com.pointlion.sys.mvc.admin.oa.common.OAConstants;
import com.pointlion.sys.mvc.admin.oa.dct.resourcecar.DctResourceCarService;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.common.base.BaseController;
import com.pointlion.sys.mvc.common.model.OaApplyUseCar;
import com.pointlion.sys.mvc.common.model.SysOrg;
import com.pointlion.sys.mvc.common.model.SysUser;
import com.pointlion.sys.mvc.common.utils.Constants;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.mvc.common.utils.StringUtil;
import com.pointlion.sys.mvc.common.utils.UuidUtil;
import com.pointlion.sys.plugin.shiro.ShiroKit;

@Before(MainPageTitleInterceptor.class)
public class OaApplyUseCarController extends BaseController {
	public static final OaApplyUseCarService service = OaApplyUseCarService.me;
	public static final DctResourceCarService carService = DctResourceCarService.me;
	static WorkFlowService wfservice = WorkFlowService.me;
	/***
	 * 列表页面
	 */
	public void getApplyListPage(){
		setBread("用车申请",this.getRequest().getServletPath(),"管理");
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
    	OaApplyUseCar o = getModel(OaApplyUseCar.class);
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
    	setBread("用车申请",this.getRequest().getServletPath().substring(0,this.getRequest().getServletPath().lastIndexOf("/")+1)+"getApplyListPage","申请起草");
    	//添加和修改
    	String id = getPara("id");
    	if(StrKit.notBlank(id)){//修改
    		OaApplyUseCar o = service.getById(id);
    		setAttr("o", o);
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
    	}
    	setAttr("formModelName",StringUtil.toLowerCaseFirstOne(OaApplyUseCar.class.getSimpleName()));//模型名称
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
     * 选择车页面
     */
    public void getSelectCarPage(){
    	render("selectCar.html");
    }
    /***
     * 选择车数据
     */
    public void selectCarData(){
    	String curr = getPara("pageNumber");
    	String pageSize = getPara("pageSize");
    	Page<Record> page = carService.getPage(Integer.valueOf(curr),Integer.valueOf(pageSize));
    	for(Record r : page.getList()){
    		if(service.isInBorrow(r.getStr("id"))){
    			r.set("ifInBorrow", "1");
    		}else{
    			r.set("ifInBorrow", "0");
    		}
    	}
    	renderPage(page.getList(),"",page.getTotalRow());
    }
    
    /***
     * 提交
     */
    public void startProcess(){
    	String id = getPara("id");
    	OaApplyUseCar o = OaApplyUseCar.dao.getById(id);
    	o.setIfSubmit(Constants.IF_SUBMIT_YES);
    	String insId = wfservice.startProcess(id, OAConstants.DEFKEY_APPLY_USE_CAR,o.getTitle(),null);
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
        	OaApplyUseCar o = OaApplyUseCar.dao.getById(id);
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