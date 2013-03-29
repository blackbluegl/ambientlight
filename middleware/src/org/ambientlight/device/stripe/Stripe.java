package org.ambientlight.device.stripe;

import java.util.ArrayList;
import java.util.List;

import org.ambientlight.device.stripe.configuration.StripeConfiguration;
import org.ambientlight.scenery.entities.StripePart;

public class Stripe {
	
	public StripeConfiguration configuration;
	List<Integer> pixels;
	List<StripePart> subStripes;
	
	
	public Stripe(StripeConfiguration configuration){
		this.configuration=configuration;
		this.clear();
	}
	
	
	public List<StripePart> getStripeParts() {
		return subStripes;
	}

	
	public void setStripeParts(List<StripePart> subStripes) {
		this.subStripes = subStripes;
	}
	
	
	public void setPixel(int position, int rgbValue){
		this.pixels.set(position, rgbValue);
	}
	
	
	public List<Integer> getOutputResult(){
		return pixels;
	}
	
	
	public void clear(){
		this.pixels = new ArrayList<Integer>(configuration.pixelAmount);
		for(int i=0;i<configuration.pixelAmount;i++){
			pixels.add(0);
		}
	}
}
