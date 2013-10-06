package org.ambient.rest;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;


public class DeleteProcessTask extends AsyncTask<String, Void, Void> {

	private final String URL = "/process/processes/";

	@Override
	protected Void doInBackground(String... params) {

		String url = URLUtils.getBaseUrl(params[0]) + URL + params[1];

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		RestTemplate restTemplate = new RestTemplate(true, requestFactory);

		restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
		restTemplate.delete(url);
		return null;
	}
}