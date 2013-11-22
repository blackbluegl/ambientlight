package org.ambientlight.config.device.drivers;

import java.util.ArrayList;
import java.util.List;

import org.ambientlight.config.device.led.StripeConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("dummyLedStyleDeviceConfiguration")
public class DummyLedStripeDeviceConfiguration extends DeviceConfiguration {
	public List<StripeConfiguration> configuredStripes = new ArrayList<StripeConfiguration>();
}
