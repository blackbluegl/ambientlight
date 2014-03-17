package org.ambient.rest;

import org.ambientlight.room.entities.features.actor.types.SwitchType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;


public class SetSwitchablePowerState extends AsyncTask<Object, Void, Void> {

	private final String URL = "/switchables";
	private final String URL2 = "/state";


	@Override
	protected Void doInBackground(Object... params) {

		SwitchType type = (SwitchType) params[1];
		String id = (String) params[2];
		Boolean state = (Boolean) params[3];

		String url = Rest.getBaseUrl((String) params[0]) + URL + "/" + type.toString() + "/" + id + URL2;

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		RestTemplate restTemplate = new RestTemplate(true, requestFactory);

		restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());

		restTemplate.put(url, state);
		return null;
	}
}