package org.ambientlight.scenery.entities;

import org.ambientlight.device.stripe.Stripe;
import org.ambientlight.device.stripe.StripePartConfiguration;

public class StripePart {
	public StripePartConfiguration configuration;
	public Stripe stripe;
	
	public void setPixelData(int position, int rgbData) {
		int positionInStripe = configuration.offsetInStripe + position;
		stripe.setPixel(positionInStripe, rgbData);
	}
}
