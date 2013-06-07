package org.ambientlight.device.stripe;

import java.util.ArrayList;
import java.util.List;


import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("stripe")
public class StripeConfiguration {

	public static String PROTOCOLL_TYPE_TM1812 = "tm1812";
	public static String PROTOCOLL_TYPE_DIRECT_SPI  = "directSpi";
	
	public int port;
	public int pixelAmount;
	public String protocollType;
	public List<StripePartConfiguration> stripeParts = new ArrayList<StripePartConfiguration>();
}
