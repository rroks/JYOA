package com.pointlion.sys.mvc.admin.oa.contract.apply;

import java.util.Date;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.admin.oa.common.OAConstants;
import com.pointlion.sys.mvc.admin.sys.dataauth.SysDataAuthTranslator;
import com.pointlion.sys.mvc.common.model.OaContractApply;
import com.pointlion.sys.mvc.common.model.OaCustomflowCase;
import com.pointlion.sys.mvc.common.model.SysOrg;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.mvc.common.utils.PinYinUtil;
import com.pointlion.sys.mvc.common.utils.StringUtil;

public class OaContractApplyService{
	public static final OaContractApplyService me = new OaContractApplyService();
	public static final String TABLE_NAME = OaContractApply.tableName;
	
	/***
	 * 根据主键查询
	 */
	public OaContractApply getById(String id){
		return OaContractApply.dao.findById(id);
	}
	
	/***
	 * 获取分页
	 */
	public Page<Record> getPage(int pnum,int psize,String type,String name,String startTime,String endTime){
		String dataAuth = SysDataAuthTranslator.me.translator(TABLE_NAME);
		String sql  = " from "+dataAuth+" o LEFT JOIN act_hi_procinst p ON o.proc_ins_id=p.ID_  where 1=1 ";
		if(StrKit.notBlank(type)){
			sql = sql + " and o.type = '"+type+"'";
		}
		sql = sql + getQuerySql(type,name,startTime,endTime);
		return Db.paginate(pnum, psize, " select o.*,p.PROC_DEF_ID_ defid ", sql);
	}
	
	
	
	/****
	 * 获取查询sql
	 * @param type
	 * @param name
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public String  getQuerySql(String type,String name,String startTime,String endTime){
		String sql = " ";
		if(StrKit.notBlank(type)){
			sql = sql + " and o.type='"+type+"' ";
		}
		if(StrKit.notBlank(name)){//des\title\name\custom_name\custom_contact_person\custom_contact_moble
			sql = sql + " and (o.applyer_name like '%"+name+"%' or o.org_name like '%"+name+"%'"
					+ " or o.des like '%"+name+"%' or o.title like '%"+name+"%' or o.name like '%"+name+"%'"
							+ " or o.custom_name like '%"+name+"%' or o.custom_contact_person like '%"+name+"%')";
		}
		if(StrKit.notBlank(startTime)){
			sql = sql + "  and o.create_time >= '"+startTime+" 00:00:00'";
		}
		if(StrKit.notBlank(endTime)){
			sql = sql + "  and o.create_time <= '"+endTime+" 23:59:59'";
		}
		return sql;
	}
	
	
	
	public Page<Record> getContractPage(int pnum,int psize, String userid, String type,String name,String startTime,String endTime){
			
			String sql  = " from "+TABLE_NAME+" o LEFT JOIN act_hi_procinst p ON o.proc_ins_id=p.ID_  where 1=1 ";
			if(StrKit.notBlank(type)){
				sql = sql + " and o.type='"+type+"' ";
			}		
			if(StrKit.notBlank(userid)){
				sql = sql + " and o.userid='"+userid+"' ";
			}
			sql = sql + getQuerySql(type,name,startTime,endTime);
			return Db.paginate(pnum, psize, " select o.*,p.PROC_DEF_ID_ defid ", sql);
		}
	
	/***
	 * 删除
	 * @param ids
	 */
	@Before(Tx.class)
	public void deleteByIds(String ids){
    	String idarr[] = ids.split(",");
    	for(String id : idarr){
    		OaContractApply o = me.getById(id);
    		if("1".equals(o.getIfCustomflow())&&StrKit.notBlank(o.getProcInsId())){

			}
    		o.delete();
    	}
	}
	
	/***
	 * 获取申请类型对应的名称
	 */
	public String getTypeName(String type){
		String name = "";
		if("1".equals(type)){
			name = "物业管理顾问服务合同";
		}else if("2".equals(type)){
			name = "物业管理早期介入协议";
		}else if("3".equals(type)){
			name = "前期物业服务合同及补充协议";
		}else if("4".equals(type)){
			name = "常态化物业管理服务合同及补充协议";
		}else if("5".equals(type)){
			name = "拓展物业管理项目合作协议及补充协议";
		}else if("6".equals(type)){
			name = "采购方案-多种经营类采购方案";
		}else if("7".equals(type)){
			name = "采购方案-物资类采购方案";
		}else if("8".equals(type)){
			name = "采购方案-工程、服务分包类采购方案";
		}else if("9".equals(type)){
			name = "中介合作协议";
		}else if("10".equals(type)){
			name = "销售案场物业管理服务合同";
		}else if("11".equals(type)){
			name = "公共经营方案";
		}else if("12".equals(type)){
			name = "公共经营合同";
		}else if("13".equals(type)){
			name = "配套增值服务开发合同";
		}else if("14".equals(type)){
			name = "多种经营类采购合同";
		}else if("15".equals(type)){
			name = "物资类采购合同";
		}else if("16".equals(type)){
			name = "工程、服务分包类采购合同";
		}else if("17".equals(type)){
			name = "行政管理类合同";
		}
		return name;
	}
	
	/***
	 * 获取申请类型对应的名称
	 */
	public String getDefKeyByType(String type){
		String defkey = "";
		if("1".equals(type)){
			defkey = OAConstants.DEFKEY_CONTRACT_APPLY_1;
		}else if("2".equals(type)){
			defkey = OAConstants.DEFKEY_CONTRACT_APPLY_2;
		}else if("3".equals(type)){
			defkey = OAConstants.DEFKEY_CONTRACT_APPLY_3;
		}else if("4".equals(type)){
			defkey = OAConstants.DEFKEY_CONTRACT_APPLY_4;
		}else if("5".equals(type)){
			defkey = OAConstants.DEFKEY_CONTRACT_APPLY_5;
		}else if("6".equals(type)){
			defkey = OAConstants.DEFKEY_CONTRACT_APPLY_6;
		}else if("7".equals(type)){
			defkey = OAConstants.DEFKEY_CONTRACT_APPLY_7;
		}else if("8".equals(type)){
			defkey = OAConstants.DEFKEY_CONTRACT_APPLY_8;
		}else if("9".equals(type)){
			defkey = OAConstants.DEFKEY_CONTRACT_APPLY_9;
		}else if("10".equals(type)){
			defkey = OAConstants.DEFKEY_CONTRACT_APPLY_10;
		}else if("11".equals(type)){
			defkey = OAConstants.DEFKEY_CONTRACT_APPLY_11;
		}else if("12".equals(type)){
			defkey = OAConstants.DEFKEY_CONTRACT_APPLY_12;
		}else if("13".equals(type)){
			defkey = OAConstants.DEFKEY_CONTRACT_APPLY_13;
		}else if("14".equals(type)){
			defkey = OAConstants.DEFKEY_CONTRACT_APPLY_14;
		}else if("15".equals(type)){
			defkey = OAConstants.DEFKEY_CONTRACT_APPLY_15;
		}else if("16".equals(type)){
			defkey = OAConstants.DEFKEY_CONTRACT_APPLY_16;
		}else if("17".equals(type)){
			defkey = OAConstants.DEFKEY_CONTRACT_APPLY_17;
		}
		return defkey;
	}
	
	
	/***
	 * 合同编号。根据分公司递增。
	 * @param username
	 * @return
	 */
	public Integer getAddContractNumNum(String username){
		SysOrg org = SysOrg.dao.getByUsername(username);
		Integer num = 1;
		if(org!=null){
			//当前分公司，最大的编号
			Record r = Db.findFirst("select * from oa_contract_apply a where a.contract_num_child_company_id='"+org.getParentChildCompanyId()+"' and a.contract_num_year='"+DateUtil.format(new Date(), "yyyyMM")+"' ORDER BY a.contract_num_num DESC");
			if(r!=null){
				Integer i = (Integer)r.get("contract_num_num");
				num = i+1;
			}
		}
		return num;
	}
	
	/***
	 * 合同编号。分公司简称+自增编号
	 * @param username
	 * @return
	 */
	public String getAddContractNum(Integer num,String username){
		String cnum = "CONTRACTNUM001";
		SysOrg org = SysOrg.dao.getByUsername(username);
		if(org!=null){
			if("0".equals(org.getType())){//部门
				String pcid = org.getParentChildCompanyId();
				SysOrg pcorg = SysOrg.dao.getById(pcid);
				if(pcorg!=null){
					String name = pcorg.getName().replace("厦门", "XM").replace("长沙", "C沙");
					cnum = PinYinUtil.converterToFirstSpell(name)+DateUtil.format(new Date(), "yyyyMM")+StringUtil.addZeroForNum(num+"", 3, "left");
				}
			}else if("1".equals(org.getType())){//公司
				String name = org.getName().replace("厦门", "XM").replace("长沙", "C沙");
				cnum = PinYinUtil.converterToFirstSpell(name)+DateUtil.format(new Date(), "yyyyMM")+StringUtil.addZeroForNum(num+"", 3, "left");
			}
			
		}
		return cnum;
	}
}