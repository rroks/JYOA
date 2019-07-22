package com.pointlion.sys.mvc.admin.oa.apply.bankaccount;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.ImmutableMap;
import com.pointlion.sys.mvc.common.model.SysUserSign;
import com.pointlion.sys.mvc.common.utils.word.CustomXWPFDocument;
import com.pointlion.sys.mvc.common.utils.word.WordUtil;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.pointlion.sys.mvc.admin.oa.common.OAConstants;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.admin.sys.dataauth.SysDataAuthTranslator;
import com.pointlion.sys.mvc.common.model.OaApplyBankAccount;
import com.pointlion.sys.mvc.common.model.OaFlowCarbonC;
import com.pointlion.sys.mvc.common.utils.ExportUtil;
import com.pointlion.sys.mvc.common.utils.ModelToMapUtil;
import com.pointlion.sys.mvc.common.utils.office.word.POITemplateUtil;

public class OaApplyBankAccountService {
    public static final OaApplyBankAccountService me = new OaApplyBankAccountService();
    public static final String TABLE_NAME = OaApplyBankAccount.tableName;
    public static final WorkFlowService workFlowService = new WorkFlowService();

    /***
     * 根据主键查询
     */
    public OaApplyBankAccount getById(String id) {
        return OaApplyBankAccount.dao.findById(id);
    }

    /***
     * 获取分页
     */
    public Page<Record> getPage(int pnum, int psize, String type, String name, String startTime, String endTime) {
        String dataAuth = SysDataAuthTranslator.me.translator(TABLE_NAME);
        String sql = " from " + dataAuth + " o LEFT JOIN act_hi_procinst p ON o.proc_ins_id=p.ID_  where 1=1 ";
        if (StrKit.notBlank(type)) {
            sql = sql + " and o.type='" + type + "' ";
        }
        sql = sql + getQuerySql(type, name, startTime, endTime);
        return Db.paginate(pnum, psize, " select o.*,p.PROC_DEF_ID_ defid ", sql);
    }

    public Page<Record> getBankAccountPage(int pnum, int psize, String userid, String type, String name, String startTime, String endTime) {

        String sql = " from " + TABLE_NAME + " o LEFT JOIN act_hi_procinst p ON o.proc_ins_id=p.ID_  where 1=1 ";
        if (StrKit.notBlank(type)) {
            sql = sql + " and o.type='" + type + "' ";
        }
        if (StrKit.notBlank(userid)) {
            sql = sql + " and o.userid='" + userid + "' ";
        }
        sql = sql + getQuerySql(type, name, startTime, endTime);
        return Db.paginate(pnum, psize, " select o.*,p.PROC_DEF_ID_ defid ", sql);
    }

    public File exportFile(String id, HttpServletRequest request) throws IOException {
        OaApplyBankAccount bankaccount = OaApplyBankAccount.dao.findById(id);
        String path = request.getSession().getServletContext().getRealPath("") + "/WEB-INF/admin/oa/apply/bankaccount/template/";
        String templateUrl = path + "bankaccount_" + bankaccount.getType() + ".docx";
        String basepath = request.getSession().getServletContext().getRealPath("");
        List<Record> list = workFlowService.getHisTaskList2(bankaccount.getProcInsId());
        Map<String, Object> data = ModelToMapUtil.ModelToPoiMap(bankaccount);
        List<String> receivelist = new ArrayList<String>();
        try {
            prepareParas(data, request);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (CollectionUtils.isNotEmpty(list)) {
            for (Record record : list) {
                prepareTask(basepath, record, data);
                String lineContent = record.getStr("taskName") + ":" + record.getStr("assignee") + ":" + record.getStr("message");
                lineContent = ExportUtil.addEmptyString(lineContent, 15);
                receivelist.add(lineContent);
            }
        }

        bankaccount.put("projectName", bankaccount.getFormProjectName());
        bankaccount.put("receiver", StringUtils.join(receivelist, POITemplateUtil.NewLine + "-------------------------------------------------------------------------------" + POITemplateUtil.NewLine));
        //抄送
        String findCcTargetSql = "select * from oa_flow_carbon_c c ,sys_user u  where u.id=c.user_id and business_id = '%s' ";
        List<OaFlowCarbonC> cclist = OaFlowCarbonC.dao.find(String.format(findCcTargetSql, id));
        if (StrKit.notNull(cclist)) {
            List<String> nameList = new ArrayList<String>();
            for (OaFlowCarbonC cc : cclist) {
                nameList.add(cc.getStr("name"));
            }
            bankaccount.put("cc", StringUtils.join(nameList, ","));
        } else {
            bankaccount.put("cc", "无");
        }

        bankaccount.put("beizhuName", bankaccount.getDes());
        data.put("${projectName}", bankaccount.getFormProjectName() == null ? "" : bankaccount.getFormProjectName());

        String exportURL = path + bankaccount.getTitle() + "_" + bankaccount.getCreateTime().replaceAll(" ", "_").replaceAll(":", "-") + ".docx";
        CustomXWPFDocument doc = WordUtil.generateWord(data, templateUrl);
        File file = new File(exportURL);
        FileOutputStream fopts = new FileOutputStream(file);
        doc.write(fopts);
        fopts.close();
        return file.exists() ? file : null;
    }

    private void prepareParas(Map<String, Object> data, HttpServletRequest request) throws FileNotFoundException {
        data.put("${caiwu}", "");
//        data.put("${caiwu_img}", getdefaultImg(request,350,80));
        data.put("${caiwu_img}", "");
        data.put("${gc}", "");
//        data.put("${gc_img}", getdefaultImg(request,350,80));
        data.put("${gc_img}", "");
        data.put("${zcaiwu}", "");
//        data.put("${zcaiwu_img}", getdefaultImg(request,350,80));
        data.put("${zcaiwu_img}", "");
        data.put("${b1}", "");
//        data.put("${b1_img}", getdefaultImg(request,175,80));
        data.put("${b1_img}", "");
        data.put("${b2}", "");
//        data.put("${b2_img}", getdefaultImg(request,175,80));
        data.put("${b2_img}", "");
        data.put("${MainTopLeader}", "");
//        data.put("${MainTopLeader_img}", getdefaultImg(request,350,80));
        data.put("${MainTopLeader_img}", "");
        data.put("${xmjl}", "");
//        data.put("${xmjl_img}", getdefaultImg(request,350,80));
        data.put("${xmjl_img}", "");
        data.put("${zrenshi}", "");
//        data.put("${zrenshi_img}", getdefaultImg(request,350,80));
        data.put("${zrenshi_img}", "");
    }

    private void prepareTask(String basePath, Record r, Map<String, Object> data) throws FileNotFoundException {
        String userId = r.getStr("assigneeId");
        String taskName = r.getStr("taskName");
        String taskId = r.getStr("taskId");
        SysUserSign sign = SysUserSign.dao.getByUserTaskid(userId,taskId);
        if (!StrKit.notNull(sign)) {
            sign = SysUserSign.dao.getByUserid(userId);
        }

        Map<String, Object> header = null;
        int width, height;
        if (taskName.equals("分公司财务部审核") || taskName.equals("总公司财务部审核")) {
            width = 200;
            height = 50;
        } else {
            width = 128;
            height = 27;
        }
        if (StrKit.notNull(sign)) {
            header = ImmutableMap.<String, Object>builder()
                    .put("width", width)
                    .put("height", height)
                    .put("type", "png")
                    .put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basePath + "/" + sign.getSignLocal()), true))
                    .build();
        }
        switch (taskName) {
            case "分公司财务部审核":
                data.put("${caiwu}", r.getStr("message") == null ? "" : r.getStr("message"));
//                data.put("${caiwu_img}", StrKit.notNull(sign) ? header : "");
                if (StrKit.notNull(sign)) {
                    data.put("${caiwu_img}", header);
                }
                break;
            case "总公司财务部审核":
                data.put("${zcaiwu}", r.getStr("message") == null ? "" : r.getStr("message"));
//                data.put("${zcaiwu_img}", StrKit.notNull(sign) ? header : "");
                if (StrKit.notNull(sign)) {
                    data.put("${zcaiwu_img}", header);
                }
                break;
            case "总公司总经理审核":
                data.put("${MainTopLeader}", r.getStr("message") == null ? "" : r.getStr("message"));
//                data.put("${MainTopLeader_img}", StrKit.notNull(sign) ? header : "");
                if (StrKit.notNull(sign)) {
                    data.put("${caiwu_img}", header);
                }
                break;
            case "一级分公司总经理审核":
                data.put("${b1}", r.getStr("message") == null ? "" : r.getStr("message"));
//                data.put("${b1_img}", StrKit.notNull(sign) ? header : "");
                if (StrKit.notNull(sign)) {
                    data.put("${b1_img}", header);
                }
                break;
            case "二级分公司总经理审核":
                data.put("${b2}", r.getStr("message") == null ? "" : r.getStr("message"));
//                data.put("${b2_img}", StrKit.notNull(sign) ? header : "");
                if (StrKit.notNull(sign)) {
                    data.put("${b2_img}", header);
                }
                break;
            case "总公司综合管理部经理审核":
                data.put("${gc}", r.getStr("message") == null ? "" : r.getStr("message"));
//                data.put("${gc_img}", StrKit.notNull(sign) ? header : "");
                if (StrKit.notNull(sign)) {
                    data.put("${gc_img}", header);
                }
                break;
            case "项目经理审核":
                data.put("${xmjl}", r.getStr("message") == null ? "" : r.getStr("message"));
//                data.put("${xmjl_img}", StrKit.notNull(sign) ? header : "");
                if (StrKit.notNull(sign)) {
                    data.put("${xmjl_img}", header);
                }
                break;
            case "总公司人事行政部经理审核":
                data.put("${zrenshi}", r.getStr("message") == null ? "" : r.getStr("message"));
//                data.put("${zrenshi_img}", StrKit.notNull(sign) ? header : "");
                if (StrKit.notNull(sign)) {
                    data.put("${zrenshi_img}", header);
                }
                break;
            case "总公司相关职能部门":
                data.put("${znbm}", r.getStr("message") == null ? "" : r.getStr("message"));
//                data.put("${znbm_img}", StrKit.notNull(sign) ? header : "");
                if (StrKit.notNull(sign)) {
                    data.put("${znbm_img}", header);
                }
                break;
            default:
                break;
        }
    }
    /***
     * 导出
     * @throws Exception
     */
    public File export(String id, HttpServletRequest request) throws Exception {
        OaApplyBankAccount bankaccount = OaApplyBankAccount.dao.findById(id);
        String path = request.getSession().getServletContext().getRealPath("") + "/WEB-INF/admin/oa/apply/bankaccount/template/";
        String templateUrl = path + "bankaccount_" + bankaccount.getType() + ".docx";
        String basepath = request.getSession().getServletContext().getRealPath("");
        List<Record> list = workFlowService.getHisTaskList2(bankaccount.getProcInsId());
        Map<String, Object> data = new HashMap<>(16);
        List<String> receivelist = new ArrayList<String>();
        data = ModelToMapUtil.ModelToPoiMap(bankaccount);
        data.put("${caiwu}", "");
        data.put("${caiwu_img}", "");
        data.put("${gc}", "");
        data.put("${gc_img}", "");
        data.put("${zcaiwu}", "");
        data.put("${zcaiwu_img}", "");
        data.put("${b1}", "");
        data.put("${b1_img}", "");
        data.put("${b2}", "");
        data.put("${b2_img}", "");
        data.put("${MainTopLeader}", "");
        data.put("${MainTopLeader_img}", "");
        data.put("${xmjl}", "");
        data.put("${xmjl_img}", "");
        data.put("${zrenshi}", "");
        data.put("${zrenshi_img}", "");
        for (Record r : list) {
            String userId = r.getStr("assigneeId");
            String taskName = r.getStr("taskName");
            String taskId = r.getStr("taskId");
            SysUserSign sign = SysUserSign.dao.getByUserTaskid(userId,taskId);
            if (!StrKit.notNull(sign)) {
                sign = SysUserSign.dao.getByUserid(userId);
            }
            if (taskName.equals("分公司财务部审核")) {
                data.put("${caiwu}", r.getStr("message") == null ? "" : r.getStr("message"));
                if (StrKit.notNull(sign)) {
                    Map<String, Object> header = new HashMap<String, Object>();
                    header.put("width", 200);//128
                    header.put("height", 50);//27
                    header.put("type", "png");
                    header.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basepath + "/" + sign.getSignLocal()), true));
                    data.put("${caiwu_img}", header);
                } else {
                    data.put("${caiwu_img}", "");
                }
            }
            if (taskName.equals("总公司财务部审核")) {
                data.put("${zcaiwu}", r.getStr("message") == null ? "" : r.getStr("message"));
                if (StrKit.notNull(sign)) {
                    Map<String, Object> header = new HashMap<String, Object>();
                    header.put("width", 200);
                    header.put("height", 50);
                    header.put("type", "png");
                    header.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basepath + "/" + sign.getSignLocal()), true));
                    data.put("${zcaiwu_img}", header);
                } else {
                    data.put("${zcaiwu_img}", "");
                }
            }
            if (taskName.equals("总公司总经理审核")) {
                data.put("${MainTopLeader}", r.getStr("message") == null ? "" : r.getStr("message"));
                if (StrKit.notNull(sign)) {
                    Map<String, Object> header = new HashMap<String, Object>();
                    header.put("width", 128);
                    header.put("height", 27);
                    header.put("type", "png");
                    header.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basepath + "/" + sign.getSignLocal()), true));
                    data.put("${MainTopLeader_img}", header);
                } else {
                    data.put("${MainTopLeader_img}", "");
                }
            }
            if (taskName.equals("一级分公司总经理审核")) {
                data.put("${b1}", r.getStr("message") == null ? "" : r.getStr("message"));
                if (StrKit.notNull(sign)) {
                    Map<String, Object> header = new HashMap<String, Object>();
                    header.put("width", 128);
                    header.put("height", 27);
                    header.put("type", "png");
                    header.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basepath + "/" + sign.getSignLocal()), true));
                    data.put("${b1_img}", header);
                } else {
                    data.put("${b1_img}", "");
                }
            }
            if (taskName.equals("二级分公司总经理审核")) {
                data.put("${b2}", r.getStr("message") == null ? "" : r.getStr("message"));
                if (StrKit.notNull(sign)) {
                    Map<String, Object> header = new HashMap<String, Object>();
                    header.put("width", 128);
                    header.put("height", 27);
                    header.put("type", "png");
                    header.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basepath + "/" + sign.getSignLocal()), true));
                    data.put("${b2_img}", header);
                } else {
                    data.put("${b2_img}", "");
                }
            }
            if (taskName.equals("总公司综合管理部经理审核")) {
                data.put("${gc}", r.getStr("message") == null ? "" : r.getStr("message"));
                if (StrKit.notNull(sign)) {
                    Map<String, Object> header = new HashMap<String, Object>();
                    header.put("width", 128);
                    header.put("height", 27);
                    header.put("type", "png");
                    header.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basepath + "/" + sign.getSignLocal()), true));
                    data.put("${gc_img}", header);
                } else {
                    data.put("${gc_img}", "");
                }
            }
            if (taskName.equals("项目经理审核")) {
                data.put("${xmjl}", r.getStr("message") == null ? "" : r.getStr("message"));
                if (StrKit.notNull(sign)) {
                    Map<String, Object> header = new HashMap<String, Object>();
                    header.put("width", 128);
                    header.put("height", 27);
                    header.put("type", "png");
                    header.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basepath + "/" + sign.getSignLocal()), true));
                    data.put("${xmjl_img}", header);
                } else {
                    data.put("${xmjl_img}", "");
                }
            }
            if (taskName.equals("总公司人事行政部经理审核")) {
                data.put("${zrenshi}", r.getStr("message") == null ? "" : r.getStr("message"));
                if (StrKit.notNull(sign)) {
                    Map<String, Object> header = new HashMap<String, Object>();
                    header.put("width", 128);
                    header.put("height", 27);
                    header.put("type", "png");
                    header.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basepath + "/" + sign.getSignLocal()), true));
                    data.put("${zrenshi_img}", header);
                } else {
                    data.put("${zrenshi_img}", "");
                }
            }
            if (taskName.equals("总公司相关职能部门")) {
                data.put("${znbm}", r.getStr("message") == null ? "" : r.getStr("message"));
                if (StrKit.notNull(sign)) {
                    Map<String, Object> header = new HashMap<String, Object>();
                    header.put("width", 128);
                    header.put("height", 27);
                    header.put("type", "png");
                    header.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basepath + "/" + sign.getSignLocal()), true));
                    data.put("${znbm_img}", header);
                } else {
                    data.put("${znbm_img}", "");
                }
            }
            String lineContent = r.getStr("taskName") + ":" + r.getStr("assignee") + ":" + r.getStr("message");
            lineContent = ExportUtil.addEmptyString(lineContent, 15);
            receivelist.add(lineContent);
        }

        bankaccount.put("projectName", bankaccount.getFormProjectName());
        bankaccount.put("receiver", StringUtils.join(receivelist, POITemplateUtil.NewLine + "-------------------------------------------------------------------------------" + POITemplateUtil.NewLine));
        //抄送
        List<OaFlowCarbonC> cclist = OaFlowCarbonC.dao.find("select * from oa_flow_carbon_c c ,sys_user u  where u.id=c.user_id and business_id = '" + id + "' ");
        if (StrKit.notNull(cclist)) {
            List<String> nameList = new ArrayList<String>();
            for (OaFlowCarbonC cc : cclist) {
                nameList.add(cc.getStr("name"));
            }
            bankaccount.put("cc", StringUtils.join(nameList, ","));
        } else {
            bankaccount.put("cc", "无");
        }

        bankaccount.put("beizhuName", bankaccount.getDes());
        data.put("${projectName}", bankaccount.getFormProjectName() == null ? "" : bankaccount.getFormProjectName());
        String exportURL = path + bankaccount.getTitle() + "_" + bankaccount.getCreateTime().replaceAll(" ", "_").replaceAll(":", "-") + ".docx";
        CustomXWPFDocument doc = WordUtil.generateWord(data, templateUrl);
        File file = new File(exportURL);
        FileOutputStream fopts = new FileOutputStream(file);
        doc.write(fopts);
        fopts.close();

        if (file.exists()) {
            return file;
        } else {
            return null;
        }
    }

    /****
     * 获取查询sql
     * @param type
     * @param name
     * @param startTime
     * @param endTime
     * @return
     */
    public String getQuerySql(String type, String name, String startTime, String endTime) {
        String sql = " ";
        if (StrKit.notBlank(type)) {
            sql = sql + " and o.type='" + type + "' ";
        }
        if (StrKit.notBlank(name)) {//des\reason\open_bank\account_type\legel_seal\finance_seal\account_name\account_num\formproject_name
            sql = sql + " and (o.applyer_name like '%" + name + "%' or o.org_name like '%" + name + "%' "
                    + "or o.reason like '%" + name + "%' or o.des like '%" + name + "%' or o.open_bank like '%" + name + "%'"
                    + "or o.account_type like '%" + name + "%' or o.account_name like '%" + name + "%' or o.account_num like '%" + name + "%'"
                    + " or o.formproject_name like '%" + name + "%')";
        }
        if (StrKit.notBlank(startTime)) {
            sql = sql + "  and o.create_time >= '" + startTime + " 00:00:00'";
        }
        if (StrKit.notBlank(endTime)) {
            sql = sql + "  and o.create_time <= '" + endTime + " 23:59:59'";
        }
        return sql;
    }

    /***
     * 删除
     * @param ids
     */
    @Before(Tx.class)
    public void deleteByIds(String ids) {
        String idarr[] = ids.split(",");
        for (String id : idarr) {
            OaApplyBankAccount o = me.getById(id);
            o.delete();
        }
    }

    /***
     * 获取申请类型对应的名称
     * @param type
     * @return
     * 1：开户申请
     * 2：销户申请
     * 3：变更申请
     * 4：开通网银
     * 5：注销网银
     * 6：开户+开通网银
     */
    public String getTypeName(String type) {
        String name = "";
        if ("1".equals(type)) {
            name = "开户申请";
        } else if ("2".equals(type)) {
            name = "销户申请";
        } else if ("3".equals(type)) {
            name = "变更申请";
        } else if ("4".equals(type)) {
            name = "开通网银";
        } else if ("5".equals(type)) {
            name = "注销网银";
        } else if ("6".equals(type)) {
            name = "开户+开通网银";
        }
        return name;
    }

    /***
     * 获取申请类型对应的名称
     * @param type
     * @return
     * 1：开户申请
     * 2：销户申请
     * 3：变更申请
     * 4：开通网银
     * 5：注销网银
     * 6：开户开通网银
     */
    public String getDefKeyByType(String type) {
        String defkey = "";
        if ("1".equals(type)) {
            defkey = OAConstants.DEFKEY_APPLY_ACCOUNTBANK_1;
        } else if ("2".equals(type)) {
            defkey = OAConstants.DEFKEY_APPLY_ACCOUNTBANK_2;
        } else if ("3".equals(type)) {
            defkey = OAConstants.DEFKEY_APPLY_ACCOUNTBANK_3;
        } else if ("4".equals(type)) {
            defkey = OAConstants.DEFKEY_APPLY_ACCOUNTBANK_4;
        } else if ("5".equals(type)) {
            defkey = OAConstants.DEFKEY_APPLY_ACCOUNTBANK_5;
        } else if ("6".equals(type)) {
            defkey = OAConstants.DEFKEY_APPLY_ACCOUNTBANK_6;
        }
        return defkey;
    }
    public Map<String, Object> getdefaultImg(HttpServletRequest request,int Width,int Height) throws FileNotFoundException {
        String basepath = request.getSession().getServletContext().getRealPath("");
        Map<String, Object> header = new HashMap<String, Object>();
        header.put("width", Width);//350
        header.put("height",Height);//80
        header.put("type", "png");
        header.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basepath + "/common/img/new.png") , true));
        return header;
    }


}