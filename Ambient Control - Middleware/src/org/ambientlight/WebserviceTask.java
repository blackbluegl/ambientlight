package org.ambientlight;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class WebserviceTask extends Thread {

	static final String BASE_URI = "http://"+AmbientControlMW.bindingAdressAndPort+"/rest";

	@Override
	public void run() {

		// start Webservice
		try {
			final ResourceConfig rc = new PackagesResourceConfig("org.ambientlight.webservice");
			rc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
			rc.getFeatures().put("com.sun.jersey.config.feature.Trace", true);

			HttpServer server = GrizzlyServerFactory.createHttpServer(BASE_URI, rc);

			ThreadPoolConfig config = ThreadPoolConfig.defaultConfig().setPoolName("mypool").setCorePoolSize(1)
					.setMaxPoolSize(20);

			NetworkListener listener = server.getListeners().iterator().next();
			listener.getTransport().setWorkerThreadPoolConfig(config);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
