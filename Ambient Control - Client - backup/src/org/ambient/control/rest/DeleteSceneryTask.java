package org.ambient.control.rest;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;

public class DeleteSceneryTask  extends AsyncTask<String,Void, Void>{

	private final String URL= "/sceneryControl/config/room/sceneries";
		@Override
		protected Void doInBackground(String... params) {
			
			String url = URLUtils.getBaseUrl(params[0])+URL;
			
			// Create a new RestTemplate instance
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			RestTemplate restTemplate = new RestTemplate(true, requestFactory);

			// Add the String message converter
			restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
			// Make the HTTP GET request, marshaling the response to a String
			restTemplate.delete(url+"/"+params[1]);
			return null;
		}
	}