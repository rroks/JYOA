package com.pointlion.sys.mvc.common.utils.word;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Test {
      
    public static void main(String[] args) throws Exception {  
          
        Map<String, Object> param = new HashMap<String, Object>();  
          
        Map<String,Object> header = new HashMap<String, Object>();  
        header.put("width", 300);
        header.put("height", 80);
        header.put("type", "png");  
        header.put("content", WordUtil.inputStream2ByteArray(new FileInputStream("D:\\new.png"), true)); 
        param.put("${caiwu_img}",header);
        
        
        Map<String,Object> header2 = new HashMap<String, Object>();  
        header2.put("width", 300);
        header2.put("height", 80);
        header2.put("type", "png");  
        header2.put("content", WordUtil.inputStream2ByteArray(new FileInputStream("D:\\new.png"), true)); 
        
        List list = new ArrayList();
        Map map = new HashMap();
        map.put("", "");
        map.put("", "");
        map.put("", "");
//        list.add("人民币玩家你好");
        list.add(header2);
        list.add(header);
//        list.add("helloword");
        param.put("${zcaiwu_img}",list);
        
          
        param.put("${table1}", "1");
        param.put("${table2}", "2");
        param.put("${table5}", "5");
        param.put("${table3}", "3");
        int title = 1;
        //param.put("${caiwu}", "财务审核通过：【同意】");
        param.put("${img"+title+"}", header);
        //param.put("${zcaiwu}", "总公司1221综合部经理审核通过：【同意】");
        param.put("${zcaiwu_img}", header2);

        CustomXWPFDocument doc = WordUtil.generateWord(param, "D:\\bankaccount_1.docx");
        FileOutputStream fopts = new FileOutputStream("D:\\new2018.docx");  
        doc.write(fopts);  
        fopts.close();  
    }
}  