package org.ambient.control.rest;

public class URLUtils {
	
//	public static final String[] ANDROID_ADT_SERVERS= new String[]{"10.0.2.2:9998","10.0.2.2:9999"};
	public static final String[] ANDROID_ADT_SERVERS= new String[]{"rfmbridge:9998","rfmbridge:9999"};
	
	public static String getBaseUrl(String hostname){
		return "http://"+hostname+"/rest";
	}
}
