package org.ambient.control.rest;

import org.ambientlight.process.validation.ValidationResult;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;


public class VerifyProcessTask extends AsyncTask<Object, Void, ValidationResult> {

	private final String URL = "/process/processes/validation";

	@Override
	protected ValidationResult doInBackground(Object... params) {

		String url = URLUtils.getBaseUrl((String) params[0]) + URL;

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		RestTemplate restTemplate = new RestTemplate(true, requestFactory);

		restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
		ValidationResult result = restTemplate.postForObject(url, params[1], ValidationResult.class);
		return result;
	}
}