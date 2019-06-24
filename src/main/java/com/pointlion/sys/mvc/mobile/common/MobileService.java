package com.pointlion.sys.mvc.mobile.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.pointlion.sys.mvc.admin.oa.bumph.BumphConstants;
import com.pointlion.sys.mvc.admin.oa.common.OAConstants;
import com.pointlion.sys.mvc.admin.oa.common.WorkFlowUtil;
import com.pointlion.sys.mvc.admin.oa.notice.NoticeService;
import com.pointlion.sys.mvc.common.model.CmsContent;
import com.pointlion.sys.mvc.common.model.OaApplyBankAccount;
import com.pointlion.sys.mvc.common.model.OaApplyCustom;
import com.pointlion.sys.mvc.common.model.OaApplyFinance;
import com.pointlion.sys.mvc.common.model.OaBumphOrg;
import com.pointlion.sys.mvc.common.model.SysUser;
import com.pointlion.sys.mvc.common.utils.Constants;
import com.pointlion.sys.mvc.common.utils.PinYinUtil;
import com.pointlion.sys.mvc.common.utils.StringUtil;

/***
 * 手机端通知公告调用服务
 * 
 * @author Administrator
 *
 */
public class MobileService extends NoticeService {
	public static final MobileService me = new MobileService();

	/***
	 * 获取内容列表
	 * 
	 * @param type
	 * @return
	 */
	public List<CmsContent> getContentList(String type) {
		return CmsContent.dao.find("select * from cms_content t where t.type='" + type
				+ "' and t.if_publish='1' order by t.publish_time desc");
	}

	public List<CmsContent> getContentPage(String type, Integer num, Integer count) {
		Integer a = num * count - count;
		return CmsContent.dao.find("select * from cms_content t where t.type='" + type
				+ "' and t.if_publish='1' order by t.publish_time desc limit " + a + "," + count);
	}

	/***
	 * 获取banner
	 * 
	 * @param type
	 * @return
	 */
	public List<CmsContent> getBanner(String type) {
		return CmsContent.dao.find("select * from cms_content t where t.type='" + type
				+ "' and t.if_publish='1' order by t.publish_time desc");
	}

	/***
	 * 获取内容
	 * 
	 * @param type
	 * @return
	 */
	public CmsContent getContent(String id) {
		return CmsContent.dao
				.findFirst("select * from cms_content t where t.id='" + id + "' order by t.publish_time desc");
	}

	/***
	 * 获取个人单据
	 */
	public Map<String, List<Record>> getUserDocumentList(String defKey, String docType, String userId, String type, String searchText) {
		String tableName = WorkFlowUtil.getTablenameByDefkey(defKey);
		if ("1".equals(type)) {
			tableName = OaApplyCustom.tableName;
		}
		String userColumn = "userid";
		if (OAConstants.DEFKEY_BUMPH.equals(defKey)) { // 如果是公文类型
			userColumn = "sender_id";
		}
		String sql = "select * from " + tableName + " o LEFT JOIN act_hi_procinst p ON o.proc_ins_id=p.ID_ where o."
				+ userColumn + "='" + userId + "'";
		String sqlEXT = "";
		if (StrKit.notBlank(docType)) {
			sql = sql + " and o.type='" + docType + "'";
			sqlEXT += " and o.type='" + docType + "'";
		}
		if ("1".equals(type)) {
			sql = sql + " and o.def_key ='" + defKey + "'";
		}
		if(StrKit.notBlank(searchText)){
			sql += " and ( o.applyer_name like '%"+searchText+"%' or o.org_name like '%"+searchText+"%' ) ";
		}
		sql = sql + " order by o.create_time desc";
		List<Record> list = Db.find(sql);

		Map<String, List<Record>> map = new HashMap<String, List<Record>>();
		List<Record> spzList = new ArrayList<Record>();
		List<Record> wtjList = new ArrayList<Record>();
		List<Record> ywcList = new ArrayList<Record>();
		for (int i = 0; i < list.size(); i++) {
			Record r = list.get(i);
			String if_complete = r.getStr("if_complete");
			String if_submit = r.getStr("if_submit");
			if ("1".equals(if_complete)) {
				ywcList.add(r);
			} else if ("1".equals(if_submit)) {
				spzList.add(r);
			} else {
				wtjList.add(r);
			}
		}
		map.put("YWC", ywcList);
		map.put("SPZ", spzList);
		map.put("WTJ", wtjList);
		map.put("CC", getFlowCCPage(defKey,userId,sqlEXT));
		return map;
	}
	/***
	 * 查询抄送给我的
	 * --管理页面使用
	 */
	public List<Record> getFlowCCPage(String defkey ,String userid,String sqlEXT){
		String tableName = WorkFlowUtil.getTablenameByDefkey(defkey);
		if(StrKit.isBlank(tableName)){
			tableName = OaApplyCustom.tableName;
		}
		String sql = "select * from "+tableName+" o, oa_flow_carbon_c  cc, act_hi_procinst p  where o.proc_ins_id=p.ID_ and cc.business_id = o.id and cc.user_id='"+userid+"' and cc.defkey='"+defkey+"' ";
		if(StrKit.notBlank(sqlEXT)){
			sql = sql + sqlEXT;
		}
		sql = sql +" order by o.create_time desc";
		return Db.find(sql);
	}
	/**
	 * 获取单据信息
	 */
	public Record getDocument(String defKey, String id, String pageType) {
		String tableName = WorkFlowUtil.getTablenameByDefkey(defKey);
		if (tableName == null) {
			tableName = OaApplyCustom.tableName;
		}
		String sql = "";
		if("ReEdit".equals(pageType)){
			sql = "select * from " + tableName + " where proc_ins_id='" + id + "'";			
		}else{
			sql = "select * from " + tableName + " where id='" + id + "'";
		}
		Record result = Db.findFirst(sql);
		Record newResult = MobileService.modifyResult(result, defKey);
		return newResult;
	}

	/***
	 * 获取已经办理流程DefKey集合
	 */
	public List<String> getHavedoneDefkeyList(String username) {
		List<String> havedoneKeyList = new ArrayList<String>();
		List<Record> list = Db
				.find("select d.KEY_ DEFKEY,d.NAME_ DEFNAME from act_hi_taskinst t ,act_re_procdef d where d.ID_=t.PROC_DEF_ID_ and t.ASSIGNEE_='"
						+ username + "' GROUP BY d.KEY_");// 所有已办数据类型
		for (Record r : list) {
			havedoneKeyList.add(r.getStr("DEFKEY") + "####" + r.getStr("DEFNAME"));
		}
		return havedoneKeyList;
	}
	
	

	/***
	 * 获取分页
	 */
	public Page<Record> getHavedonePage(String defkey, String username, int pnum, int psize) {
		Page<Record> p = null;
		try {
			String tableName = WorkFlowUtil.getTablenameByDefkey(defkey);
			if (tableName == null) {
				tableName = OaApplyCustom.tableName;
			}
			String sql = " from " + tableName
					+ " o , (select BUSINESS_KEY_,d.ID_ defid from act_hi_identitylink i,act_hi_procinst p,act_re_procdef d where i.TYPE_='participant' and p.ID_=i.PROC_INST_ID_ and p.PROC_DEF_ID_=d.ID_ and d.KEY_='"
					+ defkey + "' and i.USER_ID_='" + username + "') tt where tt.BUSINESS_KEY_=o.id";
			p = Db.paginate(pnum, psize, " select o.*,defid ", sql);
			return p;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Record modifyResult(Record result, String defKey) {
		// 合同表关联
		if (OAConstants.DEFKEY_CONTRACT_CHANGE.equals(defKey) || OAConstants.DEFKEY_CONTRACT_INVOICE.equals(defKey)
				|| OAConstants.DEFKEY_CONTRACT_PAY.equals(defKey) || OAConstants.DEFKEY_CONTRACT_STOP.equals(defKey)) {
			String contract_id = result.getStr("contract_id");
			String sql = "select name from oa_contract where id='" + contract_id + "'";
			Record r = Db.findFirst(sql);
			result.set("contract_name", r.get("name"));
		} else if (OAConstants.DEFKEY_PROJECT_BUILD.equals(defKey)) {
			String leader = result.getStr("leader");
			if(StrKit.notBlank(leader)){
				String[] leaderArr = leader.split(",");
				String leaderNameStr = getUsersByIDArr(leaderArr);
				result.set("leader_name", leaderNameStr);
			}else{
				result.set("leader_name", "");
			}

			String member = result.getStr("member");
			if(StrKit.notBlank(member)){
				String[] memberArr = member.split(",");
				String memberNameStr = getUsersByIDArr(memberArr);
				result.set("member_name", memberNameStr);
			}else{
				result.set("member_name", "");
			}
			
		} else if (OAConstants.DEFKEY_PROJECT_CHANGE_MEMBER.equals(defKey)) {
			String project_id = result.getStr("project_id");
			if(StringUtils.isNotBlank(project_id)){
				String sql = "select project_name from oa_project where id='" + project_id + "'";
				Record r = Db.findFirst(sql);
				result.set("project_name", r.get("project_name"));
			}

			String leader = result.getStr("leader");
			String[] leaderArr = leader.split(",");
			String leaderNameStr = getUsersByIDArr(leaderArr);
			result.set("leader_name", leaderNameStr);

			String member = result.getStr("member");
			String[] memberArr = member.split(",");
			String memberNameStr = getUsersByIDArr(memberArr);
			result.set("member_name", memberNameStr);
		} else if (OAConstants.DEFKEY_PROJECT_EXPRESS_CONFIRM.equals(defKey)) {
			String project_id = result.getStr("project_id");
			if(StringUtils.isNotBlank(project_id)){
				String sql = "select project_name from oa_project where id='" + project_id + "'";
				Record r = Db.findFirst(sql);
				result.set("project_name", r.get("project_name"));
			}
		} else if (OAConstants.DEFKEY_BUMPH.equals(defKey)) {
			String bumphId = result.getStr("id");
			// 获取所有主送单位
			List<OaBumphOrg> flist = OaBumphOrg.dao.getList(bumphId, BumphConstants.TYPE_ZHUSONG);
			List<String> f = new ArrayList<String>();
			List<String> fn = new ArrayList<String>();
			for (OaBumphOrg fo : flist) {
				f.add(fo.getOrgid());
				fn.add(fo.getOrgname());
			}
			result.set("firstOrgId", StringUtil.join(f, ","));
			result.set("firstOrgName", StringUtil.join(fn, ","));
			// 获取所有抄送单位
			List<OaBumphOrg> slist = OaBumphOrg.dao.getList(bumphId, BumphConstants.TYPE_CHAOSONG);
			List<String> s = new ArrayList<String>();
			List<String> sn = new ArrayList<String>();
			for (OaBumphOrg so : slist) {
				s.add(so.getOrgid());
				sn.add(so.getOrgname());
			}
			result.set("secondOrgId", StringUtil.join(s, ","));
			result.set("secondOrgName", StringUtil.join(sn, ","));
		}else if(OaApplyFinance.class.getName().equals(WorkFlowUtil.getClassFullNameByDefKey(defKey))){//finance
			String contract_id = result.getStr("contract_id");
			if(StringUtils.isNotBlank(contract_id)){
				String sql = "select name from oa_contract where id='" + contract_id + "'";
				Record r = Db.findFirst(sql);
				if(r!=null) result.set("contract_name", r.get("name"));
			}
			String project_id = result.getStr("project_id");
			if(StringUtils.isNotBlank(project_id)){
				String sql2 = "select project_name from oa_project where id='" + project_id + "'";
				Record r2 = Db.findFirst(sql2);
				if(r2!=null) result.set("project_name", r2.get("project_name"));
			}
			String about_orgid = result.getStr("about_orgid");
			if(StringUtils.isNotBlank(project_id)){
				String sql3 = "select name from sys_org where id='" + about_orgid + "'";
				Record r3 = Db.findFirst(sql3);
				if(r3!=null) result.set("about_orgname", r3.get("name"));
			}
			
		}else if(OaApplyBankAccount.class.getName().equals(WorkFlowUtil.getClassFullNameByDefKey(defKey))){
			String contract_id = result.getStr("contract_id");
			if(StringUtils.isNotBlank(contract_id)){
				String sql = "select name from oa_contract where id='" + contract_id + "'";
				Record r = Db.findFirst(sql);
				if(r!=null) result.set("contract_name", r.get("name"));
			}
			String project_id = result.getStr("project_id");
			if(StringUtils.isNotBlank(project_id)){
				String sql2 = "select project_name from oa_project where id='" + project_id + "'";
				Record r2 = Db.findFirst(sql2);
				if(r2!=null) result.set("project_name", r2.get("project_name"));
			}
		}
		return result;
	}

	private static String getUsersByIDArr(String[] leaders) {
		String sql = "select username from sys_user where 1=1 ";
		String nameStr = "";
		for (int i = 0; i < leaders.length; i++) {
			Record r = Db.findFirst(sql + " and id='" + leaders[i] + "'");
			nameStr += r.getStr("username") + ",";
		}
		return nameStr.substring(0, nameStr.length() - 1);
	}

	public List<Record> getBackList(String userid) {
		String sql = "select * from ams_asset_borrow o LEFT JOIN act_hi_procinst p ON o.proc_ins_id=p.ID_  where o.userid='"
				+ userid + "'  and o.if_complete='1' and o.if_agree='1' and o.if_back='0' order by o.create_time desc";
		List<Record> list = Db.find(sql);
		return list;
	}

	/***
	 * 获取所有人员
	 */
	public Map<String, Object> getUserContactAddress() {
		Map<String, Object> data = new HashMap<String, Object>();
		List<SysUser> userList = SysUser.dao.getAllUser();
		List<String> groupList = new ArrayList<String>();
		String nowGroup = "";
		for (SysUser u : userList) {
			u.put("enName", PinYinUtil.getPingYin(u.getName()));// 英文名字
			String s = PinYinUtil.getAlpha(u.getName());// 首字母大写短名字
			String alpha = StrKit.notBlank(s) ? s.substring(0, 1) : "";
			u.put("alpha", alpha);// 首字母
			if (!nowGroup.equals(alpha)) {
				groupList.add(alpha);
				nowGroup = alpha;
			}
		}
		data.put("user", userList);
		data.put("group", groupList);
		return data;
	}

	public Record getUserInfoById(String id) {
		String sql = "select * from sys_user where id='" + id + "'";
		Record r = Db.findFirst(sql);
		return r;
	}

	public void resetTable(String defKey, String id) {
		String tableName = WorkFlowUtil.getTablenameByDefkey(defKey);
		if (tableName == null) {
			tableName = OaApplyCustom.tableName;
		}
		String sql = "update " + tableName + " set if_submit='" + Constants.IF_SUBMIT_NO + "',proc_ins_id='' where id='"
				+ id + "'";
		Db.update(sql);
	}

	public void deleteTable(String defKey, String id) {
		String tableName = WorkFlowUtil.getTablenameByDefkey(defKey);
		if (tableName == null) {
			tableName = OaApplyCustom.tableName;
		}
		String sql = "delete from " + tableName + " where id='"+id+"'";
		Db.update(sql);

	}
	public static List<Record> getAttachMentById(String bid) {
		String sql = "select * from sys_attachment o where o.business_id='"+bid+"'";
		List<Record> list = Db.find(sql);
		return list;
	}
	
	public static List<Record> getAttachMentByIdNotPic(String bid) {
		String sql = "select * from sys_attachment o where o.business_id='"+bid+"' and o.suffix != 'png'";
		List<Record> list = Db.find(sql);
		return list;
	}
}
