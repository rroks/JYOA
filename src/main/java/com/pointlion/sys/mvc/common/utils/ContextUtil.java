package com.pointlion.sys.mvc.common.utils;

public class ContextUtil {
	private static String ctx="";
	public static void setCtx(String context){
		ctx = context;
	} 
	public static String getCtx(){
		return ctx;
	}
}
