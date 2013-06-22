package org.ambientlight;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class WebserviceTask extends Thread {

	//static final String BASE_URI = "http://192.168.1.36:9998/rest";
	static final String BASE_URI = "http://"+AmbientControlMW.bindingAdressAndPort+"/rest";

	@Override
	public void run() {

		// Start Webservice
		try {
			final ResourceConfig rc = new PackagesResourceConfig(
"org.ambientlight.webservice");
			rc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
			rc.getFeatures().put("com.sun.jersey.config.feature.Trace", true);

			// HttpServer server = HttpServerFactory.create(BASE_URI, rc);

			GrizzlyServerFactory.createHttpServer(BASE_URI,rc);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
