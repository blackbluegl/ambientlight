package org.ambient.rest;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;


public class SetCurrentSceneryTask extends AsyncTask<Object, Void, Void> {

	private final String URL = "/sceneries/current";




	@Override
	protected Void doInBackground(Object... params) {

		String url = URLUtils.getBaseUrl((String) params[0]) + URL;

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		RestTemplate restTemplate = new RestTemplate(true, requestFactory);

		restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

		restTemplate.put(url, params[1]);
		return null;
	}


}