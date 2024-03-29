package org.ambientlight.device.drivers.remoteswitches;

import java.io.IOException;

import org.ambientlight.config.device.drivers.RemoteSwitchBridgeConfiguration;
import org.ambientlight.device.drivers.RemoteSwtichDeviceDriver;

public class DummySwitchingDeviceDriver implements RemoteSwtichDeviceDriver{

	public void setConfiguration(RemoteSwitchBridgeConfiguration configuration) {
	}

	public void setState(String type, int housecode, int switchingUnit, boolean state) throws IOException {
		System.out.println("DummySwitchingDeviceDriver: switching housecode: " + housecode + ", switchunit: " + switchingUnit
				+ " to state: " + state);
	}


}
