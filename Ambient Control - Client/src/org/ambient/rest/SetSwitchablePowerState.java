package org.ambient.rest;

import java.util.HashMap;
import java.util.Map;

import org.ambientlight.room.entities.features.EntityId;

import android.os.AsyncTask;


public class SetSwitchablePowerState extends AsyncTask<Object, Void, Void> {

	private final String URL = "/features/{roomName}/switchables/{domain}/{id}/state";


	@Override
	protected Void doInBackground(Object... params) {

		Map<String, String> vars = new HashMap<String, String>();
		vars.put("roomName", (String) params[0]);
		vars.put("domain", ((EntityId) params[1]).domain);
		vars.put("id", ((EntityId) params[1]).id);

		Rest.getRestTemplate().put(Rest.getUrl(URL), params[2], vars);

		return null;
	}
}