package com.pointlion.sys.plugin.mail;

import java.util.ArrayList;
import java.util.List;

public class MailTemplate {
	public static String changePwdMail(String name, String usercode, String mailAddress){
		String subjectText = "来自南方物业OA系统邮件";
		String content ="<div style='width:640px; background:#fff; border:solid 1px #efefef; margin:0 auto; padding:35px 0 35px 0'>"+
		" <table width='560' border='0' align='center' cellpadding='0' cellspacing='0' style=' margin:0 auto; margin-left:30px; margin-right:30px;'>"+
		"    <tbody><tr>"+
		" <td><img src=''></td>"+
		"    </tr>"+
		"    <tr>"+
		"      <td>"+
		"      <h3 style='font-weight:normal; font-size:18px'>您好！亲爱的<span style='font-weight:bold; margin-left:5px;'>"+name+"</span></h3>"+
		"      <p style='margin:5px 0; padding:3px 0;color:#666; font-size:14px'>南方物业OA邮箱验证通知:</p>"+
		"      <p style='color:#666; font-size:14px'>请在24小时内使用下面验证码完成邮箱验证： "+usercode+
		"      <p style='margin:0 0 5px 0; padding:0 0 3px 0;'>"+
		"      <p style='margin:10px 0 5px 0; padding:3px 0;color:#666; font-size:14px'>如果验证码失效，请重新发送验证码。</p>"+
		"      <p style='text-align:center'><img src=''></p>"+
		"      </td>"+
		"    </tr>"+
		"    </tbody>"+
		" </table>"+
		"</div>";
		List list = new ArrayList();
		MailKit.send(mailAddress,list, subjectText, content);
		return "success";
	}
}
