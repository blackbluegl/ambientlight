package org.ambientlight.device.stripe.configuration;

import java.util.ArrayList;
import java.util.List;

import org.ambientlight.scenery.entities.configuration.StripePartConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("stripe")
public class StripeConfiguration {

	public int port;
	public int pixelAmount;
	public List<StripePartConfiguration> stripeParts = new ArrayList<StripePartConfiguration>();
}
