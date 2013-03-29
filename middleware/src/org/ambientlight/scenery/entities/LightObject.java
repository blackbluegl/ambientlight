package org.ambientlight.scenery.entities;

import java.awt.image.BufferedImage;
import java.util.List;

import org.ambientlight.scenery.entities.configuration.LightObjectConfiguration;


public class LightObject {

	
	private LightObjectConfiguration configuration;
	public List<StripePart> stripeParts;
	BufferedImage pixelMap;
	BufferedImage pixelMapAfterEffect;
	

	public LightObject(LightObjectConfiguration configuration, List<StripePart> stripesParts) {
		this.configuration = configuration;
		this.stripeParts= stripesParts;
		this.pixelMap = new BufferedImage(configuration.width, configuration.height, BufferedImage.TYPE_INT_ARGB);
		this.pixelMapAfterEffect = new BufferedImage(configuration.width, configuration.height, BufferedImage.TYPE_INT_ARGB);

	}

	
	public BufferedImage getPixelMapAfterEffect() {
		return pixelMapAfterEffect;
	}

	
	public void setPixelMapAfterEffect(BufferedImage pixelMapAfterEffect) {
		this.pixelMapAfterEffect = pixelMapAfterEffect;
	}

	
	public BufferedImage getPixelMap() {
		return pixelMap;
	}


	public void setPixelMap(BufferedImage pixelMap) {
		this.pixelMap = pixelMap;
	}

	
	public synchronized LightObjectConfiguration getConfiguration() {
		return configuration;
	}

	
	public synchronized void setConfiguration(LightObjectConfiguration configuration) {
		this.configuration = configuration;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((configuration == null) ? 0 : configuration.hashCode());
		return result;
	}

	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LightObject other = (LightObject) obj;
		if (configuration == null) {
			if (other.configuration != null)
				return false;
		} else if (!configuration.equals(other.configuration))
			return false;
		return true;
	}
	
	
}