package org.ambient.rest;

import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;


public class SetRenderingConfigurationTask extends AsyncTask<Object, Void, Void> {

	private final String URL = "/features/{roomName}/renderables/{id}/config";


	@Override
	protected Void doInBackground(Object... params) {

		Map<String, String> vars = new HashMap<String, String>();
		vars.put("roomName", (String) params[0]);
		vars.put("id", params[1].toString());

		Rest.getRestTemplate().put(Rest.getUrl(URL), params[2], vars);

		return null;
	}

}