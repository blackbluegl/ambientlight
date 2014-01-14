package org.ambientlight.device.drivers;

import java.io.IOException;

import org.ambientlight.config.device.drivers.SwitchDeviceOverEthernetConfiguration;

public interface RemoteSwtichDeviceDriver extends DeviceDriver {
	
	public void setConfiguration(SwitchDeviceOverEthernetConfiguration configuration);
	public void setState(String type, int housecode,int switchingUnit,boolean state) throws IOException;
}
