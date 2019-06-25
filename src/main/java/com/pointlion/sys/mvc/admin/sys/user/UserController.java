/**
 * @author Lion
 * @date 2017年1月24日 下午12:02:35
 * @qq 439635374
 */
package com.pointlion.sys.mvc.admin.sys.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.pointlion.sys.interceptor.MainPageTitleInterceptor;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowIdentityService;
import com.pointlion.sys.mvc.common.base.BaseController;
import com.pointlion.sys.mvc.common.model.SysOrg;
import com.pointlion.sys.mvc.common.model.SysRole;
import com.pointlion.sys.mvc.common.model.SysUser;
import com.pointlion.sys.mvc.common.utils.RandomUtil;
import com.pointlion.sys.mvc.common.utils.StringUtil;
import com.pointlion.sys.mvc.common.utils.UuidUtil;
import com.pointlion.sys.plugin.mail.MailTemplate;
import com.pointlion.sys.plugin.shiro.ShiroKit;

/***
 * 用户管理控制器
 * @author Administrator
 *
 */
@Before(MainPageTitleInterceptor.class)
public class UserController extends BaseController {
	public static final WorkFlowIdentityService idService = WorkFlowIdentityService.me;
	/***
	 * 获取管理首页
	 */
	public void getListPage(){
		setAttr("isAdmin", ShiroKit.getUsername());
		setAttr("orgid", ShiroKit.getUserOrgId());
		if(SysRole.dao.ifSuperAdmin(ShiroKit.getUserId())){
			render("list.html");
		}else{
			setAttr("userid", ShiroKit.getUserId());
			render("list4common.html");
		}
    }
	/***
     * 获取分页数据
     **/
    public void listData(){
    	String curr = getPara("pageNumber");
    	String pageSize = getPara("pageSize");
    	String orgid = getPara("orgid");
    	String usernameSearch = getPara("usernameSearch","");
    	String nameSearch = getPara("nameSearch","");
//    	String roleid = getPara("roleid");
    	if(StrKit.notBlank(usernameSearch)||StrKit.notBlank(nameSearch)){
    		orgid="";
    	}
    	Page<Record> page = SysUser.dao.getPage(Integer.valueOf(curr),Integer.valueOf(pageSize),orgid,usernameSearch,nameSearch);
    	List<Record> pageList = page.getList();
    	for(Record r : pageList){
    		List<SysRole> list = SysRole.dao.getAllRoleByUserid(r.getStr("id"));
    		List<String> nameList = new ArrayList<String>();
    		for(SysRole role : list){
    			nameList.add(role.getName());
    		}
    		r.set("userRoleName", StringUtils.join(nameList, ","));
    	}
    	renderPage(page.getList(),"" ,page.getTotalRow());
    }
    /***
     * 根据角色获取用户
     */
    public void getListDataByRoleid(){
    	String curr = getPara("pageNumber");
    	String pageSize = getPara("pageSize");
    	String roleid = getPara("roleid");
    	if(StrKit.isBlank(roleid)){
    		renderPage(null,"" ,0);
    	}else{
    		Page<Record> page = SysUser.dao.getPageByRoleid(Integer.valueOf(curr),Integer.valueOf(pageSize),roleid);
    		renderPage(page.getList(),"" ,page.getTotalRow());
    	}
    }
    /***
     * 保存
     */
    public void save(){
    	SysUser newUser = getModel(SysUser.class);
    	String userRoleId = getPara("userRoleId","");
    	if(StrKit.notBlank(newUser.getId())){
    		String password = newUser.getPassword();
    		SysUser old = SysUser.dao.findById(newUser.getId());
    		if(StrKit.isBlank(password)){//如果没传递密码
    			password = old.getPassword();//获取原始密码
    			newUser.setPassword(password);//设置为原始密码
    		}else{//传递了密码 , 设置新密码
    			PasswordService svc = new DefaultPasswordService();
    			newUser.setPassword(svc.encryptPassword(password));//加密新密码
    		}
    		newUser.update();
    		SysUser.dao.giveUserRole(newUser.getId(),userRoleId);
    	}else{
    		newUser.setId(UuidUtil.getUUID());
    		PasswordService svc = new DefaultPasswordService();
    		newUser.setPassword(svc.encryptPassword(newUser.getPassword()));//加密新密码
    		newUser.save();
    		SysUser.dao.giveUserRole(newUser.getId(),userRoleId);
    		idService.addUser(newUser.getUsername());
    	}
    	renderSuccess();
    }
    
    /**
     * 邮箱设置密码
     */
    public void setNewPwd(){
    	SysUser newUser = getModel(SysUser.class);
    	String userRoleId = getPara("userRoleId","");
    	SysUser user = SysUser.dao.getById(newUser.getId());
    	if(StrKit.notBlank(newUser.getId())){
    		String password = newUser.getPassword();
    		SysUser old = SysUser.dao.findById(newUser.getId());
    		if(StrKit.isBlank(password)){//如果没传递密码
    			password = old.getPassword();//获取原始密码
    			user.setPassword(password);//设置为原始密码
    		}else{//传递了密码 , 设置新密码
    			PasswordService svc = new DefaultPasswordService();
    			user.setPassword(svc.encryptPassword(password));//加密新密码
    		}
    		user.update();
    	}
    	renderSuccess();
    }
    
    
    
    
    
    
    /***
     * 获取编辑页面
     */
    public void getEditPage(){
    	//添加和修改
    	String id = getPara("id");
    	if(StrKit.notBlank(id)){//修改
    		SysUser o = SysUser.dao.getById(id);
    		String orgid = o.getOrgid();
    		SysOrg org = SysOrg.dao.getById(orgid);
    		List<SysOrg> orgList = SysOrg.dao.getParentAll(orgid);
    		String orgname = "";
			if(StrKit.notNull(orgList)){
				for (SysOrg sysOrg : orgList) {
					if(StrKit.notBlank(orgname)){
						orgname = orgname+"-"+sysOrg.getName();
					}else{
						orgname = sysOrg.getName();
					}
				}
			}
			setAttr("orgname",org.getName()+orgname);
    		setAttr("org", org);
    		setAttr("o", o);
    		if(StrKit.notNull(o)){
    			setAttr("view", "edit");
    		}
    		List<SysRole> list = SysRole.dao.getAllRoleByUserid(o.getId());
    		List<String> idList = new ArrayList<String>();
    		List<String> nameList = new ArrayList<String>();
    		for(SysRole role : list){
    			idList.add(role.getId());
    			nameList.add(role.getName());
    		}
    		setAttr("userRoleId", StringUtils.join(idList, ","));
    		setAttr("userRoleName", StringUtils.join(nameList, ","));
    	}else{
    		String orgid = getPara("orgid");
    		if(StrKit.notBlank(orgid)){
    			SysOrg org = SysOrg.dao.getById(orgid);
    			setAttr("org", org);
    		}
    	}
    	render("edit.html");
    }
    
    public void userSetting(){
    	setBread("个人信息设置","/","个人信息设置");
    	String id = getPara("id");
    	if(StrKit.notBlank(id)){//修改
    		SysUser o = SysUser.dao.getById(id);
    		String orgid = o.getOrgid();
    		SysOrg org = SysOrg.dao.getById(orgid);
    		setAttr("org", org);
    		setAttr("o", o);
    		List<SysRole> list = SysRole.dao.getAllRoleByUserid(o.getId());
    		List<String> idList = new ArrayList<String>();
    		List<String> nameList = new ArrayList<String>();
    		for(SysRole role : list){
    			idList.add(role.getId());
    			nameList.add(role.getName());
    		}
    		setAttr("userRoleId", StringUtils.join(idList, ","));
    		setAttr("userRoleName", StringUtils.join(nameList, ","));
    	}
    	render("userSetting.html");
    }
    
    /**重置密码
     * 
     */
    public void userpwdSetting(){
    	setBread("个人密码设置","/","个人密码设置");
    	String id = getPara("id");
    	if(StrKit.notBlank(id)){//修改
    		SysUser o = SysUser.dao.getById(id);
    		String orgid = o.getOrgid();
    		SysOrg org = SysOrg.dao.getById(orgid);
    		setAttr("org", org);
    		setAttr("o", o);
    	}
    	render("userpwdSetting.html");
    }
    
    /**发送修改密码邮件验证码
     * 
     */
    public void pwdMail(){
    	String id = getPara("id");
    	System.out.println(id);
    	SysUser user = SysUser.dao.getById(id);
    	String code = RandomUtil.getAuthCodeNumber6(RandomUtil.getAuthCodeNumber(9));
    	if(StrKit.notBlank(user.getEmail())){
    		String flag = MailTemplate.changePwdMail(user.getName(), code, user.getEmail());//发送邮件
        	if("success".equals(flag)){
        		setSessionAttr("user-"+id, code);//将code存入session中，设置有效期为24小时。
        		System.out.println(getSessionAttr("user-"+id));
        		renderSuccess("已发送验证码至邮箱："+user.getEmail()+"，请注意查收！");//前端success内容提示
        	}
    	}else{
    		renderSuccess("无效邮箱地址，请联系系统管理员处理！");//前端错误提示
    	}
    }
    
    /**校验邮箱验证码
     * 
     */
    public void checkMailCode(){
    	String pageMailCode = getPara("verification");
    	String sessionMailCode = getSessionAttr("user-"+ShiroKit.getUserId());
    	if(StrKit.notBlank(sessionMailCode)){
    		if(sessionMailCode.equals(pageMailCode)){
        		renderValidSuccess();
        	}else{
        		renderValidFail();
        	}
    	}else{
    		renderValidFail();
    	}
    }
    
    
    /***
     * 验证用户 , 是否被注册
     */
    public void validUsername(){
    	SysUser user = getModel(SysUser.class);
    	if(user!=null){
    		String username = user.getUsername();
    		if(StrKit.notBlank(username)){
    			SysUser o =  SysUser.dao.getByUsername(username);
    			if(o==null){//用户不存在 
    				renderValidSuccess();
    			}else{
    				renderValidFail();
    			}
    		}else{
    			renderValidSuccess();
    		}
    	}else{
    		renderValidSuccess();
    	}
    }
    
    /***
     * 给用户赋值角色
     */
    public void getGiveUserRolePage(){
    	String userid = getPara("userid");
    	setAttr("userid", userid);
    	render("giveUserRole.html");
    }
    /***
     * 给用户赋值角色
     */
    public void giveUserRole(){
    	String userid = getPara("userid");
    	String data = getPara("data");
    	SysUser.dao.giveUserRole(userid,data);
    	renderSuccess();
    }
    /***
     * 删除
     * @throws Exception
     */
    public void delete() throws Exception{
		String ids = getPara("ids");
    	//执行删除
		SysUser.dao.deleteByIds(ids);
    	renderSuccess("删除成功!");
    }
    
    /***
     * 打开选择多个人员的页面
     */
    public void openSelectManyUserPage(){
    	String orgid = getPara("orgid");
    	setAttr("orgid", orgid);
    	render("selectManyUser.html");
    }
    
    /***
     * 打开选择多个人员的页面，抄送使用
     */
    public void openSelectManyUserPage4CC(){
    	String orgid = getPara("orgid");
    	setAttr("orgid", orgid);
    	String[] idarr = getPara("flowCCId").split(",");
    	List usernameList = new ArrayList();
    	List nameList = new ArrayList();
    	List orgList = new ArrayList();
    	if(StrKit.notBlank(idarr)){
    		for (String userid : idarr) {
				SysUser user = SysUser.dao.getById(userid);
				SysOrg org = SysOrg.dao.getById(user.getOrgid());
				usernameList.add(user.getUsername());
				nameList.add(user.getName());
				orgList.add(org.getName());
			} 
    	}
    	String usernamearr = StringUtil.join(usernameList, ",");
    	String namearr = StringUtil.join(nameList, ",");
    	String orgnamearr = StringUtil.join(orgList, ",");
    	setAttr("idarr", getPara("flowCCId"));
    	setAttr("usernamearr", usernamearr);
    	setAttr("namearr", namearr);
    	setAttr("orgnamearr", orgnamearr);
    	System.out.println("================");
    	System.out.println(usernamearr);
    	System.out.println(orgnamearr);
    	System.out.println("================");
    	render("selectManyUser4CC.html");
    }
    /***
     * 打开选择单个人员的页面
     */
    public void openSelectOneUserPage(){
    	String orgid = getPara("orgid");
    	setAttr("orgid", orgid);
    	render("selectOneUser.html");
    }

    /***
     * 使用角色打开选择人员的页面
     */
    public void openSelectOneUserUseRolePage(){
    	String roleKey = getPara("roleKey");
    	if(StrKit.notBlank(roleKey)){
    		String firstRoleKey = roleKey.split(",")[0];
    		SysRole firstRole = SysRole.dao.getRoleByRoleKey(firstRoleKey);
    		if(firstRole!=null){
    			setAttr("firstRoleId", firstRole.getId());
    		}
    	}
    	setAttr("roleKey", roleKey);
    	render("selectOneUserUseRole.html");
    }
    /**************************************************************************/
	private String pageTitle = "用户管理";//模块页面标题
	private String breadHomeMethod = "getListPage";//面包屑首页方法
	
	public Map<String,String> getPageTitleBread() {
		Map<String,String> pageTitleBread = new HashMap<String,String>();
		pageTitleBread.put("pageTitle", pageTitle);
		pageTitleBread.put("breadHomeMethod", breadHomeMethod);
		return pageTitleBread;
	}
	
	public void setBread(String name,String url,String nowBread){
		Map<String,String> pageTitleBread = new HashMap<String,String>();
		pageTitleBread.put("pageTitle", name);
		pageTitleBread.put("url", url);
		pageTitleBread.put("nowBread", nowBread);
		this.setAttr("pageTitleBread", pageTitleBread);
	}
}