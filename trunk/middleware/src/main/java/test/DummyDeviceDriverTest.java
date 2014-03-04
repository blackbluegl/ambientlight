package test;

import java.awt.Color;

import org.ambientlight.config.device.led.StripeConfiguration;
import org.ambientlight.device.drivers.ledstripes.DummyLedStripeDeviceDriver;
import org.ambientlight.device.led.Stripe;

public class DummyDeviceDriverTest {

	public static void main(String[] args) throws InterruptedException {

		DummyLedStripeDeviceDriver dummy = new DummyLedStripeDeviceDriver();

		StripeConfiguration sc = new StripeConfiguration();
		sc.pixelAmount = 128;
		sc.port = 0;
		
		Stripe myStripe = new Stripe(sc);
		
		dummy.attachStripe(myStripe);
		
		dummy.connect();
		while(true){
			double random = Math.random();
			int colorValue=(int) (255*random);
			Color color = new Color(colorValue,colorValue,colorValue);
			myStripe.setPixel(0, color.getRGB());
			myStripe.setPixel(1, color.getRGB());
			myStripe.setPixel(2, color.getRGB());
			Color white = new Color(255,255,255);
			myStripe.setPixel(3, white.getRGB());

			dummy.writeData();
			Thread.sleep(333,0);
		}
	}
}
