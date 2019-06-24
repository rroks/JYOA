package com.pointlion.sys.mvc.admin.oa.common;

import java.lang.reflect.Field;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.pointlion.sys.mvc.common.model.SysDct;

public class WorkFlowUtil {
	/***
	 * 根据流程定义获取流程名称
	 * 模版引擎中也用到了，修改时候注意！！！！！！！
	 * @param defKey
	 */
	public static String getDefNameByDefKey(String defKey){
		Record r= Db.findFirst("select d.* from act_re_procdef d,act_re_deployment p where d.DEPLOYMENT_ID_=p.ID_ AND d.KEY_='"+defKey+"' ORDER BY p.DEPLOY_TIME_ DESC");
		if(r==null){
			return "";
		}else{
			return r.getStr("NAME_");
		}
	}
	
	
	/**根据流程defKey获取流程名称
	 * @param key
	 * @return
	 */
	public static String getDefNameByKey(String key){
		SysDct dct = SysDct.dao.getByKeyAndType(key, "FLOW_DEFKEY_TO_MODELNAME");
		if(dct != null){
			return dct.getName();
		}else{
			return "未找到对应流程";
		}
	}
	
	/***
	 * 根据流程定义获取对应的classname
	 * @param defkey
	 * @return
	 */
	public static String getClassFullNameByDefKey(String defkey){
		String result = "";
		SysDct dct = SysDct.dao.getByKeyAndType(defkey, "FLOW_DEFKEY_TO_MODELNAME");
		if(dct!=null){
			result = "com.pointlion.sys.mvc.common.model."+dct.getValue();
		}else{
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!根据流程定义key没有找到对应的model!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}
		return result;
	}
	
	/***
     * 表名转类名
     * @param tableName
     * @return
     */
    public static String tableNameToClassName(String tableName){
    	String arr[] = tableName.split("_");
    	String result = "";
    	for(String s:arr){
    		result = result+s.substring(0, 1).toUpperCase()+s.substring(1).toLowerCase();
    	}
    	return result;
    }
	
	public static String getDefKeyByTablename(String tablename){
		String result = "";
		String classname = tableNameToClassName(tablename);
		SysDct dct = SysDct.dao.getByValueAndType(classname, "FLOW_DEFKEY_TO_MODELNAME");
		if(dct!=null){
			result = dct.getKey()+"###"+dct.getName()+"###"+dct.getName();
		}else{
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!根据流程定义key没有找到对应的字典类型!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}
		return result;
	}
	
	/***
	 * 根据defkey获取表名
	 * @throws ClassNotFoundException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static String getTablenameByDefkey(String defKey){
		String result="";
		try{
			String classname = getClassFullNameByDefKey(defKey);
			if(StrKit.notBlank(classname)){
				Class<?> clazz = Class.forName(classname);
				Field f = clazz.getField("tableName");
				if(f!=null){
					Object o = f.get(clazz);
					if(o!=null){
						result = o.toString();
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		if("".equals(result)){
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!根据流程定义key没有找到对应的table!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}
		return result;
	}
	
	/***
	 * 判断是否有该固定流程
	 * @param key
	 * @return
	 */
	public static Boolean ifHaveThisFlow(String key){
		Record r= Db.findFirst("select * from act_re_model r where r.KEY_='"+key+"'");
		if(r!=null){
			return true;
		}else{
			return false;
		}
	}
}
