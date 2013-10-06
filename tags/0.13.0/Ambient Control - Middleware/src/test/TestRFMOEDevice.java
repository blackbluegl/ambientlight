package test;

import java.io.IOException;
import java.net.UnknownHostException;

import org.ambientlight.device.drivers.SwitchDeviceOverEthernetConfiguration;
import org.ambientlight.device.drivers.switchoverethernet.SwitchDeviceOverEthernetDriver;

public class TestRFMOEDevice {

	public static void main(String[] args) throws UnknownHostException, IOException{
		SwitchDeviceOverEthernetDriver device = new SwitchDeviceOverEthernetDriver();
		SwitchDeviceOverEthernetConfiguration config = new SwitchDeviceOverEthernetConfiguration();
		config.hostName="127.0.0.1";
		config.port=2003;
		device.setConfiguration(config);
		
			device.writeData("ELRO",15,1,true);
	}
}
