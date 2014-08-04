package org.ambient.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;
import android.util.Log;


public class StartStopProcessTask extends AsyncTask<Object, Void, Void> {

	private static final String LOG = StartStopProcessTask.class.getName();

	private final String URL = "/process/{roomName}/{id}/state";


	@Override
	protected Void doInBackground(Object... params) {

		try {
			Map<String, String> vars = new HashMap<String, String>();
			vars.put("roomName", params[0].toString());
			vars.put("id", params[1].toString());

			RestTemplate restTemplate = Rest.getRestTemplate();

			restTemplate.put(Rest.getUrl(URL), params[2], vars);

		} catch (Exception e) {
			Log.e(LOG, e.getMessage());
		}

		return null;
	}
}