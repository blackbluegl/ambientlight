package test;

import java.awt.Color;
import java.io.IOException;
import java.net.UnknownHostException;

import org.ambientlight.device.drivers.configuration.MultiStripeOverEthernetClientDeviceConfiguration;
import org.ambientlight.device.drivers.multistripeoverethernet.MultistripeOverEthernetClientDeviceDriver;
import org.ambientlight.device.stripe.Stripe;
import org.ambientlight.device.stripe.configuration.StripeConfiguration;

public class TestMultiStripeOEDevice {

	public static void main(String[] args) throws UnknownHostException, IOException{
		MultistripeOverEthernetClientDeviceDriver device = new MultistripeOverEthernetClientDeviceDriver();
		MultiStripeOverEthernetClientDeviceConfiguration config = new MultiStripeOverEthernetClientDeviceConfiguration();
		config.hostName="127.0.0.1";
		config.port=2002;
		device.setConfiguration(config);
		
		
		StripeConfiguration sc = new StripeConfiguration();
		sc.pixelAmount = 3;
		sc.port = 0;
		
		Stripe myStripe = new Stripe(sc);
		
		for(int i=0;i<sc.pixelAmount;i++){
			Color c = new Color(i,0,0);
			myStripe.setPixel(i, c.getRGB());
		}
		
		device.attachStripe(myStripe, 0);
		
		device.connect();

		for (int i =0;i<256;i++){
			Color c = new Color(myStripe.getOutputResult().get(0));
			Color c2 = new Color(c.getRed(),i,c.getBlue());
			myStripe.setPixel(0, c2.getRGB());
			device.writeData();
		}
		device.closeConnection();
	}
}
