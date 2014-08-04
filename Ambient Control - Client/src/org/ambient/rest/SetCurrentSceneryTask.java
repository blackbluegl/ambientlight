package org.ambient.rest;

import java.util.Collections;
import java.util.Map;

import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;
import android.util.Log;


public class SetCurrentSceneryTask extends AsyncTask<String, Void, Void> {

	private static final String LOG = SetCurrentSceneryTask.class.getName();

	private final String URL = "/sceneries/{roomName}/current";


	@Override
	protected Void doInBackground(String... params) {

		try {
			String url = Rest.getUrl(URL);
			Map<String, String> vars = Collections.singletonMap("roomName", params[0]);

			RestTemplate restTemplate = Rest.getRestTemplate();

			restTemplate.put(url, params[1], vars);
		} catch (Exception e) {
			Log.e(LOG, e.getMessage());
		}

		return null;
	}
}