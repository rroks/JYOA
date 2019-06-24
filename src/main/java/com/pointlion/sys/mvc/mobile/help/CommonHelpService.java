package com.pointlion.sys.mvc.mobile.help;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.pointlion.sys.mvc.admin.oa.common.OAConstants;

public class CommonHelpService {
	/***
	 * 获取个人单据
	 */
	public List<Record> getCanUseCar() {
		String sql = "select id bh,concat_ws(' ',color,brand,version,'车牌号码：',num) mc from dct_resource_car where id not in (select car_id from oa_apply_use_car where status='1')";
		List<Record> list = Db.find(sql);
		return list;
	}

	public List<Record> getStationInfo() {
		String sql = "select  id bh, name mc from sys_role";
		List<Record> list = Db.find(sql);
		return list;
	}

	public List<Record> getMeetRoom() {
		String sql = "select id bh,concat_ws(' ',building,floor_num,room_num,name) mc from dct_resource_meetroom";
		List<Record> list = Db.find(sql);
		return list;
	}

	public List<Record> getCanUseDepart() {
		String sql = "select id bh,name mc from sys_org where isparent=false order by sort";
		List<Record> list = Db.find(sql);
		return list;
	}

	public Object getCanUseAsset(String orgid,String conditon) {
		String sql = " select id bh,asset_name mc  from ams_asset o where 1=1 and o.state='"+OAConstants.AMS_ASSET_STATE_0+"' ";
		if("1".equals(conditon)){
			sql += " and o.belong_org_id = '"+orgid+"' ";
		}else if("2".equals(conditon)){
			sql += " and o.belong_org_id <> '"+orgid+"' ";
		}
		
		List<Record> list = Db.find(sql);
		return list;
	}
	public List<Record> getUsers(String depart) {
		String sql = "select id bh,name mc  from sys_user where orgid='"+depart+"'";
		List<Record> list = Db.find(sql);
		return list;
	}
	
	public List<Record> getProjects() {
		String sql = "select id from sys_dct_group d where d.key='formProject'";
		Record r = Db.findFirst(sql);
		String pId = r.getStr("id");
		
		String sql2 = "select id bh,name mc  from sys_dct d where 1=1 and d.group_id='"+pId+"' order by d.key ";
		List<Record> list = Db.find(sql2);
		return list;
	}
	
	public List<Record> getContracts(){
		String sql = "select id bh,name mc  from oa_contract o where o.state !='"+OAConstants.OA_CONTRACT_STATE_STOP+"'";
		List<Record> list = Db.find(sql);
		return list;
	}

	public List<Record>  getHeadOfficeOrg() {
		String sql = "select id bh,name mc from sys_org where (parent_id='#root' and type='0')";
		List<Record> list = Db.find(sql);
		return list;
	}
}
