package org.ambientlight.device.drivers.remoteswitches;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import org.ambientlight.config.device.drivers.SwitchDeviceOverEthernetConfiguration;
import org.ambientlight.device.drivers.RemoteSwtichDeviceDriver;

public class SwitchDeviceOverEthernetDriver implements RemoteSwtichDeviceDriver{
			 
	SwitchDeviceOverEthernetConfiguration configuration;
	

	
	public void setState(String type, int housecode,int switchingUnit,boolean state) throws IOException {
		Socket controlSocket = new Socket(this.configuration.hostName,
				this.configuration.port);

		PrintStream os = new PrintStream(controlSocket.getOutputStream());

		BufferedReader in = new BufferedReader(new InputStreamReader(
				controlSocket.getInputStream()));

			os.println("switch=" + type+"|"+housecode+"|"+switchingUnit+"|"+(state == true? 1:0));

			in = new BufferedReader(new InputStreamReader(
					controlSocket.getInputStream()));

			String stripePortResult = in.readLine();
			if (!"OK".equals(stripePortResult)) {
				throw new IOException("server did not correclty respond!");
			}
		in.close();
		controlSocket.close();
	}

	
	public void setConfiguration(SwitchDeviceOverEthernetConfiguration configuration){
		this.configuration =  configuration;
	}
}
