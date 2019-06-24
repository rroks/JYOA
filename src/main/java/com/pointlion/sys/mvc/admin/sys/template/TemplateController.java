package com.pointlion.sys.mvc.admin.sys.template;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.pointlion.sys.interceptor.MainPageTitleInterceptor;
import com.pointlion.sys.mvc.common.base.BaseController;
import com.pointlion.sys.mvc.admin.oa.workflow.WorkFlowService;
import com.pointlion.sys.mvc.common.utils.*;
import com.pointlion.sys.mvc.common.model.SysTemplate;
import com.pointlion.sys.plugin.shiro.ShiroKit;

@Before(MainPageTitleInterceptor.class)
public class TemplateController extends BaseController {
        public static final SysTemplateService service = SysTemplateService.me;
        public static WorkFlowService wfservice = WorkFlowService.me;
        /***
         * 列表页面
         */
    public void getListPage(){
        setBread("功能名称",this.getRequest().getServletPath(),"管理");
        render("list.html");
    }
        /***
         * 获取分页数据
         **/
    public void listData(){
        String curr = getPara("pageNumber");
        String pageSize = getPara("pageSize");
        String nameSearch = getPara("nameSearch","");
        Page<Record> page = service.getPage(Integer.valueOf(curr),Integer.valueOf(pageSize),nameSearch);
        renderPage(page.getList(),"",page.getTotalRow());
    }
        /***
         * 保存
         */
    public void save(){
        SysTemplate o = getModel(SysTemplate.class);
        if(StrKit.notBlank(o.getId())){
            o.update();
        }else{
            o.setId(UuidUtil.getUUID());
            o.setUploadtime(DateUtil.getTime());
            o.save();
        }
        renderSuccess();
    }
    public void Uploadpage() throws FileNotFoundException {
        String tempid = getPara("tempid","");
        setAttr("tempid", tempid);
        render("upload.html");
    }

    public void Upload() throws Exception {
        String tempid = getPara("tempid","");
        SysTemplate o = service.getById(tempid);
        o.setUploaduser(ShiroKit.getUserId());
        o.setUploadtime(DateUtil.getTime());
        String projectPath =  this.getRequest().getSession().getServletContext().getRealPath("");
        String path = "/attachment/"+DateUtil.getYear()+"/"+DateUtil.getMonth()+"/"+DateUtil.getDay();//保存路径
        UploadFile uploadFile = getFile("file",path);
        File goalfile = uploadFile.getFile();
        String bathPath = uploadFile.getUploadPath().replace(path, "");//根路径upload目录
        goalfile.renameTo(new File(o.getFilename()));//文件重命名
        byte[] bufferarray = new byte[1024 * 64];
        FileInputStream input = new FileInputStream(goalfile);
        FileOutputStream fopts = new FileOutputStream(projectPath+o.getFilepath()+o.getFilename());
        int prereadlength;
        while ((prereadlength = input.read(bufferarray)) != -1) {
            fopts.write(bufferarray, 0, prereadlength);
        }
        fopts.close();
        o.update();//保存上传信息
        renderSuccess("上传成功");
    }

    /***
     * 下载文件
     */
    public void downloadFile(){
        String id = getPara("id");
        SysTemplate o = service.getById(id);
        String fileUrl = o.getFilepath();
        String fileName = o.getFilename();
        String baseUrl = this.getRequest().getSession().getServletContext().getRealPath("");
        File file = new File(baseUrl+fileUrl+"/"+fileName);
        renderFile(file);
    }
        /***
         * 提交
         */
        public void startProcess(){
        String id = getPara("id");
        SysTemplate o = SysTemplate.dao.getById(id);
//        o.setIfSubmit(Constants.IF_SUBMIT_YES);
//        String insId = wfservice.startProcess(id, OAConstants.DEFKEY_PROJECT_CHANGE_MEMBER,o.getTitle(),null);
//        o.setProcInsId(insId);
        o.update();
        renderSuccess("提交成功");
    }
        /***
         * 撤回
         */
        public void callBack(){
        String id = getPara("id");
        try{
            SysTemplate o = SysTemplate.dao.getById(id);
//            wfservice.callBack(o.getProcInsId());
//            o.setIfSubmit(Constants.IF_SUBMIT_NO);
//            o.setProcInsId("");
            o.update();
            renderSuccess("撤回成功");
        }catch(Exception e){
            e.printStackTrace();
            renderError("撤回失败");
        }
    }
        /**************************************************************************/
        public void setBread(String name,String url,String nowBread){
        Map<String,String> pageTitleBread = new HashMap<String,String>();
        pageTitleBread.put("pageTitle", name);
        pageTitleBread.put("url", url);
        pageTitleBread.put("nowBread", nowBread);
        this.setAttr("pageTitleBread", pageTitleBread);
    }
}
