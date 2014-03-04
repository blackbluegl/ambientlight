package test;

import java.io.IOException;
import java.net.UnknownHostException;

import org.ambientlight.config.device.drivers.RemoteSwitchBridgeConfiguration;
import org.ambientlight.device.drivers.remoteswitches.SwitchDeviceOverEthernetDriver;

public class TestRFMOEDevice {

	public static void main(String[] args) throws UnknownHostException, IOException{
		SwitchDeviceOverEthernetDriver device = new SwitchDeviceOverEthernetDriver();
		RemoteSwitchBridgeConfiguration config = new RemoteSwitchBridgeConfiguration();
		config.hostName="127.0.0.1";
		config.port=2003;
		device.setConfiguration(config);
		
			device.setState("ELRO",15,1,true);
	}
}
