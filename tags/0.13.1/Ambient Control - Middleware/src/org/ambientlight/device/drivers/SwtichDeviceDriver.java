package org.ambientlight.device.drivers;

import java.io.IOException;

public interface SwtichDeviceDriver extends DeviceDriver {
	
	public void setConfiguration(SwitchDeviceOverEthernetConfiguration configuration);
	public void writeData(String type, int housecode,int switchingUnit,boolean state) throws IOException;
}
