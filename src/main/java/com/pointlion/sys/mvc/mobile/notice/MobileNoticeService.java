package com.pointlion.sys.mvc.mobile.notice;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.pointlion.sys.mvc.admin.oa.notice.NoticeConstants;
import com.pointlion.sys.mvc.admin.oa.notice.NoticeService;
import com.pointlion.sys.mvc.common.model.OaNotice;

/***
 * 手机端通知公告调用服务
 * @author Administrator
 *
 */
public class MobileNoticeService extends NoticeService{
	public static final MobileNoticeService me = new MobileNoticeService();
	
	public Record getNoticeDetail(String id,String userid){
		return Db.findFirst("SELECT DISTINCT n.*, u.if_sign FROM oa_notice n,oa_notice_user u WHERE n.id = u.notice_id AND n.id='"+id+"' and u.user_id='"+userid+"'");
	}
	public Page<OaNotice> getMobileHomePageMyNoticePage(String userid){
		return OaNotice.dao.paginate(1, 3, "select DISTINCT n.*,u.if_sign "," from oa_notice n ,oa_notice_user u where n.id=u.notice_id and u.user_id='"+userid+"' and n.if_publish='"+NoticeConstants.NOTICE_IF_PUBLISH_YES+"' order by n.publish_time desc");
	}
}
