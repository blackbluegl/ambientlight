package org.ambientlight.rendering.util;

import org.ambientlight.room.StripePart;

public class StripePixelMapping {

	public StripePixelMapping(int xPosition, int yPosition, int stripeModellPosition, StripePart stripeModell) {
		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.stripePartPosition = stripeModellPosition;
		this.stripePart = stripeModell;
	}

	public int xPosition;
	public int yPosition;
	public int stripePartPosition;
	
	public StripePart stripePart;
}
