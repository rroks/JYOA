package com.pointlion.sys.mvc.admin.oa.common;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pointlion.sys.mvc.admin.oa.customWorkflow.OaCustomflowCaseService;
import com.pointlion.sys.mvc.admin.oa.customWorkflow.OaCustomflowCasenodeService;
import com.pointlion.sys.mvc.common.model.*;
import com.pointlion.sys.mvc.common.utils.*;
import org.apache.commons.lang.StringUtils;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.pointlion.sys.mvc.admin.oa.bumph.BumphService;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.common.base.BaseController;
import com.pointlion.sys.mvc.mobile.common.MobileService;
import com.pointlion.sys.plugin.shiro.ShiroKit;

public class CommonFlowController extends BaseController{
	static final CommonFlowService service = CommonFlowService.me;
	static WorkFlowService wfservice = WorkFlowService.me;
	static final OaCustomflowCaseService customService = OaCustomflowCaseService.me;

	/****
	 * 获取申请办理任务页面
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws ClassNotFoundException 
	 */
	public void getDoTaskPage() throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		String defKey = getPara("defkey");
		//自定义审批
		String custom = getPara("custom");
		String defName = WorkFlowUtil.getDefNameByDefKey(defKey);
		setBread(defName,"/","办理任务");
		String taskid = getPara("taskid");
		setAttr("title", defName);
		if(StrKit.notBlank(taskid)){
			if(StrKit.notBlank(custom)){
				//设置自定义审批办理界面
				String className = WorkFlowUtil.getClassFullNameByDefKey(defKey);
				Class<?> userClass = Class.forName(className);
				setAttr("formModelName",StringUtil.toLowerCaseFirstOne(userClass.getSimpleName()));//模型名称
				setAttr("view", getPara("view"));
				Record o = new Record();//单据对象
				o = customService.getTaskObject(taskid,defKey);
				if(o!=null){
					setAttr("o", o);
					String procInsId = o.getStr("proc_ins_id");
					if(StrKit.notBlank(procInsId)){
						setAttr("procInsId", procInsId);
					}
					String userid = o.getStr("userid");
					if(StrKit.notBlank(userid)){
						SysUser user = SysUser.dao.findById(userid);
						setAttr("user", user);
					}
					List<OaFlowCarbonC> cclist = OaFlowCarbonC.dao.find("select * from oa_flow_carbon_c c ,sys_user u  where u.id=c.user_id and business_id = '"+o.getStr("id")+"' ");
					//抄送人员
					List flowList = new ArrayList();
					List<String> nameList = new ArrayList<String>();
					List<String> idList = new ArrayList<String>();
					if(StrKit.notNull(cclist)){
						for(OaFlowCarbonC cc : cclist){
							idList.add(cc.getUserId());
							nameList.add(cc.getStr("name"));
						}
					}
					setAttr("flowCC", StringUtils.join(idList,","));
					setAttr("flowCCName", StringUtils.join(nameList,","));
				}
				setPageUrl(defKey,o);//设置渲染的页面，以及所需要的属性

				render("customDoTask.html");
			}else{
				//组织都需要的参数
				VTasklist task = VTasklist.dao.getTaskRecord(taskid);
				try {
					if("ReEdit".equals(task.getTASKDEFKEY())){//判断是否要重新编辑
						String className = WorkFlowUtil.getClassFullNameByDefKey(task.getDEFKEY());
						Class<?> userClass = Class.forName(className);
						setAttr("formModelName",StringUtil.toLowerCaseFirstOne(userClass.getSimpleName()));//模型名称
						setAttr("view", "reEdit");
					}else{
						setAttr("view", "detail");
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				setAttr("task", task);
				Record o = new Record();//单据对象
				if(StrKit.notBlank(WorkFlowUtil.getTablenameByDefkey(defKey))){//如果属于固定流程
					o = service.getTaskObject(taskid,defKey);
				}else{
					o = service.getCustomTaskObject(taskid,defKey);
				}
				if(o!=null){
					setAttr("o", o);
					String procInsId = o.getStr("proc_ins_id");
					if(StrKit.notBlank(procInsId)){
						setAttr("procInsId", procInsId);
						setAttr("defId", wfservice.getDefIdByInsId(procInsId));
					}
					String userid = o.getStr("userid");
					if(StrKit.notBlank(userid)){
						SysUser user = SysUser.dao.findById(userid);
						setAttr("user", user);
					}
					List<OaFlowCarbonC> cclist = OaFlowCarbonC.dao.find("select * from oa_flow_carbon_c c ,sys_user u  where u.id=c.user_id and business_id = '"+o.getStr("id")+"' ");
					//抄送人员
					List flowList = new ArrayList();
					List<String> nameList = new ArrayList<String>();
					List<String> idList = new ArrayList<String>();
					if(StrKit.notNull(cclist)){
						for(OaFlowCarbonC cc : cclist){
							idList.add(cc.getUserId());
							nameList.add(cc.getStr("name"));
						}
					}
					setAttr("flowCC", StringUtils.join(idList,","));
					setAttr("flowCCName", StringUtils.join(nameList,","));
				}
				setPageUrl(defKey,o);//设置渲染的页面，以及所需要的属性

				render("commonDoTask.html");
			}
    	}
	}
	public void setPageUrl(String defKey,Record o){
		if(StrKit.notBlank(WorkFlowUtil.getTablenameByDefkey(defKey))){//如果属于固定流程
			//组织各自需要的参数
    		if(defKey.equals(OAConstants.DEFKEY_PROJECT_BUILD)){//不属于申请系列的----项目立项申请
    			setAttr("leadersNameStr", SysUser.dao.idStrToNameStr(o.getStr("leader"), ","));
        		setAttr("membersNameStr", SysUser.dao.idStrToNameStr(o.getStr("member"), ","));
    			setAttr("pageUrl", "/WEB-INF/admin/oa/project/build/editForm.html");
    		}else if(defKey.equals(OAConstants.DEFKEY_PROJECT_CHANGE_MEMBER)){//不属于申请系列的----项目重要人员变更
    			OaProject p = OaProject.dao.getById(o.getStr("project_id"));
        		setAttr("projectName", p!=null?p.getProjectName():"");
    			setAttr("leadersNameStr", SysUser.dao.idStrToNameStr(o.getStr("leader"), ","));
        		setAttr("membersNameStr", SysUser.dao.idStrToNameStr(o.getStr("member"), ","));
    			setAttr("pageUrl", "/WEB-INF/admin/oa/project/changemember/editForm.html");
    		}else if(defKey.equals(OAConstants.DEFKEY_PROJECT_EXPRESS_CONFIRM)){//不属于申请系列的----项目快递确认
    			OaProject p = OaProject.dao.getById(o.getStr("project_id"));
        		setAttr("projectName", p!=null?p.getProjectName():"");
        		setAttr("pageUrl", "/WEB-INF/admin/oa/project/expressconfirm/editForm.html");
    		}else if(defKey.equals(OAConstants.DEFKEY_BUMPH)){//不属于申请系列的----公文的办理页面
    	    	BumphService bumphservice = new BumphService();
    	    	bumphservice.setAttrFirstSecond(this,o.getStr("id"));
    	    	setAttr("pageUrl", "/WEB-INF/admin/oa/bumph/editForm.html");
    		}else if(defKey.indexOf("Ams")==0){//资产类型的流程
    			setAttr("pageUrl", "/WEB-INF/admin/ams/"+defKey.substring(3)+"/editForm.html");
    		}else if(defKey.indexOf("Contract")==0){//合同类的
    			OaContract contract = OaContract.dao.getById(o.getStr("contract_id"));
   				setAttr("contractName",contract!=null?contract.getName():"");
   				setAttr("type", o.getStr("type"));
   				if(defKey.indexOf("ContractApply")==0){
   					setAttr("pageUrl", "/WEB-INF/admin/oa/contract/apply/editForm.html");
   				}else{
   					setAttr("pageUrl", "/WEB-INF/admin/oa/contract/"+defKey.substring(8)+"/editForm.html");
   				}
    		}else if(defKey.indexOf("Finance")==0){//财务类的
    			OaProject p = OaProject.dao.getById(o.getStr("project_id"));
        		setAttr("projectName", p!=null?p.getProjectName():"");
        		OaContract contract = OaContract.dao.getById(o.getStr("contract_id"));
        		setAttr("type", o.getStr("type"));
        		String aboutOrgId = o.getStr("about_orgid");
        		if(StrKit.notBlank(aboutOrgId)){
        			SysOrg aboutOrg = SysOrg.dao.getById(aboutOrgId);
        			if(aboutOrg!=null){
        				setAttr("aboutOrg", aboutOrg);
        			}
        		}
   				setAttr("contractName",contract!=null?contract.getName():"");
    			setAttr("pageUrl", "/WEB-INF/admin/oa/apply/finance/editForm.html");
    		}else if(defKey.indexOf("AccountBank")==0){//银行卡类的
    			OaProject p = OaProject.dao.getById(o.getStr("project_id"));
    			setAttr("type", o.getStr("type"));
        		setAttr("projectName", p!=null?p.getProjectName():"");
        		setAttr("pageUrl", "/WEB-INF/admin/oa/apply/bankaccount/editForm.html");
    		}else{	
    			if(defKey.equals(OAConstants.DEFKEY_APPLY_BUY)){//采购申请
    				List<OaApplyBuyItem> itemList = OaApplyBuyItem.dao.getAllItemByBuyId(o.getStr("id"));
    	    		setAttr("itemList", itemList);
    			}
    			setAttr("pageUrl", "/WEB-INF/admin/oa/apply/"+defKey.toLowerCase()+"/editForm.html");
    		}
		}else{
			setAttr("defName", wfservice.getDefNameByDefKey(defKey));
			setAttr("pageUrl", "/WEB-INF/admin/oa/apply/custom/editForm.html");
		}
	}
	/***
	 * 获取已办列表数据
	 */
	public void getHaveDoneTaskDataList(){
		String defkey = getPara("defkey");
		String username = ShiroKit.getUsername();
		String curr = getPara("pageNumber");
    	String pageSize = getPara("pageSize");
    	Page<Record> page = wfservice.getHavedonePage(Integer.valueOf(curr),Integer.valueOf(pageSize),defkey, username,null);
		Page<Record> custompage = customService.getHavedonePage(Integer.valueOf(curr),Integer.valueOf(pageSize),defkey, ShiroKit.getUserId(),null);
		page.getList().addAll(custompage.getList());
		renderPage(page.getList(),"",page.getTotalRow()+custompage.getTotalRow());
	}
	
	/***
	 * 获取已办打开已办任务详情页面
	 */
	public void openHavedoneBusinessPage(){
		String defKey = getPara("defkey");
		String defName = WorkFlowUtil.getDefNameByDefKey(defKey);
		String businessType = "";
		String tablename = WorkFlowUtil.getTablenameByDefkey(defKey);
		setBread(defName,"/","单据详情");
		setAttr("title", defName);
		setAttr("view", "detail");
		String id = getPara("id");
		if(StrKit.notBlank(id)){
    		Record o = service.getBusinessObject(id,defKey);
    		if(o!=null){
    			if(tablename.toLowerCase().contains("contract")){//合同
    				businessType = "contract";
    			}
    			if(tablename.toLowerCase().contains("finance")){//财务
    				businessType = "finance";
    			}
    			if(tablename.toLowerCase().contains("bank")){//银行卡
    				businessType = "bankaccount";
    			}
    			o.set("businessType", businessType);
    			setAttr("o", o);
    			String procInsId = o.getStr("proc_ins_id");
    			if(StrKit.notBlank(procInsId)){
    				setAttr("procInsId", procInsId);
    				if(!"1".equals(o.get("if_customflow")))
    					setAttr("defId", wfservice.getDefIdByInsId(procInsId));
    			}
    			String userid = o.getStr("userid");//申请人
    			if(StrKit.notBlank(userid)){
    				SysUser user = SysUser.dao.findById(userid);
    				setAttr("user", user);
    			}
    		}
    		setPageUrl(defKey,o);//设置渲染的页面
    	}
		render("/WEB-INF/admin/oa/common/commonHavedoneBusinessPage.html");
	}
	
	/***
	 * 提交任务
	 */
	@SuppressWarnings("rawtypes")
	public void submitTask(){
		try{
			String taskid = getPara("taskId");
			VTasklist task = VTasklist.dao.getTaskRecord(taskid);
			Map<String,Object> var = new HashMap<String,Object>();
			if(task!=null){
				if("ReEdit".equals(task.getTASKDEFKEY())){//如果是重新编辑
					String className = WorkFlowUtil.getClassFullNameByDefKey(task.getDEFKEY());
					Class<?> userClass = Class.forName(className);
					Model o = (Model) getModel(userClass);
					o.update();
					var = wfservice.getVar(var, o.getStr("id"), ShiroKit.getUsername(), task.getDEFKEY());
				}
			}
			String comment = getPara("comment");
			var.put("pass", getPara("pass"));
			String HWsign = getPara("signBase64Stream");
			String userid = ShiroKit.getUserId();
			if(HWsign!=null){
				String basepath = this.getRequest().getSession().getServletContext().getRealPath("");
				String pictureId = UuidUtil.getUUID();
				String filename = userid +taskid+ ".png";
				String finalPath = "nfwyoa/images/signImg/";

				new AliyunOssUtil().uplodaImg(finalPath + filename, Base64Util.GenerateImageInput(HWsign));
				Base64Util.GenerateImage(HWsign,basepath+"/"+finalPath,filename);
				SysUserSign oldSign = SysUserSign.dao.getByUserTaskid(userid,taskid);
				if(StrKit.notNull(oldSign)){
					oldSign.delete();
				}
				SysUserSign newSign = new SysUserSign();
				newSign.setId(UuidUtil.getUUID());
				newSign.setUserid(userid);
				newSign.setOrgid(ShiroKit.getUserOrgId());
				newSign.setCreatetime(DateUtil.getTime());
				newSign.setUpdatetime(DateUtil.getTime());
				newSign.setSignImg(new AliyunOssUtil().getPicURL(finalPath + filename));
				newSign.setSignLocal(finalPath + filename);
				newSign.setSignFile(HWsign.getBytes());
				newSign.setTaskid(taskid);
				newSign.save();
			}
			wfservice.completeTask(taskid,ShiroKit.getUsername(), comment, var);
			renderSuccess();
		}catch(Exception e){
			e.printStackTrace();
			renderError();
		}
		
	}
	
	/**
	 * 获取我申请的流程
	 */
	public void getMyApplyTaskList(){
		String curr = getPara("pageNumber");
    	String pageSize = getPara("pageSize");
    	String ifComplete = getPara("ifComplete");//"0"-未完成,"1"-已完成
    	if(StrKit.notBlank(ifComplete)){
    		if("myHaveDone".equals(ifComplete)){
    			ifComplete = "1";
    		}else{
    			ifComplete = "0";
    		}
    	}else{ 
    		ifComplete = "0";
    	}
    	
    	String tablenameStr = "oa_apply_bank_account,oa_apply_finance,oa_contract_apply";
		String tablename[] = tablenameStr.split(",");
		List<Map<String,String>> totalList = new ArrayList<Map<String,String>>();
//		for (int i = 0; i < tablename.length; i++) {
//			List<Record> list = new ArrayList<Record>();
//			list = new WorkFlowService().getApplyBykey(tablename,ShiroKit.getUserId(), ifComplete);
//			totalList.addAll(list);
//		}
		for (int i = 0; i < tablename.length; i++) {
			List<Record> list = new ArrayList<Record>();
			list = new WorkFlowService().getApplyBykeySingle(tablename[i],ShiroKit.getUserId(), ifComplete);
			//自定义审批
			list.addAll(new OaCustomflowCaseService().getApplyBykeySingle(tablename[i],ShiroKit.getUserId(), ifComplete)) ;
			//自定义审批
			if(StrKit.notNull(list)){
//				String keyvalue = new WorkFlowUtil().getDefKeyByTablename(tablename[i]);
//				String keyValue[] = keyvalue.split("###");
				for (Record record : list) {
					if("1".equals(record.getStr("if_customflow"))){
						String def = record.getStr("defkey");
						record.set("defKey", def);
						String defName = new WorkFlowUtil().getDefNameByKey(def);
						record.set("defName", defName);
						record.set("defrealName", defName);
					}else{
						String[] def = record.getStr("defid").split(":");
						record.set("defKey", def[0]);
						String defName = new WorkFlowUtil().getDefNameByKey(def[0]);
						record.set("defName", defName);
						record.set("defrealName", defName);
					}

				}
			}
			System.out.println(list.size());
			for (Record record : list) {
				Map<String, String> map = new HashMap();
				map.put("id", record.getStr("id"));
				map.put("defid", record.getStr("defid"));
				map.put("proc_ins_id", record.getStr("proc_ins_id"));
				map.put("createtime", record.getStr("createtime"));
				map.put("applyer_name", record.getStr("applyer_name"));
				map.put("org_name", record.getStr("org_name"));
				map.put("title", record.getStr("title"));
				map.put("if_complete", record.getStr("if_complete"));
				map.put("if_agree", record.getStr("if_agree"));
				map.put("if_submit", record.getStr("if_submit"));
				map.put("defKey", record.getStr("defKey"));
				map.put("defName", record.getStr("defName"));
				map.put("defrealName", record.getStr("defrealName"));
				map.put("if_customflow", record.getStr("if_customflow"));
				totalList.add(map);
			}
//			totalList.addAll(new ArrayList(list));

		}
		if(totalList.size()>0){
			Collections.sort(totalList, new Comparator<Map<String, String>>() {
	            public int compare(Map<String, String> o1, Map<String, String> o2) {
//	                return o1.get(key).compareTo(o2.get(key));//顺序
		                try {
							return String.valueOf(DateUtil.sdfTime.parse(o2.get("createtime")).getTime()).compareTo(String.valueOf(DateUtil.sdfTime.parse(o1.get("createtime")).getTime()));
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}//降序
		                return 0;
	            }
	     });
		}
		
		List<Map<String,String>> returnList = new ArrayList<Map<String,String>>();
//		renderSuccess(totalList,null);
		if(StrKit.notNull(totalList)){
			int endInt = Integer.valueOf(curr)*Integer.valueOf(pageSize);
			int fromInt = (Integer.valueOf(curr)-1)*Integer.valueOf(pageSize);
			if(fromInt>totalList.size()){
				renderPage(null, null, totalList.size());
			}else{
				if(endInt>totalList.size()){
					endInt = totalList.size();
				}
				returnList = totalList.subList(fromInt, endInt);
				renderPage(returnList, null, totalList.size());
			}
		}
//    	if (StrKit.notBlank(ShiroKit.getUserName())) {
//			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//			List<String> defList = new WorkFlowService().getAllDefkeyList();
//			WorkFlowService wfs = new WorkFlowService();
//			System.out.println(ShiroKit.getUsername());
//			System.out.println(ShiroKit.getUserId());
//			Page<Record> page = wfs.getApplyPage(Integer.valueOf(curr),Integer.valueOf(pageSize),defList,ShiroKit.getUserId(),ShiroKit.getUsername(),ifComplete,null);
//			if(StrKit.notNull(page)){
//				renderPage(page.getList(),"",page.getTotalRow());
//			}else{
//				renderPage(null,"",0);
//			}
//		}
	}
	
	
	public void getContractName(String contractId){
		
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
