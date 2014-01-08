package org.ambientlight.room.entities.lightobject.programms;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.ambientlight.room.entities.lightobject.LightObject;

public class SimpleColor extends RenderingProgramm {

	boolean hasDirtyRegion = true;
	
	Color color;
	
	public SimpleColor(Color color) {
		this.color = color;
	}

	@Override
	public BufferedImage renderLightObject(LightObject lightObject) {
		Color actualColor = new Color(lightObject.getPixelMap().getRGB(0, 0));
		
		if(actualColor.equals(this.color) == false){
			
			this.hasDirtyRegion=true;
			
			BufferedImage result = lightObject.getPixelMap();
			Graphics2D    graphics = result.createGraphics();
			graphics.setPaint (color);
			graphics.fillRect ( 0, 0, result.getWidth(), result.getHeight() );

			return result;
		}
		else{
			
			this.hasDirtyRegion=false;
			
			return(lightObject.getPixelMap());
		}
	}

	@Override
	public boolean hasDirtyRegion() {
		return this.hasDirtyRegion;
	}
}
