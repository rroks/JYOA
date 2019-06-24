package com.pointlion.sys.mvc.admin.oa.hrorg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.dto.ZtreeNode;
import com.pointlion.sys.mvc.common.model.OaPermissionOrg;
import com.pointlion.sys.mvc.common.model.SysHrOrg;
import com.pointlion.sys.mvc.common.model.SysOrg;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.mvc.common.utils.UuidUtil;
import com.pointlion.sys.plugin.shiro.ShiroKit;

public class HrOrgService{
	public static final HrOrgService me = new HrOrgService();
	public static final String TABLE_NAME = SysHrOrg.tableName;
	
	/***
	 * 根据主键查询
	 */
	public SysHrOrg getById(String id){
		return SysHrOrg.dao.findById(id);
	}
	
	/***
	 * 获取分页
	 */
	public Page<Record> getPage(int pnum,int psize){
		String sql  = " from "+TABLE_NAME+" o LEFT JOIN act_hi_procinst p ON o.proc_ins_id=p.ID_  where o.userid='"+ShiroKit.getUserId()+"'  order by o.create_time desc";
		return Db.paginate(pnum, psize, " select * ", sql);
	}
	
	/***
	 * 删除
	 * @param ids
	 */
	@Before(Tx.class)
	public void deleteByIds(String ids){
    	String idarr[] = ids.split(",");
    	for(String id : idarr){
    		SysHrOrg o = me.getById(id);
    		o.delete();
    	}
	}
	
	/***
	 * 获取所有组织
	 * 根据用户id遍历出来
	 */
	public List<ZtreeNode> getAllOrgTreeNodeByuserid(String userid){
		List<SysHrOrg> tList = SysHrOrg.dao.getCheckOrgByUserid(userid);
		List<ZtreeNode> treelist = new ArrayList<ZtreeNode>();
		for(SysHrOrg org:tList){
			ZtreeNode tree = new ZtreeNode();
			tree.setId(org.getOrgid());
			tree.setName(org.getOrgname());
			treelist.add(tree);
		}
		return treelist;
	}
	
	
	/***
	 * 获取所有组织及父节点
	 * 根据用户id遍历出来
	 */
	public List<ZtreeNode> getAllOrgTreeNodeAndParentByuserid(String userid){
		List<SysHrOrg> tList = SysHrOrg.dao.getCheckOrgByUserid(userid);
		List<SysOrg> orgList = SysOrg.dao.getAll();
		Map map = new HashMap();
		List tmpList = new ArrayList();
		if(tList.size()>0){
			tmpList.addAll(tList);
			for (SysHrOrg hrorg : tList) {
				map.put(hrorg.getOrgid(), hrorg.getOrgname());
				List<SysOrg> list = SysOrg.dao.getParentAllAndMe(hrorg.getOrgid());
				if(list.size()>0){
					for (SysOrg sysOrg : list) {
						orgList.remove(sysOrg);
						map.put(sysOrg.getId(), sysOrg.getName());
					}
				}
			}
		}
		List<ZtreeNode> treelist = new ArrayList<ZtreeNode>();
//		for (Object key : map.keySet()) {
//			ZtreeNode tree = new ZtreeNode();
//			tree.setId(key.toString());
//			tree.setName((String) map.get(key));
//			treelist.add(tree);
//		}
//		orgList.removeAll(tmpList);
		if(tList.size()>0){
			for (SysOrg org : orgList) {
				ZtreeNode tree = new ZtreeNode();
				tree.setId(org.getId());
				tree.setName(org.getName());
				treelist.add(tree);
			}
		}
		return treelist;
	}
	
	
    /**赋表
	 * @param userid
	 * @param groupid
	 * @param tabledata
	 */
	@Before(Tx.class)
	public void giveUserOrg(String userid, String orgdata){
		SysHrOrg.dao.deleteByUserid(userid);
		String dataArr[] = orgdata.split(",");
		if (StrKit.notBlank(orgdata)) {
			for (String data : dataArr) {
				SysOrg org = SysOrg.dao.getById(data);
				SysHrOrg orgPer = new SysHrOrg();
				if(StrKit.notNull(orgPer)){
					orgPer.setId(UuidUtil.getUUID());
					orgPer.setOrgid(data);
					orgPer.setOrgname(org.getName());
					orgPer.setOrgParent(org.getParentId());
					orgPer.setCreatetime(DateUtil.getTime());
					orgPer.setUserid(userid);
					orgPer.save();
				}
			}
		}
	}
	
}