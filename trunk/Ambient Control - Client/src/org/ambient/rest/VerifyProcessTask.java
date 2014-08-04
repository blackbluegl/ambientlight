package org.ambient.rest;

import java.util.Collections;
import java.util.Map;

import org.ambientlight.ws.process.validation.ValidationResult;

import android.os.AsyncTask;
import android.util.Log;


public class VerifyProcessTask extends AsyncTask<Object, Void, ValidationResult> {

	private static final String LOG = VerifyProcessTask.class.getName();

	private final String URL = "/process/{room}/validation";


	@Override
	protected ValidationResult doInBackground(Object... params) {

		try {
			Map<String, String> vars = Collections.singletonMap("room", params[0].toString());

			ValidationResult result = Rest.getRestTemplate().postForObject(Rest.getUrl(URL), params[1], ValidationResult.class,
					vars);

			return result;

		} catch (Exception e) {
			Log.e(LOG, e.getMessage());
			return null;
		}

	}
}