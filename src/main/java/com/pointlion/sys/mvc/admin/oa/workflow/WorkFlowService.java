package com.pointlion.sys.mvc.admin.oa.workflow;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.pointlion.sys.mvc.admin.oa.common.CommonFlowController;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.admin.oa.common.OAConstants;
import com.pointlion.sys.mvc.admin.oa.common.WorkFlowUtil;
import com.pointlion.sys.mvc.admin.sys.mobilemessage.SysMobileMessageService;
import com.pointlion.sys.mvc.common.model.ActReProcdef;
import com.pointlion.sys.mvc.common.model.OaApplyCustom;
import com.pointlion.sys.mvc.common.model.OaContract;
import com.pointlion.sys.mvc.common.model.OaFlowCarbonC;
import com.pointlion.sys.mvc.common.model.OaPicture;
import com.pointlion.sys.mvc.common.model.SysAttachment;
import com.pointlion.sys.mvc.common.model.SysOrg;
import com.pointlion.sys.mvc.common.model.SysUser;
import com.pointlion.sys.mvc.common.model.VTasklist;
import com.pointlion.sys.mvc.common.utils.Constants;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.mvc.common.utils.StringUtil;
import com.pointlion.sys.mvc.mobile.common.MobileService;
import com.pointlion.sys.plugin.activiti.ActivitiPlugin;
import com.pointlion.sys.plugin.shiro.ShiroKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkFlowService {
	public static final WorkFlowService me = new WorkFlowService();
	public static final SysMobileMessageService mobileMessageService = SysMobileMessageService.me;
	private Logger logger = LoggerFactory.getLogger(CommonFlowController.class);

	/**
	 * 创建新模型
	 * @throws UnsupportedEncodingException 
	 * */
	public void createModel(ProcessEngine pe,String name,String key) throws UnsupportedEncodingException{
		RepositoryService repositoryService = pe.getRepositoryService();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.put("stencilset", stencilSetNode);
        Model modelData = repositoryService.newModel();

        ObjectNode modelObjectNode = objectMapper.createObjectNode();
        modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, name);
        modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
        String description = StringUtils.defaultString("模型描述信息");
        modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
        modelData.setMetaInfo(modelObjectNode.toString());
        modelData.setName(name);
        modelData.setKey(StringUtils.defaultString(key));

        repositoryService.saveModel(modelData);
        repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
	}
	
	/***
	 * 部署模型
	 * @param id
	 * @return
	 */
	@Before(Tx.class)
	public String deploy(String id) {
		String message = "";
		try {
			ProcessEngine pe = ActivitiPlugin.buildProcessEngine();
			RepositoryService repositoryService = pe.getRepositoryService();
			org.activiti.engine.repository.Model modelData = repositoryService.getModel(id);
			BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
			JsonNode editorNode = new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
			BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);
			BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
			byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel,"ISO-8859-1");
			
			String processName = modelData.getName();
			if (!StringUtils.endsWith(processName, ".bpmn20.xml")){
				processName += ".bpmn20.xml";
			}
//			System.out.println("========="+processName+"============"+modelData.getName());
			ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);
			Deployment deployment = repositoryService.createDeployment().name(modelData.getName())
					.addInputStream(processName, in).deploy();
//					.addString(processName, new String(bpmnBytes)).deploy();
			
			// 设置流程分类
			List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).list();
			for (ProcessDefinition processDefinition : list) {
				repositoryService.setProcessDefinitionCategory(processDefinition.getId(), modelData.getCategory());
				message = "部署成功";
			}
			if (list.size() == 0){
				message = "部署失败，没有流程。";
			}
		} catch (Exception e) {
			throw new ActivitiException("设计模型图不正确，检查模型正确性", e);
		}
		return message;
	}
	
	/***
	 * 删除模型
	 */
	@Before(Tx.class)
	public void deleteModel(String id){
		ActivitiPlugin.buildProcessEngine().getRepositoryService().deleteModel(id);
	}
	
	/***
	 * 挂起/激活
	 */
	@Before(Tx.class)
	public String updateState(String state,String procDefId){
		if (state.equals("active")) {
			ActivitiPlugin.buildProcessEngine().getRepositoryService().activateProcessDefinitionById(procDefId, true, null);
			return "激活成功";
		} else if (state.equals("suspend")) {
			ActivitiPlugin.buildProcessEngine().getRepositoryService().suspendProcessDefinitionById(procDefId, true, null);
			return "挂起成功";
		}
		return "无操作";
	}
	
	/***
	 * 转化为模型
	 */
	@Before(Tx.class)
	public Model convertToModel(String procDefId) throws UnsupportedEncodingException, XMLStreamException {
		RepositoryService repositoryService = ActivitiPlugin.buildProcessEngine().getRepositoryService();
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefId).singleResult();
		InputStream bpmnStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(),
		processDefinition.getResourceName());
		XMLInputFactory xif = XMLInputFactory.newInstance();
		InputStreamReader in = new InputStreamReader(bpmnStream, "UTF-8");
		XMLStreamReader xtr = xif.createXMLStreamReader(in);
		BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
	
		BpmnJsonConverter converter = new BpmnJsonConverter();
		ObjectNode modelNode = converter.convertToJson(bpmnModel);
		org.activiti.engine.repository.Model modelData = repositoryService.newModel();
		modelData.setKey(processDefinition.getKey());
		modelData.setName(processDefinition.getName());
		modelData.setCategory(processDefinition.getCategory());//.getDeploymentId());
		modelData.setDeploymentId(processDefinition.getDeploymentId());
		modelData.setVersion(Integer.parseInt(String.valueOf(repositoryService.createModelQuery().modelKey(modelData.getKey()).count()+1)));
	
		ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
		modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, processDefinition.getName());
		modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, modelData.getVersion());
		modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, processDefinition.getDescription());
		modelData.setMetaInfo(modelObjectNode.toString());
	
		repositoryService.saveModel(modelData);
	
		repositoryService.addModelEditorSource(modelData.getId(), modelNode.toString().getBytes("utf-8"));
	
		return modelData;
	}
	
	/**
	 * 读取资源，通过部署ID
	 * @param processDefinitionId  流程定义ID
	 * @param processInstanceId 流程实例ID
	 * @param resourceType 资源类型(xml|image)
	 */
	public InputStream resourceRead(String procDefId, String proInsId, String resType) throws Exception {
		RuntimeService runtimeService = ActivitiPlugin.buildProcessEngine().getRuntimeService();
		RepositoryService repositoryService = ActivitiPlugin.buildProcessEngine().getRepositoryService();
		if (StringUtils.isBlank(procDefId)){
			ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(proInsId).singleResult();
			procDefId = processInstance.getProcessDefinitionId();
		}
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefId).singleResult();
		
		String resourceName = "";
		if (resType.equals("image")) {
			resourceName = processDefinition.getDiagramResourceName();
		} else if (resType.equals("xml")) {
			resourceName = processDefinition.getResourceName();
		}
		
		InputStream resourceAsStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), resourceName);
		return resourceAsStream;
	}
	/***
	 * 删除正在运行的流程
	 */
	@Before(Tx.class)
	public void deleteDeployment(String deploymentId) {
		ActivitiPlugin.buildProcessEngine().getRepositoryService().deleteDeployment(deploymentId, true);
	}
	
	/***
	 * 启动流程
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	@Before(Tx.class)
	public String startProcess(String id,String defKey,String title,Map<String, Object> var){
		return startProcess(id,defKey,title,var,ShiroKit.getUsername());
	}
	/***
	 * 启动流程,移动端，无法使用shiro---手机端使用
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	@Before(Tx.class)
	public String startProcess(String id,String defKey,String title,Map<String, Object> var,String username){
		if(var==null){
			var = new HashMap<String, Object>();
		}
		if(StrKit.notBlank(title)){
			var.put(OAConstants.WORKFLOW_VAR_APPLY_TITLE, title);
		}
		var.put(OAConstants.WORKFLOW_VAR_APPLY_USERNAME, username);
		var.put(OAConstants.WORKFLOW_VAR_PROCESS_INSTANCE_START_TIME, DateUtil.getTime());
		var = getVar(var,id,username,defKey);
		ProcessInstance procIns = ActivitiPlugin.buildProcessEngine().getRuntimeService().startProcessInstanceByKey(defKey,id,var);
		return procIns.getId();
	}
	
	
	/***
	 * 完成任务
	 */
	@Before(Tx.class)
	public void completeTask(String taskid,String username,Map<String,Object> var){
		completeTask(taskid,username,"",var);
	}
	@Before(Tx.class)
	public void completeTask(String taskid,String username,String comment,Map<String,Object> var){
		TaskService service = ActivitiPlugin.buildProcessEngine().getTaskService();
		VTasklist task = VTasklist.dao.getTaskRecord(taskid);
		String insid = task.getStr("INSID");
		if(var==null){
			var = new HashMap<String,Object>();
		}
		String pass = String.valueOf(var.get("pass"));
		if(comment==null){comment="";}
		if(Constants.FLOW_IF_AGREE_YES.equals(pass)){//如果同意
			comment = "[同意]"+comment;
		}else if(Constants.FLOW_IF_AGREE_NO.equals(pass)){//如果不同意
			comment = "[不同意]"+comment;
		}
		if(StrKit.notBlank(insid)&&StrKit.notBlank(comment)){
			service.addComment(taskid, insid, comment);
		}
		service.claim(taskid, username);
		service.complete(taskid, var);
		//发送短信
//		mobileMessageService.sendMessage(insid);
	}
	/***
	 * 查询某人的所有公文待办
	 * --首页使用
	 */
	public List<Record> getToDoListByKey(String tableName ,String key,String username){
		String sql = "select * from v_tasklist t ,"+tableName+" b where t.INSID=b.proc_ins_id and  t.DEFKEY='"+key+"'";
		if(StrKit.notBlank(username)){
			sql = sql + " and (t.ASSIGNEE='"+username+"' or t.CANDIDATE='"+username+"')";
		}
		return Db.find(sql);
	}
	
	/***
	 * 根据inisid公文待办
	 * --首页使用
	 */
	public Record getToDoListByInsID(String insId){
		String sql = "select * from v_tasklist t where t.INSID='"+insId+"'";
		return Db.findFirst(sql);
	}
	

/**
 * 获取所有流程
 * @return
 */
public List<String> getAllDefkeyList(){
	List<String> havedoneKeyList = new ArrayList<String>();
	List<Record> list = Db
			.find("select KEY_ DEFKEY,NAME_ DEFNAME from act_re_procdef GROUP BY KEY_");// 所有已办数据类型
	for (Record r : list) {
		havedoneKeyList.add(r.getStr("DEFKEY") + "####" + r.getStr("DEFNAME"));
	}
	return havedoneKeyList;
	}
	
	/**
	 * 获取所有申请事项
	 * @param tableName表明
	 * @param key
	 * @param userId
	 * @param ifComplete
	 * @return
	 */
	
	public List<Record> getApplyBykey2(String[] tableName, String userId, String ifComplete){
		String totalSql = "";
		if("0".equals(ifComplete)){
			
		}else{
			
		}
		
		for (int i = 0; i < tableName.length; i++) {
			String sql = "select b.id, b., b.create_time as createtime, b.applyer_name ,b.org_name, b.title, b.if_submit, b.if_complete, b.if_agree from v_tasklist t ,"+tableName[i]+" b where t.INSID=b.proc_ins_id";
			if(StrKit.notBlank(userId)){
				sql = sql + " and b.userid='"+userId+"'and b.if_submit='1'";
			}
			if(StrKit.notBlank(ifComplete)){
				sql = sql + " and b.if_complete='"+ifComplete+"'";
			}
			if(i==0){
				totalSql = totalSql;
			}else if(i==tableName.length-1){
				totalSql+=sql;
			}else{
				totalSql+=sql+" union all ";
			}
		}
		String end = " order by createtime desc";
		return Db.find(totalSql+end);
	}
	
	public List<Record> getApplyBykey(String[] tableName, String userId, String ifComplete){
		String totalSql = "";
		for (int i = 0; i < tableName.length; i++) {
			String sql = "select b.id, b.proc_ins_id, b.create_time as createtime, b.applyer_name ,b.org_name, b.title, b.if_submit, b.if_complete, b.if_agree from "+tableName[i]+" b where 1=1";
			if(StrKit.notBlank(userId)){
				sql = sql + " and b.userid='"+userId+"'and b.if_submit='1'";
			}
			if(StrKit.notBlank(ifComplete)){
				sql = sql + " and b.if_complete='"+ifComplete+"'";
			}
			if(i==0){
				totalSql = sql;
			}else{
				totalSql+= totalSql+ " union all "+sql;
			}
		}
		String end = " order by createtime desc";
		return Db.find(totalSql+end);
	}

	/**单张表查询任务
	 * @param tableName
	 * @param userId
	 * @param ifComplete
	 * @return
	 */
	public List<Record> getApplyBykeySingle(String tableName, String userId, String ifComplete){
		String totalSql = "";
		String sql = "select b.id,b.type,p.PROC_DEF_ID_ as defid, b.proc_ins_id, b.create_time as createtime, b.applyer_name ,b.org_name, b.title, b.if_submit, b.if_complete, b.if_agree from "+tableName+" b left join act_hi_procinst p on p.PROC_INST_ID_ = b.proc_ins_id  where 1=1";
		if(StrKit.notBlank(userId)){
			sql = sql + " and b.userid='"+userId+"'and b.if_submit='1' and b.if_customflow='0'";
		}
		if(StrKit.notBlank(ifComplete)){
			sql = sql + " and b.if_complete='"+ifComplete+"'";
		}
		String end = " order by createtime desc";
		return Db.find(sql+end);
	}
	
	/***
	 * 查询所有待办
	 */
	public List<Record> getToDoListByUsername(String username){
		String sql = "select u.UserName,tt.* from (SELECT e.BUSINESS_KEY_ businessId,v.TEXT_ username,t.* FROM v_tasklist t,act_ru_execution e, act_ru_variable v WHERE	e.PROC_INST_ID_ = t.INSID AND t.INSID = v.PROC_INST_ID_ AND v.NAME_ = '"+OAConstants.WORKFLOW_VAR_APPLY_USERNAME+"'";
		if(StrKit.notBlank(username)){
			sql = sql + " and (t.ASSIGNEE='"+username+"' or t.CANDIDATE='"+username+"')";
		}
		sql = sql + " ORDER BY t.DEFNAME) tt LEFT JOIN sys_user u on u.username=tt.username ";
		return Db.find(sql);
	}

//	/***
//	 * 查询某人的待办条目
//	 * --管理页面使用
//	 */
//	public Page<Record> getToDoPageByKey(int pnum,int psize,String tableName,String key ,String username){
//		String sql = " from v_tasklist t ,"+tableName+" b where t.INSID=b.proc_ins_id ";
//		if(StrKit.notBlank(key)){
//			sql = sql + "and  t.DEFKEY='"+key+"'";
//		}
//		if(StrKit.notBlank(username)){
//			sql = sql + " and (t.ASSIGNEE='"+username+"' or t.CANDIDATE='"+username+"')";
//		}
//		return Db.paginate(pnum, psize, "select * ",sql);
//	}
	/***
	 * 查询某人的待办条目
	 * --管理页面使用
	 */
	public Page<Record> getToDoPageByKey(int pnum,int psize,String key ,String username,String sqlEXT){
		String tableName = WorkFlowUtil.getTablenameByDefkey(key);
		String sql = " from v_tasklist t ,"+tableName+" o,act_hi_procinst p where o.proc_ins_id=p.ID_ and t.INSID=o.proc_ins_id ";
		if(StrKit.notBlank(key)){
			sql = sql + "and  t.DEFKEY='"+key+"'";
		}
		if(StrKit.notBlank(username)){
			sql = sql + " and (t.ASSIGNEE='"+username+"' or t.CANDIDATE='"+username+"')";
		}
		if(StrKit.notBlank(sqlEXT)){
			sql = sql + sqlEXT;
		}
		sql = sql +" order by o.create_time desc";
		return Db.paginate(pnum, psize, "select *,p.PROC_DEF_ID_ defid  ",sql);
	}
	
	/***
	 * 查询某人的申请
	 * --管理页面使用
	 */
	public Page<Record> getApplyPageByKey(int pnum,int psize,String key ,String username,String sqlEXT){
		String tableName = WorkFlowUtil.getTablenameByDefkey(key);
		String sql = " from v_tasklist t ,"+tableName+" o,act_hi_procinst p where o.proc_ins_id=p.ID_ and t.INSID=o.proc_ins_id ";
		if(StrKit.notBlank(key)){
			sql = sql + "and  t.DEFKEY='"+key+"'";
		}
		if(StrKit.notBlank(username)){
			sql = sql + " and (t.ASSIGNEE='"+username+"' or t.CANDIDATE='"+username+"')";
		}
		if(StrKit.notBlank(sqlEXT)){
			sql = sql + sqlEXT;
		}
		sql = sql +" order by o.create_time desc";
		return Db.paginate(pnum, psize, "select *,p.PROC_DEF_ID_ defid  ",sql);
	}
	
	
	/***
	 * 查询某组织的申请
	 * --管理页面使用
	 */
	public Page<Record> getApplyPageByKeyAndOrgid(int pnum,int psize,String key ,String orgids,String type, String ifSubmit,String sqlEXT){
		String tableName = WorkFlowUtil.getTablenameByDefkey(key);
		String sql = " from "+tableName+" o,act_hi_procinst p where o.proc_ins_id=p.ID_ and o.type='"+type+"' and if_submit = '"+ifSubmit+"'";
		if(StrKit.notBlank(orgids)){
			if(StrKit.isBlank(orgids.substring(1))){
				sql = sql + " and org_id ="+orgids+"";
			}else{
				sql = sql + " and org_id in("+ orgids +")";
			}
		}
		if(StrKit.notBlank(sqlEXT)){
			sql = sql + sqlEXT;
		}
		
		sql = sql +" order by o.create_time desc";
		return Db.paginate(pnum, psize, "select *,p.PROC_DEF_ID_ defid  ",sql);
	}
	
	
	/***
	 * 获取已办分页
	 * --首页和管理页面使用
	 */
	public Page<Record> getHavedonePage(int pnum,int psize,String defkey,String username,String sqlEXT){
		String tableName = WorkFlowUtil.getTablenameByDefkey(defkey);
		if(StrKit.isBlank(tableName)){
			tableName = OaApplyCustom.tableName;
		}
		String sql = "FROM "+tableName+" o, ( SELECT DISTINCT p.BUSINESS_KEY_, d.ID_ defid FROM act_hi_taskinst t, act_hi_procinst p, act_re_procdef d WHERE t.ASSIGNEE_='"+username+"' AND p.PROC_DEF_ID_ = d.ID_ AND d.KEY_ = '"+defkey+"' AND t.END_TIME_ is not NULL AND t.DELETE_REASON_='completed' AND t.PROC_INST_ID_ = p.ID_) tt WHERE tt.BUSINESS_KEY_ = o.id ";
//		String sql  = " from "+tableName+" o , (select BUSINESS_KEY_,d.ID_ defid from act_hi_identitylink i,act_hi_procinst p,act_re_procdef d where i.TYPE_='participant' and p.ID_=i.PROC_INST_ID_ and p.PROC_DEF_ID_=d.ID_ and d.KEY_='"+defkey+"' and i.USER_ID_='"+username+"') tt where tt.BUSINESS_KEY_=o.id";
		if(StrKit.notBlank(sqlEXT)){
			sql = sql + sqlEXT;
		}
		sql = sql +" order by o.create_time desc";
		return Db.paginate(pnum, psize, " select o.*,defid ", sql);
	}
	
	/***
	 * 获取已办分页
	 * --首页和管理页面使用
	 */
	public List<Record> getHavedoneList(String defkey,String username,String sqlEXT){
		String tableName = WorkFlowUtil.getTablenameByDefkey(defkey);
		if(StrKit.isBlank(tableName)){
			tableName = OaApplyCustom.tableName;
		}
		String sql = "FROM "+tableName+" o, ( SELECT DISTINCT p.BUSINESS_KEY_, d.ID_ defid FROM act_hi_taskinst t, act_hi_procinst p, act_re_procdef d WHERE t.ASSIGNEE_='"+username+"' AND p.PROC_DEF_ID_ = d.ID_ AND d.KEY_ = '"+defkey+"' AND t.END_TIME_ is not NULL AND t.DELETE_REASON_='completed' AND t.PROC_INST_ID_ = p.ID_) tt WHERE tt.BUSINESS_KEY_ = o.id ";
//		String sql  = " from "+tableName+" o , (select BUSINESS_KEY_,d.ID_ defid from act_hi_identitylink i,act_hi_procinst p,act_re_procdef d where i.TYPE_='participant' and p.ID_=i.PROC_INST_ID_ and p.PROC_DEF_ID_=d.ID_ and d.KEY_='"+defkey+"' and i.USER_ID_='"+username+"') tt where tt.BUSINESS_KEY_=o.id";
		if(StrKit.notBlank(sqlEXT)){
			sql = sql + sqlEXT;
		}
		sql = sql +" order by o.create_time desc";
		return Db.find(" select o.*,defid "+sql);
	}
	
	/**
	 * @param pnum
	 * @param psize
	 * @param defList
	 * @param userid
	 * @param username
	 * @param ifcomplete
	 * @param sqlEXT
	 * @return
	 */
	public Page<Record> getApplyPage(int pnum,int psize,List<String> defList,String userid, String username,String ifcomplete,String sqlEXT){
		String totalSql = "";
		for (int i = 0; i < defList.size(); i++) {
			String[] strArr = defList.get(i).split("####");
			System.out.println(defList.get(i));
			String defKey = strArr[0];
			String defName = strArr[1];
			String tableName = WorkFlowUtil.getTablenameByDefkey(defKey);
			if(StrKit.isBlank(tableName)){
				tableName = OaApplyCustom.tableName;
			}
			String sql = "";
			sql = "select o.id,o.userid,defname,o.create_time as createtime,o.applyer_name,o.org_name,o.title,o.if_submit,o.if_complete,defid,defKey  FROM "+tableName+" o, ( SELECT DISTINCT p.BUSINESS_KEY_,d.KEY_ defKey,d.NAME_ defname, d.ID_ defid FROM act_hi_taskinst t, act_hi_procinst p, act_re_procdef d WHERE t.ASSIGNEE_='"+username+"' AND p.PROC_DEF_ID_ = d.ID_ AND d.KEY_ = '"+defKey+"' AND t.END_TIME_ is not NULL AND t.DELETE_REASON_='completed' AND t.PROC_INST_ID_ = p.ID_) tt WHERE tt.BUSINESS_KEY_ = o.id and o.if_complete ='"+ifcomplete+"' and o.userid ='"+userid+"'";
			if(StrKit.notBlank(sqlEXT)){
				sql = sql + sqlEXT;
			}
			if(i==0){
				totalSql = sql;
			}else{
				totalSql+=" union all "+sql;
			}
		}
		System.out.println(totalSql);
		totalSql = "from ("+totalSql +") ff where ff.if_complete ='"+ifcomplete+"' and ff.userid ='"+userid+"' order by createtime desc";
		System.out.println(totalSql);
		return Db.paginate(pnum, psize, "select ff.* ", totalSql);
	}
	
	public Page<Record> getHavedonePage2(int pnum,int psize,List<String> defList,String userid, String username,String ifcomplete,String sqlEXT){
		String totalSql = "";
		for (int i = 0; i < defList.size(); i++) {
			String[] strArr = defList.get(i).split("####");
			System.out.println(defList.get(i));
			String defKey = strArr[0];
			String defName = strArr[1];
			String tableName = WorkFlowUtil.getTablenameByDefkey(defKey);
			if(StrKit.isBlank(tableName)){
				tableName = OaApplyCustom.tableName;
			}
			String sql = "";
			sql = "select o.id,o.userid,defname,o.create_time as createtime,o.applyer_name,o.org_name,o.title,o.if_submit,o.if_complete,defid,defKey  FROM "+tableName+" o, ( SELECT DISTINCT p.BUSINESS_KEY_,d.KEY_ defKey,d.NAME_ defname, d.ID_ defid FROM act_hi_taskinst t, act_hi_procinst p, act_re_procdef d WHERE t.ASSIGNEE_='"+username+"' AND p.PROC_DEF_ID_ = d.ID_ AND d.KEY_ = '"+defKey+"' AND t.END_TIME_ is not NULL AND t.DELETE_REASON_='completed' AND t.PROC_INST_ID_ = p.ID_) tt WHERE tt.BUSINESS_KEY_ = o.id ";
			if(StrKit.notBlank(sqlEXT)){
				sql = sql + sqlEXT;
			}
			if(i==0){
				totalSql = sql;
			}else{
				totalSql+=" union all "+sql;
			}
		}
		System.out.println(totalSql);
		totalSql = "from ("+totalSql +") ff where ff.if_complete ='"+ifcomplete+"' and ff.userid ='"+userid+"' order by createtime desc";
		System.out.println(totalSql);
//		String[] strArr = defList.get(0).split("####");
//		String defKey = strArr[0];
//		String defName = strArr[1];
//		String tableName = WorkFlowUtil.getTablenameByDefkey(defKey);
//		if(StrKit.isBlank(tableName)){
//			tableName = OaApplyCustom.tableName;
//		}
//		String sql = "select o.id,o.create_time as createtime,o.applyer_name,o.org_name,o.title,o.if_submit,o.if_complete,defid,defKey  FROM "+tableName+" o, ( SELECT DISTINCT p.BUSINESS_KEY_,d.KEY_ defKey, d.ID_ defid FROM act_hi_taskinst t, act_hi_procinst p, act_re_procdef d WHERE t.ASSIGNEE_='"+username+"' AND p.PROC_DEF_ID_ = d.ID_ AND d.KEY_ = '"+defKey+"' AND t.END_TIME_ is not NULL AND t.DELETE_REASON_='completed' AND t.PROC_INST_ID_ = p.ID_) tt WHERE tt.BUSINESS_KEY_ = o.id ";
		return Db.paginate(pnum, psize, "select ff.* ", totalSql);
//		return Db.find(totalSql);
	}
	/***
	 * 查询抄送给我的
	 * --管理页面使用
	 */
	public Page<Record> getFlowCCPage(int pnum,int psize,String key ,String userid,String sqlEXT){
		String tableName = WorkFlowUtil.getTablenameByDefkey(key);
		if(StrKit.isBlank(tableName)){
			tableName = OaApplyCustom.tableName;
		}
		String sql = " from "+tableName+" o, oa_flow_carbon_c  cc, act_hi_procinst p  where o.proc_ins_id=p.ID_ and cc.business_id = o.id and cc.user_id='"+userid+"' and cc.defkey='"+key+"' ";
		if(StrKit.notBlank(sqlEXT)){
			sql = sql + sqlEXT;
		}
		sql = sql +" order by o.create_time desc";
		return Db.paginate(pnum, psize, "select o.*,p.PROC_DEF_ID_ defid  ",sql);
	}
	/***
	 * 获取流转历史
	 */
	public List<Record> getHisTaskList2(String insid){
		List<Record> taskList = Db.find("select  t.NAME_ taskName,t.ASSIGNEE_ assignee,t.EXECUTION_ID_ exeId,t.ID_ taskId,t.END_TIME_ endTime,c.MESSAGE_ message from act_hi_taskinst t LEFT JOIN act_hi_comment c  ON c.TASK_ID_=t.ID_ where t.proc_inst_id_ = '"+insid+"' order by t.end_time_ is null,t.end_time_ asc");
		logger.debug("========================= getHisTaskList2 \n" + taskList.size());
		for(Record r : taskList){
			String assignee = r.getStr("assignee");
			if(StrKit.notBlank(assignee)){
				SysUser user  = SysUser.dao.getByUsername(assignee);
				if(user!=null){
					r.set("assigneeId", user.getId());
					r.set("assignee", user.getName()+"["+assignee+"]");
				}else{
					r.set("assignee", "无用户["+assignee+"]");
				}
			}else{
				List<VTasklist> tl = VTasklist.dao.find("select * from v_tasklist t where t.TASKID='"+r.getStr("taskId")+"'");
				List<String> cl = new ArrayList<String>();
				for(VTasklist t : tl){
					String c = t.getCANDIDATE();
					if(StrKit.notBlank(c)){
						SysUser u = SysUser.dao.getByUsername(c);
						if(u!=null){
							cl.add(u.getName()+"["+c+"]");
						}else{
							cl.add("无用户["+c+"]");
						}
					}else{
						cl.add("无用户["+c+"]");
					}
					
				}
				r.set("assignee", StringUtils.join(cl,","));
			}
			String message = r.getStr("message");
			if(message==null){
				r.set("message","");
			}
			String endtime = r.getStr("endTime");
			r.set("endTime", StrKit.notBlank(endtime)?endtime.substring(0,endtime.indexOf(".")):"");
		}
		return taskList;
//		return Db.find("SELECT t.assignee_,	u.name,	t.name_,	t.end_time_,	c.message_ FROM	sys_user u ,	act_hi_taskinst t LEFT JOIN act_hi_comment c ON t.id_ = c.task_id_ where u.username=t.ASSIGNEE_ AND t.proc_inst_id_ = '"+insid+"' ORDER BY t.end_time_ desc ");
	}
	
	
	/***
	 * 获取流转历史(用于打印显示，去除账号信息)
	 */
	public List<Record> getHisTaskList(String insid){
		List<Record> taskList = Db.find("select  t.NAME_ taskName,t.ASSIGNEE_ assignee,t.EXECUTION_ID_ exeId,t.ID_ taskId,t.END_TIME_ endTime,c.MESSAGE_ message from act_hi_taskinst t LEFT JOIN act_hi_comment c  ON c.TASK_ID_=t.ID_ where t.proc_inst_id_ = '"+insid+"' order by t.end_time_ is null,t.end_time_ asc");
		for(Record r : taskList){
			String assignee = r.getStr("assignee");
			if(StrKit.notBlank(assignee)){
				SysUser user  = SysUser.dao.getByUsername(assignee);
				if(user!=null){
					r.set("assigneeId", user.getId());
					r.set("assignee", user.getName());
				}else{
					r.set("assignee", "无用户");
				}
			}else{
				List<VTasklist> tl = VTasklist.dao.find("select * from v_tasklist t where t.TASKID='"+r.getStr("taskId")+"'");
				List<String> cl = new ArrayList<String>();
				List<String> c2 = new ArrayList<String>();
				for(VTasklist t : tl){
					String c = t.getCANDIDATE();
					if(StrKit.notBlank(c)){
						SysUser u = SysUser.dao.getByUsername(c);
						if(u!=null){
							cl.add(u.getName());
						}else{
							cl.add("无用户");
						}
					}else{
						cl.add("无用户");
					}
					
				}
				r.set("assigneeId", "");
				r.set("assignee", StringUtils.join(cl,","));
			}
			String message = r.getStr("message");
			if(message==null){
				r.set("message","");
			}
			String endtime = r.getStr("endTime");
			r.set("endTime", StrKit.notBlank(endtime)?endtime.substring(0,endtime.indexOf(".")):"");
		}
		return taskList;
//		return Db.find("SELECT t.assignee_,	u.name,	t.name_,	t.end_time_,	c.message_ FROM	sys_user u ,	act_hi_taskinst t LEFT JOIN act_hi_comment c ON t.id_ = c.task_id_ where u.username=t.ASSIGNEE_ AND t.proc_inst_id_ = '"+insid+"' ORDER BY t.end_time_ desc ");
	}
	
	
	
	
	
	/***
	 * 获取流程经办人数据
	 */
	public List<Record> getHisTaskParter(String insid){
		return Db.find("select i.*,u.name from act_hi_identitylink i,sys_user u where u.username=i.USER_ID_ AND PROC_INST_ID_='"+insid+"'");
	}
	
	/***
	 * 根据流程实例id获取流程定义ID
	 * @param insid
	 * @return
	 */
	public String getDefIdByInsId(String insid){
		Record proc = Db.findFirst("select * from act_hi_procinst p where p.PROC_INST_ID_=?",insid);
		return proc.getStr("PROC_DEF_ID_");
	}
	
	/***
	 * 根据流程defkey获取流程定义名称
	 * @param defKey
	 * @return
	 */
	public String getDefNameByDefKey(String defKey){
		ActReProcdef def = ActReProcdef.dao.findFirst("select * from act_re_procdef p where p.KEY_='"+defKey+"' ORDER BY VERSION_ DESC");
		if(def!=null){
			return def.getName();
		}else{
			return "";
		}
	}
	
	/***
	 * 删除流程实例
	 */
	public void deleteIns(String procid){//正式使用时，需要将try...catch注释掉
		ProcessEngine pe = ActivitiPlugin.buildProcessEngine();
		Record run = Db.findFirst("select * from act_ru_execution t where t.PROC_INST_ID_='"+procid+"'");
		if(run!=null){
//			try{
				pe.getRuntimeService().deleteProcessInstance(procid, "删除流程实例");
//			}catch(Exception e){
//				e.printStackTrace();
//			}
		}
		Record his = Db.findFirst("select * from act_hi_procinst t where t.PROC_INST_ID_='"+procid+"'");
		if(his!=null){
//			try{
				pe.getHistoryService().deleteHistoricProcessInstance(procid);
//			}catch(Exception e){
//				e.printStackTrace();
//			}
		}
	}
	
	/**回退到上一流程节点
	 * @param procid
	 */
	public void callBack2LastStep(String procid){
		
	}
	
	/***
	 * 撤回流程
	 */
	public void callBack(String procid){
		deleteIns(procid);
	}
	
	
	
	//---------------- mobile  zhouzhongyan --------------------
	/**
	 * 获取待办list
	 * @author 28995
	 * @param username
	 * @return
	 */
	public List<Record> getToDoList(String username){
		String sql = "select *,INSID proc_ins_id,DEFKEY defKey from v_tasklist where 1=1 ";
		if(StrKit.notBlank(username)){
			sql = sql + " and ASSIGNEE='"+username+"' or CANDIDATE='"+username+"'";
		}
		return Db.find(sql);
	}
	
	
	public Page<Record> getToDoListPage(int pageNum, int pageSize, String username){
		String sql = "from v_tasklist where 1=1 ";
		if(StrKit.notBlank(username)){
			sql = sql + " and ASSIGNEE='"+username+"' or CANDIDATE='"+username+"'";
		}
		return Db.paginate(pageNum, pageSize, "select * ", sql);
	}
	
	public Map<String,Object> getTodoInfos(String insId,String defKey){
		Map<String,Object> map = new HashMap<String,Object>();
		String sql = "select * from v_tasklist WHERE INSID='"+insId+"'";
		Record r = Db.findFirst(sql);
		String tableName = WorkFlowUtil.getTablenameByDefkey(defKey);
		if(StrKit.isBlank(tableName)){
			tableName = OaApplyCustom.tableName;
		}
		try{
			String msql = "select * from "+tableName+" where proc_ins_id ='"+insId+"'";
			Record result = Db.findFirst(msql);
			Record newResult = MobileService.modifyResult(result,defKey);
			String bumphId = result.getStr("id");
			
			List<Record> attachmentList = MobileService.getAttachMentByIdNotPic(bumphId);
			
			List<OaFlowCarbonC> cclist = OaFlowCarbonC.dao.find("select * from oa_flow_carbon_c c ,sys_user u  where u.id=c.user_id and business_id = '"+bumphId+"' ");
			List<String> idList = new ArrayList<String>();
			List<String> nameList = new ArrayList<String>();
			List flowList = new ArrayList();
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
			newResult.set("FLOWCC", StringUtils.join(idList,","));
			newResult.set("FLOWCCNAME", StringUtils.join(nameList,","));
			newResult.set("FLOWCCLIST", flowList);
			map.put("Model", newResult);
			map.put("Task", r);
			map.put("Flow", getHisTaskList(insId));
			map.put("ATTACHS", attachmentList);
			//查找流程相关图片
//			List<OaPicture> picList = OaPicture.dao.getBylinkidAndDefKey(bumphId,defKey);
//			String imgs = "";
//			List imgList = new ArrayList();
//			if(picList.size()>0){
//				for (OaPicture pic : picList) {
//					imgList.add(pic.getImgurl());
//				}
//				imgs = StringUtil.join(imgList, ",");
//			}
//			map.put("IMGS", picList);
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
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		return map;
	}
	/**
	 * 获取待办数目
	 * @author 28995
	 * @param username
	 * @return
	 */
	public int getTodoNum(String username){
		String sql = "select count(1) NUM from v_tasklist where 1=1 ";
		if(StrKit.notBlank(username)){
			sql = sql + " and ASSIGNEE='"+username+"' or CANDIDATE='"+username+"'";
		}
		Record result =  Db.findFirst(sql);
		return Integer.parseInt(result.getStr("NUM"));
	}
	public int getHaveDoneNum(String username){
		String sql = "select COUNT(1) NUM FROM oa_contract_apply o, ( SELECT DISTINCT p.BUSINESS_KEY_, d.ID_ defid FROM act_hi_taskinst t,"
				+ " act_hi_procinst p, act_re_procdef d WHERE t.ASSIGNEE_='"+username+"' AND p.PROC_DEF_ID_ = d.ID_ AND t.END_TIME_ is not NULL"
				+ " AND t.DELETE_REASON_='completed' AND t.PROC_INST_ID_ = p.ID_) tt WHERE tt.BUSINESS_KEY_ = o.id  order by o.create_time desc";

		Record re = Db.findFirst(sql);
		return Integer.parseInt(re.getStr("NUM"));
		
	}
	
	
	/**已办合同流程
	 * @param username
	 * @return
	 */
	public int getContractHaveDoneNum(String username){
		String sql = "select COUNT(1) NUM FROM oa_contract_apply o, ( SELECT DISTINCT p.BUSINESS_KEY_, d.ID_ defid FROM act_hi_taskinst t,"
				+ " act_hi_procinst p, act_re_procdef d WHERE t.ASSIGNEE_='"+username+"' AND p.PROC_DEF_ID_ = d.ID_ AND t.END_TIME_ is not NULL"
				+ " AND t.DELETE_REASON_='completed' AND t.PROC_INST_ID_ = p.ID_) tt WHERE tt.BUSINESS_KEY_ = o.id  order by o.create_time desc";

		Record re = Db.findFirst(sql);
		return Integer.parseInt(re.getStr("NUM"));
		
	}
	
	/**已办财务流程
	 * @param username
	 * @return
	 */
	public int getFinanceHaveDoneNum(String username){
		String sql = "select COUNT(1) NUM FROM oa_apply_finance o, ( SELECT DISTINCT p.BUSINESS_KEY_, d.ID_ defid FROM act_hi_taskinst t,"
				+ " act_hi_procinst p, act_re_procdef d WHERE t.ASSIGNEE_='"+username+"' AND p.PROC_DEF_ID_ = d.ID_ AND t.END_TIME_ is not NULL"
				+ " AND t.DELETE_REASON_='completed' AND t.PROC_INST_ID_ = p.ID_) tt WHERE tt.BUSINESS_KEY_ = o.id  order by o.create_time desc";

		Record re = Db.findFirst(sql);
		return Integer.parseInt(re.getStr("NUM"));
		
	}
	
	/**已办银行流程
	 * @param username
	 * @return
	 */
	public int getBankAccountHaveDoneNum(String username){
		String sql = "select COUNT(1) NUM FROM oa_apply_bank_account o, ( SELECT DISTINCT p.BUSINESS_KEY_, d.ID_ defid FROM act_hi_taskinst t,"
				+ " act_hi_procinst p, act_re_procdef d WHERE t.ASSIGNEE_='"+username+"' AND p.PROC_DEF_ID_ = d.ID_ AND t.END_TIME_ is not NULL"
				+ " AND t.DELETE_REASON_='completed' AND t.PROC_INST_ID_ = p.ID_) tt WHERE tt.BUSINESS_KEY_ = o.id  order by o.create_time desc";

		Record re = Db.findFirst(sql);
		return Integer.parseInt(re.getStr("NUM"));
		
	}
	
	/***
	 * 流程取回
	 */
	public void callBackTask(String taskId){
		ProcessEngine pe =  ActivitiPlugin.buildProcessEngine();
		HistoryService historyService = pe.getHistoryService();
		RuntimeService runTimeService = pe.getRuntimeService();
		RepositoryService repositoryService = pe.getRepositoryService();
		TaskService taskService = pe.getTaskService();
		try {
            Map<String, Object> variables;
            // 取得当前任务
            HistoricTaskInstance currTask =historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
            // 取得流程实例
            ProcessInstance instance = runTimeService.createProcessInstanceQuery().processInstanceId(currTask.getProcessInstanceId()).singleResult();
            if (instance == null) {
//                log.error("流程已经结束");
            }
            variables=instance.getProcessVariables();
            // 取得流程定义
            ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(currTask.getProcessDefinitionId());
            if (definition == null) {
//                log.error("流程定义未找到");
            }
            // 取得下一步活动
            ActivityImpl currActivity = ((ProcessDefinitionImpl) definition).findActivity(currTask.getTaskDefinitionKey());
            List<PvmTransition> nextTransitionList = currActivity.getOutgoingTransitions();
            for (PvmTransition nextTransition : nextTransitionList) {
                PvmActivity nextActivity = nextTransition.getDestination();
                List<HistoricTaskInstance> completeTasks = historyService.createHistoricTaskInstanceQuery().processInstanceId(instance.getId()).taskDefinitionKey(nextActivity.getId()).finished().list();
                int finished = completeTasks.size();
                if (finished > 0) {
//                    log.error("存在已经完成的下一步，流程不能取回");
                }
                List<Task> nextTasks = taskService.createTaskQuery().processInstanceId(instance.getId()).taskDefinitionKey(nextActivity.getId()).list();
                for (Task nextTask : nextTasks) {
                    //取活动，清除活动方向
                    List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();
                    List<PvmTransition> pvmTransitionList = nextActivity.getOutgoingTransitions();
                    for (PvmTransition pvmTransition : pvmTransitionList) {
                        oriPvmTransitionList.add(pvmTransition);
                    }
                    pvmTransitionList.clear();
                    //建立新方向
                    ActivityImpl nextActivityImpl = ((ProcessDefinitionImpl) definition).findActivity(nextTask.getTaskDefinitionKey());
                    TransitionImpl newTransition = nextActivityImpl.createOutgoingTransition();
                    newTransition.setDestination(currActivity);
                    //完成任务
                    taskService.complete(nextTask.getId(), variables);
                    historyService.deleteHistoricTaskInstance(nextTask.getId());
                    //恢复方向
                    currActivity.getIncomingTransitions().remove(newTransition);
                    List<PvmTransition> pvmTList = nextActivity.getOutgoingTransitions();
                    pvmTList.clear();
                    for (PvmTransition pvmTransition : oriPvmTransitionList) {
                        pvmTransitionList.add(pvmTransition);
                    }
                }
            }
            historyService.deleteHistoricTaskInstance(currTask.getId());
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	public void updateIfCompleteAndIfAgree(String tableName,String pass,String id){
		if(StrKit.isBlank(tableName)){//如果固定流程里没有，则为自定义流程
			tableName = OaApplyCustom.tableName;
		}
		String ifAgree = Constants.IF_AGREE_NO;//不同意
		if(Constants.SUBMIT_PASS_YES.equals(pass)){//同意
			ifAgree = Constants.IF_AGREE_YES;
		}
		Db.update("UPDATE "+tableName+" SET if_complete = '"+Constants.IF_COMPLETE_YES+"' WHERE id = '"+id+"' ");//更新为已经完成
		Db.update("UPDATE "+tableName+" SET if_agree = '"+ifAgree+"' WHERE id = '"+id+"' ");//是否同意
	}
	
	
	/***
	 * 组织必要的流程变量
	 * @param var
	 * @param username
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, Object> getVar(Map<String, Object> var,String id,String username,String defkey){
		SysUser user = SysUser.dao.getByUsername(username);
		SysOrg org = SysOrg.dao.getById(user.getOrgid());
		if(var==null){
			var = new HashMap<String, Object>();
		}
		/*******************部门角色***********************/
		//设置所在部门经理   ------- 开始
		List<SysUser> list = SysUser.dao.getUserByOrgidAndRoleKey3(user.getOrgid(),OAConstants.WORKFLOW_ROLE_KEY_OrgLeader);//查询当前单位的所有经理
//		if(list==null||list.size()==0){//找父级
//			String pid=org.getParentId();
//			list = SysUser.dao.getUserByOrgidAndRoleKey(pid, OAConstants.WORKFLOW_ROLE_KEY_OrgLeader);//查询父级单位的所有经理
//			if(list==null||list.size()==0){//找父父级
//				SysOrg porg = SysOrg.dao.getById(org.getParentId());//父级
//				String ppid = porg.getParentId();
//				list = SysUser.dao.getUserByOrgidAndRoleKey(ppid, OAConstants.WORKFLOW_ROLE_KEY_OrgLeader);//查询当前单位的所有经理
//			}
//		}
		if(list==null||list.size()==0){
			var.put(OAConstants.WORKFLOW_VAR_APPLY_VAR_MY_ORG_LEADER_EXIT, "0");
//			var.put(OAConstants.WORKFLOW_VAR_APPLY_MY_ORG_LEADER, username);//设置自己为
			var.put(OAConstants.WORKFLOW_VAR_APPLY_MY_ORG_LEADER, "");//先设置为空
		}else{
			List<String> userlist = new ArrayList<String>();
			for(SysUser u : list){
				userlist.add(u.getUsername());
			}
			System.out.println("orgLeader exits");
			var.put(OAConstants.WORKFLOW_VAR_APPLY_VAR_MY_ORG_LEADER_EXIT, "1");
			var.put(OAConstants.WORKFLOW_VAR_APPLY_MY_ORG_LEADER, StringUtils.join(userlist,","));//设置经理为办理人
		}
		/*******************项目经理角色START***********************/
		List<SysUser> listPro = SysUser.dao.getUserByOrgidAndRoleKey3(user.getOrgid(),OAConstants.WORKFLOW_ROLE_KEY_ProLeader);//查询当前用户所在项目的项目经理
		if(listPro==null||listPro.size()==0){
			var.put(OAConstants.WORKFLOW_VAR_APPLY_VAR_XMJL_EXIT, "0");//设置为无项目经理审批
		}else{
			var.put(OAConstants.WORKFLOW_VAR_APPLY_VAR_XMJL_EXIT, "1");//设置为有项目经理审批
			List<String> userlist = new ArrayList<String>();
			for(SysUser u : listPro){
				userlist.add(u.getUsername());
			}
			var.put(OAConstants.WORKFLOW_VAR_APPLY_MY_PROJECT_LEADER, StringUtils.join(userlist,","));//设置经理为办理人
		}
		
		/***********************项目经理END***********************/
		/*******************金额变量********************/
		//设置金额------开始
		try {
			Class<?> userClass;
			String className = WorkFlowUtil.getClassFullNameByDefKey(defkey);
			userClass = Class.forName(className);
			com.jfinal.plugin.activerecord.Model m = (com.jfinal.plugin.activerecord.Model) userClass.newInstance();
			com.jfinal.plugin.activerecord.Model o = m.findById(id);//对象id
			var = getVarBusiness(var,user,org,defkey,o);//针对业务的流程变量
			if(o!=null){
				//如果是财务类型的流程
				if(defkey.indexOf("Finance")>=0||defkey.indexOf("Contract")>=0){//如果是【财务】或者【合同】的流程----设置金额变量
					String moneyStr = "0";
					if(defkey.indexOf("Finance")>=0){//如果是财务流程
						if(defkey.equals(OAConstants.DEFKEY_APPLY_FINANCE_1)||defkey.equals(OAConstants.DEFKEY_APPLY_FINANCE_2)){//1，2的时候
							moneyStr = o.getStr("now_apply_pay_price");
						}else if(defkey.equals(OAConstants.DEFKEY_APPLY_FINANCE_3)){
							moneyStr = o.getStr("tax_price");
						}else if(defkey.equals(OAConstants.DEFKEY_APPLY_FINANCE_4)){
							moneyStr = o.getStr("now_apply_back_money");
						}else{
							moneyStr = o.getStr("common_price");
						}
					}else if(defkey.indexOf("Contract")>=0){//如果是合同流程
						if(defkey.indexOf("ContractApply")>=0){//如果是合同申请流程
							moneyStr = o.getStr("amount_of_money");
						}else{
							String contractId = o.getStr("contract_id");
							if(StrKit.notBlank(contractId)){
								OaContract contract = OaContract.dao.getById(contractId);
								moneyStr = contract.getAmountOfMoney();
							}
						}
					}
					if(StrKit.notBlank(moneyStr)){//设置金额变量
						var.put(OAConstants.WORKFLOW_VAR_APPLY_VAR_MONEY, Double.parseDouble(moneyStr));
					}else{
						var.put(OAConstants.WORKFLOW_VAR_APPLY_VAR_MONEY, 0);
					}
				}
				//银行卡类型的财务流程
				if(defkey.indexOf("AccountBank")>=0){
					var.put(OAConstants.WORKFLOW_VAR_APPLY_BANK_ACCOUNT_AUDIT1, o.getStr("audit1_username"));
					var.put(OAConstants.WORKFLOW_VAR_APPLY_BANK_ACCOUNT_AUDIT2, o.getStr("audit2_username"));
					var.put(OAConstants.WORKFLOW_VAR_APPLY_BANK_ACCOUNT_AUDIT3, o.getStr("audit3_username"));
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		/***************下面是分公司的角色********************/
		//查找分公司财务的办理人
		//设置是一级分公司还是二级分公司
		String childCompanyId = org.getParentChildCompanyId();
		SysOrg ccOrg = SysOrg.dao.getById(childCompanyId);//申请人直接所属子公司
		SysOrg fccOrg = SysOrg.dao.getFirstChildCompany(org);//申请人所在一级子公司
		if("#root".equals(org.getParentId())||"#root".equals(org.getParentChildCompanyId())){//如果父级是总公司，自身的子公司是总公司
			var.put(OAConstants.WORKFLOW_VAR_APPLY_VAR_CHILDCOMPANY_LEVEL, "0");//总公司
		}else{
			if(ccOrg!=null&&fccOrg!=null){
				if(ccOrg.getId().equals(fccOrg.getId())){//两个相同
					var.put(OAConstants.WORKFLOW_VAR_APPLY_VAR_CHILDCOMPANY_LEVEL, "1");//子公司级别为1
				}else{
					var.put(OAConstants.WORKFLOW_VAR_APPLY_VAR_CHILDCOMPANY_LEVEL, "2");//子公司级别为2
				}
			}else{
				var.put(OAConstants.WORKFLOW_VAR_APPLY_VAR_CHILDCOMPANY_LEVEL, "1");//子公司级别为1
			}
		}
		if(ccOrg!=null){
			List<SysUser> list1 = SysUser.dao.getUserByOrgidAndRoleKeyForChildCompany(ccOrg.getId(),  OAConstants.WORKFLOW_ROLE_KEY_ChildCompanyFinanceLeader);//分公司财务经理
			if(list1!=null&&list1.size()>0){
				List<String> userlist = new ArrayList<String>();
				for(SysUser u : list1){
					userlist.add(u.getUsername());
				}
				var.put(OAConstants.WORKFLOW_VAR_APPLY_MY_CHILD_COMPANY_FINANCE, StringUtils.join(userlist,","));
			}
			List<SysUser> list2 = SysUser.dao.getUserByOrgidAndRoleKeyForChildCompany(ccOrg.getId(),  OAConstants.WORKFLOW_ROLE_KEY_ChildCompanyLeader);//分公司总经理
			if(list2!=null&&list2.size()>0){
				List<String> userlist = new ArrayList<String>();
				for(SysUser u : list2){
					userlist.add(u.getUsername());
				}
				var.put(OAConstants.WORKFLOW_VAR_APPLY_MY_CHILD_COMPANY_LEADER, StringUtils.join(userlist,","));
			}

			
			List<SysUser> list3 = SysUser.dao.getUserByRoleKey( OAConstants.WORKFLOW_ROLE_KEY_MainOfficeLeader);//总公司行政部经理
			if(list3!=null&&list3.size()>0){
				List<String> userlist = new ArrayList<String>();
				for(SysUser u : list3){
					userlist.add(u.getUsername());
				}
				var.put(OAConstants.WORKFLOW_VAR_APPLY_TOP_COMPANY_OFFICE, StringUtils.join(userlist,","));
			}
			List<SysUser> list4 = SysUser.dao.getUserByRoleKey( OAConstants.WORKFLOW_ROLE_KEY_MainTopLeader);//总公司总经理
			if(list4!=null&&list4.size()>0){
				List<String> userlist = new ArrayList<String>();
				for(SysUser u : list4){
					userlist.add(u.getUsername());
				}
				var.put(OAConstants.WORKFLOW_VAR_APPLY_TOP_COMPANY_LEADER, StringUtils.join(userlist,","));
			}
			List<SysUser> list5 = SysUser.dao.getUserByRoleKey( OAConstants.WORKFLOW_ROLE_KEY_MainFinanceLeader);//总公司财务部经理
			if(list5!=null&&list5.size()>0){
				List<String> userlist = new ArrayList<String>();
				for(SysUser u : list5){
					userlist.add(u.getUsername());
				}
				var.put(OAConstants.WORKFLOW_VAR_APPLY_TOP_COMPANY_FINANCE, StringUtils.join(userlist,","));
			}
			List<SysUser> list6 = SysUser.dao.getUserByRoleKey( OAConstants.WORKFLOW_ROLE_KEY_MainProjectLeader);//总公司工程部经理
			if(list6!=null&&list6.size()>0){
				List<String> userlist = new ArrayList<String>();
				for(SysUser u : list6){
					userlist.add(u.getUsername());
				}
				var.put(OAConstants.WORKFLOW_VAR_APPLY_TOP_COMPANY_PROJECT, StringUtils.join(userlist,","));
			}
			List<SysUser> list7 = SysUser.dao.getUserByRoleKey( OAConstants.WORKFLOW_ROLE_KEY_MainHRLeader);//总公司人事行政部经理
			if(list7!=null&&list7.size()>0){
				List<String> userlist = new ArrayList<String>();
				for(SysUser u : list7){
					userlist.add(u.getUsername());
				}
				var.put(OAConstants.WORKFLOW_VAR_APPLY_TOP_COMPANY_HR, StringUtils.join(userlist,","));
			}
		}
		if(fccOrg!=null){
			List<SysUser> list1 = SysUser.dao.getUserByOrgidAndRoleKeyForChildCompany(fccOrg.getId(), OAConstants.WORKFLOW_ROLE_KEY_ChildCompanyFinanceLeader);//一级分公司财务经理
			if(list1!=null&&list1.size()>0){
				List<String> userlist = new ArrayList<String>();
				for(SysUser u : list1){
					userlist.add(u.getUsername());
				}
				var.put(OAConstants.WORKFLOW_VAR_APPLY_FIRST_CHILD_COMPANY_FINANCE, StringUtils.join(userlist,","));
			}
			List<SysUser> list2 = SysUser.dao.getUserByOrgidAndRoleKeyForChildCompany(fccOrg.getId(),  OAConstants.WORKFLOW_ROLE_KEY_ChildCompanyLeader);//一级分公司总经理
			if(list2!=null&&list2.size()>0){
				List<String> userlist = new ArrayList<String>();
				for(SysUser u : list2){
					userlist.add(u.getUsername());
				}
				var.put(OAConstants.WORKFLOW_VAR_APPLY_FIRST_CHILD_COMPANY_LEADER, StringUtils.join(userlist,","));
			}
			//自己本单位的财务经理审批
			Object myFinanceLeader = var.get(OAConstants.WORKFLOW_VAR_APPLY_MY_CHILD_COMPANY_FINANCE);
			if(myFinanceLeader==null){//如果是子级本单位没有财务经理。使用一级财务经理审批。
				var.put(OAConstants.WORKFLOW_VAR_APPLY_MY_CHILD_COMPANY_FINANCE, var.get(OAConstants.WORKFLOW_VAR_APPLY_FIRST_CHILD_COMPANY_FINANCE).toString());
			}else if(myFinanceLeader.toString().equals("")||myFinanceLeader.toString().equals(",")){
				var.put(OAConstants.WORKFLOW_VAR_APPLY_MY_CHILD_COMPANY_FINANCE, var.get(OAConstants.WORKFLOW_VAR_APPLY_FIRST_CHILD_COMPANY_FINANCE).toString());
			}
			//自己本单位的财务经理审批
			Object myCompanyLeader = var.get(OAConstants.WORKFLOW_VAR_APPLY_MY_CHILD_COMPANY_LEADER);
			if(myCompanyLeader==null){//如果是子级本单位没有财务经理。使用一级财务经理审批。
				var.put(OAConstants.WORKFLOW_VAR_APPLY_MY_CHILD_COMPANY_LEADER, var.get(OAConstants.WORKFLOW_VAR_APPLY_FIRST_CHILD_COMPANY_LEADER));
			}else if(myCompanyLeader.toString().equals("")||myCompanyLeader.toString().equals(",")){
				var.put(OAConstants.WORKFLOW_VAR_APPLY_MY_CHILD_COMPANY_LEADER, var.get(OAConstants.WORKFLOW_VAR_APPLY_FIRST_CHILD_COMPANY_LEADER));
			}
		}
		System.out.println("======================================");
		System.out.println(var);
		System.out.println("======================================");
		return var;
	}
	
	/***
	 * 针对业务的流程变量
	 * @param var
	 * @param id
	 * @param username
	 * @param defkey
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, Object> getVarBusiness(Map<String, Object> var,SysUser user,SysOrg org,String defkey,com.jfinal.plugin.activerecord.Model o){
		if(OAConstants.DEFKEY_APPLY_FINANCE_9.equals(defkey)){//业务暂借款申请
			List<SysUser> list = SysUser.dao.getUserByOrgidAndRoleKey(o.getStr("about_orgid"),OAConstants.WORKFLOW_ROLE_KEY_OrgLeader);
			if(list!=null&&list.size()>0){
				List<String> userlist = new ArrayList<String>();
				for(SysUser u : list){
					userlist.add(u.getUsername());
				}
				var.put(OAConstants.WORKFLOW_VAR_APPLY_ABOUT_ORG_LEADER, StringUtils.join(userlist,","));
			}
		}
		return var;
	}
}
