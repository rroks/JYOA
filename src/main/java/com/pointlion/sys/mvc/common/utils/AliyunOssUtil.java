package com.pointlion.sys.mvc.common.utils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.GetObjectRequest;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;

public class AliyunOssUtil {
	
	public final static String ossEndpoint = PropKit.use("aliyunoss.properties").get("osseEndpoint");
	public final static String ossAccesskeyId = PropKit.use("aliyunoss.properties").get("ossAccessKeyId");
	public final static String ossAccessKeySecret= PropKit.use("aliyunoss.properties").get("ossAccessKeySecret");
	public final static String ossBucketName = PropKit.use("aliyunoss.properties").get("ossBucketName");
	public final static String ossFirstKey = PropKit.use("aliyunoss.properties").get("ossFirstKey");
	public final static String ossImagestyle = PropKit.use("aliyunoss.properties").get("ossImagestyle");
	public final static String ossImageWaterMark = PropKit.use("aliyunoss.properties").get("ossImageWaterMark");

	
	/**图片加水印
	 * @param img
	 * @param style
	 * @param objectName
	 * @author:linjq
	 * @param
	 * 
	 */
	public static void setWaterMark(String img, String style, String objectName){
		// 创建OSSClient实例。
		OSSClient ossClient = new OSSClient(ossEndpoint, ossAccesskeyId, ossAccessKeySecret);
		GetObjectRequest request = new GetObjectRequest(ossBucketName, objectName);
		request.setProcess(style);
		ossClient.getObject(request, new File(objectName));
		ossClient.shutdown();
	}
	
	public static String genUrl(String img, String bucketName, String objectName){
		OSSClient ossClient = new OSSClient(ossEndpoint, ossAccesskeyId, ossAccessKeySecret);
		GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucketName, objectName, HttpMethod.GET);
		URL signedUrl = ossClient.generatePresignedUrl(req);
		System.out.println(signedUrl);
		// 关闭OSSClient。
		ossClient.shutdown();
		return signedUrl.toString();
	}
	
	/**
	 * 上传文件
	 * @author:linjq
	 * @param
	 * 
	 */
	public void uplodaImg(String objectName, InputStream buf){
		OSSClient ossClient = new OSSClient(ossEndpoint, ossAccesskeyId, ossAccessKeySecret);
		ossClient.putObject(ossBucketName, objectName, buf);
		ossClient.shutdown();
	}
	
	/**
	 * 删除图片
	 * @author:linjq
	 * @param
	 * 
	 */
	public void delImg(String objectName){
		OSSClient ossClient = new OSSClient(ossEndpoint, ossAccesskeyId, ossAccessKeySecret);
		// 删除文件。
		ossClient.deleteObject(ossBucketName, objectName);
		// 关闭OSSClient。
		ossClient.shutdown();
	}
	
	public String getPicURL(String uri){
		if(!StrKit.isBlank(uri)){
			return "https://" + ossBucketName + "." +ossEndpoint.split("//")[1]  + "/" +uri;
		}else{
			return "";
		}
	}
	
	/**下载文件到本地
	 * @param objectName
	 * @param path
	 */
	public void downloadFile(String objectName, String path){
		OSSClient ossClient = new OSSClient(ossEndpoint, ossAccesskeyId, ossAccessKeySecret);
		ossClient.getObject(new GetObjectRequest(ossBucketName, objectName), new File(path));
	}
	
	public static void main(String[] args){
		System.out.println(ossEndpoint);
		System.out.println(ossAccesskeyId);
		System.out.println(ossAccessKeySecret);
		System.out.println(ossBucketName);
	}
}
