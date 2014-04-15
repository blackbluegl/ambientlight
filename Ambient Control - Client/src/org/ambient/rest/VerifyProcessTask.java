package org.ambient.rest;

import java.util.Collections;
import java.util.Map;

import org.ambientlight.ws.process.validation.ValidationResult;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;


public class VerifyProcessTask extends AsyncTask<Object, Void, ValidationResult> {

	private final String URL = "/process/{room}/validation";


	@Override
	protected ValidationResult doInBackground(Object... params) {

		String url = Rest.getUrl(URL);
		Map<String, String> vars = Collections.singletonMap("room", params[0].toString());
		RestTemplate restTemplate = Rest.getRestTemplate();

		ValidationResult result = restTemplate.postForObject(url, params[1], ValidationResult.class, vars);
		return result;
	}
}