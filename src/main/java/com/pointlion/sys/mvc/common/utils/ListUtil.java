package com.pointlion.sys.mvc.common.utils;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import com.jfinal.plugin.activerecord.Record;

public class ListUtil{
	/***
	 * list去重复
	 * @param list
	 * @return
	 */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static List removeDuplicate(List list) {   
	    HashSet h = new HashSet(list);   
	    list.clear();   
	    list.addAll(h);   
	    return list;   
    }


}
