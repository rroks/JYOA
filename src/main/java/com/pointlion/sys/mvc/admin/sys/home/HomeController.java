/**

 * @author Lion
 * @date 2017年1月24日 下午12:02:35
 * @qq 439635374
 */
package com.pointlion.sys.mvc.admin.sys.home;

import java.util.*;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Record;
import com.pointlion.sys.mvc.admin.oa.common.CommonFlowService;
import com.pointlion.sys.mvc.admin.oa.common.WorkFlowUtil;
import com.pointlion.sys.mvc.admin.oa.customWorkflow.OaCustomflowCasenodeService;
import com.pointlion.sys.mvc.admin.oa.customWorkflow.OaCustomflowJoinltyService;
import com.pointlion.sys.mvc.admin.oa.notice.NoticeService;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.admin.sys.login.SessionUtil;
//import com.pointlion.sys.mvc.admin.apply.resget.OaResGetConstants;
//import com.pointlion.sys.mvc.admin.bumph.BumphConstants;
//import com.pointlion.sys.mvc.admin.login.SessionUtil;
//import com.pointlion.sys.mvc.admin.notice.NoticeService;
//import com.pointlion.sys.mvc.admin.assessment.WorkFlowService;
import com.pointlion.sys.mvc.common.base.BaseController;
import com.pointlion.sys.mvc.common.model.OaApplyCustom;
import com.pointlion.sys.mvc.common.model.SysCustomSetting;
import com.pointlion.sys.mvc.common.model.SysFriend;
import com.pointlion.sys.mvc.common.model.SysMenu;
import com.pointlion.sys.mvc.common.model.SysRole;
import com.pointlion.sys.mvc.common.model.SysUser;
import com.pointlion.sys.mvc.common.model.VTasklist;
import com.pointlion.sys.mvc.common.utils.Constants;
import com.pointlion.sys.plugin.shiro.ShiroKit;
import com.pointlion.sys.plugin.shiro.ext.SimpleUser;

/***
 * 首页控制器
 */
public class HomeController extends BaseController {
	static WorkFlowService workflowService = WorkFlowService.me;
	static NoticeService noticeService = new NoticeService();
	static CommonFlowService commonFlowService = CommonFlowService.me;
	static OaCustomflowCasenodeService CustomService = OaCustomflowCasenodeService.me;
	static OaCustomflowJoinltyService JoinltyService = OaCustomflowJoinltyService.me;
	/***
	 * 登陆成功获取首页
	 */
    public void index(){
    	SimpleUser user = ShiroKit.getLoginUser();
    	String username = user.getUsername();
    	SysUser u = SysUser.dao.getByUsername(username);
    	SessionUtil.setUsernameToSession(this.getRequest(), username);
    	//加载个性化设置
    	SysCustomSetting setting = SysCustomSetting.dao.getCstmSettingByUsername(username);
    	if(setting==null){
    		setAttr("setting", SysCustomSetting.dao.getDefaultCstmSetting());
    	}else{
    		setAttr("setting", setting);
    	}
    	List<SysUser> friends = SysFriend.dao.getUserFriend(u.getId());
    	setAttr("friends", friends);//我的好友
    	setAttr("user", u);//当前用户
    	setAttr("userName", user.getName());//我的姓名
    	setAttr("userEmail", user.getEmail());//我的邮箱
    	setAttrToDoList(username,user.getId());//获取待办
    	setAttrHavedoneList(username);//获取已办
    	//获取首页通知公告
    	setAttr("NoticeList",noticeService.getMyNotice(user.getId()));
    	List<SysMenu> mlist = new ArrayList<SysMenu>();
    	if(user.getUsername().equals(Constants.SUUUUUUUUUUUUUPER_USER_NAME)){//特殊入口
    		mlist=SysMenu.dao.getAllMenu();
    	}else{
    		//查询所有有权限的菜单
        	mlist = SysRole.dao.getRoleAuthByUserid(u.getId(), "1","#root");//规定只有四级菜单 在这里暂定为A,B,C,D
        	for(SysMenu a : mlist){
        		List<SysMenu> blist = SysRole.dao.getRoleAuthByUserid(u.getId(), "1",a.getId());//A下面的菜单
        		a.setChildren(blist);
        		for(SysMenu b : blist){
        			List<SysMenu> clist = SysRole.dao.getRoleAuthByUserid(u.getId(), "1",b.getId());//B下面的菜单
        			b.setChildren(clist);
        			for(SysMenu c : clist){
            			List<SysMenu> dlist = SysRole.dao.getRoleAuthByUserid(u.getId(), "1",c.getId());//B下面的菜单
            			c.setChildren(dlist);
            		}
        		}
        	}
    	}
    	setAttr("mlist", mlist);
    	render("/WEB-INF/admin/home/index.html");
    }
    
    /***
     * 设定，待办，数据
     * @param username
     */
    private void setAttrToDoList(String username,String userid){
    	Map<String,List<Record>> todoMap = new HashMap<String,List<Record>>();
    	int todoListCount = 0; 
    	List<VTasklist> defkList = VTasklist.dao.find("select DEFKEY from v_tasklist t where (t.ASSIGNEE='"+username+"' or t.CANDIDATE='"+username+"') GROUP BY t.DEFKEY");
    	for(VTasklist t:defkList){
    		String defkey = t.getDEFKEY();
    		String tablename = WorkFlowUtil.getTablenameByDefkey(defkey);
    		if(StrKit.notBlank(tablename)){//如果属于固定流程
    			List<Record> todolist = workflowService.getToDoListByKey(tablename,defkey,username);
    			if(todolist!=null&&todolist.size()>0){
    				todoListCount = todoListCount + todolist.size();
    				todoMap.put(defkey, todolist);
    			}
    		}else{//自定义流程
    			List<Record> todolist = workflowService.getToDoListByKey(OaApplyCustom.tableName,defkey,username);
    			if(todolist!=null&&todolist.size()>0){
    				todoListCount = todoListCount + todolist.size();
    				todoMap.put(defkey, todolist);
    			}
    		}
    	}
    	//自定义审批流程
		Map<String, List<Record>> CustomtodoMap = CustomService.getcurrenttask(userid);
		Iterator<Map.Entry<String, List<Record>>> it = CustomtodoMap.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, List<Record>> entry =it.next();
			if(todoMap.containsKey(entry.getKey())){
				List<Record> list = todoMap.get(entry.getKey());
				for(Record r:entry.getValue()){
					list.add(r);
				}
				todoListCount = todoListCount +entry.getValue().size();
			}else{
				todoMap.put(entry.getKey(),entry.getValue());
				todoListCount =todoListCount +entry.getValue().size();
			}
		}
		//自定义审批协办
		Map<String, List<Record>> CustomJoinMap = JoinltyService.getcurrenttask(userid);
		Iterator<Map.Entry<String, List<Record>>> Joinit = CustomJoinMap.entrySet().iterator();
		while(Joinit.hasNext()){
			Map.Entry<String, List<Record>> entry =Joinit.next();
			if(todoMap.containsKey(entry.getKey())){
				List<Record> list = todoMap.get(entry.getKey());
				for(Record r:entry.getValue()){
					list.add(r);
				}
				todoListCount = todoListCount +entry.getValue().size();
			}else{
				todoMap.put(entry.getKey(),entry.getValue());
				todoListCount =todoListCount +entry.getValue().size();
			}
		}

    	setAttr("todoListCount", todoListCount);
    	setAttr("todoMap", todoMap);
    	
    	
//    	List<Record> todoList = workflowService.getToDoListByUsername(username);
//    	setAttr("todoList", todoList);
    	
    	
//    	//内部发文待办
//    	List<Record> bumphList = workflowService.getToDoListByKey(OaBumph.tableName,OAConstants.DEFKEY_BUMPH,username);
//    	setAttr("bumphList", bumphList);
//    	//物品领用待办
//    	List<Record> resGetList = workflowService.getToDoListByKey(OaApplyResGet.tableName,OAConstants.DEFKEY_APPLY_RESGET,username);
//    	setAttr("resGetList", resGetList);
//    	//名片印刷申请
//    	List<Record> businessCardList = workflowService.getToDoListByKey(OaApplyBusinessCard.tableName,OAConstants.DEFKEY_APPLY_BUSINESSCARD,username);
//    	setAttr("businessCardList", businessCardList);
//    	//花费申请
//    	List<Record> costList = workflowService.getToDoListByKey(OaApplyCost.tableName,OAConstants.DEFKEY_APPLY_COST,username);
//    	setAttr("costList", costList);
//    	//礼物申请
//    	List<Record> giftList = workflowService.getToDoListByKey(OaApplyGift.tableName,OAConstants.DEFKEY_APPLY_GIFT,username);
//    	setAttr("giftList", giftList);
//    	//宾馆申请
//    	List<Record> hotelList = workflowService.getToDoListByKey(OaApplyHotel.tableName,OAConstants.DEFKEY_APPLY_HOTEL,username);
//    	setAttr("hotelList", hotelList);
//    	//会议室申请
//    	List<Record> meetRoomList = workflowService.getToDoListByKey(OaApplyMeetroom.tableName,OAConstants.DEFKEY_APPLY_MEETROOM,username);
//    	setAttr("meetRoomList", meetRoomList);
//    	//办公用品申请
//    	List<Record> officeObjectList = workflowService.getToDoListByKey(OaApplyOfficeObject.tableName,OAConstants.DEFKEY_APPLY_OFFICEOBJECT,username);
//    	setAttr("officeObjectList", officeObjectList);
//    	//汽车借用申请
//    	List<Record> useCarList = workflowService.getToDoListByKey(OaApplyUseCar.tableName,OAConstants.DEFKEY_APPLY_USE_CAR,username);
//    	setAttr("useCarList", useCarList);
//    	//公章申请
//    	List<Record> sealList = workflowService.getToDoListByKey(OaApplySeal.tableName,OAConstants.DEFKEY_APPLY_SEAL,username);
//    	setAttr("sealList", sealList);
//    	//车船票申请
//    	List<Record> trainTicketList = workflowService.getToDoListByKey(OaApplyTrainTicket.tableName,OAConstants.DEFKEY_APPLY_TRAINTICKET,username);
//    	setAttr("trainTicketList", trainTicketList);
//    	//私车公用申请
//    	List<Record> userCarWorkList = workflowService.getToDoListByKey(OaApplyUsercarWork.tableName,OAConstants.DEFKEY_APPLY_USERCARWORK,username);
//    	setAttr("userCarWorkList", userCarWorkList);
//    	//调岗申请
//    	List<Record> userChangeStationList = workflowService.getToDoListByKey(OaApplyUserChangeStation.tableName,OAConstants.DEFKEY_APPLY_USERCHANGESTATION,username);
//    	setAttr("userChangeStationList", userChangeStationList);
//    	//转正申请
//    	List<Record> userRegularList = workflowService.getToDoListByKey(OaApplyUserRegular.tableName,OAConstants.DEFKEY_APPLY_USERREGULAR,username);
//    	setAttr("userRegularList", userRegularList);
//    	//辞职申请
//    	List<Record> userDimissionList = workflowService.getToDoListByKey(OaApplyUserDimission.tableName,OAConstants.DEFKEY_APPLY_USERDIMISSION,username);
//    	setAttr("userDimissionList", userDimissionList);
//    	//请假/公出申请
//    	List<Record> leaveList = workflowService.getToDoListByKey(OaApplyLeave.tableName,OAConstants.DEFKEY_APPLY_LEAVE,username);
//    	setAttr("leaveList", leaveList);
//    	//项目立项申请
//    	List<Record> projectList = workflowService.getToDoListByKey(OaProject.tableName,OAConstants.DEFKEY_PROJECT,username);
//    	setAttr("projectList", projectList);
    }
    
    /***
     * 设定已办数据
     */
    public void setAttrHavedoneList(String username){
    	List<String> havedoneKeyList = commonFlowService.getHavedoneDefkeyList(ShiroKit.getUsername());
		havedoneKeyList.addAll(CustomService.getHavedoneDefkeyList(ShiroKit.getUserId()));
    	setAttr("havedoneKeyList", havedoneKeyList);
    }
    
    /***
     * 首页内容页
     */
    public void getMyHome(){
    	render("/WEB-INF/admin/home/myHome.html");
    }
    /**
     * 内容页
     * */
    public void content(){
    	render("/WEB-INF/admin/home/content.html");
    }
    /***
     * 获取消息中心最新消息
     */
    public void getSiteMessageTipPage(){
    	render("/WEB-INF/admin/home/siteMessageTip.html");
    }
	}
