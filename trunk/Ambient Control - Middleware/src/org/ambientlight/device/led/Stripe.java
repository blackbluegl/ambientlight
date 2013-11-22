package org.ambientlight.device.led;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.config.device.led.StripeConfiguration;
import org.ambientlight.device.led.color.Color64Bit;
import org.ambientlight.device.led.color.DitheringRGB;
import org.ambientlight.room.StripePart;


public class Stripe {

	public StripeConfiguration configuration;
	List<Color64Bit> pixels;
	List<StripePart> subStripes;

	private DitheringRGB dithering;


	public Stripe(StripeConfiguration configuration) {
		this.configuration = configuration;
		this.dithering = new DitheringRGB();
		this.clear();
	}


	public List<StripePart> getStripeParts() {
		return subStripes;
	}


	public void setStripeParts(List<StripePart> subStripes) {
		this.subStripes = subStripes;
	}


	public void setPixel(int position, int rgbValue) {
		this.pixels.set(position, new Color64Bit(new Color(rgbValue), configuration.colorConfiguration));
	}


	public List<Color> getOutputResult() {
		return dithering.getDitheredRGB(pixels);
	}


	public void clear() {
		this.pixels = new ArrayList<Color64Bit>(configuration.pixelAmount);
		for (int i = 0; i < configuration.pixelAmount; i++) {
			pixels.add(new Color64Bit(Color.BLACK, configuration.colorConfiguration));
		}
	}
}
