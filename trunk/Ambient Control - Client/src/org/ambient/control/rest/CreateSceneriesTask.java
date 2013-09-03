package org.ambient.control.rest;

import java.util.List;

import org.ambientlight.scenery.AbstractSceneryConfiguration;
import org.ambientlight.ws.container.SceneriesContainer;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;

public class CreateSceneriesTask extends AsyncTask<Object, Void, Void> {

	private final String URL = "/sceneryControl/config/room/sceneries";

	@Override
	protected Void doInBackground(Object... params) {

		String url = URLUtils.getBaseUrl((String) params[0]) + URL;

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		RestTemplate restTemplate = new RestTemplate(true, requestFactory);

		restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());

		SceneriesContainer sceneryContainer = new SceneriesContainer();
		sceneryContainer.sceneries = (List<AbstractSceneryConfiguration>) params[1];

		restTemplate.put(url, sceneryContainer);
		return null;
	}
}