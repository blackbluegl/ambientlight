package org.ambient.control.rest;

import java.util.Set;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;

public class GetSceneriesTask  extends AsyncTask<String,Void, String[]>{

	private final String URL= "/sceneryControl/config/room/sceneries";
		@Override
		protected String[] doInBackground(String... params) {
			
			String url = URLUtils.getBaseUrl(params[0])+URL;
			
			// Create a new RestTemplate instance
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			RestTemplate restTemplate = new RestTemplate(true, requestFactory);

			// Add the String message converter
			restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
			// Make the HTTP GET request, marshaling the response to a String
			@SuppressWarnings("unchecked")
			Set<String> response = restTemplate.getForObject(url, Set.class, "SpringSource");
			
			String[] result = new String[response.size()];
			int i=0;
			for(String current : response){
				result[i] = current;
				i++;
			}
			return result;
		}
		
	}