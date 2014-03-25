package org.ambient.rest;

import java.util.Collections;
import java.util.Map;

import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;


public class SetCurrentSceneryTask extends AsyncTask<String, Void, Void> {

	private final String URL = "/sceneries/{roomName}/current";


	@Override
	protected Void doInBackground(String... params) {

		String url = Rest.getUrl(URL);
		Map<String, String> vars = Collections.singletonMap("roomName", params[0]);

		RestTemplate restTemplate = Rest.getRestTemplate();

		restTemplate.put(url, params[1], vars);
		return null;
	}
}