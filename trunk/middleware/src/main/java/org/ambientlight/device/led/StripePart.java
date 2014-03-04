package org.ambientlight.device.led;

import org.ambientlight.config.device.led.StripePartConfiguration;

public class StripePart {
	public StripePartConfiguration configuration;
	public Stripe stripe;
	
	public void setPixelData(int position, int rgbData) {
		int positionInStripe = configuration.offsetInStripe + position;
		stripe.setPixel(positionInStripe, rgbData);
	}
}
