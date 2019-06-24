package com.pointlion.sys.mvc.mobile.common;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.mchange.v2.uid.UidUtils;
import com.pointlion.sys.mvc.admin.oa.bumph.BumphService;
import com.pointlion.sys.mvc.admin.oa.common.CommonFlowService;
import com.pointlion.sys.mvc.admin.oa.common.FlowCCService;
import com.pointlion.sys.mvc.admin.oa.common.OAConstants;
import com.pointlion.sys.mvc.admin.oa.common.WorkFlowUtil;
import com.pointlion.sys.mvc.admin.oa.contract.apply.OaContractApplyService;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.common.base.BaseController;
import com.pointlion.sys.mvc.common.model.ActReModel;
import com.pointlion.sys.mvc.common.model.AmsAssetBorrow;
import com.pointlion.sys.mvc.common.model.OaApplyCustom;
import com.pointlion.sys.mvc.common.model.OaApplyFinance;
import com.pointlion.sys.mvc.common.model.OaBumph;
import com.pointlion.sys.mvc.common.model.OaContractApply;
import com.pointlion.sys.mvc.common.model.OaFlowCarbonC;
import com.pointlion.sys.mvc.common.model.OaNotice;
import com.pointlion.sys.mvc.common.model.SysAttachment;
import com.pointlion.sys.mvc.common.model.SysOrg;
import com.pointlion.sys.mvc.common.model.SysPoint;
import com.pointlion.sys.mvc.common.model.SysPointUser;
import com.pointlion.sys.mvc.common.model.SysUser;
import com.pointlion.sys.mvc.common.model.SysUserSign;
import com.pointlion.sys.mvc.common.utils.AliyunOssUtil;
import com.pointlion.sys.mvc.common.utils.Base64Util;
import com.pointlion.sys.mvc.common.utils.Constants;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.mvc.common.utils.UuidUtil;
import com.pointlion.sys.mvc.mobile.notice.MobileNoticeService;
import com.pointlion.sys.plugin.mail.MailKit;

/***
 * 手机首页控制器
 * 
 * @author Administrator
 *
 */
public class MobileController extends BaseController {

	static MobileService service = MobileService.me;
	static MobileNoticeService noticeService = MobileNoticeService.me;
	public static CommonFlowService commonFlowService = CommonFlowService.me;
	public static WorkFlowService wfService = WorkFlowService.me;
	public static FlowCCService ccService = FlowCCService.me;

	/**
	 * 获取欢迎页数据
	 * 
	 * @author 28995
	 */
	public void getWelcomeData() {
		String username = getPara("username");
		if (StrKit.notBlank(username)) {
			SysUser user = SysUser.dao.getByUsername(username);
			Map<String,Object> map = new HashMap<String,Object>();
    		map.put("ID", user.getId());
    		map.put("USERNAME", user.getUsername());
    		map.put("NAME", user.getName());
    		map.put("IDCARD", user.getIdcard());
    		map.put("sign_img", user.getSignImg()!=null?user.getSignImg():"");
    		map.put("head_img", user.getImg()!=null?user.getImg():"");
    		map.put("MOBILE", user.getMobile()!=null?user.getMobile():"");
    		map.put("MAIL", user.getEmail()!=null?user.getEmail():"");
    		String orgId = user.getOrgid();
    		map.put("ORGID", orgId);
    		SysOrg org = new SysOrg();
    		map.put("ORGNAME", org.getById(orgId)==null?"":org.getById(orgId).getName());
    		WorkFlowService wfs = new WorkFlowService();
			Page<OaNotice> noticePage = noticeService.getMobileHomePageMyNoticePage(user.getId());
			map.put("noticeList", noticePage.getList());//通告list
			map.put("todoNum", String.valueOf(wfs.getTodoNum(user.getUsername())));//待办条数
			map.put("haveDoneNum", String.valueOf(wfs.getContractHaveDoneNum(username)+wfs.getFinanceHaveDoneNum(username)+wfs.getBankAccountHaveDoneNum(username)));//已完成条数
			map.put("myApplyDone", String.valueOf(new CommonFlowService().countMyApplyTaskList(username,"1")));//我的申请-已完成数
			map.put("myApplyUndo", String.valueOf(new CommonFlowService().countMyApplyTaskList(username,"0")));//我的申请-正在审批数
			renderSuccess800(map, 1,messageSuccess);
		} else {
			renderError900(messageFail);
		}
	}

	/**
	 * 获取待办list
	 * 
	 * @author 28995
	 */
	public void getTodoList() {
		String username = getPara("username");
		if (StrKit.notBlank(username)) {
			WorkFlowService wfs = new WorkFlowService();
			SysUser u = SysUser.dao.getByUsername(username);
			renderSuccess(wfs.getToDoList(u.getUsername()), null);
		} else {
			renderError();
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
			renderSuccess(wfs.getTodoInfos(insId, defKey), null);
		} else {
			renderError();
		}
	}

	/**
	 * 获取已办list
	 */
	public void getDoneList() {
		String username = getPara("username");
		if (StrKit.notBlank(username)) {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			List<String> defList = service.getHavedoneDefkeyList(username);
			for (int i = 0; i < defList.size(); i++) {
				Map<String, Object> map = new HashMap<String, Object>();
				String[] strArr = defList.get(i).split("####");
				String defKey = strArr[0];
				String defName = strArr[1];
				WorkFlowService wfs = new WorkFlowService();
				Page<Record> page = wfs.getHavedonePage(1, 50, defKey, username, "");
				//Page<Record> page = service.getHavedonePage(defKey, username, 1, 50);
				map.put("defKey", defKey);
				map.put("defName", defName);
				map.put("page", page);
				list.add(map);
			}
			renderSuccess(list, null);
		} else {
			renderError();
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
			renderSuccess("审批成功！");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			renderSuccess("审批失败！");
		}

	}
	
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
			
			renderSuccess();
		} catch (Exception e) {
			e.printStackTrace();
			renderError("保存出错");
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
			renderSuccess("撤回成功");
		} catch (Exception e) {
			e.printStackTrace();
			renderError("撤回失败");
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
			renderSuccess("删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			renderError("删除失败");
		}
	}

	/**
	 * 获取某人某种单据的所有制单
	 * 
	 * @author 28995
	 * @throws UnsupportedEncodingException 
	 */
	public void getUserDocumentList() throws UnsupportedEncodingException {
		String defKey = getPara("defKey");// defkey
		String docType = getPara("docType");
		String userId = getPara("userId");
		String type = getPara("type");
		String searchText = URLDecoder.decode(getPara("searchText"), "UTF-8");
		Map<String, List<Record>> map = service.getUserDocumentList(defKey, docType, userId, type, searchText);
		renderSuccess(map, null);
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
		map.put("MODEL", r);
		String bumphId = r.getStr("id");
		List<Record> attachmentList = MobileService.getAttachMentById(bumphId);
		
		List<OaFlowCarbonC> cclist = OaFlowCarbonC.dao.find("select * from oa_flow_carbon_c c ,sys_user u  where u.id=c.user_id and business_id = '"+id+"' ");
		List<String> idList = new ArrayList<String>();
		List<String> nameList = new ArrayList<String>();
		 for(OaFlowCarbonC cc : cclist){
			 idList.add(cc.getUserId());
			 nameList.add(cc.getStr("name"));
		 }
		r.set("FLOWCC", StringUtils.join(idList,","));
		r.set("FLOWCCNAME", StringUtils.join(nameList,","));
		
		map.put("ATTACHS", attachmentList);
		
		renderSuccess(map, null);
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
		String type = getPara("type");
		try {
			String className = WorkFlowUtil.getClassFullNameByDefKey(defKey);
			if ("1".equals(type)) {
				className = OaApplyCustom.class.getName();
			}
			Class<?> userClass = Class.forName(className);
			Model o = (Model) getModel(userClass);
			String id = o.getStr("id");
			if (OAConstants.DEFKEY_BUMPH.equals(defKey)) {// 公文特殊处理
				BumphService bs = new BumphService();
				String firstOrgId = getPara("firstOrgId");// 主送单位
				String secondOrgId = getPara("secondOrgId");// 抄送单位
				id = bs.save((OaBumph) o, firstOrgId, secondOrgId);
			}else if(defKey.indexOf("ContractApply")!=-1){
		    	OaContractApply oc = getModel(OaContractApply.class);
		    	List<SysUser> list = SysUser.dao.getUserByOrgidAndRoleKey2(o.getStr("org_id"),OAConstants.WORKFLOW_ROLE_KEY_OrgLeader);//查询当前单位的所有经理
		    	/*if(list==null||list.size()==0){
		    		renderError("您所在部门没有部门经理审批此流程，请联系管理员!");
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
		    	/*if(!"10".equals(oType)&&!"4".equals(oType)&&!"5".equals(oType)){//这个三个不需要经理审批，其他的需要经理审批--需要校验
		    		List<SysUser> s = SysUser.dao.getUserByOrgidAndRoleKey2(oOrg,OAConstants.WORKFLOW_ROLE_KEY_OrgLeader);//查询当前单位的所有经理
		    		if(s==null||s.size()==0){
		        		renderError("您所在部门没有部门经理审批此流程，请联系管理员!");
		        		return;
		        	}
		    	}
	        	if(OAConstants.DEFKEY_APPLY_FINANCE_9.equals(defKey)){//业务暂借款申请
	    			String aboutId = o.getStr("about_orgid");
	    			List<SysUser> list = SysUser.dao.getUserByOrgidAndRoleKey(aboutId,OAConstants.WORKFLOW_ROLE_KEY_OrgLeader);
	    			if(list==null||list.size()==0){
	    				renderError("所选职能单位中没有设置【部门经理】角色，流程无法提交。请重新选择职能单位，或者联系管理员，维护相关角色。");
	    				return;
	    			}
		    	}*/
				if (StrKit.notBlank(id)) {
					o.update();
				} else {
					id = UuidUtil.getUUID();
					o.set("id", id);
					o.set("create_time", DateUtil.getTime());
					o.save();
				}
			}else{
				if (StrKit.notBlank(id)) {
					o.update();
				} else {
					id = UuidUtil.getUUID();
					o.set("id", id);
					o.set("create_time", DateUtil.getTime());
					o.save();
				}
			}
			
			String tableName = WorkFlowUtil.getTablenameByDefkey(defKey);
			ccService.addFlowCC(this, id, defKey, tableName);
			
			if ("1".equals(flag)) {// 如果是提交
				startProcess(defKey, id, username, type);
			}
			renderSuccess();
		} catch (Exception e) {
			e.printStackTrace();
			renderError("保存出错");
		}

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

	/***
	 * 获取新闻头条
	 */
	public void getBanner() {
		renderSuccess(service.getBanner("banner"), "请求成功");
	}

	/***
	 * 获取内容
	 */
	public void getContent() {
		String id = getPara("id");// 主键
		renderSuccess(service.getContent(id), "请求成功");
	}

	/***
	 * 获取某种类型的列表
	 */
	public void getContentList() {
		String type = getPara("type");// 类型
		String pageNum = getPara("pageNum");
		String pageCount = getPara("pageCount");
		if (StrKit.notBlank(pageNum) && StrKit.notBlank(pageCount)) {
			Integer num = Integer.parseInt(pageNum);
			Integer count = Integer.parseInt(pageCount);
			renderSuccess(service.getContentPage(type, num, count), "请求成功");
		} else {
			renderSuccess(service.getContentList(type), "请求成功");
		}

	}

	/******************* 个人设置 ************************/
	public void getUser() {
		String userid = getPara("userid");
		SysUser user = SysUser.dao.getByUsername(userid);
		renderSuccess(user, "查询成功");
	}

	public void updateUser() {
		SysUser user = getModel(SysUser.class);
		if (StrKit.notBlank(user.getId())) {
			String password = user.getPassword();
			if (StrKit.notBlank(password)) {// 如果修改密码
				PasswordService svc = new DefaultPasswordService();
				user.setPassword(svc.encryptPassword(password));// 加密新密码
			}
			user.update();
			renderSuccess("保存成功");
		} else {
			renderError();
		}
	}

	public void modifyPassword() {
		String userid = getPara("userid");
		String nowPassword = getPara("nowPassword");
		String newPassword = getPara("newPassword");
		SysUser user = SysUser.dao.getByUsername(userid);
		if (user == null) {
			renderError("数据错误");
		} else {
			// 验证密码
			PasswordService svc = new DefaultPasswordService();
			if (svc.passwordsMatch(nowPassword, user.getPassword())) {
				user.setPassword(svc.encryptPassword(newPassword));// 加密新密码
				user.update();
				renderSuccess("修改密码成功");
			} else {
				renderError("当前密码错误");
			}
		}

	}

	/***
	 * 获取某人签到数据
	 */
	public void getMyDaySign() {
		String userid = getPara("userid");
		SysUser user = SysUser.dao.getByUsername(userid);
		renderSuccess(SysPointUser.dao.getSignByUser(user.getId()), "查询成功");
	}

	/***
	 * 签到
	 */
	public void daySign() {
		final String userid = getPara("userid");
		Db.tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				SysUser user = SysUser.dao.getByUsername(userid);
				String day = DateUtil.getDate();
				SysPointUser pu = SysPointUser.dao.getSignByUserAndDate(user.getId(), day);
				if (pu != null) {// 已经签到
					renderError("您已经签到过了！");
				} else {
					SysPoint point = SysPoint.dao.getByType("daySign");
					Integer p = point.getPoint();
					pu = new SysPointUser();
					pu.setCreateDate(DateUtil.getTime());
					pu.setUserid(user.getId());
					pu.setSignDay(day);
					pu.setId(UuidUtil.getUUID());
					pu.setPoint(p);
					pu.setUserid(user.getId());
					pu.setReason("签到");
					pu.save();
					renderSuccess("签到成功");
				}
				return true;
			}
		});

	}

	/***
	 * 查询用户积分
	 */
	public void pointQuery() {
		String userid = getPara("userid");
		SysUser user = SysUser.dao.getByUsername(userid);// 用户
		Map<String, Integer> data = new HashMap<String, Integer>();
		Integer daySignCount = SysPointUser.dao.getPointByDaySign(user.getId());
		Integer receiveCount = SysPointUser.dao.getPointByReceive(user.getId());
		data.put("daySignCount", daySignCount);
		data.put("receiveCount", receiveCount);
		data.put("sumCount", daySignCount + receiveCount);
		renderSuccess(data, "查询成功");
	}

	/***
	 * 查询用户个人信息
	 */
	public void getUserInfo() {
		Map<String, Object> data = new HashMap<String, Object>();
		String userid = getPara("userid");
		SysUser user = SysUser.dao.getByUsername(userid);// 用户
		SysOrg org = SysOrg.dao.getById(user.getOrgid());
		data.put("orgid", org.getId());// --组织建设和全程纪实用
		data.put("orgname", org.getName());// --组织建设和全程纪实用
		data.put("orgtype", org.getType());// 类型，用来打开不同的组织建设页面
		data.put("user", user);
		// 获取全程纪实最高权限
		Record r = Db.findFirst("select * from sys_user_role ur , sys_role r where ur.role_id=r.id and ur.user_id='"
				+ user.getId() + "' ORDER BY field(r.key,'VillageManager','StreetManager','AreaManager') DESC ");
		if (r == null) {
			data.put("auth", "none");
		} else {
			data.put("auth", r.get("key"));
		}

		renderSuccess(data, "查询成功");
	}

	/***
	 * 文件上传
	 */
	public void uploadHead() {
		String userid = getPara("userid");
		SysUser user = SysUser.dao.getByUsername(userid);// 用户

		String base64 = getPara("file");
		String basepath = this.getRequest().getSession().getServletContext().getRealPath("");
		String path = "/upload/userHead/" + DateUtil.getYear() + "/" + DateUtil.getMonth();
		String filename = UuidUtil.getUUID() + ".png";
		Base64Util.GenerateImage(base64, basepath + path, filename);
		user.setImg(path + "/" + filename);
		user.update();
		renderSuccess("上传成功");
	}

	/***
	 * 通知列表
	 */
	public void getNoticeList() {
		String userid = getPara("userid");
		List<OaNotice> noticeList = noticeService.getMyNotice(userid);
		renderSuccess(noticeList, "");
	}

	public void getNoticeDetail() {
		String id = getPara("id");
		String userid = getPara("userid");
		renderSuccess(noticeService.getNoticeDetail(id, userid), "");
	}

	/***
	 * 签收公告
	 */
	public void signNotice() {
		service.sign(getPara("userid"), getPara("id"));
		renderSuccess("签收成功");
	}

	public void backlistData() {
		String userid = getPara("userid");
		List<Record> list = service.getBackList(userid);
		renderSuccess(list, "");
	}

	/****
	 * 归还
	 */
	public void backBorrow() {
		String id = getPara("id");
		AmsAssetBorrow borrow = AmsAssetBorrow.dao.getById(id);
		borrow.setIfBack("1");
		borrow.update();
		renderSuccess("归还成功");
	}

	/***
	 * 获取通讯录
	 */
	public void getUserContactAddress() {
		renderSuccess(service.getUserContactAddress(), "");
	}

	public void getUserInfoById() {
		String id = getPara("id");
		renderSuccess(service.getUserInfoById(id), "");
	}

	public void getCustomModelPage() {
		ActReModel arm = new ActReModel();
		renderSuccess(arm.getCustomModelPage(1, 100), "");
	}
	
	
	/***
	 * 下载文件
	 */
	public void downloadFile(){
		String id = getPara("id");
		SysAttachment attachment = SysAttachment.dao.getById(id);
		String fileUrl = attachment.getUrl();
		String baseUrl = this.getRequest().getSession().getServletContext().getRealPath("");
		File file = new File(baseUrl+"/upload"+fileUrl);
		renderFile(file);
	}
	
	public void sendMailTest(){
		System.out.println("11111111");
		String username = "人民币玩家";
		String subjectText = "来自南方物业OA系统邮件";
		String usercode = "233333";
		String content ="<div style='width:640px; background:#fff; border:solid 1px #efefef; margin:0 auto; padding:35px 0 35px 0'>"+
		" <table width='560' border='0' align='center' cellpadding='0' cellspacing='0' style=' margin:0 auto; margin-left:30px; margin-right:30px;'>"+
		"    <tbody><tr>"+
		" <td><img src=''></td>"+
		"    </tr>"+
		"    <tr>"+
		"      <td>"+
		"      <h3 style='font-weight:normal; font-size:18px'>您好！亲爱的<span style='font-weight:bold; margin-left:5px;'>"+username+"</span></h3>"+
		"      <p style='margin:5px 0; padding:3px 0;color:#666; font-size:14px'>南方物业OA邮箱验证通知:</p>"+
		"      <p style='color:#666; font-size:14px'>请在24小时内使用下面验证码完成邮箱验证： "+usercode+
		"      <p style='margin:0 0 5px 0; padding:0 0 3px 0;'>"+
		"      <p style='margin:10px 0 5px 0; padding:3px 0;color:#666; font-size:14px'>如果验证码失效，请重新发送验证码。</p>"+
		"      <p style='text-align:center'><img src=''></p>"+
		"      </td>"+
		"    </tr>"+
		"    </tbody>"+
		" </table>"+
		"</div>";
		List list = new ArrayList();
		list.add("370406608@qq.com");
		MailKit.send("81313779@qq.com",list, subjectText, content);
		System.out.println("222222222");
		renderSuccess("");
	}
	
	/**上传签名
	 * 
	 */
	public void uploadSign(){
		String img = getPara("img");
		String userid = getPara("userid");
		SysUser user = SysUser.dao.getById(userid);
		String basepath = this.getRequest().getSession().getServletContext().getRealPath("");
        String pictureId = UuidUtil.getUUID();
        String filename = user.getId() + ".png";
        String finalPath = "nfwyoa/images/signImg/";
        
        new AliyunOssUtil().uplodaImg(finalPath + filename, Base64Util.GenerateImageInput(img));
        Base64Util.GenerateImage(img,basepath+"/"+finalPath,filename);
		if(StrKit.notBlank(user.getSignImg())){
			 new AliyunOssUtil().delImg(user.getSignImg());
		}
		SysUserSign oldSign = SysUserSign.dao.getByUserid(user.getId());
		if(StrKit.notNull(oldSign)){
			oldSign.delete();
		}
		user.setSignImg(new AliyunOssUtil().getPicURL(finalPath + filename));
		user.update();
		 SysUserSign newSign = new SysUserSign();
		 newSign.setId(UuidUtil.getUUID());
		 newSign.setUserid(userid);
		 newSign.setOrgid(user.getOrgid());
		 newSign.setCreatetime(DateUtil.getTime());
		 newSign.setUpdatetime(DateUtil.getTime());
		 newSign.setSignImg(new AliyunOssUtil().getPicURL(finalPath + filename));
		 newSign.setSignLocal(finalPath + filename);
		 newSign.setSignFile(img.getBytes());
		 newSign.save();
		 Map<String,Object> map = new HashMap<String,Object>();
		 map.put("ID", user.getId());
		 map.put("USERNAME", user.getUsername());
		 map.put("NAME", user.getName());
		 map.put("IDCARD", user.getIdcard());
		 map.put("sign_img", user.getSignImg()!=null?user.getSignImg():"");
		 map.put("head_img", user.getImg()!=null?user.getImg():"");
		 map.put("MOBILE", user.getMobile()!=null?user.getMobile():"");
		 map.put("MAIL", user.getEmail()!=null?user.getEmail():"");
		 String orgId = user.getOrgid();
		 map.put("ORGID", orgId);
		 SysOrg org = new SysOrg();
		 map.put("ORGNAME", org.getById(orgId)==null?"":org.getById(orgId).getName());
		 renderSuccess800(map, 1, "上传成功");
	}
	

}
