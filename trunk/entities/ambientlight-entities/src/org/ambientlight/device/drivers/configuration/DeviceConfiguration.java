package org.ambientlight.device.drivers.configuration;

import java.util.ArrayList;
import java.util.List;

import org.ambientlight.device.stripe.configuration.StripeConfiguration;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("device")

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class DeviceConfiguration {
	public List<StripeConfiguration> configuredStripes = new ArrayList<StripeConfiguration>();
	public String driverName;
}
