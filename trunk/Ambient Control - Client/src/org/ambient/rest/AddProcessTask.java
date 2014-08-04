package org.ambient.rest;

import java.util.Collections;
import java.util.Map;

import org.ambientlight.ws.process.validation.ValidationResult;

import android.os.AsyncTask;
import android.util.Log;


public class AddProcessTask extends AsyncTask<Object, Void, ValidationResult> {

	private static final String LOG = AddProcessTask.class.getName();

	private final String URL = "/process/{roomName}";


	@Override
	protected ValidationResult doInBackground(Object... params) {

		try {
			Map<String, String> vars = Collections.singletonMap("roomName", (String) params[0]);

			return Rest.getRestTemplate().postForObject(Rest.getUrl(URL), params[1], ValidationResult.class, vars);
		} catch (Exception e) {
			Log.e(LOG, e.getMessage());
			return null;
		}

	}
}