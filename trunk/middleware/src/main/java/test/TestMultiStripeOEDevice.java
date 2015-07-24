package test;

import java.awt.Color;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ambientlight.config.device.drivers.MultiStripeOverEthernetClientDeviceConfiguration;
import org.ambientlight.config.device.led.ColorConfiguration;
import org.ambientlight.config.device.led.StripeConfiguration;
import org.ambientlight.device.drivers.ledstripes.MultistripeOverEthernetClientDeviceDriver;
import org.ambientlight.device.led.Stripe;


public class TestMultiStripeOEDevice {

	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		MultistripeOverEthernetClientDeviceDriver device = new MultistripeOverEthernetClientDeviceDriver();
		MultiStripeOverEthernetClientDeviceConfiguration config = new MultiStripeOverEthernetClientDeviceConfiguration();
		// config.hostName = "192.168.1.44";
		config.hostName = "led-bridge-wohnen";
		config.port = 2002;
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
		sc.pixelAmount = 6;
		sc.port = 1;
		sc.protocollType = StripeConfiguration.PROTOCOLL_TYPE_DIRECT_SPI;

		Stripe myStripe = new Stripe(sc);
		device.attachStripe(myStripe);

		device.connect();

		// init colors
		List<Color> oldColors = new ArrayList<Color>();
		List<Color> newColors = new ArrayList<Color>();

		for (int i = 0; i < 6; i++) {
			oldColors.add(Color.WHITE);
			newColors.add(getNewColor(Color.WHITE));
		}

		while (true) {
			for (int position = 0; position < 256; position++) {
				for (int i = 0; i < 6; i++) {
					myStripe.setPixel(i, getTransitColor(oldColors.get(i), newColors.get(i), position).getRGB());
				}

				device.writeData();
				Thread.sleep(40);
			}
			oldColors = new ArrayList<Color>(newColors);
			for (int i = 0; i < 6; i++) {
				newColors.set(i, getNewColor(oldColors.get(i)));
			}
		}
	}


	// device.closeConnection();
	// }

	public static Color getNewColor(Color oldColor) {
		int[] values = new int[3];
		Random rand = new Random();
		int position = rand.nextInt(3);
		values[0] = oldColor.getRed();
		values[1] = oldColor.getGreen();
		values[2] = oldColor.getBlue();

		Random randomValue = new Random();
		int randomInt = randomValue.nextInt(256);
		int med = values[0] + values[1] + values[2];
		med = med / 3;

		while (Math.abs(randomInt - med) < 40) {
			randomInt = randomValue.nextInt(256);
		}
		values[position] = randomInt;
		return new Color(values[0], values[1], values[2]);
	}


	public static Color getTransitColor(Color old, Color newColor, int pos) {
		int r = getTransitValue(old.getRed(), newColor.getRed(), pos);
		int g = getTransitValue(old.getGreen(), newColor.getGreen(), pos);
		int b = getTransitValue(old.getBlue(), newColor.getBlue(), pos);
		return new Color(r, g, b);
	}


	public static int getTransitValue(long start, long end, long position) {
		int result = (int) ((start * (255L - position) + end * position) / 255L);
		return result;
	}
}
