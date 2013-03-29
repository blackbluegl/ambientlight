package org.ambientlight.device.drivers.configuration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("multiStripeOverEthernetClientDeviceConfiguration")
public class MultiStripeOverEthernetClientDeviceConfiguration extends
		DeviceConfiguration {

	public int port;
	public String hostName;
}
