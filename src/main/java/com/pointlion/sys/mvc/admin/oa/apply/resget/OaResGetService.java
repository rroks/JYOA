package com.pointlion.sys.mvc.admin.oa.apply.resget;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ProcessEngine;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.admin.oa.common.OAConstants;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.admin.sys.dataauth.SysDataAuthTranslator;
import com.pointlion.sys.mvc.common.model.OaApplyResGet;
import com.pointlion.sys.mvc.common.utils.Constants;
import com.pointlion.sys.plugin.activiti.ActivitiPlugin;

public class OaResGetService{
	public static final OaResGetService me = new OaResGetService();
	private static final String TABLE_NAME = OaApplyResGet.tableName;
	
	/***
	 * 根据主键查询
	 */
	public OaApplyResGet getById(String id){
		return OaApplyResGet.dao.findById(id);
	}
	
	/***
	 * 获取分页
	 */
	public Page<Record> getPage(int pnum,int psize){
		String dataAuth = SysDataAuthTranslator.me.translator(TABLE_NAME);
		String sql  = " from "+dataAuth+" b LEFT JOIN act_hi_procinst p ON b.proc_ins_id=p.ID_  where 1=1 order by o.create_time desc";
		return Db.paginate(pnum, psize, " select b.*,p.PROC_DEF_ID_ defid ", sql);
	}
	
	/***
	 * 删除
	 * @param ids
	 */
	@Before(Tx.class)
	public void deleteByIds(String ids){
    	String idarr[] = ids.split(",");
    	for(String id : idarr){
    		OaApplyResGet o = me.getById(id);
    		o.delete();
    	}
	}
	
	/***
	 * 启动流程
	 */
	@Before(Tx.class)
	public void startProcess(String id){
		WorkFlowService service = new WorkFlowService();
		Map<String, Object> var = new HashMap<String,Object>();
		String procInsId = service.startProcess(id, OAConstants.DEFKEY_APPLY_RESGET,null,var);
		OaApplyResGet resget = OaApplyResGet.dao.findById(id);
		resget.setProcInsId(procInsId);
		resget.setIfSubmit(Constants.IF_SUBMIT_YES);
		resget.update();
	}
	/***
	 * 撤回
	 */
	@Before(Tx.class)
	public void callBack(String id){
		OaApplyResGet resget = OaApplyResGet.dao.findById(id);
		String procid = resget.getProcInsId();//流程实例id
    	resget.setProcInsId("");
    	resget.setIfSubmit(Constants.IF_SUBMIT_NO);
    	resget.setIfComplete(Constants.IF_COMPLETE_NO);
    	resget.update();
    	ProcessEngine pe = ActivitiPlugin.buildProcessEngine();
    	pe.getRuntimeService().deleteProcessInstance(procid, "删除流程实例");
    	pe.getHistoryService().deleteHistoricProcessInstance(procid);
	}
	
}