package org.ambient.rest;

import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;


public class SetSwitchablePowerState extends AsyncTask<Object, Void, Void> {

	private final String URL = "/features/{roomName}/switchables/{type}/{id}/state";


	@Override
	protected Void doInBackground(Object... params) {

		Map<String, String> vars = new HashMap<String, String>();
		vars.put("roomName", (String) params[0]);
		vars.put("type", params[1].toString());
		vars.put("id", (String) params[2]);

		Rest.getRestTemplate().put(Rest.getUrl(URL), params[3], vars);

		return null;
	}
}