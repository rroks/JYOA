package com.pointlion.sys.mvc.common.utils.word;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONObject;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WordUtil {
    private static Logger logger = LoggerFactory.getLogger(WordUtil.class);
  
    /** 
     * 根据指定的参数值、模板，生成 word 文档 
     * @param param 需要替换的变量 
     * @param template 模板 
     */  
    public static CustomXWPFDocument generateWord(Map<String, Object> param, String template) {  
        CustomXWPFDocument doc = null;  
        try {  
            OPCPackage pack = POIXMLDocument.openPackage(template);  
            doc = new CustomXWPFDocument(pack);  
            if (param != null && param.size() > 0) {  
                  
                //处理段落  
                List<XWPFParagraph> paragraphList = doc.getParagraphs();
                processParagraphs(paragraphList, param, doc);

                //处理表格  
                Iterator<XWPFTable> it = doc.getTablesIterator();  
                while (it.hasNext()) {  
                    XWPFTable table = it.next();  
                    List<XWPFTableRow> rows = table.getRows();  
                    for (XWPFTableRow row : rows) {  
                        List<XWPFTableCell> cells = row.getTableCells();  
                        for (XWPFTableCell cell : cells) {
                            List<XWPFParagraph> paragraphListTable =  cell.getParagraphs();  
                            processParagraphs(paragraphListTable, param, doc);  
                        }
                    }  
                }  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return doc;  
    }  
    /** 
     * 处理段落 
     * @param paragraphList 
     */  
    public static void processParagraphs(List<XWPFParagraph> paragraphList,Map<String, Object> param,CustomXWPFDocument doc){  
        if(paragraphList != null && paragraphList.size() > 0){  
            for(XWPFParagraph paragraph:paragraphList){  
                List<XWPFRun> runs = paragraph.getRuns();  

                	String text = "";
                	for (XWPFRun run : runs) 
                		text += run.getText(0); 
                   // System.out.println(runs);
                    if(text != null){  
                        boolean isSetText = false;  
                        for (Entry<String, Object> entry : param.entrySet()) {  
                            String key = entry.getKey();  
                            if(text.indexOf(key) != -1){  
                                isSetText = true;  
                                Object value = entry.getValue();  
                                if (value instanceof String) {//文本替换  
                                    text = text.replace(key, value.toString());
                                } else if (value instanceof Map) {//图片替换  
                                    text = text.replace(key, "");
                                    logger.info("$%^&*&^%$#@#$%^&*()(*&^%$#\n" + new JSONObject((Map)value).toJSONString());
                                    Map pic = (Map)value;  
                                    int width = Integer.parseInt(pic.get("width").toString());  
                                    int height = Integer.parseInt(pic.get("height").toString());  
                                    int picType = getPictureType(pic.get("type").toString());  
                                    byte[] byteArray = (byte[]) pic.get("content");  
                                    ByteArrayInputStream byteInputStream = new ByteArrayInputStream(byteArray);  
                                    try {  
//                                        int ind = doc.addPicture(byteInputStream,picType);  
//                                        doc.createPicture(ind, width , height,paragraph);
                                    	doc.addPictureData(byteInputStream, picType);
                                        doc.createPicture(doc.getAllPictures().size() - 1, width, height, paragraph);
                                        System.out.println("@@@@@@@@\nreplace pic");
                                        logger.info("@@@@@@@@\nreplace pic");
                                    } catch (Exception e) {  
                                        e.printStackTrace();
                                        logger.info("()()()(*&^%$#@#$%^&\n not replace pic");
                                    }
                                }
                            }
                        }
                        if(isSetText){  
                            int flag = 1;
                        	for (XWPFRun run : runs){
                        		if(flag--==1) {
                                    run.setText(text, 0);
                                    run.setFontFamily("微软雅黑");
                                }
                        		else {
                                    run.setText("", 0);
                                }
                        	
                            }
                        }
                }  
            }  
        }  
    }  
    /** 
     * 根据图片类型，取得对应的图片类型代码 
     * @param picType 
     * @return int 
     */  
    private static int getPictureType(String picType){  
        int res = CustomXWPFDocument.PICTURE_TYPE_PICT;  
        if(picType != null){  
            if(picType.equalsIgnoreCase("png")){  
                res = CustomXWPFDocument.PICTURE_TYPE_PNG;  
            }else if(picType.equalsIgnoreCase("dib")){  
                res = CustomXWPFDocument.PICTURE_TYPE_DIB;  
            }else if(picType.equalsIgnoreCase("emf")){  
                res = CustomXWPFDocument.PICTURE_TYPE_EMF;  
            }else if(picType.equalsIgnoreCase("jpg") || picType.equalsIgnoreCase("jpeg")){  
                res = CustomXWPFDocument.PICTURE_TYPE_JPEG;  
            }else if(picType.equalsIgnoreCase("wmf")){  
                res = CustomXWPFDocument.PICTURE_TYPE_WMF;  
            }  
        }  
        return res;  
    }  
    /** 
     * 将输入流中的数据写入字节数组 
     * @param in 
     * @return 
     */  
    public static byte[] inputStream2ByteArray(InputStream in,boolean isClose){  
        byte[] byteArray = null;  
        try {  
            int total = in.available();  
            byteArray = new byte[total];  
            in.read(byteArray);  
        } catch (IOException e) {
            logger.info("**********\n" + e.getMessage(), e);
        } finally{  
            if(isClose){  
                try {  
                    in.close();  
                } catch (Exception e2) {
                    System.out.println("关闭流失败");  
                }  
            }  
        }  
        return byteArray;  
    }


}  