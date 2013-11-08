package test;

import java.awt.Color;
import java.io.IOException;
import java.net.UnknownHostException;

import org.ambientlight.device.drivers.MultiStripeOverEthernetClientDeviceConfiguration;
import org.ambientlight.device.drivers.multistripeoverethernet.MultistripeOverEthernetClientDeviceDriver;
import org.ambientlight.device.led.ColorConfiguration;
import org.ambientlight.device.led.Stripe;
import org.ambientlight.device.led.StripeConfiguration;


public class TestMultiStripeOEDevice {

	public static void main(String[] args) throws UnknownHostException, IOException {
		MultistripeOverEthernetClientDeviceDriver device = new MultistripeOverEthernetClientDeviceDriver();
		MultiStripeOverEthernetClientDeviceConfiguration config = new MultiStripeOverEthernetClientDeviceConfiguration();
		config.hostName = "ambi-schlafen";
		// config.hostName = "localhost";
		config.port = 30000;
		device.setConfiguration(config);

		float value = 1.0f;
		float gamma = 1.0f;
		ColorConfiguration cConfig = new ColorConfiguration();
		cConfig.gammaRed = gamma;
		cConfig.gammaGreen = gamma;
		cConfig.gammaBlue = gamma;
		cConfig.levelRed = value;
		cConfig.levelBlue = value;
		cConfig.levelGreen = value;

		StripeConfiguration sc = new StripeConfiguration();
		sc.colorConfiguration = cConfig;
		sc.pixelAmount = 162;
		sc.port = 0;
		sc.protocollType = StripeConfiguration.PROTOCOLL_TYPE_TM1812;

		Stripe myStripe = new Stripe(sc);
		device.attachStripe(myStripe);

		device.connect();
		for (int z = 0; z < 10000; z++) {
			for (int i = 0; i < 25500; i++) {

				int color = i / 100;

				Color c2 = new Color(color, color, color);
				boolean darker = false;
				for (int g = 0; g < sc.pixelAmount; g++) {

					Color c1 = new Color(color + 1, color + 1, color + 01);
					if (darker) {
						myStripe.setPixel(g, c1.getRGB());
						darker = false;
					} else {
						myStripe.setPixel(g, c2.getRGB());
						darker = true;
					}

				}
				device.writeData();
				System.out.println("sent data");
			}
			try {
				Thread.currentThread().sleep(39);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		device.closeConnection();

	}
}
