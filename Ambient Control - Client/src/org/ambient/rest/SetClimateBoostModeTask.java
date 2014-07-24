package org.ambient.rest;

import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;


public class SetClimateBoostModeTask extends AsyncTask<Object, Void, Void> {

	private final String URL = "/climate/{roomName}/mode";


	@Override
	protected Void doInBackground(Object... params) {

		Map<String, String> vars = new HashMap<String, String>();
		vars.put("roomName", (String) params[0]);

		Rest.getRestTemplate().put(Rest.getUrl(URL), params[1], vars);

		return null;
	}

}