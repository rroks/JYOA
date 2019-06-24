package com.pointlion.sys.mvc.admin.oa.common;

import java.util.Date;

import com.jfinal.plugin.activerecord.Db;
import com.pointlion.sys.mvc.common.model.SysOrg;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.mvc.common.utils.PinYinUtil;
import com.pointlion.sys.mvc.common.utils.StringUtil;

public class BusinessUtil {
	
	
	/***
	 * 合同编号。根据分公司递增。
	 * @param username
	 * @return
	 */
	public static Integer getAddContractBankaccountFinanceNumNum(String username){
		SysOrg org = SysOrg.dao.getByUsername(username);
		Integer num = 1;
	    if(org!=null){
			String oid = org.getParentChildCompanyId();
			String year = DateUtil.format(new Date(), "yyyyMM");
			String sql = " select GREATEST(ifnull(maxb,0), ifnull(maxf,0), ifnull(maxc,0)) mmm from "
						+"(select max(b.bankaccount_num_num) maxb from oa_apply_bank_account b where b.bankaccount_num_child_company_id='"+oid+"' and b.bankaccount_num_year='"+year+"') mmb ,"
						+"(select max(f.finance_num_num) maxf from oa_apply_finance f where f.finance_num_child_company_id='"+oid+"' and f.finance_num_year='"+year+"') mmf ,"
						+"(select max(c.contract_num_num) maxc from oa_contract_apply c where c.contract_num_child_company_id='"+oid+"' and c.contract_num_year='"+year+"') mmc ";
			//当前分公司，最大的编号
			Integer r = Db.queryInt(sql);
			if(r!=null){
			num = r+1;
			}
		}
		return num;
	}
	
	
	
	/***
	 * 编号。分公司简称+自增编号
	 * @param username
	 * @return
	 */
	public static String getAddNum(Integer num,String username){
		String cnum = "BUSINESS001";
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