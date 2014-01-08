package test;

import java.awt.Color;
import java.io.IOException;
import java.net.UnknownHostException;

import org.ambientlight.config.device.drivers.MultiStripeOverEthernetClientDeviceConfiguration;
import org.ambientlight.config.device.led.ColorConfiguration;
import org.ambientlight.config.device.led.StripeConfiguration;
import org.ambientlight.device.drivers.multistripeoverethernet.MultistripeOverEthernetClientDeviceDriver;
import org.ambientlight.device.led.Stripe;


public class TestMultiStripeOEDevice {

	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		MultistripeOverEthernetClientDeviceDriver device = new MultistripeOverEthernetClientDeviceDriver();
		MultiStripeOverEthernetClientDeviceConfiguration config = new MultiStripeOverEthernetClientDeviceConfiguration();
		config.hostName = "led-bridge-schlafen";
		// config.hostName = "localhost";
		config.port = 2002;
		device.setConfiguration(config);

		float value = 1.0f;
		float gamma = 2.20f;
		ColorConfiguration cConfig = new ColorConfiguration();
		cConfig.gammaRed = gamma;
		cConfig.gammaGreen = gamma;
		cConfig.gammaBlue = gamma;
		cConfig.levelRed = value;
		cConfig.levelBlue = value;
		cConfig.levelGreen = value;

		StripeConfiguration sc = new StripeConfiguration();
		sc.colorConfiguration = cConfig;
		sc.pixelAmount = 22;
		sc.port = 1;
		sc.protocollType = StripeConfiguration.PROTOCOLL_TYPE_DIRECT_SPI;

		Stripe myStripe = new Stripe(sc);
		device.attachStripe(myStripe);

		device.connect();

		while (true) {
			for (int i = 1; i < 256; i++) {
				int color = i;
				Color c2 = new Color((int) (color * 1.0f), (int) (color * 1.0f), (int) (color * 1.0f));

				for (int g = 0; g < sc.pixelAmount; g++) {
					myStripe.setPixel(g, c2.getRGB());
				}
				device.writeData();
				System.out.println(i);
				Thread.sleep(40);
			}

			// for (int i = 0; i < sc.pixelAmount; i++) {
			// Color black = Color.BLACK;
			// Color white = Color.WHITE;
			// for (int y = 0; y < sc.pixelAmount; y++) {
			// myStripe.setPixel(i, y == i ? white.getRGB() : black.getRGB());
			// }
			// device.writeData();
			// System.out.println(i);
			// Thread.sleep(40);
			// }
		}

		// device.closeConnection();
	}
}
