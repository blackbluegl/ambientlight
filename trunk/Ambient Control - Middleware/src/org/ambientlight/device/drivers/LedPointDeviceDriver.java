package org.ambientlight.device.drivers;

import java.io.IOException;
import java.net.UnknownHostException;

import org.ambientlight.device.led.LedPoint;


public interface LedPointDeviceDriver extends DeviceDriver{

	public void setConfiguration(RemoteHostConfiguration configuration);

	public LedPoint getLedPoint();

	public void setLedPoint(LedPoint ledPoint);

	public void connect() throws UnknownHostException, IOException;

	public void closeConnection();

	void writeData() throws IOException;
}
