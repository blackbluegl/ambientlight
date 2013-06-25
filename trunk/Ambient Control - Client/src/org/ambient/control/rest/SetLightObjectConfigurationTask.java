package org.ambient.control.rest;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;

public class SetLightObjectConfigurationTask extends AsyncTask<Object, Void, Void> {
	private final String URL = "/sceneryControl/config/room/sceneries/";
	private final String URL1 = "/items/";
	private final String URL2 = "/program";

	private final String URL_SWITCH = "/sceneryControl/control/room/sceneries/";
	private final String URL_SWITCH1 = "/items";

	@Override
	protected Void doInBackground(Object... params) {

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		RestTemplate restTemplate = new RestTemplate(true, requestFactory);
		restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());

		// updating the new config
		String urlForUpdateOfConfig = URLUtils.getBaseUrl((String) params[0]) 
				+ URL + (String) params[1] 
				+ URL1+ (String) params[2] 
				+ URL2;
		restTemplate.put(urlForUpdateOfConfig, params[3]);

		// switching real item to newConfig
		String urlForSwitch = URLUtils.getBaseUrl((String) params[0])
				+ URL_SWITCH + (String) params[1] 
				+ URL_SWITCH1;
		restTemplate.put(urlForSwitch, params[2]);

		return null;
	}

}