package org.ambient.control.rest;

public class URLUtils {

	// public static final String[] ANDROID_ADT_SERVERS = new String[] {
	// "192.168.1.32:9998", "192.168.1.32:9999" };


	public static final String[] ANDROID_ADT_SERVERS = new String[] { "192.168.1.32:9999" };
	// String[]{"rfmbridge:9999","rfmbridge:9998"};
	// public static// String[]{"rfmbridge:9999","rfmbridge:9998"}; final
	// String[] ANDROID_ADT_SERVERS = new String[] { "10.0.2.2:9999" };
	//	public static final String[] ANDROID_ADT_SERVERS= new String[]{"rfmbridge:9998"};

	public static String getBaseUrl(String hostname){
		return "http://"+hostname+"/rest";
	}
}
