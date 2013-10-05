package org.ambientlight.device.led;

import java.util.ArrayList;
import java.util.List;

import org.ambientlight.device.led.color.DitheringRGB;
import org.ambientlight.room.StripePart;


public class Stripe {

	public StripeConfiguration configuration;
	List<Integer> pixels;
	List<StripePart> subStripes;

	private DitheringRGB dithering;


	public Stripe(StripeConfiguration configuration) {
		this.configuration = configuration;
		this.dithering = new DitheringRGB(configuration.gammaRed, configuration.gammaGreen, configuration.gammaBlue);
		this.clear();
	}


	public List<StripePart> getStripeParts() {
		return subStripes;
	}


	public void setStripeParts(List<StripePart> subStripes) {
		this.subStripes = subStripes;
	}


	public void setPixel(int position, int rgbValue) {
		this.pixels.set(position, rgbValue);
	}


	public List<Integer> getOutputResult() {
		return dithering.getDitheredRGB(pixels);
	}


	public void clear() {
		this.pixels = new ArrayList<Integer>(configuration.pixelAmount);
		for (int i = 0; i < configuration.pixelAmount; i++) {
			pixels.add(0);
		}
	}
}
