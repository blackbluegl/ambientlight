package org.ambient.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;


public class StartStopProcessTask extends AsyncTask<Object, Void, Void> {

	private final String URL = "/process/{roomName}/{id}/state";



	@Override
	protected Void doInBackground(Object... params) {

		String url = Rest.getUrl(URL);
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("roomName", params[0].toString());
		vars.put("id", params[1].toString());

		RestTemplate restTemplate = Rest.getRestTemplate();

		restTemplate.put(url, params[2], vars);
		return null;
	}
}