package com.pointlion.sys.mvc.admin.oa.contract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.admin.oa.common.OAConstants;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.common.model.OaContract;
import com.pointlion.sys.mvc.common.model.OaContractApply;
import com.pointlion.sys.mvc.common.model.OaFlowCarbonC;
import com.pointlion.sys.mvc.common.model.SysUser;
import com.pointlion.sys.mvc.common.model.SysUserSign;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.mvc.common.utils.ExportUtil;
import com.pointlion.sys.mvc.common.utils.ModelToMapUtil;
import com.pointlion.sys.mvc.common.utils.office.word.POITemplateUtil;
import com.pointlion.sys.mvc.common.utils.word.CustomXWPFDocument;
import com.pointlion.sys.mvc.common.utils.word.WordUtil;

public class OaContractService{
	public static final OaContractService me = new OaContractService();
	public static final WorkFlowService workFlowService = new WorkFlowService();
	public static final String TABLE_NAME = OaContract.tableName;
	
	/***
	 * 根据主键查询
	 */
	public OaContract getById(String id){
		return OaContract.dao.findById(id);
	}
	
	/***
	 * 获取分页
	 */
	public Page<Record> getPage(String userid ,String state,int pnum,int psize){
		String sql  = " from "+TABLE_NAME+" o where 1=1 ";
		if(StrKit.notBlank(userid)){
			sql = sql + " and o.create_userid='"+userid+"' ";
		}
		if(StrKit.notBlank(state)){
			sql = sql + " and o.state = '"+state+"'";
		}
		sql = sql + " order by o.create_time desc";
		return Db.paginate(pnum, psize, " select o.* ", sql);
	}
	/***
	 * 获取分页
	 */
	public Page<Record> getCanUsePage(String userid,int pnum,int psize){
		String sql  = " from "+TABLE_NAME+" o where 1=1 ";
		if(StrKit.notBlank(userid)){
			sql = sql + " and o.create_userid='"+userid+"' ";
		}
		sql = sql + " and o.state !='"+OAConstants.OA_CONTRACT_STATE_STOP+"'";
		sql = sql + " order by o.create_time desc";
		return Db.paginate(pnum, psize, " select o.* ", sql);
	}
	
	/***
	 * 删除
	 * @param ids
	 */
	@Before(Tx.class)
	public void deleteByIds(String ids){
    	String idarr[] = ids.split(",");
    	for(String id : idarr){
    		OaContract o = me.getById(id);
    		o.delete();
    	}
	}
	/***
	 * 获取合同类型
	 * @return
	 */
	public String getTypeName(String type){
		String name = "";
		if("1".equals(type)){
			name = "买卖";
		}else if("2".equals(type)){
			name = "租赁";
		}else if("3".equals(type)){
			name = "供用电/水/气";
		}else if("4".equals(type)){
			name = "融资租赁合同";
		}else if("5".equals(type)){
			name = "技术合同";
		}else if("6".equals(type)){
			name = "运输合同及建设工程合同";
		}else if("7".equals(type)){
			name = "场地使用";
		}
		return name;
	}
	/***
	 * 导出
	 * @throws Exception 
	 */
	public File export(String id,HttpServletRequest request) throws Exception{
		//		Contract_Apply.docx
		String path = request.getSession().getServletContext().getRealPath("")+"/WEB-INF/admin/oa/contract/apply/template/";
		OaContractApply contract = OaContractApply.dao.findById(id);
		Map<String , Object > data = ModelToMapUtil.ModelToMap2(contract);
		String start_time = contract.getStartTime();
		if(StrKit.notBlank(start_time)){
			data.put("start_time", DateUtil.dateToString(DateUtil.StringToDate(start_time),7));
		}
		String end_time = contract.getEndTime();
		if(StrKit.notBlank(end_time)){
			data.put("end_time",DateUtil.dateToString(DateUtil.StringToDate(end_time),7));
		}
		String can_able_date = contract.getCanAbleDate();
		if(StrKit.notBlank(can_able_date)){
			data.put("can_able_date",DateUtil.dateToString(DateUtil.StringToDate(can_able_date),7));
		}
		List<Record> list = workFlowService.getHisTaskList(contract.getProcInsId());
		List<String> receivelist = new ArrayList<String>();
		for(Record r : list){
			String lineContent = r.getStr("taskName")+":"+r.getStr("assignee")+":"+r.getStr("message");
			lineContent = addEmptyString(lineContent,15);
			receivelist.add(lineContent);
		}
		data.put("receiver", StringUtils.join(receivelist,POITemplateUtil.NewLine+"-------------------------------------------------------------------------------"+POITemplateUtil.NewLine));
		data.put("htbh", contract.getContractNum());
		data.put("type", getTypeName(contract.getType2()));
		//抄送
				List<OaFlowCarbonC> cclist = OaFlowCarbonC.dao.find("select * from oa_flow_carbon_c c ,sys_user u  where u.id=c.user_id and business_id = '"+id+"' ");
				if(StrKit.notNull(cclist)){
					List<String> nameList = new ArrayList<String>();
					 for(OaFlowCarbonC cc : cclist){
						 nameList.add(cc.getStr("name"));
					 }
					 if(!StrKit.isBlank(nameList.toString())){
						 data.put("cc",StringUtils.join(nameList,","));
					 }else{
						 data.put("cc","无");
					 }
				}else{
					data.put("cc","无");
				}
		System.out.println(data);
		String exportURL = path+contract.getName()+"_"+contract.getTitle()+"_"+contract.getCreateTime().replaceAll(" ", "_").replaceAll(":", "-")+".docx";
		ExportUtil.export(data, path+"Contract.docx", exportURL);
		File file = new File(exportURL);
		if(file.exists()){
			return file;
		}else{
			return null;
		}
		
		
	}
	
	public File exportSign(String id,HttpServletRequest request) throws Exception{
		//		Contract_Apply.docx
		String path = request.getSession().getServletContext().getRealPath("")+"/WEB-INF/admin/oa/contract/apply/template/";
		String basepath = request.getSession().getServletContext().getRealPath("");
		OaContractApply contract = OaContractApply.dao.findById(id);
//		Map<String , Object > data = ModelToMapUtil.ModelToMap2(contract);
		Map<String, Object> data = new HashMap<String, Object>();
		String start_time = contract.getStartTime();
		data.put("${name}", contract.getName()!=null?contract.getName():"");
		data.put("${applyer_name}", contract.getApplyerName()!=null?contract.getApplyerName():"");
		data.put("${org_name}", contract.getOrgName()!=null?contract.getOrgName():"");
		data.put("${custom_name}", contract.getCustomName()!=null?contract.getCustomName():"");
		data.put("${custom_contact_person}", contract.getCustomContactPerson()!=null?contract.getCustomContactPerson():"");
		data.put("${custom_contact_mobile}", contract.getCustomContactMobile()!=null?contract.getCustomContactMobile():"");
		data.put("${title}", contract.getTitle()!=null?contract.getTitle():"");
		data.put("${amount_of_money}", contract.getAmountOfMoney()!=null?contract.getAmountOfMoney():"");
		if(StrKit.notBlank(start_time)){
			data.put("${start_time}", DateUtil.dateToString(DateUtil.StringToDate(start_time),7));
		}else{
			data.put("${start_time}", "");
		}
		String end_time = contract.getEndTime();
		if(StrKit.notBlank(end_time)){
			data.put("${end_time}",DateUtil.dateToString(DateUtil.StringToDate(end_time),7));
		}else{
			data.put("${end_time}","");
		}
		String can_able_date = contract.getCanAbleDate();
		if(StrKit.notBlank(can_able_date)){
			data.put("${can_able_date}",DateUtil.dateToString(DateUtil.StringToDate(can_able_date),7));
		}else{
			data.put("${can_able_date}","");
		}
		//流程中审批节点，模板中设置了20条审批的节点，so需要遍历所有的流程节点，有则打印真实，无则打印empty String
		List<Record> list = workFlowService.getHisTaskList(contract.getProcInsId());
		List<String> receivelist = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			//***********************用户信息start*************************
			if(StrKit.notBlank(list.get(i).getStr("assigneeId"))){
				String userid = list.get(i).getStr("assigneeId");
				String taskId = list.get(i).getStr("taskId");
				SysUserSign sign = SysUserSign.dao.getByUserTaskid(userid,taskId);
				String receiver = list.get(i).getStr("taskName")+":"+list.get(i).getStr("assignee")+":"+list.get(i).getStr("message");
				data.put("${receiver"+i+"}", receiver);
				if(StrKit.notNull(sign)){
					 Map<String,Object> header = new HashMap<String, Object>();  
				        header.put("width", 128);  
				        header.put("height", 27);  
				        header.put("type", "png");  
				        header.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basepath+"/"+sign.getSignLocal()), true)); 
					data.put("${img"+i+"}", header);
				}else{
					data.put("${img"+i+"}", "");
				}
			}else{
				String receiver = list.get(i).getStr("taskName")+":"+list.get(i).getStr("assignee")+":"+list.get(i).getStr("message");
				data.put("${receiver"+i+"}", receiver);
				data.put("${img"+i+"}", "");
			}
			
			//***********************用户信息end*************************
		}
		for (int i = list.size(); i < 21; i++) {
			data.put("${receiver"+i+"}", "");
			data.put("${img"+i+"}", "");
		}
		for(Record r : list){
			String lineContent = r.getStr("taskName")+":"+r.getStr("assignee")+":"+r.getStr("message");
			lineContent = addEmptyString(lineContent,15);
			receivelist.add(lineContent);
		}

		//data.put("receiver", StringUtils.join(receivelist,POITemplateUtil.NewLine+"-------------------------------------------------------------------------------"+POITemplateUtil.NewLine));
		data.put("${htbh}", contract.getContractNum());
		data.put("${type}", getTypeName(contract.getType2()));
		//抄送
				List<OaFlowCarbonC> cclist = OaFlowCarbonC.dao.find("select * from oa_flow_carbon_c c ,sys_user u  where u.id=c.user_id and business_id = '"+id+"' ");
				if(StrKit.notNull(cclist)){
					List<String> nameList = new ArrayList<String>();
					 for(OaFlowCarbonC cc : cclist){
						 nameList.add(cc.getStr("name"));
					 }
					 if(!StrKit.isBlank(nameList.toString())){
						 data.put("${cc}",StringUtils.join(nameList,","));
					 }else{
						 data.put("${cc}","无");
					 }
				}else{
					data.put("${cc}","无");
				}
		System.out.println(data);
		String exportURL = path+contract.getName()+"_"+contract.getTitle()+"_"+contract.getCreateTime().replaceAll(" ", "_").replaceAll(":", "-")+".docx";
		
		CustomXWPFDocument doc = WordUtil.generateWord(data, path+"Contract.docx");
		File file = new File(exportURL);
        FileOutputStream fopts = new FileOutputStream(file);  
        doc.write(fopts);  
        fopts.close();
		if(file.exists()){
			return file;
		}else{
			return null;
		}
		
		
	}
	
	
	
	
	
	public String addEmptyString(String lineContent, int num){
		int offset = num - lineContent.length()%num;
		for (int i = 0; i < offset; i++) {
			lineContent += "\t";
		}
		return lineContent;
	}
	
	
	public static void main(String[] args) {
		String inputFile = "D://1.docx";
		String outputFile = "D://2.docx";
		Map map = new HashMap();
		try {
			map.put("picture", new FileInputStream("D://logo1.png"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		map.put("picture_1", "23333333");
		try {
			ExportUtil.export(map,inputFile,outputFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}