package org.ambientlight.device.stripe;

import java.util.ArrayList;
import java.util.List;


import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("stripe")
public class StripeConfiguration {

	public int port;
	public int pixelAmount;
	public List<StripePartConfiguration> stripeParts = new ArrayList<StripePartConfiguration>();
}
