package org.ambientlight.device.drivers;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import org.ambientlight.device.led.Stripe;


public interface LedStripeDeviceDriver extends DeviceDriver{

	public void setConfiguration(RemoteHostConfiguration configuration);

	public List<Stripe> getAllStripes();

	public void attachStripe(Stripe stripe);

	public void connect() throws UnknownHostException, IOException;

	public void closeConnection();

	void writeData() throws IOException;
}
