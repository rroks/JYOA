package com.pointlion.sys.mvc.admin.oa.apply.bankaccount;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.interceptor.MainPageTitleInterceptor;
import com.pointlion.sys.mvc.admin.oa.common.BusinessUtil;
import com.pointlion.sys.mvc.admin.oa.common.FlowCCService;
import com.pointlion.sys.mvc.admin.oa.customWorkflow.OaCustomflowCaseService;
import com.pointlion.sys.mvc.admin.oa.customWorkflow.OaCustomflowCasenodeService;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.common.base.BaseController;
import com.pointlion.sys.mvc.common.model.OaApplyBankAccount;
import com.pointlion.sys.mvc.common.model.OaPermissionGroup;
import com.pointlion.sys.mvc.common.model.OaProject;
import com.pointlion.sys.mvc.common.model.SysOrg;
import com.pointlion.sys.mvc.common.model.SysUser;
import com.pointlion.sys.mvc.common.utils.Constants;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.mvc.common.utils.StringUtil;
import com.pointlion.sys.mvc.common.utils.UuidUtil;
import com.pointlion.sys.plugin.shiro.ShiroKit;



@Before(MainPageTitleInterceptor.class)
public class OaApplyBankAccountController extends BaseController {
	public static final OaApplyBankAccountService service = OaApplyBankAccountService.me;
	public static WorkFlowService wfservice = WorkFlowService.me;
	public static FlowCCService ccService = FlowCCService.me;
	public static OaCustomflowCaseService ocfcService = OaCustomflowCaseService.me;
	public static OaCustomflowCasenodeService ocfcNodeService = OaCustomflowCasenodeService.me;
	/***
	 * 列表页面
	 */
	public void getListPage(){
		String type = getPara("type");
		String name = service.getTypeName(type);
		setAttr("type",type);
		setBread(name,this.getRequest().getServletPath()+"?type="+type,"管理");
    	render("list.html");
    }
	/***
     * 获取分页数据
	 * @throws UnsupportedEncodingException 
     **/
    public void listData() throws UnsupportedEncodingException{
    	String type = getPara("type","");
    	String curr = getPara("pageNumber");
    	String pageSize = getPara("pageSize");
    	String endTime = getPara("endTime","");
    	String startTime = getPara("startTime","");
    	String name = java.net.URLDecoder.decode(getPara("name",""),"UTF-8");
    	String c = getPara("c","");
    	if(StrKit.notBlank(c)){
    		if(c.equals("myCC")){
    			Page<Record> page = wfservice.getFlowCCPage(Integer.valueOf(curr),Integer.valueOf(pageSize), service.getDefKeyByType(type), ShiroKit.getUserId(), service.getQuerySql(type, name, startTime, endTime));
				Page<Record> Custompage = ocfcService.getFlowCCPage(Integer.valueOf(curr),Integer.valueOf(pageSize), service.getDefKeyByType(type), ShiroKit.getUserId(), service.getQuerySql(type, name, startTime, endTime));
				page.getList().addAll(Custompage.getList());
    			renderPage(page.getList(),"",page.getTotalRow()+Custompage.getTotalRow());
    		}else if(c.equals("myTask")){
    			Page<Record> page = wfservice.getToDoPageByKey(Integer.valueOf(curr),Integer.valueOf(pageSize), service.getDefKeyByType(type), ShiroKit.getUsername(), service.getQuerySql(type, name, startTime, endTime));
				Page<Record> Custompage = ocfcService.getToDoPageByKey(Integer.valueOf(curr),Integer.valueOf(pageSize), service.getDefKeyByType(type), ShiroKit.getUserId(), service.getQuerySql(type, name, startTime, endTime));
				page.getList().addAll(Custompage.getList());
    			renderPage(page.getList(),"",page.getTotalRow()+Custompage.getTotalRow());
    		}else if(c.equals("myHaveDone")){
    			Page<Record> page = wfservice.getHavedonePage(Integer.valueOf(curr),Integer.valueOf(pageSize), service.getDefKeyByType(type), ShiroKit.getUsername(), service.getQuerySql(type, name, startTime, endTime));
				Page<Record> Custompage = ocfcService.getHavedonePage(Integer.valueOf(curr),Integer.valueOf(pageSize), service.getDefKeyByType(type), ShiroKit.getUserId(), service.getQuerySql(type, name, startTime, endTime));
				page.getList().addAll(Custompage.getList());
    			renderPage(page.getList(),"",page.getTotalRow()+Custompage.getTotalRow());
    		}else if(c.equals("mypermission")){
    			//我可查看的
    			//先查出可查询的组织
    			String defKey = "AccountBank_"+type;//拼接defKey
    			System.out.println(defKey);
    			List<Record> recordList = OaPermissionGroup.dao.getOrgidByDefKeyAndU(defKey, ShiroKit.getUserId());
    			System.out.println(recordList);
    			String orgids = "'LINJIUQIANG'";
    			List strList = new ArrayList();
    			if(recordList.size()>0){
    				for (Record r : recordList) {
    					strList.add("'"+r.getStr("orgid")+"'");
					}
    				orgids = StringUtil.join(strList,",");
    			}
    			Page<Record> page = wfservice.getApplyPageByKeyAndOrgid(Integer.valueOf(curr),Integer.valueOf(pageSize), service.getDefKeyByType(type), orgids,type,"1", service.getQuerySql(type, name, startTime, endTime));
				Page<Record> Custompage = ocfcService.getApplyPageByKeyAndOrgid(Integer.valueOf(curr),Integer.valueOf(pageSize), service.getDefKeyByType(type), orgids,type,"1", service.getQuerySql(type, name, startTime, endTime));
				//自定义归档
				Page<Record> Customfilepage = ocfcService.getfileview(Integer.valueOf(curr),Integer.valueOf(pageSize), service.getDefKeyByType(type), ShiroKit.getUserId(), type,"1",service.getQuerySql(type, name, startTime, endTime));
				page.getList().addAll(Customfilepage.getList());
				page.getList().addAll(Custompage.getList());
    			renderPage(page.getList(),"",page.getTotalRow()+Custompage.getTotalRow()+Customfilepage.getTotalRow());
    		}else{
    			Page<Record> page = service.getBankAccountPage(Integer.valueOf(curr),Integer.valueOf(pageSize),ShiroKit.getUserId(),type,name,startTime,endTime);
            	renderPage(page.getList(),"",page.getTotalRow());	
    		}
    	}else{
    		Page<Record> page = service.getBankAccountPage(Integer.valueOf(curr),Integer.valueOf(pageSize),ShiroKit.getUserId(),type,name,startTime,endTime);
        	renderPage(page.getList(),"",page.getTotalRow());	
    	}
    	
    }
    /***
     * 保存
     */
    public void save(){
    	OaApplyBankAccount o = getModel(OaApplyBankAccount.class);
    	if(StrKit.notBlank(o.getProjectId())){
    		o.setProjectName(OaProject.dao.getById(o.getProjectId()).getProjectName());
    	}
    	if(StrKit.notBlank(o.getId())){
    		o.update();
    		ccService.addFlowCC(this, o.getId(), service.getDefKeyByType(o.getType()),OaApplyBankAccount.tableName);
    	}else{
    		String username = ShiroKit.getUsername();
     		String id = UuidUtil.getUUID();
     		o.setId(id);
     		o.setCreateTime(DateUtil.getTime());
     		Integer num = BusinessUtil.getAddContractBankaccountFinanceNumNum(username); 
    		o.setBankaccountNumNum(num);//自增编号
    		o.setBankaccountNum(BusinessUtil.getAddNum(num,username));
    		o.setBankaccountNumYear(DateUtil.format(new Date(), "yyyyMM"));
    		o.save();
    		ccService.addFlowCC(this, id, service.getDefKeyByType(o.getType()),OaApplyBankAccount.tableName);
    	}
    	renderSuccess();
    }
    /***
     * 获取编辑页面
     */
    public void getEditPage(){
    	String type = getPara("type");
    	setAttr("type", type);
    	String name = service.getTypeName(type);
    	setBread(name,this.getRequest().getServletPath().substring(0,this.getRequest().getServletPath().lastIndexOf("/")+1)+"getListPage?type="+type,name);
    	String id = getPara("id");
    	String view = getPara("view");//查看
    	setAttr("view", view);
    	if(StrKit.notBlank(id)){//修改
    		OaApplyBankAccount o = service.getById(id);
    		setAttr("o", o);
    		//是否是查看详情页面
    		if("detail".equals(view)){
    			if(StrKit.notBlank(o.getProcInsId())){
    				setAttr("procInsId", o.getProcInsId());
					if("1".equals(o.getIfCustomflow())){
						setAttr("defId", ocfcNodeService.getHisList(o.getProcInsId()));
					}else {
						setAttr("defId", wfservice.getDefIdByInsId(o.getProcInsId()));
					}
    			}
    		}
    		if(StrKit.notBlank(o.getProjectId())){
    			setAttr("projectName", OaProject.dao.getById(o.getProjectId()).getProjectName());
        		o.setProjectName(OaProject.dao.getById(o.getProjectId()).getProjectName());
        	}
    		ccService.setAttrFlowCC(this, o.getId(), service.getDefKeyByType(o.getType()));
    	}else{
    		SysUser user = SysUser.dao.findById(ShiroKit.getUserId());
    		SysOrg org = SysOrg.dao.findById(user.getOrgid());
    		setAttr("user", user);
    		setAttr("org", org);
    	}
    	setAttr("formModelName",StringUtil.toLowerCaseFirstOne(OaApplyBankAccount.class.getSimpleName()));//模型名称
    	
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
    	OaApplyBankAccount o = OaApplyBankAccount.dao.getById(id);
    	o.setIfSubmit(Constants.IF_SUBMIT_YES);
    	String insId = wfservice.startProcess(id, service.getDefKeyByType(o.getType()),o.getTitle(),null);
    	o.setProcInsId(insId);
    	o.update();
    	renderSuccess("提交成功");
    }
	/***
	 * 自定义审批提交
	 */
	@Before(Tx.class)
	public void startMyProcess(){
		String id = getPara("id");
		String modelid = getPara("modelid");
		String approvaluserid = getPara("approvaluserid");
		String SelectFileid = getPara("SelectFileid");
		String type = getPara("type");
		OaApplyBankAccount o = OaApplyBankAccount.dao.getById(id);
		if("1".equals(o.getIfSubmit())){
			renderError("该单据已提交，不可重复提交！");
			return;
		}
		o.setIfCustomflow(Constants.IF_CUSTOMFLOW_YES);
		o.setIfSubmit(Constants.IF_SUBMIT_YES);
		String defkey = service.getDefKeyByType(type);
		String insId = ocfcService.startProcess(approvaluserid,o.getProcInsId(),id, modelid,OaApplyBankAccount.tableName,defkey,SelectFileid);
		o.setProcInsId(insId);
		o.update();
		renderSuccess("提交成功");
		//}
	}
    /***
     * 撤回
     */
    public void callBack(){
    	String id = getPara("id");
    	try{
    		OaApplyBankAccount o = OaApplyBankAccount.dao.getById(id);
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
	/***
	 * 撤回
	 */
	@Before(Tx.class)
	public void customCallBack(){
		String id = getPara("id");
		try{
			OaApplyBankAccount o = OaApplyBankAccount.dao.getById(id);
			ocfcService.callBack(o.getProcInsId());
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