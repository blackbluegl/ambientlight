package org.ambient.rest;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;


public class SetActorConductConfigurationTask extends AsyncTask<Object, Void, Void> {

	private final String URL = "/sceneryControl/config/room/items/";
	private final String URL1 = "/program";


	@Override
	protected Void doInBackground(Object... params) {

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		RestTemplate restTemplate = new RestTemplate(true, requestFactory);
		restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());

		String urlForUpdateOfConfig = URLUtils.getBaseUrl((String) params[0]) + URL + (String) params[1] + URL1;
		restTemplate.put(urlForUpdateOfConfig, params[2]);

		return null;
	}

}