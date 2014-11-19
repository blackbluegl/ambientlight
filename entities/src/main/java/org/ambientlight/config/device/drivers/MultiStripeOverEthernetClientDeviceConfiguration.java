package org.ambientlight.config.device.drivers;

import java.util.ArrayList;
import java.util.List;

import org.ambientlight.config.device.led.StripeConfiguration;


public class MultiStripeOverEthernetClientDeviceConfiguration extends
RemoteHostConfiguration {

	private static final long serialVersionUID = 1L;

	public List<StripeConfiguration> configuredStripes = new ArrayList<StripeConfiguration>();

}
