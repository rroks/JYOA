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
                String fullFileName = file.getName();
                String absoluteName = file.getAbsolutePath();
                filePdf = Transform2PDF.office2PDF(absoluteName, absoluteName.substring(0, absoluteName.lastIndexOf(".")) + ".pdf");
            } else if ("bankaccount".equals(type)) {//如果是银行卡开卡流程
                file = bankAccountService.export(id, this.getRequest());
                String fullFileName = file.getName();
                String absoluteName = file.getAbsolutePath();
                filePdf = Transform2PDF.office2PDF(absoluteName, absoluteName.substring(0, absoluteName.lastIndexOf(".")) + ".pdf");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(null != filePdf){
            logger.info("@@@@@@@@@@@@@@@@@@@@@@\nfile is not null");
        	file = filePdf;
        }
        renderFile(file);
    }
}
