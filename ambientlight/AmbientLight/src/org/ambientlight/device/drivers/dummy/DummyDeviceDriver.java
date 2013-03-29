package org.ambientlight.device.drivers.dummy;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.debug.BufferedImageDisplayOutput;
import org.ambientlight.device.drivers.DeviceDriver;
import org.ambientlight.device.drivers.configuration.DeviceConfiguration;
import org.ambientlight.device.stripe.Stripe;

public class DummyDeviceDriver implements DeviceDriver {

	List<Stripe> myStripes = new ArrayList<Stripe>();
	BufferedImage stripeContet;
	BufferedImageDisplayOutput display;
	
	
	public DummyDeviceDriver() {
	}
	
	@Override
	public List<Stripe> getAllStripes() {
		return this.myStripes;
	}

	@Override
	public void attachStripe(Stripe stripe, int port) {
		
		this.myStripes.add(stripe);
		
		if(this.stripeContet== null || this.stripeContet.getWidth()/3<stripe.configuration.pixelAmount){
			this.stripeContet = new BufferedImage(stripe.configuration.pixelAmount*3, myStripes.size()*3, BufferedImage.TYPE_INT_RGB);
		}
	}

	@Override
	public void connect() {
		 display = new BufferedImageDisplayOutput(400,50, "DummyDeviceDriver");
	}

	@Override
	public void closeConnection() {
		//do nothing
	}

	@Override
	public void writeData() {
		for(int i= 0;i<this.myStripes.size();i++){
			Stripe currentStripe = this.myStripes.get(i);
			for(int y=0;y<currentStripe.configuration.pixelAmount;y++){
				this.stripeContet.setRGB(y*3+0, i*3+0, currentStripe.getOutputResult().get(y));	
				this.stripeContet.setRGB(y*3+1, i*3+0, currentStripe.getOutputResult().get(y));	
				this.stripeContet.setRGB(y*3+2, i*3+0, currentStripe.getOutputResult().get(y));	
				this.stripeContet.setRGB(y*3+0, i*3+1, currentStripe.getOutputResult().get(y));	
				this.stripeContet.setRGB(y*3+1, i*3+1, currentStripe.getOutputResult().get(y));	
				this.stripeContet.setRGB(y*3+2, i*3+1, currentStripe.getOutputResult().get(y));	
				this.stripeContet.setRGB(y*3+0, i*3+2, currentStripe.getOutputResult().get(y));	
				this.stripeContet.setRGB(y*3+1, i*3+2, currentStripe.getOutputResult().get(y));	
				this.stripeContet.setRGB(y*3+2, i*3+2, currentStripe.getOutputResult().get(y));	
			}
		}
		display.setImageContent(this.stripeContet);
	}

	@Override
	public void setConfiguration(DeviceConfiguration configuration) {
	}

}
