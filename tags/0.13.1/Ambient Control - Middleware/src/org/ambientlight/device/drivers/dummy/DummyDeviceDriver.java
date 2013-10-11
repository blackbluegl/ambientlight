package org.ambientlight.device.drivers.dummy;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.ambientlight.debug.BufferedImageDisplayOutput;
import org.ambientlight.device.drivers.LedStripeDeviceDriver;
import org.ambientlight.device.drivers.RemoteHostConfiguration;
import org.ambientlight.device.led.Stripe;

public class DummyDeviceDriver implements LedStripeDeviceDriver {

	List<Stripe> myStripes = new ArrayList<Stripe>();
	BufferedImageDisplayOutput display;
	BufferedImage stripeContet;


	public DummyDeviceDriver() {
	}

	@Override
	public List<Stripe> getAllStripes() {
		return this.myStripes;
	}

	@Override
	public void attachStripe(Stripe stripe) {

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
		display.dispose();
		display = null;
	}

	@Override
	public void writeData() {
		for(int i= 0;i<this.myStripes.size();i++){
			Stripe currentStripe = this.myStripes.get(i);
			for(int y=0;y<currentStripe.configuration.pixelAmount;y++){
				this.stripeContet.setRGB(y * 3 + 0, i * 3 + 0, currentStripe.getOutputResult().get(y).getRGB());
				this.stripeContet.setRGB(y * 3 + 1, i * 3 + 0, currentStripe.getOutputResult().get(y).getRGB());
				this.stripeContet.setRGB(y * 3 + 2, i * 3 + 0, currentStripe.getOutputResult().get(y).getRGB());
				this.stripeContet.setRGB(y * 3 + 0, i * 3 + 1, currentStripe.getOutputResult().get(y).getRGB());
				this.stripeContet.setRGB(y * 3 + 1, i * 3 + 1, currentStripe.getOutputResult().get(y).getRGB());
				this.stripeContet.setRGB(y * 3 + 2, i * 3 + 1, currentStripe.getOutputResult().get(y).getRGB());
				this.stripeContet.setRGB(y * 3 + 0, i * 3 + 2, currentStripe.getOutputResult().get(y).getRGB());
				this.stripeContet.setRGB(y * 3 + 1, i * 3 + 2, currentStripe.getOutputResult().get(y).getRGB());
				this.stripeContet.setRGB(y * 3 + 2, i * 3 + 2, currentStripe.getOutputResult().get(y).getRGB());
			}
		}
		display.setImageContent(this.stripeContet);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ambientlight.device.drivers.LedStripeDeviceDriver#setConfiguration
	 * (org.ambientlight.device.drivers.RemoteHostConfiguration)
	 */
	@Override
	public void setConfiguration(RemoteHostConfiguration configuration) {
		// TODO Auto-generated method stub

	}


}
