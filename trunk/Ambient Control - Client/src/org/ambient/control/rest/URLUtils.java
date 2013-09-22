package org.ambient.control.rest;

public class URLUtils {

	public static final String[] ANDROID_ADT_SERVERS = new String[] { "192.168.1.29:9998", "192.168.1.29:9997",
	"192.168.1.29:9999" };


	// public static final String[] ANDROID_ADT_SERVERS = { "server:9997",
	// "server:9999", "server:9998" };


	// public static final String[] ANDROID_ADT_SERVERS = new String[] {
	// "10.0.2.2:9998", "10.0.2.2:9999" };
	// public static final String[] ANDROID_ADT_SERVERS= new
	// String[]{"rfmbridge:9998"};

	public static String getBaseUrl(String hostname) {
		return "http://" + hostname + "/rest";
	}
}
