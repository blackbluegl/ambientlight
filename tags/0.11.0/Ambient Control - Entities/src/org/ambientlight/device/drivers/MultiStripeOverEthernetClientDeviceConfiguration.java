package org.ambientlight.device.drivers;

import java.util.ArrayList;
import java.util.List;

import org.ambientlight.device.led.StripeConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("multiStripeOverEthernetClientDeviceConfiguration")
public class MultiStripeOverEthernetClientDeviceConfiguration extends
		RemoteHostConfiguration {
	public List<StripeConfiguration> configuredStripes = new ArrayList<StripeConfiguration>();

}
