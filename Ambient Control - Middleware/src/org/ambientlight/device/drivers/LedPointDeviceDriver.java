package org.ambientlight.device.drivers;

import java.util.List;

import org.ambientlight.device.led.LedPoint;


public interface LedPointDeviceDriver extends AnimateableLedDevice {

	public void setConfiguration(RemoteHostConfiguration configuration);

	public List<LedPoint> getLedPoints();

	public void attachLedPoint(LedPoint ledPoint);




}
