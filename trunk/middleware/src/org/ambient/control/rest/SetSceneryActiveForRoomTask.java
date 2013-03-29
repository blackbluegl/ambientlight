package org.ambient.control.rest;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;

public class SetSceneryActiveForRoomTask extends AsyncTask<Object, Void, Void> {

	private final String URL = "/sceneryControl/control/room/sceneries/";

	@Override
	protected Void doInBackground(Object... params) {

		String url = URLUtils.getBaseUrl((String) params[0]) + URL;

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		RestTemplate restTemplate = new RestTemplate(true, requestFactory);
		
		restTemplate.getMessageConverters().add(
				new StringHttpMessageConverter());

	
//		restTemplate.postForEntity(url, params[1], Void.class);
		restTemplate.put(url,  params[1]);
		return null;
	}

}