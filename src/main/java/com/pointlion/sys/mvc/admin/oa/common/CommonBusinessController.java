package com.pointlion.sys.mvc.admin.oa.common;

import java.io.File;

import com.pointlion.sys.mvc.admin.oa.apply.bankaccount.OaApplyBankAccountService;
import com.pointlion.sys.mvc.admin.oa.apply.finance.OaApplyFinanceService;
import com.pointlion.sys.mvc.admin.oa.contract.OaContractService;
import com.pointlion.sys.mvc.common.base.BaseController;
import com.pointlion.sys.mvc.common.utils.Transform2PDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonBusinessController extends BaseController {
    public static final OaContractService contractService = OaContractService.me;
    public static final OaApplyFinanceService financeService = OaApplyFinanceService.me;
    public static final OaApplyBankAccountService bankAccountService = OaApplyBankAccountService.me;

    private Logger logger = LoggerFactory.getLogger(CommonBusinessController.class);

    public void export() {
        String id = getPara("id");
        String type = getPara("type");
        logger.info(type);
        File file = null;
        File filePdf = null;
        try {
            if ("contract".equals(type)) {//如果是合同
                file = contractService.exportSign(id, this.getRequest());
                String fullFileName = file.getName();
                String absoluteName = file.getAbsolutePath();
                filePdf = Transform2PDF.office2PDF(absoluteName, absoluteName.substring(0, absoluteName.lastIndexOf(".")) + ".pdf");
            } else if ("finance".equals(type)) {//如果是财务流程
                file = financeService.exportFile(id, this.getRequest());
                logger.info("reach file");
                String fullFileName = file.getName();
                String absoluteName = file.getAbsolutePath();
                logger.info("reach before 2PDF");
                try {
                    filePdf = Transform2PDF.office2PDF(absoluteName, absoluteName.substring(0, absoluteName.lastIndexOf(".")) + ".pdf");
                } catch (Exception e) {
                    logger.info(e.getMessage(), e);
                }
                logger.info("reach after 2PDF");
            } else if ("bankaccount".equals(type)) {//如果是银行卡开卡流程
                file = bankAccountService.exportFile(id, this.getRequest());
                String fullFileName = file.getName();
                String absoluteName = file.getAbsolutePath();
                filePdf = Transform2PDF.office2PDF(absoluteName, absoluteName.substring(0, absoluteName.lastIndexOf(".")) + ".pdf");
            }
        } catch (Exception e) {
            logger.info("&&&&&&&&&&&&&&&&&\n" + e.getMessage(), e);
        }
        logger.info("@@@@@@@@@@@@@@@@@@@@@@\n" + String.valueOf(null != filePdf) + String.valueOf(null != file));
//        renderSuccess();
        renderFile(null != filePdf ? filePdf : file);
    }
}
