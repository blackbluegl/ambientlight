package org.ambientlight.device.drivers;

import java.util.List;

import org.ambientlight.config.device.drivers.RemoteHostConfiguration;
import org.ambientlight.device.led.Stripe;


public interface LedStripeDeviceDriver extends AnimateableLedDevice {

	public void setConfiguration(RemoteHostConfiguration configuration);


	public List<Stripe> getAllStripes();


	public void attachStripe(Stripe stripe);

}
