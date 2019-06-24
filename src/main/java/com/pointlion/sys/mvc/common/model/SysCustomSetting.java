package com.pointlion.sys.mvc.common.model;

import com.pointlion.sys.mvc.common.model.base.BaseSysCustomSetting;
import com.pointlion.sys.plugin.shiro.ShiroKit;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class SysCustomSetting extends BaseSysCustomSetting<SysCustomSetting> {
	
	
	public static final SysCustomSetting dao = new SysCustomSetting();
	
	public SysCustomSetting getDefaultCstmSetting(){
		SysCustomSetting set = new SysCustomSetting();
		set.setBoxLay("0");
		set.setBoxBackImg("");//背景图
		set.setAnimate("1");
		set.setAnimateType("");//动画效果
		set.setFixedNavbar("0");
		set.setFixedFooter("0");
		set.setFixedNav("0");
		set.setShowUserImg("1");
		set.setShowShortCut("1");
		set.setNavColl("0");
		set.setOffType("");//侧拉效果
		set.setShowAsd("0");
		set.setFixedAsd("0");
		set.setFloatAsd("1");
		set.setLeftAsd("0");
		set.setDarkAsd("0");
		set.setColorTheme("/common/css/themes/type-a/theme-dark.css");//颜色主题
		return set;
	}
	
	/***
	 * 根据主键获取设置
	 * @param id
	 * @return
	 */
	public SysCustomSetting getCstmSettingById(String id){
		return this.dao().findById(id);
	}
	
	/***
	 * 根据用户id获取设置
	 * @param userid
	 * @return
	 */
	public SysCustomSetting getCstmSettingByUsername(String userid){
		return this.dao().findFirst("select * from sys_custom_setting s where s.user_id='"+userid+"'");
	}
	
	/***
	 * 获取当前登陆用户的设置信息 
	 * @return
	 */
	public SysCustomSetting getLoginUserCstSetting(){
		return SysCustomSetting.dao.getCstmSettingByUsername(ShiroKit.getUsername());
	}
}
