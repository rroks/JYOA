package com.pointlion.sys.mvc.mobile.workflow;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.util.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import com.jfinal.kit.HttpKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.pointlion.sys.mvc.admin.oa.bumph.BumphService;
import com.pointlion.sys.mvc.admin.oa.common.BusinessUtil;
import com.pointlion.sys.mvc.admin.oa.common.FlowCCService;
import com.pointlion.sys.mvc.admin.oa.common.OAConstants;
import com.pointlion.sys.mvc.admin.oa.common.WorkFlowUtil;
import com.pointlion.sys.mvc.admin.oa.contract.apply.OaContractApplyService;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.common.base.BaseController;
import com.pointlion.sys.mvc.common.model.OaApplyCustom;
import com.pointlion.sys.mvc.common.model.OaBumph;
import com.pointlion.sys.mvc.common.model.OaContractApply;
import com.pointlion.sys.mvc.common.model.OaFlowCarbonC;
import com.pointlion.sys.mvc.common.model.OaPicture;
import com.pointlion.sys.mvc.common.model.SysAttachment;
import com.pointlion.sys.mvc.common.model.SysOrg;
import com.pointlion.sys.mvc.common.model.SysUser;
import com.pointlion.sys.mvc.common.utils.AliyunOssUtil;
import com.pointlion.sys.mvc.common.utils.Base64Util;
import com.pointlion.sys.mvc.common.utils.Constants;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.mvc.common.utils.StringUtil;
import com.pointlion.sys.mvc.common.utils.UuidUtil;
import com.pointlion.sys.mvc.mobile.common.MobileService;
import com.pointlion.sys.plugin.shiro.ShiroKit;

public class MobileWorkflowController extends BaseController{
	
	
	static MobileWorkflowService service = MobileWorkflowService.me;
	public static WorkFlowService wfService = WorkFlowService.me;
	public static FlowCCService ccService = FlowCCService.me;
	/**
	 * 获取待办list
	 * 
	 * @author 28995
	 */
	public void getTodoList() {
		String curr = getPara("pageNum");
		if(StrKit.isBlank(curr)){
			curr = "1";
		}
		String pageSize = getPara("pageSize");
		if(StrKit.isBlank(pageSize)){
			pageSize = "10";
		}
		String username = getPara("username");
		if (StrKit.notBlank(username)) {
			WorkFlowService wfs = new WorkFlowService();
			SysUser u = SysUser.dao.getByUsername(username);
			List totalList = wfs.getToDoList(u.getUsername());
			List returnList = new ArrayList();
			//处理分页信息
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
				}
			}
			renderSuccess800(returnList,totalList.size(),"");
//			renderSuccess800(page.getList(),page.getTotalRow(),"");
		} else {
			renderError900("");
		}
	}

	/**
	 * 获取待办详情
	 * 
	 * @author 28995
	 */
	public void getTodoInfos() {
		String insId = getPara("INSID");
		String defKey = getPara("defKey");
		if (StrKit.notBlank(insId)) {
			WorkFlowService wfs = new WorkFlowService();
			renderSuccess800(wfs.getTodoInfos(insId, defKey),wfs.getTodoInfos(insId, defKey).size(), "");
		} else {
			renderError900("");
		}
	}

	
	/**
	 * 获取我的申请
	 */
	public void getMyApplyList(){
		String curr = getPara("pageNum");
		if(StrKit.isBlank(curr)){
			curr = "1";
		}
		String pageSize = getPara("pageSize");
		if(StrKit.isBlank(pageSize)){
			pageSize = "10";
		}
		String username = getPara("username");
		SysUser user = SysUser.dao.getByUsername(username);
		String ifComplete = getPara("ifComplete");//"0"-未完成,"1"-已完成
    	
    	String tablenameStr = "oa_apply_bank_account,oa_apply_finance,oa_contract_apply";
		String tablename[] = tablenameStr.split(",");
		List<Map<String,String>> totalList = new ArrayList<Map<String,String>>();
		for (int i = 0; i < tablename.length; i++) {
			List<Record> list = new ArrayList<Record>();
			list = new WorkFlowService().getApplyBykeySingle(tablename[i],user.getId(), ifComplete);
			if(list.size()>0){
//				String keyvalue = new WorkFlowUtil().getDefKeyByTablename(tablename[i]);
//				String keyValue[] = keyvalue.split("###");
				for (Record record : list) {
					String[] def = record.getStr("defid").split(":");
					record.set("defKey", def[0]);
					String defName = new WorkFlowUtil().getDefNameByKey(def[0]);
					record.set("defName", defName);
					record.set("defrealName", defName);
				}
				
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
					map.put("defKey", record.getStr("defKey"));
					map.put("defName", record.getStr("defName"));
					map.put("defrealName", record.getStr("defrealName"));
					totalList.add(map);
				}
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
				renderSuccess800("", totalList.size(), "当前页码无内容");
//				renderPage(null, null, totalList.size());
			}else{
				if(endInt>totalList.size()){
					endInt = totalList.size();
				}
				returnList = totalList.subList(fromInt, endInt);
				renderSuccess800(returnList, totalList.size(), "返回成功");
			}
		}
	}
	
	/**
	 * 获取已办list
	 */
	public void getDoneList() {
		String username = getPara("username");
		String pageNum = getPara("pageNum");
		String defKey = "";
		String defName = "";
		if(StrKit.isBlank(pageNum)){
			pageNum = "1";
		}
		String pageSize = getPara("pageSize");
		if(StrKit.isBlank(pageSize)){
			pageSize = "10";
		}
		if (StrKit.notBlank(username)) {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			List<String> defList = service.getHavedoneDefkeyList(username);
			List totalList = new ArrayList();
			for (int i = 0; i < defList.size(); i++) {
				Map<String, Object> map = new HashMap<String, Object>();
				String[] strArr = defList.get(i).split("####");
				defKey = strArr[0];
				defName = strArr[1];
				WorkFlowService wfs = new WorkFlowService();
//				Page<Record> page = wfs.getHavedonePage(Integer.valueOf(pageNum), Integer.valueOf(pageSize), defKey, username, "");
				List<Record> findlist = wfs.getHavedoneList(defKey, username, "");
				//Page<Record> page = service.getHavedonePage(defKey, username, 1, 50);
				if(StrKit.notNull(findlist)){
					for (Record record : findlist) {
						Map addMap = new HashMap();
						addMap.put("defKey", defKey);
						addMap.put("DEFNAME", defName);
						addMap.put("CREATETIME", record.getStr("create_time"));
						Record taskRecord = new WorkFlowService().getToDoListByInsID(record.getStr("proc_ins_id"));
						if(StrKit.notNull(taskRecord)){
							addMap.put("TASKNAME", taskRecord.getStr("TASKNAME"));
						}else{
							addMap.put("TASKNAME", "");
						}
						addMap.put("DEFID", record.getStr("defid"));
						addMap.put("userId", record.getStr("userid"));
						addMap.put("id", record.getStr("id"));
						addMap.put("proc_ins_id", record.getStr("proc_ins_id"));
						addMap.put("applyer_name", record.getStr("applyer_name"));
						System.out.println("=============");
						System.out.println(addMap);
						System.out.println("=============");
						totalList.add(addMap);
					}
				}
			}
			if(totalList.size()>0){
				Collections.sort(totalList, new Comparator<Map<String, String>>() {
		            public int compare(Map<String, String> o1, Map<String, String> o2) {
//		                return o1.get(key).compareTo(o2.get(key));//顺序
			                try {
								return String.valueOf(DateUtil.sdfTime.parse(o2.get("CREATETIME")).getTime()).compareTo(String.valueOf(DateUtil.sdfTime.parse(o1.get("CREATETIME")).getTime()));
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}//降序
			                return 0;
		            }
		     });
			}
			
			List<Map<String,String>> returnList = new ArrayList<Map<String,String>>();
//			renderSuccess(totalList,null);
			if(StrKit.notNull(totalList)){
				int endInt = Integer.valueOf(pageNum)*Integer.valueOf(pageSize);
				int fromInt = (Integer.valueOf(pageNum)-1)*Integer.valueOf(pageSize);
				if(fromInt>totalList.size()){
					renderSuccess800("", totalList.size(),"");
				}else{
					if(endInt>totalList.size()){
						endInt = totalList.size();
					}
					returnList = totalList.subList(fromInt, endInt);
				}
			}
			renderSuccess800(returnList,totalList.size(), null);
		} else {
			renderError900("未找到该用户已办信息");
		}
	}

	/**
	 * 审核待办
	 * 
	 * @author 28995
	 */
	public void compelteTask() {
		String taskId = getPara("TASKID");
		String pass = getPara("PASS");
		String username = getPara("username");
		String comment;
		try {
			comment = URLDecoder.decode(getPara("COMMENT"), "UTF-8");
			WorkFlowService wfs = new WorkFlowService();
			Map<String, Object> var = new HashMap<String, Object>();
			var.put("pass", pass);
			wfs.completeTask(taskId,username, comment, var);
			renderSuccess800("",0,"审批成功！");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			renderError900("审批失败！");
		}

	}
	
	/**保存并提交
	 * 
	 */
	public void compelteTaskAndSave(){
		String defKey = getPara("defKey");// defkey
		String username = getPara("username");
		String taskId = getPara("TASKID");
		String pass = getPara("PASS");
		String comment;
		try {
			comment = URLDecoder.decode(getPara("COMMENT"), "UTF-8");
			String className = WorkFlowUtil.getClassFullNameByDefKey(defKey);
			Class<?> userClass = Class.forName(className);
			Model o = (Model) getModel(userClass);
			o.update();
			WorkFlowService wfs = new WorkFlowService();
			Map<String, Object> var = new HashMap<String,Object>();
			var.put(OAConstants.WORKFLOW_VAR_APPLY_USERNAME, username);
			var.put(OAConstants.WORKFLOW_VAR_PROCESS_INSTANCE_START_TIME, DateUtil.getTime());
			var = wfService.getVar(var,o.getStr("id"),username,defKey);
			var.put("pass", pass);
			wfs.completeTask(taskId,username, comment, var);
			String tableName = WorkFlowUtil.getTablenameByDefkey(defKey);
			ccService.addFlowCC(this, o.getStr("id"), defKey, tableName);
			renderSuccess800("",0,"保存成功");
		} catch (Exception e) {
			e.printStackTrace();
			renderError900("保存出错");
		}
	
	}

	/***
	 * 撤回
	 */
	public void reset() {
		String defKey = getPara("defKey");
		String id = getPara("id");
		String insId = getPara("insId");
		try {
			service.resetTable(defKey, id);
			WorkFlowService wfs = new WorkFlowService();
			wfs.callBack(insId);
			renderSuccess800("",0,"撤回成功");
		} catch (Exception e) {
			e.printStackTrace();
			renderError900("撤回失败");
		}
	}
	
	/**
	 * 撤回单据
	 * 
	 * @author 28995
	 */
	public void recallDocument() {

	}

	/**
	 * 保存单据
	 * 
	 * @author 28995
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("rawtypes")
	public void saveDocument() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String defKey = getPara("defKey");// defkey
		String flag = getPara("flag");// 是否是提交
		String username = getPara("username");
		String type = getPara("type");//用于判定自定义流程
		/***************表单保存start**************/
		try {
			String className = WorkFlowUtil.getClassFullNameByDefKey(defKey);
			if ("1".equals(type)) {
				className = OaApplyCustom.class.getName();
			}
			Class<?> userClass = Class.forName(className);
			Model o = (Model) getModel(userClass);
			List<SysUser> userList = SysUser.dao.getUserByOrgidAndRoleKey2(o.getStr("org_id"),OAConstants.WORKFLOW_ROLE_KEY_OrgLeader);//查询当前单位的所有经理
			String id = o.getStr("id");
			if (OAConstants.DEFKEY_BUMPH.equals(defKey)) {// 公文特殊处理
				BumphService bs = new BumphService();
				String firstOrgId = getPara("firstOrgId");// 主送单位
				String secondOrgId = getPara("secondOrgId");// 抄送单位
				id = bs.save((OaBumph) o, firstOrgId, secondOrgId);
			}else if(defKey.indexOf("ContractApply")!=-1){
		    	OaContractApply oc = getModel(OaContractApply.class);
		    	System.out.println(o.getStr("org_id"));
		    	List<SysUser> list = SysUser.dao.getUserByOrgidAndRoleKey2(o.getStr("org_id"),OAConstants.WORKFLOW_ROLE_KEY_OrgLeader);//查询当前单位的所有经理
		    	/*if(list==null||list.size()==0){
		    		renderError900("您所在部门没有部门经理审批此流程，请联系管理员!");
		    		return;
		    	}*/
		    	OaContractApplyService osvc = OaContractApplyService.me;
		    	if(StrKit.notBlank(oc.getId())){
		    		oc.update();
		    	}else{
		    		id = UuidUtil.getUUID();
		    		oc.setId(id);
		    		oc.setCreateTime(DateUtil.getTime());
		    		SysOrg org = SysOrg.dao.getByUsername(username);
		    		if(org!=null){
		    			oc.setContractNumChildCompanyId(org.getParentChildCompanyId());//所属子公司
		    		}
		    		Integer num = osvc.getAddContractNumNum(username); 
		    		oc.setContractNumNum(num);//自增编号
		    		oc.setContractNum(osvc.getAddContractNum(num,username));//合同号
		    		oc.setContractNumYear(DateUtil.format(new Date(), "yyyyMM"));
		    		oc.save();
		    	}
			} else if(defKey.indexOf("Finance")!=-1) {
		    	String oType = o.getStr("type");
		    	String oOrg = o.getStr("org_id");
		    	if(!"10".equals(oType)&&!"4".equals(oType)&&!"5".equals(oType)){//这个三个不需要经理审批，其他的需要经理审批--需要校验
		    		List<SysUser> s = SysUser.dao.getUserByOrgidAndRoleKey2(oOrg,OAConstants.WORKFLOW_ROLE_KEY_OrgLeader);//查询当前单位的所有经理
		    		/*if(s==null||s.size()==0){
		    			System.out.println("222222error90022222");
		        		renderError900("您所在部门没有部门经理审批此流程，请联系管理员!");
		        		return;
		        	}*/
		    	}
	        	if(OAConstants.DEFKEY_APPLY_FINANCE_9.equals(defKey)){//业务暂借款申请
	    			String aboutId = o.getStr("about_orgid");
	    			List<SysUser> list = SysUser.dao.getUserByOrgidAndRoleKey(aboutId,OAConstants.WORKFLOW_ROLE_KEY_OrgLeader);
	    			/*if(list==null||list.size()==0){
	    				System.out.println("33333error90033333");
	    				renderError900("所选职能单位中没有设置【部门经理】角色，流程无法提交。请重新选择职能单位，或者联系管理员，维护相关角色。");
	    				return;
	    			}*/
		    	}
				if (StrKit.notBlank(id)) {
					o.update();
				} else {
					id = UuidUtil.getUUID();
					o.set("id", id);
					o.set("create_time", DateUtil.getTime());
					Integer num = BusinessUtil.getAddContractBankaccountFinanceNumNum(username); 
					o.set("finance_num", BusinessUtil.getAddNum(num,username));
					o.set("finance_num_num", num);
					o.set("finance_num_year", DateUtil.format(new Date(), "yyyyMM"));
					o.save();
				}
			}else{
				if (StrKit.notBlank(id)) {
					o.update();
				} else {
					id = UuidUtil.getUUID();
					o.set("id", id);
					o.set("create_time", DateUtil.getTime());
		    		Integer num = BusinessUtil.getAddContractBankaccountFinanceNumNum(username); 
					o.set("bankaccount_num", BusinessUtil.getAddNum(num,username));
					o.set("bankaccount_num_num", num);
					o.set("bankaccount_num_year", DateUtil.format(new Date(), "yyyyMM"));
					o.save();
				}
			}
			/***************表单保存end**************/
			String tableName = WorkFlowUtil.getTablenameByDefkey(defKey);
			ccService.addFlowCC(this, id, defKey, tableName);
			/***************处理流程start**************/
			if ("1".equals(flag)) {// 如果是提交
				startProcess(defKey, id, username, type);
			}
			/***************处理流程保存end**************/
			renderSuccess800("",0,messageSuccess);
		} catch (Exception e) {
			e.printStackTrace();
			renderError900("保存出错");
		}

	}
	
	/**
	 * 删除
	 */
	public void delete() {
		String defKey = getPara("defKey");
		String id = getPara("id");
		try {
			service.deleteTable(defKey, id);
			renderSuccess800("",0,"删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			renderError900("删除失败");
		}
	}

	/**
	 * 获取某人某种单据的所有制单
	 * 
	 * @author 28995
	 * @throws UnsupportedEncodingException 
	 */
	public void getUserDocumentList() throws UnsupportedEncodingException {
		String curr = getPara("pageNum");
		if(StrKit.isBlank(curr)){
			curr = "1";
		}
		String pageSize = getPara("pageSize");
		if(StrKit.isBlank(pageSize)){
			pageSize = "10";
		}
		String defKey = getPara("defKey");// defkey
		String docType = getPara("docType");
		String userId = getPara("userId");
		String type = getPara("type");
		String flowType = getPara("flowType");//WTJ未完成，SPZ审批中，YWC已完成，YWC已完成，CC操送给我
		String searchText = URLDecoder.decode(getPara("searchText"), "UTF-8");
//		String searchText =new String(getPara("searchText").getBytes("iso8859-1"),"UTF-8");
		Page<Record> pageList = service.getUserDocumentList2(curr, pageSize, flowType,defKey, docType, userId, type, searchText);
		renderSuccess800(pageList, pageList.getTotalRow(),messageSuccess);
	}

	/**
	 * 获取某单据信息
	 * 
	 * @author 28995
	 */
	public void getDocument() {
		String defKey = getPara("defKey");// defkey
		String id = getPara("id");
		String pageType = getPara("pageType");
		Record r = service.getDocument(defKey, id, pageType);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("Model", r);
		String bumphId = r.getStr("id");
		List<Record> attachmentList = MobileService.getAttachMentByIdNotPic(bumphId);
		List<OaFlowCarbonC> cclist = OaFlowCarbonC.dao.find("select * from oa_flow_carbon_c c ,sys_user u  where u.id=c.user_id and business_id = '"+id+"' ");
		//修改抄送人员
		List flowList = new ArrayList();
		List<String> nameList = new ArrayList<String>();
		List<String> idList = new ArrayList<String>();
		if(StrKit.notNull(cclist)){
			for(OaFlowCarbonC cc : cclist){
				idList.add(cc.getUserId());
				nameList.add(cc.getStr("name"));
				Record record = SysUser.dao.getUserAndParentOrg(cc.getUserId());
				Map userMap = new HashMap();
				userMap.put("id", cc.getUserId());
				userMap.put("name", cc.getStr("name"));
				userMap.put("org", record.getStr("name")+"-"+record.getStr("parentname"));
				flowList.add(userMap);
			 }
		}
		r.set("FLOWCC", StringUtils.join(idList,","));
		r.set("FLOWCCNAME", StringUtils.join(nameList,","));
		r.set("FLOWCCLIST", flowList);
		map.put("ATTACHS", attachmentList);
		//查找流程相关图片
		List<SysAttachment> picList = SysAttachment.dao.getByBusidAndSuffix(bumphId,"png");
		List imgList = new ArrayList();
		if(picList.size()>0){
			for (SysAttachment pic : picList) {
				Map newMap = new HashMap();
				newMap.put("id", pic.getId());
				newMap.put("imgurl", pic.getRealUrl());
				imgList.add(newMap);
			}
		}
		map.put("IMGS", imgList);
//		List<OaPicture> picList = OaPicture.dao.getBylinkidAndDefKey(id,defKey);
//		String imgs = "";
//		List imgList = new ArrayList();
//		if(picList.size()>0){
//			for (OaPicture pic : picList) {
//				imgList.add(pic.getImgurl());
//			}
//			imgs = StringUtil.join(imgList, ",");
//		}
//		map.put("IMGS", picList);
		renderSuccess800(map,1, "");
	}

	

	/***
	 * 提交
	 * 
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@SuppressWarnings("rawtypes")
	public void startProcess(String defKey, String id, String username, String type)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String className = WorkFlowUtil.getClassFullNameByDefKey(defKey);
		if ("1".equals(type)) {
			className = OaApplyCustom.class.getName();
		}
		Class<?> userClass = Class.forName(className);
		Model m = (Model) userClass.newInstance();
		Model o = m.findById(id);
		o.set("if_submit", Constants.IF_SUBMIT_YES);
		WorkFlowService wfs = new WorkFlowService();
		Map<String, Object> var = new HashMap<String,Object>();
		var.put(OAConstants.WORKFLOW_VAR_APPLY_USERNAME, username);
		var.put(OAConstants.WORKFLOW_VAR_PROCESS_INSTANCE_START_TIME, DateUtil.getTime());
		var = wfService.getVar(var,id,username,defKey);
		String insId = wfs.startProcess(id, defKey, o.getStr("title"), var, username);
		o.set("proc_ins_id", insId);
		o.update();
		renderSuccess("提交成功");
	}
	
	
	/**
	 * 获取我申请的流程
	 */
	public void getMyApplyTaskList(){
		String username = getPara("username");
		String curr = getPara("pageNum");
		if(StrKit.isBlank(curr)){
			curr = "1";
		}
		String pageSize = getPara("pageSize");
		if(StrKit.isBlank(pageSize)){
			pageSize = "10";
		}
    	String ifComplete = getPara("ifComplete");//"0"-未完成,"1"-已完成
    	SysUser user = SysUser.dao.getByUsername(username);
    	String tablenameStr = "oa_apply_bank_account,oa_apply_finance,oa_contract_apply";
		String tablename[] = tablenameStr.split(",");
		List<Map<String,String>> totalList = new ArrayList<Map<String,String>>();
		for (int i = 0; i < tablename.length; i++) {
			List<Record> list = new ArrayList<Record>();
			list = new WorkFlowService().getApplyBykeySingle(tablename[i],user.getId(), ifComplete);
			if(list.size()>0){
				String keyvalue = new WorkFlowUtil().getDefKeyByTablename(tablename[i]);
//				String keyValue[] = keyvalue.split("###");
				for (Record record : list) {
					String[] def = record.getStr("defid").split(":");
					record.set("defKey", def[0]);
					String defName = new WorkFlowUtil().getDefNameByKey(def[0]);
					record.set("defName", defName);
					record.set("defrealName", defName);
				}
				System.out.println(list.size());
				for (Record record : list) {
					Map<String, String> map = new HashMap();
					map.put("id", record.getStr("id"));
					map.put("defid", record.getStr("defid"));
					map.put("proc_ins_id", record.getStr("proc_ins_id"));
					Record taskRecord = new WorkFlowService().getToDoListByInsID(record.getStr("proc_ins_id"));
					if(StrKit.notNull(taskRecord)){
						map.put("TASKNAME", taskRecord.getStr("TASKNAME"));
					}else{
						map.put("TASKNAME", "");
					}
					map.put("createtime", record.getStr("createtime"));
					map.put("applyer_name", record.getStr("applyer_name"));
					map.put("org_name", record.getStr("org_name"));
					map.put("title", record.getStr("title"));
					map.put("if_complete", record.getStr("if_complete"));
					map.put("if_agree", record.getStr("if_agree"));
					map.put("defKey", record.getStr("defKey"));
					map.put("defName", record.getStr("defName"));
					map.put("defrealName", record.getStr("defrealName"));
					totalList.add(map);
				}
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
		if(StrKit.notNull(totalList)){
			int endInt = Integer.valueOf(curr)*Integer.valueOf(pageSize);
			int fromInt = (Integer.valueOf(curr)-1)*Integer.valueOf(pageSize);
			if(fromInt>totalList.size()){
				renderSuccess800("", totalList.size(), "");
			}else{
				if(endInt>totalList.size()){
					endInt = totalList.size();
				}
				returnList = totalList.subList(fromInt, endInt);
			}
		}
		renderSuccess800(returnList, totalList.size(), "");
	}
	
	/**根据用户真实名字或电话查询
	 * 
	 */
	public void getUserBynameOrMobile(){
		String sqlTxt="";
		System.out.println(getRequest().getCharacterEncoding());
		try {
//			 sqlTxt =new String(getPara("sqlTxt").getBytes("iso8859-1"),"UTF-8");
			 sqlTxt =new String(getPara("sqlTxt").getBytes("UTF-8"),"UTF-8");
			 System.out.println(sqlTxt);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};//电话or用户真实名字
		String curr = getPara("pageNum");
		if(StrKit.isBlank(curr)){
			curr = "1";
		}
		String pageSize = getPara("pageSize");
		if(StrKit.isBlank(pageSize)){
			pageSize = "10";
		}
		Page<Record> userPage = SysUser.dao.getByNameOrMobilePage(curr,pageSize,sqlTxt);
		List returnList = new ArrayList();
		if(StrKit.notNull(userPage)){
			for (Record record : userPage.getList()) {
				Map map = new HashMap();
				map.put("id", record.getStr("id"));
				map.put("username", record.getStr("username"));
//				map.put("org", value);
				map.put("name", record.getStr("name"));
				map.put("mobile", record.getStr("mobile")!=null?record.getStr("mobile"):"");
				map.put("email", record.getStr("email")!=null?record.getStr("email"):"");
				if(StrKit.notNull(record.getStr("position"))){
					map.put("position", record.getStr("position"));	
				}else{
					map.put("position", "");
				}
				String orgName = "";
				if(StrKit.notBlank(record.getStr("orgname"))){
					orgName = record.getStr("orgname")+"-"+(record.getStr("parentname")!=null?record.getStr("parentname"):"");
				}
				map.put("org", orgName);
//				if(StrKit.notBlank(record.getStr("orgid"))){
//					SysOrg org = SysOrg.dao.getById(record.getStr("orgid"));
//					if(StrKit.notNull(org)){
//						SysOrg parentOrg = SysOrg.dao.getById(org.getParentId());
//						String parentName = "";
//						if(StrKit.notNull(parentOrg)){
//							parentName = parentOrg.getName();
//						}
////						List<SysOrg> orgList = SysOrg.dao.getParentAll(user.getOrgid());
////						if(StrKit.notNull(orgList)){
////							System.out.println(orgList);
////							String orgname = "";
////							for (SysOrg sysOrg : orgList) {
////								if(StrKit.notBlank(orgname)){
////									orgname = orgname+"-"+sysOrg.getName();
////								}else{
////									orgname = sysOrg.getName();
////								}
////							}
//							map.put("org", org.getName()+"-"+parentName);
////						}
//					}else{
//						map.put("org", "");
//					}
//				}else{
//					map.put("org", "");
//				}

				returnList.add(map);
			}
		}
		renderSuccess800(returnList, userPage.getTotalRow(), messageSuccess);
	}
	
	public void updateImg(){
		String buf = HttpKit.readData(getRequest());
        JSONObject json = new JSONObject(buf); 
        String linkid = json.getString("linkid");
        String defKey = json.getString("defKey");
        String userid = getPara("userid");
        String type = json.getString("type");
        String title = json.getString("title");
        String addImg = json.getString("addImg");
        String delImg = json.getString("delImg");
        //图片处理
        if (!StrKit.isBlank(addImg)) {
            String[] addPic = addImg.split(",");
            for (String img64 : addPic) {
                String picType = "attachments";
                String base64 = img64;
                String basepath = this.getRequest().getSession().getServletContext().getRealPath("");
                String pictureId = UuidUtil.getUUID();
                String filename = pictureId + ".png";
                String finalPath = "nfwyoa/images/attachments/" + filename;
                new AliyunOssUtil().uplodaImg(finalPath, Base64Util.GenerateImageInput(base64));
//                OaPicture picture = new OaPicture();
//                picture.setId(UuidUtil.getUUID());
//                picture.setDefkey(defKey);
//                picture.setCreatedate(DateUtil.getTime());
//                picture.setFormat("png");
//                picture.setImgurl(new AliyunOssUtil().getPicURL(finalPath));
//                picture.setLinkid(linkid);
//                picture.setTitle(title);
//                picture.save();
                SysAttachment attachment = new SysAttachment();
        		attachment.setId(pictureId);
        		attachment.setUrl(finalPath);
        		attachment.setRealUrl(new AliyunOssUtil().getPicURL(finalPath));
        		SysUser user = SysUser.dao.getById(userid);
        		SysOrg org = SysOrg.dao.getById(user.getOrgid());
        		attachment.setCreateUserId(user.getId());
        		attachment.setCreateUserName(user.getName());
        		attachment.setCreateOrgId(org.getId());
        		attachment.setCreateOrgName(org.getName());
        		attachment.setCreateTime(DateUtil.getTime());
        		attachment.setSuffix("png");
        		attachment.setFileName(filename);
        		attachment.setBusinessId(linkid);
        		attachment.save();
            }
        }
        if (!StrKit.isBlank(delImg)) {
            String[] delPic = delImg.split(",");
            for (String imgId : delPic) {
//            	OaPicture picture = OaPicture.dao.getById(imgId);
//            	if(StrKit.notNull(picture)){
//            		 new AliyunOssUtil().delImg(picture.getImgurl());
//            		 picture.delete();
//            	}
            	SysAttachment attachment = SysAttachment.dao.getById(imgId);
        		if(StrKit.notNull(attachment)){
           		 	new AliyunOssUtil().delImg(attachment.getRealUrl());
           		 	attachment.delete();
        		}
            }

        }
        renderSuccess800("", 0, messageSuccess);
	}
	
	/**上传单张图片
	 * 
	 */
	public void addImg(){
		String picType = "attachments";
        String linkid = getPara("linkid");
        String defKey = getPara("defKey");
        String userid = getPara("userid");
        String type = getPara("type");
        String title = getPara("title");
        String base64 = getPara("img");
        String basepath = this.getRequest().getSession().getServletContext().getRealPath("");
        String path = "/attachment/"+DateUtil.getYear()+"/"+DateUtil.getMonth()+"/"+DateUtil.getDay();//保存路径
        String pictureId = UuidUtil.getUUID();
        String filename = pictureId + ".png";
        String finalPath = "nfwyoa"+ path + filename;
        new AliyunOssUtil().uplodaImg(finalPath, Base64Util.GenerateImageInput(base64));
        SysAttachment attachment = new SysAttachment();
		attachment.setId(pictureId);
		attachment.setUrl(finalPath);
		attachment.setRealUrl(new AliyunOssUtil().getPicURL(finalPath));
		SysUser user = SysUser.dao.getById(userid);
		SysOrg org = SysOrg.dao.getById(user.getOrgid());
		attachment.setCreateUserId(user.getId());
		attachment.setCreateUserName(user.getName());
		attachment.setCreateOrgId(org.getId());
		attachment.setCreateOrgName(org.getName());
		attachment.setCreateTime(DateUtil.getTime());
		attachment.setSuffix("png");
		attachment.setFileName(filename);
		attachment.setBusinessId(linkid);
		attachment.save();
        
        
     /*   OaPicture picture = new OaPicture();
        picture.setId(UuidUtil.getUUID());
        picture.setDefkey(defKey);
        picture.setCreatedate(DateUtil.getTime());
        picture.setFormat("png");
        picture.setImgurl(new AliyunOssUtil().getPicURL(finalPath));
        picture.setLinkid(linkid);
        picture.setTitle(title);
        picture.save();*/
		Map map = new HashMap();
		map.put("id", pictureId);
		map.put("imgrul", new AliyunOssUtil().getPicURL(finalPath));
        renderSuccess800(map, 1, messageSuccess);
	}
	
	
	/**删除单张图片
	 * 
	 */
	public void delImg(){
		String imgId = getPara("imgId");
		
		
//		OaPicture picture = OaPicture.dao.getById(imgId);
//    	if(StrKit.notNull(picture)){
//    		 new AliyunOssUtil().delImg(picture.getImgurl());
//    		 picture.delete();
//    	}
		SysAttachment attachment = SysAttachment.dao.getById(imgId);
		if(StrKit.notNull(attachment)){
   		 	new AliyunOssUtil().delImg(attachment.getRealUrl());
   		 	attachment.delete();
		}
    	renderSuccess800("", 0, messageSuccess);
	}
	
	
	
	/***
	 * 清空所有流程信息!!!!!!!!!!!!!!!!!!!!
	 * 测试环境使用，正式环境，请勿调用该接口
	 */
	public void deleteFlowInstanceById(){
		
		String insid = getPara("insid");
		System.out.println("======================");
		System.out.println(insid);
		System.out.println("======================");
		if(StrKit.notBlank(insid)){
			new WorkFlowService().deleteIns(insid);
			renderSuccess();
		}else{
			renderError();
		}
		
	}
	
	/***
	 * 清空所有流程信息!!!!!!!!!!!!!!!!!!!!
	 * 测试环境使用，正式环境，请勿调用该接口
	 */
	public void deleteFlowInstanceByIds(){
		
		String insids = getPara("insids");
		String[] insidArr = getPara("insids").split(",");
		System.out.println("======================");
		System.out.println(insids);
		System.out.println("======================");
		if(StrKit.notBlank(insids)){
			for (String insid : insidArr) {
				new WorkFlowService().deleteIns(insid);
			}
			renderSuccess();
		}else{
			renderError();
		}
		
	}
}
