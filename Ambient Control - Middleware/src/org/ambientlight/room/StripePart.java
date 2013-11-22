package org.ambientlight.room;

import org.ambientlight.config.device.led.StripePartConfiguration;
import org.ambientlight.device.led.Stripe;

public class StripePart {
	public StripePartConfiguration configuration;
	public Stripe stripe;
	
	public void setPixelData(int position, int rgbData) {
		int positionInStripe = configuration.offsetInStripe + position;
		stripe.setPixel(positionInStripe, rgbData);
	}
}
