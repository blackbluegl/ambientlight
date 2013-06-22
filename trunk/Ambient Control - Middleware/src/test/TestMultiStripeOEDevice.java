package test;

import java.awt.Color;
import java.io.IOException;
import java.net.UnknownHostException;

import org.ambientlight.device.drivers.MultiStripeOverEthernetClientDeviceConfiguration;
import org.ambientlight.device.drivers.multistripeoverethernet.MultistripeOverEthernetClientDeviceDriver;
import org.ambientlight.device.stripe.Stripe;
import org.ambientlight.device.stripe.StripeConfiguration;


public class TestMultiStripeOEDevice {

	public static void main(String[] args) throws UnknownHostException, IOException {
		MultistripeOverEthernetClientDeviceDriver device = new MultistripeOverEthernetClientDeviceDriver();
		MultiStripeOverEthernetClientDeviceConfiguration config = new MultiStripeOverEthernetClientDeviceConfiguration();
		config.hostName = "192.168.1.44";
		//config.hostName = "localhost";
		config.port = 2002;
		device.setConfiguration(config);

		StripeConfiguration sc = new StripeConfiguration();
		sc.pixelAmount = 64;
		sc.port = 0;
		sc.protocollType = StripeConfiguration.PROTOCOLL_TYPE_DIRECT_SPI;

		Stripe myStripe = new Stripe(sc);
		device.attachStripe(myStripe);

		device.connect();

		for(int z=0;z<10000;z++){
			for (int i = 0; i < 25500; i++) {
				Color c2 = new Color(i/100, 100, i/100);
				for(int g=0;g<sc.pixelAmount;g++){
				myStripe.setPixel(g, c2.getRGB());
				}
				device.writeData();
				System.out.println("sent data");
			}
			try {
				Thread.currentThread().sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		device.closeConnection();

	}
}
