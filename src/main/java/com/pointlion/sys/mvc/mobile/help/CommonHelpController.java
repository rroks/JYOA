package com.pointlion.sys.mvc.mobile.help;

import java.util.ArrayList;
import java.util.List;

import com.pointlion.sys.mvc.common.base.BaseController;
import com.pointlion.sys.mvc.common.dto.ZtreeNode;
import com.pointlion.sys.mvc.common.model.SysMobileVersion;
import com.pointlion.sys.mvc.common.model.SysOrg;
import com.pointlion.sys.mvc.common.model.SysUser;

public class CommonHelpController extends BaseController  {
	/**
	 * 获取可用车辆信息
	 * @author 28995
	 */
	public void getCanUseCar(){
		CommonHelpService service = new CommonHelpService();
		renderSuccess(service.getCanUseCar(),null);
	}
	/**
	 * 获取岗位信息
	 */
	public void getStationInfo(){
		CommonHelpService service = new CommonHelpService();
		renderSuccess(service.getStationInfo(),null);
	}
	/**
	 * 获取会议室
	 */
	public void getMeetRoom(){
		CommonHelpService service = new CommonHelpService();
		renderSuccess(service.getMeetRoom(),null);
	}
	/**
	 * 获取明细部门
	 */
	public void getCanUseDepart(){
		CommonHelpService service = new CommonHelpService();
		renderSuccess(service.getCanUseDepart(),null);
	}
	/**
	 * 获取可用资产
	 */
	public void getCanUseAsset(){
		String userId = getPara("USERID");
		String conditon = getPara("conditon");
		SysUser user = SysUser.dao.getById(userId);
		CommonHelpService service = new CommonHelpService();
		renderSuccess(service.getCanUseAsset(user.getOrgid(),conditon),null);
	}
	/**
	 * 获取相关职能部门
	 */
	public void getHeadOfficeOrg(){
		CommonHelpService service = new CommonHelpService();
		renderSuccess800(service.getHeadOfficeOrg(),service.getHeadOfficeOrg().size(),null);
	}
	
	  /***
     * 获取树
     */
    public void getOrgTree(){
    	List<SysOrg> menuList = SysOrg.dao.getChildrenAllTree("#root");
    	List<ZtreeNode> nodelist = SysOrg.dao.toZTreeNode(menuList,true,false);//数据库中的菜单
    	renderSuccess(nodelist,null);
    }
    public void getOrgTreeOnlyLeaf(){
    	List<SysOrg> menuList = SysOrg.dao.getChildrenAllTree("#root");
    	List<ZtreeNode> nodelist = SysOrg.dao.toZTreeNode(menuList,true,true);//数据库中的菜单
    	List<ZtreeNode> rootList = new ArrayList<ZtreeNode>();//页面展示的,带根节点
    	//声明根节点
    	ZtreeNode root = new ZtreeNode();
    	root.setId("#root");
    	root.setName("公司部门组织结构");
    	root.setChildren(nodelist);
    	root.setOpen(true);
    	root.setNocheck(true);
    	rootList.add(root);
    	renderJson(rootList);
    }
    public void getUsers(){
    	String userId = getPara("USERID");
    	SysUser user = SysUser.dao.getById(userId);
    	String depart = user.getOrgid();
		CommonHelpService service = new CommonHelpService();
		renderSuccess(service.getUsers(depart),null);
    }
    /**
     * 获取项目
     * key:formProject
     */
    public void getProjects(){
		CommonHelpService service = new CommonHelpService();
		renderSuccess800(service.getProjects(),service.getProjects().size(),messageSuccess);
    }
    /**
     * 获取合同(已完成的合同)
     * 
     */
    public void getContracts(){
    	CommonHelpService service = new CommonHelpService();
		renderSuccess800(service.getContracts(),service.getContracts().size(),messageSuccess);
    }
    
    public void getVersion(){
    	SysMobileVersion version = SysMobileVersion.dao.getByPublish("1");
    	renderSuccess800(version!=null?version:"", version!=null?1:0, messageSuccess);
    }
    
    public void getDownloadPage(){
    	System.out.println(this.getRequest().getSession().getServletContext().getRealPath("")+"/");
    	SysMobileVersion version = SysMobileVersion.dao.getByPublish("1");
    	setAttr("iosUri", version.getIosUri());
    	setAttr("androidUri", version.getAndroidUri());
    	render("/WEB-INF/admin/help/OAdownload/download.html");
    }
    
}
