package test;

import java.awt.Color;

import org.ambientlight.device.drivers.dummy.DummyDeviceDriver;
import org.ambientlight.device.stripe.Stripe;
import org.ambientlight.device.stripe.configuration.StripeConfiguration;

public class DummyDeviceDriverTest {

	public static void main(String[] args) throws InterruptedException {

		DummyDeviceDriver dummy = new DummyDeviceDriver();

		StripeConfiguration sc = new StripeConfiguration();
		sc.pixelAmount = 128;
		sc.port = 0;
		
		Stripe myStripe = new Stripe(sc);
		
		dummy.attachStripe(myStripe, 0);
		
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
