package com.pointlion.sys.mvc.admin.oa.common;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.common.model.OaApplyCustom;
import com.pointlion.sys.mvc.common.model.SysUser;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.plugin.shiro.ShiroKit;

public class CommonFlowService{
	public static final CommonFlowService me = new CommonFlowService();
	

	/***
	 * 获取固定流程待办任务对象
	 */
	public Record getTaskObject(String taskid,String defKey){
		String tableName = WorkFlowUtil.getTablenameByDefkey(defKey);
		Record o = Db.findFirst("select * from "+tableName+" o , v_tasklist t where t.INSID = o.proc_ins_id and t.TASKID='"+taskid+"'");
		return o;
	}
	/***
	 * 获取自定义流程待办任务对象
	 */
	public Record getCustomTaskObject(String taskid,String defKey){
		String tableName = OaApplyCustom.tableName;
		Record o = Db.findFirst("select * from "+tableName+" o , v_tasklist t where t.INSID = o.proc_ins_id and t.TASKID='"+taskid+"'");
		return o;
	}
	/***
	 * 获取已办任务对象
	 */
	public Record getBusinessObject(String id,String defkey){
		String tableName = WorkFlowUtil.getTablenameByDefkey(defkey);
		if(StrKit.isBlank(tableName)){
			tableName = OaApplyCustom.tableName;
		}
		Record o = Db.findFirst("select * from "+tableName+" o where o.id='"+id+"'");
		return o;
	}
	
	/***
	 * 获取已经办理流程DefKey集合
	 */
	public List<String> getHavedoneDefkeyList(String username){
		List<String> havedoneKeyList = new ArrayList<String>();
    	List<Record> list = Db.find("select KEY_ DEFKEY from act_hi_taskinst t ,act_re_procdef d where d.ID_=t.PROC_DEF_ID_ and t.ASSIGNEE_='"+username+"' GROUP BY d.KEY_");//所有已办数据类型
    	for(Record r:list){
    		havedoneKeyList.add(r.getStr("DEFKEY"));
    	}
    	return havedoneKeyList;
	}

	/**获取我的申请
	 * @param username
	 * @param ifComplete
	 * @return
	 */
	public int countMyApplyTaskList(String username, String ifComplete){
		SysUser user = SysUser.dao.getByUsername(username);
		String tablenameStr = "oa_apply_bank_account,oa_apply_finance,oa_contract_apply";
		String tablename[] = tablenameStr.split(",");
		List<Map<String,String>> totalList = new ArrayList<Map<String,String>>();
		for (int i = 0; i < tablename.length; i++) {
			List<Record> list = new ArrayList<Record>();
			list = new WorkFlowService().getApplyBykeySingle(tablename[i],user.getId(), ifComplete);
			if(StrKit.notNull(list)){
				String keyvalue = new WorkFlowUtil().getDefKeyByTablename(tablename[i]);
				String keyValue[] = keyvalue.split("###");
				for (Record record : list) {
					record.set("defKey", keyValue[0]);
					record.set("defName", keyValue[1]);
					record.set("defrealName", keyValue[2]);
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
				map.put("defKey", record.getStr("defKey"));
				map.put("defName", record.getStr("defName"));
				map.put("defrealName", record.getStr("defrealName"));
				totalList.add(map);
			}
//			totalList.addAll(new ArrayList(list));

		}
		System.out.println("=======================");
		System.out.println(totalList.size());
		System.out.println("=======================");
		return totalList.size();
	}
	
	/***
	 * 
	 * 启动流程图的时候，写入固定的流程变量。根据流程图不同。
	 */
//	@SuppressWarnings("rawtypes")
//	public Map<String, Object> setAttrByDefKey(String defKey,Model o){
//		Map<String, Object> var = new HashMap<String, Object>();
//		if(defKey.equals(OAConstants.DEFKEY_APPLY_COST)){//如果是报销的时候
//			String money = o.getStr("money");
//	    	if(StrKit.notBlank(money)){
//	    		Integer m = Integer.parseInt(money);
//	    		var.put("money",m);
//	    	}
//		}
//    	return var;
//	}
}