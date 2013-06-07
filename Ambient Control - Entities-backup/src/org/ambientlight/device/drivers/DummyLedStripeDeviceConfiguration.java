package org.ambientlight.device.drivers;

import java.util.ArrayList;
import java.util.List;

import org.ambientlight.device.stripe.StripeConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("dummyLedStyleDeviceConfiguration")
public class DummyLedStripeDeviceConfiguration extends DeviceConfiguration {
	public List<StripeConfiguration> configuredStripes = new ArrayList<StripeConfiguration>();
}
