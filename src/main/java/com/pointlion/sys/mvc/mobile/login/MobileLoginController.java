/**
 * @author Lion
 * @date 2017年1月24日 下午12:02:35
 * @qq 439635374
 */
package com.pointlion.sys.mvc.mobile.login;

import java.util.HashMap;
import java.util.Map;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.pointlion.sys.mvc.admin.oa.common.CommonFlowService;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.common.base.BaseController;
import com.pointlion.sys.mvc.common.model.OaNotice;
import com.pointlion.sys.mvc.common.model.SysOrg;
import com.pointlion.sys.mvc.common.model.SysUser;
import com.pointlion.sys.mvc.common.utils.AliyunOssUtil;
import com.pointlion.sys.mvc.common.utils.Base64Util;
import com.pointlion.sys.mvc.common.utils.DateUtil;
import com.pointlion.sys.mvc.common.utils.RandomUtil;
import com.pointlion.sys.mvc.common.utils.UuidUtil;
import com.pointlion.sys.mvc.mobile.notice.MobileNoticeService;
import com.pointlion.sys.plugin.mail.MailTemplate;

/***
 * 手机的登陆控制器（手机端）
 * @author Administrator
 *
 */
public class MobileLoginController extends BaseController {
	static MobileNoticeService noticeService = MobileNoticeService.me;
	/***
	 * 手机端登陆
	 */
	public void doLogin(){
		String username = getPara("username");
		String password = getPara("password");
		//验证用户是否存在
//		 SysUser user = null;
//	        if("admin".equals(username)){
//	        	user = SysUser.dao.getByUsername(username);
//	        }else{
//	        	user = SysUser.dao.getByUserMobile(username);
//	        }
		SysUser user = SysUser.dao.getByUserNameOrMobile(username);
		if(user==null){
        	renderError900("用户不存在");
        }else{
        	//验证密码
        	PasswordService svc = new DefaultPasswordService();
        	if(svc.passwordsMatch(password, user.getPassword())){
        		Map<String,Object> map = new HashMap<String,Object>();
        		map.put("ID", user.getId());
        		map.put("USERNAME", user.getUsername());
        		map.put("NAME", user.getName());
        		map.put("IDCARD", user.getIdcard());
        		map.put("sign_img", user.getSignImg()!=null?user.getSignImg():"");
        		map.put("head_img", user.getImg()!=null?user.getImg():"");
        		map.put("MOBILE", user.getMobile()!=null?user.getMobile():"");
        		map.put("MAIL", user.getEmail()!=null?user.getEmail():"");
        		String orgId = user.getOrgid();
        		map.put("ORGID", orgId);
        		SysOrg org = new SysOrg();
        		map.put("ORGNAME", org.getById(orgId)==null?"":org.getById(orgId).getName());
        		WorkFlowService wfs = new WorkFlowService();
    			Page<OaNotice> noticePage = noticeService.getMobileHomePageMyNoticePage(user.getId());
    			map.put("noticeList", noticePage.getList());//通告list
    			map.put("todoNum", String.valueOf(wfs.getTodoNum(user.getUsername())));//待办条数
    			map.put("haveDoneNum", String.valueOf(wfs.getContractHaveDoneNum(username)+wfs.getFinanceHaveDoneNum(username)+wfs.getBankAccountHaveDoneNum(username)));//已完成条数
    			map.put("myApplyDone", String.valueOf(new CommonFlowService().countMyApplyTaskList(username,"1")));//我的申请-已完成数
    			map.put("myApplyUndo", String.valueOf(new CommonFlowService().countMyApplyTaskList(username,"0")));//我的申请-正在审批数
        		renderSuccess800(map, 1, "登录成功");
        	}else{
        		renderError900("用户名或密码错误");
        	}
        }
	}
	
    /***
     * 上传头像
     */
    public void uploadHead() {
        String userId = getPara("userId");
        SysUser user = SysUser.dao.getById(userId);
        String base64 = getPara("img");
        if (StrKit.isBlank(base64)) {
            renderError900("图片为空");
        } else {
            System.out.println(base64);
            String basepath = this.getRequest().getSession().getServletContext().getRealPath("");
            String path = "/upload/userHead/" + DateUtil.getYear() + "/" + DateUtil.getMonth();
            String filename = UuidUtil.getUUID() + ".png";
            System.out.println(basepath);
            String finalPath = "decoAssit/images/head/" + filename;
            if (!StrKit.isBlank(user.getImg())) {
//				new MobileRegisterService().delOldImg(basepath, user.getImg());
                new AliyunOssUtil().delImg(user.getImg());
            }
//			Base64Util.GenerateImage(base64, basepath + path, filename);
            new AliyunOssUtil().uplodaImg(finalPath, Base64Util.GenerateImageInput(base64));
//			user.setImg(path + "/" + filename);
            user.setImg(finalPath);
            user.update();
//			renderSuccess800(new MobileRegisterService().getUserDetail(user,basepath), 1, null);
//            renderSuccess800(new MobileRegisterService().getUserDetail(user, basepath), 1, null);
        }
    }
    
    /**获取邮箱验证码
     * 
     */
    public void getMsgCode(){
    	String username = getPara("username");
     	SysUser user = SysUser.dao.getByUsername(username);
     	String code = RandomUtil.getAuthCodeNumber6(RandomUtil.getAuthCodeNumber(9));
     	if(StrKit.notBlank(user.getEmail())){
     		String flag = MailTemplate.changePwdMail(user.getName(), code, user.getEmail());//发送邮件
         	if("success".equals(flag)){
         		setSessionAttr("user-"+user.getId(), code);//将code存入session中，设置有效期为24小时。
         		System.out.println(getSessionAttr("user-"+user.getId()));
         		renderSuccess800("", 0, "已发送验证码至邮箱："+user.getEmail()+"，请注意查收！");
//         		renderSuccess("已发送验证码至邮箱："+user.getEmail()+"，请注意查收！");//前端success内容提示
         	}
     	}else{
     		renderError900("无效邮箱地址，请联系系统管理员处理！");//前端错误提示
     	}
    }
    
    /**找回密码
     * 
     */
    public void findPassword(){
    	String username = getPara("username");
    	String newPassword = getPara("newPassword");
    	String msgCode = getPara("msgCode");
    	SysUser user = SysUser.dao.getByUsername(username);
    	String sesseionCode = getSessionAttr("user-"+user.getId());
    	if(StrKit.notBlank(sesseionCode)){
    		if(msgCode.equals(sesseionCode)){
    			PasswordService svc = new DefaultPasswordService();
                user.setPassword(svc.encryptPassword(newPassword));// 加密新密码
                user.update();
                renderSuccess800("", 0, "修改成功");
    		}else{
    			renderError900("验证码错误，请核实！");
    		}
    	}else{
    		renderError900("验证码已失效，请重新发送！");
    	}
    }
}
