package org.ambientlight.device.drivers;

import java.io.IOException;

public interface RemoteSwtichDeviceDriver extends DeviceDriver {

	public void setState(String type, int housecode,int switchingUnit,boolean state) throws IOException;
}
