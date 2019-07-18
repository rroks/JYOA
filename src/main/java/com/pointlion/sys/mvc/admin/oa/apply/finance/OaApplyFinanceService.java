package com.pointlion.sys.mvc.admin.oa.apply.finance;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.ImmutableMap;
import com.pointlion.sys.mvc.admin.oa.common.CommonFlowController;
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
import com.pointlion.sys.mvc.common.model.OaApplyFinance;
import com.pointlion.sys.mvc.common.model.OaContract;
import com.pointlion.sys.mvc.common.model.OaFlowCarbonC;
import com.pointlion.sys.mvc.common.model.SysUserSign;
import com.pointlion.sys.mvc.common.utils.ExportUtil;
import com.pointlion.sys.mvc.common.utils.ModelToMapUtil;
import com.pointlion.sys.mvc.common.utils.office.word.POITemplateUtil;
import com.pointlion.sys.mvc.common.utils.word.CustomXWPFDocument;
import com.pointlion.sys.mvc.common.utils.word.WordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OaApplyFinanceService {
    public static final OaApplyFinanceService me = new OaApplyFinanceService();
    public static final String TABLE_NAME = OaApplyFinance.tableName;
    public static final WorkFlowService workFlowService = new WorkFlowService();

    private Logger logger = LoggerFactory.getLogger(OaApplyFinanceService.class);

    /***
     * 根据主键查询
     */
    public OaApplyFinance getById(String id) {
        return OaApplyFinance.dao.findById(id);
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


    public Page<Record> getFinancetPage(int pnum, int psize, String userid, String type, String name, String startTime, String endTime) {

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
        if (StrKit.notBlank(name)) {//title\des \receive_money_org_name\tax_price_name\common_price_name\change_des\formproject_name\about_orgname
            sql = sql + " and (o.applyer_name like '%" + name + "%' or o.org_name like '%" + name + "%'"
                    + " or o.title like '%" + name + "%' or o.des like '%" + name + "%' or o.receive_money_org_name like '%" + name + "%'"
                    + " or o.tax_price_name like '%" + name + "%' or o.common_price_name like '%" + name + "%'"
                    + "or  o.change_des like '%" + name + "%' or o.formproject_name like '%" + name + "%' "
                    + "or o.about_orgname like '%" + name + "%' )";
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
            OaApplyFinance o = me.getById(id);
            o.delete();
        }
    }

    public File exportFile(String id, HttpServletRequest request) throws FileNotFoundException {
        OaApplyFinance finance = OaApplyFinance.dao.findById(id);
        OaContract contract = OaContract.dao.getById(finance.getContractId());

        if ("3".equals(finance.getType())) {//如果是税费申请
            finance.setCommonPrice(finance.getTaxPrice());
            finance.setCommonPriceName(finance.getTaxPriceName());
        }

        String path = request.getSession().getServletContext().getRealPath("") + "/WEB-INF/admin/oa/apply/finance/template/";
        String basepath = request.getSession().getServletContext().getRealPath("");
        String templateUrl = path + "finance_" + finance.getType() + ".docx";

        List<Record> list = workFlowService.getHisTaskList2(finance.getProcInsId());
        Map<String, Object> data;
        String exportURL = path + finance.getTitle() + "_" + finance.getCreateTime().replaceAll(" ", "_").replaceAll(":", "-") + ".docx";
        data = ModelToMapUtil.ModelToPoiMap(finance);

        try {
            prepareParas(data, request, contract, finance);
        } catch (FileNotFoundException e) {
            logger.info(e.getMessage(), e);
        }

        if (CollectionUtils.isNotEmpty(list)) {
            for (Record record : list) {
                prepareTask(basepath, record, data);
            }
        }
        File file = null;
        try {
            file = new File(exportURL);
            CustomXWPFDocument doc = WordUtil.generateWord(data, templateUrl);
            FileOutputStream fopts = new FileOutputStream(file);
            doc.write(fopts);
            fopts.close();
        } catch (IOException e) {
            logger.info("@@@@@@@@@@@@@@@@@@\n" + e.getMessage(), e);
        }
        logger.info("@@@@@@@@@@@@@@@@@\n" + String.valueOf(file.exists()));
        return file.exists() ? file : null;
    }

    private void prepareParas(Map<String, Object> data, HttpServletRequest request, OaContract contract, OaApplyFinance finance) throws FileNotFoundException {
        data.put("${caiwu}", "");
        data.put("${caiwu_img}", getdefaultImg(request,350,80));
        data.put("${gc}", "");
        data.put("${gc_img}", getdefaultImg(request,350,80));
        data.put("${zcaiwu}", "");
        data.put("${zcaiwu_img}", getdefaultImg(request,350,80));
        data.put("${b1}", "");
        data.put("${b1_img}", getdefaultImg(request,175,80));
        data.put("${b2}", "");
        data.put("${b2_img}", getdefaultImg(request,350,80));
        data.put("${MainTopLeader}", "");
        data.put("${MainTopLeader_img}", getdefaultImg(request,350,80));
        data.put("${xmjl}", "");
        data.put("${xmjl_img}", getdefaultImg(request,350,80));
        data.put("${zrenshi}", "");
        data.put("${zrenshi_img}", getdefaultImg(request,350,80));
        data.put("${znbm}", "");
        data.put("${znbm_img}", getdefaultImg(request,350,80));

        data.put("${finance_num}", finance.getFinanceNum() == null ? "" : finance.getFinanceNum());
        data.put("${contractName}", contract != null ? contract.getName() : "");
        data.put("${projectName}", finance.getFormprojectName() == null ? "" : finance.getFormprojectName());
    }

    private void prepareTask(String basePath, Record record, Map<String, Object> data) throws FileNotFoundException {
        logger.info(String.format("@@@@@@@@@@@@@@@@@@@@@@@@@@\n%s", "prepareTask"));
        String taskName = record.getStr("taskName");
        String userId = record.getStr("assigneeId");
        String taskId = record.getStr("taskId");
        SysUserSign sign = SysUserSign.dao.getByUserTaskid(userId,taskId);
        Map<String, Object> header = null;
        if (StrKit.notNull(sign)) {
            header = ImmutableMap.<String, Object>builder()
                    .put("width", 128)
                    .put("height", 27)
                    .put("type", "png")
                    .put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basePath + "/" + sign.getSignLocal()), true))
                    .build();
        }
        logger.info(String.format("@@@@@@@@@@@@@@@@@@@@@@@@@@\n%s", taskName));
        switch (taskName) {
            case "分公司财务部审核":
                data.put("${caiwu}", record.getStr("message") == null ? "" : record.getStr("message"));
                data.put("${caiwu_img}", StrKit.notNull(sign) ? header : "");
                break;
            case "总公司财务部审核":
                data.put("${zcaiwu}", record.getStr("message") == null ? "" : record.getStr("message"));
                data.put("${zcaiwu_img}", StrKit.notNull(sign) ? header : "");
                break;
            case "总公司总经理审核":
                data.put("${MainTopLeader}", record.getStr("message") == null ? "" : record.getStr("message"));
                data.put("${MainTopLeader_img}", StrKit.notNull(sign) ? header : "");
                break;
            case "一级分公司总经理审核":
                data.put("${b1}", record.getStr("message") == null ? "" : record.getStr("message"));
                data.put("${b1_img}", StrKit.notNull(sign) ? header : "");
                break;
            case "二级分公司总经理审核":
                data.put("${b2}", record.getStr("message") == null ? "" : record.getStr("message"));
                data.put("${b2_img}", StrKit.notNull(sign) ? header : "");
                break;
            case "总公司综合管理部经理审核":
                data.put("${gc}", record.getStr("message") == null ? "" : record.getStr("message"));
                data.put("${gc_img}", StrKit.notNull(sign) ? header : "");
                break;
            case "项目经理审核":
                data.put("${xmjl}", record.getStr("message") == null ? "" : record.getStr("message"));
                data.put("${xmjl_img}", StrKit.notNull(sign) ? header : "");
                break;
            case "总公司人事行政部经理审核":
                data.put("${zrenshi}", record.getStr("message") == null ? "" : record.getStr("message"));
                data.put("${zrenshi_img}", StrKit.notNull(sign) ? header : "");
                break;
            case "总公司相关职能部门":
                data.put("${znbm}", record.getStr("message") == null ? "" : record.getStr("message"));
                data.put("${znbm_img}", StrKit.notNull(sign) ? header : "");
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
        OaApplyFinance finance = OaApplyFinance.dao.findById(id);
        logger.info("====================\n OaApplyFinance " + finance.toJson());

        if ("3".equals(finance.getType())) {//如果是税费申请
            finance.setCommonPrice(finance.getTaxPrice());
            finance.setCommonPriceName(finance.getTaxPriceName());
        }
        String path = request.getSession().getServletContext().getRealPath("") + "/WEB-INF/admin/oa/apply/finance/template/";
        String basepath = request.getSession().getServletContext().getRealPath("");
        String templateUrl = path + "finance_" + finance.getType() + ".docx";
        logger.info("====================\n path" + path + "=============\n" + basepath);
        logger.info("===================\n templateUrl " + templateUrl);
        OaContract contract = OaContract.dao.getById(finance.getContractId());
//		OaProject project = OaProject.dao.getById(finance.getProjectId());
        List<Record> list = workFlowService.getHisTaskList2(finance.getProcInsId());
        Map<String, Object> data = new HashMap<>(16);
        String exportURL = path + finance.getTitle() + "_" + finance.getCreateTime().replaceAll(" ", "_").replaceAll(":", "-") + ".docx";
//        String exportURL = path + finance.getTitle() + "_" + finance.getCreateTime().replaceAll("：", "-").replaceAll(" ", "_") + ".docx";
        logger.info("====================\n exportURL" + exportURL);
        //       if (finance.getType().equals("1")) {
        data = ModelToMapUtil.ModelToPoiMap(finance);
        data.put("${finance_num}", finance.getFinanceNum() == null ? "" : finance.getFinanceNum());
        data.put("${caiwu}", "");
        data.put("${caiwu_img}", getdefaultImg(request,350,80));
        data.put("${gc}", "");
        data.put("${gc_img}", getdefaultImg(request,350,80));
        data.put("${zcaiwu}", "");
        data.put("${zcaiwu_img}", getdefaultImg(request,350,80));
        data.put("${b1}", "");
        data.put("${b1_img}", getdefaultImg(request,175,80));
        data.put("${b2}", "");
        data.put("${b2_img}", getdefaultImg(request,350,80));
        data.put("${MainTopLeader}", "");
        data.put("${MainTopLeader_img}", getdefaultImg(request,350,80));
        data.put("${xmjl}", "");
        data.put("${xmjl_img}", getdefaultImg(request,350,80));
        data.put("${zrenshi}", "");
        data.put("${zrenshi_img}", getdefaultImg(request,350,80));
        data.put("${znbm}", "");
        data.put("${znbm_img}", getdefaultImg(request,350,80));
        //存放各节点审批人信息
        if (list.size() > 0) {
            for (Record record : list) {
                String taskName = record.getStr("taskName");
                String userId = record.getStr("assigneeId");
                String taskId = record.getStr("taskId");
                SysUserSign sign = SysUserSign.dao.getByUserTaskid(userId,taskId);
                if (StrKit.notNull(sign)) {
                    logger.info("\n\n" + sign.toJson() + "\n\n");
                }
                if (taskName.equals("分公司财务部审核")) {
                    data.put("${caiwu}", record.getStr("message") == null ? "" : record.getStr("message"));
                    if (StrKit.notNull(sign)) {
                        Map<String, Object> header = new HashMap<String, Object>();
                        header.put("width", 128);
                        header.put("height", 27);
                        header.put("type", "png");
                        header.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basepath + "/" + sign.getSignLocal()), true));
                        data.put("${caiwu_img}", header);
                    } else {
                        data.put("${caiwu_img}", "");
                        logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@\n分公司财务部审核");
                    }
                }
                if (taskName.equals("总公司财务部审核")) {
                    data.put("${zcaiwu}", record.getStr("message") == null ? "" : record.getStr("message"));
                    if (StrKit.notNull(sign)) {
                        Map<String, Object> header = new HashMap<String, Object>();
                        header.put("width", 128);
                        header.put("height", 27);
                        header.put("type", "png");
                        header.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basepath + "/" + sign.getSignLocal()), true));
                        data.put("${zcaiwu_img}", header);
                    } else {
                        data.put("${zcaiwu_img}", "");
                        logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@\n总公司财务部审核");
                    }
                }
                if (taskName.equals("总公司总经理审核")) {
                    data.put("${MainTopLeader}", record.getStr("message") == null ? "" : record.getStr("message"));
                    if (StrKit.notNull(sign)) {
                        Map<String, Object> header = new HashMap<String, Object>();
                        header.put("width", 128);
                        header.put("height", 27);
                        header.put("type", "png");
                        header.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basepath + "/" + sign.getSignLocal()), true));
                        data.put("${MainTopLeader_img}", header);
                    } else {
                        data.put("${MainTopLeader_img}", "");
                        logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@\n总公司总经理审核");
                    }
                }
                if (taskName.equals("一级分公司总经理审核")) {
                    data.put("${b1}", record.getStr("message") == null ? "" : record.getStr("message"));
                    if (StrKit.notNull(sign)) {
                        Map<String, Object> header = new HashMap<String, Object>();
                        header.put("width", 128);
                        header.put("height", 27);
                        header.put("type", "png");
                        header.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basepath + "/" + sign.getSignLocal()), true));
                        data.put("${b1_img}", header);
                    } else {
                        data.put("${b1_img}", "");
                        logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@\n一级分公司总经理审核");
                    }
                }
                if (taskName.equals("二级分公司总经理审核")) {
                    data.put("${b2}", record.getStr("message") == null ? "" : record.getStr("message"));
                    if (StrKit.notNull(sign)) {
                        Map<String, Object> header = new HashMap<String, Object>();
                        header.put("width", 128);
                        header.put("height", 27);
                        header.put("type", "png");
                        header.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basepath + "/" + sign.getSignLocal()), true));
                        data.put("${b2_img}", header);
                    } else {
                        data.put("${b2_img}", "");
                        logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@\n二级分公司总经理审核");
                    }
                }
                if (taskName.equals("总公司综合管理部经理审核")) {
                    data.put("${gc}", record.getStr("message") == null ? "" : record.getStr("message"));
                    logger.info("sign local ====================" + sign.getSignLocal());
                    if (StrKit.notNull(sign)) {
                        Map<String, Object> header = new HashMap<String, Object>();
                        header.put("width", 128);
                        header.put("height", 27);
                        header.put("type", "png");
                        header.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basepath + "/" + sign.getSignLocal()), true));
                        data.put("${gc_img}", header);
                    } else {
                        data.put("${gc_img}", "");
                        logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@\n总公司综合管理部经理审核");
                    }
                }
                if (taskName.equals("项目经理审核")) {
                    data.put("${xmjl}", record.getStr("message") == null ? "" : record.getStr("message"));
                    if (StrKit.notNull(sign)) {
                        Map<String, Object> header = new HashMap<String, Object>();
                        header.put("width", 128);
                        header.put("height", 27);
                        header.put("type", "png");
                        header.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basepath + "/" + sign.getSignLocal()), true));
                        data.put("${xmjl_img}", header);
                    } else {
                        data.put("${xmjl_img}", "");
                        logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@\n项目经理审核");
                    }
                }
                if (taskName.equals("总公司人事行政部经理审核")) {
                    data.put("${zrenshi}", record.getStr("message") == null ? "" : record.getStr("message"));
                    if (StrKit.notNull(sign)) {
                        Map<String, Object> header = new HashMap<String, Object>();
                        header.put("width", 128);
                        header.put("height", 27);
                        header.put("type", "png");
                        header.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basepath + "/" + sign.getSignLocal()), true));
                        data.put("${zrenshi_img}", header);
                    } else {
                        data.put("${zrenshi_img}", "");
                        logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@\n总公司人事行政部经理审核");
                    }
                }
                if (taskName.equals("总公司相关职能部门")) {
                    data.put("${znbm}", record.getStr("message") == null ? "" : record.getStr("message"));
                    if (StrKit.notNull(sign)) {
                        Map<String, Object> header = new HashMap<String, Object>();
                        header.put("width", 128);
                        header.put("height", 27);
                        header.put("type", "png");
                        header.put("content", WordUtil.inputStream2ByteArray(new FileInputStream(basepath + "/" + sign.getSignLocal()), true));
                        data.put("${znbm_img}", header);
                    } else {
                        data.put("${znbm_img}", "");
                        logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@\n总公司相关职能部门");
                    }
                }
            }
        }
//        }
//        else {
//            List<String> receivelist = new ArrayList<String>();
//            for (Record r : list) {
//                String lineContent = r.getStr("taskName") + ":" + r.getStr("assignee") + ":" + r.getStr("message");
//                lineContent = ExportUtil.addEmptyString(lineContent, 15);
//                receivelist.add(lineContent);
//            }
//            finance.put("receiver", StringUtils.join(receivelist, POITemplateUtil.NewLine + "-------------------------------------------------------------------------------" + POITemplateUtil.NewLine));
//            //抄送
//            List<OaFlowCarbonC> cclist = OaFlowCarbonC.dao.find("select * from oa_flow_carbon_c c ,sys_user u  where u.id=c.user_id and business_id = '" + id + "' ");
//            if (!StrKit.isBlank(cclist.toString())) {
//                List<String> nameList = new ArrayList<String>();
//                for (OaFlowCarbonC cc : cclist) {
//                    nameList.add(cc.getStr("name"));
//                }
//                data.put("cc", StringUtils.join(nameList, ","));
//            } else {
//                data.put("cc", "无");
//            }
//            data = ModelToMapUtil.ModelToPoiMap(finance);
//        }
        logger.info("==================== \n FQ \n");
        data.put("${contractName}", contract != null ? contract.getName() : "");
        data.put("${projectName}", finance.getFormprojectName() == null ? "" : finance.getFormprojectName());
        logger.info("==================== \n DATA \n" + data.size());
        File file = null;
        try {
            CustomXWPFDocument doc = WordUtil.generateWord(data, templateUrl);
            logger.info("======================= CustomXWPFDocument \n" + doc);
            file = new File(exportURL);
            FileOutputStream fopts = new FileOutputStream(file);
            doc.write(fopts);
            fopts.close();
            logger.info(file.toString());
        } catch (IOException e) {
            logger.info(e.getMessage(), e);
        }
//		ExportUtil.export(data, templateUrl, exportURL);
//		File file = new File(exportURL);
        if (file.exists()) {
            logger.info("yes, it has file ===========================");
            return file;
        } else {
            logger.info("no, we don't have ===========================");
            return null;
        }
    }

    /***
     * 获取申请类型对应的名称
     * @param type
     * @return
     * 1：建材及设备采购款支付申请
     * 2：工程款支付申请
     * 3：税费申请
     * 4：归还贷款申请
     * 5：财务相关费用支付申请
     * 6：管理费用支付申请（差旅，应酬）
     * 7：管理费用支付申请（其他）
     * 8：无票报销
     * 9：业务暂借款申请
     * 10：资金调拨申请
     */
    public String getTypeName(String type) {
        String name = "";
        if ("1".equals(type)) {
            name = "建材及设备采购款支付申请";
        } else if ("2".equals(type)) {
            name = "工程款支付申请";
        } else if ("3".equals(type)) {
            name = "税费申请";
        } else if ("4".equals(type)) {
            name = "归还贷款申请";
        } else if ("5".equals(type)) {
            name = "财务相关费用支付申请";
        } else if ("6".equals(type)) {
            name = "管理费用支付申请（差旅，应酬）";
        } else if ("7".equals(type)) {
            name = "管理费用支付申请（其他）";
        } else if ("8".equals(type)) {
            name = "无票报销";
        } else if ("9".equals(type)) {
            name = "业务暂借款申请";
        } else if ("10".equals(type)) {
            name = "资金调拨申请";
        } else if ("11".equals(type)) {
            name = "审批内卡(融资用)";
        } else if ("12".equals(type)) {
            name = "审批内卡(财务筹划)";
        } else if ("13".equals(type)) {
            name = "审批内卡(工商登记变更等)";
        } else if ("14".equals(type)) {
            name = "审批内卡(其他)";
        } else if ("15".equals(type)) {
            name = "固定资产申请";
        }
        return name;
    }

    /***
     * 获取申请类型对应的名称
     * @param type
     * @return
     * 1：建材及设备采购款支付申请
     * 2：工程款支付申请
     * 3：税费申请
     * 4：归还贷款申请
     * 5：财务相关费用支付申请
     * 6：管理费用支付申请（差旅，应酬）
     * 7：管理费用支付申请（其他）
     * 8：无票报销
     * 9：业务暂借款申请
     * 10：资金调拨申请
     */
    public String getDefKeyByType(String type) {
        String defkey = "";
        if ("1".equals(type)) {
            defkey = OAConstants.DEFKEY_APPLY_FINANCE_1;
        } else if ("2".equals(type)) {
            defkey = OAConstants.DEFKEY_APPLY_FINANCE_2;
        } else if ("3".equals(type)) {
            defkey = OAConstants.DEFKEY_APPLY_FINANCE_3;
        } else if ("4".equals(type)) {
            defkey = OAConstants.DEFKEY_APPLY_FINANCE_4;
        } else if ("5".equals(type)) {
            defkey = OAConstants.DEFKEY_APPLY_FINANCE_5;
        } else if ("6".equals(type)) {
            defkey = OAConstants.DEFKEY_APPLY_FINANCE_6;
        } else if ("7".equals(type)) {
            defkey = OAConstants.DEFKEY_APPLY_FINANCE_7;
        } else if ("8".equals(type)) {
            defkey = OAConstants.DEFKEY_APPLY_FINANCE_8;
        } else if ("9".equals(type)) {
            defkey = OAConstants.DEFKEY_APPLY_FINANCE_9;
        } else if ("10".equals(type)) {
            defkey = OAConstants.DEFKEY_APPLY_FINANCE_10;
        } else if ("11".equals(type)) {
            defkey = OAConstants.DEFKEY_APPLY_FINANCE_11;
        } else if ("12".equals(type)) {
            defkey = OAConstants.DEFKEY_APPLY_FINANCE_12;
        } else if ("13".equals(type)) {
            defkey = OAConstants.DEFKEY_APPLY_FINANCE_13;
        } else if ("14".equals(type)) {
            defkey = OAConstants.DEFKEY_APPLY_FINANCE_14;
        } else if ("15".equals(type)) {
            defkey = OAConstants.DEFKEY_APPLY_FINANCE_15;
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