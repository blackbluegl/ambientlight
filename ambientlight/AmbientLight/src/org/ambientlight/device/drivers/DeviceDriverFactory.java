package org.ambientlight.device.drivers;

import org.ambientlight.device.drivers.configuration.DeviceConfiguration;
import org.ambientlight.device.drivers.dummy.DummyDeviceDriver;
import org.ambientlight.device.drivers.multistripeoverethernet.MultistripeOverEthernetClientDeviceDriver;

public class DeviceDriverFactory {

	public DeviceDriver createByName(String name, DeviceConfiguration dc){
		
		if(DummyDeviceDriver.class.getSimpleName().equals(name)){
			System.out.println("init simple device");
			return new DummyDeviceDriver();
		}
		
		if(MultistripeOverEthernetClientDeviceDriver.class.getSimpleName().equals(name)){
			System.out.println("init MultistripeOverEthernetClient device");
			DeviceDriver device = new MultistripeOverEthernetClientDeviceDriver();
			device.setConfiguration(dc);
			
			return  device;
		}
		
		return null;
	}
}
