/**
 * @author Lion
 * @date 2017年1月24日 下午12:02:35
 * @qq 439635374
 */
package com.pointlion.sys.mvc.admin.sys.org;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.pointlion.sys.interceptor.MainPageTitleInterceptor;
import com.pointlion.sys.mvc.admin.oa.hrorg.HrOrgService;
import com.pointlion.sys.mvc.common.base.BaseController;
import com.pointlion.sys.mvc.common.dto.ZtreeNode;
import com.pointlion.sys.mvc.common.model.SysHrOrg;
import com.pointlion.sys.mvc.common.model.SysOrg;
import com.pointlion.sys.mvc.common.model.SysRole;
import com.pointlion.sys.mvc.common.utils.ContextUtil;
import com.pointlion.sys.mvc.common.utils.UuidUtil;
import com.pointlion.sys.plugin.shiro.ShiroKit;

/***
 * 菜单管理控制器
 */
@Before(MainPageTitleInterceptor.class)
public class OrgController extends BaseController {
	/***
	 * 获取列表页面
	 */
	public void getListPage(){
//		String userParentChildCompanyId = ShiroKit.getUserParentChileCompanyId();//上级子公司id
		setAttr("pid",ShiroKit.getUserOrgId());
    	render("list.html");
    }
    /***
     * 获取树
     */
    public void getOrgTree(){
    	String orgid = getPara("orgid");
    	String userid = ShiroKit.getUserId();
    	List<SysHrOrg> hrOrgList = SysHrOrg.dao.getCheckOrgByUserid(userid);
    	String isUser = getPara("");
    	String ifAllChild = "1";//默认查询孩子
    	Boolean open = false;//是否展开所有
    	Boolean ifOnlyLeaf = false;//是否只选叶子
    	if(StrKit.notBlank(getPara("ifAllChild"))){//是否查询所有孩子
    		ifAllChild = getPara("ifAllChild");
    	}
    	if(StrKit.notBlank(getPara("ifOpen"))){//是否查询所有孩子
    		if("1".equals(getPara("ifOpen"))){
    			open = true;
    		}
    	}
    	if(StrKit.notBlank(getPara("ifOnlyLeaf"))){//是否查询所有孩子
    		if("1".equals(getPara("ifOnlyLeaf"))){
    			ifOnlyLeaf = true;
    		}
    	}
    	if(SysRole.dao.ifSuperAdmin(ShiroKit.getUserId())){//如果是超级管理员
    		List<SysOrg> menuList = SysOrg.dao.getChildrenAllTree("#root");
        	List<ZtreeNode> nodelist = SysOrg.dao.toZTreeNode(menuList,open,ifOnlyLeaf);//数据库中的菜单
        	List<ZtreeNode> rootList = new ArrayList<ZtreeNode>();//页面展示的,带根节点
        	//声明根节点
        	SysOrg rootOrg = SysOrg.dao.getById("#root");
        	ZtreeNode root = new ZtreeNode();
        	root.setId("#root");
        	if(rootOrg==null){
        		root.setName("公司组织结构");
        	}else{
        		root.setName(rootOrg.getName());
        	}
        	root.setChildren(nodelist);
        	root.setOpen(true);
        	root.setIcon(ContextUtil.getCtx()+"/common/plugins/zTree_v3/css/zTreeStyle/img/diy/9.png");
        	rootList.add(root);
        	renderJson(rootList);
    	}else{
    		if(hrOrgList.size()>0){
    			List<SysOrg> menuList = SysOrg.dao.getChildrenAllTree("#root");
            	List<ZtreeNode> nodelist = SysOrg.dao.toZTreeNode(menuList,open,ifOnlyLeaf);//数据库中的菜单
            	List<ZtreeNode> rootList = new ArrayList<ZtreeNode>();//页面展示的,带根节点
            	//声明根节点
            	SysOrg rootOrg = SysOrg.dao.getById("#root");
            	ZtreeNode root = new ZtreeNode();
            	root.setId("#root");
            	if(rootOrg==null){
            		root.setName("公司组织结构");
            	}else{
            		root.setName(rootOrg.getName());
            	}
            	root.setChildren(nodelist);
            	root.setOpen(true);
            	root.setIcon(ContextUtil.getCtx()+"/common/plugins/zTree_v3/css/zTreeStyle/img/diy/9.png");
            	rootList.add(root);
            	System.out.println(rootList.toString());
//            	List<ZtreeNode> exitList = new HrOrgService().getAllOrgTreeNodeAndParentByuserid(userid);
//            	for (ZtreeNode ztreeNode : rootList) {
//					for (ZtreeNode exitNode : exitList) {
//						if(ztreeNode.getId() != exitNode.getId()){
//							rootList.remove(ztreeNode);
//						}
//					}
//				}
            	renderJson(rootList);
//    			ZtreeNode root = new ZtreeNode();
//            	if(org!=null){
//            		root.setId(org.getId());
//            		root.setName(org.getName());
//            	}
//            	root.setChildren(nodelist);
//            	root.setOpen(true);
//            	root.setIcon(ContextUtil.getCtx()+"/common/plugins/zTree_v3/css/zTreeStyle/img/diy/1_open.png");
//            	rootList.add(root);
//            	renderJson(rootList);
    		}else{
//    			if(StrKit.isBlank(orgid)){//如果没传递过来机构id，使用子公司id
//        			orgid = ShiroKit.getUserParentChileCompanyId();
//        		}
        		if(StrKit.isBlank(orgid)){//如果没传递过来机构id，使用子公司id
        			orgid = ShiroKit.getUserOrgId();
        		}
        		List<SysOrg> menuList = new ArrayList<SysOrg>();
        		if("1".equals(ifAllChild)){
        			menuList = SysOrg.dao.getChildrenAllTree(orgid);
        		}else{
        			menuList = SysOrg.dao.getChildrenByPid(orgid, "0");
        		}
            	List<ZtreeNode> nodelist = SysOrg.dao.toZTreeNode(menuList,open,ifOnlyLeaf);//数据库中的菜单
            	List<ZtreeNode> rootList = new ArrayList<ZtreeNode>();//页面展示的,带根节点
            	SysOrg org = SysOrg.dao.getById(orgid);//传进来的id
            	ZtreeNode root = new ZtreeNode();
            	if(org!=null){
            		root.setId(org.getId());
            		root.setName(org.getName());
            	}
            	root.setChildren(nodelist);
            	root.setOpen(true);
            	root.setIcon(ContextUtil.getCtx()+"/common/plugins/zTree_v3/css/zTreeStyle/img/diy/1_open.png");
            	rootList.add(root);
            	renderJson(rootList);
    		}
    	}
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
    /***
     * 获取分页数据
     **/
    public void listData(){
    	String curr = getPara("pageNumber");
    	String pageSize = getPara("pageSize");
    	String pid = getPara("pid");
    	Page<Record> page = SysOrg.dao.getChildrenPageByPid(Integer.valueOf(curr),Integer.valueOf(pageSize),pid);
    	renderPage(page.getList(),"" ,page.getTotalRow());
    }
    
    /***
     * 获取编辑页面
     */
    public void getEditPage(){
    	setAttr("type", getPara("type"));
    	//添加和修改
    	String id = getPara("id");
    	if(StrKit.notBlank(id)){//修改
    		SysOrg o = SysOrg.dao.getById(id);
    		SysOrg parent = SysOrg.dao.getById(o.getParentId());
    		setAttr("o", o);
    		setAttr("p", parent);
    		setAttr("type", o.getType());
    		if("1".equals(parent.getType())){//父级就是公司
    			setAttr("parentCompany", parent);//父级子公司
    		}else if("0".equals(parent.getType())){//父级是部门
    			String parentCompanyId = parent.getParentChildCompanyId();//该部门所属子公司
        		SysOrg parentCompany = SysOrg.dao.getById(parentCompanyId);
        		setAttr("parentCompany", parentCompany);//父级子公司
    		}
    	}
    	String parentid = getPara("parentid");
    	if(StrKit.notBlank(parentid)){
    		SysOrg parent = SysOrg.dao.getById(parentid);
    		setAttr("p", parent);//父级机构
    		if("1".equals(parent.getType())){//父级就是公司
    			setAttr("parentCompany", parent);//父级子公司
    		}else if("0".equals(parent.getType())){//父级是部门
    			String parentCompanyId = parent.getParentChildCompanyId();//该部门所属子公司
        		SysOrg parentCompany = SysOrg.dao.getById(parentCompanyId);
        		setAttr("parentCompany", parentCompany);//父级子公司
    		}
    	}
    	render("edit.html");
    }
    /***
     * 保存
     */
    public void save(){
    	SysOrg o = getModel(SysOrg.class);
    	if(StrKit.notBlank(o.getId())){
    		o.update();
    	}else{
    		o.setId(UuidUtil.getUUID());
    		o.save();
    	}
    	renderSuccess();
    }
    /***
     * 获取选择树页面
     * */
    public void getSelectOneOrgPage(){
    	String orgid = getPara("orgid");
    	String ifAllChild = getPara("ifAllChild");
    	String ifOpen = getPara("ifOpen");
    	String ifOnlyLeaf = getPara("ifOnlyLeaf");
    	setAttr("orgid", orgid);
    	setAttr("ifAllChild", ifAllChild);
    	setAttr("ifOpen", ifOpen);
    	setAttr("ifOnlyLeaf", ifOnlyLeaf);
    	render("/WEB-INF/admin/sys/org/selectOneOrg.html");
    }
    public void getSelectManyOrgPage(){
    	render("/WEB-INF/admin/sys/org/selectManyOrg.html");
    }
    
    /**为数据权限创建的组织选择
     * 
     */
    public void getSelectManyOrgPage4Per(){
    	setAttr("userid", getPara("userid"));
    	setAttr("groupid", getPara("groupid"));
    	render("/WEB-INF/admin/oa/dataPermission/selectManyOrg.html");
    }
    
    /**为数据权限创建的组织选择
     * 
     */
    public void getSelectManyOrgPage4Hr(){
    	setAttr("userid", getPara("userid"));
    	setAttr("groupid", getPara("groupid"));
    	render("/WEB-INF/admin/sys/user/selectManyOrg.html");
    }
    
    /***
     * 删除
     * @throws Exception
     */
    public void delete() throws Exception{
		String ids = getPara("ids");
    	String idarr[] = ids.split(",");
    	for(String id : idarr){
    		List<SysOrg> list = SysOrg.dao.getChildrenByPid(id,null);
    		if(list.size()>0){
    			renderError("有子菜单,不允许删除!");
    			return;
    		}
    	}
    	//执行删除
    	SysOrg.dao.deleteByIds(ids);
    	renderSuccess("删除成功!");
    }
    /***
     * 同级别的组织结构下，不允许重名
     */
    public void validOrgname(){
    	SysOrg org = getModel(SysOrg.class);
    	if(org!=null){
    		String parentId = getPara("parentId");
    		String orgId = getPara("orgId");
    		if(StrKit.isBlank(orgId)){//新增
    			String name = org.getName();
        		if(StrKit.notBlank(parentId)&&StrKit.notBlank(name)){
        			List<SysOrg> list = SysOrg.dao.find("select * from sys_org o where o.parent_id='"+parentId+"' and o.name='"+name+"' ");
        			if(list!=null&&list.size()>0){//如果有值，则校验失败
        				renderValidFail();
        			}else{
        				renderValidSuccess();
        			}
        		}else{
        			renderValidFail();
        		}
    		}else{//更新
    			String name = org.getName();//当前存入的名字
        		if(StrKit.notBlank(parentId)&&StrKit.notBlank(name)){
        			List<SysOrg> list = SysOrg.dao.find("select * from sys_org o where o.parent_id='"+parentId+"' and o.name='"+name+"' and o.id!='"+orgId+"' ");
        			if(list!=null&&list.size()>0){//如果有值，则校验失败
        				renderValidFail();
        			}else{
        				renderValidSuccess();
        			}
        		}else{
        			renderValidFail();
        		}
    		}
    		
    	}else{
    		renderValidSuccess();
    	}
    }
    //修改组织
	public void geteditManyOrgPage(){
		setAttr("orgid", getPara("orgid"));
		render("/WEB-INF/admin/sys/org/editManyOrg.html");
	}/**
	 * 获取已选中的组织
	 */
	public void getAllOrgTreeNodeByids(){
		String orgid = getPara("orgid");
		List<ZtreeNode> treelist = new ArrayList<ZtreeNode>();
		if(StrKit.notBlank(orgid)){
			String idarr[] = orgid.split(",");
			for(String id : idarr){
				SysOrg org = SysOrg.dao.getById(id);
				ZtreeNode tree = new ZtreeNode();
				tree.setId(org.getId());
				tree.setName(org.getName());
				treelist.add(tree);
			}
		}
		renderJson(treelist);
	}


	/**************************************************************************/
	private String pageTitle = "组织机构";//模块页面标题
	private String breadHomeMethod = "getListPage";//面包屑首页方法
	
	public Map<String,String> getPageTitleBread() {
		Map<String,String> pageTitleBread = new HashMap<String,String>();
		pageTitleBread.put("pageTitle", pageTitle);
		pageTitleBread.put("breadHomeMethod", breadHomeMethod);
		return pageTitleBread;
	}
}
