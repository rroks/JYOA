package com.pointlion.sys.mvc.common.utils;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;
import com.jfinal.kit.PropKit;

import java.io.File;
import java.io.IOException;

public class Transform2PDF {
    public final static String officeAddress = PropKit.use("config.properties").get("jodconverter.address");
    public final static String officeHome = PropKit.use("config.properties").get("jodconverter.officeHome");
    public final static String portNumber = PropKit.use("config.properties").get("jodconverter.portNumber");

    public static File office2PDF(String sourceFile, String destFile) {
        try {
            File outputFile = new File(destFile);
//        	if(outputFile.exists()){
//        		return outputFile;
//        	}else{
            File inputFile = new File(sourceFile);
            if (!inputFile.exists()) {
                System.out.println("null");
                ;// 找不到源文件, 则返回-1
            }

            // 如果目标路径不存在, 则新建该路径
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }
            // 调用openoffice服务线程
            /** 我把openOffice下载到了 C:/Program Files (x86)/下  ,下面的写法自己修改编辑就可以**/
//            C:\Program Files (x86)\OpenOffice 4\program\soffice.exe
//            String command = "C:/Program Files (x86)/OpenOffice 4/program/soffice.exe -headless -accept=\"socket,host=127.0.0.1,port=8100;urp;\"";
            String command = "C:/Program Files (x86)/OpenOffice 4/program/soffice.exe -headless -accept=\"socket,host=127.0.0.1,port=8100;urp;\"";
            Process p = Runtime.getRuntime().exec(command);

            // 连接openoffice服务
//            OpenOfficeConnection connection = new SocketOpenOfficeConnection(officeAddress, Integer.valueOf(portNumber));
            OpenOfficeConnection connection = new SocketOpenOfficeConnection("127.0.0.1", Integer.valueOf(portNumber));
            connection.connect();

            // 转换
            DocumentConverter converter = new OpenOfficeDocumentConverter(connection);
            converter.convert(inputFile, outputFile);

            // 关闭连接
            connection.disconnect();

            // 关闭进程
            p.destroy();
            return outputFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


//    public static void main(String args[]) {
//        String inputFile = "D://申请人：彭仕银   项目：_2018-12-01_09-30-06.docx";
//        String outputFile = "D://申请人：管理员   项目：_2018-12-11_10-44-31.pdf";
//        office2PDF(inputFile, outputFile);
//        System.out.println(officeAddress);
//        System.out.println(officeHome);
//        System.out.println(portNumber);
//		System.out.println("1         ");
//    }
}
