package org.ambientlight.device.drivers.dummyswitching;

import java.io.IOException;

import org.ambientlight.device.drivers.SwitchDeviceOverEthernetConfiguration;
import org.ambientlight.device.drivers.SwtichDeviceDriver;

public class DummySwitchingDeviceDriver implements SwtichDeviceDriver{

	public void setConfiguration(SwitchDeviceOverEthernetConfiguration configuration) {
	}

	public void writeData(String type, int housecode, int switchingUnit, boolean state) throws IOException {
		System.out.println("DummySwitchingDeviceDriver: switching housecode: " + housecode + ", switchunit: " + switchingUnit
				+ " to state: " + state);
	}


}
