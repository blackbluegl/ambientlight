package org.ambient.control.rest;

public class URLUtils {

	//public static final String ANDROID_ADT_HOSTNAME="tv-arbeitszimmer";
	public static final String ANDROID_ADT_HOSTNAME="10.0.2.2";
	
	public static String getBaseUrl(String hostname){
		return "http://"+hostname+":9998/rest";
	}
}
