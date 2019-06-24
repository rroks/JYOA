/**
 * @author Lion
 * @date 2017年1月24日 下午12:02:35
 * @qq 439635374
 */
package com.pointlion.sys.config;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.Const;
import com.jfinal.ext.handler.UrlSkipHandler;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.template.Engine;
import com.pointlion.sys.config.routes.AMSRoutes;
import com.pointlion.sys.config.routes.MobileRoutes;
import com.pointlion.sys.config.routes.OARoutes;
import com.pointlion.sys.config.routes.SysRoutes;
import com.pointlion.sys.handler.GlobalHandler;
import com.pointlion.sys.interceptor.ExceptionInterceptor;
import com.pointlion.sys.interceptor.IfLoginInterceptor;
import com.pointlion.sys.interceptor.WorkFlowHisInterceptor;
import com.pointlion.sys.mvc.admin.oa.common.OAConstants;
import com.pointlion.sys.mvc.admin.oa.common.WorkFlowUtil;
import com.pointlion.sys.mvc.common.model._MappingKit;
import com.pointlion.sys.mvc.common.utils.UuidUtil;
import com.pointlion.sys.plugin.activiti.ActivitiPlugin;
import com.pointlion.sys.plugin.mail.MailPlugin;
import com.pointlion.sys.plugin.quartz.QuartzPlugin;
import com.pointlion.sys.plugin.shiro.ShiroInterceptor;
import com.pointlion.sys.plugin.shiro.ShiroKit;
import com.pointlion.sys.plugin.shiro.ShiroPlugin;

public class MainConfig extends JFinalConfig {
    Routes routes;
    
	/**
	 * 配置JFinal常量
	 */
	@Override
	public void configConstant(Constants me) {
		//读取数据库配置文件
		PropKit.use("config.properties");
		//设置当前是否为开发模式
		me.setDevMode(PropKit.getBoolean("devMode"));
		//设置默认上传文件保存路径 getFile等使用
		me.setBaseUploadPath("upload/");
		//设置上传最大限制尺寸
		me.setMaxPostSize(100*Const.DEFAULT_MAX_POST_SIZE);
		//获取beetl模版引擎
//		me.setRenderFactory(new BeetlRenderFactory());
//		me.setError404View("/error/404.html");
        // 获取GroupTemplate ,可以设置共享变量等操作
//        @SuppressWarnings("unused")
//		GroupTemplate groupTemplate = BeetlRenderFactory.groupTemplate ;
		//设置编码
		me.setEncoding("UTF-8");
	}

	/**
	 * 配置JFinal插件
	 * 数据库连接池
	 * ORM
	 * 缓存等插件
	 * 自定义插件
	 */
	@Override
	public void configPlugin(Plugins me) {
		//配置数据库连接池插件
		DruidPlugin druidPlugin =new DruidPlugin(PropKit.get("jdbcUrl"), PropKit.get("user"), PropKit.get("password"));
		//orm映射 配置ActiveRecord插件
		ActiveRecordPlugin arp=new ActiveRecordPlugin(druidPlugin);
		arp.setShowSql(PropKit.getBoolean("devMode"));
		arp.setDialect(new MysqlDialect());
		_MappingKit.mapping(arp);
		ActivitiPlugin acitivitiPlugin = new ActivitiPlugin();
		ShiroPlugin shiroPlugin = new ShiroPlugin(this.routes);
	    shiroPlugin.setLoginUrl("/admin/login");//登陆url：未验证成功跳转
	    shiroPlugin.setSuccessUrl("/admin/index");//登陆成功url：验证成功自动跳转
	    shiroPlugin.setUnauthorizedUrl("/admin/login/needPermission");//授权url：未授权成功自动跳转
	    QuartzPlugin quatrZPlugin = new QuartzPlugin();
	    MailPlugin mailPlugin = new MailPlugin(PropKit.use("mail.properties").getProperties());
	    //添加到插件列表中
	    me.add(druidPlugin);//数据库连接池插件
	    me.add(arp);//ARP插件
	    me.add(acitivitiPlugin);//流程插件
	    me.add(shiroPlugin);//权限插件
	    me.add(mailPlugin);//邮件发送插件
	    me.add(quatrZPlugin);//定时任务插件
	}
	
	/**
	 * 配置全局拦截器
	 */
	@Override
	public void configInterceptor(Interceptors me) {
		 me.add(new ShiroInterceptor());//shiro拦截器
		 me.add(new IfLoginInterceptor());//判断是否登录拦截器
		 me.add(new WorkFlowHisInterceptor());//流程历史拦截器
		 me.add(new ExceptionInterceptor());//通用异常处理拦截器
	}
	
	/**
	 * 配置全局处理器
	 */
	@Override
	public void configHandler(Handlers handler) {
		//log.info("configHandler 全局配置处理器，设置跳过哪些URL不处理");
		handler.add(new UrlSkipHandler("/|/admin/friendchat/.*|/ca/.*|/se/.*|.*.htm|.*.html|.*.js|.*.css|.*.json|.*.png|.*.gif|.*.jpg|.*.jpeg|.*.bmp|.*.ico|.*.exe|.*.txt|.*.zip|.*.rar|.*.7z|.*.tff|.*.woff|.*.ttf|.*.map|.*.xml|.*.woff2", false));
		handler.add(new GlobalHandler());
	}
	

	
	/**
	 * 配置JFinal路由映射
	 */
	@Override
	public void configRoute(Routes me) {
		this.routes = me;//shiro使用
		me.add(new OARoutes());//办公路由
		me.add(new AMSRoutes());//资产路由
		me.add(new SysRoutes());//系统管理路由
		me.add(new MobileRoutes());//手机端管理路由
	}
	
	@Override
	public void configEngine(Engine me) {
		me.addSharedObject("shiro",new ShiroKit());//提供给模板能使用Shiro权限校验
		me.addSharedObject("OAConstants", new OAConstants());//提供给模板能引用后台常量类
		me.addSharedObject("WorkFlowUtil", new WorkFlowUtil());//提供给模板使用能流程工具类
		me.addSharedObject("UuidUtil", new UuidUtil());//提供给模板能生成UUID
		//通用附件绑定模版函数
		me.addSharedFunction("/WEB-INF/admin/sys/attachment/businessIncludeBtn.html");
		//通用流程函数
		me.addSharedFunction("/WEB-INF/admin/oa/workflow/commonFlowModule.html");
		//资产选择资产信息组件
		me.addSharedFunction("/WEB-INF/admin/ams/asset/selectAssetButton.html");
		//项目管理选择项目组件
		me.addSharedFunction("/WEB-INF/admin/oa/project/selectProjectButton.html");
		//合同管理选择合同组件
		me.addSharedFunction("/WEB-INF/admin/oa/contract/selectContractButton.html");
	}
}
