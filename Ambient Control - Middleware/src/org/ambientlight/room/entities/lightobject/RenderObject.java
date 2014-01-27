package org.ambientlight.room.entities.lightobject;

import java.awt.image.BufferedImage;
import java.util.List;

import org.ambientlight.device.led.StripePart;


public class RenderObject {

	public LightObject lightObject;
	public List<StripePart> stripeParts;
	BufferedImage pixelMap;
	BufferedImage pixelMapAfterEffect;


	public RenderObject(LightObject lightObject, List<StripePart> stripesParts) {
		this.lightObject = lightObject;
		this.stripeParts = stripesParts;
		this.pixelMap = new BufferedImage(lightObject.width, lightObject.height, BufferedImage.TYPE_INT_ARGB);
		this.pixelMapAfterEffect = new BufferedImage(lightObject.width, lightObject.height, BufferedImage.TYPE_INT_ARGB);
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


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lightObject == null) ? 0 : lightObject.hashCode());
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
		RenderObject other = (RenderObject) obj;
		if (lightObject == null) {
			if (other.lightObject != null)
				return false;
		} else if (!lightObject.equals(other.lightObject))
			return false;
		return true;
	}

}