package org.ambientlight.device.drivers;

import java.io.IOException;

import org.ambientlight.config.device.drivers.SwitchDeviceOverEthernetConfiguration;

public interface SwtichDeviceDriver extends DeviceDriver {
	
	public void setConfiguration(SwitchDeviceOverEthernetConfiguration configuration);
	public void writeData(String type, int housecode,int switchingUnit,boolean state) throws IOException;
}
