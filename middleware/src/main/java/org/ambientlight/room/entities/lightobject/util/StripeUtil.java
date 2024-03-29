package org.ambientlight.room.entities.lightobject.util;

import java.util.ArrayList;
import java.util.List;

import org.ambientlight.device.led.StripePart;
import org.ambientlight.room.entities.lightobject.LightObject;


public class StripeUtil {

	public static List<StripePixelMapping> getStripePixelMapping(List<StripePart> stripeParts) {

		List<StripePixelMapping> result = new ArrayList<StripePixelMapping>();

		for (StripePart current : stripeParts) {
			try {
				result.addAll(getStripePixelMapping(current));
			} catch (Exception e) {
				System.out.println("error while trying to map pixels at position: " + stripeParts.indexOf(current));
				throw e;
			}
		}

		return result;
	}


	public static List<StripePixelMapping> getStripePixelMapping(StripePart stripePart) {

		List<StripePixelMapping> result = new ArrayList<StripePixelMapping>();

		if (stripePart.configuration.pixelAmount == 1) {
			result.add(new StripePixelMapping(stripePart.configuration.startXPositionInRoom,
					stripePart.configuration.startYPositionInRoom, 0, stripePart));

		} else {

			Float pixelDifferenceX = (float) ((stripePart.configuration.endXPositionInRoom - stripePart.configuration.startXPositionInRoom) / (stripePart.configuration.pixelAmount - 1));
			Float pixelDifferenceY = (float) ((stripePart.configuration.endYPositionInRoom - stripePart.configuration.startYPositionInRoom) / (stripePart.configuration.pixelAmount - 1));

			for (int i = 0; i < stripePart.configuration.pixelAmount; i++) {
				float xPosition = stripePart.configuration.startXPositionInRoom + (i * pixelDifferenceX);
				float yPosition = stripePart.configuration.startYPositionInRoom + (i * pixelDifferenceY);
				result.add(new StripePixelMapping((int) xPosition, (int) yPosition, i, stripePart));
			}
		}
		return result;
	}


	/**
	 * determine if a stripePart has pixels that are located within a lightObject.
	 * 
	 * @param stripePart
	 * @param lightObjectConfig
	 * @return true if any pixel of the stripe is within the lightObject
	 */
	public static boolean isStripePartInLightObject(StripePart stripePart, LightObject lightObjectConfig) {
		// first get all pixelPositions we will check each pixel afterwards
		List<StripePixelMapping> pixelMapping = getStripePixelMapping(stripePart);

		int xStart = lightObjectConfig.xOffsetInRoom;
		int yStart = lightObjectConfig.yOffsetInRoom;

		int xEnd = lightObjectConfig.xOffsetInRoom + lightObjectConfig.width;
		int yEnd = lightObjectConfig.yOffsetInRoom + lightObjectConfig.height;

		// if pixel is within lightObject the stripe is part of it
		for (StripePixelMapping currentPixel : pixelMapping) {
			if (currentPixel.xPosition >= xStart && currentPixel.xPosition <= xEnd && currentPixel.yPosition >= yStart
					&& currentPixel.yPosition <= yEnd)
				return true;
		}

		return false;
	}
}
