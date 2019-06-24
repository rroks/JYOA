package com.pointlion.sys.mvc.admin.oa.permissiongroup;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.common.dto.ZtreeNode;
import com.pointlion.sys.mvc.common.model.OaPermissionGroup;
import com.pointlion.sys.mvc.common.model.OaPermissionOrg;
import com.pointlion.sys.mvc.common.model.OaPermissionTable;
import com.pointlion.sys.mvc.common.model.SysDct;
import com.pointlion.sys.mvc.common.model.SysRole;
import com.pointlion.sys.mvc.common.utils.StringUtil;
import com.pointlion.sys.plugin.shiro.ShiroKit;

public class OaPermissionGroupService{
	public static final OaPermissionGroupService me = new OaPermissionGroupService();
	public static final String TABLE_NAME = OaPermissionGroup.tableName;
	
	/***
	 * 根据主键查询
	 */
	public OaPermissionGroup getById(String id){
		return OaPermissionGroup.dao.findById(id);
	}
	
	/***
	 * 获取分页
	 */
	public Page<Record> getPage(int pnum,int psize, String userid){
		String sql  = " from oa_permission_group  where userid='"+userid+"' order by createtime desc";
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
    		//删除另外两张表
    		OaPermissionOrg.dao.deleteByGroupid(id);
    		OaPermissionTable.dao.deleteByGroupid(id);
    		OaPermissionGroup o = me.getById(id);
    		o.delete();
    	}
	}
	
	/***
	 * 获取所有表格
	 * 给用户赋值角色时候用
	 */
	public List<ZtreeNode> getAllTableTreeNode(String userid, String groupid){
//		('1','2','3')
		List<OaPermissionTable> list = OaPermissionTable.dao.getByUAndNotGroupid(userid,groupid);
		List strList = new ArrayList();
		String tableStr = "";
		if(StrKit.notNull(list)){
			for (OaPermissionTable table : list) {
				strList.add(table.getTableid());
			}
			tableStr = StringUtil.join(strList, ",");
		}
		List<SysDct> dctList = SysDct.dao.getByTypeAndNotGroupid("FLOW_DEFKEY_TO_MODELNAME",tableStr);
		List<ZtreeNode> treelist = new ArrayList<ZtreeNode>();
		for(SysDct dct:dctList){
			ZtreeNode tree = new ZtreeNode();
			tree.setId(dct.getId());
			tree.setName(dct.getName());
			tree.setType(dct.getKey());
			treelist.add(tree);
		}
		System.out.println(treelist);
		return treelist;
	}
	
	/***
	 * 获取所有表格
	 * 根据用户id遍历出来
	 */
	public List<ZtreeNode> getAllTableTreeNodeByuserid(String userid, String groupid){
		List<OaPermissionTable> tList = OaPermissionTable.dao.getByUseridAndGruopid(userid,groupid);
		List<ZtreeNode> treelist = new ArrayList<ZtreeNode>();
		for(OaPermissionTable table:tList){
			ZtreeNode tree = new ZtreeNode();
			tree.setId(table.getTableid());
			tree.setName(table.getTablename());
			tree.setType(table.getDefkey());
			treelist.add(tree);
		}
		return treelist;
	}
	
	/***
	 * 获取所有组织
	 * 根据用户id遍历出来
	 */
	public List<ZtreeNode> getAllOrgTreeNodeByuserid(String userid, String groupid){
		List<OaPermissionOrg> tList = OaPermissionOrg.dao.getByUseridAndGroupid(userid,groupid);
		List<ZtreeNode> treelist = new ArrayList<ZtreeNode>();
		for(OaPermissionOrg org:tList){
			ZtreeNode tree = new ZtreeNode();
			tree.setId(org.getOrgid());
			tree.setName(org.getOrgname());
			treelist.add(tree);
		}
		return treelist;
	}
}