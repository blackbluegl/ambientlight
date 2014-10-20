package org.ambient.rest;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;


public class Rest {

    public static final String SERVER_NAME = "10.0.2.2:8080";
	//public static final String SERVER_NAME = "server:8080";

	public static String getUrl(String suffix) {
		return "http://" + SERVER_NAME + "/middleware/webapi" + suffix;
	}

	public static RestTemplate getRestTemplate() {
		// ObjectMapper mapper = new ObjectMapper();
		// mapper.registerModule(new SwitchableIdModule());

		// set a custom filter
		// Set<String> filterProperties = new HashSet<String>();
		// FilterProvider filters = new
		// SimpleFilterProvider().addFilter("apiFilter",
		// SimpleBeanPropertyFilter.serializeAllExcept(filterProperties));
		// mapper.setFilters(filters);

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		RestTemplate restTemplate = new RestTemplate(true, requestFactory);
		MappingJackson2HttpMessageConverter conv = new MappingJackson2HttpMessageConverter();
		// conv.setObjectMapper(mapper);
		restTemplate.getMessageConverters().add(conv);
		return restTemplate;
	}
}
