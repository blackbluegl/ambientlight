package org.ambient.rest;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;


public class RegisterCallbackTask extends AsyncTask<Object, Void, Boolean> {

	private final String URL = "/callback/client";


	@Override
	protected Boolean doInBackground(Object... params) {

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		RestTemplate restTemplate = new RestTemplate(true, requestFactory);
		restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());

		String eventSenderURL = URLUtils.getBaseUrl((String) params[0]) + URL;
		try {
			restTemplate.put(eventSenderURL, params[1]);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}